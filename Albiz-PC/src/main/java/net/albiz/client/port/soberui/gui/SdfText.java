// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.albiz.client.port.soberui.gui.GuiBatch;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class SdfText {
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"albiz", (String)"sdf_text")).withVertexShader(class_2960.method_60656((String)"core/position_tex_color")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"sdf_text")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).build();
    private static final class_2960 ATLAS = class_2960.method_60655((String)"albiz", (String)"textures/font/xuong_regular_sdf.png");
    private static final GuiBatch BATCH = new GuiBatch();
    private static final int[] ADVANCES = new int[]{2, 2, 3, 7, 6, 6, 7, 2, 2, 2, 4, 4, 2, 4, 2, 3, 6, 4, 6, 5, 6, 5, 6, 5, 6, 6, 2, 2, 4, 4, 4, 5, 8, 6, 6, 6, 6, 6, 5, 6, 6, 2, 5, 6, 4, 7, 6, 6, 6, 6, 5, 6, 5, 6, 6, 9, 6, 5, 6, 2, 3, 2, 6, 4, 4, 6, 6, 6, 6, 6, 5, 6, 6, 2, 5, 6, 4, 7, 6, 6, 6, 6, 5, 6, 5, 6, 6, 9, 6, 5, 6, 3, 2, 3, 5};

    private SdfText() {
    }

    public static boolean supports(String value) {
        return value.codePoints().allMatch(c -> c >= 32 && c < 127);
    }

    public static int width(String value) {
        int width = 0;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            width += c >= ' ' && c < '\u007f' ? ADVANCES[c - 32] : 6;
        }
        return width;
    }

    public static void draw(class_332 context, String value, int x, int y, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        GuiBatch batch = BATCH.reset();
        int cursor = x;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c < ' ' || c >= '\u007f') {
                cursor += 6;
                continue;
            }
            int index = c - 32;
            if (c != ' ') {
                float u = (float)(index % 16) * 96.0f;
                float v = (float)(index / 16) * 96.0f;
                batch.quad(cursor - 1, y - 5, cursor + 15, y + 11, (u + 0.0f) / 1536.0f, (v + 0.0f) / 576.0f, (u + 96.0f) / 1536.0f, (v + 96.0f) / 576.0f);
            }
            cursor += ADVANCES[index];
        }
        batch.submitTextured(context, PIPELINE, ATLAS, color);
    }
}

