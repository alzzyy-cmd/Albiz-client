package net.albiz.client.port.soberui.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_350;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_350.class})
public abstract class EntryListWidgetMixin {
    private class_339 sober$self() {
        return (class_339)(Object)this;
    }

    @Inject(method={"method_57715"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$background(class_332 context, CallbackInfo ci) {
        if (!ScreenStyle.screens()) {
            return;
        }
        class_339 self = this.sober$self();
        ScreenStyle.listBackground(context, self.method_46426(), self.method_46427(), self.method_25368(), self.method_25364());
        ci.cancel();
    }

    @Inject(method={"method_57713"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void sober$separators(class_332 context, CallbackInfo ci) {
        if (!ScreenStyle.screens()) {
            return;
        }
        class_339 self = this.sober$self();
        ScreenStyle.separators(context, self.method_46426(), self.method_46427(), self.method_55443(), self.method_25368());
        ci.cancel();
    }

    @WrapOperation(method={"method_44398"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_332;method_25294(IIIII)V", ordinal=0)}, require=0)
    private void sober$selection(class_332 context, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        if (!ScreenStyle.widgets()) {
            original.call(new Object[]{context, x1, y1, x2, y2, color});
            return;
        }
        ScreenStyle.selection(context, x1, y1, x2 - x1, y2 - y1, color == -1);
    }

    @WrapOperation(method={"method_44398"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_332;method_25294(IIIII)V", ordinal=1)}, require=0)
    private void sober$selectionInner(class_332 context, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        if (!ScreenStyle.widgets()) {
            original.call(new Object[]{context, x1, y1, x2, y2, color});
        }
    }
}

