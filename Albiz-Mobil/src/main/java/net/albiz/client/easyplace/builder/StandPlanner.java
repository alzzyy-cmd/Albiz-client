package net.albiz.client.easyplace.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2346;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
final class StandPlanner {
   private static final double PILLAR_COST = 8.0;
   private static final double BRIDGE_COST = 5.0;
   private static final double BUILD_BRIDGE_COST = 2.0;
   private static final double PROP_PENALTY = 8.0;
   private static final double LEAP_COST = 2.5;
   private static final double WADE_COST = 2.5;
   private static final double ICE_COST = 2.0;
   private static final int MAX_GAP = 1;
   private static final double FALL_COST = 40.0;
   private static final double DROP_COST = 2.0;
   private static final int BRIDGE_LIMIT = 10;
   private static final double EYE = 1.62;
   private static final double CROUCH_EYE = 1.27;
   private static final int MAX_SPOTS = 12000;
   private static final double SLACK = 10.0;
   private static final double GAIN = 2.0;
   private static final int MAX_CANDIDATES = 16;
   private static final int COUNT_CAP = 24;
   private static final int RANGE = 40;
   private static final int AROUND = 8;
   private static final double[][] FACE_POINTS = new double[][]{{0.5, 0.5}, {0.2, 0.5}, {0.8, 0.5}, {0.5, 0.2}, {0.5, 0.8}};
   private static final double[][] EDGE_POINTS = new double[][]{{0.5, 0.5}, {0.03, 0.5}, {0.97, 0.5}, {0.5, 0.03}, {0.5, 0.97}};
   private static final int MAX_LEAVES = 10;
   private static final long[] NO_LEAVES = new long[0];
   private final class_638 level;
   private final class_746 player;
   private final List<StandPlanner.Target> targets;
   private final Predicate<class_2338> avoid;
   private final double reach;
   private final StandPlanner.Pillar canPillar;
   private final Predicate<class_2338> canBridge;
   private final StandPlanner.Exact exact;
   private final StandPlanner.Support support;
   private double maxFall;
   private final int maxFeetY;
   private final class_238 around;
   private final class_238 targetBox;
   private final class_2338 start;
   private final PriorityQueue<StandPlanner.Node> open = new PriorityQueue<>(Comparator.comparingDouble(StandPlanner.Node::rank));
   private final Map<Long, Double> best = new HashMap<>();
   private final Map<Long, Long> parent = new HashMap<>();
   private final Map<Long, StandPlanner.Move> moves = new HashMap<>();
   private final Map<Long, Integer> bridges = new HashMap<>();
   private final Map<Long, long[]> leaves = new HashMap<>();
   private final Map<Long, StandPlanner.Target> bridgeTargets = new HashMap<>();
   private int visited;
   private List<StandPlanner.Step> route;
   private StandPlanner.Target found;
   private class_2338 bestSpot;
   private double bestValue = -Double.MAX_VALUE;
   private double firstCost = -1.0;
   private int candidates;
   private boolean propped;
   private class_2338 closest;
   private double closestDistance = Double.MAX_VALUE;
   private final double startDistance;

   StandPlanner(
      class_638 level,
      class_746 player,
      List<StandPlanner.Target> targets,
      Predicate<class_2338> avoid,
      StandPlanner.Pillar canPillar,
      Predicate<class_2338> canBridge,
      StandPlanner.Exact exact,
      StandPlanner.Support support
   ) {
      this.level = level;
      this.player = player;
      this.targets = targets;
      this.avoid = avoid;
      this.canPillar = canPillar;
      this.canBridge = canBridge;
      this.exact = exact;
      this.support = support;
      this.reach = player.method_55754() - 0.25;
      this.start = player.method_24515();

      for (StandPlanner.Target target : targets) {
         class_2680 expected = target.expected();
         if (target.use() == StandPlanner.Use.PLACE
            && StateRules.lookFor(expected) == null
            && StateRules.flatLookFor(expected) == null
            && !(expected.method_26204() instanceof class_2346)
            && expected.method_26234(level, target.pos())) {
            this.bridgeTargets.put(target.pos().method_10063(), target);
         }
      }

      class_238 box = null;

      for (StandPlanner.Target targetx : targets) {
         box = box == null ? new class_238(targetx.pos()) : box.method_991(new class_238(targetx.pos()));
      }

      this.maxFeetY = box == null ? this.start.method_10264() : (int)box.field_1325 + 1;
      this.around = box == null ? new class_238(this.start) : box.method_991(new class_238(this.start)).method_1014(8.0);
      this.targetBox = box == null ? new class_238(this.start) : box.method_1014(8.0);
      this.startDistance = this.workDistance(this.start);
      this.open.add(new StandPlanner.Node(this.start.method_10063(), 0.0, 0.0));
      this.best.put(this.start.method_10063(), 0.0);
      this.parent.put(this.start.method_10063(), Long.MIN_VALUE);
      this.leaves.put(this.start.method_10063(), NO_LEAVES);
   }

   int visited() {
      return this.visited;
   }

   int lowestTargetY() {
      return (int)Math.round(this.targetBox.field_1322) + 8;
   }

   String extent() {
      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxY = Integer.MIN_VALUE;
      int minZ = Integer.MAX_VALUE;
      int maxZ = Integer.MIN_VALUE;
      int pillars = 0;
      int bridgesSeen = 0;

      for (Entry<Long, StandPlanner.Move> entry : this.moves.entrySet()) {
         class_2338 pos = class_2338.method_10092(entry.getKey());
         minX = Math.min(minX, pos.method_10263());
         maxX = Math.max(maxX, pos.method_10263());
         minY = Math.min(minY, pos.method_10264());
         maxY = Math.max(maxY, pos.method_10264());
         minZ = Math.min(minZ, pos.method_10260());
         maxZ = Math.max(maxZ, pos.method_10260());
         if (entry.getValue() == StandPlanner.Move.PILLAR) {
            pillars++;
         }

         if (entry.getValue() == StandPlanner.Move.BRIDGE) {
            bridgesSeen++;
         }
      }

      return "x "
         + minX
         + ".."
         + maxX
         + " y "
         + minY
         + ".."
         + maxY
         + " z "
         + minZ
         + ".."
         + maxZ
         + " pillars "
         + pillars
         + " bridges "
         + bridgesSeen
         + " targetBox "
         + this.targetBox
         + " maxFeetY "
         + this.maxFeetY
         + " | "
         + this.whyStuck();
   }

   String whyStuck() {
      boolean near = this.around.method_1006(this.start.method_46558());
      StringBuilder out = new StringBuilder(
         "near=" + near + " around=" + this.around + " maxFeetY=" + this.maxFeetY + " avoidUp=" + this.avoid.test(this.start.method_10086(2))
      );
      if (this.canPillar != null) {
         out.append(" pillar=").append(this.canPillar.possible(this.start, false));
      }

      out.append(" floor=").append(Walker.floor(this.level, this.start));
      if (this.canBridge != null) {
         for (class_2350 way : class_2353.field_11062) {
            class_2338 next = this.start.method_10093(way);
            out.append(' ')
               .append(way)
               .append(":side=")
               .append(class_2248.method_9501(this.level.method_8320(this.start.method_10074()).method_26218(this.level, this.start.method_10074()), way))
               .append(",gap=")
               .append(Double.isNaN(Walker.floor(this.level, next)))
               .append(",room=")
               .append(Walker.roomAt(this.level, next))
               .append(",fits=")
               .append(this.canBridge.test(next.method_10074()))
               .append(",avoid=")
               .append(this.avoid.test(next));
         }
      }

      return out.toString();
   }

   StandPlanner falls(double blocks) {
      this.maxFall = blocks;
      return this;
   }

   List<StandPlanner.Step> route() {
      return this.route;
   }

   StandPlanner.Target target() {
      return this.found;
   }

   StandPlanner.Status step(long deadline) {
      while (!this.open.isEmpty()) {
         if (System.nanoTime() > deadline) {
            return StandPlanner.Status.RUNNING;
         }

         StandPlanner.Node node = this.open.poll();
         if (!(node.cost() > this.best.getOrDefault(node.pos(), Double.MAX_VALUE))) {
            if (this.firstCost >= 0.0 && (node.cost() > this.firstCost + 10.0 || this.candidates >= 16)) {
               return this.finish();
            }

            if (++this.visited > 12000) {
               return this.finish();
            }

            class_2338 spot = class_2338.method_10092(node.pos());
            if (!spot.equals(this.start)) {
               double distance = this.workDistance(spot);
               if (distance < this.closestDistance) {
                  this.closestDistance = distance;
                  this.closest = spot;
               }

               long[] planned = this.leaves.getOrDefault(node.pos(), NO_LEAVES);
               StandPlanner.Target target = this.moves.get(node.pos()) == StandPlanner.Move.BRIDGE
                  ? this.bridgeTargets.get(spot.method_10074().method_10063())
                  : null;
               this.propped = false;
               if (target == null) {
                  target = this.workFrom(spot, planned);
               }

               boolean needsProp = this.propped;
               if (target != null) {
                  double value = this.countFrom(spot, planned) * 2.0
                     - node.cost()
                     - (needsProp && this.bridgeTargets.containsKey(target.pos().method_10063()) ? 8.0 : 0.0);
                  if (this.firstCost < 0.0) {
                     this.firstCost = node.cost();
                  }

                  this.candidates++;
                  if (value > this.bestValue) {
                     this.bestValue = value;
                     this.bestSpot = spot;
                     this.found = target;
                  }
               }
            }

            for (class_2338 next : Walker.steps(this.level, spot)) {
               int rise = next.method_10264() - spot.method_10264();
               double wade = (this.level.method_8316(next).method_15769() ? 0.0 : 2.5) + (Walker.icy(this.level, next) ? 2.0 : 0.0);
               this.relax(spot, next, node.cost() + 1.0 + (rise > 0 ? 0.6 : -rise * 2.0) + wade, StandPlanner.Move.WALK);
            }

            if (this.maxFall > 0.0) {
               for (class_2350 way : class_2353.field_11062) {
                  class_2338 landing = Walker.fall(this.level, spot, way, this.maxFall);
                  if (landing != null) {
                     this.relax(spot, landing, node.cost() + 40.0 + (spot.method_10264() - landing.method_10264()) * 2.0, StandPlanner.Move.WALK);
                  }
               }
            }

            double fromY = Walker.floorOr(this.level, spot);

            for (class_2350 wayx : class_2353.field_11062) {
               for (int gap = 1; gap <= 1; gap++) {
                  if (Walker.leap(this.level, spot, fromY, wayx, gap)) {
                     this.relax(spot, spot.method_10079(wayx, gap + 1), node.cost() + 2.5 + gap, StandPlanner.Move.LEAP);
                  }
               }
            }

            boolean near = this.around.method_1006(spot.method_46558()) || spot.method_19455(this.start) <= 4;
            boolean under = near
               || spot.method_10264() < this.targetBox.field_1322
                  && spot.method_10263() + 0.5 >= this.targetBox.field_1323
                  && spot.method_10263() + 0.5 <= this.targetBox.field_1320
                  && spot.method_10260() + 0.5 >= this.targetBox.field_1321
                  && spot.method_10260() + 0.5 <= this.targetBox.field_1324;
            boolean onLeaf = this.moves.get(node.pos()) == StandPlanner.Move.PILLAR || this.moves.get(node.pos()) == StandPlanner.Move.BRIDGE;
            if (under && this.canPillar != null && spot.method_10264() + 1 <= this.maxFeetY && this.canPillar.possible(spot, onLeaf)) {
               if (spot.method_10264() + 2 <= this.maxFeetY) {
                  this.relax(spot, spot.method_10086(2), node.cost() + 8.0, StandPlanner.Move.PILLAR);
               }

               if (spot.method_10264() + 1 >= this.lowestTargetY()) {
                  this.relax(spot, spot.method_10086(1), node.cost() + 4.8, StandPlanner.Move.PILLAR);
               }
            }

            if (near && spot.method_10264() >= this.targetBox.field_1322 && this.canBridge != null && this.bridges.getOrDefault(node.pos(), 0) < 10) {
               this.bridgeFrom(spot, node.cost());
            }
         }
      }

      return this.finish();
   }

   private StandPlanner.Status finish() {
      if (this.bestSpot == null) {
         if (this.closest != null && !(this.closestDistance > this.startDistance - 2.0)) {
            this.route = this.path(this.closest);
            return StandPlanner.Status.APPROACH;
         } else {
            return StandPlanner.Status.NONE;
         }
      } else {
         this.route = this.path(this.bestSpot);
         return StandPlanner.Status.FOUND;
      }
   }

   private int countFrom(class_2338 feet, long[] planned) {
      double y = Walker.floorOr(this.level, feet);
      class_243 eye = new class_243(feet.method_10263() + 0.5, y + 1.62, feet.method_10260() + 0.5);
      class_238 body = new class_238(feet.method_10263() + 0.2, y, feet.method_10260() + 0.2, feet.method_10263() + 0.8, y + 1.8, feet.method_10260() + 0.8);
      int count = 0;

      for (StandPlanner.Target target : this.targets) {
         class_2338 pos = target.pos();
         if (!(pos.method_46558().method_1022(eye) > Math.min(this.reach, target.within()))
            && !body.method_994(new class_238(pos))
            && (
               target.use() == StandPlanner.Use.PLACE
                  ? this.seenFrom(eye, target) || this.onPlanned(eye, target, planned)
                  : this.placeableFrom(eye, target, planned)
            )) {
            if (++count >= 24) {
               break;
            }
         }
      }

      return count;
   }

   private boolean seenFrom(class_243 eye, StandPlanner.Target target) {
      class_2350 look = StateRules.lookFor(target.expected());
      class_2350 flat = StateRules.flatLookFor(target.expected());

      for (class_2350 side : class_2350.values()) {
         class_2338 support = target.pos().method_10093(side);
         class_2680 state = this.level.method_8320(support);
         if (!state.method_45474()) {
            class_265 shape = state.method_26218(this.level, support);
            if (!shape.method_1110()) {
               class_2350 face = side.method_10153();
               class_238 box = shape.method_1107().method_996(support);
               if (this.seen(eye, support, box, face, look, flat)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void bridgeFrom(class_2338 spot, double cost) {
      StandPlanner.Move how = this.moves.get(spot.method_10063());
      boolean onLeaf = how == StandPlanner.Move.BRIDGE || how == StandPlanner.Move.PILLAR;
      if (onLeaf || Walker.floor(this.level, spot) == spot.method_10264()) {
         class_2338 under = spot.method_10074();

         for (class_2350 way : class_2353.field_11062) {
            if (onLeaf || class_2248.method_9501(this.level.method_8320(under).method_26218(this.level, under), way)) {
               class_2338 next = spot.method_10093(way);
               if (Double.isNaN(Walker.floor(this.level, next)) && Walker.roomAt(this.level, next)) {
                  boolean build = this.bridgeTargets.containsKey(next.method_10074().method_10063());
                  if (build || this.canBridge.test(next.method_10074())) {
                     this.relax(spot, next, cost + (build ? 2.0 : 5.0), StandPlanner.Move.BRIDGE);
                  }
               }
            }
         }
      }
   }

   private void relax(class_2338 from, class_2338 next, double cost, StandPlanner.Move move) {
      if (!this.avoid.test(next)) {
         if (Math.abs(next.method_10263() - this.start.method_10263()) <= 40
            && Math.abs(next.method_10260() - this.start.method_10260()) <= 40
            && Math.abs(next.method_10264() - this.start.method_10264()) <= 40
            && next.method_10264() <= this.maxFeetY + 1) {
            long key = next.method_10063();
            if (!(cost >= this.best.getOrDefault(key, Double.MAX_VALUE))) {
               this.best.put(key, cost);
               this.parent.put(key, from.method_10063());
               this.moves.put(key, move);
               this.bridges.put(key, this.bridges.getOrDefault(from.method_10063(), 0) + (move == StandPlanner.Move.BRIDGE ? 1 : 0));
               long[] before = this.leaves.getOrDefault(from.method_10063(), NO_LEAVES);
               this.leaves
                  .put(
                     key,
                     move != StandPlanner.Move.BRIDGE && move != StandPlanner.Move.PILLAR
                        ? before
                        : withLeaf(before, next.method_10074(), move == StandPlanner.Move.PILLAR)
                  );
               this.open.add(new StandPlanner.Node(key, cost, cost + this.guess(next)));
            }
         }
      }
   }

   private double workDistance(class_2338 spot) {
      class_243 eye = new class_243(spot.method_10263() + 0.5, spot.method_10264() + 1.62, spot.method_10260() + 0.5);
      double best = Double.MAX_VALUE;

      for (int i = 0; i < Math.min(32, this.targets.size()); i++) {
         best = Math.min(best, this.targets.get(i).pos().method_46558().method_1022(eye));
      }

      return best;
   }

   private double guess(class_2338 spot) {
      class_243 eye = new class_243(spot.method_10263() + 0.5, spot.method_10264() + 1.62, spot.method_10260() + 0.5);
      double dx = Math.max(0.0, Math.max(this.targetBox.field_1323 + 8.0 - eye.field_1352, eye.field_1352 - (this.targetBox.field_1320 - 8.0)));
      double dy = Math.max(0.0, Math.max(this.targetBox.field_1322 + 8.0 - eye.field_1351, eye.field_1351 - (this.targetBox.field_1325 - 8.0))) * 6.5;
      double dz = Math.max(0.0, Math.max(this.targetBox.field_1321 + 8.0 - eye.field_1350, eye.field_1350 - (this.targetBox.field_1324 - 8.0)));
      return Math.max(0.0, Math.sqrt(dx * dx + dy * dy + dz * dz) - this.reach) * 0.6;
   }

   private List<StandPlanner.Step> path(class_2338 end) {
      List<StandPlanner.Step> out = new ArrayList<>();
      long at = end.method_10063();

      while (at != this.start.method_10063() && at != Long.MIN_VALUE) {
         out.add(new StandPlanner.Step(class_2338.method_10092(at), this.moves.getOrDefault(at, StandPlanner.Move.WALK)));
         at = this.parent.get(at);
      }

      Collections.reverse(out);
      return out;
   }

   private StandPlanner.Target workFrom(class_2338 feet, long[] planned) {
      if ((this.level.method_8316(feet).method_15769() || this.level.method_8316(feet).method_15771()) && !Walker.nearLava(this.level, feet)) {
         double y = Walker.floorOr(this.level, feet);
         class_243 eye = new class_243(feet.method_10263() + 0.5, y + 1.62, feet.method_10260() + 0.5);
         class_238 body = new class_238(feet.method_10263() + 0.2, y, feet.method_10260() + 0.2, feet.method_10263() + 0.8, y + 1.8, feet.method_10260() + 0.8);

         for (StandPlanner.Target target : this.targets) {
            class_2338 pos = target.pos();
            if (!(pos.method_46558().method_1025(eye) > (this.reach + 0.9) * (this.reach + 0.9))
               && !(pos.method_46558().method_1022(eye) > target.within())
               && (
                  !body.method_994(new class_238(pos))
                     || target.use() == StandPlanner.Use.PLACE && target.expected().method_26220(this.level, pos).method_1110()
               )
               && this.placeableFrom(eye, target, planned)) {
               return target;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean placeableFrom(class_243 eye, StandPlanner.Target target, long[] planned) {
      class_2338 pos = target.pos();
      switch (target.use()) {
         case WATERLOG:
         case BREAK:
         case CLICK:
            if (pos.method_46558().method_1022(eye) > this.reach) {
               return false;
            } else {
               class_265 shape = this.level.method_8320(pos).method_26218(this.level, pos);
               if (shape.method_1110()) {
                  return false;
               } else {
                  class_238 box = shape.method_1107().method_996(pos);

                  for (class_2350 face : class_2350.values()) {
                     if (facesEye(box, face, eye) && this.seen(eye, pos, box, face, null, null)) {
                        return true;
                     }
                  }

                  return false;
               }
            }
         case NEAR:
            return pos.method_46558().method_1022(eye) <= this.reach - 0.5 && this.support != null && this.support.possible(pos, eye);
         default:
            class_2350 look = target.use() == StandPlanner.Use.PLACE ? StateRules.lookFor(target.expected()) : null;
            class_2350 flat = target.use() == StandPlanner.Use.PLACE ? StateRules.flatLookFor(target.expected()) : null;

            for (class_2350 side : class_2350.values()) {
               class_2338 support = pos.method_10093(side);
               class_2680 state = this.level.method_8320(support);
               if (!state.method_45474()) {
                  class_265 shapex = state.method_26218(this.level, support);
                  if (!shapex.method_1110()) {
                     class_2350 facex = side.method_10153();
                     class_238 box = shapex.method_1107().method_996(support);
                     if (this.seen(eye, support, box, facex, look, flat)
                        && (target.use() != StandPlanner.Use.PLACE || this.exact == null || this.exact.placeable(target.expected(), pos, eye))) {
                        return true;
                     }
                  }
               }
            }

            if (this.onPlanned(eye, target, planned)) {
               return true;
            } else if (target.use() != StandPlanner.Use.PLACE) {
               return false;
            } else if (StateRules.slabHalf(this.level.method_8320(pos), target.expected())) {
               return this.exact != null && this.exact.placeable(target.expected(), pos, eye);
            } else {
               this.propped = this.support != null && this.support.behind(target.expected(), pos, eye);
               return this.propped;
            }
      }
   }

   private boolean onPlanned(class_243 eye, StandPlanner.Target target, long[] planned) {
      if (planned.length == 0) {
         return false;
      } else if (target.use() != StandPlanner.Use.PLACE || StateRules.lookFor(target.expected()) == null && StateRules.flatLookFor(target.expected()) == null) {
         class_2338 pos = target.pos();

         for (long key : planned) {
            class_2338 leaf = class_2338.method_10092(key);
            if (leaf.method_19455(pos) == 1 && this.level.method_8320(leaf).method_45474()) {
               class_2350 face = class_2350.method_10147(
                  pos.method_10263() - leaf.method_10263(), pos.method_10264() - leaf.method_10264(), pos.method_10260() - leaf.method_10260()
               );
               class_238 box = new class_238(leaf);
               if (facesEye(box, face, eye)) {
                  for (double[] point : FACE_POINTS) {
                     class_243 aim = facePoint(box, face, point[0], point[1]);
                     if (!(aim.method_1022(eye) > this.reach)) {
                        class_243 shy = aim.method_1020(aim.method_1020(eye).method_1029().method_1021(0.05));
                        if (this.level.method_17742(new class_3959(eye, shy, class_3960.field_17559, class_242.field_1348, this.player)).method_17783()
                           == class_240.field_1333) {
                           return true;
                        }
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

   private static long[] withLeaf(long[] before, class_2338 leaf, boolean pillar) {
      long[] out = new long[Math.min(before.length + 1, 10)];
      int n = 0;

      for (int i = Math.max(0, before.length + 1 - 10); i < before.length; i++) {
         class_2338 old = class_2338.method_10092(before[i]);
         if (!pillar || old.method_10263() != leaf.method_10263() || old.method_10260() != leaf.method_10260() || old.method_10264() >= leaf.method_10264()) {
            out[n++] = before[i];
         }
      }

      out[n++] = leaf.method_10063();
      return n == out.length ? out : Arrays.copyOf(out, n);
   }

   private boolean seen(class_243 eye, class_2338 block, class_238 box, class_2350 face, class_2350 look, class_2350 flat) {
      return this.seenFrom(eye, block, box, face, look, flat, FACE_POINTS, 0.2)
         ? true
         : look == class_2350.field_11036 && this.seenFrom(eye.method_1023(0.0, 0.3500000000000001, 0.0), block, box, face, look, flat, EDGE_POINTS, 0.05);
   }

   private boolean seenFrom(class_243 eye, class_2338 block, class_238 box, class_2350 face, class_2350 look, class_2350 flat, double[][] points, double margin) {
      if (!facesEye(box, face, eye)) {
         return false;
      } else {
         for (double[] point : points) {
            class_243 aim = facePoint(box, face, point[0], point[1]);
            if (!(aim.method_1022(eye) > this.reach)
               && (look == null || Walker.dominant(aim.method_1020(eye), look, margin))
               && (flat == null || Walker.facingFlat(aim.method_1020(eye), flat))) {
               class_3965 hit = this.level
                  .method_17742(
                     new class_3959(
                        eye, aim.method_1019(aim.method_1020(eye).method_1029().method_1021(0.05)), class_3960.field_17559, class_242.field_1348, this.player
                     )
                  );
               if (hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(block) && hit.method_17780() == face) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean facesEye(class_238 box, class_2350 face, class_243 eye) {
      double gap = 0.05;

      return switch (face) {
         case field_11036 -> eye.field_1351 > box.field_1325 + gap;
         case field_11033 -> eye.field_1351 < box.field_1322 - gap;
         case field_11043 -> eye.field_1350 < box.field_1321 - gap;
         case field_11035 -> eye.field_1350 > box.field_1324 + gap;
         case field_11039 -> eye.field_1352 < box.field_1323 - gap;
         case field_11034 -> eye.field_1352 > box.field_1320 + gap;
         default -> throw new MatchException(null, null);
      };
   }

   private static class_243 facePoint(class_238 box, class_2350 face, double u, double v) {
      double x = box.field_1323 + (box.field_1320 - box.field_1323) * u;
      double y = box.field_1322 + (box.field_1325 - box.field_1322) * v;

      return switch (face) {
         case field_11036 -> new class_243(x, box.field_1325, box.field_1321 + (box.field_1324 - box.field_1321) * v);
         case field_11033 -> new class_243(x, box.field_1322, box.field_1321 + (box.field_1324 - box.field_1321) * v);
         case field_11043 -> new class_243(x, y, box.field_1321);
         case field_11035 -> new class_243(x, y, box.field_1324);
         case field_11039 -> new class_243(box.field_1323, y, box.field_1321 + (box.field_1324 - box.field_1321) * u);
         case field_11034 -> new class_243(box.field_1320, y, box.field_1321 + (box.field_1324 - box.field_1321) * u);
         default -> throw new MatchException(null, null);
      };
   }

   @Environment(EnvType.CLIENT)
   interface Exact {
      boolean placeable(class_2680 var1, class_2338 var2, class_243 var3);
   }

   @Environment(EnvType.CLIENT)
   static enum Move {
      WALK,
      PILLAR,
      BRIDGE,
      LEAP;
   }

   @Environment(EnvType.CLIENT)
   private record Node(long pos, double cost, double rank) {
   }

   @Environment(EnvType.CLIENT)
   interface Pillar {
      boolean possible(class_2338 var1, boolean var2);
   }

   @Environment(EnvType.CLIENT)
   static enum Status {
      RUNNING,
      FOUND,
      NONE,
      APPROACH;
   }

   @Environment(EnvType.CLIENT)
   record Step(class_2338 pos, StandPlanner.Move move) {
   }

   @Environment(EnvType.CLIENT)
   interface Support {
      boolean possible(class_2338 var1, class_243 var2);

      boolean behind(class_2680 var1, class_2338 var2, class_243 var3);
   }

   @Environment(EnvType.CLIENT)
   record Target(class_2338 pos, class_2680 expected, StandPlanner.Use use, double within) {
      Target(class_2338 pos, class_2680 expected, StandPlanner.Use use) {
         this(pos, expected, use, Double.MAX_VALUE);
      }
   }

   @Environment(EnvType.CLIENT)
   static enum Use {
      PLACE,
      POUR,
      WATERLOG,
      NEAR,
      BREAK,
      CLICK;
   }
}
