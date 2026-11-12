package net.albiz.client.module.impl.combat.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.albiz.client.easyplace.inventory.InventoryCalm;
import net.albiz.client.module.impl.combat.AutoInvTotemModule;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
final class BotKit {
   private static final int CLICK_GAP = 4;
   private int lastClick = -4;

   boolean needed(class_746 player, boolean anchors) {
      return next(player, wants(anchors)) != null;
   }

   boolean refill(class_310 mc, class_746 player, boolean anchors) {
      if (mc.field_1761 != null
         && mc.field_1755 == null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && !AutoInvTotemModule.busy()
         && !player.method_6115()) {
         int[] move = next(player, wants(anchors));
         if (move == null) {
            return false;
         } else {
            int tick = player.field_6012;
            if (InventoryCalm.settle(mc, player) && (tick < this.lastClick || tick - this.lastClick >= 4)) {
               mc.field_1761.method_2906(player.field_7498.field_7763, move[0], move[1], class_1713.field_7791, player);
               player.field_3944.method_52787(new class_2815(player.field_7498.field_7763));
               MeleeAttacks.noteAction(player);
               this.lastClick = tick;
               return true;
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static List<BotKit.Want> wants(boolean anchors) {
      List<BotKit.Want> wants = new ArrayList<>();
      wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8301), 16, 32));
      wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8281), 8, 16));
      wants.add(new BotKit.Want(MeleeAttacks::isMeleeWeapon, 1, 1));
      wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8367) || stack.method_31574(class_1802.field_8463), 1, 1));
      wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8634), 1, 1));
      wants.add(new BotKit.Want(stack -> stack.method_31573(class_3489.field_42614), 1, 1));
      wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8287), 1, 1));
      if (anchors) {
         wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_23141), 2, 4));
         wants.add(new BotKit.Want(stack -> stack.method_31574(class_1802.field_8801), 2, 4));
      }

      return wants;
   }

   private static int[] next(class_746 player, List<BotKit.Want> wants) {
      class_1661 inventory = player.method_31548();

      for (BotKit.Want want : wants) {
         int inHotbar = 0;
         int smallest = -1;

         for (int i = 0; i < 9; i++) {
            class_1799 stack = inventory.method_5438(i);
            if (want.item().test(stack)) {
               inHotbar += stack.method_7947();
               if (smallest < 0 || stack.method_7947() < inventory.method_5438(smallest).method_7947()) {
                  smallest = i;
               }
            }
         }

         if (inHotbar < want.low()) {
            int source = -1;

            for (int ix = 9; ix < 36; ix++) {
               class_1799 stack = inventory.method_5438(ix);
               if (want.item().test(stack) && (source < 0 || stack.method_7947() > inventory.method_5438(source).method_7947())) {
                  source = ix;
               }
            }

            if (source >= 0) {
               int bigger = inventory.method_5438(source).method_7947();
               if (smallest >= 0 && bigger > inHotbar && (inHotbar < want.topUp() || bigger >= inHotbar + 8)) {
                  return new int[]{source, smallest};
               }

               if (smallest < 0) {
                  int free = freeSlot(inventory, wants);
                  if (free >= 0) {
                     return new int[]{source, free};
                  }
               }
            }
         }
      }

      return null;
   }

   private static int freeSlot(class_1661 inventory, List<BotKit.Want> wants) {
      for (int i = 0; i < 9; i++) {
         if (inventory.method_5438(i).method_7960()) {
            return i;
         }
      }

      for (int ix = 8; ix >= 0; ix--) {
         class_1799 stack = inventory.method_5438(ix);
         if (!stack.method_31574(class_1802.field_8288)) {
            boolean used = false;

            for (BotKit.Want want : wants) {
               used |= want.item().test(stack);
            }

            if (!used) {
               return ix;
            }
         }
      }

      return -1;
   }

   @Environment(EnvType.CLIENT)
   private record Want(Predicate<class_1799> item, int low, int topUp) {
   }
}
