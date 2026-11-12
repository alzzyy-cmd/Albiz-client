package net.albiz.client.port.soberui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.HudLayout;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.albiz.client.port.soberui.modules.Configs;
import net.albiz.client.port.soberui.setting.Setting;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigSlots {
    public static final int COUNT = 5;
    public static final String FORMAT = "soberclient-config";
    public static final String PREFIX = "SC-";
    private static final String OLD_PREFIX = "YC-";
    private static final List<Class<? extends Module>> SENSITIVE = List.of();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("albız").resolve("sober-ui-profiles.json");
    private static final String[] NAMES = new String[5];
    private static final long[] SAVED_AT = new long[5];
    private static final JsonObject[] STATES = new JsonObject[5];
    private static final boolean[] IMPORTED = new boolean[5];
    private static int active = -1;
    private static boolean loaded;

    private ConfigSlots() {
    }

    public static void reset() {
        loaded = false;
        active = -1;
        Arrays.fill(SAVED_AT, 0L);
        Arrays.fill(STATES, null);
        Arrays.fill(IMPORTED, false);
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        for (int i = 0; i < 5; ++i) {
            ConfigSlots.NAMES[i] = "Config " + (i + 1);
        }
        if (!Files.exists(FILE, new LinkOption[0])) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString((String)Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            int n = active = root.has("active") ? root.get("active").getAsInt() : -1;
            if (root.has("slots")) {
                JsonArray slots = root.getAsJsonArray("slots");
                for (int i = 0; i < Math.min(5, slots.size()); ++i) {
                    JsonObject slot = slots.get(i).getAsJsonObject();
                    if (slot.has("name")) {
                        ConfigSlots.NAMES[i] = slot.get("name").getAsString();
                    }
                    ConfigSlots.SAVED_AT[i] = slot.has("savedAt") ? slot.get("savedAt").getAsLong() : 0L;
                    ConfigSlots.IMPORTED[i] = slot.has("imported") && slot.get("imported").getAsBoolean();
                    ConfigSlots.STATES[i] = slot.has("state") && slot.get("state").isJsonObject() ? slot.getAsJsonObject("state") : null;
                }
            }
        }
        catch (Exception e) {
            SoberClient.LOG.error("Could not load configs", (Throwable)e);
        }
    }

    private static void write() {
        JsonObject root = new JsonObject();
        root.addProperty("active", (Number)active);
        JsonArray slots = new JsonArray();
        for (int i = 0; i < 5; ++i) {
            JsonObject slot = new JsonObject();
            slot.addProperty("name", NAMES[i]);
            slot.addProperty("savedAt", (Number)SAVED_AT[i]);
            slot.addProperty("imported", Boolean.valueOf(IMPORTED[i]));
            if (STATES[i] != null) {
                slot.add("state", (JsonElement)STATES[i]);
            }
            slots.add((JsonElement)slot);
        }
        root.add("slots", (JsonElement)slots);
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((JsonElement)root), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception e) {
            SoberClient.LOG.error("Could not save configs", (Throwable)e);
        }
    }

    public static String name(int slot) {
        ConfigSlots.ensureLoaded();
        return NAMES[slot];
    }

    public static boolean empty(int slot) {
        ConfigSlots.ensureLoaded();
        return STATES[slot] == null;
    }

    public static int active() {
        ConfigSlots.ensureLoaded();
        return active;
    }

    public static long savedAt(int slot) {
        ConfigSlots.ensureLoaded();
        return SAVED_AT[slot];
    }

    public static int firstEmpty() {
        ConfigSlots.ensureLoaded();
        for (int i = 0; i < 5; ++i) {
            if (STATES[i] != null) continue;
            return i;
        }
        return -1;
    }

    public static int enabledCount(int slot) {
        ConfigSlots.ensureLoaded();
        JsonObject state = STATES[slot];
        if (state == null || !state.has("modules") || !state.get("modules").isJsonObject()) {
            return 0;
        }
        int count = 0;
        for (Map.Entry entry : state.getAsJsonObject("modules").entrySet()) {
            if (!((JsonElement)entry.getValue()).isJsonObject() || !ConfigSlots.flag(((JsonElement)entry.getValue()).getAsJsonObject(), "enabled")) continue;
            ++count;
        }
        return count;
    }

    public static String pack(String json) {
        Deflater deflater = new Deflater(9, true);
        deflater.setInput(json.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer));
        }
        deflater.end();
        return "Y1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
    }

    public static String unpack(String payload) {
        if (!payload.startsWith("Y1:")) {
            throw new IllegalArgumentException("This code does not belong to a SoberClient config.");
        }
        try {
            return ConfigSlots.decode(payload.substring(3));
        }
        catch (Exception e) {
            throw new IllegalArgumentException("This shared config is broken.");
        }
    }

    public static void delete(int slot) {
        ConfigSlots.ensureLoaded();
        ConfigSlots.STATES[slot] = null;
        ConfigSlots.SAVED_AT[slot] = 0L;
        ConfigSlots.IMPORTED[slot] = false;
        ConfigSlots.NAMES[slot] = "Config " + (slot + 1);
        if (active == slot) {
            active = -1;
        }
        ConfigSlots.write();
    }

    public static void rename(int slot, String name) {
        String clean;
        ConfigSlots.ensureLoaded();
        String string = clean = name == null ? "" : name.strip();
        if (clean.isEmpty() || clean.equals(NAMES[slot])) {
            return;
        }
        ConfigSlots.NAMES[slot] = clean.length() > 32 ? clean.substring(0, 32) : clean;
        ConfigSlots.write();
    }

    private static JsonElement clean(JsonElement data) {
        if (!data.isJsonObject()) {
            return data;
        }
        JsonObject copy = data.getAsJsonObject().deepCopy();
        if (copy.has("settings") && copy.get("settings").isJsonObject()) {
            JsonObject settings = copy.getAsJsonObject("settings");
            settings.remove("Saved Options");
            for (String key : List.copyOf(settings.keySet())) {
                if (!key.endsWith(" Version")) continue;
                settings.remove(key);
            }
        }
        return copy;
    }

    private static boolean sensitive(Module module) {
        if(module instanceof net.albiz.client.port.soberui.AlbizModuleView v){String id=v.target.id().toLowerCase(java.util.Locale.ROOT);return v.target.category()==net.albiz.client.module.ModuleCategory.DONUT_MACROS || id.contains("pay") || id.contains("auth") || id.contains("account") || id.contains("panic");}

        for (Class<? extends Module> type : SENSITIVE) {
            if (!type.isInstance(module)) continue;
            return true;
        }
        return false;
    }

    public static void save(int slot) {
        ConfigSlots.ensureLoaded();
        JsonObject modules = new JsonObject();
        for (Map.Entry entry : SoberClient.CONFIG.toJson().entrySet()) {
            Module module = ConfigSlots.module((String)entry.getKey());
            if (module == null || module instanceof Configs) continue;
            modules.add(module.name + "@" + module.category.name(), ConfigSlots.clean((JsonElement)entry.getValue()));
        }
        JsonObject state = new JsonObject();
        state.add("modules", (JsonElement)modules);
        state.add("hud", (JsonElement)HudLayout.toJson());
        ConfigSlots.STATES[slot] = state;
        ConfigSlots.SAVED_AT[slot] = System.currentTimeMillis();
        ConfigSlots.IMPORTED[slot] = false;
        ConfigSlots.write();
    }

    public static boolean activate(int slot) {
        ConfigSlots.ensureLoaded();
        JsonObject state = STATES[slot];
        if (state == null) {
            return false;
        }
        JsonObject modules = new JsonObject();
        if (state.has("modules")) {
            for (Map.Entry entry : state.getAsJsonObject("modules").entrySet()) {
                String key = (String)entry.getKey();
                int at = key.lastIndexOf(64);
                modules.add(at > 0 ? key.substring(0, at) : key, ConfigSlots.clean((JsonElement)entry.getValue()));
            }
        }
        boolean imported = IMPORTED[slot];
        SoberClient.CONFIG.apply(modules, module -> module instanceof Configs || imported && ConfigSlots.sensitive(module), module -> module instanceof ClickGui);
        if (state.has("hud") && state.get("hud").isJsonObject()) {
            HudLayout.fromJson(state.getAsJsonObject("hud"));
        }
        active = slot;
        ConfigSlots.write();
        SoberClient.CONFIG.save();
        HudLayout.save();
        return true;
    }

    public static boolean imported(int slot) {
        ConfigSlots.ensureLoaded();
        return IMPORTED[slot];
    }

    public static String export(int slot) {
        ConfigSlots.ensureLoaded();
        JsonObject state = STATES[slot];
        if (state == null) {
            return null;
        }
        JsonObject saved = state.has("modules") && state.get("modules").isJsonObject() ? state.getAsJsonObject("modules") : new JsonObject();
        JsonObject modules = new JsonObject();
        for (Map.Entry entry : saved.entrySet()) {
            String key = (String)entry.getKey();
            int at = key.lastIndexOf(64);
            Module module = ConfigSlots.module(at > 0 ? key.substring(0, at) : key);
            if (module == null || module instanceof Configs || !((JsonElement)entry.getValue()).isJsonObject()) continue;
            JsonObject data = ConfigSlots.clean((JsonElement)entry.getValue()).getAsJsonObject();
            JsonObject out = new JsonObject();
            out.addProperty("enabled", Boolean.valueOf(ConfigSlots.flag(data, "enabled")));
            out.addProperty("keybind", (Number)ConfigSlots.number(data, "key", ConfigSlots.number(data, "keybind", -1)));
            out.add("settings", (JsonElement)(data.has("settings") && data.get("settings").isJsonObject() ? data.get("settings") : new JsonObject()));
            modules.add(module.name + "@" + module.category.name(), (JsonElement)out);
        }
        JsonObject exported = new JsonObject();
        exported.add("modules", (JsonElement)modules);
        if (state.has("hud") && state.get("hud").isJsonObject()) {
            exported.add("hud", state.get("hud"));
        }
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("version", (Number)1);
        root.addProperty("name", NAMES[slot]);
        root.addProperty("savedAt", (Number)SAVED_AT[slot]);
        root.addProperty("client", ConfigSlots.version());
        root.add("state", (JsonElement)exported);
        return GSON.toJson((JsonElement)root);
    }

    private static String version() {
        return FabricLoader.getInstance().getModContainer("soberclient").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("1.0.0");
    }

    private static boolean flag(JsonObject data, String key) {
        try {
            return data.has(key) && data.get(key).getAsBoolean();
        }
        catch (Exception e) {
            return false;
        }
    }

    private static int number(JsonObject data, String key, int fallback) {
        try {
            return data.has(key) ? data.get(key).getAsInt() : fallback;
        }
        catch (Exception e) {
            return fallback;
        }
    }

    private static long stamp(JsonObject data, String key) {
        try {
            return data.has(key) ? data.get(key).getAsLong() : 0L;
        }
        catch (Exception e) {
            return 0L;
        }
    }

    private static String text(JsonObject data, String key) {
        try {
            return data.has(key) ? data.get(key).getAsString().strip() : "";
        }
        catch (Exception e) {
            return "";
        }
    }

    private static String store(int slot, JsonObject byName, JsonElement hud, String name, long savedAt) {
        JsonObject modules = new JsonObject();
        for (Module module : SoberClient.MODULES.all()) {
            if (module instanceof Configs) continue;
            JsonObject source = byName.has(module.name) && byName.get(module.name).isJsonObject() ? byName.getAsJsonObject(module.name) : new JsonObject();
            JsonObject given = source.has("settings") && source.get("settings").isJsonObject() ? source.getAsJsonObject("settings") : new JsonObject();
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                settings.add(setting.name, given.has(setting.name) ? given.get(setting.name) : setting.defaultJson());
            }
            JsonObject data = new JsonObject();
            data.addProperty("enabled", Boolean.valueOf(ConfigSlots.flag(source, "enabled")));
            data.addProperty("key", (Number)ConfigSlots.number(source, "key", ConfigSlots.number(source, "keybind", -1)));
            data.add("settings", (JsonElement)settings);
            modules.add(module.name + "@" + module.category.name(), (JsonElement)data);
        }
        JsonObject state = new JsonObject();
        state.add("modules", (JsonElement)modules);
        if (hud != null && hud.isJsonObject()) {
            state.add("hud", hud);
        }
        ConfigSlots.STATES[slot] = state;
        ConfigSlots.SAVED_AT[slot] = savedAt > 0L ? savedAt : System.currentTimeMillis();
        ConfigSlots.IMPORTED[slot] = true;
        if (!name.isEmpty()) {
            String string = ConfigSlots.NAMES[slot] = name.length() > 32 ? name.substring(0, 32) : name;
        }
        if (active == slot) {
            active = -1;
        }
        ConfigSlots.write();
        return NAMES[slot];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String decode(String code) throws DataFormatException {
        byte[] data = Base64.getUrlDecoder().decode(code);
        Inflater inflater = new Inflater(true);
        inflater.setInput(Arrays.copyOf(data, data.length + 1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.needsInput()) break;
                    if (inflater.needsDictionary()) {
                        break;
                    }
                }
                out.write(buffer, 0, count);
                if (out.size() <= 4000000) continue;
                throw new DataFormatException("too large");
            }
        }
        finally {
            inflater.end();
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static String importCode(int slot, String body) {
        JsonObject root;
        try {
            root = JsonParser.parseString((String)ConfigSlots.decode(body)).getAsJsonObject();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("This SC- code is broken or incomplete.");
        }
        JsonObject coded = root.has("m") && root.get("m").isJsonObject() ? root.getAsJsonObject("m") : new JsonObject();
        JsonObject byName = new JsonObject();
        for (Map.Entry entry : coded.entrySet()) {
            if (!((JsonElement)entry.getValue()).isJsonObject()) continue;
            JsonObject compact = ((JsonElement)entry.getValue()).getAsJsonObject();
            JsonObject data = new JsonObject();
            data.addProperty("enabled", Boolean.valueOf(ConfigSlots.number(compact, "e", 0) == 1));
            data.addProperty("key", (Number)ConfigSlots.number(compact, "k", -1));
            if (compact.has("s") && compact.get("s").isJsonObject()) {
                data.add("settings", compact.get("s"));
            }
            byName.add((String)entry.getKey(), (JsonElement)data);
        }
        return ConfigSlots.store(slot, byName, root.get("h"), ConfigSlots.text(root, "n"), ConfigSlots.stamp(root, "t"));
    }

    public static String importJson(int slot, String text) {
        JsonObject state;
        JsonObject root;
        ConfigSlots.ensureLoaded();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Your clipboard is empty.");
        }
        String compact = text.replaceAll("\\s+", "");
        if (compact.startsWith(PREFIX) || compact.startsWith(OLD_PREFIX)) {
            return ConfigSlots.importCode(slot, compact.substring(PREFIX.length()));
        }
        try {
            root = JsonParser.parseString((String)text.strip()).getAsJsonObject();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("This is not a SoberClient config. Copy the whole text from the first { to the last }.");
        }
        if (!FORMAT.equals(ConfigSlots.text(root, "format"))) {
            throw new IllegalArgumentException("This is not a SoberClient config.");
        }
        JsonObject jsonObject = state = root.has("state") && root.get("state").isJsonObject() ? root.getAsJsonObject("state") : null;
        if (state == null || !state.has("modules") || !state.get("modules").isJsonObject()) {
            throw new IllegalArgumentException("This config has no settings in it.");
        }
        JsonObject byName = new JsonObject();
        for (Map.Entry entry : state.getAsJsonObject("modules").entrySet()) {
            if (!((JsonElement)entry.getValue()).isJsonObject()) continue;
            String key = (String)entry.getKey();
            int at = key.lastIndexOf(64);
            byName.add(at > 0 ? key.substring(0, at) : key, (JsonElement)entry.getValue());
        }
        return ConfigSlots.store(slot, byName, state.get("hud"), ConfigSlots.text(root, "name"), ConfigSlots.stamp(root, "savedAt"));
    }

    private static Module module(String name) {
        for (Module module : SoberClient.MODULES.all()) {
            if (!module.name.equals(name)) continue;
            return module;
        }
        return null;
    }
}

