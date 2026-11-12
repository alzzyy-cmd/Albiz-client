// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.model.ModelLibrary;
import net.albiz.client.port.soberui.model.ModelLoader;
import net.albiz.client.port.soberui.model.ModelMesh;
import net.albiz.client.port.soberui.model.SpriteModel;
import net.albiz.client.port.soberui.model.WaterModels;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.CallSite;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class ModelManager {
    private static final Map<String, ModelMesh> CACHE = new java.util.LinkedHashMap<String,ModelMesh>(64,.75f,true){protected boolean removeEldestEntry(java.util.Map.Entry<String,ModelMesh> e){return size()>64;}};
    private static final Map<String,String> FAILED=new java.util.LinkedHashMap<String,String>(128,.75f,true){protected boolean removeEldestEntry(java.util.Map.Entry<String,String> e){return size()>128;}};
    private static final Map<String, SpriteModel> SPRITES = new java.util.LinkedHashMap<String,SpriteModel>(64,.75f,true){protected boolean removeEldestEntry(java.util.Map.Entry<String,SpriteModel> e){if(size()<=64)return false;e.getValue().release();return true;}};
    private static final Map<String, CompletableFuture<SpriteModel.Prepared>> PENDING = new HashMap<String, CompletableFuture<SpriteModel.Prepared>>();
    private static final java.util.concurrent.ThreadPoolExecutor LOADER=new java.util.concurrent.ThreadPoolExecutor(1,1,0L,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.ArrayBlockingQueue<>(32),r->{Thread t=new Thread(r,"Albiz Sprite Loader");t.setDaemon(true);t.setPriority(1);return t;},new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
    public static final String WATERY = "Watery/";
    private static final Map<String, String> BUILTIN_PACKS = Map.of("Fortnite Pack/", "/assets/albiz/models/fortnite_pack/");
    private static final Map<String, Set<String>> BUILTIN_ITEMS = ModelManager.builtinItems();
    private static volatile List<String> files = ModelManager.initial();
    private static volatile Map<String, Set<String>> packs = Map.of();
    private static volatile long lastScan;
    private static volatile boolean scanning;
    private static Path folder;

    private ModelManager() {
    }

    private static List<String> initial() {
        ArrayList<String> list = new ArrayList<String>(ModelLibrary.names());
        list.addAll(BUILTIN_PACKS.keySet());
        list.add(WATERY);
        return List.copyOf(list);
    }

    private static Map<String, Set<String>> builtinItems() {
        HashMap result = new HashMap();
        for (Map.Entry<String, String> pack : BUILTIN_PACKS.entrySet()) {
            HashSet<String> items = new HashSet<String>();
            try (InputStream in = ModelManager.class.getResourceAsStream(pack.getValue() + "items.txt");){
                if (in != null) {
                    for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                        if (line.isBlank()) continue;
                        items.add(line.trim().toLowerCase(Locale.ROOT));
                    }
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            result.put(pack.getKey(), Set.copyOf(items));
        }
        return Map.copyOf(result);
    }

    public static Path folder() {
        if (folder != null) {
            return folder;
        }
        Path path = net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("models");
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        folder = path;
        return path;
    }

    public static List<String> files() {
        long now = System.currentTimeMillis();
        if (now - lastScan > 5000L && !scanning) {
            lastScan = now;
            scanning = true;
            CompletableFuture.runAsync(() -> {
                try {
                    ModelManager.rescan();
                }
                finally {
                    scanning = false;
                }
            });
        }
        return files;
    }

    public static void rescan() {
        lastScan = System.currentTimeMillis();
        ArrayList<String> found = new ArrayList<String>(ModelManager.initial());
        ArrayList<String> local = new ArrayList<String>();
        HashMap<String, Set<String>> foundPacks = new HashMap<String, Set<String>>();
        try (Stream<Path> stream = Files.list(ModelManager.folder());){
            for (Path path : (Iterable<Path>)stream.limit(128)::iterator) {
                String name = path.getFileName().toString();
                if (Files.isDirectory(path, new LinkOption[0])) {
                    Set<String> items;
                    if (BUILTIN_PACKS.containsKey(name + "/") || WATERY.equals(name + "/") || (items = ModelManager.packItems(path)).isEmpty()) continue;
                    foundPacks.put(name + "/", items);
                    local.add(name + "/");
                    continue;
                }
                if (!Files.isRegularFile(path, new LinkOption[0]) || !ModelManager.supported(name)) continue;
                local.add(name);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        local.sort(String.CASE_INSENSITIVE_ORDER);
        found.addAll(local);
        packs = Map.copyOf(foundPacks);
        files = List.copyOf(found);
    }

    private static Set<String> packItems(Path directory) {
        HashSet<String> items = new HashSet<String>();
        try (Stream<Path> stream = Files.list(directory);){
            stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(p -> p.getFileName().toString()).filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png")).limit(512).forEach(n -> items.add(n.substring(0, n.length() - 4).toLowerCase(Locale.ROOT)));
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return items;
    }

    public static void reload() {
        CACHE.clear();
        FAILED.clear();
        for (SpriteModel sprite : SPRITES.values()) {
            if (sprite == null) continue;
            sprite.release();
        }
        SPRITES.clear();
        for(var pending:PENDING.values())pending.cancel(false);
        PENDING.clear();LOADER.getQueue().clear();
        ModelManager.rescan();
    }

    private static boolean supported(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".stl") || lower.endsWith(".obj") || lower.endsWith(".png");
    }

    public static boolean isPack(String name) {
        return name != null && name.endsWith("/");
    }

    public static boolean isSprite(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).endsWith(".png");
    }

    public static boolean isWatery(String name) {
        return WATERY.equals(name);
    }

    public static boolean packHas(String pack, String item) {
        if (WATERY.equals(pack)) {
            return WaterModels.kind(item) != null;
        }
        Set<String> builtin = BUILTIN_ITEMS.get(pack);
        if (builtin != null) {
            return builtin.contains(item);
        }
        Set<String> items = packs.get(pack);
        return items != null && items.contains(item);
    }

    public static SpriteModel sprite(String name) {
        try{return ModelManager.loadSprite(name, VisualModelPaths.resolve(ModelManager.folder(),name), null);}catch(IllegalArgumentException e){FAILED.put(name,"Invalid model path");return null;}
    }

    public static SpriteModel packSprite(String pack, String item) {
        if (WATERY.equals(pack) || !ModelManager.packHas(pack, item)) {
            return null;
        }
        String resource = BUILTIN_PACKS.get(pack);
        if (resource != null) {
            return ModelManager.loadSprite(pack + item, null, resource + item + ".png");
        }
        try{return ModelManager.loadSprite(pack + item, VisualModelPaths.resolve(ModelManager.folder(),pack + item + ".png"), null);}catch(IllegalArgumentException e){FAILED.put(pack+item,"Invalid model path");return null;}
    }

    private static SpriteModel loadSprite(String key, Path file, String resource) {
        if (FAILED.containsKey(key)) {
            return null;
        }
        SpriteModel sprite = SPRITES.get(key);
        if (sprite != null) {
            return sprite;
        }
        CompletableFuture<SpriteModel.Prepared> pending = PENDING.get(key);
        if (pending == null) {
            if(PENDING.size()>=32)return null;
            try{PENDING.put(key, CompletableFuture.supplyAsync(() -> ModelManager.prepare(file, resource), LOADER));}catch(java.util.concurrent.RejectedExecutionException busy){return null;}
            return null;
        }
        if (!pending.isDone()) {
            return null;
        }
        PENDING.remove(key);
        try {
            sprite = SpriteModel.upload(pending.join());
            SPRITES.put(key, sprite);
            return sprite;
        }
        catch (RuntimeException e) {
            Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
            FAILED.put(key, String.valueOf(cause.getMessage()));
            SoberClient.LOG.error("Could not load sprite {}", (Object)key, (Object)cause);
            return null;
        }
    }

    private static SpriteModel.Prepared prepare(Path file,String resource){
      try{if(resource==null){if(Files.size(file)>16L*1024*1024)throw new IOException("Image exceeds 16 MiB budget");return SpriteModel.prepare(file);}
       try(InputStream in=ModelManager.class.getResourceAsStream(resource)){if(in==null)throw new IOException("Missing "+resource);return SpriteModel.prepare(in);}
      }catch(IOException ex){throw new UncheckedIOException(ex);}
    }

    public static ModelMesh get(String name) {
        if (name == null || name.isEmpty() || ModelManager.isPack(name) || ModelManager.isSprite(name) || FAILED.containsKey(name)) {
            return null;
        }
        ModelMesh mesh = CACHE.get(name);
        if (mesh != null) {
            return mesh;
        }
        if (ModelLibrary.has(name)) {
            try {
                mesh = ModelLibrary.build(name);
                CACHE.put(name, mesh);
                return mesh;
            }
            catch (RuntimeException e) {
                FAILED.put(name, String.valueOf(e.getMessage()));
                SoberClient.LOG.error("Could not build model {}", (Object)name, (Object)e);
                return null;
            }
        }
        Path file;try{file=VisualModelPaths.resolve(ModelManager.folder(),name);}catch(IllegalArgumentException e){FAILED.put(name,"Invalid model path");return null;}
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            FAILED.put(name, "file not found");
            return null;
        }
        try {
            if(Files.size(file)>16L*1024*1024)throw new IOException("Model exceeds 16 MiB budget");
            mesh = ModelLoader.load(file);
            CACHE.put(name, mesh);
            return mesh;
        }
        catch (IOException | RuntimeException e) {
            FAILED.put(name, String.valueOf(e.getMessage()));
            SoberClient.LOG.error("Could not load model {}", (Object)name, (Object)e);
            return null;
        }
    }

    public static String error(String name) {
        return FAILED.get(name);
    }

    public static String title(String file) {
        if (ModelLibrary.has(file)) {
            return ModelLibrary.title(file);
        }
        if (ModelManager.isPack(file)) {
            return file.substring(0, file.length() - 1);
        }
        int dot = file.lastIndexOf(46);
        String base = dot > 0 ? file.substring(0, dot) : file;
        return base.replace('_', ' ').replace('-', ' ');
    }
}

