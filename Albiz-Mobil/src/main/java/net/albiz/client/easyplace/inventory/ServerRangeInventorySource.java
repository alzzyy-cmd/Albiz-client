package net.albiz.client.easyplace.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_2815;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class ServerRangeInventorySource implements InventoryPickSource {
   private static final int SYNC_TIMEOUT_TICKS = 12;
   private static final int HOTBAR_SIZE = 9;
   private static final int MAIN_END = 35;
   private static final int OFFHAND_SLOT = 40;
   private final class_310 mc;
   private ServerRangeInventorySource.PendingPick pending;

   public ServerRangeInventorySource(class_310 mc) {
      this.mc = mc;
   }

   @Override
   public InventoryPickResult prepare(class_1792 item, long tick) {
      return this.prepare(item, tick, false);
   }

   public InventoryPickResult prepare(class_1792 item, long tick, boolean driving) {
      class_746 player = this.mc.field_1724;
      if (player != null && this.mc.field_1761 != null) {
         if (this.pending != null) {
            if (this.pending.item == item && tick - this.pending.startedTick <= 12L) {
               return this.pollPending();
            }

            this.pending = null;
         }

         if (player.method_6079().method_31574(item)) {
            return InventoryPickResult.ready(item, class_1268.field_5810, 40);
         } else {
            class_1661 inventory = player.method_31548();
            if (player.method_6047().method_31574(item)) {
               return InventoryPickResult.ready(item, class_1268.field_5808, inventory.method_67532());
            } else {
               int found = find(inventory, item);
               if (found < 0) {
                  return InventoryPickResult.missing(item);
               } else if (found < 9) {
                  return InventoryPickResult.ready(item, class_1268.field_5808, found);
               } else if (player.field_7512 != player.field_7498) {
                  return InventoryPickResult.missing(item);
               } else {
                  int destination = findHotbarDestination(inventory);
                  if (driving ? InventoryCalm.settle(this.mc, player) : InventoryCalm.idle(player)) {
                     this.mc.field_1761.method_2906(player.field_7498.field_7763, found, destination, class_1713.field_7791, player);
                     if (this.mc.field_1755 == null) {
                        player.field_3944.method_52787(new class_2815(player.field_7498.field_7763));
                     }

                     this.pending = new ServerRangeInventorySource.PendingPick(item, destination, tick);
                     return InventoryPickResult.waiting(item, destination);
                  } else {
                     return InventoryPickResult.waiting(item, destination);
                  }
               }
            }
         }
      } else {
         return InventoryPickResult.missing(item);
      }
   }

   private InventoryPickResult pollPending() {
      if (!this.mc.field_1724.method_31548().method_5438(this.pending.hotbarSlot).method_31574(this.pending.item)) {
         return InventoryPickResult.waiting(this.pending.item, this.pending.hotbarSlot);
      } else {
         InventoryPickResult result = InventoryPickResult.ready(this.pending.item, class_1268.field_5808, this.pending.hotbarSlot);
         this.pending = null;
         return result;
      }
   }

   private static int find(class_1661 inventory, class_1792 item) {
      for (int slot = 0; slot <= 35; slot++) {
         if (inventory.method_5438(slot).method_31574(item)) {
            return slot;
         }
      }

      return -1;
   }

   private static int findHotbarDestination(class_1661 inventory) {
      for (int slot = 0; slot < 9; slot++) {
         if (inventory.method_5438(slot).method_7960()) {
            return slot;
         }
      }

      return inventory.method_67532();
   }

   @Override
   public void reset() {
      this.pending = null;
   }

   @Environment(EnvType.CLIENT)
   private record PendingPick(class_1792 item, int hotbarSlot, long startedTick) {
   }
}
