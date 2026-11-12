// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.DropdownScreen;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.PanelScreen;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.albiz.client.port.soberui.modules.Configs;
import net.albiz.client.port.soberui.render.Color;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_10789;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public final class MenuKit {
    public static final class_2960 BRAND = class_2960.method_60655((String)"albiz", (String)"textures/gui/brand.png");
    static final RenderPipeline ICON_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"albiz", (String)"icon_sdf")).withVertexShader(class_2960.method_60656((String)"core/position_tex_color")).withFragmentShader(class_2960.method_60655((String)"albiz", (String)"icon_sdf")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).build();
    static final int DARK = -15069650;
    static final int TEXT = -855051;
    static final int MUTED = -4012338;
    static final int FAINT = -6906969;
    private static final Object[][] THEMES = new Object[][]{{"Lavender", new Color(176, 140, 240, 255)}, {"Blossom", new Color(255, 111, 181, 255)}, {"Amethyst", new Color(167, 121, 255, 255)}, {"Ocean", new Color(79, 163, 255, 255)}, {"Crimson", new Color(232, 58, 74, 255)}, {"Jade", new Color(63, 214, 155, 255)}, {"Sunset", new Color(255, 150, 60, 255)}, {"Rainbow", ClickGui.AccentMode.Rainbow}, {"Galaxy", ClickGui.AccentMode.Galaxy}};
    private static final Deque<Module> RECENT = new ArrayDeque<Module>();
    private static final Map<String, Float> WIDTHS = MenuKit.lru(6000);
    private static final Map<String, String> FITS = MenuKit.lru(2000);
    private static final Map<String, List<String>> WRAPS = MenuKit.lru(600);

    private static <V> Map<String, V> lru(final int limit) {
        return new LinkedHashMap<String, V>(256, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
                return this.size() > limit;
            }
        };
    }

    private MenuKit() {
    }

    static ClickGui options() {
        return SoberClient.MODULES.get(ClickGui.class);
    }

    static int accent() {
        return 0xFF000000 | Theme.accent();
    }

    static int alpha(int color, float opacity) {
        int a = Math.round((float)(color >>> 24) * Math.max(0.0f, Math.min(1.0f, opacity)));
        return a << 24 | color & 0xFFFFFF;
    }

    static int withAlpha(int color, int a) {
        return (Math.max(0, Math.min(255, a)) & 0xFF) << 24 | color & 0xFFFFFF;
    }

    static int lerp(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int aa = a >>> 24;
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int ba = b >>> 24;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return Math.round((float)aa + (float)(ba - aa) * t) << 24 | Math.round((float)ar + (float)(br - ar) * t) << 16 | Math.round((float)ag + (float)(bg - ag) * t) << 8 | Math.round((float)ab + (float)(bb - ab) * t);
    }

    static float quint(float p) {
        p = Math.max(0.0f, Math.min(1.0f, p));
        float inverse = 1.0f - p;
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    static float approach(float current, float target, float speed, float delta) {
        float value = current + (target - current) * (1.0f - (float)Math.exp(-delta * speed * Theme.animationSpeed()));
        return Math.abs(value - target) < 0.002f ? target : value;
    }

    static void text(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
        ScreenStyle.crisp(context, value, x, y, size, color, heavy);
    }

    static float width(String value, float size, boolean heavy) {
        if (value == null || value.isEmpty()) {
            return 0.0f;
        }
        String key = Theme.crispFile(heavy) + "|" + size + "|" + value;
        Float cached = WIDTHS.get(key);
        if (cached != null) {
            return cached.floatValue();
        }
        float measured = ScreenStyle.crispWidth(value, size, heavy);
        WIDTHS.put(key, Float.valueOf(measured));
        return measured;
    }

    static void centered(class_332 context, String value, float cx, float y, float size, int color, boolean heavy) {
        MenuKit.text(context, value, cx - MenuKit.width(value, size, heavy) / 2.0f, y, size, color, heavy);
    }

    static String fit(String value, float max, float size, boolean heavy) {
        if (value == null) {
            return "";
        }
        if (max < 8.0f) {
            return "";
        }
        if (MenuKit.width(value, size, heavy) <= max) {
            return value;
        }
        String key = Theme.crispFile(heavy) + "|" + size + "|" + Math.round(max * 2.0f) + "|" + value;
        String cached = FITS.get(key);
        if (cached != null) {
            return cached;
        }
        int low = 0;
        int high = value.length();
        while (low < high) {
            int middle = (low + high + 1) / 2;
            if (MenuKit.width(value.substring(0, middle).trim() + "...", size, heavy) <= max) {
                low = middle;
                continue;
            }
            high = middle - 1;
        }
        String result = value.substring(0, low).trim() + "...";
        FITS.put(key, result);
        return result;
    }

    static String tail(String value, float max, float size, boolean heavy) {
        if (MenuKit.width(value, size, heavy) <= max) {
            return value;
        }
        int low = 0;
        int high = value.length();
        while (low < high) {
            int middle = (low + high) / 2;
            if (MenuKit.width(value.substring(middle), size, heavy) <= max) {
                high = middle;
                continue;
            }
            low = middle + 1;
        }
        return value.substring(low);
    }

    static List<String> wrap(String value, float max, float size, boolean heavy) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<String>();
        }
        String key = Theme.crispFile(heavy) + "|" + size + "|" + Math.round(max * 2.0f) + "|" + value;
        List<String> cached = WRAPS.get(key);
        if (cached != null) {
            return cached;
        }
        List<String> lines = MenuKit.wrapUncached(value, max, size, heavy);
        WRAPS.put(key, lines);
        return lines;
    }

    private static List<String> wrapUncached(String value, float max, float size, boolean heavy) {
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (String word : value.split(" ")) {
            String next;
            String string = next = line.isEmpty() ? word : String.valueOf(line) + " " + word;
            if (MenuKit.width(next, size, heavy) > max && !line.isEmpty()) {
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

    static String label(String value) {
        return value.toUpperCase(Locale.ROOT);
    }

    static void panel(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        if (w <= 0 || h <= 0 || opacity <= 0.01f) {
            return;
        }
        if (Theme.liquidGlass()) {
            Smooth.liquid(context, x, y, w, h, r, opacity, true);
            return;
        }
        int base = Theme.fill(-484893920);
        base = MenuKit.withAlpha(base, Math.round((float)((base >>> 24) * MenuKit.options().glassOpacity.get()) / 100.0f));
        Smooth.rect(context, x, y + 2, w, h + 2, r, MenuKit.alpha(0x30000000, opacity));
        Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(base, opacity));
        Smooth.sheen(context, x, y, w, h, r, MenuKit.alpha(0x12FFFFFF, opacity));
        Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(0x24FFFFFF, opacity));
    }

    static void shadow(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        ScreenStyle.shadow(context, x, y, w, h, r, opacity);
    }

    static void well(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
        Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(0x12FFFFFF, 0x20FFFFFF, hover), opacity));
        Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(0x1CFFFFFF, 0x34FFFFFF, hover), opacity));
    }

    static void toggle(class_332 context, float x, float y, float w, float h, float on, float opacity) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(w);
        int ih = Math.round(h);
        int accent = MenuKit.accent();
        if (on > 0.01f) {
            Smooth.glow(context, ix + iw / 2, iy + ih / 2, Math.round((float)iw * 0.75f), MenuKit.alpha(MenuKit.withAlpha(accent, 85), on * opacity));
        }
        Smooth.rect(context, ix, iy, iw, ih, ih / 2, MenuKit.alpha(MenuKit.lerp(0x3CFFFFFF, accent, on), opacity));
        if (on < 0.99f) {
            Smooth.outline(context, ix, iy, iw, ih, ih / 2, MenuKit.alpha(0x22FFFFFF, (1.0f - on) * opacity));
        }
        int knob = ih - 4;
        int kx = Math.round((float)(ix + 2) + (float)(iw - knob - 4) * on);
        Smooth.rect(context, kx, iy + 3, knob, knob, knob / 2, MenuKit.alpha(0x38000000, opacity));
        Smooth.rect(context, kx, iy + 2, knob, knob, knob / 2, MenuKit.alpha(-855051, opacity));
    }

    static void chip(class_332 context, String value, float x, float y, float size, float opacity) {
        float w = MenuKit.width(value, size, true) + 10.0f;
        float h = 8.4f * size + 6.0f;
        Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 4, MenuKit.alpha(1510606354, opacity));
        Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 4, MenuKit.alpha(0x30FFFFFF, opacity));
        MenuKit.text(context, value, x + 5.0f, y + 3.0f, size, MenuKit.alpha(-420285451, opacity), true);
    }

    static float chipWidth(String value, float size) {
        return MenuKit.width(value, size, true) + 10.0f;
    }

    static void hint(class_332 context, float x, float y, String key, String action, float opacity) {
        MenuKit.chip(context, key, x, y, 0.62f, opacity);
        MenuKit.text(context, action, x + MenuKit.chipWidth(key, 0.62f) + 4.0f, y + 3.0f, 0.66f, MenuKit.alpha(-4012338, opacity), false);
    }

    static float hintWidth(String key, String action) {
        return MenuKit.chipWidth(key, 0.62f) + 4.0f + MenuKit.width(action, 0.66f, false);
    }

    static void brand(class_332 context, float x, float y, float size, float opacity) {
        Smooth.glow(context, Math.round(x + size / 2.0f), Math.round(y + size / 2.0f), Math.round(size * 0.85f), MenuKit.alpha(777384181, opacity));
        Smooth.brandCrisp(context, x, y, size, MenuKit.alpha(-1, opacity));
    }

    static void tooltip(class_332 context, String tip, int mouseX, int mouseY, int screenW, int screenH, float opacity) {
        MenuKit.tooltip(context, tip, mouseX, mouseY, screenW, screenH, opacity, false);
    }

    static void tooltip(class_332 context, String tip, int mouseX, int mouseY, int screenW, int screenH, float opacity, boolean light) {
        if (tip == null || tip.isEmpty() || opacity <= 0.01f) {
            return;
        }
        List<String> lines = MenuKit.wrap(tip, 190.0f, 0.72f, false);
        float w = 0.0f;
        for (String line : lines) {
            w = Math.max(w, MenuKit.width(line, 0.72f, false));
        }
        int bw = Math.round(w + 14.0f);
        int bh = lines.size() * 10 + 9;
        int tx = Math.max(4, Math.min(mouseX + 12, screenW - bw - 4));
        int ty = Math.max(4, Math.min(mouseY + 14, screenH - bh - 4));
        Smooth.rect(context, tx, ty + 2, bw, bh, 6, MenuKit.alpha(light ? 0x18000000 : 0x50000000, opacity));
        Smooth.rect(context, tx, ty, bw, bh, 6, MenuKit.alpha(light ? -50331649 : -182772959, opacity));
        Smooth.outline(context, tx, ty, bw, bh, 6, MenuKit.alpha(light ? 0x1C000000 : 0x26FFFFFF, opacity));
        float ly = ty + 5;
        for (String line : lines) {
            MenuKit.text(context, line, tx + 7, ly, 0.72f, MenuKit.alpha(light ? -14935010 : -1644308, opacity), false);
            ly += 10.0f;
        }
    }

    static void background(class_332 context, class_310 client, int width, int height, float alpha, boolean blur) {
        ClickGui options = MenuKit.options();
        if (blur && options.blur.get().booleanValue()) {
            context.method_71278();
        }
        boolean liquid = Theme.liquidGlass();
        int dim = Math.round((float)(255 * options.backgroundDim.get()) / 100.0f * alpha * (liquid ? 0.45f : 1.0f));
        context.method_25294(0, 0, width, height, MenuKit.withAlpha(526861, dim));
        if (liquid) {
            float haze = (float)options.glassTint.get().intValue() / 100.0f * alpha;
            context.method_25296(0, 0, width, height, MenuKit.withAlpha(Theme.glassTint(), Math.round(80.0f * haze)), MenuKit.withAlpha(656912, Math.round(112.0f * haze)));
        }
    }

    static Icons.Icon icon(Category category) {
        return switch (category) {
            default -> throw new MatchException(null, null);
            case Category.BASE_FINDING -> Icons.Icon.SEARCH;
            case Category.DONUT -> Icons.Icon.DONUT;
            case Category.COMBAT, Category.MACRO -> Icons.Icon.SWORD;
            case Category.VISUALS, Category.RENDER -> Icons.Icon.EYE;
            case Category.MOVEMENT -> Icons.Icon.SPEED;
            case Category.CLIENT -> Icons.Icon.GEAR;
            case Category.CONFIG -> Icons.Icon.COLUMNS;
        };
    }

    static Icons.Icon icon(Module module) {
        String name = module.name.toLowerCase(Locale.ROOT);
        if (name.equals("sky")) {
            return Icons.Icon.MOON;
        }
        if (name.contains("glint")) {
            return Icons.Icon.SPARKLE;
        }
        if (name.contains("accessories") || name.contains("crown")) {
            return Icons.Icon.CROWN;
        }
        if (name.contains("crosshair")) {
            return Icons.Icon.CROSSHAIR;
        }
        if (name.contains("freecam")) {
            return Icons.Icon.CAMERA;
        }
        if (name.contains("jump circle")) {
            return Icons.Icon.RINGS;
        }
        if (name.contains("name tag")) {
            return Icons.Icon.TAG;
        }
        if (name.contains("skin")) {
            return Icons.Icon.PERSON;
        }
        if (name.contains("swing")) {
            return Icons.Icon.SWING;
        }
        if (name.contains("weather")) {
            return Icons.Icon.CLOUD;
        }
        if (name.contains("key") || name.contains("typing")) {
            return Icons.Icon.KEY;
        }
        if (name.contains("blur") || name.contains("speed") || name.contains("fly") || name.contains("sprint")) {
            return Icons.Icon.SPEED;
        }
        if (name.contains("look") || name.contains("esp") || name.contains("inspector") || name.contains("hider")) {
            return Icons.Icon.EYE;
        }
        if (name.contains("overlay") || name.contains("model") || name.contains("chunk")) {
            return Icons.Icon.CUBE;
        }
        if (name.contains("tool") || name.contains("mine")) {
            return Icons.Icon.PICKAXE;
        }
        if (name.contains("sell") || name.contains("shop") || name.contains("auction") || name.contains("inventory")) {
            return Icons.Icon.CHEST;
        }
        if (name.contains("beam") || name.contains("live") || name.contains("notifier")) {
            return Icons.Icon.PULSE;
        }
        if (name.contains("hud") || name.contains("menu") || name.contains("spotify") || name.contains("map")) {
            return Icons.Icon.WINDOW;
        }
        if (name.contains("click gui") || name.contains("ribbon")) {
            return Icons.Icon.GRID;
        }
        if (name.contains("config")) {
            return Icons.Icon.COLUMNS;
        }
        if (name.contains("totem") || name.contains("crystal") || name.contains("anchor")) {
            return Icons.Icon.SPARKLE;
        }
        if (name.contains("rtp") || name.contains("relog")) {
            return Icons.Icon.CHEVRONS;
        }
        return MenuKit.icon(module.category);
    }

    static void iconBox(class_332 context, Module module, float x, float y, float size, float on, float opacity) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int is = Math.round(size);
        int r = Math.max(3, Math.round(size * 0.3f));
        int accent = MenuKit.accent();
        if (on > 0.01f) {
            Smooth.glow(context, ix + is / 2, iy + is / 2, Math.round(size * 0.95f), MenuKit.alpha(MenuKit.withAlpha(accent, 80), on * opacity));
        }
        Smooth.rect(context, ix, iy, is, is, r, MenuKit.alpha(MenuKit.lerp(0x12FFFFFF, MenuKit.withAlpha(accent, 68), on), opacity));
        Smooth.outline(context, ix, iy, is, is, r, MenuKit.alpha(MenuKit.lerp(0x22FFFFFF, MenuKit.withAlpha(accent, 144), on), opacity));
        int glyph = Math.round(size * 0.52f);
        Icons.draw(context, MenuKit.icon(module), ix + (is - glyph) / 2, iy + (is - glyph) / 2, glyph, MenuKit.alpha(MenuKit.lerp(-420285451, MenuKit.lerp(accent, -1, 0.55f), on), opacity));
    }

    static void touch(Module module) {
        if (module == null) {
            return;
        }
        RECENT.remove(module);
        RECENT.addFirst(module);
        while (RECENT.size() > 4) {
            RECENT.removeLast();
        }
    }

    static List<Module> recent() {
        return new ArrayList<Module>(RECENT);
    }

    static int count(Category category) {
        int count = 0;
        for (Module module : SoberClient.MODULES.all()) {
            if (module.category != category) continue;
            ++count;
        }
        return count;
    }

    static boolean opensMenu(Module module) {
        return module instanceof ClickGui || module instanceof Configs;
    }

    public static class_437 menu() {
        ClickGui options = MenuKit.options();
        return options != null && options.style.get() == ClickGui.Style.Panel ? new PanelScreen() : new DropdownScreen();
    }

    static int presetCount(Module module) {
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            return presets.presetCount();
        }
        return module instanceof ClickGui ? THEMES.length : 0;
    }

    static int themeCount() {
        return THEMES.length;
    }

    static int themeColor(int index) {
        int n;
        Object value = THEMES[index][1];
        if (value instanceof Color) {
            Color color = (Color)value;
            n = color.packed() & 0xFFFFFF;
        } else {
            n = -1;
        }
        return n;
    }

    static String themeName(int index) {
        return (String)THEMES[index][0];
    }

    static String presetName(Module module, int index) {
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            return presets.presetName(index);
        }
        return (String)THEMES[index][0];
    }

    static boolean presetActive(Module module, int index) {
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            return presets.presetActive(index);
        }
        ClickGui gui = MenuKit.options();
        Object value = THEMES[index][1];
        if (value instanceof ClickGui.AccentMode) {
            ClickGui.AccentMode mode = (ClickGui.AccentMode)((Object)value);
            return gui.accentMode.get() == mode;
        }
        return gui.accentMode.get() == ClickGui.AccentMode.Static && gui.accent.get().equals(value);
    }

    static String presetDescription(Module module) {
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            return presets.presetDescription();
        }
        return "Applies this accent color to the menus and the HUD.";
    }

    static class_1799 presetIcon(Module module, int index) {
        class_1799 class_17992;
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            class_17992 = presets.presetIcon(index);
        } else {
            class_17992 = class_1799.field_8037;
        }
        return class_17992;
    }

    static boolean presetIcons(Module module) {
        Presets presets;
        return module instanceof Presets && (presets = (Presets)((Object)module)).presetCount() > 1 && !presets.presetIcon(1).method_7960();
    }

    static void presetItem(class_332 context, class_1799 stack, float cx, float cy, float size) {
        if (stack.method_7960()) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(cx - size / 2.0f, cy - size / 2.0f);
        matrices.scale(size / 16.0f, size / 16.0f);
        context.method_51427(stack, 0, 0);
        matrices.popMatrix();
    }

    static void selectPreset(Module module, int index) {
        if (module instanceof Presets) {
            Presets presets = (Presets)((Object)module);
            presets.selectPreset(index);
            return;
        }
        ClickGui gui = MenuKit.options();
        Object value = THEMES[index][1];
        if (value instanceof ClickGui.AccentMode) {
            ClickGui.AccentMode mode = (ClickGui.AccentMode)((Object)value);
            gui.accentMode.set(mode);
        } else {
            gui.accentMode.set(ClickGui.AccentMode.Static);
            gui.accent.set((Color)value);
        }
    }

    public static String keyName(int key) {
        if (key < 0) {
            return "None";
        }
        return class_3675.class_307.field_1668.method_1447(key).method_27445().getString();
    }

    static void openLink(String link) {
        if (link == null || link.isEmpty()) {
            return;
        }
        try {
            class_156.method_668().method_673(URI.create(link));
        }
        catch (RuntimeException e) {
            SoberClient.LOG.warn("Could not open {}", (Object)link, (Object)e);
        }
    }
}

