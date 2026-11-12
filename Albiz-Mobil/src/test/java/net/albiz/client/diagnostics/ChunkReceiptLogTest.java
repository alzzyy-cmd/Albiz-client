package net.albiz.client.diagnostics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.albiz.client.diagnostics.ChunkReceiptLog.Packet;
import net.albiz.client.diagnostics.ChunkReceiptLog.Status;
import org.junit.jupiter.api.Test;

class ChunkReceiptLogTest {
   @Test
   void statusSeparatesMissingPacketsFromUnobservedOnes() {
      ChunkReceiptLog log = new ChunkReceiptLog(8);
      assertEquals(Status.NOT_RECEIVED, log.status(3, -4, false));
      assertEquals(Status.UNKNOWN, log.status(3, -4, true));
      log.record(3, -4, Packet.BLOCK_UPDATE, 10L);
      // A block update alone does not prove the chunk's sections were received.
      assertEquals(Status.UNKNOWN, log.status(3, -4, true));
      log.record(3, -4, Packet.CHUNK_DATA, 20L);
      assertEquals(Status.AVAILABLE, log.status(3, -4, true));
      assertEquals(Status.INCOMPLETE, log.status(3, -4, false));
      assertEquals(1, log.snapshot(3, -4).count(Packet.BLOCK_UPDATE));
      assertEquals(10L, log.snapshot(3, -4).firstMs());
      assertEquals(20L, log.snapshot(3, -4).lastMs());
   }

   @Test
   void negativeChunkCoordinatesDoNotCollide() {
      ChunkReceiptLog log = new ChunkReceiptLog(8);
      log.record(-1, 0, Packet.CHUNK_DATA, 0L);
      assertNull(log.snapshot(0, -1));
      assertNull(log.snapshot(1, 0));
      assertEquals(1, log.snapshot(-1, 0).count(Packet.CHUNK_DATA));
   }

   @Test
   void boundedLeastRecentlyUsed() {
      ChunkReceiptLog log = new ChunkReceiptLog(3);
      for (int i = 0; i < 10; i++) {
         log.record(i, 0, Packet.CHUNK_DATA, i);
      }
      assertEquals(3, log.trackedChunks());
      assertEquals(10L, log.totalPackets());
      assertNull(log.snapshot(0, 0));
      assertEquals(1, log.snapshot(9, 0).count(Packet.CHUNK_DATA));
      log.clear();
      assertEquals(0, log.trackedChunks());
   }

   @Test
   void sectionMathUsesTheRealWorldBottom() {
      // Overworld 1.18+: min_y -64 -> bottom section -4, 24 sections.
      assertEquals(-64, ChunkReceiptLog.sectionBaseY(-4, 0));
      assertEquals(-16, ChunkReceiptLog.sectionBaseY(-4, 3));
      assertEquals(0, ChunkReceiptLog.sectionBaseY(-4, 4));
      assertEquals(0, ChunkReceiptLog.sectionIndex(-4, 24, -64));
      assertEquals(3, ChunkReceiptLog.sectionIndex(-4, 24, -1));
      assertEquals(4, ChunkReceiptLog.sectionIndex(-4, 24, 0));
      assertEquals(23, ChunkReceiptLog.sectionIndex(-4, 24, 319));
      assertEquals(-1, ChunkReceiptLog.sectionIndex(-4, 24, 320));
      assertEquals(-1, ChunkReceiptLog.sectionIndex(-4, 24, -65));
      // Nether-style dimension: bottom 0, 16 sections. Negative Y is outside, not section 0.
      assertEquals(-1, ChunkReceiptLog.sectionIndex(0, 16, -1));
      assertEquals(0, ChunkReceiptLog.sectionIndex(0, 16, 0));
      // Custom datapack dimension: min_y -128.
      assertEquals(-128, ChunkReceiptLog.sectionBaseY(-8, 0));
      assertEquals(0, ChunkReceiptLog.sectionIndex(-8, 32, -128));
   }
}
