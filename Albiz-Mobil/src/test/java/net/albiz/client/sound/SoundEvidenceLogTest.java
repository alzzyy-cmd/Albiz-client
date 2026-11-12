package net.albiz.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.albiz.client.sound.SoundEvidenceLog.Confidence;
import net.albiz.client.sound.SoundEvidenceLog.Hotspot;
import net.albiz.client.sound.SoundEvidenceLog.Source;
import org.junit.jupiter.api.Test;

class SoundEvidenceLogTest {
   private static Hotspot add(SoundEvidenceLog log, long now, SoundKind kind, String detail, double x, double y, double z) {
      return log.add(now, Source.SOUND_PACKET, kind, detail, x, y, z, kind.weight(), 400L);
   }

   @Test
   void duplicatesInsideTheWindowAreMergedNotRescored() {
      SoundEvidenceLog log = new SoundEvidenceLog(16, 4);
      Hotspot first = add(log, 1000L, SoundKind.PISTON, "minecraft:block.piston.extend", 10.5, -40.0, 10.5);
      assertNotNull(first);
      assertNull(add(log, 1200L, SoundKind.PISTON, "minecraft:block.piston.extend", 10.5, -40.0, 10.5));
      assertEquals(1, log.historySize());
      assertEquals(1L, log.merged());
      assertEquals(2, log.history().iterator().next().repeats);
      assertEquals(SoundKind.PISTON.weight(), (int)first.score);
      // After the window the same sound is a new observation.
      assertNotNull(add(log, 5000L, SoundKind.PISTON, "minecraft:block.piston.extend", 10.5, -40.0, 10.5));
      assertEquals(2, log.historySize());
   }

   @Test
   void negativeCoordinatesMapToTheCorrectChunk() {
      SoundEvidenceLog log = new SoundEvidenceLog(16, 4);
      Hotspot spot = add(log, 0L, SoundKind.KELP, "a", -0.5, -63.5, -16.5);
      assertEquals(-1, spot.chunkX);
      assertEquals(-2, spot.chunkZ);
      assertEquals(-63.5, spot.minY, 1.0E-9);
      Hotspot same = add(log, 1L, SoundKind.KELP, "b", -15.9, -10.0, -32.0);
      assertTrue(spot == same);
      assertEquals(2, same.events);
      assertEquals(-10.0, same.maxY, 1.0E-9);
   }

   @Test
   void historyAndHotspotsAreBounded() {
      SoundEvidenceLog log = new SoundEvidenceLog(8, 3);
      for (int i = 0; i < 100; i++) {
         add(log, i, SoundKind.BLOCK_BREAK, "s" + i, i * 16 + 1.0, 0.0, 0.0);
      }
      assertEquals(8, log.historySize());
      assertEquals(3, log.hotspotCount());
      assertEquals(100L, log.accepted());
   }

   @Test
   void confidenceNeedsPlayerEvidenceToBeHigh() {
      SoundEvidenceLog log = new SoundEvidenceLog(64, 8);
      Hotspot mobs = null;
      for (int i = 0; i < 20; i++) {
         mobs = add(log, i, SoundKind.HOSTILE_MOB, "zombie" + i, 1.0, -20.0, 1.0);
      }
      assertEquals(Confidence.LOW, mobs.confidence());

      Hotspot farm = add(log, 100L, SoundKind.PISTON, "p", 100.0, -20.0, 1.0);
      assertEquals(Confidence.LOW, farm.confidence());
      farm = add(log, 101L, SoundKind.KELP, "k", 101.0, -20.0, 1.0);
      assertEquals(Confidence.MEDIUM, farm.confidence());
      farm = add(log, 102L, SoundKind.CONTAINER, "c", 102.0, -20.0, 1.0);
      assertEquals(Confidence.HIGH, farm.confidence());
      assertTrue(farm.explain(4).contains("Piston x1"));
   }

   @Test
   void zeroWeightIsLoggedButNeverCreatesAHotspot() {
      SoundEvidenceLog log = new SoundEvidenceLog(8, 3);
      assertNull(log.add(0L, Source.SOUND_PACKET, SoundKind.AMBIENT, "minecraft:ambient.cave", 0.0, 0.0, 0.0, 0.0, 400L));
      assertEquals(1, log.historySize());
      assertEquals(0, log.hotspotCount());
   }

   @Test
   void expiryAndClear() {
      SoundEvidenceLog log = new SoundEvidenceLog(8, 3);
      add(log, 1000L, SoundKind.PISTON, "old", 0.0, 0.0, 0.0);
      add(log, 9000L, SoundKind.PISTON, "new", 64.0, 0.0, 0.0);
      log.expire(5000L);
      assertEquals(1, log.historySize());
      assertEquals(1, log.hotspotCount());
      // An expired entry must not keep suppressing a later identical sound.
      assertNotNull(add(log, 9100L, SoundKind.PISTON, "old", 0.0, 0.0, 0.0));
      log.clear();
      assertEquals(0, log.historySize());
      assertEquals(0, log.hotspotCount());
      assertFalse(log.hotspots().iterator().hasNext());
      assertThrows(IllegalArgumentException.class, () -> new SoundEvidenceLog(0, 1));
   }
}
