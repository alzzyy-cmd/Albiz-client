// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Smooth;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

final class SharpTexture {
    private static final float STEP = 0.84f;
    private final class_2960 source;
    private final String key;
    private final Map<Integer, class_2960> levels = new HashMap<Integer, class_2960>();
    private final Map<Integer, int[]> sizes = new HashMap<Integer, int[]>();
    private class_1011 image;
    private boolean failed;

    SharpTexture(String path) {
        this.source = class_2960.method_60655((String)"albiz", (String)path);
        this.key = path.replace('/', '_').replace(".png", "");
    }

    private boolean load(){
      if(image!=null)return true;if(failed)return false;
      try(InputStream stream=class_310.method_1551().method_1478().open(source)){image=class_1011.method_4309(stream);return true;}
      catch(Exception ex){failed=true;SoberClient.LOG.error("Could not load {}",source,ex);return false;}
    }

    float aspect() {
        return this.load() ? (float)this.image.method_4323() / (float)this.image.method_4307() : 1.0f;
    }

    void draw(class_332 context, float x, float y, float w, float h, int color) {
        this.draw(context, x, y, w, h, w, color);
    }

    void draw(class_332 context, float x, float y, float w, float h, float levelWidth, int color) {
        if (color >>> 24 == 0 || w <= 0.0f || h <= 0.0f || !this.load()) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        float scale = (float)Math.sqrt(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01());
        float pixels = levelWidth * scale * (float)class_310.method_1551().method_22683().method_4495();
        int level = pixels >= (float)this.image.method_4307() ? 0 : Math.max(0, (int)Math.floor(Math.log(pixels / (float)this.image.method_4307()) / Math.log(0.84f)));
        class_2960 id = this.levels.get(level);
        if (id == null) {
            id = this.create(level);
        }
        int[] size = this.sizes.get(level);
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(w / (float)size[0], h / (float)size[1]);
        context.method_25293(class_10799.field_56883, id, 0, 0, 0.0f, 0.0f, size[0], size[1], size[0], size[1], size[0], size[1], color);
        matrices.popMatrix();
    }

    private class_2960 create(int level) {
        class_1011 scaled;
        int w = Math.max(1, Math.round((float)this.image.method_4307() * (float)Math.pow(0.84f, level)));
        int h = Math.max(1, Math.round((float)this.image.method_4323() * (float)Math.pow(0.84f, level)));
        if (level == 0) {
            scaled = new class_1011(this.image.method_4307(), this.image.method_4323(), false);
            scaled.method_4317(this.image);
            w = this.image.method_4307();
            h = this.image.method_4323();
        } else {
            scaled = SharpTexture.downscale(this.image, w, h);
        }
        class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/" + this.key + "_" + level));
        Smooth.register(id, scaled);
        this.levels.put(level, id);
        this.sizes.put(level, new int[]{w, h});
        return id;
    }

    static class_1011 downscale(class_1011 source, int width, int height) {
        int sw = source.method_4307();
        int sh = source.method_4323();
        float rx = (float)sw / (float)width;
        float ry = (float)sh / (float)height;
        class_1011 out = new class_1011(width, height, false);
        for (int oy = 0; oy < height; ++oy) {
            float y0 = (float)oy * ry;
            float y1 = y0 + ry;
            for (int ox = 0; ox < width; ++ox) {
                float x0 = (float)ox * rx;
                float x1 = x0 + rx;
                double a = 0.0;
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double total = 0.0;
                for (int sy = (int)y0; sy < Math.min(sh, (int)Math.ceil(y1)); ++sy) {
                    float wy = Math.min(y1, (float)(sy + 1)) - Math.max(y0, (float)sy);
                    for (int sx = (int)x0; sx < Math.min(sw, (int)Math.ceil(x1)); ++sx) {
                        float weight = wy * (Math.min(x1, (float)(sx + 1)) - Math.max(x0, (float)sx));
                        int argb = source.method_61940(sx, sy);
                        double alpha = (double)(argb >>> 24) / 255.0 * (double)weight;
                        a += alpha;
                        r += (double)(argb >> 16 & 0xFF) * alpha;
                        g += (double)(argb >> 8 & 0xFF) * alpha;
                        b += (double)(argb & 0xFF) * alpha;
                        total += (double)weight;
                    }
                }
                if (a <= 0.0 || total <= 0.0) {
                    out.method_61941(ox, oy, 0);
                    continue;
                }
                int alpha = (int)Math.round(Math.min(1.0, a / total) * 255.0);
                out.method_61941(ox, oy, alpha << 24 | (int)Math.round(r / a) << 16 | (int)Math.round(g / a) << 8 | (int)Math.round(b / a));
            }
        }
        return out;
    }
}

