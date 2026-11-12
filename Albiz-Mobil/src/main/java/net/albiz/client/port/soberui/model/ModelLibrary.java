// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import net.albiz.client.port.soberui.model.ModelMesh;
import net.albiz.client.port.soberui.render.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModelLibrary {
    public static final String PREFIX = "builtin:";
    private static final float STEP = 0.5f;
    private static final float BEVEL = 0.8f;
    private static final float MIN_X = -6.0f;
    private static final float MAX_X = 38.0f;
    private static final float MIN_Y = -9.0f;
    private static final float MAX_Y = 42.0f;
    private static final Map<String, Design> DESIGNS = new LinkedHashMap<String, Design>();

    private ModelLibrary() {
    }

    public static List<String> names() {
        ArrayList<String> names = new ArrayList<String>();
        for (String id : DESIGNS.keySet()) {
            names.add(PREFIX + id);
        }
        return names;
    }

    public static boolean has(String name) {
        return name != null && name.startsWith(PREFIX) && DESIGNS.containsKey(name.substring(PREFIX.length()));
    }

    public static String title(String name) {
        Design design = ModelLibrary.has(name) ? DESIGNS.get(name.substring(PREFIX.length())) : null;
        return design == null ? name : design.title();
    }

    public static int swatch(String name) {
        Design design = ModelLibrary.has(name) ? DESIGNS.get(name.substring(PREFIX.length())) : null;
        return design == null ? -1 : design.swatch();
    }

    public static ModelMesh build(String name) {
        Design design = DESIGNS.get(name.substring(PREFIX.length()));
        Builder builder = new Builder();
        for (Part part : design.parts()) {
            ModelLibrary.extrude(part, builder);
        }
        return ModelMesh.normalize(Arrays.copyOf(builder.positions, builder.size * 9), Arrays.copyOf(builder.colors, builder.size), builder.size);
    }

    private static float zOf(float depth, float f) {
        float inside = Math.max(0.0f, Math.min(1.0f, -f / 0.8f));
        float s = inside * inside * (3.0f - 2.0f * inside);
        return depth * (0.55f + 0.45f * s);
    }

    private static void extrude(Part part, Builder out) {
        int nx = Math.round(88.0f);
        int ny = Math.round(102.0f);
        float[][] values = new float[nx + 1][ny + 1];
        for (int i = 0; i <= nx; ++i) {
            for (int j = 0; j <= ny; ++j) {
                values[i][j] = part.field().at(-6.0f + (float)i * 0.5f, -9.0f + (float)j * 0.5f);
            }
        }
        float depth = part.depth();
        float edge = ModelLibrary.zOf(depth, 0.0f);
        int[][] corners = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        float[] px = new float[8];
        float[] py = new float[8];
        float[] pf = new float[8];
        boolean[] crossing = new boolean[8];
        for (int i = 0; i < nx; ++i) {
            for (int j = 0; j < ny; ++j) {
                boolean any = false;
                for (int[] c : corners) {
                    if (!(values[i + c[0]][j + c[1]] < 0.0f)) continue;
                    any = true;
                }
                if (!any) continue;
                int n = 0;
                for (int k = 0; k < 4; ++k) {
                    int[] a = corners[k];
                    int[] b = corners[(k + 1) % 4];
                    float fa = values[i + a[0]][j + a[1]];
                    float fb = values[i + b[0]][j + b[1]];
                    float ax = -6.0f + (float)(i + a[0]) * 0.5f;
                    float ay = -9.0f + (float)(j + a[1]) * 0.5f;
                    float bx = -6.0f + (float)(i + b[0]) * 0.5f;
                    float by = -9.0f + (float)(j + b[1]) * 0.5f;
                    if (fa < 0.0f) {
                        px[n] = ax;
                        py[n] = ay;
                        pf[n] = fa;
                        crossing[n++] = false;
                    }
                    if (fa < 0.0f == fb < 0.0f) continue;
                    float t = fa / (fa - fb);
                    px[n] = ax + (bx - ax) * t;
                    py[n] = ay + (by - ay) * t;
                    pf[n] = 0.0f;
                    crossing[n++] = true;
                }
                if (n < 3) continue;
                float cx = 0.0f;
                float cy = 0.0f;
                for (int k = 0; k < n; ++k) {
                    cx += px[k];
                    cy += py[k];
                }
                int color = part.paint().at(cx /= (float)n, cy /= (float)n, part.field().at(cx, cy));
                int k = 1;
                while (k + 1 < n) {
                    out.triangle(px[0], py[0], ModelLibrary.zOf(depth, pf[0]), px[k], py[k], ModelLibrary.zOf(depth, pf[k]), px[k + 1], py[k + 1], ModelLibrary.zOf(depth, pf[k + 1]), color);
                    out.triangle(px[0], py[0], -ModelLibrary.zOf(depth, pf[0]), px[k + 1], py[k + 1], -ModelLibrary.zOf(depth, pf[k + 1]), px[k], py[k], -ModelLibrary.zOf(depth, pf[k]), color);
                    ++k;
                }
                int side = ModelLibrary.mix(color, 0, 0.28f);
                for (int k2 = 0; k2 < n; ++k2) {
                    int q = (k2 + 1) % n;
                    if (!crossing[k2] || !crossing[q]) continue;
                    out.triangle(px[k2], py[k2], -edge, px[q], py[q], -edge, px[q], py[q], edge, side);
                    out.triangle(px[k2], py[k2], -edge, px[q], py[q], edge, px[k2], py[k2], edge, side);
                }
            }
        }
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int mix(int a, int b, float t) {
        t = ModelLibrary.clamp01(t);
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    private static int hsv(float h, float s, float v) {
        return Color.fromHsv(h, s, v, 255).packed() & 0xFFFFFF;
    }

    private static float segment(float px, float py, float ax, float ay, float bx, float by, float r) {
        float ex = bx - ax;
        float ey = by - ay;
        float wx = px - ax;
        float wy = py - ay;
        float t = ModelLibrary.clamp01((wx * ex + wy * ey) / (ex * ex + ey * ey));
        float dx = wx - ex * t;
        float dy = wy - ey * t;
        return (float)Math.sqrt(dx * dx + dy * dy) - r;
    }

    private static float circle(float px, float py, float cx, float cy, float r) {
        float dx = px - cx;
        float dy = py - cy;
        return (float)Math.sqrt(dx * dx + dy * dy) - r;
    }

    private static float box(float px, float py, float cx, float cy, float hw, float hh, float r) {
        float qx = Math.abs(px - cx) - hw + r;
        float qy = Math.abs(py - cy) - hh + r;
        float ox = Math.max(qx, 0.0f);
        float oy = Math.max(qy, 0.0f);
        return (float)Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(qx, qy), 0.0f) - r;
    }

    private static float polygon(float px, float py, float ... v) {
        int n = v.length / 2;
        float best = Float.MAX_VALUE;
        boolean inside = false;
        int i = 0;
        int j = n - 1;
        while (i < n) {
            float ax = v[j * 2];
            float ay = v[j * 2 + 1];
            float bx = v[i * 2];
            float by = v[i * 2 + 1];
            float ex = bx - ax;
            float ey = by - ay;
            float wx = px - ax;
            float wy = py - ay;
            float t = ModelLibrary.clamp01((wx * ex + wy * ey) / (ex * ex + ey * ey));
            float dx = wx - ex * t;
            float dy = wy - ey * t;
            best = Math.min(best, dx * dx + dy * dy);
            if (ay > py != by > py && px < ax + (py - ay) * ex / ey) {
                inside = !inside;
            }
            j = i++;
        }
        float distance = (float)Math.sqrt(best);
        return inside ? -distance : distance;
    }

    private static float[] star(float cx, float cy, float outer, float inner) {
        float[] points = new float[20];
        for (int i = 0; i < 10; ++i) {
            double angle = 1.5707963267948966 + (double)i * Math.PI / 5.0;
            float r = i % 2 == 0 ? outer : inner;
            points[i * 2] = cx + (float)Math.cos(angle) * r;
            points[i * 2 + 1] = cy + (float)Math.sin(angle) * r;
        }
        return points;
    }

    private static Part handle(float top, float radius, int base, int wrap, float depth) {
        return new Part((x, y) -> ModelLibrary.segment(x, y, 16.0f, -4.0f, 16.0f, top, radius), (x, y, f) -> {
            boolean grip = y > -3.5f && y < 7.0f && Math.floorMod((int)Math.floor((y + 4.0f) / 1.4f), 2) == 0;
            int color = grip ? wrap : ModelLibrary.mix(base, 0xFFFFFF, 0.08f * (y + 4.0f) / (top + 4.0f));
            return f > -0.3f ? ModelLibrary.mix(color, 0xFFFFFF, 0.12f) : color;
        }, depth);
    }

    private static Part gem(float cx, float cy, float r, int color, float depth) {
        return new Part((x, y) -> ModelLibrary.circle(x, y, cx, cy, r), (x, y, f) -> ModelLibrary.mix(color, 0xFFFFFF, f > -0.35f ? 0.45f : ModelLibrary.clamp01((y - cy + r) / (2.0f * r)) * 0.35f), depth);
    }

    private static float headT(float x, float y) {
        double angle = Math.atan2(y - 14.0f, x - 16.0f);
        double a0 = Math.toRadians(20.0);
        double a1 = Math.toRadians(160.0);
        return ModelLibrary.clamp01((float)((angle - a0) / (a1 - a0)));
    }

    private static float pickHead(float px, float py) {
        float distance;
        float t;
        float cx = 16.0f;
        float cy = 14.0f;
        float radius = 12.0f;
        double a0 = Math.toRadians(20.0);
        double a1 = Math.toRadians(160.0);
        float dy = py - cy;
        float dx = px - cx;
        double angle = Math.atan2(dy, dx);
        if (angle >= a0 && angle <= a1) {
            t = (float)((angle - a0) / (a1 - a0));
            distance = Math.abs((float)Math.sqrt(dx * dx + dy * dy) - radius);
        } else {
            float d1;
            float e0x = cx + (float)Math.cos(a0) * radius;
            float e0y = cy + (float)Math.sin(a0) * radius;
            float e1x = cx + (float)Math.cos(a1) * radius;
            float e1y = cy + (float)Math.sin(a1) * radius;
            float d0 = (float)Math.hypot(px - e0x, py - e0y);
            t = d0 < (d1 = (float)Math.hypot(px - e1x, py - e1y)) ? 0.0f : 1.0f;
            distance = Math.min(d0, d1);
        }
        float middle = 1.0f - Math.abs(t - 0.5f) * 2.0f;
        return distance - (0.3f + 2.45f * (float)Math.pow(middle, 0.55));
    }

    private static Design pick(String title, Palette p) {
        Part head = new Part(ModelLibrary::pickHead, (x, y, f) -> {
            float t = ModelLibrary.headT(x, y);
            float middle = 1.0f - Math.abs(t - 0.5f) * 2.0f;
            int color = ModelLibrary.mix(p.headA(), p.headB(), 1.0f - middle);
            if (f > -0.45f) {
                return ModelLibrary.mix(color, p.rim(), 0.7f);
            }
            float vein = Math.abs((float)Math.hypot(x - 16.0f, y - 14.0f) - 12.0f);
            return vein < 0.35f ? ModelLibrary.mix(color, p.rim(), 0.35f) : color;
        }, 1.5f);
        return new Design(title, p.headA(), List.of(ModelLibrary.handle(24.0f, 1.1f, p.handle(), p.wrap(), 1.0f), head, ModelLibrary.gem(16.0f, 25.6f, 2.3f, p.gem(), 1.9f), ModelLibrary.gem(16.0f, -4.6f, 1.6f, p.gem(), 1.3f)));
    }

    private static Design scythe() {
        Part blade = new Part((x, y) -> Math.max(Math.max(ModelLibrary.circle(x, y, 7.0f, 26.0f, 10.0f), -ModelLibrary.circle(x, y, 5.0f, 23.5f, 9.5f)), 26.0f - y), (x, y, f) -> {
            int color = ModelLibrary.mix(14708991, 6962431, ModelLibrary.clamp01((x + 3.0f) / 20.0f));
            return f > -0.5f ? ModelLibrary.mix(color, 16049919, 0.75f) : color;
        }, 0.75f);
        return new Design("Void Scythe", 11559167, List.of(ModelLibrary.handle(30.0f, 1.0f, 2827328, 12103888, 0.9f), blade, ModelLibrary.gem(16.0f, 29.6f, 1.9f, 0x40F0FF, 1.4f), ModelLibrary.gem(16.0f, -4.6f, 1.5f, 0x40F0FF, 1.2f)));
    }

    private static Design axe() {
        Part head = new Part((x, y) -> Math.min(Math.max(ModelLibrary.circle(x, y, 14.0f, 26.0f, 14.5f), ModelLibrary.polygon(x, y, 16.0f, 21.0f, 30.0f, 15.0f, 30.0f, 37.0f, 16.0f, 31.0f)), Math.max(ModelLibrary.circle(x, y, 18.0f, 26.0f, 14.5f), ModelLibrary.polygon(x, y, 16.0f, 21.0f, 16.0f, 31.0f, 2.0f, 37.0f, 2.0f, 15.0f))), (x, y, f) -> {
            int color = ModelLibrary.hsv(ModelLibrary.clamp01((y - 15.0f) / 22.0f) * 0.8f, 0.62f, 1.0f);
            return f > -0.45f ? ModelLibrary.mix(color, 0xFFFFFF, 0.55f) : color;
        }, 1.1f);
        return new Design("Prism Axe", 16747224, List.of(ModelLibrary.handle(28.0f, 1.1f, 15132400, 6970061, 1.0f), head, ModelLibrary.gem(16.0f, 26.0f, 2.6f, 0xF8F8FF, 1.6f), ModelLibrary.gem(16.0f, -4.6f, 1.6f, 10128639, 1.3f)));
    }

    private static Design hammer() {
        float[] star = ModelLibrary.star(16.0f, 28.5f, 3.3f, 1.35f);
        Part head = new Part((x, y) -> ModelLibrary.box(x, y, 16.0f, 28.5f, 8.5f, 4.2f, 1.5f), (x, y, f) -> {
            int color = ModelLibrary.mix(14263584, 16769658, ModelLibrary.clamp01((y - 24.3f) / 8.4f));
            return f > -0.45f ? ModelLibrary.mix(color, 16773808, 0.7f) : color;
        }, 2.2f);
        Part emblem = new Part((x, y) -> ModelLibrary.polygon(x, y, star), (x, y, f) -> f > -0.3f ? 16774864 : 0xFFFFFF, 2.5f);
        return new Design("Star Hammer", 15911244, List.of(ModelLibrary.handle(25.0f, 1.25f, 3947600, 15911244, 1.1f), head, emblem, ModelLibrary.gem(16.0f, -4.6f, 1.7f, 15911244, 1.4f)));
    }

    static {
        DESIGNS.put("frost_pick", ModelLibrary.pick("Frost Pick", new Palette(3424348, 10475775, 7129343, 14743551, 0xFFFFFF, 3844351)));
        DESIGNS.put("ember_pick", ModelLibrary.pick("Ember Pick", new Palette(2759188, 16742958, 16726815, 16763210, 0xFFF0C0, 16769146)));
        DESIGNS.put("amethyst_pick", ModelLibrary.pick("Amethyst Pick", new Palette(3023936, 13148415, 9063935, 14723327, 16181503, 16743136)));
        DESIGNS.put("toxic_pick", ModelLibrary.pick("Toxic Pick", new Palette(1976862, 10288983, 3070064, 14221162, 16056288, 6750160)));
        DESIGNS.put("void_scythe", ModelLibrary.scythe());
        DESIGNS.put("prism_axe", ModelLibrary.axe());
        DESIGNS.put("star_hammer", ModelLibrary.hammer());
    }

    private record Design(String title, int swatch, List<Part> parts) {
    }

    private static final class Builder {
        float[] positions = new float[36864];
        int[] colors = new int[4096];
        int size;

        private Builder() {
        }

        void triangle(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, int color) {
            if (this.size == this.colors.length) {
                this.colors = Arrays.copyOf(this.colors, this.size * 2);
                this.positions = Arrays.copyOf(this.positions, this.size * 18);
            }
            int o = this.size * 9;
            this.positions[o] = ax;
            this.positions[o + 1] = ay;
            this.positions[o + 2] = az;
            this.positions[o + 3] = bx;
            this.positions[o + 4] = by;
            this.positions[o + 5] = bz;
            this.positions[o + 6] = cx;
            this.positions[o + 7] = cy;
            this.positions[o + 8] = cz;
            this.colors[this.size++] = color & 0xFFFFFF;
        }
    }

    private record Part(Field field, Paint paint, float depth) {
    }

    private static interface Field {
        public float at(float var1, float var2);
    }

    private static interface Paint {
        public int at(float var1, float var2, float var3);
    }

    private record Palette(int handle, int wrap, int headA, int headB, int rim, int gem) {
    }
}

