package net.albiz.client.util.seedscan;

/**
 * Vanilla overworld stone/deepslate transition: deepslate at Y <= 0, stone at Y >= 8, and in
 * between a per-block seeded roll against a linear chance.
 */
public final class DeepslateGradient {
   public static final int ALWAYS_AT_OR_BELOW = 0;
   public static final int NEVER_AT_OR_ABOVE = 8;

   private DeepslateGradient() {
   }

   /** Y levels where the seeded roll decides, i.e. where a mismatch is informative. */
   public static boolean informative(int y) {
      return y > ALWAYS_AT_OR_BELOW && y < NEVER_AT_OR_ABOVE;
   }

   public static double chance(int y) {
      if (y <= ALWAYS_AT_OR_BELOW) {
         return 1.0;
      }
      if (y >= NEVER_AT_OR_ABOVE) {
         return 0.0;
      }
      return 1.0 - (double)y / (double)NEVER_AT_OR_ABOVE;
   }

   /** @param roll the positional random float in [0,1) for the block. */
   public static boolean expectsDeepslate(int y, float roll) {
      return (double)roll < chance(y);
   }

   /** @return the parsed seed, or {@code null} when the text is not a 64-bit integer. */
   public static Long parseSeed(String text) {
      if (text == null) {
         return null;
      }
      try {
         return Long.parseLong(text.trim());
      } catch (NumberFormatException e) {
         return null;
      }
   }
}
