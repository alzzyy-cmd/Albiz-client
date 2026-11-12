package net.albiz.client.module.impl.combat;

import java.util.ArrayDeque;
import java.util.Deque;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.PacketSendEvent;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ModeSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10370;
import net.minecraft.class_10371;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2793;
import net.minecraft.class_2797;
import net.minecraft.class_2805;
import net.minecraft.class_2811;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_2820;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2840;
import net.minecraft.class_2846;
import net.minecraft.class_2863;
import net.minecraft.class_2873;
import net.minecraft.class_2877;
import net.minecraft.class_2879;
import net.minecraft.class_2885;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_7472;
import net.minecraft.class_9449;

@Environment(EnvType.CLIENT)
public final class FakeLagModule extends ClientModule {
   private static final String DYNAMIC = "Dynamic";
   private static final String ALWAYS = "Always";
   private static final double AIM_CONE_DEGREES = 30.0;
   private static final double TOWARDS = 0.02;
   private static final int BOX_COLOR = -43691;
   private final ModeSetting mode = this.addSetting("mode", new ModeSetting("Mode", "Dynamic", "Dynamic", "Always"));
   private final NumberSetting delay = this.addSetting("delay", new NumberSetting("Delay (ms)", 100.0F, 10.0F, 1000.0F).step(10.0F));
   private final NumberSetting range = this.addSetting("range", new NumberSetting("Range", 6.0F, 3.0F, 12.0F).step(0.5F));
   private final BooleanSetting render = this.addSetting("render", new BooleanSetting("Show Server Position", true));
   private final class_310 mc = class_310.method_1551();
   private final Deque<FakeLagModule.Held> held = new ArrayDeque<>();
   private boolean releasing;
   private class_634 heldFor;
   private volatile class_243 serverFeet;
   private volatile boolean lagNow;

   public FakeLagModule() {
      super(
         "fake-lag",
         "Fake Lag",
         "Holds your outgoing packets for a moment so the server sees you a little behind. Dynamic: only while you walk at the player you aim at.",
         ModuleCategory.COMBAT
      );
      this.range.visibleWhen(() -> this.mode.is("Dynamic"));
   }

   @Override
   protected void onEnable() {
      synchronized (this.held) {
         this.held.clear();
      }

      this.serverFeet = null;
      this.on(TickEvent.TYPE, Stage.PRE, event -> this.tick());
      this.on(Render3DEvent.TYPE, Stage.POST, event -> this.render());
   }

   @Override
   protected void onDisable() {
      this.lagNow = false;
      this.releaseAll();
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      if (!this.releasing) {
         class_2596<?> packet = event.packet;
         if (this.lagNow && !acts(packet)) {
            synchronized (this.held) {
               if (this.held.isEmpty()) {
                  this.heldFor = this.mc.method_1562();
               }

               this.held.addLast(new FakeLagModule.Held(packet, System.currentTimeMillis()));
            }

            event.setCancelled(true);
         } else {
            this.releaseAll();
            this.track(packet);
         }
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveNettyEvent event) {
      if (event.packet instanceof class_2708) {
         this.mc.execute(this::releaseAll);
      }
   }

   private void tick() {
      if (this.mc.method_1562() != null && this.mc.field_1724 != null) {
         this.lagNow = this.lagging();
         if (!this.lagNow) {
            this.releaseAll();
         } else {
            long due = System.currentTimeMillis() - Math.round(this.delay.get());

            while (true) {
               FakeLagModule.Held next;
               synchronized (this.held) {
                  next = this.held.peekFirst();
                  if (next == null || next.at() > due) {
                     return;
                  }

                  this.held.pollFirst();
               }

               this.send(next.packet());
            }
         }
      } else {
         this.lagNow = false;
         this.drop();
      }
   }

   private boolean lagging() {
      class_746 player = this.mc.field_1724;
      return player != null && this.mc.method_1562() != null && player.method_5805() ? this.mode.is("Always") || this.walkingAtTarget(player) : false;
   }

   private boolean walkingAtTarget(class_746 player) {
      class_243 eye = player.method_33571();
      class_243 look = player.method_5828(1.0F);
      class_243 motion = player.method_18798();
      double reach = this.range.get();

      for (class_1657 other : this.mc.field_1687.method_18456()) {
         if (other != player && MeleeAttacks.isEnemy(player, other) && !(other.method_5739(player) > reach)) {
            class_243 to = other.method_5829().method_1005().method_1020(eye).method_1029();
            if (!(Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, look.method_1026(to))))) > 30.0)
               && motion.field_1352 * to.field_1352 + motion.field_1350 * to.field_1350 > 0.02) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean acts(class_2596<?> packet) {
      return packet instanceof class_2824
         || packet instanceof class_2885
         || packet instanceof class_2886
         || packet instanceof class_2846
         || packet instanceof class_2879
         || packet instanceof class_2813
         || packet instanceof class_2811
         || packet instanceof class_2815
         || packet instanceof class_2797
         || packet instanceof class_7472
         || packet instanceof class_9449
         || packet instanceof class_2805
         || packet instanceof class_2877
         || packet instanceof class_2820
         || packet instanceof class_2840
         || packet instanceof class_2863
         || packet instanceof class_2873
         || packet instanceof class_10370
         || packet instanceof class_10371
         || packet instanceof class_2793;
   }

   private void releaseAll() {
      while (true) {
         FakeLagModule.Held next;
         synchronized (this.held) {
            next = this.held.pollFirst();
         }

         if (next == null) {
            return;
         }

         this.send(next.packet());
      }
   }

   private void drop() {
      synchronized (this.held) {
         this.held.clear();
      }

      this.serverFeet = null;
   }

   private void send(class_2596<?> packet) {
      class_634 connection = this.mc.method_1562();
      if (connection != null && (connection == this.heldFor || this.heldFor == null)) {
         this.releasing = true;

         try {
            connection.method_52787(packet);
         } finally {
            this.releasing = false;
         }

         this.track(packet);
      } else {
         this.drop();
      }
   }

   private void track(class_2596<?> packet) {
      if (packet instanceof class_2828 move && move.method_36171() && this.mc.field_1724 != null) {
         this.serverFeet = new class_243(
            move.method_12269(this.mc.field_1724.method_23317()),
            move.method_12268(this.mc.field_1724.method_23318()),
            move.method_12274(this.mc.field_1724.method_23321())
         );
      }
   }

   private void render() {
      class_746 player = this.mc.field_1724;
      class_243 feet = this.serverFeet;
      boolean holding;
      synchronized (this.held) {
         holding = !this.held.isEmpty();
      }

      if (this.render.get() && player != null && feet != null && holding) {
         double half = player.method_17681() / 2.0;
         class_12180.method_75541(
            new class_238(
               feet.field_1352 - half,
               feet.field_1351,
               feet.field_1350 - half,
               feet.field_1352 + half,
               feet.field_1351 + player.method_17682(),
               feet.field_1350 + half
            ),
            class_12179.method_75536(-43691, 2.0F)
         );
      }
   }

   @Environment(EnvType.CLIENT)
   private record Held(class_2596<?> packet, long at) {
   }
}
