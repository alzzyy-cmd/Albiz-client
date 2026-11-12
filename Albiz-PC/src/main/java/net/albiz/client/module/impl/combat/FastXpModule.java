package net.albiz.client.module.impl.combat;

import net.albiz.client.mixin.AccessorCombatMinecraft;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.EventPriority;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.util.pvp.ServerRotation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class FastXpModule extends ClientModule {
   private final NumberSetting delay = this.addSetting("delay", new NumberSetting("Delay", 0.0F, 0.0F, 3.0F).step(1.0F));

   public FastXpModule() {
      super("fastxp", "FastXP", "Throws experience bottles faster while use is held.", ModuleCategory.COMBAT);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1755 == null && mc.field_1690.field_1904.method_1434()) {
         if (!player.method_6115() && holdsXp(player)) {
            AccessorCombatMinecraft access = (AccessorCombatMinecraft)mc;
            int wait = access.albiz$rightClickDelay();
            if (!ServerRotation.pending() && !MeleeAttacks.actedThisTick(player)) {
               if (wait > this.delay.getInt() + 1) {
                  access.albiz$setRightClickDelay(this.delay.getInt() + 1);
               }
            } else {
               access.albiz$setRightClickDelay(Math.max(wait, 2));
               class_304 use = mc.field_1690.field_1904;

               while (use.method_1436()) {
               }
            }
         }
      }
   }

   private static boolean holdsXp(class_746 player) {
      class_1799 main = player.method_6047();
      return main.method_31574(class_1802.field_8287)
         || player.method_6079().method_31574(class_1802.field_8287) && !(main.method_7909() instanceof class_1747);
   }
}
