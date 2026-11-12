package net.albiz.client.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.BitSet;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2804;
import net.minecraft.class_3562;
import net.minecraft.class_3568;
import net.minecraft.class_4076;
import net.minecraft.class_638;
import net.minecraft.class_6606;

@Environment(EnvType.CLIENT)
public final class ServerBlockLight {
   public static final int UNKNOWN = -1;
   public static final int TOP_SECTION = 3;
   public static final int TOP_Y = 63;
   private static final byte[] DARK = new byte[0];
   private final Long2ObjectOpenHashMap<ServerBlockLight.Column> columns = new Long2ObjectOpenHashMap();

   public void onChunk(class_638 level, int chunkX, int chunkZ, class_6606 data) {
      this.columns.put(class_1923.method_8331(chunkX, chunkZ), apply(level, null, data));
   }

   public void onLight(class_638 level, int chunkX, int chunkZ, class_6606 data) {
      long key = class_1923.method_8331(chunkX, chunkZ);
      this.columns.put(key, apply(level, (ServerBlockLight.Column)this.columns.get(key), data));
   }

   public void onBlockChanged(class_2338 pos) {
      if (pos.method_10264() <= 63) {
         ServerBlockLight.Column column = (ServerBlockLight.Column)this.columns.get(class_1923.method_8331(pos.method_10263() >> 4, pos.method_10260() >> 4));
         if (column != null) {
            if (column.touched == null) {
               column.touched = new LongOpenHashSet();
            }

            column.touched.add(pos.method_10063());
         }
      }
   }

   public ServerBlockLight.Column column(class_638 level, int chunkX, int chunkZ) {
      long key = class_1923.method_8331(chunkX, chunkZ);
      ServerBlockLight.Column column = (ServerBlockLight.Column)this.columns.get(key);
      if (column == null && LevelUtil.isLoaded(level, chunkX, chunkZ)) {
         column = fromClient(level, chunkX, chunkZ);
         this.columns.put(key, column);
      }

      return column;
   }

   public void forgetUnloaded(class_638 level) {
      this.columns.keySet().removeIf(key -> !LevelUtil.isLoaded(level, class_1923.method_8325(key), class_1923.method_8332(key)));
   }

   public void clear() {
      this.columns.clear();
   }

   private static ServerBlockLight.Column apply(class_638 level, ServerBlockLight.Column previous, class_6606 data) {
      class_3568 engine = level.method_22336();
      int minSection = engine.method_31929();
      int count = engine.method_31928();
      byte[][] sections = previous != null && previous.minSection == minSection && previous.sections.length == count
         ? (byte[][])previous.sections.clone()
         : new byte[count][];
      BitSet sent = data.method_38608();
      BitSet cleared = data.method_38609();
      Iterator<byte[]> updates = data.method_38610().iterator();

      for (int i = 0; i < count; i++) {
         boolean kept = minSection + i <= 3;
         if (sent.get(i)) {
            byte[] bytes = updates.hasNext() ? updates.next() : null;
            sections[i] = kept && bytes != null && bytes.length == 2048 ? (byte[])bytes.clone() : null;
         } else if (cleared.get(i)) {
            sections[i] = kept ? DARK : null;
         }
      }

      return new ServerBlockLight.Column(minSection, sections);
   }

   private static ServerBlockLight.Column fromClient(class_638 level, int chunkX, int chunkZ) {
      class_3568 engine = level.method_22336();
      class_3562 listener = engine.method_15562(class_1944.field_9282);
      int minSection = engine.method_31929();
      byte[][] sections = new byte[engine.method_31928()][];

      for (int i = 0; i < sections.length && minSection + i <= 3; i++) {
         class_2804 layer = listener.method_15544(class_4076.method_18676(chunkX, minSection + i, chunkZ));
         if (layer != null) {
            sections[i] = layer.method_12146() ? DARK : (byte[])layer.method_12137().clone();
         }
      }

      return new ServerBlockLight.Column(minSection, sections);
   }

   @Environment(EnvType.CLIENT)
   public static final class Column {
      private final int minSection;
      private final byte[][] sections;
      private LongOpenHashSet touched;

      private Column(int minSection, byte[][] sections) {
         this.minSection = minSection;
         this.sections = sections;
      }

      public int get(int x, int y, int z) {
         int index = (y >> 4) - this.minSection;
         if (index >= 0 && index < this.sections.length) {
            byte[] data = this.sections[index];
            if (data == null) {
               return -1;
            } else if (this.touched != null && this.touched.contains(class_2338.method_10064(x, y, z))) {
               return -1;
            } else if (data == ServerBlockLight.DARK) {
               return 0;
            } else {
               int cell = (y & 15) << 8 | (z & 15) << 4 | x & 15;
               return data[cell >> 1] >> ((cell & 1) << 2) & 15;
            }
         } else {
            return -1;
         }
      }
   }
}
