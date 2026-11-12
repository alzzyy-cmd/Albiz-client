// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1059;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class GlassLayers {
    private static final int SCALE = 8;
    private static final RenderPipeline PLAYER = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56854, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/glass_player")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withCull(false).build();
    private static final RenderPipeline ITEM = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56854, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/glass_item")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withShaderDefine("ITEM").withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withCull(false).build();
    private static final RenderPipeline GUI = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56854, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/glass_gui")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/glass_entity")).withShaderDefine("ITEM").withShaderDefine("GUI").withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withCull(false).build();
    private static final class_2960 WIDE = class_2960.method_60655((String)"albiz", (String)"glass/wide");
    private static final class_2960 SLIM = class_2960.method_60655((String)"albiz", (String)"glass/slim");
    private static final Map<class_2960, class_1921> TEXTURED = new HashMap<class_2960, class_1921>();
    private static class_1921 wide;
    private static class_1921 slim;
    private static class_1921 items;
    private static class_1921 blocks;
    private static class_1921 guiItems;
    private static class_1921 guiBlocks;

    private GlassLayers() {
    }

    public static class_1921 player(boolean slimArms) {
        if (wide == null) {
            GlassLayers.register(WIDE, false);
            GlassLayers.register(SLIM, true);
            wide = class_1921.method_75940((String)"albiz_glass_wide", (class_12247)class_12247.method_75927((RenderPipeline)PLAYER).method_75934("Sampler0", WIDE).method_75928().method_75937().method_75938());
            slim = class_1921.method_75940((String)"albiz_glass_slim", (class_12247)class_12247.method_75927((RenderPipeline)PLAYER).method_75934("Sampler0", SLIM).method_75928().method_75937().method_75938());
        }
        return slimArms ? slim : wide;
    }

    public static class_1921 item(boolean blockAtlas, boolean gui) {
        if (items == null) {
            items = class_1921.method_75940((String)"albiz_glass_items", (class_12247)class_12247.method_75927((RenderPipeline)ITEM).method_75934("Sampler0", class_1059.field_64467).method_75928().method_75937().method_75938());
            blocks = class_1921.method_75940((String)"albiz_glass_blocks", (class_12247)class_12247.method_75927((RenderPipeline)ITEM).method_75934("Sampler0", class_1059.field_5275).method_75928().method_75937().method_75938());
            guiItems = class_1921.method_75940((String)"albiz_glass_gui_items", (class_12247)class_12247.method_75927((RenderPipeline)GUI).method_75934("Sampler0", class_1059.field_64467).method_75928().method_75937().method_75938());
            guiBlocks = class_1921.method_75940((String)"albiz_glass_gui_blocks", (class_12247)class_12247.method_75927((RenderPipeline)GUI).method_75934("Sampler0", class_1059.field_5275).method_75928().method_75937().method_75938());
        }
        if (gui) {
            return blockAtlas ? guiBlocks : guiItems;
        }
        return blockAtlas ? blocks : items;
    }

    public static class_1921 textured(class_2960 texture) {
        if(TEXTURED.size()>=128 && !TEXTURED.containsKey(texture)) TEXTURED.clear();
        return TEXTURED.computeIfAbsent(texture, id -> class_1921.method_75940((String)("albiz_glass_" + id.method_12836() + "_" + id.method_12832().replace('/', '_').replace('.', '_')), (class_12247)class_12247.method_75927((RenderPipeline)ITEM).method_75934("Sampler0", id).method_75928().method_75937().method_75938()));
    }

    private static void register(class_2960 id, boolean slimArms) {
        class_1011 image = new class_1011(512, 512, true);
        image.method_4326(0, 0, image.method_4307(), image.method_4323(), 0);
        int arm = slimArms ? 3 : 4;
        GlassLayers.cuboid(image, 0, 0, 8, 8, 8);
        GlassLayers.cuboid(image, 16, 16, 8, 12, 4);
        GlassLayers.cuboid(image, 40, 16, arm, 12, 4);
        GlassLayers.cuboid(image, 32, 48, arm, 12, 4);
        GlassLayers.cuboid(image, 0, 16, 4, 12, 4);
        GlassLayers.cuboid(image, 16, 48, 4, 12, 4);
        class_310.method_1551().method_1531().method_4616(id, (class_1044)new Texture(id, image));
    }

    private static void cuboid(class_1011 image, int u, int v, int w, int h, int d) {
        GlassLayers.face(image, u + d, v, w, d);
        GlassLayers.face(image, u + d + w, v, w, d);
        GlassLayers.face(image, u, v + d, d, h);
        GlassLayers.face(image, u + d, v + d, w, h);
        GlassLayers.face(image, u + d + w, v + d, d, h);
        GlassLayers.face(image, u + d + w + d, v + d, w, h);
    }

    private static void face(class_1011 image, int u, int v, int w, int h) {
        int x0 = u * 8;
        int y0 = v * 8;
        int pw = w * 8;
        int ph = h * 8;
        for (int y = 0; y < ph; ++y) {
            for (int x = 0; x < pw; ++x) {
                double dx = Math.min((double)x + 0.5, (double)(pw - x) - 0.5);
                double dy = Math.min((double)y + 0.5, (double)(ph - y) - 0.5);
                double edge = Math.min(dx, dy) / 8.0;
                double glow = Math.exp(-edge / 0.09) + 0.22 * Math.exp(-edge / 0.7);
                int r = (int)Math.round(Math.min(1.0, glow) * 255.0);
                image.method_61941(x0 + x, y0 + y, 0xFF000000 | r << 16 | 0xFF00 | 0xFF);
            }
        }
    }

    private static final class Texture
    extends class_1043 {
        Texture(class_2960 id, class_1011 image) {
            super(() -> ((class_2960)id).toString(), image);
        }

        public class_12137 method_75484() {
            return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        }
    }
}

