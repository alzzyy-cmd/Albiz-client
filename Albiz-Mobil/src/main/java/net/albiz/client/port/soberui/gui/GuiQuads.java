// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import net.minecraft.class_1044;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

final class GuiQuads {
    private float[] xy = new float[128];
    private float[] uv = new float[128];
    private int[] colors = new int[64];
    private int count;
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    GuiQuads() {
    }

    GuiQuads reset() {
        this.count = 0;
        this.minY = Float.MAX_VALUE;
        this.minX = Float.MAX_VALUE;
        this.maxY = -3.4028235E38f;
        this.maxX = -3.4028235E38f;
        return this;
    }

    void quad(float[] corners, float[] coords, int[] tints) {
        int[] nArray;
        if (this.count * 8 + 8 > this.xy.length) {
            this.xy = Arrays.copyOf(this.xy, this.xy.length * 2);
            this.uv = Arrays.copyOf(this.uv, this.uv.length * 2);
            this.colors = Arrays.copyOf(this.colors, this.colors.length * 2);
        }
        float area = 0.0f;
        for (int k = 0; k < 4; ++k) {
            int n = (k + 1) % 4;
            area += corners[k * 2] * corners[n * 2 + 1] - corners[n * 2] * corners[k * 2 + 1];
        }
        if (area > 0.0f) {
            int[] nArray2 = new int[4];
            nArray2[0] = 0;
            nArray2[1] = 3;
            nArray2[2] = 2;
            nArray = nArray2;
            nArray2[3] = 1;
        } else {
            int[] nArray3 = new int[4];
            nArray3[0] = 0;
            nArray3[1] = 1;
            nArray3[2] = 2;
            nArray = nArray3;
            nArray3[3] = 3;
        }
        int[] order = nArray;
        for (int k = 0; k < 4; ++k) {
            int from = order[k];
            int o = this.count * 8 + k * 2;
            this.xy[o] = corners[from * 2];
            this.xy[o + 1] = corners[from * 2 + 1];
            this.uv[o] = coords[from * 2];
            this.uv[o + 1] = coords[from * 2 + 1];
            this.colors[this.count * 4 + k] = tints[from];
            this.minX = Math.min(this.minX, this.xy[o]);
            this.minY = Math.min(this.minY, this.xy[o + 1]);
            this.maxX = Math.max(this.maxX, this.xy[o]);
            this.maxY = Math.max(this.maxY, this.xy[o + 1]);
        }
        ++this.count;
    }

    void submit(class_332 context, RenderPipeline pipeline, class_2960 texture) {
        if (this.count == 0) {
            return;
        }
        class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(texture);
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        class_8030 scissor = context.field_44659.method_70863();
        int left = (int)Math.floor(this.minX);
        int top = (int)Math.floor(this.minY);
        class_8030 area = new class_8030(left, top, Math.max(1, (int)Math.ceil(this.maxX) - left), Math.max(1, (int)Math.ceil(this.maxY) - top)).method_71523((Matrix3x2fc)pose);
        class_8030 bounds = scissor != null ? scissor.method_49701(area) : area;
        context.field_59826.method_70919((class_11244)new Element(pipeline, class_11231.method_70900((GpuTextureView)abstractTexture.method_71659(), (class_12137)abstractTexture.method_75484()), pose, Arrays.copyOf(this.xy, this.count * 8), Arrays.copyOf(this.uv, this.count * 8), Arrays.copyOf(this.colors, this.count * 4), this.count, scissor, bounds));
        this.count = 0;
    }

    private static final class Element
    implements class_11244 {
        private final RenderPipeline pipeline;
        private final class_11231 textureSetup;
        private final Matrix3x2f pose;
        private final float[] xy;
        private final float[] uv;
        private final int[] colors;
        private final int quads;
        private final class_8030 scissorArea;
        private final class_8030 bounds;

        private Element(RenderPipeline pipeline, class_11231 textureSetup, Matrix3x2f pose, float[] xy, float[] uv, int[] colors, int quads, class_8030 scissorArea, class_8030 bounds) {
            this.pipeline = pipeline;
            this.textureSetup = textureSetup;
            this.pose = pose;
            this.xy = xy;
            this.uv = uv;
            this.colors = colors;
            this.quads = quads;
            this.scissorArea = scissorArea;
            this.bounds = bounds;
        }

        public void method_70917(class_4588 consumer) {
            for (int i = 0; i < this.quads; ++i) {
                for (int k = 0; k < 4; ++k) {
                    int o = i * 8 + k * 2;
                    consumer.method_70815((Matrix3x2fc)this.pose, this.xy[o], this.xy[o + 1]).method_22913(this.uv[o], this.uv[o + 1]).method_39415(this.colors[i * 4 + k]);
                }
            }
        }




        public RenderPipeline comp_4055() {
            return this.pipeline;
        }

        public class_11231 comp_4056() {
            return this.textureSetup;
        }

        public Matrix3x2f pose() {
            return this.pose;
        }

        public float[] xy() {
            return this.xy;
        }

        public float[] uv() {
            return this.uv;
        }

        public int[] colors() {
            return this.colors;
        }

        public int quads() {
            return this.quads;
        }

        public class_8030 comp_4069() {
            return this.scissorArea;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }
    }
}

