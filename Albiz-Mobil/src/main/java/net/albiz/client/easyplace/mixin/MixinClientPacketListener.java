package net.albiz.client.easyplace.mixin;

import net.albiz.client.easyplace.sync.BlockAcks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4463;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_634.class})
public abstract class MixinClientPacketListener {
   @Inject(
      method = {"method_21707"},
      at = {@At("TAIL")}
   )
   private void easyplace$acked(class_4463 packet, CallbackInfo ci) {
      BlockAcks.acked(packet.comp_633());
   }
}
