package net.albiz.client.easyplace.sync;

import net.albiz.client.easyplace.mixin.AccessorClientLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public final class BlockAcks {
   private static class_638 level;
   private static int lastAcked = -1;

   private BlockAcks() {
   }

   public static void acked(int sequence) {
      class_638 now = class_310.method_1551().field_1687;
      if (now != level) {
         level = now;
         lastAcked = -1;
      }

      lastAcked = Math.max(lastAcked, sequence);
   }

   public static int lastSent(class_638 level) {
      return ((AccessorClientLevel)level).easyplace$predictions().method_41942();
   }

   public static boolean answered(class_638 now, int sequence) {
      return now == level && lastAcked >= sequence;
   }
}
