// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import net.albiz.client.port.soberui.model.WaterMesh;
import java.util.Arrays;

public final class WaterIconRaster {
    private static final int SAMPLES = 3;

    private WaterIconRaster() {
    }

    private static float clamp(float v) {
        return v < 0.0f ? 0.0f : Math.min(1.0f, v);
    }

    public static int[] render(WaterMesh mesh, int rgb, int size) {
        int c;
        int big = size * 3;
        float[] p = mesh.positions;
        float[] n = mesh.normals;
        int vertices = mesh.quads * 4;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int v = 0; v < vertices; ++v) {
            minX = Math.min(minX, p[v * 3]);
            maxX = Math.max(maxX, p[v * 3]);
            minY = Math.min(minY, p[v * 3 + 1]);
            maxY = Math.max(maxY, p[v * 3 + 1]);
        }
        float extent = Math.max(maxX - minX, maxY - minY);
        float margin = (float)big * 0.08f;
        float scale = ((float)big - margin * 2.0f) / Math.max(1.0E-6f, extent);
        float offX = margin + ((float)big - margin * 2.0f - (maxX - minX) * scale) / 2.0f;
        float offY = margin + ((float)big - margin * 2.0f - (maxY - minY) * scale) / 2.0f;
        float br = (float)(rgb >> 16 & 0xFF) / 255.0f;
        float bg = (float)(rgb >> 8 & 0xFF) / 255.0f;
        float bb = (float)(rgb & 0xFF) / 255.0f;
        float lx = -0.4f;
        float ly = 0.7f;
        float lz = 0.6f;
        float ll = (float)Math.sqrt(lx * lx + ly * ly + lz * lz);
        float hx = lx /= ll;
        float hy = ly /= ll;
        float hz = (lz /= ll) + 1.0f;
        float hl = (float)Math.sqrt(hx * hx + hy * hy + hz * hz);
        hx /= hl;
        hy /= hl;
        hz /= hl;
        float[] depth = new float[big * big];
        Arrays.fill(depth, -3.4028235E38f);
        int[] pixels = new int[big * big];
        float[] sx = new float[3];
        float[] sy = new float[3];
        float[] sz = new float[3];
        float[] nx = new float[3];
        float[] ny = new float[3];
        float[] nz = new float[3];
        int[][] tris = new int[][]{{0, 1, 2}, {0, 2, 3}};
        for (int q = 0; q < mesh.quads; ++q) {
            for (int[] tri : tris) {
                for (int k = 0; k < 3; ++k) {
                    int v = q * 4 + tri[k];
                    sx[k] = (p[v * 3] - minX) * scale + offX;
                    sy[k] = (float)big - ((p[v * 3 + 1] - minY) * scale + offY);
                    sz[k] = p[v * 3 + 2];
                    nx[k] = n[v * 3];
                    ny[k] = n[v * 3 + 1];
                    nz[k] = n[v * 3 + 2];
                }
                float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0]);
                if (Math.abs(area) < 1.0E-6f) continue;
                int x0 = Math.max(0, (int)Math.floor(Math.min(sx[0], Math.min(sx[1], sx[2]))));
                int x1 = Math.min(big - 1, (int)Math.ceil(Math.max(sx[0], Math.max(sx[1], sx[2]))));
                int y0 = Math.max(0, (int)Math.floor(Math.min(sy[0], Math.min(sy[1], sy[2]))));
                int y1 = Math.min(big - 1, (int)Math.ceil(Math.max(sy[0], Math.max(sy[1], sy[2]))));
                for (int y = y0; y <= y1; ++y) {
                    for (int x = x0; x <= x1; ++x) {
                        int index;
                        float z;
                        float px = (float)x + 0.5f;
                        float py = (float)y + 0.5f;
                        float w0 = ((sx[1] - px) * (sy[2] - py) - (sx[2] - px) * (sy[1] - py)) / area;
                        float w1 = ((sx[2] - px) * (sy[0] - py) - (sx[0] - px) * (sy[2] - py)) / area;
                        float w2 = 1.0f - w0 - w1;
                        if (w0 < 0.0f || w1 < 0.0f || w2 < 0.0f || (z = w0 * sz[0] + w1 * sz[1] + w2 * sz[2]) <= depth[index = y * big + x]) continue;
                        depth[index] = z;
                        float ax = w0 * nx[0] + w1 * nx[1] + w2 * nx[2];
                        float ay = w0 * ny[0] + w1 * ny[1] + w2 * ny[2];
                        float az = w0 * nz[0] + w1 * nz[1] + w2 * nz[2];
                        float len = (float)Math.sqrt(ax * ax + ay * ay + az * az);
                        if (len < 1.0E-6f) continue;
                        ax /= len;
                        ay /= len;
                        if ((az /= len) < 0.0f) {
                            ax = -ax;
                            ay = -ay;
                            az = -az;
                        }
                        pixels[index] = WaterIconRaster.shade(ax, ay, az, br, bg, bb, lx, ly, lz, hx, hy, hz);
                    }
                }
            }
        }
        int[] small = new int[size * size];
        float[] mask = new float[size * size];
        int count = 9;
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                for (int dy = 0; dy < 3; ++dy) {
                    for (int dx = 0; dx < 3; ++dx) {
                        c = pixels[(y * 3 + dy) * big + x * 3 + dx];
                        int ca = c >>> 24;
                        a += ca;
                        r += (c >> 16 & 0xFF) * ca / 255;
                        g += (c >> 8 & 0xFF) * ca / 255;
                        b += (c & 0xFF) * ca / 255;
                    }
                }
                int alpha = a / count;
                int sum = Math.max(1, a);
                small[y * size + x] = alpha << 24 | Math.min(255, r * 255 / sum) << 16 | Math.min(255, g * 255 / sum) << 8 | Math.min(255, b * 255 / sum);
                mask[y * size + x] = (float)alpha / 255.0f;
            }
        }
        float[] glow = WaterIconRaster.blur(WaterIconRaster.blur(mask, size, 2), size, 2);
        float gr = br + (1.0f - br) * 0.55f;
        float gg = bg + (1.0f - bg) * 0.55f;
        float gb = bb + (1.0f - bb) * 0.55f;
        int[] out = new int[size * size];
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                int i = y * size + x;
                c = small[i];
                float fa = (float)(c >>> 24) / 255.0f;
                float halo = WaterIconRaster.clamp(glow[i] * 1.6f) * 0.75f * (1.0f - fa);
                float outA = fa + halo;
                if (outA <= 0.001f) {
                    out[i] = 0;
                    continue;
                }
                float r = ((float)(c >> 16 & 0xFF) / 255.0f * fa + gr * halo) / outA;
                float g = ((float)(c >> 8 & 0xFF) / 255.0f * fa + gg * halo) / outA;
                float b = ((float)(c & 0xFF) / 255.0f * fa + gb * halo) / outA;
                out[i] = Math.round(WaterIconRaster.clamp(outA) * 255.0f) << 24 | Math.round(WaterIconRaster.clamp(r) * 255.0f) << 16 | Math.round(WaterIconRaster.clamp(g) * 255.0f) << 8 | Math.round(WaterIconRaster.clamp(b) * 255.0f);
            }
        }
        return out;
    }

    private static int shade(float x, float y, float z, float br, float bg, float bb, float lx, float ly, float lz, float hx, float hy, float hz) {
        float ndv = WaterIconRaster.clamp(z);
        float diffuse = WaterIconRaster.clamp(x * lx + y * ly + z * lz);
        float mix = WaterIconRaster.clamp(diffuse * 0.6f + ndv * 0.45f - 0.08f);
        float r = br * (0.18f + 0.5f * mix);
        float g = bg * (0.18f + 0.5f * mix);
        float b = bb * (0.18f + 0.5f * mix);
        float fres = (float)Math.pow(1.0f - ndv, 3.0);
        r += 0.3f * fres * 0.9f;
        g += 0.65f * fres * 0.9f;
        b += 1.0f * fres * 0.9f;
        float edge = WaterIconRaster.clamp((0.28f - ndv) / 0.28f);
        r += 0.6f * edge * 0.75f;
        g += 0.92f * edge * 0.75f;
        b += 1.0f * edge * 0.75f;
        float spec = (float)Math.pow(WaterIconRaster.clamp(x * hx + y * hy + z * hz), 60.0) * 1.4f;
        return 0xFF000000 | Math.round(WaterIconRaster.clamp(r += spec) * 255.0f) << 16 | Math.round(WaterIconRaster.clamp(g += spec) * 255.0f) << 8 | Math.round(WaterIconRaster.clamp(b += spec) * 255.0f);
    }

    private static float[] blur(float[] src, int size, int radius) {
        int k;
        float sum;
        int x;
        int y;
        float[] tmp = new float[src.length];
        float[] out = new float[src.length];
        for (y = 0; y < size; ++y) {
            for (x = 0; x < size; ++x) {
                sum = 0.0f;
                for (k = -radius; k <= radius; ++k) {
                    int xx = Math.max(0, Math.min(size - 1, x + k));
                    sum += src[y * size + xx];
                }
                tmp[y * size + x] = sum / (float)(radius * 2 + 1);
            }
        }
        for (y = 0; y < size; ++y) {
            for (x = 0; x < size; ++x) {
                sum = 0.0f;
                for (k = -radius; k <= radius; ++k) {
                    int yy = Math.max(0, Math.min(size - 1, y + k));
                    sum += tmp[yy * size + x];
                }
                out[y * size + x] = sum / (float)(radius * 2 + 1);
            }
        }
        return out;
    }
}

