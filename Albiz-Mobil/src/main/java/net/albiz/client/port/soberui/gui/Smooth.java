// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.GuiBatch;
import net.albiz.client.port.soberui.gui.GuiQuads;
import net.albiz.client.port.soberui.gui.Theme;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class Smooth {
    private static final int CIRCLE_SIZE = 128;
    private static final class_2960 CIRCLE = class_2960.method_60655((String)"albiz", (String)"dynamic/circle");
    private static final class_2960 GLOW = class_2960.method_60655((String)"albiz", (String)"dynamic/glow");
    private static final int[] LOGO_SIZES = new int[]{512, 256, 128, 64, 32, 16};
    private static final class_2960[] LOGOS = new class_2960[LOGO_SIZES.length];
    private static final class_2960 WAVE;
    private static final int WAVE_SIZE = 256;
    private static final class_2960 TRIANGLE;
    private static final int TRIANGLE_SIZE = 128;
    private static final Map<Integer, class_2960> RINGS;
    private static final Map<Integer, class_2960> DISCS;
    private static final Map<Integer, class_2960> LENSES;
    private static final Map<Integer, class_2960> BRANDS;
    private static class_1011 brandSource;
    private static boolean brandFailed;
    private static boolean created;
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    public static final int BOTTOM_LEFT = 4;
    public static final int BOTTOM_RIGHT = 8;
    public static final int ALL = 15;
    private static final GuiBatch BATCH;
    private static final GuiQuads LINES;
    private static final float[] CENTER_UV;
    private static final int[] BRAND_STEPS;
    private static final Map<Integer, class_2960> BRAND_WHITE;
    private static final Map<Integer, class_2960> BRAND_STEP;

    private Smooth() {
    }

    public static void register(class_2960 id, class_1011 image) {
        class_310.method_1551().method_1531().method_4616(id, (class_1044)new LinearTexture(() -> ((class_2960)id).toString(), image));
    }

    static float pixelScale() {
        return class_310.method_1551().method_22683().method_4495();
    }

    private static void ensureCreated() {
        if (created) {
            return;
        }
        created = true;
        class_1011 circle = new class_1011(128, 128, false);
        class_1011 glow = new class_1011(128, 128, false);
        float center = 64.0f;
        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) {
                float dx = (float)x + 0.5f - center;
                float dy = (float)y + 0.5f - center;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                float coverage = Math.max(0.0f, Math.min(1.0f, center - distance));
                circle.method_61941(x, y, Math.round(coverage * 255.0f) << 24 | 0xFFFFFF);
                float falloff = Math.max(0.0f, 1.0f - distance / center);
                glow.method_61941(x, y, Math.round(falloff * falloff * falloff * 255.0f) << 24 | 0xFFFFFF);
            }
        }
        Smooth.register(CIRCLE, circle);
        Smooth.register(GLOW, glow);
        class_1011 wave = new class_1011(256, 256, false);
        float waveCenter = 128.0f;
        for (int x = 0; x < 256; ++x) {
            for (int y = 0; y < 256; ++y) {
                float dx = (float)x + 0.5f - waveCenter;
                float dy = (float)y + 0.5f - waveCenter;
                float distance = (float)Math.sqrt(dx * dx + dy * dy) / waveCenter;
                float d = (distance - 0.9f) / 0.05f;
                float value = distance > 1.0f ? 0.0f : (float)Math.exp(-d * d);
                wave.method_61941(x, y, Math.round(value * 255.0f) << 24 | 0xFFFFFF);
            }
        }
        Smooth.register(WAVE, wave);
        class_1011 triangle = new class_1011(128, 128, false);
        float pad = 7.68f;
        float ax = 64.0f;
        float ay = pad;
        float bx = 128.0f - pad;
        float by = 128.0f - pad;
        float qx = pad;
        float qy = 128.0f - pad;
        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) {
                int hits = 0;
                for (int sx = 0; sx < 4; ++sx) {
                    for (int sy = 0; sy < 4; ++sy) {
                        float px = (float)x + ((float)sx + 0.5f) / 4.0f;
                        float py = (float)y + ((float)sy + 0.5f) / 4.0f;
                        float e1 = (bx - ax) * (py - ay) - (by - ay) * (px - ax);
                        float e2 = (qx - bx) * (py - by) - (qy - by) * (px - bx);
                        float e3 = (ax - qx) * (py - qy) - (ay - qy) * (px - qx);
                        if (!(e1 >= 0.0f) || !(e2 >= 0.0f) || !(e3 >= 0.0f)) continue;
                        ++hits;
                    }
                }
                triangle.method_61941(x, y, Math.round((float)hits * 255.0f / 16.0f) << 24 | 0xFFFFFF);
            }
        }
        Smooth.register(TRIANGLE, triangle);
        for (int i = 0; i < LOGO_SIZES.length; ++i) {
            try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655((String)"albiz", (String)("textures/gui/logo_" + LOGO_SIZES[i] + ".png")));){
                Smooth.register(LOGOS[i], class_1011.method_4309((InputStream)stream));
                continue;
            }
            catch (Exception e) {
                SoberClient.LOG.error("Could not load logo", (Throwable)e);
            }
        }
    }

    private static int logoIndex(int size) {
        int pixels = (int)Math.ceil(size * class_310.method_1551().method_22683().method_4495());
        for (int i = LOGO_SIZES.length - 1; i >= 0; --i) {
            if (LOGO_SIZES[i] < pixels) continue;
            return i;
        }
        return 0;
    }

    private static int scale(class_332 context) {
        Matrix3x2fStack m = context.method_51448();
        double zoom = Math.max(Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01()), Math.sqrt(m.m10() * m.m10() + m.m11() * m.m11()));
        if (!(zoom > 0.01)) {
            zoom = 1.0;
        }
        class_1041 window = class_310.method_1551().method_22683();
        double pixels = (double)window.method_4506() / (double)Math.max(1, window.method_4502());
        return Math.max(2, Math.min(24, (int)Math.ceil(pixels * zoom * 3.0)));
    }

    private static class_2960 disc(int radius, int requested, boolean ring) {
        int scale = Math.max(1, Math.min(requested, 1024 / Math.max(1, radius * 2)));
        int key = radius * 1000 + scale * 2 + (ring ? 1 : 0);
        return (ring ? RINGS : DISCS).computeIfAbsent(key, k -> {
            int size = radius * 2 * scale;
            class_1011 image = new class_1011(size, size, false);
            float center = (float)size / 2.0f;
            float inner = center - (float)scale;
            for (int x = 0; x < size; ++x) {
                for (int y = 0; y < size; ++y) {
                    float dx = (float)x + 0.5f - center;
                    float dy = (float)y + 0.5f - center;
                    float distance = (float)Math.sqrt(dx * dx + dy * dy);
                    float coverage = Math.max(0.0f, Math.min(1.0f, center - distance + 0.5f));
                    if (ring) {
                        coverage = Math.min(coverage, Math.max(0.0f, Math.min(1.0f, distance - inner + 0.5f)));
                    }
                    image.method_61941(x, y, Math.round(coverage * 255.0f) << 24 | 0xFFFFFF);
                }
            }
            class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/" + (ring ? "ring_" : "disc_") + radius + "_" + scale));
            Smooth.register(id, image);
            return id;
        });
    }

    private static void solid(GuiBatch batch, int x1, int y1, int x2, int y2) {
        batch.quad(x1, y1, x2, y2, 0.5f, 0.5f, 0.5f, 0.5f);
    }

    private static void corners(GuiBatch batch, int x, int y, int w, int h, int r, int mask) {
        if ((mask & 1) != 0) {
            batch.quad(x, y, x + r, y + r, 0.0f, 0.0f, 0.5f, 0.5f);
        }
        if ((mask & 2) != 0) {
            batch.quad(x + w - r, y, x + w, y + r, 0.5f, 0.0f, 1.0f, 0.5f);
        }
        if ((mask & 4) != 0) {
            batch.quad(x, y + h - r, x + r, y + h, 0.0f, 0.5f, 0.5f, 1.0f);
        }
        if ((mask & 8) != 0) {
            batch.quad(x + w - r, y + h - r, x + w, y + h, 0.5f, 0.5f, 1.0f, 1.0f);
        }
    }

    public static void rect(class_332 context, int x, int y, int w, int h, int r, int color, int mask) {
        int rightBottom;
        if (color >>> 24 == 0 || w <= 0 || h <= 0) {
            return;
        }
        Smooth.ensureCreated();
        boolean stacked = (mask & 1) != 0 && (mask & 4) != 0 || (mask & 2) != 0 && (mask & 8) != 0;
        r = Math.max(0, Math.min(r, Math.min(w / 2, stacked ? h / 2 : h)));
        if (r == 0 || mask == 0) {
            context.method_25294(x, y, x + w, y + h, color);
            return;
        }
        GuiBatch batch = BATCH.reset();
        Smooth.solid(batch, x + r, y, x + w - r, y + h);
        int leftTop = (mask & 1) != 0 ? y + r : y;
        int leftBottom = (mask & 4) != 0 ? y + h - r : y + h;
        int rightTop = (mask & 2) != 0 ? y + r : y;
        int n = rightBottom = (mask & 8) != 0 ? y + h - r : y + h;
        if (leftBottom > leftTop) {
            Smooth.solid(batch, x, leftTop, x + r, leftBottom);
        }
        if (rightBottom > rightTop) {
            Smooth.solid(batch, x + w - r, rightTop, x + w, rightBottom);
        }
        Smooth.corners(batch, x, y, w, h, r, mask);
        batch.submitTextured(context, class_10799.field_56883, Smooth.disc(r, Smooth.scale(context), false), color);
    }

    public static void circle(class_332 context, int cx, int cy, int radius, int color) {
        Smooth.rect(context, cx - radius, cy - radius, radius * 2, radius * 2, radius, color);
    }

    public static void ring(class_332 context, int cx, int cy, int radius, int color) {
        Smooth.outline(context, cx - radius, cy - radius, radius * 2, radius * 2, radius, color);
    }

    public static void orb(class_332 context, int cx, int cy, int radius, int base) {
        Smooth.circle(context, cx, cy + 1, radius + 1, 0x30000000);
        Smooth.circle(context, cx, cy + 2, radius + 2, 0x14000000);
        Smooth.circle(context, cx, cy, radius, base);
        Smooth.glow(context, cx, cy - Math.round((float)radius * 0.45f), Math.round((float)radius * 0.72f), 0x1CFFFFFF);
        Smooth.ring(context, cx, cy, radius, 0x22FFFFFF);
    }

    public static void triangle(class_332 context, float cx, float cy, float size, int color) {
        if (color >>> 24 == 0 || size <= 0.0f) {
            return;
        }
        Smooth.ensureCreated();
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        float s = size / 16.0f;
        matrices.scale(s, s);
        context.method_25293(class_10799.field_56883, TRIANGLE, -8, -8, 0.0f, 0.0f, 16, 16, 128, 128, 128, 128, color);
        matrices.popMatrix();
    }

    public static void rect(class_332 context, int x, int y, int w, int h, int r, int color) {
        if (color >>> 24 == 0 || w <= 0 || h <= 0) {
            return;
        }
        Smooth.ensureCreated();
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            context.method_25294(x, y, x + w, y + h, color);
            return;
        }
        GuiBatch batch = BATCH.reset();
        Smooth.solid(batch, x + r, y + r, x + w - r, y + h - r);
        Smooth.edges(batch, x, y, w, h, r);
        Smooth.corners(batch, x, y, w, h, r, 15);
        batch.submitTextured(context, class_10799.field_56883, Smooth.disc(r, Smooth.scale(context), false), color);
    }

    private static void edges(GuiBatch batch, int x, int y, int w, int h, int r) {
        batch.quad(x + r, y, x + w - r, y + r, 0.5f, 0.0f, 0.5f, 0.5f);
        batch.quad(x + r, y + h - r, x + w - r, y + h, 0.5f, 0.5f, 0.5f, 1.0f);
        batch.quad(x, y + r, x + r, y + h - r, 0.0f, 0.5f, 0.5f, 0.5f);
        batch.quad(x + w - r, y + r, x + w, y + h - r, 0.5f, 0.5f, 1.0f, 0.5f);
    }

    public static void outline(class_332 context, int x, int y, int w, int h, int r, int color) {
        if (color >>> 24 == 0 || w <= 0 || h <= 0) {
            return;
        }
        Smooth.ensureCreated();
        r = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        GuiBatch batch = BATCH.reset();
        Smooth.edges(batch, x, y, w, h, r);
        Smooth.corners(batch, x, y, w, h, r, 15);
        batch.submitTextured(context, class_10799.field_56883, Smooth.disc(r, Smooth.scale(context), true), color);
    }

    public static void glass(class_332 context, int x, int y, int w, int h, int r, int base) {
        Smooth.glass(context, x, y, w, h, r, base, 1.0f);
    }

    public static void glass(class_332 context, int x, int y, int w, int h, int r, int base, float opacity) {
        if (w <= 0 || h <= 0 || opacity <= 0.01f) {
            return;
        }
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        Smooth.rect(context, x, y + 1, w, h + 1, r, Smooth.alphaOf(0x2A000000, opacity));
        Smooth.rect(context, x + 1, y + 2, w - 2, h + 2, r, Smooth.alphaOf(0x14000000, opacity));
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf(base, opacity));
        Smooth.sheen(context, x, y, w, h, r, Smooth.alphaOf(0x14FFFFFF, opacity));
        Smooth.outline(context, x, y, w, h, r, Smooth.alphaOf(0x1EFFFFFF, opacity));
    }

    private static class_2960 lens(int radius, int requested, int kind) {
        int scale = Math.max(1, Math.min(requested, 1024 / Math.max(1, radius * 2)));
        int key = (radius * 100 + scale) * 4 + kind;
        return LENSES.computeIfAbsent(key, k -> {
            int size = radius * 2 * scale;
            class_1011 image = new class_1011(size, size, false);
            float center = (float)size / 2.0f;
            float line = scale;
            for (int x = 0; x < size; ++x) {
                for (int y = 0; y < size; ++y) {
                    float dx = (float)x + 0.5f - center;
                    float dy = (float)y + 0.5f - center;
                    float distance = (float)Math.sqrt(dx * dx + dy * dy);
                    float body = Smooth.clamp(center - distance + 0.5f);
                    float value = 0.0f;
                    if (body > 0.0f) {
                        float rim = Math.min(body, Smooth.clamp(distance - (center - line) + 0.5f));
                        float inner = Math.min(Smooth.clamp(center - line - distance + 0.5f), Smooth.clamp(distance - (center - line * 2.0f) + 0.5f));
                        float light = distance > 0.001f ? (-dx - dy) / (distance * 1.4142135f) : 0.0f;
                        float height = dy / center;
                        if (kind == 0) {
                            float edge = rim * (0.16f + 0.62f * (float)Math.pow(Math.max(0.0f, light), 2.2));
                            float second = inner * (0.04f + 0.1f * Math.max(0.0f, light));
                            float sheen = body * 0.085f * (float)Math.pow(Smooth.clamp((0.3f - height) / 1.3f), 1.7);
                            value = 1.0f - (1.0f - edge) * (1.0f - second) * (1.0f - sheen);
                        } else {
                            value = kind == 1 ? rim * 0.55f * (float)Math.pow(Math.max(0.0f, -light), 2.0) : body * 0.11f * (float)Math.pow(Smooth.clamp((height - 0.1f) / 0.9f), 2.0);
                        }
                    }
                    image.method_61941(x, y, Math.round(Smooth.clamp(value) * 255.0f) << 24 | 0xFFFFFF);
                }
            }
            class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/lens_" + kind + "_" + radius + "_" + scale));
            Smooth.register(id, image);
            return id;
        });
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static void lens(class_332 context, int x, int y, int w, int h, int kind, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        int radius = Math.max(1, Math.min(w, h) / 2);
        GuiBatch batch = BATCH.reset();
        if (w >= h) {
            batch.quad(x, y, x + radius, y + h, 0.0f, 0.0f, 0.5f, 1.0f);
            batch.quad(x + radius, y, x + w - radius, y + h, 0.5f, 0.0f, 0.5f, 1.0f);
            batch.quad(x + w - radius, y, x + w, y + h, 0.5f, 0.0f, 1.0f, 1.0f);
        } else {
            batch.quad(x, y, x + w, y + radius, 0.0f, 0.0f, 1.0f, 0.5f);
            batch.quad(x, y + radius, x + w, y + h - radius, 0.0f, 0.5f, 1.0f, 0.5f);
            batch.quad(x, y + h - radius, x + w, y + h, 0.0f, 0.5f, 1.0f, 1.0f);
        }
        batch.submitTextured(context, class_10799.field_56883, Smooth.lens(radius, Smooth.scale(context), kind), color);
    }

    private static float bend(int w, int h) {
        return Math.max(2.0f, Math.min(13.0f, (float)Math.min(w, h) * 0.13f));
    }

    private static void liquidRound(class_332 context, int x, int y, int w, int h, int r, float opacity, boolean blurred) {
        Backdrop.draw(context, x, y, w, h, r, opacity, Smooth.bend(w, h));
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf((blurred ? 0x5C000000 : 0x64000000) | Theme.glassTint(), opacity));
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf(0x8FFFFFF, opacity));
        Smooth.lens(context, x, y, w, h, 2, Smooth.alphaOf(-16777216, opacity));
        Smooth.lens(context, x, y, w, h, 0, Smooth.alphaOf(-1, opacity));
        Smooth.lens(context, x, y, w, h, 1, Smooth.alphaOf(0xFF000000 | Theme.glassRim(), opacity));
    }

    public static void liquid(class_332 context, int x, int y, int w, int h, int r, float opacity, boolean blurred) {
        Smooth.liquid(context, x, y, w, h, r, opacity, blurred, true);
    }

    public static void liquid(class_332 context, int x, int y, int w, int h, int r, float opacity, boolean blurred, boolean shine) {
        if (w <= 0 || h <= 0 || opacity <= 0.01f) {
            return;
        }
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        Smooth.liquidBody(context, x, y, w, h, r, opacity, blurred);
        if (shine && Theme.shimmer()) {
            Smooth.shimmer(context, x, y, w, h, opacity);
        }
    }

    public static void shimmer(class_332 context, int x, int y, int w, int h, float opacity) {
        float run;
        float cycle;
        if (w <= 6 || h <= 6 || opacity <= 0.01f) {
            return;
        }
        double seconds = (double)System.nanoTime() / 1.0E9;
        float t = (float)((seconds + (double)x * 0.003 + (double)y * 0.0015) % (double)(cycle = 4.6f));
        if (t > (run = 1.5f)) {
            return;
        }
        float p = t / run;
        float eased = p * p * (3.0f - 2.0f * p);
        float slant = (float)h * 0.6f;
        float travel = (float)w + slant * 2.0f + 24.0f;
        float cx = (float)x - slant - 12.0f + travel * eased;
        float strength = opacity * (float)Math.sin((double)p * Math.PI);
        int soft = Smooth.alphaOf(0x22FFFFFF, strength);
        int line = Smooth.alphaOf(0x46FFFFFF, strength);
        float band = Math.max(6.0f, Math.min((float)h * 0.5f, 22.0f));
        context.method_44379(x + 1, y + 1, x + w - 1, y + h - 1);
        Smooth.polyline(context, new float[]{cx - slant / 2.0f, cx + slant / 2.0f}, new float[]{(float)(y + h) + 2.0f, (float)y - 2.0f}, new float[]{band, band}, new int[]{soft, soft}, 2);
        Smooth.polyline(context, new float[]{cx - slant / 2.0f + band * 0.7f, cx + slant / 2.0f + band * 0.7f}, new float[]{(float)(y + h) + 2.0f, (float)y - 2.0f}, new float[]{1.4f, 1.4f}, new int[]{line, line}, 2);
        context.method_44380();
    }

    private static void liquidBody(class_332 context, int x, int y, int w, int h, int r, float opacity, boolean blurred) {
        int step;
        if (Backdrop.glass(context, x, y, w, h, r, opacity, Smooth.bend(w, h), Theme.glassTint())) {
            return;
        }
        if (r > 2 && r * 2 >= Math.min(w, h) - 1) {
            Smooth.liquidRound(context, x, y, w, h, r, opacity, blurred);
            return;
        }
        float bend = Smooth.bend(w, h);
        Backdrop.draw(context, x, y, w, h, r, opacity, bend);
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf((blurred ? 0x5C000000 : 0x64000000) | Theme.glassTint(), opacity));
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf(0x6FFFFFF, opacity));
        Smooth.sheen(context, x, y, w, h, r, Smooth.alphaOf(0xEFFFFFF, opacity));
        int depth = Math.max(2, h * 2 / 5);
        int inset = Math.max(1, r / 2);
        if (h > r * 2 + 2 && w > inset * 2) {
            context.method_25296(x + inset, y + h - depth, x + w - inset, y + h - Math.max(1, r / 2), Smooth.alphaOf(0, opacity), Smooth.alphaOf(0x16000000, opacity));
        }
        int glowSteps = Math.max(1, Math.round(bend / 2.5f));
        for (int i = 0; i < glowSteps && w > (step = 3 + i * 2) * 2 + 2 && h > step * 2 + 2; ++i) {
            float fall = 1.0f - (float)i / (float)glowSteps;
            Smooth.outline(context, x + step, y + step, w - step * 2, h - step * 2, Math.max(1, r - step), Smooth.alphaOf(Math.round(12.0f * fall * fall) << 24 | 0xFFFFFF, opacity));
        }
        Smooth.outline(context, x, y, w, h, r, Smooth.alphaOf(0x40FFFFFF, opacity));
        if (w > 4 && h > 4) {
            Smooth.outline(context, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), Smooth.alphaOf(0x2A000000, opacity));
        }
        if (w > 6 && h > 6) {
            Smooth.outline(context, x + 2, y + 2, w - 4, h - 4, Math.max(1, r - 2), Smooth.alphaOf(0x14FFFFFF, opacity));
        }
        int bright = Smooth.alphaOf(-1090519041, opacity);
        int soft = Smooth.alphaOf(0x7A000000 | Theme.glassRim(), opacity);
        int clear = Smooth.alphaOf(0xFFFFFF, opacity);
        Smooth.arcs(context, x, y, w, h, r, bright, 1);
        Smooth.arcs(context, x, y, w, h, r, soft, 8);
        int span = w - r * 2;
        int rise = h - r * 2;
        if (span > 2) {
            Smooth.horizontalGradient(context, x + r, y, x + w - r, y + 1, bright, clear);
            Smooth.horizontalGradient(context, x + r, y + h - 1, x + w - r, y + h, clear, soft);
        }
        if (span > 8 && h > 6) {
            int middle = x + r + span / 2;
            int line = Smooth.alphaOf(0x2EFFFFFF, opacity);
            Smooth.horizontalGradient(context, x + r, y + 1, middle, y + 2, clear, line);
            Smooth.horizontalGradient(context, middle, y + 1, x + w - r, y + 2, line, clear);
        }
        if (rise > 2) {
            context.method_25296(x, y + r, x + 1, y + h - r, bright, clear);
            context.method_25296(x + w - 1, y + r, x + w, y + h - r, clear, soft);
        }
    }

    public static void chip(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
        int span;
        if (w <= 0 || h <= 0 || opacity <= 0.01f) {
            return;
        }
        r = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf(0x34000000, opacity));
        if (hover > 0.01f) {
            Smooth.rect(context, x, y, w, h, r, Smooth.alphaOf(Math.round(14.0f * hover) << 24 | 0xFFFFFF, opacity));
        }
        if ((span = w - r * 2) > 4 && h > 8) {
            int depth = Math.max(2, Math.min(6, h / 5));
            context.method_25296(x + r / 2, y + 1, x + w - r / 2, y + 1 + depth, Smooth.alphaOf(0x2C000000, opacity), Smooth.alphaOf(0, opacity));
        }
        Smooth.outline(context, x, y, w, h, r, Smooth.alphaOf(Math.round(22.0f + 38.0f * hover) << 24 | 0xFFFFFF, opacity));
        if (span > 4 && h > 6) {
            int middle = x + r + span / 2;
            int clear = Smooth.alphaOf(0xFFFFFF, opacity);
            int line = Smooth.alphaOf(Math.round(38.0f + 42.0f * hover) << 24 | 0xFFFFFF, opacity);
            Smooth.horizontalGradient(context, x + r, y + h - 1, middle, y + h, clear, line);
            Smooth.horizontalGradient(context, middle, y + h - 1, x + w - r, y + h, line, clear);
        }
    }

    private static void arcs(class_332 context, int x, int y, int w, int h, int r, int color, int mask) {
        if (color >>> 24 == 0 || w <= 0 || h <= 0) {
            return;
        }
        Smooth.ensureCreated();
        r = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        GuiBatch batch = BATCH.reset();
        Smooth.corners(batch, x, y, w, h, r, mask);
        batch.submitTextured(context, class_10799.field_56883, Smooth.disc(r, Smooth.scale(context), true), color);
    }

    public static void sheen(class_332 context, int x, int y, int w, int h, int r, int color) {
        if (color >>> 24 == 0 || w <= 0 || h <= 0) {
            return;
        }
        int top = Math.max(1, r);
        Smooth.rect(context, x, y, w, top, top, color, 3);
        int depth = Math.min(h - r, Math.max(top + 2, h * 3 / 5));
        if (depth > top) {
            context.method_25296(x, y + top, x + w, y + depth, color, color & 0xFFFFFF);
        }
    }

    private static int alphaOf(int color, float opacity) {
        int a = Math.round((float)(color >>> 24) * Math.max(0.0f, Math.min(1.0f, opacity)));
        return a << 24 | color & 0xFFFFFF;
    }

    public static void polygon(class_332 context, float[] corners, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        Smooth.polygon(context, corners, new int[]{color, color, color, color});
    }

    public static void polygon(class_332 context, float[] corners, int[] colors) {
        if (corners.length < 8 || colors.length < 4) {
            return;
        }
        Smooth.ensureCreated();
        GuiQuads quads = LINES.reset();
        quads.quad(corners, CENTER_UV, colors);
        quads.submit(context, class_10799.field_56883, CIRCLE);
    }

    public static void polyline(class_332 context, float[] xs, float[] ys, float[] widths, int[] colors, int count) {
        if (count < 2) {
            return;
        }
        Smooth.ensureCreated();
        float feather = 1.1f / Math.max(0.5f, Smooth.pixelsPerUnit(context));
        GuiQuads quads = LINES.reset();
        int i = 0;
        while (i + 1 < count) {
            float x1 = xs[i + 1];
            float x0 = xs[i];
            float dx = x1 - x0;
            float y1 = ys[i + 1];
            float y0 = ys[i];
            float dy = y1 - y0;
            float length = (float)Math.sqrt(dx * dx + dy * dy);
            if (!(length < 1.0E-4f)) {
                float nx = -dy / length;
                float ny = dx / length;
                float h0 = widths[i] / 2.0f;
                float h1 = widths[i + 1] / 2.0f;
                int c0 = colors[i];
                int c1 = colors[i + 1];
                int t0 = c0 & 0xFFFFFF;
                int t1 = c1 & 0xFFFFFF;
                if (c0 >>> 24 != 0 || c1 >>> 24 != 0) {
                    quads.quad(new float[]{x0 + nx * h0, y0 + ny * h0, x1 + nx * h1, y1 + ny * h1, x1 - nx * h1, y1 - ny * h1, x0 - nx * h0, y0 - ny * h0}, CENTER_UV, new int[]{c0, c1, c1, c0});
                    float o0 = h0 + feather;
                    float o1 = h1 + feather;
                    quads.quad(new float[]{x0 + nx * o0, y0 + ny * o0, x1 + nx * o1, y1 + ny * o1, x1 + nx * h1, y1 + ny * h1, x0 + nx * h0, y0 + ny * h0}, CENTER_UV, new int[]{t0, t1, c1, c0});
                    quads.quad(new float[]{x0 - nx * h0, y0 - ny * h0, x1 - nx * h1, y1 - ny * h1, x1 - nx * o1, y1 - ny * o1, x0 - nx * o0, y0 - ny * o0}, CENTER_UV, new int[]{c0, c1, t1, t0});
                }
            }
            ++i;
        }
        quads.submit(context, class_10799.field_56883, CIRCLE);
    }

    public static void glow(class_332 context, int cx, int cy, int radius, int color) {
        if (color >>> 24 == 0 || radius <= 0) {
            return;
        }
        Smooth.ensureCreated();
        context.method_25293(class_10799.field_56883, GLOW, cx - radius, cy - radius, 0.0f, 0.0f, radius * 2, radius * 2, 128, 128, 128, 128, color);
    }

    public static void softDot(class_332 context, float cx, float cy, float radius, int color) {
        if (color >>> 24 == 0 || radius <= 0.0f) {
            return;
        }
        Smooth.ensureCreated();
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        float s = radius / 16.0f;
        matrices.scale(s, s);
        context.method_25293(class_10799.field_56883, GLOW, -16, -16, 0.0f, 0.0f, 32, 32, 128, 128, 128, 128, color);
        matrices.popMatrix();
    }

    public static void horizontalGradient(class_332 context, int x1, int y1, int x2, int y2, int left, int right) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)x1, (float)y2);
        matrices.rotate(-1.5707964f);
        context.method_25296(0, 0, y2 - y1, x2 - x1, left, right);
        matrices.popMatrix();
    }

    public static void wave(class_332 context, int cx, int cy, int radius, int color) {
        if (color >>> 24 == 0 || radius <= 0) {
            return;
        }
        Smooth.ensureCreated();
        context.method_25293(class_10799.field_56883, WAVE, cx - radius, cy - radius, 0.0f, 0.0f, radius * 2, radius * 2, 256, 256, 256, 256, color);
    }

    public static void logo(class_332 context, int x, int y, int size, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        Smooth.ensureCreated();
        int index = Smooth.logoIndex(size);
        int texture = LOGO_SIZES[index];
        context.method_25293(class_10799.field_56883, LOGOS[index], x, y, 0.0f, 0.0f, size, size, texture, texture, texture, texture, color);
    }

    public static void brand(class_332 context, int x, int y, int size, float scale, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        int pixels = Math.max(8, Math.min(384, Math.round((float)size * scale * Smooth.pixelScale())));
        class_2960 texture = Smooth.brandTexture(pixels);
        if (texture == null) {
            return;
        }
        context.method_25293(class_10799.field_56883, texture, x, y, 0.0f, 0.0f, size, size, pixels, pixels, pixels, pixels, color);
    }

    private static class_2960 brandTexture(int pixels) {
        class_2960 cached = BRANDS.get(pixels);
        if (cached != null) {
            return cached;
        }
        if (brandSource == null) {
            if (brandFailed) {
                return null;
            }
            try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655((String)"albiz", (String)"textures/gui/brand.png"));){
                brandSource = class_1011.method_4309((InputStream)stream);
            }
            catch (Exception e) {
                brandFailed = true;
                SoberClient.LOG.error("Could not load brand logo", (Throwable)e);
                return null;
            }
        }
        if (BRANDS.size() >= 12) {
            Iterator<Map.Entry<Integer, class_2960>> oldest = BRANDS.entrySet().iterator();
            class_310.method_1551().method_1531().method_4615(oldest.next().getValue());
            oldest.remove();
        }
        class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/brand_" + pixels));
        Smooth.register(id, Smooth.downscale(brandSource, pixels, 1.7f));
        BRANDS.put(pixels, id);
        return id;
    }

    public static void brandCrisp(class_332 context, float x, float y, float size, int color) {
        if (color >>> 24 == 0 || size <= 0.0f) {
            return;
        }
        int pixels = Math.max(8, Math.min(384, Math.round(size * Smooth.pixelsPerUnit(context))));
        class_2960 texture = Smooth.brandTexture(pixels);
        if (texture == null) {
            return;
        }
        class_1041 window = class_310.method_1551().method_22683();
        float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
        Matrix3x2fStack matrices = context.method_51448();
        Vector2f origin = matrices.transformPosition(x, y, new Vector2f());
        int left = Math.round(origin.x * ratio);
        int top = Math.round(origin.y * ratio);
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        context.method_25293(class_10799.field_56883, texture, left, top, 0.0f, 0.0f, pixels, pixels, pixels, pixels, pixels, pixels, color);
        matrices.popMatrix();
    }

    static float pixelsPerUnit(class_332 context) {
        Matrix3x2fStack m = context.method_51448();
        double zoom = Math.max(Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01()), Math.sqrt(m.m10() * m.m10() + m.m11() * m.m11()));
        if (!(zoom > 0.01)) {
            zoom = 1.0;
        }
        class_1041 window = class_310.method_1551().method_22683();
        return (float)(zoom * (double)window.method_4506() / (double)Math.max(1, window.method_4502()));
    }

    static class_2960 brandStep(float pixels, boolean white) {
        int step = BRAND_STEPS[BRAND_STEPS.length - 1];
        for (int candidate : BRAND_STEPS) {
            if (!((float)candidate >= pixels)) continue;
            step = candidate;
            break;
        }
        if (brandSource == null && Smooth.brandTexture(step) == null) {
            return null;
        }
        int size = step;
        return (white ? BRAND_WHITE : BRAND_STEP).computeIfAbsent(size, k -> {
            class_1011 image = Smooth.downscale(brandSource, size, 1.7f);
            if (white) {
                for (int y = 0; y < size; ++y) {
                    for (int x = 0; x < size; ++x) {
                        image.method_61941(x, y, image.method_61940(x, y) & 0xFF000000 | 0xFFFFFF);
                    }
                }
            }
            class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/brand_" + (white ? "white_" : "step_") + size));
            Smooth.register(id, image);
            return id;
        });
    }

    private static class_1011 downscale(class_1011 source, int size, float boost) {
        int width = source.method_4307();
        int height = source.method_4323();
        float ratioX = (float)width / (float)size;
        float ratioY = (float)height / (float)size;
        class_1011 image = new class_1011(size, size, false);
        for (int oy = 0; oy < size; ++oy) {
            float y0 = (float)oy * ratioY;
            float y1 = y0 + ratioY;
            for (int ox = 0; ox < size; ++ox) {
                float x0 = (float)ox * ratioX;
                float x1 = x0 + ratioX;
                double a = 0.0;
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double total = 0.0;
                for (int sy = (int)y0; sy < Math.min(height, (int)Math.ceil(y1)); ++sy) {
                    float wy = Math.min(y1, (float)(sy + 1)) - Math.max(y0, (float)sy);
                    for (int sx = (int)x0; sx < Math.min(width, (int)Math.ceil(x1)); ++sx) {
                        float w = wy * (Math.min(x1, (float)(sx + 1)) - Math.max(x0, (float)sx));
                        int argb = source.method_61940(sx, sy);
                        double alpha = (double)(argb >>> 24) / 255.0 * (double)w;
                        a += alpha;
                        r += (double)(argb >> 16 & 0xFF) * alpha;
                        g += (double)(argb >> 8 & 0xFF) * alpha;
                        b += (double)(argb & 0xFF) * alpha;
                        total += (double)w;
                    }
                }
                if (a <= 0.0 || total <= 0.0) {
                    image.method_61941(ox, oy, 0);
                    continue;
                }
                int alpha = (int)Math.round(Math.min(1.0, a / total * (double)boost) * 255.0);
                image.method_61941(ox, oy, alpha << 24 | (int)Math.round(r / a) << 16 | (int)Math.round(g / a) << 8 | (int)Math.round(b / a));
            }
        }
        return image;
    }

    static {
        for (int i = 0; i < LOGO_SIZES.length; ++i) {
            Smooth.LOGOS[i] = class_2960.method_60655((String)"albiz", (String)("dynamic/logo_" + LOGO_SIZES[i]));
        }
        WAVE = class_2960.method_60655((String)"albiz", (String)"dynamic/wave");
        TRIANGLE = class_2960.method_60655((String)"albiz", (String)"dynamic/triangle");
        RINGS = new HashMap<Integer, class_2960>();
        DISCS = new HashMap<Integer, class_2960>();
        LENSES = new HashMap<Integer, class_2960>();
        BRANDS = new LinkedHashMap<Integer, class_2960>();
        BATCH = new GuiBatch();
        LINES = new GuiQuads();
        CENTER_UV = new float[]{0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
        BRAND_STEPS = new int[]{32, 48, 64, 96, 128, 192, 256, 384};
        BRAND_WHITE = new HashMap<Integer, class_2960>();
        BRAND_STEP = new HashMap<Integer, class_2960>();
    }

    private static final class LinearTexture
    extends class_1043 {
        LinearTexture(Supplier<String> name, class_1011 image) {
            super(name, image);
        }

        public class_12137 method_75484() {
            return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        }
    }
}

