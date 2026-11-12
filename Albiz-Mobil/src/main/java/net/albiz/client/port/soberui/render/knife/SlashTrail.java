// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render.knife;

import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;

public final class SlashTrail {
    private static final int CAPACITY = 96;
    private static final long GAP_NANOS = 100000000L;
    private static final float SLOW = 0.6f;
    private static final float FAST = 2.8f;
    private static final float ALPHA = 0.6f;
    private static final float BASE_ALPHA = 0.12f;
    private final float[] points = new float[576];
    private final long[] times = new long[96];
    private int newest = -1;
    private int size;

    public void add(long now, float[] sample, int from) {
        this.newest = (this.newest + 1) % 96;
        System.arraycopy(sample, from, this.points, this.newest * 6, 6);
        this.times[this.newest] = now;
        this.size = Math.min(this.size + 1, 96);
    }

    public void clear() {
        this.size = 0;
        this.newest = -1;
    }

    public boolean isEmpty(long now, long lifetime) {
        return this.size == 0 || now - this.times[this.newest] > lifetime;
    }

    public void emit(class_4587.class_4665 pose, class_4588 out, long now, long lifetime, int rgb, float strength) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        int k = 0;
        while (k + 1 < this.size) {
            float dz;
            float dy;
            int o;
            int n;
            float dx;
            float speed;
            float fast;
            int newer = Math.floorMod(this.newest - k, 96);
            int older = Math.floorMod(this.newest - k - 1, 96);
            long ageNewer = now - this.times[newer];
            long ageOlder = now - this.times[older];
            if (ageOlder > lifetime) break;
            long dt = this.times[newer] - this.times[older];
            if (dt > 0L && dt <= 100000000L && !((fast = class_3532.method_15363((float)(((speed = class_3532.method_15355((float)((dx = this.points[(n = newer * 6) + 3] - this.points[(o = older * 6) + 3]) * dx + (dy = this.points[n + 4] - this.points[o + 4]) * dy + (dz = this.points[n + 5] - this.points[o + 5]) * dz)) / ((float)dt / 1.0E9f)) - 0.6f) / 2.1999998f), (float)0.0f, (float)1.0f)) <= 0.0f)) {
                int v;
                float aNewer = SlashTrail.fade(ageNewer, lifetime) * fast * strength;
                float aOlder = SlashTrail.fade(ageOlder, lifetime) * fast * strength;
                float[] quad = new float[]{this.points[o], this.points[o + 1], this.points[o + 2], aOlder * 0.12f, this.points[o + 3], this.points[o + 4], this.points[o + 5], aOlder, this.points[n + 3], this.points[n + 4], this.points[n + 5], aNewer, this.points[n], this.points[n + 1], this.points[n + 2], aNewer * 0.12f};
                for (v = 0; v < 4; ++v) {
                    SlashTrail.vertex(pose, out, quad, v, r, g, b);
                }
                for (v = 3; v >= 0; --v) {
                    SlashTrail.vertex(pose, out, quad, v, r, g, b);
                }
            }
            ++k;
        }
    }

    private static float fade(long age, long lifetime) {
        float left = 1.0f - class_3532.method_15363((float)((float)age / (float)lifetime), (float)0.0f, (float)1.0f);
        return left * left * 0.6f;
    }

    private static void vertex(class_4587.class_4665 pose, class_4588 out, float[] quad, int v, int r, int g, int b) {
        int i = v * 4;
        out.method_56824(pose, quad[i], quad[i + 1], quad[i + 2]).method_1336(r, g, b, Math.round(class_3532.method_15363((float)quad[i + 3], (float)0.0f, (float)1.0f) * 255.0f)).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(0xF000F0).method_60831(pose, 0.0f, 0.0f, 1.0f);
    }

    public static void sparkle(class_4587.class_4665 pose, class_4588 out, float x, float y, float z, float size, float turn, int rgb, float alpha) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        int core = Math.round(class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f) * 255.0f);
        for (int arm = 0; arm < 4; ++arm) {
            float angle = turn + (float)arm * 1.5707964f;
            float cos = class_3532.method_15362((double)angle);
            float sin = class_3532.method_15374((double)angle);
            float length = size * (arm % 2 == 0 ? 1.0f : 0.7f);
            float width = size * 0.16f;
            float tx = x + cos * length;
            float ty = y + sin * length;
            float lx = x - sin * width;
            float ly = y + cos * width;
            float rx = x + sin * width;
            float ry = y - cos * width;
            SlashTrail.star(pose, out, x, y, z, r, g, b, core);
            SlashTrail.star(pose, out, lx, ly, z, r, g, b, core / 3);
            SlashTrail.star(pose, out, tx, ty, z, r, g, b, 0);
            SlashTrail.star(pose, out, rx, ry, z, r, g, b, core / 3);
            SlashTrail.star(pose, out, rx, ry, z, r, g, b, core / 3);
            SlashTrail.star(pose, out, tx, ty, z, r, g, b, 0);
            SlashTrail.star(pose, out, lx, ly, z, r, g, b, core / 3);
            SlashTrail.star(pose, out, x, y, z, r, g, b, core);
        }
    }

    private static void star(class_4587.class_4665 pose, class_4588 out, float x, float y, float z, int r, int g, int b, int a) {
        out.method_56824(pose, x, y, z).method_1336(r, g, b, a).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(0xF000F0).method_60831(pose, 0.0f, 0.0f, 1.0f);
    }
}

