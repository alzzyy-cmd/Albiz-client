// Adapted from SoberClient (Violet), GPL-3.0-only.
package net.albiz.client.port.sober.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.albiz.client.mixin.AccessorGameRenderer;

import net.albiz.client.port.sober.sky.SkySettings;
import net.albiz.client.port.sober.sky.SkyType;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10799;
import net.minecraft.class_1294;
import net.minecraft.class_156;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_6367;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public final class SkyRenderer {
    private static final float TIME_WRAP = 7200.0f;
    private static final int CUBE_VERTICES = 36;
    private static final Map<SkyType, RenderPipeline> PIPELINES = new EnumMap<SkyType, RenderPipeline>(SkyType.class);
    private static final Map<SkyType, Boolean> REPORTED_INVALID = new EnumMap<SkyType, Boolean>(SkyType.class);
    private static final long REFRESH_NANOS = 33333333L;
    private static final RenderPipeline UPSCALE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/sky_upscale")).withVertexShader("core/screenquad").withFragmentShader("core/blit_screen").withSampler("InSampler").withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).build();
    private static @Nullable GpuBuffer cube;
    private static float time;
    private static long lastNanos;
    private static boolean drawnThisFrame;
    private static @Nullable class_6367 lowRes;
    private static final Matrix4f lastView;
    private static float lastFov;
    private static double lastX;
    private static double lastY;
    private static double lastZ;
    private static long lastDrawn;
    private static @Nullable SkyType lastType;

    private SkyRenderer() {
    }

    private static RenderPipeline pipeline(SkyType type) {
        return PIPELINES.computeIfAbsent(type, t -> {
            RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"albiz", (String)("pipeline/sky_" + t.id()))).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/sober_sky")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)("core/sober_sky_" + t.id()))).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1592, VertexFormat.class_5596.field_27379);
            if (t.texture() != null) {
                builder.withSampler("Sampler0");
            }
            return builder.build();
        });
    }

    public static boolean isCustomSkySelected() {
        class_638 world = class_310.method_1551().field_1687;
        return SkySettings.sky.isCustom() && world != null && world.method_27983() == class_1937.field_25179;
    }

    public static boolean drewThisFrame() {
        return drawnThisFrame;
    }

    public static float @Nullable [] fogColor() {
        if (!SkySettings.matchFog || !SkyRenderer.isCustomSkySelected()) {
            return null;
        }
        float[] fog = SkySettings.sky.fogColor();
        if (fog == null) {
            return null;
        }
        float brightness = (float)SkySettings.brightness / 100.0f;
        return new float[]{Math.min(fog[0] * brightness, 1.0f), Math.min(fog[1] * brightness, 1.0f), Math.min(fog[2] * brightness, 1.0f)};
    }

    public static boolean renderSky() {
        int quality;
        drawnThisFrame = false;
        if (!isCustomSkySelected()) {
            return false;
        }
        SkyRenderer.advanceClock();
        SkyType type = SkySettings.sky;
        if (!type.isCustom()) {
            return false;
        }
        RenderPipeline pipeline = SkyRenderer.pipeline(type);
        if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            if (REPORTED_INVALID.put(type, Boolean.TRUE) == null) {
                org.slf4j.LoggerFactory.getLogger("Albiz-Sky").error("Sky shader '{}' failed to compile, falling back to the vanilla sky", (Object)type.id());
            }
            return false;
        }
        class_276 target = class_310.method_1551().method_1522();
        GpuTextureView texture = null;
        if (type.texture() != null) {
            try {
                texture = class_310.method_1551().method_1531().method_4619(type.texture()).method_71659();
            }
            catch (RuntimeException e) {
                if (REPORTED_INVALID.put(type, Boolean.TRUE) == null) {
                    org.slf4j.LoggerFactory.getLogger("Albiz-Sky").error("Sky texture '{}' could not be loaded", (Object)type.texture(), (Object)e);
                }
                return false;
            }
            if (texture == null) {
                return false;
            }
        }
        if ((quality = SkySettings.quality) >= 100 || !RenderSystem.getDevice().precompilePipeline(UPSCALE).isValid()) {
            SkyRenderer.draw(pipeline, texture, target.method_71639(), target.method_71640());
        } else {
            boolean resized;
            int width = Math.max(1, target.field_1482 * quality / 100);
            int height = Math.max(1, target.field_1481 * quality / 100);
            boolean bl = resized = lowRes == null || SkyRenderer.lowRes.field_1482 != width || SkyRenderer.lowRes.field_1481 != height;
            if (lowRes == null) {
                lowRes = new class_6367("Albiz sky", width, height, false);
            } else if (resized) {
                lowRes.method_1234(width, height);
            }
            long now = class_156.method_648();
            boolean steady = SkyRenderer.steady();
            if (resized || type != lastType || !steady || now - lastDrawn >= 33333333L) {
                SkyRenderer.draw(pipeline, texture, lowRes.method_71639(), null);
                lastDrawn = now;
                lastType = type;
            }
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sober sky upscale", target.method_71639(), OptionalInt.empty());){
                pass.setPipeline(UPSCALE);
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.bindTexture("InSampler", lowRes.method_71639(), RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false));
                pass.draw(0, 3);
            }
        }
        drawnThisFrame = true;
        return true;
    }

    private static void draw(RenderPipeline pipeline, @Nullable GpuTextureView texture, GpuTextureView color, @Nullable GpuTextureView depth) {
        int tint = SkySettings.auroraColor;
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(time, (float)SkySettings.brightness / 100.0f, (float)SkySettings.auroraPalette, 0.0f), (Vector3fc)new Vector3f((float)(tint >> 16 & 0xFF) / 255.0f, (float)(tint >> 8 & 0xFF) / 255.0f, (float)(tint & 0xFF) / 255.0f), (Matrix4fc)new Matrix4f());
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass pass = depth == null ? encoder.createRenderPass(() -> "Albiz sky", color, OptionalInt.empty()) : encoder.createRenderPass(() -> "Albiz sky", color, OptionalInt.empty(), depth, OptionalDouble.empty());){
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            if (texture != null) {
                pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            }
            pass.setVertexBuffer(0, SkyRenderer.cube());
            pass.draw(0, 36);
        }
    }

    private static boolean steady() {
        class_310 mc = class_310.method_1551();
        class_4184 camera = mc.field_1773.method_19418();
        float tickProgress = mc.method_61966().method_60637(true);
        float fov = ((AccessorGameRenderer)mc.field_1773).albiz$getFov(camera, tickProgress, true);
        class_243 pos = camera.method_71156();
        Matrix4f view = RenderSystem.getModelViewMatrix();
        class_746 player = mc.field_1724;
        boolean calm = player == null || player.field_6235 <= 0 && player.field_44911 <= 0.0f && !player.method_6059(class_1294.field_5916) && ((Boolean)mc.field_1690.method_42448().method_41753() == false || !(player.method_74192().method_74204(tickProgress) > 1.0E-4f));
        boolean same = calm && fov == lastFov && view.equals((Matrix4fc)lastView, 1.0E-6f) && pos.field_1352 == lastX && pos.field_1351 == lastY && pos.field_1350 == lastZ;
        lastFov = fov;
        lastView.set((Matrix4fc)view);
        lastX = pos.field_1352;
        lastY = pos.field_1351;
        lastZ = pos.field_1350;
        return same;
    }

    public static boolean renderPreview(SkyType type, GpuTextureView target, Matrix4f view, float seconds, float brightness, int palette, int color) {
        if (type == null || !type.isCustom()) {
            return false;
        }
        RenderPipeline pipeline = SkyRenderer.pipeline(type);
        if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            return false;
        }
        GpuTextureView texture = null;
        if (type.texture() != null && (texture = class_310.method_1551().method_1531().method_4619(type.texture()).method_71659()) == null) {
            return false;
        }
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)view, (Vector4fc)new Vector4f(seconds, brightness, (float)palette, 0.0f), (Vector3fc)new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f), (Matrix4fc)new Matrix4f());
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sober sky preview", target, OptionalInt.of(-16777216));){
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            if (texture != null) {
                pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            }
            pass.setVertexBuffer(0, SkyRenderer.cube());
            pass.draw(0, 36);
        }
        return true;
    }

    private static void advanceClock() {
        long now = class_156.method_648();
        if (lastNanos >= 0L) {
            float dt = Math.min((float)(now - lastNanos) / 1.0E9f, 0.25f);
            time = (time + dt * (float)SkySettings.speed / 100.0f) % 7200.0f;
        }
        lastNanos = now;
    }

    private static GpuBuffer cube() {
        if (cube == null || cube.isClosed()) {
            float s = 10.0f;
            float[][] corners = new float[][]{{-s, -s, -s}, {s, -s, -s}, {s, s, -s}, {-s, s, -s}, {-s, -s, s}, {s, -s, s}, {s, s, s}, {-s, s, s}};
            int[][] faces = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
            try (class_9799 bytes = class_9799.method_72201((int)(36 * class_290.field_1592.getVertexSize()));){
                class_287 builder = new class_287(bytes, VertexFormat.class_5596.field_27379, class_290.field_1592);
                for (int[] face : faces) {
                    for (int index : new int[]{face[0], face[1], face[2], face[0], face[2], face[3]}) {
                        float[] c = corners[index];
                        builder.method_22912(c[0], c[1], c[2]);
                    }
                }
                try (class_9801 mesh = builder.method_60800();){
                    cube = RenderSystem.getDevice().createBuffer(() -> "Sober sky cube", 32, mesh.method_60818());
                }
            }
        }
        return cube;
    }

    static {
        time = 20.0f;
        lastNanos = -1L;
        lastView = new Matrix4f();
        lastFov = -1.0f;
    }
}

