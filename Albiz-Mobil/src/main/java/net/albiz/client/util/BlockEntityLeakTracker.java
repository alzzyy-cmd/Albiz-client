package net.albiz.client.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import net.albiz.client.module.impl.client.PerformanceModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2343;
import net.minecraft.class_2487;
import net.minecraft.class_2591;
import net.minecraft.class_2596;
import net.minecraft.class_2622;
import net.minecraft.class_2623;
import net.minecraft.class_2626;
import net.minecraft.class_2672;
import net.minecraft.class_2673;
import net.minecraft.class_2678;
import net.minecraft.class_2680;
import net.minecraft.class_2724;
import net.minecraft.class_2767;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_6603;
import net.minecraft.class_6880;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
public final class BlockEntityLeakTracker {
   private static final double ON_EDGE = 0.07;
   private static final Map<class_2338, BlockEntityLeakTracker.Entry> FOUND = new ConcurrentHashMap<>();
   private static final ConcurrentLinkedQueue<class_2338> ADDED = new ConcurrentLinkedQueue<>();
   private static final AtomicInteger GENERATION = new AtomicInteger();
   private static final AtomicIntegerArray COUNTS = new AtomicIntegerArray(BlockEntityLeakTracker.Source.values().length);
   private static volatile boolean recordAdditions;

   private BlockEntityLeakTracker() {
   }

   public static Map<class_2338, BlockEntityLeakTracker.Entry> view() {
      return FOUND;
   }

   public static boolean contains(class_2338 pos) {
      return FOUND.containsKey(pos);
   }

   public static boolean containsReal(class_2338 pos) {
      BlockEntityLeakTracker.Entry entry = FOUND.get(pos);
      return entry != null && entry.real();
   }

   public static int count(BlockEntityLeakTracker.Source source) {
      return COUNTS.get(source.ordinal());
   }

   public static void clear() {
      FOUND.clear();
      ADDED.clear();

      for (int i = 0; i < COUNTS.length(); i++) {
         COUNTS.set(i, 0);
      }

      GENERATION.incrementAndGet();
   }

   public static int generation() {
      return GENERATION.get();
   }

   public static void recordAdditions(boolean on) {
      recordAdditions = on;
      if (!on) {
         ADDED.clear();
      }
   }

   public static class_2338 pollAdded() {
      return ADDED.poll();
   }

   public static void ingest(class_2596<?> packet) {
      if (packet instanceof class_2672 chunkPacket) {
         class_6603 data = chunkPacket.method_38598();

         try {
            data.method_38587(chunkPacket.method_11523(), chunkPacket.method_11524()).accept((pos, type, tag) -> {
               if (type != null) {
                  put(pos, type, tag, BlockEntityLeakTracker.Source.CHUNK);
               }
            });
         } catch (Throwable var9) {
         }
      } else if (packet instanceof class_2622 bePacket) {
         put(bePacket.method_11293(), bePacket.method_11291(), bePacket.method_11290(), BlockEntityLeakTracker.Source.DATA);
      } else if (packet instanceof class_2623 event) {
         if (event.method_11295() instanceof class_2343) {
            hint(event.method_11298(), event.method_11295(), BlockEntityLeakTracker.Source.EVENT);
         }
      } else if (packet instanceof class_2673 eventx) {
         class_2248 block = eventBlock(eventx.method_11532());
         if (block != null && !eventx.method_11533()) {
            hint(eventx.method_11531(), block, BlockEntityLeakTracker.Source.EVENT);
         }
      } else if (packet instanceof class_2767 sound) {
         class_2248 block = soundBlock(sound.method_11894());
         if (block != null) {
            hintAt(sound.method_11890(), sound.method_11889(), sound.method_11893(), block);
         }
      } else if (packet instanceof class_2626 update) {
         dropRevealedHint(update.method_11309(), update.method_11308());
      } else if (packet instanceof class_2678 || packet instanceof class_2724) {
         clear();
      }
   }

   public static void hint(class_2338 pos, class_2248 block, BlockEntityLeakTracker.Source source) {
      class_2338 key=pos.method_10062();
      Entry next=new Entry(block,null,source);
      Entry old=FOUND.putIfAbsent(key,next);
      boolean accepted=old==null;
      if(!accepted && !old.real() && old.block()==class_2246.field_10124 && block!=class_2246.field_10124) accepted=FOUND.replace(key,old,next);
      // An inferred hint must never race with and overwrite a real received packet.
      if(accepted) {
         COUNTS.incrementAndGet(source.ordinal());
         if(recordAdditions)ADDED.add(key);
      }
   }

   private static void put(class_2338 pos, class_2591<?> type, class_2487 tag, BlockEntityLeakTracker.Source source) {
      class_2338 key = pos.method_10062();
      class_2487 data = tag != null && !tag.method_33133() ? tag : null;
      // Every fresh network observation replaces the immutable token, even in optimized mode.
      // Consumers can then distinguish a new observation from a retired cached record.
      Entry next = new Entry(blockFromType(type),data,source);
      Entry old = FOUND.put(key,next);
      if (old == null || !old.real()) COUNTS.incrementAndGet(source.ordinal());
      if (recordAdditions) ADDED.add(key);
   }

   private static void dropRevealedHint(class_2338 pos, class_2680 state) {
      BlockEntityLeakTracker.Entry old = FOUND.get(pos);
      if (old != null && !old.real()) {
         if (!state.method_26215()
            && !state.method_27852(class_2246.field_10340)
            && !state.method_27852(class_2246.field_28888)
            && !(state.method_26204() instanceof class_2343)) {
            if (FOUND.remove(pos, old)) {
               GENERATION.incrementAndGet();
            }
         }
      }
   }

   private static void hintAt(double x, double y, double z, class_2248 block) {
      int bx = (int)Math.floor(x);
      int by = (int)Math.floor(y);
      int bz = (int)Math.floor(z);
      double fx = x - bx;
      double fy = y - by;
      double fz = z - bz;
      if (!(Math.abs(fy - 0.5) > 0.07)) {
         boolean seamX = fx < 0.07;
         boolean seamZ = fz < 0.07;
         if ((seamX || !(Math.abs(fx - 0.5) > 0.07)) && (seamZ || !(Math.abs(fz - 0.5) > 0.07)) && (!seamX || !seamZ)) {
            hint(new class_2338(bx, by, bz), block, BlockEntityLeakTracker.Source.SOUND);
            if (seamX) {
               hint(new class_2338(bx - 1, by, bz), block, BlockEntityLeakTracker.Source.SOUND);
            }

            if (seamZ) {
               hint(new class_2338(bx, by, bz - 1), block, BlockEntityLeakTracker.Source.SOUND);
            }
         }
      }
   }

   private static class_2248 eventBlock(int type) {
      return switch (type) {
         case 1000, 1001, 1002, 2000 -> class_2246.field_10200;
         case 1035 -> class_2246.field_10333;
         case 1049, 1050, 2010 -> class_2246.field_46797;
         default -> null;
      };
   }

   private static class_2248 soundBlock(class_6880<class_3414> sound) {
      class_2960 id = ((class_3414)sound.comp_349()).comp_3319();
      if (!id.method_12836().equals("minecraft")) {
         return null;
      } else {
         String path = id.method_12832();
         if (!path.startsWith("block.")) {
            return null;
         } else {
            String name = path.substring("block.".length());
            int dot = name.indexOf(46);
            if (dot < 0) {
               return null;
            } else {
               String kind = name.substring(0, dot);

               return switch (kind) {
                  case "chest" -> class_2246.field_10034;
                  case "ender_chest" -> class_2246.field_10443;
                  case "barrel" -> class_2246.field_16328;
                  case "shulker_box" -> class_2246.field_10603;
                  case "beacon" -> class_2246.field_10327;
                  case "vault" -> class_2246.field_48851;
                  case "trial_spawner" -> class_2246.field_47336;
                  case "chiseled_bookshelf" -> class_2246.field_40276;
                  case "copper_chest" -> (class_2248)class_7923.field_41175.method_63535(class_2960.method_60656("copper_chest"));
                  default -> null;
               };
            }
         }
      }
   }

   public static class_2248 blockFromType(class_2591<?> type) {
      class_2960 id = class_7923.field_41181.method_10221(type);
      if (id == null) {
         return class_2246.field_10124;
      } else {
         String path = id.method_12832();

         class_2248 mapped = switch (path) {
            case "mob_spawner" -> class_2246.field_10260;
            case "piston" -> class_2246.field_10008;
            case "sign" -> class_2246.field_10121;
            case "hanging_sign" -> class_2246.field_40262;
            case "skull" -> class_2246.field_10481;
            case "banner" -> class_2246.field_10154;
            case "bed" -> class_2246.field_10069;
            case "brushable_block" -> class_2246.field_42728;
            default -> null;
         };
         return mapped != null ? mapped : class_7923.field_41175.method_10223(id).<class_2248>map(class_6880::comp_349).orElse(class_2246.field_10124);
      }
   }

   @Environment(EnvType.CLIENT)
   public record Entry(class_2248 block, class_2487 tag, BlockEntityLeakTracker.Source source) {
      public boolean real() {
         return this.source == BlockEntityLeakTracker.Source.CHUNK || this.source == BlockEntityLeakTracker.Source.DATA;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Source {
      CHUNK,
      DATA,
      EVENT,
      SOUND,
      LIGHT;
   }
}
