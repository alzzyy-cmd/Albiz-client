// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render;

import net.albiz.client.port.soberui.render.GlassLayers;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

public final class GlassGear {
    private static boolean active;

    private GlassGear() {
    }

    public static void set(boolean on) {
        active = on;
    }

    public static class_1921 layer(class_2960 texture) {
        return active && texture != null ? GlassLayers.textured(texture) : null;
    }
}

