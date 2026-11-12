// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.setting.StringSetting;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_9334;

public class CustomTotems
extends Module
implements Presets {
    private static final class_2960 VANILLA = class_2960.method_60656((String)"totem_of_undying");
    private static final Style[] STYLES = new Style[]{new Style("shadow", "Shadow", 0x2C2C2C), new Style("frost_ghost", "Frost Ghost", 11392734), new Style("xkisses_pink_blue", "Xkisses Pink Blue", 0xACAAAE), new Style("blue_eyes", "Blue Eyes", 11765331), new Style("stone_statue", "Stone Statue", 0x535557), new Style("mini", "Mini", 12355404), new Style("violet_eyes", "Violet Eyes", 11895378), new Style("red_eyes", "Red Eyes", 12091975), new Style("gentleman", "Gentleman", 7759177), new Style("void", "Void", 0x3C3C3C), new Style("silver", "Silver", 0x7E7E7E), new Style("ember", "Ember", 12289097), new Style("steve", "Steve", 8294020), new Style("pale_emerald", "Pale Emerald", 12757349), new Style("classic_dusk", "Classic Dusk", 11764809), new Style("classic_ash", "Classic Ash", 11830345), new Style("slime", "Slime", 4567138), new Style("amethyst_blob", "Amethyst Blob", 6303649), new Style("nightshade", "Nightshade", 3351115), new Style("ice", "Ice", 6458291), new Style("blaze", "Blaze", 10571550), new Style("lime", "Lime", 5211691), new Style("ender", "Ender", 2298932), new Style("rose", "Rose", 10173012), new Style("aqua", "Aqua", 2648975), new Style("coral", "Coral", 10376013), new Style("pearl", "Pearl", 8226705), new Style("emerald", "Emerald", 3376987), new Style("lavender", "Lavender", 7689377), new Style("ruby", "Ruby", 9514812), new Style("sapphire", "Sapphire", 4086177), new Style("gold", "Gold", 11175226), new Style("mint", "Mint", 5742984), new Style("bubblegum", "Bubblegum", 11887515), new Style("demon_eyes", "Demon Eyes", 2497569), new Style("snow", "Snow", 12306646), new Style("neon_lime", "Neon Lime", 7708982), new Style("copper", "Copper", 8870454), new Style("salmon", "Salmon", 10580586), new Style("navy", "Navy", 2240871), new Style("crimson", "Crimson", 7804704), new Style("lilac", "Lilac", 0x887AA8), new Style("peach", "Peach", 11627095), new Style("teal", "Teal", 3508103), new Style("jade", "Jade", 4882009), new Style("ghost", "Ghost", 14475235), new Style("sakura", "Sakura", 11958668), new Style("sky", "Sky", 6193818), new Style("inferno", "Inferno", 9455135), new Style("magenta", "Magenta", 7548564), new Style("glacier", "Glacier", 5336237), new Style("zombie", "Zombie", 5992321), new Style("frost", "Frost", 5212073), new Style("cherry", "Cherry", 13588334), new Style("snowman", "Snowman", 12697532), new Style("antlers", "Antlers", 9657216), new Style("gray_figure", "Gray Figure", 9473429), new Style("royal_violet", "Royal Violet", 11896911), new Style("purple_gaze", "Purple Gaze", 12223570), new Style("marble", "Marble", 0xA5A5A5), new Style("pale_watcher", "Pale Watcher", 0xC0BCBC), new Style("shade", "Shade", 0x3E3E3E), new Style("santa", "Santa", 10900553), new Style("pink_gaze", "Pink Gaze", 12091986), new Style("green_gaze", "Green Gaze", 13080397), new Style("statue", "Statue", 0x585858), new Style("batman", "Batman", 5789256), new Style("demon", "Demon", 6163225), new Style("redstone", "Redstone", 7738649), new Style("mint_gaze", "Mint Gaze", 12161870), new Style("rose_gaze", "Rose Gaze", 12223311), new Style("cat", "Cat", 8221806), new Style("pink_figure", "Pink Figure", 14455482), new Style("hellspawn", "Hellspawn", 6166560), new Style("sunset", "Sunset", 11961421), new Style("dark_figure", "Dark Figure", 0x535353), new Style("orchid", "Orchid", 12158287), new Style("classic_hd", "Classic HD", 0xBB8B55), new Style("crystal", "Crystal", 9995220), new Style("purple_hd", "Purple HD", 8743851), new Style("pink_ghost", "Pink Ghost", 12564160), new Style("tiny", "Tiny", 12159315), new Style("honey", "Honey", 12422484), new Style("skull", "Skull", 0x6E6E6E), new Style("polar", "Polar", 11318720), new Style("emerald_gaze", "Emerald Gaze", 11767136), new Style("monkey", "Monkey", 9129000), new Style("ocean_gaze", "Ocean Gaze", 11831378), new Style("pocket", "Pocket", 12159567), new Style("lilac_figure", "Lilac Figure", 8488609), new Style("bright_hd", "Bright HD", 12225875), new Style("blush", "Blush", 12225360), new Style("robot", "Robot", 0x535557), new Style("pink_robot", "Pink Robot", 0x696466), new Style("dark_statue", "Dark Statue", 0x5E5D5D), new Style("spirit", "Spirit", 0xC3C1C1), new Style("wisp", "Wisp", 0x929292)};
    private static volatile CustomTotems instance;
    public final Setting<String> style = this.add(new StringSetting("Style", "", "", true)).visible(() -> false);
    private final class_1799[] icons = new class_1799[STYLES.length + 1];
    private String cachedName;
    private class_2960 cachedModel;

    public CustomTotems() {
        super("Custom Totems", "Swaps the totem of undying for one of many totem styles, in your hand, your inventory and when a totem pops.", Category.VISUALS);
        instance = this;
    }

    private static class_2960 modelOf(String id) {
        return class_2960.method_60655((String)"albiz", (String)("totem/" + id));
    }

    private class_2960 current() {
        String name = this.style.get();
        if (!name.equals(this.cachedName)) {
            this.cachedName = name;
            this.cachedModel = null;
            for (Style s : STYLES) {
                if (!s.id().equals(name)) continue;
                this.cachedModel = CustomTotems.modelOf(s.id());
            }
        }
        return this.cachedModel;
    }

    public static class_2960 model(class_1799 stack, class_2960 model) {
        CustomTotems module = instance;
        if (!(module != null && module.isActive() && VANILLA.equals((Object)model) && stack.method_31574(class_1802.field_8288))) {
            return model;
        }
        class_2960 chosen = module.current();
        return chosen == null ? model : chosen;
    }

    @Override
    public int presetCount() {
        return STYLES.length + 1;
    }

    @Override
    public String presetName(int index) {
        return index == 0 ? "Default" : STYLES[index - 1].title();
    }

    @Override
    public int presetColor(int index) {
        return index == 0 ? -2054070 : 0xFF000000 | STYLES[index - 1].color();
    }

    @Override
    public boolean presetActive(int index) {
        String name = this.style.get();
        return index == 0 ? name.isEmpty() || this.current() == null : STYLES[index - 1].id().equals(name);
    }

    @Override
    public void selectPreset(int index) {
        this.style.set(index == 0 ? "" : STYLES[index - 1].id());
        if (index > 0 && !this.isActive()) {
            this.setEnabled(true);
        }
    }

    @Override
    public String presetDescription() {
        return "Shows this totem in your hand, in your inventory and in the pop animation.";
    }

    @Override
    public class_1799 presetIcon(int index) {
        if (index < 0 || index >= this.icons.length) {
            return class_1799.field_8037;
        }
        if (this.icons[index] == null) {
            class_1799 stack = new class_1799((class_1935)class_1802.field_8288);
            if (index > 0) {
                stack.method_57379(class_9334.field_54199, CustomTotems.modelOf(STYLES[index - 1].id()));
            }
            this.icons[index] = stack;
        }
        return this.icons[index];
    }

    @Override
    public boolean showInHud() {
        return false;
    }

    @Override
    public boolean bindable() {
        return false;
    }

    private record Style(String id, String title, int color) {
    }
}

