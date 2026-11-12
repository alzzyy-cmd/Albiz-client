// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.soberui.setting.Setting;

public class EnumSetting<E extends Enum<E>>
extends Setting<E> {
    private final E[] values;

    public EnumSetting(String name, String description, E defaultValue) {
        super(name, description, defaultValue);
        this.values = (E[])defaultValue.getDeclaringClass().getEnumConstants();
    }

    public void cycle(boolean forward) {
        int index = ((Enum)this.value).ordinal() + (forward ? 1 : -1);
        if (index < 0) {
            index = this.values.length - 1;
        }
        if (index >= this.values.length) {
            index = 0;
        }
        this.set(this.values[index]);
    }

    @Override
    public String display() {
        return ((Enum)this.value).toString();
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(((Enum)this.value).name());
    }

    @Override
    public void load(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            return;
        }
        String name = element.getAsString();
        for (E e : this.values) {
            if (!((Enum)e).name().equals(name)) continue;
            this.value = e;
        }
    }
}

