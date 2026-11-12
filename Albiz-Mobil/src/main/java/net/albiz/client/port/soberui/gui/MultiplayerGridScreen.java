// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Avatars;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.ExactImage;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.Logo3D;
import net.albiz.client.port.soberui.gui.Mono;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.mixin.ClickableWidgetAccessor;
import net.albiz.client.port.soberui.mixin.LanServerEntryAccessor;
import net.albiz.client.port.soberui.mixin.MultiplayerScreenAccessor;
import net.albiz.client.port.soberui.mixin.ServerEntryAccessor;
import net.albiz.client.port.soberui.modules.MainMenu;
import net.albiz.client.port.soberui.modules.NameTags;
import net.albiz.client.port.soberui.modules.SkinChange;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_1131;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_4267;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_642;
import net.minecraft.class_7417;
import net.minecraft.class_7919;
import net.minecraft.class_8573;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public class MultiplayerGridScreen
extends class_500 {
    private static final float WIDTH = 860.0f;
    private static final float HEIGHT = 440.0f;
    private static final float GRID_X = 54.0f;
    private static final float GRID_Y = 124.0f;
    private static final float GRID_W = 752.0f;
    private static final float CARD_H = 74.0f;
    private static final float GAP = 10.0f;
    private static final float ROW = 84.0f;
    private static final float GRID_BOTTOM = 380.0f;
    private static final float DOCK_Y = 392.0f;
    private static final float DOCK_H = 38.0f;
    private static final float SEARCH_X = 552.0f;
    private static final float SEARCH_Y = 72.0f;
    private static final float SEARCH_W = 192.0f;
    private static final float FIELD_H = 26.0f;
    private static final float REFRESH_X = 752.0f;
    private static final int TEXT = -855051;
    private static final int MUTED = -4275510;
    private static final int FAINT = -7367779;
    private static final int CARD = 0x14FFFFFF;
    private static final int CARD_HOVER = 0x22FFFFFF;
    private static final int EDGE = 0x1FFFFFFF;
    private static final class_2960 UNKNOWN = class_2960.method_60656((String)"textures/misc/unknown_server.png");
    private static final String[] DOCK = new String[]{"selectServer.select", "selectServer.direct", "selectServer.add", "selectServer.edit", "selectServer.delete", "selectServer.refresh", "gui.back"};
    private static final float[] DOCK_W = new float[]{100.0f, 124.0f, 96.0f, 64.0f, 70.0f, 74.0f, 64.0f};
    private static final String ADD = "add";
    private static final String DIRECT = "direct";
    private static final String SEARCH = "search";
    private static final String REFRESH = "refresh";
    private static final ExactImage LOGO = new ExactImage("textures/brand/lockup.png");
    private static final float COL_W = 440.0f;
    private static final float COL_X = 210.0f;
    private static final float HEAD_Y = 40.0f;
    private static final float LIST_Y = 96.0f;
    private static final float LIST_BOTTOM = 350.0f;
    private static final float ROW_H = 38.0f;
    private static final float ROW_STEP = 44.0f;
    private static final float ACTION_H = 30.0f;
    private static final float LSEARCH_W = 170.0f;
    private static final float LSEARCH_X = 448.0f;
    private static final float LREFRESH_X = 624.0f;
    private static final int CREAM = -723723;
    private static final int CREAM_MUTED = -4934468;
    private static final int CREAM_FAINT = -7434602;
    private static final int INK = -15856112;
    private static final String[] ACTIONS = new String[]{"selectServer.select", "selectServer.add", "selectServer.direct", "gui.back"};
    private final Map<String, class_4185> buttons = new HashMap<String, class_4185>();
    private final Set<class_364> vanilla = Collections.newSetFromMap(new IdentityHashMap());
    private final Map<Object, Float> hover = new IdentityHashMap<Object, Float>();
    private final Map<Object, Float> chosen = new IdentityHashMap<Object, Float>();
    private final List<Card> cards = new ArrayList<Card>();
    private final float[] dividers = new float[2];
    private class_342 search;
    private Supplier<class_8685> skin;
    private float scale = 1.0f;
    private float originX;
    private float originY;
    private float dockX;
    private float dockW;
    private float scroll;
    private float scrollTarget;
    private float content;
    private float delta;
    private float fade = 1.0f;
    private float colShift;
    private float listView = 254.0f;
    private long openedAt;
    private long lastFrame;
    private boolean arranged;
    private List<class_2561> tooltip;
    private List<class_5481> orderedTooltip;
    private final List<class_339> foreign = new ArrayList<class_339>();
    private int tooltipX;
    private int tooltipY;

    public MultiplayerGridScreen(class_437 parent) {
        super(parent);
    }

    public static class_437 replace(class_500 screen) {
        return new MultiplayerGridScreen(((MultiplayerScreenAccessor)screen).sober$parent());
    }

    protected void method_25426() {
        super.method_25426();
        try{
        if (this.openedAt == 0L) {
            this.openedAt = System.nanoTime();
        }
        this.vanilla.clear();
        this.buttons.clear();
        this.vanilla.add((class_364)this.field_3043);
        this.method_37066((class_364)this.field_3043);
        for (class_364 element : List.copyOf(this.method_25396())) {
            if (!(element instanceof class_339)) continue;
            class_339 widget = (class_339)element;
            String key = MultiplayerGridScreen.key(widget.method_25369());
            if (key.equals("multiplayer.title")) {
                widget.field_22764 = false;
                this.vanilla.add((class_364)widget);
                continue;
            }
            for (String dock : DOCK) {
                if (!dock.equals(key) || !(widget instanceof class_4185)) continue;
                class_4185 button = (class_4185)widget;
                this.buttons.put(key, button);
                this.vanilla.add((class_364)widget);
            }
        }
        class_4185 join = this.buttons.get(DOCK[0]);
        if (join != null) {
            ScreenStyle.primary(join);
        }
        for (int i = 1; i < DOCK.length; ++i) {
            class_4185 button = this.buttons.get(DOCK[i]);
            if (button == null) continue;
            ScreenStyle.flat(button);
        }
        this.search = new class_342(this.field_22793, 0, 0, 100, 10, (class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Search servers")));
        this.search.method_1858(false);
        this.search.method_71503(false);
        this.search.method_1868(-855051);
        this.search.method_1880(48);
        this.search.method_47404((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display(MultiplayerGridScreen.soberStyle() ? "Search servers" : "Search servers...")).method_10862(Theme.crispStyle().method_36139(MultiplayerGridScreen.soberStyle() ? 9342614 : 7040888)));
        this.search.method_73210(Theme.fieldFormatter());
        this.search.method_1863(value -> {
            this.scrollTarget = 0.0f;
        });
        this.method_37063(this.search);
        this.vanilla.add((class_364)this.search);
        this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
        this.arranged = false;
        this.layout();
        renderFailed=false;
        }catch(RuntimeException error){renderFailed=true;SoberClient.LOG.error("Albiz multiplayer initialization failed",error);}
    }

    protected void method_48640() {
        super.method_48640();
        if (this.search != null) {
            this.layout();
            this.arranged = false;
        }
    }

    private void layout() {
        this.scale = Math.max(0.45f, Math.min((float)this.field_22789 / 860.0f, (float)this.field_22790 / 440.0f));
        this.originX = Math.round(((float)this.field_22789 - 860.0f * this.scale) / 2.0f);
        this.originY = Math.round(((float)this.field_22790 - 440.0f * this.scale) / 2.0f);
        if (MultiplayerGridScreen.listMode()) {
            this.search.method_55444(Math.round(130.0f * this.scale), 10, this.px(476.0f), this.py(40.0f) + Math.round((26.0f * this.scale - 9.0f) / 2.0f));
            for (class_4185 button : this.buttons.values()) {
                button.field_22764 = false;
            }
            return;
        }
        this.search.method_55444(Math.round(158.0f * this.scale), 10, this.px(578.0f), this.py(72.0f) + Math.round((26.0f * this.scale - 9.0f) / 2.0f));
        if (MultiplayerGridScreen.soberStyle()) {
            for (class_4185 button : this.buttons.values()) {
                button.field_22764 = false;
            }
            return;
        }
        float total = 32.0f;
        for (float w : DOCK_W) {
            total += w;
        }
        this.dockW = total += (float)(DOCK.length - 1) * 6.0f;
        this.dockX = (860.0f - total) / 2.0f;
        float x = this.dockX + 8.0f;
        int divider = 0;
        for (int i = 0; i < DOCK.length; ++i) {
            class_4185 button;
            if (i == 3 || i == 5) {
                this.dividers[divider++] = x + 1.0f;
                x += 8.0f;
            }
            if ((button = this.buttons.get(DOCK[i])) != null) {
                button.method_55444(Math.round(DOCK_W[i] * this.scale), Math.round(26.0f * this.scale), this.px(x), this.py(398.0f));
            }
            x += DOCK_W[i] + 6.0f;
        }
    }

    private void arrange() {
        this.foreign.clear();
        for (class_364 element : this.method_25396()) {
            class_339 widget;
            if (!(element instanceof class_339) || this.vanilla.contains(widget = (class_339)element) || !widget.field_22764 || widget.method_46427() >= this.py(52.0f)) continue;
            this.foreign.add(widget);
        }
        this.foreign.sort(Comparator.comparingInt(class_339::method_46426));
        for (class_339 widget : this.foreign) {
            widget.field_22764 = false;
        }
    }

    private List<float[]> foreignBoxes() {
        ArrayList<float[]> boxes = new ArrayList<float[]>();
        float right = MultiplayerGridScreen.vanillaPlus() || MultiplayerGridScreen.listMode() ? 836.0f : this.clockLeft() - 10.0f;
        for (int i = this.foreign.size() - 1; i >= 0; --i) {
            String label = MultiplayerGridScreen.plain(this.foreign.get(i).method_25369());
            float w = Math.max(40.0f, this.textWidth(label, 1.0f) + 22.0f);
            boxes.add(0, new float[]{right -= w, 13.0f, w, 24.0f});
            right -= 6.0f;
        }
        return boxes;
    }

    private static String plain(class_2561 text) {
        StringBuilder out = new StringBuilder();
        if (text != null) {
            for (ScreenStyle.Run run : ScreenStyle.runs((class_5348)text, -1)) {
                out.append(run.text());
            }
        }
        return out.toString();
    }

    private void foreignButtons(class_332 context, double mx, double my, int mouseX, int mouseY) {
        List<float[]> boxes = this.foreignBoxes();
        int accent = MultiplayerGridScreen.accent();
        for (int i = 0; i < this.foreign.size(); ++i) {
            class_7919 tip;
            ClickableWidgetAccessor access;
            class_339 widget = this.foreign.get(i);
            float[] b = boxes.get(i);
            boolean over = MultiplayerGridScreen.inside(mx, my, b[0], b[1], b[2], b[3]);
            float lit = this.animate(this.hover, widget, over && widget.field_22763, 12.0f);
            if (!this.glass(context, b[0], b[1], b[2], b[3], 12)) {
                this.rect(context, b[0], b[1], b[2], b[3], 12, MultiplayerGridScreen.lerp(0x14FFFFFF, 0x22FFFFFF, lit));
                this.outline(context, b[0], b[1], b[2], b[3], 12, 0x1FFFFFFF);
            }
            this.neon(context, b[0], b[1], b[2], b[3], 12, accent, lit);
            float cx = b[0] + (b[2] - this.textWidth(MultiplayerGridScreen.plain(widget.method_25369()), 1.0f)) / 2.0f;
            for (ScreenStyle.Run run2 : ScreenStyle.runs((class_5348)widget.method_25369(), widget.field_22763 ? -855051 : -4275510)) {
                this.text(context, run2.text(), cx, b[1] + 8.0f, 1.0f, run2.color());
                cx += this.textWidth(run2.text(), 1.0f);
            }
            if (!over || !(widget instanceof ClickableWidgetAccessor) || (access = (ClickableWidgetAccessor)(Object)widget).sober$tooltip() == null || (tip = access.sober$tooltip().method_56137()) == null) continue;
            this.orderedTooltip = tip.method_47405(this.field_22787);
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean clickForeign(class_11909 click, double mx, double my, boolean doubled) {
        List<float[]> boxes = this.foreignBoxes();
        for (int i = 0; i < this.foreign.size(); ++i) {
            float[] b = boxes.get(i);
            if (!MultiplayerGridScreen.inside(mx, my, b[0], b[1], b[2], b[3])) continue;
            class_339 widget = this.foreign.get(i);
            widget.field_22764 = true;
            try {
                widget.method_25402(new class_11909((double)widget.method_46426() + (double)widget.method_25368() / 2.0, (double)widget.method_46427() + (double)widget.method_25364() / 2.0, click.comp_4800()), doubled);
            }
            finally {
                if (this.field_22787.field_1755 == this) {
                    widget.field_22764 = false;
                }
            }
            return true;
        }
        return false;
    }

    private static String key(class_2561 text) {
        String string;
        class_7417 class_74172;
        if (text != null && (class_74172 = text.method_10851()) instanceof class_2588) {
            class_2588 translatable = (class_2588)class_74172;
            string = translatable.method_11022();
        } else {
            string = "";
        }
        return string;
    }

    private int px(float x) {
        return Math.round(this.originX + x * this.scale);
    }

    private int py(float y) {
        return Math.round(this.originY + y * this.scale);
    }

    private static boolean soberStyle() {
        return ScreenStyle.soberStyle();
    }

    private static boolean listMode() {
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        return MultiplayerGridScreen.soberStyle() && menu != null && menu.serverLayout.get() == MainMenu.ServerLayout.List;
    }

    private float listTop() {
        return MultiplayerGridScreen.listMode() ? 96.0f + this.colShift : 124.0f;
    }

    private float listBottom() {
        return MultiplayerGridScreen.listMode() ? 96.0f + this.colShift + this.listView : 380.0f;
    }

    private float headY() {
        return 40.0f + this.colShift;
    }

    private float actionY() {
        return this.listBottom() + 14.0f;
    }

    private float[] chipBox() {
        float w = Math.max(130.0f, 34.0f + CrispText.width(this.shownName(), "montserrat-bold.ttf", 9.0f, 0.0f) + 30.0f);
        return new float[]{806.0f - w, 9.0f, w, 32.0f};
    }

    private void soberPanel(class_332 context, float x, float y, float w, float h, int r, float hover, float selected) {
        ScreenStyle.soberPanel(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, hover, selected, this.fade);
    }

    private void spaced(class_332 context, String value, float x, float baseline, float em, int color) {
        CrispText.draw(context, value, "montserrat-bold.ttf", x, baseline, em, 0.5f, this.f(color));
    }

    private static int accent() {
        if (MultiplayerGridScreen.soberStyle()) {
            return -1513234;
        }
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        if (menu != null && menu.style.get() == MainMenu.Style.VanillaPlus) {
            return -1184014;
        }
        return 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
    }

    private static boolean vanillaPlus() {
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        return menu != null && menu.style.get() == MainMenu.Style.VanillaPlus;
    }

    private void base(class_332 context, float x, float y, float w, float h, int r) {
        if (MultiplayerGridScreen.vanillaPlus()) {
            this.rect(context, x, y, w, h, r, 0x70000000);
        }
    }

    private static int alpha(int color, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;
    }

    private int f(int color) {
        return MultiplayerGridScreen.alpha(color, (float)(color >>> 24) / 255.0f * this.fade);
    }

    private static int lerp(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
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

    private static float quint(float p) {
        float inverse = 1.0f - Math.max(0.0f, Math.min(1.0f, p));
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    private float animate(Map<Object, Float> map, Object key, boolean on, float speed) {
        float value = map.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        value += ((on ? 1.0f : 0.0f) - value) * (1.0f - (float)Math.exp(-speed * this.delta));
        float f = on ? 1.0f : 0.0f;
        if (Math.abs(f - value) < 0.002f) {
            value = on ? 1.0f : 0.0f;
        }
        map.put(key, Float.valueOf(value));
        return value;
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    private class_2561 styled(String value, float size) {
        return size >= 1.6f ? Theme.large(value) : (size >= 1.1f ? Theme.bold(value) : Theme.text(value));
    }

    private void text(class_332 context, String value, float x, float y, float size, int color) {
        value=net.albiz.client.i18n.AlbizI18n.display(value);
        if (ScreenStyle.formatted(value)) {
            float cx = x;
            for (ScreenStyle.Run run : ScreenStyle.runs(class_5348.method_29430((String)value), color)) {
                this.text(context, run.text(), cx, y, size, run.color());
                cx += this.textWidth(run.text(), size);
            }
            return;
        }
        String file = Theme.crispFile(size >= 1.1f);
        if (!CrispText.canDisplay(value, file)) {
            this.text(context, this.styled(value, size), x, y, size, color);
            return;
        }
        int shown = this.f(ScreenStyle.readable(color));
        float em = 9.4f * size;
        float baseline = y + 7.6f * size;
        if (Theme.liquidGlass()) {
            CrispText.draw(context, value, file, x + 0.5f, baseline + 0.7f, em, 0.0f, MultiplayerGridScreen.alpha(0, (float)(shown >>> 24) / 255.0f * 0.3f));
        }
        CrispText.draw(context, value, file, x, baseline, em, 0.0f, shown);
    }

    private List<List<Run>> runs(class_2561 value, int fallback) {
        ArrayList<List<Run>> lines = new ArrayList<List<Run>>();
        lines.add(new ArrayList());
        if (value == null) {
            return lines;
        }
        value.method_27658((style, string) -> {
            int color = style.method_10973() != null ? 0xFF000000 | style.method_10973().method_27716() : fallback;
            String[] parts = string.split("\n", -1);
            for (int i = 0; i < parts.length; ++i) {
                if (i > 0) {
                    lines.add(new ArrayList());
                }
                if (parts[i].isEmpty()) continue;
                ((List)lines.get(lines.size() - 1)).add(new Run(parts[i], color));
            }
            return Optional.empty();
        }, class_2583.field_24360);
        return lines;
    }

    private float runsWidth(List<Run> line, float size) {
        float width = 0.0f;
        for (Run run : line) {
            width += this.textWidth(run.text(), size);
        }
        return width;
    }

    private void drawRuns(class_332 context, List<Run> line, float x, float y, float size, float maxWidth) {
        float cx = x;
        float limit = x + maxWidth;
        for (Run run : line) {
            String value = run.text();
            float width = this.textWidth(value, size);
            if (cx + width > limit) {
                String cut = value;
                while (!cut.isEmpty() && cx + this.textWidth(cut + "...", size) > limit) {
                    cut = cut.substring(0, cut.length() - 1);
                }
                this.text(context, cut.stripTrailing() + "...", cx, y, size, run.color());
                return;
            }
            this.text(context, value, cx, y, size, run.color());
            cx += width;
        }
    }

    private List<List<Run>> wrap(List<List<Run>> lines, float size, float maxWidth) {
        if (lines.size() != 1 || this.runsWidth(lines.get(0), size) <= maxWidth) {
            return lines;
        }
        ArrayList<Run> first = new ArrayList<Run>();
        ArrayList<Run> second = new ArrayList<Run>();
        float used = 0.0f;
        boolean full = false;
        for (Run run : lines.get(0)) {
            for (String word : run.text().split("(?<= )")) {
                float width = this.textWidth(word, size);
                if (!full && used + width <= maxWidth) {
                    first.add(new Run(word, run.color()));
                    used += width;
                    continue;
                }
                full = true;
                second.add(new Run(second.isEmpty() ? word.stripLeading() : word, run.color()));
            }
        }
        return List.of(first, second);
    }

    private void text(class_332 context, class_2561 value, float x, float y, float size, int color) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();try{
        matrices.translate(x, y);
        matrices.scale(size, size);
        context.method_51439(this.field_22793, value, 0, 0, this.f(ScreenStyle.readable(color)), Theme.liquidGlass());
        }finally{matrices.popMatrix();}
    }

    private boolean glass(class_332 context, float x, float y, float w, float h, int r) {
        if (MultiplayerGridScreen.vanillaPlus()) {
            return false;
        }
        return ScreenStyle.glass(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.fade);
    }

    private void neon(class_332 context, float x, float y, float w, float h, int r, int color, float strength) {
        if (MultiplayerGridScreen.vanillaPlus()) {
            return;
        }
        ScreenStyle.neon(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, color, strength * this.fade);
    }

    private void topLight(class_332 context, float x, float y, float w, int r, float strength) {
        if (MultiplayerGridScreen.vanillaPlus()) {
            return;
        }
        ScreenStyle.topLight(context, Math.round(x), Math.round(y), Math.round(w), r, strength * this.fade);
    }

    private float textWidth(String value, float size) {
        value=net.albiz.client.i18n.AlbizI18n.display(value);
        if (ScreenStyle.formatted(value)) {
            float width = 0.0f;
            for (ScreenStyle.Run run : ScreenStyle.runs(class_5348.method_29430((String)value), -1)) {
                width += this.textWidth(run.text(), size);
            }
            return width;
        }
        String file = Theme.crispFile(size >= 1.1f);
        if (!CrispText.canDisplay(value, file)) {
            return (float)this.field_22793.method_27525((class_5348)this.styled(value, size)) * size;
        }
        return CrispText.width(value, file, 9.4f * size, 0.0f);
    }

    private float textWidth(class_2561 value, float size) {
        return (float)this.field_22793.method_27525((class_5348)value) * size;
    }

    private String fit(String value, float max, float size) {
        if (value == null) {
            return "";
        }
        if (this.textWidth(value, size) <= max) {
            return value;
        }
        String cut = value;
        while (cut.length() > 1 && this.textWidth(cut + "...", size) > max) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
        Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), MultiplayerGridScreen.vanillaPlus() && r > 4 ? 3 : r, this.f(color));
    }

    private void outline(class_332 context, float x, float y, float w, float h, int r, int color) {
        Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), MultiplayerGridScreen.vanillaPlus() && r > 4 ? 3 : r, this.f(color));
    }

    private String shownName() {
        String custom = NameTags.customName().trim();
        if (!custom.isEmpty()) {
            return custom;
        }
        return this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
    }

    private class_8685 worn(class_8685 original) {
        SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
        return skins != null && skins.isActive() ? skins.selfSkin(original) : original;
    }

    private static String clock() {
        LocalTime local = LocalTime.now();
        return String.format("%02d:%02d", local.getHour(), local.getMinute());
    }

    private float clockLeft() {
        if (MultiplayerGridScreen.soberStyle()) {
            return this.chipBox()[0] - 12.0f - CrispText.width(MultiplayerGridScreen.clock(), "montserrat-bold.ttf", 9.0f, 0.0f);
        }
        int pill = Math.round(30.0f + this.textWidth(this.shownName(), 1.0f));
        return (float)(830 - pill - 12) - this.textWidth(MultiplayerGridScreen.clock(), 1.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void group(class_332 context, float time, float delay, Runnable draw) {
        float in = MultiplayerGridScreen.quint((time - delay) / 0.6f);
        if (in <= 0.002f) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(0.0f, 10.0f * (1.0f - in));
        this.fade = in;
        try {
            draw.run();
        }
        finally {
            this.fade = 1.0f;
            matrices.popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void poll(class_332 context, float deltaTicks) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(-100000.0f, -100000.0f);
        try {
            for (class_4267.class_504 entry : this.field_3043.method_25396()) {
                if (!(entry instanceof class_4267.class_4270)) continue;
                class_4267.class_4270 server = (class_4267.class_4270)entry;
                server.method_25343(context, -1, -1, false, deltaTicks);
            }
        }
        catch (RuntimeException e) {
            SoberClient.LOG.debug("Server entry update failed", (Throwable)e);
        }
        finally {
            matrices.popMatrix();
        }
    }

    private void buildCards() {
        this.cards.clear();
        String query = this.search == null ? "" : this.search.method_1882().trim().toLowerCase(Locale.ROOT);
        ArrayList<Object> items = new ArrayList<Object>();
        for (class_4267.class_504 entry : this.field_3043.method_25396()) {
            class_4267.class_4269 lan;
            class_642 info;
            if (entry instanceof class_4267.class_4270) {
                class_4267.class_4270 server = (class_4267.class_4270)entry;
                info = server.method_20133();
                if (!query.isEmpty() && !info.field_3752.toLowerCase(Locale.ROOT).contains(query) && !info.field_3761.toLowerCase(Locale.ROOT).contains(query)) continue;
                items.add(server);
                continue;
            }
            if (!(entry instanceof class_4267.class_4269))continue;
            lan=(class_4267.class_4269)entry;
            class_1131 lanInfo=((LanServerEntryAccessor)(Object)lan).sober$server();
            if (!query.isEmpty() && !lanInfo.method_4813().toLowerCase(Locale.ROOT).contains(query) && !lanInfo.method_4812().contains(query))continue;
            items.add(lan);
        }
        if (MultiplayerGridScreen.listMode()) {
            this.content = items.isEmpty() ? 0.0f : (float)items.size() * 44.0f - 6.0f;
            this.listView = Math.min(254.0f, Math.max(this.content, 82.0f));
            this.colShift = Math.round((254.0f - this.listView) / 2.0f);
            for (int i = 0; i < items.size(); ++i) {
                Object item = items.get(i);
                this.cards.add(new Card(item, (item) instanceof class_4267.class_4269 ? 1 : 0, 210.0f, this.listTop() + (float)i * 44.0f, 440.0f, 38.0f));
            }
            this.scrollTarget = this.clampScroll(this.scrollTarget);
            this.search.method_46419(this.py(this.headY()) + Math.round((26.0f * this.scale - 9.0f) / 2.0f));
            return;
        }
        if (query.isEmpty()) {
            items.add(ADD);
            items.add(DIRECT);
        }
        float cw = 244.0f;
        for (int i = 0; i < items.size(); ++i) {
            Object item = items.get(i);
            int kind = item == ADD ? 2 : (item == DIRECT ? 3 : (item instanceof class_4267.class_4269 ? 1 : 0));
            this.cards.add(new Card(item, kind, 54.0f + (float)(i % 3) * (cw + 10.0f), 124.0f + (float)(i / 3) * 84.0f, cw, 74.0f));
        }
        int rows = (items.size() + 2) / 3;
        this.content = rows <= 0 ? 0.0f : (float)rows * 84.0f - 10.0f;
        this.scrollTarget = this.clampScroll(this.scrollTarget);
    }

    private float clampScroll(float value) {
        return Math.max(0.0f, Math.min(value, Math.max(0.0f, this.content - (this.listBottom() - this.listTop()))));
    }

    private boolean renderFailed;
    public void method_25394(class_332 context,int mouseX,int mouseY,float deltaTicks){
      if(!renderFailed){try{this.paint(context,mouseX,mouseY,deltaTicks);return;}catch(RuntimeException ex){renderFailed=true;SoberClient.LOG.error("Albiz multiplayer render failed; remaining on the custom screen",ex);}}
      context.method_25294(0,0,field_22789,field_22790,0xff08090c);
      context.method_25303(field_22787.field_1772,net.albiz.client.i18n.AlbizI18n.display("Multiplayer could not be displayed. Escape: back. See latest.log."),16,24,0xffff6a70);
    }

    private void paint(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        long nano = System.nanoTime();
        this.delta = this.lastFrame == 0L ? 0.0f : Math.min(0.05f, (float)(nano - this.lastFrame) / 1.0E9f);
        this.lastFrame = nano;
        if (!this.arranged) {
            this.arranged = true;
            this.arrange();
        }
        this.poll(context, deltaTicks);
        this.buildCards();
        this.scroll += (this.scrollTarget - this.scroll) * (1.0f - (float)Math.exp(-16.0f * this.delta));
        float time = (float)(nano - this.openedAt) / 1.0E9f;
        double mx = ((float)mouseX - this.originX) / this.scale;
        double my = ((float)mouseY - this.originY) / this.scale;
        this.tooltip = null;
        this.orderedTooltip = null;
        Matrix3x2fStack matrices = context.method_51448();
        boolean list = MultiplayerGridScreen.listMode();
        float top = this.listTop();
        float bottom = this.listBottom();
        float left = list ? 210.0f : 54.0f;
        float wide = list ? 440.0f : 752.0f;
        matrices.pushMatrix();try{
        matrices.translate(this.originX, this.originY);
        matrices.scale(this.scale, this.scale);
        if (list) {
            this.group(context, time, 0.0f, () -> this.listHeader(context, mx, my));
            this.group(context, time, 0.03f, () -> this.foreignButtons(context, mx, my, mouseX, mouseY));
            this.group(context, time, 0.16f, () -> this.actions(context, mx, my, mouseX, mouseY));
            if (this.cards.isEmpty()) {
                this.group(context, time, 0.1f, () -> this.emptyList(context));
            }
        } else {
            this.group(context, time, 0.0f, () -> this.topBar(context));
            this.group(context, time, 0.03f, () -> this.foreignButtons(context, mx, my, mouseX, mouseY));
            this.group(context, time, 0.06f, () -> this.header(context, mx, my));
            if (MultiplayerGridScreen.soberStyle()) {
                this.group(context, time, 0.18f, () -> this.actions(context, mx, my, mouseX, mouseY));
            } else {
                this.group(context, time, 0.18f, () -> this.dock(context));
            }
        }
        }finally{matrices.popMatrix();}
        context.method_44379(this.px(left - 14.0f), this.py(top - 10.0f), this.px(left + wide + 14.0f), this.py(bottom + 8.0f));try{
        matrices.pushMatrix();try{
        matrices.translate(this.originX, this.originY);
        matrices.scale(this.scale, this.scale);
        boolean grid = my >= (double)(top - 10.0f) && my <= (double)(bottom + 8.0f);
        for (int i = 0; i < this.cards.size(); ++i) {
            Card card = this.cards.get(i);
            float y = card.y() - this.scroll;
            if (y + card.h() < top - 12.0f || y > bottom + 12.0f) continue;
            float delay = 0.1f + (float)Math.min(i, 12) * 0.035f;
            this.group(context, time, delay, () -> this.drawCard(context, card, y, grid, mx, my, mouseX, mouseY));
        }
        }finally{matrices.popMatrix();}
        }finally{context.method_44380();}
        if (this.content > bottom - top) {
            float view = bottom - top;
            float thumb = Math.max(24.0f, view * view / this.content);
            float travel = view - thumb;
            float at = top + (this.content - view <= 0.0f ? 0.0f : travel * this.scroll / (this.content - view));
            matrices.pushMatrix();try{
            matrices.translate(this.originX, this.originY);
            matrices.scale(this.scale, this.scale);
            this.rect(context, left + wide + 8.0f, at, 3.0f, thumb, 1, MultiplayerGridScreen.alpha(0xFFFFFF, 0.22f));
            }finally{matrices.popMatrix();}
        }
        super.method_25394(context, mouseX, mouseY, deltaTicks);
        if (this.tooltip != null && !this.tooltip.isEmpty()) {
            context.method_51434(this.field_22793, this.tooltip, this.tooltipX, this.tooltipY);
        } else if (this.orderedTooltip != null && !this.orderedTooltip.isEmpty()) {
            context.method_51447(this.field_22793, this.orderedTooltip, this.tooltipX, this.tooltipY);
        }
    }

    private void soberTopBar(class_332 context) {
        float lh = 26.0f;
        float lw = lh / LOGO.aspect();
        LOGO.draw(context, 54.0f, 12.0f, lw, this.f(-1));
        this.spaced(context, net.albiz.client.i18n.AlbizI18n.display("CLIENT"), 54.0f + lw + 8.0f, 29.0f, 6.6f, -1258291201);
        float[] chip = this.chipBox();
        this.soberPanel(context, chip[0], chip[1], chip[2], chip[3], 8, 0.0f, 0.0f);
        if (this.fade > 0.9f && this.skin != null) {
            Avatars.draw(context, this.worn(this.skin.get()), Math.round(chip[0] + 7.0f), Math.round(chip[1] + 6.0f), 20);
        }
        CrispText.draw(context, this.shownName(), "montserrat-bold.ttf", chip[0] + 34.0f, chip[1] + 15.0f, 9.0f, 0.0f, this.f(-1));
        this.spaced(context, net.albiz.client.i18n.AlbizI18n.display("IN MENU"), chip[0] + 34.0f, chip[1] + 25.0f, 6.6f, -1593835521);
        String clock = MultiplayerGridScreen.clock();
        CrispText.draw(context, clock, "montserrat-bold.ttf", this.clockLeft(), 29.0f, 9.0f, 0.0f, this.f(-922746881));
    }

    private void topBar(class_332 context) {
        if (MultiplayerGridScreen.soberStyle()) {
            this.soberTopBar(context);
            return;
        }
        if (MultiplayerGridScreen.vanillaPlus()) {
            return;
        }
        if (!this.glass(context, 20.0f, 8.0f, 820.0f, 34.0f, 17)) {
            this.base(context, 20.0f, 8.0f, 820.0f, 34.0f, 17);
            this.rect(context, 20.0f, 8.0f, 820.0f, 34.0f, 17, 0x12FFFFFF);
            this.outline(context, 20.0f, 8.0f, 820.0f, 34.0f, 17, 0x1FFFFFFF);
        }
        this.topLight(context, 20.0f, 8.0f, 820.0f, 17, 0.35f);
        Smooth.glow(context, 42, 25, 16, this.f(MultiplayerGridScreen.alpha(4059872, 0.3f)));
        Logo3D.draw(context, 42.0f, 25.0f, 20.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, this.fade, 0.7f);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("Albız"), 58.0f, 20.0f, 1.15f, -855051);
        String name = this.shownName();
        int pill = Math.round(30.0f + this.textWidth(name, 1.0f));
        int pillX = 830 - pill;
        if (!this.glass(context, pillX, 12.0f, pill, 26.0f, 13)) {
            this.base(context, pillX, 12.0f, pill, 26.0f, 13);
            this.rect(context, pillX, 12.0f, pill, 26.0f, 13, 0x14FFFFFF);
            this.outline(context, pillX, 12.0f, pill, 26.0f, 13, 0x1FFFFFFF);
        }
        if (this.fade > 0.9f && this.skin != null) {
            Avatars.draw(context, this.worn(this.skin.get()), pillX + 6, 17, 16);
        }
        this.text(context, name, (float)(pillX + 26), 21.0f, 1.0f, -855051);
        String clock = MultiplayerGridScreen.clock();
        this.text(context, clock, (float)(pillX - 12) - this.textWidth(clock, 1.0f), 21.0f, 1.0f, -855051);
    }

    private void soberHeader(class_332 context, double mx, double my) {
        CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display("Multiplayer"), "montserrat-bold.ttf", 54.0f, 98.0f, 30.0f, -0.01f, this.f(-1));
        int servers = 0;
        int lan = 0;
        for (class_4267.class_504 entry : this.field_3043.method_25396()) {
            if (entry instanceof class_4267.class_4270) {
                ++servers;
                continue;
            }
            if (!(entry instanceof class_4267.class_4269)) continue;
            ++lan;
        }
        String sub=net.albiz.client.i18n.AlbizI18n.isTurkish()?servers+" sunucu  ·  "+(lan>0?lan+" yerel ağ oyunu":"Yerel ağ oyunları aranıyor"):servers+(servers==1?" server":" servers")+"  ·  "+(lan>0?lan+" in your network":"Looking for LAN games");
        this.spaced(context, sub, 56.0f, 114.0f, 7.4f, -1761607681);
        boolean overSearch = MultiplayerGridScreen.inside(mx, my, 552.0f, 72.0f, 192.0f, 26.0f);
        float focus = this.animate(this.hover, SEARCH, this.search.method_25370(), 14.0f);
        float lit = this.animate(this.chosen, SEARCH, overSearch, 12.0f);
        this.soberPanel(context, 552.0f, 72.0f, 192.0f, 26.0f, 8, Math.max(lit, focus), focus * 0.6f);
        Icons.draw(context, Icons.Icon.SEARCH, Math.round(561.0f), Math.round(79.0f), 12, this.f(MultiplayerGridScreen.lerp(-1761607681, -1, focus)));
        boolean overRefresh = MultiplayerGridScreen.inside(mx, my, 752.0f, 72.0f, 26.0f, 26.0f);
        float spin = this.animate(this.hover, REFRESH, overRefresh, 12.0f);
        this.soberPanel(context, 752.0f, 72.0f, 26.0f, 26.0f, 8, spin, 0.0f);
        int cx = Math.round(765.0f);
        int cy = Math.round(85.0f);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();try{
        matrices.translate((float)cx, (float)cy);
        matrices.rotate(spin * (float)Math.PI);
        matrices.translate((float)(-cx), (float)(-cy));
        int color = this.f(MultiplayerGridScreen.lerp(-570425345, -1, spin));
        Smooth.ring(context, cx, cy, 6, color);
        Smooth.rect(context, cx + 2, cy - 7, 5, 5, 1, this.f(MultiplayerGridScreen.lerp(-15329253, -14934494, spin)));
        Smooth.triangle(context, (float)cx + 5.5f, (float)cy - 4.5f, 4.5f, color);
        }finally{matrices.popMatrix();}
    }

    private void header(class_332 context, double mx, double my) {
        if (MultiplayerGridScreen.soberStyle()) {
            this.soberHeader(context, mx, my);
            return;
        }
        int accent = MultiplayerGridScreen.accent();
        if (!Theme.liquidGlass() && !MultiplayerGridScreen.vanillaPlus()) {
            Smooth.glow(context, Math.round(134.0f), 88, 100, this.f(MultiplayerGridScreen.alpha(accent, 0.2f)));
        }
        for (int k = 0; k < 8 && !Theme.liquidGlass() && !MultiplayerGridScreen.vanillaPlus(); ++k) {
            double angle = Math.PI * 2 * (double)k / 8.0;
            CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display("Multiplayer"), "montserrat-bold.ttf", 54.0f + (float)Math.cos(angle) * 1.4f, 98.0f + (float)Math.sin(angle) * 1.4f, 30.0f, -0.01f, this.f(MultiplayerGridScreen.alpha(accent, 0.16f)));
        }
        CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display("Multiplayer"), MultiplayerGridScreen.vanillaPlus() ? "geist-semibold.ttf" : "montserrat-bold.ttf", 54.0f, 98.0f, MultiplayerGridScreen.vanillaPlus() ? 26.0f : 30.0f, -0.01f, this.f(-855051));
        int servers = 0;
        int lan = 0;
        for (class_4267.class_504 entry : this.field_3043.method_25396()) {
            if (entry instanceof class_4267.class_4270) {
                ++servers;
                continue;
            }
            if (!(entry instanceof class_4267.class_4269)) continue;
            ++lan;
        }
        String sub = servers + (servers == 1 ? " SERVER" : " SERVERS") + "  \u00b7  " + (String)(lan > 0 ? lan + " LAN " + (lan == 1 ? "GAME" : "GAMES") : "LAN SCAN ACTIVE");
        this.text(context, sub, 56.0f, 106.0f, 0.85f, -4275510);
        boolean overSearch = MultiplayerGridScreen.inside(mx, my, 552.0f, 72.0f, 192.0f, 26.0f);
        float focus = this.animate(this.hover, SEARCH, this.search.method_25370(), 14.0f);
        float lit = this.animate(this.chosen, SEARCH, overSearch, 12.0f);
        if (!this.glass(context, 552.0f, 72.0f, 192.0f, 26.0f, 13)) {
            this.base(context, 552.0f, 72.0f, 192.0f, 26.0f, 13);
            this.rect(context, 552.0f, 72.0f, 192.0f, 26.0f, 13, MultiplayerGridScreen.lerp(0xFFFFFFF, 0x1AFFFFFF, Math.max(focus, lit * 0.6f)));
            this.outline(context, 552.0f, 72.0f, 192.0f, 26.0f, 13, MultiplayerGridScreen.lerp(0x1FFFFFFF, 0x33FFFFFF, lit * (1.0f - focus)));
        }
        this.neon(context, 552.0f, 72.0f, 192.0f, 26.0f, 13, accent, focus);
        Icons.draw(context, Icons.Icon.SEARCH, Math.round(561.0f), Math.round(79.0f), 12, this.f(MultiplayerGridScreen.lerp(-4275510, -855051, focus)));
        boolean overRefresh = MultiplayerGridScreen.inside(mx, my, 752.0f, 72.0f, 26.0f, 26.0f);
        float spin = this.animate(this.hover, REFRESH, overRefresh, 12.0f);
        if (!this.glass(context, 752.0f, 72.0f, 26.0f, 26.0f, 13)) {
            this.base(context, 752.0f, 72.0f, 26.0f, 26.0f, 13);
            this.rect(context, 752.0f, 72.0f, 26.0f, 26.0f, 13, MultiplayerGridScreen.lerp(0xFFFFFFF, 0x1CFFFFFF, spin));
            this.outline(context, 752.0f, 72.0f, 26.0f, 26.0f, 13, MultiplayerGridScreen.lerp(0x1FFFFFFF, 0xFFFFFF, spin));
        }
        this.neon(context, 752.0f, 72.0f, 26.0f, 26.0f, 13, accent, spin);
        int cx = Math.round(765.0f);
        int cy = Math.round(85.0f);
        int color = this.f(MultiplayerGridScreen.lerp(-855051, accent, spin));
        Smooth.ring(context, cx, cy, 6, color);
        Smooth.rect(context, cx + 2, cy - 7, 5, 5, 1, this.f(MultiplayerGridScreen.lerp(0x24FFFFFF, 0x30FFFFFF, spin)));
        Smooth.triangle(context, (float)cx + 5.5f, (float)cy - 4.5f, 4.5f, color);
    }

    private void dock(class_332 context) {
        if (MultiplayerGridScreen.vanillaPlus() || MultiplayerGridScreen.soberStyle()) {
            return;
        }
        if (!this.glass(context, this.dockX, 392.0f, this.dockW, 38.0f, 19)) {
            this.base(context, this.dockX, 392.0f, this.dockW, 38.0f, 19);
            this.rect(context, this.dockX, 392.0f, this.dockW, 38.0f, 19, 0x12FFFFFF);
            this.outline(context, this.dockX, 392.0f, this.dockW, 38.0f, 19, 0x1FFFFFFF);
        }
        this.topLight(context, this.dockX, 392.0f, this.dockW, 19, 0.35f);
        for (float x : this.dividers) {
            this.rect(context, x, 402.0f, 1.0f, 18.0f, 0, 0x1FFFFFFF);
        }
    }

    private void drawCard(class_332 context, Card card, float y, boolean grid, double mx, double my, int mouseX, int mouseY) {
        float x = card.x();
        float w = card.w();
        float h = card.h();
        boolean over = grid && MultiplayerGridScreen.inside(mx, my, x, y, w, h);
        float lit = this.animate(this.hover, card.entry(), over, 12.0f);
        int accent = MultiplayerGridScreen.accent();
        if (MultiplayerGridScreen.listMode()) {
            this.creamRow(context, card, x, y, w, h, lit, mx, my, mouseX, mouseY);
            return;
        }
        if (MultiplayerGridScreen.soberStyle()) {
            this.soberCard(context, card, x, y, w, h, lit, mx, my, mouseX, mouseY);
            return;
        }
        if (card.kind() >= 2) {
            this.base(context, x, y, w, h, 14);
            this.rect(context, x, y, w, h, 14, MultiplayerGridScreen.lerp(0x7FFFFFF, 0x12FFFFFF, lit));
            this.outline(context, x, y, w, h, 14, MultiplayerGridScreen.lerp(0x1AFFFFFF, MultiplayerGridScreen.alpha(accent, 0.6f), lit));
            String label = card.kind() == 2 ? "+  Add Server" : "Direct Connect";
            float tw = this.textWidth(label, 1.15f);
            this.text(context, label, x + (w - tw) / 2.0f, y + h / 2.0f - 5.0f, 1.15f, MultiplayerGridScreen.lerp(-4275510, -855051, lit));
            return;
        }
        boolean selected = card.entry() == this.field_3043.method_25334();
        float sel = this.animate(this.chosen, card.entry(), selected, 14.0f);
        float glow = Math.max(sel, lit * 0.55f);
        if (glow > 0.01f && !MultiplayerGridScreen.vanillaPlus()) {
            Smooth.glow(context, Math.round(x + w / 2.0f), Math.round(y + h / 2.0f), Math.round(w * 0.66f), this.f(MultiplayerGridScreen.alpha(accent, 0.2f * glow)));
        }
        if (!this.glass(context, x, y, w, h, 14)) {
            this.base(context, x, y, w, h, 14);
            this.rect(context, x, y, w, h, 14, MultiplayerGridScreen.lerp(0x14FFFFFF, 0x22FFFFFF, Math.max(lit, sel * 0.6f)));
            this.outline(context, x, y, w, h, 14, MultiplayerGridScreen.vanillaPlus() ? MultiplayerGridScreen.lerp(MultiplayerGridScreen.lerp(0x30FFFFFF, -1325400065, lit), -1, sel) : MultiplayerGridScreen.lerp(MultiplayerGridScreen.lerp(0x1FFFFFFF, 0x33FFFFFF, lit), 0xFFFFFF, sel));
        }
        this.topLight(context, x, y, w, 14, 0.42f * (1.0f - Math.max(sel, lit)));
        this.neon(context, x, y, w, h, 14, MultiplayerGridScreen.lerp(-1, accent, 0.7f), lit * 0.45f * (1.0f - sel));
        this.neon(context, x, y, w, h, 14, accent, sel);
        if (card.kind() == 0) {
            this.drawServer(context, (class_4267.class_4270)card.entry(), x, y, w, h, sel, mx, my, mouseX, mouseY);
        } else {
            this.drawLan(context, (class_4267.class_4269)card.entry(), x, y, w, h, sel);
        }
        if (sel > 0.01f) {
            int cx = Math.round(x + w - 21.0f);
            int cy = Math.round(y + h - 21.0f);
            boolean overPlay = MultiplayerGridScreen.inside(mx, my, x + w - 33.0f, y + h - 33.0f, 24.0f, 24.0f);
            if (!MultiplayerGridScreen.vanillaPlus()) {
                Smooth.glow(context, cx, cy, 22, this.f(MultiplayerGridScreen.alpha(accent, 0.55f * sel)));
            }
            Smooth.circle(context, cx, cy, overPlay ? 12 : 11, this.f(MultiplayerGridScreen.alpha(accent, sel)));
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();try{
            matrices.translate((float)cx + 1.0f, (float)cy);
            matrices.rotate(1.5707964f);
            Smooth.triangle(context, 0.0f, 0.0f, 9.0f, this.f(MultiplayerGridScreen.alpha(1707524, sel)));
            }finally{matrices.popMatrix();}
        }
    }

    private void soberCard(class_332 context, Card card, float x, float y, float w, float h, float lit, double mx, double my, int mouseX, int mouseY) {
        if (card.kind() >= 2) {
            Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 5, this.f(MultiplayerGridScreen.lerp(-1777265898, -937944546, lit)));
            this.dashed(context, x, y, w, h, this.f(MultiplayerGridScreen.lerp(0x24FFFFFF, 0x5AFFFFFF, lit)));
            String label = card.kind() == 2 ? "+  ADD SERVER" : "DIRECT CONNECT";
            float tw = CrispText.width(label, "montserrat-bold.ttf", 8.4f, 0.5f);
            this.spaced(context, label, x + (w - tw) / 2.0f, y + h / 2.0f + 3.0f, 8.4f, MultiplayerGridScreen.lerp(-1761607681, -1, lit));
            return;
        }
        boolean selected = card.entry() == this.field_3043.method_25334();
        float sel = this.animate(this.chosen, card.entry(), selected, 14.0f);
        if (sel > 0.01f || lit > 0.01f) {
            Smooth.glow(context, Math.round(x + w / 2.0f), Math.round(y + h / 2.0f), Math.round(w * 0.6f), this.f(MultiplayerGridScreen.alpha(0xFFFFFF, 0.05f * Math.max(sel, lit * 0.6f))));
        }
        this.soberPanel(context, x, y, w, h, 9, lit, sel);
        if (card.kind() == 0) {
            this.drawServer(context, (class_4267.class_4270)card.entry(), x, y, w, h, sel, mx, my, mouseX, mouseY);
        } else {
            this.drawLan(context, (class_4267.class_4269)card.entry(), x, y, w, h, sel);
        }
        if (sel > 0.01f) {
            int cx = Math.round(x + w - 21.0f);
            int cy = Math.round(y + h - 21.0f);
            boolean overPlay = MultiplayerGridScreen.inside(mx, my, x + w - 33.0f, y + h - 33.0f, 24.0f, 24.0f);
            Smooth.circle(context, cx, cy, overPlay ? 12 : 11, this.f(MultiplayerGridScreen.alpha(0xF4F4F7, sel)));
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();try{
            matrices.translate((float)cx + 1.0f, (float)cy);
            matrices.rotate(1.5707964f);
            Smooth.triangle(context, 0.0f, 0.0f, 9.0f, this.f(MultiplayerGridScreen.alpha(0x141418, sel)));
            }finally{matrices.popMatrix();}
            if (card.kind() == 0) {
                this.smallButton(context, this.cardEditBox(x, y, w, h), Icons.Icon.GEAR, sel, mx, my, "edit");
                this.smallButton(context, this.cardDeleteBox(x, y, w, h), Icons.Icon.CLOSE, sel, mx, my, "delete");
            }
        }
    }

    private float[] cardEditBox(float x, float y, float w, float h) {
        return new float[]{x + w - 84.0f, y + h - 31.0f, 20.0f, 20.0f};
    }

    private float[] cardDeleteBox(float x, float y, float w, float h) {
        return new float[]{x + w - 60.0f, y + h - 31.0f, 20.0f, 20.0f};
    }

    private void dashed(class_332 context, float x, float y, float w, float h, int color) {
        int[][] corners;
        float len;
        float r = 5.0f;
        float dash = 5.0f;
        float gap = 3.5f;
        for (float sx = x + r; sx < x + w - r; sx += dash + gap) {
            len = Math.min(dash, x + w - r - sx);
            context.method_25294(Math.round(sx), Math.round(y), Math.round(sx + len), Math.round(y) + 1, color);
            context.method_25294(Math.round(sx), Math.round(y + h) - 1, Math.round(sx + len), Math.round(y + h), color);
        }
        for (float sy = y + r; sy < y + h - r; sy += dash + gap) {
            len = Math.min(dash, y + h - r - sy);
            context.method_25294(Math.round(x), Math.round(sy), Math.round(x) + 1, Math.round(sy + len), color);
            context.method_25294(Math.round(x + w) - 1, Math.round(sy), Math.round(x + w), Math.round(sy + len), color);
        }
        for (int[] corner : corners = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}}) {
            float cy;
            float cx = corner[0] == 0 ? x + r : x + w - r;
            float f = cy = corner[1] == 0 ? y + r : y + h - r;
            float start = corner[0] == 0 ? (corner[1] == 0 ? (float)Math.PI : 1.5707964f) : (corner[1] == 0 ? 4.712389f : 0.0f);
            float[] xs = new float[7];
            float[] ys = new float[7];
            float[] ws = new float[7];
            int[] cs = new int[7];
            for (int i = 0; i < 7; ++i) {
                double a = (double)start + 1.5707963267948966 * (double)i / 6.0;
                xs[i] = cx + (float)Math.cos(a) * (r - 0.5f);
                ys[i] = cy + (float)Math.sin(a) * (r - 0.5f);
                ws[i] = 1.0f;
                cs[i] = color;
            }
            Smooth.polyline(context, xs, ys, ws, cs, 7);
        }
    }

    private void drawServer(class_332 context, class_4267.class_4270 entry, float x, float y, float w, float h, float sel, double mx, double my, int mouseX, int mouseY) {
        boolean waiting;
        class_8573 world;
        ServerEntryAccessor access;
        class_642 server = entry.method_20133();
        class_2960 icon = UNKNOWN;
        class_4267.class_4270 raw = entry;
        if (server.method_49306() != null && raw instanceof ServerEntryAccessor && (access = (ServerEntryAccessor)raw).sober$favicon() != null && (world = access.sober$icon()) != null && !world.method_73412()) {
            icon = world.method_52201();
        }
        context.method_25293(class_10799.field_56883, icon, Math.round(x + 10.0f), Math.round(y + 10.0f), 0.0f, 0.0f, 32, 32, 64, 64, 64, 64, this.f(-1));
        this.outline(context, x + 10.0f, y + 10.0f, 32.0f, 32.0f, 7, 0x24FFFFFF);
        class_642.class_9083 status = server.method_55825();
        boolean bl = waiting = status == class_642.class_9083.field_47880 || status == class_642.class_9083.field_47881;
        Object players = status == class_642.class_9083.field_47883 ? server.field_3760.method_27661().method_27692(class_124.field_1061) : (waiting ? class_2561.method_43470((String)".".repeat(1 + (int)(System.currentTimeMillis() / 350L % 3L))).method_27692(class_124.field_1080) : (server.field_3753 == null ? class_2561.method_43473() : server.field_3753));
        List<Run> playerRuns = this.runs((class_2561)players, -855051).get(0);
        float pw = this.runsWidth(playerRuns, 0.85f);
        float barsX = x + w - 28.0f;
        this.drawRuns(context, playerRuns, barsX - 6.0f - pw, y + 13.0f, 0.85f, pw + 2.0f);
        this.pingBars(context, barsX, y + 21.0f, server, status);
        if (MultiplayerGridScreen.inside(mx, my, barsX - 6.0f - pw, y + 10.0f, pw, 12.0f) && server.field_3762 != null && !server.field_3762.isEmpty()) {
            this.tooltip = server.field_3762;
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        } else if (MultiplayerGridScreen.inside(mx, my, barsX - 1.0f, y + 9.0f, 22.0f, 14.0f)) {
            this.tooltip = List.of(status == class_642.class_9083.field_47884 ? class_2561.method_43470((String)(server.field_3758 + " ms")) : (waiting ? class_2561.method_43471((String)"multiplayer.status.pinging") : class_2561.method_43471((String)"multiplayer.status.no_connection")));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        }
        float nameRoom = barsX - 12.0f - pw - (x + 50.0f);
        this.text(context, this.fit(server.field_3752, Math.max(40.0f, nameRoom), 1.15f), x + 50.0f, y + 13.0f, 1.15f, -855051);
        this.text(context, this.fit(server.field_3761, w - 62.0f, 0.85f), x + 50.0f, y + 28.0f, 0.85f, -4275510);
        if (waiting || server.field_3757 == null) {
            this.text(context, class_2561.method_43471((String)"multiplayer.status.pinging").getString(), x + 10.0f, y + 50.0f, 0.85f, -7367779);
        } else {
            float room = w - 20.0f - (sel > 0.01f ? 30.0f : 0.0f);
            List<List<Run>> lines = this.wrap(this.runs(server.field_3757, -4275510), 0.85f, room);
            for (int i = 0; i < Math.min(2, lines.size()); ++i) {
                this.drawRuns(context, lines.get(i), x + 10.0f, y + 50.0f + (float)i * 11.0f, 0.85f, room);
            }
        }
    }

    private void pingBars(class_332 context, float x, float bottom, class_642 server, class_642.class_9083 status) {
        int color;
        int bars;
        if (status == class_642.class_9083.field_47884) {
            long ping = server.field_3758;
            bars = ping < 0L ? 5 : (ping < 150L ? 5 : (ping < 300L ? 4 : (ping < 600L ? 3 : (ping < 1000L ? 2 : 1))));
            color = MultiplayerGridScreen.listMode() ? (bars >= 3 ? -723723 : -7434602) : (bars >= 4 ? -9637766 : (bars == 3 ? -14006 : -38037));
        } else if (status == class_642.class_9083.field_47880 || status == class_642.class_9083.field_47881) {
            bars = 1 + (int)(System.currentTimeMillis() / 160L % 5L);
            color = MultiplayerGridScreen.listMode() ? -10724252 : -6446678;
        } else {
            bars = 1;
            color = MultiplayerGridScreen.listMode() ? -1750963 : -38037;
        }
        for (int i = 0; i < 5; ++i) {
            int bh = 3 + i * 2;
            this.rect(context, x + (float)i * 4.0f, bottom - (float)bh, 2.0f, bh, 1, i < bars ? color : (MultiplayerGridScreen.listMode() ? -14013906 : 0x26FFFFFF));
        }
    }

    private void drawLan(class_332 context, class_4267.class_4269 entry, float x, float y, float w, float h, float sel) {
        class_1131 info = ((LanServerEntryAccessor)entry).sober$server();
        context.method_25293(class_10799.field_56883, UNKNOWN, Math.round(x + 10.0f), Math.round(y + 10.0f), 0.0f, 0.0f, 32, 32, 64, 64, 64, 64, this.f(-1));
        this.outline(context, x + 10.0f, y + 10.0f, 32.0f, 32.0f, 7, 0x24FFFFFF);
        float chip = this.textWidth("LAN", 0.85f) + 12.0f;
        this.rect(context, x + w - 10.0f - chip, y + 10.0f, chip, 14.0f, 7, MultiplayerGridScreen.alpha(MultiplayerGridScreen.accent(), 0.25f));
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("LAN"), x + w - 4.0f - chip, y + 13.5f, 0.85f, -855051);
        this.text(context, this.fit(info.method_4813(), w - 70.0f - chip, 1.15f), x + 50.0f, y + 13.0f, 1.15f, -855051);
        this.text(context, this.fit(info.method_4812(), w - 62.0f, 0.85f), x + 50.0f, y + 28.0f, 0.85f, -4275510);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("Local network game"), x + 10.0f, y + 50.0f, 0.85f, -4275510);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        if (!MultiplayerGridScreen.listMode() || this.field_22787 == null || this.field_22787.field_1687 != null) {
            super.method_25420(context, mouseX, mouseY, deltaTicks);
            return;
        }
        SoberScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0f);
        float markH = (float)this.field_22790 * 0.28f;
        Mono.watermark(context, (float)this.field_22789 * 0.82f - Mono.watermarkWidth(markH) / 2.0f, (float)this.field_22790 * 0.36f, markH, 18);
    }

    private void creamPanel(class_332 context, float x, float y, float w, float h, int r, float lit, float selected) {
        this.rect(context, x, y, w, h, 3, MultiplayerGridScreen.lerp(-15198181, -14737629, Math.max(lit, selected)));
        this.outline(context, x, y, w, h, 3, MultiplayerGridScreen.lerp(MultiplayerGridScreen.lerp(-14013906, -12237491, lit), -723723, selected * 0.85f));
    }

    private void creamText(class_332 context, String value, float x, float baseline, float em, int color) {
        CrispText.draw(context, value, "geist-semibold.ttf", x, baseline, em, 0.0f, this.f(color));
    }

    private static String creamFit(String value, float max, float em) {
        if (value == null) {
            return "";
        }
        if (CrispText.width(value, "geist-semibold.ttf", em, 0.0f) <= max) {
            return value;
        }
        String cut = value;
        while (cut.length() > 1 && CrispText.width(cut + "...", "geist-semibold.ttf", em, 0.0f) > max) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.stripTrailing() + "...";
    }

    private void listHeader(class_332 context, double mx, double my) {
        float hy = this.headY();
        float lh = 30.0f;
        float lw = lh / LOGO.aspect();
        LOGO.draw(context, 208.0f, hy - 4.0f, lw, this.f(-1));
        int servers = 0;
        int lan = 0;
        for (class_4267.class_504 entry : this.field_3043.method_25396()) {
            if (entry instanceof class_4267.class_4270) {
                ++servers;
                continue;
            }
            if (!(entry instanceof class_4267.class_4269)) continue;
            ++lan;
        }
        String sub = servers + (servers == 1 ? " server" : " servers") + "  \u00b7  " + (String)(lan > 0 ? lan + " in your network" : "Looking for LAN games");
        this.creamText(context, sub, 211.0f, hy + 40.0f, 7.0f, -7434602);
        boolean overSearch = MultiplayerGridScreen.inside(mx, my, 448.0f, hy, 170.0f, 26.0f);
        float focus = this.animate(this.hover, SEARCH, this.search.method_25370(), 14.0f);
        float lit = this.animate(this.chosen, SEARCH, overSearch, 12.0f);
        this.creamPanel(context, 448.0f, hy, 170.0f, 26.0f, 13, Math.max(lit, focus), focus * 0.5f);
        Icons.draw(context, Icons.Icon.SEARCH, Math.round(459.0f), Math.round(hy + 7.0f), 12, this.f(MultiplayerGridScreen.lerp(-4934468, -723723, focus)));
        boolean overRefresh = MultiplayerGridScreen.inside(mx, my, 624.0f, hy, 26.0f, 26.0f);
        float spin = this.animate(this.hover, REFRESH, overRefresh, 12.0f);
        this.creamPanel(context, 624.0f, hy, 26.0f, 26.0f, 13, spin, 0.0f);
        MultiplayerGridScreen.refreshIcon(context, 637.0f, hy + 13.0f, spin * (float)Math.PI, this.f(MultiplayerGridScreen.lerp(-4934468, -723723, spin)));
        if (overRefresh) {
            this.tooltip = List.of(class_2561.method_43471((String)"selectServer.refresh"));
            this.tooltipX = (int)Math.round((double)this.originX + mx * (double)this.scale);
            this.tooltipY = (int)Math.round((double)this.originY + my * (double)this.scale);
        }
    }

    private static void refreshIcon(class_332 context, float cx, float cy, float turn, int color) {
        int n = 22;
        float[] xs = new float[n];
        float[] ys = new float[n];
        float[] ws = new float[n];
        int[] cs = new int[n];
        float start = -0.6f + turn;
        float sweep = 4.8694687f;
        float r = 5.5f;
        for (int i = 0; i < n; ++i) {
            double a = start + sweep * (float)i / (float)(n - 1);
            xs[i] = cx + (float)Math.cos(a) * r;
            ys[i] = cy + (float)Math.sin(a) * r;
            ws[i] = 1.4f;
            cs[i] = color;
        }
        Smooth.polyline(context, xs, ys, ws, cs, n);
        double end = start + sweep;
        float ex = cx + (float)Math.cos(end) * r;
        float ey = cy + (float)Math.sin(end) * r;
        float tx = (float)(-Math.sin(end));
        float ty = (float)Math.cos(end);
        float nx = (float)Math.cos(end);
        float ny = (float)Math.sin(end);
        Smooth.polyline(context, new float[]{ex - nx * 2.6f - tx * 0.6f, ex + tx * 2.2f, ex + nx * 2.6f - tx * 0.6f}, new float[]{ey - ny * 2.6f - ty * 0.6f, ey + ty * 2.2f, ey + ny * 2.6f - ty * 0.6f}, new float[]{1.4f, 1.4f, 1.4f}, new int[]{color, color, color}, 3);
    }

    private void emptyList(class_332 context) {
        String title = net.albiz.client.i18n.AlbizI18n.display("No servers yet");
        String hint = net.albiz.client.i18n.AlbizI18n.display("Add one with the + button below.");
        float tw = CrispText.width(title, "geist-semibold.ttf", 10.0f, 0.0f);
        float hw = CrispText.width(hint, "geist-semibold.ttf", 7.4f, 0.0f);
        float cy = (this.listTop() + this.listBottom()) / 2.0f;
        this.creamText(context, title, 210.0f + (440.0f - tw) / 2.0f, cy - 2.0f, 10.0f, -723723);
        this.creamText(context, hint, 210.0f + (440.0f - hw) / 2.0f, cy + 13.0f, 7.4f, -7434602);
    }

    private float[][] actionBoxes() {
        float dw = CrispText.width(net.albiz.client.i18n.AlbizI18n.display(class_2561.method_43471((String)ACTIONS[2]).getString()), "geist-semibold.ttf", 8.2f, 0.0f) + 30.0f;
        float bw = CrispText.width(net.albiz.client.i18n.AlbizI18n.display(class_2561.method_43471((String)ACTIONS[3]).getString()), "geist-semibold.ttf", 8.2f, 0.0f) + 30.0f;
        float plus = 30.0f;
        float jw = 440.0f - plus - dw - bw - 18.0f;
        float ay = MultiplayerGridScreen.listMode() ? this.actionY() : 394.0f;
        return new float[][]{{210.0f, ay, jw, 30.0f}, {210.0f + jw + 6.0f, ay, plus, 30.0f}, {210.0f + jw + 12.0f + plus, ay, dw, 30.0f}, {210.0f + jw + 18.0f + plus + dw, ay, bw, 30.0f}};
    }

    private void actions(class_332 context, double mx, double my, int mouseX, int mouseY) {
        float[][] boxes = this.actionBoxes();
        for (int i = 0; i < boxes.length; ++i) {
            float tw;
            String label;
            float[] b = boxes[i];
            class_4185 button = this.buttons.get(ACTIONS[i]);
            boolean active = button == null || button.field_22763;
            boolean over = MultiplayerGridScreen.inside(mx, my, b[0], b[1], b[2], b[3]);
            float lit = this.animate(this.hover, ACTIONS[i], over && active, 12.0f);
            int r = 3;
            if (i == 0) {
                this.rect(context, b[0], b[1], b[2], b[3], r, active ? MultiplayerGridScreen.lerp(-723723, -1, lit) : 871691509);
                label = net.albiz.client.i18n.AlbizI18n.display(class_2561.method_43471((String)ACTIONS[0]).getString());
                tw = CrispText.width(label, "geist-semibold.ttf", 8.6f, 0.0f);
                this.creamText(context, label, b[0] + (b[2] - tw) / 2.0f, b[1] + b[3] / 2.0f + 3.0f, 8.6f, active ? -15856112 : -7434602);
                continue;
            }
            this.creamPanel(context, b[0], b[1], b[2], b[3], r, lit, 0.0f);
            if (i == 1) {
                Icons.draw(context, Icons.Icon.PLUS, Math.round(b[0] + b[2] / 2.0f - 6.0f), Math.round(b[1] + b[3] / 2.0f - 6.0f), 12, this.f(MultiplayerGridScreen.lerp(-4934468, -723723, lit)));
                if (!over) continue;
                this.tooltip = List.of(class_2561.method_43471((String)ACTIONS[1]));
                this.tooltipX = mouseX;
                this.tooltipY = mouseY;
                continue;
            }
            label = net.albiz.client.i18n.AlbizI18n.display(class_2561.method_43471((String)ACTIONS[i]).getString());
            tw = CrispText.width(label, "geist-semibold.ttf", 8.2f, 0.0f);
            this.creamText(context, label, b[0] + (b[2] - tw) / 2.0f, b[1] + b[3] / 2.0f + 3.0f, 8.2f, MultiplayerGridScreen.lerp(-4934468, -723723, lit));
        }
    }

    private float[] editBox(float x, float y, float w, float h) {
        return new float[]{x + w - 84.0f, y + (h - 20.0f) / 2.0f, 20.0f, 20.0f};
    }

    private float[] deleteBox(float x, float y, float w, float h) {
        return new float[]{x + w - 60.0f, y + (h - 20.0f) / 2.0f, 20.0f, 20.0f};
    }

    private void smallButton(class_332 context, float[] b, Icons.Icon icon, float shown, double mx, double my, Object key) {
        float lit = this.animate(this.hover, key, MultiplayerGridScreen.inside(mx, my, b[0], b[1], b[2], b[3]), 14.0f);
        this.rect(context, b[0], b[1], b[2], b[3], 3, MultiplayerGridScreen.alpha(0xF4F4F5, (0.08f + 0.16f * lit) * shown));
        Icons.draw(context, icon, Math.round(b[0] + 5.0f), Math.round(b[1] + 5.0f), 10, this.f(MultiplayerGridScreen.alpha(MultiplayerGridScreen.lerp(-4934468, -723723, lit), shown)));
    }

    private class_2960 favicon(class_4267.class_4270 entry) {
        class_8573 world;
        ServerEntryAccessor access;
        class_4267.class_4270 raw = entry;
        if (entry.method_20133().method_49306() != null && raw instanceof ServerEntryAccessor && (access = (ServerEntryAccessor)raw).sober$favicon() != null && (world = access.sober$icon()) != null && !world.method_73412()) {
            return world.method_52201();
        }
        return UNKNOWN;
    }

    private void creamRow(class_332 context, Card card, float x, float y, float w, float h, float lit, double mx, double my, int mouseX, int mouseY) {
        boolean selected = card.entry() == this.field_3043.method_25334();
        float sel = this.animate(this.chosen, card.entry(), selected, 14.0f);
        this.creamPanel(context, x, y, w, h, 12, lit, sel);
        if (card.kind() == 1) {
            this.creamLan(context, (class_4267.class_4269)card.entry(), x, y, w, h);
            return;
        }
        class_4267.class_4270 entry = (class_4267.class_4270)card.entry();
        class_642 server = entry.method_20133();
        context.method_25293(class_10799.field_56883, this.favicon(entry), Math.round(x + 7.0f), Math.round(y + 6.0f), 0.0f, 0.0f, 26, 26, 64, 64, 64, 64, this.f(-1));
        class_642.class_9083 status = server.method_55825();
        boolean waiting = status == class_642.class_9083.field_47880 || status == class_642.class_9083.field_47881;
        float barsX = x + w - 30.0f;
        this.pingBars(context, barsX, y + h / 2.0f + 5.0f, server, status);
        if (sel > 0.01f) {
            this.smallButton(context, this.editBox(x, y, w, h), Icons.Icon.GEAR, sel, mx, my, "edit");
            this.smallButton(context, this.deleteBox(x, y, w, h), Icons.Icon.CLOSE, sel, mx, my, "delete");
        }
        Object players = status == class_642.class_9083.field_47883 ? server.field_3760.method_27661().method_27692(class_124.field_1061) : (waiting ? class_2561.method_43470((String)".".repeat(1 + (int)(System.currentTimeMillis() / 350L % 3L))).method_27692(class_124.field_1080) : (server.field_3753 == null ? class_2561.method_43473() : server.field_3753));
        List<Run> playerRuns = this.runs((class_2561)players, -4934468).get(0);
        float pw = this.runsWidth(playerRuns, 0.8f);
        float playersRight = barsX - 10.0f - 56.0f * sel;
        this.drawRuns(context, playerRuns, playersRight - pw, y + h / 2.0f - 4.0f, 0.8f, pw + 2.0f);
        float textX = x + 42.0f;
        float room = playersRight - pw - 12.0f - textX;
        this.creamText(context, MultiplayerGridScreen.creamFit(server.field_3752, Math.max(40.0f, room), 8.6f), textX, y + 17.0f, 8.6f, -723723);
        if (waiting || server.field_3757 == null) {
            this.creamText(context, class_2561.method_43471((String)"multiplayer.status.pinging").getString(), textX, y + 29.0f, 6.8f, -7434602);
        } else {
            List<List<Run>> lines = this.runs(server.field_3757, -4934468);
            if (!lines.isEmpty()) {
                this.drawRuns(context, lines.get(0), textX, y + 21.5f, 0.72f, Math.max(40.0f, room));
            }
        }
        if (MultiplayerGridScreen.inside(mx, my, playersRight - pw, y + 8.0f, pw, 14.0f) && server.field_3762 != null && !server.field_3762.isEmpty()) {
            this.tooltip = server.field_3762;
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        } else if (MultiplayerGridScreen.inside(mx, my, barsX - 1.0f, y + 8.0f, 22.0f, h - 16.0f)) {
            this.tooltip = List.of(status == class_642.class_9083.field_47884 ? class_2561.method_43470((String)(server.field_3758 + " ms")) : (waiting ? class_2561.method_43471((String)"multiplayer.status.pinging") : class_2561.method_43471((String)"multiplayer.status.no_connection")));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        } else if (sel > 0.5f && MultiplayerGridScreen.inside(mx, my, this.editBox(x, y, w, h)[0], this.editBox(x, y, w, h)[1], 20.0f, 20.0f)) {
            this.tooltip = List.of(class_2561.method_43471((String)"selectServer.edit"));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        } else if (sel > 0.5f && MultiplayerGridScreen.inside(mx, my, this.deleteBox(x, y, w, h)[0], this.deleteBox(x, y, w, h)[1], 20.0f, 20.0f)) {
            this.tooltip = List.of(class_2561.method_43471((String)"selectServer.delete"));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
        }
    }

    private void creamLan(class_332 context, class_4267.class_4269 entry, float x, float y, float w, float h) {
        class_1131 info = ((LanServerEntryAccessor)entry).sober$server();
        context.method_25293(class_10799.field_56883, UNKNOWN, Math.round(x + 7.0f), Math.round(y + 6.0f), 0.0f, 0.0f, 26, 26, 64, 64, 64, 64, this.f(-1));
        float chip = CrispText.width("LAN", "geist-semibold.ttf", 6.8f, 0.0f) + 14.0f;
        this.rect(context, x + w - 12.0f - chip, y + (h - 14.0f) / 2.0f, chip, 14.0f, 3, 871691509);
        this.creamText(context, "LAN", x + w - 5.0f - chip, y + h / 2.0f + 2.4f, 6.8f, -723723);
        this.creamText(context, MultiplayerGridScreen.creamFit(info.method_4813(), w - 70.0f - chip, 8.6f), x + 42.0f, y + 17.0f, 8.6f, -723723);
        this.creamText(context, info.method_4812(), x + 42.0f, y + 29.0f, 6.8f, -7434602);
    }

    private boolean clickList(class_11909 click, double mx, double my) {
        if (MultiplayerGridScreen.inside(mx, my, 624.0f, this.headY(), 26.0f, 26.0f)) {
            UiSounds.click();
            this.press(DOCK[5]);
            return true;
        }
        if (MultiplayerGridScreen.inside(mx, my, 448.0f, this.headY(), 170.0f, 26.0f)) {
            this.method_25395((class_364)this.search);
            this.search.method_25365(true);
            return true;
        }
        return this.clickActions(mx, my);
    }

    private boolean clickActions(double mx, double my) {
        float[][] boxes = this.actionBoxes();
        for (int i = 0; i < boxes.length; ++i) {
            if (!MultiplayerGridScreen.inside(mx, my, boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3])) continue;
            class_4185 button = this.buttons.get(ACTIONS[i]);
            if (button != null && button.field_22763) {
                UiSounds.click();
                this.press(ACTIONS[i]);
            }
            return true;
        }
        return false;
    }

    private boolean clickRow(Card card, float y, double mx, double my, boolean doubled) {
        class_4267.class_504 entry = (class_4267.class_504)card.entry();
        if (MultiplayerGridScreen.listMode() && card.kind() == 0 && entry == this.field_3043.method_25334()) {
            float[] edit = this.editBox(card.x(), y, card.w(), card.h());
            float[] delete = this.deleteBox(card.x(), y, card.w(), card.h());
            if (MultiplayerGridScreen.inside(mx, my, edit[0], edit[1], edit[2], edit[3])) {
                UiSounds.click();
                this.press(DOCK[3]);
                return true;
            }
            if (MultiplayerGridScreen.inside(mx, my, delete[0], delete[1], delete[2], delete[3])) {
                UiSounds.click();
                this.press(DOCK[4]);
                return true;
            }
        }
        this.select(entry);
        if (doubled) {
            entry.method_74043();
        }
        return true;
    }

    private void press(String key) {
        class_4185 button = this.buttons.get(key);
        if (button != null && button.field_22763) {
            button.method_25306((class_11907)new class_11908(257, 0, 0));
        }
    }

    private void select(class_4267.class_504 entry) {
        this.field_3043.method_20122(entry);
        this.method_20121();
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if(renderFailed)return true;
        double my;
        double mx = (click.comp_4798() - (double)this.originX) / (double)this.scale;
        if (this.clickForeign(click, mx, my = (click.comp_4799() - (double)this.originY) / (double)this.scale, doubled)) {
            return true;
        }
        if (MultiplayerGridScreen.listMode() && click.method_74245() == 0) {
            if (this.search.method_25370() && MultiplayerGridScreen.inside(mx, my, 448.0f, this.headY(), 170.0f, 26.0f)) {
                if (!super.method_25402(click, doubled)) {
                    // empty if block
                }
                return true;
            }
            if (this.clickList(click, mx, my)) {
                return true;
            }
            if (my >= (double)(this.listTop() - 6.0f) && my <= (double)(this.listBottom() + 6.0f)) {
                for (Card card : this.cards) {
                    float y = card.y() - this.scroll;
                    if (!MultiplayerGridScreen.inside(mx, my, card.x(), y, card.w(), card.h())) continue;
                    this.method_25395(null);
                    return this.clickRow(card, y, mx, my, doubled);
                }
            }
            return super.method_25402(click, doubled);
        }
        if (click.method_74245() == 0) {
            if (MultiplayerGridScreen.inside(mx, my, 752.0f, 72.0f, 26.0f, 26.0f)) {
                this.press(DOCK[5]);
                return true;
            }
            if (MultiplayerGridScreen.soberStyle() && this.clickActions(mx, my)) {
                return true;
            }
            if (MultiplayerGridScreen.inside(mx, my, 552.0f, 72.0f, 192.0f, 26.0f)) {
                this.method_25395((class_364)this.search);
                this.search.method_25365(true);
                if (!super.method_25402(click, doubled)) {
                    // empty if block
                }
                return true;
            }
            if (my >= 114.0 && my <= 388.0) {
                for (Card card : this.cards) {
                    float y = card.y() - this.scroll;
                    if (!MultiplayerGridScreen.inside(mx, my, card.x(), y, card.w(), card.h())) continue;
                    this.method_25395(null);
                    if (card.kind() == 2) {
                        this.press(DOCK[2]);
                    } else if (card.kind() == 3) {
                        this.press(DOCK[1]);
                    } else {
                        class_4267.class_504 entry = (class_4267.class_504)card.entry();
                        if (MultiplayerGridScreen.soberStyle() && card.kind() == 0 && entry == this.field_3043.method_25334()) {
                            float[] edit = this.cardEditBox(card.x(), y, card.w(), card.h());
                            float[] delete = this.cardDeleteBox(card.x(), y, card.w(), card.h());
                            if (MultiplayerGridScreen.inside(mx, my, edit[0], edit[1], edit[2], edit[3])) {
                                UiSounds.click();
                                this.press(DOCK[3]);
                                return true;
                            }
                            if (MultiplayerGridScreen.inside(mx, my, delete[0], delete[1], delete[2], delete[3])) {
                                UiSounds.click();
                                this.press(DOCK[4]);
                                return true;
                            }
                        }
                        boolean play = entry == this.field_3043.method_25334() && MultiplayerGridScreen.inside(mx, my, card.x() + card.w() - 33.0f, y + card.h() - 33.0f, 24.0f, 24.0f);
                        this.select(entry);
                        if (play || doubled) {
                            entry.method_74043();
                        }
                    }
                    return true;
                }
            }
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double my = (mouseY - (double)this.originY) / (double)this.scale;
        if (my >= (double)(this.listTop() - 10.0f) && my <= (double)(this.listBottom() + 8.0f)) {
            this.scrollTarget = this.clampScroll(this.scrollTarget - (float)verticalAmount * 32.0f);
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(class_11908 input) {
        int key = input.comp_4795();
        if(renderFailed){if(key==256)this.method_25419();return true;}
        if (key == 257 || key == 335) {
            class_4267.class_504 entry = (class_4267.class_504)this.field_3043.method_25334();
            if (entry == null && this.search.method_25370()) {
                for (Card card : this.cards) {
                    if (card.kind() > 1) continue;
                    entry = (class_4267.class_504)card.entry();
                    break;
                }
            }
            if (entry != null) {
                entry.method_74043();
                return true;
            }
        }
        if (!this.search.method_25370()) {
            int step=0;
            if (MultiplayerGridScreen.listMode()) {
                switch (key) {
                    case 265: {
                        step = -1;
                        break;
                    }
                    case 264: {
                        step = 1;
                        break;
                    }
                    default: {
                        step = 0;
                        break;
                    }
                }
            } else {
                switch (key) {
                    case 263: {
                        step = -1;
                        break;
                    }
                    case 262: {
                        step = 1;
                        break;
                    }
                    case 265: {
                        step = -3;
                        break;
                    }
                    case 264: {
                        step = 3;
                        break;
                    }
                    default: {
                        step = 0;
                    }
                }
            }
            if (step != 0 && this.move(step)) {
                return true;
            }
        }
        return super.method_25404(input);
    }

    private boolean move(int step) {
        ArrayList<Card> servers = new ArrayList<Card>();
        for (Card card : this.cards) {
            if (card.kind() > 1) continue;
            servers.add(card);
        }
        if (servers.isEmpty()) {
            return false;
        }
        int index = -1;
        var selected = this.field_3043.method_25334();
        for (int i = 0; i < servers.size(); ++i) {
            if (((Card)servers.get(i)).entry() != selected) continue;
            index = i;
        }
        int next = index < 0 ? 0 : Math.max(0, Math.min(servers.size() - 1, index + step));
        Card card = (Card)servers.get(next);
        this.select((class_4267.class_504)card.entry());
        float top = card.y() - this.listTop();
        float bottom = card.y() + card.h() - this.listTop();
        float view = this.listBottom() - this.listTop();
        if (top < this.scrollTarget) {
            this.scrollTarget = this.clampScroll(top);
        } else if (bottom > this.scrollTarget + view) {
            this.scrollTarget = this.clampScroll(bottom - view);
        }
        return true;
    }

    private record Run(String text, int color) {
    }

    private record Card(Object entry, int kind, float x, float y, float w, float h) {
    }
}

