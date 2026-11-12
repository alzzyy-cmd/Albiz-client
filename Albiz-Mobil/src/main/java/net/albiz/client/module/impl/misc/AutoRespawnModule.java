// Behaviour adapted from SoberClient (dev.sober.modules.AutoRespawn), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.module.impl.misc;

import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.port.sober.util.Chat;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_418;

/** Presses "Respawn" on the death screen after a configurable delay and can note where you died. */
public final class AutoRespawnModule extends ClientModule {
   private final NumberSetting delay = this.addSetting("delay", new NumberSetting("Respawn Delay Ticks", 0.0F, 0.0F, 60.0F).step(1.0F));
   private final BooleanSetting coords = this.addSetting("death-coords", new BooleanSetting("Death Coords", true));
   private int waited;
   private boolean dead;

   public AutoRespawnModule() {
      super("auto-respawn", "Auto Respawn", "Respawns you right away when you die.", ModuleCategory.MISC);
   }

   @Override
   protected void onEnable() {
      this.waited = 0;
      this.dead = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         this.waited = 0;
         this.dead = false;
         return;
      }

      if (!(mc.field_1755 instanceof class_418)) {
         this.waited = 0;
         this.dead = false;
         return;
      }

      if (!this.dead) {
         this.dead = true;
         if (this.coords.get()) {
            class_2338 pos = mc.field_1724.method_24515();
            Chat.info("Auto Respawn", AlbizI18n.display("You died at") + " " + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260());
         }
      }

      if (this.waited++ < this.delay.getInt()) {
         return;
      }

      mc.field_1724.method_7331();
      mc.method_1507(null);
   }
}
