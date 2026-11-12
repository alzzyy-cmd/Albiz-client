// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.Theme;

public final class GuiPalette {
    private GuiPalette() {
    }

    public static int accent() {
        return GuiPalette.abgr(Theme.accent() | 0xFF000000);
    }

    public static int abgr(int argb) {
        return argb & 0xFF00FF00 | argb >> 16 & 0xFF | (argb & 0xFF) << 16;
    }
}

