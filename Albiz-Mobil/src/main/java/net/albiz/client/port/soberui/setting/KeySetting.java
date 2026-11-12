// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.soberui.setting.Setting;
import java.util.function.Supplier;
import net.minecraft.class_1041;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

public class KeySetting
extends Setting<Integer> {
    private final Supplier<class_304> fallback;

    public KeySetting(String name, String description, Supplier<class_304> fallback) {
        super(name, description, -1);
        this.fallback = fallback;
    }

    public KeySetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
        this.fallback = null;
    }

    public boolean isPressed() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1755 != null) {
            return false;
        }
        if ((Integer)this.get() >= 0) {
            return class_3675.method_15987((class_1041)mc.method_22683(), (int)((Integer)this.get()));
        }
        class_304 binding = this.fallback == null ? null : this.fallback.get();
        return binding != null && binding.method_1434();
    }

    public void bind(int key) {
        this.set(key < 0 ? -1 : key);
    }

    @Override
    public String display() {
        if ((Integer)this.get() >= 0) {
            return class_3675.class_307.field_1668.method_1447(((Integer)this.get()).intValue()).method_27445().getString();
        }
        class_304 binding = this.fallback == null ? null : this.fallback.get();
        return binding == null ? "None" : "Game (" + binding.method_16007().getString() + ")";
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive((Number)this.value);
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.value = element.getAsInt() < 0 ? -1 : element.getAsInt();
        }
    }
}

