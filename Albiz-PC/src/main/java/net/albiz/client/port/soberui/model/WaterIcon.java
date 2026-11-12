// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.albiz.client.port.soberui.model.WaterIconRaster;
import net.albiz.client.port.soberui.model.WaterMesh;
import net.albiz.client.port.soberui.model.WaterModels;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class WaterIcon {
    private static final Map<WaterModels.Kind, Integer> COLORS = new EnumMap<WaterModels.Kind, Integer>(WaterModels.Kind.class);

    private WaterIcon() {
    }

    private static class_2960 id(WaterModels.Kind kind) {
        return class_2960.method_60655((String)"albiz", (String)("dynamic/watery_icon_" + net.albiz.client.port.soberui.UiResourceNames.lower(kind.name())));
    }

    public static class_2960 get(WaterModels.Kind kind, int rgb) {
        WaterMesh mesh = WaterModels.mesh(kind);
        if (mesh == null) {
            return null;
        }
        class_2960 id = WaterIcon.id(kind);
        Integer cached = COLORS.get((Object)kind);
        if (cached == null || cached != rgb) {
            int size = 64;
            int[] pixels = WaterIconRaster.render(mesh, rgb, size);
            class_1011 image = new class_1011(size, size, false);
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    image.method_61941(x, y, pixels[y * size + x]);
                }
            }
            class_1060 manager = class_310.method_1551().method_1531();
            if (cached != null) {
                manager.method_4615(id);
            }
            manager.method_4616(id, (class_1044)new LinearTexture(id, image));
            COLORS.put(kind, rgb);
        }
        return id;
    }

    private static final class LinearTexture
    extends class_1043 {
        LinearTexture(class_2960 id, class_1011 image) {
            super(() -> ((class_2960)id).toString(), image);
        }

        public class_12137 method_75484() {
            return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        }
    }
}

