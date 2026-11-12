package net.albiz.client.easyplace.builder;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2902.class_2903;

@Environment(EnvType.CLIENT)
final class ElytraFlight {
   static final int BOOST_TICKS = 45;
   static final int HORIZON = 60;
   private static final double CRASH = 1000000.0;
   private static final float[] PITCHES = new float[]{-62.0F, -46.0F, -33.0F, -23.0F, -15.0F, -9.0F, -4.0F, 1.0F, 6.0F, 12.0F, 19.0F, 27.0F, 36.0F};
   private static final float[] NEAR_YAWS = new float[]{0.0F, 12.0F, -12.0F};
   private static final float[] WIDE_YAWS = new float[]{30.0F, -30.0F, 55.0F, -55.0F, 90.0F, -90.0F, 140.0F, -140.0F};
   private static final float[] LAND_PITCHES = new float[]{-28.0F, -18.0F, -10.0F, -4.0F, 0.0F, 4.0F, 8.0F, 12.0F, 16.0F, 21.0F, 27.0F, 34.0F};
   private static final float[] LAND_YAWS = new float[]{0.0F, 18.0F, -18.0F, 36.0F, -36.0F, 60.0F, -60.0F, 90.0F, -90.0F};
   private static final double FOOT = 0.3;

   private ElytraFlight() {
   }

   static float bearing(double fromX, double fromZ, double toX, double toZ) {
      return (float)(class_3532.method_15349(toZ - fromZ, toX - fromX) * 180.0F / (float)Math.PI) - 90.0F;
   }

   static double cost(ElytraFlight.Glide start, ElytraFlight.Goal goal, ElytraFlight.Terrain terrain, float yaw, float pitch, boolean rocket, float maxTurn) {
      ElytraFlight.Glide glide = start.copy();
      if (rocket) {
         glide.boost = 45;
      }

      double before = Math.hypot(goal.x() - glide.x, goal.z() - glide.z);
      double shortfall = 0.0;
      double dive = 0.0;
      double below = 0.0;
      double altitude = 0.0;

      for (int t = 1; t <= 60; t++) {
         glide.step(yaw, pitch, maxTurn);
         double clear = glide.y - terrain.under(glide.x, glide.z);
         if (clear < 0.05) {
            return 1000000.0 + (60 - t) * 1000.0;
         }

         double need = Math.max(goal.minClear(), Math.max(glide.speed() * 5.0, -glide.vy * 25.0));
         if (clear < need) {
            double gap = need - clear;
            shortfall += gap * gap / 2.0 + 2.0 * gap;
         }

         if (glide.vy < -0.5) {
            dive += (-glide.vy - 0.5) * 25.0;
            if (clear < 8.0) {
               dive += (-glide.vy - 0.5) * 30.0;
            }
         }

         if (glide.y < goal.floor()) {
            below += (goal.floor() - glide.y) * 4.0;
         }

         altitude += Math.abs(glide.y - goal.altitude()) * (t < 20 ? 0.4 : 1.0);
      }

      double cost = shortfall / 60.0 * 10.0 + dive / 60.0 * 10.0 + below / 60.0 + altitude / 60.0 * 0.4;
      double off = glide.y - goal.altitude();
      cost += off < 0.0 ? -off * 2.2 : off * (goal.landing() ? 1.6 : 0.6);
      double after = Math.hypot(goal.x() - glide.x, goal.z() - glide.z);
      cost -= before - after;
      if (rocket) {
         cost += goal.rocketCost();
      }

      double speed = glide.flatSpeed();
      if (speed < 0.55) {
         cost += (0.55 - speed) * 60.0;
      }

      if (goal.landing() && after < 120.0 && speed > goal.maxSpeed()) {
         cost += (speed - goal.maxSpeed()) * 40.0;
      }

      return cost;
   }

   static boolean clear(ElytraFlight.Glide start, ElytraFlight.Goal goal, ElytraFlight.Terrain terrain, float yaw, float pitch, boolean rocket, float maxTurn) {
      ElytraFlight.Glide glide = start.copy();
      if (rocket) {
         glide.boost = 45;
      }

      for (int t = 1; t <= 60; t++) {
         glide.step(yaw, pitch, maxTurn);
         if (glide.y - terrain.under(glide.x, glide.z) < goal.minClear()) {
            return false;
         }
      }

      return glide.flatSpeed() > 0.4;
   }

   static ElytraFlight.Plan search(ElytraFlight.Glide now, ElytraFlight.Goal goal, ElytraFlight.Terrain terrain, boolean boosting, float maxTurn) {
      float bearing = bearing(now.x, now.z, goal.x(), goal.z());
      ElytraFlight.Plan best = pass(now, goal, terrain, bearing, NEAR_YAWS, boosting, maxTurn, false, null);
      if (best.safe()) {
         return best;
      } else {
         best = pass(now, goal, terrain, bearing, NEAR_YAWS, boosting, maxTurn, true, best);
         return best.safe() ? best : pass(now, goal, terrain, bearing, WIDE_YAWS, boosting, maxTurn, true, best);
      }
   }

   private static ElytraFlight.Plan pass(
      ElytraFlight.Glide now,
      ElytraFlight.Goal goal,
      ElytraFlight.Terrain terrain,
      float bearing,
      float[] yaws,
      boolean boosting,
      float maxTurn,
      boolean anyPitchRocket,
      ElytraFlight.Plan best
   ) {
      for (float offset : yaws) {
         float yaw = class_3532.method_15393(bearing + offset);

         for (float pitch : PITCHES) {
            for (int r = 0; r < 2; r++) {
               boolean rocket = r == 1;
               if (!rocket || goal.rockets() && !boosting && (anyPitchRocket || !(pitch > -30.0F))) {
                  double cost = cost(now, goal, terrain, yaw, pitch, rocket, maxTurn) + Math.abs(offset) * 0.08;
                  if (best == null || cost < best.cost()) {
                     best = new ElytraFlight.Plan(yaw, pitch, rocket, cost, cost < 400.0);
                  }
               }
            }
         }
      }

      return best;
   }

   static ElytraFlight.Plan touchdown(ElytraFlight.Glide now, class_2338 spot, ElytraFlight.Terrain terrain, float maxTurn) {
      float bearing = bearing(now.x, now.z, spot.method_10263() + 0.5, spot.method_10260() + 0.5);
      ElytraFlight.Plan best = null;

      for (float offset : LAND_YAWS) {
         float yaw = class_3532.method_15393(bearing + offset);

         for (float pitch : LAND_PITCHES) {
            double cost = landing(now, spot, terrain, yaw, pitch, maxTurn) + Math.abs(offset) * 0.15;
            if (best == null || cost < best.cost()) {
               best = new ElytraFlight.Plan(yaw, pitch, false, cost, cost < 900.0);
            }
         }
      }

      return best;
   }

   private static double landing(ElytraFlight.Glide start, class_2338 spot, ElytraFlight.Terrain terrain, float yaw, float pitch, float maxTurn) {
      ElytraFlight.Glide glide = start.copy();
      double ground = terrain.under(glide.x, glide.z);

      for (int t = 1; t <= 200; t++) {
         double lastGround = ground;
         glide.step(yaw, pitch, maxTurn);
         ground = terrain.under(glide.x, glide.z);
         if (!(glide.y > ground)) {
            if (ground - lastGround > 0.6 && glide.flatSpeed() > 0.3) {
               return 1000000.0;
            }

            double cost = Math.hypot(glide.x - (spot.method_10263() + 0.5), glide.z - (spot.method_10260() + 0.5));
            if (glide.vy < -0.5) {
               cost += 200.0;
            }

            if (glide.flatSpeed() > 1.0) {
               cost += (glide.flatSpeed() - 1.0) * 20.0;
            }

            if (!goodFloor(terrain.level(), class_2338.method_49637(glide.x, ground - 1.0, glide.z))) {
               cost += 900.0;
            }

            return cost;
         }
      }

      return 1000000.0;
   }

   static boolean goodFloor(class_638 level, class_2338 floor) {
      if (floor.method_10264() < level.method_31607()) {
         return false;
      } else {
         class_2680 state = level.method_8320(floor);
         if (!state.method_26227().method_15769() || state.method_26164(class_3481.field_15503) || hurts(state)) {
            return false;
         } else if (!state.method_27852(class_2246.field_10030) && !state.method_27852(class_2246.field_21211) && !state.method_27852(class_2246.field_27879)) {
            class_265 shape = state.method_26220(level, floor);
            if (shape.method_1110()) {
               return false;
            } else {
               class_238 box = shape.method_1107();
               if (!(box.field_1325 < 0.45) && !(box.field_1325 > 1.01) && !(box.method_17939() < 0.9) && !(box.method_17941() < 0.9)) {
                  class_2338 above = floor.method_10084();
                  class_2680 over = level.method_8320(above);
                  return over.method_26220(level, above).method_1110() && over.method_26227().method_15769() && !hurts(over);
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }
   }

   private static boolean hurts(class_2680 state) {
      return state.method_27852(class_2246.field_10092)
         || state.method_26164(class_3481.field_23799)
         || state.method_27852(class_2246.field_10029)
         || state.method_27852(class_2246.field_16999)
         || state.method_26164(class_3481.field_21952)
         || state.method_27852(class_2246.field_28048)
         || state.method_27852(class_2246.field_10606)
         || state.method_27852(class_2246.field_10343)
         || state.method_27852(class_2246.field_10164);
   }

   static class_2338 landingSpot(ElytraFlight.Terrain terrain, int centerX, int centerZ, int radius, boolean strict, Predicate<class_2338> banned) {
      class_638 level = terrain.level();

      for (int r = 0; r <= radius; r += 2) {
         for (int dx = -r; dx <= r; dx += 2) {
            for (int dz = -r; dz <= r; dz += 2) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                  int x = centerX + dx;
                  int z = centerZ + dz;
                  if (terrain.known(x - 1, z - 1) && terrain.known(x + 1, z + 1)) {
                     int top = level.method_8624(class_2903.field_13197, x, z);
                     class_2338 spot = new class_2338(x, top, z);
                     if (!banned.test(spot) && flat(level, x, z, top, strict)) {
                        return spot;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static boolean flat(class_638 level, int x, int z, int top, boolean strict) {
      if (!goodFloor(level, new class_2338(x, top - 1, z))) {
         return false;
      } else {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dz != 0) {
                  int height = level.method_8624(class_2903.field_13197, x + dx, z + dz);
                  if (strict ? height != top : Math.abs(height - top) > 1) {
                     return false;
                  }

                  if (!goodFloor(level, new class_2338(x + dx, height - 1, z + dz))) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   static final class Glide {
      double x;
      double y;
      double z;
      double vx;
      double vy;
      double vz;
      float yaw;
      float pitch;
      int boost;
      double gravity;

      static ElytraFlight.Glide of(class_746 player, int boostLeft) {
         ElytraFlight.Glide glide = new ElytraFlight.Glide();
         glide.x = player.method_23317();
         glide.y = player.method_23318();
         glide.z = player.method_23321();
         class_243 motion = player.method_18798();
         glide.vx = motion.field_1352;
         glide.vy = motion.field_1351;
         glide.vz = motion.field_1350;
         glide.yaw = player.method_36454();
         glide.pitch = player.method_36455();
         glide.boost = boostLeft;
         glide.gravity = player.method_6059(class_1294.field_5906) ? 0.01 : 0.08;
         return glide;
      }

      ElytraFlight.Glide copy() {
         ElytraFlight.Glide glide = new ElytraFlight.Glide();
         glide.x = this.x;
         glide.y = this.y;
         glide.z = this.z;
         glide.vx = this.vx;
         glide.vy = this.vy;
         glide.vz = this.vz;
         glide.yaw = this.yaw;
         glide.pitch = this.pitch;
         glide.boost = this.boost;
         glide.gravity = this.gravity;
         return glide;
      }

      void step(float wantYaw, float wantPitch, float maxTurn) {
         this.yaw = this.yaw + class_3532.method_15363(class_3532.method_15393(wantYaw - this.yaw), -maxTurn, maxTurn);
         this.pitch = class_3532.method_15363(this.pitch + class_3532.method_15363(wantPitch - this.pitch, -maxTurn, maxTurn), -90.0F, 90.0F);
         class_243 look = class_243.method_1030(this.pitch, this.yaw);
         double radians = this.pitch * (float) (Math.PI / 180.0);
         double flat = Math.sqrt(look.field_1352 * look.field_1352 + look.field_1350 * look.field_1350);
         double speed = Math.sqrt(this.vx * this.vx + this.vz * this.vz);
         double lift = Math.cos(radians);
         lift *= lift;
         double fall = this.vy <= 0.0 ? this.gravity : 0.08;
         this.vy += fall * (-1.0 + lift * 0.75);
         if (this.vy < 0.0 && flat > 0.0) {
            double turn = this.vy * -0.1 * lift;
            this.vx = this.vx + look.field_1352 * turn / flat;
            this.vy += turn;
            this.vz = this.vz + look.field_1350 * turn / flat;
         }

         if (radians < 0.0 && flat > 0.0) {
            double climb = speed * -Math.sin(radians) * 0.04;
            this.vx = this.vx - look.field_1352 * climb / flat;
            this.vy += climb * 3.2;
            this.vz = this.vz - look.field_1350 * climb / flat;
         }

         if (flat > 0.0) {
            this.vx = this.vx + (look.field_1352 / flat * speed - this.vx) * 0.1;
            this.vz = this.vz + (look.field_1350 / flat * speed - this.vz) * 0.1;
         }

         this.vx *= 0.99;
         this.vy *= 0.98;
         this.vz *= 0.99;
         if (this.boost > 0) {
            this.vx = this.vx + (look.field_1352 * 0.1 + (look.field_1352 * 1.5 - this.vx) * 0.5);
            this.vy = this.vy + (look.field_1351 * 0.1 + (look.field_1351 * 1.5 - this.vy) * 0.5);
            this.vz = this.vz + (look.field_1350 * 0.1 + (look.field_1350 * 1.5 - this.vz) * 0.5);
            this.boost--;
         }

         this.x = this.x + this.vx;
         this.y = this.y + this.vy;
         this.z = this.z + this.vz;
      }

      double speed() {
         return Math.sqrt(this.vx * this.vx + this.vy * this.vy + this.vz * this.vz);
      }

      double flatSpeed() {
         return Math.sqrt(this.vx * this.vx + this.vz * this.vz);
      }
   }

   @Environment(EnvType.CLIENT)
   record Goal(double x, double z, double altitude, double minClear, boolean landing, boolean rockets, double rocketCost, double maxSpeed, double floor) {
   }

   @Environment(EnvType.CLIENT)
   record Plan(float yaw, float pitch, boolean rocket, double cost, boolean safe) {
   }

   @Environment(EnvType.CLIENT)
   static final class Terrain {
      private final class_638 level;
      private final double unknown;
      private final Long2DoubleOpenHashMap heights = new Long2DoubleOpenHashMap();

      Terrain(class_638 level, double unknown) {
         this.level = level;
         this.unknown = unknown;
         this.heights.defaultReturnValue(Double.NaN);
      }

      double surface(double x, double z) {
         int bx = class_3532.method_15357(x);
         int bz = class_3532.method_15357(z);
         long key = class_2338.method_10064(bx, 0, bz);
         double height = this.heights.get(key);
         if (Double.isNaN(height)) {
            height = this.known(bx, bz) ? this.level.method_8624(class_2903.field_13197, bx, bz) : this.unknown;
            this.heights.put(key, height);
         }

         return height;
      }

      double under(double x, double z) {
         double top = this.surface(x, z);
         top = Math.max(top, this.surface(x + 0.3, z + 0.3));
         top = Math.max(top, this.surface(x - 0.3, z - 0.3));
         top = Math.max(top, this.surface(x + 0.3, z - 0.3));
         return Math.max(top, this.surface(x - 0.3, z + 0.3));
      }

      boolean known(int x, int z) {
         return this.level.method_2935().method_12123(x >> 4, z >> 4);
      }

      class_638 level() {
         return this.level;
      }
   }
}
