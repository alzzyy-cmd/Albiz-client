package net.albiz.client.easyplace.builder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3481;
import net.minecraft.class_3489;
import net.minecraft.class_746;
import net.minecraft.class_8923;

@Environment(EnvType.CLIENT)
final class Scaffold {
   private Scaffold() {
   }

   static boolean item(class_1799 stack) {
      return glass(stack) || stack.method_31573(class_3489.field_15558);
   }

   static boolean glass(class_1799 stack) {
      return stack.method_7909() instanceof class_1747 item && item.method_7711() instanceof class_8923;
   }

   static boolean block(class_2680 state) {
      return state.method_26164(class_3481.field_15503) || state.method_26204() instanceof class_8923;
   }

   static int count(class_746 player) {
      int count = 0;
      class_1661 inventory = player.method_31548();

      for (int i = 0; i < 36; i++) {
         if (item(inventory.method_5438(i))) {
            count += inventory.method_5438(i).method_7947();
         }
      }

      if (item(player.method_6079())) {
         count += player.method_6079().method_7947();
      }

      return count;
   }

   static boolean has(class_746 player) {
      return Hands.has(player, Scaffold::item);
   }

   static boolean hold(class_310 mc, class_746 player) {
      if (Hands.has(player, stack -> stack.method_31574(class_1802.field_8280))) {
         return Hands.hold(mc, player, stack -> stack.method_31574(class_1802.field_8280));
      } else {
         return Hands.has(player, stack -> stack.method_31573(class_3489.field_15558))
            ? Hands.hold(mc, player, stack -> stack.method_31573(class_3489.field_15558))
            : Hands.hold(mc, player, Scaffold::glass);
      }
   }
}
