package net.albiz.client.port.soberui.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_437.class})
public abstract class ScreenMixin {
    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final @org.spongepowered.asm.mixin.Mutable protected net.minecraft.class_2561 field_22785;
    @Inject(method="<init>",at=@At("RETURN")) private void albiz$worldHeading(net.minecraft.class_2561 title,CallbackInfo ci){
      if((Object)this instanceof net.minecraft.class_526){String before=title.getString(),after=net.albiz.client.i18n.AlbizI18n.display(before);if(!before.equals(after))this.field_22785=net.minecraft.class_2561.method_43470(after).method_10862(title.method_10866());}
    }

    @Shadow
    protected class_310 field_22787;
    @Shadow
    public int field_22789;
    @Shadow
    public int field_22790;
    @Unique
    private long sober$openedAt;

    private boolean sober$menu() {
        return this.field_22787 != null && this.field_22787.field_1687 == null && ScreenStyle.screens();
    }

    @Inject(method={"method_25423(II)V"}, at={@At(value="TAIL")}, require=0)
    private void sober$opened(int width, int height, CallbackInfo ci) {
        if (this.sober$openedAt == 0L) {
            this.sober$openedAt = System.nanoTime();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"method_47413"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_437;method_25394(Lnet/minecraft/class_332;IIF)V")}, require=0)
    private void sober$intro(class_437 screen, class_332 context, int mouseX, int mouseY, float delta, Operation<Void> original) {
        float p = ScreenStyle.intro(this.sober$openedAt, this.field_22787, screen);
        if (p >= 1.0f) {
            original.call(new Object[]{screen, context, mouseX, mouseY, Float.valueOf(delta)});
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(0.0f, 10.0f * (1.0f - p));
        try {
            original.call(new Object[]{screen, context, mouseX, mouseY, Float.valueOf(delta)});
        }
        finally {
            matrices.popMatrix();
        }
        context.method_71048();
        ScreenStyle.introVeil(context, this.field_22789, this.field_22790, p);
    }

    @Inject(method={"method_57728"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$backdrop(class_332 context, float deltaTicks, CallbackInfo ci) {
        if (!this.sober$menu() || ScreenStyle.vanillaPlus()) {
            return;
        }
        ScreenStyle.backdrop(context, this.field_22789, this.field_22790);
        if (Backdrop.requestMenu(this.field_22787)) {
            try {
                context.method_71048();
                context.method_71278();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        ci.cancel();
    }

    @Inject(method={"method_57734"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$blur(class_332 context, CallbackInfo ci) {
        if (this.sober$menu() && !ScreenStyle.vanillaPlus()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_57736(Lnet/minecraft/class_332;IIII)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$darkening(class_332 context, int x, int y, int w, int h, CallbackInfo ci) {
        if (!this.sober$menu()) {
            return;
        }
        ScreenStyle.darkening(context, x, y, w, h);
        ci.cancel();
    }
}

