package net.albiz.client.easyplace.builder;

import net.albiz.client.easyplace.util.PlacementUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
final class Shuffle {
   private static final double CLOSE = 0.3;
   private static final double SLOW = 0.0025;
   private static final double KEY = 0.12;
   private static final double SLIDE = 1.2;
   private static final int TIME_LIMIT = 60;
   private static final double QUICK_SLOW = 0.0144;
   private final class_243 spot;
   private final class_243 look;
   private final double slow;
   private int ticks;

   Shuffle(class_243 spot, class_243 look, boolean quick) {
      this.spot = spot;
      this.look = look;
      this.slow = quick ? 0.0144 : 0.0025;
   }

   boolean quick() {
      return this.slow == 0.0144;
   }

   class_243 spot() {
      return this.spot;
   }

   Shuffle.Result tick(class_310 mc) {
      class_746 player = mc.field_1724;
      if (player == null) {
         return Shuffle.Result.FAILED;
      } else {
         float[] rot = PlacementUtil.calcRotation(player.method_33571(), this.look);
         Aim.turn(mc, rot[0], rot[1], 22.0F);
         double dx = this.spot.field_1352 - player.method_23317();
         double dz = this.spot.field_1350 - player.method_23321();
         class_243 motion = player.method_18798();
         double left = Math.hypot(dx, dz);
         if (left < 0.3 && motion.method_37268() < this.slow) {
            this.release(mc);
            return Shuffle.Result.DONE;
         } else if (++this.ticks > 60) {
            this.release(mc);
            return left < 0.45 ? Shuffle.Result.DONE : Shuffle.Result.FAILED;
         } else {
            double yaw = Math.toRadians(player.method_36454());
            double sin = Math.sin(yaw);
            double cos = Math.cos(yaw);
            double ahead = -sin * dx + cos * dz - 1.2 * (-sin * motion.field_1352 + cos * motion.field_1350);
            double toLeft = cos * dx + sin * dz - 1.2 * (cos * motion.field_1352 + sin * motion.field_1350);
            mc.field_1690.field_1894.method_23481(ahead > 0.12);
            mc.field_1690.field_1881.method_23481(ahead < -0.12);
            mc.field_1690.field_1913.method_23481(toLeft > 0.12);
            mc.field_1690.field_1849.method_23481(toLeft < -0.12);
            return Shuffle.Result.RUNNING;
         }
      }
   }

   void release(class_310 mc) {
      mc.field_1690.field_1894.method_23481(false);
      mc.field_1690.field_1881.method_23481(false);
      mc.field_1690.field_1913.method_23481(false);
      mc.field_1690.field_1849.method_23481(false);
   }

   @Environment(EnvType.CLIENT)
   static enum Result {
      RUNNING,
      DONE,
      FAILED;
   }
}
