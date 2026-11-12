package net.albiz.client.module.impl.combat.bot;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.CombatMacroLock;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ModeSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.util.pvp.InputControl;
import net.albiz.client.util.pvp.Rotation;
import net.albiz.client.util.pvp.ServerRotation;
import net.albiz.client.util.pvp.TargetingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_640;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class SwordBotModule extends ClientModule {
   private static final String PATIENT = "Patient";
   private static final String AGGRESSIVE = "Aggressive";
   private static final Predicate<class_1799> GAPPLE = stack -> stack.method_31574(class_1802.field_8463) || stack.method_31574(class_1802.field_8367);
   private static final double HIT_RANGE = 3.0;
   private static final double CHASE_RANGE = 6.0;
   private static final int RESET_WINDOW = 3;
   private final ModeSetting style = this.addSetting("style", new ModeSetting("Style", "Patient", "Patient", "Aggressive"));
   private final NumberSetting range = this.addSetting("range", new NumberSetting("Fight Range", 16.0F, 6.0F, 32.0F).step(1.0F));
   private final NumberSetting turnSpeed = this.addSetting("turn-speed", new NumberSetting("Turn Speed", 1.0F, 0.5F, 1.5F).step(0.05F));
   private final BooleanSetting strafe = this.addSetting("strafe", new BooleanSetting("Strafe", true));
   private final BooleanSetting crits = this.addSetting("crits", new BooleanSetting("Jump Crits", true));
   private final BooleanSetting jumpReset = this.addSetting("jump-reset", new BooleanSetting("Jump Reset", true));
   private final NumberSetting eatBelow = this.addSetting("eat-below", new NumberSetting("Gapple Below HP", 10.0F, 0.0F, 20.0F).step(1.0F));
   private final class_310 mc = class_310.method_1551();
   private final BotBody body = new BotBody();
   private class_1657 fought;
   private double closing;
   private double lastTheirReach = Double.NaN;
   private double jitter;
   private int tapTicks;
   private boolean tapBack;
   private boolean critJump;
   private int landedTicks;
   private int resetUntil;
   private int lastHurt;
   private boolean eating;

   public SwordBotModule() {
      super(
         "sword-bot",
         "Sword PvP Bot",
         "Fights the nearest enemy player with a sword or axe: keeps out of their reach while charging, times crits, strafes and eats gapples.",
         ModuleCategory.COMBAT
      );
   }

   @Override
   protected void onEnable() {
      this.body.forget();
      this.newFight(null);
      this.eating = false;
      this.on(TickEvent.TYPE, Stage.PRE, event -> this.tick());
   }

   @Override
   protected void onDisable() {
      this.body.release(this.mc);
      this.body.forget();
      CombatMacroLock.release(this);
   }

   private void newFight(class_1657 target) {
      this.fought = target;
      this.closing = 0.0;
      this.lastTheirReach = Double.NaN;
      this.jitter = randomJitter();
      this.tapTicks = 0;
      this.critJump = false;
      this.resetUntil = 0;
   }

   private void tick() {
      class_746 player = this.mc.field_1724;
      this.body.release(this.mc);
      if (player != null && this.mc.field_1687 != null && MeleeAttacks.canAct(this.mc) && !player.method_29504()) {
         class_1657 target = this.body.pick(this.mc, player, this.range.get());
         int tick = this.body.tick();
         if (target != this.fought) {
            if (target != null) {
               ModuleChat.send(this, ModuleChat.body().text("Fighting ").name(target.method_5477().getString()).text("."));
            } else if (this.fought != null) {
               ModuleChat.send(this, ModuleChat.body().text("No enemy in range, waiting."));
            }

            this.newFight(target);
         }

         if (target != null) {
            this.body.face(this.mc, player, target, this.turnSpeed.get());
            boolean low = player.method_6032() <= this.eatBelow.get() && player.method_6067() < 4.0F;
            if ((low || this.eating && player.method_6115()) && this.body.use(this.mc, player, GAPPLE)) {
               this.eating = true;
               this.body.backOff(this.mc, player);
            } else {
               this.eating = false;
               if (!player.method_6115()) {
                  holdWeapon(player);
                  class_243 eye = player.method_33571();
                  double myReach = eye.method_1022(TargetingUtils.closestPoint(eye, target.method_5829()));
                  class_243 theirEye = target.method_33571();
                  double theirReach = theirEye.method_1022(TargetingUtils.closestPoint(theirEye, player.method_5829()));
                  if (!Double.isNaN(this.lastTheirReach)) {
                     this.closing = this.closing * 0.5 + (this.lastTheirReach - theirReach) * 0.5;
                  }

                  this.lastTheirReach = theirReach;
                  int ready = MeleeAttacks.ticksToFull(player);
                  float charge = MeleeAttacks.charge(player);
                  double facing = facingAngle(target, player);
                  class_243 away = new class_243(target.method_23317() - player.method_23317(), 0.0, target.method_23321() - player.method_23321())
                     .method_1029();
                  class_243 theirMotion = target.method_73189().method_1023(target.field_6014, target.field_6036, target.field_5969);
                  double fleeing = theirMotion.field_1352 * away.field_1352 + theirMotion.field_1350 * away.field_1350;
                  boolean iframes = target.field_6235 > 1;
                  boolean shielded = target.method_6039();
                  boolean threatened = theirReach <= 3.15 && facing <= 40.0;
                  boolean close = myReach <= 3.0;
                  double enemyReach = 3.35
                     + 0.12 * class_3532.method_15340(Math.round(ping(player) / 50.0F), 0, 3)
                     + 2.5 * class_3532.method_15350(this.closing, 0.0, 0.35)
                     - (this.style.is("Aggressive") ? 0.2 : 0.0);
                  double gap = ready <= 0
                     ? 2.9
                     : (ready <= 2 ? 3.08 + 0.1 * (ready - 1) + this.jitter : Math.max(enemyReach + this.jitter, 2.9 + 0.26 * ready));
                  if (facing < 8.0 && theirReach < 4.2) {
                     this.body.flipSide();
                  }

                  double side = this.strafe.get() ? 1.0 : 0.0;
                  if (player.field_6235 > this.lastHurt && player.field_6235 >= 8) {
                     this.resetUntil = tick + 3;
                  }

                  this.lastHurt = player.field_6235;
                  if (this.tapTicks > 0) {
                     this.tapTicks--;
                     this.body.drive(this.mc, player, this.tapBack ? -1 : 0, side * (this.tapBack ? 0.8 : 1.0), false, false);
                  } else if ((!(myReach > 6.0) || !(myReach > gap + 1.0)) && !(Math.abs(target.method_23318() - player.method_23318()) > 2.5)) {
                     if (this.critJump) {
                        if (player.method_24828()) {
                           if (++this.landedTicks >= 2) {
                              this.critJump = false;
                           }
                        } else {
                           this.landedTicks = 0;
                        }

                        if (this.critJump) {
                           boolean falling = ready <= 1 && player.method_18798().field_1351 < 0.1;
                           this.body.drive(this.mc, player, falling ? 0 : 1, falling ? side * 0.35 : 0.0, !falling, false);
                           if (ready <= 0 && close && !iframes) {
                              InputControl.dropSprint();
                              this.strike(player, target, 0.95F, true);
                           }

                           return;
                        }
                     }

                     if (this.crits.get() && player.method_24828() && !player.method_5799() && !threatened && this.critWindow(ready, myReach, facing, fleeing)) {
                        this.critJump = true;
                        this.landedTicks = 0;
                        this.body.drive(this.mc, player, 1, 0.0, true, true);
                     } else if (ready <= 0 && !shielded) {
                        if (iframes) {
                           this.body.drive(this.mc, player, myReach > 3.12 ? 1 : (myReach < 2.88 ? -1 : 0), side, false, false);
                        } else {
                           this.body.drive(this.mc, player, myReach < 1.6 ? -1 : 1, myReach <= 3.5 ? side * 0.35 : 0.0, myReach >= 1.6, false);
                           if (close) {
                              this.strike(player, target, 0.95F, false);
                           }
                        }
                     } else {
                        if (shielded) {
                           gap = Math.max(gap, enemyReach);
                        }

                        int band = theirReach > gap + 0.12 ? 1 : (theirReach < gap - 0.12 ? -1 : 0);
                        if (band < 0 && fleeing > 0.15) {
                           band = 0;
                        }

                        boolean counter = threatened && close && charge >= 0.6F && !iframes && !shielded;
                        boolean resetting = this.jumpReset.get() && tick < this.resetUntil && player.method_24828() && !player.method_5799();
                        if (resetting) {
                           this.body.drive(this.mc, player, 1, 0.0, true, true);
                        } else if (ready <= 2 && !counter && this.strafe.get() && !shielded) {
                           this.body.drive(this.mc, player, 1, 1.0, true, false);
                        } else {
                           double amount = !this.strafe.get() ? 0.0 : (band == 0 ? 1.0 : (band < 0 ? 0.8 : 0.5));
                           this.body.drive(this.mc, player, band, amount, band > 0, false);
                        }

                        if (counter) {
                           this.strike(player, target, 0.6F, false);
                        }
                     }
                  } else {
                     this.body.drive(this.mc, player, 1, 0.0, true, false);
                     if (close && ready <= 0 && !iframes && !shielded) {
                        this.strike(player, target, 0.95F, false);
                     }
                  }
               }
            }
         }
      } else {
         this.body.forget();
      }
   }

   private boolean critWindow(int ready, double myReach, double facing, double fleeing) {
      if (ready >= 7 && ready <= 10 && myReach >= 2.3 && myReach <= 5.0) {
         return true;
      } else {
         return ready >= 3 && ready <= 10 && !(myReach < 2.3) && !(myReach > 4.2)
            ? facing >= 100.0 && fleeing > 0.05 || this.style.is("Aggressive") && facing > 60.0
            : false;
      }
   }

   private void strike(class_746 player, class_1657 target, float charge, boolean crit) {
      if (!CombatMacroLock.heldByOther(this) && !MeleeAttacks.actedThisTick(player) && this.mc.field_1771 <= 0) {
         if (!(MeleeAttacks.charge(player) < charge) && MeleeAttacks.isMeleeWeapon(player.method_6047())) {
            if (!crit || MeleeAttacks.airborne(player) && !(player.method_18798().field_1351 >= 0.0)) {
               Rotation sent = ServerRotation.sent();
               if (sent != null) {
                  class_243 eye = player.method_33571();
                  double reach = Math.min(3.0, player.method_55755());
                  class_238 box = target.method_5829();
                  class_243 at = box.method_1006(eye)
                     ? eye
                     : (class_243)box.method_992(eye, eye.method_1019(class_243.method_1030(sent.pitch(), sent.yaw()).method_1021(reach))).orElse(null);
                  if (at != null && MeleeAttacks.lineIsClear(player, eye, at)) {
                     if (MeleeAttacks.attack(this.mc, new class_3966(target, at))) {
                        MeleeAttacks.claimTick(this.mc);
                        this.jitter = randomJitter();
                        this.tapTicks = 1;
                        this.tapBack = player.method_33571().method_1022(TargetingUtils.closestPoint(eye, box)) < 2.3;
                        if (this.tapBack) {
                           this.tapTicks = 2;
                        }

                        this.critJump = false;
                     }
                  }
               }
            }
         }
      }
   }

   private static double facingAngle(class_1657 target, class_746 self) {
      class_243 look = class_243.method_1030(target.method_36455(), target.method_5791());
      class_243 toMe = self.method_73189().method_1031(0.0, 1.1, 0.0).method_1020(target.method_33571()).method_1029();
      return Math.toDegrees(Math.acos(class_3532.method_15350(look.method_1026(toMe), -1.0, 1.0)));
   }

   private static int ping(class_746 player) {
      class_640 info = player.field_3944.method_2871(player.method_5667());
      return info == null ? 0 : Math.max(0, info.method_2959());
   }

   private static double randomJitter() {
      return ThreadLocalRandom.current().nextDouble(-0.06, 0.06);
   }

   private static void holdWeapon(class_746 player) {
      class_1661 inventory = player.method_31548();
      if (!MeleeAttacks.isMeleeWeapon(player.method_6047())) {
         int best = -1;
         double most = 0.0;

         for (int i = 0; i < 9; i++) {
            class_1799 stack = inventory.method_5438(i);
            if (MeleeAttacks.isMeleeWeapon(stack)) {
               double damage = MeleeAttacks.attackDamage(stack);
               if (best < 0 || damage > most) {
                  best = i;
                  most = damage;
               }
            }
         }

         if (best >= 0) {
            inventory.method_61496(best);
         }
      }
   }
}
