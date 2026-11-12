// Adapted from SoberClient (Violet), GPL-3.0-only.
package net.albiz.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.albiz.client.module.impl.render.CustomTotemsModule;
import net.minecraft.class_10442;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_10442.class})
public abstract class MixinCustomTotems {
    @WrapOperation(method={"method_65596"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1799;method_58694(Lnet/minecraft/class_9331;)Ljava/lang/Object;")}, require=0)
    private Object albiz$totemStyle(class_1799 stack, class_9331<?> type, Operation<Object> original) {
        Object object;
        Object value = original.call(new Object[]{stack, type});
        if (type == class_9334.field_54199 && value instanceof class_2960) {
            class_2960 model = (class_2960)value;
            object = CustomTotemsModule.model(stack, model);
        } else {
            object = value;
        }
        return object;
    }
}

