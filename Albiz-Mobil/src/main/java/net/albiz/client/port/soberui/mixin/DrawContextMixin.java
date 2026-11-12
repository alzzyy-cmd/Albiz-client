// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.modules.CustomModels;
import net.minecraft.class_10799;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_332.class})
public abstract class DrawContextMixin {
    @Inject(method={"method_51425(Lnet/minecraft/class_1309;Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;III)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$modelIcon(class_1309 entity, class_1937 world, class_1799 stack, int x, int y, int seed, CallbackInfo ci) {
        if (net.minecraft.class_310.method_1551().field_1755 instanceof net.albiz.client.port.soberui.gui.VisualPresetsScreen || stack.method_7960()) {
            return;
        }
        try {
            class_2960 icon;
            CustomModels models = SoberClient.MODULES.get(CustomModels.class);
            class_2960 class_29602 = icon = models == null || !models.isActive() ? null : models.icon(stack);
            if (icon == null) {
                return;
            }
            class_332 context = (class_332)(Object)this;
            context.method_25302(class_10799.field_56883, icon, x, y, 0.0f, 0.0f, 16, 16, 64, 64, 64, 64);
            ci.cancel();
        }
        catch (RuntimeException e) {
            SoberClient.LOG.error("Custom model icon failed", (Throwable)e);
        }
    }
}

