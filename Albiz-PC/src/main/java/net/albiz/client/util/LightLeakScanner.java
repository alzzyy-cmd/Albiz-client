package net.albiz.client.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.albiz.client.module.impl.client.PerformanceModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3562;
import net.minecraft.class_4076;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public final class LightLeakScanner {
   private static final int MIN_INSIDE = 2;
   private static final int DARK_GAP = 3;
   private static final long BUDGET_NANOS = 1500000L;
   private static final int NOISY_CHUNK = 512;
   private final LongLinkedOpenHashSet queue = new LongLinkedOpenHashSet();

   public void clear() {
      this.queue.clear();
   }

   public void queueLoaded(class_638 level, class_2338 around, int radius) {
      int px = around.method_10263() >> 4;
      int pz = around.method_10260() >> 4;
      LongArrayList found = new LongArrayList();

      for (int ring = 0; ring <= radius; ring++) {
         for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == ring && LevelUtil.isLoaded(level, px + dx, pz + dz)) {
                  found.add(class_1923.method_8331(px + dx, pz + dz));
               }
            }
         }
      }

      for (int i = 0; i < found.size(); i++) {
         this.queue.add(found.getLong(i));
      }
   }

   public void onPacketApplied(class_2596<?> packet) {
      if (packet instanceof class_2672 chunk) {
         this.queue.add(class_1923.method_8331(chunk.method_11523(), chunk.method_11524()));
      } else if (packet instanceof class_2676 light) {
         this.queue.add(class_1923.method_8331(light.method_11558(), light.method_11554()));
      }
   }

   public void tick(class_638 level) {
      if (!this.queue.isEmpty()) {
         boolean optimized = PerformanceModule.optimizationsEnabled();
         long started = System.nanoTime();
         long deadline = optimized ? PerformanceBudget.acquire(1500000L) : started + 1500000L;

         while (!this.queue.isEmpty() && System.nanoTime() < deadline) {
            long key = this.queue.removeFirstLong();
            scan(level, class_1923.method_8325(key), class_1923.method_8332(key));
         }

         if (optimized) {
            PerformanceBudget.record(started);
         }
      }
   }

   private static void scan(class_638 level, int chunkX, int chunkZ) {
      class_2818 chunk = level.method_2935().method_2857(chunkX, chunkZ, class_2806.field_12803, false);
      if (chunk != null) {
         class_2826[] sections = chunk.method_12006();
         class_3562 light = level.method_22336().method_15562(class_1944.field_9282);
         LongArrayList found = new LongArrayList();

         for (int i = 0; i < sections.length; i++) {
            class_2826 section = sections[i];
            if (!section.method_38292()) {
               int sectionY = chunk.method_31604(i);
               class_2804 data = light.method_15544(class_4076.method_18676(chunkX, sectionY, chunkZ));
               if (data != null && !data.method_12146() && anyLight(data)) {
                  scanSection(level, section, data, chunkX << 4, sectionY << 4, chunkZ << 4, found);
                  if (found.size() > 512) {
                     return;
                  }
               }
            }
         }

         for (int ix = 0; ix < found.size(); ix++) {
            BlockEntityLeakTracker.hint(class_2338.method_10092(found.getLong(ix)), class_2246.field_10124, BlockEntityLeakTracker.Source.LIGHT);
         }
      }
   }

   private static void scanSection(class_638 level, class_2826 section, class_2804 data, int baseX, int baseY, int baseZ, LongArrayList found) {
      for (int y = 0; y < 16; y++) {
         for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
               class_2680 state = section.method_12254(x, y, z);
               int inside = data.method_12139(x, y, z);
               if (state.method_26193() >= 15) {
                  if (inside >= 2 && inside > state.method_26213()) {
                     found.add(class_2338.method_10064(baseX + x, baseY + y, baseZ + z));
                  }
               } else if (state.method_26215() && inside <= 12 && brightestInside(data, x, y, z) - inside >= 3) {
                  class_2338 pos = new class_2338(baseX + x, baseY + y, baseZ + z);
                  if (!nextToGeode(level, pos)) {
                     found.add(pos.method_10063());
                  }
               }
            }
         }
      }
   }

   private static int brightestInside(class_2804 data, int x, int y, int z) {
      int best = 0;
      if (x > 0) {
         best = Math.max(best, data.method_12139(x - 1, y, z));
      }

      if (x < 15) {
         best = Math.max(best, data.method_12139(x + 1, y, z));
      }

      if (y > 0) {
         best = Math.max(best, data.method_12139(x, y - 1, z));
      }

      if (y < 15) {
         best = Math.max(best, data.method_12139(x, y + 1, z));
      }

      if (z > 0) {
         best = Math.max(best, data.method_12139(x, y, z - 1));
      }

      if (z < 15) {
         best = Math.max(best, data.method_12139(x, y, z + 1));
      }

      return best;
   }

   private static boolean anyLight(class_2804 data) {
      byte[] raw = data.method_12137();

      for (byte value : raw) {
         if (value != 0) {
            return true;
         }
      }

      return false;
   }

   private static boolean nextToGeode(class_638 level, class_2338 pos) {
      for (class_2338 next : new class_2338[]{
         pos.method_10084(), pos.method_10074(), pos.method_10095(), pos.method_10072(), pos.method_10078(), pos.method_10067()
      }) {
         class_2680 state = level.method_8320(next);
         if (state.method_27852(class_2246.field_27160) || state.method_27852(class_2246.field_27159)) {
            return true;
         }
      }

      return false;
   }
}
