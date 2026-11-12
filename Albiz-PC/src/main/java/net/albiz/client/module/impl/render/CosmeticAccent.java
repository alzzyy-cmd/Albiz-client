package net.albiz.client.module.impl.render;

import net.albiz.client.config.ThemeStore;
import net.albiz.client.gui.ThemeTint;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class CosmeticAccent {
   private CosmeticAccent() {
   }

   static int argb() {
      HudModule hud = ModuleRegistry.get(HudModule.class);
      int argb = hud != null && hud.customAccent() ? hud.accentArgb() : ThemeStore.get().accent.argb();
      return ThemeTint.accent(argb) | 0xFF000000;
   }
}
