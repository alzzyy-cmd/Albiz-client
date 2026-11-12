// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.soberui.setting.Setting;

public class IntSetting
extends Setting<Integer> {
    public final int min;
    public final int max;

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Integer validate(Integer newValue) {
        return Math.max(this.min, Math.min(this.max, newValue));
    }

    @Override
    public String display() {
        return Integer.toString((Integer)this.value);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive((Number)this.value);
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.value = this.validate(element.getAsInt());
        }
    }
}

