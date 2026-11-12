package net.albiz.client.esp;

import java.util.ArrayDeque;
import java.util.HashSet;

/**
 * Deduplicating, bounded FIFO of chunk coordinates for budgeted client-thread scans.
 * Not thread-safe: use it from the client thread only.
 */
public final class ChunkWorkQueue {
   private final int limit;
   private final ArrayDeque<Long> queue = new ArrayDeque<>();
   private final HashSet<Long> queued = new HashSet<>();

   public ChunkWorkQueue(int limit) {
      if (limit < 1) {
         throw new IllegalArgumentException("limit must be positive");
      }
      this.limit = limit;
   }

   public static long key(int chunkX, int chunkZ) {
      return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
   }

   public static int x(long key) {
      return (int)key;
   }

   public static int z(long key) {
      return (int)(key >>> 32);
   }

   /** @return false when the chunk is already queued or the queue is full. */
   public boolean offer(int chunkX, int chunkZ) {
      if (this.queue.size() >= this.limit) {
         return false;
      }
      long key = key(chunkX, chunkZ);
      if (!this.queued.add(key)) {
         return false;
      }
      this.queue.add(key);
      return true;
   }

   /** Queues the square around a centre, nearest rings first. */
   public void offerSquare(int centerX, int centerZ, int radius) {
      this.offer(centerX, centerZ);
      for (int ring = 1; ring <= radius; ring++) {
         for (int d = -ring; d <= ring; d++) {
            this.offer(centerX + d, centerZ - ring);
            this.offer(centerX + d, centerZ + ring);
         }
         for (int d = -ring + 1; d < ring; d++) {
            this.offer(centerX - ring, centerZ + d);
            this.offer(centerX + ring, centerZ + d);
         }
      }
   }

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   public int size() {
      return this.queue.size();
   }

   /** @throws java.util.NoSuchElementException when empty. */
   public long poll() {
      long key = this.queue.remove();
      this.queued.remove(key);
      return key;
   }

   public void clear() {
      this.queue.clear();
      this.queued.clear();
   }
}
