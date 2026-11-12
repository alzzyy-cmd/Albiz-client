// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.render.Color;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class ColorWheel {
    private static final class_2960 TEXTURE = class_2960.method_60655((String)"albiz", (String)"dynamic/color_wheel");
    private static final int SIZE = 256;
    private static boolean created;

    private ColorWheel() {
    }

    private static void ensureCreated() {
        if (created) {
            return;
        }
        created = true;
        class_1011 image = new class_1011(256, 256, false);
        float center = 128.0f;
        float radius = 126.0f;
        for (int x = 0; x < 256; ++x) {
            for (int y = 0; y < 256; ++y) {
                float dx = (float)x + 0.5f - center;
                float dy = (float)y + 0.5f - center;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                if (distance > radius + 1.5f) {
                    image.method_61941(x, y, 0);
                    continue;
                }
                float hue = (float)((Math.atan2(dy, dx) / (Math.PI * 2) + 1.0) % 1.0);
                float saturation = Math.min(1.0f, distance / radius);
                float edge = Math.max(0.0f, Math.min(1.0f, (radius + 1.5f - distance) / 2.0f));
                Color color = Color.fromHsv(hue, saturation, 1.0f, Math.round(255.0f * edge));
                image.method_61941(x, y, color.packed());
            }
        }
        Smooth.register(TEXTURE, image);
    }

    public static void draw(class_332 context, int x, int y, int size, float brightness, float alpha) {
        ColorWheel.ensureCreated();
        int v = Math.round(255.0f * Math.max(0.15f, brightness));
        int tint = Math.round(255.0f * alpha) << 24 | v << 16 | v << 8 | v;
        context.method_25293(class_10799.field_56883, TEXTURE, x, y, 0.0f, 0.0f, size, size, 256, 256, 256, 256, tint);
    }

    public static float[] pick(double mouseX, double mouseY, int x, int y, int size) {
        double radius = (double)size / 2.0;
        double dx = mouseX - ((double)x + radius);
        double dy = mouseY - ((double)y + radius);
        double distance = Math.min(radius, Math.sqrt(dx * dx + dy * dy));
        float hue = (float)((Math.atan2(dy, dx) / (Math.PI * 2) + 1.0) % 1.0);
        float saturation = (float)(distance / radius);
        return new float[]{hue, saturation};
    }
}

