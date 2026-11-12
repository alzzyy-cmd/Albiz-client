// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

public final class ModelMesh {
    public final int triangles;
    public final float[] positions;
    public final float[] normals;
    public final float[] along;
    public final int[] colors;
    public final float depth;

    ModelMesh(int triangles, float[] positions, float[] normals, float[] along, int[] colors) {
        this.triangles = triangles;
        float deepest = 1.0E-6f;
        for (int v = 0; v < triangles * 3; ++v) {
            deepest = Math.max(deepest, Math.abs(positions[v * 3 + 2] - 0.5f));
        }
        this.depth = deepest;
        this.positions = positions;
        this.normals = normals;
        this.along = along;
        this.colors = colors;
    }

    public boolean hasColors() {
        for (int color : this.colors) {
            if (color == -1) continue;
            return true;
        }
        return false;
    }

    public static ModelMesh normalize(float[] raw, int[] colors, int triangles) {
        if(triangles<1||triangles>60000||raw.length<triangles*9||colors.length<triangles)throw new IllegalArgumentException("Invalid model geometry or triangle budget");
        for(int i=0;i<triangles*9;i++)if(!Float.isFinite(raw[i]))throw new IllegalArgumentException("Non-finite vertex");
        float[] min = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] max = new float[]{-3.4028235E38f, -3.4028235E38f, -3.4028235E38f};
        for (int i = 0; i < triangles * 9; ++i) {
            int axis = i % 3;
            min[axis] = Math.min(min[axis], raw[i]);
            max[axis] = Math.max(max[axis], raw[i]);
        }
        float[] extent = new float[]{max[0] - min[0], max[1] - min[1], max[2] - min[2]};
        int longAxis = 0;
        for (int a = 1; a < 3; ++a) {
            if (!(extent[a] > extent[longAxis])) continue;
            longAxis = a;
        }
        int wideAxis = -1;
        int thinAxis = -1;
        for (int a = 0; a < 3; ++a) {
            if (a == longAxis) continue;
            if (wideAxis < 0 || extent[a] > extent[wideAxis]) {
                if (wideAxis >= 0) {
                    thinAxis = wideAxis;
                }
                wideAxis = a;
                continue;
            }
            thinAxis = a;
        }
        float length = Math.max(1.0E-6f, extent[longAxis]);
        float lowMin = Float.MAX_VALUE;
        float lowMax = -3.4028235E38f;
        float highMin = Float.MAX_VALUE;
        float highMax = -3.4028235E38f;
        for (int v = 0; v < triangles * 3; ++v) {
            float t = (raw[v * 3 + longAxis] - min[longAxis]) / length;
            float w = raw[v * 3 + wideAxis];
            if (t < 0.25f) {
                lowMin = Math.min(lowMin, w);
                lowMax = Math.max(lowMax, w);
                continue;
            }
            if (!(t > 0.75f)) continue;
            highMin = Math.min(highMin, w);
            highMax = Math.max(highMax, w);
        }
        float lowWidth = lowMax > lowMin ? lowMax - lowMin : 0.0f;
        float highWidth = highMax > highMin ? highMax - highMin : 0.0f;
        boolean headHigh = highWidth >= lowWidth;
        float cx = (min[0] + max[0]) / 2.0f;
        float cy = (min[1] + max[1]) / 2.0f;
        float cz = (min[2] + max[2]) / 2.0f;
        float[] center = new float[]{cx, cy, cz};
        float scale = 1.3f / length;
        float cos = (float)Math.cos(Math.toRadians(-45.0));
        float sin = (float)Math.sin(Math.toRadians(-45.0));
        float[] positions = new float[triangles * 9];
        float[] along = new float[triangles * 3];
        for (int v = 0; v < triangles * 3; ++v) {
            float ly = (raw[v * 3 + longAxis] - center[longAxis]) * scale;
            float lx = (raw[v * 3 + wideAxis] - center[wideAxis]) * scale;
            float lz = (raw[v * 3 + thinAxis] - center[thinAxis]) * scale;
            if (!headHigh) {
                ly = -ly;
                lx = -lx;
            }
            positions[v * 3] = lx * cos - ly * sin + 0.5f;
            positions[v * 3 + 1] = lx * sin + ly * cos + 0.5f;
            positions[v * 3 + 2] = lz + 0.5f;
            along[v] = ly / 1.3f + 0.5f;
        }
        float[] normals = new float[triangles * 3];
        for (int t = 0; t < triangles; ++t) {
            int o = t * 9;
            float ay = positions[o + 4] - positions[o + 1];
            float bz = positions[o + 8] - positions[o + 2];
            float az = positions[o + 5] - positions[o + 2];
            float by = positions[o + 7] - positions[o + 1];
            float nx = ay * bz - az * by;
            float bx = positions[o + 6] - positions[o];
            float ax = positions[o + 3] - positions[o];
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;
            float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1.0E-9f) {
                normals[t * 3 + 2] = 1.0f;
                continue;
            }
            normals[t * 3] = nx / len;
            normals[t * 3 + 1] = ny / len;
            normals[t * 3 + 2] = nz / len;
        }
        return new ModelMesh(triangles, positions, normals, along, colors);
    }
}

