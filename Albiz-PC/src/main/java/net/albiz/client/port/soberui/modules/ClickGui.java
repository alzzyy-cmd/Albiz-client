// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.ResetAll;
import net.albiz.client.port.soberui.gui.Fonts;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.render.Color;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.ColorSetting;
import net.albiz.client.port.soberui.setting.EnumSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.setting.StringSetting;

public class ClickGui
extends Module {
    public final Setting<Style> style;
    public final Setting<Integer> panelWidth;
    public final Setting<Integer> panelHeight;
    public final Setting<Integer> panelScale;
    public final Setting<String> favorites;
    public final Setting<Format> format;
    public final Setting<Integer> glassTint;
    public final Setting<Boolean> shimmer;
    public final Setting<Integer> guiScale;
    public final Setting<Integer> glassOpacity;
    public final Setting<Integer> backgroundDim;
    public final Setting<AccentMode> accentMode;
    public final Setting<Color> accent;
    public final Setting<Integer> accentSpeed;
    public final Setting<Color> panelColor;
    public final Setting<Integer> panelOpacity;
    public final Setting<Color> textColor;
    private final Setting<Integer> styleVersion;
    public final Setting<Boolean> uppercase;
    public final Setting<Boolean> blur;
    public final Setting<Fonts.Family> font;
    public final Setting<Fonts.Weight> fontWeight;
    public final Setting<Fonts.Family> gameFont;
    public final Setting<Fonts.Weight> gameWeight;
    public final Setting<Integer> volume;
    public final Setting<SoundPack> soundPack;
    public final Setting<Boolean> openSound;
    public final Setting<Boolean> hoverSound;
    public final Setting<HoverStyle> hoverStyle;
    public final Setting<Boolean> clickSound;
    public final Setting<Boolean> moduleSound;
    public final Setting<Integer> animationSpeed;
    public final Setting<Boolean> overrideKeys;
    private final Setting<Boolean> resetAll;
    private long resetArmedAt;

    public ClickGui() {
        super("Click GUI", "Opens the module menu.", Category.CLIENT);
        this.group("Interface");
        this.style = this.add(new EnumSetting<Style>("Menu Style", "Panel is one window with tabs and the modules as cards. Dropdown shows the floating category panels.", Style.Panel));
        this.panelWidth = this.add(new IntSetting("Panel Width", "Width of the Panel window in percent of your screen. You can also drag its bottom right corner.", 68, 35, 100)).visible(() -> this.style.get() == Style.Panel);
        this.panelHeight = this.add(new IntSetting("Panel Height", "Height of the Panel window in percent of your screen. You can also drag its bottom right corner.", 74, 35, 100)).visible(() -> this.style.get() == Style.Panel);
        this.panelScale = this.add(new IntSetting("Panel Scale", "Size of the text and everything inside the Panel, in percent. Ctrl and the mouse wheel change it too.", 100, 70, 160)).visible(() -> this.style.get() == Style.Panel);
        this.favorites = this.add(new StringSetting("Favorites", "", "", true)).visible(() -> false);
        this.format = this.add(new EnumSetting<Format>("Format", "Look of the dropdown menu, the HUD and the start menu. Liquid Glass draws clear see-through glass panels with a light rim and a soft shine.", Format.Standard));
        this.glassTint = this.add(new IntSetting("Glass Tint", "How strongly the Liquid Glass is tinted in your accent color, in percent. 0 is clear glass.", 45, 0, 100)).visible(() -> this.format.get() == Format.LiquidGlass);
        this.shimmer = this.add(new BoolSetting("Shimmer", "Now and then a soft light runs over the glass panels.", true)).visible(() -> this.format.get() == Format.LiquidGlass);
        this.guiScale = this.add(new IntSetting("GUI Scale", "Size of the panel relative to your screen in percent. It always fits every resolution and Minecraft GUI scale.", 100, 50, 100));
        this.glassOpacity = this.add(new IntSetting("Glass Opacity", "Opacity of the panel in percent.", 88, 55, 100));
        this.backgroundDim = this.add(new IntSetting("Background Dim", "Darkness behind the panel in percent.", 45, 0, 90));
        this.group("Theme");
        this.accentMode = this.add(new EnumSetting<AccentMode>("Accent Mode", "Static color, flowing rainbow or a galaxy shimmer.", AccentMode.Static));
        this.accent = this.add(new ColorSetting("Accent", "Accent color of the GUI, HUD and notifications.", new Color(139, 92, 246, 255))).visible(() -> this.accentMode.get() == AccentMode.Static);
        this.accentSpeed = this.add(new IntSetting("Accent Speed", "Speed of the rainbow and galaxy accent.", 5, 1, 20)).visible(() -> this.accentMode.get() != AccentMode.Static);
        this.panelColor = this.add(new ColorSetting("Panel Color", "Background color of panels and notifications.", new Color(24, 16, 48, 255)));
        this.panelOpacity = this.add(new IntSetting("Panel Opacity", "How solid the panel backgrounds are, in percent. Module rows stay the same. 100 makes the background fully opaque.", 88, 10, 100));
        this.textColor = this.add(new ColorSetting("Text Color", "Main text color.", new Color(255, 255, 255, 255)));
        this.styleVersion = this.add(new IntSetting("Style Version", "", 0, 0, 100)).visible(() -> false);
        this.uppercase = this.add(new BoolSetting("Uppercase", "Writes module and panel names in capitals.", false));
        this.blur = this.add(new BoolSetting("Background Blur", "Blurs the game behind the menu for a frosted glass look. Uses the blur strength from the video settings.", true));
        this.group("Fonts");
        this.font = this.add(new EnumSetting<Fonts.Family>("Menu Font", "Font of the Click GUI menus, notifications and the region map. Ubuntu is the default. The HUD always uses Ubuntu.", Fonts.Family.Ubuntu));
        this.fontWeight = this.add(new EnumSetting<Fonts.Weight>("Menu Weight", "Thickness of the GUI font.", Fonts.Weight.Medium)).visible(() -> this.font.get().hasWeights());
        this.gameFont = this.add(new EnumSetting<Fonts.Family>("Game Font", "Font for the whole game: chat, menus, tooltips and signs.", Fonts.Family.Inter));
        this.gameWeight = this.add(new EnumSetting<Fonts.Weight>("Game Weight", "Thickness of the game font.", Fonts.Weight.Regular)).visible(() -> this.gameFont.get().hasWeights());
        this.group("Sounds");
        this.volume = this.add(new IntSetting("Master Volume", "Volume of all GUI sounds in percent.", 70, 0, 100));
        this.soundPack = this.add(new EnumSetting<SoundPack>("Sound Pack", "Glide plays a soft rising tone when the menu opens, a falling one when it closes, a small click when modules turn on or off and calm, soft tones for clicks, switches and typing. Pulse plays a tick with a soft tone when modules turn on or off, a light swish for clicks and a short tock when the menu opens. Silk plays soft, clean tones. Minecraft uses the game sounds.", SoundPack.Glide)).onChanged(v -> UiSounds.click());
        this.openSound = this.add(new BoolSetting("Open/Close", "Sound when the GUI opens or closes.", true));
        this.hoverSound = this.add(new BoolSetting("Hover", "Soft tick when hovering modules.", true));
        this.hoverStyle = this.add(new EnumSetting<HoverStyle>("Hover Sound", "Which sound plays when you hover a module. Changing it plays a preview. Classic is the old sound.", HoverStyle.Glide)).visible(this.hoverSound::get).onChanged(v -> UiSounds.previewHover());
        this.clickSound = this.add(new BoolSetting("Clicks & Toggles", "Sound when clicking and toggling.", true));
        this.moduleSound = this.add(new BoolSetting("Module On/Off", "Sound when you turn a module on or off in the menu. With Glide and Pulse it only plays in the menu, not on keybinds.", false));
        this.group("Animations");
        this.animationSpeed = this.add(new IntSetting("Animation Speed", "Speed of all GUI animations in percent.", 100, 25, 300));
        this.group("Keybinds");
        this.overrideKeys = this.add(new BoolSetting("Override Keys", "Keys you bind to modules only toggle the module and no longer trigger Minecraft or server actions, for example G for the DonutSMP quick actions menu.", true));
        this.group("Reset");
        this.resetAll = this.add(new BoolSetting("Reset All", "Resets native menu and HUD options only. Leaves existing modules, keys and secrets untouched. Press twice within 5 seconds. A UI backup is kept.", false)).onChanged(v -> this.pressReset((boolean)v));
        this.setKey(344);
    }

    public boolean migrate() {
        boolean changed = false;
        if (this.styleVersion.get() < 2) {
            this.panelColor.set(new Color(13, 17, 32, 255));
            this.panelOpacity.set(86);
            this.textColor.set(new Color(255, 255, 255, 255));
            this.styleVersion.set(2);
            changed = true;
        }
        if (this.styleVersion.get() < 4) {
            this.font.set(Fonts.Family.Xuong);
            this.fontWeight.set(Fonts.Weight.Regular);
            this.panelColor.set(new Color(27, 30, 35, 255));
            this.panelOpacity.set(88);
            this.styleVersion.set(4);
            changed = true;
        }
        if (this.styleVersion.get() < 5) {
            this.font.set(Fonts.Family.Geist);
            this.fontWeight.set(Fonts.Weight.Regular);
            this.uppercase.set(false);
            this.styleVersion.set(5);
            changed = true;
        }
        if (this.styleVersion.get() < 6) {
            this.font.set(Fonts.Family.Ubuntu);
            this.fontWeight.set(Fonts.Weight.Medium);
            this.blur.set(true);
            this.styleVersion.set(6);
            changed = true;
        }
        if (this.styleVersion.get() < 7) {
            this.accentMode.set(AccentMode.Static);
            this.accent.set(new Color(255, 210, 31, 255));
            this.styleVersion.set(7);
            changed = true;
        }
        if (this.styleVersion.get() < 8) {
            this.soundPack.set(SoundPack.Pulse);
            this.moduleSound.set(true);
            this.styleVersion.set(8);
            changed = true;
        }
        if (this.styleVersion.get() < 9) {
            this.soundPack.set(SoundPack.Glide);
            this.moduleSound.set(true);
            this.openSound.set(true);
            this.styleVersion.set(9);
            changed = true;
        }
        if (this.styleVersion.get() < 10) {
            this.soundPack.set(SoundPack.Glide);
            this.hoverStyle.set(HoverStyle.Glide);
            this.styleVersion.set(10);
            changed = true;
        }
        if (this.styleVersion.get() < 11) {
            this.accentMode.set(AccentMode.Static);
            this.accent.set(new Color(139, 92, 246, 255));
            this.panelColor.set(new Color(24, 16, 48, 255));
            this.panelOpacity.set(90);
            this.font.set(Fonts.Family.Montserrat);
            this.fontWeight.set(Fonts.Weight.Medium);
            this.styleVersion.set(11);
            changed = true;
        }
        if (this.styleVersion.get() < 12) {
            this.format.set(Format.LiquidGlass);
            this.glassTint.set(45);
            this.shimmer.set(true);
            this.styleVersion.set(12);
            changed = true;
        }
        if (this.styleVersion.get() < 13) {
            this.format.set(Format.Standard);
            this.styleVersion.set(13);
            changed = true;
        }
        if (this.styleVersion.get() < 14) {
            this.style.set(Style.Panel);
            this.styleVersion.set(14);
            changed = true;
        }
        return changed;
    }

    private void pressReset(boolean value) {
        if (!value) {
            return;
        }
        this.resetAll.set(false);
        long now = System.currentTimeMillis();
        if (now - this.resetArmedAt > 5000L) {
            this.resetArmedAt = now;
            Notifications.push("Reset All", "Press Reset All again within 5 seconds to reset menu/HUD options only.", Notifications.Type.WARNING);
            return;
        }
        this.resetArmedAt = 0L;
        mc.execute(ResetAll::run);
    }

    @Override
    public void onEnable() {
        mc.method_1507(MenuKit.menu());
        UiSounds.open();
        this.setEnabledSilently(false);
    }

    @Override
    public boolean showInHud() {
        return false;
    }

    public static enum Style {
        Panel("Panel"),
        Dropdown("Dropdown");

        private final String title;

        private Style(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }

    public static enum Format {
        Standard("Standard"),
        LiquidGlass("Liquid Glass");

        private final String title;

        private Format(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }

    public static enum AccentMode {
        Static,
        Rainbow,
        Galaxy;

    }

    public static enum SoundPack {
        Glide,
        Pulse,
        Silk,
        Minecraft;

    }

    public static enum HoverStyle {
        Glide,
        Silk,
        Bubble,
        Crystal,
        Wood,
        Tick,
        Classic;

    }
}

