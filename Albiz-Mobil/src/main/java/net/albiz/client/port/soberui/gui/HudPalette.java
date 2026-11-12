// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.Theme;

public final class HudPalette {
    public static final int CARD = -233301989;
    public static final int BORDER = -13750733;
    public static final int INNER = 351597813;
    public static final int INNER_BORDER = 653587701;
    public static final int PRESSED = -218827531;
    public static final int TEXT = -723723;
    public static final int MUTED = -5329226;
    public static final int HEADER = -2565922;
    public static final int GLASS_VEIL = -2146430958;
    public static final int WIDGET = -484697058;
    public static final int WIDGET_EDGE = 0x1CFFFFFF;
    public static final int FILL = 695761024;
    public static final int SEPARATOR = 1089203189;
    public static final int GREEN = -13577896;
    public static final int ORANGE = -24822;
    public static final int RED = -47814;
    public static final int BLUE = -16087809;
    public static final int PURPLE = -4236558;
    public static final int GRAY = -7434605;
    public static final int RADIUS = 9;

    private HudPalette() {
    }

    public static int widget() {
        return Theme.opacity(-484697058);
    }

    public static int card() {
        return Theme.opacity(-233301989);
    }
}

