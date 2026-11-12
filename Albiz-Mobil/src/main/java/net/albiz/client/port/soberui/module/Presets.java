// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.module;

import net.minecraft.class_1799;

public interface Presets {
    public int presetCount();

    public String presetName(int var1);

    public int presetColor(int var1);

    public boolean presetActive(int var1);

    public void selectPreset(int var1);

    public String presetDescription();

    default public class_1799 presetIcon(int index) {
        return class_1799.field_8037;
    }
}

