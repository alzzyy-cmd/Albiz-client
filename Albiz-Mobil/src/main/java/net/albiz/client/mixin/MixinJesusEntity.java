package net.albiz.client.mixin;

import net.albiz.client.module.impl.movement.JesusModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_1297.class})
public abstract class MixinJesusEntity {
   @Inject(
      method = {"method_5700"},
      at = {@At("HEAD")}
   )
   private void albiz$jesusAboveBubbleColumn(CallbackInfo ci) {
      this.albiz$jesusMarkBubbleColumn();
   }

   @Inject(
      method = {"method_5764"},
      at = {@At("HEAD")}
   )
   private void albiz$jesusInsideBubbleColumn(CallbackInfo ci) {
      this.albiz$jesusMarkBubbleColumn();
   }

   @Unique
   private void albiz$jesusMarkBubbleColumn() {
      JesusModule jesus = JesusModule.active;
      if (jesus != null && (Object)this == class_310.method_1551().field_1724) {
         jesus.markInBubbleColumn();
      }
   }
}
