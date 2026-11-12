// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.VectorIcon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_2960;

final class SpotifyLogo {
    private static final String PATH = "M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z";
    private static final int[] SIZES = new int[]{16, 20, 24, 32, 40, 48, 64, 96, 128};
    private static final int GREEN = 2021216;
    private static final int INK = 0;
    private static final Map<Integer, class_2960> TEXTURES = new HashMap<Integer, class_2960>();
    private static List<float[]> shapes;

    private SpotifyLogo() {
    }

    static int sizeFor(float pixels) {
        for (int candidate : SIZES) {
            if (!((float)candidate >= pixels - 0.5f)) continue;
            return candidate;
        }
        return SIZES[SIZES.length - 1];
    }

    static class_2960 texture(int chosen) {
        return TEXTURES.computeIfAbsent(chosen, k -> {
            if (shapes == null) {
                shapes = VectorIcon.parse(PATH);
            }
            float[] circle = VectorIcon.coverage(shapes, 0, 1, 24.0f, chosen);
            float[] waves = VectorIcon.coverage(shapes, 1, shapes.size(), 24.0f, chosen);
            class_1011 image = new class_1011(chosen, chosen, false);
            for (int i = 0; i < chosen * chosen; ++i) {
                float total = Math.min(1.0f, circle[i]);
                float ink = Math.min(total, waves[i]);
                float green = total - ink;
                int argb = 0;
                if (total > 0.002f) {
                    int r = Math.round((30.0f * green + 0.0f * ink) / total);
                    int g = Math.round((215.0f * green + 0.0f * ink) / total);
                    int b = Math.round((96.0f * green + 0.0f * ink) / total);
                    argb = Math.round(total * 255.0f) << 24 | r << 16 | g << 8 | b;
                }
                image.method_61941(i % chosen, i / chosen, argb);
            }
            class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/spotify_logo_" + chosen));
            Smooth.register(id, image);
            return id;
        });
    }
}

