// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;import com.mojang.blaze3d.pipeline.RenderPipeline;import net.albiz.client.port.soberui.gui.ScreenStyle;import net.minecraft.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.At;
@Mixin(class_342.class) public abstract class TextFieldWidgetMixin{
    @WrapOperation(method={"method_48579"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V")}, require=0)
    private void sober$field(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!ScreenStyle.widgets()) {
            original.call(new Object[]{context, pipeline, sprite, x, y, width, height});
            return;
        }
        ScreenStyle.textField(context, this, x, y, width, height, ((class_342)(Object)this).method_25370());
    }

}
