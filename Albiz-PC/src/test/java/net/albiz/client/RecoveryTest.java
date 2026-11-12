package net.albiz.client;
import net.albiz.client.config.AtomicFiles;
import net.albiz.client.config.ConfigMigration;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.input.ModuleKeybinds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryTest {
    @TempDir Path dir;
    @Test void atomicWriteReplacesAndKeepsUtf8() throws Exception {
        Path file = dir.resolve("config/modules.json");
        AtomicFiles.writeUtf8(file, "old");
        AtomicFiles.writeUtf8(file, "ıİğĞşŞçÇöÖüÜ");
        assertEquals("ıİğĞşŞçÇöÖüÜ", Files.readString(file));
        try (var files = Files.list(file.getParent())) { assertEquals(1, files.count()); }
    }
    @Test void corruptBackupPreservesExactBytes() throws Exception {
        Path file = dir.resolve("bad.json"); Files.writeString(file, "{broken");
        Path backup = AtomicFiles.backupCorrupt(file);
        assertFalse(Files.exists(file)); assertEquals("{broken", Files.readString(backup));
    }
    @Test void migrationCopiesOnceNeverDeletesLegacy() throws Exception {
        Path old = dir.resolve("anubis/profiles"); Files.createDirectories(old);
        Files.writeString(old.resolve("one.json"), "{\"version\":3}");
        Path migrated = ConfigMigration.root(dir);
        assertEquals("{\"version\":3}", Files.readString(migrated.resolve("profiles/one.json")));
        assertTrue(Files.exists(old.resolve("one.json")));
        Files.writeString(old.resolve("one.json"), "changed");
        ConfigMigration.root(dir);
        assertEquals("{\"version\":3}", Files.readString(migrated.resolve("profiles/one.json")));
    }
    @Test void existingTargetIsNeverOverwritten() throws Exception {
        Files.createDirectories(dir.resolve("anubis")); Files.createDirectories(dir.resolve("albız"));
        Files.writeString(dir.resolve("albız/test"), "keep");
        assertEquals(dir.resolve("albız"), ConfigMigration.root(dir));
        assertEquals("keep", Files.readString(dir.resolve("albız/test")));
    }
    @Test void platformManualOverrideAndGlslSelection() {
        try {
            PlatformInfo.setMode(PlatformInfo.Mode.MOBILE); assertTrue(PlatformInfo.isMobile());
            PlatformInfo.setMode(PlatformInfo.Mode.DESKTOP); assertFalse(PlatformInfo.isMobile());
            assertEquals("#version 300 es", PlatformInfo.glslVersion("OpenGL ES 3.0"));
            assertEquals("#version 130", PlatformInfo.glslVersion("3.0 GL4ES"));
        } finally { PlatformInfo.setMode(PlatformInfo.Mode.AUTO); }
    }
    @Test void unsupportedKeysAreRejected() {
        for (int key : new int[] {-1, Integer.MIN_VALUE, 0, 40, 64, 163, 255, 270, 285, 315, 337, 349, 999, 1008, Integer.MAX_VALUE})
            assertFalse(ModuleKeybinds.isBindable(key), "key=" + key);
        for (int key : new int[] {32, 39, 65, 256, 261, 348, 1000, 1007}) assertTrue(ModuleKeybinds.isBindable(key));
    }
    @Test void nullPollingInputDoesNotCrash() {
        ModuleKeybinds bindings = new ModuleKeybinds();
        assertDoesNotThrow(() -> bindings.update(null, null, true));
        assertDoesNotThrow(() -> bindings.update(List.of(), key -> false, false));
    }
    @Test void moduleSetterRejectsInvalidAndSupportsClear() {
        var module = new net.albiz.client.module.ClientModule("test-module", "Test", "Test", net.albiz.client.module.ModuleCategory.CLIENT) {};
        module.setKeybind(65); assertEquals(65, module.keybind());
        module.setKeybind(Integer.MAX_VALUE); assertEquals(65, module.keybind());
        module.setKeybind(-1); assertEquals(-1, module.keybind());
    }
    @Test void staleCachedBindingIsCheckedBeforeArrayAccess() throws Exception {
        var module = new net.albiz.client.module.ClientModule("test-stale", "Test", "Test", net.albiz.client.module.ModuleCategory.CLIENT) {};
        module.setKeybind(65);
        List<net.albiz.client.module.ClientModule> modules = List.of(module);
        ModuleKeybinds bindings = new ModuleKeybinds(); bindings.update(modules, key -> false, true);
        var field = net.albiz.client.module.ClientModule.class.getDeclaredField("keybind");
        field.setAccessible(true); field.setInt(module, Integer.MAX_VALUE);
        assertDoesNotThrow(() -> bindings.update(modules, key -> false, true));
    }
    @Test void officialBuildNeverEnforcesOrStops() {
        assertDoesNotThrow(net.albiz.client.security.OfficialBuild::checkBeforeLaunch);
        assertDoesNotThrow(net.albiz.client.security.OfficialBuild::enforceIfPending);
        assertDoesNotThrow(net.albiz.client.security.OfficialBuild::enforceOnFirstTick);
        assertEquals(net.albiz.client.security.OfficialBuild.State.NOT_CHECKED, net.albiz.client.security.OfficialBuild.state());
    }
    @Test void suppliedAndroidNativeIsActuallyArm64() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/albiz/natives/android-arm64/libimgui-java64.so")) {
            assertNotNull(stream); byte[] bytes = stream.readAllBytes();
            assertEquals(0x7f, bytes[0]); assertEquals('E', bytes[1]);
            assertEquals(183, java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort(18));
            assertEquals("bb6ea1f6087ab0bfd8aafbbbb84a3826c1dfd293f7f535964aad094a0771d73c", java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));
        }
    }
    @Test void desktopPreLaunchDoesNotOverrideNativePath() {
        if (!PlatformInfo.isAndroidArm64()) {
            String before = System.getProperty("imgui.library.path");
            new net.albiz.client.platform.AlbizPreLaunch().onPreLaunch();
            assertEquals(before, System.getProperty("imgui.library.path"));
        }
    }
 @org.junit.jupiter.api.Test void editorPathsAvoidInternalJniAndGhostTerrainCancellation() throws Exception {
  var root=java.nio.file.Path.of("src/main/java/net/albiz/client");
  for(String file:new String[]{"gui/ItemPickerEditor.java","gui/BlockListEditor.java"}) {
   String text=java.nio.file.Files.readString(root.resolve(file));
   org.junit.jupiter.api.Assertions.assertFalse(text.contains("imgui.internal.ImGui"),file);
   org.junit.jupiter.api.Assertions.assertTrue(text.contains("PickerIcons"),file);
  }
  org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.readString(root.resolve("module/impl/render/BlockEntityDebugModule.java")).contains("event.setCancelled(true)"));
 }
 @org.junit.jupiter.api.Test void pickerUsesRealIconsRatherThanMobileInitialsBadges() throws Exception {
  String text=java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/net/albiz/client/gui/PickerIcons.java"));
  org.junit.jupiter.api.Assertions.assertTrue(text.contains("ItemIcons.draw"));
  org.junit.jupiter.api.Assertions.assertTrue(text.contains("BlockIcons.draw"));
  org.junit.jupiter.api.Assertions.assertFalse(text.contains("badge("));
  org.junit.jupiter.api.Assertions.assertFalse(text.contains("isMobile()"));
 }
}
