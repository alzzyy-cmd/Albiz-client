// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;
import net.albiz.client.port.soberui.model.SpriteGeometryBuilder.Geometry;

import net.albiz.client.port.soberui.model.SmoothTexture;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class SpriteModel {
    private static final int SIZE = 256;
    private static final int GRID = 128;
    private static final int ICON_SIZE = 64;
    private static final float THICKNESS = 0.041666668f;
    private static final float CUTOFF = 0.1f;
    private static final float SOFTNESS = 2.2f;
    private static final float SIMPLIFY = 0.3f;
    private static final int SIDE = -3552046;
    private static int counter;
    public final class_2960 texture;
    public final class_2960 icon;
    public final int quads;
    public final float[] positions;
    public final float[] uvs;
    public final float[] normals;
    public final int[] colors;

    private SpriteModel(class_2960 texture, class_2960 icon, Geometry geometry) {
        this.texture = texture;
        this.icon = icon;
        this.quads = geometry.count;
        this.positions = Arrays.copyOf(geometry.p, geometry.count * 12);
        this.uvs = Arrays.copyOf(geometry.t, geometry.count * 8);
        this.normals = Arrays.copyOf(geometry.n, geometry.count * 12);
        this.colors = Arrays.copyOf(geometry.c, geometry.count);
    }

    static Prepared prepare(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file, new OpenOption[0]);){
            Prepared prepared = SpriteModel.prepare(in);
            return prepared;
        }
    }

    static Prepared prepare(InputStream in) throws IOException {
        int i;
        java.io.BufferedInputStream checked=new java.io.BufferedInputStream(in);checked.mark(32);byte[] header=checked.readNBytes(24);checked.reset();
        SpritePngBudget.check(header);
        in=checked;
        class_1011 source = class_1011.method_4309((InputStream)in);
        int sw = source.method_4307();
        int sh = source.method_4323();
        int[] pixels = new int[sw * sh];
        for (int y = 0; y < sh; ++y) {
            for (int x = 0; x < sw; ++x) {
                pixels[y * sw + x] = source.method_61940(x, y);
            }
        }
        source.close();
        SpriteGeometryBuilder.bleed(pixels, sw, sh);
        float aspect = (float)sw / (float)sh;
        int w = aspect >= 1.0f ? 256 : Math.max(1, Math.round(256.0f * aspect));
        int h = aspect >= 1.0f ? Math.max(1, Math.round(256.0f / aspect)) : 256;
        int[] main = sw > w || sh > h ? SpriteGeometryBuilder.downscale(pixels, sw, sh, w, h) : SpriteGeometryBuilder.upscale(pixels, sw, sh, w, h);
        SpriteGeometryBuilder.bleed(main, w, h);
        float[] alpha = new float[w * h];
        for (i = 0; i < alpha.length; ++i) {
            alpha[i] = (float)(main[i] >>> 24) / 255.0f;
        }
        alpha = SpriteGeometryBuilder.soften(alpha, w, h, 2.2f * (float)Math.max(w, h) / 256.0f);
        for (i = 0; i < alpha.length; ++i) {
            main[i] = Math.round(alpha[i] * 255.0f) << 24 | main[i] & 0xFFFFFF;
        }
        int gw = Math.max(1, w * 256 / 512);
        int gh = Math.max(1, h * 256 / 512);
        float[] grid = SpriteGeometryBuilder.shrink(alpha, w, h, gw, gh);
        Geometry geometry = new Geometry(Math.max(64, gw * gh / 6));
        float front = 0.5208333f;
        float back = 0.47916666f;
        geometry.quad(0.0f, 1.0f, front, 0.0f, 0.0f, 0.0f, 0.0f, front, 0.0f, 1.0f, 1.0f, 0.0f, front, 1.0f, 1.0f, 1.0f, 1.0f, front, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1);
        geometry.quad(1.0f, 1.0f, back, 1.0f, 0.0f, 1.0f, 0.0f, back, 1.0f, 1.0f, 0.0f, 0.0f, back, 0.0f, 1.0f, 0.0f, 1.0f, back, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, -1);
        SpriteGeometryBuilder.contour(geometry, grid, gw, gh, front, back);
        int[] iconPixels = SpriteGeometryBuilder.downscale(main, w, h, 64, 64);
        SpriteGeometryBuilder.bleed(iconPixels, 64, 64);
        return new Prepared(main, w, h, iconPixels, geometry);
    }

    static SpriteModel upload(Prepared prepared) {
        class_310 mc = class_310.method_1551();
        int id = counter++;
        class_2960 texture = class_2960.method_60655((String)"albiz", (String)("sprite/model_" + id));
        class_2960 icon = class_2960.method_60655((String)"albiz", (String)("sprite/icon_" + id));
        mc.method_1531().method_4616(texture, (class_1044)new SmoothTexture(() -> "sober sprite " + String.valueOf(texture), SpriteModel.image(prepared.main, prepared.width, prepared.height)));
        mc.method_1531().method_4616(icon, (class_1044)new SmoothTexture(() -> "sober sprite icon " + String.valueOf(icon), SpriteModel.image(prepared.icon, 64, 64)));
        return new SpriteModel(texture, icon, prepared.geometry);
    }

    private static class_1011 image(int[] pixels, int w, int h) {
        class_1011 image = new class_1011(w, h, false);
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                image.method_61941(x, y, pixels[y * w + x]);
            }
        }
        return image;
    }

    void release() {
        class_310 mc = class_310.method_1551();
        mc.method_1531().method_4615(this.texture);
        mc.method_1531().method_4615(this.icon);
    }

    static final class Prepared {
        private final int[] main;
        private final int width;
        private final int height;
        private final int[] icon;
        private final Geometry geometry;

        private Prepared(int[] main, int width, int height, int[] icon, Geometry geometry) {
            this.main = main;
            this.width = width;
            this.height = height;
            this.icon = icon;
            this.geometry = geometry;
        }
    }

}
