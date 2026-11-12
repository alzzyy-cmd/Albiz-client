// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Smooth;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_7532;
import net.minecraft.class_8685;

public final class Avatars {
    private static final int SIZE = 64;
    private static final Map<class_2960, class_2960> CACHE = new HashMap<class_2960, class_2960>();

    private Avatars() {
    }

    public static void draw(class_332 context, class_8685 skin, int x, int y, int size) {
        class_2960 face = Avatars.face(skin.comp_1626().comp_3627());
        if (face == null) {
            class_7532.method_52722((class_332)context, (class_8685)skin, (int)x, (int)y, (int)size);
            return;
        }
        context.method_25293(class_10799.field_56883, face, x, y, 0.0f, 0.0f, size, size, 64, 64, 64, 64, -1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static class_2960 face(class_2960 skin) {
        if (CACHE.containsKey(skin)) {
            return CACHE.get(skin);
        }
        class_2960 result = null;
        class_1011 image = null;
        boolean owned = false;
        try {
            class_310 mc = class_310.method_1551();
            class_1044 texture = mc.method_1531().method_4619(skin);
            if (texture instanceof class_1043) {
                class_1043 backed = (class_1043)texture;
                image = backed.method_4525();
            }
            if (image == null) {
                try (InputStream stream = mc.method_1478().open(skin);){
                    image = class_1011.method_4309((InputStream)stream);
                    owned = true;
                }
            }
            if (image != null && image.method_4307() >= 64) {
                int scale = image.method_4307() / 64;
                int[] pixels = new int[64];
                for (int py = 0; py < 8; ++py) {
                    for (int px = 0; px < 8; ++px) {
                        int base = image.method_61940((8 + px) * scale, (8 + py) * scale) | 0xFF000000;
                        int hat = image.method_61940((40 + px) * scale, (8 + py) * scale);
                        pixels[py * 8 + px] = Avatars.blend(base, hat);
                    }
                }
                int[] up = pixels;
                for (int side = 8; side < 64; side *= 2) {
                    up = Avatars.scale2x(up, side);
                }
                class_1011 out = new class_1011(64, 64, false);
                for (int py = 0; py < 64; ++py) {
                    for (int px = 0; px < 64; ++px) {
                        out.method_61941(px, py, up[py * 64 + px]);
                    }
                }
                result = class_2960.method_60655((String)"albiz", (String)("dynamic/avatar_" + Integer.toHexString(skin.hashCode())));
                Smooth.register(result, out);
            }
        }
        catch (Exception e) {
            SoberClient.LOG.debug("Could not build avatar for {}", (Object)skin, (Object)e);
        }
        finally {
            if (owned && image != null) {
                image.close();
            }
        }
        CACHE.put(skin, result);
        return result;
    }

    private static int blend(int base, int top) {
        int a = top >>> 24;
        if (a == 0) {
            return base;
        }
        if (a == 255) {
            return top;
        }
        int r = ((top >> 16 & 0xFF) * a + (base >> 16 & 0xFF) * (255 - a)) / 255;
        int g = ((top >> 8 & 0xFF) * a + (base >> 8 & 0xFF) * (255 - a)) / 255;
        int b = ((top & 0xFF) * a + (base & 0xFF) * (255 - a)) / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int[] scale2x(int[] source, int side) {
        int out = side * 2;
        int[] result = new int[out * out];
        for (int y = 0; y < side; ++y) {
            for (int x = 0; x < side; ++x) {
                int p = source[y * side + x];
                int a = source[Math.max(0, y - 1) * side + x];
                int b = source[y * side + Math.min(side - 1, x + 1)];
                int c = source[y * side + Math.max(0, x - 1)];
                int d = source[Math.min(side - 1, y + 1) * side + x];
                result[y * 2 * out + x * 2] = c == a && c != d && a != b ? a : p;
                result[y * 2 * out + x * 2 + 1] = a == b && a != c && b != d ? b : p;
                result[(y * 2 + 1) * out + x * 2] = d == c && d != b && c != a ? c : p;
                result[(y * 2 + 1) * out + x * 2 + 1] = b == d && b != a && d != c ? d : p;
            }
        }
        return result;
    }
}

