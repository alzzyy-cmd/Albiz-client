package net.albiz.client.esp;

/**
 * Approximate biped skeleton from the values the client already has for a player: body yaw, head
 * yaw and pitch, walk-cycle position and speed, and the sneaking pose. It mirrors the vanilla
 * biped walk formula, so it follows running and sneaking closely; it does not capture item-use,
 * swimming or elytra poses. Pure maths: no game types, no allocation per call.
 */
public final class SkeletonPose {
   /** Number of line segments written by {@link #compute}. */
   public static final int BONES = 8;
   /** Floats written by {@link #compute}: {@code x1,y1,z1,x2,y2,z2} per bone. */
   public static final int FLOATS = BONES * 6;

   public static final int SPINE = 0;
   public static final int HEAD = 1;
   public static final int SHOULDERS = 2;
   public static final int LEFT_ARM = 3;
   public static final int RIGHT_ARM = 4;
   public static final int HIPS = 5;
   public static final int LEFT_LEG = 6;
   public static final int RIGHT_LEG = 7;

   /** Size of the player model relative to its 2-block geometry. */
   public static final float MODEL_SCALE = 0.9375F;
   private static final float LIMB = 0.75F;
   private static final float HIP_HALF = 0.125F;
   private static final float SHOULDER_HALF = 0.3125F;
   private static final float HEAD_LENGTH = 0.5F;
   private static final float DEG = (float)(Math.PI / 180.0);

   private SkeletonPose() {
   }

   /**
    * Writes bone segments, relative to the player's feet, into {@code out}.
    *
    * @param bodyYaw body yaw in degrees (0 faces +Z, as in the game)
    * @param headYaw head yaw in degrees
    * @param pitch head pitch in degrees (positive looks down)
    * @param limbPos walk-cycle position
    * @param limbSpeed walk-cycle amplitude, 0 (standing) to about 1 (sprinting)
    * @param height entity height in blocks; 1.8 gives the standard model
    */
   public static void compute(float bodyYaw, float headYaw, float pitch, float limbPos, float limbSpeed, boolean sneaking, float height, float[] out) {
      if (out.length < FLOATS) {
         throw new IllegalArgumentException("out needs " + FLOATS + " floats");
      }

      // The player model is 2 blocks tall and drawn at 15/16 size inside a 1.8-block hitbox.
      float scale = MODEL_SCALE * height / (sneaking ? 1.5F : 1.8F);
      float speed = Math.max(0.0F, Math.min(1.0F, limbSpeed));
      float legSwing = (float)Math.cos(limbPos * 0.6662F) * 1.4F * speed;
      float armSwing = (float)Math.cos(limbPos * 0.6662F + Math.PI) * speed;
      float lean = sneaking ? 0.5F : 0.0F;

      float hipY = LIMB;
      float neckY = hipY + (float)Math.cos(lean) * LIMB;
      float neckZ = (float)Math.sin(lean) * LIMB;

      float sin = (float)Math.sin(bodyYaw * DEG);
      float cos = (float)Math.cos(bodyYaw * DEG);

      // Head points along the look direction, relative to the body so it turns independently.
      float headTurn = (headYaw - bodyYaw) * DEG;
      float headPitch = pitch * DEG;
      float headX = -(float)Math.sin(headTurn) * (float)Math.sin(headPitch) * HEAD_LENGTH;
      float headY = (float)Math.cos(headPitch) * HEAD_LENGTH;
      float headZ = (float)Math.cos(headTurn) * (float)Math.sin(headPitch) * HEAD_LENGTH;

      bone(out, SPINE, 0.0F, hipY, 0.0F, 0.0F, neckY, neckZ, sin, cos, scale);
      bone(out, HEAD, 0.0F, neckY, neckZ, headX, neckY + headY, neckZ + headZ, sin, cos, scale);
      bone(out, SHOULDERS, -SHOULDER_HALF, neckY, neckZ, SHOULDER_HALF, neckY, neckZ, sin, cos, scale);
      limb(out, LEFT_ARM, SHOULDER_HALF, neckY, neckZ, -armSwing + lean * 0.4F, sin, cos, scale);
      limb(out, RIGHT_ARM, -SHOULDER_HALF, neckY, neckZ, armSwing + lean * 0.4F, sin, cos, scale);
      bone(out, HIPS, -HIP_HALF, hipY, 0.0F, HIP_HALF, hipY, 0.0F, sin, cos, scale);
      limb(out, LEFT_LEG, HIP_HALF, hipY, 0.0F, -legSwing, sin, cos, scale);
      limb(out, RIGHT_LEG, -HIP_HALF, hipY, 0.0F, legSwing, sin, cos, scale);
   }

   /** A limb hangs from its joint and swings forwards/backwards by {@code angle} radians. */
   private static void limb(float[] out, int index, float x, float y, float z, float angle, float sin, float cos, float scale) {
      float endY = y - (float)Math.cos(angle) * LIMB;
      float endZ = z + (float)Math.sin(angle) * LIMB;
      bone(out, index, x, y, z, x, endY, endZ, sin, cos, scale);
   }

   private static void bone(float[] out, int index, float x1, float y1, float z1, float x2, float y2, float z2, float sin, float cos, float scale) {
      int i = index * 6;
      out[i] = (x1 * cos - z1 * sin) * scale;
      out[i + 1] = y1 * scale;
      out[i + 2] = (x1 * sin + z1 * cos) * scale;
      out[i + 3] = (x2 * cos - z2 * sin) * scale;
      out[i + 4] = y2 * scale;
      out[i + 5] = (x2 * sin + z2 * cos) * scale;
   }
}
