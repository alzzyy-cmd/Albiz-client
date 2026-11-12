// Adapted Sober sprite contour stitching; recovered algorithm, no decompiler stubs.
package net.albiz.client.port.soberui.model;
import java.util.*;
    final class SpriteSegments {
        private float[] data = new float[4096];
        private int count;

        SpriteSegments() {
        }

        void add(float x1, float y1, float x2, float y2) {
            if (this.count * 4 + 4 > this.data.length) {
                this.data = Arrays.copyOf(this.data, this.data.length * 2);
            }
            int o = this.count * 4;
            this.data[o] = x1;
            this.data[o + 1] = y1;
            this.data[o + 2] = x2;
            this.data[o + 3] = y2;
            ++this.count;
        }

        private static long key(float x, float y) {
            return (long)Float.floatToIntBits(x) << 32 | (long)Float.floatToIntBits(y) & 0xFFFFFFFFL;
        }

        private void link(Map<Long,int[]> ends,long key,int segment){
            int[] previous=ends.get(key);
            if(previous==null){ends.put(key,new int[]{segment});return;}
            int[] extended=Arrays.copyOf(previous,previous.length+1);extended[previous.length]=segment;ends.put(key,extended);
        }

        List<float[]> chains() {
            HashMap<Long, int[]> ends = new HashMap<Long, int[]>();
            for (int s = 0; s < this.count; ++s) {
                this.link(ends, SpriteSegments.key(this.data[s * 4], this.data[s * 4 + 1]), s);
                this.link(ends, SpriteSegments.key(this.data[s * 4 + 2], this.data[s * 4 + 3]), s);
            }
            boolean[] used = new boolean[this.count];
            ArrayList<float[]> chains = new ArrayList<float[]>();
            for (int s = 0; s < this.count; ++s) {
                int i;
                if (used[s]) continue;
                used[s] = true;
                FloatBuffer forward = this.walk(ends, used, s, this.data[s * 4 + 2], this.data[s * 4 + 3]);
                FloatBuffer backward = this.walk(ends, used, s, this.data[s * 4], this.data[s * 4 + 1]);
                float[] chain = new float[(backward.size + forward.size) * 2 + 4];
                int o = 0;
                for (i = backward.size - 1; i >= 0; --i) {
                    chain[o++] = backward.values[i * 2];
                    chain[o++] = backward.values[i * 2 + 1];
                }
                chain[o++] = this.data[s * 4];
                chain[o++] = this.data[s * 4 + 1];
                chain[o++] = this.data[s * 4 + 2];
                chain[o++] = this.data[s * 4 + 3];
                for (i = 0; i < forward.size; ++i) {
                    chain[o++] = forward.values[i * 2];
                    chain[o++] = forward.values[i * 2 + 1];
                }
                chains.add(chain);
            }
            return chains;
        }

        private FloatBuffer walk(Map<Long, int[]> ends, boolean[] used, int start, float x, float y) {
            FloatBuffer points = new FloatBuffer();
            int current = start;
            while (true) {
                int[] slots = ends.get(SpriteSegments.key(x, y));
                int next = -1;
                if (slots != null) {
                    for (int candidate : slots) {
                        if (candidate < 0 || candidate == current || used[candidate]) continue;
                        next = candidate;
                        break;
                    }
                }
                if (next < 0) {
                    return points;
                }
                used[next] = true;
                int o = next * 4;
                boolean fromStart = this.data[o] == x && this.data[o + 1] == y;
                x = fromStart ? this.data[o + 2] : this.data[o];
                y = fromStart ? this.data[o + 3] : this.data[o + 1];
                points.add(x, y);
                current = next;
            }
        }
        private static final class FloatBuffer {
        private float[] values = new float[64];
        private int size;

        private FloatBuffer() {
        }

        void add(float x, float y) {
            if (this.size * 2 + 2 > this.values.length) {
                this.values = Arrays.copyOf(this.values, this.values.length * 2);
            }
            this.values[this.size * 2] = x;
            this.values[this.size * 2 + 1] = y;
            ++this.size;
        }
    }
}
