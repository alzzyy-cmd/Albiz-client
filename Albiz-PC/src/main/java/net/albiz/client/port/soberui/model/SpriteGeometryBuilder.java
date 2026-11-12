// Adapted from SoberClient, Violet, GPL-3.0-only; CPU-only geometry path.
package net.albiz.client.port.soberui.model;
import java.util.*;
final class SpriteGeometryBuilder {
    static int channel(int c, int shift) {
        return c >> shift & 0xFF;
    }

    static int[] upscale(int[] source, int sw, int sh, int w, int h) {
        int[] out = new int[w * h];
        for (int y = 0; y < h; ++y) {
            float fy = ((float)y + 0.5f) * (float)sh / (float)h - 0.5f;
            int y0 = Math.max(0, Math.min(sh - 1, (int)Math.floor(fy)));
            int y1 = Math.min(sh - 1, y0 + 1);
            float ty = Math.max(0.0f, Math.min(1.0f, fy - (float)y0));
            for (int x = 0; x < w; ++x) {
                float fx = ((float)x + 0.5f) * (float)sw / (float)w - 0.5f;
                int x0 = Math.max(0, Math.min(sw - 1, (int)Math.floor(fx)));
                int x1 = Math.min(sw - 1, x0 + 1);
                float tx = Math.max(0.0f, Math.min(1.0f, fx - (float)x0));
                int a = source[y0 * sw + x0];
                int b = source[y0 * sw + x1];
                int c = source[y1 * sw + x0];
                int d = source[y1 * sw + x1];
                int value = 0;
                for (int shift = 0; shift <= 24; shift += 8) {
                    float top = (float)SpriteGeometryBuilder.channel(a, shift) * (1.0f - tx) + (float)SpriteGeometryBuilder.channel(b, shift) * tx;
                    float bottom = (float)SpriteGeometryBuilder.channel(c, shift) * (1.0f - tx) + (float)SpriteGeometryBuilder.channel(d, shift) * tx;
                    value |= Math.max(0, Math.min(255, Math.round(top * (1.0f - ty) + bottom * ty))) << shift;
                }
                out[y * w + x] = value;
            }
        }
        return out;
    }

    static int[] downscale(int[] source, int sw, int sh, int w, int h) {
        int[] out = new int[w * h];
        for (int y = 0; y < h; ++y) {
            int y0 = y * sh / h;
            int y1 = Math.max(y0 + 1, (y + 1) * sh / h);
            for (int x = 0; x < w; ++x) {
                int x0 = x * sw / w;
                int x1 = Math.max(x0 + 1, (x + 1) * sw / w);
                long a = 0L;
                long r = 0L;
                long g = 0L;
                long b = 0L;
                int count = 0;
                for (int yy = y0; yy < y1; ++yy) {
                    for (int xx = x0; xx < x1; ++xx) {
                        int c = source[yy * sw + xx];
                        int alpha = c >>> 24;
                        a += (long)alpha;
                        r += (long)SpriteGeometryBuilder.channel(c, 16) * (long)alpha;
                        g += (long)SpriteGeometryBuilder.channel(c, 8) * (long)alpha;
                        b += (long)SpriteGeometryBuilder.channel(c, 0) * (long)alpha;
                        ++count;
                    }
                }
                int alpha = (int)(a / (long)Math.max(1, count));
                int red = a == 0L ? 0 : (int)(r / a);
                int green = a == 0L ? 0 : (int)(g / a);
                int blue = a == 0L ? 0 : (int)(b / a);
                out[y * w + x] = alpha << 24 | red << 16 | green << 8 | blue;
            }
        }
        return out;
    }

    static void bleed(int[] pixels, int w, int h) {
        boolean[] known = new boolean[w * h];
        for (int i = 0; i < pixels.length; ++i) {
            known[i] = pixels[i] >>> 24 > 8;
        }
        for (int pass = 0; pass < 12; ++pass) {
            boolean[] next = (boolean[])known.clone();
            boolean changed = false;
            for (int y = 0; y < h; ++y) {
                for (int x = 0; x < w; ++x) {
                    int i = y * w + x;
                    if (known[i]) continue;
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int n = 0;
                    for (int dy = -1; dy <= 1; ++dy) {
                        for (int dx = -1; dx <= 1; ++dx) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h || !known[ny * w + nx]) continue;
                            int c = pixels[ny * w + nx];
                            r += SpriteGeometryBuilder.channel(c, 16);
                            g += SpriteGeometryBuilder.channel(c, 8);
                            b += SpriteGeometryBuilder.channel(c, 0);
                            ++n;
                        }
                    }
                    if (n == 0) continue;
                    pixels[i] = pixels[i] & 0xFF000000 | r / n << 16 | g / n << 8 | b / n;
                    next[i] = true;
                    changed = true;
                }
            }
            known = next;
            if (!changed) break;
        }
    }

    static float[] soften(float[] alpha, int w, int h, float sigma) {
        float value;
        int x;
        int y;
        int i;
        int radius = Math.max(1, (int)Math.ceil(sigma * 2.5f));
        float[] kernel = new float[radius * 2 + 1];
        float sum = 0.0f;
        for (i = -radius; i <= radius; ++i) {
            kernel[i + radius] = (float)Math.exp((float)(-(i * i)) / (2.0f * sigma * sigma));
            sum += kernel[i + radius];
        }
        i = 0;
        while (i < kernel.length) {
            int n = i++;
            kernel[n] = kernel[n] / sum;
        }
        float[] temp = new float[w * h];
        float[] out = new float[w * h];
        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                value = 0.0f;
                for (int k = -radius; k <= radius; ++k) {
                    int xx = x + k;
                    if (xx < 0 || xx >= w) continue;
                    value += alpha[y * w + xx] * kernel[k + radius];
                }
                temp[y * w + x] = value;
            }
        }
        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                value = 0.0f;
                for (int k = -radius; k <= radius; ++k) {
                    int yy = y + k;
                    if (yy < 0 || yy >= h) continue;
                    value += temp[yy * w + x] * kernel[k + radius];
                }
                float t = Math.max(0.0f, Math.min(1.0f, (value - 0.38f) / 0.24f));
                out[y * w + x] = t * t * (3.0f - 2.0f * t);
            }
        }
        return out;
    }

    static float[] shrink(float[] source, int sw, int sh, int w, int h) {
        if (sw == w && sh == h) {
            return source;
        }
        float[] out = new float[w * h];
        for (int y = 0; y < h; ++y) {
            int y0 = y * sh / h;
            int y1 = Math.max(y0 + 1, (y + 1) * sh / h);
            for (int x = 0; x < w; ++x) {
                int x0 = x * sw / w;
                int x1 = Math.max(x0 + 1, (x + 1) * sw / w);
                float sum = 0.0f;
                for (int yy = y0; yy < y1; ++yy) {
                    for (int xx = x0; xx < x1; ++xx) {
                        sum += source[yy * sw + xx];
                    }
                }
                out[y * w + x] = sum / (float)((y1 - y0) * (x1 - x0));
            }
        }
        return out;
    }

    static float sample(float[] alpha, int w, int h, int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h) {
            return 0.0f;
        }
        return alpha[y * w + x];
    }

    static float sampleBilinear(float[] alpha, int w, int h, float x, float y) {
        float fx = x - 0.5f;
        float fy = y - 0.5f;
        int x0 = (int)Math.floor(fx);
        int y0 = (int)Math.floor(fy);
        float tx = fx - (float)x0;
        float ty = fy - (float)y0;
        float a = SpriteGeometryBuilder.sample(alpha, w, h, x0, y0);
        float b = SpriteGeometryBuilder.sample(alpha, w, h, x0 + 1, y0);
        float c = SpriteGeometryBuilder.sample(alpha, w, h, x0, y0 + 1);
        float d = SpriteGeometryBuilder.sample(alpha, w, h, x0 + 1, y0 + 1);
        return (a * (1.0f - tx) + b * tx) * (1.0f - ty) + (c * (1.0f - tx) + d * tx) * ty;
    }

    static void contour(Geometry geometry, float[] alpha, int w, int h, float front, float back) {
        SpriteSegments segments = new SpriteSegments();
        for (int cy = -1; cy < h; ++cy) {
            block11: for (int cx = -1; cx < w; ++cx) {
                float bl;
                float br;
                float tr;
                float tl = SpriteGeometryBuilder.sample(alpha, w, h, cx, cy);
                int index = (tl >= 0.1f ? 8 : 0) | ((tr = SpriteGeometryBuilder.sample(alpha, w, h, cx + 1, cy)) >= 0.1f ? 4 : 0) | ((br = SpriteGeometryBuilder.sample(alpha, w, h, cx + 1, cy + 1)) >= 0.1f ? 2 : 0) | ((bl = SpriteGeometryBuilder.sample(alpha, w, h, cx, cy + 1)) >= 0.1f ? 1 : 0);
                if (index == 0 || index == 15) continue;
                float px = (float)cx + 0.5f;
                float py = (float)cy + 0.5f;
                float topX = px + SpriteGeometryBuilder.cross(tl, tr);
                float topY = py;
                float rightX = px + 1.0f;
                float rightY = py + SpriteGeometryBuilder.cross(tr, br);
                float bottomX = px + SpriteGeometryBuilder.cross(bl, br);
                float bottomY = py + 1.0f;
                float leftX = px;
                float leftY = py + SpriteGeometryBuilder.cross(tl, bl);
                boolean center = (tl + tr + br + bl) / 4.0f >= 0.1f;
                switch (index) {
                    case 1: 
                    case 14: {
                        segments.add(leftX, leftY, bottomX, bottomY);
                        continue block11;
                    }
                    case 2: 
                    case 13: {
                        segments.add(bottomX, bottomY, rightX, rightY);
                        continue block11;
                    }
                    case 3: 
                    case 12: {
                        segments.add(leftX, leftY, rightX, rightY);
                        continue block11;
                    }
                    case 4: 
                    case 11: {
                        segments.add(topX, topY, rightX, rightY);
                        continue block11;
                    }
                    case 6: 
                    case 9: {
                        segments.add(topX, topY, bottomX, bottomY);
                        continue block11;
                    }
                    case 7: 
                    case 8: {
                        segments.add(leftX, leftY, topX, topY);
                        continue block11;
                    }
                    case 5: {
                        if (center) {
                            segments.add(leftX, leftY, topX, topY);
                            segments.add(bottomX, bottomY, rightX, rightY);
                            continue block11;
                        }
                        segments.add(leftX, leftY, bottomX, bottomY);
                        segments.add(topX, topY, rightX, rightY);
                        continue block11;
                    }
                    case 10: {
                        if (center) {
                            segments.add(topX, topY, rightX, rightY);
                            segments.add(leftX, leftY, bottomX, bottomY);
                            continue block11;
                        }
                        segments.add(leftX, leftY, topX, topY);
                        segments.add(bottomX, bottomY, rightX, rightY);
                        continue block11;
                    }
                }
            }
        }
        for (float[] chain : segments.chains()) {
            float[] points = SpriteGeometryBuilder.simplify(chain, 0.3f);
            int i = 0;
            while (i + 3 < points.length) {
                SpriteGeometryBuilder.wall(geometry, alpha, w, h, points[i], points[i + 1], points[i + 2], points[i + 3], front, back);
                i += 2;
            }
        }
    }

    static float cross(float a, float b) {
        if (Math.abs(b - a) < 1.0E-6f) {
            return 0.5f;
        }
        return Math.max(0.0f, Math.min(1.0f, (0.1f - a) / (b - a)));
    }

    static void wall(Geometry geometry, float[] alpha, int w, int h, float x1, float y1, float x2, float y2, float front, float back) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0E-5f) {
            return;
        }
        float mx = (x1 + x2) / 2.0f;
        float nx = dy / length;
        float my = (y1 + y2) / 2.0f;
        float ny = -dx / length;
        if (SpriteGeometryBuilder.sampleBilinear(alpha, w, h, mx + nx * 0.6f, my + ny * 0.6f) >= 0.1f) {
            nx = -nx;
            ny = -ny;
        }
        float inset = 1.5f;
        float u1 = Math.max(0.5f, Math.min((float)w - 0.5f, x1 - nx * inset)) / (float)w;
        float v1 = Math.max(0.5f, Math.min((float)h - 0.5f, y1 - ny * inset)) / (float)h;
        float u2 = Math.max(0.5f, Math.min((float)w - 0.5f, x2 - nx * inset)) / (float)w;
        float v2 = Math.max(0.5f, Math.min((float)h - 0.5f, y2 - ny * inset)) / (float)h;
        float ax = x1 / (float)w;
        float ay = 1.0f - y1 / (float)h;
        float bx = x2 / (float)w;
        float by = 1.0f - y2 / (float)h;
        float sx = nx * 0.8f;
        float sy = -ny * 0.8f;
        float sz = 0.6f;
        geometry.quad(ax, ay, back, u1, v1, ax, ay, front, u1, v1, bx, by, front, u2, v2, bx, by, back, u2, v2, sx, sy, -sz, sx, sy, sz, -3552046);
    }

    static float[] simplify(float[] points, float epsilon) {
        int i;
        boolean closed;
        int n = points.length / 2;
        if (n <= 2) {
            return points;
        }
        boolean[] keep = new boolean[n];
        keep[0] = true;
        keep[n - 1] = true;
        ArrayDeque<int[]> stack = new ArrayDeque<int[]>();
        boolean bl = closed = points[0] == points[(n - 1) * 2] && points[1] == points[(n - 1) * 2 + 1];
        if (closed && n > 3) {
            int far = 1;
            float best = -1.0f;
            for (i = 1; i < n - 1; ++i) {
                float dx = points[i * 2] - points[0];
                float dy = points[i * 2 + 1] - points[1];
                float d = dx * dx + dy * dy;
                if (!(d > best)) continue;
                best = d;
                far = i;
            }
            keep[far] = true;
            stack.push(new int[]{0, far});
            stack.push(new int[]{far, n - 1});
        } else {
            stack.push(new int[]{0, n - 1});
        }
        while (!stack.isEmpty()) {
            int a;
            int[] range = (int[])stack.pop();
            int b = range[1];
            if (b - (a = range[0]) < 2) continue;
            float ax = points[a * 2];
            float ay = points[a * 2 + 1];
            float bx = points[b * 2];
            float by = points[b * 2 + 1];
            float dx = bx - ax;
            float dy = by - ay;
            float length = dx * dx + dy * dy;
            int index = -1;
            float max = epsilon * epsilon;
            for (int i2 = a + 1; i2 < b; ++i2) {
                float ey;
                float py = points[i2 * 2 + 1] - ay;
                float px = points[i2 * 2] - ax;
                float t = length > 1.0E-12f ? Math.max(0.0f, Math.min(1.0f, (px * dx + py * dy) / length)) : 0.0f;
                float ex = px - dx * t;
                float d = ex * ex + (ey = py - dy * t) * ey;
                if (!(d > max)) continue;
                max = d;
                index = i2;
            }
            if (index < 0) continue;
            keep[index] = true;
            stack.push(new int[]{a, index});
            stack.push(new int[]{index, b});
        }
        int kept = 0;
        for (boolean k : keep) {
            if (!k) continue;
            ++kept;
        }
        float[] out = new float[kept * 2];
        int o = 0;
        for (i = 0; i < n; ++i) {
            if (!keep[i]) continue;
            out[o++] = points[i * 2];
            out[o++] = points[i * 2 + 1];
        }
        return out;
    }

    static final class Geometry {
        float[] p;
        float[] t;
        float[] n;
        int[] c;
        int count;

        Geometry(int capacity) {
            this.p = new float[capacity * 12];
            this.t = new float[capacity * 8];
            this.n = new float[capacity * 12];
            this.c = new int[capacity];
        }

        void quad(float ax, float ay, float az, float au, float av, float bx, float by, float bz, float bu, float bv, float cx, float cy, float cz, float cu, float cv, float dx, float dy, float dz, float du, float dv, float n1x, float n1y, float n1z, float n2x, float n2y, float n2z, int color) {
            if (this.count * 12 + 12 > this.p.length) {
                int capacity = this.count * 2 + 16;
                this.p = Arrays.copyOf(this.p, capacity * 12);
                this.t = Arrays.copyOf(this.t, capacity * 8);
                this.n = Arrays.copyOf(this.n, capacity * 12);
                this.c = Arrays.copyOf(this.c, capacity);
            }
            int o = this.count * 12;
            int u = this.count * 8;
            this.p[o] = ax;
            this.p[o + 1] = ay;
            this.p[o + 2] = az;
            this.p[o + 3] = bx;
            this.p[o + 4] = by;
            this.p[o + 5] = bz;
            this.p[o + 6] = cx;
            this.p[o + 7] = cy;
            this.p[o + 8] = cz;
            this.p[o + 9] = dx;
            this.p[o + 10] = dy;
            this.p[o + 11] = dz;
            this.t[u] = au;
            this.t[u + 1] = av;
            this.t[u + 2] = bu;
            this.t[u + 3] = bv;
            this.t[u + 4] = cu;
            this.t[u + 5] = cv;
            this.t[u + 6] = du;
            this.t[u + 7] = dv;
            float l1 = (float)Math.sqrt(n1x * n1x + n1y * n1y + n1z * n1z);
            float l2 = (float)Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
            n1x /= l1;
            n1y /= l1;
            n1z /= l1;
            n2x /= l2;
            n2y /= l2;
            n2z /= l2;
            this.n[o] = n1x;
            this.n[o + 1] = n1y;
            this.n[o + 2] = n1z;
            this.n[o + 3] = n2x;
            this.n[o + 4] = n2y;
            this.n[o + 5] = n2z;
            this.n[o + 6] = n2x;
            this.n[o + 7] = n2y;
            this.n[o + 8] = n2z;
            this.n[o + 9] = n1x;
            this.n[o + 10] = n1y;
            this.n[o + 11] = n1z;
            this.c[this.count] = color;
            ++this.count;
        }
    }

}
