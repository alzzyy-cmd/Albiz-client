package net.albiz.client.platform;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import java.nio.file.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Select the supplied Android ABI before any ImGui class initializes. */
public final class AlbizPreLaunch implements PreLaunchEntrypoint {
    private static final String SHA256 = "bb6ea1f6087ab0bfd8aafbbbb84a3826c1dfd293f7f535964aad094a0771d73c";
    @Override public void onPreLaunch() {
        if (!PlatformInfo.isAndroidArm64() || System.getProperty("imgui.library.path") != null) return;
        try (var stream = AlbizPreLaunch.class.getResourceAsStream("/assets/albiz/natives/android-arm64/libimgui-java64.so")) {
            if (stream == null) throw new IOException("Android native resource missing");
            byte[] bytes = stream.readAllBytes();
            if (!SHA256.equals(digest(bytes))) throw new IOException("Android native checksum mismatch");
            // Android shared storage may be noexec. Use the launcher's JVM private temp directory.
            Path directory = Path.of(System.getProperty("java.io.tmpdir")).resolve("albiz-natives").resolve(SHA256);
            Files.createDirectories(directory);
            Path library = directory.resolve("libimgui-java64.so");
            if (!Files.isRegularFile(library) || !SHA256.equals(digest(Files.readAllBytes(library)))) {
                Path tmp = Files.createTempFile(directory, "native-", ".tmp");
                try {
                    Files.write(tmp, bytes);
                    try { Files.move(tmp, library, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                    catch (AtomicMoveNotSupportedException ex) { Files.move(tmp, library, StandardCopyOption.REPLACE_EXISTING); }
                } finally { Files.deleteIfExists(tmp); }
            }
            System.setProperty("imgui.library.path", directory.toAbsolutePath().toString());
            System.err.println("[ALBIZ] Selected supplied Android arm64 ImGui native");
        } catch (IOException | NoSuchAlgorithmException | RuntimeException ex) {
            // Do not halt the game. A launcher-provided native remains a possible fallback.
            System.err.println("[ALBIZ] Android native preparation failed; launcher fallback will be used: " + ex);
        }
    }
    private static String digest(byte[] bytes) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
