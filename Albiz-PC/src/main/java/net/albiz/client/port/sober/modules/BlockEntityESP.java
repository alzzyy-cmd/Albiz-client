// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.modules;

import net.albiz.client.port.sober.module.Category;
import net.albiz.client.port.sober.module.Module;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.setting.BoolSetting;
import net.albiz.client.port.sober.setting.ColorSetting;
import net.albiz.client.port.sober.setting.EnumSetting;
import net.albiz.client.port.sober.setting.IntSetting;
import net.albiz.client.port.sober.setting.Setting;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2806;
import net.minecraft.class_2818;

public class BlockEntityESP
extends Module {
    private static final Map<class_2591<?>, Group> GROUPS = new IdentityHashMap();
    private static final int MAX_FOUND = 4096;
    private final Setting<ShapeMode> shape = this.add(new EnumSetting<ShapeMode>("Shape", "Lines draws the edges, Sides fills the box, Both does both.", ShapeMode.Both));
    private final Setting<Integer> range = this.add(new IntSetting("Range", "Block entities closer than this many blocks get a box.", 128, 16, 256));
    private final Setting<Boolean> tracers = this.add(new BoolSetting("Tracers", "Lines from your crosshair to every box.", false));
    private final Setting<Boolean>[] shown;
    private final Setting<Color>[] colors;
    private final List<Found> found;
    private int rescan;

    private static void put(Group group, class_2591<?> ... types) {
        for (class_2591<?> type : types) {
            GROUPS.put(type, group);
        }
    }

    public BlockEntityESP() {
        super("Block Entity ESP", "Draws boxes around chests, furnaces, signs and every other block entity near you.", Category.BASE_FINDING);
        this.group("Show");
        this.shown = new Setting[Group.values().length];
        this.colors = new Setting[Group.values().length];
        for (Group group : Group.values()) {
            Setting<Boolean> on = this.add(new BoolSetting(group.label, "Boxes around " + group.what + ".", group.on));
            this.shown[group.ordinal()] = on;
            this.colors[group.ordinal()] = this.add(new ColorSetting(group.label + " Color", "Color of the " + group.label.toLowerCase(Locale.ROOT) + " boxes.", Color.fromPacked(0xFF000000 | group.color))).visible(on::get);
        }
        this.found = new ArrayList<Found>();
    }

    @Override
    public void onEnable() {
        this.found.clear();
        this.rescan = 0;
    }

    @Override
    public void onDisable() {
        this.found.clear();
    }

    @Override
    public void onGameLeft() {
        this.found.clear();
    }

    @Override
    public void onTick() {
        if (BlockEntityESP.mc.field_1687 == null || BlockEntityESP.mc.field_1724 == null || --this.rescan > 0) {
            return;
        }
        this.rescan = net.albiz.client.platform.PlatformInfo.isMobile()?20:10;
        this.found.clear();
        double max = (double)this.range.get().intValue() * (double)this.range.get().intValue();
        int radius = Math.min((int)Math.ceil((double)this.range.get().intValue() / 16.0) + 1, BlockEntityESP.mc.field_1690.method_38521() + 1);
        radius=Math.min(radius,net.albiz.client.platform.PlatformInfo.isMobile()?6:20);
        class_1923 center = BlockEntityESP.mc.field_1724.method_31476();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                class_2818 chunk = BlockEntityESP.mc.field_1687.method_2935().method_2857(center.field_9181 + dx, center.field_9180 + dz, class_2806.field_12803, false);
                if (chunk == null) continue;
                for (class_2586 entity : chunk.method_12214().values()) {
                    class_2338 pos;
                    Group group = GROUPS.get(entity.method_11017());
                    if (group == null || !this.shown[group.ordinal()].get().booleanValue() || BlockEntityESP.mc.field_1724.method_5649((double)(pos = entity.method_11016()).method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5) > max) continue;
                    this.found.add(new Found(pos.method_10062(), group));
                    if (this.found.size() < (net.albiz.client.platform.PlatformInfo.isMobile()?512:4096)) continue;
                    return;
                }
            }
        }
    }

    @Override
    public void onRender(Render3D renderer) {
        ShapeMode mode = this.shape.get();
        boolean lines = this.tracers.get();
        boolean beam=net.albiz.client.module.ModuleRegistry.all().stream().anyMatch(m->m.id().equals("spawner-beam")&&m.isEnabled());
        for (Found entry : this.found) {
            if(beam && BlockEntityESP.mc.field_1687.method_8320(entry.pos).method_27852(net.minecraft.class_2246.field_10260))continue;
            Color color = this.colors[entry.group().ordinal()].get();
            class_2338 pos = entry.pos();
            renderer.box(pos, color, color, mode);
            if (!lines) continue;
            renderer.tracer((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5, color, 1.0f, false);
        }
    }

    @Override
    public String getInfoString() {
        return this.found.isEmpty() ? null : String.valueOf(this.found.size());
    }

    static {
        BlockEntityESP.put(Group.STORAGE, class_2591.field_11914, class_2591.field_11891, class_2591.field_16411, class_2591.field_11896, class_2591.field_11901);
        BlockEntityESP.put(Group.UTILITY, class_2591.field_11888, class_2591.field_11899, class_2591.field_11887, class_2591.field_46808);
        BlockEntityESP.put(Group.FURNACE, class_2591.field_11903, class_2591.field_16415, class_2591.field_16414);
        BlockEntityESP.put(Group.SIGN, class_2591.field_11911, class_2591.field_40330);
        BlockEntityESP.put(Group.BED, class_2591.field_11910);
        BlockEntityESP.put(Group.BANNER, class_2591.field_11905);
        BlockEntityESP.put(Group.HEAD, class_2591.field_11913);
        BlockEntityESP.put(Group.WORKSHOP, class_2591.field_11894, class_2591.field_11912, class_2591.field_11890, class_2591.field_11902, class_2591.field_16412, class_2591.field_11907, class_2591.field_17380, class_2591.field_42781, class_2591.field_40329, class_2591.field_11908, class_2591.field_11900, class_2591.field_61437);
        BlockEntityESP.put(Group.NATURAL, class_2591.field_11889, class_2591.field_47352, class_2591.field_48859, class_2591.field_20431, class_2591.field_16413, class_2591.field_42780, class_2591.field_28117, class_2591.field_37648, class_2591.field_37647, class_2591.field_43258, class_2591.field_11906, class_2591.field_11898, class_2591.field_11897);
    }

    private static enum Group {
        STORAGE("Storage", "chests, trapped chests, barrels, shulker boxes and ender chests", 0xFFAA00, true),
        UTILITY("Hoppers & Droppers", "hoppers, droppers, dispensers and crafters", 0x9E9E9E, true),
        FURNACE("Furnaces", "furnaces, blast furnaces and smokers", 16740419, true),
        SIGN("Signs", "signs and hanging signs", 14136183, true),
        BED("Beds", "beds", 15022389, true),
        BANNER("Banners", "banners", 4367861, true),
        HEAD("Heads", "player and mob heads", 11225020, true),
        WORKSHOP("Workshop", "brewing stands, enchanting tables, beacons, lecterns, jukeboxes, campfires, pots, bookshelves, shelves and redstone parts", 2541274, true),
        NATURAL("Natural", "spawners, vaults, bee nests, bells, sculk and other blocks that also generate on their own", 0x66BB6A, false);

        final String label;
        final String what;
        final int color;
        final boolean on;

        private Group(String label, String what, int color, boolean on) {
            this.label = label;
            this.what = what;
            this.color = color;
            this.on = on;
        }
    }

    private record Found(class_2338 pos, Group group) {
    }
}

