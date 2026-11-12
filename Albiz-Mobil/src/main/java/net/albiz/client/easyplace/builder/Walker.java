package net.albiz.client.easyplace.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.albiz.client.easyplace.compat.BaritoneCompat;
import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2350.class_2353;

@Environment(EnvType.CLIENT)
final class Walker {
   private static final int STILL_LIMIT = 60;
   private static final int GRACE = 40;
   private static final int TIME_LIMIT = 600;
   private static final double EYE = 1.62;
   private static final double HEIGHT = 1.8;
   private static final double JUMP = 1.2;
   private static final float SWIM_DEPTH = 0.4F;
   private static final double FULL = 0.999;
   private static final double STEP_UP = 0.55;
   private static final double MAX_DROP = 3.0;
   private static final double LEAP_ROOM = 2.6;
   private class_2338 goal;
   private long started;
   private long lastMove;
   private class_243 lastPos;

   boolean active() {
      return this.goal != null;
   }

   class_2338 goal() {
      return this.goal;
   }

   void go(class_746 player, class_2338 feet, long tick) {
      this.goal = feet;
      this.started = tick;
      this.lastMove = tick;
      this.lastPos = player.method_73189();
      BaritoneCompat.goTo(feet);
   }

   Walker.Result tick(class_746 player, long tick) {
      if (this.goal == null) {
         return Walker.Result.FAILED;
      } else {
         class_243 pos = player.method_73189();
         double dx = pos.field_1352 - (this.goal.method_10263() + 0.5);
         double dz = pos.field_1350 - (this.goal.method_10260() + 0.5);
         if (dx * dx + dz * dz < 0.36 && Math.abs(pos.field_1351 - this.goal.method_10264()) < 0.6 && player.method_24828()) {
            this.goal = null;
            return Walker.Result.ARRIVED;
         } else {
            if (pos.method_1025(this.lastPos) > 0.0025) {
               this.lastPos = pos;
               this.lastMove = tick;
            }

            if ((tick - this.started <= 40L || tick - this.lastMove <= 60L) && tick - this.started <= 600L) {
               return Walker.Result.RUNNING;
            } else {
               this.stop();
               return Walker.Result.FAILED;
            }
         }
      }
   }

   void stop() {
      if (this.goal != null) {
         BaritoneCompat.cancel();
      }

      this.goal = null;
   }

   static class_2338 standFor(class_638 level, class_746 player, class_2338 target, class_2680 expected, Predicate<class_2338> avoid) {
      return standFor(level, player, target, expected, avoid, player.method_55754() - 0.7);
   }

   static class_2338 standFor(class_638 level, class_746 player, class_2338 target, class_2680 expected, Predicate<class_2338> avoid, double reach) {
      class_2350 look = StateRules.lookFor(expected);
      class_2350 compass = StateRules.flatLookFor(expected);
      class_243 from = player.method_73189();
      int playerFeet = player.method_24515().method_10264();
      class_2338 best = null;
      double bestScore = Double.MAX_VALUE;

      for (int dy = -1; dy <= 2; dy++) {
         for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
               class_2338 feet = target.method_10069(dx, dy, dz);
               if (!feet.equals(target) && !feet.method_10084().equals(target) && !avoid.test(feet) && standable(level, feet)) {
                  class_243 eye = new class_243(feet.method_10263() + 0.5, floor(level, feet) + 1.62, feet.method_10260() + 0.5);
                  class_243 to = target.method_46558().method_1020(eye);
                  if (!(to.method_1033() > reach) && (look == null || dominant(to, look)) && (compass == null || facingFlat(to, compass))) {
                     double flat = Math.hypot(from.field_1352 - (feet.method_10263() + 0.5), from.field_1350 - (feet.method_10260() + 0.5));
                     int up = feet.method_10264() - playerFeet;
                     double score = flat + (up > 0 ? up * 4.0 : -up * 0.3) + Math.abs(dy) * 0.2;
                     if (score < bestScore) {
                        bestScore = score;
                        best = feet;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   static class_2338 stepToward(class_638 level, class_746 player, class_2338 target, Predicate<class_2338> avoid) {
      if (!player.method_24828()) {
         return null;
      } else {
         class_2338 feet = player.method_24515();
         class_243 aim = target.method_46558();
         double now = player.method_33571().method_1022(aim);
         class_2338 best = null;
         double bestDist = now - 0.3;

         for (class_2338 spot : steps(level, feet)) {
            if (!avoid.test(spot)) {
               double dist = new class_243(spot.method_10263() + 0.5, floor(level, spot) + 1.62, spot.method_10260() + 0.5).method_1022(aim);
               if (dist < bestDist) {
                  bestDist = dist;
                  best = spot;
               }
            }
         }

         return best;
      }
   }

   static List<class_2338> steps(class_638 level, class_2338 feet) {
      return steps(level, feet, true);
   }

   private static List<class_2338> steps(class_638 level, class_2338 feet, boolean noPits) {
      List<class_2338> out = new ArrayList<>();
      double from = floorOr(level, feet);

      for (class_2350 direction : class_2353.field_11062) {
         class_2338 side = feet.method_10093(direction);

         for (int dy = 1; dy >= -3; dy--) {
            class_2338 spot = side.method_10086(dy);
            double to = floor(level, spot);
            if (!Double.isNaN(to)) {
               boolean intoWater = from - to > 0.6 && !level.method_8316(spot).method_15769();
               boolean pit = noPits && from - to > 0.01 && steps(level, spot, false).isEmpty();
               if (to - from <= 1.2 && from - to <= 3.0 && !intoWater && !pit && !nearLava(level, spot) && passage(level, feet, from, spot, to)) {
                  out.add(spot);
               }
               break;
            }
         }
      }

      return out;
   }

   static String explain(class_638 level, class_2338 feet) {
      StringBuilder out = new StringBuilder("from " + feet.method_23854() + " floor " + floor(level, feet) + ":");
      double from = floorOr(level, feet);

      for (class_2350 direction : class_2353.field_11062) {
         class_2338 side = feet.method_10093(direction);
         out.append(" ").append(direction).append("[");

         for (int dy = 1; dy >= -3; dy--) {
            class_2338 spot = side.method_10086(dy);
            double to = floor(level, spot);
            out.append(dy)
               .append(":")
               .append(Double.isNaN(to) ? "-" : String.format("%.2f%s", to, to - from <= 1.2 && passage(level, feet, from, spot, to) ? "ok" : "x"))
               .append(" ");
         }

         out.append("]");
      }

      return out.toString();
   }

   static class_2338 fall(class_638 level, class_2338 feet, class_2350 way, double maxFall) {
      double from = floorOr(level, feet);
      class_2338 side = feet.method_10093(way);
      if (Double.isNaN(floor(level, side)) && roomAt(level, side)) {
         for (int depth = 1; depth <= Math.ceil(maxFall) + 1.0; depth++) {
            class_2338 spot = side.method_10087(depth);
            double to = floor(level, spot);
            if (!level.method_8316(spot).method_15769()) {
               return null;
            }

            if (!Double.isNaN(to)) {
               if (!(from - to <= 3.0) && !(from - to > maxFall) && !nearLava(level, spot) && passage(level, feet, from, spot, to)) {
                  return spot;
               }

               return null;
            }

            if (!level.method_8320(spot).method_26220(level, spot).method_1110()) {
               return null;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static double floor(class_638 level, class_2338 feet) {
      if (!level.method_22340(feet)) {
         return Double.NaN;
      } else if (level.method_8316(feet).method_15767(class_3486.field_15517) && level.method_8316(feet).method_15763(level, feet) > 0.4F) {
         return Double.NaN;
      } else {
         double own = topUnderFoot(level, feet);
         double y;
         if (!Double.isNaN(own)) {
            if (own > 0.999) {
               return Double.NaN;
            }

            y = feet.method_10264() + own;
         } else {
            if (level.method_8316(feet).method_15767(class_3486.field_15518) || level.method_8320(feet).method_27852(class_2246.field_10422)) {
               return Double.NaN;
            }

            class_2338 below = feet.method_10074();
            double ground = topUnderFoot(level, below);
            if (Double.isNaN(ground) || ground <= 0.999) {
               return Double.NaN;
            }

            y = below.method_10264() + ground;
         }

         if (!level.method_8316(feet.method_10084()).method_15769()) {
            return Double.NaN;
         } else {
            double x = feet.method_10263();
            double z = feet.method_10260();
            if (!level.method_18026(new class_238(x + 0.2, y + 0.01, z + 0.2, x + 0.8, y + 1.8, z + 0.8))) {
               return Double.NaN;
            } else {
               return level.method_18026(new class_238(x + 0.2, y - 0.05, z + 0.2, x + 0.8, y - 0.001, z + 0.8)) ? Double.NaN : y;
            }
         }
      }
   }

   static boolean roomAt(class_638 level, class_2338 feet) {
      if (level.method_22340(feet) && level.method_8316(feet).method_15769() && level.method_8316(feet.method_10084()).method_15769()) {
         double x = feet.method_10263();
         double y = feet.method_10264();
         double z = feet.method_10260();
         return level.method_18026(new class_238(x + 0.2, y + 0.01, z + 0.2, x + 0.8, y + 1.8, z + 0.8));
      } else {
         return false;
      }
   }

   static boolean leap(class_638 level, class_2338 from, double fromY, class_2350 way) {
      return leap(level, from, fromY, way, 1);
   }

   static boolean leap(class_638 level, class_2338 from, double fromY, class_2350 way, int gap) {
      class_2338 spot = from.method_10079(way, gap + 1);
      double to = floor(level, spot);
      if (!Double.isNaN(to) && !(Math.abs(to - fromY) > 0.1)) {
         for (int i = 1; i <= gap; i++) {
            class_2338 cell = from.method_10079(way, i);
            if (!Double.isNaN(floor(level, cell)) || !level.method_8316(cell).method_15769()) {
               return false;
            }

            if (!safeFall(level, cell)) {
               return false;
            }

            for (class_2350 side : new class_2350[]{way.method_10170(), way.method_10160()}) {
               class_2338 beside = cell.method_10093(side);
               if (Double.isNaN(floor(level, beside)) && !safeFall(level, beside)) {
                  return false;
               }
            }
         }

         class_2338 beyond = spot.method_10093(way);
         if (Double.isNaN(floor(level, beyond)) && !safeFall(level, beyond)) {
            return false;
         } else {
            class_238 over = column(from, fromY, fromY).method_991(column(spot, fromY, fromY)).method_35578(fromY + 2.6);
            return level.method_18026(over);
         }
      } else {
         return false;
      }
   }

   private static boolean safeFall(class_638 level, class_2338 cell) {
      for (int depth = 1; depth <= 3; depth++) {
         class_2338 below = cell.method_10087(depth);
         if (level.method_8316(below).method_15767(class_3486.field_15518) || level.method_8320(below).method_26164(class_3481.field_21952)) {
            return false;
         }

         if (!level.method_8320(below).method_26220(level, below).method_1110()) {
            return true;
         }
      }

      return false;
   }

   private static double topUnderFoot(class_638 level, class_2338 pos) {
      class_265 shape = level.method_8320(pos).method_26220(level, pos);
      if (shape.method_1110()) {
         return Double.NaN;
      } else {
         double top = Double.NaN;

         for (class_238 box : shape.method_1090()) {
            if (!(box.field_1320 <= 0.2) && !(box.field_1323 >= 0.8) && !(box.field_1324 <= 0.2) && !(box.field_1321 >= 0.8)) {
               top = Double.isNaN(top) ? box.field_1325 : Math.max(top, box.field_1325);
            }
         }

         return top;
      }
   }

   static boolean risky(class_638 level, class_2338 feet) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx != 0 || dz != 0) {
               class_2338 side = feet.method_10069(dx, 0, dz);
               if (Double.isNaN(floor(level, side))) {
                  boolean beside = dx == 0 || dz == 0;

                  for (int depth = 0; depth <= 24; depth++) {
                     class_2338 below = side.method_10087(depth);
                     if (level.method_8316(below).method_15767(class_3486.field_15518) || level.method_8320(below).method_26164(class_3481.field_21952)) {
                        if (depth <= 4) {
                           return true;
                        }
                        break;
                     }

                     boolean deep = depth > (beside ? 4 : 8);
                     if (level.method_8316(below).method_15767(class_3486.field_15517)) {
                        if (deep) {
                           return true;
                        }
                        break;
                     }

                     if (!level.method_8320(below).method_26220(level, below).method_1110()) {
                        if (deep) {
                           return true;
                        }
                        break;
                     }

                     if (depth == 24) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   static boolean icy(class_638 level, class_2338 feet) {
      return level.method_8320(feet.method_10074()).method_26164(class_3481.field_15467) || level.method_8320(feet).method_26164(class_3481.field_15467);
   }

   static int dropAt(class_638 level, class_2338 feet) {
      if (!level.method_22340(feet)) {
         return 0;
      } else if (level.method_8316(feet).method_15767(class_3486.field_15518)) {
         return 24;
      } else if (!level.method_8320(feet).method_26220(level, feet).method_1110()) {
         return 0;
      } else {
         int depth = 0;

         while (depth < 24) {
            class_2338 below = feet.method_10087(depth + 1);
            if (!level.method_8316(below).method_15767(class_3486.field_15518) && !level.method_8320(below).method_26164(class_3481.field_21952)) {
               if (level.method_8316(below).method_15769() && level.method_8320(below).method_26220(level, below).method_1110()) {
                  depth++;
                  continue;
               }

               return depth;
            }

            return 24;
         }

         return 24;
      }
   }

   static boolean nearLava(class_638 level, class_2338 feet) {
      for (class_2338 cell : new class_2338[]{
         feet, feet.method_10084(), feet.method_10074(), feet.method_10095(), feet.method_10072(), feet.method_10078(), feet.method_10067()
      }) {
         if (level.method_8316(cell).method_15767(class_3486.field_15518)) {
            return true;
         }
      }

      return false;
   }

   static double floorOr(class_638 level, class_2338 feet) {
      double y = floor(level, feet);
      return Double.isNaN(y) ? feet.method_10264() : y;
   }

   private static boolean passage(class_638 level, class_2338 feet, double from, class_2338 spot, double to) {
      double top = Math.max(from, to);
      class_238 here = column(feet, from, top);
      class_238 there = column(spot, to, top);
      class_238 across = new class_238(
         Math.min(here.field_1323, there.field_1323),
         top + 0.55,
         Math.min(here.field_1321, there.field_1321),
         Math.max(here.field_1320, there.field_1320),
         top + 1.8,
         Math.max(here.field_1324, there.field_1324)
      );
      return level.method_18026(here) && level.method_18026(there) && level.method_18026(across);
   }

   private static class_238 column(class_2338 pos, double bottom, double top) {
      return new class_238(pos.method_10263() + 0.2, bottom + 0.01, pos.method_10260() + 0.2, pos.method_10263() + 0.8, top + 1.8, pos.method_10260() + 0.8);
   }

   static class_2338 stepAside(class_638 level, class_746 player, Predicate<class_2338> avoid) {
      class_2338 at = player.method_24515();
      class_2338 best = null;
      double bestScore = Double.MAX_VALUE;

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (dx != 0 || dz != 0) {
                  class_2338 feet = at.method_10069(dx, dy, dz);
                  if (!avoid.test(feet) && standable(level, feet)) {
                     double score = Math.abs(dx) + Math.abs(dz) + Math.abs(dy) * 1.5;
                     if (score < bestScore) {
                        bestScore = score;
                        best = feet;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   static boolean standable(class_638 level, class_2338 feet) {
      return !Double.isNaN(floor(level, feet));
   }

   static boolean facingFlat(class_243 to, class_2350 look) {
      double along = to.field_1352 * look.method_10148() + to.field_1350 * look.method_10165();
      double across = Math.abs(to.field_1352 * look.method_10165()) + Math.abs(to.field_1350 * look.method_10148());
      return along > across + 0.1;
   }

   static boolean dominant(class_243 to, class_2350 look) {
      return dominant(to, look, 0.2);
   }

   static boolean dominant(class_243 to, class_2350 look, double margin) {
      double along = to.field_1352 * look.method_10148() + to.field_1351 * look.method_10164() + to.field_1350 * look.method_10165();
      if (along <= 0.0) {
         return false;
      } else {
         double ax = Math.abs(to.field_1352);
         double ay = Math.abs(to.field_1351);
         double az = Math.abs(to.field_1350);

         return switch (look.method_10166()) {
            case field_11048 -> ax > ay + margin && ax > az + margin;
            case field_11052 -> ay > ax + margin && ay > az + margin;
            case field_11051 -> az > ax + margin && az > ay + margin;
            default -> throw new MatchException(null, null);
         };
      }
   }

   @Environment(EnvType.CLIENT)
   static enum Result {
      RUNNING,
      ARRIVED,
      FAILED;
   }
}
