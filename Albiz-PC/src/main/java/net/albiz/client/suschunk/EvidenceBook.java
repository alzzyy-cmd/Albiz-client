package net.albiz.client.suschunk;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded per-chunk evidence store with weighted scoring.
 *
 * <p>Scan results are <em>set</em>, not added, so rescanning a chunk never inflates its score
 * (deduplication). Live evidence (block changes, sounds) is <em>added</em> and capped per type.
 * Not thread-safe: client thread only.
 */
public final class EvidenceBook {
   public enum Confidence {
      NONE,
      LOW,
      MEDIUM,
      HIGH
   }

   public static final class Entry {
      private final int[] counts = new int[EvidenceType.values().length];
      private int minY = Integer.MAX_VALUE;
      private int maxY = Integer.MIN_VALUE;
      private long lastAlertMs = Long.MIN_VALUE;
      private long updatedMs;
      private boolean alerted;

      public int count(EvidenceType type) {
         return this.counts[type.ordinal()];
      }

      public boolean hasY() {
         return this.minY <= this.maxY;
      }

      public int minY() {
         return this.minY;
      }

      public int maxY() {
         return this.maxY;
      }

      public long updatedMs() {
         return this.updatedMs;
      }

      /** Number of different evidence types present. */
      public int kinds() {
         int kinds = 0;
         for (int count : this.counts) {
            if (count > 0) {
               kinds++;
            }
         }
         return kinds;
      }

      private boolean empty() {
         return this.kinds() == 0;
      }

      private void y(int low, int high) {
         if (low <= high) {
            this.minY = Math.min(this.minY, low);
            this.maxY = Math.max(this.maxY, high);
         }
      }
   }

   private final int limit;
   private final LinkedHashMap<Long, Entry> entries = new LinkedHashMap<>(256, 0.75F, true);
   private final EnumMap<EvidenceType, Float> weights = new EnumMap<>(EvidenceType.class);

   public EvidenceBook(int limit) {
      if (limit < 1) {
         throw new IllegalArgumentException("limit must be positive");
      }
      this.limit = limit;
      for (EvidenceType type : EvidenceType.values()) {
         this.weights.put(type, type.defaultWeight());
      }
   }

   public void weight(EvidenceType type, float weight) {
      this.weights.put(type, Math.max(0.0F, weight));
   }

   public float weight(EvidenceType type) {
      return this.weights.get(type);
   }

   private Entry entry(long chunk, boolean create) {
      Entry entry = this.entries.get(chunk);
      if (entry == null && create) {
         if (this.entries.size() >= this.limit) {
            Iterator<Long> eldest = this.entries.keySet().iterator();
            eldest.next();
            eldest.remove();
         }
         entry = new Entry();
         this.entries.put(chunk, entry);
      }
      return entry;
   }

   /**
    * Replaces the scan-derived counts of a chunk. {@code counts} is indexed by
    * {@link EvidenceType#ordinal()}; types listed in {@code live} keep their accumulated value.
    */
   public void setScan(long chunk, int[] counts, int minY, int maxY, long nowMs, EvidenceType... live) {
      boolean any = false;
      for (int count : counts) {
         any |= count > 0;
      }
      Entry entry = this.entry(chunk, any);
      if (entry == null) {
         return;
      }
      outer:
      for (EvidenceType type : EvidenceType.values()) {
         for (EvidenceType keep : live) {
            if (keep == type) {
               continue outer;
            }
         }
         entry.counts[type.ordinal()] = Math.max(0, counts[type.ordinal()]);
      }
      if (any) {
         entry.y(minY, maxY);
      }
      entry.updatedMs = nowMs;
      if (entry.empty()) {
         this.entries.remove(chunk);
      }
   }

   /** Adds one live observation, saturating at twice the type's scoring cap. */
   public void addLive(long chunk, EvidenceType type, int y, long nowMs) {
      Entry entry = this.entry(chunk, true);
      int index = type.ordinal();
      if (entry.counts[index] < type.cap() * 2) {
         entry.counts[index]++;
      }
      entry.y(y, y);
      entry.updatedMs = nowMs;
   }

   public Entry get(long chunk) {
      return this.entries.get(chunk);
   }

   public float score(Entry entry) {
      if (entry == null) {
         return 0.0F;
      }
      float score = 0.0F;
      for (EvidenceType type : EvidenceType.values()) {
         score += this.weights.get(type) * (float)Math.min(entry.counts[type.ordinal()], type.cap());
      }
      return score;
   }

   public float score(long chunk) {
      return this.score(this.entries.get(chunk));
   }

   /**
    * Confidence needs both score and independent evidence types: a single type can reach at most
    * MEDIUM, however many times it occurs.
    */
   public Confidence confidence(Entry entry, float threshold) {
      float score = this.score(entry);
      if (entry == null || score <= 0.0F || threshold <= 0.0F) {
         return Confidence.NONE;
      }
      int kinds = 0;
      for (EvidenceType type : EvidenceType.values()) {
         if (entry.counts[type.ordinal()] > 0 && this.weights.get(type) > 0.0F) {
            kinds++;
         }
      }
      if (score >= threshold * 2.0F && kinds >= 3) {
         return Confidence.HIGH;
      }
      if (score >= threshold && kinds >= 2 || score >= threshold * 2.0F) {
         return Confidence.MEDIUM;
      }
      return score >= threshold * 0.5F ? Confidence.LOW : Confidence.NONE;
   }

   public boolean flagged(Entry entry, float threshold) {
      Confidence confidence = this.confidence(entry, threshold);
      return confidence == Confidence.MEDIUM || confidence == Confidence.HIGH;
   }

   /**
    * @return true at most once per cooldown for a flagged chunk, and only when it was not already
    *     announced with the same evidence (so a rescan does not repeat the alert).
    */
   public boolean shouldAlert(long chunk, float threshold, long nowMs, long cooldownMs) {
      Entry entry = this.entries.get(chunk);
      if (!this.flagged(entry, threshold)) {
         if (entry != null) {
            entry.alerted = false;
         }
         return false;
      }
      if (entry.alerted || entry.lastAlertMs != Long.MIN_VALUE && nowMs - entry.lastAlertMs < cooldownMs) {
         return false;
      }
      entry.alerted = true;
      entry.lastAlertMs = nowMs;
      return true;
   }

   /** Strongest contributions first, e.g. {@code "Storage blocks x3 (+9.0), Fully grown kelp x12 (+12.0)"}. */
   public String explain(Entry entry, int maxParts) {
      if (entry == null) {
         return "";
      }
      List<EvidenceType> present = new ArrayList<>();
      for (EvidenceType type : EvidenceType.values()) {
         if (entry.counts[type.ordinal()] > 0) {
            present.add(type);
         }
      }
      present.sort((a, b) -> Float.compare(this.contribution(entry, b), this.contribution(entry, a)));
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < present.size() && i < maxParts; i++) {
         EvidenceType type = present.get(i);
         if (out.length() > 0) {
            out.append(", ");
         }
         out.append(type.label()).append(" x").append(entry.counts[type.ordinal()]).append(" (+").append(format(this.contribution(entry, type))).append(')');
      }
      if (present.size() > maxParts) {
         out.append(", +").append(present.size() - maxParts);
      }
      return out.toString();
   }

   public float contribution(Entry entry, EvidenceType type) {
      return this.weights.get(type) * (float)Math.min(entry.counts[type.ordinal()], type.cap());
   }

   private static String format(float value) {
      return Float.toString(Math.round(value * 10.0F) / 10.0F);
   }

   public interface Visitor {
      void visit(long chunk, Entry entry);
   }

   public void forEach(Visitor visitor) {
      for (Map.Entry<Long, Entry> entry : this.entries.entrySet()) {
         visitor.visit(entry.getKey(), entry.getValue());
      }
   }

   public void remove(long chunk) {
      this.entries.remove(chunk);
   }

   /** Drops live evidence older than {@code maxAgeMs}; scan evidence is refreshed by rescans instead. */
   public void expireLive(long nowMs, long maxAgeMs, EvidenceType... live) {
      Iterator<Entry> it = this.entries.values().iterator();
      while (it.hasNext()) {
         Entry entry = it.next();
         if (nowMs - entry.updatedMs < maxAgeMs) {
            continue;
         }
         for (EvidenceType type : live) {
            entry.counts[type.ordinal()] = 0;
         }
         if (entry.empty()) {
            it.remove();
         }
      }
   }

   public int size() {
      return this.entries.size();
   }

   public void clear() {
      this.entries.clear();
   }

   /** Tab-separated export, one line per chunk with evidence, header first. */
   public List<String> export(float threshold) {
      List<String> lines = new ArrayList<>(this.entries.size() + 1);
      StringBuilder header = new StringBuilder("chunk_x\tchunk_z\tblock_x\tblock_z\tscore\tconfidence\tmin_y\tmax_y");
      for (EvidenceType type : EvidenceType.values()) {
         header.append('\t').append(type.name().toLowerCase(java.util.Locale.ROOT));
      }
      lines.add(header.toString());
      this.forEach((chunk, entry) -> {
         int x = (int)chunk;
         int z = (int)(chunk >>> 32);
         StringBuilder line = new StringBuilder();
         line.append(x).append('\t').append(z).append('\t').append(x << 4).append('\t').append(z << 4).append('\t').append(format(this.score(entry)))
            .append('\t').append(this.confidence(entry, threshold)).append('\t').append(entry.hasY() ? Integer.toString(entry.minY) : "").append('\t')
            .append(entry.hasY() ? Integer.toString(entry.maxY) : "");
         for (EvidenceType type : EvidenceType.values()) {
            line.append('\t').append(entry.counts[type.ordinal()]);
         }
         lines.add(line.toString());
      });
      return lines;
   }
}
