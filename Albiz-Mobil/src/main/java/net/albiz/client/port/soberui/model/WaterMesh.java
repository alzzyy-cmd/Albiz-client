// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import java.util.Arrays;

public final class WaterMesh {
    public final float[] positions;
    public final float[] normals;
    public final int quads;
    private static final float PICK_TOP = 1.12f;
    private static final int PICK_SEGMENTS = 24;
    private static final float[][] FRONT = WaterMesh.profile(0.34f, 0.25f, 0.007f);
    private static final float[][] BACK = WaterMesh.profile(0.2f, 0.13f, 0.022f);
    private static final int CHAIN = 16;
    private static final float[][] GUARD = WaterMesh.guard();
    private static final float[][] PRONG = WaterMesh.prong();

    WaterMesh(float[] positions, float[] normals) {
        this.positions = positions;
        this.normals = normals;
        this.quads = positions.length / 12;
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : Math.min(v, hi);
    }

    private static float length(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }

    private static float length(float x, float y, float z) {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    private static float smin(float a, float b, float k) {
        float h = Math.max(k - Math.abs(a - b), 0.0f) / k;
        return Math.min(a, b) - h * h * k * 0.25f;
    }

    private static float smax(float a, float b, float k) {
        return -WaterMesh.smin(-a, -b, k);
    }

    private static float roundBox(float x, float y, float z, float hx, float hy, float hz, float r) {
        float qx = Math.abs(x) - hx + r;
        float qy = Math.abs(y) - hy + r;
        float qz = Math.abs(z) - hz + r;
        float outside = WaterMesh.length(Math.max(qx, 0.0f), Math.max(qy, 0.0f), Math.max(qz, 0.0f));
        return outside + Math.min(Math.max(qx, Math.max(qy, qz)), 0.0f) - r;
    }

    private static float cone(float px, float py, float pz, float ax, float ay, float az, float bx, float by, float bz, float ra, float rb) {
        float bax = bx - ax;
        float bay = by - ay;
        float baz = bz - az;
        float pax = px - ax;
        float pay = py - ay;
        float paz = pz - az;
        float t = WaterMesh.clamp((pax * bax + pay * bay + paz * baz) / (bax * bax + bay * bay + baz * baz), 0.0f, 1.0f);
        return WaterMesh.length(pax - bax * t, pay - bay * t, paz - baz * t) - (ra + (rb - ra) * t);
    }

    private static float ring(float axial, float radial, float major, float minor) {
        return WaterMesh.length(radial - major, axial) - minor;
    }

    private static float[][] profile(float span, float drop, float tip) {
        float[][] out = new float[3][25];
        for (int i = 0; i <= 24; ++i) {
            float f = (float)i / 24.0f;
            out[0][i] = 1.05f - drop * (float)Math.pow(f, 2.3) + 0.016f * (float)Math.sin(Math.PI * (double)f) * (1.0f - f);
            out[1][i] = f * span;
            out[2][i] = tip + (0.078f - tip) * (float)Math.pow(1.0f - f, 0.6) + 0.008f * (float)Math.exp(-Math.pow((f - 0.12f) / 0.14f, 2.0));
        }
        return out;
    }

    private static float pickaxeField(float s, float l, float z) {
        float handle;
        float radial = WaterMesh.length(l, z);
        float d = handle = WaterMesh.cone(s, l, z, 0.035f, 0.0f, 0.0f, 1.02f, 0.0f, 0.0f, 0.026f, 0.027f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.47f, radial, 0.028f, 0.008f), 0.008f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.455f, radial, 0.027f, 0.006f), 0.006f);
        float pommel = WaterMesh.roundBox(s - 0.02f, l, z, 0.03f, 0.033f, 0.033f, 0.026f);
        d = WaterMesh.smin(d, pommel, 0.016f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.062f, radial, 0.03f, 0.0075f), 0.006f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.92f, 0.0f, 0.0f, 0.98f, 0.0f, 0.0f, 0.028f, 0.04f), 0.012f);
        float al = Math.abs(l);
        float hz = z / 0.68f;
        float head = Float.MAX_VALUE;
        float[][] side = l >= 0.0f ? FRONT : BACK;
        for (int i = 0; i < 24; ++i) {
            float seg = WaterMesh.cone(s, al, hz, side[0][i], side[1][i], 0.0f, side[0][i + 1], side[1][i + 1], 0.0f, side[2][i], side[2][i + 1]);
            if (!(seg < head)) continue;
            head = seg;
        }
        d = WaterMesh.smin(d, head *= 0.8f, 0.022f);
        float cap = WaterMesh.roundBox(s - 1.122f, l, z, 0.017f, 0.055f, 0.042f, 0.009f);
        d = WaterMesh.smin(d, cap, 0.006f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.945f, radial, 0.031f, 0.008f), 0.006f);
        return d;
    }

    private static float totemField(float x, float y, float z) {
        float cx = x - 0.5f;
        float cz = z - 0.5f;
        float side = Math.abs(cx);
        float d = WaterMesh.roundBox(cx, y - 0.805f, cz, 0.148f, 0.17f, 0.128f, 0.055f);
        float eye = WaterMesh.roundBox(side - 0.064f, y - 0.795f, cz - 0.128f, 0.028f, 0.03f, 0.013f, 0.011f);
        d = WaterMesh.smax(d, -eye, 0.012f);
        float torso = WaterMesh.roundBox(cx, y - 0.565f, cz, 0.118f, 0.09f, 0.1f, 0.055f);
        d = WaterMesh.smin(d, torso, 0.03f);
        float arm = WaterMesh.cone(side, y, cz, 0.06f, 0.605f, 0.0f, 0.305f, 0.6f, -0.035f, 0.05f, 0.058f);
        float sleeve = WaterMesh.roundBox(side - 0.2f, y - 0.54f, cz + 0.012f, 0.105f, 0.058f, 0.017f, 0.012f);
        float teeth = 0.49f + 0.034f * Math.abs((side - 0.1f) / 0.042f % 1.0f - 0.5f) * 2.0f;
        sleeve = Math.max(sleeve, teeth - y);
        arm = WaterMesh.smin(arm, sleeve, 0.02f);
        d = WaterMesh.smin(d, arm, 0.028f);
        float belt = WaterMesh.ring(y - 0.465f, WaterMesh.length(cx, cz / 0.85f), 0.11f, 0.019f);
        belt = WaterMesh.smin(belt, WaterMesh.length(cx + 0.02f, y - 0.46f, cz - 0.098f) - 0.024f, 0.01f);
        belt = WaterMesh.smin(belt, WaterMesh.length(cx - 0.105f, y - 0.47f, cz - 0.02f) - 0.022f, 0.01f);
        d = WaterMesh.smin(d, belt, 0.012f);
        float angle = (float)Math.atan2(cz, cx);
        float t = WaterMesh.clamp((0.47f - y) / 0.44f, 0.0f, 1.0f);
        float folds = 0.05f * t * (float)Math.sin(angle * 6.0f + 0.7f) + 0.025f * t * (float)Math.sin(angle * 11.0f + 2.1f);
        float radius = (0.104f + 0.135f * (float)Math.pow(t, 1.8)) * (1.0f + folds);
        float robeSide = (WaterMesh.length(cx, cz / 0.8f) - radius) * 0.72f;
        float wave = 0.5f + 0.5f * (float)Math.cos((double)(angle * 5.0f) - 1.5707963267948966);
        float edge = WaterMesh.clamp(WaterMesh.length(cx, cz / 0.8f) / radius, 0.0f, 1.0f);
        float hem = 0.025f + 0.065f * (1.0f - wave * wave * wave) * edge * edge * edge;
        float robe = Math.max(robeSide, Math.max(y - 0.48f, hem - y));
        d = WaterMesh.smin(d, robe, 0.035f);
        return d;
    }

    private static float chain(float s, float l, float z, float[] cs, float[] cl, float[] cr) {
        float best = Float.MAX_VALUE;
        int i = 0;
        while (i + 1 < cs.length) {
            float d = WaterMesh.cone(s, l, z, cs[i], cl[i], 0.0f, cs[i + 1], cl[i + 1], 0.0f, cr[i], cr[i + 1]);
            if (d < best) {
                best = d;
            }
            ++i;
        }
        return best;
    }

    private static float[][] guard() {
        float[][] out = new float[3][17];
        for (int i = 0; i <= 16; ++i) {
            float f = (float)i / 16.0f;
            out[0][i] = 0.29f + 0.05f * f * f;
            out[1][i] = 0.165f * f;
            out[2][i] = 0.024f - 0.006f * f;
        }
        return out;
    }

    private static float[][] prong() {
        float[][] out = new float[3][17];
        for (int i = 0; i <= 16; ++i) {
            float f = (float)i / 16.0f;
            out[0][i] = 0.975f + 0.27f * f;
            out[1][i] = 0.135f + 0.035f * (float)Math.sin(Math.PI * (double)f * 0.85);
            out[2][i] = 0.019f - 0.0155f * (float)Math.pow(f, 1.6);
        }
        return out;
    }

    private static float swordField(float s, float l, float z) {
        float radial = WaterMesh.length(l, z);
        float d = WaterMesh.length(s - 0.036f, l, z) - 0.036f;
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.05f, 0.0f, 0.0f, 0.085f, 0.0f, 0.0f, 0.028f, 0.02f), 0.012f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.08f, 0.0f, 0.0f, 0.28f, 0.0f, 0.0f, 0.023f, 0.026f), 0.008f);
        for (int i = 0; i < 3; ++i) {
            d = WaterMesh.smin(d, WaterMesh.ring(s - (0.125f + (float)i * 0.05f), radial, 0.025f, 0.006f), 0.005f);
        }
        float al = Math.abs(l);
        float guard = WaterMesh.chain(s, al, z, GUARD[0], GUARD[1], GUARD[2]);
        guard = WaterMesh.smin(guard, WaterMesh.length(s - 0.342f, al - 0.17f, z) - 0.03f, 0.014f);
        guard = WaterMesh.smin(guard, WaterMesh.roundBox(s - 0.295f, l, z, 0.032f, 0.042f, 0.032f, 0.016f), 0.012f);
        d = WaterMesh.smin(d, guard, 0.012f);
        float t = WaterMesh.clamp((s - 0.3f) / 0.92f, 0.0f, 1.0f);
        float width = 0.076f * (1.0f - (float)Math.pow(t, 2.4)) * (1.0f + 0.12f * (float)Math.sin(Math.PI * (double)t * 0.85)) + 0.004f;
        float thick = 0.024f * (1.0f - (float)Math.pow(t, 1.6)) + 0.006f;
        float ridge = 1.0f - 0.35f * Math.min(1.0f, al / width);
        float blade = (WaterMesh.length(al / width, Math.abs(z) / (thick * ridge)) - 1.0f) * Math.min(width, thick * ridge) * 0.85f;
        blade = Math.max(blade, Math.max(0.3f - s, s - 1.22f));
        d = WaterMesh.smin(d, blade, 0.01f);
        return d;
    }

    private static float shovelField(float s, float l, float z) {
        float radial = WaterMesh.length(l, z);
        float d = WaterMesh.cone(s, l, z, 0.035f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.025f, 0.026f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.4f, radial, 0.027f, 0.008f), 0.008f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.385f, radial, 0.026f, 0.006f), 0.006f);
        d = WaterMesh.smin(d, WaterMesh.roundBox(s - 0.02f, l, z, 0.03f, 0.033f, 0.033f, 0.026f), 0.016f);
        d = WaterMesh.smin(d, WaterMesh.ring(s - 0.062f, radial, 0.03f, 0.0075f), 0.006f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.7f, 0.0f, 0.0f, 0.81f, 0.0f, 0.0f, 0.027f, 0.04f), 0.01f);
        float es = (s - 0.95f) / 0.19f;
        float el = l / 0.118f;
        float point = Math.max(0.0f, es) * 0.35f;
        float shape = ((float)Math.sqrt(es * es + el * el / ((1.0f - point) * (1.0f - point))) - 1.0f) * 0.11f;
        float dish = 0.032f * el * el - 0.012f;
        float plate = Math.abs(z - dish) - 0.016f;
        float blade = Math.max(shape, plate);
        blade = WaterMesh.smin(blade, WaterMesh.cone(s, l, z + 0.006f, 0.79f, 0.0f, 0.0f, 1.07f, 0.0f, 0.0f, 0.022f, 0.008f), 0.012f);
        d = WaterMesh.smin(d, blade, 0.014f);
        return d;
    }

    private static float tridentField(float s, float l, float z) {
        float radial = WaterMesh.length(l, z);
        float d = WaterMesh.cone(s, l, z, 0.025f, 0.0f, 0.0f, 0.93f, 0.0f, 0.0f, 0.0185f, 0.02f);
        d = WaterMesh.smin(d, WaterMesh.length(s - 0.03f, l, z) - 0.026f, 0.01f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.03f, 0.0f, 0.0f, -0.035f, 0.0f, 0.0f, 0.018f, 0.004f), 0.006f);
        for (int i = 0; i < 3; ++i) {
            d = WaterMesh.smin(d, WaterMesh.ring(s - (0.26f + (float)i * 0.24f), radial, 0.02f, 0.006f), 0.005f);
        }
        float al = Math.abs(l);
        float bar = WaterMesh.chain(s, al, z, new float[]{0.92f, 0.935f, 0.975f}, new float[]{0.0f, 0.08f, 0.14f}, new float[]{0.024f, 0.022f, 0.02f});
        bar = WaterMesh.smin(bar, WaterMesh.roundBox(s - 0.9f, l, z, 0.032f, 0.036f, 0.03f, 0.014f), 0.012f);
        d = WaterMesh.smin(d, bar, 0.012f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, l, z, 0.92f, 0.0f, 0.0f, 1.34f, 0.0f, 0.0f, 0.022f, 0.003f), 0.01f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, al, z, 1.215f, 0.004f, 0.0f, 1.17f, 0.05f, 0.0f, 0.011f, 0.003f), 0.006f);
        d = WaterMesh.smin(d, WaterMesh.chain(s, al, z, PRONG[0], PRONG[1], PRONG[2]), 0.012f);
        d = WaterMesh.smin(d, WaterMesh.cone(s, al, z, 1.15f, 0.158f, 0.0f, 1.11f, 0.118f, 0.0f, 0.01f, 0.003f), 0.006f);
        return d;
    }

    static WaterMesh contour(Field field, Place place, float x0, float y0, float z0, float x1, float y1, float z1, float cell) {
        int nx = (int)Math.ceil((x1 - x0) / cell) + 1;
        int ny = (int)Math.ceil((y1 - y0) / cell) + 1;
        int nz = (int)Math.ceil((z1 - z0) / cell) + 1;
        float[] values = new float[nx * ny * nz];
        for (int k = 0; k < nz; ++k) {
            for (int j = 0; j < ny; ++j) {
                for (int i = 0; i < nx; ++i) {
                    values[(k * ny + j) * nx + i] = field.at(x0 + (float)i * cell, y0 + (float)j * cell, z0 + (float)k * cell);
                }
            }
        }
        int cx = nx - 1;
        int cy = ny - 1;
        int cz = nz - 1;
        int[] index = new int[cx * cy * cz];
        Arrays.fill(index, -1);
        float[] verts = new float[12288];
        float[] norms = new float[12288];
        int count = 0;
        int[][] edges = new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        float[] corner = new float[8];
        float h = cell * 0.35f;
        for (int k = 0; k < cz; ++k) {
            for (int j = 0; j < cy; ++j) {
                for (int i = 0; i < cx; ++i) {
                    float gl;
                    int mask = 0;
                    for (int c = 0; c < 8; ++c) {
                        int ci = i + (c & 1);
                        int cj = j + (c >> 1 & 1);
                        int ck = k + (c >> 2 & 1);
                        corner[c] = values[(ck * ny + cj) * nx + ci];
                        if (!(corner[c] < 0.0f)) continue;
                        mask |= 1 << c;
                    }
                    if (mask == 0 || mask == 255) continue;
                    float sx = 0.0f;
                    float sy = 0.0f;
                    float sz = 0.0f;
                    int crossings = 0;
                    for (int[] e : edges) {
                        float b;
                        float a = corner[e[0]];
                        if (a < 0.0f == (b = corner[e[1]]) < 0.0f) continue;
                        float t = a / (a - b);
                        float ax = e[0] & 1;
                        float ay = e[0] >> 1 & 1;
                        float az = e[0] >> 2 & 1;
                        float bx = e[1] & 1;
                        float by = e[1] >> 1 & 1;
                        float bz = e[1] >> 2 & 1;
                        sx += ax + (bx - ax) * t;
                        sy += ay + (by - ay) * t;
                        sz += az + (bz - az) * t;
                        ++crossings;
                    }
                    float px = x0 + ((float)i + sx / (float)crossings) * cell;
                    float py = y0 + ((float)j + sy / (float)crossings) * cell;
                    float pz = z0 + ((float)k + sz / (float)crossings) * cell;
                    float gx = 0.0f;
                    float gy = 0.0f;
                    float gz = 0.0f;
                    for (int step = 0; step < 3; ++step) {
                        float g2;
                        float f = field.at(px, py, pz);
                        gx = (field.at(px + h, py, pz) - field.at(px - h, py, pz)) / (2.0f * h);
                        gy = (field.at(px, py + h, pz) - field.at(px, py - h, pz)) / (2.0f * h);
                        gz = (field.at(px, py, pz + h) - field.at(px, py, pz - h)) / (2.0f * h);
                        if (step == 2 || (g2 = gx * gx + gy * gy + gz * gz) < 1.0E-8f) break;
                        float mx = WaterMesh.clamp(-f * gx / g2, -cell * 0.5f, cell * 0.5f);
                        float my = WaterMesh.clamp(-f * gy / g2, -cell * 0.5f, cell * 0.5f);
                        float mz = WaterMesh.clamp(-f * gz / g2, -cell * 0.5f, cell * 0.5f);
                        px += mx;
                        py += my;
                        pz += mz;
                    }
                    if ((gl = WaterMesh.length(gx, gy, gz)) < 1.0E-6f) {
                        gl = 1.0f;
                    }
                    if (count * 3 + 3 > verts.length) {
                        verts = Arrays.copyOf(verts, verts.length * 2);
                        norms = Arrays.copyOf(norms, norms.length * 2);
                    }
                    verts[count * 3] = px;
                    verts[count * 3 + 1] = py;
                    verts[count * 3 + 2] = pz;
                    norms[count * 3] = gx / gl;
                    norms[count * 3 + 1] = gy / gl;
                    norms[count * 3 + 2] = gz / gl;
                    index[(k * cy + j) * cx + i] = count++;
                }
            }
        }
        Builder builder = new Builder();
        float[] p = new float[3];
        float[] n = new float[3];
        int[] cellAt = new int[4];
        for (int axis = 0; axis < 3; ++axis) {
            int b = (axis + 1) % 3;
            int c = (axis + 2) % 3;
            for (int k = 0; k < nz; ++k) {
                for (int j = 0; j < ny; ++j) {
                    for (int i = 0; i < nx; ++i) {
                        int id;
                        float v1;
                        int[] g = new int[]{i, j, k};
                        int[] size = new int[]{nx, ny, nz};
                        if (g[axis] + 1 >= size[axis] || g[b] < 1 || g[c] < 1 || g[b] >= size[b] - 1 || g[c] >= size[c] - 1) continue;
                        int[] next = (int[])g.clone();
                        int n2 = axis;
                        next[n2] = next[n2] + 1;
                        float v0 = values[(g[2] * ny + g[1]) * nx + g[0]];
                        if (v0 < 0.0f == (v1 = values[(next[2] * ny + next[1]) * nx + next[0]]) < 0.0f) continue;
                        int[][] offsets = new int[][]{{-1, -1}, {0, -1}, {0, 0}, {-1, 0}};
                        boolean ok = true;
                        for (int q = 0; q < 4; ++q) {
                            int[] cc = (int[])g.clone();
                            int n3 = b;
                            cc[n3] = cc[n3] + offsets[q][0];
                            int n4 = c;
                            cc[n4] = cc[n4] + offsets[q][1];
                            id = index[(cc[2] * cy + cc[1]) * cx + cc[0]];
                            if (id < 0) {
                                ok = false;
                                break;
                            }
                            cellAt[q] = id;
                        }
                        if (!ok) continue;
                        boolean forward = v0 < 0.0f;
                        for (int q = 0; q < 4; ++q) {
                            id = cellAt[forward ? q : 3 - q];
                            p[0] = verts[id * 3];
                            p[1] = verts[id * 3 + 1];
                            p[2] = verts[id * 3 + 2];
                            n[0] = norms[id * 3];
                            n[1] = norms[id * 3 + 1];
                            n[2] = norms[id * 3 + 2];
                            if (place != null) {
                                place.apply(p, n);
                            }
                            builder.vertex(p, n);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    public static WaterMesh pickaxe() {
        return WaterMesh.pickaxe(0.0f, 0.0105f);
    }

    public static WaterMesh pickaxeGlow() {
        return WaterMesh.pickaxe(0.016f, 0.02f);
    }

    public static WaterMesh sword() {
        return WaterMesh.diagonal(WaterMesh::swordField, 0.0f, 0.0095f, 0.06f, -0.01f, 1.24f, 0.21f, 0.06f);
    }

    public static WaterMesh swordGlow() {
        return WaterMesh.diagonal(WaterMesh::swordField, 0.016f, 0.02f, 0.06f, -0.01f, 1.24f, 0.21f, 0.06f);
    }

    public static WaterMesh shovel() {
        return WaterMesh.diagonal(WaterMesh::shovelField, 0.0f, 0.0105f, 0.06f, -0.01f, 1.16f, 0.14f, 0.07f);
    }

    public static WaterMesh shovelGlow() {
        return WaterMesh.diagonal(WaterMesh::shovelField, 0.016f, 0.02f, 0.06f, -0.01f, 1.16f, 0.14f, 0.07f);
    }

    public static WaterMesh trident() {
        return WaterMesh.diagonal(WaterMesh::tridentField, 0.0f, 0.009f, 0.02f, -0.05f, 1.36f, 0.2f, 0.04f);
    }

    public static WaterMesh tridentGlow() {
        return WaterMesh.diagonal(WaterMesh::tridentField, 0.016f, 0.02f, 0.02f, -0.05f, 1.36f, 0.2f, 0.04f);
    }

    public static WaterMesh totem() {
        return WaterMesh.totem(0.0f, 0.015f);
    }

    public static WaterMesh totemGlow() {
        return WaterMesh.totem(0.016f, 0.026f);
    }

    private static WaterMesh diagonal(Field field, float grow, float cell, float origin, float s0, float s1, float halfWidth, float halfDepth) {
        float root = (float)Math.sqrt(0.5);
        Place place = (p, n) -> {
            float s = p[0];
            float l = p[1];
            float z = p[2];
            p[0] = origin + root * s - root * l;
            p[1] = origin + root * s + root * l;
            p[2] = 0.5f + z;
            float ns = n[0];
            float nl = n[1];
            float nz = n[2];
            n[0] = root * ns - root * nl;
            n[1] = root * ns + root * nl;
            n[2] = nz;
        };
        float pad = grow + cell;
        return WaterMesh.contour((s, l, z) -> field.at(s, l, z) - grow, place, s0 - pad, -halfWidth - pad, -halfDepth - pad, s1 + pad, halfWidth + pad, halfDepth + pad, cell);
    }

    private static WaterMesh pickaxe(float grow, float cell) {
        float root = (float)Math.sqrt(0.5);
        Place place = (p, n) -> {
            float s = p[0];
            float l = p[1];
            float z = p[2];
            p[0] = 0.05f + root * s - root * l;
            p[1] = 0.05f + root * s + root * l;
            p[2] = 0.5f + z;
            float ns = n[0];
            float nl = n[1];
            float nz = n[2];
            n[0] = root * ns - root * nl;
            n[1] = root * ns + root * nl;
            n[2] = nz;
        };
        float pad = grow + cell;
        return WaterMesh.contour((s, l, z) -> WaterMesh.pickaxeField(s, l, z) - grow, place, -0.04f - pad, -0.25f - pad, -0.08f - pad, 1.15f + pad, 0.37f + pad, 0.08f + pad, cell);
    }

    private static WaterMesh totem(float grow, float cell) {
        float pad = grow + cell;
        return WaterMesh.contour((x, y, z) -> WaterMesh.totemField(x, y, z) - grow, null, 0.1f - pad, 0.0f - pad, 0.28f - pad, 0.9f + pad, 1.0f + pad, 0.72f + pad, cell);
    }

    static interface Field {
        public float at(float var1, float var2, float var3);
    }

    private static final class Builder {
        private float[] pos = new float[12288];
        private float[] nrm = new float[12288];
        private int size;

        private Builder() {
        }

        void vertex(float[] p, float[] n) {
            if (this.size + 3 > this.pos.length) {
                this.pos = Arrays.copyOf(this.pos, this.pos.length * 2);
                this.nrm = Arrays.copyOf(this.nrm, this.nrm.length * 2);
            }
            System.arraycopy(p, 0, this.pos, this.size, 3);
            System.arraycopy(n, 0, this.nrm, this.size, 3);
            this.size += 3;
        }

        WaterMesh build() {
            return new WaterMesh(Arrays.copyOf(this.pos, this.size), Arrays.copyOf(this.nrm, this.size));
        }
    }

    static interface Place {
        public void apply(float[] var1, float[] var2);
    }
}

