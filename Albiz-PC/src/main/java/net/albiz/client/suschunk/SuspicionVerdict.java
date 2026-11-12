package net.albiz.client.suschunk;

/**
 * Four-state verdict for one chunk, from the suspicious blocks Suspicious ESP found in it.
 * A single kind of block is never enough for {@link #STRONG}: geodes, jungles and deep caves
 * produce plenty of one kind naturally.
 */
public enum SuspicionVerdict {
   CLEAR,
   WEAK,
   SUSPICIOUS,
   STRONG;

   /** A single kind needs at least this many blocks to count as more than {@link #WEAK}. */
   public static final int CLUSTER = 4;

   /**
    * @param deepslate plain deepslate found inside the configured (unnatural) Y range
    * @param rotatedDeepslate sideways deepslate-family blocks; these are placed by players
    * @param vines vines of any type
    * @param amethyst amethyst clusters
    * @param kelpColumn whether the chunk passed the fully-grown-kelp threshold
    */
   public static SuspicionVerdict of(int deepslate, int rotatedDeepslate, int vines, int amethyst, boolean kelpColumn) {
      int kinds = (deepslate > 0 ? 1 : 0) + (rotatedDeepslate > 0 ? 1 : 0) + (vines > 0 ? 1 : 0) + (amethyst > 0 ? 1 : 0) + (kelpColumn ? 1 : 0);
      if (kinds == 0) {
         return CLEAR;
      }

      // Rotated deepslate and a full kelp column are deliberate player work.
      boolean playerMade = rotatedDeepslate > 0 || kelpColumn;
      if (kinds >= 3 || kinds == 2 && playerMade) {
         return STRONG;
      }

      int total = Math.max(0, deepslate) + Math.max(0, rotatedDeepslate) + Math.max(0, vines) + Math.max(0, amethyst);
      return playerMade || kinds == 2 || total >= CLUSTER ? SUSPICIOUS : WEAK;
   }

   public boolean atLeast(SuspicionVerdict other) {
      return this.ordinal() >= other.ordinal();
   }
}
