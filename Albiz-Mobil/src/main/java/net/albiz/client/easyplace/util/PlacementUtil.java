package net.albiz.client.easyplace.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public final class PlacementUtil {
   private PlacementUtil() {
   }

   public static class_243 faceCenterCursor(class_2338 targetPos, class_2350 placeSide) {
      return targetPos.method_46558().method_1031(placeSide.method_10148() * 0.5, placeSide.method_10164() * 0.5, placeSide.method_10165() * 0.5);
   }

   public static float[] calcRotation(class_243 eyePos, class_243 target) {
      double dx = target.field_1352 - eyePos.field_1352;
      double dy = target.field_1351 - eyePos.field_1351;
      double dz = target.field_1350 - eyePos.field_1350;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float pitch = (float)Math.toDegrees(-Math.atan2(dy, dist));
      return new float[]{class_3532.method_15393(yaw), class_3532.method_15363(pitch, -90.0F, 90.0F)};
   }

   public static class_3965 rayTraceTowards(class_1657 player, float yaw, float pitch, double reach) {
      return rayTraceFrom(player, getEyePos(player), yaw, pitch, reach);
   }

   public static class_3965 rayTraceFrom(class_1657 player, class_243 eyePos, float yaw, float pitch, double reach) {
      class_1937 level = class_310.method_1551().field_1687;
      if (level == null) {
         return null;
      } else {
         double yawRad = Math.toRadians(yaw);
         double cosPitch = Math.cos(Math.toRadians(pitch));
         double sinPitch = Math.sin(Math.toRadians(pitch));
         class_243 direction = new class_243(-Math.sin(yawRad) * cosPitch, -sinPitch, Math.cos(yawRad) * cosPitch);
         class_243 end = eyePos.method_1019(direction.method_1021(reach));
         return level.method_17742(new class_3959(eyePos, end, class_3960.field_17559, class_242.field_1348, player));
      }
   }

   public static class_243 getEyePos(class_1657 player) {
      return new class_243(player.method_23317(), player.method_23320(), player.method_23321());
   }

   public static class_2350 getClickFace(class_2350 placeSide) {
      return placeSide.method_10153();
   }

   public static class_2350 getPlaceSide(class_2338 targetPos, class_243 eyePos) {
      class_1937 level = class_310.method_1551().field_1687;
      if (level == null) {
         return null;
      } else {
         class_2350 best = null;
         double bestScore = -Double.MAX_VALUE;

         for (class_2350 dir : class_2350.values()) {
            class_2338 againstPos = targetPos.method_10093(dir);
            class_2680 againstState = level.method_8320(againstPos);
            if (!againstState.method_26215() && !againstState.method_26220(level, againstPos).method_1110()) {
               class_243 diff = eyePos.method_1020(againstPos.method_46558());
               double score = diff.field_1352 * dir.method_10148() + diff.field_1351 * dir.method_10164() + diff.field_1350 * dir.method_10165();
               if (score > bestScore) {
                  bestScore = score;
                  best = dir;
               }
            }
         }

         return best;
      }
   }

   public static boolean faceMatchesAxis(class_2680 expected, class_2350 clickedFace) {
      return !expected.method_28498(class_2741.field_12496) || expected.method_11654(class_2741.field_12496) == clickedFace.method_10166();
   }

   public static boolean isFaceVisibleToEye(class_2338 pos, class_2350 clickedFace, class_243 eyePos) {
      double buffer = 0.05;

      return switch (clickedFace) {
         case field_11043 -> eyePos.field_1350 < pos.method_10260() - 0.05;
         case field_11035 -> eyePos.field_1350 > pos.method_10260() + 1.0 + 0.05;
         case field_11039 -> eyePos.field_1352 < pos.method_10263() - 0.05;
         case field_11034 -> eyePos.field_1352 > pos.method_10263() + 1.0 + 0.05;
         case field_11036 -> eyePos.field_1351 > pos.method_10264() + 1.0 + 0.05;
         case field_11033 -> eyePos.field_1351 < pos.method_10264() - 0.05;
         default -> throw new MatchException(null, null);
      };
   }
}
