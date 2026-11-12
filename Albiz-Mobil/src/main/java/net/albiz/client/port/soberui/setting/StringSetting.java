// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.soberui.setting.Setting;

public class StringSetting
extends Setting<String> {
    private final boolean hidden;

    public StringSetting(String name, String description, String defaultValue) {
        this(name, description, defaultValue, false);
    }

    public StringSetting(String name, String description, String defaultValue, boolean hidden) {
        super(name, description, defaultValue);
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public String display() {
        if (this.hidden && !((String)this.value).isEmpty()) {
            return "*".repeat(Math.min(12, ((String)this.value).length()));
        }
        return (String)this.value;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive((String)this.value);
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.value = element.getAsString();
        }
    }
}

