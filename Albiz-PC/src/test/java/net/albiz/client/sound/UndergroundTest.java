package net.albiz.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UndergroundTest {
   @Test
   void negativeYIsHandledLikeAnyOtherHeight() {
      assertTrue(Underground.accept(-58.5, 20, -64, 70, 8, true));
      assertTrue(Underground.accept(-0.5, 20, -64, 70, 8, true));
      assertTrue(Underground.accept(0.0, 20, -64, 70, 8, true));
      assertTrue(Underground.accept(12.0, 20, -64, 70, 8, true));
   }

   @Test
   void worldBottomComesFromTheDimensionNotFromAConstant() {
      // Custom datapack dimension with min_y = -128.
      assertTrue(Underground.accept(-120.0, 20, -128, 64, 8, true));
      assertFalse(Underground.accept(-120.0, 20, -64, 64, 8, true));
      // Nether/End style dimension starting at 0.
      assertFalse(Underground.accept(-1.0, 20, 0, 64, 8, true));
      assertTrue(Underground.accept(5.0, 20, 0, 64, 8, true));
   }

   @Test
   void ceilingAndDepth() {
      assertFalse(Underground.accept(21.0, 20, -64, 100, 8, true));
      // Ocean floor at Y=14 under a surface at Y=16 is not deep enough.
      assertFalse(Underground.accept(14.0, 20, -64, 16, 8, true));
      assertTrue(Underground.accept(14.0, 20, -64, 16, 8, false));
      assertEquals(Underground.State.SURFACE, Underground.state(64.0, 64, 0));
      assertEquals(Underground.State.BELOW_SURFACE, Underground.state(55.5, 64, 8));
   }

   @Test
   void unknownSurfaceIsReportedAndDoesNotHideEvidence() {
      assertEquals(Underground.State.UNKNOWN_SURFACE, Underground.state(-30.0, Underground.NO_SURFACE, 8));
      assertTrue(Underground.accept(-30.0, 20, -64, Underground.NO_SURFACE, 8, true));
      assertFalse(Underground.accept(Double.NaN, 20, -64, 70, 8, false));
   }
}
