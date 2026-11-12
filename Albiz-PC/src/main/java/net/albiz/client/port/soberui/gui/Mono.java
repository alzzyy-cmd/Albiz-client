// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.BrandTextures;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.minecraft.class_332;

public final class Mono {
    public static final int BG = -15856112;
    public static final int PANEL = -15198181;
    public static final int PANEL_HOVER = -14737629;
    public static final int LINE = -14013906;
    public static final int LINE_HOVER = -12237491;
    public static final int TEXT = -723723;
    public static final int MUTED = -7434602;
    public static final int FAINT = -10724252;
    public static final int RADIUS = 3;
    public static final String FONT = "geist-semibold.ttf";
    public static final String FONT_LIGHT = "geist-medium.ttf";

    private Mono() {
    }

    public static void box(class_332 context, int x, int y, int w, int h, float hover, float opacity) {
        Smooth.rect(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-15198181, -14737629, hover), opacity));
        Smooth.outline(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-14013906, -12237491, hover), opacity));
    }

    public static void primary(class_332 context, int x, int y, int w, int h, float hover, float opacity) {
        Smooth.rect(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-723723, -1, hover), opacity));
    }

    public static float watermarkWidth(float height) {
        BrandTextures.Sprite full = BrandTextures.get("orbit");
        return full == null ? height : height * (float)full.width() / (float)full.height();
    }

    public static void watermark(class_332 context, float x, float y, float height, int alpha) {
        BrandTextures.Sprite full = BrandTextures.get("orbit");
        if (full == null) {
            return;
        }
        float width = height * (float)full.width() / (float)full.height();
        BrandTextures.Sprite sprite = BrandTextures.fit("orbit", width * Smooth.pixelsPerUnit(context));
        SoberScreen.image(context, sprite, x, y, width, height, alpha << 24 | 0xF4F4F5);
    }
}

