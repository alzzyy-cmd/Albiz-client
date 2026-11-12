package net.albiz.client.mixin;

import net.albiz.client.module.impl.misc.AntiGhostBlockModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_636.class})
public abstract class MixinAntiGhostBlockMultiPlayerGameMode {
   @Shadow
   private int field_3716;

   @Shadow
   public abstract void method_2925();

   @Inject(
      method = {"method_2899"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void albiz$holdBreak(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      if (AntiGhostBlockModule.holdBreak(pos)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"method_2910"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void albiz$waitBeforeStart(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
      if (AntiGhostBlockModule.waiting(pos)) {
         this.method_2925();
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"method_2902"},
      at = {@At("HEAD")},
      cancellable = true,
      order = 1100
   )
   private void albiz$waitBeforeContinue(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
      if (AntiGhostBlockModule.waiting(pos)) {
         if (this.field_3716 > 0) {
            this.field_3716--;
         }

         this.method_2925();
         cir.setReturnValue(false);
      }
   }
}
