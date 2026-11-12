// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.GuiQuads;
import net.albiz.client.port.soberui.gui.Smooth;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

final class Logo3D {
    private static final GuiQuads QUADS = new GuiQuads();
    private static final int STRIPS = 28;
    private static final int LAYERS = 8;
    private static final int FRONT = -7342342;
    private static final int DEEP = -15844800;

    private Logo3D() {
    }

    static void draw(class_332 context, float cx, float cy, float size, float yaw, float pitch, float depth, float shine, float shinePos, float alpha) {
        Logo3D.draw(context, cx, cy, size, yaw, pitch, depth, shine, shinePos, alpha, 0.0f);
    }

    static void draw(class_332 context, float cx, float cy, float size, float yaw, float pitch, float depth, float shine, float shinePos, float alpha, float bloom) {
        if (alpha <= 0.003f || size <= 0.5f) {
            return;
        }
        float pixels = size * Smooth.pixelsPerUnit(context);
        class_2960 colored = Smooth.brandStep(pixels, false);
        class_2960 white = Smooth.brandStep(pixels, true);
        if (colored == null || white == null) {
            return;
        }
        float cosY = (float)Math.cos(yaw);
        float sinY = (float)Math.sin(yaw);
        float cosP = (float)Math.cos(pitch);
        float sinP = (float)Math.sin(pitch);
        float camera = size * 2.8f;
        float thickness = size * 0.14f * depth;
        boolean front = cosY * cosP >= 0.0f;
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        if (thickness > 0.05f) {
            for (int n = 0; n < 8; ++n) {
                int k = front ? 8 - n : n + 1;
                float t = (float)k / 8.0f;
                int shade = Logo3D.lerp(Logo3D.lerp(-7342342, -15844800, 0.35f), -15844800, t);
                float light = 0.55f + 0.45f * Math.abs(sinY);
                int tint = Math.round((float)a * Math.min(1.0f, depth * 1.6f)) << 24 | Logo3D.scale(shade, light);
                Logo3D.strips(context, white, cx, cy, size, thickness * t, cosY, sinY, cosP, sinP, camera, tint, null);
            }
        }
        if (bloom > 0.01f && front) {
            int halo = Math.round((float)a * 0.3f * bloom) << 24 | 0x8FF6FA;
            int haze = Math.round((float)a * 0.13f * bloom) << 24 | 0x8FF6FA;
            Logo3D.strips(context, white, cx, cy, size * 1.09f, 0.0f, cosY, sinY, cosP, sinP, camera, haze, null);
            Logo3D.strips(context, white, cx, cy, size * 1.04f, 0.0f, cosY, sinY, cosP, sinP, camera, halo, null);
        }
        float facing = Math.abs(cosY * cosP);
        int face = a << 24 | Logo3D.scale(0xFFFFFF, front ? 0.72f + 0.28f * facing : 0.5f);
        float faceDepth = front ? 0.0f : thickness;
        Logo3D.strips(context, colored, cx, cy, size, faceDepth, cosY, sinY, cosP, sinP, camera, face, null);
        if (shine > 0.01f && front) {
            Logo3D.strips(context, white, cx, cy, size, 0.0f, cosY, sinY, cosP, sinP, camera, a << 24 | 0xFFFFFF, new float[]{shinePos, shine});
        }
    }

    private static void strips(class_332 context, class_2960 texture, float cx, float cy, float size, float z, float cosY, float sinY, float cosP, float sinP, float camera, int tint, float[] sweep) {
        GuiQuads quads = QUADS.reset();
        float[] corners = new float[8];
        float[] coords = new float[8];
        int[] tints = new int[4];
        int baseAlpha = tint >>> 24;
        for (int i = 0; i < 28; ++i) {
            float u0 = (float)i / 28.0f;
            float u1 = (float)(i + 1) / 28.0f;
            float[] us = new float[]{u0, u0, u1, u1};
            float[] vs = new float[]{0.0f, 1.0f, 1.0f, 0.0f};
            boolean visible = sweep == null;
            for (int k = 0; k < 4; ++k) {
                float lx = (us[k] - 0.5f) * size;
                float ly = (vs[k] - 0.5f) * size;
                float x1 = lx * cosY + z * sinY;
                float z1 = -lx * sinY + z * cosY;
                float y2 = ly * cosP - z1 * sinP;
                float z2 = ly * sinP + z1 * cosP;
                float f = camera / Math.max(camera * 0.2f, camera + z2);
                corners[k * 2] = cx + x1 * f;
                corners[k * 2 + 1] = cy + y2 * f;
                coords[k * 2] = us[k];
                coords[k * 2 + 1] = vs[k];
                if (sweep == null) {
                    tints[k] = tint;
                    continue;
                }
                float d = (us[k] + vs[k] * 0.45f) / 1.45f - sweep[0];
                float glow = (float)Math.exp(-d * d / 0.012f) * sweep[1];
                int value = Math.round((float)baseAlpha * Math.min(1.0f, glow) * 0.9f);
                tints[k] = value << 24 | tint & 0xFFFFFF;
                if (value <= 0) continue;
                visible = true;
            }
            if (!visible) continue;
            quads.quad(corners, coords, tints);
        }
        quads.submit(context, class_10799.field_56883, texture);
    }

    private static int scale(int rgb, float factor) {
        int r = Math.min(255, Math.round((float)(rgb >> 16 & 0xFF) * factor));
        int g = Math.min(255, Math.round((float)(rgb >> 8 & 0xFF) * factor));
        int b = Math.min(255, Math.round((float)(rgb & 0xFF) * factor));
        return r << 16 | g << 8 | b;
    }

    private static int lerp(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return 0xFF000000 | Math.round((float)ar + (float)(br - ar) * t) << 16 | Math.round((float)ag + (float)(bg - ag) * t) << 8 | Math.round((float)ab + (float)(bb - ab) * t);
    }
}

