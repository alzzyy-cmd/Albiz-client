// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.Smooth;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class Icons {
    private static final int SAMPLES = 5;
    private static final float UNITS = 12.0f;
    private static final Map<Integer, class_2960> CACHE = new HashMap<Integer, class_2960>();

    private Icons() {
    }

    public static void draw(class_332 context, Icon icon, int x, int y, int size, int color) {
        if (color >>> 24 == 0 || size <= 0) {
            return;
        }
        int pixels = Math.max(8, Math.min(256, (int)Math.ceil((float)size * Smooth.pixelScale())));
        class_2960 id = CACHE.computeIfAbsent(icon.ordinal() * 1000 + pixels, key -> Icons.build(icon, pixels));
        context.method_25293(class_10799.field_56883, id, x, y, 0.0f, 0.0f, size, size, pixels, pixels, pixels, pixels, color);
    }

    private static class_2960 build(Icon icon, int pixels) {
        class_1011 image = new class_1011(pixels, pixels, false);
        float unit = 12.0f / (float)pixels;
        for (int px = 0; px < pixels; ++px) {
            for (int py = 0; py < pixels; ++py) {
                int hits = 0;
                for (int sx = 0; sx < 5; ++sx) {
                    for (int sy = 0; sy < 5; ++sy) {
                        float u = ((float)px + ((float)sx + 0.5f) / 5.0f) * unit;
                        float v = ((float)py + ((float)sy + 0.5f) / 5.0f) * unit;
                        if (!(Icons.distance(icon, u, v) <= 0.0f)) continue;
                        ++hits;
                    }
                }
                image.method_61941(px, py, Math.round((float)hits * 255.0f / 25.0f) << 24 | 0xFFFFFF);
            }
        }
        class_2960 id = class_2960.method_60655((String)"albiz", (String)("dynamic/icon_" + net.albiz.client.port.soberui.UiResourceNames.lower(icon.name()) + "_" + pixels));
        Smooth.register(id, image);
        return id;
    }

    private static float length(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }

    private static float circle(float u, float v, float cx, float cy, float r) {
        return Icons.length(u - cx, v - cy) - r;
    }

    private static float ring(float u, float v, float cx, float cy, float r, float width) {
        return Math.abs(Icons.length(u - cx, v - cy) - r) - width / 2.0f;
    }

    private static float segment(float u, float v, float ax, float ay, float bx, float by, float width) {
        float ex = bx - ax;
        float ey = by - ay;
        float wx = u - ax;
        float wy = v - ay;
        float t = Math.max(0.0f, Math.min(1.0f, (wx * ex + wy * ey) / (ex * ex + ey * ey)));
        return Icons.length(wx - ex * t, wy - ey * t) - width / 2.0f;
    }

    private static float box(float u, float v, float x1, float y1, float x2, float y2, float r) {
        float cx = (x1 + x2) / 2.0f;
        float cy = (y1 + y2) / 2.0f;
        float hw = (x2 - x1) / 2.0f;
        float hh = (y2 - y1) / 2.0f;
        float qx = Math.abs(u - cx) - hw + r;
        float qy = Math.abs(v - cy) - hh + r;
        return Icons.length(Math.max(qx, 0.0f), Math.max(qy, 0.0f)) + Math.min(Math.max(qx, qy), 0.0f) - r;
    }

    private static float polyline(float u, float v, float width, float ... points) {
        float best = Float.MAX_VALUE;
        int i = 0;
        while (i + 3 < points.length) {
            best = Math.min(best, Icons.segment(u, v, points[i], points[i + 1], points[i + 2], points[i + 3], width));
            i += 2;
        }
        return best;
    }

    private static float shape(float u, float v, float ... points) {
        int n = points.length / 2;
        boolean in = false;
        float edge = Float.MAX_VALUE;
        int k = 0;
        int j = n - 1;
        while (k < n) {
            float yj;
            float xi = points[k * 2];
            float yi = points[k * 2 + 1];
            float xj = points[j * 2];
            if (yi > v != (yj = points[j * 2 + 1]) > v && u < (xj - xi) * (v - yi) / (yj - yi) + xi) {
                in = !in;
            }
            edge = Math.min(edge, Icons.segment(u, v, xj, yj, xi, yi, 0.0f));
            j = k++;
        }
        return in ? -edge : edge;
    }

    private static float heart(float u, float v) {
        return Math.min(Math.min(Icons.circle(u, v, 4.1f, 4.7f, 2.7f), Icons.circle(u, v, 7.9f, 4.7f, 2.7f)), Icons.shape(u, v, 1.55f, 5.6f, 10.45f, 5.6f, 6.0f, 10.6f));
    }

    private static float distance(Icon icon, float u, float v) {
        return switch (icon.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Math.min(Icons.ring(u, v, 5.1f, 5.1f, 3.5f, 1.5f), Icons.segment(u, v, 7.8f, 7.8f, 10.7f, 10.7f, 1.8f));
            case 1 -> Icons.ring(u, v, 6.0f, 6.0f, 3.7f, 2.6f);
            case 2 -> {
                float lens = Math.max(Icons.circle(u, v, 6.0f, 10.6f, 7.4f), Icons.circle(u, v, 6.0f, 1.4f, 7.4f));
                yield Math.min(Math.abs(lens) - 0.65f, Icons.circle(u, v, 6.0f, 6.0f, 1.75f));
            }
            case 3 -> {
                float dx = Math.abs(u - 5.4f);
                float dy = Math.abs(v - 6.4f);
                float big = (float)(Math.sqrt(dx) + Math.sqrt(dy) - Math.sqrt(5.2));
                float sx = Math.abs(u - 10.0f);
                float sy = Math.abs(v - 2.2f);
                float small = (float)(Math.sqrt(sx) + Math.sqrt(sy) - Math.sqrt(1.9));
                yield Math.min(big, small);
            }
            case 4 -> Math.min(Icons.polyline(u, v, 1.25f, 6.0f, 1.0f, 10.6f, 3.6f, 10.6f, 8.4f, 6.0f, 11.0f, 1.4f, 8.4f, 1.4f, 3.6f, 6.0f, 1.0f), Math.min(Icons.segment(u, v, 1.4f, 3.6f, 6.0f, 6.2f, 1.25f), Math.min(Icons.segment(u, v, 10.6f, 3.6f, 6.0f, 6.2f, 1.25f), Icons.segment(u, v, 6.0f, 6.2f, 6.0f, 11.0f, 1.25f))));
            case 5 -> Math.min(Math.abs(Icons.box(u, v, 1.2f, 2.4f, 10.8f, 10.4f, 1.8f)) - 0.65f, Math.min(Icons.segment(u, v, 1.4f, 5.6f, 10.6f, 5.6f, 1.2f), Icons.box(u, v, 4.9f, 4.4f, 7.1f, 7.6f, 0.7f)));
            case 6 -> {
                float head;
                float handle = Icons.segment(u, v, 1.6f, 10.6f, 8.2f, 4.0f, 1.6f);
                float cx = 2.5f;
                float cy = 9.5f;
                float radius = 8.25f;
                double angle = Math.atan2(v - cy, u - cx);
                double a0 = Math.atan2(-8.0, 2.0);
                double a1 = Math.atan2(-2.0, 8.0);
                if (angle >= a0 && angle <= a1) {
                    float t = (float)((angle - a0) / (a1 - a0));
                    float middle = 1.0f - Math.abs(t - 0.5f) * 2.0f;
                    head = Math.abs(Icons.length(u - cx, v - cy) - radius) - (0.35f + 1.05f * (float)Math.pow(middle, 0.6));
                } else {
                    head = Math.min(Icons.circle(u, v, cx + radius * (float)Math.cos(a0), cy + radius * (float)Math.sin(a0), 0.35f), Icons.circle(u, v, cx + radius * (float)Math.cos(a1), cy + radius * (float)Math.sin(a1), 0.35f));
                }
                yield Math.min(handle, head);
            }
            case 7 -> Icons.polyline(u, v, 1.35f, 0.9f, 6.6f, 3.2f, 6.6f, 4.6f, 2.6f, 6.8f, 10.2f, 8.4f, 4.8f, 9.3f, 6.6f, 11.1f, 6.6f);
            case 8 -> Math.min(Icons.box(u, v, 1.0f, 1.6f, 3.6f, 10.4f, 0.9f), Math.min(Icons.box(u, v, 4.7f, 1.6f, 7.3f, 10.4f, 0.9f), Icons.box(u, v, 8.4f, 1.6f, 11.0f, 10.4f, 0.9f)));
            case 9 -> Math.min(Math.abs(Icons.box(u, v, 1.2f, 1.8f, 10.8f, 10.2f, 1.8f)) - 0.6f, Icons.segment(u, v, 4.5f, 2.0f, 4.5f, 10.0f, 1.2f));
            case 10 -> Math.min(Math.min(Icons.box(u, v, 1.0f, 1.0f, 5.4f, 5.4f, 1.3f), Icons.box(u, v, 6.6f, 1.0f, 11.0f, 5.4f, 1.3f)), Math.min(Icons.box(u, v, 1.0f, 6.6f, 5.4f, 11.0f, 1.3f), Icons.box(u, v, 6.6f, 6.6f, 11.0f, 11.0f, 1.3f)));
            case 11 -> Math.min(Icons.polyline(u, v, 1.6f, 2.0f, 6.4f, 6.0f, 2.4f, 10.0f, 6.4f), Icons.polyline(u, v, 1.6f, 2.0f, 10.4f, 6.0f, 6.4f, 10.0f, 10.4f));
            case 12 -> Math.min(Icons.segment(u, v, 2.6f, 2.6f, 9.4f, 9.4f, 1.6f), Icons.segment(u, v, 9.4f, 2.6f, 2.6f, 9.4f, 1.6f));
            case 13 -> Math.max(Icons.circle(u, v, 5.6f, 6.4f, 4.6f), -Icons.circle(u, v, 8.6f, 4.0f, 3.9f));
            case 14 -> Icons.polyline(u, v, 1.3f, 1.4f, 9.6f, 1.4f, 3.6f, 4.0f, 6.4f, 6.0f, 2.4f, 8.0f, 6.4f, 10.6f, 3.6f, 10.6f, 9.6f, 1.4f, 9.6f);
            case 15 -> Math.min(Icons.segment(u, v, 1.5f, 3.4f, 8.0f, 3.4f, 1.4f), Math.min(Icons.segment(u, v, 3.6f, 6.0f, 10.5f, 6.0f, 1.4f), Icons.segment(u, v, 1.5f, 8.6f, 7.0f, 8.6f, 1.4f)));
            case 16 -> Math.min(Math.abs(Icons.box(u, v, 1.2f, 3.4f, 10.8f, 10.2f, 1.6f)) - 0.6f, Math.min(Icons.ring(u, v, 6.0f, 6.8f, 1.9f, 1.2f), Icons.segment(u, v, 4.0f, 2.2f, 7.4f, 2.2f, 1.3f)));
            case 17 -> {
                float outer = (Icons.length((u - 6.0f) / 5.0f, (v - 6.6f) / 2.4f) - 1.0f) * 2.4f;
                float inner = (Icons.length((u - 6.0f) / 2.5f, (v - 6.6f) / 1.1f) - 1.0f) * 1.1f;
                yield Math.min(Math.abs(outer) - 0.6f, Math.abs(inner) - 0.55f);
            }
            case 18 -> Math.min(Icons.polyline(u, v, 1.3f, 1.4f, 3.4f, 7.4f, 3.4f, 10.6f, 6.0f, 7.4f, 8.6f, 1.4f, 8.6f, 1.4f, 3.4f), Icons.circle(u, v, 7.1f, 6.0f, 0.9f));
            case 19 -> Math.min(Icons.ring(u, v, 6.0f, 6.0f, 3.3f, 1.2f), Math.min(Math.min(Icons.segment(u, v, 6.0f, 0.8f, 6.0f, 3.2f, 1.3f), Icons.segment(u, v, 6.0f, 8.8f, 6.0f, 11.2f, 1.3f)), Math.min(Icons.segment(u, v, 0.8f, 6.0f, 3.2f, 6.0f, 1.3f), Icons.segment(u, v, 8.8f, 6.0f, 11.2f, 6.0f, 1.3f))));
            case 20 -> Math.min(Icons.circle(u, v, 6.0f, 3.6f, 2.2f), Math.max(Icons.circle(u, v, 6.0f, 11.8f, 4.8f), v - 11.2f));
            case 21 -> Math.min(Icons.polyline(u, v, 1.3f, 1.8f, 7.0f, 2.36f, 4.9f, 3.9f, 3.36f, 6.0f, 2.8f, 8.1f, 3.36f, 9.64f, 4.9f, 10.2f, 7.2f), Math.min(Icons.segment(u, v, 10.2f, 7.4f, 8.5f, 5.9f, 1.3f), Icons.segment(u, v, 10.2f, 7.4f, 11.3f, 5.6f, 1.3f)));
            case 22 -> {
                float body = Math.min(Math.min(Icons.circle(u, v, 4.2f, 7.0f, 2.6f), Icons.circle(u, v, 6.6f, 5.2f, 3.1f)), Math.min(Icons.circle(u, v, 8.8f, 7.3f, 2.3f), Icons.box(u, v, 2.2f, 7.2f, 10.2f, 9.6f, 1.2f)));
                yield Math.abs(body) - 0.6f;
            }
            case 23 -> Math.min(Icons.ring(u, v, 3.8f, 6.0f, 2.3f, 1.3f), Math.min(Icons.segment(u, v, 6.1f, 6.0f, 11.0f, 6.0f, 1.3f), Math.min(Icons.segment(u, v, 9.2f, 6.0f, 9.2f, 8.4f, 1.3f), Icons.segment(u, v, 10.8f, 6.0f, 10.8f, 7.8f, 1.3f))));
            case 24 -> Math.min(Icons.segment(u, v, 3.4f, 8.6f, 10.6f, 1.4f, 1.5f), Math.min(Icons.segment(u, v, 2.0f, 6.4f, 5.6f, 10.0f, 1.3f), Icons.segment(u, v, 1.2f, 10.8f, 3.6f, 8.4f, 1.6f)));
            case 25 -> {
                float teeth = Float.MAX_VALUE;
                for (int k = 0; k < 6; ++k) {
                    double a = (double)k * Math.PI / 3.0;
                    float c = (float)Math.cos(a);
                    float s = (float)Math.sin(a);
                    teeth = Math.min(teeth, Icons.segment(u, v, 6.0f + c * 3.4f, 6.0f + s * 3.4f, 6.0f + c * 5.0f, 6.0f + s * 5.0f, 1.7f));
                }
                yield Math.min(Icons.ring(u, v, 6.0f, 6.0f, 2.8f, 1.5f), teeth);
            }
            case 26 -> Icons.polyline(u, v, 1.7f, 4.4f, 2.0f, 8.4f, 6.0f, 4.4f, 10.0f);
            case 27 -> Math.min(Icons.segment(u, v, 6.0f, 1.8f, 6.0f, 10.2f, 1.6f), Icons.segment(u, v, 1.8f, 6.0f, 10.2f, 6.0f, 1.6f));
            case 28 -> Icons.segment(u, v, 1.8f, 6.0f, 10.2f, 6.0f, 1.6f);
            case 29 -> {
                float[] points = new float[20];
                for (int k = 0; k < 10; ++k) {
                    double a = -1.5707963267948966 + (double)k * Math.PI / 5.0;
                    float r = k % 2 == 0 ? 5.4f : 2.3f;
                    points[k * 2] = 6.0f + (float)Math.cos(a) * r;
                    points[k * 2 + 1] = 6.4f + (float)Math.sin(a) * r;
                }
                boolean in = false;
                int k = 0;
                int j = 9;
                while (k < 10) {
                    float yj;
                    float xi = points[k * 2];
                    float yi = points[k * 2 + 1];
                    float xj = points[j * 2];
                    if (yi > v != (yj = points[j * 2 + 1]) > v && u < (xj - xi) * (v - yi) / (yj - yi) + xi) {
                        in = !in;
                    }
                    j = k++;
                }
                float edge = Float.MAX_VALUE;
                int k2 = 0;
                int j2 = 9;
                while (k2 < 10) {
                    edge = Math.min(edge, Icons.segment(u, v, points[j2 * 2], points[j2 * 2 + 1], points[k2 * 2], points[k2 * 2 + 1], 0.0f));
                    j2 = k2++;
                }
                yield (in ? -edge : edge) - 0.3f;
            }
            case 30 -> Math.abs(Icons.heart(u, v)) - 0.6f;
            case 31 -> {
                float body = Math.min(Icons.circle(u, v, 6.0f, 7.7f, 3.3f), Icons.shape(u, v, 2.75f, 7.4f, 9.25f, 7.4f, 7.1f, 3.6f, 6.4f, 0.9f, 4.6f, 3.4f));
                yield Math.max(body, -Icons.circle(u, v, 6.2f, 8.4f, 1.3f));
            }
            case 32 -> Math.min(Math.min(Icons.segment(u, v, 2.4f, 9.6f, 10.0f, 2.0f, 1.3f), Icons.segment(u, v, 9.6f, 9.6f, 2.0f, 2.0f, 1.3f)), Math.min(Icons.segment(u, v, 1.6f, 7.4f, 4.6f, 10.4f, 1.3f), Icons.segment(u, v, 7.4f, 10.4f, 10.4f, 7.4f, 1.3f)));
            case 33 -> Math.min(Icons.polyline(u, v, 1.35f, 1.3f, 9.4f, 4.6f, 6.1f, 6.9f, 8.2f, 10.5f, 3.6f), Icons.polyline(u, v, 1.35f, 7.3f, 3.3f, 10.7f, 3.3f, 10.7f, 6.7f));
            case 34 -> Math.min(Icons.polyline(u, v, 1.35f, 1.3f, 2.6f, 4.6f, 5.9f, 6.9f, 3.8f, 10.5f, 8.4f), Icons.polyline(u, v, 1.35f, 7.3f, 8.7f, 10.7f, 8.7f, 10.7f, 5.3f));
            case 35 -> {
                float head = Math.min(Icons.circle(u, v, 6.0f, 5.3f, 4.3f), Icons.box(u, v, 3.5f, 7.2f, 8.5f, 10.8f, 1.1f));
                float eyes = Math.min(Icons.circle(u, v, 4.4f, 5.7f, 1.2f), Icons.circle(u, v, 7.6f, 5.7f, 1.2f));
                yield Math.max(Math.max(head, -eyes), -Icons.shape(u, v, 5.3f, 8.6f, 6.7f, 8.6f, 6.0f, 7.3f));
            }
            case 36 -> Math.min(Math.abs(Icons.shape(u, v, 2.3f, 2.4f, 9.7f, 2.4f, 9.7f, 6.1f, 6.0f, 10.5f, 2.3f, 6.1f)) - 0.6f, Icons.segment(u, v, 1.6f, 1.6f, 10.4f, 10.4f, 1.3f));
        };
    }

    public static enum Icon {
        SEARCH,
        DONUT,
        EYE,
        SPARKLE,
        CUBE,
        CHEST,
        PICKAXE,
        PULSE,
        COLUMNS,
        WINDOW,
        GRID,
        CHEVRONS,
        CLOSE,
        MOON,
        CROWN,
        SPEED,
        CAMERA,
        RINGS,
        TAG,
        CROSSHAIR,
        PERSON,
        SWING,
        CLOUD,
        KEY,
        SWORD,
        GEAR,
        CHEVRON,
        PLUS,
        MINUS,
        STAR,
        HEART,
        FLAME,
        SWORDS,
        TREND_UP,
        TREND_DOWN,
        SKULL,
        SHIELD_OFF;

    }
}

