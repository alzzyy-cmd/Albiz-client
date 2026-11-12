package net.albiz.client.sound;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded history of received sound evidence plus per-chunk hotspots.
 *
 * <p>Not thread-safe: owned by the client thread. Network-thread code hands observations over
 * through a queue and never touches this class.
 */
public final class SoundEvidenceLog {
   /** Where the observation came from; shown verbatim so the user can judge it. */
   public enum Source {
      SOUND_PACKET("Sound"),
      ENTITY_SOUND_PACKET("Entity sound"),
      WORLD_EVENT("World event"),
      BLOCK_ACTION("Block action"),
      BLOCK_CHANGE("Block change"),
      ENTITY_SEEN("Entity seen");

      private final String label;

      Source(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }
   }

   /** How far the evidence in a chunk supports "someone is or was active here". */
   public enum Confidence {
      /** Only natural-capable sources so far. */
      LOW("Low"),
      /** Automation or several independent kinds. */
      MEDIUM("Medium"),
      /** Player-only sources, or automation combined with player sources. */
      HIGH("High");

      private final String label;

      Confidence(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }
   }

   public static final class Entry {
      public final long timeMs;
      public final Source source;
      public final SoundKind kind;
      /** Sound id, world-event label or block transition; exactly what was received. */
      public final String detail;
      public final double x;
      public final double y;
      public final double z;
      /** Identical observations merged into this entry inside the duplicate window. */
      public int repeats = 1;
      public long lastMs;
      private final long key;

      Entry(long key, long timeMs, Source source, SoundKind kind, String detail, double x, double y, double z) {
         this.key = key;
         this.timeMs = timeMs;
         this.lastMs = timeMs;
         this.source = source;
         this.kind = kind;
         this.detail = detail;
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }

   public static final class Hotspot {
      public final int chunkX;
      public final int chunkZ;
      public double score;
      public long firstMs;
      public long lastMs;
      public int events;
      public double sumY;
      public double minY = Double.POSITIVE_INFINITY;
      public double maxY = Double.NEGATIVE_INFINITY;
      private final int[] kindCounts = new int[SoundKind.values().length];
      public boolean alerted;

      Hotspot(int chunkX, int chunkZ, long now) {
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         this.firstMs = now;
         this.lastMs = now;
      }

      public int count(SoundKind kind) {
         return this.kindCounts[kind.ordinal()];
      }

      public int distinctKinds() {
         int n = 0;
         for (int c : this.kindCounts) {
            if (c > 0) {
               n++;
            }
         }
         return n;
      }

      public double averageY() {
         return this.events == 0 ? 0.0 : this.sumY / this.events;
      }

      private int countOrigin(SoundKind.Origin origin) {
         int n = 0;
         for (SoundKind kind : SoundKind.values()) {
            if (kind.origin() == origin) {
               n += this.kindCounts[kind.ordinal()];
            }
         }
         return n;
      }

      public Confidence confidence() {
         int player = this.countOrigin(SoundKind.Origin.PLAYER);
         int machine = this.countOrigin(SoundKind.Origin.AUTOMATION_OR_PLAYER);
         if (player >= 2 || player >= 1 && machine >= 1) {
            return Confidence.HIGH;
         }
         if (player == 1 || machine >= 2 || machine >= 1 && this.distinctKinds() >= 2) {
            return Confidence.MEDIUM;
         }
         return Confidence.LOW;
      }

      /** Kinds ordered by count, e.g. {@code "Piston x4, Kelp x2"}; the visible evidence explanation. */
      public String explain(int maxKinds) {
         List<SoundKind> kinds = new ArrayList<>();
         for (SoundKind kind : SoundKind.values()) {
            if (this.kindCounts[kind.ordinal()] > 0) {
               kinds.add(kind);
            }
         }
         kinds.sort((a, b) -> Integer.compare(this.kindCounts[b.ordinal()], this.kindCounts[a.ordinal()]));
         StringBuilder out = new StringBuilder();
         for (int i = 0; i < kinds.size() && i < maxKinds; i++) {
            if (i > 0) {
               out.append(", ");
            }
            out.append(kinds.get(i).label()).append(" x").append(this.kindCounts[kinds.get(i).ordinal()]);
         }
         if (kinds.size() > maxKinds) {
            out.append(", +").append(kinds.size() - maxKinds);
         }
         return out.toString();
      }
   }

   private final int historyLimit;
   private final int hotspotLimit;
   private final ArrayDeque<Entry> history = new ArrayDeque<>();
   private final Map<Long, Entry> recent = new HashMap<>();
   private final LinkedHashMap<Long, Hotspot> hotspots = new LinkedHashMap<>(64, 0.75F, true);
   private long accepted;
   private long merged;

   public SoundEvidenceLog(int historyLimit, int hotspotLimit) {
      if (historyLimit < 1 || hotspotLimit < 1) {
         throw new IllegalArgumentException("limits must be positive");
      }
      this.historyLimit = historyLimit;
      this.hotspotLimit = hotspotLimit;
   }

   public static long chunkKey(int chunkX, int chunkZ) {
      return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
   }

   private static int floor(double v) {
      int i = (int)v;
      return v < i ? i - 1 : i;
   }

   private static long dedupeKey(Source source, String detail, double x, double y, double z) {
      long h = 1469598103934665603L;
      h = (h ^ source.ordinal()) * 1099511628211L;
      h = (h ^ detail.hashCode()) * 1099511628211L;
      h = (h ^ floor(x)) * 1099511628211L;
      h = (h ^ floor(y)) * 1099511628211L;
      h = (h ^ floor(z)) * 1099511628211L;
      return h;
   }

   /**
    * Records one observation.
    *
    * @param weight        hotspot weight; zero keeps the entry in the history without scoring
    * @param dedupeMs      identical source/detail/block within this window is merged, not re-scored
    * @return the hotspot that changed, or {@code null} when the observation was merged or unscored
    */
   public Hotspot add(long now, Source source, SoundKind kind, String detail, double x, double y, double z, double weight, long dedupeMs) {
      String text = detail == null ? "" : detail;
      long key = dedupeKey(source, text, x, y, z);
      Entry previous = this.recent.get(key);
      if (previous != null && now - previous.lastMs <= dedupeMs && previous.source == source && previous.detail.equals(text)) {
         previous.repeats++;
         previous.lastMs = now;
         this.merged++;
         return null;
      }

      Entry entry = new Entry(key, now, source, kind, text, x, y, z);
      this.history.addLast(entry);
      this.recent.put(key, entry);
      this.accepted++;
      while (this.history.size() > this.historyLimit) {
         Entry dropped = this.history.removeFirst();
         this.recent.remove(dropped.key, dropped);
      }

      if (weight <= 0.0) {
         return null;
      }

      int cx = floor(x) >> 4;
      int cz = floor(z) >> 4;
      long chunk = chunkKey(cx, cz);
      Hotspot spot = this.hotspots.get(chunk);
      if (spot == null) {
         if (this.hotspots.size() >= this.hotspotLimit) {
            Iterator<Long> eldest = this.hotspots.keySet().iterator();
            eldest.next();
            eldest.remove();
         }
         spot = new Hotspot(cx, cz, now);
         this.hotspots.put(chunk, spot);
      }
      spot.score = Math.min(10000.0, spot.score + weight);
      spot.lastMs = now;
      spot.events++;
      spot.sumY += y;
      spot.minY = Math.min(spot.minY, y);
      spot.maxY = Math.max(spot.maxY, y);
      spot.kindCounts[kind.ordinal()]++;
      return spot;
   }

   /** Drops history and hotspots that have not been refreshed since {@code cutoffMs}. */
   public void expire(long cutoffMs) {
      while (!this.history.isEmpty() && this.history.peekFirst().lastMs < cutoffMs) {
         Entry dropped = this.history.removeFirst();
         this.recent.remove(dropped.key, dropped);
      }
      this.hotspots.values().removeIf(spot -> spot.lastMs < cutoffMs);
   }

   public void clear() {
      this.history.clear();
      this.recent.clear();
      this.hotspots.clear();
      this.accepted = 0L;
      this.merged = 0L;
   }

   /** Oldest first. Read-only view; do not hold across ticks. */
   public Iterable<Entry> history() {
      return Collections.unmodifiableCollection(this.history);
   }

   public Iterable<Hotspot> hotspots() {
      return Collections.unmodifiableCollection(this.hotspots.values());
   }

   public int historySize() {
      return this.history.size();
   }

   public int hotspotCount() {
      return this.hotspots.size();
   }

   public long accepted() {
      return this.accepted;
   }

   public long merged() {
      return this.merged;
   }
}
