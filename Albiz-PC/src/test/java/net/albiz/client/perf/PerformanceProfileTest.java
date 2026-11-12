package net.albiz.client.perf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PerformanceProfileTest {
   @Test
   void customKeepsTheUsersOwnValues() {
      assertEquals(3.5F, PerformanceProfile.CUSTOM.scanBudgetMs(3.5F, false), 0.0F);
      assertEquals(6, PerformanceProfile.CUSTOM.chunksPerTick(6, false));
   }

   @Test
   void presetsIgnoreTheCustomSliderAndOrderCorrectly() {
      float quality = PerformanceProfile.QUALITY.scanBudgetMs(0.5F, false);
      float balanced = PerformanceProfile.BALANCED.scanBudgetMs(0.5F, false);
      float performance = PerformanceProfile.PERFORMANCE.scanBudgetMs(8.0F, false);
      assertTrue(quality > balanced && balanced > performance);
      assertEquals(2.0F, balanced, 0.0F);
      assertEquals(9, PerformanceProfile.QUALITY.chunksPerTick(6, false));
      assertEquals(6, PerformanceProfile.BALANCED.chunksPerTick(6, false));
      assertEquals(3, PerformanceProfile.PERFORMANCE.chunksPerTick(6, false));
   }

   @Test
   void mobileIsCappedButNeverStalls() {
      for (PerformanceProfile profile : PerformanceProfile.values()) {
         assertTrue(profile.scanBudgetMs(8.0F, true) <= PerformanceProfile.MOBILE_SCAN_BUDGET_MS);
         assertTrue(profile.chunksPerTick(1, true) >= 1);
         assertTrue(profile.chunksPerTick(0, false) >= 1);
         assertTrue(profile.chunksPerTick(8, true) <= profile.chunksPerTick(8, false));
      }
      assertEquals(0.5F, PerformanceProfile.CUSTOM.scanBudgetMs(0.5F, true), 0.0F);
   }

   @Test
   void labelsRoundTripAndUnknownFallsBackToCustom() {
      for (PerformanceProfile profile : PerformanceProfile.values()) {
         assertEquals(profile, PerformanceProfile.byLabel(profile.label()));
      }
      assertEquals(PerformanceProfile.CUSTOM, PerformanceProfile.byLabel("nonsense"));
      assertEquals(PerformanceProfile.CUSTOM, PerformanceProfile.byLabel(null));
   }
}
