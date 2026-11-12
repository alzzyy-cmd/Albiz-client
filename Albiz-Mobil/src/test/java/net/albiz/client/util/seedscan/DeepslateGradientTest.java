package net.albiz.client.util.seedscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeepslateGradientTest {
   @Test
   void chanceIsLinearBetweenZeroAndEight() {
      assertEquals(1.0, DeepslateGradient.chance(-64), 0.0);
      assertEquals(1.0, DeepslateGradient.chance(0), 0.0);
      assertEquals(0.875, DeepslateGradient.chance(1), 1.0E-9);
      assertEquals(0.5, DeepslateGradient.chance(4), 1.0E-9);
      assertEquals(0.125, DeepslateGradient.chance(7), 1.0E-9);
      assertEquals(0.0, DeepslateGradient.chance(8), 0.0);
      assertEquals(0.0, DeepslateGradient.chance(200), 0.0);
   }

   @Test
   void onlyTheTransitionBandIsInformative() {
      assertFalse(DeepslateGradient.informative(0));
      assertTrue(DeepslateGradient.informative(1));
      assertTrue(DeepslateGradient.informative(7));
      assertFalse(DeepslateGradient.informative(8));
   }

   @Test
   void rollDecides() {
      assertTrue(DeepslateGradient.expectsDeepslate(4, 0.49F));
      assertFalse(DeepslateGradient.expectsDeepslate(4, 0.5F));
      assertTrue(DeepslateGradient.expectsDeepslate(0, 0.999F));
      assertFalse(DeepslateGradient.expectsDeepslate(8, 0.0F));
   }

   @Test
   void seedParsing() {
      assertEquals(Long.valueOf(-4172144997902289642L), DeepslateGradient.parseSeed(" -4172144997902289642 "));
      assertNull(DeepslateGradient.parseSeed(""));
      assertNull(DeepslateGradient.parseSeed("abc"));
      assertNull(DeepslateGradient.parseSeed(null));
   }
}
