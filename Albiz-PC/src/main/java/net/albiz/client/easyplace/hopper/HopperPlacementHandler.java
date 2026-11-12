package net.albiz.client.easyplace.hopper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2350;
import net.minecraft.class_2377;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2350.class_2351;

@Environment(EnvType.CLIENT)
public final class HopperPlacementHandler {
   public boolean isHopper(class_2680 state) {
      return state.method_26204() instanceof class_2377;
   }

   public boolean faceCanProduceExpectedFacing(class_2680 expected, class_2350 clickedFace) {
      if (this.isHopper(expected) && expected.method_28498(class_2741.field_12545)) {
         class_2350 wanted = (class_2350)expected.method_11654(class_2741.field_12545);
         class_2350 produced = clickedFace.method_10153();
         if (produced.method_10166() == class_2351.field_11052) {
            produced = class_2350.field_11033;
         }

         return produced == wanted;
      } else {
         return true;
      }
   }

   public int facingScore(class_2680 expected, class_2350 clickedFace) {
      return this.faceCanProduceExpectedFacing(expected, clickedFace) ? 1000 : -1000;
   }
}
