// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.modules.MainMenu;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3419;

public final class MenuAudio {
    private static final class_2960 THEME = class_2960.method_60655((String)"albiz", (String)"menu_theme");
    private static class_1113 current;
    private static boolean pending;

    private MenuAudio() {
    }

    public static void menuShown() {
        if (!pending) {
            return;
        }
        pending = false;
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        if (menu == null || !menu.audio.get().booleanValue()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client.method_1483() == null) {
            return;
        }
        client.method_1538().method_4859();
        current = new class_1109(THEME, class_3419.field_61058, (float)menu.audioVolume.get().intValue() / 100.0f, 1.0f, class_1113.method_43221(), false, 0, class_1113.class_1114.field_5478, 0.0, 0.0, 0.0, true);
        client.method_1483().method_4873(current);
    }

    public static boolean playing() {
        if (current == null) {
            return false;
        }
        if (class_310.method_1551().method_1483().method_4877(current)) {
            return true;
        }
        current = null;
        return false;
    }

    public static void stop() {
        if (current != null) {
            class_310.method_1551().method_1483().method_4870(current);
        }
        current = null;
    }

    public static void tick(class_310 client) {
        if (client.field_1687 == null) {
            return;
        }
        pending = false;
        if (current != null) {
            MenuAudio.stop();
        }
    }

    static {
        pending = true;
    }
}

