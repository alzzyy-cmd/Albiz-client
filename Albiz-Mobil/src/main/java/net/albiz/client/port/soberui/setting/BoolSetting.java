// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.soberui.setting.Setting;

public class BoolSetting
extends Setting<Boolean> {
    public BoolSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        this.set(this.get() == false);
    }

    @Override
    public String display() {
        return this.get() != false ? "On" : "Off";
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.get());
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.value = element.getAsBoolean();
        }
    }
}

