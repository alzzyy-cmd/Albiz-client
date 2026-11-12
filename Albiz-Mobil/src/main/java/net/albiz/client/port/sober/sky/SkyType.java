// Adapted from SoberClient (Violet), GPL-3.0-only.
package net.albiz.client.port.sober.sky;

import net.minecraft.class_2960;
import org.jspecify.annotations.Nullable;

public enum SkyType {
    VANILLA("vanilla", null),
    AURORA("aurora", new float[]{0.03f, 0.085f, 0.14f}),
    GALAXY("galaxy", new float[]{0.03f, 0.02f, 0.07f}),
    ANIME("anime", new float[]{0.72f, 0.89f, 1.0f}),
    STORM("storm", new float[]{0.09f, 0.16f, 0.26f}),
    BLOODMOON("bloodmoon", new float[]{0.2f, 0.03f, 0.025f}),
    PETALDUSK("petaldusk", new float[]{0.95f, 0.7f, 0.62f}),
    EVENTHORIZON("eventhorizon", new float[]{0.02f, 0.012f, 0.04f}),
    FROSTHALO("frosthalo", new float[]{0.78f, 0.87f, 0.97f}),
    FIREFLYNIGHT("fireflynight", new float[]{0.045f, 0.11f, 0.14f}),
    CANDYCLOUDS("candyclouds", new float[]{1.0f, 0.86f, 0.9f}),
    NEBULABLOOM("nebulabloom", new float[]{0.12f, 0.05f, 0.2f}, class_2960.method_60655((String)"albiz", (String)"textures/sky/nebula_bloom.png")),
    ASHENSQUALL("ashensquall", new float[]{0.27f, 0.29f, 0.32f}, class_2960.method_60655((String)"albiz", (String)"textures/sky/ashen_squall.png")),
    NIGHTSHADE("nightshade", new float[]{0.06f, 0.025f, 0.11f}),
    PHANTOMMIST("phantommist", new float[]{0.15f, 0.16f, 0.18f}),
    SYNTHWAVE("synthwave", new float[]{0.2f, 0.05f, 0.24f}),
    BLACKHOLE("blackhole", new float[]{0.02f, 0.015f, 0.045f}),
    SAKURA("sakura", new float[]{0.86f, 0.62f, 0.66f}),
    UNDERWATER("underwater", new float[]{0.035f, 0.22f, 0.32f}),
    CYBERPUNK("cyberpunk", new float[]{0.13f, 0.05f, 0.17f}),
    SOBER("sober", new float[]{0.02f, 0.02f, 0.024f}, class_2960.method_60655((String)"albiz", (String)"textures/sky/sober_discord.png"));

    private final String id;
    private final float @Nullable [] fogColor;
    private final @Nullable class_2960 texture;

    private SkyType(String id, float[] fogColor) {
        this(id, fogColor, null);
    }

    private SkyType(@Nullable String id, float[] fogColor, class_2960 texture) {
        this.id = id;
        this.fogColor = fogColor;
        this.texture = texture;
    }

    public String id() {
        return this.id;
    }

    public boolean isCustom() {
        return this != VANILLA;
    }

    public float @Nullable [] fogColor() {
        return this.fogColor;
    }

    public @Nullable class_2960 texture() {
        return this.texture;
    }
}

