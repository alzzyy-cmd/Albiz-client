package net.albiz.client.easyplace.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;

@Environment(EnvType.CLIENT)
public interface InventoryPickSource {
   InventoryPickResult prepare(class_1792 var1, long var2);

   void reset();
}
