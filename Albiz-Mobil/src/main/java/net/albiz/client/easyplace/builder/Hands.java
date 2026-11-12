package net.albiz.client.easyplace.builder;

import java.util.function.Predicate;
import net.albiz.client.easyplace.inventory.InventoryCalm;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2815;
import net.minecraft.class_2846;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;

@Environment(EnvType.CLIENT)
final class Hands {
   private static final long SWAP_WAIT_NANOS = 1000000000L;
   private static long swapSentAt = System.nanoTime() - 1000000000L;

   private Hands() {
   }

   static boolean hold(class_310 mc, class_746 player, Predicate<class_1799> want) {
      class_1661 inventory = player.method_31548();
      if (want.test(player.method_6047())) {
         return true;
      } else {
         for (int i = 0; i < 9; i++) {
            if (want.test(inventory.method_5438(i))) {
               inventory.method_61496(i);
               return true;
            }
         }

         if (player.field_7512 == player.field_7498 && mc.field_1761 != null) {
            for (int ix = 9; ix < 36; ix++) {
               if (want.test(inventory.method_5438(ix))) {
                  if (!InventoryCalm.settle(mc, player)) {
                     return false;
                  }

                  mc.field_1761.method_2906(player.field_7498.field_7763, ix, inventory.method_67532(), class_1713.field_7791, player);
                  close(mc, player);
                  return false;
               }
            }

            return false;
         } else {
            return false;
         }
      }
   }

   static boolean has(class_746 player, Predicate<class_1799> want) {
      class_1661 inventory = player.method_31548();

      for (int i = 0; i < 36; i++) {
         if (want.test(inventory.method_5438(i))) {
            return true;
         }
      }

      return want.test(player.method_6079());
   }

   static boolean toOffhand(class_310 mc, class_746 player, Predicate<class_1799> want) {
      if (player.field_7512 == player.field_7498 && mc.field_1761 != null && mc.method_1562() != null && player.field_7498.method_34255().method_7960()) {
         class_1661 inventory = player.method_31548();
         if (want.test(player.method_6047())) {
            long now = System.nanoTime();
            if (now - swapSentAt < 1000000000L) {
               return true;
            } else {
               swapSentAt = now;
               mc.method_1562().method_52787(new class_2846(class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
               return true;
            }
         } else {
            for (int i = 0; i < 9; i++) {
               if (want.test(inventory.method_5438(i))) {
                  inventory.method_61496(i);
                  return true;
               }
            }

            for (int ix = 9; ix < 36; ix++) {
               if (want.test(inventory.method_5438(ix))) {
                  if (!InventoryCalm.settle(mc, player)) {
                     return true;
                  }

                  mc.field_1761.method_2906(player.field_7498.field_7763, ix, 40, class_1713.field_7791, player);
                  close(mc, player);
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static void close(class_310 mc, class_746 player) {
      if (mc.field_1755 == null) {
         player.field_3944.method_52787(new class_2815(player.field_7498.field_7763));
      }
   }
}
