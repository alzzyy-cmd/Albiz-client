package net.albiz.client.module.impl.render;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.concurrent.CompletableFuture;
import net.albiz.client.config.ConfigMigration;
import net.albiz.client.esp.ChunkWorkQueue;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.BlockUpdateEvent;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.ActionSetting;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.util.Chat;
import net.albiz.client.suschunk.BlockEvidenceRules;
import net.albiz.client.suschunk.EvidenceBook;
import net.albiz.client.suschunk.EvidenceBook.Confidence;
import net.albiz.client.suschunk.EvidenceType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2248;
import net.minecraft.class_2350;
import net.minecraft.class_2393;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_7923;

/**
 * Scores chunks by several independent kinds of evidence of player activity and explains every
 * score. Works on the blocks the client actually received, across the dimension's whole build
 * range (including below Y 0). A score is a hint to investigate, never proof.
 */
public final class SusChunkModule extends ClientModule {
   private static final String TITLE = "SusChunk";
   private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());
   private static final EvidenceType[] LIVE = {EvidenceType.BLOCK_CHANGE, EvidenceType.SOUND};
   /** Marker for blocks that carry no evidence, so the path lookup happens once per block type. */
   private static final Object IRRELEVANT = new Object();

   private final NumberSetting threshold = this.addSetting("score-threshold", new NumberSetting("Score Threshold", 12.0F, 2.0F, 80.0F).step(1.0F));
   private final NumberSetting chunksPerTick = this.addSetting(
      "chunks-per-tick", new NumberSetting("Chunks Per Tick", PlatformInfo.isMobile() ? 1.0F : 2.0F, 1.0F, 12.0F).step(1.0F)
   );
   private final NumberSetting rescanSeconds = this.addSetting("rescan-seconds", new NumberSetting("Rescan Seconds", 30.0F, 5.0F, 300.0F).step(5.0F));
   private final NumberSetting deepslateMinY = this.addSetting("deepslate-min-y", new NumberSetting("Deepslate Min Y", 16.0F, -64.0F, 320.0F).step(1.0F));
   private final BooleanSetting liveChanges = this.addSetting("live-block-changes", new BooleanSetting("Count Live Block Changes", true));
   private final NumberSetting liveMinutes = this.addSetting("live-minutes", new NumberSetting("Live Evidence Minutes", 10.0F, 1.0F, 60.0F).step(1.0F));
   private final BooleanSetting notify = this.addSetting("notify", new BooleanSetting("Notify", true));
   private final NumberSetting cooldown = this.addSetting("alert-cooldown", new NumberSetting("Alert Cooldown Seconds", 120.0F, 5.0F, 900.0F).step(5.0F));
   private final BooleanSetting showLow = this.addSetting("show-low", new BooleanSetting("Show Low Confidence", false));
   private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
   private final ColorSetting highColor = this.addSetting("high-color", new ColorSetting("High Confidence Color", 0xFFE3262E));
   private final ColorSetting mediumColor = this.addSetting("medium-color", new ColorSetting("Medium Confidence Color", 0xFFFFB75C));
   private final ColorSetting lowColor = this.addSetting("low-color", new ColorSetting("Low Confidence Color", 0xFF8FA3B8));
   private final EnumMap<EvidenceType, NumberSetting> weights = new EnumMap<>(EvidenceType.class);
   private final ActionSetting explainNearest = this.addSetting("explain-nearest", new ActionSetting("Explain Nearest Chunk", this::explainNearest));
   private final ActionSetting export = this.addSetting("export", new ActionSetting("Export Results", this::exportResults));
   private final ActionSetting clear = this.addSetting("clear", new ActionSetting("Clear Results", this::clearResults));

   private final EvidenceBook book = new EvidenceBook(PlatformInfo.isMobile() ? 1024 : 4096);
   private final ChunkWorkQueue work = new ChunkWorkQueue(4096);
   private final IdentityHashMap<class_2248, Object> blockCache = new IdentityHashMap<>();
   private final int[] scratch = new int[EvidenceType.values().length];
   private Object world;
   private long nextRefillMs;
   private int ticks;

   public SusChunkModule() {
      super(
         "sus-chunk",
         TITLE,
         "Scores chunks from several kinds of evidence of player activity and explains each score. Hints, not proof.",
         ModuleCategory.BASE_RAID
      );

      for (EvidenceType type : EvidenceType.values()) {
         NumberSetting weight = this.addSetting(
            "weight-" + type.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-'),
            new NumberSetting(type.weightLabel(), type.defaultWeight(), 0.0F, 10.0F).step(0.25F)
         );
         this.weights.put(type, weight);
      }
   }

   @Override
   protected void onEnable() {
      this.reset();
   }

   @Override
   protected void onDisable() {
      this.reset();
      this.world = null;
   }

   private void reset() {
      this.book.clear();
      this.work.clear();
      this.nextRefillMs = 0L;
   }

   /** Chunks currently flagged (MEDIUM or HIGH), keyed like {@code ChunkPos.toLong}. Empty while disabled. */
   public LongSet flaggedChunks() {
      LongOpenHashSet out = new LongOpenHashSet();
      if (this.isEnabled()) {
         float limit = this.threshold.getInt();
         this.book.forEach((chunk, entry) -> {
            if (this.book.flagged(entry, limit)) {
               out.add(chunk);
            }
         });
      }

      return out;
   }

   /** Lets other diagnostics (e.g. Sound Debug) contribute a live observation. Client thread only. */
   public void reportSound(int chunkX, int chunkZ, int y) {
      if (this.isEnabled()) {
         this.book.addLive(ChunkWorkQueue.key(chunkX, chunkZ), EvidenceType.SOUND, y, System.currentTimeMillis());
      }
   }

   private void syncWeights() {
      for (EvidenceType type : EvidenceType.values()) {
         this.book.weight(type, this.weights.get(type).get());
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level != this.world) {
         this.world = level;
         this.reset();
      }

      if (level == null || mc.field_1724 == null) {
         return;
      }

      long now = System.currentTimeMillis();
      if (++this.ticks % 20 == 0) {
         this.syncWeights();
         this.book.expireLive(now, (long)this.liveMinutes.getInt() * 60000L, LIVE);
      }

      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      if (this.work.isEmpty() && now >= this.nextRefillMs) {
         this.work.offerSquare(cx, cz, (Integer)mc.field_1690.method_42503().method_41753() + 1);
         this.nextRefillMs = now + (long)this.rescanSeconds.getInt() * 1000L;
      }

      int budget = net.albiz.client.module.impl.client.PerformanceModule.chunksPerTick(this.chunksPerTick.getInt());
      float limit = this.threshold.getInt();
      while (budget > 0 && !this.work.isEmpty()) {
         long key = this.work.poll();
         int x = ChunkWorkQueue.x(key);
         int z = ChunkWorkQueue.z(key);
         class_2818 chunk = level.method_2935().method_21730(x, z);
         if (chunk == null) {
            // Not loaded: keep earlier evidence (cache) rather than treating missing data as "clean".
            continue;
         }

         budget--;
         this.scan(chunk, key, now);
         if (this.notify.get() && this.book.shouldAlert(key, limit, now, (long)this.cooldown.getInt() * 1000L)) {
            EvidenceBook.Entry entry = this.book.get(key);
            Chat.info(
               TITLE,
               this.book.confidence(entry, limit) + " · X " + (x << 4) + " Z " + (z << 4) + " · " + AlbizI18n.display("score") + " "
                  + Math.round(this.book.score(entry)) + " · " + this.localizedExplain(entry, 3)
            );
         }
      }
   }

   private Object lookup(class_2248 block) {
      Object cached = this.blockCache.get(block);
      if (cached == null) {
         EvidenceType type = BlockEvidenceRules.candidate(class_7923.field_41175.method_10221(block).method_12832());
         cached = type == null ? IRRELEVANT : type;
         this.blockCache.put(block, cached);
      }

      return cached;
   }

   private void scan(class_2818 chunk, long key, long now) {
      int[] counts = this.scratch;
      java.util.Arrays.fill(counts, 0);
      class_2826[] sections = chunk.method_12006();
      int bottomSection = chunk.method_32891();
      int minDeepslateY = this.deepslateMinY.getInt();
      int minY = Integer.MAX_VALUE;
      int maxY = Integer.MIN_VALUE;

      for (int i = 0; i < sections.length; i++) {
         class_2826 section = sections[i];
         if (section == null || section.method_38292() || !section.method_19523(state -> this.lookup(state.method_26204()) != IRRELEVANT)) {
            continue;
         }

         int baseY = (bottomSection + i) << 4;
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  class_2680 state = section.method_12254(x, y, z);
                  Object found = this.lookup(state.method_26204());
                  if (found == IRRELEVANT) {
                     continue;
                  }

                  EvidenceType type = (EvidenceType)found;
                  int blockY = baseY + y;
                  if (type == EvidenceType.GROWN_KELP || type == EvidenceType.GROWN_VINES) {
                     if (!state.method_28498(class_2393.field_22509) || (Integer)state.method_11654(class_2393.field_22509) != 25) {
                        continue;
                     }
                  } else if (type == EvidenceType.HIGH_DEEPSLATE) {
                     boolean rotated = state.method_28498(class_2741.field_12496) && state.method_11654(class_2741.field_12496) != class_2350.class_2351.field_11052;
                     if (rotated) {
                        type = EvidenceType.ROTATED_DEEPSLATE;
                     } else if (!BlockEvidenceRules.deepslateOutOfRange(blockY, minDeepslateY)) {
                        continue;
                     }
                  }

                  counts[type.ordinal()]++;
                  minY = Math.min(minY, blockY);
                  maxY = Math.max(maxY, blockY);
               }
            }
         }
      }

      this.book.setScan(key, counts, minY, maxY, now, LIVE);
   }

   @EventHandler
   public void onBlockUpdate(BlockUpdateEvent event) {
      if (!this.liveChanges.get() || event.pos == null || event.oldState == null || event.newState == null) {
         return;
      }

      // Only changes that add or remove an evidence block count; water flow and crop growth do not.
      boolean relevant = this.lookup(event.oldState.method_26204()) != IRRELEVANT || this.lookup(event.newState.method_26204()) != IRRELEVANT;
      if (relevant && event.oldState.method_26204() != event.newState.method_26204()) {
         this.book.addLive(
            ChunkWorkQueue.key(event.pos.method_10263() >> 4, event.pos.method_10260() >> 4), EvidenceType.BLOCK_CHANGE, event.pos.method_10264(), System.currentTimeMillis()
         );
      }
   }

   private String localizedExplain(EvidenceBook.Entry entry, int maxParts) {
      StringBuilder out = new StringBuilder();
      int parts = 0;
      for (EvidenceType type : EvidenceType.values()) {
         if (entry.count(type) <= 0) {
            continue;
         }

         if (parts++ >= maxParts) {
            out.append(" …");
            break;
         }

         if (out.length() > 0) {
            out.append(", ");
         }

         out.append(AlbizI18n.display(type.label())).append(" x").append(entry.count(type));
      }

      return out.toString();
   }

   private void explainNearest() {
      class_310 mc = class_310.method_1551();
      if (!this.isEnabled() || mc.field_1724 == null) {
         Chat.info(TITLE, AlbizI18n.display("Enable the module first."));
         return;
      }

      this.syncWeights();
      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      long[] best = {0L, Long.MAX_VALUE};
      this.book.forEach((chunk, entry) -> {
         long dx = ChunkWorkQueue.x(chunk) - cx;
         long dz = ChunkWorkQueue.z(chunk) - cz;
         long distance = dx * dx + dz * dz;
         if (distance < best[1]) {
            best[0] = chunk;
            best[1] = distance;
         }
      });
      if (best[1] == Long.MAX_VALUE) {
         Chat.info(TITLE, AlbizI18n.display("No evidence recorded yet."));
         return;
      }

      EvidenceBook.Entry entry = this.book.get(best[0]);
      float limit = this.threshold.getInt();
      Chat.info(
         TITLE,
         AlbizI18n.display("Chunk") + " X " + (ChunkWorkQueue.x(best[0]) << 4) + " Z " + (ChunkWorkQueue.z(best[0]) << 4) + " · " + AlbizI18n.display("score") + " "
            + Math.round(this.book.score(entry)) + "/" + Math.round(limit) + " · " + this.book.confidence(entry, limit)
            + (entry.hasY() ? " · Y " + entry.minY() + " .. " + entry.maxY() : "")
      );

      for (EvidenceType type : EvidenceType.values()) {
         if (entry.count(type) > 0) {
            Chat.info(
               TITLE,
               AlbizI18n.display(type.label()) + " x" + entry.count(type) + " (+" + Math.round(this.book.contribution(entry, type) * 10.0F) / 10.0F + ") — "
                  + AlbizI18n.display(type.caveat())
            );
         }
      }
   }

   private void exportResults() {
      this.syncWeights();
      StringBuilder out = new StringBuilder("# Albiz SusChunk export. Scores are hints from received block data, not proof of a base.\n");
      for (String line : this.book.export(this.threshold.getInt())) {
         out.append(line).append('\n');
      }

      String text = out.toString();
      Path file = ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("diagnostics").resolve("sus-chunk-" + STAMP.format(Instant.now()) + ".tsv");
      class_310 mc = class_310.method_1551();
      CompletableFuture.runAsync(() -> {
         String message;
         try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, text, StandardCharsets.UTF_8);
            message = AlbizI18n.display("Log exported") + ": " + file.getFileName();
         } catch (java.io.IOException | RuntimeException error) {
            message = AlbizI18n.display("Log export failed") + ": " + error.getClass().getSimpleName();
         }

         String result = message;
         mc.execute(() -> Chat.info(TITLE, result));
      });
   }

   private void clearResults() {
      this.reset();
      Chat.info(TITLE, AlbizI18n.display("Results cleared."));
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null || mc.field_1724 == null || this.book.size() == 0) {
         return;
      }

      Render3D render = new Render3D(event);
      float limit = this.threshold.getInt();
      boolean low = this.showLow.get();
      boolean lines = this.tracers.get();
      Color high = Color.fromPacked(this.highColor.argb());
      Color medium = Color.fromPacked(this.mediumColor.argb());
      Color faint = Color.fromPacked(this.lowColor.argb());
      double fallbackY = mc.field_1724.method_23318();
      this.book.forEach((chunk, entry) -> {
         Confidence confidence = this.book.confidence(entry, limit);
         if (confidence == Confidence.NONE || confidence == Confidence.LOW && !low) {
            return;
         }

         Color color = confidence == Confidence.HIGH ? high : confidence == Confidence.MEDIUM ? medium : faint;
         double x = ChunkWorkQueue.x(chunk) << 4;
         double z = ChunkWorkQueue.z(chunk) << 4;
         double bottom = entry.hasY() ? entry.minY() : fallbackY;
         double top = entry.hasY() ? entry.maxY() + 1.0 : fallbackY + 1.0;
         render.box(x, bottom, z, x + 16.0, top, z + 16.0, color.withAlpha(22), color, ShapeMode.Both);
         if (lines && confidence != Confidence.LOW) {
            render.tracer(x + 8.0, (bottom + top) * 0.5, z + 8.0, color);
         }
      });
   }
}
