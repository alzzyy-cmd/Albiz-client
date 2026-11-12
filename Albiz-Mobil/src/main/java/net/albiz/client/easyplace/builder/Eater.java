package net.albiz.client.easyplace.builder;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_4174;
import net.minecraft.class_746;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
final class Eater {
   static final int HUNGRY = 14;
   private static final int EAT_TICKS = 70;
   private final Predicate<class_1799> food;
   private int ticks;
   private int startFood = -1;

   Eater() {
      this(Eater::edible);
   }

   Eater(Predicate<class_1799> food) {
      this.food = food;
   }

   static boolean hungry(class_746 player) {
      return player.method_7344().method_7586() <= 14 || player.method_6032() < player.method_6063() && player.method_7344().method_7586() < 18;
   }

   static boolean hasFood(class_746 player) {
      return Hands.has(player, Eater::edible);
   }

   static boolean edible(class_1799 stack) {
      class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
      return food != null && food.comp_2491() > 0
         ? !stack.method_31574(class_1802.field_8233)
            && !stack.method_31574(class_1802.field_8511)
            && !stack.method_31574(class_1802.field_8680)
            && !stack.method_31574(class_1802.field_8635)
            && !stack.method_31574(class_1802.field_8323)
            && !stack.method_31574(class_1802.field_8766)
            && !stack.method_31574(class_1802.field_8367)
            && !stack.method_31574(class_1802.field_8463)
            && !stack.method_31574(class_1802.field_8726)
         : false;
   }

   Eater.Result tick(class_310 mc) {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1761 != null) {
         if (this.startFood < 0) {
            this.startFood = player.method_7344().method_7586();
         }

         if (player.method_7344().method_7586() > this.startFood) {
            this.release(mc);
            return Eater.Result.DONE;
         } else if (++this.ticks > 70) {
            this.release(mc);
            return Eater.Result.FAILED;
         } else if (!Hands.hold(mc, player, this.food)) {
            return Eater.Result.RUNNING;
         } else {
            Aim.turn(mc, player.method_36454(), -75.0F, 22.0F);
            mc.field_1690.field_1904.method_23481(player.method_36455() < -60.0F);
            return Eater.Result.RUNNING;
         }
      } else {
         return Eater.Result.FAILED;
      }
   }

   void release(class_310 mc) {
      mc.field_1690.field_1904.method_23481(false);
   }

   @Environment(EnvType.CLIENT)
   static enum Result {
      RUNNING,
      DONE,
      FAILED;
   }
}
