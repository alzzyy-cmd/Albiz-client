// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Smooth;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class BrandTextures {
    private static final Map<String, List<Sprite>> CACHE = new HashMap<String, List<Sprite>>();

    private BrandTextures() {
    }

    public static Sprite get(String name) {
        List<Sprite> levels = BrandTextures.levels(name);
        return levels.isEmpty() ? null : levels.get(0);
    }

    public static Sprite fit(String name, float pixels) {
        List<Sprite> levels = BrandTextures.levels(name);
        if (levels.isEmpty()) {
            return null;
        }
        Sprite best = levels.get(0);
        for (Sprite sprite : levels) {
            if (!((float)Math.max(sprite.width(), sprite.height()) >= pixels)) continue;
            best = sprite;
        }
        return best;
    }

    private static List<Sprite> levels(String name) {
        List<Sprite> cached = CACHE.get(name);
        if (cached != null) {
            return cached;
        }
        ArrayList<Sprite> levels = new ArrayList<Sprite>();
        try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655((String)"albiz", (String)("textures/brand/" + name + ".png")));){
            class_1011 image = class_1011.method_4309((InputStream)stream);
            int level = 0;
            while (true) {
                class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/brand_" + name + "_" + level));
                levels.add(new Sprite(id, image.method_4307(), image.method_4323()));
                class_1011 next = image.method_4307() >= 48 && image.method_4323() >= 48 && level < 4 ? BrandTextures.half(image) : null;
                Smooth.register(id, image);
                if (next == null) {
                    break;
                }
                image = next;
                ++level;
            }
        }
        catch (Exception e) {
            SoberClient.LOG.warn("Could not load SoberClient texture {}", (Object)name, (Object)e);
        }
        CACHE.put(name, levels);
        return levels;
    }

    private static class_1011 half(class_1011 src) {
        int w = Math.max(1, src.method_4307() / 2);
        int h = Math.max(1, src.method_4323() / 2);
        class_1011 out = new class_1011(w, h, false);
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                long a = 0L;
                long r = 0L;
                long g = 0L;
                long b = 0L;
                for (int dy = 0; dy < 2; ++dy) {
                    for (int dx = 0; dx < 2; ++dx) {
                        int c = src.method_61940(Math.min(src.method_4307() - 1, x * 2 + dx), Math.min(src.method_4323() - 1, y * 2 + dy));
                        int ca = c >>> 24;
                        a += (long)ca;
                        r += (long)(c >> 16 & 0xFF) * (long)ca;
                        g += (long)(c >> 8 & 0xFF) * (long)ca;
                        b += (long)(c & 0xFF) * (long)ca;
                    }
                }
                int outA = (int)(a / 4L);
                int outR = a == 0L ? 0 : (int)(r / a);
                int outG = a == 0L ? 0 : (int)(g / a);
                int outB = a == 0L ? 0 : (int)(b / a);
                out.method_61941(x, y, outA << 24 | outR << 16 | outG << 8 | outB);
            }
        }
        return out;
    }

    public record Sprite(class_2960 id, int width, int height) {
    }
}

