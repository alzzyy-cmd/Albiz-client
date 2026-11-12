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

final class GuiBatch {
    private int[] xy = new int[64];
    private float[] uv = new float[64];
    private int[] colors = new int[16];
    private boolean tinted;
    private int count;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    GuiBatch() {
    }

    GuiBatch reset() {
        this.count = 0;
        this.tinted = false;
        this.minY = Integer.MAX_VALUE;
        this.minX = Integer.MAX_VALUE;
        this.maxY = Integer.MIN_VALUE;
        this.maxX = Integer.MIN_VALUE;
        return this;
    }

    void fill(int x1, int y1, int x2, int y2) {
        this.quad(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2), 0.0f, 0.0f, 0.0f, 0.0f);
    }

    void quad(int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2) {
        this.quad(x1, y1, x2, y2, u1, v1, u2, v2, 0);
    }

    void quad(int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        if (this.count * 4 + 4 > this.xy.length) {
            this.xy = Arrays.copyOf(this.xy, this.xy.length * 2);
            this.uv = Arrays.copyOf(this.uv, this.uv.length * 2);
        }
        if (this.count + 1 > this.colors.length) {
            this.colors = Arrays.copyOf(this.colors, this.colors.length * 2);
        }
        int o = this.count * 4;
        this.xy[o] = x1;
        this.xy[o + 1] = y1;
        this.xy[o + 2] = x2;
        this.xy[o + 3] = y2;
        this.uv[o] = u1;
        this.uv[o + 1] = v1;
        this.uv[o + 2] = u2;
        this.uv[o + 3] = v2;
        this.colors[this.count] = color;
        if (color != 0) {
            this.tinted = true;
        }
        ++this.count;
        this.minX = Math.min(this.minX, x1);
        this.minY = Math.min(this.minY, y1);
        this.maxX = Math.max(this.maxX, x2);
        this.maxY = Math.max(this.maxY, y2);
    }

    void submitColored(class_332 context, RenderPipeline pipeline, int color) {
        this.submit(context, pipeline, class_11231.method_70899(), false, color);
    }

    void submitTextured(class_332 context, RenderPipeline pipeline, class_2960 texture, int color) {
        if (this.count == 0) {
            return;
        }
        class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(texture);
        this.submit(context, pipeline, class_11231.method_70900((GpuTextureView)abstractTexture.method_71659(), (class_12137)abstractTexture.method_75484()), true, color);
    }

    private void submit(class_332 context, RenderPipeline pipeline, class_11231 textureSetup, boolean textured, int color) {
        if (this.count == 0) {
            return;
        }
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        class_8030 scissor = context.field_44659.method_70863();
        class_8030 area = new class_8030(this.minX, this.minY, this.maxX - this.minX, this.maxY - this.minY).method_71523((Matrix3x2fc)pose);
        class_8030 bounds = scissor != null ? scissor.method_49701(area) : area;
        int[] tints = null;
        if (this.tinted) {
            tints = Arrays.copyOf(this.colors, this.count);
            for (int i = 0; i < this.count; ++i) {
                if (tints[i] != 0) continue;
                tints[i] = color;
            }
        }
        context.field_59826.method_70919((class_11244)new Element(pipeline, textureSetup, pose, Arrays.copyOf(this.xy, this.count * 4), textured ? Arrays.copyOf(this.uv, this.count * 4) : null, tints, this.count, color, scissor, bounds));
        this.count = 0;
    }

    private static final class Element
    implements class_11244 {
        private final RenderPipeline pipeline;
        private final class_11231 textureSetup;
        private final Matrix3x2f pose;
        private final int[] xy;
        private final float[] uv;
        private final int[] colors;
        private final int quads;
        private final int color;
        private final class_8030 scissorArea;
        private final class_8030 bounds;

        private Element(RenderPipeline pipeline, class_11231 textureSetup, Matrix3x2f pose, int[] xy, float[] uv, int[] colors, int quads, int color, class_8030 scissorArea, class_8030 bounds) {
            this.pipeline = pipeline;
            this.textureSetup = textureSetup;
            this.pose = pose;
            this.xy = xy;
            this.uv = uv;
            this.colors = colors;
            this.quads = quads;
            this.color = color;
            this.scissorArea = scissorArea;
            this.bounds = bounds;
        }

        public void method_70917(class_4588 consumer) {
            for (int i = 0; i < this.quads; ++i) {
                int tint;
                int o = i * 4;
                float x1 = this.xy[o];
                float y1 = this.xy[o + 1];
                float x2 = this.xy[o + 2];
                float y2 = this.xy[o + 3];
                int n = tint = this.colors == null ? this.color : this.colors[i];
                if (this.uv == null) {
                    consumer.method_70815((Matrix3x2fc)this.pose, x1, y1).method_39415(tint);
                    consumer.method_70815((Matrix3x2fc)this.pose, x1, y2).method_39415(tint);
                    consumer.method_70815((Matrix3x2fc)this.pose, x2, y2).method_39415(tint);
                    consumer.method_70815((Matrix3x2fc)this.pose, x2, y1).method_39415(tint);
                    continue;
                }
                float u1 = this.uv[o];
                float v1 = this.uv[o + 1];
                float u2 = this.uv[o + 2];
                float v2 = this.uv[o + 3];
                consumer.method_70815((Matrix3x2fc)this.pose, x1, y1).method_22913(u1, v1).method_39415(tint);
                consumer.method_70815((Matrix3x2fc)this.pose, x1, y2).method_22913(u1, v2).method_39415(tint);
                consumer.method_70815((Matrix3x2fc)this.pose, x2, y2).method_22913(u2, v2).method_39415(tint);
                consumer.method_70815((Matrix3x2fc)this.pose, x2, y1).method_22913(u2, v1).method_39415(tint);
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

        public int[] xy() {
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

        public int color() {
            return this.color;
        }

        public class_8030 comp_4069() {
            return this.scissorArea;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }
    }
}

