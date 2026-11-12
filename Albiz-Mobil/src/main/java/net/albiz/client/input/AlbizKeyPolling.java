package net.albiz.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_3675;

@Environment(EnvType.CLIENT)
public final class AlbizKeyPolling {
   private AlbizKeyPolling() {
   }

   public static boolean isKeyDown(class_1041 window, int key) {
      if (window == null || !ModuleKeybinds.isBindable(key) || key >= ModuleKeybinds.MOUSE_OFFSET) return false;
      return class_3675.method_15987(window, key);
   }
}
