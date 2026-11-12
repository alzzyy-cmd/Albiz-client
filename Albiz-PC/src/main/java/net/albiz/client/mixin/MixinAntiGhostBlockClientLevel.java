package net.albiz.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.albiz.client.module.impl.misc.AntiGhostBlockModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_638.class})
public abstract class MixinAntiGhostBlockClientLevel {
   @WrapOperation(
      method = {"method_41928"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_7202;method_41940(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;)Z"
      )}
   )
   private boolean albiz$serverAnswer(class_7202 handler, class_2338 pos, class_2680 state, Operation<Boolean> original) {
      boolean predicted = (Boolean)original.call(new Object[]{handler, pos, state});
      return predicted && !AntiGhostBlockModule.serverAnswered((class_638)(Object)this, pos);
   }

   @Inject(
      method = {"method_41927"},
      at = {@At("TAIL")}
   )
   private void albiz$acknowledged(int sequence, CallbackInfo ci) {
      AntiGhostBlockModule.acknowledged((class_638)(Object)this, sequence);
   }
}
