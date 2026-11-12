package net.albiz.client.port.soberui.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.modules.BlockOverlay;


import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_10799;
import net.minecraft.class_12074;
import net.minecraft.class_12249;
import net.minecraft.class_156;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.class_9848;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public final class AuraRenderer {
    private static final RenderPipeline GALAXY_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/aura_galaxy")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/aura_galaxy")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/aura_galaxy")).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1592, VertexFormat.class_5596.field_27379).build();
    private static final RenderPipeline PALM_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/aura_palm")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/aura_palm")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/aura_palm")).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1585, VertexFormat.class_5596.field_27379).build();
    private static final RenderPipeline HOLOGRAM_PIPELINE = AuraRenderer.facePipeline("hologram");
    private static final RenderPipeline OPAL_PIPELINE = AuraRenderer.facePipeline("opal");
    private static final RenderPipeline MOLTEN_PIPELINE = AuraRenderer.facePipeline("molten");
    private static final int[][] FACE_CORNERS = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};
    private static final class_2960 PALM_TEXTURE = class_2960.method_60655((String)"albiz", (String)"textures/aura/palm.png");
    private static final int[][] FACES = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
    private static final int[][] PALM_FACES = new int[][]{{1, 0, 3, 2}, {4, 5, 6, 7}, {0, 4, 7, 3}, {5, 1, 2, 6}, {7, 6, 2, 3}, {0, 1, 5, 4}};
    private static final float[][] UV = new float[][]{{0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}, {0.0f, 0.0f}};
    private static class_276 previewTarget;
    private static Matrix4f previewView;

    private AuraRenderer() {
    }

    private static float[][] corners(class_238 box, double x, double y, double z) {
        class_238 b = box.method_1014(0.003).method_989(x, y, z);
        float x0 = (float)b.field_1323;
        float y0 = (float)b.field_1322;
        float z0 = (float)b.field_1321;
        float x1 = (float)b.field_1320;
        float y1 = (float)b.field_1325;
        float z1 = (float)b.field_1324;
        return new float[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
    }

    private static void drawGalaxy(List<class_238> boxes, double x, double y, double z, float seconds, int rgb) {
        if (!RenderSystem.getDevice().precompilePipeline(GALAXY_PIPELINE).isValid()) {
            return;
        }
        int vertices = boxes.size() * 36;
        try (class_9799 bytes = class_9799.method_72201((int)(vertices * class_290.field_1592.getVertexSize()));){
            class_287 builder = new class_287(bytes, VertexFormat.class_5596.field_27379, class_290.field_1592);
            for (class_238 box : boxes) {
                float[][] c = AuraRenderer.corners(box, x, y, z);
                for (int[] f : FACES) {
                    for (int i : new int[]{f[0], f[1], f[2], f[0], f[2], f[3]}) {
                        builder.method_22912(c[i][0], c[i][1], c[i][2]);
                    }
                }
            }
            AuraRenderer.draw(builder, class_290.field_1592, GALAXY_PIPELINE, "Sober galaxy aura", vertices, seconds, rgb, null);
        }
    }

    private static void drawPalms(List<class_238> boxes, double x, double y, double z, float seconds, int rgb) {
        if (!RenderSystem.getDevice().precompilePipeline(PALM_PIPELINE).isValid()) {
            return;
        }
        GpuTextureView texture = class_310.method_1551().method_1531().method_4619(PALM_TEXTURE).method_71659();
        int vertices = boxes.size() * 36;
        try (class_9799 bytes = class_9799.method_72201((int)(vertices * class_290.field_1585.getVertexSize()));){
            class_287 builder = new class_287(bytes, VertexFormat.class_5596.field_27379, class_290.field_1585);
            for (class_238 box : boxes) {
                float[][] c = AuraRenderer.corners(box, x, y, z);
                for (int[] f : PALM_FACES) {
                    for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                        builder.method_22912(c[f[k]][0], c[f[k]][1], c[f[k]][2]).method_22913(UV[k][0], UV[k][1]);
                    }
                }
            }
            AuraRenderer.draw(builder, class_290.field_1585, PALM_PIPELINE, "Sober palm aura", vertices, seconds, rgb, texture);
        }
    }

    private static RenderPipeline facePipeline(String name) {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"albiz", (String)("pipeline/aura_" + name))).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/aura_face")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)("core/aura_" + name))).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).build();
    }

    private static void drawFaces(List<class_238> boxes, double x, double y, double z, float seconds, int rgb, RenderPipeline pipeline, String label) {
        if (boxes.isEmpty() || !RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            return;
        }
        class_238 bounds = boxes.get(0);
        for (class_238 box : boxes) {
            bounds = bounds.method_991(box);
        }
        double[] low = new double[]{bounds.field_1323, bounds.field_1322, bounds.field_1321};
        double[] size = new double[]{Math.max(1.0E-4, bounds.method_17939()), Math.max(1.0E-4, bounds.method_17940()), Math.max(1.0E-4, bounds.method_17941())};
        int vertices = boxes.size() * 36;
        try (class_9799 bytes = class_9799.method_72201((int)(vertices * class_290.field_1575.getVertexSize()));){
            class_287 builder = new class_287(bytes, VertexFormat.class_5596.field_27379, class_290.field_1575);
            for (class_238 box : boxes) {
                class_238 b = box.method_1014(0.003);
                double[] min = new double[]{b.field_1323, b.field_1322, b.field_1321};
                double[] max = new double[]{b.field_1320, b.field_1325, b.field_1324};
                for (int face = 0; face < 6; ++face) {
                    int axis = face < 2 ? 1 : (face < 4 ? 2 : 0);
                    int ua = axis == 0 ? 2 : 0;
                    int va = axis == 1 ? 2 : 1;
                    double[][] corner = new double[4][3];
                    for (int k = 0; k < 4; ++k) {
                        corner[k][axis] = face % 2 == 1 ? max[axis] : min[axis];
                        corner[k][ua] = FACE_CORNERS[k][0] == 0 ? min[ua] : max[ua];
                        corner[k][va] = FACE_CORNERS[k][1] == 0 ? min[va] : max[va];
                    }
                    for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                        double[] p = corner[k];
                        builder.method_22912((float)(p[0] + x), (float)(p[1] + y), (float)(p[2] + z)).method_22913((float)FACE_CORNERS[k][0], (float)FACE_CORNERS[k][1]).method_1336(AuraRenderer.unit((p[0] - low[0]) / size[0]), AuraRenderer.unit((p[1] - low[1]) / size[1]), AuraRenderer.unit((p[2] - low[2]) / size[2]), face * 51);
                    }
                }
            }
            AuraRenderer.draw(builder, class_290.field_1575, pipeline, label, vertices, seconds, rgb, null);
        }
    }

    private static int unit(double value) {
        return (int)Math.round(Math.max(0.0, Math.min(1.0, value)) * 255.0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void preview(class_276 target, Matrix4f view, BlockOverlay.Style style, float seconds, int rgb) {
        previewTarget = target;
        previewView = view;
        try {
            List<class_238> boxes = List.of(new class_238(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
            switch (style) {
                case Galaxy: {
                    AuraRenderer.drawGalaxy(boxes, 0.0, 0.0, 0.0, seconds, rgb);
                    return;
                }
                case Palm: {
                    AuraRenderer.drawPalms(boxes, 0.0, 0.0, 0.0, seconds, rgb);
                    return;
                }
                case Hologram: {
                    AuraRenderer.drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, HOLOGRAM_PIPELINE, "Sober hologram preview");
                    return;
                }
                case Opal: {
                    AuraRenderer.drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, OPAL_PIPELINE, "Sober opal preview");
                    return;
                }
                case Molten: {
                    AuraRenderer.drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, MOLTEN_PIPELINE, "Sober molten preview");
                    return;
                }
            }
            return;
        }
        finally {
            previewTarget = null;
            previewView = null;
        }
    }

    private static void draw(class_287 builder, VertexFormat format, RenderPipeline pipeline, String label, int vertices, float seconds, int rgb, @Nullable GpuTextureView texture) {
        try (class_9801 mesh = builder.method_60800();){
            GpuBuffer buffer = format.uploadImmediateVertexBuffer(mesh.method_60818());
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)(previewView != null ? previewView : RenderSystem.getModelViewMatrix()), (Vector4fc)new Vector4f(seconds, (float)(rgb >> 16 & 0xFF) / 255.0f, (float)(rgb >> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
            class_276 target = previewTarget != null ? previewTarget : class_310.method_1551().method_1522();
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> label, target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());){
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("DynamicTransforms", transforms);
                if (texture != null) {
                    pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR));
                }
                pass.setVertexBuffer(0, buffer);
                pass.draw(0, vertices);
            }
        }
    }

    private static void drawBox(class_4587 matrices, class_4588 consumer, class_238 box, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x0 = (float)box.field_1323;
        float y0 = (float)box.field_1322;
        float z0 = (float)box.field_1321;
        float x1 = (float)box.field_1320;
        float y1 = (float)box.field_1325;
        float z1 = (float)box.field_1324;
        AuraRenderer.quad(entry, consumer, color, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        AuraRenderer.quad(entry, consumer, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        AuraRenderer.quad(entry, consumer, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        AuraRenderer.quad(entry, consumer, color, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        AuraRenderer.quad(entry, consumer, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        AuraRenderer.quad(entry, consumer, color, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void quad(class_4587.class_4665 entry, class_4588 consumer, int color, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz) {
        consumer.method_56824(entry, ax, ay, az).method_39415(color);
        consumer.method_56824(entry, bx, by, bz).method_39415(color);
        consumer.method_56824(entry, cx, cy, cz).method_39415(color);
        consumer.method_56824(entry, dx, dy, dz).method_39415(color);
    }

    public static int hsvToRgb(float h, float s, float v) {
        float r = AuraRenderer.channel(5, h, s, v);
        float g = AuraRenderer.channel(3, h, s, v);
        float b = AuraRenderer.channel(1, h, s, v);
        return Math.round(r * 255.0f) << 16 | Math.round(g * 255.0f) << 8 | Math.round(b * 255.0f);
    }

    private static float channel(int n, float h, float s, float v) {
        float k = ((float)n + h * 6.0f) % 6.0f;
        return v - v * s * Math.max(0.0f, Math.min(Math.min(k, 4.0f - k), 1.0f));
    }
}

