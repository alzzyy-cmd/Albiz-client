package net.albiz.client.easyplace.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class InventoryCalm {
   private static final double STOPPED = 0.0025;
   private static final double HALTED = 1.0E-4;

   private InventoryCalm() {
   }

   public static boolean idle(class_746 player) {
      class_10185 in = player.field_3913.field_54155;
      return !moving(in) && !in.comp_3164();
   }

   public static boolean still(class_746 player) {
      return idle(player) && player.method_24828() && player.method_18798().method_37268() < 0.0025;
   }

   public static boolean settle(class_310 mc, class_746 player) {
      if (still(player)) {
         return true;
      } else {
         class_315 options = mc.field_1690;
         options.field_1894.method_23481(false);
         options.field_1881.method_23481(false);
         options.field_1913.method_23481(false);
         options.field_1849.method_23481(false);
         options.field_1903.method_23481(false);
         options.field_1867.method_23481(false);
         options.field_1904.method_23481(false);
         options.field_1886.method_23481(false);
         class_10185 in = player.field_3913.field_54155;
         if (moving(in) || !player.method_24828() || player.method_18798().method_37268() >= 1.0E-4) {
            return false;
         } else if (!overGround(player)) {
            return true;
         } else {
            options.field_1832.method_23481(false);
            return false;
         }
      }
   }

   private static boolean moving(class_10185 in) {
      return in.comp_3159() || in.comp_3160() || in.comp_3161() || in.comp_3162() || in.comp_3163() || in.comp_3165();
   }

   private static boolean overGround(class_746 player) {
      class_2338 below = class_2338.method_49637(player.method_23317(), player.method_23318() - 0.05, player.method_23321());
      return !player.method_73183().method_8320(below).method_26220(player.method_73183(), below).method_1110();
   }
}
