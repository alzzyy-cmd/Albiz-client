// Adapted from SoberClient (Violet), GPL-3.0-only.
package net.albiz.client.port.sober.sky;

import net.albiz.client.port.sober.sky.SkyType;

public final class SkySettings {
    public static SkyType sky = SkyType.VANILLA;
    public static int speed = 100;
    public static int brightness = 100;
    public static int auroraPalette = 0;
    public static int auroraColor = 4063152;
    public static boolean hideVanillaClouds = true;
    public static boolean matchFog = true;
    public static int quality = 50;

    private SkySettings() {
    }
}

