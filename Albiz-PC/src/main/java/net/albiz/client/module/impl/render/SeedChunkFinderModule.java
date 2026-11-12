// Detection rule adapted from SoberClient (dev.sober.modules.SeedChunkFinder), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.module.impl.render;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.albiz.client.esp.ChunkWorkQueue;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.module.setting.StringSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.util.Chat;
import net.albiz.client.util.seedscan.DeepslateGradient;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_6574;
import net.minecraft.class_6677;

/**
 * Flags chunks containing deepslate at Y 1..7 where the vanilla generator, for the given seed,
 * would have placed stone. Only meaningful in the overworld on vanilla terrain generation with the
 * correct seed; with a wrong seed or custom generation nearly every chunk is flagged.
 */
public final class SeedChunkFinderModule extends ClientModule {
   private static final String TITLE = "Seed Chunk Finder";

   private final StringSetting seed = this.addSetting("server-seed", new StringSetting("Server Seed", ""));
   private final NumberSetting minBlocks = this.addSetting("min-blocks", new NumberSetting("Minimum Mismatched Blocks", 1.0F, 1.0F, 32.0F).step(1.0F));
   private final NumberSetting chunksPerTick = this.addSetting(
      "chunks-per-tick", new NumberSetting("Chunks Per Tick", PlatformInfo.isMobile() ? 1.0F : 3.0F, 1.0F, 16.0F).step(1.0F)
   );
   private final BooleanSetting notify = this.addSetting("notify", new BooleanSetting("Notify", true));
   private final ColorSetting color = this.addSetting("color", new ColorSetting("Color", 0x9600FF00));
   private final NumberSetting renderY = this.addSetting("render-y", new NumberSetting("Render Y", 63.0F, -64.0F, 320.0F).step(1.0F));

   private final int chunkLimit = PlatformInfo.isMobile() ? 512 : 2048;
   /** Chunk key to mismatch count (0 = scanned, clean). Insertion ordered, bounded. */
   private final LinkedHashMap<Long, Integer> scanned = new LinkedHashMap<>();
   private final ChunkWorkQueue work = new ChunkWorkQueue(4096);
   private class_6574 deepslateRandom;
   private String activeSeed;
   private Object world;
   private boolean warnedDimension;

   public SeedChunkFinderModule() {
      super("seed-chunk-finder", TITLE, "Finds player placed deepslate using the server seed.", ModuleCategory.BASE_RAID);
   }

   @Override
   protected void onEnable() {
      this.reset();
      this.activeSeed = null;
      this.deepslateRandom = null;
      this.warnedDimension = false;
      if (DeepslateGradient.parseSeed(this.seed.get()) == null) {
         Chat.info(TITLE, AlbizI18n.display("Set a valid numeric server seed first."));
      }
   }

   @Override
   protected void onDisable() {
      this.reset();
      this.deepslateRandom = null;
      this.world = null;
   }

   private void reset() {
      this.scanned.clear();
      this.work.clear();
   }

   /** Rebuilds the positional random when the seed text changes; returns false while the seed is invalid. */
   private boolean prepareSeed() {
      String text = this.seed.get();
      if (text != null && text.equals(this.activeSeed)) {
         return this.deepslateRandom != null;
      }

      this.activeSeed = text;
      this.reset();
      Long parsed = DeepslateGradient.parseSeed(text);
      this.deepslateRandom = parsed == null ? null : new class_6677(parsed).method_38421().method_38995("minecraft:deepslate").method_38421();
      return this.deepslateRandom != null;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level != this.world) {
         this.world = level;
         this.reset();
         this.warnedDimension = false;
      }

      if (level == null || mc.field_1724 == null || !this.prepareSeed()) {
         return;
      }

      if (level.method_27983() != class_1937.field_25179) {
         if (!this.warnedDimension) {
            this.warnedDimension = true;
            Chat.info(TITLE, AlbizI18n.display("Only works in the overworld."));
         }

         return;
      }

      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      int radius = (Integer)mc.field_1690.method_42503().method_41753() + 1;
      if (this.work.isEmpty()) {
         for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
               if (!this.scanned.containsKey(ChunkWorkQueue.key(x, z))) {
                  this.work.offer(x, z);
               }
            }
         }
      }

      int budget = net.albiz.client.module.impl.client.PerformanceModule.chunksPerTick(this.chunksPerTick.getInt());
      while (budget > 0 && !this.work.isEmpty()) {
         long key = this.work.poll();
         int x = ChunkWorkQueue.x(key);
         int z = ChunkWorkQueue.z(key);
         class_2818 chunk = level.method_2935().method_21730(x, z);
         if (chunk == null) {
            continue;
         }

         budget--;
         int mismatches = this.count(chunk);
         if (this.scanned.size() >= this.chunkLimit) {
            Iterator<Long> eldest = this.scanned.keySet().iterator();
            eldest.next();
            eldest.remove();
         }

         this.scanned.put(key, mismatches);
         if (mismatches >= this.minBlocks.getInt() && this.notify.get()) {
            Chat.info(
               TITLE,
               AlbizI18n.display("Suspicious chunk at") + " X " + (x << 4) + " Z " + (z << 4) + " · " + mismatches + " " + AlbizI18n.display("mismatched blocks")
            );
         }
      }
   }

   /** Counts deepslate blocks in the Y 1..7 band that the seed says should be stone (capped at 32). */
   private int count(class_2818 chunk) {
      int index = chunk.method_31602(1);
      class_2826[] sections = chunk.method_12006();
      if (index < 0 || index >= sections.length) {
         return 0;
      }

      class_2826 section = sections[index];
      if (section == null || section.method_38292()) {
         return 0;
      }

      int baseX = chunk.method_12004().method_8326();
      int baseZ = chunk.method_12004().method_8328();
      int found = 0;

      for (int y = 1; y < DeepslateGradient.NEVER_AT_OR_ABOVE; y++) {
         for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
               if (section.method_12254(x, y, z).method_27852(class_2246.field_28888)
                  && !DeepslateGradient.expectsDeepslate(y, this.deepslateRandom.method_38418(baseX + x, y, baseZ + z).method_43057())
                  && ++found >= 32) {
                  return found;
               }
            }
         }
      }

      return found;
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      class_638 level = class_310.method_1551().field_1687;
      if (level == null || this.scanned.isEmpty()) {
         return;
      }

      Render3D render = new Render3D(event);
      Color fill = Color.fromPacked(this.color.argb());
      Color edge = fill.withAlpha(255);
      double y = this.renderY.getInt();
      int threshold = this.minBlocks.getInt();

      for (Map.Entry<Long, Integer> entry : this.scanned.entrySet()) {
         if (entry.getValue() < threshold) {
            continue;
         }

         int x = ChunkWorkQueue.x(entry.getKey()) << 4;
         int z = ChunkWorkQueue.z(entry.getKey()) << 4;
         render.box(x, y, z, x + 16, y + 0.1, z + 16, fill, edge, ShapeMode.Both);
      }
   }
}
