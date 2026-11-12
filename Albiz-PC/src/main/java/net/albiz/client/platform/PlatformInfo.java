package net.albiz.client.platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** No Minecraft/GL dependency; safe before client bootstrap. */
public final class PlatformInfo {
    public enum Mode { AUTO, MOBILE, DESKTOP }
    private static final boolean DETECTED = detect();
    private static volatile Mode override = Mode.AUTO;
    private PlatformInfo() {}
    public static boolean isMobile() {
        return BuildProfile.mobile() || switch (override) { case MOBILE -> true; case DESKTOP -> false; default -> DETECTED; };
    }
    public static boolean isAndroidArm64() { return DETECTED; }
    public static Mode mode() { return override; }
    public static void setMode(Mode mode) { override = mode == null ? Mode.AUTO : mode; }
    public static void setMode(String mode) {
        try { setMode(Mode.valueOf(mode.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException | NullPointerException ignored) { setMode(Mode.AUTO); }
    }
    public static String glslVersion(String version) {
        String forced = System.getProperty("albiz.glsl", "");
        if (forced.equals("130")) return "#version 130";
        if (forced.equals("300 es")) return "#version 300 es";
        return version != null && version.contains("OpenGL ES") ? "#version 300 es" : "#version 130";
    }
    private static boolean detect() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (!arch.equals("aarch64") && !arch.equals("arm64")) return false;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String runtime = System.getProperty("java.runtime.name", "").toLowerCase(Locale.ROOT);
        return os.contains("android") || runtime.contains("android")
            || System.getenv("ANDROID_ROOT") != null || System.getenv("ANDROID_DATA") != null
            || Files.exists(Path.of("/system/build.prop"));
    }
}
