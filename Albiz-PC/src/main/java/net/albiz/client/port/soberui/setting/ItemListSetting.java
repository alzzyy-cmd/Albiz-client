// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.albiz.client.port.soberui.setting.Setting;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1792;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class ItemListSetting
extends Setting<Set<class_1792>> {
    private final Map<class_1792, Integer> colors = new ConcurrentHashMap<class_1792, Integer>();

    public ItemListSetting(String name, String description, class_1792 ... defaults) {
        super(name, description, Collections.unmodifiableSet(new LinkedHashSet<class_1792>(Arrays.asList(defaults))));
    }

    public boolean allows(class_1792 block){return true;}
    public boolean contains(class_1792 block) {
        return (this.get()).contains(block);
    }

    public void toggle(class_1792 block) {
        LinkedHashSet<class_1792> next = new LinkedHashSet<class_1792>((Collection)this.get());
        if (!next.remove(block)) {
            next.add(block);
        }
        this.set(Collections.unmodifiableSet(next));
    }

    public void clear() {
        this.set(Collections.unmodifiableSet(new LinkedHashSet()));
    }

    public Integer color(class_1792 block) {
        return this.colors.get(block);
    }

    public void setColor(class_1792 block, Integer rgb) {
        if (rgb == null) {
            this.colors.remove(block);
        } else {
            this.colors.put(block, rgb & 0xFFFFFF);
        }
    }

    @Override
    public String display() {
        return (this.get()).size() == 1 ? "1 item" : (this.get()).size() + " items";
    }

    @Override
    public JsonElement save() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for (class_1792 block : this.get()) {
            array.add(class_7923.field_41178.method_10221(block).toString());
        }
        object.add("items", (JsonElement)array);
        JsonObject colorObject = new JsonObject();
        for (Map.Entry<class_1792, Integer> entry : this.colors.entrySet()) {
            colorObject.addProperty(class_7923.field_41178.method_10221(entry.getKey()).toString(), String.format("%06X", entry.getValue()));
        }
        object.add("colors", (JsonElement)colorObject);
        return object;
    }

    @Override
    public void load(JsonElement element) {
        class_2960 id;
        JsonArray array = null;
        this.colors.clear();
        if (element.isJsonArray()) {
            array = element.getAsJsonArray();
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("items") && object.get("items").isJsonArray()) {
                array = object.getAsJsonArray("items");
            }
            if (object.has("colors") && object.get("colors").isJsonObject()) {
                for (var entry : object.getAsJsonObject("colors").entrySet()) {
                    id = class_2960.method_12829((String)((String)entry.getKey()));
                    if (id == null || !class_7923.field_41178.method_10250(id)) continue;
                    try {
                        this.colors.put((class_1792)class_7923.field_41178.method_63535(id), Integer.parseInt(((JsonElement)entry.getValue()).getAsString(), 16) & 0xFFFFFF);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            }
        }
        if (array == null) {
            return;
        }
        LinkedHashSet<class_1792> loaded = new LinkedHashSet<class_1792>();
        for (JsonElement entry : array) {
            id = class_2960.method_12829((String)entry.getAsString());
            if (id == null || !class_7923.field_41178.method_10250(id)) continue;
            loaded.add((class_1792)class_7923.field_41178.method_63535(id));
        }
        this.value = Collections.unmodifiableSet(loaded);
    }
}

