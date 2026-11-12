package net.albiz.client.diagnostics;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Bounded per-chunk record of which world-data packets actually arrived.
 *
 * <p>Written from the network thread and read from the client thread, so every method is
 * synchronized; the critical sections are a single map operation.
 */
public final class ChunkReceiptLog {
   public enum Packet {
      CHUNK_DATA,
      SECTION_UPDATE,
      BLOCK_UPDATE
   }

   /** What can truthfully be said about a chunk's data. */
   public enum Status {
      /** A chunk-data packet arrived while listening and the chunk is in the client world. */
      AVAILABLE,
      /** A chunk-data packet arrived but the chunk is not in the client world (unloaded again or rejected). */
      INCOMPLETE,
      /** No chunk-data packet arrived while listening and the chunk is not loaded. */
      NOT_RECEIVED,
      /** The chunk is loaded but arrived before listening started; its packet was not observed. */
      UNKNOWN
   }

   public static final class Receipt {
      private final int[] counts = new int[Packet.values().length];
      private long firstMs;
      private long lastMs;

      public int count(Packet packet) {
         return this.counts[packet.ordinal()];
      }

      public long firstMs() {
         return this.firstMs;
      }

      public long lastMs() {
         return this.lastMs;
      }
   }

   private final int limit;
   private final LinkedHashMap<Long, Receipt> receipts = new LinkedHashMap<>(256, 0.75F, true);
   private long total;

   public ChunkReceiptLog(int limit) {
      if (limit < 1) {
         throw new IllegalArgumentException("limit must be positive");
      }
      this.limit = limit;
   }

   public static long key(int chunkX, int chunkZ) {
      return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
   }

   public synchronized void record(int chunkX, int chunkZ, Packet packet, long nowMs) {
      long key = key(chunkX, chunkZ);
      Receipt receipt = this.receipts.get(key);
      if (receipt == null) {
         if (this.receipts.size() >= this.limit) {
            Iterator<Long> eldest = this.receipts.keySet().iterator();
            eldest.next();
            eldest.remove();
         }
         receipt = new Receipt();
         receipt.firstMs = nowMs;
         this.receipts.put(key, receipt);
      }
      receipt.counts[packet.ordinal()]++;
      receipt.lastMs = nowMs;
      this.total++;
   }

   /** Copy of the counters, or {@code null} when nothing was observed for the chunk. */
   public synchronized Receipt snapshot(int chunkX, int chunkZ) {
      Receipt receipt = this.receipts.get(key(chunkX, chunkZ));
      if (receipt == null) {
         return null;
      }
      Receipt copy = new Receipt();
      System.arraycopy(receipt.counts, 0, copy.counts, 0, receipt.counts.length);
      copy.firstMs = receipt.firstMs;
      copy.lastMs = receipt.lastMs;
      return copy;
   }

   public Status status(int chunkX, int chunkZ, boolean loadedInClientWorld) {
      Receipt receipt = this.snapshot(chunkX, chunkZ);
      boolean data = receipt != null && receipt.count(Packet.CHUNK_DATA) > 0;
      if (data) {
         return loadedInClientWorld ? Status.AVAILABLE : Status.INCOMPLETE;
      }
      return loadedInClientWorld ? Status.UNKNOWN : Status.NOT_RECEIVED;
   }

   public synchronized int trackedChunks() {
      return this.receipts.size();
   }

   public synchronized long totalPackets() {
      return this.total;
   }

   public synchronized void clear() {
      this.receipts.clear();
      this.total = 0L;
   }

   /** Block Y of a section index, using the dimension's real bottom section instead of assuming Y=0 or Y=-64. */
   public static int sectionBaseY(int bottomSectionCoord, int sectionIndex) {
      return (bottomSectionCoord + sectionIndex) << 4;
   }

   /** Section index containing {@code blockY}, or -1 when it is outside the build range. */
   public static int sectionIndex(int bottomSectionCoord, int sectionCount, int blockY) {
      int index = (blockY >> 4) - bottomSectionCoord;
      return index >= 0 && index < sectionCount ? index : -1;
   }
}
