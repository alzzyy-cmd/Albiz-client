package net.albiz.client.easyplace.builder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.easyplace.inventory.InventoryCalm;
import net.albiz.client.event.EventPriority;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.MouseInputEvent;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.misc.FreecamModule;
import net.albiz.client.module.setting.ActionSetting;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.module.setting.StringSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public final class AreaMinerModule extends ClientModule {
   private static final long PLAN_BUDGET_NANOS = 3000000L;
   private static final int PLAN_TARGETS = 400;
   private static final int BREAK_TRIES = 3;
   private static final int PLAN_FAILS = 2;
   private static final int SCAN_TICKS = 10;
   private static final int MEND_TICKS = 1200;
   private static final int MAX_CELLS = 2000000;
   private static final int AREA_COLOR = -11155201;
   private static final int TARGET_COLOR = -20416;
   private static final Predicate<class_1799> XP = stack -> stack.method_31574(class_1802.field_8287);
   private final StringSetting corner1 = this.addSetting("corner-1", new StringSetting("Corner 1", "").hint("x y z"));
   private final StringSetting corner2 = this.addSetting("corner-2", new StringSetting("Corner 2", "").hint("x y z"));
   private final ActionSetting select = this.addSetting("select-area", new ActionSetting("Select Area With Clicks", this::selectArea));
   private final ActionSetting feet1 = this.addSetting("corner-1-feet", new ActionSetting("Corner 1 At Feet", () -> this.setCorner(this.corner1, false)));
   private final ActionSetting look1 = this.addSetting("corner-1-look", new ActionSetting("Corner 1 At Crosshair", () -> this.setCorner(this.corner1, true)));
   private final ActionSetting feet2 = this.addSetting("corner-2-feet", new ActionSetting("Corner 2 At Feet", () -> this.setCorner(this.corner2, false)));
   private final ActionSetting look2 = this.addSetting("corner-2-look", new ActionSetting("Corner 2 At Crosshair", () -> this.setCorner(this.corner2, true)));
   private final BooleanSetting useShard = this.addSetting("use-shard", new BooleanSetting("Use Shard Pickaxe", true));
   private final BooleanSetting eat = this.addSetting("eat", new BooleanSetting("Eat When Hungry", true));
   private final BooleanSetting mend = this.addSetting("mend", new BooleanSetting("Mend With XP Bottles", true));
   private final NumberSetting mendBelow = this.addSetting("mend-below", new NumberSetting("Mend Below Durability", 150.0F, 20.0F, 1000.0F).step(10.0F));
   private final BooleanSetting pauseInMenus = this.addSetting("pause-in-menus", new BooleanSetting("Pause In Menus", true));
   private final BooleanSetting showArea = this.addSetting("show-area", new BooleanSetting("Show Area", true));
   private final BooleanSetting chatFeedback = this.addSetting("chat-feedback", new BooleanSetting("Chat Feedback", true));
   private final class_310 mc = class_310.method_1551();
   private class_2338 min;
   private class_2338 max;
   private class_638 seenLevel;
   private long tick;
   private long nextScan;
   private long noPlanUntil;
   private Breaker breaker;
   private boolean breakerShard;
   private Hop hop;
   private Eater eater;
   private StandPlanner planner;
   private List<StandPlanner.Step> route;
   private int routeIndex;
   private class_2338 routeStart;
   private boolean mending;
   private long mendStarted;
   private int mined;
   private int startCount;
   private int layer = Integer.MIN_VALUE;
   private final List<class_2338> layerCells = new ArrayList<>();
   private final Map<Long, Integer> breakTries = new HashMap<>();
   private final Map<Long, Integer> planFails = new HashMap<>();
   private final Set<Long> unreachable = new HashSet<>();
   private final Set<Long> holding = new HashSet<>();
   private final Map<String, Long> lastFeedback = new HashMap<>();
   private int picking;
   private class_2338 firstPick;

   public AreaMinerModule() {
      super(
         "area-miner",
         "Area Miner",
         "Mines out everything between two corners, top layer first, walking and turning by hand like a player. A Shard Pickaxe cuts 3x3 wherever the whole cut stays inside the area; blocks holding back lava or water stay.",
         ModuleCategory.MISC
      );
      this.usesNotifications();
      this.mendBelow.visibleWhen(this.mend::get);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   protected void onEnable() {
      class_746 player = this.mc.field_1724;
      if (player != null && this.mc.field_1687 != null) {
         this.on(TickEvent.TYPE, Stage.PRE, event -> this.step());
         this.on(Render3DEvent.TYPE, Stage.POST, this::render);
         this.on(MouseInputEvent.TYPE, Stage.PRE, EventPriority.HIGH, this::onMouse);
         class_2338 a = parse(this.corner1.get());
         class_2338 b = parse(this.corner2.get());
         if (a != null && b != null) {
            if (!this.begin(a, b)) {
               this.requestDisable();
            }
         } else {
            this.startPicking();
         }
      } else {
         this.say(ModuleChat.body().warn("Join a world first."));
         this.requestDisable();
      }
   }

   private boolean begin(class_2338 a, class_2338 b) {
      class_2338 low = new class_2338(
         Math.min(a.method_10263(), b.method_10263()), Math.min(a.method_10264(), b.method_10264()), Math.min(a.method_10260(), b.method_10260())
      );
      class_2338 high = new class_2338(
         Math.max(a.method_10263(), b.method_10263()), Math.max(a.method_10264(), b.method_10264()), Math.max(a.method_10260(), b.method_10260())
      );
      long volume = (long)(high.method_10263() - low.method_10263() + 1)
         * (high.method_10264() - low.method_10264() + 1)
         * (high.method_10260() - low.method_10260() + 1);
      if (volume > 2000000L) {
         this.say(ModuleChat.body().warn("The area is too big").text(" (" + volume + " blocks, at most 2000000)."));
         return false;
      } else {
         this.reset();
         this.min = low;
         this.max = high;
         this.seenLevel = this.mc.field_1687;
         this.startCount = this.countLeft(this.mc.field_1687);
         this.say(ModuleChat.body().text("Mining " + this.startCount + " blocks between " + this.min.method_23854() + " and " + this.max.method_23854() + "."));
         return true;
      }
   }

   private void startPicking() {
      this.release();
      this.min = null;
      this.max = null;
      this.firstPick = null;
      this.picking = 1;
      this.say(ModuleChat.body().text("Hit the first corner block").text(" (or type both corners in the settings)."));
   }

   private void selectArea() {
      this.corner1.set("");
      this.corner2.set("");
      if (this.isEnabled()) {
         this.startPicking();
      } else {
         this.setEnabled(true);
      }
   }

   private void onMouse(MouseInputEvent event) {
      if (this.picking != 0 && event.button == 0 && event.action == 1) {
         if (this.mc.field_1755 == null && this.mc.field_1724 != null && this.mc.field_1687 != null) {
            event.setCancelled(true);
            class_2338 pos = this.aimedBlock();
            if (pos == null) {
               this.say(ModuleChat.body().warn("No block there").text(", look at a block."));
            } else {
               String text = pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260();
               if (this.picking == 1) {
                  this.firstPick = pos;
                  this.corner1.set(text);
                  this.picking = 2;
                  this.say(ModuleChat.body().text("Corner 1 set to " + pos.method_23854() + ". Hit the second corner block."));
               } else {
                  this.corner2.set(text);
                  this.say(ModuleChat.body().text("Corner 2 set to " + pos.method_23854() + "."));
                  if (this.begin(this.firstPick, pos)) {
                     this.picking = 0;
                  } else {
                     this.say(ModuleChat.body().text("Hit the second corner block again."));
                  }
               }
            }
         }
      }
   }

   private class_2338 aimedBlock() {
      class_746 player = this.mc.field_1724;
      if (player != null && this.mc.field_1687 != null) {
         FreecamModule freecam = ModuleRegistry.get(FreecamModule.class);
         class_243 origin;
         class_243 direction;
         if (freecam != null && freecam.isEnabled()) {
            class_4184 camera = this.mc.field_1773.method_19418();
            origin = camera.method_71156();
            direction = class_243.method_1030(camera.method_19329(), camera.method_19330());
         } else {
            origin = player.method_5836(1.0F);
            direction = player.method_5828(1.0F);
         }

         double reach = this.mc.field_1690.method_38521() * 16.0;
         class_3965 hit = this.mc
            .field_1687
            .method_17742(new class_3959(origin, origin.method_1019(direction.method_1021(reach)), class_3960.field_17559, class_242.field_1348, player));
         return hit.method_17783() == class_240.field_1332 ? hit.method_17777() : null;
      } else {
         return null;
      }
   }

   @Override
   protected void onDisable() {
      this.release();
      this.planner = null;
      this.route = null;
      this.picking = 0;
      this.firstPick = null;
   }

   private void reset() {
      this.tick = 0L;
      this.nextScan = 0L;
      this.noPlanUntil = 0L;
      this.breaker = null;
      this.hop = null;
      this.eater = null;
      this.planner = null;
      this.route = null;
      this.mending = false;
      this.mined = 0;
      this.layer = Integer.MIN_VALUE;
      this.layerCells.clear();
      this.breakTries.clear();
      this.planFails.clear();
      this.unreachable.clear();
      this.holding.clear();
      this.lastFeedback.clear();
   }

   private void step() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null && this.mc.field_1761 != null) {
         if (this.picking == 0 && this.min != null) {
            this.tick++;
            if (level != this.seenLevel || !player.method_5805()) {
               this.say(ModuleChat.body().warn("Stopped").text(", the world changed or you died."));
               this.requestDisable();
            } else if (this.mc.field_1755 != null && this.pauseInMenus.get()) {
               this.release();
            } else if (this.eater != null) {
               if (this.eater.tick(this.mc) != Eater.Result.RUNNING) {
                  this.eater = null;
               }
            } else if (this.mending) {
               this.mend(player);
            } else if (this.breaker != null) {
               class_2680 state = level.method_8320(this.breaker.pos());
               if (state.method_26215() || Hands.hold(this.mc, player, toolFor(player, state, this.breakerShard))) {
                  Breaker.Result result = this.breaker.tick(this.mc);
                  if (result != Breaker.Result.RUNNING) {
                     class_2338 pos = this.breaker.pos();
                     this.breaker = null;
                     if (minable(level, pos) && this.breakTries.merge(pos.method_10063(), 1, Integer::sum) >= 3) {
                        this.unreachable.add(pos.method_10063());
                        this.feedback("gave-up", "Could not mine " + pos.method_23854() + ", leaving it", 40);
                     }

                     this.nextScan = 0L;
                  }
               }
            } else if (this.hop != null) {
               Hop.Result result = this.hop.tick(this.mc);
               if (result != Hop.Result.RUNNING) {
                  this.hop = null;
                  if (result == Hop.Result.FAILED) {
                     this.route = null;
                     this.noPlanUntil = this.tick + 20L;
                  }
               }
            } else if (this.eat.get() && Eater.hungry(player) && Eater.hasFood(player) && InventoryCalm.still(player)) {
               this.release();
               this.eater = new Eater();
            } else if (this.mendDue(player)) {
               this.mending = true;
               this.mendStarted = this.tick;
               this.feedback("mend", "Tool is worn, mending it with XP bottles", 200);
            } else if (this.route != null) {
               this.followRoute(level, player);
            } else if (this.planner != null) {
               this.followPlanner();
            } else {
               if (this.tick >= this.nextScan) {
                  this.nextScan = this.tick + 10L;
                  if (!this.pickLayer(level)) {
                     this.finish();
                     return;
                  }
               }

               this.dig(level, player);
            }
         }
      }
   }

   private boolean pickLayer(class_638 level) {
      this.layerCells.clear();
      class_2339 cursor = new class_2339();

      for (int y = this.max.method_10264(); y >= this.min.method_10264(); y--) {
         for (int x = this.min.method_10263(); x <= this.max.method_10263(); x++) {
            for (int z = this.min.method_10260(); z <= this.max.method_10260(); z++) {
               cursor.method_10103(x, y, z);
               long key = cursor.method_10063();
               if (!this.unreachable.contains(key) && level.method_22340(cursor) && minable(level, cursor)) {
                  if (this.holdsFluid(level, cursor)) {
                     if (this.holding.add(key)) {
                        this.feedback("fluid", "Leaving " + cursor.method_23854() + ", it holds back lava or water", 100);
                     }
                  } else {
                     this.holding.remove(key);
                     this.layerCells.add(cursor.method_10062());
                  }
               }
            }
         }

         if (!this.layerCells.isEmpty()) {
            if (y != this.layer) {
               this.layer = y;
               this.planFails.clear();
               this.feedback("layer", "Layer Y=" + y + ", " + this.layerCells.size() + (this.layerCells.size() == 1 ? " block" : " blocks"), 1);
            }

            return true;
         }
      }

      return false;
   }

   private void dig(class_638 level, class_746 player) {
      if (!this.layerCells.isEmpty()) {
         class_243 eye = player.method_33571();
         double reach = player.method_55754() - 0.3;
         List<class_2338> near = new ArrayList<>();

         for (class_2338 pos : this.layerCells) {
            if (pos.method_46558().method_1022(eye) <= reach && minable(level, pos)) {
               near.add(pos);
            }
         }

         class_2338 under = player.method_24515().method_10074();
         near.sort(Comparator.<class_2338>comparingInt(posx -> posx.equals(under) ? 1 : 0).thenComparingDouble(posx -> posx.method_46558().method_1025(eye)));

         for (class_2338 posx : near) {
            if (Aim.at(this.mc, posx) != null) {
               class_2680 state = level.method_8320(posx);
               this.breakerShard = this.useShard.get() && this.cutInside(level, posx);
               if (!Hands.hold(this.mc, player, toolFor(player, state, this.breakerShard))) {
                  return;
               }

               this.breaker = new Breaker(posx, true);
               return;
            }
         }

         if (this.tick >= this.noPlanUntil) {
            List<StandPlanner.Target> targets = new ArrayList<>();
            class_243 at = player.method_73189();
            List<class_2338> sorted = new ArrayList<>(this.layerCells);
            sorted.sort(Comparator.comparingDouble(posxx -> posxx.method_19770(at)));

            for (class_2338 posxx : sorted) {
               if (targets.size() >= 400) {
                  break;
               }

               targets.add(new StandPlanner.Target(posxx, level.method_8320(posxx), StandPlanner.Use.BREAK));
            }

            this.planner = new StandPlanner(level, player, targets, feet -> false, null, null, null, null);
            this.followPlanner();
         }
      }
   }

   private void followPlanner() {
      StandPlanner.Status status = this.planner.step(System.nanoTime() + 3000000L);
      if (status != StandPlanner.Status.RUNNING) {
         StandPlanner done = this.planner;
         this.planner = null;
         if ((status == StandPlanner.Status.FOUND || status == StandPlanner.Status.APPROACH) && !done.route().isEmpty()) {
            this.route = done.route();
            this.routeIndex = 0;
            this.routeStart = this.mc.field_1724 == null ? null : this.mc.field_1724.method_24515();
         } else {
            this.noPlanUntil = this.tick + 40L;
            boolean gaveUp = false;

            for (class_2338 pos : this.layerCells) {
               if (this.planFails.merge(pos.method_10063(), 1, Integer::sum) >= 2) {
                  this.unreachable.add(pos.method_10063());
                  gaveUp = true;
               }
            }

            if (gaveUp) {
               this.feedback("unreachable", "No spot gets at the rest of layer Y=" + this.layer + ", leaving those blocks", 40);
            }

            this.nextScan = 0L;
         }
      }
   }

   private void followRoute(class_638 level, class_746 player) {
      if (this.routeIndex >= this.route.size()) {
         this.route = null;
         this.nextScan = 0L;
      } else if (player.method_24828()) {
         class_2338 expected = this.routeIndex == 0 ? this.routeStart : this.route.get(this.routeIndex - 1).pos();
         class_2338 at = player.method_24515();
         if (expected == null
            || at.method_10263() == expected.method_10263()
               && at.method_10260() == expected.method_10260()
               && Math.abs(at.method_10264() - expected.method_10264()) <= 1) {
            StandPlanner.Step next = this.route.get(this.routeIndex++);
            if (next.move() != StandPlanner.Move.WALK && next.move() != StandPlanner.Move.LEAP) {
               this.route = null;
               this.noPlanUntil = this.tick + 40L;
            } else {
               boolean through = next.move() == StandPlanner.Move.WALK
                  && this.routeIndex < this.route.size()
                  && this.route.get(this.routeIndex).move() == StandPlanner.Move.WALK;
               this.hop = new Hop(next.pos(), through, next.move() == StandPlanner.Move.LEAP);
               if (next.move() == StandPlanner.Move.WALK && (Walker.risky(level, at) || Walker.risky(level, next.pos()))) {
                  if (next.pos().method_10264() >= at.method_10264()) {
                     this.hop.careful();
                  } else {
                     this.hop.slow();
                  }
               }
            }
         } else {
            this.release();
            this.route = null;
            this.noPlanUntil = this.tick + 10L;
         }
      }
   }

   private static boolean minable(class_638 level, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      return !state.method_26215() && !(state.method_26204() instanceof class_2404) ? state.method_26214(level, pos) >= 0.0F : false;
   }

   private boolean holdsFluid(class_638 level, class_2338 pos) {
      if (!level.method_8320(pos).method_26227().method_15769()) {
         return true;
      } else {
         for (class_2350 side : class_2350.values()) {
            if (side != class_2350.field_11033 && !level.method_8316(pos.method_10093(side)).method_15769()) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean cutInside(class_638 level, class_2338 pos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               class_2338 cell = pos.method_10069(dx, dy, dz);
               if (!this.inside(cell)) {
                  return false;
               }

               if (minable(level, cell) && this.holdsFluid(level, cell)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private boolean inside(class_2338 pos) {
      return this.min != null
         && pos.method_10263() >= this.min.method_10263()
         && pos.method_10263() <= this.max.method_10263()
         && pos.method_10264() >= this.min.method_10264()
         && pos.method_10264() <= this.max.method_10264()
         && pos.method_10260() >= this.min.method_10260()
         && pos.method_10260() <= this.max.method_10260();
   }

   private static Predicate<class_1799> toolFor(class_746 player, class_2680 state, boolean shard) {
      class_1661 inventory = player.method_31548();
      class_1799 best = class_1799.field_8037;
      float bestSpeed = 1.0F;

      for (int i = 0; i < 36; i++) {
         class_1799 stack = inventory.method_5438(i);
         if (!stack.method_7960() && isShard(stack) == shard) {
            float speed = stack.method_7924(state);
            if (speed > bestSpeed || speed == bestSpeed && !best.method_7960() && stack.method_7951(state) && !best.method_7951(state)) {
               best = stack;
               bestSpeed = speed;
            }
         }
      }

      if (best.method_7960()) {
         return stackx -> !isShard(stackx);
      } else {
         class_1799 chosen = best;
         return stackx -> class_1799.method_31577(stackx, chosen);
      }
   }

   static boolean isShard(class_1799 stack) {
      return stack.method_31573(class_3489.field_42614) && TunnelMinerModule.shardName(stack);
   }

   private boolean mendDue(class_746 player) {
      if (this.mend.get() && (Hands.has(player, XP) || XP.test(player.method_6079()))) {
         class_1799 held = player.method_6047();
         return held.method_7963() && mendable(held) ? held.method_7936() - held.method_7919() < this.mendBelow.get() : false;
      } else {
         return false;
      }
   }

   private void mend(class_746 player) {
      class_1799 held = player.method_6047();
      boolean repaired = held.method_7963() && held.method_7919() == 0;
      boolean out = !XP.test(player.method_6079()) && !Hands.has(player, XP);
      if (repaired || out || this.tick - this.mendStarted > 1200L || !held.method_7963()) {
         this.mc.field_1690.field_1904.method_23481(false);
         this.mending = false;
         if (repaired) {
            this.feedback("mended", "Tool mended", 200);
         } else if (out) {
            this.feedback("no-xp", "Out of XP bottles, mining on", 200);
         }
      } else if (!XP.test(player.method_6079())) {
         this.mc.field_1690.field_1904.method_23481(false);
         if (this.tick % 5L == 0L && InventoryCalm.still(player)) {
            Hands.toOffhand(this.mc, player, XP);
         }
      } else {
         Aim.turn(this.mc, player.method_36454(), 80.0F, 22.0F);
         this.mc.field_1690.field_1904.method_23481(player.method_36455() > 60.0F);
      }
   }

   private static boolean mendable(class_1799 stack) {
      return stack.method_58657().method_57534().stream().anyMatch(enchantment -> enchantment.method_40225(class_1893.field_9101));
   }

   private int countLeft(class_638 level) {
      int count = 0;
      class_2339 cursor = new class_2339();

      for (int y = this.min.method_10264(); y <= this.max.method_10264(); y++) {
         for (int x = this.min.method_10263(); x <= this.max.method_10263(); x++) {
            for (int z = this.min.method_10260(); z <= this.max.method_10260(); z++) {
               cursor.method_10103(x, y, z);
               if (level.method_22340(cursor) && minable(level, cursor)) {
                  count++;
               }
            }
         }
      }

      return count;
   }

   private void finish() {
      this.release();
      class_638 level = this.mc.field_1687;
      int left = level == null ? 0 : this.countLeft(level);
      ModuleChat.Body body = ModuleChat.body().good("Area cleared").text(", " + Math.max(0, this.startCount - left) + " blocks mined.");
      if (!this.unreachable.isEmpty()) {
         body.text(" ")
            .warn(this.unreachable.size() + (this.unreachable.size() == 1 ? " block" : " blocks") + " could not be reached")
            .text(" (" + some(this.unreachable) + ").");
      }

      if (!this.holding.isEmpty()) {
         body.text(
            " "
               + this.holding.size()
               + (this.holding.size() == 1 ? " block was" : " blocks were")
               + " left to hold back lava or water ("
               + some(this.holding)
               + ")."
         );
      }

      this.say(body);
      this.requestDisable();
   }

   private static String some(Set<Long> keys) {
      List<String> shown = new ArrayList<>();

      for (long key : keys) {
         if (shown.size() == 4) {
            break;
         }

         shown.add(class_2338.method_10092(key).method_23854());
      }

      return String.join("; ", shown) + (keys.size() > 4 ? "; ..." : "");
   }

   private void render(Render3DEvent event) {
      if (this.picking != 0) {
         class_2338 aimed = this.aimedBlock();
         class_2338 from = this.picking == 2 ? this.firstPick : aimed;
         class_2338 to = aimed != null ? aimed : from;
         if (from != null) {
            class_12180.method_75541(
               new class_238(
                  Math.min(from.method_10263(), to.method_10263()),
                  Math.min(from.method_10264(), to.method_10264()),
                  Math.min(from.method_10260(), to.method_10260()),
                  Math.max(from.method_10263(), to.method_10263()) + 1,
                  Math.max(from.method_10264(), to.method_10264()) + 1,
                  Math.max(from.method_10260(), to.method_10260()) + 1
               ),
               class_12179.method_75536(-11155201, 2.0F)
            );
            if (this.picking == 2) {
               class_12180.method_75541(new class_238(this.firstPick), class_12179.method_75536(-20416, 2.0F));
            }
         }
      } else if (this.showArea.get() && this.min != null) {
         class_12180.method_75541(
            new class_238(
               this.min.method_10263(),
               this.min.method_10264(),
               this.min.method_10260(),
               this.max.method_10263() + 1,
               this.max.method_10264() + 1,
               this.max.method_10260() + 1
            ),
            class_12179.method_75536(-11155201, 2.0F)
         );
         if (this.breaker != null) {
            class_12180.method_75550(this.breaker.pos(), class_12179.method_75536(-20416, 2.0F));
         }
      }
   }

   private void setCorner(StringSetting corner, boolean looking) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         class_2338 pos = player.method_24515();
         if (looking) {
            if (!(this.mc.field_1765 instanceof class_3965 hit) || hit.method_17783() != class_240.field_1332) {
               this.say(ModuleChat.body().warn("Look at a block first."));
               return;
            }

            pos = hit.method_17777();
         }

         corner.set(pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260());
         this.say(ModuleChat.body().text(corner.name() + " set to " + pos.method_23854() + "."));
      }
   }

   private static class_2338 parse(String text) {
      String[] parts = text.trim().replace(",", " ").split("\\s+");
      if (parts.length != 3) {
         return null;
      } else {
         try {
            return new class_2338(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   private void release() {
      if (this.breaker != null) {
         this.breaker.release(this.mc);
      }

      this.breaker = null;
      if (this.hop != null) {
         this.hop.release(this.mc);
      }

      this.hop = null;
      if (this.eater != null) {
         this.eater.release(this.mc);
      }

      this.eater = null;
      this.mending = false;
      this.mc.field_1690.field_1904.method_23481(false);
      this.mc.field_1690.field_1894.method_23481(false);
      this.mc.field_1690.field_1881.method_23481(false);
      this.mc.field_1690.field_1913.method_23481(false);
      this.mc.field_1690.field_1849.method_23481(false);
      this.mc.field_1690.field_1903.method_23481(false);
      this.mc.field_1690.field_1867.method_23481(false);
      this.mc.field_1690.field_1832.method_23481(false);
      if (this.mc.field_1761 != null) {
         this.mc.field_1761.method_2925();
      }
   }

   private void say(ModuleChat.Body body) {
      ModuleChat.send(this, body);
   }

   private void feedback(String key, String message, int minimumIntervalTicks) {
      if (this.chatFeedback.get()) {
         long last = this.lastFeedback.getOrDefault(key, -4611686018427387904L);
         if (this.tick - last >= minimumIntervalTicks) {
            this.lastFeedback.put(key, this.tick);
            ModuleChat.send(this, message);
         }
      }
   }
}
