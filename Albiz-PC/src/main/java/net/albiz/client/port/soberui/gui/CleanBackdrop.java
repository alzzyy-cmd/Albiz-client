// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

final class CleanBackdrop {
    private static final class_2960 ID = class_2960.method_60655((String)"albiz", (String)"dynamic/clean_backdrop");
    private static int builtW;
    private static int builtH;

    private CleanBackdrop() {
    }

    static void draw(class_332 context, float opacity) {
        if (opacity <= 0.002f) {
            return;
        }
        class_1041 window = class_310.method_1551().method_22683();
        int w = Math.max(1, window.method_4489());
        int h = Math.max(1, window.method_4506());
        if (w != builtW || h != builtH) {
            CleanBackdrop.build(w, h);
        }
        float ratio = (float)h / (float)Math.max(1, window.method_4502());
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        context.method_25293(class_10799.field_56883, ID, 0, 0, 0.0f, 0.0f, w, h, w, h, w, h, MenuKit.alpha(-1, opacity));
        matrices.popMatrix();
    }

    private static void build(int w, int h) {
        class_1011 image = new class_1011(w, h, false);
        float cx = (float)w * 0.5f;
        float cy = (float)h * 0.42f;
        float sx = 1.0f / ((float)w * 0.7f);
        float sy = 1.0f / ((float)h * 0.85f);
        int seed = -1640531527;
        for (int y = 0; y < h; ++y) {
            float dy = ((float)y - cy) * sy;
            for (int x = 0; x < w; ++x) {
                float dx = ((float)x - cx) * sx;
                float d = Math.min(1.0f, (float)Math.sqrt(dx * dx + dy * dy));
                float t = d * d * (3.0f - 2.0f * d);
                seed ^= seed << 13;
                seed ^= seed >>> 17;
                seed ^= seed << 5;
                float n1 = (float)(seed & 0xFFFF) / 65535.0f;
                float n2 = (float)(seed >>> 16 & 0xFFFF) / 65535.0f;
                float noise = n1 + n2 - 1.0f;
                int r = CleanBackdrop.clamp(Math.round(22.0f - 10.0f * t + noise));
                int g = CleanBackdrop.clamp(Math.round(22.0f - 10.0f * t + noise));
                int b = CleanBackdrop.clamp(Math.round(25.0f - 11.0f * t + noise));
                image.method_61941(x, y, 0xFF000000 | r << 16 | g << 8 | b);
            }
        }
        class_310.method_1551().method_1531().method_4615(ID);
        Smooth.register(ID, image);
        builtW = w;
        builtH = h;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}

