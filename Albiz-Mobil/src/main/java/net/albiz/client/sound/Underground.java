package net.albiz.client.sound;

/**
 * Decides whether a position counts as underground without assuming that Y=0 is special.
 *
 * <p>The surface height comes from the client's heightmap when the column is loaded. When it is
 * not, the result is {@link State#UNKNOWN_SURFACE} and only the absolute ceiling can be applied.
 */
public final class Underground {
   private Underground() {
   }

   public enum State {
      /** Below the known surface by at least the requested depth. */
      BELOW_SURFACE,
      /** At or above the known surface. */
      SURFACE,
      /** The column's heightmap is not available on the client; depth cannot be judged. */
      UNKNOWN_SURFACE
   }

   /** Sentinel for "the client has no heightmap for this column". */
   public static final int NO_SURFACE = Integer.MIN_VALUE;

   public static State state(double y, int surfaceY, int minDepth) {
      if (!Double.isFinite(y) || surfaceY == NO_SURFACE) {
         return State.UNKNOWN_SURFACE;
      }
      // The heightmap value is the first free Y above the top block, hence the strict comparison.
      return y < (double)surfaceY - Math.max(0, minDepth) ? State.BELOW_SURFACE : State.SURFACE;
   }

   /**
    * @param ceilingY     positions above this Y are never accepted
    * @param worldBottomY actual bottom build height of the dimension (not assumed to be -64 or 0)
    * @param requireDepth when true, a known surface must be at least {@code minDepth} above the position
    */
   public static boolean accept(double y, int ceilingY, int worldBottomY, int surfaceY, int minDepth, boolean requireDepth) {
      if (!Double.isFinite(y) || y > ceilingY || y < worldBottomY) {
         return false;
      }
      if (!requireDepth) {
         return true;
      }
      // An unknown surface is not treated as proof of depth, but it must not hide evidence either.
      return state(y, surfaceY, minDepth) != State.SURFACE;
   }
}
