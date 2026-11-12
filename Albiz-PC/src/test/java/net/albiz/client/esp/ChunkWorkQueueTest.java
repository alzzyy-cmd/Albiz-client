package net.albiz.client.esp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChunkWorkQueueTest {
   @Test
   void keyRoundTripsNegativeCoordinates() {
      long key = ChunkWorkQueue.key(-7, 12345);
      assertEquals(-7, ChunkWorkQueue.x(key));
      assertEquals(12345, ChunkWorkQueue.z(key));
      key = ChunkWorkQueue.key(30000, -30000);
      assertEquals(30000, ChunkWorkQueue.x(key));
      assertEquals(-30000, ChunkWorkQueue.z(key));
   }

   @Test
   void deduplicatesUntilPolled() {
      ChunkWorkQueue queue = new ChunkWorkQueue(16);
      assertTrue(queue.offer(1, 2));
      assertFalse(queue.offer(1, 2));
      assertEquals(1, queue.size());
      assertEquals(ChunkWorkQueue.key(1, 2), queue.poll());
      assertTrue(queue.isEmpty());
      assertTrue(queue.offer(1, 2));
   }

   @Test
   void squareCoversEveryChunkOnceNearestFirst() {
      ChunkWorkQueue queue = new ChunkWorkQueue(1000);
      queue.offerSquare(-3, 5, 2);
      assertEquals(25, queue.size());
      long first = queue.poll();
      assertEquals(-3, ChunkWorkQueue.x(first));
      assertEquals(5, ChunkWorkQueue.z(first));
      int lastRing = 0;
      while (!queue.isEmpty()) {
         long key = queue.poll();
         int ring = Math.max(Math.abs(ChunkWorkQueue.x(key) + 3), Math.abs(ChunkWorkQueue.z(key) - 5));
         assertTrue(ring >= lastRing);
         assertTrue(ring <= 2);
         lastRing = ring;
      }
   }

   @Test
   void bounded() {
      ChunkWorkQueue queue = new ChunkWorkQueue(4);
      queue.offerSquare(0, 0, 5);
      assertEquals(4, queue.size());
      assertFalse(queue.offer(100, 100));
      queue.clear();
      assertTrue(queue.offer(100, 100));
   }
}
