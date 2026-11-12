package net.albiz.client.easyplace.builder;

import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_8881;

@Environment(EnvType.CLIENT)
final class BlockUser {
   private static final int CLICK_LIMIT = 30;
   private static final int ANSWER_TICKS = 12;
   private static final int OPEN_TICKS = 40;
   private static final int SYNC_TICKS = 4;
   private final class_2338 pos;
   private final class_2680 expected;
   private final boolean[] disabledSlots;
   private Aim.Shot shot;
   private class_2680 before;
   private int phase;
   private int phaseTicks;
   private int clicks;
   private int rounds;
   private boolean lever;

   private BlockUser(class_2338 pos, class_2680 expected, boolean[] disabledSlots) {
      this.pos = pos;
      this.expected = expected;
      this.disabledSlots = disabledSlots;
   }

   static BlockUser adjust(class_2338 pos, class_2680 expected) {
      return new BlockUser(pos, expected, null);
   }

   static BlockUser crafter(class_2338 pos, boolean[] disabledSlots) {
      return new BlockUser(pos, null, disabledSlots);
   }

   static BlockUser lever(class_2338 pos, class_2680 expected) {
      BlockUser user = new BlockUser(pos, expected, null);
      user.lever = true;
      return user;
   }

   class_2338 pos() {
      return this.pos;
   }

   boolean crafter() {
      return this.disabledSlots != null;
   }

   BlockUser.Result tick(class_310 mc) {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1687 != null && mc.field_1761 != null) {
         this.phaseTicks++;
         class_2680 now = mc.field_1687.method_8320(this.pos);
         if (this.lever) {
            if (!now.method_28498(class_2741.field_12484)) {
               return BlockUser.Result.FAILED;
            }

            if (now.method_11654(class_2741.field_12484) == this.expected.method_11654(class_2741.field_12484)) {
               return BlockUser.Result.DONE;
            }
         } else if (this.disabledSlots == null && StateRules.done(now, this.expected)) {
            return BlockUser.Result.DONE;
         }

         switch (this.phase) {
            case 0:
               if (player.method_5715() || player.field_7512 != player.field_7498) {
                  return BlockUser.Result.RUNNING;
               }

               this.shot = Aim.at(mc, this.pos);
               if (this.shot == null) {
                  return BlockUser.Result.FAILED;
               }

               emptyHand(player);
               this.next(1);
               break;
            case 1:
               Aim.turn(mc, this.shot.yaw(), this.shot.pitch(), 22.0F);
               if (Aim.looking(player, this.shot.yaw(), this.shot.pitch()) && this.phaseTicks >= 2) {
                  this.before = mc.field_1687.method_8320(this.pos);
                  if (!mc.field_1761.method_2896(player, class_1268.field_5808, this.shot.hit()).method_23665()) {
                     return BlockUser.Result.FAILED;
                  }

                  player.method_6104(class_1268.field_5808);
                  this.clicks++;
                  this.next(this.disabledSlots == null ? 2 : 3);
                  break;
               }

               if (this.phaseTicks > 30) {
                  return BlockUser.Result.FAILED;
               }

               return BlockUser.Result.RUNNING;
            case 2:
               if (!mc.field_1687.method_8320(this.pos).equals(this.before) || this.phaseTicks > 12) {
                  if (this.clicks >= 30) {
                     return BlockUser.Result.FAILED;
                  }

                  this.next(0);
               }
               break;
            case 3:
               if (player.field_7512 instanceof class_8881 && this.phaseTicks >= 4) {
                  this.next(4);
               } else if (this.phaseTicks > 40) {
                  return BlockUser.Result.FAILED;
               }
               break;
            case 4:
               if (!(player.field_7512 instanceof class_8881 menu)) {
                  return BlockUser.Result.FAILED;
               }

               boolean var10 = false;

               for (int var11 = 0; var11 < 9; var11++) {
                  if (menu.method_54461(var11) != this.disabledSlots[var11] && !menu.method_7611(var11).method_7681()) {
                     boolean enable = !this.disabledSlots[var11];
                     menu.method_54458(var11, enable);
                     mc.field_1761.method_54634(var11, menu.field_7763, enable);
                     var10 = true;
                  }
               }

               this.next(var10 ? 5 : 6);
               break;
            case 5:
               if (this.phaseTicks >= 12) {
                  this.next(6);
               }
               break;
            case 6:
               boolean right = player.field_7512 instanceof class_8881 menu && this.matches(menu);
               player.method_7346();
               if (right) {
                  return BlockUser.Result.DONE;
               }

               if (++this.rounds >= 2) {
                  return BlockUser.Result.FAILED;
               }

               this.next(0);
               break;
            default:
               return BlockUser.Result.FAILED;
         }

         return BlockUser.Result.RUNNING;
      } else {
         return BlockUser.Result.FAILED;
      }
   }

   void close(class_310 mc) {
      class_746 player = mc.field_1724;
      if (player != null && this.disabledSlots != null && player.field_7512 instanceof class_8881) {
         player.method_7346();
      }
   }

   private static void emptyHand(class_746 player) {
      class_1661 inventory = player.method_31548();
      if (inventory.method_5438(inventory.method_67532()).method_7909() instanceof class_1747) {
         int fallback = -1;

         for (int slot = 0; slot < 9; slot++) {
            class_1799 stack = inventory.method_5438(slot);
            if (stack.method_7960()) {
               inventory.method_61496(slot);
               return;
            }

            if (fallback < 0 && !(stack.method_7909() instanceof class_1747)) {
               fallback = slot;
            }
         }

         if (fallback >= 0) {
            inventory.method_61496(fallback);
         }
      }
   }

   private boolean matches(class_8881 menu) {
      for (int slot = 0; slot < 9; slot++) {
         if (menu.method_54461(slot) != this.disabledSlots[slot]) {
            return false;
         }
      }

      return true;
   }

   private void next(int value) {
      this.phase = value;
      this.phaseTicks = 0;
   }

   @Environment(EnvType.CLIENT)
   static enum Result {
      RUNNING,
      DONE,
      FAILED;
   }
}
