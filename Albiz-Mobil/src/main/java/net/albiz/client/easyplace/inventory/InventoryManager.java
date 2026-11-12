package net.albiz.client.easyplace.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class InventoryManager {
   private final class_310 mc;
   private final ServerRangeInventorySource serverRange;

   public InventoryManager(class_310 mc) {
      this.mc = mc;
      this.serverRange = new ServerRangeInventorySource(mc);
   }

   public InventoryPickResult prepare(InventoryPickSourceMode source, class_1792 item, long tick, boolean driving) {
      return switch (source) {
         case SERVER_RANGE -> this.serverRange.prepare(item, tick, driving);
         case HOTBAR_ONLY -> this.hotbarOnly(item);
      };
   }

   private InventoryPickResult hotbarOnly(class_1792 item) {
      if (this.mc.field_1724 == null) {
         return InventoryPickResult.missing(item);
      } else {
         for (int slot = 0; slot < 9; slot++) {
            if (this.mc.field_1724.method_31548().method_5438(slot).method_7909() == item) {
               return InventoryPickResult.ready(item, null, slot);
            }
         }

         return InventoryPickResult.missing(item);
      }
   }

   public void reset() {
      this.serverRange.reset();
   }
}
