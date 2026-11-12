package net.albiz.client.util.pvp;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class ServerRotation {
   private static Rotation requested;
   private static Rotation committed;
   private static Rotation server;
   private static boolean silent;
   private static boolean steered;
   private static class_746 owner;
   private static float ownYaw;
   private static float ownPitch;
   private static boolean swapped;
   private static Rotation walked;
   private static final float MAX_STEP = 25.0F;
   private static final int EASE_TICKS = 10;
   private static final float EASE_SPEED = 5.0F;
   private static final float EASE_DONE = 0.5F;
   private static final float EASE_GIVE_UP = 150.0F;
   private static float requestedSpeed;
   private static Rotation stepped;
   private static boolean requestedLastTick;
   private static int easeLeft;

   private ServerRotation() {
   }

   public static void request(Rotation rotation) {
      request(rotation, 0.0F);
   }

   public static void request(Rotation rotation, float speed) {
      if (requested == null && committed == null) {
         requested = rotation;
         requestedSpeed = speed;
      }
   }

   public static boolean pending() {
      return requested != null || committed != null;
   }

   public static Rotation nextStep(class_746 player, Rotation wanted) {
      return step(player, wanted, 0.0F);
   }

   public static Rotation sent() {
      return server;
   }

   public static boolean desynced() {
      return silent;
   }

   public static boolean canRotate(class_746 player) {
      return !player.method_6128() && !player.method_6123() && !player.method_5765() && !player.method_5681() && player.field_3944.method_76760();
   }

   public static Rotation next(class_746 player, Rotation wanted) {
      Rotation base = server != null ? server : new Rotation(player.method_36454(), player.method_36455());
      double k = RotationUtils.mouseScale(class_310.method_1551(), player);
      float step = (float)k * 0.15F;
      long yawCounts = Math.round(class_3532.method_15393(wanted.yaw() - base.yaw()) / step);
      long pitchCounts = Math.round((wanted.pitch() - base.pitch()) / step);
      float yaw = base.yaw() + (float)(yawCounts * k) * 0.15F;
      float pitch = class_3532.method_15363(base.pitch() + (float)(pitchCounts * k) * 0.15F, -90.0F, 90.0F);
      return new Rotation(yaw, pitch);
   }

   public static void startTick() {
      class_746 player = class_310.method_1551().field_1724;
      if (player != owner) {
         owner = player;
         server = null;
      }

      requestedLastTick = requested != null;
      requested = null;
      committed = null;
      stepped = null;
      if (requestedLastTick) {
         easeLeft = 10;
      } else if (easeLeft > 0) {
         easeLeft--;
      }

      if (easeLeft > 0 && (player == null || server == null || closeToCamera(player))) {
         easeLeft = 0;
      }

      if (player != null && server != null && steered && !(Math.abs(player.method_36454() - server.yaw()) <= 180.0F)) {
         float shift = server.yaw() + class_3532.method_15393(player.method_36454() - server.yaw()) - player.method_36454();
         player.method_36456(player.method_36454() + shift);
         player.field_5982 += shift;
         player.field_3932 += shift;
         player.field_3931 += shift;
         player.field_6241 += shift;
         player.field_6259 += shift;
         player.field_6283 += shift;
         player.field_6220 += shift;
      }
   }

   public static void noteSent(class_2596<?> packet) {
      if (packet instanceof class_2828 move && move.method_36172() && class_310.method_1551().method_18854()) {
         class_746 player = class_310.method_1551().field_1724;
         server = new Rotation(move.method_12271(0.0F), move.method_12270(0.0F));
         silent = swapped;
         steered = swapped || player != null && (server.yaw() != player.method_36454() || server.pitch() != player.method_36455());
         owner = player;
      }
   }

   public static Rotation commit(class_746 player) {
      if (committed == null) {
         Rotation target = canRotate(player) ? target(player) : null;
         committed = target != null ? target : new Rotation(player.method_36454(), player.method_36455());
      }

      return committed;
   }

   public static boolean isSwapped() {
      return swapped;
   }

   public static void applyForMovement(class_746 player) {
      if (!swapped && canRotate(player)) {
         Rotation target = target(player);
         if (target != null && (target.yaw() != player.method_36454() || target.pitch() != player.method_36455())) {
            ownYaw = player.method_36454();
            ownPitch = player.method_36455();
            player.method_36456(target.yaw());
            player.method_36457(target.pitch());
            walked = target;
            swapped = true;
         }
      }
   }

   public static class_10185 correctInput(class_10185 keys) {
      if (walked == null) {
         return keys;
      } else {
         int forward = impulse(keys.comp_3159(), keys.comp_3160());
         int strafe = impulse(keys.comp_3161(), keys.comp_3162());
         if (forward == 0 && strafe == 0) {
            return keys;
         } else {
            double[] wanted = heading(ownYaw, strafe, forward);
            int bestForward = forward;
            int bestStrafe = strafe;
            double bestDot = Double.NEGATIVE_INFINITY;

            for (int f = -1; f <= 1; f++) {
               for (int s = -1; s <= 1; s++) {
                  if (f != 0 || s != 0) {
                     double[] way = heading(walked.yaw(), s, f);
                     double dot = way[0] * wanted[0] + way[1] * wanted[1];
                     if (dot > bestDot) {
                        bestDot = dot;
                        bestForward = f;
                        bestStrafe = s;
                     }
                  }
               }
            }

            return bestForward == forward && bestStrafe == strafe
               ? keys
               : new class_10185(bestForward > 0, bestForward < 0, bestStrafe > 0, bestStrafe < 0, keys.comp_3163(), keys.comp_3164(), keys.comp_3165());
         }
      }
   }

   public static boolean misses(class_746 player, class_238 box, double range) {
      if (server == null || !canRotate(player)) {
         return false;
      } else if (!pending()) {
         endEase();
         return false;
      } else {
         class_243 eye = player.method_33571();
         if (box.method_1006(eye)) {
            return false;
         } else {
            Rotation look = target(player);
            if (look == null) {
               look = new Rotation(player.method_36454(), player.method_36455());
            }

            return !hits(eye, box, range, look.yaw(), look.pitch())
               && !hits(eye, box, range, server.yaw(), look.pitch())
               && !hits(eye, box, range, server.yaw(), server.pitch());
         }
      }
   }

   public static boolean missesBlock(class_746 player, class_238 box, double range) {
      if (server == null) {
         return false;
      } else {
         if (!pending()) {
            endEase();
         }

         if (!silent) {
            if (!pending() || !canRotate(player)) {
               return false;
            }

            Rotation look = target(player);
            if (look == null || look.yaw() == player.method_36454() && look.pitch() == player.method_36455()) {
               return false;
            }
         }

         class_243 eye = player.method_33571();
         return !box.method_1006(eye) && !hits(eye, box, range, server.yaw(), server.pitch());
      }
   }

   public static void afterSend(class_746 player) {
      if (swapped) {
         restore(player);
      }
   }

   public static void finishTick(class_746 player) {
      if (swapped) {
         restore(player);
      }
   }

   private static Rotation target(class_746 player) {
      if (committed != null) {
         return committed;
      } else if (stepped != null) {
         return stepped;
      } else {
         if (requested != null) {
            stepped = step(player, requested, requestedSpeed);
         } else if (easeLeft > 0 && server != null) {
            stepped = step(player, new Rotation(player.method_36454(), player.method_36455()), 5.0F);
         }

         return stepped;
      }
   }

   private static Rotation step(class_746 player, Rotation wanted, float speed) {
      Rotation base = server != null ? server : new Rotation(player.method_36454(), player.method_36455());
      double unit = RotationUtils.mouseScale(class_310.method_1551(), player) * 0.15;
      double yaw = class_3532.method_15393(wanted.yaw() - base.yaw());
      double pitch = wanted.pitch() - base.pitch();
      double distance = Math.hypot(yaw, pitch);
      if (distance < 1.0E-4) {
         return next(player, wanted);
      } else {
         double scale = 1.0;
         if (speed > 0.0F) {
            double stride = Math.min(distance, 0.313 * distance + 1.56 * speed);
            if (stride < distance) {
               stride *= 0.85 + 0.15 * ThreadLocalRandom.current().nextDouble();
            }

            scale = stride / distance;
         }

         double bigger = Math.max(Math.abs(yaw), Math.abs(pitch)) * scale;
         if (bigger > 25.0) {
            scale *= 25.0 / bigger;
         }

         long cap = Math.max(1L, (long)Math.floor(25.0 / unit));
         long yawCounts = class_3532.method_53062(Math.round(yaw * scale / unit), -cap, cap);
         long pitchCounts = class_3532.method_53062(Math.round(pitch * scale / unit), -cap, cap);
         return new Rotation(base.yaw() + (float)(yawCounts * unit), class_3532.method_15363(base.pitch() + (float)(pitchCounts * unit), -90.0F, 90.0F));
      }
   }

   private static void endEase() {
      if (easeLeft != 0 && committed == null) {
         easeLeft = 0;
         stepped = null;
      }
   }

   private static boolean closeToCamera(class_746 player) {
      double unit = RotationUtils.mouseScale(class_310.method_1551(), player) * 0.15;
      double yaw = Math.abs(class_3532.method_15393(player.method_36454() - server.yaw()));
      double pitch = Math.abs(player.method_36455() - server.pitch());
      double off = Math.max(yaw, pitch);
      return off <= Math.max(0.5, 2.0 * unit) || off > 150.0;
   }

   private static int impulse(boolean positive, boolean negative) {
      return positive == negative ? 0 : (positive ? 1 : -1);
   }

   private static double[] heading(float yaw, int strafe, int forward) {
      double sin = class_3532.method_15374(yaw * (float) (Math.PI / 180.0));
      double cos = class_3532.method_15362(yaw * (float) (Math.PI / 180.0));
      double x = strafe * cos - forward * sin;
      double z = forward * cos + strafe * sin;
      double length = Math.sqrt(x * x + z * z);
      return new double[]{x / length, z / length};
   }

   private static boolean hits(class_243 eye, class_238 box, double range, float yaw, float pitch) {
      class_243 direction = class_243.method_1030(pitch, yaw);
      return box.method_992(eye, eye.method_1019(direction.method_1021(range))).isPresent();
   }

   private static void restore(class_746 player) {
      player.field_3932 = player.field_3932 + (ownYaw - walked.yaw()) * 0.5F;
      player.field_3916 = player.field_3916 + (ownPitch - walked.pitch()) * 0.5F;
      walked = null;
      player.method_36456(ownYaw);
      player.method_36457(ownPitch);
      swapped = false;
   }
}
