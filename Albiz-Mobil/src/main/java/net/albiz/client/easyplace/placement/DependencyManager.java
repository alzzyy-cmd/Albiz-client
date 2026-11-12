package net.albiz.client.easyplace.placement;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.LayerRange;
import java.util.function.LongPredicate;
import net.albiz.client.easyplace.hopper.HopperPlacementHandler;
import net.albiz.client.easyplace.mixin.InvokerBlockItem;
import net.albiz.client.easyplace.util.BlockStateUtil;
import net.albiz.client.easyplace.util.PlacementUtil;
import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1747;
import net.minecraft.class_1750;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2279;
import net.minecraft.class_2281;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2358;
import net.minecraft.class_238;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2745;
import net.minecraft.class_2771;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4050;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;

@Environment(EnvType.CLIENT)
public final class DependencyManager {
   private static final int SEARCH_RADIUS = 5;
   private final class_310 mc;
   private final HopperPlacementHandler hopperHandler = new HopperPlacementHandler();
   private BuildScope currentScope;
   private double reachSpare;
   private double feetY = Double.NaN;
   private boolean builderEyes;
   private static final double BUILDER_SPARE = 0.3;
   private static final double REACH_SPARE = 0.6499999999999999;
   private static final double STEADY_SHIFT = 0.02;
   private static final float[][] STEADY_TURNS = new float[][]{{0.5F, 0.0F}, {-0.5F, 0.0F}, {0.0F, 0.5F}, {0.0F, -0.5F}};

   public DependencyManager(class_310 mc) {
      this.mc = mc;
   }

   public PlacementTarget searchForPlaceables(
      WorldSchematic schematicWorld,
      boolean hopperForceFacing,
      boolean fovFilter,
      float fovDegrees,
      double maxYawChange,
      float lastYawDelta,
      BuildScope scope,
      LongPredicate held
   ) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && schematicWorld != null) {
         class_2338 center = this.mc.field_1724.method_24515();
         class_243 eyePos = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23320(), this.mc.field_1724.method_23321());
         this.feetY = this.mc.field_1724.method_23318();
         this.builderEyes = scope != null;
         this.reachSpare = scope != null ? 0.3 : 0.0;
         float currentYaw = this.mc.field_1724.method_36454();
         PlacementTarget best = null;
         double bestScore = Double.MAX_VALUE;

         for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
               for (int dz = -5; dz <= 5; dz++) {
                  int x = center.method_10263() + dx;
                  int y = center.method_10264() + dy;
                  int z = center.method_10260() + dz;
                  class_2338 pos = new class_2338(x, y, z);
                  if ((scope == null || scope.allows(pos)) && !held.test(pos.method_10063())) {
                     class_2680 expected = schematicWorld.method_8320(pos);
                     if (!expected.method_26215() && BlockStateUtil.isPrimaryPlacementState(expected) && (scope != null || this.isInRenderLayer(pos))) {
                        class_2680 current = this.mc.field_1687.method_8320(pos);
                        boolean secondSlab = StateRules.slabHalf(current, expected);
                        if ((secondSlab || isReplaceable(current))
                           && !StateRules.placed(current, expected)
                           && (secondSlab || !this.mc.field_1724.method_5829().method_994(new class_238(pos)))) {
                           this.currentScope = scope;
                           PlacementTarget placement = secondSlab
                              ? this.secondSlab(expected, current, pos, eyePos, scope != null)
                              : this.possibleToPlace(expected, x, y, z, eyePos, hopperForceFacing, scope != null);
                           if (placement != null) {
                              if (fovFilter) {
                                 double fdx = x + 0.5 - eyePos.field_1352;
                                 double fdy = y + 0.5 - eyePos.field_1351;
                                 double fdz = z + 0.5 - eyePos.field_1350;
                                 double distSq = fdx * fdx + fdy * fdy + fdz * fdz;
                                 if (distSq < 1.0E-4) {
                                    continue;
                                 }

                                 double yawRad = Math.toRadians(currentYaw);
                                 double pitchRad = Math.toRadians(this.mc.field_1724.method_36455());
                                 double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
                                 double lookY = -Math.sin(pitchRad);
                                 double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
                                 double dot = (fdx * lookX + fdy * lookY + fdz * lookZ) / Math.sqrt(distSq);
                                 dot = Math.max(-1.0, Math.min(1.0, dot));
                                 double angleDeg = Math.toDegrees(Math.acos(dot));
                                 if (angleDeg > fovDegrees * 0.5) {
                                    continue;
                                 }
                              }

                              double yawDiff = Math.abs(class_3532.method_15393(placement.yaw() - currentYaw));
                              if (!(yawDiff > maxYawChange)) {
                                 double dist = eyePos.method_1025(new class_243(x + 0.5, y + 0.5, z + 0.5));
                                 double score = yawDiff * 2.0 + dist * 0.5;
                                 if (scope != null) {
                                    score = yawDiff * 0.2 - Math.sqrt(dist) * 20.0 + (y - center.method_10264() + 5) * scope.lowWeight();
                                 }

                                 if (lastYawDelta != 0.0F) {
                                    double candidateDelta = class_3532.method_15393(placement.yaw() - currentYaw);
                                    if (Math.signum(candidateDelta) == -Math.signum(lastYawDelta) && Math.abs(candidateDelta) > 10.0) {
                                       score += Math.abs(candidateDelta) * 5.0;
                                    }
                                 }

                                 if (score < bestScore) {
                                    bestScore = score;
                                    best = placement;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private PlacementTarget possibleToPlace(class_2680 toPlace, int x, int y, int z, class_243 eyePos, boolean hopperForceFacing, boolean wholeInventory) {
      class_2338 targetPos = new class_2338(x, y, z);
      boolean skipChestType = this.partnerMissing(toPlace, targetPos);
      int slot = this.findItem(toPlace, wholeInventory);
      if (slot < 0) {
         return null;
      } else {
         for (class_2350 against : class_2350.values()) {
            class_2338 placeAgainstPos = targetPos.method_10093(against);
            class_2680 placeAgainstState = this.mc.field_1687.method_8320(placeAgainstPos);
            if (!isReplaceable(placeAgainstState) && toPlace.method_26184(this.mc.field_1687, targetPos)) {
               class_2350 clickedFace = against.method_10153();
               if (!hopperForceFacing || this.hopperHandler.faceCanProduceExpectedFacing(toPlace, clickedFace)) {
                  boolean sneak = !placeAgainstPos.equals(targetPos) && Clicks.usesClick(this.mc.field_1687, placeAgainstPos, this.wholeInventory(slot));
                  boolean visible = PlacementUtil.isFaceVisibleToEye(placeAgainstPos, clickedFace, this.builderEye(eyePos, sneak))
                     || this.builderEyes && !sneak && PlacementUtil.isFaceVisibleToEye(placeAgainstPos, clickedFace, this.builderEye(eyePos, true));
                  if (visible) {
                     class_265 shape = placeAgainstState.method_26218(this.mc.field_1687, placeAgainstPos);
                     if (!shape.method_1110()) {
                        class_238 aabb = shape.method_1107();

                        for (class_243 multiplier : aabbSideMultipliers(against)) {
                           double placeX = placeAgainstPos.method_10263()
                              + aabb.field_1323 * multiplier.field_1352
                              + aabb.field_1320 * (1.0 - multiplier.field_1352);
                           double placeY = placeAgainstPos.method_10264()
                              + aabb.field_1322 * multiplier.field_1351
                              + aabb.field_1325 * (1.0 - multiplier.field_1351);
                           double placeZ = placeAgainstPos.method_10260()
                              + aabb.field_1321 * multiplier.field_1350
                              + aabb.field_1324 * (1.0 - multiplier.field_1350);
                           class_243 cursor = new class_243(placeX, placeY, placeZ);
                           PlacementTarget target = this.tryAim(toPlace, targetPos, placeAgainstPos, clickedFace, cursor, eyePos, slot, skipChestType);
                           if (target != null) {
                              return target;
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   public boolean placeableFrom(class_2680 expected, class_2338 pos, class_243 eyePos, boolean hopperForceFacing) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         class_2680 current = this.mc.field_1687.method_8320(pos);
         this.reachSpare = 0.6499999999999999;
         double wasFeet = this.feetY;
         boolean wasBuilder = this.builderEyes;
         this.feetY = eyePos.field_1351 - this.mc.field_1724.method_18381(class_4050.field_18076);
         this.builderEyes = true;

         boolean var9;
         try {
            if (StateRules.slabHalf(current, expected)) {
               return this.secondSlab(expected, current, pos, eyePos, true) != null;
            }

            if (!isReplaceable(current) || StateRules.placed(current, expected)) {
               return false;
            }

            var9 = this.possibleToPlace(expected, pos.method_10263(), pos.method_10264(), pos.method_10260(), eyePos, hopperForceFacing, true) != null;
         } finally {
            this.reachSpare = 0.0;
            this.feetY = wasFeet;
            this.builderEyes = wasBuilder;
         }

         return var9;
      } else {
         return false;
      }
   }

   private boolean partnerMissing(class_2680 toPlace, class_2338 targetPos) {
      if (!toPlace.method_28498(class_2741.field_12506)) {
         return true;
      } else {
         class_2745 type = (class_2745)toPlace.method_11654(class_2741.field_12506);
         if (type == class_2745.field_12569) {
            return true;
         } else {
            class_2350 facing = (class_2350)toPlace.method_11654(class_2741.field_12481);
            class_2350 partnerDir = type == class_2745.field_12574 ? facing.method_10170() : facing.method_10160();
            class_2680 partnerState = this.mc.field_1687.method_8320(targetPos.method_10093(partnerDir));
            return !partnerState.method_28498(class_2741.field_12506)
               || partnerState.method_11654(class_2741.field_12506) != class_2745.field_12569
               || partnerState.method_11654(class_2741.field_12481) != facing;
         }
      }
   }

   public boolean verify(class_2680 desired, class_2338 targetPos, class_3965 hit, float yaw, float pitch, int slot, boolean sneaking) {
      return this.mc.field_1724 != null && this.mc.field_1687 != null && slot >= 0
         ? this.wouldPlace(desired, targetPos, hit, new float[]{yaw, pitch}, slot, this.partnerMissing(desired, targetPos), sneaking)
         : false;
   }

   public String wouldBe(class_2338 targetPos, class_3965 hit, float yaw, float pitch, int slot, boolean sneaking) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && slot >= 0) {
         class_746 player = this.mc.field_1724;
         class_10185 keys = player.field_3913.field_54155;
         class_1799 stack = player.method_31548().method_5438(slot);
         if (stack.method_7909() instanceof class_1747 blockItem) {
            float var20 = player.method_36454();
            float originalPitch = player.method_36455();
            player.method_36456(yaw);
            player.method_36457(pitch);
            player.field_3913.field_54155 = new class_10185(
               keys.comp_3159(), keys.comp_3160(), keys.comp_3161(), keys.comp_3162(), keys.comp_3163(), sneaking, keys.comp_3165()
            );

            String var21;
            try {
               class_1750 context = new class_1750(player, class_1268.field_5808, stack, hit);
               if (!context.method_7716()) {
                  return "cannot place";
               }

               if (context.method_8037().equals(targetPos)) {
                  return ((InvokerBlockItem)blockItem).easyplace$getPlacementState(context) + " facing " + context.method_8042();
               }

               var21 = "goes to " + context.method_8037().method_23854();
            } catch (Exception var18) {
               return var18.toString();
            } finally {
               player.method_36456(var20);
               player.method_36457(originalPitch);
               player.field_3913.field_54155 = keys;
            }

            return var21;
         } else {
            return "no block item";
         }
      } else {
         return "?";
      }
   }

   private PlacementTarget secondSlab(class_2680 expected, class_2680 current, class_2338 pos, class_243 eyePos, boolean wholeInventory) {
      int slot = this.findItem(expected, wholeInventory);
      if (slot < 0) {
         return null;
      } else {
         boolean bottom = current.method_11654(class_2741.field_12485) == class_2771.field_12681;
         class_2350 face = bottom ? class_2350.field_11036 : class_2350.field_11033;
         double faceY = pos.method_10264() + 0.5;
         if (bottom ? !(eyePos.field_1351 <= faceY + 0.05) : !(eyePos.field_1351 >= faceY - 0.05)) {
            for (double[] offset : new double[][]{{0.5, 0.5}, {0.25, 0.5}, {0.75, 0.5}, {0.5, 0.25}, {0.5, 0.75}}) {
               class_243 cursor = new class_243(pos.method_10263() + offset[0], faceY, pos.method_10260() + offset[1]);
               PlacementTarget target = this.tryAim(expected, pos, pos, face, cursor, eyePos, slot, true);
               if (target != null) {
                  return target;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   private PlacementTarget tryAim(
      class_2680 toPlace,
      class_2338 targetPos,
      class_2338 againstPos,
      class_2350 clickedFace,
      class_243 cursor,
      class_243 eyePos,
      int slot,
      boolean skipChestType
   ) {
      boolean supportSneak = !againstPos.equals(targetPos) && Clicks.usesClick(this.mc.field_1687, againstPos, this.wholeInventory(slot));
      PlacementTarget target = this.aimWith(toPlace, targetPos, againstPos, clickedFace, cursor, eyePos, slot, skipChestType, supportSneak, false);
      if (target != null) {
         return target;
      } else {
         return supportSneak || !(toPlace.method_26204() instanceof class_2281) && !this.builderEyes
            ? null
            : this.aimWith(toPlace, targetPos, againstPos, clickedFace, cursor, eyePos, slot, skipChestType, true, true);
      }
   }

   private PlacementTarget aimWith(
      class_2680 toPlace,
      class_2338 targetPos,
      class_2338 againstPos,
      class_2350 clickedFace,
      class_243 cursor,
      class_243 eyePos,
      int slot,
      boolean skipChestType,
      boolean sneaking,
      boolean sneakFlag
   ) {
      class_243 eye = this.builderEye(eyePos, sneaking);
      float[] rot = PlacementUtil.calcRotation(eye, cursor);
      double reach = this.mc.field_1724.method_55754() - this.reachSpare;
      class_3965 result = PlacementUtil.rayTraceFrom(this.mc.field_1724, eye, rot[0], rot[1], reach);
      if (result == null || result.method_17783() != class_240.field_1332) {
         return null;
      } else if (!result.method_17777().equals(againstPos) || result.method_17780() != clickedFace) {
         return null;
      } else if (!this.wouldPlace(toPlace, targetPos, result, rot, slot, skipChestType, sneaking)) {
         return null;
      } else {
         return !this.steady(toPlace, targetPos, result, rot, slot, skipChestType, sneaking)
            ? null
            : new PlacementTarget(targetPos, againstPos, clickedFace, clickedFace, slot, rot[0], rot[1], 0, result.method_17784(), sneakFlag);
      }
   }

   private boolean steady(class_2680 toPlace, class_2338 targetPos, class_3965 result, float[] rot, int slot, boolean skipChestType, boolean sneaking) {
      class_243 at = result.method_17784();
      class_2350 face = result.method_17780();

      for (class_2350 side : class_2350.values()) {
         if (side.method_10166() != face.method_10166()) {
            class_243 moved = at.method_1031(side.method_10148() * 0.02, side.method_10164() * 0.02, side.method_10165() * 0.02);
            class_3965 nudged = new class_3965(moved, face, result.method_17777(), result.method_17781());
            if (!this.wouldPlace(toPlace, targetPos, nudged, rot, slot, skipChestType, sneaking)) {
               return false;
            }
         }
      }

      for (float[] turn : STEADY_TURNS) {
         if (!this.wouldPlace(toPlace, targetPos, result, new float[]{rot[0] + turn[0], rot[1] + turn[1]}, slot, skipChestType, sneaking)) {
            return false;
         }
      }

      return true;
   }

   private class_243 builderEye(class_243 eyePos, boolean sneaking) {
      return this.builderEyes && !Double.isNaN(this.feetY)
         ? new class_243(
            eyePos.field_1352, this.feetY + this.mc.field_1724.method_18381(sneaking ? class_4050.field_18081 : class_4050.field_18076), eyePos.field_1350
         )
         : eyePos;
   }

   private int findItem(class_2680 desired, boolean wholeInventory) {
      class_1792 required = BlockStateUtil.requiredItem(desired);
      if (required != null && required instanceof class_1747 && this.mc.field_1724 != null) {
         class_1661 inventory = this.mc.field_1724.method_31548();
         int end = wholeInventory ? 36 : 9;

         for (int i = 0; i < end; i++) {
            if (inventory.method_5438(i).method_31574(required)) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean wholeInventory(int slot) {
      return slot >= 9 || this.currentScope != null;
   }

   private boolean wouldPlace(class_2680 desired, class_2338 targetPos, class_3965 result, float[] rot, int slot, boolean skipChestType, boolean sneaking) {
      class_746 player = this.mc.field_1724;
      class_10185 keys = player.field_3913.field_54155;
      class_1799 stack = player.method_31548().method_5438(slot);
      if (stack.method_7909() instanceof class_1747 blockItem) {
         float var25 = player.method_36454();
         float originalPitch = player.method_36455();
         float delta = class_3532.method_15393(rot[0] - var25);
         player.method_36456(var25 + delta);
         player.method_36457(rot[1]);
         player.field_3913.field_54155 = new class_10185(
            keys.comp_3159(), keys.comp_3160(), keys.comp_3161(), keys.comp_3162(), keys.comp_3163(), sneaking, keys.comp_3165()
         );

         boolean var19;
         try {
            class_1750 context = new class_1750(player, class_1268.field_5808, stack, result);
            if (!context.method_7716() || !context.method_8037().equals(targetPos)) {
               return false;
            }

            class_2680 wouldBe = ((InvokerBlockItem)blockItem).easyplace$getPlacementState(context);
            if (wouldBe == null) {
               return false;
            }

            if (!(desired.method_26204() instanceof class_2281) || !(wouldBe.method_26204() instanceof class_2281)) {
               return StateRules.wouldFit(wouldBe, desired, false);
            }

            class_2745 want = (class_2745)desired.method_11654(class_2741.field_12506);
            class_2745 got = (class_2745)wouldBe.method_11654(class_2741.field_12506);
            if (got != want && (got != class_2745.field_12569 || !skipChestType)) {
               return false;
            }

            var19 = StateRules.wouldFit(wouldBe, desired, true);
         } catch (Exception var23) {
            return false;
         } finally {
            player.method_36456(var25);
            player.method_36457(originalPitch);
            player.field_3913.field_54155 = keys;
         }

         return var19;
      } else {
         return false;
      }
   }

   private static class_243[] aabbSideMultipliers(class_2350 dir) {
      return switch (dir) {
         case field_11033 -> new class_243[]{
            new class_243(0.5, 0.0, 0.5),
            new class_243(0.1, 0.0, 0.5),
            new class_243(0.9, 0.0, 0.5),
            new class_243(0.5, 0.0, 0.1),
            new class_243(0.5, 0.0, 0.9),
            new class_243(0.03, 0.0, 0.5),
            new class_243(0.97, 0.0, 0.5),
            new class_243(0.5, 0.0, 0.03),
            new class_243(0.5, 0.0, 0.97),
            new class_243(0.25, 0.0, 0.25),
            new class_243(0.75, 0.0, 0.25),
            new class_243(0.25, 0.0, 0.75),
            new class_243(0.75, 0.0, 0.75)
         };
         case field_11036 -> new class_243[]{
            new class_243(0.5, 1.0, 0.5),
            new class_243(0.1, 1.0, 0.5),
            new class_243(0.9, 1.0, 0.5),
            new class_243(0.5, 1.0, 0.1),
            new class_243(0.5, 1.0, 0.9),
            new class_243(0.25, 1.0, 0.25),
            new class_243(0.75, 1.0, 0.25),
            new class_243(0.25, 1.0, 0.75),
            new class_243(0.75, 1.0, 0.75)
         };
         default -> {
            double x = dir.method_10148() == 0 ? 0.5 : (1 + dir.method_10148()) / 2.0;
            double z = dir.method_10165() == 0 ? 0.5 : (1 + dir.method_10165()) / 2.0;
            yield new class_243[]{
               new class_243(x, 0.5, z),
               new class_243(x, 0.25, z),
               new class_243(x, 0.75, z),
               new class_243(dir.method_10148() == 0 ? 0.15 : x, 0.5, dir.method_10165() == 0 ? 0.15 : z),
               new class_243(dir.method_10148() == 0 ? 0.85 : x, 0.5, dir.method_10165() == 0 ? 0.85 : z)
            };
         }
      };
   }

   private static boolean isReplaceable(class_2680 state) {
      if (state.method_26215()) {
         return true;
      } else {
         class_2248 block = state.method_26204();
         if (block instanceof class_2404) {
            return true;
         } else if (block instanceof class_2302 && state.method_28498(class_2302.field_10835) && (Integer)state.method_11654(class_2302.field_10835) == 7) {
            return true;
         } else {
            return !(block instanceof class_2358) && !(block instanceof class_2279) ? state.method_45474() : true;
         }
      }
   }

   private boolean isInRenderLayer(class_2338 pos) {
      try {
         LayerRange range = DataManager.getRenderLayerRange();
         return range == null || range.isPositionWithinRange(pos);
      } catch (Exception var3) {
         return true;
      }
   }
}
