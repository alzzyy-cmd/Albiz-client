// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.albiz.client.port.soberui.render.GlassGear;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_12249.class})
public abstract class GlassGearMixin {
    @Inject(method={"method_75966(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75978(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75982(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75990(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75994(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_76000(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private static void albizGlass$glassGear(class_2960 texture, CallbackInfoReturnable<class_1921> cir) {
        class_1921 layer = GlassGear.layer(texture);
        if (layer != null) {
            cir.setReturnValue(layer);
        }
    }

    @Inject(method={"method_75969(Lnet/minecraft/class_2960;Z)Lnet/minecraft/class_1921;", "method_75980(Lnet/minecraft/class_2960;Z)Lnet/minecraft/class_1921;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private static void albizGlass$glassGearOutline(class_2960 texture, boolean affectsOutline, CallbackInfoReturnable<class_1921> cir) {
        class_1921 layer = GlassGear.layer(texture);
        if (layer != null) {
            cir.setReturnValue(layer);
        }
    }
}

