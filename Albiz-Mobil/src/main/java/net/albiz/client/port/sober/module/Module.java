// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.module;

import net.albiz.client.port.sober.module.Category;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.setting.Setting;
import net.albiz.client.port.sober.util.Chat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2604;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_310;

public abstract class Module {
    protected static class_310 mc;
    public final String name;
    public final String description;
    public final Category category;
    private final List<Setting<?>> settings = new ArrayList();
    private String currentGroup = "General";
    private boolean enabled;
    private int key = -1;
    private long enabledAt;

    public static void bindClient() {
        if (mc == null) {
            mc = class_310.method_1551();
        }
    }

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected void group(String name) {
        this.currentGroup = name;
    }

    protected <T> Setting<T> add(Setting<T> setting) {
        setting.setGroup(this.currentGroup);
        this.settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }

    public boolean isActive() {
        return this.enabled;
    }

    public void toggle() {
        this.setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.enabledAt = System.currentTimeMillis();
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public float fadeIn() {
        float t = Math.min(1.0f, (float)(System.currentTimeMillis() - this.enabledAt) / 600.0f);
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    public void setEnabledSilently(boolean enabled) {
        this.enabled = enabled;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getInfoString() {
        return null;
    }

    public boolean showInHud() {
        return true;
    }

    public boolean bindable() {
        return true;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onRender(Render3D renderer) {
    }

    public void onChunkLoad(class_2818 chunk) {
    }

    public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
    }

    public void onGameLeft() {
    }

    public boolean onEntitySpawn(class_2604 packet) {
        return false;
    }

    protected void info(String message, Object ... args) {
        Chat.info(this.name, String.format(message, args));
    }

    protected void warning(String message, Object ... args) {
        Chat.warning(this.name, String.format(message, args));
    }

    protected void error(String message, Object ... args) {
        Chat.error(this.name, String.format(message, args));
    }
}

