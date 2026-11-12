package net.albiz.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.albiz.client.module.impl.movement.BoatFlyModule;
import net.albiz.client.module.impl.movement.EntityControlModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1316;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({class_746.class})
public abstract class MixinEntityControlLocalPlayer {
   @ModifyReturnValue(
      method = {"method_3151"},
      at = {@At("RETURN")}
   )
   private float albiz$entityControlMaxJump(float original) {
      return EntityControlModule.maxJump() ? 1.0F : original;
   }

   @ModifyReturnValue(
      method = {"method_45773"},
      at = {@At("RETURN")}
   )
   private class_1316 albiz$boatFlyCancelJump(class_1316 original) {
      return original != null && BoatFlyModule.cancelsMountJump((class_746)(Object)this) ? null : original;
   }
}
