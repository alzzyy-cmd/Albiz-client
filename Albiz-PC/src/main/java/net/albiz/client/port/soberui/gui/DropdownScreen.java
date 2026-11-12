// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.albiz.client.port.soberui.ConfigSlots;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Avatars;
import net.albiz.client.port.soberui.gui.BlockPickerScreen;
import net.albiz.client.port.soberui.gui.ColorWheel;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.ExactImage;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.ModuleMenu;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.albiz.client.port.soberui.gui.SdfText;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.TypedText;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.module.Status;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.albiz.client.port.soberui.modules.Configs;
import net.albiz.client.port.soberui.modules.Hud;
import net.albiz.client.port.soberui.modules.SkinChange;
import net.albiz.client.port.soberui.render.Color;
import net.albiz.client.port.soberui.setting.BlockListSetting;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.ColorSetting;
import net.albiz.client.port.soberui.setting.EnumSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.ItemSetting;
import net.albiz.client.port.soberui.setting.KeySetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.setting.StringSetting;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class DropdownScreen
extends class_437
implements ModuleMenu {
    private static final int CANVAS_W = 1386;
    private static final int CANVAS_H = 780;
    private static final int PANEL_W = 166;
    private static final int GAP = 12;
    private static final int HEADER_H = 34;
    private static final int BAR_H = 34;
    private static final int ROW_H = 20;
    private static final int ROW_STEP = 21;
    private static final int PAD = 6;
    private static final int PICKER_H = 150;
    private static final int BAR_W = 436;
    private static final int PILL_H = 22;
    private static final int TOP_SPACE = 76;
    private static final int POPUP_H = 254;
    private static final int GROUP_H = 13;
    private static final int TILE = 24;
    private static final int TILE_GAP = 3;
    private static final int LINK = -12877066;
    private static final ExactImage LOGO = new ExactImage("textures/brand/orbit_glow.png");
    private static final ExactImage WORDMARK = new ExactImage("textures/brand/wordmark.png");
    private static final String[] BOTTOM = new String[]{"CONFIGS", "HUD", "SETTINGS"};
    private static final int PANEL_BASE = -401270497;
    private static final int CARD_BASE = 0xBFFFFFF;
    private static final int CARD_HOVER_BASE = 0x16FFFFFF;
    private static final int SUB_BASE = 0x8FFFFFF;
    private static final int SUB_HOVER_BASE = 0x12FFFFFF;
    private static final int BORDER_BASE = 0x38FFFFFF;
    private static final int TEXT_BASE = -1184016;
    private static final int MUTED_BASE = -6052178;
    private static final int VALUE_BASE = 0x14FFFFFF;
    private static int PANEL = -401270497;
    private static int CARD = 0xBFFFFFF;
    private static int CARD_HOVER = 0x16FFFFFF;
    private static int SUB = 0x8FFFFFF;
    private static int SUB_HOVER = 0x12FFFFFF;
    private static int BORDER = 0x38FFFFFF;
    private static int TEXT = -1184016;
    private static int MUTED = -6052178;
    private static int ACCENT = -2762274;
    private static int VALUE = 0x14FFFFFF;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soberclient-dropdown.json");
    private static final EnumMap<Category, Panel> PANELS = new EnumMap(Category.class);
    private static final List<Panel> ORDER = new ArrayList<Panel>();
    private static final List<Panel> HOME = new ArrayList<Panel>();
    private static final Set<Module> EXPANDED = new HashSet<Module>();
    private static final Set<ColorSetting> OPEN_COLORS = new HashSet<ColorSetting>();
    private final Map<Object, Float> animations = new HashMap<Object, Float>();
    private final Map<Key, Timed> timed = new HashMap<Key, Timed>();
    private Supplier<class_8685> skin;
    private long frame = System.nanoTime();
    private final Map<Object, Long> pressedAt = new HashMap<Object, Long>();
    private final List<Hit> hits = new ArrayList<Hit>();
    private String search = "";
    private final TypedText searchText = new TypedText();
    private boolean searching;
    private Panel draggingPanel;
    private float grabX;
    private float grabY;
    private double pressX;
    private double pressY;
    private boolean panelMoved;
    private Hit dragging;
    private Setting<?> editing;
    private String editBuffer = "";
    private long invalidUntil;
    private Setting<?> invalidSetting;
    private Module binding;
    private KeySetting keyBinding;
    private long swallowUntil;
    private long lastFrame = System.nanoTime();
    private float delta;
    private float openProgress;
    private final long openedAt = System.nanoTime();
    private float alpha;
    private boolean closing;
    private float renderScale = 1.0f;
    private float scaleShown = -1.0f;
    private Object hoveredKey;
    private long hoverStart;
    private String tooltip;
    private boolean configOpen;
    private float popupOpen;
    private int popupX;
    private int popupY;
    private boolean renaming;
    private String renameBuffer = "";
    private static final int GLASS_VEIL = -2146169816;

    public static void resetLayout() {
        float start = DropdownScreen.layoutLeft(HOME.size());
        for (int i = 0; i < HOME.size(); ++i) {
            Panel panel = HOME.get(i);
            panel.x = start + (float)(i * 178);
            panel.y = 76.0f;
            panel.collapsed = false;
            panel.scrollTarget = 0.0f;
            panel.scroll = 0.0f;
        }
        EXPANDED.clear();
        OPEN_COLORS.clear();
    }

    private static float layoutLeft(int count) {
        return (float)(1386 - count * 178 + 12) / 2.0f;
    }

    private static float layoutWidth(int count) {
        return count * 178 - 12;
    }

    public DropdownScreen() {
        super((class_2561)class_2561.method_43470((String)"Albız"));
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25432() {
        net.albiz.client.config.ModuleConfigStore.getInstance().saveIfChanged();
        SoberClient.CONFIG.save();
        DropdownScreen.save();
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        if (this.editing != null) {
            this.commitEdit();
        }
        if (this.renaming) {
            this.commitRename();
        }
        this.binding = null;
        this.closing = true;
        UiSounds.close();
    }

    private static ClickGui options() {
        return SoberClient.MODULES.get(ClickGui.class);
    }

    private static Configs configs() {
        return SoberClient.MODULES.get(Configs.class);
    }

    private static int withAlpha(int color, int a) {
        return (Math.max(0, Math.min(255, a)) & 0xFF) << 24 | color & 0xFFFFFF;
    }

    private static int lerp(int a, int b, float t) {
        int aa = a >>> 24;
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int ba = b >>> 24;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)aa + (float)(ba - aa) * t) << 24 | (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    private int col(int color) {
        return DropdownScreen.withAlpha(color, Math.round((float)(color >>> 24) * this.alpha));
    }

    private static void palette() {
        PANEL = Theme.fill(-401270497);
        CARD = Theme.tint(0xBFFFFFF);
        CARD_HOVER = Theme.tint(0x16FFFFFF);
        SUB = Theme.tint(0x8FFFFFF);
        SUB_HOVER = Theme.tint(0x12FFFFFF);
        BORDER = Theme.tint(0x38FFFFFF);
        TEXT = Theme.textTint(-1184016);
        MUTED = Theme.textTint(Theme.liquidGlass() ? -3749167 : -6052178);
        ACCENT = Theme.accent();
        VALUE = Theme.tint(0x14FFFFFF);
        if (Theme.liquidGlass()) {
            CARD = 0x1CFFFFFF;
            CARD_HOVER = 0x30FFFFFF;
            SUB = 0x12FFFFFF;
            SUB_HOVER = 0x24FFFFFF;
            BORDER = 0x40FFFFFF;
        }
    }

    private int panel(int color) {
        return DropdownScreen.withAlpha(color, Math.round((float)((color >>> 24) * DropdownScreen.options().glassOpacity.get()) / 100.0f));
    }

    private float approach(float current, float target, float speed) {
        float value = current + (target - current) * (1.0f - (float)Math.exp(-this.delta * speed * Theme.animationSpeed()));
        return Math.abs(value - target) < 0.002f ? target : value;
    }

    private static int millis(String tag) {
        return switch (tag) {
            case "open", "bool", "switch" -> 200;
            case "expand", "picker" -> 250;
            default -> 150;
        };
    }

    private static boolean eased(String tag) {
        return switch (tag) {
            case "open", "expand", "picker", "match" -> true;
            default -> false;
        };
    }

    private float anim(String tag, Object target, boolean on) {
        Key key = new Key(tag, target);
        float goal = on ? 1.0f : 0.0f;
        Timed timed = this.timed.get(key);
        if (timed == null) {
            timed = new Timed(goal);
            this.timed.put(key, timed);
        } else if (timed.target != goal) {
            timed.from = timed.at(this.frame);
            timed.target = goal;
            timed.start = this.frame;
            timed.duration = Math.round((float)((long)DropdownScreen.millis(tag) * 1000000L) / Math.max(0.05f, Theme.animationSpeed()));
            timed.eased = DropdownScreen.eased(tag);
        }
        return timed.at(this.frame);
    }

    private float value(String tag, Object target) {
        Timed timed = this.timed.get(new Key(tag, target));
        return timed == null ? 0.0f : timed.at(this.frame);
    }

    private float press(Object key) {
        Long time = this.pressedAt.get(key);
        return time == null ? 0.0f : Math.max(0.0f, 1.0f - (float)(System.nanoTime() - time) / 2.1E8f);
    }

    private float fitScale() {
        return Math.min((float)this.field_22789 / 1386.0f, (float)this.field_22790 / 780.0f);
    }

    private float baseScale() {
        if (this.scaleShown < 0.0f) {
            this.scaleShown = DropdownScreen.options().guiScale.get().intValue();
        }
        return Math.max(0.2f, this.fitScale() * this.scaleShown / 100.0f);
    }

    private boolean previewingScale() {
        return this.dragging != null && this.dragging.target() == DropdownScreen.options().guiScale;
    }

    private void drawScalePreview(class_332 context) {
        float shown = this.anim("scalepreview", DropdownScreen.options().guiScale, this.previewingScale());
        if (shown <= 0.01f) {
            return;
        }
        int percent = DropdownScreen.options().guiScale.get();
        float target = Math.max(0.2f, this.fitScale() * (float)percent / 100.0f);
        float w = 1386.0f * target;
        float h = 780.0f * target;
        float x = ((float)this.field_22789 - w) / 2.0f;
        float y = ((float)this.field_22790 - h) / 2.0f;
        float previous = this.alpha;
        this.alpha = previous * shown;
        this.rect(context, x, y, w, h, 8, DropdownScreen.withAlpha(ACCENT, 20));
        int dash = 7;
        int gap = 5;
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + w);
        int bottom = Math.round(y + h);
        int line = this.col(DropdownScreen.withAlpha(ACCENT, 230));
        for (int px = left; px < right; px += dash + gap) {
            context.method_25294(px, top, Math.min(right, px + dash), top + 1, line);
            context.method_25294(px, bottom - 1, Math.min(right, px + dash), bottom, line);
        }
        for (int py = top; py < bottom; py += dash + gap) {
            context.method_25294(left, py, left + 1, Math.min(bottom, py + dash), line);
            context.method_25294(right - 1, py, right, Math.min(bottom, py + dash), line);
        }
        double factor = this.field_22787.method_22683().method_4495();
        String label = percent + "%";
        String size = Math.round((double)w * factor) + " \u00d7 " + Math.round((double)h * factor);
        float big = 30.0f;
        float small = 9.0f;
        float labelW = CrispText.width(label, DropdownScreen.menuFont(true), DropdownScreen.menuEm(big), 0.0f);
        float sizeW = CrispText.width(size, DropdownScreen.menuFont(false), DropdownScreen.menuEm(small), 0.0f);
        float boxW = Math.max(labelW, sizeW) + 36.0f;
        float boxH = 58.0f;
        float cx = (float)this.field_22789 / 2.0f;
        float cy = (float)this.field_22790 / 2.0f;
        this.rect(context, cx - boxW / 2.0f, cy - boxH / 2.0f, boxW, boxH, 12, DropdownScreen.withAlpha(1316380, 216));
        CrispText.draw(context, label, DropdownScreen.menuFont(true), cx - labelW / 2.0f, cy + 6.0f, DropdownScreen.menuEm(big), 0.0f, this.col(-1));
        CrispText.draw(context, size, DropdownScreen.menuFont(false), cx - sizeW / 2.0f, cy + 20.0f, DropdownScreen.menuEm(small), 0.0f, this.col(DropdownScreen.withAlpha(0xFFFFFF, 168)));
        CrispText.draw(context, "New size", DropdownScreen.menuFont(false), left + 8, top + 14, DropdownScreen.menuEm(small), 0.0f, this.col(DropdownScreen.withAlpha(ACCENT, 240)));
        this.alpha = previous;
    }

    private float viewLeft() {
        return 693.0f - (float)this.field_22789 / (2.0f * this.baseScale());
    }

    private float viewRight() {
        return 693.0f + (float)this.field_22789 / (2.0f * this.baseScale());
    }

    private float viewTop() {
        return 390.0f - (float)this.field_22790 / (2.0f * this.baseScale());
    }

    private float viewBottom() {
        return 390.0f + (float)this.field_22790 / (2.0f * this.baseScale());
    }

    private double uiX(double x) {
        return (x - (double)this.field_22789 / 2.0) / (double)this.renderScale + 693.0;
    }

    private double uiY(double y) {
        return (y - (double)this.field_22790 / 2.0) / (double)this.renderScale + 390.0;
    }

    private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.col(color));
    }

    private void glass(class_332 context, int x, int y, int w, int h, int r, int color) {
        if (Theme.liquidGlass()) {
            Smooth.liquid(context, x, y, w, h, r, this.alpha, true);
            Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(-2146169816, this.alpha));
        } else {
            Smooth.rect(context, x, y, w, h, r, this.col(color));
        }
    }

    private void text(class_332 context, String value, int x, int y, int color) {
        if (Theme.menuCrisp()) {
            ScreenStyle.crisp(context, value, x, y, 1.0f, this.col(color), false);
        } else if (Theme.smoothFont(value)) {
            SdfText.draw(context, value, x, y, this.col(color));
        } else {
            context.method_51439(this.field_22793, Theme.text(value), x, y, this.col(color), false);
        }
    }

    private int textWidth(String value) {
        if (Theme.menuCrisp()) {
            return (int)Math.ceil(ScreenStyle.crispWidth(value, 1.0f, false));
        }
        return Theme.smoothFont(value) ? SdfText.width(value) : this.field_22793.method_27525((class_5348)Theme.text(value));
    }

    private String fit(String value, int maxWidth) {
        if (maxWidth < 12) {
            return "";
        }
        if (this.textWidth(value) <= maxWidth) {
            return value;
        }
        while (!value.isEmpty() && this.textWidth(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private String tail(String value, int maxWidth) {
        while (!value.isEmpty() && this.textWidth(value) > maxWidth) {
            value = value.substring(1);
        }
        return value;
    }

    private void icon(class_332 context, Category item, int x, int y, int size) {
        String name = switch (item) {
            default -> throw new MatchException(null, null);
            case Category.BASE_FINDING -> "boxes";
            case Category.DONUT -> "crown";
            case Category.COMBAT -> "flame";
            case Category.VISUALS, Category.RENDER -> "sparkles";
            case Category.CLIENT -> "box";
            case Category.CONFIG, Category.MACRO -> "config";
            case Category.MOVEMENT -> "lock";
        };
        context.method_25293(MenuKit.ICON_PIPELINE, class_2960.method_60655((String)"albiz", (String)("textures/gui/" + name + ".png")), x, y, 0.0f, 0.0f, size, size, 128, 128, 128, 128, this.col(-1));
    }

    private void brand(class_332 context, int x, int y, int size) {
        Smooth.brand(context, x, y, size, this.renderScale, this.col(-1));
    }

    private void hit(Kind kind, float x, float y, float w, float h, Object target, int index) {
        this.hits.add(new Hit(kind, x, y, w, h, target, index));
    }

    private void clippedHit(Kind kind, float x, float y, float w, float h, Object target, int index, float top, float bottom) {
        float y1 = Math.max(y, top);
        float y2 = Math.min(y + h, bottom);
        if (y2 > y1) {
            this.hits.add(new Hit(kind, x, y1, w, y2 - y1, target, index));
        }
    }

    private Hit hitAt(double mx, double my) {
        for (int i = this.hits.size() - 1; i >= 0; --i) {
            if (!this.hits.get(i).contains(mx, my)) continue;
            return this.hits.get(i);
        }
        return null;
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && my >= (double)y && mx < (double)(x + w) && my < (double)(y + h);
    }

    private float sinceOpen() {
        return (float)(System.nanoTime() - this.openedAt) / 1.0E9f;
    }

    private static float quint(float p) {
        p = Math.max(0.0f, Math.min(1.0f, p));
        float inverse = 1.0f - p;
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    private void chevron(class_332 context, float cx, float cy, float size, float rotation, int color) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        matrices.rotate(rotation);
        Smooth.triangle(context, 0.0f, 0.0f, size, this.col(color));
        matrices.popMatrix();
    }

    private int pillWidth(String label) {
        return Math.round(CrispText.width(label, DropdownScreen.menuFont(true), DropdownScreen.menuEm(8.0f), 0.5f)) + 26;
    }

    private float topY() {
        return this.viewTop() + 16.0f;
    }

    private float bottomY() {
        return this.viewBottom() - 22.0f - 12.0f;
    }

    private String[] bottomLabels() {
        return new String[]{BOTTOM[0], BOTTOM[1], "LAYOUT", BOTTOM[2]};
    }

    private int bottomWidth() {
        String[] labels;
        int w = 0;
        for (String label : labels = this.bottomLabels()) {
            w += this.pillWidth(label);
        }
        return w + (labels.length - 1) * 6;
    }

    private float barLeft() {
        return DropdownScreen.layoutLeft(PANELS.size());
    }

    private float barWidth() {
        return DropdownScreen.layoutWidth(PANELS.size());
    }

    private boolean inBars(double mx, double my) {
        if (DropdownScreen.inside(mx, my, this.barLeft(), this.topY(), this.barWidth(), 34.0f)) {
            return true;
        }
        return DropdownScreen.inside(mx, my, 693.0f - (float)this.bottomWidth() / 2.0f, this.bottomY(), this.bottomWidth(), 22.0f);
    }

    private void spaced(class_332 context, String value, float x, float baseline, float em, int color) {
        CrispText.draw(context, value, DropdownScreen.menuFont(true), x, baseline, DropdownScreen.menuEm(em), 0.5f, this.col(color));
    }

    private static String menuFont(boolean heavy) {
        return Theme.crispFile(heavy);
    }

    private static float menuEm(float em) {
        return em * Theme.crispScale();
    }

    private void zeroPanel(class_332 context, float x, float y, float w, float h, int r, int fill, int border) {
        if (Theme.liquidGlass()) {
            Smooth.liquid(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.alpha, true);
            Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, MenuKit.alpha(-2146169816, this.alpha));
            return;
        }
        this.rect(context, x, y, w, h, r, fill);
        Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.col(border));
    }

    private static int groupCount(Module module) {
        int count = 0;
        String group = null;
        for (Setting<?> setting : DropdownScreen.visibleSettings(module)) {
            if (setting.getGroup().equals(group)) continue;
            group = setting.getGroup();
            ++count;
        }
        return count;
    }

    private List<Module> all(Category category) {
        ArrayList<Module> list = new ArrayList<Module>();
        for (Module module : SoberClient.MODULES.all()) {
            if (module.category != category) continue;
            list.add(module);
        }
        return list;
    }

    private boolean matches(Module module) {
        return this.search.isEmpty() || module.name.toLowerCase(Locale.ROOT).contains(this.search.toLowerCase(Locale.ROOT));
    }

    private List<Module> modules(Category category) {
        ArrayList<Module> list = new ArrayList<Module>();
        for (Module module : this.all(category)) {
            if (!this.matches(module)) continue;
            list.add(module);
        }
        return list;
    }

    private static List<Setting<?>> visibleSettings(Module module) {
        ArrayList list = new ArrayList();
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            list.add(setting);
        }
        return list;
    }

    private float settingHeight(Setting<?> setting) {
        if (setting instanceof IntSetting) {
            return 28.0f;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            return 20.0f + 150.0f * this.value("picker", color);
        }
        return 20.0f;
    }

    private static int tileColumns() {
        return Math.max(1, 5);
    }

    private static int tileRows(int count) {
        return (count + DropdownScreen.tileColumns() - 1) / DropdownScreen.tileColumns();
    }

    private float settingsHeight(Module module) {
        int presets;
        Status status;
        int rows;
        float h = 4.0f;
        if (module instanceof Status && (rows = (status = (Status)((Object)module)).statusRows().size()) > 0) {
            h += (float)(16 + rows * 30 + 3);
        }
        if ((presets = MenuKit.presetCount(module)) > 0 && MenuKit.presetIcons(module)) {
            h += (float)(15 + DropdownScreen.tileRows(presets) * 27 + 3);
        } else if (presets > 0) {
            h += (float)(15 + presets * 20 + 3);
        }
        for (Setting<?> setting : DropdownScreen.visibleSettings(module)) {
            h += this.settingHeight(setting);
        }
        h += (float)(13 * DropdownScreen.groupCount(module));
        if (module.bindable()) {
            h += 20.0f;
        }
        return h + 2.0f;
    }

    private float moduleHeight(Module module) {
        return 21.0f + (this.settingsHeight(module) + 4.0f) * this.value("expand", module);
    }

    private float rowHeight(Module module) {
        return this.moduleHeight(module) * this.value("match", module);
    }

    private void clampPanel(Panel panel) {
        panel.x = Math.clamp(panel.x, this.viewLeft() + 2.0f, Math.max(this.viewLeft() + 2.0f, this.viewRight() - 166.0f - 2.0f));
        panel.y = Math.clamp(panel.y, this.viewTop() + 76.0f, Math.max(this.viewTop() + 76.0f, this.viewBottom() - 34.0f - 22.0f - 30.0f));
    }

    private void front(Panel panel) {
        ORDER.remove(panel);
        ORDER.add(panel);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        if (this.field_22787.field_1687 == null) {
            super.method_25420(context, mouseX, mouseY, deltaTicks);
            return;
        }
        if (DropdownScreen.options().blur.get().booleanValue()) {
            context.method_71278();
        }
        int dim = Math.round((float)(255 * DropdownScreen.options().backgroundDim.get()) / 100.0f * this.alpha * (Theme.liquidGlass() ? 0.45f : 1.0f));
        context.method_25294(0, 0, this.field_22789, this.field_22790, DropdownScreen.withAlpha(526861, dim));
        if (Theme.liquidGlass()) {
            float haze = (float)DropdownScreen.options().glassTint.get().intValue() / 100.0f * this.alpha;
            int tint = Theme.glassTint();
            context.method_25296(0, 0, this.field_22789, this.field_22790, DropdownScreen.withAlpha(tint, Math.round(80.0f * haze)), DropdownScreen.withAlpha(656912, Math.round(112.0f * haze)));
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        float scale;
        DropdownScreen.palette();
        long now = System.nanoTime();
        this.delta = Math.min(0.06f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        this.frame = now;
        this.openProgress = this.approach(this.openProgress, this.closing ? 0.0f : 1.0f, this.closing ? 16.0f : 12.0f);
        if (this.closing && this.openProgress < 0.025f) {
            this.field_22787.method_1507(null);
            return;
        }
        this.alpha = 1.0f - (float)Math.pow(1.0f - this.openProgress, 3.0);
        if (this.scaleShown < 0.0f) {
            this.scaleShown = DropdownScreen.options().guiScale.get().intValue();
        }
        if (!this.previewingScale()) {
            this.scaleShown = this.approach(this.scaleShown, DropdownScreen.options().guiScale.get().intValue(), 11.0f);
        }
        this.renderScale = scale = this.baseScale();
        this.hits.clear();
        this.tooltip = null;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f);
        matrices.scale(scale, scale);
        matrices.translate(-693.0f, -390.0f);
        int mx = (int)Math.round(this.uiX(mouseX));
        int my = (int)Math.round(this.uiY(mouseY));
        Panel top = this.topPanel(mx, my);
        boolean first = true;
        for (Panel panel : new ArrayList<Panel>(ORDER)) {
            this.clampPanel(panel);
            if (!first) {
                context.method_71048();
            }
            first = false;
            boolean mouse = panel == top || this.draggingPanel == panel;
            float shown = DropdownScreen.quint((this.sinceOpen() - (float)panel.category.ordinal() * 0.04f) / 0.5f);
            float previous = this.alpha;
            this.alpha = previous * shown;
            matrices.pushMatrix();
            matrices.translate(0.0f, 10.0f * (1.0f - shown));
            this.drawPanel(context, panel, mouse ? mx : Integer.MIN_VALUE, mouse ? my : Integer.MIN_VALUE);
            matrices.popMatrix();
            this.alpha = previous;
        }
        context.method_71048();
        this.drawTopBar(context, mx, my);
        this.drawBottomBar(context, mx, my);
        this.drawConfigPopup(context, mx, my);
        matrices.popMatrix();
        context.method_71048();
        this.drawScalePreview(context);
        context.method_71048();
        Notifications.render(context, true);
        context.method_71048();
        this.drawTooltip(context, mouseX, mouseY);
    }

    private Panel topPanel(double mx, double my) {
        if (this.inBars(mx, my)) {
            return null;
        }
        if (this.popupOpen > 0.5f && DropdownScreen.inside(mx, my, this.popupX, this.popupY, 436.0f, 254.0f)) {
            return null;
        }
        for (int i = ORDER.size() - 1; i >= 0; --i) {
            Panel panel = ORDER.get(i);
            if (!DropdownScreen.inside(mx, my, panel.x, panel.y, 166.0f, Math.max(34.0f, panel.height))) continue;
            return panel;
        }
        return null;
    }

    private void drawPanel(class_332 context, Panel panel, int mouseX, int mouseY) {
        float gone;
        List<Module> modules = this.all(panel.category);
        int matching = 0;
        for (Module module : modules) {
            boolean match = this.matches(module);
            if (match) {
                ++matching;
            }
            this.anim("match", module, match);
            boolean expanded = EXPANDED.contains(module);
            this.anim("expand", module, expanded);
            if (!expanded && this.value("expand", module) < 0.01f) continue;
            for (Setting setting : module.getSettings()) {
                if (!(setting instanceof ColorSetting)) continue;
                ColorSetting color = (ColorSetting)setting;
                this.anim("picker", color, OPEN_COLORS.contains(color));
            }
        }
        int x = Math.round(panel.x);
        int y = Math.round(panel.y);
        float open = this.anim("open", (Object)panel.category, !panel.collapsed);
        float rows = 0.0f;
        for (Module module : modules) {
            rows += this.rowHeight(module);
        }
        float content = 6.0f + rows;
        if (matching == 0) {
            content = Math.max(28.0f, content);
        }
        float f = Math.max(40.0f, this.bottomY() - 12.0f - (float)(y + 34));
        panel.content = content;
        panel.view = Math.min(content, f);
        panel.scrollTarget = Math.clamp(panel.scrollTarget, 0.0f, Math.max(0.0f, content - panel.view));
        panel.scroll = this.approach(panel.scroll, panel.scrollTarget, 16.0f);
        float body = panel.view * open;
        int h = 34 + Math.round(body);
        panel.height = h;
        this.hit(Kind.PANEL, x, y, 166.0f, h, panel, 0);
        this.zeroPanel(context, x, y, 166.0f, h, 6, PANEL, 0x1AFFFFFF);
        boolean overHeader = DropdownScreen.inside(mouseX, mouseY, x, y, 166.0f, 34.0f);
        float headerHover = this.anim("header", (Object)panel.category, overHeader || this.draggingPanel == panel);
        if (headerHover > 0.01f) {
            this.rect(context, x + 4, y + 4, 158.0f, 26.0f, 4, DropdownScreen.withAlpha(0xFFFFFF, Math.round(12.0f * headerHover)));
        }
        this.icon(context, panel.category, x + 12, y + 11, 12);
        String title = this.fit(panel.category.title.toUpperCase(Locale.ROOT), 106);
        this.spaced(context, title, x + 32, (float)y + 20.5f, 9.2f, -1);
        float fold = Math.max(headerHover, 1.0f - open);
        if (fold > 0.01f) {
            this.chevron(context, x + 166 - 15, (float)y + 17.0f, 5.0f, (float)(1.5707963267948966 + 1.5707963267948966 * (double)open), DropdownScreen.withAlpha(DropdownScreen.lerp(MUTED, TEXT, headerHover), Math.round(255.0f * fold)));
        }
        this.hit(Kind.HEADER, x, y, 166.0f, 34.0f, panel, 0);
        if (overHeader) {
            this.hover((Object)panel.category, "Drag to move. Click to fold the panel in or out.");
        }
        if (body < 0.5f) {
            return;
        }
        int top = y + 34 - 2;
        int bottom = top + Math.round(body);
        context.method_44379(x, top, x + 166, bottom);
        float sy = (float)top - panel.scroll;
        float since = this.sinceOpen() - (float)panel.category.ordinal() * 0.04f - 0.08f;
        int row = 0;
        for (Module module : modules) {
            float mh = this.rowHeight(module);
            if (mh < 0.5f) continue;
            int clipTop = Math.max(top, Math.round(sy));
            int clipBottom = Math.min(bottom, Math.round(sy + mh));
            if (clipBottom > clipTop) {
                boolean clip;
                float shown = DropdownScreen.quint((since - (float)row * 0.012f) / 0.35f);
                float rowAlpha = this.alpha;
                this.alpha = rowAlpha * shown;
                boolean bl = clip = this.value("match", module) < 1.0f;
                if (clip) {
                    context.method_44379(x, clipTop, x + 166, clipBottom);
                }
                this.drawModule(context, module, x, sy, panel.content > panel.view + 1.0f ? 6 : 0, mouseX, mouseY, clip ? clipTop : top, clip ? clipBottom : bottom);
                if (clip) {
                    context.method_44380();
                }
                this.alpha = rowAlpha;
            }
            sy += mh;
            ++row;
        }
        if (matching == 0 && (gone = 1.0f - Math.min(1.0f, rows / 21.0f)) > 0.01f) {
            this.text(context, this.search.isEmpty() ? "Empty" : "No matches", x + 12, top + 9, DropdownScreen.withAlpha(MUTED, Math.round(255.0f * gone)));
        }
        context.method_44380();
        if (panel.content > panel.view + 1.0f) {
            float barH = Math.max(16.0f, body * panel.view / panel.content);
            float barY = (float)top + (body - barH) * (panel.scroll / Math.max(1.0f, panel.content - panel.view));
            this.rect(context, x + 166 - 7, barY + 2.0f, 2.5f, barH - 6.0f, 1, 0x3CFFFFFF);
        }
    }

    private String fitCrisp(String value, String font, float em, float max) {
        if (CrispText.width(value, font, em, 0.0f) <= max) {
            return value;
        }
        while (value.length() > 1 && CrispText.width(value + "...", font, em, 0.0f) > max) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private void drawModule(class_332 context, Module module, int px, float sy, int scrollbar, int mouseX, int mouseY, int top, int bottom) {
        float click;
        float expanded;
        float lit;
        int x = px + 6;
        int w = 154 - scrollbar;
        int y = Math.round(sy);
        boolean over = mouseX >= x && mouseX < x + w && (float)mouseY >= Math.max(sy, (float)top) && (float)mouseY < Math.min(sy + 20.0f, (float)bottom);
        boolean overMore = over && mouseX >= x + w - 22;
        float hover = this.anim("module", module, over);
        boolean link = module instanceof Configs;
        float enabled = this.anim("enabled", module, link || module.isActive());
        float fill = Math.max(enabled, (lit = Math.max(hover * 0.6f, (expanded = this.value("expand", module)) * 0.6f)) * 0.6f);
        if (fill > 0.01f && !link) {
            this.rect(context, x, y, w, 20.0f, 3, DropdownScreen.withAlpha(0xFFFFFF, Math.round(10.0f * lit)));
            if (enabled > 0.01f) {
                this.rect(context, x, y, w, 20.0f, 3, DropdownScreen.withAlpha(ACCENT, Math.round(82.0f * enabled)));
                Smooth.outline(context, x, y, w, 20, 3, this.col(DropdownScreen.withAlpha(ACCENT, Math.round(140.0f * enabled))));
            }
        }
        if ((click = this.press(module)) > 0.0f) {
            this.rect(context, x, y, w, 20.0f, 3, DropdownScreen.withAlpha(0xFFFFFF, Math.round(255.0f * click * 0.12f)));
        }
        String font = DropdownScreen.menuFont(enabled > 0.5f);
        int color = DropdownScreen.lerp(DropdownScreen.lerp(Theme.liquidGlass() ? -3091238 : -3946546, -1512722, hover), -1, enabled);
        String name = this.fitCrisp(Theme.label(module.name), font, DropdownScreen.menuEm(8.8f), w - (link ? 52 : 30));
        CrispText.draw(context, name, font, x + 7, (float)y + 13.4f, DropdownScreen.menuEm(8.8f), 0.1f, this.col(color));
        float more = Math.max(overMore ? 1.0f : 0.0f, expanded);
        float dotX = x + w - 12;
        float dotY = (float)y + 10.0f;
        if (link) {
            float lw = CrispText.width("OPEN", DropdownScreen.menuFont(true), DropdownScreen.menuEm(8.0f), 0.4f);
            CrispText.draw(context, "OPEN", DropdownScreen.menuFont(true), (float)(x + w - 8) - lw, (float)y + 13.2f, DropdownScreen.menuEm(8.0f), 0.4f, this.col(DropdownScreen.lerp(-12877066, -8539905, Math.max(hover, this.configOpen ? 1.0f : 0.0f))));
        } else if (more > 0.01f) {
            this.chevron(context, dotX, dotY, 4.0f, (float)(1.5707963267948966 + 1.5707963267948966 * (double)expanded), DropdownScreen.withAlpha(DropdownScreen.lerp(MUTED, TEXT, expanded), Math.round(255.0f * more)));
        } else {
            float r = 1.8f + 0.4f * enabled;
            Smooth.softDot(context, dotX, dotY, r, this.col(DropdownScreen.lerp(0x3CFFFFFF, -1, enabled)));
        }
        this.clippedHit(Kind.MODULE, x, sy, w, 20.0f, module, 0, top, bottom);
        if (!link) {
            this.clippedHit(Kind.MORE, x + w - 22, sy, 22.0f, 20.0f, module, 0, top, bottom);
        }
        if (over) {
            this.hover(module, module.description);
        }
        if (expanded > 0.01f) {
            this.drawModuleSettings(context, module, px, sy + 21.0f, expanded, mouseX, mouseY, top, bottom);
        }
    }

    private void drawModuleSettings(class_332 context, Module module, int px, float sy, float expanded, int mouseX, int mouseY, int top, int bottom) {
        int presets;
        Status status;
        List<Status.Row> rows;
        float full = this.settingsHeight(module);
        float area = full * expanded;
        int clipTop = Math.max(top, Math.round(sy));
        int clipBottom = Math.min(bottom, Math.round(sy + area));
        if (clipBottom <= clipTop) {
            return;
        }
        context.method_44379(px, clipTop, px + 166, clipBottom);
        this.rect(context, px + 6, sy, 154.0f, full, 4, 0x38000000);
        int sx = px + 6 + 4;
        int sw = 146;
        float y = sy + 4.0f;
        if (module.bindable()) {
            boolean over = mouseX >= sx && mouseX < sx + sw && (float)mouseY >= Math.max(y, (float)clipTop) && (float)mouseY < Math.min(y + 18.0f, (float)clipBottom);
            boolean active = this.binding == module;
            float h = this.anim("bind", module, over || active);
            this.rect(context, sx, y, sw, 18.0f, 4, DropdownScreen.lerp(CARD, CARD_HOVER, h));
            this.text(context, Theme.label("Keybind"), sx + 7, Math.round(y + 5.0f), MUTED);
            String value = this.fit(active ? "Press a key" : MenuKit.keyName(module.getKey()), sw - 70);
            int vw = this.textWidth(value) + 10;
            this.rect(context, sx + sw - 3 - vw, y + 3.0f, vw, 12.0f, 3, active ? DropdownScreen.withAlpha(ACCENT, 85) : VALUE);
            this.text(context, value, sx + sw - 3 - vw + 5, Math.round(y + 5.0f), active ? TEXT : MUTED);
            this.clippedHit(Kind.BIND, sx, y, sw, 18.0f, module, 0, clipTop, clipBottom);
            if (over) {
                this.hover("bind" + module.name, "Click, then press a key. Escape or Delete removes the bind.");
            }
            y += 20.0f;
        }
        if (module instanceof Status && !(rows = (status = (Status)((Object)module)).statusRows()).isEmpty()) {
            this.text(context, this.fit(status.statusTitle(), sw - 6), sx + 3, Math.round(y + 4.0f), MUTED);
            y += 16.0f;
            for (int i = 0; i < rows.size(); ++i) {
                Status.Row row = rows.get(i);
                boolean over = mouseX >= sx && mouseX < sx + sw && (float)mouseY >= Math.max(y, (float)clipTop) && (float)mouseY < Math.min(y + 28.0f, (float)clipBottom);
                float h = this.anim("status", module.name + i, over);
                this.rect(context, sx, y, sw, 28.0f, 4, DropdownScreen.lerp(CARD, CARD_HOVER, h));
                float click = this.press("status" + i);
                if (click > 0.0f) {
                    this.rect(context, sx, y, sw, 28.0f, 6, DropdownScreen.withAlpha(ACCENT, Math.round(255.0f * click * 0.22f)));
                }
                int bw = this.textWidth(row.badge()) + 10;
                this.rect(context, sx + sw - 5 - bw, y + 7.0f, bw, 14.0f, 7, row.badgeColor());
                this.text(context, row.badge(), sx + sw - 5 - bw + 5, Math.round(y + 10.0f), -1);
                this.text(context, this.fit(row.title(), sw - bw - 18), sx + 7, Math.round(y + 4.0f), TEXT);
                this.text(context, this.fit(row.detail(), sw - bw - 18), sx + 7, Math.round(y + 16.0f), DropdownScreen.lerp(MUTED, ACCENT, h));
                this.clippedHit(Kind.STATUS, sx, y, sw, 28.0f, row.link(), i, clipTop, clipBottom);
                if (over) {
                    this.hover("status" + module.name + i, status.statusDescription());
                }
                y += 30.0f;
            }
            y += 3.0f;
        }
        if ((presets = MenuKit.presetCount(module)) > 0 && MenuKit.presetIcons(module)) {
            this.text(context, "PRESETS", sx + 3, Math.round(y + 4.0f), MUTED);
            y += 15.0f;
            int columns = DropdownScreen.tileColumns();
            float left = (float)sx + (float)(sw - (columns * 27 - 3)) / 2.0f;
            for (int i = 0; i < presets; ++i) {
                float tx = left + (float)(i % columns * 27);
                float ty = y + (float)(i / columns * 27);
                if (ty + 24.0f < (float)clipTop || ty > (float)clipBottom) continue;
                boolean active = MenuKit.presetActive(module, i);
                boolean over = (float)mouseX >= tx && (float)mouseX < tx + 24.0f && (float)mouseY >= Math.max(ty, (float)clipTop) && (float)mouseY < Math.min(ty + 24.0f, (float)clipBottom);
                float h = this.anim("preset", module.name + i, over);
                this.rect(context, tx, ty, 24.0f, 24.0f, 5, DropdownScreen.lerp(CARD, CARD_HOVER, h));
                if (active) {
                    Smooth.outline(context, Math.round(tx), Math.round(ty), 24, 24, 5, this.col(ACCENT));
                }
                MenuKit.presetItem(context, MenuKit.presetIcon(module, i), tx + 12.0f, ty + 12.0f - h, 18.0f + 2.0f * h);
                this.clippedHit(Kind.PRESET, tx, ty, 24.0f, 24.0f, module, i, clipTop, clipBottom);
                if (!over) continue;
                this.hover("preset" + module.name + i, MenuKit.presetName(module, i));
            }
            y += (float)(DropdownScreen.tileRows(presets) * 27 + 3);
        } else if (presets > 0) {
            this.text(context, "PRESETS", sx + 3, Math.round(y + 4.0f), MUTED);
            y += 15.0f;
            for (int i = 0; i < presets; ++i) {
                boolean active = MenuKit.presetActive(module, i);
                boolean over = mouseX >= sx && mouseX < sx + sw && (float)mouseY >= Math.max(y, (float)clipTop) && (float)mouseY < Math.min(y + 18.0f, (float)clipBottom);
                float h = this.anim("preset", module.name + i, over);
                this.rect(context, sx, y, sw, 18.0f, 4, active ? ACCENT : DropdownScreen.lerp(CARD, CARD_HOVER, h));
                this.text(context, this.fit(MenuKit.presetName(module, i), sw - 16), sx + 7, Math.round(y + 5.0f), active ? -16182498 : TEXT);
                this.clippedHit(Kind.PRESET, sx, y, sw, 18.0f, module, i, clipTop, clipBottom);
                if (over && module instanceof Presets) {
                    Presets p = (Presets)((Object)module);
                    this.hover("preset" + module.name + i, p.presetDescription());
                }
                y += 20.0f;
            }
            y += 3.0f;
        }
        String group = null;
        for (Setting<?> setting : DropdownScreen.visibleSettings(module)) {
            float h;
            if (!setting.getGroup().equals(group)) {
                group = setting.getGroup();
                this.text(context, this.fit(group.toUpperCase(Locale.ROOT), sw - 8), sx + 3, Math.round(y + 3.0f), DropdownScreen.withAlpha(MUTED, 192));
                y += 13.0f;
            }
            if (y + (h = this.settingHeight(setting)) >= (float)clipTop && y <= (float)clipBottom) {
                this.drawSetting(context, setting, sx, y, sw, mouseX, mouseY, clipTop, clipBottom);
            }
            y += h;
        }
        context.method_44380();
    }

    private void drawSetting(class_332 context, Setting<?> setting, int sx, float sy, int sw, int mouseX, int mouseY, int top, int bottom) {
        float f;
        if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            f = this.value("picker", color);
        } else {
            f = 0.0f;
        }
        float picker = f;
        int base = setting instanceof IntSetting ? 26 : 18;
        int rh = base + Math.round(150.0f * picker);
        int y = Math.round(sy);
        boolean over = mouseX >= sx && mouseX < sx + sw && (float)mouseY >= Math.max(sy, (float)top) && (float)mouseY < Math.min(sy + (float)base, (float)bottom);
        float h = this.anim("setting", setting, over);
        this.rect(context, sx, y, sw, rh, 4, DropdownScreen.lerp(CARD, CARD_HOVER, h));
        float click = this.press(setting);
        if (click > 0.0f) {
            this.rect(context, sx, y, sw, base, 4, DropdownScreen.withAlpha(ACCENT, Math.round(255.0f * click * 0.2f)));
        }
        if (over) {
            this.hover(setting, setting.description);
        }
        if (setting instanceof BoolSetting) {
            BoolSetting bool = (BoolSetting)setting;
            this.text(context, this.fit(setting.name, sw - 40), sx + 7, y + 5, TEXT);
            float p = this.anim("bool", setting, (Boolean)bool.get());
            int tx = sx + sw - 26;
            float s = 1.0f - 0.06f * h;
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)(tx + 10), (float)(y + 9));
            matrices.scale(s, s);
            matrices.translate((float)(-(tx + 10)), (float)(-(y + 9)));
            this.rect(context, tx, y + 4, 20.0f, 10.0f, 5, BORDER);
            if (p > 0.0f) {
                context.method_44379(tx, y + 4, tx + Math.round(20.0f * p), y + 14);
                this.rect(context, tx, y + 4, 20.0f, 10.0f, 5, ACCENT);
                context.method_44380();
            }
            this.rect(context, (float)(tx + 2) + 10.0f * p, y + 6, 6.0f, 6.0f, 3, -1);
            matrices.popMatrix();
        } else if (setting instanceof IntSetting) {
            IntSetting integer = (IntSetting)setting;
            String value = integer.display();
            int vw = this.textWidth(value) + 10;
            this.text(context, this.fit(setting.name, sw - 18 - vw), sx + 7, y + 5, TEXT);
            this.rect(context, sx + sw - 3 - vw, y + 3, vw, 12.0f, 3, VALUE);
            this.text(context, value, sx + sw - 3 - vw + 5, y + 5, TEXT);
            int trackX = sx + 7;
            int trackW = sw - 14;
            this.rect(context, trackX, y + 19, trackW, 2.0f, 1, BORDER);
            float p = (float)((Integer)integer.get() - integer.min) / (float)Math.max(1, integer.max - integer.min);
            float shown = this.approach(this.animations.getOrDefault(new Key("slider", setting), Float.valueOf(p)).floatValue(), p, 20.0f);
            this.animations.put(new Key("slider", setting), Float.valueOf(shown));
            if (shown > 0.0f) {
                this.rect(context, trackX, y + 19, Math.max(2.0f, (float)trackW * shown), 2.0f, 1, ACCENT);
            }
            int knob = trackX + Math.round((float)(trackW - 8) * shown);
            this.rect(context, knob - 1, y + 15, 10.0f, 10.0f, 5, 0x33000000);
            this.rect(context, knob, y + 16, 8.0f, 8.0f, 4, -1);
        } else {
            Object value;
            boolean invalid;
            if (setting instanceof ColorSetting) {
                ColorSetting color = (ColorSetting)setting;
                boolean edit = this.editing == setting;
                boolean invalid2 = this.invalidSetting == setting && System.currentTimeMillis() < this.invalidUntil;
                Object hex = edit ? this.editBuffer + "|" : color.display();
                int hw = Math.max(this.textWidth("#DDDDDD|"), this.textWidth((String)hex)) + 10;
                int hx = sx + sw - 21 - hw;
                this.text(context, this.fit(setting.name, sw - 30 - hw), sx + 7, y + 5, TEXT);
                this.rect(context, hx, y + 3, hw, 12.0f, 3, invalid2 ? -11525341 : VALUE);
                this.text(context, (String)hex, hx + 5, y + 5, edit ? TEXT : MUTED);
                this.rect(context, sx + sw - 17, y + 2, 14.0f, 14.0f, 5, 0x40FFFFFF);
                this.rect(context, sx + sw - 16, y + 3, 12.0f, 12.0f, 4, ((Color)color.get()).packed() | 0xFF000000);
                if (picker > 0.01f) {
                    this.drawPicker(context, color, sx, y, sw, picker, top, bottom);
                }
                this.clippedHit(Kind.SETTING, sx, sy, sw, base, setting, 0, top, bottom);
                this.clippedHit(Kind.HEX, hx, sy + 2.0f, hw, 14.0f, setting, 0, top, bottom);
                return;
            }
            boolean edit = this.editing == setting;
            boolean bl = invalid = edit && System.currentTimeMillis() < this.invalidUntil;
            if (setting instanceof BlockListSetting) {
                BlockListSetting blocks = (BlockListSetting)setting;
                int count = ((Set)blocks.get()).size();
                value = count == 0 ? "Pick" : count + " blocks";
            } else if (edit) {
                value = this.tail(this.editBuffer, sw / 2 - 16) + "|";
            } else if (this.keyBinding == setting) {
                value = "Press a key";
            } else {
                String string;
                if (setting instanceof EnumSetting) {
                    EnumSetting e = (EnumSetting)setting;
                    string = e.display();
                } else {
                    string = setting.display();
                }
                value = string;
            }
            boolean dropdown = setting instanceof EnumSetting;
            int extra = dropdown ? 10 : 0;
            int bw = edit ? sw / 2 : Math.max(30, Math.min(sw / 2 + extra, this.textWidth((String)value) + 12 + extra));
            int bx = sx + sw - 3 - bw;
            this.text(context, this.fit(setting.name, sw - bw - 14), sx + 7, y + 5, TEXT);
            this.rect(context, bx, y + 3, bw, 12.0f, 3, invalid ? -11525341 : VALUE);
            this.text(context, this.fit((String)value, bw - 10 - extra), bx + 6, y + 5, TEXT);
            if (dropdown) {
                this.chevron(context, bx + bw - 7, y + 9, 4.0f, (float)Math.PI, MUTED);
            }
        }
        this.clippedHit(Kind.SETTING, sx, sy, sw, base, setting, 0, top, bottom);
    }

    private void drawPicker(class_332 context, ColorSetting color, int sx, int y, int sw, float open, int top, int bottom) {
        float[] hsv = color.hsv();
        int wheel = 96;
        int wx = sx + (sw - wheel) / 2;
        int wy = y + 22;
        ColorWheel.draw(context, wx, wy, wheel, hsv[2], this.alpha);
        double angle = (double)hsv[0] * Math.PI * 2.0;
        double radius = (double)hsv[1] * ((double)wheel / 2.0 - 1.0);
        int px = wx + wheel / 2 + (int)Math.round(Math.cos(angle) * radius);
        int py = wy + wheel / 2 + (int)Math.round(Math.sin(angle) * radius);
        Smooth.circle(context, px, py, 4, this.col(-1));
        Smooth.circle(context, px, py, 3, this.col(0xFF000000 | Color.fromHsv(hsv[0], hsv[1], 1.0f, 255).packed()));
        if (open > 0.95f) {
            this.clippedHit(Kind.WHEEL, wx, wy, wheel, wheel, color, 0, top, bottom);
        }
        int side = sx + 8;
        int sideW = sw - 16;
        for (int channel = 0; channel < 2; ++channel) {
            int barY = wy + wheel + 18 + channel * 24;
            int start = channel == 0 ? -16777216 : ((Color)color.get()).packed() & 0xFFFFFF;
            int end = 0xFF000000 | (channel == 0 ? Color.fromHsv(hsv[0], hsv[1], 1.0f, 255).packed() : ((Color)color.get()).packed());
            this.text(context, this.fit(channel == 0 ? "Brightness" : "Alpha", sideW), side, barY - 10, MUTED);
            Smooth.horizontalGradient(context, side + 3, barY, side + sideW - 3, barY + 5, this.col(start), this.col(end));
            Smooth.rect(context, side, barY, 5, 5, 2, this.col(start));
            Smooth.rect(context, side + sideW - 5, barY, 5, 5, 2, this.col(end));
            float value = channel == 0 ? hsv[2] : (float)((Color)color.get()).a() / 255.0f;
            Smooth.circle(context, side + Math.round((float)sideW * value), barY + 2, 5, this.col(0x55000000));
            Smooth.circle(context, side + Math.round((float)sideW * value), barY + 2, 4, this.col(TEXT));
            if (!(open > 0.95f)) continue;
            this.clippedHit(channel == 0 ? Kind.BRIGHTNESS : Kind.ALPHA, side - 4, barY - 4, sideW + 8, 13.0f, color, channel, top, bottom);
        }
    }

    private void pill(class_332 context, String label, int x, int y, int w, boolean active, int mouseX, int mouseY) {
        float click;
        boolean over = DropdownScreen.inside(mouseX, mouseY, x, y, w, 22.0f);
        float h = this.anim("pill", label, over);
        this.zeroPanel(context, x, y, w, 22.0f, 4, this.panel(PANEL), DropdownScreen.lerp(0x1AFFFFFF, 0x40FFFFFF, Math.max(h, active ? 1.0f : 0.0f)));
        if (h > 0.01f) {
            this.rect(context, x, y, w, 22.0f, 4, DropdownScreen.withAlpha(0xFFFFFF, Math.round(14.0f * h)));
        }
        if (active) {
            Smooth.outline(context, x, y, w, 22, 4, this.col(-1929379841));
        }
        if ((click = this.press("pill" + label)) > 0.0f) {
            this.rect(context, x, y, w, 22.0f, 4, DropdownScreen.withAlpha(0xFFFFFF, Math.round(255.0f * click * 0.14f)));
        }
        this.spaced(context, label, x + 13, (float)y + 11.0f + 3.0f, 8.0f, DropdownScreen.lerp(-922746881, -1, Math.max(h, active ? 1.0f : 0.0f)));
    }

    private void drawTopBar(class_332 context, int mouseX, int mouseY) {
        float resultsW;
        float y = this.topY();
        float left = this.barLeft();
        float total = this.barWidth();
        float lh = 18.0f;
        float lw = lh / LOGO.aspect();
        LOGO.draw(context, left - 2.0f, y + (34.0f - lh) / 2.0f, lw, this.col(-1));
        float wh = 25.0f;
        float ww = wh / WORDMARK.aspect();
        WORDMARK.draw(context, left + lw + 3.0f, y + (34.0f - wh) / 2.0f, ww, this.col(-1));
        String name = this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
        float chipW = Math.max(200.0f, 52.0f + CrispText.width(name, DropdownScreen.menuFont(true), DropdownScreen.menuEm(9.4f), 0.0f) + 20.0f);
        float chipX = left + total - chipW;
        this.zeroPanel(context, chipX, y, chipW, 34.0f, 5, this.panel(PANEL), 0x1AFFFFFF);
        try {
            if (this.skin == null) {
                this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
            }
            class_8685 textures = this.skin.get();
            SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
            if (textures != null && skins != null && skins.isActive()) {
                textures = skins.selfSkin(textures);
            }
            if (textures != null && this.alpha > 0.6f) {
                Avatars.draw(context, textures, Math.round(chipX + 8.0f), Math.round(y + 6.0f), 22);
            }
        }
        catch (RuntimeException textures) {
            // empty catch block
        }
        CrispText.draw(context, name, DropdownScreen.menuFont(true), chipX + 38.0f, y + 15.0f, DropdownScreen.menuEm(9.4f), 0.0f, this.col(-1));
        this.spaced(context, this.field_22787.field_1687 != null ? "IN GAME" : "IN MENU", chipX + 38.0f, y + 26.0f, 7.0f, -1593835521);
        float sx = Math.max(left + 166.0f + 12.0f + 120.0f, left + lw + ww + 16.0f);
        float sw = chipX - 28.0f - sx;
        boolean overSearch = DropdownScreen.inside(mouseX, mouseY, sx, y, sw, 34.0f);
        float focus = this.anim("searchfocus", "search", this.searching);
        float lit = this.anim("searchhover", "search", overSearch);
        this.zeroPanel(context, sx, y, sw, 34.0f, 5, this.panel(PANEL), DropdownScreen.lerp(DropdownScreen.lerp(0x1AFFFFFF, 0x30FFFFFF, lit), -1929379841, focus));
        int iconColor = this.col(DropdownScreen.lerp(-1426063361, -1, focus));
        int ix = Math.round(sx + 14.0f);
        int iy = Math.round(y + 17.0f);
        context.method_25294(ix, iy - 4, ix + 8, iy - 3, iconColor);
        context.method_25294(ix, iy, ix + 5, iy + 1, iconColor);
        context.method_25294(ix, iy + 4, ix + 5, iy + 5, iconColor);
        Smooth.ring(context, ix + 9, iy + 1, 3, iconColor);
        context.method_25294(ix + 11, iy + 4, ix + 13, iy + 5, iconColor);
        int found = 0;
        if (!this.search.isEmpty()) {
            for (Panel panel : ORDER) {
                found += this.modules(panel.category).size();
            }
        }
        String results = this.search.isEmpty() ? "" : found + (found == 1 ? " RESULT" : " RESULTS");
        float f = resultsW = results.isEmpty() ? 0.0f : CrispText.width(results, DropdownScreen.menuFont(true), DropdownScreen.menuEm(7.4f), 0.5f) + 14.0f;
        if (!results.isEmpty()) {
            this.spaced(context, results, sx + sw - 12.0f - resultsW + 14.0f, y + 17.0f + 2.8f, 7.4f, -1258291201);
        }
        this.searchText.sync(this.search, value -> CrispText.width(value, DropdownScreen.menuFont(true), DropdownScreen.menuEm(8.6f), 0.3f));
        this.searchText.draw(context, sx + 36.0f, y + 17.0f - 3.6f, sw - 52.0f - resultsW, 8.0f, this.searching, "SEARCH MODULES", -1, -1090519041, -1, this.alpha, value -> CrispText.width(value, DropdownScreen.menuFont(true), DropdownScreen.menuEm(8.6f), 0.3f), (ctx, value, tx, ty, color) -> CrispText.draw(ctx, value, DropdownScreen.menuFont(true), tx, ty + 6.9f, DropdownScreen.menuEm(8.6f), 0.3f, this.col(color)));
        this.hit(Kind.SEARCH, sx, y, sw, 34.0f, null, 0);
    }

    private void drawBottomBar(class_332 context, int mouseX, int mouseY) {
        int y = Math.round(this.bottomY());
        int x = Math.round(693.0f - (float)this.bottomWidth() / 2.0f);
        String[] labels = this.bottomLabels();
        for (int i = 0; i < labels.length; ++i) {
            String label = labels[i];
            int w = this.pillWidth(label);
            boolean active = i == 0 && this.configOpen;
            this.pill(context, label, x, y, w, active, mouseX, mouseY);
            boolean over = DropdownScreen.inside(mouseX, mouseY, x, y, w, 22.0f);
            switch (i) {
                case 2: {
                    this.hit(Kind.LAYOUT, x, y, w, 22.0f, null, 0);
                    if (!over) break;
                    this.hover("layout", "Opens the chat so you can drag, resize and reset your HUD cards.");
                    break;
                }
                default: {
                    int index = i == 3 ? 2 : i;
                    this.hit(Kind.BOTTOM, x, y, w, 22.0f, BOTTOM[index], index);
                    if (!over) break;
                    String string = "bottom" + index;
                    this.hover(string, switch (index) {
                        case 0 -> "Opens your configs: save, activate, rename, delete, export and import.";
                        case 1 -> "Opens the HUD settings in the Client panel.";
                        default -> "Opens the menu settings: style, colors, fonts and sounds.";
                    });
                }
            }
            x += w + 6;
        }
    }

    private void reveal(Module module) {
        if (module == null) {
            return;
        }
        Panel panel = PANELS.get((Object)module.category);
        if (panel == null) {
            return;
        }
        this.search = "";
        this.searching = false;
        panel.collapsed = false;
        this.front(panel);
        EXPANDED.add(module);
        float offset = 0.0f;
        for (Module other : this.modules(panel.category)) {
            if (other == module) break;
            offset += this.moduleHeight(other);
        }
        panel.scrollTarget = offset;
        UiSounds.click();
        DropdownScreen.save();
    }

    private void drawConfigPopup(class_332 context, int mouseX, int mouseY) {
        boolean live;
        this.popupOpen = this.approach(this.popupOpen, this.configOpen ? 1.0f : 0.0f, 14.0f);
        if (this.popupOpen < 0.01f) {
            return;
        }
        int x = Math.round(475.0f);
        int w = 436;
        int y = Math.round(this.bottomY() - 8.0f - 254.0f + (1.0f - this.popupOpen) * 12.0f);
        this.popupX = x;
        this.popupY = y;
        float previous = this.alpha;
        this.alpha = previous * this.popupOpen;
        boolean bl = live = this.popupOpen > 0.5f;
        if (live) {
            this.hit(Kind.PANEL, x, y, w, 254.0f, null, 0);
        }
        this.glass(context, x, y, w, 254, 10, this.panel(PANEL));
        this.rect(context, x + 9, y + 9, 22.0f, 22.0f, 7, CARD);
        this.icon(context, Category.CONFIG, x + 13, y + 13, 14);
        this.text(context, "CONFIGS", x + 38, y + 16, TEXT);
        this.configButton(context, "+ ADD CONFIG", -1, x + w - 196, y + 8, 106, 24, 6, mouseX, mouseY, live);
        this.configButton(context, "IMPORT", -1, x + w - 84, y + 8, 76, 24, 5, mouseX, mouseY, live);
        Configs configs = DropdownScreen.configs();
        int activeSlot = ConfigSlots.active();
        int rx = x + 9;
        int rw = w - 18;
        int ry = y + 42;
        int shown = 0;
        for (int i = 0; i < 5; ++i) {
            if (ConfigSlots.empty(i)) continue;
            ++shown;
            boolean active = activeSlot == i;
            int buttons = rx + rw - 8 - 200 - 12;
            int nameMax = buttons - 12 - (rx + 13);
            boolean over = live && DropdownScreen.inside(mouseX, mouseY, rx, ry, rw, 34.0f);
            float h = this.anim("cslot", i, over);
            this.rect(context, rx, ry, rw, 34.0f, 4, DropdownScreen.lerp(CARD, CARD_HOVER, h));
            if (active) {
                this.rect(context, rx, ry, rw, 34.0f, 4, DropdownScreen.withAlpha(ACCENT, 34));
                this.rect(context, rx + 4, ry + 9, 3.0f, 16.0f, 1, ACCENT);
            }
            if (this.renaming && configs.slot() == i) {
                int fw = Math.max(60, nameMax);
                this.rect(context, rx + 9, ry + 4, fw, 14.0f, 5, VALUE);
                this.text(context, this.tail(this.renameBuffer, fw - 14) + "|", rx + 13, ry + 7, TEXT);
            } else {
                int badge = this.textWidth("ACTIVE") + 12;
                String name = this.fit(ConfigSlots.name(i), nameMax - (active ? badge + 6 : 0));
                this.text(context, name, rx + 13, ry + 7, TEXT);
                if (active) {
                    int bx = rx + 13 + this.textWidth(name) + 6;
                    this.rect(context, bx, ry + 5, badge, 12.0f, 6, ACCENT);
                    this.text(context, "ACTIVE", bx + 6, ry + 7, -16182498);
                }
            }
            this.text(context, this.fit(ConfigSlots.enabledCount(i) + " modules  -  " + configs.status(i), nameMax), rx + 13, ry + 21, MUTED);
            if (live) {
                this.hit(Kind.CONFIG_SLOT, rx, ry, buttons - rx - 4, 34.0f, null, i);
            }
            if (over && mouseX < buttons - 4) {
                this.hover("cslot" + i, "Double click the name to rename this config.");
            }
            this.configButton(context, "LOAD", i, buttons, ry + 6, 50, 22, 1, mouseX, mouseY, live);
            this.configButton(context, "SAVE", i, buttons + 54, ry + 6, 50, 22, 0, mouseX, mouseY, live);
            this.configButton(context, "SHARE", i, buttons + 108, ry + 6, 50, 22, 2, mouseX, mouseY, live);
            this.configButton(context, "DELETE", i, buttons + 162, ry + 6, 50, 22, 4, mouseX, mouseY, live);
            ry += 38;
        }
        if (shown == 0) {
            String title = "NO CONFIGS YET";
            this.text(context, title, x + (w - this.textWidth(title)) / 2, y + 110, TEXT);
            String hint = this.fit("Save your setup with + Add Config, or copy a code like SC-123456 and press Import.", w - 24);
            this.text(context, hint, x + (w - this.textWidth(hint)) / 2, y + 126, MUTED);
        }
        int fieldW = 196;
        this.drawSetting(context, configs.shareName, x + w - 9 - fieldW, y + 254 - 22, fieldW, mouseX, mouseY, y, y + 254);
        String footer = configs.busy() ? "Connecting..." : "Double click a name to rename it.";
        this.text(context, this.fit(footer, w - 32 - fieldW), x + 12, y + 254 - 16, MUTED);
        this.alpha = previous;
    }

    private void configButton(class_332 context, String label, int slot, int bx, int by, int bw, int bh, int action, int mouseX, int mouseY, boolean live) {
        float click;
        boolean primary;
        String key = action + ":" + slot;
        boolean over = live && DropdownScreen.inside(mouseX, mouseY, bx, by, bw, bh);
        float h = this.anim("caction", key, over);
        boolean armed = action == 4 && DropdownScreen.configs().deleteArmed() && DropdownScreen.configs().slot() == slot;
        boolean bl = primary = action == 6 || action == 1 && ConfigSlots.active() != slot;
        int fill = armed ? -7721158 : (primary ? ACCENT : DropdownScreen.lerp(CARD, CARD_HOVER, h));
        float s = 1.0f - 0.06f * h;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)bx + (float)bw / 2.0f, (float)by + (float)bh / 2.0f);
        matrices.scale(s, s);
        matrices.translate(-((float)bx + (float)bw / 2.0f), -((float)by + (float)bh / 2.0f));
        this.rect(context, bx, by, bw, bh, 4, fill);
        if (action == 4 && !armed) {
            Smooth.outline(context, bx, by, bw, bh, 6, this.col(-1864346005));
        }
        if ((armed || primary) && h > 0.01f) {
            this.rect(context, bx, by, bw, bh, 6, DropdownScreen.withAlpha(0xFFFFFF, Math.round(36.0f * h)));
        }
        if ((click = this.press("caction" + key)) > 0.0f) {
            this.rect(context, bx, by, bw, bh, 6, DropdownScreen.withAlpha(primary ? 0xFFFFFF : ACCENT, Math.round(255.0f * click * 0.22f)));
        }
        String shown = armed ? "SURE?" : label;
        this.text(context, shown, bx + (bw - this.textWidth(shown)) / 2, by + (bh - 8) / 2, primary ? -16182498 : TEXT);
        matrices.popMatrix();
        if (live) {
            this.hit(Kind.CONFIG_ACTION, bx, by, bw, bh, slot, action);
        }
        if (over) {
            this.hover("caction" + key, Configs.TIPS[action]);
        }
    }

    private void startRename(int slot) {
        DropdownScreen.configs().select(slot);
        this.renaming = true;
        this.renameBuffer = ConfigSlots.name(slot);
        this.searching = false;
    }

    private void commitRename() {
        this.renaming = false;
        DropdownScreen.configs().renameSlot(this.renameBuffer);
    }

    private void configAction(int action, Object target) {
        Integer slot;
        if (target instanceof Integer && (slot = (Integer)target) >= 0) {
            DropdownScreen.configs().select(slot);
        }
        DropdownScreen.configs().run(action);
        SoberClient.CONFIG.save();
    }

    private void hover(Object key, String description) {
        if (!(key == this.hoveredKey || this.hoveredKey != null && this.hoveredKey.equals(key))) {
            this.hoveredKey = key;
            this.hoverStart = System.currentTimeMillis();
            if (key instanceof Module || key instanceof Category) {
                UiSounds.hover();
            }
        }
        this.tooltip = description;
    }

    private void drawTooltip(class_332 context, int mouseX, int mouseY) {
        if (this.tooltip == null) {
            this.hoveredKey = null;
            return;
        }
        if (this.tooltip.isEmpty() || this.dragging != null || this.draggingPanel != null || System.currentTimeMillis() - this.hoverStart < 600L) {
            return;
        }
        float visible = Math.min(1.0f, (float)(System.currentTimeMillis() - this.hoverStart - 600L) / 150.0f);
        float scale = this.renderScale;
        int localX = Math.round((float)mouseX / scale);
        int localY = Math.round((float)mouseY / scale);
        int localW = Math.round((float)this.field_22789 / scale);
        int localH = Math.round((float)this.field_22790 / scale);
        List<String> lines = this.wrap(this.tooltip, 190);
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, this.textWidth(line));
        }
        int h = lines.size() * 11 + 9;
        int tx = Math.max(4, Math.min(localX + 12, localW - w - 18));
        int ty = Math.max(4, Math.min(localY + 14, localH - h - 6));
        float previous = this.alpha;
        this.alpha = previous * visible;
        context.method_51448().pushMatrix();
        context.method_51448().scale(scale, scale);
        this.rect(context, tx, ty, w + 14, h, 6, -266263510);
        int ly = ty + 7;
        for (String line : lines) {
            this.text(context, line, tx + 7, ly, -2301722);
            ly += 11;
        }
        context.method_51448().popMatrix();
        this.alpha = previous;
    }

    private List<String> wrap(String value, int maxWidth) {
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (String word : value.split(" ")) {
            String next;
            String string = next = line.isEmpty() ? word : String.valueOf(line) + " " + word;
            if (this.textWidth(next) > maxWidth && !line.isEmpty()) {
                lines.add(line.toString());
                line = new StringBuilder(word);
                continue;
            }
            line = new StringBuilder(next);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void toggleCollapse(Panel panel) {
        panel.collapsed = !panel.collapsed;
        UiSounds.click();
        DropdownScreen.save();
    }

    private void toggleExpand(Module module) {
        if (!EXPANDED.remove(module)) {
            EXPANDED.add(module);
        }
        if (this.editing != null) {
            this.commitEdit();
        }
        this.binding = null;
        UiSounds.click();
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closing || this.openProgress < 0.8f) {
            return true;
        }
        double mx = this.uiX(click.comp_4798());
        double my = this.uiY(click.comp_4799());
        int button = click.method_74245();
        Hit hit = this.hitAt(mx, my);
        if (this.editing != null && (hit == null || hit.target() != this.editing)) {
            this.commitEdit();
        }
        if (this.binding != null && (hit == null || hit.kind() != Kind.BIND)) {
            this.binding = null;
        }
        if (this.keyBinding != null && (hit == null || hit.target() != this.keyBinding)) {
            this.keyBinding = null;
        }
        if (hit == null || hit.kind() != Kind.SEARCH) {
            this.searching = false;
        }
        if (this.renaming && (hit == null || hit.kind() != Kind.CONFIG_SLOT)) {
            this.commitRename();
        }
        if (hit == null) {
            this.configOpen = false;
            return super.method_25402(click, doubled);
        }
        switch (hit.kind().ordinal()) {
            case 13: {
                this.configOpen = !this.configOpen;
                UiSounds.click();
                break;
            }
            case 10: {
                UiSounds.click();
                Hud hud = SoberClient.MODULES.get(Hud.class);
                boolean editable = hud != null && hud.isActive() && hud.chatEditing.get() != false;
                this.field_22787.method_1507((class_437)new class_408("", false));
                Notifications.push("HUD", editable ? "Drag your HUD cards to move them, scroll to resize, right click to reset the size." : "Turn on the HUD and Chat Editing in the HUD settings to move your HUD cards.", Notifications.Type.INFO);
                break;
            }
            case 11: {
                this.pressedAt.put("pill" + String.valueOf(hit.target()), System.nanoTime());
                if (hit.index() == 0) {
                    this.configOpen = !this.configOpen;
                    UiSounds.click();
                    break;
                }
                this.configOpen = false;
                this.reveal(hit.index() == 1 ? SoberClient.MODULES.get(Hud.class) : DropdownScreen.options());
                break;
            }
            case 12: {
                this.pressedAt.put(hit.target(), System.nanoTime());
                this.toggleExpand((Module)hit.target());
                break;
            }
            case 14: {
                if (!doubled || this.renaming) break;
                this.startRename(hit.index());
                UiSounds.click();
                break;
            }
            case 15: {
                this.pressedAt.put("caction" + hit.index() + ":" + String.valueOf(hit.target()), System.nanoTime());
                UiSounds.click();
                this.configAction(hit.index(), hit.target());
                break;
            }
            case 16: {
                this.pressedAt.put("status" + hit.index(), System.nanoTime());
                UiSounds.click();
                MenuKit.openLink((String)hit.target());
                break;
            }
            case 0: {
                Panel panel = (Panel)hit.target();
                this.front(panel);
                if (button == 0) {
                    this.draggingPanel = panel;
                    this.grabX = (float)(mx - (double)panel.x);
                    this.grabY = (float)(my - (double)panel.y);
                    this.pressX = mx;
                    this.pressY = my;
                    this.panelMoved = false;
                    break;
                }
                if (button != 1) break;
                this.toggleCollapse(panel);
                break;
            }
            case 1: {
                Module module = (Module)hit.target();
                this.pressedAt.put(module, System.nanoTime());
                if (module instanceof Configs && button == 0) {
                    this.configOpen = !this.configOpen;
                    UiSounds.click();
                    break;
                }
                if (button == 1 || module instanceof ClickGui) {
                    this.toggleExpand(module);
                    break;
                }
                if (button == 2) {
                    EXPANDED.add(module);
                    this.binding = module;
                    UiSounds.click();
                    break;
                }
                if (button != 0) break;
                module.toggle();
                UiSounds.moduleToggle(module.isActive());
                SoberClient.CONFIG.save();
                break;
            }
            case 4: {
                MenuKit.selectPreset((Module)hit.target(), hit.index());
                UiSounds.click();
                SoberClient.CONFIG.save();
                break;
            }
            case 5: {
                this.binding = this.binding == hit.target() ? null : (Module)hit.target();
                UiSounds.click();
                break;
            }
            case 2: {
                this.clickSetting(hit, mx, button);
                break;
            }
            case 3: {
                ColorSetting color = (ColorSetting)hit.target();
                if (button == 1) {
                    color.reset();
                    this.editing = null;
                    SoberClient.CONFIG.save();
                } else if (this.editing != color) {
                    this.editing = color;
                    this.editBuffer = color.display();
                }
                UiSounds.click();
                break;
            }
            case 6: 
            case 7: 
            case 8: {
                this.dragging = hit;
                this.updateDrag(hit, mx, my);
                break;
            }
            case 9: {
                this.searching = true;
                break;
            }
            case 17: {
                Object object = hit.target();
                if (!(object instanceof Panel)) break;
                Panel panel = (Panel)object;
                this.front(panel);
            }
        }
        return true;
    }

    private void clickSetting(Hit hit, double mx, int button) {
        Setting setting = (Setting)hit.target();
        this.pressedAt.put(setting, System.nanoTime());
        if(setting instanceof net.albiz.client.port.soberui.setting.ItemListSetting items){this.commitEdit();this.field_22787.method_1507(new ItemPickerScreen(this,items));return;}
        if(setting instanceof net.albiz.client.port.soberui.AlbizModuleView.LegacyEditorSetting complex){if(complex.original instanceof net.albiz.client.module.setting.ActionSetting action){action.run();return;}for(var candidate:SoberClient.MODULES.all())if(candidate instanceof net.albiz.client.port.soberui.AlbizModuleView view&&view.getSettings().contains(setting)){this.commitEdit();this.field_22787.method_1507(new net.albiz.client.gui.LegacySettingsScreen(this,view.target));return;}}
        if (setting instanceof BoolSetting) {
            BoolSetting bool = (BoolSetting)setting;
            bool.toggle();
            UiSounds.toggle((Boolean)bool.get());
        } else if (setting instanceof IntSetting) {
            this.dragging = hit;
            this.updateDrag(hit, mx, 0.0);
        } else if (setting instanceof EnumSetting) {
            EnumSetting enumeration = (EnumSetting)setting;
            enumeration.cycle(button != 1);
            UiSounds.click();
        } else if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            if (!OPEN_COLORS.remove(color)) {
                OPEN_COLORS.add(color);
            }
            UiSounds.click();
        } else {
            if (setting instanceof BlockListSetting) {
                BlockListSetting blocks = (BlockListSetting)setting;
                UiSounds.click();
                this.field_22787.method_1507((class_437)new BlockPickerScreen(this, blocks));
                return;
            }
            if (setting instanceof ItemSetting) {
                ItemSetting item = (ItemSetting)setting;
                if (button == 1) {
                    item.reset();
                    this.editing = null;
                } else {
                    this.editing = item;
                    this.editBuffer = item.id();
                }
            } else if (setting instanceof KeySetting) {
                KeySetting key = (KeySetting)setting;
                if (button == 1) {
                    key.bind(-1);
                    this.keyBinding = null;
                } else {
                    this.keyBinding = this.keyBinding == key ? null : key;
                }
                UiSounds.click();
            } else if (setting instanceof StringSetting) {
                StringSetting string = (StringSetting)setting;
                if (button == 1) {
                    string.set("");
                    this.editing = null;
                } else {
                    this.editing = string;
                    this.editBuffer = (String)string.get();
                }
            }
        }
        SoberClient.CONFIG.save();
    }

    private void updateDrag(Hit hit, double mx, double my) {
        Object object = hit.target();
        if (object instanceof IntSetting) {
            IntSetting integer = (IntSetting)object;
            double trackX = hit.x() + 7.0f;
            double trackW = hit.w() - 14.0f;
            double progress = Math.max(0.0, Math.min(1.0, (mx - trackX - 4.0) / (trackW - 8.0)));
            integer.set((int)Math.round((double)integer.min + progress * (double)(integer.max - integer.min)));
        } else {
            Object trackX = hit.target();
            if (trackX instanceof ColorSetting) {
                ColorSetting color = (ColorSetting)trackX;
                if (hit.kind() == Kind.WHEEL) {
                    float[] picked = ColorWheel.pick(mx, my, Math.round(hit.x()), Math.round(hit.y()), Math.round(hit.w()));
                    float value = color.hsv()[2];
                    color.setHsv(picked[0], picked[1], value < 0.05f ? 1.0f : value);
                } else {
                    double progress = Math.max(0.0, Math.min(1.0, (mx - (double)(hit.x() + 4.0f)) / (double)(hit.w() - 8.0f)));
                    if (hit.kind() == Kind.BRIGHTNESS) {
                        float[] hsv = color.hsv();
                        color.setHsv(hsv[0], hsv[1], (float)progress);
                    } else {
                        color.setAlpha((int)Math.round(progress * 255.0));
                    }
                }
            }
        }
    }

    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        double mx = this.uiX(click.comp_4798());
        double my = this.uiY(click.comp_4799());
        if (this.draggingPanel != null) {
            if (!this.panelMoved && Math.hypot(mx - this.pressX, my - this.pressY) < 3.0) {
                return true;
            }
            this.panelMoved = true;
            float nx = (float)(mx - (double)this.grabX);
            float ny = (float)(my - (double)this.grabY);
            if (!this.field_22787.method_74187()) {
                for (Panel other : ORDER) {
                    if (other == this.draggingPanel) continue;
                    if (Math.abs(ny - other.y) < 6.0f) {
                        ny = other.y;
                    }
                    if (Math.abs(nx - other.x) < 6.0f) {
                        nx = other.x;
                    }
                    if (Math.abs(nx - (other.x + 166.0f + 12.0f)) < 6.0f) {
                        nx = other.x + 166.0f + 12.0f;
                    }
                    if (!(Math.abs(nx + 166.0f + 12.0f - other.x) < 6.0f)) continue;
                    nx = other.x - 166.0f - 12.0f;
                }
            }
            this.draggingPanel.x = nx;
            this.draggingPanel.y = ny;
            this.clampPanel(this.draggingPanel);
            return true;
        }
        if (this.dragging != null) {
            this.updateDrag(this.dragging, mx, my);
            return true;
        }
        return super.method_25403(click, offsetX, offsetY);
    }

    public boolean method_25406(class_11909 click) {
        if (this.draggingPanel != null) {
            if (!this.panelMoved) {
                this.toggleCollapse(this.draggingPanel);
            }
            this.draggingPanel = null;
            DropdownScreen.save();
            return true;
        }
        if (this.dragging != null) {
            this.dragging = null;
            SoberClient.CONFIG.save();
            return true;
        }
        return super.method_25406(click);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
        Object object;
        if (this.closing) {
            return true;
        }
        double mx = this.uiX(mouseX);
        double my = this.uiY(mouseY);
        int step = vertical > 0.0 ? 1 : -1;
        Hit hit = this.hitAt(mx, my);
        boolean ctrl = class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)341);
        boolean shift = class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)340);
        if (ctrl && hit != null && (object = hit.target()) instanceof IntSetting) {
            IntSetting integer = (IntSetting)object;
            integer.set((Integer)integer.get() + step * (shift ? 10 : 1));
            return true;
        }
        if (this.popupOpen > 0.5f && DropdownScreen.inside(mx, my, this.popupX, this.popupY, 436.0f, 254.0f)) {
            return true;
        }
        Panel panel = this.topPanel(mx, my);
        if (panel != null && !panel.collapsed && panel.content > panel.view) {
            panel.scrollTarget = Math.clamp(panel.scrollTarget - (float)(step * 21), 0.0f, Math.max(0.0f, panel.content - panel.view));
            UiSounds.key(0.3f);
        }
        return true;
    }

    private void commitEdit() {
        Setting<?> setting = this.editing;
        if (setting instanceof ItemSetting) {
            ItemSetting item = (ItemSetting)setting;
            if (!item.setFromId(this.editBuffer)) {
                this.invalidUntil = System.currentTimeMillis() + 600L;
                this.editing = null;
                return;
            }
        } else {
            setting = this.editing;
            if (setting instanceof StringSetting) {
                StringSetting string = (StringSetting)setting;
                string.set(this.editBuffer.trim());
            } else {
                ColorSetting color;
                setting = this.editing;
                if (setting instanceof ColorSetting && !(color = (ColorSetting)setting).setHex(this.editBuffer)) {
                    this.invalidSetting = color;
                    this.invalidUntil = System.currentTimeMillis() + 600L;
                }
            }
        }
        this.editing = null;
        SoberClient.CONFIG.save();
    }

    public boolean method_25404(class_11908 input) {
        if (this.closing) {
            return true;
        }
        int key = input.comp_4795();
        if (this.binding != null) {
            this.binding.setKey(key == 256 || key == 259 || key == 261 ? -1 : key);
            UiSounds.keybind(this.binding.getKey() >= 0);
            this.binding = null;
            this.swallowUntil = System.currentTimeMillis() + 150L;
            SoberClient.CONFIG.save();
            return true;
        }
        if (this.keyBinding != null) {
            if (key != 256) {
                this.keyBinding.bind(key == 259 || key == 261 ? -1 : key);
            }
            UiSounds.keybind((Integer)this.keyBinding.get() >= 0);
            this.keyBinding = null;
            this.swallowUntil = System.currentTimeMillis() + 150L;
            SoberClient.CONFIG.save();
            return true;
        }
        if (this.renaming) {
            switch (key) {
                case 257: 
                case 335: {
                    this.commitRename();
                    break;
                }
                case 256: {
                    this.renaming = false;
                    break;
                }
                case 259: {
                    if (this.renameBuffer.isEmpty()) break;
                    this.renameBuffer = this.renameBuffer.substring(0, this.renameBuffer.length() - 1);
                    break;
                }
                case 86: {
                    if (!input.method_75377()) break;
                    String pasted = this.renameBuffer + this.field_22787.field_1774.method_1460().replace("\n", "").replace("\r", "");
                    this.renameBuffer = pasted.length() > 32 ? pasted.substring(0, 32) : pasted;
                    break;
                }
            }
            return true;
        }
        if (this.editing != null) {
            switch (key) {
                case 257: 
                case 335: {
                    this.commitEdit();
                    break;
                }
                case 256: {
                    this.commitEdit();
                    break;
                }
                case 259: {
                    if (this.editBuffer.isEmpty()) break;
                    this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
                    break;
                }
                case 86: {
                    if (!input.method_75377()) break;
                    this.editBuffer = this.editBuffer + this.field_22787.field_1774.method_1460().replace("\n", "").replace("\r", "");
                    break;
                }
            }
            return true;
        }
        if (key == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.searching = true;
            UiSounds.key(0.7f);
            return true;
        }
        if (key == 256) {
            if (this.searching || !this.search.isEmpty()) {
                this.search = "";
                this.searching = false;
            } else if (this.configOpen) {
                this.configOpen = false;
                UiSounds.close();
            } else {
                this.method_25419();
            }
            return true;
        }
        if (key == DropdownScreen.options().getKey() && this.search.isEmpty()) {
            this.method_25419();
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        if (this.closing || !input.method_74227()) {
            return true;
        }
        if (System.currentTimeMillis() < this.swallowUntil) {
            this.swallowUntil = 0L;
            return true;
        }
        if (this.renaming) {
            if (this.renameBuffer.length() < 32) {
                this.renameBuffer = this.renameBuffer + input.method_74226();
            }
            return true;
        }
        if (this.editing != null) {
            String chars = input.method_74226();
            if (this.editing instanceof ColorSetting) {
                if (this.editBuffer.length() < 9 && chars.matches("[#0-9a-fA-FxX]")) {
                    this.editBuffer = this.editBuffer + chars.toUpperCase(Locale.ROOT);
                }
            } else if (this.editBuffer.length() < 100) {
                this.editBuffer = this.editBuffer + chars;
            }
            return true;
        }
        String typed = input.method_74226();
        if (this.search.isEmpty() && typed.isBlank()) {
            return true;
        }
        if (this.search.length() < 40) {
            this.search = this.search + typed;
            this.searching = true;
            UiSounds.key(1.0f);
            for (Panel panel : ORDER) {
                panel.scrollTarget = 0.0f;
            }
        }
        return true;
    }

    private static void load() {
        if (!Files.exists(FILE, new LinkOption[0])) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString((String)Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("version") || root.get("version").getAsInt() != 8) {
                return;
            }
            for (Panel panel : PANELS.values()) {
                if (!root.has(panel.category.name())) continue;
                JsonObject o = root.getAsJsonObject(panel.category.name());
                panel.x = o.get("x").getAsFloat();
                panel.y = o.get("y").getAsFloat();
                panel.collapsed = o.has("collapsed") && o.get("collapsed").getAsBoolean();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", (Number)8);
            for (Panel panel : PANELS.values()) {
                JsonObject o = new JsonObject();
                o.addProperty("x", (Number)Math.round(panel.x));
                o.addProperty("y", (Number)Math.round(panel.y));
                o.addProperty("collapsed", Boolean.valueOf(panel.collapsed));
                root.add(panel.category.name(), (JsonElement)o);
            }
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((JsonElement)root), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static {
        ArrayList<Category> categories = new ArrayList<Category>();
        for (Category category2 : Category.values()) {
            if (category2 == Category.CONFIG) continue;
            categories.add(category2);
        }
        EnumMap<Category, Integer> sizes = new EnumMap<Category, Integer>(Category.class);
        for (Module module : SoberClient.MODULES.all()) {
            sizes.merge(module.category, 1, Integer::sum);
        }
        categories.sort(Comparator.<Category>comparingInt(category -> -sizes.getOrDefault(category, 0).intValue()).thenComparing(category -> category.title));
        categories.add(Category.CONFIG);
        float start = DropdownScreen.layoutLeft(categories.size());
        int index = 0;
        for (Category category3 : categories) {
            Panel panel = new Panel(category3, start + (float)(index++ * 178), 76.0f);
            PANELS.put(category3, panel);
            ORDER.add(panel);
            HOME.add(panel);
        }
        DropdownScreen.load();
    }

    private static final class Panel {
        final Category category;
        float x;
        float y;
        boolean collapsed;
        float scroll;
        float scrollTarget;
        float content;
        float view;
        float height;

        Panel(Category category, float x, float y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }

    private record Key(String tag, Object target) {
    }

    private static final class Timed {
        float from;
        float target;
        long start;
        long duration;
        boolean eased;

        Timed(float value) {
            this.from = this.target = value;
        }

        float at(long now) {
            long elapsed = now - this.start;
            if (this.duration <= 0L || elapsed >= this.duration) {
                return this.target;
            }
            float p = (float)elapsed / (float)this.duration;
            float k = this.eased ? (p < 0.5f ? 4.0f * p * p * p : 1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3.0) / 2.0f) : p;
            return this.from + (this.target - this.from) * k;
        }
    }

    private record Hit(Kind kind, float x, float y, float w, float h, Object target, int index) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
        }
    }

    private static enum Kind {
        HEADER,
        MODULE,
        SETTING,
        HEX,
        PRESET,
        BIND,
        WHEEL,
        BRIGHTNESS,
        ALPHA,
        SEARCH,
        LAYOUT,
        BOTTOM,
        MORE,
        CONFIGS,
        CONFIG_SLOT,
        CONFIG_ACTION,
        STATUS,
        PANEL;

    }
}

