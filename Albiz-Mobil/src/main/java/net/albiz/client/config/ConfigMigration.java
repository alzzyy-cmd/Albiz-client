package net.albiz.client.config;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/** Copies once, publishes only after the entire copy succeeds; never removes originals. */
public final class ConfigMigration {
    private ConfigMigration() {}
    private static final String DIRECTORY = portableDirectory();
    private static String portableDirectory() {
        try { Path.of("albız"); return "albız"; }
        catch (InvalidPathException ex) {
            System.err.println("[ALBIZ] Filesystem encoding cannot represent albız; using portable albiz (never anubis)");
            return "albiz";
        }
    }
    public static String directoryName() { return DIRECTORY; }
    public static String mainFileName() { return DIRECTORY + ".json"; }
    private static Path prepare(Path folder) {
        Path previous = folder.resolve("modules.json"), current = folder.resolve(mainFileName());
        if (!Files.isRegularFile(previous) || Files.exists(current)) return folder;
        Path staging = null;
        try {
            staging = Files.createTempFile(folder,".albiz-config-migration-",".tmp");
            Files.copy(previous,staging,StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(staging,current,StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(staging,current); }
        } catch (IOException ex) {
            System.err.println("[ALBIZ] Main config rename failed; original retained: " + ex);
        } finally {
            if(staging != null) try { Files.deleteIfExists(staging); } catch(IOException ignored) {}
        }
        return folder;
    }
    public static synchronized Path root(Path configDir) {
        Path target = configDir.resolve(DIRECTORY);
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return prepare(target);
        Path legacy = configDir.resolve("albiz");
        if (!Files.isDirectory(legacy, LinkOption.NOFOLLOW_LINKS)) legacy = configDir.resolve("anubis");
        if (!Files.isDirectory(legacy, LinkOption.NOFOLLOW_LINKS)) legacy = configDir.resolve("anubisplus");
        if (!Files.isDirectory(legacy, LinkOption.NOFOLLOW_LINKS)) return prepare(target);
        Path staging = null;
        try {
            Files.createDirectories(configDir);
            staging = Files.createTempDirectory(configDir, ".albiz-migration-");
            try (var paths = Files.walk(legacy)) {
                for (Path from : (Iterable<Path>) paths::iterator) {
                    if (Files.isSymbolicLink(from)) continue;
                    Path to = staging.resolve(legacy.relativize(from));
                    if (Files.isDirectory(from, LinkOption.NOFOLLOW_LINKS)) Files.createDirectories(to);
                    else if (Files.isRegularFile(from, LinkOption.NOFOLLOW_LINKS))
                        Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
            Files.writeString(staging.resolve(".migration-complete"), "Legacy configuration copied; originals preserved.\n");
            try { Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(staging, target); }
        } catch (IOException ex) {
            System.err.println("[ALBIZ] Config migration failed; originals preserved: " + ex);
        } finally {
            if (staging != null && Files.exists(staging)) {
                try (var paths = Files.walk(staging)) {
                    for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
                } catch (IOException ignored) { /* No originals are touched. */ }
            }
        }
        return prepare(target);
    }
}
