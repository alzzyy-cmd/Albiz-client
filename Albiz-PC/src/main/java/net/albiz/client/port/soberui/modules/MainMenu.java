// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.MenuAudio;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.modules.Sky;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.EnumSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.Setting;

public class MainMenu
extends Module {
    public final Setting<Style> style = this.add(new EnumSetting<Style>("Menu Style", "Choose the Albız title screen: Albız, Vanilla+, Home, Island or Classic. Changes apply next time you return to the title screen.", Style.Sober));
    public final Setting<ServerLayout> serverLayout = this.add(new EnumSetting<ServerLayout>("Server List", "Cards shows your servers as cards in three columns with the big Multiplayer title. List shows them as one slim column.", ServerLayout.Cards)).visible(() -> this.style.get() == Style.Sober);
    private final Setting<Integer> menuVersion = this.add(new IntSetting("Menu Version", "", 0, 0, 100)).visible(() -> false);
    public final Setting<Boolean> intro = this.add(new BoolSetting("Intro", "Plays the Albız intro when the game starts.", true));
    public final Setting<Boolean> audio = this.add(new BoolSetting("Menu Sound", "Plays a short Albız chime once when the start menu opens after starting the game.", true)).onChanged(v -> {
        if (!v.booleanValue()) {
            MenuAudio.stop();
        }
    });
    public final Setting<Integer> audioVolume = this.add(new IntSetting("Menu Audio Volume", "Volume of the start menu sound in percent.", 70, 0, 100)).visible(this.audio::get);
    public final Setting<Boolean> matchSky = this.add(new BoolSetting("Match Sky", "Menu colors follow your sky.", true));
    public final Setting<Boolean> stars = this.add(new BoolSetting("Stars", "Floating stars in the menu background.", true));
    public final Setting<Boolean> customScreens = this.add(new BoolSetting("Custom Screens", "Restyles singleplayer, multiplayer, options and other menus.", true));
    public final Setting<Boolean> customWidgets = this.add(new BoolSetting("Custom Buttons", "Rounded buttons, sliders, text fields and lists everywhere.", true));

    public MainMenu() {
        super("Main Menu", "Custom Albız title screen with an intro animation.", Category.CLIENT);
        this.setEnabledSilently(true);
    }

    public boolean migrate() {
        if (this.menuVersion.get() >= 5) {
            return false;
        }
        this.style.set(Style.Sober);
        this.menuVersion.set(5);
        return true;
    }

    public int color() {
        int sky;
        if (this.matchSky.get().booleanValue() && (sky = SoberClient.MODULES.get(Sky.class).accentColor()) >= 0 && sky != 0xFFFFFF) {
            return Theme.neon(0xFF000000 | sky) & 0xFFFFFF;
        }
        return Theme.accent() & 0xFFFFFF;
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
        Sober("Albız"),
        VanillaPlus("Vanilla+"),
        Home("Home"),
        Island("Island"),
        Classic("Classic");

        private final String title;

        private Style(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }

    public static enum ServerLayout {
        Cards,
        List;

    }
}

