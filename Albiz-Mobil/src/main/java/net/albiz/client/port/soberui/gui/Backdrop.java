// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.GuiBatch;
import net.albiz.client.port.soberui.gui.Theme;
import java.util.OptionalInt;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_6367;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class Backdrop {
    private static final class_2960 ID = class_2960.method_60655((String)"albiz", (String)"dynamic/backdrop");
    private static final float LENS = 0.94f;
    private static final int DOWN_STEPS = 2;
    private static final int UP_STEPS = 1;
    private static final RenderPipeline GLASS = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56864}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/liquid_glass")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/liquid_glass")).build();
    private static final RenderPipeline GLASS_DENSE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56864}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/liquid_glass_dense")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/liquid_glass")).withShaderDefine("YS_DENSE").build();
    private static int glassState;
    private static final class_6367[] down;
    private static final class_6367[] up;
    private static final GuiBatch BATCH;
    private static Texture texture;
    private static boolean ready;
    private static boolean wanted;
    private static boolean broken;
    private static boolean menuRequested;
    private static int menuMisses;

    private Backdrop() {
    }

    public static void capture(class_310 mc) {
        ready = false;
        wanted = !broken && Theme.liquidGlass() && mc.field_1687 != null && !mc.field_1690.field_1842;
    }

    private static boolean ensure() {
        if (!ready && wanted) {
            wanted = false;
            ready = Backdrop.chain(class_310.method_1551());
        }
        return ready;
    }

    public static boolean requestMenu(class_310 mc) {
        if (broken || menuMisses > 5 || mc.field_1687 != null || !Theme.liquidGlass() || !Backdrop.glassReady()) {
            return false;
        }
        if (menuRequested) {
            ++menuMisses;
        }
        if (texture == null && !Backdrop.chain(mc)) {
            return false;
        }
        menuRequested = true;
        return true;
    }

    public static boolean menuActive() {
        return menuRequested;
    }

    public static boolean captureMenu() {
        if (!menuRequested) {
            return false;
        }
        menuRequested = false;
        menuMisses = 0;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 != null) {
            return false;
        }
        Backdrop.chain(mc);
        return true;
    }

    private static boolean chain(class_310 mc) {
        try {
            class_276 main = mc.method_1522();
            if (main == null || main.method_71639() == null) {
                return false;
            }
            GpuTextureView source = main.method_71639();
            for (int i = 0; i < 2; ++i) {
                int shift = i + 1;
                Backdrop.down[i] = Backdrop.sized(down[i], "Sober backdrop down " + shift, Math.max(1, main.field_1482 >> shift), Math.max(1, main.field_1481 >> shift));
                Backdrop.blit(source, down[i].method_71639());
                source = down[i].method_71639();
            }
            class_6367 result = down[1];
            for (int i = 0; i < 1; ++i) {
                int shift = 1 - i;
                Backdrop.up[i] = Backdrop.sized(up[i], "Sober backdrop up " + shift, Math.max(1, main.field_1482 >> shift), Math.max(1, main.field_1481 >> shift));
                Backdrop.blit(source, up[i].method_71639());
                source = up[i].method_71639();
                result = up[i];
            }
            if (texture == null) {
                texture = new Texture();
                texture.use(result.method_30277(), result.method_71639());
                mc.method_1531().method_4616(ID, (class_1044)texture);
            }
            texture.use(result.method_30277(), result.method_71639());
            return true;
        }
        catch (RuntimeException e) {
            broken = true;
            SoberClient.LOG.warn("Liquid Glass backdrop is not available on this system, the glass stays plain", (Throwable)e);
            return false;
        }
    }

    private static class_6367 sized(class_6367 framebuffer, String name, int width, int height) {
        if (framebuffer == null) {
            return new class_6367(name, width, height, false);
        }
        if (framebuffer.field_1482 != width || framebuffer.field_1481 != height) {
            framebuffer.method_1234(width, height);
        }
        return framebuffer;
    }

    private static void blit(GpuTextureView from, GpuTextureView to) {
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sober backdrop", to, OptionalInt.empty());){
            pass.setPipeline(class_10799.field_57928);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.bindTexture("InSampler", from, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            pass.draw(0, 3);
        }
    }

    private static boolean glassReady() {
        if (glassState == 0) {
            try {
                glassState = RenderSystem.getDevice().precompilePipeline(GLASS).isValid() && RenderSystem.getDevice().precompilePipeline(GLASS_DENSE).isValid() ? 1 : -1;
            }
            catch (RuntimeException e) {
                glassState = -1;
            }
            if (glassState < 0) {
                SoberClient.LOG.warn("Liquid Glass shader is not available, using the classic glass");
            }
        }
        return glassState > 0;
    }

    public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend, int tint) {
        return Backdrop.glass(context, x, y, w, h, r, opacity, bend, tint, false);
    }

    public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend, int tint, boolean dense) {
        block6: {
            block5: {
                boolean menu;
                boolean bl = menu = class_310.method_1551().field_1687 == null;
                if (w <= 0 || h <= 0 || (!menu ? !Backdrop.ensure() : !menuRequested)) break block5;
                if (texture != null && Backdrop.glassReady()) break block6;
            }
            return false;
        }
        int alpha = Math.round(255.0f * Math.max(0.0f, Math.min(1.0f, opacity)));
        if (alpha <= 0) {
            return true;
        }
        class_310 mc = class_310.method_1551();
        float pixel = (float)mc.method_22683().method_4489() / (float)Math.max(1, context.method_51421());
        Matrix3x2fStack matrix = context.method_51448();
        Vector2f start = matrix.transformPosition((float)x, (float)y, new Vector2f());
        Vector2f end = matrix.transformPosition((float)(x + w), (float)y, new Vector2f());
        float scale = Math.abs(end.x - start.x) * pixel / (float)w;
        if (scale <= 0.0f) {
            return true;
        }
        float radius = (float)Math.floor(Math.min(1000.0f, (float)Math.max(0, r) * scale * 1.35f));
        float band = (float)Math.floor(Math.min(1000.0f, Math.max(1.0f, bend * scale * 2.5f)));
        GuiBatch batch = BATCH.reset();
        batch.quad(x, y, x + w, y + h, radius * 4.0f + 1.5f, band * 4.0f + 1.5f, radius * 4.0f + 2.5f, band * 4.0f + 2.5f);
        batch.submitTextured(context, dense ? GLASS_DENSE : GLASS, ID, alpha << 24 | tint & 0xFFFFFF);
        return true;
    }

    public static boolean draw(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        return Backdrop.draw(context, x, y, w, h, r, opacity, 0.0f);
    }

    public static boolean draw(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend) {
        if (w <= 0 || h <= 0 || !Backdrop.ensure() || texture == null) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        int frameWidth = mc.method_22683().method_4489();
        int frameHeight = mc.method_22683().method_4506();
        float pixelX = (float)frameWidth / (float)Math.max(1, context.method_51421());
        float pixelY = (float)frameHeight / (float)Math.max(1, context.method_51443());
        Matrix3x2fStack matrix = context.method_51448();
        Vector2f topLeft = matrix.transformPosition((float)x, (float)y, new Vector2f());
        Vector2f bottomRight = matrix.transformPosition((float)(x + w), (float)(y + h), new Vector2f());
        float left = topLeft.x * pixelX;
        float top = topLeft.y * pixelY;
        float spanX = (bottomRight.x - topLeft.x) * pixelX;
        float spanY = (bottomRight.y - topLeft.y) * pixelY;
        if (spanX < 1.0f || spanY < 1.0f) {
            return false;
        }
        int width = Math.max(1, Math.round(spanX));
        int height = Math.max(1, Math.round(spanY));
        float lensX = left + spanX * 0.060000002f / 2.0f;
        float lensY = top + spanY * 0.060000002f / 2.0f;
        float perX = spanX * 0.94f / (float)width;
        float perY = spanY * 0.94f / (float)height;
        int alpha = Math.round(255.0f * Math.max(0.0f, Math.min(1.0f, opacity)));
        if (alpha <= 0) {
            return false;
        }
        int color = alpha << 24 | 0xC2C2C2;
        float radius = Math.max(0.0f, Math.min((float)Math.max(0, r) * spanX / (float)w, (float)Math.min(width, height) / 2.0f));
        float band = Math.max(0.0f, Math.min(bend * spanX / (float)w, (float)Math.min(width, height) * 0.3f));
        Mapping map = new Mapping(lensX, lensY, perX, perY, frameWidth, frameHeight, width, height, band, Math.max(1, Math.round(band / 6.0f)));
        GuiBatch batch = BATCH.reset();
        float zone = Math.max(radius, band);
        int middleTop = -1;
        for (int row = 0; row < height; ++row) {
            float dx;
            float coverage;
            int start;
            float center = (float)row + 0.5f;
            if (center >= zone && center <= (float)height - zone) {
                if (middleTop >= 0) continue;
                middleTop = row;
                continue;
            }
            if (middleTop >= 0) {
                map.span(batch, 0, width, middleTop, row, 0);
                middleTop = -1;
            }
            float dy = center < radius ? radius - center : (center > (float)height - radius ? center - ((float)height - radius) : 0.0f);
            int half = (width + 1) / 2;
            for (start = 0; dy > 0.0f && start < half && !((coverage = Math.max(0.0f, Math.min(1.0f, radius - (float)Math.sqrt((dx = Math.max(0.0f, radius - ((float)start + 0.5f))) * dx + dy * dy) + 0.5f))) >= 0.996f); ++start) {
                int edge = Math.round((float)alpha * coverage);
                if (edge <= 0) continue;
                int tint = edge << 24 | 0xC2C2C2;
                map.span(batch, start, start + 1, row, row + 1, tint);
                if (width - 1 - start == start) continue;
                map.span(batch, width - 1 - start, width - start, row, row + 1, tint);
            }
            if (width - start * 2 <= 0) continue;
            map.span(batch, start, width - start, row, row + 1, 0);
        }
        if (middleTop >= 0) {
            map.span(batch, 0, width, middleTop, height, 0);
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)x, (float)y);
        matrices.scale((float)w / spanX, (float)h / spanY);
        batch.submitTextured(context, class_10799.field_56883, ID, color);
        matrices.popMatrix();
        return true;
    }

    static {
        down = new class_6367[2];
        up = new class_6367[1];
        BATCH = new GuiBatch();
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

    private record Mapping(float lensX, float lensY, float perX, float perY, int frameWidth, int frameHeight, int width, int height, float band, int step) {
        private static final float STRENGTH = 0.42f;

        private float bent(float distance) {
            if (distance >= this.band) {
                return distance;
            }
            float t = 1.0f - distance / this.band;
            return distance + 0.42f * this.band * t * t;
        }

        private float u(float px) {
            float mapped = this.band <= 0.5f ? px : (px < this.band ? this.bent(px) : (px > (float)this.width - this.band ? (float)this.width - this.bent((float)this.width - px) : px));
            return (this.lensX + mapped * this.perX) / (float)this.frameWidth;
        }

        private float v(float py) {
            float mapped = this.band <= 0.5f ? py : (py < this.band ? this.bent(py) : (py > (float)this.height - this.band ? (float)this.height - this.bent((float)this.height - py) : py));
            return ((float)this.frameHeight - (this.lensY + mapped * this.perY)) / (float)this.frameHeight;
        }

        void span(GuiBatch batch, int x1, int x2, int y1, int y2, int tint) {
            float v1 = this.v(y1);
            float v2 = this.v(y2);
            int cursor = x1;
            int inner = (int)Math.ceil(this.band);
            int outer = (int)Math.floor((float)this.width - this.band);
            while (cursor < x2) {
                int next = this.band > 0.5f && cursor < inner ? Math.min(Math.min(x2, inner), cursor + this.step) : (this.band > 0.5f && cursor >= outer ? Math.min(x2, cursor + this.step) : (this.band > 0.5f ? Math.min(x2, Math.max(cursor + 1, outer)) : x2));
                batch.quad(cursor, y1, next, y2, this.u(cursor), v1, this.u(next), v2, tint);
                cursor = next;
            }
        }
    }
}

