// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.modules;

import net.albiz.client.port.sober.module.Category;
import net.albiz.client.port.sober.module.Module;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.setting.BoolSetting;
import net.albiz.client.port.sober.setting.ColorSetting;
import net.albiz.client.port.sober.setting.IntSetting;
import net.albiz.client.port.sober.setting.Setting;
import net.albiz.client.port.sober.util.ChunkScanner;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2680;
import net.minecraft.class_2818;

public class SpawnerBeam
extends Module {
    private final Setting<Color> color;
    private final Setting<Boolean> rainbow;
    private final Setting<Integer> height;
    private final Setting<Integer> width;
    private final Setting<Boolean> pulse;
    private final Setting<Boolean> outline;
    private final Setting<Boolean> throughWalls;
    private final Setting<Boolean> tracers;
    private final Setting<Boolean> notify;
    private final Map<class_2338, Long> spawners;
    private final ArrayDeque<class_1923> queue;
    private int perTick;
    private int ticks;
    private int pendingAlerts;
    private class_2338 pendingPos;

    public SpawnerBeam() {
        super("Spawner Beam", "Shoots a beacon like beam into the sky above every mob spawner.", Category.BASE_FINDING);
        this.group("Beam");
        this.color = this.add(new ColorSetting("Beam Color", "Color of the beam above spawners.", new Color(170, 110, 255, 255)));
        this.rainbow = this.add(new BoolSetting("Rainbow", "Rainbow colored beams.", false));
        this.height = this.add(new IntSetting("Beam Height", "Height of the beam in blocks.", 120, 10, 320));
        this.width = this.add(new IntSetting("Beam Width", "Thickness of the beam in tenths of a block.", 4, 1, 10));
        this.pulse = this.add(new BoolSetting("Pulse", "The beam slowly breathes.", true));
        this.outline = this.add(new BoolSetting("Outline", "Box around the spawner itself.", true));
        this.throughWalls = this.add(new BoolSetting("Through Walls", "Beams are visible through blocks.", true));
        this.tracers = this.add(new BoolSetting("Tracers", "Line from your crosshair to every spawner.", false));
        this.group("Alerts");
        this.notify = this.add(new BoolSetting("Notify", "Notification when a new spawner is found.", true));
        this.spawners = new HashMap<class_2338, Long>();
        this.queue = new ArrayDeque();
        this.perTick = 16;
    }

    @Override
    public void onEnable() {
        this.spawners.clear();
        this.ticks = 0;
        for (class_2818 chunk : ChunkScanner.loadedChunks()) {
            this.scan(chunk, false);
        }
    }

    @Override
    public void onDisable() {
        this.spawners.clear();
    }

    @Override
    public void onGameLeft() {
        this.spawners.clear();
        this.pendingAlerts = 0;
        this.pendingPos = null;
    }

    @Override
    public void onChunkLoad(class_2818 chunk) {
        this.scan(chunk, true);
    }

    @Override
    public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
        if (newState.method_27852(class_2246.field_10260)) {
            this.found(pos, true);
        } else if (oldState.method_27852(class_2246.field_10260)) {
            this.spawners.remove(pos);
        }
    }

    @Override
    public void onTick() {
        if (this.ticks % 60 == 0) {
            this.flushAlerts();
        }
        ++this.ticks;
        if (SpawnerBeam.mc.field_1724 == null || SpawnerBeam.mc.field_1687 == null) {
            return;
        }
        if (this.queue.isEmpty() && this.ticks % 100 == 0) {
            for (class_2818 chunk : ChunkScanner.loadedChunks()) {
                this.queue.add(chunk.method_12004());
            }
            this.perTick = net.albiz.client.platform.PlatformInfo.isMobile()?2:8;
        }
        for (int i = 0; i < this.perTick && !this.queue.isEmpty(); ++i) {
            class_1923 pos = this.queue.poll();
            if (!SpawnerBeam.mc.field_1687.method_2935().method_12123(pos.field_9181, pos.field_9180)) continue;
            this.scan(SpawnerBeam.mc.field_1687.method_8497(pos.field_9181, pos.field_9180), true);
        }
        if (this.ticks % 40 != 0) {
            return;
        }
        class_1923 center = SpawnerBeam.mc.field_1724.method_31476();
        int range = SpawnerBeam.mc.field_1690.method_38521() + 2;
        Iterator<class_2338> it = this.spawners.keySet().iterator();
        while (it.hasNext()) {
            class_2338 pos = it.next();
            if (Math.abs((pos.method_10263() >> 4) - center.field_9181) > range || Math.abs((pos.method_10260() >> 4) - center.field_9180) > range) {
                it.remove();
                continue;
            }
            if (!SpawnerBeam.mc.field_1687.method_8393(pos.method_10263() >> 4, pos.method_10260() >> 4) || SpawnerBeam.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10260)) continue;
            it.remove();
        }
    }

    private void scan(class_2818 chunk, boolean alert) {
        for (class_2586 blockEntity : chunk.method_12214().values()) {
            if (!(blockEntity instanceof class_2636)) continue;
            this.found(blockEntity.method_11016().method_10062(), alert);
        }
    }

    private void found(class_2338 pos, boolean alert) {
        if (this.spawners.putIfAbsent(pos, System.currentTimeMillis()) != null) {
            return;
        }
        if (!alert || !this.notify.get().booleanValue()) {
            return;
        }
        ++this.pendingAlerts;
        if (this.pendingPos == null || SpawnerBeam.mc.field_1724 != null && SpawnerBeam.mc.field_1724.method_5707(pos.method_46558()) < SpawnerBeam.mc.field_1724.method_5707(this.pendingPos.method_46558())) {
            this.pendingPos = pos;
        }
    }

    private void flushAlerts() {
        if (this.pendingAlerts == 0 || this.pendingPos == null) {
            return;
        }
        if (this.pendingAlerts == 1) {
            this.info("Spawner found at %d, %d, %d", this.pendingPos.method_10263(), this.pendingPos.method_10264(), this.pendingPos.method_10260());
        } else {
            this.info("%d spawners found, closest at %d, %d, %d", this.pendingAlerts, this.pendingPos.method_10263(), this.pendingPos.method_10264(), this.pendingPos.method_10260());
        }
        this.pendingAlerts = 0;
        this.pendingPos = null;
    }

    @Override
    public void onRender(Render3D renderer) {
        if (this.spawners.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        renderer.setDepthTest(this.throughWalls.get() == false);
        for (Map.Entry<class_2338, Long> entry : this.spawners.entrySet()) {
            class_2338 pos = entry.getKey();
            double appear = Math.min(1.0, (double)(now - entry.getValue()) / 900.0);
            appear = 1.0 - Math.pow(1.0 - appear, 3.0);
            double phase = ((double)pos.method_10263() * 0.37 + (double)pos.method_10260() * 0.61) % (Math.PI * 2);
            double wave = this.pulse.get() != false ? 0.5 + 0.5 * Math.sin((double)now / 1000.0 * Math.PI * 2.0 / 2.4 + phase) : 0.5;
            Color base = this.rainbow.get() != false ? Color.rainbow(phase / (Math.PI * 2), 0.12, 0.75f, 1.0f, 255) : this.color.get().withAlpha(255);
            double cx = (double)pos.method_10263() + 0.5;
            double cz = (double)pos.method_10260() + 0.5;
            double bottom = pos.method_10264() + 1;
            double top = bottom + (double)this.height.get().intValue() * appear;
            double radius = (double)this.width.get().intValue() / 20.0 * (0.9 + 0.1 * wave);
            renderer.beam(cx, cz, bottom, top, radius, base.scaleAlpha(appear), 0.75 + 0.25 * wave);
            if (this.outline.get().booleanValue()) {
                renderer.boxSides(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1, base.scaleAlpha(0.14 * appear));
                renderer.setLineWidth(2.0f + (float)wave);
                renderer.boxLines(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1, base.scaleAlpha(appear));
            }
            if (!this.tracers.get().booleanValue()) continue;
            renderer.tracer(cx, (double)pos.method_10264() + 0.5, cz, base.scaleAlpha(0.8 * appear));
        }
        renderer.setLineWidth(2.0f);
        renderer.setDepthTest(false);
    }

    @Override
    public String getInfoString() {
        return this.spawners.isEmpty() ? null : String.valueOf(this.spawners.size());
    }
}

