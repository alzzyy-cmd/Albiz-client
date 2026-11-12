package net.albiz.client.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.albiz.client.gui.SettingsTransition.Config;
import net.albiz.client.gui.SettingsTransition.Direction;
import net.albiz.client.gui.SettingsTransition.Easing;
import net.albiz.client.gui.SettingsTransition.Layout;
import org.junit.jupiter.api.Test;

class SettingsTransitionTest {
   private static final long MS = 1000000L;
   private static final Config LINEAR = new Config(true, 200.0F, Easing.LINEAR, 0.5F, 0.0F, 1.0F, Direction.TOP, 0.25F);

   @Test
   void progressDependsOnElapsedTimeOnly() {
      SettingsTransition a = new SettingsTransition();
      a.setOpen(true, 0L, LINEAR);
      assertEquals(0.0F, a.linear(0L, LINEAR), 1.0E-6F);
      assertEquals(0.5F, a.linear(100L * MS, LINEAR), 1.0E-6F);
      assertEquals(1.0F, a.linear(200L * MS, LINEAR), 1.0E-6F);
      assertEquals(1.0F, a.linear(9000L * MS, LINEAR), 1.0E-6F);
      // Sampling many times (high frame rate) gives the same value as sampling once.
      SettingsTransition b = new SettingsTransition();
      b.setOpen(true, 0L, LINEAR);
      for (long t = 0; t < 100; t++) {
         b.linear(t * MS, LINEAR);
         b.setOpen(true, t * MS, LINEAR);
      }
      assertEquals(a.linear(100L * MS, LINEAR), b.linear(100L * MS, LINEAR), 1.0E-6F);
   }

   @Test
   void reversesFromCurrentPositionWithoutJump() {
      SettingsTransition t = new SettingsTransition();
      t.setOpen(true, 0L, LINEAR);
      t.setOpen(false, 50L * MS, LINEAR);
      assertEquals(0.25F, t.linear(50L * MS, LINEAR), 1.0E-6F);
      assertEquals(0.125F, t.linear(75L * MS, LINEAR), 1.0E-6F);
      assertFalse(t.fullyClosed(75L * MS, LINEAR));
      assertTrue(t.fullyClosed(100L * MS, LINEAR));
      assertFalse(t.opening());
   }

   @Test
   void startsClosedAndDisabledJumps() {
      SettingsTransition t = new SettingsTransition();
      assertEquals(0.0F, t.linear(123L, LINEAR), 0.0F);
      assertTrue(t.settled(0L, LINEAR));
      t.setOpen(true, 0L, Config.OFF);
      assertEquals(1.0F, t.progress(0L, Config.OFF), 0.0F);
      t.setOpen(false, 1L, Config.OFF);
      assertTrue(t.fullyClosed(1L, Config.OFF));
   }

   @Test
   void easingsHitBothEnds() {
      for (Easing easing : Easing.values()) {
         assertEquals(0.0F, SettingsTransition.ease(easing, 0.0F), 1.0E-5F);
         assertEquals(1.0F, SettingsTransition.ease(easing, 1.0F), 1.0E-5F);
         assertEquals(1.0F, SettingsTransition.ease(easing, 7.0F), 1.0E-5F);
      }
      assertTrue(SettingsTransition.ease(Easing.EASE_OUT, 0.5F) > 0.5F);
      assertEquals(0.5F, SettingsTransition.ease(Easing.EASE_IN_OUT, 0.5F), 1.0E-5F);
      assertTrue(SettingsTransition.ease(Easing.OVERSHOOT, 0.8F) > 1.0F);
   }

   @Test
   void openLayoutLeavesAQuarterOfTheBrowserAndFillsTheRest() {
      Layout open = SettingsTransition.layout(Config.SMOOTH, 1.0F, 1920.0F, 1080.0F, 80.0F, 16.0F);
      assertEquals(-1440.0F, open.mainShiftX(), 1.0E-3F);
      assertEquals(480.0F + 16.0F, open.panelX(), 1.0E-3F);
      assertEquals(1440.0F - 32.0F, open.panelW(), 1.0E-3F);
      assertEquals(16.0F, open.panelY(), 1.0E-3F);
      assertEquals(1080.0F - 80.0F - 32.0F, open.panelH(), 1.0E-3F);
      assertEquals(1.0F, open.panelAlpha(), 1.0E-6F);
      // Panel width is ~75% of the screen.
      assertTrue(open.panelW() / 1920.0F > 0.7F);
   }

   @Test
   void closedLayoutStartsAboveTransparentAndSmaller() {
      Layout closed = SettingsTransition.layout(Config.SMOOTH, 0.0F, 1920.0F, 1080.0F, 80.0F, 16.0F);
      Layout open = SettingsTransition.layout(Config.SMOOTH, 1.0F, 1920.0F, 1080.0F, 80.0F, 16.0F);
      assertEquals(0.0F, closed.mainShiftX(), 0.0F);
      assertEquals(0.0F, closed.panelAlpha(), 1.0E-6F);
      assertTrue(closed.panelY() < open.panelY());
      assertTrue(closed.panelW() < open.panelW());
      Layout mid = SettingsTransition.layout(Config.SMOOTH, 0.5F, 1920.0F, 1080.0F, 80.0F, 16.0F);
      assertTrue(mid.panelY() > closed.panelY() && mid.panelY() < open.panelY());
      assertEquals(-720.0F, mid.mainShiftX(), 1.0E-3F);
   }

   @Test
   void directionsMoveAlongTheRightAxis() {
      float sw = 1000.0F;
      float sh = 500.0F;
      Layout rest = SettingsTransition.layout(LINEAR, 1.0F, sw, sh, 0.0F, 0.0F);
      for (Direction direction : Direction.values()) {
         Config config = new Config(true, 200.0F, Easing.LINEAR, 0.5F, 1.0F, 1.0F, direction, 0.25F);
         Layout start = SettingsTransition.layout(config, 0.0F, sw, sh, 0.0F, 0.0F);
         switch (direction) {
            case TOP -> assertEquals(rest.panelY() - 250.0F, start.panelY(), 1.0E-3F);
            case BOTTOM -> assertEquals(rest.panelY() + 250.0F, start.panelY(), 1.0E-3F);
            case LEFT -> assertEquals(rest.panelX() - 500.0F, start.panelX(), 1.0E-3F);
            case RIGHT -> assertEquals(rest.panelX() + 500.0F, start.panelX(), 1.0E-3F);
         }
      }
   }

   @Test
   void smallTouchScreenStillGetsAPositivePanel() {
      Layout open = SettingsTransition.layout(Config.FAST, 1.0F, 640.0F, 360.0F, 70.0F, 12.0F);
      assertTrue(open.panelW() > 400.0F);
      assertTrue(open.panelH() > 200.0F);
      assertTrue(open.panelX() + open.panelW() <= 640.0F);
   }

   @Test
   void configIsClampedAndPresetsResolve() {
      Config wild = new Config(true, -5.0F, null, 9.0F, -1.0F, 0.1F, null, 4.0F);
      assertEquals(0.0F, wild.durationMs(), 0.0F);
      assertEquals(1.0F, wild.distance(), 0.0F);
      assertEquals(0.0F, wild.startOpacity(), 0.0F);
      assertEquals(0.5F, wild.startScale(), 0.0F);
      assertEquals(1.0F, wild.mainVisible(), 0.0F);
      assertEquals(Easing.EASE_OUT, wild.easing());
      assertEquals(Direction.TOP, wild.direction());
      assertEquals(Config.SMOOTH, Config.preset("Smooth"));
      assertEquals(Config.FAST, Config.preset("Fast"));
      assertEquals(Config.SOFT, Config.preset("Soft"));
      assertNull(Config.preset("Custom"));
      assertNull(Config.preset(null));
      assertEquals(Easing.OVERSHOOT, Easing.byLabel("Overshoot"));
      assertEquals(Easing.EASE_OUT, Easing.byLabel("nope"));
      assertEquals(Direction.RIGHT, Direction.byLabel("From Right"));
   }
}
