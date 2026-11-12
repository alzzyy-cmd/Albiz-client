// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_12137;

final class SmoothTexture
extends class_1043 {
    SmoothTexture(Supplier<String> name, class_1011 image) {
        super(name, image);
        this.field_63613 = SmoothTexture.linear();
    }

    private static class_12137 linear() {
        return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
    }

    public class_12137 method_75484() {
        return SmoothTexture.linear();
    }
}

