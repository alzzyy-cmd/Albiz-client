package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.SharpTexture;
import net.albiz.client.port.soberui.gui.Smooth;
import java.util.Random;
import net.minecraft.class_332;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

final class IslandScene {
    private static final float GROUP = 1447.0f;
    private static final float CANVAS_W = 1494.0f;
    private static final float SEA_X = 807.8f;
    private static final float SEA_Y = 1351.0f;
    private static final float BASE_X = 807.8f;
    private static final float BASE_Y = 1222.3f;
    private static final float CROWN_X = 661.4f;
    private static final float CROWN_Y = 326.2f;
    private static final float WATER_RX = 578.0f;
    private static final float WATER_RY = 79.0f;
    private static final float[] ISLAND_BOX = new float[]{137.0f, 1142.0f, 1331.0f, 331.0f};
    private static final float[] TRUNK_BOX = new float[]{619.0f, 290.0f, 288.0f, 973.0f};
    private static final float[] BACK_BOX = new float[]{140.0f, 26.0f, 1070.0f, 472.0f};
    private static final float[] FRONT_BOX = new float[]{26.0f, 64.0f, 1313.0f, 708.0f};
    private static final float[][] CLOUDS = new float[][]{{0.08f, 0.4f, 0.17f, 0.0f, 0.0075f, 1.0f}, {0.62f, 0.33f, 0.15f, 1.0f, 0.0095f, 1.0f}, {0.3f, 0.52f, 0.075f, 3.0f, 0.0042f, 0.92f}, {0.86f, 0.55f, 0.09f, 2.0f, 0.005f, 0.92f}, {0.47f, 0.47f, 0.06f, 0.0f, 0.0035f, 0.85f}, {0.18f, 0.08f, 0.11f, 2.0f, 0.006f, 0.9f}, {0.74f, 0.05f, 0.12f, 3.0f, 0.0068f, 0.9f}};
    private static final float[][] SPARKLES = new float[22][4];
    private static final SharpTexture ISLAND = new SharpTexture("textures/menu/island3d.png");
    private static final SharpTexture TRUNK = new SharpTexture("textures/menu/trunk3d.png");
    private static final SharpTexture CROWN_BACK = new SharpTexture("textures/menu/crown_back.png");
    private static final SharpTexture CROWN_FRONT = new SharpTexture("textures/menu/crown_front.png");
    private static final SharpTexture SEA = new SharpTexture("textures/menu/sea.png");
    private static final SharpTexture FOAM = new SharpTexture("textures/menu/foam.png");
    private static final SharpTexture SHALLOW = new SharpTexture("textures/menu/shallow.png");
    private static final SharpTexture BIRD = new SharpTexture("textures/menu/bird.png");
    private static final SharpTexture[] CLOUD = new SharpTexture[4];
    private static final SharpTexture TITLE = new SharpTexture("textures/brand/wordmark.png");
    private static float parallaxX;
    private static float parallaxY;

    private IslandScene() {
    }

    private static int alpha(int rgb, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | rgb & 0xFFFFFF;
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float easeOut(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f * f;
    }

    static float horizon(int height) {
        return (float)height * 0.635f;
    }

    static float scale(int width, int height) {
        return Math.min((float)height * (height < 300 ? 0.47f : 0.5f) / 1447.0f, (float)width * 0.62f / 1494.0f);
    }

    static float seaLevel(int height) {
        return (float)height * 0.758f;
    }

    static float islandBottom(int width, int height) {
        return IslandScene.seaLevel(height) + (ISLAND_BOX[1] + ISLAND_BOX[3] - 1351.0f) * IslandScene.scale(width, height);
    }

    static void background(class_332 context, int width, int height, float t, int mouseX, int mouseY, float delta) {
        double now = (double)System.nanoTime() / 1.0E9;
        float targetX = (float)mouseX / (float)Math.max(1, width) - 0.5f;
        float targetY = (float)mouseY / (float)Math.max(1, height) - 0.5f;
        float follow = 1.0f - (float)Math.exp(-3.5f * delta);
        parallaxX += (targetX - parallaxX) * follow;
        parallaxY += (targetY - parallaxY) * follow;
        float horizon = IslandScene.horizon(height);
        IslandScene.sky(context, width, height, horizon);
        Matrix3x2fStack matrices = context.method_51448();
        float zoom = 1.0f + 0.07f * (1.0f - IslandScene.easeOut(IslandScene.clamp(t / 2.2f)));
        matrices.pushMatrix();
        matrices.translate((float)width / 2.0f, horizon);
        matrices.scale(zoom, zoom);
        matrices.translate((float)(-width) / 2.0f, -horizon);
        Smooth.glow(context, width / 2, Math.round((float)height * 0.16f), Math.round(Math.min((float)width, (float)height * 1.6f) * 0.42f), IslandScene.alpha(0xFFFFFF, 0.16f));
        IslandScene.clouds(context, width, height, now, 0.6f);
        IslandScene.birds(context, width, height, now);
        IslandScene.sea(context, width, height, horizon, now);
        IslandScene.island(context, width, height, horizon, t, now);
        matrices.popMatrix();
    }

    private static void sky(class_332 context, int width, int height, float horizon) {
        int h = Math.round(horizon);
        int a = Math.round(horizon * 0.45f);
        int b = Math.round(horizon * 0.8f);
        context.method_25296(0, 0, width, a, -13668400, -12151832);
        context.method_25296(0, a, width, b, -12151832, -10637586);
        context.method_25296(0, b, width, h, -10637586, -7678984);
        int sea1 = h + Math.round((float)(height - h) * 0.12f);
        int sea2 = h + Math.round((float)(height - h) * 0.55f);
        context.method_25296(0, h, width, sea1, -12941610, -12611622);
        context.method_25296(0, sea1, width, sea2, -12611622, -12082470);
        context.method_25296(0, sea2, width, height, -12082470, -12675640);
        context.method_25296(0, Math.round(horizon - (float)height * 0.07f), width, h, 0xFFFFFF, 0x2EFFFFFF);
        context.method_25296(0, h, width, h + Math.max(2, Math.round((float)height * 0.014f)), 0x40FFFFFF, 0xFFFFFF);
    }

    private static void clouds(class_332 context, int width, int height, double now, float depth) {
        for (float[] cloud : CLOUDS) {
            float w = (float)width * cloud[2];
            float h = w * CLOUD[(int)cloud[3]].aspect();
            float span = (float)width + w * 2.0f;
            float x = (float)(((double)(cloud[0] * span) + now * (double)cloud[4] * (double)width) % (double)span) - w;
            float near = cloud[2] / 0.17f;
            float y = (float)height * cloud[1] - h / 2.0f + (float)Math.sin(now * 0.35 + (double)(cloud[0] * 9.0f)) * (float)height * 0.004f - parallaxY * (float)height * 0.006f * near;
            CLOUD[(int)cloud[3]].draw(context, x -= parallaxX * (float)width * 0.012f * near * depth / 0.6f, y, w, h, IslandScene.alpha(0xFFFFFF, cloud[5]));
        }
    }

    private static void birds(class_332 context, int width, int height, double now) {
        for (int i = 0; i < 2; ++i) {
            double period = 22 + i * 7;
            double local = (now + (double)i * 9.5) % period;
            double travel = 15.0;
            if (local > travel) continue;
            float p = (float)(local / travel);
            float size = (float)width * (0.016f - (float)i * 0.004f);
            float x = -size + p * ((float)width + size * 2.0f);
            float y = (float)height * (0.27f + (float)i * 0.06f) + (float)Math.sin(now * 0.8 + (double)i) * (float)height * 0.015f;
            float flap = (float)Math.sin(now * 7.5 + (double)(i * 2));
            float sy = 0.25f + 0.75f * Math.abs(flap);
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(1.0f, flap < 0.0f ? -sy * 0.6f : sy);
            BIRD.draw(context, -size / 2.0f, -size / 4.0f, size, size / 2.0f, size, IslandScene.alpha(1979728, 0.75f));
            matrices.popMatrix();
        }
    }

    private static void sea(class_332 context, int width, int height, float horizon, double now) {
        float seaH = (float)height - horizon;
        float tile = (float)width * 0.75f;
        for (int layer = 0; layer < 2; ++layer) {
            float speed = layer == 0 ? 0.012f : -0.007f;
            float offset = (float)(now * (double)speed * (double)width % (double)tile);
            if (offset > 0.0f) {
                offset -= tile;
            }
            float shift = -parallaxX * (float)width * 0.01f;
            float a = layer == 0 ? 0.55f : 0.32f;
            float pulse = 0.85f + 0.15f * (float)Math.sin(now * 0.9 + (double)(layer * 2));
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            if (layer == 1) {
                matrices.translate((float)width, 0.0f);
                matrices.scale(-1.0f, 1.0f);
            }
            for (float x = offset + shift % tile - tile; x < (float)width + tile; x += tile) {
                SEA.draw(context, x, horizon + (layer == 1 ? seaH * 0.015f : 0.0f), tile, seaH, tile, IslandScene.alpha(0xFFFFFF, a * pulse));
            }
            matrices.popMatrix();
        }
        for (float[] sparkle : SPARKLES) {
            float depth = (float)Math.pow(sparkle[1], 1.4);
            float x = sparkle[0] * (float)width - parallaxX * (float)width * 0.01f * depth;
            float y = horizon + 3.0f + depth * (seaH - 6.0f);
            float twinkle = (float)Math.pow(Math.max(0.0, Math.sin(now * (double)sparkle[2] + (double)sparkle[3])), 14.0);
            if (twinkle < 0.02f) continue;
            float r = 0.8f + 2.6f * depth;
            Smooth.softDot(context, x, y, r * 2.2f, IslandScene.alpha(0xFFFFFF, twinkle * 0.45f));
            Smooth.softDot(context, x, y, r, IslandScene.alpha(0xFFFFFF, twinkle));
        }
    }

    private static void island(class_332 context, int width, int height, float horizon, float t, double now) {
        float k = IslandScene.scale(width, height);
        float cx = (float)width / 2.0f - parallaxX * (float)width * 0.018f;
        float seaY = IslandScene.seaLevel(height);
        int white = IslandScene.alpha(0xFFFFFF, 1.0f);
        float shallowW = 1560.6f * k;
        float shallowH = 363.4f * k;
        SHALLOW.draw(context, cx - shallowW / 2.0f, seaY - shallowH / 2.0f, shallowW, shallowH, IslandScene.alpha(0xFFFFFF, 0.9f));
        float ringW = 1344.186f * k;
        float ringH = 183.72093f * k;
        for (int i = 0; i < 2; ++i) {
            float cycle = (float)((now / 4.2 + (double)i * 0.5) % 1.0);
            float s = 1.0f + cycle * 0.32f;
            FOAM.draw(context, cx - ringW * s / 2.0f, seaY - ringH * (1.0f + cycle * 0.9f) / 2.0f, ringW * s, ringH * (1.0f + cycle * 0.9f), ringW, IslandScene.alpha(0xFFFFFF, 0.42f * (1.0f - cycle) * Math.min(1.0f, cycle * 6.0f)));
        }
        float breathe = 1.0f + 0.01f * (float)Math.sin(now * 1.7);
        FOAM.draw(context, cx - ringW * breathe / 2.0f, seaY - ringH * breathe / 2.0f, ringW * breathe, ringH * breathe, ringW, IslandScene.alpha(0xFFFFFF, 0.8f));
        IslandScene.layer(context, ISLAND, ISLAND_BOX, cx, seaY, k, white);
        float sway = 0.016f * (float)Math.sin(now * 0.85) + 0.006f * (float)Math.sin(now * 2.3 + 1.1);
        if (t > 0.5f && t < 4.0f) {
            float local = t - 0.5f;
            sway += 0.05f * (float)Math.exp(-local * 1.9f) * (float)Math.sin(local * 7.5f);
        }
        float baseX = cx + 0.0f * k;
        float baseY = seaY + -128.69995f * k;
        float crownX = cx + -146.39996f * k;
        float crownY = seaY + -1024.8f * k;
        float crownShift = sway * (baseY - crownY);
        float angle = sway * 0.85f + 0.012f * (float)Math.sin(now * 2.7 + 0.4);
        Matrix3x2fStack matrices = context.method_51448();
        IslandScene.crown(context, CROWN_BACK, BACK_BOX, cx, seaY, k, crownX, crownY, crownShift, angle, white);
        matrices.pushMatrix();
        matrices.translate(baseX, baseY);
        matrices.mul((Matrix3x2fc)new Matrix3x2f(1.0f, 0.0f, -sway, 1.0f, 0.0f, 0.0f));
        matrices.translate(-baseX, -baseY);
        IslandScene.layer(context, TRUNK, TRUNK_BOX, cx, seaY, k, white);
        matrices.popMatrix();
        IslandScene.crown(context, CROWN_FRONT, FRONT_BOX, cx, seaY, k, crownX, crownY, crownShift, angle, white);
    }

    private static void layer(class_332 context, SharpTexture texture, float[] box, float cx, float seaY, float k, int color) {
        texture.draw(context, cx + (box[0] - 807.8f) * k, seaY + (box[1] - 1351.0f) * k, box[2] * k, box[3] * k, color);
    }

    private static void crown(class_332 context, SharpTexture texture, float[] box, float cx, float seaY, float k, float pivotX, float pivotY, float shift, float angle, int color) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(pivotX + shift, pivotY);
        matrices.rotate(angle);
        matrices.translate(-pivotX, -pivotY);
        IslandScene.layer(context, texture, box, cx, seaY, k, color);
        matrices.popMatrix();
    }

    static float titleBottom(int width, int height) {
        float titleW = Math.min((float)width * 0.54f, (float)height * 1.0f);
        return (float)height * 0.215f + titleW * 0.0655f;
    }

    static void title(class_332 context, int width, int height, float t) {
        float p = IslandScene.clamp((t - 0.75f) / 0.85f);
        if (p <= 0.0f) {
            return;
        }
        double now = (double)System.nanoTime() / 1.0E9;
        float titleW = Math.min((float)width * 0.54f, (float)height * 1.0f);
        float w = titleW * 0.62f;
        float h = w * TITLE.aspect();
        float ease = IslandScene.easeOut(p);
        float size = 0.9f + 0.1f * ease;
        float bob = (float)Math.sin(now * 1.15) * (float)height * 0.0035f;
        float rise = (1.0f - ease) * (float)height * 0.025f;
        float cx = (float)width / 2.0f - parallaxX * (float)width * 0.008f;
        float cy = (float)height * 0.215f - titleW * 0.064f - parallaxY * (float)height * 0.008f + bob + rise;
        TITLE.draw(context, cx - w * size / 2.0f, cy - h * size / 2.0f, w * size, h * size, w, IslandScene.alpha(0xFFFFFF, IslandScene.clamp(p * 2.2f)));
    }

    static {
        for (int i = 0; i < CLOUD.length; ++i) {
            IslandScene.CLOUD[i] = new SharpTexture("textures/menu/cloud_" + i + ".png");
        }
        Random random = new Random(1337L);
        for (float[] sparkle : SPARKLES) {
            sparkle[0] = random.nextFloat();
            sparkle[1] = random.nextFloat();
            sparkle[2] = 0.6f + random.nextFloat() * 1.4f;
            sparkle[3] = random.nextFloat() * 6.28f;
        }
    }
}

