package net.albiz.client.esp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SkeletonPoseTest {
   private static final float S = SkeletonPose.MODEL_SCALE;

   private static float[] pose(float bodyYaw, float headYaw, float pitch, float pos, float speed, boolean sneak, float height) {
      float[] out = new float[SkeletonPose.FLOATS];
      SkeletonPose.compute(bodyYaw, headYaw, pitch, pos, speed, sneak, height, out);
      return out;
   }

   private static float at(float[] pose, int bone, int component) {
      return pose[bone * 6 + component];
   }

   @Test
   void standingPoseIsUprightAndSymmetric() {
      float[] p = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      // Feet on the ground, directly under the hips.
      assertEquals(0.0F, at(p, SkeletonPose.LEFT_LEG, 4), 1.0E-5F);
      assertEquals(0.0F, at(p, SkeletonPose.RIGHT_LEG, 4), 1.0E-5F);
      assertEquals(at(p, SkeletonPose.LEFT_LEG, 0), at(p, SkeletonPose.LEFT_LEG, 3), 1.0E-5F);
      assertEquals(-at(p, SkeletonPose.LEFT_LEG, 0), at(p, SkeletonPose.RIGHT_LEG, 0), 1.0E-5F);
      // Spine from hip height to shoulder height, head on top.
      assertEquals(0.75F * S, at(p, SkeletonPose.SPINE, 1), 1.0E-5F);
      assertEquals(1.5F * S, at(p, SkeletonPose.SPINE, 4), 1.0E-5F);
      assertEquals(2.0F * S, at(p, SkeletonPose.HEAD, 4), 1.0E-5F);
      // Hands hang straight down to hip height.
      assertEquals(0.75F * S, at(p, SkeletonPose.LEFT_ARM, 4), 1.0E-5F);
   }

   @Test
   void bodyYawRotatesAroundTheVerticalAxis() {
      float[] south = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      float[] west = pose(90.0F, 90.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      // Shoulders lie along X when facing +Z, and along Z after a 90 degree turn.
      assertEquals(0.3125F * S, Math.abs(at(south, SkeletonPose.SHOULDERS, 0)), 1.0E-5F);
      assertEquals(0.0F, at(south, SkeletonPose.SHOULDERS, 2), 1.0E-5F);
      assertEquals(0.0F, at(west, SkeletonPose.SHOULDERS, 0), 1.0E-5F);
      assertEquals(0.3125F * S, Math.abs(at(west, SkeletonPose.SHOULDERS, 2)), 1.0E-5F);
      // Heights are unaffected by yaw.
      for (int bone = 0; bone < SkeletonPose.BONES; bone++) {
         assertEquals(at(south, bone, 1), at(west, bone, 1), 1.0E-5F);
         assertEquals(at(south, bone, 4), at(west, bone, 4), 1.0E-5F);
      }
   }

   @Test
   void walkingSwingsLegsInOppositeDirectionsAndKeepsLength() {
      float[] p = pose(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, false, 1.8F);
      float left = at(p, SkeletonPose.LEFT_LEG, 5);
      float right = at(p, SkeletonPose.RIGHT_LEG, 5);
      assertTrue(Math.abs(left) > 0.3F);
      assertEquals(-left, right, 1.0E-5F);
      for (int bone : new int[]{SkeletonPose.LEFT_LEG, SkeletonPose.RIGHT_LEG, SkeletonPose.LEFT_ARM, SkeletonPose.RIGHT_ARM}) {
         double dx = at(p, bone, 3) - at(p, bone, 0);
         double dy = at(p, bone, 4) - at(p, bone, 1);
         double dz = at(p, bone, 5) - at(p, bone, 2);
         assertEquals(0.75 * S, Math.sqrt(dx * dx + dy * dy + dz * dz), 1.0E-4);
      }
      // Arms swing against the legs on the same side.
      assertTrue(at(p, SkeletonPose.LEFT_ARM, 5) * left < 0.0F);
   }

   @Test
   void speedIsClampedSoBadDataCannotFoldTheLegs() {
      float[] fast = pose(0.0F, 0.0F, 0.0F, 0.0F, 50.0F, false, 1.8F);
      float[] one = pose(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, false, 1.8F);
      assertEquals(at(one, SkeletonPose.LEFT_LEG, 5), at(fast, SkeletonPose.LEFT_LEG, 5), 1.0E-5F);
   }

   @Test
   void sneakingLeansForwardAndFitsTheLowerHitbox() {
      float[] stand = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      float[] sneak = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, 1.5F);
      assertTrue(at(sneak, SkeletonPose.SPINE, 5) > 0.2F);
      assertTrue(at(sneak, SkeletonPose.SPINE, 4) < at(stand, SkeletonPose.SPINE, 4));
      assertEquals(0.0F, at(sneak, SkeletonPose.LEFT_LEG, 4), 1.0E-5F);
   }

   @Test
   void headFollowsPitchAndRelativeYaw() {
      float[] level = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      float[] down = pose(0.0F, 0.0F, 90.0F, 0.0F, 0.0F, false, 1.8F);
      assertEquals(0.5F * S, at(level, SkeletonPose.HEAD, 4) - at(level, SkeletonPose.HEAD, 1), 1.0E-5F);
      assertEquals(0.0F, at(down, SkeletonPose.HEAD, 4) - at(down, SkeletonPose.HEAD, 1), 1.0E-5F);
      assertEquals(0.5F * S, at(down, SkeletonPose.HEAD, 5), 1.0E-5F);
      // Head turned 90 degrees relative to the body tips sideways instead of forwards.
      float[] turned = pose(0.0F, 90.0F, 90.0F, 0.0F, 0.0F, false, 1.8F);
      assertEquals(0.5F * S, Math.abs(at(turned, SkeletonPose.HEAD, 3)), 1.0E-5F);
      assertEquals(0.0F, at(turned, SkeletonPose.HEAD, 5), 1.0E-5F);
   }

   @Test
   void scalesWithEntityHeightAndRejectsShortBuffers() {
      float[] normal = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 1.8F);
      float[] half = pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 0.9F);
      for (int i = 0; i < SkeletonPose.FLOATS; i++) {
         assertEquals(normal[i] * 0.5F, half[i], 1.0E-5F);
      }
      assertThrows(IllegalArgumentException.class, () -> SkeletonPose.compute(0, 0, 0, 0, 0, false, 1.8F, new float[10]));
   }
}
