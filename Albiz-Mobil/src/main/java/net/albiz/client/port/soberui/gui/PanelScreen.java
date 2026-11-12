// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.DropdownScreen;
import net.albiz.client.port.soberui.gui.ExactImage;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Mono;
import net.albiz.client.port.soberui.gui.SettingsPane;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.albiz.client.port.soberui.modules.Configs;
import net.albiz.client.port.soberui.modules.Hud;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1041;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;

public final class PanelScreen
extends class_437 implements ModuleMenu {
    private static final ExactImage WORDMARK = new ExactImage("textures/brand/lockup.png");
    private static final Category[] SIDEBAR = new Category[]{Category.BASE_FINDING, Category.VISUALS, Category.MOVEMENT, Category.CLIENT, Category.DONUT, Category.COMBAT,Category.RENDER,Category.MACRO};
    private static final int TOP_H = 30;
    private static final int SIDE_W = 122;
    private static final int CARD_H = 26;
    private static final int GAP = 5;
    private static final int SEARCH_W = 170;
    private static final int FOOTER_H = 16;
    private static final int MIN_W = 380;
    private static final int MIN_H = 250;
    private static final int GRIP = 12;
    private static final String HINT = "Left click toggles  \u00b7  Right click opens settings  \u00b7  Middle click binds a key";
    private static final float NAME_EM = 7.6f;
    private static final float SMALL_EM = 6.4f;
    private static final float TAB_EM = 7.2f;
    private static final SettingsPane.Palette PALETTE = new SettingsPane.Palette(true, -723723, -7434602, -10724252, 284488949, 485815541, -14013906, -12237491, 653587701, 217380085, -723723, 821359861, -723723, false, -723723);
    private static Tab tab = Tab.MODULES;
    private static Category category = Category.BASE_FINDING;
    private static boolean acUnlocked=true;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<Key, Float> animations = new HashMap<Key, Float>();
    private final SettingsPane pane = new SettingsPane(this);
    private Module selected;
    private Module binding;
    private String search = "";
    private float scroll;
    private float scrollTarget;
    private float content;
    private float view;
    private long lastFrame = System.nanoTime();
    private long swallowUntil;
    private float delta;
    private float open;
    private float alpha;
    private boolean closing;
    private int wx;
    private int wy;
    private int ww;
    private int wh;
    private int gridX;
    private int gridY;
    private int gridW;
    private int gridH;
    private float z = 1.0f;
    private int vw;
    private int vh;
    private boolean resizing;
    private double resizeX;
    private double resizeY;
    private int resizeW;
    private int resizeH;
    private String tooltip;

    public PanelScreen() {
        super((class_2561)class_2561.method_43470((String)"Albız"));
        this.pane.palette(PALETTE);
    }

    private static ClickGui options() {
        return SoberClient.MODULES.get(ClickGui.class);
    }

    public void openSettings(Module module) {
        tab = Tab.MODULES;
        this.selected = module;
        this.pane.show(module);
    }

    public void openSettingsTab() {
        this.switchTab(Tab.SETTINGS);
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        this.layout();
        if (tab != Tab.MODULES) {
            this.pane.show(PanelScreen.tabModule(tab));
        } else if (this.selected != null) {
            this.pane.show(this.selected);
        }
    }

    private void layout() {
        ClickGui options = PanelScreen.options();
        float most = Math.max(0.5f, Math.min((float)this.field_22789 / 388.0f, (float)this.field_22790 / 258.0f));
        this.z = Math.max(0.5f, Math.min((float)options.panelScale.get().intValue() / 100.0f, most));
        this.vw = Math.round((float)this.field_22789 / this.z);
        this.vh = Math.round((float)this.field_22790 / this.z);
        this.ww = Math.clamp((long)Math.round((float)(this.vw * options.panelWidth.get()) / 100.0f), Math.min(380, this.vw - 8), this.vw - 8);
        this.wh = Math.clamp((long)Math.round((float)(this.vh * options.panelHeight.get()) / 100.0f), Math.min(250, this.vh - 8), this.vh - 8);
        this.wx = (this.vw - this.ww) / 2;
        this.wy = (this.vh - this.wh) / 2;
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.pane.commit();
        this.binding = null;
        this.closing = true;
        UiSounds.close();
    }

    public void method_25432() {
        net.albiz.client.config.ModuleConfigStore.getInstance().saveIfChanged();
        SoberClient.CONFIG.save();
    }

    private static Module tabModule(Tab tab) {
        return switch (tab.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> SoberClient.MODULES.get(Hud.class);
            case 2 -> SoberClient.MODULES.get(Configs.class);
            case 3 -> SoberClient.MODULES.get(ClickGui.class);
            case 0 -> null;
        };
    }

    private void switchTab(Tab next) {
        tab = next;
        this.selected = null;
        this.binding = null;
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
        this.pane.show(PanelScreen.tabModule(next));
    }

    private float anim(String tag, Object target, boolean on, float speed) {
        Key key = new Key(tag, target);
        float value = MenuKit.approach(this.animations.getOrDefault(key, Float.valueOf(on ? 1.0f : 0.0f)).floatValue(), on ? 1.0f : 0.0f, speed, this.delta);
        this.animations.put(key, Float.valueOf(value));
        return value;
    }

    private int c(int color) {
        return MenuKit.alpha(color, this.alpha);
    }

    private void text(class_332 context, String value, String font, float x, float baseline, float em, int color) {
        CrispText.draw(context, value, font, x, baseline, em, 0.0f, this.c(color));
    }

    private static float width(String value, String font, float em) {
        return CrispText.width(value, font, em, 0.0f);
    }

    private static String fit(String value, String font, float max, float em) {
        if (value == null || max < 8.0f) {
            return "";
        }
        if (PanelScreen.width(value, font, em) <= max) {
            return value;
        }
        String cut = value;
        while (cut.length() > 1 && PanelScreen.width(cut + "...", font, em) > max) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.stripTrailing() + "...";
    }

    private void hit(String kind, float x, float y, float w, float h, Object target) {
        this.hits.add(new Hit(kind, x, y, w, h, target));
    }

    private void toggle(class_332 context, float x, float y, float on) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int kx = ix + 2 + Math.round(12.0f * on);
        Smooth.rect(context, ix, iy, 26, 14, 7, this.c(MenuKit.lerp(787805429, -723723, on)));
        Smooth.rect(context, kx, iy + 3, 10, 10, 5, this.c(0x38000000));
        Smooth.rect(context, kx, iy + 2, 10, 10, 5, this.c(MenuKit.lerp(-4934468, -15856112, on)));
    }

    private void lock(class_332 context, float cx, float cy, float size, int color) {
        int i;
        int bw = Math.round(size);
        int bh = Math.round(size * 0.68f);
        int bx = Math.round(cx - (float)bw / 2.0f);
        int by = Math.round(cy - (float)bh / 2.0f + size * 0.2f);
        Smooth.rect(context, bx, by, bw, bh, 1, this.c(color));
        int n = 11;
        float r = size * 0.3f;
        float top = (float)by - 0.5f;
        float[] xs = new float[n + 2];
        float[] ys = new float[n + 2];
        float[] ws = new float[n + 2];
        int[] cs = new int[n + 2];
        xs[0] = cx - r;
        ys[0] = (float)by + 1.0f;
        for (i = 0; i < n; ++i) {
            double a = Math.PI + Math.PI * (double)i / (double)(n - 1);
            xs[i + 1] = cx + (float)Math.cos(a) * r;
            ys[i + 1] = top + (float)Math.sin(a) * r;
        }
        xs[n + 1] = cx + r;
        ys[n + 1] = (float)by + 1.0f;
        for (i = 0; i < n + 2; ++i) {
            ws[i] = Math.max(1.0f, size * 0.14f);
            cs[i] = this.c(color);
        }
        Smooth.polyline(context, xs, ys, ws, cs, n + 2);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        if (this.field_22787.field_1687 == null) {
            SoberScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0f);
            return;
        }
        MenuKit.background(context, this.field_22787, this.field_22789, this.field_22790, this.alpha, true);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        long now = System.nanoTime();
        this.delta = Math.min(0.06f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        this.open = MenuKit.approach(this.open, this.closing ? 0.0f : 1.0f, this.closing ? 16.0f : 12.0f, this.delta);
        if (this.closing && this.open < 0.03f) {
            this.field_22787.method_1507(null);
            return;
        }
        this.alpha = 1.0f - (float)Math.pow(1.0f - this.open, 3.0);
        super.method_25394(context, mouseX, mouseY, deltaTicks);
        this.hits.clear();
        this.tooltip = null;
        this.layout();
        context.method_51448().pushMatrix();
        context.method_51448().scale(this.z, this.z);
        int mx = Math.round((float)mouseX / this.z);
        int my = Math.round((float)mouseY / this.z);
        Smooth.rect(context, this.wx, this.wy, this.ww, this.wh, 4, this.c(Theme.opacity(-183628272)));
        Smooth.outline(context, this.wx, this.wy, this.ww, this.wh, 4, this.c(-14013906));
        float markH = (float)this.wh * 0.24f;
        Mono.watermark(context, (float)(this.wx + this.ww) - 40.0f - Mono.watermarkWidth(markH), (float)(this.wy + this.wh) - markH - 16.0f, markH, Math.round(13.0f * this.alpha));
        this.drawTop(context, mx, my);
        context.method_25294(this.wx + 1, this.wy + 30, this.wx + this.ww - 1, this.wy + 30 + 1, this.c(-14013906));
        if (tab == Tab.MODULES) {
            this.drawModules(context, mx, my);
        } else {
            this.drawTabSettings(context, mx, my);
        }
        this.drawGrip(context, mx, my);
        String tip = this.pane.tooltip();
        if (tip == null || tip.isEmpty()) {
            tip = this.tooltip;
        }
        if (tip != null && !tip.isEmpty()) {
            MenuKit.tooltip(context, tip, mx, my, this.vw, this.vh, this.alpha);
        }
        context.method_51448().popMatrix();
    }

    private boolean overGrip(double mx, double my) {
        return mx >= (double)(this.wx + this.ww - 12) && mx < (double)(this.wx + this.ww + 2) && my >= (double)(this.wy + this.wh - 12) && my < (double)(this.wy + this.wh + 2);
    }

    private void drawGrip(class_332 context, int mx, int my) {
        float hv = this.anim("grip", (Object)this, this.resizing || this.overGrip(mx, my), 14.0f);
        int color = this.c(MenuKit.lerp(-10724252, -723723, hv));
        float right = (float)(this.wx + this.ww) - 3.0f;
        float bottom = (float)(this.wy + this.wh) - 3.0f;
        for (int i = 0; i < 3; ++i) {
            float d = 3.0f + (float)i * 3.0f;
            Smooth.polyline(context, new float[]{right - d, right}, new float[]{bottom, bottom - d}, new float[]{1.0f, 1.0f}, new int[]{color, color}, 2);
        }
        if (this.overGrip(mx, my) && !this.resizing) {
            this.tooltip = "Drag to resize";
        }
    }

    private void drawTop(class_332 context, int mouseX, int mouseY) {
        float lw = 22.0f / WORDMARK.aspect();
        WORDMARK.draw(context, this.wx + 10, this.wy + 4, lw, this.c(-1));
        float x = (float)(this.wx + 12) + lw + 14.0f;
        for (Tab t : Tab.values()) {
            float w = PanelScreen.width(t.label, "geist-semibold.ttf", 7.2f) + 18.0f;
            float h = 18.0f;
            float y = this.wy + 6;
            boolean over = (float)mouseX >= x && (float)mouseX < x + w && (float)mouseY >= y && (float)mouseY < y + h;
            float hv = this.anim("tab", (Object)t, over, 14.0f);
            float on = this.anim("tabOn", (Object)t, t == tab, 14.0f);
            Mono.box(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), Math.max(hv, on), this.alpha);
            if (on > 0.01f) {
                context.method_25294(Math.round(x + 6.0f), this.wy + 30 - 2, Math.round(x + w - 6.0f), this.wy + 30, this.c(MenuKit.alpha(-723723, on)));
            }
            this.text(context, t.label, "geist-semibold.ttf", x + 9.0f, y + 12.2f, 7.2f, MenuKit.lerp(-7434602, -723723, Math.max(on, hv * 0.6f)));
            this.hit("tab", x, y, w, h, (Object)t);
            x += w + 5.0f;
        }
        float cx = this.wx + this.ww - 22;
        boolean overClose = (float)mouseX >= cx - 4.0f && (float)mouseX < cx + 14.0f && mouseY >= this.wy + 6 && mouseY < this.wy + 24;
        Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx), this.wy + 10, 9, this.c(overClose ? -723723 : -10724252));
        this.hit("close", cx - 4.0f, this.wy + 6, 18.0f, 18.0f, null);
        String style = "Dropdown";
        float sw = PanelScreen.width(style, "geist-semibold.ttf", 7.2f) + 30.0f;
        float sx = cx - 10.0f - sw;
        float sy = this.wy + 6;
        boolean overStyle = (float)mouseX >= sx && (float)mouseX < sx + sw && (float)mouseY >= sy && (float)mouseY < sy + 18.0f;
        float hv = this.anim("style", style, overStyle, 14.0f);
        Mono.box(context, Math.round(sx), Math.round(sy), Math.round(sw), 18, hv, this.alpha);
        Icons.draw(context, Icons.Icon.COLUMNS, Math.round(sx + 8.0f), Math.round(sy + 5.0f), 8, this.c(MenuKit.lerp(-7434602, -723723, hv)));
        this.text(context, style, "geist-semibold.ttf", sx + 21.0f, sy + 12.2f, 7.2f, MenuKit.lerp(-4934468, -723723, hv));
        this.hit("style", sx, sy, sw, 18.0f, null);
        if (overStyle) {
            this.tooltip = "Switch to the Dropdown menu. Menu Style in Settings switches back.";
        }
    }

    private void drawModules(class_332 context, int mouseX, int mouseY) {
        boolean typing;
        int bodyY = this.wy + 30 + 1;
        float sy = bodyY + 10;
        for (Category cat : SIDEBAR) {
            float x = this.wx + 10;
            float w = 102.0f;
            float h = 22.0f;
            boolean over = (float)mouseX >= x && (float)mouseX < x + w && (float)mouseY >= sy && (float)mouseY < sy + h;
            boolean active = cat == category && this.search.isEmpty();
            float hv = this.anim("cat", (Object)cat, over, 14.0f);
            float on = this.anim("catOn", (Object)cat, active, 14.0f);
            Mono.box(context, Math.round(x), Math.round(sy), Math.round(w), Math.round(h), Math.max(hv, on), this.alpha);
            if (on > 0.01f) {
                Smooth.rect(context, Math.round(x + 1.0f), Math.round(sy + 5.0f), 2, Math.round(h - 10.0f), 1, this.c(MenuKit.alpha(-723723, on)));
            }
            int color = MenuKit.lerp(-4934468, -723723, Math.max(on, hv * 0.6f));
            Icons.draw(context, MenuKit.icon(cat), Math.round(x + 9.0f), Math.round(sy + 6.0f), 10, this.c(color));
            String title = cat == Category.MOVEMENT ? "AC Off" : cat.title;
            this.text(context, title, "geist-semibold.ttf", x + 25.0f, sy + 14.4f, 7.2f, color);
            if (cat == Category.MOVEMENT && !acUnlocked) {
                this.lock(context, x + w - 11.0f, sy + h / 2.0f - 1.5f, 6.5f, -7434602);
            } else {
                String n = Integer.toString(PanelScreen.count(cat));
                this.text(context, n, "geist-medium.ttf", x + w - 8.0f - PanelScreen.width(n, "geist-medium.ttf", 6.4f), sy + 14.2f, 6.4f, -10724252);
            }
            this.hit("cat", x, sy, w, h, (Object)cat);
            sy += h + 5.0f;
        }
        context.method_25294(this.wx + 122, bodyY, this.wx + 122 + 1, this.wy + this.wh - 1, this.c(-14013906));
        int cx = this.wx + 122 + 11;
        int cw = this.ww - 122 - 22;
        int gw = this.selected == null ? cw : Math.round((float)cw * 0.5f - 5.0f);
        int searchW = Math.min(170, gw);
        Mono.box(context, cx, bodyY + 10, searchW, 18, 0.0f, this.alpha);
        Icons.draw(context, Icons.Icon.SEARCH, cx + 7, bodyY + 15, 9, this.c(-10724252));
        boolean bl = typing = !this.search.isEmpty();
        String shown = typing ? PanelScreen.fit(this.search, "geist-medium.ttf", searchW - 30, 6.8f) + (System.currentTimeMillis() / 500L % 2L == 0L ? "_" : "") : PanelScreen.fit("Search modules", "geist-medium.ttf", searchW - 26, 6.8f);
        this.text(context, shown, "geist-medium.ttf", cx + 21, (float)bodyY + 22.2f, 6.8f, typing ? -723723 : -10724252);
        List<Module> modules = this.modules();
        String count = modules.size() + (modules.size() == 1 ? " module" : " modules");
        float countW = PanelScreen.width(count, "geist-medium.ttf", 6.4f);
        if ((float)(gw - searchW) >= countW + 10.0f) {
            this.text(context, count, "geist-medium.ttf", (float)(cx + gw) - countW, (float)bodyY + 21.5f, 6.4f, -10724252);
        }
        boolean locked = category == Category.MOVEMENT && !acUnlocked && this.search.isEmpty();
        this.gridX = cx;
        this.gridY = bodyY + 36;
        this.gridW = gw;
        this.gridH = this.wy + this.wh - 10 - 16 - this.gridY;
        String hint = PanelScreen.fit(HINT, "geist-medium.ttf", gw, 6.4f);
        this.text(context, hint, "geist-medium.ttf", cx, this.wy + this.wh - 10, 6.4f, -10724252);
        if (locked) {
            this.drawLock(context, cx, cw, mouseX, mouseY);
            return;
        }
        this.drawGrid(context, modules, mouseX, mouseY);
        if (this.selected != null) {
            this.drawDrawer(context, cx + gw + 10, bodyY + 10, cw - gw - 10, this.wy + this.wh - 10 - (bodyY + 10), mouseX, mouseY);
        }
    }

    private List<Module> modules() {
        ArrayList<Module> list = new ArrayList<Module>();
        String query = this.search.toLowerCase(Locale.ROOT).trim();
        for (Module module : SoberClient.MODULES.all()) {
            if (!query.isEmpty()) {
                if (!module.name.toLowerCase(Locale.ROOT).contains(query)) continue;
                list.add(module);
                continue;
            }
            if (module.category != category) continue;
            list.add(module);
        }
        return list;
    }

    private static int count(Category cat) {
        int n = 0;
        for (Module module : SoberClient.MODULES.all()) {
            if (module.category != cat) continue;
            ++n;
        }
        return n;
    }

    private void drawGrid(class_332 context, List<Module> modules, int mouseX, int mouseY) {
        int cols = this.selected == null ? 2 : 1;
        float colW = (float)(this.gridW - 5 * (cols - 1)) / (float)cols;
        int rows = (modules.size() + cols - 1) / cols;
        this.content = rows * 31 - 5;
        this.view = this.gridH;
        this.scrollTarget = Math.max(0.0f, Math.min(this.scrollTarget, Math.max(0.0f, this.content - this.view)));
        this.scroll = MenuKit.approach(this.scroll, this.scrollTarget, 16.0f, this.delta);
        if (modules.isEmpty()) {
            this.text(context, "No modules found", "geist-medium.ttf", this.gridX + 2, this.gridY + 12, 7.0f, -7434602);
            return;
        }
        boolean insideGrid = mouseX >= this.gridX && mouseX < this.gridX + this.gridW && mouseY >= this.gridY && mouseY < this.gridY + this.gridH;
        context.method_44379(this.gridX - 2, this.gridY - 2, this.gridX + this.gridW + 2, this.gridY + this.gridH);
        for (int i = 0; i < modules.size(); ++i) {
            Module module = modules.get(i);
            float x = (float)this.gridX + (float)(i % cols) * (colW + 5.0f);
            float y = (float)(this.gridY + i / cols * 31) - this.scroll;
            if (y + 26.0f < (float)this.gridY || y > (float)(this.gridY + this.gridH)) continue;
            boolean over = insideGrid && (float)mouseX >= x && (float)mouseX < x + colW && (float)mouseY >= y && (float)mouseY < y + 26.0f;
            this.drawCard(context, module, x, y, colW, over);
            float top = Math.max(y, (float)this.gridY);
            float bottom = Math.min(y + 26.0f, (float)(this.gridY + this.gridH));
            if (!(bottom > top)) continue;
            this.hit("card", x, top, colW, bottom - top, module);
        }
        context.method_44380();
        if (this.content > this.view + 1.0f) {
            float track = (float)this.gridH - 4.0f;
            float thumb = Math.max(18.0f, track * this.view / this.content);
            float ty = (float)this.gridY + 2.0f + (track - thumb) * (this.scroll / Math.max(1.0f, this.content - this.view));
            Smooth.rect(context, this.gridX + this.gridW + 3, Math.round(ty), 2, Math.round(thumb), 1, this.c(-12237491));
        }
    }

    private void drawCard(class_332 context, Module module, float x, float y, float w, boolean over) {
        boolean bindingThis;
        float hv = this.anim("card", module, over, 14.0f);
        float on = this.anim("on", module, module.isActive(), 14.0f);
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(w);
        Mono.box(context, ix, iy, iw, 26, hv, this.alpha);
        if (module == this.selected) {
            Smooth.outline(context, ix, iy, iw, 26, 3, this.c(-789252875));
        }
        if (on > 0.01f) {
            Smooth.rect(context, ix + 1, iy + 6, 2, 14, 1, this.c(MenuKit.alpha(-723723, on)));
        }
        float toggleX = x + w - 36.0f;
        this.toggle(context, toggleX, y + 6.0f, on);
        float right = toggleX - 8.0f;
        boolean bl = bindingThis = module == this.binding;
        if (bindingThis || module.getKey() >= 0) {
            String key = bindingThis ? "..." : MenuKit.keyName(module.getKey()).toUpperCase(Locale.ROOT);
            float kw = PanelScreen.width(key, "geist-semibold.ttf", 6.4f) + 10.0f;
            Smooth.rect(context, Math.round(right - kw), iy + 7, Math.round(kw), 12, 2, this.c(bindingThis ? 821359861 : 318043381));
            Smooth.outline(context, Math.round(right - kw), iy + 7, Math.round(kw), 12, 2, this.c(-14013906));
            this.text(context, key, "geist-semibold.ttf", right - kw + 5.0f, (float)iy + 15.6f, 6.4f, bindingThis ? -723723 : -7434602);
            right -= kw + 8.0f;
        }
        String name = Theme.label(module.name);
        float nameMax = Math.max(30.0f, right - x - 12.0f);
        String shownName = PanelScreen.fit(name, "geist-semibold.ttf", nameMax, 7.6f);
        this.text(context, shownName, "geist-semibold.ttf", x + 10.0f, y + 16.6f, 7.6f, MenuKit.lerp(-2829096, -723723, Math.max(on, hv)));
        float descX = x + 10.0f + PanelScreen.width(shownName, "geist-semibold.ttf", 7.6f) + 9.0f;
        String description = PanelScreen.fit(module.description, "geist-medium.ttf", right - descX, 6.4f);
        if (!description.isEmpty()) {
            this.text(context, description, "geist-medium.ttf", descX, y + 16.2f, 6.4f, -10724252);
        }
    }

    private void drawDrawer(class_332 context, int x, int y, int w, int h, int mouseX, int mouseY) {
        Mono.box(context, x, y, w, h, 0.0f, this.alpha);
        this.text(context, PanelScreen.fit(Theme.label(this.selected.name), "geist-semibold.ttf", w - 40, 8.6f), "geist-semibold.ttf", x + 10, (float)y + 17.0f, 8.6f, -723723);
        this.text(context, PanelScreen.fit(this.selected.description, "geist-medium.ttf", w - 20, 6.4f), "geist-medium.ttf", x + 10, (float)y + 29.0f, 6.4f, -10724252);
        float cx = x + w - 18;
        boolean over = (float)mouseX >= cx - 4.0f && (float)mouseX < cx + 14.0f && mouseY >= y + 6 && mouseY < y + 22;
        Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx), y + 9, 8, this.c(over ? -723723 : -10724252));
        this.hit("drawerClose", cx - 4.0f, y + 6, 18.0f, 16.0f, null);
        context.method_25294(x + 1, y + 37, x + w - 1, y + 38, this.c(-14013906));
        this.pane.render(context, x + 10, y + 44, w - 22, h - 50, mouseX, mouseY, this.alpha, this.delta);
    }

    private void drawLock(class_332 context, int cx, int cw, int mouseX, int mouseY) {
        float mid = (float)cx + (float)cw / 2.0f;
        float top = (float)this.gridY + Math.max(16.0f, (float)this.gridH * 0.22f);
        this.lock(context, mid, top, 13.0f, -723723);
        String title = "AC Off";
        this.text(context, title, "geist-semibold.ttf", mid - PanelScreen.width(title, "geist-semibold.ttf", 10.0f) / 2.0f, top + 26.0f, 10.0f, -723723);
        String[] lines = new String[]{"Anticheats can easily spot these modules.", "Only use them on servers where the anticheat is off."};
        for (int i = 0; i < lines.length; ++i) {
            this.text(context, lines[i], "geist-medium.ttf", mid - PanelScreen.width(lines[i], "geist-medium.ttf", 6.8f) / 2.0f, top + 42.0f + (float)(i * 11), 6.8f, -7434602);
        }
        float bw = 96.0f;
        float bh = 20.0f;
        float by = top + 70.0f;
        float gap = 8.0f;
        float ax = mid - bw - gap / 2.0f;
        float bx = mid + gap / 2.0f;
        boolean overCancel = (float)mouseX >= ax && (float)mouseX < ax + bw && (float)mouseY >= by && (float)mouseY < by + bh;
        boolean overOpen = (float)mouseX >= bx && (float)mouseX < bx + bw && (float)mouseY >= by && (float)mouseY < by + bh;
        Mono.box(context, Math.round(ax), Math.round(by), Math.round(bw), Math.round(bh), this.anim("lockCancel", (Object)this, overCancel, 14.0f), this.alpha);
        Mono.primary(context, Math.round(bx), Math.round(by), Math.round(bw), Math.round(bh), this.anim("lockOpen", (Object)this, overOpen, 14.0f), this.alpha);
        String cancel = "Cancel";
        String openLabel = "Show modules";
        this.text(context, cancel, "geist-semibold.ttf", ax + (bw - PanelScreen.width(cancel, "geist-semibold.ttf", 7.2f)) / 2.0f, by + 13.2f, 7.2f, -723723);
        this.text(context, openLabel, "geist-semibold.ttf", bx + (bw - PanelScreen.width(openLabel, "geist-semibold.ttf", 7.2f)) / 2.0f, by + 13.2f, 7.2f, -15856112);
        this.hit("lockCancel", ax, by, bw, bh, null);
        this.hit("lockOpen", bx, by, bw, bh, null);
    }

    private void drawTabSettings(class_332 context, int mouseX, int mouseY) {
        Module module = PanelScreen.tabModule(tab);
        if (module == null) {
            return;
        }
        int bodyY = this.wy + 30 + 1;
        int pw = Math.min(this.ww - 60, 440);
        int px = this.wx + (this.ww - pw) / 2;
        String title = switch (tab.ordinal()) {
            case 1 -> "HUD";
            case 2 -> "Configs";
            default -> "Settings";
        };
        String sub = switch (tab.ordinal()) {
            case 1 -> "What your HUD shows. Open the chat to move and resize the cards.";
            case 2 -> "Save, load and share your settings.";
            default -> "Look, fonts, sounds and keys of the menu. Panel Width, Height and Scale change the size of this window.";
        };
        this.text(context, title, "geist-semibold.ttf", px, (float)bodyY + 22.0f, 10.0f, -723723);
        this.text(context, PanelScreen.fit(sub, "geist-medium.ttf", pw, 6.8f), "geist-medium.ttf", px, (float)bodyY + 35.0f, 6.8f, -7434602);
        context.method_25294(px, bodyY + 43, px + pw, bodyY + 44, this.c(-14013906));
        this.pane.render(context, px, bodyY + 52, pw, this.wy + this.wh - 10 - (bodyY + 52), mouseX, mouseY, this.alpha, this.delta);
    }

    private Hit hitAt(double mx, double my) {
        for (int i = this.hits.size() - 1; i >= 0; --i) {
            if (!this.hits.get(i).contains(mx, my)) continue;
            return this.hits.get(i);
        }
        return null;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        boolean paneShown;
        if (this.closing) {
            return true;
        }
        double mx = click.comp_4798() / (double)this.z;
        double my = click.comp_4799() / (double)this.z;
        int button = click.method_74245();
        if (button == 0 && this.overGrip(mx, my)) {
            this.resizing = true;
            this.resizeX = mx;
            this.resizeY = my;
            this.resizeW = this.ww;
            this.resizeH = this.wh;
            return true;
        }
        if (this.binding != null) {
            this.binding = null;
            return true;
        }
        boolean bl = paneShown = tab != Tab.MODULES || this.selected != null;
        if (paneShown && this.pane.click(mx, my, button, doubled)) {
            return true;
        }
        Hit hit = this.hitAt(mx, my);
        if (hit == null) {
            return true;
        }
        switch (hit.kind()) {
            case "tab": {
                if (hit.target() != tab) {
                    this.switchTab((Tab)((Object)hit.target()));
                }
                UiSounds.click();
                break;
            }
            case "close": {
                this.method_25419();
                break;
            }
            case "style": {
                PanelScreen.options().style.set(ClickGui.Style.Dropdown);
                SoberClient.CONFIG.save();
                UiSounds.click();
                this.pane.commit();
                this.field_22787.method_1507((class_437)new DropdownScreen());
                break;
            }
            case "cat": {
                category = (Category)((Object)hit.target());
                this.search = "";
                this.selected = null;
                this.pane.show(null);
                this.scrollTarget = 0.0f;
                this.scroll = 0.0f;
                UiSounds.click();
                break;
            }
            case "card": {
                this.clickCard((Module)hit.target(), button);
                break;
            }
            case "drawerClose": {
                this.selected = null;
                this.pane.show(null);
                UiSounds.click();
                break;
            }
            case "lockCancel": {
                category = Category.BASE_FINDING;
                UiSounds.click();
                break;
            }
            case "lockOpen": {
                acUnlocked = true;
                UiSounds.click();
                break;
            }
        }
        return true;
    }

    private void clickCard(Module module, int button) {
        if (button == 2) {
            this.binding = module.bindable() ? module : null;
            UiSounds.click();
            return;
        }
        if (button == 1 || MenuKit.opensMenu(module)) {
            this.selected = this.selected == module ? null : module;
            this.pane.show(this.selected);
            UiSounds.click();
            return;
        }
        module.toggle();
        MenuKit.touch(module);
        UiSounds.moduleToggle(module.isActive());
        SoberClient.CONFIG.save();
    }

    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        if (this.resizing) {
            double mx = click.comp_4798() / (double)this.z;
            double my = click.comp_4799() / (double)this.z;
            int w = (int)Math.round((double)this.resizeW + (mx - this.resizeX) * 2.0);
            int h = (int)Math.round((double)this.resizeH + (my - this.resizeY) * 2.0);
            PanelScreen.options().panelWidth.set(Math.round((float)Math.clamp((long)w, 380, this.vw - 8) * 100.0f / (float)this.vw));
            PanelScreen.options().panelHeight.set(Math.round((float)Math.clamp((long)h, 250, this.vh - 8) * 100.0f / (float)this.vh));
            return true;
        }
        return this.pane.drag(click.comp_4798() / (double)this.z, click.comp_4799() / (double)this.z) || super.method_25403(click, offsetX, offsetY);
    }

    public boolean method_25406(class_11909 click) {
        if (this.resizing) {
            this.resizing = false;
            SoberClient.CONFIG.save();
            return true;
        }
        return this.pane.release() || super.method_25406(click);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
        boolean ctrl = class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)341) || class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)345);
        boolean shift = class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)340);
        if ((tab != Tab.MODULES || this.selected != null) && this.pane.scroll(mouseX /= (double)this.z, mouseY /= (double)this.z, vertical, ctrl, shift)) {
            return true;
        }
        if (ctrl && vertical != 0.0) {
            PanelScreen.options().panelScale.set(PanelScreen.options().panelScale.get() + (vertical > 0.0 ? 10 : -10));
            SoberClient.CONFIG.save();
            return true;
        }
        if (tab == Tab.MODULES && mouseX >= (double)this.gridX && mouseX < (double)(this.gridX + this.gridW + 6) && mouseY >= (double)this.gridY && mouseY < (double)(this.gridY + this.gridH)) {
            this.scrollTarget = Math.max(0.0f, Math.min(Math.max(0.0f, this.content - this.view), this.scrollTarget - (float)vertical * 31.0f));
        }
        return true;
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
        if (this.pane.key(input)) {
            return true;
        }
        if (key == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            UiSounds.key(0.7f);
            return true;
        }
        if (key == 256) {
            if (!this.search.isEmpty()) {
                this.search = "";
            } else if (this.selected != null) {
                this.selected = null;
                this.pane.show(null);
            } else {
                this.method_25419();
            }
            return true;
        }
        if (key == PanelScreen.options().getKey() && this.search.isEmpty() && !this.pane.typing()) {
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
        if (this.pane.typed(input)) {
            return true;
        }
        if (tab != Tab.MODULES) {
            return true;
        }
        String typed = input.method_74226();
        if (this.search.isEmpty() && typed.isBlank()) {
            return true;
        }
        if (this.search.length() < 40) {
            this.search = this.search + typed;
            this.selected = null;
            this.pane.show(null);
            this.scrollTarget = 0.0f;
            this.scroll = 0.0f;
            UiSounds.key(1.0f);
        }
        return true;
    }

    private static enum Tab {
        MODULES("Modules"),
        HUD("HUD"),
        CONFIGS("Configs"),
        SETTINGS("Settings");

        final String label;

        private Tab(String label) {
            this.label = label;
        }
    }

    private record Key(String tag, Object target) {
    }

    private record Hit(String kind, float x, float y, float w, float h, Object target) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
        }
    }
}

