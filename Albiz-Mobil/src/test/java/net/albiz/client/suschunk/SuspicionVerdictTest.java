package net.albiz.client.suschunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuspicionVerdictTest {
   @Test
   void nothingFoundIsClear() {
      assertEquals(SuspicionVerdict.CLEAR, SuspicionVerdict.of(0, 0, 0, 0, false));
   }

   @Test
   void aFewBlocksOfOneNaturalKindAreWeak() {
      assertEquals(SuspicionVerdict.WEAK, SuspicionVerdict.of(0, 0, 3, 0, false));
      assertEquals(SuspicionVerdict.WEAK, SuspicionVerdict.of(1, 0, 0, 0, false));
   }

   @Test
   void oneKindNeverReachesStrongHoweverMany() {
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 0, 0, 500, false));
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 0, SuspicionVerdict.CLUSTER, 0, false));
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 900, 0, 0, false));
   }

   @Test
   void playerMadeEvidenceAloneIsSuspicious() {
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 1, 0, 0, false));
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 0, 0, 0, true));
   }

   @Test
   void twoNaturalKindsAreSuspiciousNotStrong() {
      assertEquals(SuspicionVerdict.SUSPICIOUS, SuspicionVerdict.of(0, 0, 1, 1, false));
   }

   @Test
   void playerMadePlusAnotherKindIsStrong() {
      assertEquals(SuspicionVerdict.STRONG, SuspicionVerdict.of(1, 1, 0, 0, false));
      assertEquals(SuspicionVerdict.STRONG, SuspicionVerdict.of(0, 0, 2, 0, true));
      assertEquals(SuspicionVerdict.STRONG, SuspicionVerdict.of(0, 1, 0, 0, true));
   }

   @Test
   void threeKindsAreStrong() {
      assertEquals(SuspicionVerdict.STRONG, SuspicionVerdict.of(1, 0, 1, 1, false));
   }

   @Test
   void negativeCountsAreTreatedAsAbsent() {
      assertEquals(SuspicionVerdict.CLEAR, SuspicionVerdict.of(-5, -1, -2, -3, false));
   }

   @Test
   void ordering() {
      assertTrue(SuspicionVerdict.STRONG.atLeast(SuspicionVerdict.SUSPICIOUS));
      assertTrue(SuspicionVerdict.WEAK.atLeast(SuspicionVerdict.WEAK));
      assertFalse(SuspicionVerdict.WEAK.atLeast(SuspicionVerdict.SUSPICIOUS));
   }
}
