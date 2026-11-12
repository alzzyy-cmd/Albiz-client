// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.albiz.client.port.soberui.modules.Glass;
import net.minecraft.class_1921;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={class_918.class})
public abstract class ItemGlassMixin {
    @ModifyVariable(method={"method_62476"}, at=@At(value="HEAD"), argsOnly=true, require=0)
    private static class_1921 albizGlass$glass(class_1921 layer, @Local(argsOnly=true) class_811 displayContext) {
        return Glass.item(layer, displayContext);
    }
}

