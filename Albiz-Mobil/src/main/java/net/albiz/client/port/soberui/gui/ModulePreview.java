// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.GuiBatch;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.modules.BlockOverlay;
import net.albiz.client.port.soberui.modules.CustomAccessories;
import net.albiz.client.port.soberui.modules.CustomGlint;
import net.albiz.client.port.soberui.modules.Sky;
import net.albiz.client.port.soberui.sky.AuraRenderer;
import net.albiz.client.port.soberui.sky.SkyRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10366;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11286;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_490;
import net.minecraft.class_6367;
import net.minecraft.class_9334;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class ModulePreview {
    private static final RenderPipeline ROUNDED = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56864}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/rounded_texture")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/rounded_texture")).build();
    private static final RenderPipeline BLOCK = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/preview_block")).withVertexShader(class_2960.method_60656((String)"core/position_tex_color")).withFragmentShader(class_2960.method_60656((String)"core/position_tex_color")).withSampler("Sampler0").withDepthWrite(true).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).build();
    private static final class_2960 STONE = class_2960.method_60656((String)"textures/block/stone.png");
    private static final class_11286 SKY_PROJECTION = new class_11286("Sober sky preview");
    private static final class_11286 BLOCK_PROJECTION = new class_11286("Sober overlay preview");
    private static final Map<String, Target> TARGETS = new HashMap<String, Target>();
    private static final GuiBatch BATCH = new GuiBatch();
    private static final float BLOCK_FOV = (float)Math.toRadians(38.0);
    private static final Vector3f EYE = new Vector3f(2.15f, 1.55f, 2.45f);
    private static class_1799 sword;
    private static boolean broken;

    private ModulePreview() {
    }

    public static boolean supports(Module module) {
        if (module instanceof Sky || module instanceof BlockOverlay || module instanceof CustomGlint) {
            return true;
        }
        return module instanceof CustomAccessories && class_310.method_1551().field_1724 != null;
    }

    public static String subtitle(Module module) {
        if (module instanceof Sky) {
            Sky sky = (Sky)module;
            return sky.previewName();
        }
        if (module instanceof BlockOverlay) {
            BlockOverlay overlay = (BlockOverlay)module;
            return overlay.style.get().title;
        }
        if (module instanceof CustomGlint) {
            CustomGlint glint = (CustomGlint)module;
            return String.valueOf((Object)glint.glint.get());
        }
        if (module instanceof CustomAccessories) {
            return "Wings and crown";
        }
        return "";
    }

    private static int alpha(int color, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;
    }

    private static float seconds() {
        return (float)(System.nanoTime() / 1000000L % 3600000L) / 1000.0f;
    }

    public static void draw(class_332 context, Module module, float x, float y, float w, float h, float opacity) {
        if (opacity <= 0.01f || w < 8.0f || h < 8.0f) {
            return;
        }
        int radius = Math.max(4, Math.round(Math.min(w, h) * 0.08f));
        int accent = 0xFF000000 | Theme.accent();
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        int iw = Math.round(w);
        int ih = Math.round(h);
        Smooth.rect(context, 0, 0, iw, ih, radius, ModulePreview.alpha(920086, 0.92f * opacity));
        try {
            if (module instanceof Sky) {
                Sky sky = (Sky)module;
                ModulePreview.sky(context, sky, iw, ih, radius, opacity);
            } else if (module instanceof BlockOverlay) {
                BlockOverlay overlay = (BlockOverlay)module;
                ModulePreview.overlay(context, overlay, iw, ih, radius, opacity, accent);
            } else if (module instanceof CustomGlint) {
                ModulePreview.glint(context, iw, ih, opacity, accent);
            } else if (module instanceof CustomAccessories) {
                ModulePreview.accessories(context, x, y, iw, ih, opacity, accent);
            }
        }
        catch (RuntimeException e) {
            if (!broken) {
                SoberClient.LOG.warn("Module preview failed", (Throwable)e);
            }
            broken = true;
        }
        Smooth.outline(context, 0, 0, iw, ih, radius, ModulePreview.alpha(0xFFFFFF, 0.14f * opacity));
        matrices.popMatrix();
    }

    private static Target target(String name) {
        return TARGETS.computeIfAbsent(name, Target::new);
    }

    private static int[] pixels(class_332 context, int w, int h) {
        float ppu = Math.max(1.0f, Smooth.pixelsPerUnit(context));
        return new int[]{Math.max(8, Math.min(1024, Math.round((float)w * ppu))), Math.max(8, Math.min(1024, Math.round((float)h * ppu))), Math.round(ppu)};
    }

    private static void show(class_332 context, Target target, int w, int h, int radius, float opacity, float ppu) {
        float r = (float)Math.floor((float)radius * ppu);
        GuiBatch batch = BATCH.reset();
        batch.quad(0, 0, w, h, r * 4.0f + 1.5f, 5.5f, r * 4.0f + 2.5f, 6.5f);
        batch.submitTextured(context, ROUNDED, target.id, ModulePreview.alpha(0xFFFFFF, opacity));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void sky(class_332 context, Sky sky, int w, int h, int radius, float opacity) {
        if (!RenderSystem.getDevice().precompilePipeline(ROUNDED).isValid()) {
            return;
        }
        int[] px = ModulePreview.pixels(context, w, h);
        Target target = ModulePreview.target("sky");
        target.ensure(px[0], px[1], false);
        float time = ModulePreview.seconds() * sky.previewSpeed();
        Matrix4f view = new Matrix4f().rotateX((float)Math.toRadians(-14.0)).rotateY(time * 0.06f);
        Matrix4f projection = new Matrix4f().perspective((float)Math.toRadians(75.0), (float)px[0] / (float)px[1], 0.05f, 100.0f);
        RenderSystem.backupProjectionMatrix();
        try {
            RenderSystem.setProjectionMatrix((GpuBufferSlice)SKY_PROJECTION.method_71123(projection), (class_10366)class_10366.field_54953);
            if (!SkyRenderer.renderPreview(sky.previewType(), target.framebuffer.method_71639(), view, time % 7200.0f, sky.previewBrightness(), sky.previewPalette(), sky.previewAuroraColor())) {
                RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.framebuffer.method_30277(), -14998464);
            }
        }
        finally {
            RenderSystem.restoreProjectionMatrix();
        }
        ModulePreview.show(context, target, w, h, radius, opacity, Smooth.pixelsPerUnit(context));
    }

    private static Matrix4f blockView() {
        return new Matrix4f().lookAt((Vector3fc)EYE, (Vector3fc)new Vector3f(0.0f, -0.05f, 0.0f), (Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f));
    }

    private static Matrix4f blockProjection(float aspect) {
        return new Matrix4f().perspective(BLOCK_FOV, aspect, 0.05f, 100.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void overlay(class_332 context, BlockOverlay overlay, int w, int h, int radius, float opacity, int accent) {
        if (!RenderSystem.getDevice().precompilePipeline(ROUNDED).isValid() || !RenderSystem.getDevice().precompilePipeline(BLOCK).isValid()) {
            return;
        }
        int[] px = ModulePreview.pixels(context, w, h);
        Target target = ModulePreview.target("overlay");
        target.ensure(px[0], px[1], true);
        float time = ModulePreview.seconds();
        int rgb = overlay.color(time);
        Matrix4f view = ModulePreview.blockView();
        Matrix4f projection = ModulePreview.blockProjection((float)px[0] / (float)px[1]);
        RenderSystem.backupProjectionMatrix();
        try {
            RenderSystem.setProjectionMatrix((GpuBufferSlice)BLOCK_PROJECTION.method_71123(projection), (class_10366)class_10366.field_54953);
            ModulePreview.stone(target, view);
            BlockOverlay.Style style = overlay.style.get();
            if (overlay.overlay.get().booleanValue() && style != BlockOverlay.Style.Classic && style != BlockOverlay.Style.Target) {
                AuraRenderer.preview((class_276)target.framebuffer, view, style, time, rgb);
            }
        }
        finally {
            RenderSystem.restoreProjectionMatrix();
        }
        ModulePreview.show(context, target, w, h, radius, opacity, Smooth.pixelsPerUnit(context));
        if (!overlay.overlay.get().booleanValue()) {
            return;
        }
        Matrix4f mvp = new Matrix4f((Matrix4fc)projection).mul((Matrix4fc)view);
        BlockOverlay.Style style = overlay.style.get();
        if (style == BlockOverlay.Style.Target) {
            ModulePreview.target2d(context, mvp, w, h, time, rgb, accent, opacity);
        } else {
            ModulePreview.classic2d(context, mvp, w, h, rgb, style == BlockOverlay.Style.Classic ? (float)overlay.fill.get().intValue() / 100.0f : 0.0f, opacity, overlay.pulse.get(), time);
        }
    }

    private static void stone(Target target, Matrix4f view) {
        GpuTextureView stone = class_310.method_1551().method_1531().method_4619(STONE).method_71659();
        float[][][] faces = new float[][][]{new float[][]{{-0.5f, 0.5f, -0.5f}, {-0.5f, 0.5f, 0.5f}, {0.5f, 0.5f, 0.5f}, {0.5f, 0.5f, -0.5f}}, new float[][]{{-0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f}}, new float[][]{{0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f}, {0.5f, 0.5f, 0.5f}}};
        int[] shade = new int[]{-1, -3355444, -6645094};
        float[][] uv = new float[][]{{0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}, {0.0f, 0.0f}};
        int vertices = faces.length * 6;
        try (class_9799 bytes = class_9799.method_72201((int)(vertices * class_290.field_1575.getVertexSize()));){
            class_287 builder = new class_287(bytes, VertexFormat.class_5596.field_27379, class_290.field_1575);
            for (int f = 0; f < faces.length; ++f) {
                for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                    float[] p = faces[f][k];
                    builder.method_22912(p[0], p[1], p[2]).method_22913(uv[k][0], uv[k][1]).method_39415(shade[f]);
                }
            }
            try (class_9801 mesh = builder.method_60800();){
                GpuBuffer buffer = class_290.field_1575.uploadImmediateVertexBuffer(mesh.method_60818());
                GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)view, (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
                class_6367 fb = target.framebuffer;
                try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sober overlay preview block", fb.method_71639(), OptionalInt.of(-15396578), fb.method_71640(), OptionalDouble.of(1.0));){
                    pass.setPipeline(BLOCK);
                    RenderSystem.bindDefaultUniforms((RenderPass)pass);
                    pass.setUniform("DynamicTransforms", transforms);
                    pass.bindTexture("Sampler0", stone, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                    pass.setVertexBuffer(0, buffer);
                    pass.draw(0, vertices);
                }
            }
        }
    }

    private static float[] project(Matrix4f mvp, float x, float y, float z, int w, int h) {
        Vector4f v = mvp.transform(new Vector4f(x, y, z, 1.0f));
        float nx = v.x / v.w;
        float ny = v.y / v.w;
        return new float[]{(nx * 0.5f + 0.5f) * (float)w, (1.0f - (ny * 0.5f + 0.5f)) * (float)h};
    }

    private static void line(class_332 context, float[] a, float[] b, float width, int c0, int c1) {
        Smooth.polyline(context, new float[]{a[0], b[0]}, new float[]{a[1], b[1]}, new float[]{width, width}, new int[]{c0, c1}, 2);
    }

    private static void classic2d(class_332 context, Matrix4f mvp, int w, int h, int rgb, float fill, float opacity, int pulseSpeed, float time) {
        int[][] edges;
        float pulse;
        float e = 0.503f;
        float[][] c = new float[8][];
        for (int i = 0; i < 8; ++i) {
            c[i] = ModulePreview.project(mvp, (i & 1) == 0 ? -e : e, (i & 2) == 0 ? -e : e, (i & 4) == 0 ? -e : e, w, h);
        }
        float f = pulse = pulseSpeed <= 0 ? 1.0f : 0.72f + 0.28f * (float)Math.sin(time * 3.0f * (float)pulseSpeed / 100.0f);
        if (fill > 0.0f) {
            int color = ModulePreview.alpha(rgb, fill * pulse * opacity);
            Smooth.polygon(context, new float[]{c[2][0], c[2][1], c[6][0], c[6][1], c[7][0], c[7][1], c[3][0], c[3][1]}, color);
            Smooth.polygon(context, new float[]{c[4][0], c[4][1], c[5][0], c[5][1], c[7][0], c[7][1], c[6][0], c[6][1]}, color);
            Smooth.polygon(context, new float[]{c[1][0], c[1][1], c[5][0], c[5][1], c[7][0], c[7][1], c[3][0], c[3][1]}, color);
        }
        int edge = ModulePreview.alpha(rgb, opacity);
        for (int[] pair : edges = new int[][]{{1, 3}, {2, 3}, {4, 5}, {4, 6}, {1, 5}, {2, 6}, {3, 7}, {5, 7}, {6, 7}}) {
            ModulePreview.line(context, c[pair[0]], c[pair[1]], 1.2f, edge, edge);
        }
    }

    private static void target2d(class_332 context, Matrix4f mvp, int w, int h, float time, int rgb, int accent, float opacity) {
        float spread = 0.13f + 0.012f * (float)Math.sin(time * 3.2f);
        float e = 0.5f + spread;
        float len = 0.3f;
        int top = ModulePreview.alpha(rgb, opacity);
        int bottom = ModulePreview.alpha(accent, opacity);
        for (int i = 0; i < 8; ++i) {
            if (i == 0) continue;
            float x = (i & 1) == 0 ? -e : e;
            float y = (i & 2) == 0 ? -e : e;
            float z = (i & 4) == 0 ? -e : e;
            int color = y > 0.0f ? top : bottom;
            float[] p = ModulePreview.project(mvp, x, y, z, w, h);
            ModulePreview.line(context, p, ModulePreview.project(mvp, x - Math.signum(x) * len, y, z, w, h), 1.3f, color, color);
            ModulePreview.line(context, p, ModulePreview.project(mvp, x, y - Math.signum(y) * len, z, w, h), 1.3f, color, color);
            ModulePreview.line(context, p, ModulePreview.project(mvp, x, y, z - Math.signum(z) * len, w, h), 1.3f, color, color);
        }
        float spin = time * 1.1f;
        ModulePreview.ring(context, mvp, w, h, 0.86f, spin, 0.28f, bottom, 1.2f);
        ModulePreview.ring(context, mvp, w, h, 0.72f, -spin * 1.35f, -0.42f, top, 0.9f);
    }

    private static void ring(class_332 context, Matrix4f mvp, int w, int h, float radius, float spin, float tilt, int color, float width) {
        for (int arc = 0; arc < 2; ++arc) {
            int steps = 26;
            float[] xs = new float[steps + 1];
            float[] ys = new float[steps + 1];
            float[] ws = new float[steps + 1];
            int[] cs = new int[steps + 1];
            for (int s = 0; s <= steps; ++s) {
                float t = spin + (float)arc * (float)Math.PI + 2.261947f * (float)s / (float)steps;
                float vx = radius * (float)Math.cos(t);
                float vz = radius * (float)Math.sin(t);
                float vy = vz * (float)Math.sin(tilt);
                float[] p = ModulePreview.project(mvp, vx, vy, vz *= (float)Math.cos(tilt), w, h);
                xs[s] = p[0];
                ys[s] = p[1];
                ws[s] = width;
                float fade = (float)Math.sin(Math.PI * (double)s / (double)steps);
                cs[s] = ModulePreview.alpha(color, (float)(color >>> 24) / 255.0f * (0.2f + 0.8f * fade));
            }
            Smooth.polyline(context, xs, ys, ws, cs, steps + 1);
        }
    }

    private static void glint(class_332 context, int w, int h, float opacity, int accent) {
        Smooth.glow(context, w / 2, h / 2, Math.round((float)Math.min(w, h) * 0.55f), ModulePreview.alpha(accent, 0.22f * opacity));
        if (sword == null) {
            sword = new class_1799((class_1935)class_1802.field_22022);
            sword.method_57379(class_9334.field_49641, true);
        }
        float scale = (float)Math.min(w, h) * 0.78f / 16.0f;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)w / 2.0f - 8.0f * scale, (float)h / 2.0f - 8.0f * scale);
        matrices.scale(scale, scale);
        context.method_51427(sword, 0, 0);
        matrices.popMatrix();
    }

    private static void accessories(class_332 context, float x, float y, int w, int h, float opacity, int accent) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        Smooth.glow(context, w / 2, Math.round((float)h * 0.45f), Math.round((float)Math.min(w, h) * 0.6f), ModulePreview.alpha(accent, 0.22f * opacity));
        float time = ModulePreview.seconds();
        Matrix3x2fStack matrices = context.method_51448();
        Vector2f origin = matrices.transformPosition(0.0f, 0.0f, new Vector2f());
        Vector2f corner = matrices.transformPosition((float)w, (float)h, new Vector2f());
        int x1 = Math.round(origin.x);
        int y1 = Math.round(origin.y);
        int x2 = Math.round(corner.x);
        int y2 = Math.round(corner.y);
        int size = Math.max(10, Math.round((float)(y2 - y1) * 0.36f));
        float lookX = (float)(x1 + x2) / 2.0f + (float)Math.sin(time * 0.7f) * (float)(x2 - x1) * 0.9f;
        float lookY = (float)y1 + (float)(y2 - y1) * 0.35f;
        matrices.pushMatrix();
        matrices.identity();
        class_490.method_2486((class_332)context, (int)x1, (int)y1, (int)x2, (int)y2, (int)size, (float)0.0625f, (float)lookX, (float)lookY, (class_1309)mc.field_1724);
        matrices.popMatrix();
    }

    private static final class Target {
        final class_2960 id;
        final Texture texture = new Texture();
        class_6367 framebuffer;

        Target(String name) {
            this.id = class_2960.method_60655((String)"albiz", (String)("dynamic/preview_" + name));
        }

        void ensure(int width, int height, boolean depth) {
            if (this.framebuffer == null) {
                this.framebuffer = new class_6367("Sober preview " + this.id.method_12832(), width, height, depth);
                this.texture.use(this.framebuffer.method_30277(), this.framebuffer.method_71639());
                class_310.method_1551().method_1531().method_4616(this.id, (class_1044)this.texture);
            } else if (this.framebuffer.field_1482 != width || this.framebuffer.field_1481 != height) {
                this.framebuffer.method_1234(width, height);
            }
            this.texture.use(this.framebuffer.method_30277(), this.framebuffer.method_71639());
        }
    }

    private static final class Texture
    extends class_1044 {
        private Texture() {
        }

        void use(GpuTexture gpu, GpuTextureView view) {
            this.field_56974 = gpu;
            this.field_60597 = view;
            this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        }

        public void close() {
        }
    }
}

