// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.setting.BlockListSetting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import net.minecraft.class_7923;
import org.joml.Matrix3x2fStack;

public class BlockPickerScreen
extends class_437 {
    private static final int CELL = 22;
    private static final int PAD = 12;
    private static final int HEADER = 54;
    private static final int FOOTER = 54;
    private static final int SWATCH = 14;
    private static final int[] PALETTE = new int[]{15022416, 16747069, 16765514, 10215773, 4183707, 4186342, 5219327, 5793266, 10975743, 16740277, 0xF4F4F8, 10133678, 11565642, 0x30333D};
    private static List<class_2248> allBlocks;
    private final class_437 parent;
    private final BlockListSetting setting;
    private final List<class_2248> shown = new ArrayList<class_2248>();
    private String search = "";
    private boolean onlySelected;
    private class_2248 focused;
    private float scroll;
    private float shownScroll;
    private long opened;
    private long lastFrame = this.opened = System.nanoTime();
    private int px;
    private int py;
    private int pw;
    private int ph;
    private int columns;
    private int rows;

    public BlockPickerScreen(class_437 parent, BlockListSetting setting) {
        super((class_2561)class_2561.method_43470((String)"Select blocks"));
        this.parent = parent;
        this.setting = setting;
        if (allBlocks == null) {
            allBlocks = new ArrayList<class_2248>();
            for (class_2248 block : class_7923.field_41175) {
                if (block.method_8389() == class_1802.field_8162) continue;
                allBlocks.add(block);
            }
            allBlocks.sort(Comparator.comparing(b -> b.method_9518().getString()));
        }
        this.filter();
    }

    public boolean method_25421() {
        return false;
    }

    private void filter() {
        this.shown.clear();
        String query = this.search.toLowerCase(Locale.ROOT);
        for (class_2248 block : allBlocks) {
            if(!this.setting.allows(block))continue;
            if (this.onlySelected && !this.setting.contains(block)) continue;
            if (!query.isEmpty()) {
                String name = block.method_9518().getString().toLowerCase(Locale.ROOT);
                String id = class_7923.field_41175.method_10221(block).method_12832();
                if (!name.contains(query) && !id.contains(query.replace(' ', '_'))) continue;
            }
            this.shown.add(block);
        }
        this.scroll = Math.min(this.scroll, this.maxScroll());
    }

    private void layout() {
        this.pw = Math.min(this.field_22789 - 40, 376);
        this.ph = Math.min(this.field_22790 - 30, 420);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
        this.columns = Math.max(1, (this.pw - 24) / 22);
        this.rows = Math.max(1, (this.ph - 54 - 54) / 22);
    }

    private float maxScroll() {
        int totalRows = (this.shown.size() + Math.max(1, this.columns) - 1) / Math.max(1, this.columns);
        return Math.max(0, (totalRows - this.rows) * 22);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        context.method_25296(0, 0, this.field_22789, this.field_22790, 0x50000000, -1879048192);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        float max;
        int first;
        boolean filterHover;
        this.layout();
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float t = Math.min(1.0f, (float)(now - this.opened) / 2.6E8f);
        float ease = 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t) * (1.0f - t);
        this.shownScroll = MenuKit.approach(this.shownScroll, this.scroll, 16.0f, delta);
        int accent = Theme.accent();
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        float s = 0.96f + 0.04f * ease;
        matrices.translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f + (1.0f - ease) * 6.0f);
        matrices.scale(s, s);
        matrices.translate((float)(-this.field_22789) / 2.0f, (float)(-this.field_22790) / 2.0f);
        int fade = Math.round(255.0f * ease);
        for (int i = 0; i < 4; ++i) {
            int spread = 7 - i * 2;
            Smooth.rect(context, this.px - spread, this.py - spread + 3, this.pw + spread * 2, this.ph + spread * 2, 12 + spread, (new int[]{12, 20, 30, 42})[i] * fade / 255 << 24);
        }
        Smooth.rect(context, this.px, this.py, this.pw, this.ph, 12, BlockPickerScreen.withAlpha(Theme.panelSolid(242), fade));
        Smooth.outline(context, this.px, this.py, this.pw, this.ph, 12, BlockPickerScreen.withAlpha(0x14FFFFFF, fade));
        context.method_51439(this.field_22793, Theme.bold(Theme.label(this.setting.name)), this.px + 12, this.py + 10, BlockPickerScreen.withAlpha(Theme.text(), fade), false);
        String count = ((Set)this.setting.get()).size() + " selected";
        context.method_51439(this.field_22793, Theme.text(Theme.label(count)), this.px + this.pw - 12 - this.field_22793.method_27525((class_5348)Theme.text(Theme.label(count))), this.py + 10, BlockPickerScreen.withAlpha(BlockPickerScreen.lerp(-6383190, accent, 0.6f), fade), false);
        int sx = this.px + 12;
        int sy = this.py + 26;
        int sw = this.pw - 24 - 74;
        int sh = 18;
        Smooth.rect(context, sx, sy, sw, sh, 9, BlockPickerScreen.withAlpha(0x1AFFFFFF, fade));
        Smooth.outline(context, sx, sy, sw, sh, 9, BlockPickerScreen.withAlpha(this.search.isEmpty() ? 0x12FFFFFF : accent & 0xFFFFFF | 0xA0000000, fade));
        class_2561 searchText = this.search.isEmpty() ? Theme.text(Theme.label("Search blocks...")) : class_2561.method_43470((String)this.search).method_10862(Theme.style());
        context.method_51439(this.field_22793, searchText, sx + 9, sy + 5, BlockPickerScreen.withAlpha(this.search.isEmpty() ? -8618872 : Theme.text(), fade), false);
        if (!this.search.isEmpty() && System.currentTimeMillis() / 530L % 2L == 0L) {
            int cx = sx + 10 + this.field_22793.method_27525((class_5348)searchText);
            context.method_25294(cx, sy + 4, cx + 1, sy + 13, BlockPickerScreen.withAlpha(Theme.text(), fade));
        }
        int fx = sx + sw + 6;
        int fw = 68;
        boolean bl = filterHover = mouseX >= fx && mouseX < fx + fw && mouseY >= sy && mouseY < sy + sh;
        Smooth.rect(context, fx, sy, fw, sh, 9, BlockPickerScreen.withAlpha(this.onlySelected ? accent & 0xFFFFFF | 0x50000000 : (filterHover ? 0x24FFFFFF : 0x1AFFFFFF), fade));
        class_2561 filterText = Theme.text(Theme.label("Selected"));
        context.method_51439(this.field_22793, filterText, fx + (fw - this.field_22793.method_27525((class_5348)filterText)) / 2, sy + 5, BlockPickerScreen.withAlpha(this.onlySelected ? -1 : -4934466, fade), false);
        int gx = this.px + 12 + (this.pw - 24 - this.columns * 22) / 2;
        int gy = this.py + 54;
        int gh = this.rows * 22;
        context.method_44379(this.px + 2, gy - 2, this.px + this.pw - 2, gy + gh + 2);
        class_2248 hovered = null;
        for (int i = first = Math.max(0, (int)(this.shownScroll / 22.0f) * this.columns); i < this.shown.size(); ++i) {
            boolean hover;
            int col = i % this.columns;
            int row = i / this.columns;
            int cx = gx + col * 22;
            int cy = gy + row * 22 - Math.round(this.shownScroll);
            if (cy > gy + gh) break;
            class_2248 block = this.shown.get(i);
            boolean selected = this.setting.contains(block);
            boolean bl2 = hover = mouseX >= cx && mouseX < cx + 22 && mouseY >= cy && mouseY < cy + 22 && mouseY >= gy && mouseY < gy + gh;
            if (hover) {
                hovered = block;
            }
            if (selected) {
                Smooth.rect(context, cx + 1, cy + 1, 20, 20, 6, BlockPickerScreen.withAlpha(accent & 0xFFFFFF | 0x55000000, fade));
                Smooth.outline(context, cx + 1, cy + 1, 20, 20, 6, BlockPickerScreen.withAlpha(accent & 0xFFFFFF | 0xC0000000, fade));
            } else if (hover) {
                Smooth.rect(context, cx + 1, cy + 1, 20, 20, 6, BlockPickerScreen.withAlpha(0x1CFFFFFF, fade));
            }
            if (fade > 150) {
                context.method_51427(new class_1799((class_1935)block.method_8389()), cx + 3, cy + 3);
            }
            Integer custom = this.setting.color(block);
            if (selected && custom != null) {
                Smooth.rect(context, cx + 22 - 8, cy + 22 - 8, 6, 6, 3, BlockPickerScreen.withAlpha(0xFF000000 | custom, fade));
                Smooth.outline(context, cx + 22 - 8, cy + 22 - 8, 6, 6, 3, BlockPickerScreen.withAlpha(-1056964609, fade));
            }
            if (block != this.focused) continue;
            Smooth.outline(context, cx, cy, 22, 22, 7, BlockPickerScreen.withAlpha(-1, fade));
        }
        context.method_44380();
        if (this.shown.isEmpty()) {
            class_2561 empty = Theme.text(Theme.label(this.onlySelected ? "No blocks selected" : "Nothing found"));
            context.method_51439(this.field_22793, empty, this.px + (this.pw - this.field_22793.method_27525((class_5348)empty)) / 2, gy + gh / 2 - 4, BlockPickerScreen.withAlpha(-8618872, fade), false);
        }
        if ((max = this.maxScroll()) > 0.0f) {
            int trackH = gh;
            int thumbH = Math.max(18, Math.round((float)trackH * ((float)gh / ((float)gh + max))));
            int thumbY = gy + Math.round((float)(trackH - thumbH) * (this.shownScroll / max));
            Smooth.rect(context, this.px + this.pw - 7, thumbY, 3, thumbH, 1, BlockPickerScreen.withAlpha(0x40FFFFFF, fade));
        }
        int ry = this.py + this.ph - 54 + 4;
        if (this.focused != null && this.setting.contains(this.focused)) {
            boolean autoHover;
            Object name = this.focused.method_9518().getString();
            int room = this.paletteX(-1) - this.px - 12 - 6;
            while (((String)name).length() > 1 && this.field_22793.method_27525((class_5348)Theme.text((String)name)) > room) {
                name = ((String)name).substring(0, ((String)name).length() - 2) + "\u2026";
            }
            context.method_51439(this.field_22793, Theme.text((String)name), this.px + 12, ry + 4, BlockPickerScreen.withAlpha(Theme.text(), fade), false);
            Integer current = this.setting.color(this.focused);
            int ax = this.paletteX(-1);
            int aw = 30;
            boolean bl3 = autoHover = mouseX >= ax && mouseX < ax + aw && mouseY >= ry && mouseY < ry + 14 + 2;
            Smooth.rect(context, ax, ry, aw, 16, 7, BlockPickerScreen.withAlpha(current == null ? accent & 0xFFFFFF | 0x70000000 : (autoHover ? 0x30FFFFFF : 0x1AFFFFFF), fade));
            class_2561 auto = Theme.text(Theme.label("Auto"));
            context.method_51439(this.field_22793, auto, ax + (aw - this.field_22793.method_27525((class_5348)auto)) / 2, ry + 4, BlockPickerScreen.withAlpha(-1, fade), false);
            for (int i = 0; i < PALETTE.length; ++i) {
                int sx2 = this.paletteX(i);
                boolean active = current != null && current == PALETTE[i];
                boolean hover = mouseX >= sx2 && mouseX < sx2 + 14 && mouseY >= ry + 1 && mouseY < ry + 1 + 14;
                Smooth.rect(context, sx2, ry + 1, 14, 14, 7, BlockPickerScreen.withAlpha(0xFF000000 | PALETTE[i], fade));
                if (!active && !hover) continue;
                Smooth.outline(context, sx2 - 2, ry - 1, 18, 18, 9, BlockPickerScreen.withAlpha(active ? -1 : -1862270977, fade));
            }
        } else {
            class_2561 hint = Theme.text(Theme.label("Right click a block to give it its own color"));
            context.method_51439(this.field_22793, hint, this.px + 12, ry + 4, BlockPickerScreen.withAlpha(-8618872, fade), false);
        }
        int fy = this.py + this.ph - 24;
        class_2561 info = hovered != null ? Theme.text(hovered.method_9518().getString()) : Theme.text(Theme.label("Click to select"));
        context.method_51439(this.field_22793, info, this.px + 12, fy + 5, BlockPickerScreen.withAlpha(hovered != null ? Theme.text() : -8618872, fade), false);
        int dw = 54;
        int dx = this.px + this.pw - 12 - dw;
        boolean doneHover = mouseX >= dx && mouseX < dx + dw && mouseY >= fy && mouseY < fy + 18;
        Smooth.rect(context, dx, fy, dw, 18, 9, BlockPickerScreen.withAlpha(accent & 0xFFFFFF | (doneHover ? -805306368 : -1879048192), fade));
        class_2561 done = Theme.bold(Theme.label("Done"));
        context.method_51439(this.field_22793, done, dx + (dw - this.field_22793.method_27525((class_5348)done)) / 2, fy + 5, BlockPickerScreen.withAlpha(-1, fade), false);
        int cw = 50;
        int cx2 = dx - cw - 6;
        boolean clearHover = mouseX >= cx2 && mouseX < cx2 + cw && mouseY >= fy && mouseY < fy + 18;
        Smooth.rect(context, cx2, fy, cw, 18, 9, BlockPickerScreen.withAlpha(clearHover ? 0x30FFFFFF : 0x1AFFFFFF, fade));
        class_2561 clear = Theme.text(Theme.label("Clear"));
        context.method_51439(this.field_22793, clear, cx2 + (cw - this.field_22793.method_27525((class_5348)clear)) / 2, fy + 5, BlockPickerScreen.withAlpha(-1775634, fade), false);
        matrices.popMatrix();
    }

    private int paletteX(int index) {
        int end = this.px + this.pw - 12;
        int start = end - PALETTE.length * 18 - 34;
        return index < 0 ? start : start + 34 + index * 18;
    }

    private static int withAlpha(int color, int alpha) {
        int a = (color >>> 24) * Math.max(0, Math.min(255, alpha)) / 255;
        return a << 24 | color & 0xFFFFFF;
    }

    private static int lerp(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return 0xFF000000 | (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        this.layout();
        double mx = click.comp_4798();
        double my = click.comp_4799();
        int sy = this.py + 26;
        int sw = this.pw - 24 - 74;
        int fx = this.px + 12 + sw + 6;
        if (mx >= (double)fx && mx < (double)(fx + 68) && my >= (double)sy && my < (double)(sy + 18)) {
            this.onlySelected = !this.onlySelected;
            this.scroll = 0.0f;
            this.filter();
            UiSounds.click();
            return true;
        }
        int fy = this.py + this.ph - 24;
        int dx = this.px + this.pw - 12 - 54;
        int cx = dx - 56;
        if (mx >= (double)dx && mx < (double)(dx + 54) && my >= (double)fy && my < (double)(fy + 18)) {
            this.method_25419();
            return true;
        }
        if (mx >= (double)cx && mx < (double)(cx + 50) && my >= (double)fy && my < (double)(fy + 18)) {
            this.setting.clear();
            this.focused = null;
            this.filter();
            UiSounds.click();
            return true;
        }
        int ry = this.py + this.ph - 54 + 4;
        if (this.focused != null && this.setting.contains(this.focused) && my >= (double)(ry - 1) && my < (double)(ry + 14 + 3)) {
            int ax = this.paletteX(-1);
            if (mx >= (double)ax && mx < (double)(ax + 30)) {
                this.setting.setColor(this.focused, null);
                UiSounds.click();
                return true;
            }
            for (int i = 0; i < PALETTE.length; ++i) {
                int sx2 = this.paletteX(i);
                if (!(mx >= (double)(sx2 - 1)) || !(mx < (double)(sx2 + 14 + 1))) continue;
                this.setting.setColor(this.focused, PALETTE[i]);
                UiSounds.click();
                return true;
            }
        }
        int gx = this.px + 12 + (this.pw - 24 - this.columns * 22) / 2;
        int gy = this.py + 54;
        int gh = this.rows * 22;
        if (my >= (double)gy && my < (double)(gy + gh) && mx >= (double)gx && mx < (double)(gx + this.columns * 22)) {
            int row = (int)((my - (double)gy + (double)this.shownScroll) / 22.0);
            int col = (int)((mx - (double)gx) / 22.0);
            int index = row * this.columns + col;
            if (index >= 0 && index < this.shown.size()) {
                class_2248 block = this.shown.get(index);
                if (click.method_74245() == 1) {
                    if (!this.setting.contains(block)) {
                        this.setting.toggle(block);
                    }
                    this.focused = block;
                    UiSounds.click();
                } else {
                    this.setting.toggle(block);
                    this.focused = this.setting.contains(block) ? block : null;
                    UiSounds.toggle(this.setting.contains(block));
                    if (this.onlySelected) {
                        this.filter();
                    }
                }
            }
            return true;
        }
        if (mx < (double)this.px || mx > (double)(this.px + this.pw) || my < (double)this.py || my > (double)(this.py + this.ph)) {
            this.method_25419();
            return true;
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll = Math.max(0.0f, Math.min(this.maxScroll(), this.scroll - (float)verticalAmount * 22.0f * 2.0f));
        return true;
    }

    public boolean method_25404(class_11908 input) {
        int key = input.comp_4795();
        if (key == 256) {
            if (!this.search.isEmpty()) {
                this.search = "";
                this.filter();
            } else {
                this.method_25419();
            }
            return true;
        }
        if (key == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.scroll = 0.0f;
            this.filter();
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        if (!input.method_74227() || this.search.length() >= 32) {
            return true;
        }
        this.search = this.search + input.method_74226();
        this.scroll = 0.0f;
        this.filter();
        return true;
    }

    public void method_25419() {
        SoberClient.CONFIG.save();
        this.field_22787.method_1507(this.parent);
    }
}

