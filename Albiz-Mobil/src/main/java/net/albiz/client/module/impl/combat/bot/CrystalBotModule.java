package net.albiz.client.module.impl.combat.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.easyplace.builder.Aim;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.PacketAppliedEvent;
import net.albiz.client.event.impl.PacketReceiveEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.CombatMacroLock;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.combat.AnchorAuraModule;
import net.albiz.client.module.impl.combat.AutoCityModule;
import net.albiz.client.module.impl.combat.AutoInvTotemModule;
import net.albiz.client.module.impl.combat.AutoTotemModule;
import net.albiz.client.module.impl.combat.AutoTrapModule;
import net.albiz.client.module.impl.combat.CrystalAuraModule;
import net.albiz.client.module.impl.combat.anchor.AnchorGuard;
import net.albiz.client.module.impl.combat.crystal.Combo;
import net.albiz.client.module.impl.combat.crystal.CrystalDamage;
import net.albiz.client.module.impl.combat.crystal.Crystals;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.util.pvp.Rotation;
import net.albiz.client.util.pvp.RotationUtils;
import net.albiz.client.util.pvp.ServerRotation;
import net.albiz.client.util.pvp.TargetingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1684;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_2664;
import net.minecraft.class_2680;
import net.minecraft.class_2708;
import net.minecraft.class_2709;
import net.minecraft.class_2761;
import net.minecraft.class_310;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4969;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import net.minecraft.class_8143;
import net.minecraft.class_9334;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public final class CrystalBotModule extends ClientModule {
   private static final double MIN = 2.4;
   private static final double IDEAL = 3.2;
   private static final double MAX = 4.0;
   private static final Predicate<class_1799> GAPPLE = stack -> stack.method_31574(class_1802.field_8463) || stack.method_31574(class_1802.field_8367);
   private static final Predicate<class_1799> XP = stack -> stack.method_31574(class_1802.field_8287);
   private static final Predicate<class_1799> PEARL = stack -> stack.method_31574(class_1802.field_8634);
   private static final Predicate<class_1799> OBSIDIAN = stack -> stack.method_31574(class_1802.field_8281);
   private static final Predicate<class_1799> ANCHOR = stack -> stack.method_31574(class_1802.field_23141);
   private static final Predicate<class_1799> GLOWSTONE = stack -> stack.method_31574(class_1802.field_8801);
   private static final float ARMOR_LOW = 0.3F;
   private static final int RUN_HOLD = 40;
   private static final int POP_COVER = 40;
   private static final int LAUNCH_GAP = 80;
   private static final int LAUNCH_STALL = 20;
   private static final int LAUNCH_TIMEOUT = 16;
   private static final double LAUNCH_START = 3.9;
   private static final double LAUNCH_GIVE_UP = 4.1;
   private static final double LAUNCH_STEP_IN = 2.9;
   private static final int LAUNCH_WATCH = 8;
   private static final double LAUNCH_LIFT = 0.3;
   private static final double HIT_RANGE = 2.95;
   private static final int THROW_TIMEOUT = 14;
   private static final int PEARL_GAP = 30;
   private static final int SHIELD_GAP = 20;
   private static final int BREACH_WAIT = 40;
   private static final int BREACH_WAIT_CAMPER = 15;
   private static final int BREACH_GAP = 100;
   private static final double ESCAPE_FAR = 20.0;
   private static final double ESCAPE_NEAR = 12.0;
   private static final int SEARCH_GAP = 10;
   private static final double EAT_CLEAR = 8.0;
   private static final int FOLLOW_TICKS = 12;
   private static final int OPENER_WINDOW = 40;
   private static final int CAMP_MAX = 2;
   private static final int SPOT_SCAN_GAP = 5;
   private static final float SPOT_DANGER = 8.0F;
   private static final int WORKING = 0;
   private static final int PLACED = 1;
   private static final int REGROUP_TICKS = 300;
   private static final double REGROUP_RANGE = 64.0;
   private static final int BOX_AIM_TIMEOUT = 10;
   private static final float RETREAT_HEALTH = 6.0F;
   private static final int POP_STREAK = 3;
   private static final int POP_STREAK_GAP = 600;
   private static final int POP_RUN = 200;
   private static final double BLAST_WATCH = 7.0;
   private static final double BELOW_DROP = 3.0;
   private static final double BELOW_REACH = 8.0;
   private static final double DIG_DEEPEST = 6.0;
   private static final int BELOW_SETTLE = 20;
   private static final int DIG_TIMEOUT = 80;
   private static final int DIG_WALK = 40;
   private static final int DIG_MAX = 2;
   private static final int DIG_GAP = 100;
   private static final int DIG_DOWN_TICKS = 50;
   private static final int DIG_BASE_TICKS = 25;
   private static final float LEVEL_MIN_DAMAGE = 2.5F;
   private final NumberSetting range = this.addSetting("range", new NumberSetting("Fight Range", 16.0F, 8.0F, 32.0F).step(1.0F));
   private final BooleanSetting anchors = this.addSetting("anchors", new BooleanSetting("Use Anchors", true));
   private final BooleanSetting launches = this.addSetting("knock-up", new BooleanSetting("Knock-Up Hits", true));
   private final BooleanSetting breach = this.addSetting("break-holes", new BooleanSetting("Break Holes", true));
   private final BooleanSetting guardAnchors = this.addSetting("guard-anchors", new BooleanSetting("Guard Anchors", true));
   private final BooleanSetting surround = this.addSetting("surround", new BooleanSetting("Surround Self", true));
   private final NumberSetting eatBelow = this.addSetting("eat-below", new NumberSetting("Gapple Below HP", 14.0F, 0.0F, 20.0F).step(1.0F));
   private final BooleanSetting mendArmor = this.addSetting("mend-armor", new BooleanSetting("Mend Armor", true));
   private final BooleanSetting escape = this.addSetting("escape-pearls", new BooleanSetting("Escape Pearl", true));
   private final BooleanSetting chase = this.addSetting("chase-pearls", new BooleanSetting("Chase Pearls", true));
   private final BooleanSetting refill = this.addSetting("refill", new BooleanSetting("Refill Hotbar", true));
   private final BooleanSetting learn = this.addSetting("learn", new BooleanSetting("Learn Rivals", true));
   private final BooleanSetting dance = this.addSetting("strafe", new BooleanSetting("Strafe", true));
   private final class_310 mc = class_310.method_1551();
   private final BotBody body = new BotBody();
   private final BotKit kit = new BotKit();
   private final RivalBook book = new RivalBook();
   private final BotPause pause = new BotPause();
   private final AirTrack air = new AirTrack();
   private class_1657 challenger;
   private int challengerSince;
   private int attackedBy = -1;
   private int attackedAt = -1000;
   private double closest;
   private int closerAt;
   private StandPicker.Spot standing;
   private int standScanAt;
   private int defuseAt = -1000;
   private int retreatAt = -1000;
   private Boolean crystalWasOn;
   private Boolean anchorWasOn;
   private Boolean totemWasOn;
   private int popStreak;
   private boolean poppedOut;
   private class_2338 digAnchor;
   private boolean digPlaced;
   private boolean digAnnounced;
   private int digGuards;
   private int digUntil;
   private int digWalkUntil;
   private int digBlasts;
   private int digAfter;
   private int belowSince = -1;
   private int stepDigAfter;
   private int baseDigAfter;
   private int diggingAt = -100;
   private boolean eating;
   private boolean needsGapple;
   private boolean mending;
   private class_2338 boxingAt;
   private int boxingUntil;
   private int tick;
   private class_1657 fought;
   private CrystalBotModule.Stance stance = CrystalBotModule.Stance.HOLD;
   private int runUntil;
   private int ownPopAt = -1000;
   private CrystalBotModule.Throw throwing;
   private Rotation throwLook;
   private class_243 throwSpot;
   private double throwAway;
   private int throwUntil;
   private int pearlAt = -1000;
   private int searchAfter;
   private int theirPearl = -1;
   private int followedPearl = -1;
   private boolean launching;
   private int launchUntil;
   private int nextLaunch;
   private int launchedAt = -1;
   private double launchY;
   private double launchPeak;
   private int shieldAt = -1000;
   private class_2338 boxAiming;
   private int boxAimTicks;
   private class_2338 boxSkipped;
   private int boxSkippedUntil;
   private int followUntil;
   private float selfCapNow;
   private boolean followLaid = true;
   private class_2338 blocking;
   private int spotScanAt;
   private int cityFails;
   private int openerArm = -1;
   private double openerReach;
   private int openerAt;
   private float openerHealth;
   private int openerPops;
   private boolean cityStarted;
   private int breachAfter;
   private float lastTheirHealth;
   private int hurtAt;
   private int progressAt;
   private int theirPopAt = -1;
   private int holeSince = -1;
   private boolean lowSpell;
   private boolean pearledInSpell;

   public CrystalBotModule() {
      super(
         "crystal-bot",
         "Crystal PvP Bot",
         "Crystal-fights the nearest enemy player: steers the Crystal and Anchor Aura, knocks them up, breaks their hole, guards, eats, mends, refills and pearls.",
         ModuleCategory.COMBAT
      );
   }

   @Override
   protected void onEnable() {
      this.body.forget();
      this.eating = this.mending = this.launching = this.cityStarted = this.needsGapple = false;
      this.boxingAt = null;
      this.fought = null;
      this.throwing = null;
      this.tick = 0;
      this.runUntil = 0;
      this.nextLaunch = this.followUntil = this.boxSkippedUntil = 0;
      this.launchedAt = -1;
      this.ownPopAt = this.pearlAt = this.shieldAt = -1000;
      this.searchAfter = this.breachAfter = this.spotScanAt = this.standScanAt = this.stepDigAfter = this.baseDigAfter = 0;
      this.defuseAt = this.attackedAt = this.retreatAt = -1000;
      this.attackedBy = -1;
      this.challenger = null;
      this.standing = null;
      this.air.forget();
      this.pause.reset();
      this.crystalWasOn = this.anchorWasOn = this.totemWasOn = null;
      this.popStreak = 0;
      this.forgetDig();
      this.on(TickEvent.TYPE, Stage.PRE, event -> this.tick());
   }

   @Override
   protected void onDisable() {
      this.body.release(this.mc);
      this.body.forget();
      CombatMacroLock.release(this);
      this.restoreAuras();
      AutoCityModule city = ModuleRegistry.get(AutoCityModule.class);
      if (this.cityStarted && city != null && city.isEnabled()) {
         city.setEnabled(false);
      }

      this.cityStarted = false;
      this.book.save(true);
   }

   @EventHandler
   public void onPacketApplied(PacketAppliedEvent event) {
      class_746 player = this.mc.field_1724;
      if (player != null && this.mc.field_1687 != null) {
         if (event.packet instanceof class_2663 status && status.method_11470() == 35) {
            if (status.method_11469(this.mc.field_1687) == player) {
               this.popStreak = this.tick - this.ownPopAt <= 600 ? this.popStreak + 1 : 1;
               this.ownPopAt = this.tick;
            } else if (this.fought != null && status.method_11469(this.mc.field_1687) == this.fought) {
               this.theirPopAt = this.tick;
               this.openerPops++;
            }
         }
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         if (event.packet instanceof class_2708 moved) {
            class_243 to = moved.comp_3228().comp_3148();
            class_243 from = player.method_73189();
            Set<class_2709> relative = moved.comp_3229();
            this.pause
               .moved(
                  new class_243(
                     relative.contains(class_2709.field_12400) ? from.field_1352 + to.field_1352 : to.field_1352,
                     relative.contains(class_2709.field_12398) ? from.field_1351 + to.field_1351 : to.field_1351,
                     relative.contains(class_2709.field_12403) ? from.field_1350 + to.field_1350 : to.field_1350
                  ),
                  this.tick - this.pearlAt
               );
         } else if (event.packet instanceof class_2761 time) {
            this.pause.time(time.comp_3219());
         } else if (event.packet instanceof class_2664 blast) {
            this.air.blast(blast.comp_2883());
         } else if (event.packet instanceof class_8143 hurt && hurt.comp_1267() == player.method_5628() && hurt.comp_1269() >= 0) {
            this.attackedBy = hurt.comp_1269();
            this.attackedAt = this.tick;
         }
      }
   }

   private void tick() {
      this.tick++;
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      this.body.release(this.mc);
      if (player != null && level != null && MeleeAttacks.canAct(this.mc) && !player.method_29504()) {
         CombatMacroLock.release(this);
         this.keepTotem();
         this.body.stayGrounded(blastNear(level, player));
         if (this.learn.get()) {
            this.book.save(false);
         }

         BotPause.State state = this.pause.tick(this.mc, player);
         if (state == BotPause.State.STOP) {
            ModuleChat.send(this, ModuleChat.body().danger("The server keeps pulling us back").text(", stopping."));
            this.requestDisable();
         } else if (state != BotPause.State.RUN) {
            this.throwing = null;
            this.launching = this.eating = false;
            if (state == BotPause.State.HOLD) {
               CombatMacroLock.take(this);
            }
         } else {
            boolean regrouping = this.needsGapple || this.eating || this.tick - this.retreatAt < 300;
            class_1657 target = this.choose(level, player, this.body.pick(this.mc, player, this.range.get(), regrouping ? 64.0 : this.range.get() + 8.0));
            if (target != this.fought) {
               this.newFight(target);
            }

            boolean low = player.method_6032() <= this.eatAt() && player.method_6067() < 4.0F;
            if (player.method_6032() > this.eatAt() + 4.0F || player.method_6067() >= 4.0F) {
               this.needsGapple = false;
            }

            if (target == null) {
               this.launching = false;
               this.throwing = null;
               CombatMacroLock.release(this);
               if ((low || this.needsGapple || this.eating && player.method_6115()) && this.hands() && this.body.use(this.mc, player, GAPPLE)) {
                  this.eating = true;
               } else {
                  this.eating = false;
                  if (!this.refill.get() || !this.kit.refill(this.mc, player, this.anchors.get())) {
                     if (!this.mendIdle(player)) {
                        this.mending = false;
                     }
                  }
               }
            } else {
               RivalBook.Rival rival = this.learn.get() ? this.book.of(target.method_5667()) : null;
               this.watch(level, target, rival);
               if (feetY(player) < class_3532.method_15357(target.method_23318() + 0.01)) {
                  this.body.stayGrounded(true);
               }

               this.air.note(target, this.tick, rival);
               Combo.prefer(player, target);
               Combo.flight(player, target, ahead -> this.air.lead(ahead, this.tick));
               this.runAuras(level);
               CrystalBotModule.Stance was = this.stance;
               this.stance = this.stance(level, player, target, rival);
               if (this.stance == CrystalBotModule.Stance.RUN && was != CrystalBotModule.Stance.RUN) {
                  ModuleChat.send(
                     this,
                     this.poppedOut
                        ? ModuleChat.body().warn("3 totems popped").text(", getting away.")
                        : ModuleChat.body().warn("Losing").text(", getting away.")
                  );
               }

               this.poppedOut = false;
               this.steer(player, target, rival);
               double distance = player.method_5739(target);
               if (distance < this.closest - 0.5) {
                  this.closest = distance;
                  this.closerAt = this.tick;
               }

               if (this.throwing == null || !this.keepThrowing(player, target)) {
                  if (this.stance != CrystalBotModule.Stance.RUN || !this.getAway(level, player, target)) {
                     if (!this.getDown(level, player, target)) {
                        if (!this.guardAnchors.get() || !this.defuse(level, player) && !this.guard(level, player, target)) {
                           if (!this.launching || !this.launch(player, target)) {
                              if (this.followLaid || this.tick > this.followUntil || !this.followUp(level, player, target)) {
                                 boolean punish = target.method_6115()
                                    && target.method_6030().method_57826(class_9334.field_50075)
                                    && totems(player) >= 1
                                    && player.method_6032() > 8.0F;
                                 if (low && !punish) {
                                    this.needsGapple = true;
                                 }

                                 if (this.needsGapple || this.eating && player.method_6115()) {
                                    if (distance >= 8.0) {
                                       if (BotBody.hotbarSlot(player, GAPPLE) >= 0) {
                                          if (this.hands() && this.body.use(this.mc, player, GAPPLE)) {
                                             this.eating = true;
                                          }

                                          this.body.face(this.mc, player, target, 1.0F);
                                          this.body.backOff(this.mc, player);
                                          return;
                                       }
                                    } else if (this.critical(player) && BotBody.hotbarSlot(player, GAPPLE) >= 0) {
                                       this.eating = false;
                                       this.retreatAt = this.tick;
                                       if (this.pearlAway(level, player, target)) {
                                          return;
                                       }

                                       this.body.flee(this.mc, player, target);
                                       return;
                                    }
                                 }

                                 this.eating = false;
                                 if (this.cover(level, player, target, distance)) {
                                    this.body.face(this.mc, player, target, 1.0F);
                                 } else if (this.stance == CrystalBotModule.Stance.GUARD && this.closeSpot(level, player, target)) {
                                    this.body.face(this.mc, player, target, 1.0F);
                                 } else if (!this.breach.get() || !this.breakHole(level, player, target, rival)) {
                                    if (!this.mendArmor.get() || !(distance >= 12.0) || !armorLow(player) || !this.hands() || !this.throwXp(player)) {
                                       this.mending = false;
                                       if (!this.chase.get() || !this.chasePearl(level, player, target, rival)) {
                                          boolean calm = distance >= 8.0 || BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_8301)) < 0;
                                          if (this.refill.get()
                                             && calm
                                             && this.kit.needed(player, this.anchors.get())
                                             && this.hands()
                                             && this.kit.refill(this.mc, player, this.anchors.get())) {
                                             this.body.face(this.mc, player, target, 1.0F);
                                          } else if (!this.finish(player, target)) {
                                             if (!this.launches.get() || !this.startLaunch(player, target, rival)) {
                                                if (!this.digDown(level, player, target, distance) && !this.digBase(level, player, target, distance)) {
                                                   CombatMacroLock.release(this);
                                                   handBack(player);
                                                   this.body.face(this.mc, player, target, 1.0F);
                                                   if (!surrounded(level, player)) {
                                                      if (!this.stayLow(level, player, target, distance)) {
                                                         if (this.tick >= this.standScanAt) {
                                                            this.standScanAt = this.tick + 10;
                                                            this.standing = StandPicker.pick(level, player, target, 2.4, 3.2, 4.0, this.stance.cover);
                                                         }

                                                         if (this.standing == null || this.body.toward(this.mc, player, this.standing.center())) {
                                                            this.standing = null;
                                                            this.body.keepDistance(this.mc, player, target, 2.4, 3.2, 4.0, this.dance.get());
                                                         }
                                                      }
                                                   }
                                                } else {
                                                   this.body.face(this.mc, player, target, 1.0F);
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         this.body.forget();
         CombatMacroLock.release(this);
         this.throwing = null;
         this.launching = false;
      }
   }

   private class_1657 choose(class_638 level, class_746 player, class_1657 kept) {
      class_1657 best = null;
      double bestWorth = Double.NEGATIVE_INFINITY;

      for (class_1657 other : enemies(level, player, this.range.get())) {
         double worth = this.worth(player, other);
         if (worth > bestWorth) {
            best = other;
            bestWorth = worth;
         }
      }

      if (kept == null || !kept.method_5805() || this.fought == null) {
         this.challenger = null;
         if (best != null) {
            this.body.hold(best);
         }

         return best;
      } else if (best != null && best != kept) {
         boolean close = player.method_5739(kept) <= 6.0;
         if (bestWorth < this.worth(player, kept) + (close ? 8.0 : 2.0)) {
            this.challenger = null;
            return kept;
         } else if (best != this.challenger) {
            this.challenger = best;
            this.challengerSince = this.tick;
            return kept;
         } else if (this.tick - this.challengerSince < (close ? 20 : 6)) {
            return kept;
         } else {
            this.challenger = null;
            this.body.hold(best);
            return best;
         }
      } else {
         this.challenger = null;
         return kept;
      }
   }

   private double worth(class_746 player, class_1657 other) {
      double distance = player.method_5739(other);
      double near = distance <= 6.0 ? 1.0 : (distance >= 10.0 ? 0.0 : (10.0 - distance) / 4.0);
      double worth = 4.0 * near - 0.2 * distance + (20.0F - Math.min(20.0F, CrystalDamage.health(other))) / 5.0;
      if (!other.method_6079().method_31574(class_1802.field_8288) && !other.method_6047().method_31574(class_1802.field_8288)) {
         worth++;
      }

      if (armorLow(other)) {
         worth++;
      }

      if (other.method_5628() == this.attackedBy && this.tick - this.attackedAt <= 60 && distance <= 4.5) {
         worth += 4.0;
      }

      return worth;
   }

   private boolean finish(class_746 player, class_1657 target) {
      if (this.pause.slowServer() || target.field_6235 > 0 || reach(player, target) > 2.95 || CrystalDamage.health(target) > 3.0F) {
         return false;
      } else if (!MeleeAttacks.isMeleeWeapon(player.method_6047()) || MeleeAttacks.charge(player) < 0.9F) {
         return false;
      } else if (!this.hands()) {
         return true;
      } else {
         this.body.face(this.mc, player, target, 1.0F);
         this.strike(player, target);
         return true;
      }
   }

   private void newFight(class_1657 target) {
      if (this.fought != null && this.lowSpell && this.pearledInSpell && this.learn.get()) {
         this.book.of(this.fought.method_5667()).lowSpell(true);
      }

      this.fought = target;
      this.launching = false;
      this.launchedAt = this.theirPopAt = this.holeSince = -1;
      this.lowSpell = this.pearledInSpell = false;
      this.theirPearl = this.followedPearl = -1;
      this.openerArm = -1;
      this.closest = Double.MAX_VALUE;
      this.closerAt = this.tick;
      this.standing = null;
      this.followLaid = true;
      this.blocking = null;
      this.cityFails = 0;
      this.air.forget();
      this.forgetDig();
      this.progressAt = this.hurtAt = this.tick;
      this.lastTheirHealth = target == null ? 0.0F : CrystalDamage.health(target);
      if (target != null) {
         ModuleChat.send(this, ModuleChat.body().text("Fighting ").name(target.method_5477().getString()).text("."));
         if (this.learn.get()) {
            this.book.of(target.method_5667()).fought();
         }
      } else {
         ModuleChat.send(this, ModuleChat.body().text("No enemy in range, waiting."));
      }
   }

   private CrystalBotModule.Stance stance(class_638 level, class_746 player, class_1657 target, RivalBook.Rival rival) {
      int totems = totems(player);
      float health = player.method_6032() + player.method_6067();
      float risk = worstThreat(level, player, this.digAnchor) / Math.max(1.0F, health);
      int near = enemiesWithin(level, player, 8.0);
      boolean run = totems == 0 && near >= 2;
      if (run) {
         this.runUntil = Math.max(this.runUntil, this.tick + 40);
      }

      if (this.popStreak >= 3) {
         this.popStreak = 0;
         this.poppedOut = true;
         this.runUntil = this.tick + 200;
      }

      if (this.tick < this.runUntil) {
         return CrystalBotModule.Stance.RUN;
      } else if (this.tick - this.ownPopAt <= 40) {
         return CrystalBotModule.Stance.GUARD;
      } else if (this.finishing(target, rival)) {
         return CrystalBotModule.Stance.PRESS;
      } else {
         boolean guard = health <= this.retreatAt() + 2.0F || totems <= 1 || risk >= 0.35F;
         return guard && health <= 8.0F ? CrystalBotModule.Stance.GUARD : CrystalBotModule.Stance.PRESS;
      }
   }

   private boolean finishing(class_1657 target, RivalBook.Rival rival) {
      if (this.theirPopAt >= 0 && !target.method_6079().method_31574(class_1802.field_8288)) {
         int window = rival == null ? 8 : class_3532.method_15340(rival.retotemWindow(8), 3, 30);
         return this.tick - this.theirPopAt <= window;
      } else {
         return false;
      }
   }

   private void steer(class_746 player, class_1657 target, RivalBook.Rival rival) {
      float shift = 2.0F;
      float health = player.method_6032() + player.method_6067();
      float room = health - (player.method_6079().method_31574(class_1802.field_8288) ? 1.0F : 4.0F);
      float selfCap = Math.max(0.5F, Math.min(this.stance.selfCap + shift, room));
      this.selfCapNow = selfCap;
      float min = this.finishing(target, rival) ? 2.0F : this.stance.minDamage;
      if (feetY(player) >= class_3532.method_15357(target.method_23318() + 0.01) && !Combo.inHole(target)) {
         min = Math.min(min, 2.5F);
      }

      CrystalAuraModule crystal = ModuleRegistry.get(CrystalAuraModule.class);
      if (crystal != null) {
         crystal.steer(player, min, selfCap);
         crystal.crystalsFrom(player, feetY(player) + 1);
         crystal.fullSpeed(player);
         crystal.buildEagerly(player);
      }

      AnchorAuraModule anchor = ModuleRegistry.get(AnchorAuraModule.class);
      if (anchor != null) {
         anchor.steer(player, this.finishing(target, rival) ? 2.0F : this.stance.anchorMin, Math.min(selfCap, 11.0F));
      }
   }

   private float eatAt() {
      return this.eatBelow.get() - 2.0F;
   }

   private float retreatAt() {
      return 4.0F;
   }

   private boolean critical(class_746 player) {
      return player.method_6032() + player.method_6067() <= this.retreatAt() && !player.method_6079().method_31574(class_1802.field_8288);
   }

   private static int totems(class_746 player) {
      int count = player.method_6079().method_31574(class_1802.field_8288) ? 1 : 0;
      class_1661 inventory = player.method_31548();

      for (int i = 0; i < 36; i++) {
         if (inventory.method_5438(i).method_31574(class_1802.field_8288)) {
            count += inventory.method_5438(i).method_7947();
         }
      }

      return count;
   }

   private static float worstThreat(class_638 level, class_746 player, class_2338 ours) {
      List<class_1657> enemies = enemies(level, player, 16.0);
      float worst = 0.0F;

      for (class_1511 crystal : level.method_18467(class_1511.class, player.method_5829().method_1014(12.0))) {
         class_243 center = crystal.method_73189();
         float us = CrystalDamage.estimate(center, player);
         if (us > worst && worthIt(enemies, crystal.method_5829(), center, us, 4.0, 6.0F, null)) {
            worst = us;
         }
      }

      class_2338 feet = player.method_24515();

      for (class_2338 pos : class_2338.method_10097(feet.method_10069(-5, -2, -5), feet.method_10069(5, 3, 5))) {
         class_2680 state = level.method_8320(pos);
         if (state.method_27852(class_2246.field_23152) && (Integer)state.method_11654(class_4969.field_23153) > 0 && !pos.equals(ours)) {
            class_2338 anchor = pos.method_10062();
            class_243 center = class_243.method_24953(anchor);
            float us = CrystalDamage.estimate(center, player, 5.0F, anchor);
            if (us > worst && worthIt(enemies, new class_238(anchor), center, us, 5.5, 5.0F, anchor)) {
               worst = us;
            }
         }
      }

      return worst;
   }

   private static boolean worthIt(List<class_1657> enemies, class_238 source, class_243 center, float us, double reach, float power, class_2338 ignored) {
      for (class_1657 enemy : enemies) {
         class_243 eye = enemy.method_33571();
         if (!(eye.method_1025(TargetingUtils.closestPoint(eye, source)) > reach * reach) && CrystalDamage.estimate(center, enemy, power, ignored) < us) {
            return true;
         }
      }

      return false;
   }

   private static int enemiesWithin(class_638 level, class_746 player, double reach) {
      int count = 0;

      for (class_1657 other : level.method_18456()) {
         if (other != player && other.method_5805() && MeleeAttacks.isEnemy(player, other) && other.method_5739(player) <= reach) {
            count++;
         }
      }

      return count;
   }

   private static List<class_1657> enemies(class_638 level, class_746 player, double reach) {
      List<class_1657> enemies = new ArrayList<>();

      for (class_1657 other : level.method_18456()) {
         if (other != player && other.method_5805() && MeleeAttacks.isEnemy(player, other) && other.method_5739(player) <= reach) {
            enemies.add(other);
         }
      }

      return enemies;
   }

   private void runAuras(class_638 level) {
      CrystalAuraModule crystal = ModuleRegistry.get(CrystalAuraModule.class);
      if (crystal != null && !crystal.isEnabled()) {
         if (this.crystalWasOn == null) {
            this.crystalWasOn = false;
         }

         crystal.setEnabled(true);
      }

      AnchorAuraModule anchor = ModuleRegistry.get(AnchorAuraModule.class);
      boolean wantAnchors = this.anchors.get() && level.method_27983() != class_1937.field_25180;
      if (anchor != null && wantAnchors && !anchor.isEnabled()) {
         if (this.anchorWasOn == null) {
            this.anchorWasOn = false;
         }

         anchor.setEnabled(true);
      }
   }

   private void keepTotem() {
      AutoInvTotemModule inventory = ModuleRegistry.get(AutoInvTotemModule.class);
      AutoTotemModule plain = ModuleRegistry.get(AutoTotemModule.class);
      if (inventory != null && !inventory.isEnabled() && (plain == null || !plain.isEnabled())) {
         if (this.totemWasOn == null) {
            this.totemWasOn = false;
         }

         inventory.setEnabled(true);
      }
   }

   private void restoreAuras() {
      CrystalAuraModule crystal = ModuleRegistry.get(CrystalAuraModule.class);
      if (crystal != null && Boolean.FALSE.equals(this.crystalWasOn)) {
         crystal.setEnabled(false);
      }

      AnchorAuraModule anchor = ModuleRegistry.get(AnchorAuraModule.class);
      if (anchor != null && Boolean.FALSE.equals(this.anchorWasOn)) {
         anchor.setEnabled(false);
      }

      AutoInvTotemModule totem = ModuleRegistry.get(AutoInvTotemModule.class);
      if (totem != null && Boolean.FALSE.equals(this.totemWasOn)) {
         totem.setEnabled(false);
      }

      this.crystalWasOn = this.anchorWasOn = this.totemWasOn = null;
   }

   private static boolean blastNear(class_638 level, class_746 player) {
      for (class_1511 crystal : level.method_18467(class_1511.class, player.method_5829().method_1014(7.0))) {
         if (CrystalDamage.estimate(crystal.method_73189(), player) >= 1.0F) {
            return true;
         }
      }

      class_2338 feet = player.method_24515();
      int reach = (int)Math.ceil(7.0);

      for (class_2338 pos : class_2338.method_10097(feet.method_10069(-reach, -2, -reach), feet.method_10069(reach, 3, reach))) {
         class_2680 state = level.method_8320(pos);
         if (state.method_27852(class_2246.field_23152)
            && (Integer)state.method_11654(class_4969.field_23153) > 0
            && class_243.method_24953(pos).method_1022(player.method_73189()) <= 7.0) {
            return true;
         }
      }

      return false;
   }

   private static void handBack(class_746 player) {
      class_1799 main = player.method_6047();
      if (!main.method_31574(class_1802.field_8301)
         && !main.method_31574(class_1802.field_8281)
         && !main.method_31574(class_1802.field_23141)
         && !main.method_31574(class_1802.field_8801)
         && !Crystals.isCombatItem(main)) {
         int slot = BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_8301));
         if (slot < 0) {
            slot = BotBody.hotbarSlot(player, MeleeAttacks::isMeleeWeapon);
         }

         if (slot >= 0) {
            player.method_31548().method_61496(slot);
         }
      }
   }

   private boolean hands() {
      CombatMacroLock.take(this);
      return !MeleeAttacks.actedThisTick(this.mc.field_1724);
   }

   private void watch(class_638 level, class_1657 target, RivalBook.Rival rival) {
      float theirs = CrystalDamage.health(target);
      if (theirs < this.lastTheirHealth - 0.5F) {
         this.hurtAt = this.tick;
      }

      if (theirs < this.lastTheirHealth - 0.5F || !Combo.inHole(target)) {
         this.progressAt = this.tick;
      }

      this.lastTheirHealth = theirs;
      if (this.openerArm >= 0 && this.tick - this.openerAt >= 40) {
         float taken = this.openerHealth - theirs + 10.0F * this.openerPops;
         if (rival != null) {
            rival.opened(this.openerArm, this.openerReach, class_3532.method_15363(taken / 12.0F, 0.0F, 1.5F));
         }

         this.openerArm = -1;
      }

      if (this.launchedAt >= 0) {
         this.launchPeak = Math.max(this.launchPeak, target.method_23318() - this.launchY);
         if (this.tick - this.launchedAt >= 8) {
            if (rival != null) {
               rival.launched(this.launchPeak >= 0.3);
            }

            this.launchedAt = -1;
         }
      }

      if (this.theirPopAt >= 0 && rival != null) {
         if (target.method_6079().method_31574(class_1802.field_8288)) {
            rival.retotemed(this.tick - this.theirPopAt);
            this.theirPopAt = -1;
         } else if (this.tick - this.theirPopAt > 100) {
            rival.retotemed(100);
            this.theirPopAt = -1;
         }
      } else if (this.theirPopAt >= 0 && this.tick - this.theirPopAt > 100) {
         this.theirPopAt = -1;
      }

      if (Combo.inHole(target)) {
         if (this.holeSince < 0) {
            this.holeSince = this.tick;
         }
      } else if (this.holeSince >= 0) {
         if (rival != null) {
            rival.leftHole(Math.min(600, this.tick - this.holeSince));
         }

         this.holeSince = -1;
      }

      if (!this.lowSpell && theirs <= 8.0F) {
         this.lowSpell = true;
         this.pearledInSpell = false;
      } else if (this.lowSpell && theirs > 12.0F) {
         if (rival != null) {
            rival.lowSpell(this.pearledInSpell);
         }

         this.lowSpell = false;
      }

      for (class_1684 pearl : level.method_18467(class_1684.class, target.method_5829().method_1014(4.0))) {
         if (pearl.method_24921() == target && pearl.field_6012 <= 2 && pearl.method_5628() != this.followedPearl) {
            if (this.lowSpell) {
               this.pearledInSpell = true;
            }

            this.theirPearl = pearl.method_5628();
         }
      }
   }

   private boolean getAway(class_638 level, class_746 player, class_1657 target) {
      if (player.method_5739(target) < 12.0 && this.pearlAway(level, player, target)) {
         return true;
      } else if (player.method_5739(target) >= 16.0 && player.method_6032() <= this.eatAt() + 4.0F && this.hands() && this.body.use(this.mc, player, GAPPLE)) {
         this.eating = true;
         return true;
      } else {
         this.body.flee(this.mc, player, target);
         return true;
      }
   }

   private boolean pearlAway(class_638 level, class_746 player, class_1657 target) {
      if (this.escape.get() && this.pearlReady(player) && this.tick >= this.searchAfter) {
         List<class_1657> enemies = enemies(level, player, 48.0);
         Rotation look = PearlFlight.escape(player, enemies, 20.0);
         this.throwAway = 16.0;
         if (look == null) {
            look = PearlFlight.escape(player, enemies, 12.0);
            this.throwAway = 12.0;
         }

         if (look == null) {
            this.searchAfter = this.tick + 10;
            return false;
         } else {
            this.startThrow(CrystalBotModule.Throw.ESCAPE, look, null);
            return this.keepThrowing(player, target);
         }
      } else {
         return false;
      }
   }

   private boolean pearlReady(class_746 player) {
      return this.tick - this.pearlAt >= 30
         && BotBody.hotbarSlot(player, PEARL) >= 0
         && !player.method_7357().method_7904(new class_1799(class_1802.field_8634))
         && (player.method_6032() + player.method_6067() > 6.0F || player.method_6079().method_31574(class_1802.field_8288));
   }

   private void startThrow(CrystalBotModule.Throw kind, Rotation look, class_243 spot) {
      this.throwing = kind;
      this.throwLook = look;
      this.throwSpot = spot;
      this.throwUntil = this.tick + 14;
   }

   private boolean keepThrowing(class_746 player, class_1657 target) {
      if (this.tick > this.throwUntil || !this.pearlReady(player)) {
         this.throwing = null;
         this.pearlAt = this.tick - 15;
         return false;
      } else if (!this.hands()) {
         return true;
      } else {
         Aim.turn(this.mc, this.throwLook.yaw(), this.throwLook.pitch(), 27.0F);
         int slot = BotBody.hotbarSlot(player, PEARL);
         if (player.method_31548().method_67532() != slot) {
            player.method_31548().method_61496(slot);
            return true;
         } else if (!ServerRotation.desynced() && !player.method_6115() && MeleeAttacks.handsFree(this.mc, player)) {
            if (player.method_24828() && !(player.method_60478().method_37268() > 1.0E-4)) {
               float yaw = player.method_36454();
               float pitch = player.method_36455();
               Rotation sent = ServerRotation.commit(player);
               if (!(Math.abs(class_3532.method_15393(sent.yaw() - yaw)) > 0.01F) && !(Math.abs(sent.pitch() - pitch) > 0.01F)) {
                  boolean good = this.throwing == CrystalBotModule.Throw.ESCAPE
                     ? PearlFlight.escapes(player, yaw, pitch, enemies(this.mc.field_1687, player, 48.0), this.throwAway)
                     : PearlFlight.reaches(player, yaw, pitch, this.throwSpot, 0.0, 3.0);
                  if (!good) {
                     return true;
                  } else {
                     if (this.mc.field_1761.method_2919(player, class_1268.field_5808).method_23665()) {
                        player.method_6104(class_1268.field_5808);
                        MeleeAttacks.claimTick(this.mc);
                        this.pearlAt = this.tick;
                        if (this.throwing == CrystalBotModule.Throw.ESCAPE) {
                           ModuleChat.send(this, ModuleChat.body().warn("Pearled away").text("."));
                        } else {
                           ModuleChat.send(this, ModuleChat.body().text("Pearled after ").name(target.method_5477().getString()).text("."));
                        }
                     }

                     this.throwing = null;
                     return true;
                  }
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      }
   }

   private boolean chasePearl(class_638 level, class_746 player, class_1657 target, RivalBook.Rival rival) {
      if ((this.stance == CrystalBotModule.Stance.PRESS || this.stance == CrystalBotModule.Stance.HOLD) && this.pearlReady(player)) {
         float health = player.method_6032() + player.method_6067();
         if (health < 6.0F) {
            return false;
         } else {
            int pearls = count(player, PEARL);
            boolean following = this.theirPearl >= 0;
            boolean keepOne = totems(player) == 0 && player.method_6032() <= 12.0F
               || !following && rival != null && rival.pearlsWhenLow() && CrystalDamage.health(target) <= 8.0F;
            if (pearls <= (keepOne ? 1 : 0)) {
               return false;
            } else {
               class_243 spot = null;
               if (following) {
                  this.followedPearl = this.theirPearl;
                  this.theirPearl = -1;
                  if (level.method_8469(this.followedPearl) instanceof class_1684 pearl) {
                     PearlFlight.Landing landing = PearlFlight.fly(level, pearl, pearl.method_73189(), pearl.method_18798());
                     if (landing != null && landing.at().method_1022(player.method_73189()) > 8.0) {
                        spot = landing.at();
                     }
                  }
               }

               double distance = player.method_5739(target);
               if (spot == null && distance <= this.range.get() + 8.0) {
                  class_243 away = target.method_73189().method_1020(player.method_73189()).method_18805(1.0, 0.0, 1.0).method_1029();
                  class_243 motion = target.method_73189().method_1023(target.field_6014, target.field_6036, target.field_5969);
                  boolean running = distance >= 12.0 && motion.field_1352 * away.field_1352 + motion.field_1350 * away.field_1350 > 0.12;
                  class_2338 under = target.method_24515().method_10074();
                  boolean standing = !level.method_8320(under).method_26220(level, under).method_1110();
                  boolean above = distance >= 3.0 && target.method_23318() - player.method_23318() >= 3.0 && standing;
                  boolean below = distance >= 4.0 && player.method_23318() - target.method_23318() >= 4.0 && standing;
                  boolean stuck = distance > 6.0 && this.tick - this.closerAt > 60;
                  if (running || above || below || stuck) {
                     spot = target.method_73189().method_1019(motion.method_18805(1.0, 0.0, 1.0).method_1021(PearlFlight.flightTicks(distance)));
                  }

                  if (spot == null && !target.method_24828() && distance >= 6.0) {
                     AirTrack.Landing down = this.air.landing(this.tick);
                     if (down != null && target.method_23318() - down.at().field_1351 >= 3.0) {
                        spot = down.at();
                     }
                  }
               }

               if (spot != null && this.tick >= this.searchAfter) {
                  Rotation look = PearlFlight.toward(player, spot, 0.8, 3.0);
                  if (look == null) {
                     this.searchAfter = this.tick + 10;
                     return false;
                  } else {
                     this.startThrow(CrystalBotModule.Throw.CHASE, look, spot);
                     return this.keepThrowing(player, target);
                  }
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean getDown(class_638 level, class_746 player, class_1657 target) {
      double drop = player.method_23318() - target.method_23318();
      double sideways = Math.hypot(target.method_23317() - player.method_23317(), target.method_23321() - player.method_23321());
      if (!(drop < 3.0) && !(sideways > 8.0) && this.stance != CrystalBotModule.Stance.RUN && !this.critical(player)) {
         if (this.belowSince < 0) {
            this.belowSince = this.tick;
         }

         if (this.tick - this.belowSince >= 20 && player.method_24828() && target.method_24828()) {
            if (this.digAnchor != null && !level.method_8320(this.digAnchor).method_27852(class_2246.field_23152)) {
               if (this.digPlaced) {
                  this.digBlasts++;
                  this.digWalkUntil = this.tick + 40;
               }

               this.digAnchor = null;
            }

            if (this.tick >= this.digWalkUntil && !openDrop(level, player, target)) {
               boolean blast = drop <= 6.0
                  && this.digBlasts < 2
                  && this.tick >= this.digAfter
                  && this.anchors.get()
                  && level.method_27983() != class_1937.field_25180
                  && BotBody.hotbarSlot(player, ANCHOR) >= 0
                  && BotBody.hotbarSlot(player, GLOWSTONE) >= 0;
               return blast && this.blastDown(level, player, target) ? true : this.pearlDown(player, target);
            } else {
               this.body.face(this.mc, player, target, 1.0F);
               this.body.dropToward(this.mc, player, target.method_73189());
               return true;
            }
         } else {
            return false;
         }
      } else {
         this.forgetDig();
         return false;
      }
   }

   private boolean blastDown(class_638 level, class_746 player, class_1657 target) {
      if (this.digAnchor == null) {
         this.digAnchor = digCell(level, player, target);
         if (this.digAnchor == null) {
            this.digAfter = this.tick + 100;
            return false;
         }

         this.digPlaced = false;
         this.digGuards = 0;
         this.digUntil = this.tick + 80;
         if (!this.digAnnounced) {
            ModuleChat.send(this, ModuleChat.body().text("Blasting down to ").name(target.method_5477().getString()).text("."));
         }

         this.digAnnounced = true;
      }

      if (this.tick > this.digUntil) {
         this.digBlasts = 2;
         this.digAnchor = null;
         return false;
      } else {
         this.body.face(this.mc, player, target, 1.0F);
         class_2680 state = level.method_8320(this.digAnchor);
         if (!state.method_27852(class_2246.field_23152)) {
            AnchorGuard.Spot spot = state.method_45474() ? AnchorGuard.supportFor(level, player, player.method_33571(), this.digAnchor) : null;
            if (spot == null) {
               this.digAnchor = null;
               this.digAfter = this.tick + 100;
               return false;
            } else {
               if (this.placeAt(level, player, spot, BotBody.hotbarSlot(player, ANCHOR)) == 1) {
                  this.digPlaced = true;
               }

               return true;
            }
         } else if ((Integer)state.method_11654(class_4969.field_23153) <= 0) {
            return this.clickDigAnchor(level, player, BotBody.hotbarSlot(player, GLOWSTONE));
         } else {
            class_243 aim = AnchorGuard.anchorAim(level, player, this.digAnchor);
            if (aim == null) {
               this.digAnchor = null;
               return false;
            } else {
               int shieldSlot = guardBlockSlot(player);
               if (this.digGuards < 2 && shieldSlot >= 0) {
                  AnchorGuard.Spot shield = AnchorGuard.find(level, player, this.digAnchor, aim);
                  if (shield != null && !shield.standing()) {
                     if (this.placeAt(level, player, shield, shieldSlot) == 1) {
                        this.digGuards++;
                     }

                     return true;
                  }
               }

               float own = CrystalDamage.estimate(class_243.method_24953(this.digAnchor), player, 5.0F, this.digAnchor);
               int route = AnchorGuard.detonatorRoute(player.method_31548(), player.method_6047());
               if (!(own > Math.min(this.selfCapNow, 10.0F)) && !CrystalDamage.endangers(player, own) && route != -1) {
                  return this.clickDigAnchor(level, player, route == -2 ? player.method_31548().method_67532() : route);
               } else {
                  this.digBlasts = 2;
                  this.digAnchor = null;
                  return false;
               }
            }
         }
      }
   }

   private static class_2338 digCell(class_638 level, class_746 player, class_1657 target) {
      class_243 eye = player.method_33571();
      double reach = player.method_55754() - 0.5;
      class_2338 feet = player.method_24515();
      List<class_2338> cells = new ArrayList<>();

      for (class_2338 cell : class_2338.method_10097(feet.method_10069(-4, -2, -4), feet.method_10069(4, 0, 4))) {
         if (!(eye.method_1022(class_243.method_24953(cell)) > reach)) {
            class_2338 under = cell.method_10074();
            if (level.method_8320(cell).method_45474()
               && level.method_8316(cell).method_15769()
               && level.method_8320(under).method_26206(level, under, class_2350.field_11036)) {
               class_238 box = new class_238(cell);
               if (!box.method_994(player.method_5829()) && !box.method_994(target.method_5829())) {
                  cells.add(cell.method_10062());
               }
            }
         }
      }

      cells.sort(Comparator.comparingDouble(cellx -> class_243.method_24953(cellx).method_1025(target.method_73189())));
      class_2338 fallback = null;

      for (class_2338 cellx : cells.subList(0, Math.min(cells.size(), 8))) {
         if (AnchorGuard.supportFor(level, player, eye, cellx) != null) {
            if (fallback == null) {
               fallback = cellx;
            }

            class_243 sight = TargetingUtils.closestPoint(eye, new class_238(cellx).method_1011(0.1));
            if (AnchorGuard.find(level, player, cellx, sight) != null) {
               return cellx;
            }
         }
      }

      return fallback;
   }

   private static boolean openDrop(class_638 level, class_746 player, class_1657 target) {
      class_243 way = target.method_73189().method_1020(player.method_73189()).method_18805(1.0, 0.0, 1.0);
      if (way.method_1027() < 0.25) {
         return false;
      } else {
         class_243 step = player.method_73189().method_1019(way.method_1029().method_1021(0.8));
         class_2338 column = class_2338.method_49637(step.field_1352, player.method_23318() + 0.1, step.field_1350);
         int floor = class_3532.method_15357(target.method_23318() + 0.01);

         for (class_2338 pos = column; pos.method_10264() >= floor - 1; pos = pos.method_10074()) {
            class_2680 state = level.method_8320(pos);
            if (state.method_27852(class_2246.field_10164) || state.method_26164(class_3481.field_21952)) {
               return false;
            }

            if (!state.method_26220(level, pos).method_1110()) {
               return pos.method_10264() < player.method_23318() - 1.5 && pos.method_10264() <= floor + 1;
            }
         }

         return false;
      }
   }

   private boolean clickDigAnchor(class_638 level, class_746 player, int slot) {
      if (slot < 0) {
         this.digAnchor = null;
         return false;
      } else if (!this.hands()) {
         return true;
      } else {
         class_3965 hit = AnchorGuard.anchorHit(level, player, this.digAnchor);
         if (hit == null) {
            class_243 aim = AnchorGuard.anchorAim(level, player, this.digAnchor);
            if (aim == null) {
               this.digAnchor = null;
               return false;
            } else {
               ServerRotation.request(RotationUtils.fromTo(player.method_33571(), aim), 6.0F);
               return true;
            }
         } else if (player.method_31548().method_67532() != slot) {
            player.method_31548().method_61496(slot);
            return true;
         } else {
            if (MeleeAttacks.handsFree(this.mc, player)) {
               AnchorGuard.click(this.mc, player, hit);
            }

            return true;
         }
      }
   }

   private boolean pearlDown(class_746 player, class_1657 target) {
      if (this.chase.get() && this.pearlReady(player) && this.tick >= this.searchAfter) {
         class_243 spot = target.method_73189();
         Rotation look = PearlFlight.toward(player, spot, 0.8, 3.0);
         if (look == null) {
            this.searchAfter = this.tick + 10;
            return false;
         } else {
            this.startThrow(CrystalBotModule.Throw.CHASE, look, spot);
            return this.keepThrowing(player, target);
         }
      } else {
         return false;
      }
   }

   private static int feetY(class_746 player) {
      return class_3532.method_15357(player.method_23318() + 0.01);
   }

   private boolean wantsLower(class_746 player, class_1657 target, double distance) {
      return this.stance != CrystalBotModule.Stance.RUN
         && player.method_24828()
         && target.method_24828()
         && !Combo.inHole(target)
         && distance <= 5.5
         && feetY(player) == class_3532.method_15357(target.method_23318() + 0.01)
         && !this.justLaunched();
   }

   private boolean justLaunched() {
      return this.launching || this.launchedAt >= 0 || this.tick <= this.followUntil + 20;
   }

   private boolean stayLow(class_638 level, class_746 player, class_1657 target, double distance) {
      if (this.stance != CrystalBotModule.Stance.RUN && player.method_24828() && target.method_24828() && !Combo.inHole(target) && !(distance > 5.5)) {
         if (feetY(player) < class_3532.method_15357(target.method_23318() + 0.01)) {
            return true;
         } else if (!this.wantsLower(player, target, distance)) {
            return false;
         } else {
            class_2338 step = lowerStep(level, player, target, feetY(player) - 1);
            if (step == null) {
               return false;
            } else {
               this.body.toward(this.mc, player, class_243.method_24955(step));
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean digDown(class_638 level, class_746 player, class_1657 target, double distance) {
      if (this.wantsLower(player, target, distance) && this.tick >= this.stepDigAfter) {
         if (lowerStep(level, player, target, feetY(player) - 1) != null) {
            return false;
         } else {
            class_2338 under = player.method_24515().method_10074();
            class_2680 state = level.method_8320(under);
            if (BotBody.hotbarSlot(player, OBSIDIAN) < 0 && !Crystals.isBase(state)) {
               return false;
            } else {
               class_2338 landing = under.method_10074();
               boolean diggable = !state.method_31709()
                  && digTicks(level, player, under) <= 50
                  && !level.method_8320(landing).method_26220(level, landing).method_1110()
                  && !nearLava(level, under);
               if (diggable && this.mine(level, player, under)) {
                  return true;
               } else {
                  this.stepDigAfter = this.tick + 100;
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean digBase(class_638 level, class_746 player, class_1657 target, double distance) {
      int theirs = class_3532.method_15357(target.method_23318() + 0.01);
      if (this.stance != CrystalBotModule.Stance.RUN
         && player.method_24828()
         && target.method_24828()
         && !Combo.inHole(target)
         && !(distance > 5.5)
         && feetY(player) == theirs - 1
         && this.tick >= this.baseDigAfter
         && BotBody.hotbarSlot(player, OBSIDIAN) >= 0
         && !this.justLaunched()) {
         class_243 eye = player.method_33571();
         class_2338 theirFeet = target.method_24515();
         class_2338 best = null;
         double nearest = Double.MAX_VALUE;

         for (class_2338 pos : class_2338.method_10097(theirFeet.method_10069(-2, -1, -2), theirFeet.method_10069(2, -1, 2))) {
            class_2680 state = level.method_8320(pos);
            if (Crystals.isBase(state) && Crystals.canHost(level, pos, null)) {
               return false;
            }

            if ((pos.method_10263() != theirFeet.method_10263() || pos.method_10260() != theirFeet.method_10260())
               && Math.max(Math.abs(pos.method_10263() - player.method_31477()), Math.abs(pos.method_10260() - player.method_31479())) >= 2
               && !state.method_45474()
               && !state.method_31709()
               && level.method_22347(pos.method_10084())) {
               class_243 top = class_243.method_24953(pos).method_1031(0.0, 0.5, 0.0);
               double reach = top.method_1022(target.method_73189());
               if (!(reach > 2.5) && !(eye.method_1022(top) > player.method_55754() - 0.5) && digTicks(level, player, pos) <= 25 && !(reach >= nearest)) {
                  best = pos.method_10062();
                  nearest = reach;
               }
            }
         }

         if (best != null && this.mine(level, player, best)) {
            return true;
         } else {
            this.baseDigAfter = this.tick + 100;
            return false;
         }
      } else {
         return false;
      }
   }

   private static class_2338 lowerStep(class_638 level, class_746 player, class_1657 target, int feet) {
      class_2338 here = player.method_24515();
      class_2338 best = null;
      double nearest = Double.MAX_VALUE;

      for (class_2338 cell : class_2338.method_10097(here.method_10069(-2, -1, -2), here.method_10069(2, -1, 2))) {
         class_2338 spot = StandPicker.standable(level, cell);
         if (spot != null && spot.method_10264() == feet && spot.equals(cell)) {
            class_243 at = class_243.method_24955(spot);
            double reach = Math.hypot(at.field_1352 - target.method_23317(), at.field_1350 - target.method_23321());
            if (!(reach < 2.4) && !(reach > 4.5) && walkable(level, player.method_73189(), at)) {
               double distance = spot.method_10262(here);
               if (distance < nearest) {
                  best = spot.method_10062();
                  nearest = distance;
               }
            }
         }
      }

      return best;
   }

   private static int digTicks(class_638 level, class_746 player, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      float hardness = state.method_26214(level, pos);
      if (hardness < 0.0F) {
         return Integer.MAX_VALUE;
      } else if (hardness == 0.0F) {
         return 1;
      } else {
         int slot = toolSlot(player.method_31548(), state);
         class_1799 tool = slot < 0 ? class_1799.field_8037 : player.method_31548().method_5438(slot);
         float speed = tool.method_7924(state);
         if (speed > 1.0F) {
            class_2378<class_1887> enchantments = player.method_56673().method_30530(class_7924.field_41265);
            int efficiency = enchantments.method_46746(class_1893.field_9131).map(holder -> class_1890.method_8225(holder, tool)).orElse(0);
            if (efficiency > 0) {
               speed += efficiency * efficiency + 1;
            }
         }

         boolean right = tool.method_7951(state) || !state.method_29291();
         return class_3532.method_15386(hardness * (right ? 30.0F : 100.0F) / speed);
      }
   }

   private static boolean nearLava(class_638 level, class_2338 pos) {
      for (class_2350 side : class_2350.values()) {
         if (level.method_8316(pos.method_10093(side)).method_15767(class_3486.field_15518)) {
            return true;
         }
      }

      return false;
   }

   private void forgetDig() {
      this.digAnchor = null;
      this.digPlaced = this.digAnnounced = false;
      this.digGuards = this.digBlasts = 0;
      this.digUntil = this.digWalkUntil = this.digAfter = 0;
      this.belowSince = -1;
   }

   private boolean startLaunch(class_746 player, class_1657 target, RivalBook.Rival rival) {
      if (player.field_6012 - this.diggingAt <= 1) {
         return false;
      } else if (this.tick >= this.nextLaunch
         && !this.pause.slowServer()
         && (this.stance == CrystalBotModule.Stance.PRESS || this.stance == CrystalBotModule.Stance.HOLD)
         && (rival == null || !rival.shrugsOffLaunches())) {
         if (target.method_24828() && !Combo.inHole(target) && (!target.method_6039() || axeInHotbar(player))) {
            if (!this.mc.field_1687.method_18467(class_1511.class, target.method_5829().method_1014(3.0)).isEmpty()) {
               return false;
            } else {
               double reach = reach(player, target);
               if (weaponSlot(player, target.method_6039()) >= 0 && !(reach > 3.9)) {
                  boolean armed = MeleeAttacks.isMeleeWeapon(player.method_6047()) && MeleeAttacks.charge(player) >= 0.5F;
                  if (!armed) {
                     int arm = rival != null ? rival.opener(reach) : (this.tick - this.hurtAt >= 20 ? 0 : 1);
                     if (this.openerArm < 0) {
                        this.judgeOpener(arm, reach, target);
                     }

                     if (arm == 1) {
                        this.nextLaunch = this.tick + 40;
                        return false;
                     }
                  }

                  this.launching = true;
                  this.launchUntil = this.tick + 16;
                  return this.launch(player, target);
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void judgeOpener(int arm, double reach, class_1657 target) {
      this.openerArm = arm;
      this.openerReach = reach;
      this.openerAt = this.tick;
      this.openerHealth = CrystalDamage.health(target);
      this.openerPops = 0;
   }

   private boolean followUp(class_638 level, class_746 player, class_1657 target) {
      AirTrack.Landing down = this.air.landing(this.tick);
      class_243 landing = down != null ? down.at() : target.method_73189();
      if (this.anchorUnder(level, player, target, landing)) {
         return true;
      } else {
         int slot = BotBody.hotbarSlot(player, OBSIDIAN);
         if (slot < 0) {
            this.followLaid = true;
            return false;
         } else {
            class_2338 feet = class_2338.method_49637(landing.field_1352, landing.field_1351 + 0.2, landing.field_1350);
            CrystalDamage.Victim them = new CrystalDamage.Victim(target, landing.method_1020(target.method_73189()));
            CrystalDamage.Victim us = new CrystalDamage.Victim(player, class_243.field_1353);
            class_243 eye = player.method_33571();
            AnchorGuard.Spot best = null;
            float most = 4.0F;

            for (class_2338 base : class_2338.method_10097(feet.method_10069(-1, -1, -1), feet.method_10069(1, 0, 1))) {
               if (base.method_10263() != feet.method_10263() || base.method_10260() != feet.method_10260()) {
                  class_2680 state = level.method_8320(base);
                  class_243 center = Crystals.explosionAt(base);
                  if (Crystals.isBase(state) && Crystals.canHost(level, base, null)) {
                     if (them.damage(center, false) >= most) {
                        this.followLaid = true;
                        return false;
                     }
                  } else if (state.method_45474() && level.method_22347(base.method_10084()) && level.method_22347(base.method_10086(2))) {
                     class_2338 below = base.method_10074();
                     if (level.method_8320(below).method_26206(level, below, class_2350.field_11036)
                        && level.method_8628(class_2246.field_10540.method_9564(), base, class_3726.method_16194())
                        && !(eye.method_1022(class_243.method_24953(base)) > player.method_55754())) {
                        float hurt = them.damage(center, true);
                        float own = us.damage(center, true);
                        if (!(hurt <= most) && !(own > this.selfCapNow) && !CrystalDamage.endangers(player, own)) {
                           AnchorGuard.Spot spot = AnchorGuard.supportFor(level, player, eye, base.method_10062());
                           if (spot != null) {
                              best = spot;
                              most = hurt;
                           }
                        }
                     }
                  }
               }
            }

            this.body.face(this.mc, player, target, 1.0F);
            if (best == null) {
               if (eye.method_1022(landing) > 3.5) {
                  this.body.drive(this.mc, player, 1, 0.0, true, false);
               }

               return true;
            } else {
               if (this.placeAt(level, player, best, slot) == 1) {
                  this.followLaid = true;
               }

               return true;
            }
         }
      }
   }

   private boolean anchorUnder(class_638 level, class_746 player, class_1657 target, class_243 landing) {
      if (this.anchors.get() && level.method_27983() != class_1937.field_25180 && !target.method_24828()) {
         int slot = BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_23141));
         if (slot >= 0 && BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_8801)) >= 0) {
            class_2338 cell = class_2338.method_49637(landing.field_1352, landing.field_1351 + 0.2, landing.field_1350);
            class_2338 below = cell.method_10074();
            if (level.method_8320(cell).method_45474() && level.method_8320(below).method_26206(level, below, class_2350.field_11036)) {
               if (!target.method_5829().method_994(new class_238(cell)) && !player.method_5829().method_994(new class_238(cell))) {
                  class_243 eye = player.method_33571();
                  if (eye.method_1022(class_243.method_24953(cell)) > player.method_55754()) {
                     return false;
                  } else {
                     class_243 blast = class_243.method_24953(cell);
                     float own = CrystalDamage.estimate(blast, player, 5.0F, cell);
                     class_243 onTop = new class_243(landing.field_1352, cell.method_10264() + 1.0, landing.field_1350);
                     float theirs = new CrystalDamage.Victim(target, onTop.method_1020(target.method_73189())).damage(blast, 5.0F, cell);
                     if (!(theirs < 6.0F) && !(own > Math.min(this.selfCapNow, 11.0F)) && !CrystalDamage.endangers(player, own)) {
                        AnchorGuard.Spot spot = AnchorGuard.supportFor(level, player, eye, cell);
                        if (spot == null) {
                           return false;
                        } else {
                           this.body.face(this.mc, player, target, 1.0F);
                           if (this.placeAt(level, player, spot, slot) == 1) {
                              this.followLaid = true;
                           }

                           return true;
                        }
                     } else {
                        return false;
                     }
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean launch(class_746 player, class_1657 target) {
      if (this.tick > this.launchUntil
         || this.stance == CrystalBotModule.Stance.RUN
         || this.stance == CrystalBotModule.Stance.GUARD
         || reach(player, target) > 4.1
         || Combo.inHole(target)) {
         this.launching = false;
         this.nextLaunch = this.tick + 40;
         return false;
      } else if (!this.hands()) {
         return true;
      } else {
         int slot = weaponSlot(player, target.method_6039());
         if (slot < 0) {
            this.launching = false;
            return false;
         } else {
            if (player.method_31548().method_67532() != slot) {
               player.method_31548().method_61496(slot);
            }

            this.body.face(this.mc, player, target, 1.0F);
            float charge = MeleeAttacks.charge(player);
            double reach = reach(player, target);
            Rotation sent = ServerRotation.sent();
            boolean sprinting = player.method_5624();
            if (charge >= 0.95F && reach <= 2.95 && this.strike(player, target)) {
               this.launching = false;
               this.nextLaunch = this.tick + 80;
               this.launchedAt = this.tick;
               this.launchY = target.method_23318();
               this.launchPeak = 0.0;
               this.followLaid = false;
               this.followUntil = this.tick + 12;
               this.air.kicked(player, target, sent != null ? sent.yaw() : player.method_36454(), sprinting, this.tick);
               this.body.drive(this.mc, player, 0, 1.0, false, false);
               return true;
            } else {
               this.body.drive(this.mc, player, reach > 2.9 ? 1 : 0, 0.0, charge >= 0.85F, false);
               return true;
            }
         }
      }
   }

   private boolean strike(class_746 player, class_1657 target) {
      if (!this.pause.slowServer() && !MeleeAttacks.actedThisTick(player) && this.mc.field_1771 <= 0 && MeleeAttacks.isMeleeWeapon(player.method_6047())) {
         Rotation sent = ServerRotation.sent();
         if (sent == null) {
            return false;
         } else {
            class_243 eye = player.method_33571();
            double reachLimit = Math.min(2.95, player.method_55755());
            class_238 box = target.method_5829();
            class_243 at = box.method_1006(eye)
               ? eye
               : (class_243)box.method_992(eye, eye.method_1019(class_243.method_1030(sent.pitch(), sent.yaw()).method_1021(reachLimit))).orElse(null);
            if (at != null && MeleeAttacks.lineIsClear(player, eye, at)) {
               if (!MeleeAttacks.attack(this.mc, new class_3966(target, at))) {
                  return false;
               } else {
                  MeleeAttacks.claimTick(this.mc);
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static double reach(class_746 player, class_1657 target) {
      class_243 eye = player.method_33571();
      return eye.method_1022(TargetingUtils.closestPoint(eye, target.method_5829()));
   }

   private static boolean axeInHotbar(class_746 player) {
      return BotBody.hotbarSlot(player, stack -> stack.method_31573(class_3489.field_42612)) >= 0;
   }

   private static int weaponSlot(class_746 player, boolean shield) {
      class_1661 inventory = player.method_31548();
      int best = -1;
      double most = 0.0;

      for (int i = 0; i < 9; i++) {
         class_1799 stack = inventory.method_5438(i);
         if (MeleeAttacks.isMeleeWeapon(stack) && !stack.method_31574(class_1802.field_49814) && (!shield || stack.method_31573(class_3489.field_42612))) {
            double damage = MeleeAttacks.attackDamage(stack);
            if (best < 0 || damage > most) {
               best = i;
               most = damage;
            }
         }
      }

      return best;
   }

   private boolean breakHole(class_638 level, class_746 player, class_1657 target, RivalBook.Rival rival) {
      AutoCityModule city = ModuleRegistry.get(AutoCityModule.class);
      if (city == null) {
         return false;
      } else if (city.isEnabled()) {
         if (!this.cityStarted) {
            return false;
         } else {
            CombatMacroLock.release(this);
            this.body.face(this.mc, player, target, 1.0F);
            return true;
         }
      } else {
         if (this.cityStarted) {
            this.cityStarted = false;
            this.breachAfter = this.tick + 100;
            this.progressAt = this.tick;
            if (Combo.inHole(target)) {
               this.cityFails++;
            }
         }

         if (this.stance != CrystalBotModule.Stance.RUN
            && this.tick >= this.breachAfter
            && Combo.inHole(target)
            && !(player.method_6032() + player.method_6067() < 8.0F)) {
            int wait = rival != null && rival.campsInHoles() ? 15 : 40;
            if (this.tick - this.progressAt < wait) {
               return false;
            } else {
               boolean pickaxe = BotBody.hotbarSlot(player, stack -> stack.method_31573(class_3489.field_42614)) >= 0;
               if (this.cityFails >= 2 && pickaxe && this.roofOff(level, player, target)) {
                  return true;
               } else if (this.cityFails >= 2 || !pickaxe) {
                  return this.camp(level, player, target);
               } else if (player.method_5739(target) > 3.6) {
                  this.body.face(this.mc, player, target, 1.0F);
                  this.body.keepDistance(this.mc, player, target, 1.6, 2.6, 3.4, false);
                  return true;
               } else {
                  city.setEnabled(true);
                  if (!city.isEnabled()) {
                     return false;
                  } else {
                     this.cityStarted = true;
                     ModuleChat.send(this, ModuleChat.body().text("Breaking ").name(target.method_5477().getString()).text("'s hole open."));
                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   private boolean roofOff(class_638 level, class_746 player, class_1657 target) {
      if (this.anchors.get()
         && level.method_27983() != class_1937.field_25180
         && BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_23141)) >= 0
         && BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_8801)) >= 0) {
         class_2338 roof = target.method_24515().method_10086(2);
         class_2680 state = level.method_8320(roof);
         if (state.method_45474() || state.method_26214(level, roof) < 0.0F) {
            return false;
         } else if (this.mine(level, player, roof)) {
            this.body.face(this.mc, player, target, 1.0F);
            return true;
         } else {
            class_2338 view = roofView(level, player, roof);
            if (view == null) {
               return false;
            } else {
               this.body.face(this.mc, player, target, 1.0F);
               this.body.toward(this.mc, player, class_243.method_24955(view));
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean walkable(class_638 level, class_243 from, class_243 to) {
      class_243 way = to.method_1020(from);
      int steps = Math.max(1, (int)Math.ceil(way.method_37267() / 0.5));
      int lastY = class_3532.method_15357(from.field_1351 + 0.01);

      for (int i = 1; i <= steps; i++) {
         class_243 at = from.method_1019(way.method_1021((double)i / steps));
         class_2338 feet = StandPicker.standable(level, class_2338.method_49637(at.field_1352, lastY, at.field_1350));
         if (feet == null || Math.abs(feet.method_10264() - lastY) > 1) {
            return false;
         }

         lastY = feet.method_10264();
      }

      return true;
   }

   private static class_2338 roofView(class_638 level, class_746 player, class_2338 roof) {
      class_2338 here = player.method_24515();
      class_2338 best = null;
      double nearest = Double.MAX_VALUE;
      double reach = player.method_55754() - 0.2;

      for (class_2338 cell : class_2338.method_10097(here.method_10069(-4, -1, -4), here.method_10069(4, 1, 4))) {
         class_2338 feet = StandPicker.standable(level, cell);
         if (feet != null && feet.equals(cell) && walkable(level, player.method_73189(), class_243.method_24955(feet))) {
            class_243 eye = class_243.method_24955(feet).method_1031(0.0, player.method_5751(), 0.0);
            double distance = feet.method_10262(here);
            if (!(distance >= nearest)) {
               for (class_2350 face : class_2350.values()) {
                  class_243 point = class_243.method_24953(roof).method_43206(face, 0.5);
                  if (!(eye.method_1022(point) > reach)) {
                     class_3965 hit = level.method_17742(
                        new class_3959(eye, point.method_43206(face, -0.05), class_3960.field_17559, class_242.field_1348, player)
                     );
                     if (hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(roof) && hit.method_17780() == face) {
                        best = feet.method_10062();
                        nearest = distance;
                        break;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean mine(class_638 level, class_746 player, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      int slot = toolSlot(player.method_31548(), state);
      if (slot < 0) {
         return false;
      } else {
         class_3965 face = visibleFace(level, player, pos);
         if (face == null) {
            return false;
         } else if (!this.hands()) {
            return true;
         } else {
            Rotation sent = ServerRotation.sent();
            class_3965 held = sent == null ? null : AnchorGuard.trace(level, player, sent);
            if (held == null || !held.method_17777().equals(pos)) {
               ServerRotation.request(RotationUtils.fromTo(player.method_33571(), face.method_17784()), 6.0F);
               return true;
            } else if (player.method_31548().method_67532() != slot) {
               player.method_31548().method_61496(slot);
               return true;
            } else {
               ServerRotation.request(sent);
               if (MeleeAttacks.actedThisTick(player)) {
                  return true;
               } else {
                  if (this.mc.field_1761.method_2902(pos, held.method_17780())) {
                     player.method_6104(class_1268.field_5808);
                  }

                  MeleeAttacks.noteAction(player);
                  this.diggingAt = player.field_6012;
                  return true;
               }
            }
         }
      }
   }

   public static boolean digging() {
      CrystalBotModule module = ModuleRegistry.get(CrystalBotModule.class);
      class_746 player = class_310.method_1551().field_1724;
      return module != null && module.isEnabled() && player != null && player.field_6012 - module.diggingAt <= 1;
   }

   private static int toolSlot(class_1661 inventory, class_2680 state) {
      int best = -1;
      float fastest = 0.0F;

      for (int i = 0; i < 9; i++) {
         class_1799 stack = inventory.method_5438(i);
         boolean fits = stack.method_7951(state) || !state.method_29291();
         if (fits && !(stack.method_7924(state) <= fastest)) {
            best = i;
            fastest = stack.method_7924(state);
         }
      }

      return best;
   }

   private static class_3965 visibleFace(class_638 level, class_746 player, class_2338 pos) {
      class_243 eye = player.method_33571();
      class_3965 best = null;
      double nearest = player.method_55754() * player.method_55754();

      for (class_2350 face : class_2350.values()) {
         class_243 point = class_243.method_24953(pos).method_43206(face, 0.5);
         double distance = eye.method_1025(point);
         if (!(distance > nearest)) {
            class_3965 hit = AnchorGuard.trace(level, player, RotationUtils.fromTo(eye, point.method_43206(face, -0.05)));
            if (hit != null && hit.method_17777().equals(pos) && hit.method_17780() == face) {
               best = hit;
               nearest = distance;
            }
         }
      }

      return best;
   }

   private boolean camp(class_638 level, class_746 player, class_1657 target) {
      class_2338 feet = target.method_24515();
      if (level.method_18467(class_1511.class, new class_238(feet).method_1009(3.0, 1.0, 3.0)).size() >= 2) {
         return false;
      } else {
         class_243 eye = player.method_33571();
         class_2338 best = null;
         float least = this.selfCapNow - 2.0F;
         double hit = player.method_55755() - 0.1;

         for (class_2338 cell : class_2338.method_10097(feet.method_10069(-2, 0, -2), feet.method_10069(2, 0, 2))) {
            if (Math.max(Math.abs(cell.method_10263() - feet.method_10263()), Math.abs(cell.method_10260() - feet.method_10260())) == 2) {
               class_2338 base = cell.method_10074().method_10062();
               if (base.method_10264() >= feetY(player)
                  && Crystals.isBase(level.method_8320(base))
                  && Crystals.canHost(level, base, null)
                  && !(eye.method_1025(TargetingUtils.closestPoint(eye, Crystals.boxOn(base))) > hit * hit)) {
                  float own = CrystalDamage.estimate(Crystals.explosionAt(base), player);
                  if (!(own > least) && !CrystalDamage.endangers(player, own) && topAim(level, player, base) != null) {
                     best = base;
                     least = own;
                  }
               }
            }
         }

         if (best == null) {
            return false;
         } else {
            this.body.face(this.mc, player, target, 1.0F);
            this.placeCrystal(level, player, best);
            return true;
         }
      }
   }

   private static class_243 topAim(class_638 level, class_746 player, class_2338 base) {
      class_243 eye = player.method_33571();
      double[][] spots = new double[][]{{0.5, 0.5}, {0.2, 0.2}, {0.8, 0.2}, {0.2, 0.8}, {0.8, 0.8}};

      for (double[] at : spots) {
         class_243 point = new class_243(base.method_10263() + at[0], base.method_10264() + 1.0, base.method_10260() + at[1]);
         if (!(eye.method_1022(point) > player.method_55754() - 0.1)) {
            class_3965 hit = AnchorGuard.trace(level, player, RotationUtils.fromTo(eye, point));
            if (hit != null && hit.method_17777().equals(base) && hit.method_17780() == class_2350.field_11036) {
               return point;
            }
         }
      }

      return null;
   }

   private void placeCrystal(class_638 level, class_746 player, class_2338 base) {
      int slot = BotBody.hotbarSlot(player, stack -> stack.method_31574(class_1802.field_8301));
      if (slot >= 0 && this.hands()) {
         Rotation sent = ServerRotation.sent();
         class_3965 hit = sent == null ? null : AnchorGuard.trace(level, player, sent);
         if (hit == null || !hit.method_17777().equals(base) || hit.method_17780() != class_2350.field_11036) {
            class_243 aim = topAim(level, player, base);
            if (aim != null) {
               ServerRotation.request(RotationUtils.fromTo(player.method_33571(), aim), 6.0F);
            }
         } else if (player.method_31548().method_67532() != slot) {
            player.method_31548().method_61496(slot);
         } else if (MeleeAttacks.handsFree(this.mc, player)) {
            if (Crystals.rightClick(this.mc, player, hit)) {
               Crystals.notePlace(player, base);
               MeleeAttacks.claimTick(this.mc);
            }

            ServerRotation.request(sent);
         }
      }
   }

   private boolean closeSpot(class_638 level, class_746 player, class_1657 target) {
      int slot = guardBlockSlot(player);
      if (slot < 0) {
         return false;
      } else {
         if (this.blocking == null && this.tick >= this.spotScanAt) {
            this.spotScanAt = this.tick + 5;
            this.blocking = dangerSpot(level, player, target);
         }

         if (this.blocking == null) {
            return false;
         } else if (level.method_8320(this.blocking).method_45474()
            && level.method_8628(class_2246.field_10540.method_9564(), this.blocking, class_3726.method_16194())) {
            AnchorGuard.Spot spot = AnchorGuard.supportFor(level, player, player.method_33571(), this.blocking);
            if (spot == null) {
               this.blocking = null;
               return false;
            } else {
               if (this.placeAt(level, player, spot, slot) == 1) {
                  this.blocking = null;
               }

               return true;
            }
         } else {
            this.blocking = null;
            return false;
         }
      }
   }

   private static class_2338 dangerSpot(class_638 level, class_746 player, class_1657 target) {
      class_2338 feet = player.method_24515();
      class_243 theirEye = target.method_33571();
      double reach = Math.max(4.6, player.method_55754() + 0.1);
      class_2338 worst = null;
      float first = 0.0F;
      float second = 0.0F;

      for (class_2338 base : class_2338.method_10097(feet.method_10069(-4, -2, -4), feet.method_10069(4, 1, 4))) {
         if (Crystals.isBase(level.method_8320(base))
            && Crystals.canHost(level, base, null)
            && !(theirEye.method_1022(new class_243(base.method_10263() + 0.5, base.method_10264() + 1.0, base.method_10260() + 0.5)) > reach)) {
            class_243 center = Crystals.explosionAt(base);
            float us = CrystalDamage.estimate(center, player);
            if (!(us <= second) && !(CrystalDamage.estimate(center, target) >= us + 2.0F)) {
               if (us > first) {
                  second = first;
                  first = us;
                  worst = base.method_10084().method_10062();
               } else {
                  second = us;
               }
            }
         }
      }

      return first >= 8.0F && second <= first / 2.0F ? worst : null;
   }

   private int placeAt(class_638 level, class_746 player, AnchorGuard.Spot spot, int slot) {
      if (!this.hands()) {
         return 0;
      } else {
         Rotation sent = ServerRotation.sent();
         class_3965 hit = sent == null ? null : AnchorGuard.hitAlong(level, player, sent, spot);
         if (hit == null) {
            ServerRotation.request(RotationUtils.fromTo(player.method_33571(), spot.aim()), 6.0F);
            return 0;
         } else if (player.method_31548().method_67532() != slot) {
            player.method_31548().method_61496(slot);
            return 0;
         } else if (!MeleeAttacks.handsFree(this.mc, player)) {
            return 0;
         } else {
            AnchorGuard.click(this.mc, player, hit);
            ServerRotation.request(sent);
            return 1;
         }
      }
   }

   private boolean defuse(class_638 level, class_746 player) {
      if (this.tick - this.defuseAt >= 10 && level.method_27983() != class_1937.field_25180) {
         class_2338 feet = player.method_24515();

         for (class_2338 pos : class_2338.method_10097(feet.method_10069(-5, -2, -5), feet.method_10069(5, 3, 5))) {
            class_2680 state = level.method_8320(pos);
            if (state.method_27852(class_2246.field_23152)
               && (Integer)state.method_11654(class_4969.field_23153) > 0
               && player.method_56093(pos, -0.1)
               && !pos.equals(this.digAnchor)) {
               class_2338 anchor = pos.method_10062();
               class_243 center = class_243.method_24953(anchor);
               if (!(CrystalDamage.estimate(center, player, 5.0F, anchor) > Math.max(1.0F, 0.15F * player.method_6032()))) {
                  boolean theirs = false;

                  for (class_1657 enemy : enemies(level, player, 16.0)) {
                     theirs |= enemy.method_33571().method_1022(center) <= 5.5;
                  }

                  if (theirs) {
                     class_1661 inventory = player.method_31548();
                     int route = AnchorGuard.detonatorRoute(inventory, player.method_6047());
                     if (route == -1 || !this.hands()) {
                        return false;
                     }

                     class_3965 hit = AnchorGuard.anchorHit(level, player, anchor);
                     if (hit != null) {
                        if (route != -2) {
                           inventory.method_61496(route);
                           return true;
                        }

                        if (!MeleeAttacks.handsFree(this.mc, player)) {
                           return true;
                        }

                        AnchorGuard.click(this.mc, player, hit);
                        this.defuseAt = this.tick;
                        return true;
                     }

                     class_243 aim = AnchorGuard.anchorAim(level, player, anchor);
                     if (aim != null) {
                        ServerRotation.request(RotationUtils.fromTo(player.method_33571(), aim), 6.0F);
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean guard(class_638 level, class_746 player, class_1657 target) {
      float health = player.method_6032() + player.method_6067();
      class_2338 danger = null;
      float worst = 0.0F;
      class_2338 feet = player.method_24515();

      for (class_2338 pos : class_2338.method_10097(feet.method_10069(-5, -2, -5), feet.method_10069(5, 3, 5))) {
         if (level.method_8320(pos).method_27852(class_2246.field_23152) && !pos.equals(this.digAnchor)) {
            class_243 center = class_243.method_24953(pos);
            float hurt = CrystalDamage.estimate(center, player, 5.0F, pos.method_10062());
            if ((!(hurt < Math.max(4.0F, health * 0.3F)) || !(hurt < health - 1.0F)) && !(target.method_33571().method_1022(center) > 5.5) && !(hurt <= worst)) {
               worst = hurt;
               danger = pos.method_10062();
            }
         }
      }

      if (danger == null) {
         return false;
      } else {
         AnchorGuard.Spot spot = AnchorGuard.find(level, player, danger, player.method_33571());
         if (spot != null && spot.standing()) {
            return false;
         } else {
            int slot = guardBlockSlot(player);
            if (spot != null && slot >= 0 && this.tick - this.shieldAt >= 20) {
               this.body.face(this.mc, player, target, 1.0F);
               if (this.placeAt(level, player, spot, slot) == 1) {
                  this.shieldAt = this.tick;
               }

               return true;
            } else {
               this.body.face(this.mc, player, target, 1.0F);
               this.body.away(this.mc, player, class_243.method_24953(danger));
               return true;
            }
         }
      }
   }

   private static int guardBlockSlot(class_746 player) {
      int obsidian = BotBody.hotbarSlot(player, OBSIDIAN);
      return obsidian >= 0 ? obsidian : BotBody.hotbarSlot(player, AnchorGuard::isGuardBlock);
   }

   private boolean cover(class_638 level, class_746 player, class_1657 target, double distance) {
      boolean boxing = this.boxingAt != null && this.boxingAt.equals(player.method_24515()) && this.tick < this.boxingUntil;
      boolean wants = player.method_6032() + player.method_6067() <= this.retreatAt() + 4.0F || this.stance == CrystalBotModule.Stance.GUARD;
      if (this.surround.get() && !boxing && wants && player.method_24828()) {
         this.boxingAt = player.method_24515();
         this.boxingUntil = this.tick + 60;
         boxing = true;
      }

      if (!boxing) {
         this.boxingAt = null;
      }

      return this.surround.get() && boxing && player.method_24828() && this.boxIn(level, player);
   }

   private boolean boxIn(class_638 level, class_746 player) {
      List<class_2338> open = openAroundFeet(level, player);
      if (!open.isEmpty() && BotBody.hotbarSlot(player, OBSIDIAN) >= 0) {
         if (!this.hands()) {
            return true;
         } else {
            for (class_2338 cell : open) {
               class_3965 support = AutoTrapModule.support(level, player, cell);
               if (support != null && (!cell.equals(this.boxSkipped) || this.tick >= this.boxSkippedUntil)) {
                  class_3965 seen = AutoTrapModule.seenByServer(level, player, support);
                  if (seen != null) {
                     int slot = BotBody.hotbarSlot(player, OBSIDIAN);
                     if (player.method_31548().method_67532() != slot) {
                        player.method_31548().method_61496(slot);
                        return true;
                     }

                     if (!MeleeAttacks.handsFree(this.mc, player)) {
                        return true;
                     }

                     class_1269 result = this.mc.field_1761.method_2896(player, class_1268.field_5808, seen);
                     if (result.method_23665()) {
                        player.method_6104(class_1268.field_5808);
                        MeleeAttacks.noteAction(player);
                        ServerRotation.request(ServerRotation.sent());
                     }

                     return true;
                  }

                  this.boxAimTicks = cell.equals(this.boxAiming) ? this.boxAimTicks + 1 : 0;
                  this.boxAiming = cell;
                  if (this.boxAimTicks < 10) {
                     ServerRotation.request(RotationUtils.fromTo(player.method_33571(), support.method_17784()), 6.0F);
                     return true;
                  }

                  this.boxSkipped = cell;
                  this.boxSkippedUntil = this.tick + 40;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static List<class_2338> openAroundFeet(class_638 level, class_746 player) {
      List<class_2338> open = new ArrayList<>();

      for (class_2338 cell : ring(player)) {
         class_2680 state = level.method_8320(cell);
         if (state.method_45474()
            && !(cell.method_46558().method_1022(player.method_33571()) > player.method_55754())
            && level.method_8628(class_2246.field_10540.method_9564(), cell, class_3726.method_16194())) {
            open.add(cell);
         }
      }

      return open;
   }

   private static boolean surrounded(class_638 level, class_746 player) {
      for (class_2338 cell : ring(player)) {
         if (level.method_8320(cell).method_26220(level, cell).method_1110()) {
            return false;
         }
      }

      return true;
   }

   private static Set<class_2338> ring(class_746 player) {
      class_238 box = player.method_5829();
      int feet = class_3532.method_15357(box.field_1322 + 0.01);
      Set<Long> columns = new LinkedHashSet<>();

      for (int x = class_3532.method_15357(box.field_1323); x <= class_3532.method_15357(box.field_1320 - 1.0E-4); x++) {
         for (int z = class_3532.method_15357(box.field_1321); z <= class_3532.method_15357(box.field_1324 - 1.0E-4); z++) {
            columns.add(class_2338.method_10064(x, 0, z));
         }
      }

      Set<class_2338> ring = new LinkedHashSet<>();

      for (long key : columns) {
         class_2338 column = class_2338.method_10092(key).method_33096(feet);

         for (class_2350 side : class_2353.field_11062) {
            class_2338 cell = column.method_10093(side);
            if (!columns.contains(class_2338.method_10064(cell.method_10263(), 0, cell.method_10260()))) {
               ring.add(cell);
            }
         }
      }

      return ring;
   }

   private static boolean armorLow(class_1309 player) {
      for (class_1304 slot : class_1304.field_54086) {
         if (slot.method_46643()) {
            class_1799 stack = player.method_6118(slot);
            if (stack.method_7963() && stack.method_7936() - stack.method_7919() < stack.method_7936() * 0.3F) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean throwXp(class_746 player) {
      if (BotBody.hotbarSlot(player, XP) < 0) {
         return false;
      } else {
         this.mending = true;
         Aim.turn(this.mc, player.method_36454(), 86.0F, 22.0F);
         if (player.method_36455() > 70.0F) {
            this.body.use(this.mc, player, XP);
         }

         return true;
      }
   }

   private boolean mendIdle(class_746 player) {
      if (this.mendArmor.get() && (armorLow(player) || this.mending) && hasDamagedArmor(player)) {
         CombatMacroLock.take(this);
         return this.throwXp(player);
      } else {
         return false;
      }
   }

   private static boolean hasDamagedArmor(class_746 player) {
      for (class_1304 slot : class_1304.field_54086) {
         if (slot.method_46643() && player.method_6118(slot).method_7986()) {
            return true;
         }
      }

      return false;
   }

   private static int count(class_746 player, Predicate<class_1799> want) {
      int count = 0;
      class_1661 inventory = player.method_31548();

      for (int i = 0; i < 36; i++) {
         if (want.test(inventory.method_5438(i))) {
            count += inventory.method_5438(i).method_7947();
         }
      }

      return count + (want.test(player.method_6079()) ? player.method_6079().method_7947() : 0);
   }

   @Environment(EnvType.CLIENT)
   private static enum Stance {
      PRESS(3.5F, 10.0F, 5.0F, 0.7),
      HOLD(4.5F, 8.0F, 6.0F, 1.0),
      GUARD(6.0F, 5.0F, 7.0F, 1.6),
      RUN(99.0F, 3.0F, 99.0F, 2.0);

      final float minDamage;
      final float selfCap;
      final float anchorMin;
      final double cover;

      private Stance(float minDamage, float selfCap, float anchorMin, double cover) {
         this.minDamage = minDamage;
         this.selfCap = selfCap;
         this.anchorMin = anchorMin;
         this.cover = cover;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Throw {
      ESCAPE,
      CHASE;
   }
}
