package net.albiz.client.module.impl.misc;

import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TridentUtilModule extends ClientModule {
   private final NumberSetting chargeScale = this.addSetting("scale", new NumberSetting("Charge Scale", 0.0F, 0.0F, 1.0F).step(0.1F));
   private final NumberSetting riptideBoost = this.addSetting("riptide-boost", new NumberSetting("Riptide Boost", 1.0F, 1.0F, 10.0F).step(0.1F));
   private final BooleanSetting noWater = this.addSetting("no-water", new BooleanSetting("No Water", true));

   public TridentUtilModule() {
      super("trident-util", "Better Trident", "Use tridents out of water and charge them faster.", ModuleCategory.MOVEMENT);
   }

   private static TridentUtilModule active() {
      TridentUtilModule module = ModuleRegistry.get(TridentUtilModule.class);
      return module != null && module.isEnabled() ? module : null;
   }

   public static double riptideMultiplier() {
      TridentUtilModule module=active();return module==null?1:module.riptideBoost.get();
   }

   public static boolean allowOutOfWater() {
      TridentUtilModule module = active();
      return module != null && module.noWater.get();
   }

   public static int minChargeTicks(int vanilla) {
      TridentUtilModule module = active();
      return module == null ? vanilla : Math.max(1, Math.round(vanilla * module.chargeScale.get()));
   }
}
