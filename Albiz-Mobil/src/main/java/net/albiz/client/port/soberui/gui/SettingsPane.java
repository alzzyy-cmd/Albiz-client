// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.ConfigSlots;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.BlockPickerScreen;
import net.albiz.client.port.soberui.gui.ColorWheel;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.ModulePreview;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Status;
import net.albiz.client.port.soberui.modules.Configs;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

final class SettingsPane {
    private static final float NAME = 0.8f;
    private static final float VALUE = 0.74f;
    private static final float SMALL = 0.62f;
    private static final int PICKER_H = 76;
    private static final Set<ColorSetting> OPEN_COLORS = new HashSet<ColorSetting>();
    private final class_437 host;
    private final class_310 client = class_310.method_1551();
    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<Key, Float> animations = new HashMap<Key, Float>();
    private final Map<Object, Long> pressedAt = new HashMap<Object, Long>();
    private Module module;
    private boolean preview = true;
    private float scroll;
    private float scrollTarget;
    private float content;
    private float view;
    private int boxX;
    private int boxY;
    private int boxW;
    private int boxH;
    private float opacity = 1.0f;
    private float delta;
    private Hit dragging;
    private Setting<?> editing;
    private String editBuffer = "";
    private Setting<?> invalidSetting;
    private float hexX;
    private float hexW;
    private long invalidUntil;
    private boolean binding;
    private KeySetting keyBinding;
    private long swallowUntil;
    private boolean renaming;
    private String renameBuffer = "";
    private boolean typingCode;
    private String codeText = "";
    private Setting<?> focused;
    private long focusedAt;
    private float focusOffset = -1.0f;
    private String tooltip;
    private Object hoverKey;
    static final Palette DARK = new Palette(true, -855051, -4012338, -6906969, 0x16FFFFFF, 0x28FFFFFF, 0x24FFFFFF, 0x3CFFFFFF, 0x26FFFFFF, 0xEFFFFFF, -855051, 0x3CFFFFFF, 0, true, 0);
    private Palette pal = DARK;
    private static final float TILE = 30.0f;
    private static final float TILE_GAP = 4.0f;
    private static final String CONFIG_NOTE = "Imported configs never change Auction Sniper, Auto Sell, Auto Shop, RTP Method or Panic Order, because they send commands.";

    private int accent() {
        return this.pal.accent() != 0 ? this.pal.accent() : MenuKit.accent();
    }

    void palette(Palette palette) {
        this.pal = palette;
    }

    private void well(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
        Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hover), opacity));
        Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hover), opacity));
    }

    private void toggle(class_332 context, float x, float y, float w, float h, float on) {
        int onColor;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(w);
        int ih = Math.round(h);
        int n = onColor = this.pal.toggleOn() != 0 ? this.pal.toggleOn() : this.accent();
        if (this.pal.glow() && on > 0.01f) {
            Smooth.glow(context, ix + iw / 2, iy + ih / 2, Math.round((float)iw * 0.75f), this.c(MenuKit.withAlpha(onColor, Math.round(85.0f * on))));
        }
        Smooth.rect(context, ix, iy, iw, ih, ih / 2, this.c(MenuKit.lerp(this.pal.toggleOff(), onColor, on)));
        if (this.pal.dark() && on < 0.99f) {
            Smooth.outline(context, ix, iy, iw, ih, ih / 2, this.c(MenuKit.alpha(0x22FFFFFF, 1.0f - on)));
        }
        int knob = ih - 4;
        int kx = Math.round((float)(ix + 2) + (float)(iw - knob - 4) * on);
        int luma = ((onColor >> 16 & 0xFF) * 3 + (onColor >> 8 & 0xFF) * 6 + (onColor & 0xFF)) / 10;
        Smooth.rect(context, kx, iy + 3, knob, knob, knob / 2, this.c(0x38000000));
        Smooth.rect(context, kx, iy + 2, knob, knob, knob / 2, this.c(MenuKit.lerp(-1, luma > 205 ? -15395042 : -1, on)));
    }

    SettingsPane(class_437 host) {
        this.host = host;
    }

    Module module() {
        return this.module;
    }

    void show(Module next) {
        if (next == this.module) {
            return;
        }
        this.commit();
        this.module = next;
        this.scroll = 0.0f;
        this.scrollTarget = 0.0f;
        this.focused = null;
        this.focusOffset = -1.0f;
        this.binding = false;
        this.keyBinding = null;
    }

    void preview(boolean show) {
        this.preview = show;
    }

    void bindNow() {
        if (this.module != null && this.module.bindable()) {
            this.binding = true;
        }
    }

    boolean binding() {
        return this.binding;
    }

    void focus(Setting<?> setting) {
        this.focused = setting;
        this.focusedAt = System.currentTimeMillis();
        this.focusOffset = -1.0f;
    }

    String tooltip() {
        return this.tooltip;
    }

    Object hoverKey() {
        return this.hoverKey;
    }

    boolean typing() {
        return this.editing != null || this.renaming || this.typingCode || this.binding || this.keyBinding != null;
    }

    boolean contains(double mx, double my) {
        return mx >= (double)this.boxX && my >= (double)this.boxY && mx < (double)(this.boxX + this.boxW) && my < (double)(this.boxY + this.boxH);
    }

    float contentHeight() {
        return this.content;
    }

    private float anim(String tag, Object target, boolean on, float speed) {
        Key key = new Key(tag, target);
        float value = MenuKit.approach(this.animations.getOrDefault(key, Float.valueOf(on ? 1.0f : 0.0f)).floatValue(), on ? 1.0f : 0.0f, speed, this.delta);
        this.animations.put(key, Float.valueOf(value));
        return value;
    }

    private float press(Object key) {
        Long time = this.pressedAt.get(key);
        return time == null ? 0.0f : Math.max(0.0f, 1.0f - (float)(System.nanoTime() - time) / 2.1E8f);
    }

    private int c(int color) {
        return MenuKit.alpha(color, this.opacity);
    }

    private void text(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
        MenuKit.text(context, value, x, y, size, this.c(color), heavy);
    }

    private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
        Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.c(color));
    }

    private void outline(class_332 context, float x, float y, float w, float h, int r, int color) {
        Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.c(color));
    }

    private void hit(Kind kind, float x, float y, float w, float h, Object target, int index) {
        float top = this.boxY;
        float bottom = this.boxY + this.boxH;
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

    private void hover(Object key, String description) {
        this.hoverKey = key;
        this.tooltip = description;
    }

    private List<Setting<?>> visibleSettings() {
        ArrayList list = new ArrayList();
        if (this.module == null) {
            return list;
        }
        for (Setting<?> setting : this.module.getSettings()) {
            if (!setting.isVisible()) continue;
            list.add(setting);
        }
        return list;
    }

    private float rowHeight(Setting<?> setting) {
        float height;
        float f = height = setting instanceof IntSetting ? 30.0f : 22.0f;
        if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            height += 76.0f * this.animations.getOrDefault(new Key("picker", color), Float.valueOf(0.0f)).floatValue();
        }
        return height;
    }

    private boolean grouped(List<Setting<?>> settings) {
        String first = null;
        for (Setting<?> setting : settings) {
            if (first == null) {
                first = setting.getGroup();
                continue;
            }
            if (first.equals(setting.getGroup())) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - void declaration
     */
    void render(class_332 context, int x, int y, int w, int h, int mouseX, int mouseY, float opacity, float delta) {
        Module module;
        List<Status.Row> list;
        this.opacity = opacity;
        this.delta = delta;
        this.boxX = x;
        this.boxY = y;
        this.boxW = w;
        this.boxH = h;
        this.hits.clear();
        this.tooltip = null;
        this.hoverKey = null;
        if (this.module == null || opacity <= 0.01f) {
            return;
        }
        this.view = h;
        boolean inside = this.contains(mouseX, mouseY);
        int mx = inside ? mouseX : -10000;
        int my = inside ? mouseY : -10000;
        List<Setting<?>> settings = this.visibleSettings();
        boolean groups = this.grouped(settings);
        boolean configs = this.module instanceof Configs;
        Module module2 = this.module;
        if (module2 instanceof Status) {
            Status s = (Status)((Object)module2);
            list = s.statusRows();
        } else {
            list = List.of();
        }
        List status = list;
        int presets = configs ? 0 : MenuKit.presetCount(this.module);
        boolean showPreview = this.preview && ModulePreview.supports(this.module);
        float previewH = showPreview ? (float)Math.round((float)w * 0.5f) : 0.0f;
        float total = 0.0f;
        if (showPreview) {
            total += previewH + 10.0f;
        }
        if (configs) {
            total += this.configHeight(w);
        }
        if (!status.isEmpty()) {
            total += 16.0f + (float)status.size() * 32.0f + 6.0f;
        }
        if (presets > 0) {
            total += 16.0f + this.presetRowsHeight(w, presets) + 8.0f;
        }
        String group = null;
        float focusY = -1.0f;
        for (Setting<?> setting : settings) {
            if (groups && !setting.getGroup().equals(group)) {
                group = setting.getGroup();
                total += 18.0f;
            }
            if (setting == this.focused) {
                focusY = total;
            }
            total += this.rowHeight(setting);
        }
        if (this.module.bindable()) {
            total += 30.0f;
        }
        this.content = total;
        if (this.focused != null && focusY >= 0.0f && this.focusOffset < 0.0f) {
            this.focusOffset = focusY;
            this.scrollTarget = Math.max(0.0f, focusY - (float)h * 0.3f);
        }
        this.scrollTarget = Math.max(0.0f, Math.min(this.scrollTarget, Math.max(0.0f, this.content - this.view)));
        this.scroll = MenuKit.approach(this.scroll, this.scrollTarget, 16.0f, delta);
        context.method_44379(x - 6, y, x + w + 6, y + h);
        float sy = (float)y - this.scroll;
        if (showPreview) {
            if (sy + previewH >= (float)y && sy <= (float)(y + h)) {
                ModulePreview.draw(context, this.module, x, sy, w, previewH, opacity);
                String sub = ModulePreview.subtitle(this.module);
                Object object = sub.isEmpty() ? "LIVE" : "LIVE  \u00b7  " + sub;
                MenuKit.chip(context, MenuKit.fit((String)object, w - 24, 0.62f, true), x + 6, sy + previewH - 17.0f, 0.62f, opacity);
            }
            sy += previewH + 10.0f;
        }
        if (configs) {
            sy = this.drawConfigs(context, x, sy, w, mx, my);
        }
        if (!status.isEmpty() && (module = this.module) instanceof Status) {
            int var25_32=0;
            Status s = (Status)((Object)module);
            this.text(context, MenuKit.label(s.statusTitle()), x + 2, sy + 3.0f, 0.62f, this.pal.faint(), true);
            sy += 16.0f;
            boolean bl = false;
            while (var25_32 < status.size()) {
                Status.Row row = (Status.Row)status.get((int)var25_32);
                float ry = sy + (float)var25_32 * 32.0f;
                if (!(ry + 28.0f < (float)y) && !(ry > (float)(y + h))) {
                    boolean over = mx >= x && mx < x + w && (float)my >= ry && (float)my < ry + 28.0f;
                    float hv = this.anim("status", this.module.name + (int)var25_32, over, 12.0f);
                    this.well(context, x, Math.round(ry), w, 28, 7, hv, opacity);
                    float click = this.press("status" + (int)var25_32);
                    if (click > 0.0f) {
                        this.rect(context, x, ry, w, 28.0f, 7, MenuKit.withAlpha(this.accent(), Math.round(60.0f * click)));
                    }
                    float bw = MenuKit.width(row.badge(), 0.62f, true) + 10.0f;
                    this.rect(context, (float)(x + w - 6) - bw, ry + 8.0f, bw, 12.0f, 6, row.badgeColor());
                    this.text(context, row.badge(), (float)(x + w - 1) - bw, ry + 10.0f, 0.62f, -1, true);
                    this.text(context, MenuKit.fit(row.title(), (float)w - bw - 22.0f, 0.74f, true), x + 7, ry + 4.0f, 0.74f, this.pal.text(), true);
                    this.text(context, MenuKit.fit(row.detail(), (float)w - bw - 22.0f, 0.62f, false), x + 7, ry + 16.0f, 0.62f, MenuKit.lerp(this.pal.muted(), this.accent(), hv), false);
                    this.hit(Kind.STATUS, x, ry, w, 28.0f, row.link(), (int)var25_32);
                    if (over) {
                        this.hover("status" + (int)var25_32, s.statusDescription());
                    }
                }
                ++var25_32;
            }
            sy += (float)status.size() * 32.0f + 6.0f;
        }
        if (presets > 0) {
            this.text(context, "PRESETS", x + 2, sy + 3.0f, 0.62f, this.pal.faint(), true);
            sy += 16.0f;
            sy = this.drawPresets(context, x, sy, w, presets, mx, my) + 8.0f;
        }
        group = null;
        for (Setting setting : settings) {
            float height;
            if (groups && !setting.getGroup().equals(group)) {
                group = setting.getGroup();
                if (sy + 18.0f >= (float)y && sy <= (float)(y + h)) {
                    this.text(context, MenuKit.label(group), x + 2, sy + 6.0f, 0.62f, this.pal.faint(), true);
                }
                sy += 18.0f;
            }
            if (sy + (height = this.rowHeight(setting)) >= (float)y && sy <= (float)(y + h)) {
                this.drawSetting(context, setting, x, sy, w, mx, my);
            }
            sy += height;
        }
        if (this.module.bindable()) {
            if (sy + 30.0f >= (float)y && sy <= (float)(y + h)) {
                this.drawBind(context, x, sy + 6.0f, w, mx, my);
            }
            sy += 30.0f;
        }
        if (settings.isEmpty() && presets == 0 && !configs && status.isEmpty() && !showPreview && !this.module.bindable()) {
            this.text(context, "No settings", x + 2, y + 6, 0.74f, this.pal.muted(), false);
        }
        context.method_44380();
        if (this.content > this.view + 1.0f) {
            float track = (float)h - 4.0f;
            float f = Math.max(18.0f, track * this.view / this.content);
            float ty = (float)y + 2.0f + (track - f) * (this.scroll / Math.max(1.0f, this.content - this.view));
            this.rect(context, x + w + 3, ty, 2.0f, f, 1, this.pal.lineHover());
        }
    }

    private int tileColumns(int w) {
        return Math.max(1, (int)(((float)w + 4.0f) / 34.0f));
    }

    private float presetRowsHeight(int w, int count) {
        if (MenuKit.presetIcons(this.module)) {
            return (float)((count + this.tileColumns(w) - 1) / this.tileColumns(w)) * 34.0f;
        }
        float cx = 0.0f;
        float rows = 1.0f;
        for (int i = 0; i < count; ++i) {
            float pw = Math.min((float)w, MenuKit.width(MenuKit.presetName(this.module, i), 0.74f, true) + 16.0f);
            if (cx > 0.0f && cx + pw > (float)w) {
                rows += 1.0f;
                cx = 0.0f;
            }
            cx += pw + 4.0f;
        }
        return rows * 20.0f;
    }

    private float drawTiles(class_332 context, int x, float sy, int w, int count, int mx, int my) {
        int columns = this.tileColumns(w);
        float left = (float)x + ((float)w - ((float)columns * 34.0f - 4.0f)) / 2.0f;
        int accent = this.accent();
        for (int i = 0; i < count; ++i) {
            float px = left + (float)(i % columns) * 34.0f;
            float py = sy + (float)(i / columns) * 34.0f;
            if (py + 30.0f < (float)this.boxY || py > (float)(this.boxY + this.boxH)) continue;
            boolean active = MenuKit.presetActive(this.module, i);
            boolean over = (float)mx >= px && (float)mx < px + 30.0f && (float)my >= py && (float)my < py + 30.0f;
            float hv = this.anim("preset", this.module.name + i, over, 14.0f);
            float on = this.anim("presetOn", this.module.name + i, active, 14.0f);
            this.rect(context, px, py, 30.0f, 30.0f, 7, MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv));
            if (on > 0.01f) {
                if (this.pal.glow()) {
                    Smooth.glow(context, Math.round(px + 15.0f), Math.round(py + 15.0f), Math.round(21.0f), this.c(MenuKit.withAlpha(accent, Math.round(48.0f * on))));
                }
                this.outline(context, px, py, 30.0f, 30.0f, 7, MenuKit.alpha(accent, on));
            } else {
                this.outline(context, px, py, 30.0f, 30.0f, 7, MenuKit.alpha(this.pal.line(), 0.6f));
            }
            MenuKit.presetItem(context, MenuKit.presetIcon(this.module, i), px + 15.0f, py + 15.0f - hv, 22.0f + 2.0f * hv);
            this.hit(Kind.PRESET, px, py, 30.0f, 30.0f, this.module, i);
            if (!over) continue;
            this.hover("preset" + i, MenuKit.presetName(this.module, i));
        }
        return sy + (float)((count + columns - 1) / columns) * 34.0f;
    }

    private float drawPresets(class_332 context, int x, float sy, int w, int count, int mx, int my) {
        if (MenuKit.presetIcons(this.module)) {
            return this.drawTiles(context, x, sy, w, count, mx, my);
        }
        float cx = 0.0f;
        float cy = 0.0f;
        int accent = this.accent();
        for (int i = 0; i < count; ++i) {
            String name = MenuKit.presetName(this.module, i);
            float pw = Math.min((float)w, MenuKit.width(name, 0.74f, true) + 16.0f);
            if (cx > 0.0f && cx + pw > (float)w) {
                cx = 0.0f;
                cy += 20.0f;
            }
            float px = (float)x + cx;
            float py = sy + cy;
            boolean active = MenuKit.presetActive(this.module, i);
            boolean over = (float)mx >= px && (float)mx < px + pw && (float)my >= py && (float)my < py + 16.0f;
            float hv = this.anim("preset", this.module.name + i, over, 14.0f);
            float on = this.anim("presetOn", this.module.name + i, active, 14.0f);
            if (this.pal.glow() && on > 0.01f) {
                Smooth.glow(context, Math.round(px + pw / 2.0f), Math.round(py + 8.0f), Math.round(pw * 0.6f), this.c(MenuKit.withAlpha(accent, Math.round(64.0f * on))));
            }
            this.rect(context, px, py, pw, 16.0f, 8, MenuKit.lerp(MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv), accent, on));
            if (on < 0.99f) {
                this.outline(context, px, py, pw, 16.0f, 8, MenuKit.alpha(this.pal.line(), 1.0f - on));
            }
            this.text(context, MenuKit.fit(name, pw - 12.0f, 0.74f, true), px + 8.0f, py + 4.5f, 0.74f, MenuKit.lerp(this.pal.text(), -15069650, on), true);
            this.hit(Kind.PRESET, px, py, pw, 16.0f, this.module, i);
            if (over) {
                this.hover("preset" + i, MenuKit.presetDescription(this.module));
            }
            cx += pw + 4.0f;
        }
        return sy + cy + 20.0f;
    }

    private void drawSetting(class_332 context, Setting<?> setting, int x, float sy, int w, int mx, int my) {
        float age;
        float pulse;
        float click;
        float f;
        if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            f = this.anim("picker", color, OPEN_COLORS.contains(color), 13.0f);
        } else {
            f = 0.0f;
        }
        float picker = f;
        float base = setting instanceof IntSetting ? 30.0f : 22.0f;
        float rowH = base + 76.0f * picker;
        boolean over = mx >= x - 4 && mx < x + w + 4 && (float)my >= sy && (float)my < sy + base;
        float hv = this.anim("setting", setting, over, 12.0f);
        if (hv > 0.01f || picker > 0.01f) {
            this.rect(context, x - 4, sy + 1.0f, w + 8, rowH - 2.0f, 7, MenuKit.alpha(this.pal.hover(), Math.max(hv, picker)));
        }
        if ((click = this.press(setting)) > 0.0f) {
            this.rect(context, x - 4, sy + 1.0f, w + 8, base - 2.0f, 7, MenuKit.withAlpha(this.accent(), Math.round(50.0f * click)));
        }
        if (setting == this.focused && (pulse = Math.max(0.0f, 1.0f - (age = (float)(System.currentTimeMillis() - this.focusedAt) / 1000.0f) / 1.6f)) > 0.0f) {
            this.rect(context, x - 4, sy + 1.0f, w + 8, base - 2.0f, 7, MenuKit.withAlpha(this.accent(), Math.round(70.0f * pulse)));
            this.outline(context, x - 4, sy + 1.0f, w + 8, base - 2.0f, 7, MenuKit.withAlpha(this.accent(), Math.round(200.0f * pulse)));
        }
        if (over) {
            this.hover(setting, setting.description);
        }
        int accent = this.accent();
        float right = x + w;
        if (setting instanceof IntSetting) {
            IntSetting integer = (IntSetting)setting;
            String value = integer.display();
            float vw = MenuKit.width(value, 0.74f, true);
            this.text(context, MenuKit.fit(setting.name, (float)w - vw - 10.0f, 0.8f, true), x, sy + 4.0f, 0.8f, this.pal.text(), true);
            this.text(context, value, right - vw, sy + 4.5f, 0.74f, this.pal.dark() ? MenuKit.lerp(accent, -1, 0.45f) : accent, true);
            float p = (float)((Integer)integer.get() - integer.min) / (float)Math.max(1, integer.max - integer.min);
            Key key = new Key("slider", setting);
            float shown = MenuKit.approach(this.animations.getOrDefault(key, Float.valueOf(p)).floatValue(), p, 20.0f, this.delta);
            this.animations.put(key, Float.valueOf(shown));
            float ty = sy + 19.0f;
            this.rect(context, x, ty, w, 3.0f, 1, this.pal.track());
            if (shown > 0.0f) {
                this.rect(context, x, ty, Math.max(3.0f, (float)w * shown), 3.0f, 1, accent);
            }
            float kx = (float)x + (float)w * shown;
            if (this.pal.glow()) {
                Smooth.glow(context, Math.round(kx), Math.round(ty + 1.5f), 9, this.c(MenuKit.withAlpha(accent, 96)));
            }
            this.rect(context, kx - 4.0f, ty - 2.5f, 8.0f, 8.0f, 4, this.pal.knob());
            if (!this.pal.dark()) {
                this.outline(context, kx - 4.0f, ty - 2.5f, 8.0f, 8.0f, 4, this.pal.lineHover());
            }
            this.hit(Kind.SETTING, x - 4, sy, w + 8, base, setting, 0);
            return;
        }
        float nameW = (float)w * 0.56f;
        this.text(context, MenuKit.fit(setting.name, nameW, 0.8f, true), x, sy + 7.0f, 0.8f, this.pal.text(), true);
        if(setting instanceof net.albiz.client.port.soberui.setting.ItemListSetting items){this.commit();this.client.method_1507(new ItemPickerScreen(this.host,items));return;}
        if (setting instanceof net.albiz.client.port.soberui.AlbizModuleView.LegacyEditorSetting complex) {
            if(complex.original instanceof net.albiz.client.module.setting.ActionSetting action){action.run();return;}
            if(this.module instanceof net.albiz.client.port.soberui.AlbizModuleView view){this.commit();this.client.method_1507(new net.albiz.client.gui.LegacySettingsScreen(this.host,view.target));return;}
        }
        if (setting instanceof BoolSetting) {
            BoolSetting bool = (BoolSetting)setting;
            float p = this.anim("bool", setting, (Boolean)bool.get(), 15.0f);
            this.toggle(context, right - 22.0f, sy + 5.5f, 22.0f, 11.0f, p);
        } else if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            boolean edit = this.editing == setting;
            Object hex = edit ? this.editBuffer + "|" : color.display();
            this.hexW = Math.max(MenuKit.width("#DDDDDD|", 0.74f, false), MenuKit.width((String)hex, 0.74f, false)) + 10.0f;
            this.hexX = right - 18.0f - this.hexW;
            boolean overHex = (float)mx >= this.hexX && (float)mx < this.hexX + this.hexW && (float)my >= sy && (float)my < sy + base;
            boolean invalid = this.invalidSetting == setting && System.currentTimeMillis() < this.invalidUntil;
            float hp = this.anim("hex", setting, overHex || edit, 14.0f);
            this.rect(context, this.hexX, sy + 4.0f, this.hexW, 14.0f, 5, invalid ? -9819600 : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hp));
            this.outline(context, this.hexX, sy + 4.0f, this.hexW, 14.0f, 5, edit ? MenuKit.withAlpha(accent, 192) : MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hp));
            this.text(context, (String)hex, this.hexX + 5.0f, sy + 7.5f, 0.74f, edit ? this.pal.text() : this.pal.muted(), false);
            if (overHex) {
                this.hover("hex" + setting.name, "Click and type a color code like #FF0000.");
            }
            this.rect(context, right - 12.0f, sy + 5.0f, 12.0f, 12.0f, 4, ((Color)color.get()).packed() | 0xFF000000);
            this.outline(context, right - 12.0f, sy + 5.0f, 12.0f, 12.0f, 4, this.pal.lineHover());
            if (picker > 0.01f) {
                this.drawPicker(context, color, x, sy, w, picker);
            }
        } else {
            BlockListSetting blocks;
            boolean edit;
            boolean field = setting instanceof StringSetting || setting instanceof ItemSetting;
            boolean bl = edit = this.editing == setting;
            Object value = edit ? MenuKit.tail(this.editBuffer, (float)w * 0.42f - 14.0f, 0.74f, false) + "|" : (this.keyBinding == setting ? "Press a key" : (setting instanceof BlockListSetting ? (((Set)(blocks = (BlockListSetting)setting).get()).isEmpty() ? "Pick blocks" : ((Set)blocks.get()).size() + " blocks") : setting.display()));
            float pw = field ? Math.max(70.0f, (float)w * 0.42f) : Math.min((float)w * 0.44f, MenuKit.width((String)value, 0.74f, true) + (setting instanceof EnumSetting ? 22.0f : 14.0f));
            float px = right - pw;
            boolean invalid = edit && System.currentTimeMillis() < this.invalidUntil;
            float hp = this.anim("pill", setting, over || edit, 14.0f);
            this.rect(context, px, sy + 3.0f, pw, 16.0f, 6, invalid ? -9819600 : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hp));
            this.outline(context, px, sy + 3.0f, pw, 16.0f, 6, edit ? MenuKit.withAlpha(accent, 192) : MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hp));
            String shown = MenuKit.fit((String)value, pw - (setting instanceof EnumSetting ? 20.0f : 10.0f), 0.74f, !field);
            this.text(context, shown, px + 6.0f, sy + 7.5f, 0.74f, this.pal.text(), !field);
            if (setting instanceof EnumSetting) {
                Icons.draw(context, Icons.Icon.CHEVRON, Math.round(right - 12.0f), Math.round(sy + 7.0f), 8, this.c(this.pal.muted()));
            }
        }
        this.hit(Kind.SETTING, x - 4, sy, w + 8, base, setting, 0);
        if (setting instanceof ColorSetting) {
            this.hit(Kind.HEX, this.hexX, sy + 3.0f, this.hexW, 16.0f, setting, 0);
        }
    }

    private void drawPicker(class_332 context, ColorSetting color, int x, float sy, int w, float open) {
        float previous = this.opacity;
        this.opacity = previous * open;
        float[] hsv = color.hsv();
        int wheel = 60;
        int wx = x + 4;
        int wy = Math.round(sy + 26.0f);
        ColorWheel.draw(context, wx, wy, wheel, hsv[2], this.opacity);
        double angle = (double)hsv[0] * Math.PI * 2.0;
        double radius = (double)hsv[1] * ((double)wheel / 2.0 - 1.0);
        int px = wx + wheel / 2 + (int)Math.round(Math.cos(angle) * radius);
        int py = wy + wheel / 2 + (int)Math.round(Math.sin(angle) * radius);
        Smooth.circle(context, px, py, 4, this.c(-1));
        Smooth.circle(context, px, py, 3, this.c(0xFF000000 | Color.fromHsv(hsv[0], hsv[1], 1.0f, 255).packed()));
        if (open > 0.95f) {
            this.hit(Kind.WHEEL, wx, wy, wheel, wheel, color, 0);
        }
        int side = wx + wheel + 12;
        int sideW = x + w - 4 - side;
        for (int channel = 0; channel < 2; ++channel) {
            int barY = wy + 12 + channel * 26;
            int start = channel == 0 ? -16777216 : ((Color)color.get()).packed() & 0xFFFFFF;
            int end = 0xFF000000 | (channel == 0 ? Color.fromHsv(hsv[0], hsv[1], 1.0f, 255).packed() : ((Color)color.get()).packed());
            this.text(context, channel == 0 ? "Brightness" : "Alpha", side, barY - 10, 0.62f, this.pal.muted(), false);
            Smooth.horizontalGradient(context, side + 3, barY, side + sideW - 3, barY + 6, this.c(start), this.c(end));
            Smooth.rect(context, side, barY, 6, 6, 3, this.c(start));
            Smooth.rect(context, side + sideW - 6, barY, 6, 6, 3, this.c(end));
            float value = channel == 0 ? hsv[2] : (float)((Color)color.get()).a() / 255.0f;
            Smooth.circle(context, side + Math.round((float)sideW * value), barY + 3, 5, this.c(this.pal.text()));
            if (!(open > 0.95f)) continue;
            this.hit(channel == 0 ? Kind.BRIGHTNESS : Kind.ALPHA, side - 4, barY - 4, sideW + 8, 14.0f, color, channel);
        }
        this.opacity = previous;
    }

    private void drawBind(class_332 context, int x, float sy, int w, int mx, int my) {
        boolean over = mx >= x - 4 && mx < x + w + 4 && (float)my >= sy && (float)my < sy + 20.0f;
        float hv = this.anim("bindRow", this.module, over || this.binding, 12.0f);
        if (hv > 0.01f) {
            this.rect(context, x - 4, sy, w + 8, 20.0f, 7, MenuKit.alpha(this.pal.hover(), hv));
        }
        this.text(context, "Keybind", x, sy + 6.0f, 0.8f, this.pal.text(), true);
        String value = this.binding ? "Press a key..." : MenuKit.keyName(this.module.getKey());
        float pw = Math.min((float)w * 0.5f, MenuKit.width(value, 0.74f, true) + 14.0f);
        int accent = this.accent();
        this.rect(context, (float)(x + w) - pw, sy + 2.0f, pw, 16.0f, 6, this.binding ? MenuKit.withAlpha(accent, 80) : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv));
        this.outline(context, (float)(x + w) - pw, sy + 2.0f, pw, 16.0f, 6, this.binding ? MenuKit.withAlpha(accent, 208) : this.pal.line());
        this.text(context, MenuKit.fit(value, pw - 10.0f, 0.74f, true), (float)(x + w) - pw + 7.0f, sy + 6.5f, 0.74f, this.pal.text(), true);
        this.hit(Kind.BIND, x - 4, sy, w + 8, 20.0f, this.module, 0);
        if (over) {
            this.hover("bind", "Click, then press a key. Escape or Delete removes the bind.");
        }
    }

    private float configHeight(int w) {
        return 242.0f + (float)MenuKit.wrap(CONFIG_NOTE, w, 0.62f, false).size() * 9.0f + 8.0f;
    }

    private float drawConfigs(class_332 context, int x, float sy, int w, int mx, int my) {
        Configs configs = (Configs)this.module;
        int current = configs.slot();
        int accent = this.accent();
        for (int i = 0; i < 5; ++i) {
            float click;
            float ry = sy + (float)i * 30.0f;
            boolean over = mx >= x && mx < x + w && (float)my >= ry && (float)my < ry + 26.0f;
            float hv = this.anim("cslot", i, over, 12.0f);
            float s = this.anim("cselect", i, i == current, 14.0f);
            this.well(context, x, Math.round(ry), w, 26, 7, hv, this.opacity);
            if (s > 0.01f) {
                this.rect(context, x, ry, w, 26.0f, 7, MenuKit.withAlpha(accent, Math.round(38.0f * s)));
                this.rect(context, x + 3, ry + 7.0f, 2.0f, 12.0f, 1, MenuKit.withAlpha(accent, Math.round(255.0f * s)));
            }
            if ((click = this.press("cslot" + i)) > 0.0f) {
                this.rect(context, x, ry, w, 26.0f, 7, MenuKit.withAlpha(accent, Math.round(60.0f * click)));
            }
            if (this.renaming && i == current) {
                float fw = Math.min(160, w - 70);
                this.rect(context, x + 9, ry + 3.0f, fw, 12.0f, 4, this.pal.softHover());
                this.text(context, MenuKit.tail(this.renameBuffer, fw - 10.0f, 0.74f, true) + "|", x + 13, ry + 5.0f, 0.74f, this.pal.text(), true);
            } else {
                this.text(context, MenuKit.fit(ConfigSlots.name(i), w - 70, 0.74f, true), x + 10, ry + 4.0f, 0.74f, ConfigSlots.empty(i) ? this.pal.muted() : this.pal.text(), true);
            }
            this.text(context, MenuKit.fit(configs.status(i), w - 70, 0.62f, false), x + 10, ry + 15.0f, 0.62f, this.pal.muted(), false);
            if (ConfigSlots.active() == i) {
                float bw = MenuKit.width("ACTIVE", 0.62f, true) + 10.0f;
                this.rect(context, (float)(x + w - 8) - bw, ry + 7.0f, bw, 12.0f, 6, accent);
                this.text(context, "ACTIVE", (float)(x + w - 3) - bw, ry + 9.5f, 0.62f, -15069650, true);
            }
            this.hit(Kind.CONFIG_SLOT, x, ry, w, 26.0f, null, i);
            if (!over) continue;
            this.hover("cslot" + i, "Click to select this config. Double click to rename it.");
        }
        float cy = sy + 150.0f + 4.0f;
        float importW = 56.0f;
        float fw = (float)w - importW - 4.0f;
        this.well(context, x, Math.round(cy), Math.round(fw), 22, 7, this.typingCode ? 1.0f : 0.0f, this.opacity);
        String typed = this.codeText.length() > 60 ? this.codeText.length() + " characters pasted" : MenuKit.tail(this.codeText, fw - 16.0f, 0.74f, false);
        this.text(context, (String)(this.codeText.isEmpty() ? MenuKit.fit("Paste a config or leave empty for clipboard", fw - 14.0f, 0.74f, false) : typed), x + 7, cy + 7.0f, 0.74f, this.codeText.isEmpty() ? this.pal.muted() : this.pal.text(), false);
        if (this.typingCode) {
            float blink = (float)(0.5 + 0.5 * Math.cos((double)System.currentTimeMillis() / 530.0 * Math.PI));
            float cx = (float)(x + 8) + (this.codeText.isEmpty() ? 0.0f : MenuKit.width(typed, 0.74f, false));
            this.rect(context, cx, cy + 5.0f, 1.0f, 11.0f, 0, MenuKit.withAlpha(this.pal.text(), Math.round(255.0f * blink)));
        }
        this.hit(Kind.CONFIG_CODE, x, cy, fw, 22.0f, null, 0);
        this.configButton(context, "Import", (float)x + fw + 4.0f, cy, importW, 22.0f, 5, mx, my);
        float by = cy + 28.0f;
        float bw = ((float)w - 16.0f) / 5.0f;
        String[] labels = new String[]{"Save", "Activate", "Export", this.renaming ? "Done" : "Rename", "Delete"};
        for (int i = 0; i < labels.length; ++i) {
            this.configButton(context, labels[i], (float)x + (float)i * (bw + 4.0f), by, bw, 22.0f, i, mx, my);
        }
        float ty = by + 30.0f;
        this.drawSetting(context, configs.shareName, x, ty, w, mx, my);
        ty += 26.0f;
        for (String line : MenuKit.wrap(CONFIG_NOTE, w, 0.62f, false)) {
            this.text(context, line, x, ty, 0.62f, this.pal.faint(), false);
            ty += 9.0f;
        }
        return ty + 8.0f;
    }

    private void configButton(class_332 context, String label, float bx, float by, float bw, float bh, int action, int mx, int my) {
        float click;
        boolean over = (float)mx >= bx && (float)mx < bx + bw && (float)my >= by && (float)my < by + bh;
        float hv = this.anim("caction", action, over, 12.0f);
        Configs configs = (Configs)this.module;
        boolean danger = action == 4 && configs.deleteArmed();
        boolean primary = action == 1;
        int accent = this.accent();
        int fill = danger ? -7721158 : (primary ? accent : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv));
        this.rect(context, bx, by, bw, bh, 7, fill);
        if ((danger || primary) && hv > 0.01f) {
            this.rect(context, bx, by, bw, bh, 7, MenuKit.withAlpha(0xFFFFFF, Math.round(36.0f * hv)));
        }
        if (!danger && !primary) {
            this.outline(context, bx, by, bw, bh, 7, this.pal.line());
        }
        if ((click = this.press("caction" + action)) > 0.0f) {
            this.rect(context, bx, by, bw, bh, 7, MenuKit.withAlpha(0xFFFFFF, Math.round(50.0f * click)));
        }
        String shown = MenuKit.fit(danger ? "Sure?" : label, bw - 6.0f, 0.62f, true);
        MenuKit.centered(context, shown, bx + bw / 2.0f, by + (bh - 6.0f) / 2.0f, 0.62f, this.c(primary ? -15069650 : this.pal.text()), true);
        this.hit(Kind.CONFIG_ACTION, bx, by, bw, bh, null, action);
        if (over) {
            this.hover("caction" + action, Configs.TIPS[action]);
        }
    }

    private void startRename() {
        this.renaming = true;
        this.renameBuffer = ConfigSlots.name(((Configs)this.module).slot());
        this.typingCode = false;
    }

    private void commitRename() {
        this.renaming = false;
        Module module = this.module;
        if (module instanceof Configs) {
            Configs configs = (Configs)module;
            configs.renameSlot(this.renameBuffer);
        }
    }

    private void configAction(int action) {
        Module module = this.module;
        if (!(module instanceof Configs)) {
            return;
        }
        Configs configs = (Configs)module;
        if (action == 3) {
            if (this.renaming) {
                this.commitRename();
            } else {
                this.startRename();
            }
            return;
        }
        if (action == 5) {
            configs.importText(this.codeText);
            this.codeText = "";
            this.typingCode = false;
        } else {
            configs.run(action);
        }
        SoberClient.CONFIG.save();
    }

    void commit() {
        if (this.editing != null) {
            this.commitEdit();
        }
        if (this.renaming) {
            this.commitRename();
        }
        this.binding = false;
        this.keyBinding = null;
        this.typingCode = false;
        this.dragging = null;
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

    boolean click(double mx, double my, int button, boolean doubled) {
        Hit hit;
        Hit hit2 = hit = this.contains(mx, my) ? this.hitAt(mx, my) : null;
        if (this.editing != null && (hit == null || hit.target() != this.editing)) {
            this.commitEdit();
        }
        if (this.binding && (hit == null || hit.kind() != Kind.BIND)) {
            this.binding = false;
        }
        if (this.keyBinding != null && (hit == null || hit.target() != this.keyBinding)) {
            this.keyBinding = null;
        }
        if (this.renaming && (hit == null || hit.kind() != Kind.CONFIG_ACTION || hit.index() != 3)) {
            this.commitRename();
        }
        if (hit == null || hit.kind() != Kind.CONFIG_CODE) {
            this.typingCode = false;
        }
        if (!this.contains(mx, my)) {
            return false;
        }
        if (hit == null) {
            return true;
        }
        switch (hit.kind().ordinal()) {
            case 0: {
                this.clickSetting(hit, mx, button);
                break;
            }
            case 1: {
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
            case 2: {
                MenuKit.selectPreset((Module)hit.target(), hit.index());
                UiSounds.click();
                SoberClient.CONFIG.save();
                break;
            }
            case 3: {
                this.pressedAt.put("status" + hit.index(), System.nanoTime());
                UiSounds.click();
                MenuKit.openLink((String)hit.target());
                break;
            }
            case 4: {
                this.binding = !this.binding;
                UiSounds.click();
                break;
            }
            case 5: 
            case 6: 
            case 7: {
                this.dragging = hit;
                this.updateDrag(hit, mx, my);
                break;
            }
            case 8: {
                ((Configs)this.module).select(hit.index());
                this.pressedAt.put("cslot" + hit.index(), System.nanoTime());
                UiSounds.click();
                if (doubled) {
                    this.startRename();
                }
                SoberClient.CONFIG.save();
                break;
            }
            case 9: {
                this.pressedAt.put("caction" + hit.index(), System.nanoTime());
                UiSounds.click();
                this.configAction(hit.index());
                break;
            }
            case 10: {
                this.typingCode = true;
            }
        }
        return true;
    }

    private void clickSetting(Hit hit, double mx, int button) {
        Setting setting = (Setting)hit.target();
        this.pressedAt.put(setting, System.nanoTime());
        if(setting instanceof net.albiz.client.port.soberui.setting.ItemListSetting items){this.commit();this.client.method_1507(new ItemPickerScreen(this.host,items));return;}
        if (setting instanceof net.albiz.client.port.soberui.AlbizModuleView.LegacyEditorSetting complex) {
            if(complex.original instanceof net.albiz.client.module.setting.ActionSetting action){action.run();return;}
            if(this.module instanceof net.albiz.client.port.soberui.AlbizModuleView view){this.commit();this.client.method_1507(new net.albiz.client.gui.LegacySettingsScreen(this.host,view.target));return;}
        }
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
                this.client.method_1507((class_437)new BlockPickerScreen(this.host, blocks));
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
            float trackX = hit.x() + 4.0f;
            float trackW = hit.w() - 8.0f;
            double progress = Math.max(0.0, Math.min(1.0, (mx - (double)trackX) / (double)trackW));
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

    boolean drag(double mx, double my) {
        if (this.dragging == null) {
            return false;
        }
        this.updateDrag(this.dragging, mx, my);
        return true;
    }

    boolean release() {
        if (this.dragging == null) {
            return false;
        }
        this.dragging = null;
        SoberClient.CONFIG.save();
        return true;
    }

    boolean scroll(double mx, double my, double vertical, boolean ctrl, boolean shift) {
        Object object;
        if (!this.contains(mx, my) || this.module == null) {
            return false;
        }
        int step = vertical > 0.0 ? 1 : -1;
        Hit hit = this.hitAt(mx, my);
        if (ctrl && hit != null && (object = hit.target()) instanceof IntSetting) {
            IntSetting integer = (IntSetting)object;
            integer.set((Integer)integer.get() + step * (shift ? 10 : 1));
            return true;
        }
        this.scrollTarget = Math.max(0.0f, Math.min(Math.max(0.0f, this.content - this.view), this.scrollTarget - (float)step * 34.0f));
        return true;
    }

    boolean key(class_11908 input) {
        int key = input.comp_4795();
        if (this.binding && this.module != null) {
            this.module.setKey(key == 256 || key == 259 || key == 261 ? -1 : key);
            UiSounds.keybind(this.module.getKey() >= 0);
            this.binding = false;
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
                    String pasted = this.renameBuffer + this.client.field_1774.method_1460().replace("\n", "").replace("\r", "");
                    this.renameBuffer = pasted.length() > 32 ? pasted.substring(0, 32) : pasted;
                    break;
                }
            }
            return true;
        }
        if (this.typingCode) {
            switch (key) {
                case 257: 
                case 335: {
                    this.configAction(5);
                    break;
                }
                case 256: {
                    this.typingCode = false;
                    break;
                }
                case 259: {
                    if (this.codeText.isEmpty()) break;
                    this.codeText = this.codeText.substring(0, this.codeText.length() - 1);
                    break;
                }
                case 86: {
                    if (!input.method_75377()) break;
                    String pasted = this.codeText + this.client.field_1774.method_1460().replace("\r", "").replace('\n', ' ').replace('\t', ' ');
                    this.codeText = pasted.length() > 200000 ? pasted.substring(0, 200000) : pasted;
                    break;
                }
            }
            return true;
        }
        if (this.editing != null) {
            switch (key) {
                case 256: 
                case 257: 
                case 335: {
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
                    this.editBuffer = this.editBuffer + this.client.field_1774.method_1460().replace("\n", "").replace("\r", "");
                    break;
                }
            }
            return true;
        }
        return false;
    }

    boolean typed(class_11905 input) {
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
        if (this.typingCode) {
            if (this.codeText.length() < 200000) {
                this.codeText = this.codeText + input.method_74226();
            }
            return true;
        }
        if (this.editing != null) {
            String typed = input.method_74226();
            if (this.editing instanceof ColorSetting) {
                if (this.editBuffer.length() < 9 && typed.matches("[#0-9a-fA-FxX]")) {
                    this.editBuffer = this.editBuffer + typed.toUpperCase(Locale.ROOT);
                }
            } else if (this.editBuffer.length() < 100) {
                this.editBuffer = this.editBuffer + typed;
            }
            return true;
        }
        return this.binding || this.keyBinding != null;
    }

    record Palette(boolean dark, int text, int muted, int faint, int soft, int softHover, int line, int lineHover, int track, int hover, int knob, int toggleOff, int toggleOn, boolean glow, int accent) {
    }

    private record Key(String tag, Object target) {
    }

    private record Hit(Kind kind, float x, float y, float w, float h, Object target, int index) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
        }
    }

    private static enum Kind {
        SETTING,
        HEX,
        PRESET,
        STATUS,
        BIND,
        WHEEL,
        BRIGHTNESS,
        ALPHA,
        CONFIG_SLOT,
        CONFIG_ACTION,
        CONFIG_CODE;

    }
}

