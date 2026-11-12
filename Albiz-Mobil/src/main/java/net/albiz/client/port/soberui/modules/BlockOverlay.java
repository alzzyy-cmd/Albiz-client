package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.modules.Sky;
import net.albiz.client.port.soberui.render.Color;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.ColorSetting;
import net.albiz.client.port.soberui.setting.EnumSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.sky.AuraRenderer;

public class BlockOverlay
extends Module
implements Presets {
    public final Setting<Style> style;
    public final Setting<Boolean> overlay;
    public final Setting<Boolean> matchSky;
    public final Setting<Boolean> rainbow;
    public final Setting<Color> color;
    public final Setting<Integer> pulse;
    public final Setting<Integer> width;
    public final Setting<Integer> fill;

    public BlockOverlay() {
        super("Block Overlay", "Custom outline and overlay on the block you look at. Turn it off for the normal Minecraft outline.", Category.VISUALS);
        this.group("Overlay");
        this.style = this.add(new EnumSetting<Style>("Style", "Look of the block overlay.", Style.Galaxy)).visible(() -> false);
        this.overlay = this.add(new BoolSetting("Block Overlay", "Custom overlay on the block you look at.", true));
        this.matchSky = this.add(new BoolSetting("Match Sky", "The outline takes the color of your sky.", true)).visible(this.overlay::get);
        this.rainbow = this.add(new BoolSetting("Rainbow", "Rainbow colored outline.", false)).visible(() -> this.overlay.get() != false && this.matchSky.get() == false);
        this.color = this.add(new ColorSetting("Color", "Color of the outline.", new Color(124, 247, 197, 255))).visible(() -> this.overlay.get() != false && this.matchSky.get() == false && this.rainbow.get() == false);
        this.pulse = this.add(new IntSetting("Pulse", "Pulse speed in percent.", 100, 0, 300)).visible(this.overlay::get);
        this.width = this.add(new IntSetting("Width", "Edge thickness.", 3, 1, 10)).visible(this.overlay::get);
        this.fill = this.add(new IntSetting("Fill", "Fill opacity of the classic style in percent.", 18, 0, 60)).visible(() -> this.overlay.get() != false && this.style.get() == Style.Classic);
        this.setEnabledSilently(true);
    }

    public int color(float seconds) {
        if (this.matchSky.get().booleanValue()) {
            int sky = SoberClient.MODULES.get(Sky.class).accentColor();
            if (sky >= 0) {
                return sky;
            }
            return this.color.get().packed() & 0xFFFFFF;
        }
        if (this.rainbow.get().booleanValue()) {
            return AuraRenderer.hsvToRgb(seconds * 0.12f % 1.0f, 0.8f, 1.0f);
        }
        return this.color.get().packed() & 0xFFFFFF;
    }

    @Override
    public int presetCount() {
        return Style.values().length;
    }

    @Override
    public String presetName(int index) {
        return Style.values()[index].title;
    }

    @Override
    public int presetColor(int index) {
        return 0xFF000000 | Style.values()[index].color.packed();
    }

    @Override
    public boolean presetActive(int index) {
        return this.isActive() && this.overlay.get() != false && this.style.get().ordinal() == index;
    }

    @Override
    public void selectPreset(int index) {
        this.style.set(Style.values()[index]);
        this.overlay.set(true);
        this.setEnabled(true);
    }

    @Override
    public String presetDescription() {
        return "Applies this block overlay style.";
    }

    @Override
    public boolean showInHud() {
        return false;
    }

    @Override
    public boolean bindable() {
        return false;
    }

    public static enum Style {
        Classic("Classic", new Color(124, 247, 197, 255)),
        Galaxy("Galaxy", new Color(155, 80, 232, 255)),
        Palm("Palm", new Color(95, 242, 228, 255)),
        Target("Target", new Color(176, 140, 240, 255)),
        Hologram("Hologram", new Color(79, 230, 255, 255)),
        Opal("Opal", new Color(242, 184, 255, 255)),
        Molten("Molten", new Color(255, 122, 31, 255));

        public final String title;
        public final Color color;

        private Style(String title, Color color) {
            this.title = title;
            this.color = color;
        }
    }
}

