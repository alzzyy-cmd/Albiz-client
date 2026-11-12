package net.albiz.client.module.impl.combat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_2596;
import net.minecraft.class_2664;
import net.minecraft.class_2743;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class KnockbackDelayModule extends ClientModule {
   private static final double FIGHT_RANGE = 6.0;
   private final NumberSetting chance = this.addSetting("chance", new NumberSetting("Chance (%)", 40.0F, 0.0F, 100.0F).step(5.0F));
   private final NumberSetting airMin = this.addSetting("air-min", new NumberSetting("Air Delay Min (ms)", 50.0F, 0.0F, 500.0F).step(10.0F));
   private final NumberSetting airMax = this.addSetting("air-max", new NumberSetting("Air Delay Max (ms)", 100.0F, 0.0F, 500.0F).step(10.0F));
   private final NumberSetting groundMin = this.addSetting("ground-min", new NumberSetting("Ground Delay Min (ms)", 200.0F, 0.0F, 1000.0F).step(10.0F));
   private final NumberSetting groundMax = this.addSetting("ground-max", new NumberSetting("Ground Delay Max (ms)", 250.0F, 0.0F, 1000.0F).step(10.0F));
   private final BooleanSetting waterCheck = this.addSetting("water-check", new BooleanSetting("Not In Water", false));
   private final BooleanSetting onlyFights = this.addSetting("only-fights", new BooleanSetting("Only Near Enemies", true));
   private final class_310 mc = class_310.method_1551();
   private final Deque<KnockbackDelayModule.Held> held = new ArrayDeque<>();
   private class_634 heldFor;
   private volatile boolean fightNear;

   public KnockbackDelayModule() {
      super(
         "knockback-delay",
         "Knockback Delay",
         "Takes some hits' knockback a random moment late: a little in the air, longer on the ground.",
         ModuleCategory.COMBAT
      );
   }

   @Override
   protected void onEnable() {
      synchronized (this.held) {
         this.held.clear();
      }

      this.on(TickEvent.TYPE, Stage.PRE, event -> {
         this.fightNear = this.mc.field_1724 != null && this.enemyNear(this.mc.field_1724);
         this.applyDue(false);
      });
   }

   @Override
   protected void onDisable() {
      this.mc.execute(() -> this.applyDue(true));
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveNettyEvent event) {
      class_746 player = this.mc.field_1724;
      if (player != null && pushesMe(event.packet, player)) {
         boolean waiting;
         synchronized (this.held) {
            waiting = !this.held.isEmpty();
         }

         if (waiting || this.delays(player)) {
            boolean ground = player.method_24828();
            long wait = waiting ? 0L : randomBetween(ground ? this.groundMin.get() : this.airMin.get(), ground ? this.groundMax.get() : this.airMax.get());
            synchronized (this.held) {
               if (this.held.isEmpty()) {
                  this.heldFor = player.field_3944;
               }

               long due = Math.max(System.currentTimeMillis() + wait, this.held.isEmpty() ? 0L : this.held.peekLast().due());
               this.held.addLast(new KnockbackDelayModule.Held(event.packet, due));
            }

            event.setCancelled(true);
         }
      }
   }

   private static boolean pushesMe(class_2596<?> packet, class_746 player) {
      return packet instanceof class_2743 motion
         ? motion.method_11818() == player.method_5628()
         : packet instanceof class_2664 explode && explode.comp_2884().isPresent();
   }

   private boolean delays(class_746 player) {
      if (this.waterCheck.get() && player.method_5799()) {
         return false;
      } else {
         return this.onlyFights.get() && !this.fightNear ? false : ThreadLocalRandom.current().nextDouble(100.0) < this.chance.get();
      }
   }

   private boolean enemyNear(class_746 player) {
      for (class_1657 other : player.method_73183().method_18456()) {
         if (other != player && other.method_5739(player) <= 6.0 && MeleeAttacks.isEnemy(player, other)) {
            return true;
         }
      }

      return false;
   }

   private static long randomBetween(float a, float b) {
      long low = Math.round(Math.min(a, b));
      long high = Math.round(Math.max(a, b));
      return high <= low ? low : ThreadLocalRandom.current().nextLong(low, high + 1L);
   }

   private void applyDue(boolean all) {
      class_634 connection = this.mc.method_1562();
      long now = System.currentTimeMillis();

      while (true) {
         KnockbackDelayModule.Held next;
         synchronized (this.held) {
            next = this.held.peekFirst();
            if (next == null || !all && next.due() > now) {
               return;
            }

            this.held.pollFirst();
            if (connection == null || connection != this.heldFor) {
               this.held.clear();
               return;
            }
         }

         ((net.minecraft.class_2596)next.packet()).method_65081(connection);
      }
   }

   @Environment(EnvType.CLIENT)
   private record Held(class_2596<?> packet, long due) {
   }
}
