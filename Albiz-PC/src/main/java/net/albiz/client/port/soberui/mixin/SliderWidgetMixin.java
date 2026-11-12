package net.albiz.client.port.soberui.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_357;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_357.class})
public abstract class SliderWidgetMixin {
    @WrapOperation(method={"method_48579"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_332;method_52707(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIII)V")}, require=0)
    private void sober$slider(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ScreenStyle.widgets()) {
            original.call(new Object[]{context, pipeline, sprite, x, y, width, height, color});
            return;
        }
        float opacity = (float)(color >>> 24) / 255.0f;
        String path = sprite.method_12832();
        if (path.contains("handle")) {
            ScreenStyle.sliderHandle(context, x, y, width, height, path.contains("highlighted"), opacity);
        } else {
            ScreenStyle.sliderTrack(context, x, y, width, height, opacity);
        }
    }
}

