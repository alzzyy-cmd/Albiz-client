package net.albiz.client.easyplace.builder;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.albiz.client.easyplace.EasyPlace;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
final class ScaffoldStore {
   private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(task -> {
      Thread thread = new Thread(task, "EasyPlace scaffold list");
      thread.setDaemon(true);
      return thread;
   });
   private final Object lock = new Object();
   private int savedHash;
   private String savedWorld;

   private static Path file() {
      return class_310.method_1551().field_1697.toPath().resolve("config").resolve("easyplace-builder-scaffold.json");
   }

   static String worldKey(class_310 mc) {
      if (mc.field_1687 == null) {
         return null;
      } else {
         String place;
         if (mc.method_1576() != null) {
            place = "local:" + mc.method_1576().method_27728().method_150();
         } else if (mc.method_1558() != null) {
            place = mc.method_1558().field_3761.toLowerCase(Locale.ROOT);
         } else {
            place = "unknown";
         }

         return place + " " + mc.field_1687.method_27983();
      }
   }

   Set<Long> load(String world) {
      Set<Long> out = new HashSet<>();
      synchronized (this.lock) {
         JsonObject root = read();
         JsonElement var6 = root.get(world);
         if (var6 instanceof JsonArray) {
            for (JsonElement element : (JsonArray)var6) {
               out.add(element.getAsLong());
            }
         }
      }

      this.savedWorld = world;
      this.savedHash = out.hashCode();
      return out;
   }

   void save(String world, Set<Long> scaffold) {
      if (world != null) {
         int hash = scaffold.hashCode();
         if (!world.equals(this.savedWorld) || hash != this.savedHash) {
            this.savedWorld = world;
            this.savedHash = hash;
            long[] copy = scaffold.stream().mapToLong(Long::longValue).toArray();
            WRITER.execute(() -> this.write(world, copy));
         }
      }
   }

   private void write(String world, long[] positions) {
      synchronized (this.lock) {
         JsonObject root = read();
         if (positions.length == 0) {
            root.remove(world);
         } else {
            JsonArray list = new JsonArray();

            for (long position : positions) {
               list.add(position);
            }

            root.add(world, list);
         }

         try {
            Path file = file();
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (IOException var12) {
            EasyPlace.LOG.warn("Could not save the builder's scaffold list", var12);
         }
      }
   }

   private static JsonObject read() {
      Path file = file();
      if (!Files.isRegularFile(file)) {
         return new JsonObject();
      } else {
         try {
            JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
         } catch (RuntimeException | IOException var2) {
            EasyPlace.LOG.warn("Could not read the builder's scaffold list", var2);
            return new JsonObject();
         }
      }
   }
}
