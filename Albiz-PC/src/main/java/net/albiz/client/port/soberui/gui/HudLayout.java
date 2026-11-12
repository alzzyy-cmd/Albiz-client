// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumMap;
import net.fabricmc.loader.api.FabricLoader;

public final class HudLayout {
    private static final int START = 0;
    private static final int CENTER = 1;
    private static final int END = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soberclient-hud-layout.json");
    private static final EnumMap<Element, Position> POSITIONS = new EnumMap(Element.class);

    public static void reset() {
        HudLayout.defaults();
    }

    private static void defaults() {
        POSITIONS.put(Element.WATERMARK, new Position(5, 5, 1.0f, false, false));
        POSITIONS.put(Element.FPS, new Position(83, 5, 1.0f, false, false));
        POSITIONS.put(Element.BPS, new Position(145, 5, 1.0f, false, false));
        POSITIONS.put(Element.TPS, new Position(5, 29, 1.0f, false, false));
        POSITIONS.put(Element.XYZ, new Position(78, 29, 1.0f, false, false));
        POSITIONS.put(Element.RADAR, new Position(5, 57, 1.0f, false, false));
        POSITIONS.put(Element.MODULE_LIST, new Position(5, 5, 1.0f, true, false));
        POSITIONS.put(Element.STAFF_LIST, new Position(5, 142, 1.0f, true, false));
        POSITIONS.put(Element.KEYSTROKES, new Position(5, 5, 1.0f, false, true));
        POSITIONS.put(Element.INVENTORY, new Position(5, 5, 1.0f, true, true));
        POSITIONS.put(Element.REGION, new Position(5, 152, 1.0f, false, false));
        POSITIONS.put(Element.SPOTIFY, new Position(5, 92, 1.0f, false, true));
        POSITIONS.put(Element.STAFF_LIVE, new Position(5, 262, 1.0f, true, false));
        POSITIONS.put(Element.TOGIS, new Position(5, 100, 1.0f, true, true));
        POSITIONS.put(Element.HOTKEYS, new Position(5, 200, 1.0f, true, false));
        POSITIONS.put(Element.AC_OFF, new Position(5, 150, 1.0f, false, false));
        POSITIONS.put(Element.ARMOR, new Position(5, 95, 1.0f, true, true));
    }

    private HudLayout() {
    }

    private static int place(int anchor, int offset, int screen, int size) {
        return switch (anchor) {
            case 2 -> screen - offset - size;
            case 1 -> Math.round((float)(screen - size) / 2.0f) + offset;
            default -> offset;
        };
    }

    private static int anchor(int pos, int size, int screen) {
        float center = (float)pos + (float)size / 2.0f;
        if (Math.abs(center - (float)screen / 2.0f) < 1.5f) {
            return 1;
        }
        return center > (float)screen / 2.0f ? 2 : 0;
    }

    private static int offset(int anchor, int pos, int screen, int size) {
        return switch (anchor) {
            case 2 -> screen - pos - size;
            case 1 -> pos - Math.round((float)(screen - size) / 2.0f);
            default -> pos;
        };
    }

    public static Box box(Element element, int screenWidth, int screenHeight, int baseWidth, int baseHeight) {
        Position p = POSITIONS.get((Object)element);
        int w = Math.max(1, Math.round((float)baseWidth * p.scale));
        int h = Math.max(1, Math.round((float)baseHeight * p.scale));
        int x = HudLayout.place(p.anchorX, p.x, screenWidth, w);
        int y = HudLayout.place(p.anchorY, p.y, screenHeight, h);
        return new Box(Math.max(0, Math.min(screenWidth - w, x)), Math.max(0, Math.min(screenHeight - h, y)), w, h, p.scale);
    }

    public static void move(Element element, int x, int y, int screenWidth, int screenHeight, int w, int h) {
        Position p = POSITIONS.get((Object)element);
        x = Math.max(0, Math.min(screenWidth - w, x));
        y = Math.max(0, Math.min(screenHeight - h, y));
        p.anchorX = HudLayout.anchor(x, w, screenWidth);
        p.anchorY = HudLayout.anchor(y, h, screenHeight);
        p.x = HudLayout.offset(p.anchorX, x, screenWidth, w);
        p.y = HudLayout.offset(p.anchorY, y, screenHeight, h);
    }

    public static boolean hidden(Element element){return POSITIONS.get(element).hidden;}
    public static void toggleHidden(Element element){var p=POSITIONS.get(element);p.hidden=!p.hidden;}
    public static void resetElement(Element element){var p=POSITIONS.get(element);p.x=5;p.y=5;p.scale=1;p.anchorX=0;p.anchorY=0;p.hidden=false;}

    public static float scaleOf(Element element) {
        return HudLayout.POSITIONS.get((Object)((Object)element)).scale;
    }

    public static void scale(Element element, float amount) {
        Position p = POSITIONS.get((Object)element);
        p.scale = Math.max(0.55f, Math.min(2.0f, (float)Math.round((p.scale + amount) * 20.0f) / 20.0f));
    }

    public static void resetScale(Element element) {
        HudLayout.POSITIONS.get((Object)((Object)element)).scale = 1.0f;
    }

    public static void load() {
        if (!Files.exists(FILE, new LinkOption[0])) {
            return;
        }
        try {
            HudLayout.fromJson(JsonParser.parseString((String)Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void fromJson(JsonObject root) {
        for (Element e : Element.values()) {
            if (!root.has(e.name())) continue;
            try {
                JsonObject o = root.getAsJsonObject(e.name());
                Position p = POSITIONS.get((Object)e);
                p.x = o.get("x").getAsInt();
                p.y = o.get("y").getAsInt();
                p.hidden = o.has("hidden") && o.get("hidden").getAsBoolean();
                p.scale = Math.max(0.55f, Math.min(2.0f, o.get("scale").getAsFloat()));
                p.anchorX = o.has("anchorX") ? o.get("anchorX").getAsInt() : (p.anchorX = o.has("right") && o.get("right").getAsBoolean() ? 2 : 0);
                p.anchorY = o.has("anchorY") ? o.get("anchorY").getAsInt() : (o.has("bottom") && o.get("bottom").getAsBoolean() ? 2 : 0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Element e : Element.values()) {
            Position p = POSITIONS.get((Object)e);
            JsonObject o = new JsonObject();
            o.addProperty("x", (Number)p.x);
            o.addProperty("y", (Number)p.y);
            o.addProperty("hidden", p.hidden);
            o.addProperty("scale", (Number)Float.valueOf(p.scale));
            o.addProperty("anchorX", (Number)p.anchorX);
            o.addProperty("anchorY", (Number)p.anchorY);
            root.add(e.name(), (JsonElement)o);
        }
        return root;
    }

    public static void save() {
        try {
            JsonObject root = HudLayout.toJson();
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((JsonElement)root), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static {
        HudLayout.defaults();
    }

    public static enum Element {
        WATERMARK,
        FPS,
        BPS,
        TPS,
        XYZ,
        RADAR,
        MODULE_LIST,
        STAFF_LIST,
        KEYSTROKES,
        INVENTORY,
        REGION,
        SPOTIFY,
        STAFF_LIVE,
        TOGIS,
        HOTKEYS,
        AC_OFF,
        ARMOR;

    }

    private static final class Position {
        int x;
        int y;
        float scale;
        int anchorX;
        int anchorY;
        boolean hidden;

        Position(int x, int y, float scale, boolean right, boolean bottom) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.anchorX = right ? 2 : 0;
            this.anchorY = bottom ? 2 : 0;
        }
    }

    public record Box(int x, int y, int width, int height, float scale) {
        public boolean contains(double px, double py) {
            return px >= (double)this.x && py >= (double)this.y && px < (double)(this.x + this.width) && py < (double)(this.y + this.height);
        }
    }
}

