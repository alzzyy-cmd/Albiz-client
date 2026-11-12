// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.modules;

import net.albiz.client.port.sober.gui.Notifications;
import net.albiz.client.port.sober.module.Category;
import net.albiz.client.port.sober.module.Module;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.setting.BoolSetting;
import net.albiz.client.port.sober.setting.ColorSetting;
import net.albiz.client.port.sober.setting.IntSetting;
import net.albiz.client.port.sober.setting.Setting;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2902;

public class ChunkFinder
extends Module {
    private static final int SETTLE_TICKS = 200;
    private static final int PER_TICK = 8;
    private static final double BASE_Y = 55.0;
    private final Setting<Boolean> fullOnly=this.add(new BoolSetting("Full Hives Only","Only honey level five; disabling includes any received nest or hive, which is common naturally.",true));
    private final Setting<Integer> minRadius=this.add(new IntSetting("Inner Radius","Skip nearby chunks; ring scan inspired by the Larp V6 behavior.",0,0,16));
    private final Setting<Integer> maxRadius=this.add(new IntSetting("Outer Radius","Only loaded chunks are inspected; never requests hidden chunks.",12,1,32));
    private final Setting<Integer> mergeRadius = this.add(new IntSetting("Merge Radius", "Hives within this many chunks of each other merge to one middle flag", 4, 1, 8));
    private final Setting<Boolean> notify = this.add(new BoolSetting("Notifications", "Pop a notification for every new chunk", true));
    private final Setting<Boolean> sound = this.add(new BoolSetting("Discovery Sound", "Play the supplied wolf recording on a new find; shared eight-second cooldown.", true));
    private final Setting<Boolean> logo = this.add(new BoolSetting("Albiz Logo", "Replace the square marker with the Albiz wolf logo.", true));
    private final Setting<Boolean> rotateLogo = this.add(new BoolSetting("Rotate Logo", "Animate the Albiz discovery marker.", true));
    private final Setting<Boolean> beam = this.add(new BoolSetting("Beam", "Light beam above every flag", false));
    private final Setting<Color> color = this.add(new ColorSetting("Color", "Color of the flagged chunks.", new Color(255, 178, 36, 255)));
    private final LongSet hits = new LongOpenHashSet();
    private final LongSet announced = new LongOpenHashSet();
    private final Long2IntOpenHashMap seen = new Long2IntOpenHashMap();
    private final Long2LongOpenHashMap appeared = new Long2LongOpenHashMap();
    private LongSet flags = LongSet.of();
    private int cursor;
    private int ticks;
    private boolean dirty;
    private int builtRadius;

    public ChunkFinder() {
        super("Hive Finder", "Highlights received chunks containing full beehives; a heuristic, not proof of a base.", Category.BASE_FINDING);
        
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.reset());
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    @Override
    public String getInfoString() {
        return this.flags.isEmpty() ? null : String.valueOf(this.flags.size());
    }

    @Override
    public void onTick() {
        if (ChunkFinder.mc.field_1687 == null || ChunkFinder.mc.field_1724 == null) {
            return;
        }
        ++this.ticks;
        int view = Math.min(this.maxRadius.get(),Math.min(net.albiz.client.platform.PlatformInfo.isMobile()?8:24,(Integer)ChunkFinder.mc.field_1690.method_42503().method_41753()));
        class_1923 center = ChunkFinder.mc.field_1724.method_31476();
        int width = view * 2 + 1;
        int area = width * width;
        if (area <= 0) {
            return;
        }
        for (int i = 0; i < (net.albiz.client.platform.PlatformInfo.isMobile()?2:8); ++i) {
            boolean settled;
            int index = this.cursor % area;
            this.cursor = (this.cursor + 1) % area;
            int x = center.field_9181 + index % width - view;
            int z = center.field_9180 + index / width - view;
            class_2818 chunk = ChunkFinder.mc.field_1687.method_2935().method_12126(x, z, false);
            if (chunk == null || chunk.method_12223() || Math.max(Math.abs(x-center.field_9181),Math.abs(z-center.field_9180))<Math.min(view,this.minRadius.get())) continue;
            long key2 = class_1923.method_8331((int)x, (int)z);
            this.seen.putIfAbsent(key2, this.ticks);
            boolean bl = settled = this.ticks - this.seen.get(key2) >= 200;
            if (settled && this.hasFullHive(chunk)) {
                if (this.hits.add(key2)) {
                    this.dirty = true;
                }
                if (!this.announced.add(key2)) continue;
                this.announce(x, z);
                continue;
            }
            if (!this.hits.remove(key2)) continue;
            this.dirty = true;
        }
        if (this.hits.removeIf(key -> ChunkFinder.outside(key, center, view))) {
            this.dirty = true;
        }
        this.seen.keySet().removeIf(key -> ChunkFinder.outside(key, center, view));
        this.announced.removeIf(key -> ChunkFinder.outside(key, center, view));
        if (this.dirty || this.mergeRadius.get() != this.builtRadius) {
            this.rebuild();
        }
    }

    private void rebuild() {
        this.dirty = false;
        this.builtRadius = this.mergeRadius.get();
        if (this.hits.size() < 2) {
            this.flags = new LongOpenHashSet((LongCollection)this.hits);
            return;
        }
        int radius = Math.max(1, this.builtRadius);
        LongOpenHashSet out = new LongOpenHashSet();
        LongOpenHashSet visited = new LongOpenHashSet();
        LongIterator longIterator = this.hits.iterator();
        while (longIterator.hasNext()) {
            long start = (Long)longIterator.next();
            if (!visited.add(start)) continue;
            LongArrayList group = new LongArrayList();
            LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
            queue.enqueue(start);
            while (!queue.isEmpty()) {
                long key = queue.dequeueLong();
                group.add(key);
                int cx = class_1923.method_8325((long)key);
                int cz = class_1923.method_8332((long)key);
                for (int dx = -radius; dx <= radius; ++dx) {
                    for (int dz = -radius; dz <= radius; ++dz) {
                        long next;
                        if (dx == 0 && dz == 0 || !this.hits.contains(next = class_1923.method_8331((int)(cx + dx), (int)(cz + dz))) || !visited.add(next)) continue;
                        queue.enqueue(next);
                    }
                }
            }
            out.add(group.size() == 1 ? group.getLong(0) : ChunkFinder.middle(group));
        }
        this.flags = out;
    }

    private static long middle(LongArrayList group) {
        long sx = 0L;
        long sz = 0L;
        for (int i = 0; i < group.size(); ++i) {
            sx += (long)class_1923.method_8325((long)group.getLong(i));
            sz += (long)class_1923.method_8332((long)group.getLong(i));
        }
        return class_1923.method_8331((int)((int)Math.round((double)sx / (double)group.size())), (int)((int)Math.round((double)sz / (double)group.size())));
    }

    private boolean hasFullHive(class_2818 chunk) {
        for (class_2826 section : chunk.method_12006()) {
            if (section == null || section.method_38292() || !section.method_12265().method_19526(this::fullHive)) continue;
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        if (!this.fullHive(section.method_12254(x, y, z))) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean fullHive(class_2680 state) {
        return (state.method_27852(class_2246.field_20422) || state.method_27852(class_2246.field_20421)) && (!this.fullOnly.get() || state.method_28498((class_2769)class_2741.field_20432) && (Integer)state.method_11654((class_2769)class_2741.field_20432) == 5);
    }

    private static boolean outside(long key, class_1923 center, int view) {
        return Math.abs(class_1923.method_8325((long)key) - center.field_9181) > view || Math.abs(class_1923.method_8332((long)key) - center.field_9180) > view;
    }

    private void announce(int x, int z) {
        if(this.sound.get()) net.albiz.client.module.impl.client.SoundFeedbackModule.chunkAlert();
        if (!this.notify.get().booleanValue()) {
            return;
        }
        Notifications.push("Chunk Finder", "X " + (x * 16 + 8) + "  Z " + (z * 16 + 8), Notifications.Type.INFO);
    }

    private void reset() {
        this.hits.clear();
        this.announced.clear();
        this.seen.clear();
        this.appeared.clear();
        this.flags = LongSet.of();
        this.cursor = 0;
        this.ticks = 0;
        this.dirty = false;
        this.builtRadius = this.mergeRadius.get();
    }

    @Override
    public void onRender(Render3D renderer) {
        LongSet current = this.flags;
        this.appeared.keySet().removeIf(key -> !current.contains(key));
        if (current.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Color base = this.color.get();
        double pulse = 0.82 + 0.18 * Math.sin((double)now / 420.0);
        double spin = (double)now / 2600.0;
        LongIterator longIterator = current.iterator();
        while (longIterator.hasNext()) {
            long key2 = (Long)longIterator.next();
            long since = this.appeared.computeIfAbsent(key2, k -> now);
            double fade = Math.min(1.0, (double)(now - since) / 350.0);
            double ease = 1.0 - Math.pow(1.0 - fade, 3.0);
            double x1 = (double)class_1923.method_8325((long)key2) * 16.0;
            double z1 = (double)class_1923.method_8332((long)key2) * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            double cx = x1 + 8.0;
            double cz = z1 + 8.0;
            double y = this.surface(55.0, cx, cz);
            if (!renderer.inView(x1, y - 1.0, z1, x2, y + 1.0, z2)) continue;
            renderer.setLineWidth(2.0f);
            if(this.logo.get()) net.albiz.client.util.LogoMarker.draw(renderer,cx,y+.03,cz,5.5*ease,this.rotateLogo.get()?spin:0,base.withAlpha((int)(255*ease)));
            else renderer.box(x1,y,z1,x2,y+.1,z2,base.withAlpha(25),base,net.albiz.client.port.sober.render.ShapeMode.Both);
            if (!this.beam.get().booleanValue()) continue;
            renderer.beam(cx, cz, y, y + 80.0, 0.28, base.withAlpha((int)(200.0 * ease)), pulse);
        }
        renderer.setLineWidth(2.0f);
    }

    private static void corner(Render3D renderer, double x, double z, double ax, double az, double y, Color color) {
        renderer.line(x, y, z, x + ax, y, z, color);
        renderer.line(x, y, z, x, y, z + az, color);
    }

    private static void hexagon(Render3D renderer, double cx, double cz, double y, double radius, double rotation, Color color) {
        if (radius <= 0.01) {
            return;
        }
        for (int i = 0; i < 6; ++i) {
            double a0 = rotation + 1.0471975511965976 * (double)i;
            double a1 = a0 + 1.0471975511965976;
            renderer.line(cx + Math.cos(a0) * radius, y, cz + Math.sin(a0) * radius, cx + Math.cos(a1) * radius, y, cz + Math.sin(a1) * radius, color);
        }
    }

    private double surface(double base, double x, double z) {
        int bz;
        if (ChunkFinder.mc.field_1687 == null) {
            return base;
        }
        int bx = (int)Math.floor(x);
        int top = ChunkFinder.mc.field_1687.method_8624(class_2902.class_2903.field_13202, bx, bz = (int)Math.floor(z));
        if (top <= ChunkFinder.mc.field_1687.method_31607()) {
            return base;
        }
        return ChunkFinder.mc.field_1687.method_8316(new class_2338(bx, top - 1, bz)).method_15769() ? base : Math.max(base, (double)top + 0.04);
    }

    public LongSet radarChunks() {
        return this.isActive() ? this.flags : LongSet.of();
    }

    public int radarColor() {
        return 0x99000000 | this.color.get().packed() & 0xFFFFFF;
    }
}

