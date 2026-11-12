// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.model.WaterMesh;
import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_11659;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;

public final class WaterModels {
    private static final class_2960 WHITE = class_2960.method_60655((String)"albiz", (String)"textures/models/white.png");
    private static final RenderPipeline BODY = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56854, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/water_item")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/water_item")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/water_item")).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(true).withCull(true).build();
    private static final RenderPipeline RIM = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56854, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"albiz", (String)"pipeline/water_item_rim")).withVertexShader(class_2960.method_60655((String)"albiz", (String)"core/water_item")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"core/water_item")).withShaderDefine("RIM").withBlend(BlendFunction.LIGHTNING).withDepthWrite(false).withCull(true).build();
    private static class_1921 body;
    private static class_1921 rim;
    private static volatile WaterMesh[][] meshes;
    private static boolean building;

    private WaterModels() {
    }

    public static Kind kind(String item) {
        if (item == null) {
            return null;
        }
        if (item.equals("totem_of_undying")) {
            return Kind.TOTEM;
        }
        if (item.equals("trident")) {
            return Kind.TRIDENT;
        }
        if (item.endsWith("_pickaxe")) {
            return Kind.PICKAXE;
        }
        if (item.endsWith("_sword")) {
            return Kind.SWORD;
        }
        if (item.endsWith("_shovel")) {
            return Kind.SHOVEL;
        }
        return null;
    }

    private static void build() {
        if (building) {
            return;
        }
        building = true;
        Thread thread = new Thread(() -> {
            try {
                Kind[] kinds = Kind.values();
                WaterMesh[][] built = new WaterMesh[2][kinds.length];
                for (Kind kind : kinds) {
                    built[0][kind.ordinal()] = kind.body.get();
                    built[1][kind.ordinal()] = kind.glow.get();
                }
                meshes = built;
            }
            catch (Throwable e) {
                SoberClient.LOG.warn("Could not build the water models", e);
            }
        }, "Sober Water Models");
        thread.setDaemon(true);
        thread.setPriority(1);
        thread.start();
    }

    public static WaterMesh mesh(Kind kind) {
        WaterMesh[][] all = meshes;
        if (all == null) {
            WaterModels.build();
            return null;
        }
        return all[0][kind.ordinal()];
    }

    public static boolean ready() {
        if (meshes == null) {
            WaterModels.build();
        }
        return meshes != null;
    }

    public static void render(class_4587 matrices, class_11659 queue, Kind kind, int color, int light, boolean glow) {
        WaterMesh shell;
        WaterMesh[][] all = meshes;
        if (all == null) {
            return;
        }
        WaterMesh mesh = all[0][kind.ordinal()];
        WaterMesh waterMesh = shell = glow ? all[1][kind.ordinal()] : null;
        if (body == null) {
            body = class_1921.method_75940((String)"sober_water_item", (class_12247)class_12247.method_75927((RenderPipeline)BODY).method_75934("Sampler0", WHITE).method_75928().method_75937().method_75938());
            rim = class_1921.method_75940((String)"sober_water_item_rim", (class_12247)class_12247.method_75927((RenderPipeline)RIM).method_75934("Sampler0", WHITE).method_75928().method_75937().method_75938());
        }
        queue.method_73483(matrices, body, (entry, consumer) -> WaterModels.emit(entry, consumer, mesh, color, light, false));
        if (shell != null) {
            queue.method_73483(matrices, rim, (entry, consumer) -> WaterModels.emit(entry, consumer, shell, color, light, true));
        }
    }

    private static void emit(class_4587.class_4665 entry, class_4588 consumer, WaterMesh mesh, int color, int light, boolean inside) {
        float[] p = mesh.positions;
        float[] n = mesh.normals;
        int total = mesh.quads * 4;
        for (int k = 0; k < total; ++k) {
            int v = inside ? k & 0xFFFFFFFC | 3 - (k & 3) : k;
            int i = v * 3;
            float nx = n[i];
            float ny = n[i + 1];
            float nz = n[i + 2];
            float x = p[i];
            float y = p[i + 1];
            consumer.method_56824(entry, x, y, p[i + 2]).method_39415(color).method_22913(x, y).method_22922(class_4608.field_21444).method_60803(light).method_60831(entry, nx, ny, nz);
        }
    }

    public static enum Kind {
        PICKAXE(WaterMesh::pickaxe, WaterMesh::pickaxeGlow),
        SWORD(WaterMesh::sword, WaterMesh::swordGlow),
        SHOVEL(WaterMesh::shovel, WaterMesh::shovelGlow),
        TRIDENT(WaterMesh::trident, WaterMesh::tridentGlow),
        TOTEM(WaterMesh::totem, WaterMesh::totemGlow);

        private final Supplier<WaterMesh> body;
        private final Supplier<WaterMesh> glow;

        private Kind(Supplier<WaterMesh> body, Supplier<WaterMesh> glow) {
            this.body = body;
            this.glow = glow;
        }
    }
}

