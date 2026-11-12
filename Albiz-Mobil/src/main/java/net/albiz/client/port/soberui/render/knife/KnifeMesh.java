// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render.knife;

import net.albiz.client.port.soberui.render.knife.KnifeMaterial;
import java.util.Arrays;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;

public final class KnifeMesh {
    private static final float SHINE_WIDTH = 2.6f;
    private static final float SHINE_BOOST = 1.5f;
    private static final float WIPE_FRONT = 1.5f;
    private static final float GLOW_ALPHA = 0.85f;
    private static final float GLINT_UV = 0.09f;
    private final float[] pos;
    private final float[] normal;
    private final byte[] material;
    private final float[] tone;
    private final int quads;
    private final float shineFrom;
    private final float shineTo;

    private KnifeMesh(float[] pos, float[] normal, byte[] material, float[] tone, int quads, float shineFrom, float shineTo) {
        this.pos = pos;
        this.normal = normal;
        this.material = material;
        this.tone = tone;
        this.quads = quads;
        this.shineFrom = shineFrom;
        this.shineTo = shineTo;
    }

    public void collect(net.minecraft.class_4587 pose,java.util.List<Float> vertices,java.util.List<Integer> colors,int[] palette){
      var matrix=pose.method_23760().method_23761();int[] order={0,1,2,0,2,3};var v=new org.joml.Vector3f();
      for(int q=0;q<quads;q++)for(int t=0;t<2;t++){colors.add(palette[material[q]]&0xffffff);for(int n=0;n<3;n++){int a=q*12+order[t*3+n]*3;v.set(pos[a],pos[a+1],pos[a+2]).mulPosition(matrix);vertices.add(v.x);vertices.add(v.y);vertices.add(v.z);}}
    }

    void emit(class_4587.class_4665 pose, class_4588 out, int[] palette, int light, Shine shine) {
        boolean shining = shine.gain() > 0.004f;
        for (int q = 0; q < this.quads; ++q) {
            byte mat = this.material[q];
            int argb = palette[mat];
            boolean lit = shining && (shine.tail() > 0.0f ? KnifeMaterial.blade(mat) : KnifeMaterial.metallic(mat));
            float nx = this.normal[q * 3];
            float ny = this.normal[q * 3 + 1];
            float nz = this.normal[q * 3 + 2];
            for (int v = 0; v < 4; ++v) {
                int p = q * 12 + v * 3;
                float x = this.pos[p];
                float y = this.pos[p + 1];
                float z = this.pos[p + 2];
                float bright = this.tone[q];
                if (lit) {
                    bright += shine.gain() * 1.5f * this.profile(x, shine);
                }
                out.method_56824(pose, x, y, z).method_39415(KnifeMesh.tint(argb, bright)).method_22913((x + z) * 0.09f, y * 0.09f).method_22922(class_4608.field_21444).method_60803(light).method_60831(pose, nx, ny, nz);
            }
        }
    }

    void emitGlow(class_4587.class_4665 pose, class_4588 out, int rgb, Shine shine) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        float[] alpha = new float[4];
        for (int q = 0; q < this.quads; ++q) {
            if (!KnifeMaterial.blade(this.material[q])) continue;
            float most = 0.0f;
            for (int v = 0; v < 4; ++v) {
                alpha[v] = Math.min(1.0f, shine.gain() * 0.85f * this.profile(this.pos[q * 12 + v * 3], shine));
                most = Math.max(most, alpha[v]);
            }
            if (most < 0.01f) continue;
            float nx = this.normal[q * 3];
            float ny = this.normal[q * 3 + 1];
            float nz = this.normal[q * 3 + 2];
            for (int v = 0; v < 4; ++v) {
                int p = q * 12 + v * 3;
                out.method_56824(pose, this.pos[p], this.pos[p + 1], this.pos[p + 2]).method_1336(r, g, b, Math.round(alpha[v] * 255.0f)).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(0xF000F0).method_60831(pose, nx, ny, nz);
            }
        }
    }

    private float profile(float x, Shine shine) {
        float d = x - (this.shineFrom + (this.shineTo - this.shineFrom) * shine.at());
        if (shine.tail() <= 0.0f) {
            float k = d / 2.6f;
            return (float)Math.exp(-k * k);
        }
        if (d > 0.0f) {
            float k = d / 1.5f;
            return (float)Math.exp(-k * k);
        }
        return (float)Math.exp(d / shine.tail());
    }

    private static int tint(int argb, float k) {
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int b = argb & 0xFF;
        if (k <= 1.0f) {
            r = (int)((float)r * k);
            g = (int)((float)g * k);
            b = (int)((float)b * k);
        } else {
            float w = Math.min(1.0f, k - 1.0f);
            r += (int)((float)(255 - r) * w);
            g += (int)((float)(255 - g) * w);
            b += (int)((float)(255 - b) * w);
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public record Shine(float at, float gain, float tail) {
        public static final Shine NONE = new Shine(0.0f, 0.0f, 0.0f);
    }

    static final class Voxels {
        private static final int[][] FACES = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        private final byte[] cells = new byte[131072];
        private final float[] shade = new float[131072];

        Voxels() {
        }

        void set(int x, int y, int z, int material, float tone) {
            int i = Voxels.index(x, y, z);
            this.cells[i] = (byte)(material + 1);
            this.shade[i] = tone;
        }

        boolean filled(int x, int y, int z) {
            int ix = x + 64;
            int iy = y + 32;
            int iz = z + 8;
            return ix >= 0 && iy >= 0 && iz >= 0 && ix < 128 && iy < 64 && iz < 16 && this.cells[(ix * 64 + iy) * 16 + iz] != 0;
        }

        KnifeMesh mesh(float shineFrom, float shineTo) {
            int capacity = 256;
            float[] pos = new float[capacity * 12];
            float[] normal = new float[capacity * 3];
            byte[] material = new byte[capacity];
            float[] tone = new float[capacity];
            int quads = 0;
            for (int ix = 0; ix < 128; ++ix) {
                for (int iy = 0; iy < 64; ++iy) {
                    for (int iz = 0; iz < 16; ++iz) {
                        int cell = (ix * 64 + iy) * 16 + iz;
                        if (this.cells[cell] == 0) continue;
                        int x = ix - 64;
                        int y = iy - 32;
                        int z = iz - 8;
                        for (int[] f : FACES) {
                            if (this.filled(x + f[0], y + f[1], z + f[2])) continue;
                            if (quads == capacity) {
                                pos = Arrays.copyOf(pos, (capacity *= 2) * 12);
                                normal = Arrays.copyOf(normal, capacity * 3);
                                material = Arrays.copyOf(material, capacity);
                                tone = Arrays.copyOf(tone, capacity);
                            }
                            Voxels.face(pos, quads * 12, x, y, z, f);
                            normal[quads * 3] = f[0];
                            normal[quads * 3 + 1] = f[1];
                            normal[quads * 3 + 2] = f[2];
                            material[quads] = (byte)(this.cells[cell] - 1);
                            tone[quads] = this.shade[cell];
                            ++quads;
                        }
                    }
                }
            }
            return new KnifeMesh(Arrays.copyOf(pos, quads * 12), Arrays.copyOf(normal, quads * 3), Arrays.copyOf(material, quads), Arrays.copyOf(tone, quads), quads, shineFrom, shineTo);
        }

        private static void face(float[] out, int at, int x, int y, int z, int[] f) {
            float x0 = x;
            float x1 = x + 1;
            float y0 = y;
            float y1 = y + 1;
            float z0 = z;
            float z1 = z + 1;
            float[] c = f[0] == 1 ? new float[]{x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1} : (f[0] == -1 ? new float[]{x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0} : (f[1] == 1 ? new float[]{x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0} : (f[1] == -1 ? new float[]{x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1} : (f[2] == 1 ? new float[]{x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1} : new float[]{x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0}))));
            System.arraycopy(c, 0, out, at, 12);
        }

        private static int index(int x, int y, int z) {
            return ((x + 64) * 64 + y + 32) * 16 + z + 8;
        }
    }
}

