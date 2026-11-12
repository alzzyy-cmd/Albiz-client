package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.ExactImage;
import net.albiz.client.port.soberui.gui.IslandScene;
import net.albiz.client.port.soberui.gui.MenuAudio;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.albiz.client.port.soberui.gui.SdfText;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.modules.MainMenu;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.joml.Matrix3x2fStack;

public class MenuScreen
extends class_437 {
    private static final float SKIP = 2.7f;
    private static final ExactImage WORDMARK = new ExactImage("textures/brand/wordmark.png");
    private static final int PANEL = -484893920;
    private static final int CARD = -1087623114;
    private static final int CARD_HOVER = -700825785;
    private static final int DANGER_HOVER = -699781325;
    private static final int TEXT = -986380;
    private static final int MUTED = -3880752;
    private static final int ACCENT = -2762274;
    private static final int BRAND_GLOW = 6284270;
    private static boolean introPlayed;
    private static boolean vanilla;
    private final List<Entry> entries = new ArrayList<Entry>();
    private final float[] hover = new float[8];
    private final int[][] bounds = new int[8][4];
    private float linkHover;
    private int[] linkBounds = new int[4];
    private long start = -1L;
    private float offset;
    private boolean introSound;
    private boolean revealSound;
    private long lastFrame = System.nanoTime();
    private float delta;

    public MenuScreen() {
        super((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Albız")));
    }

    public static void resumeCustom(){vanilla=false;}

    public static boolean enabled() {
        if (vanilla) {
            return false;
        }
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        return menu != null && menu.isActive();
    }

    private MainMenu menu() {
        return SoberClient.MODULES.get(MainMenu.class);
    }

    private boolean island() {
        MainMenu menu = this.menu();
        return menu != null && menu.style.get() == MainMenu.Style.Island;
    }

    protected void method_25426() {
        this.entries.clear();
        this.entries.add(new Entry("Singleplayer", () -> this.field_22787.method_1507((class_437)new class_526((class_437)this)), false));
        this.entries.add(new Entry("Multiplayer", () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500((class_437)this) : new class_4749((class_437)this))), false));
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            this.entries.add(new Entry("Mods", this::openMods, false));
        }
        this.entries.add(new Entry("Options", () -> this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690)), false));
        this.entries.add(new Entry("Quit Game", () -> this.field_22787.method_1592(), true));
    }

    private void openMods() {
        try {
            Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(new Object[]{this}));
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            SoberClient.LOG.error("Could not open the mod list", (Throwable)e);
            this.openVanilla();
        }
    }

    private void openVanilla() {
        vanilla = true;
        this.field_22787.method_1507((class_437)new class_442());
    }

    public boolean method_25422() {
        return false;
    }

    public boolean method_25421() {
        return false;
    }

    private float time() {
        if (this.field_22787.method_18506() != null) {
            this.start = -1L;
            return this.offset;
        }
        if (this.start < 0L) {
            this.start = System.nanoTime();
            MainMenu menu = this.menu();
            boolean intro = !introPlayed && menu != null && menu.intro.get() != false;
            introPlayed = true;
            this.offset = intro ? 0.0f : 2.7f;
            this.revealSound = !intro;
            this.introSound = this.revealSound;
            MenuAudio.menuShown();
        }
        return this.offset + (float)(System.nanoTime() - this.start) / 1.0E9f;
    }

    private void skip() {
        float t = this.time();
        if (t < 2.7f) {
            this.offset = 2.7f;
            this.start = System.nanoTime();
            this.revealSound = true;
            this.introSound = true;
        }
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float phase(float t, float from, float length) {
        return MenuScreen.clamp((t - from) / length);
    }

    private static float easeOut(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    private static float easeInOut(float t) {
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    private static float easeSoft(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f * f * f;
    }

    private static int alpha(int color, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;
    }

    private static int scaled(int color, float alpha) {
        return MenuScreen.alpha(color, (float)(color >>> 24) / 255.0f * alpha);
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

    private static void text(class_332 context, String value, float x, float y, float scale, int color) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale, scale);
        SdfText.draw(context, value, 0, 0, color);
        matrices.popMatrix();
    }

    private static float width(String value, float scale) {
        return (float)SdfText.width(value) * scale;
    }

    private static void brand(class_332 context, float cx, float cy, float size, float alpha) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(cx - size / 2.0f, cy - size / 2.0f);
        matrices.scale(size / 64.0f, size / 64.0f);
        context.method_25293(class_10799.field_56883, MenuKit.BRAND, 0, 0, 0.0f, 0.0f, 64, 64, 384, 384, 384, 384, MenuScreen.alpha(0xFFFFFF, alpha));
        matrices.popMatrix();
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        try {
            float t = this.time();
            if (this.island()) {
                IslandScene.background(context, this.field_22789, this.field_22790, t, mouseX, mouseY, this.delta);
            } else {
                ScreenStyle.backdrop(context, this.field_22789, this.field_22790);
                context.method_25294(0, 0, this.field_22789, this.field_22790, 1376258573);
            }
            float fade = MenuScreen.easeInOut(MenuScreen.phase(t, 0.15f, 1.3f));
            if (fade < 1.0f) {
                context.method_25294(0, 0, this.field_22789, this.field_22790, MenuScreen.alpha(329224, 1.0f - fade));
            }
        }
        catch (RuntimeException e) {
            this.fail(e);
        }
    }

    private void fail(RuntimeException e) {
        SoberClient.LOG.error("Custom menu failed, using the vanilla menu", (Throwable)e);
        this.openVanilla();
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        try {
            this.drawMenu(context, mouseX, mouseY);
        }
        catch (RuntimeException e) {
            this.fail(e);
        }
    }

    private void drawMenu(class_332 context, int mouseX, int mouseY) {
        float appear;
        long now = System.nanoTime();
        this.delta = Math.min(0.05f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float t = this.time();
        if (this.field_22787.method_18506() != null) {
            return;
        }
        if (!this.introSound && t >= 0.2f) {
            this.introSound = true;
            UiSounds.intro();
        }
        if (!this.revealSound && t >= 0.85f) {
            this.revealSound = true;
            UiSounds.reveal();
        }
        if (this.island()) {
            this.drawIsland(context, mouseX, mouseY, t);
            return;
        }
        int cx = this.field_22789 / 2;
        boolean compact = this.field_22790 < 330;
        int panelW = 250;
        int buttonW = 204;
        int buttonH = compact ? 22 : 26;
        int gap = compact ? 5 : 7;
        float logoSize = compact ? 34.0f : 46.0f;
        float titleScale = compact ? 2.2f : 2.8f;
        float titleH = 8.0f * titleScale;
        int pad = compact ? 14 : 20;
        int buttonsH = this.entries.size() * (buttonH + gap) - gap;
        int panelH = Math.round((float)pad + logoSize + 10.0f + titleH + 9.0f + 8.0f + (float)(compact ? 14 : 20) + (float)buttonsH + (float)pad);
        int panelX = cx - panelW / 2;
        int panelY = Math.max(6, (this.field_22790 - panelH) / 2 - 8);
        float move = MenuScreen.easeInOut(MenuScreen.phase(t, 1.55f, 0.75f));
        float groupH = logoSize + 10.0f + titleH + 9.0f + 8.0f;
        float introTop = (float)this.field_22790 / 2.0f - groupH / 2.0f;
        float finalTop = panelY + pad;
        float top = introTop + (finalTop - introTop) * move;
        float panel = MenuScreen.easeOut(MenuScreen.phase(t, 1.75f, 0.6f));
        if (panel > 0.01f) {
            float s = 0.965f + 0.035f * panel;
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)cx, (float)panelY + (float)panelH / 2.0f);
            matrices.scale(s, s);
            matrices.translate((float)(-cx), -((float)panelY + (float)panelH / 2.0f));
            Smooth.rect(context, panelX - 1, panelY + 3, panelW + 2, panelH + 2, 16, MenuScreen.alpha(0, 0.22f * panel));
            Smooth.rect(context, panelX, panelY, panelW, panelH, 15, MenuScreen.scaled(-484893920, panel));
            Smooth.sheen(context, panelX, panelY, panelW, panelH, 15, MenuScreen.alpha(0xFFFFFF, 0.05f * panel));
            Smooth.outline(context, panelX, panelY, panelW, panelH, 15, MenuScreen.alpha(0xFFFFFF, 0.06f * panel));
            matrices.popMatrix();
        }
        float lcy = top + logoSize / 2.0f;
        float wave = MenuScreen.phase(t, 0.3f, 1.2f);
        if (wave > 0.0f && wave < 1.0f) {
            int radius = Math.round(MenuScreen.easeOut(wave) * (float)Math.max(this.field_22789, this.field_22790) * 0.5f);
            Smooth.wave(context, cx, Math.round(lcy), radius, MenuScreen.alpha(-2762274, (1.0f - wave) * 0.5f));
        }
        if ((appear = MenuScreen.phase(t, 0.25f, 0.7f)) > 0.0f) {
            double seconds = (double)System.currentTimeMillis() / 1000.0;
            float pulse = (float)(0.5 + 0.5 * Math.sin(seconds * 1.3));
            float flash = 1.0f - Math.abs(MenuScreen.phase(t, 0.55f, 0.6f) * 2.0f - 1.0f);
            Smooth.glow(context, cx, Math.round(lcy), Math.round(logoSize * (1.1f + 0.1f * pulse + flash * 0.6f)), MenuScreen.alpha(6284270, appear * (0.16f + 0.06f * pulse + flash * 0.25f)));
            float scale = 0.82f + 0.18f * MenuScreen.easeSoft(appear);
            MenuScreen.brand(context, cx, lcy, logoSize * scale, MenuScreen.easeSoft(appear));
        }
        float titleY = top + logoSize + 10.0f;
        float wordH = titleH + 17.0f;
        float reveal = MenuScreen.easeSoft(MenuScreen.phase(t, 0.85f, 0.8f));
        if (reveal > 0.01f) {
            float wordW = wordH / WORDMARK.aspect();
            float grow = 0.93f + 0.07f * reveal;
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)cx, titleY + wordH / 2.0f + (1.0f - reveal) * 8.0f);
            matrices.scale(grow, grow);
            WORDMARK.draw(context, -wordW / 2.0f, -wordH / 2.0f, wordW, MenuScreen.alpha(0xFFFFFF, reveal));
            matrices.popMatrix();
        }
        int by = Math.round(finalTop + groupH + (float)(compact ? 14 : 20));
        int bx = cx - buttonW / 2;
        for (int i = 0; i < this.entries.size(); ++i) {
            Entry entry = this.entries.get(i);
            float p = MenuScreen.easeOut(MenuScreen.phase(t, 2.05f + (float)i * 0.08f, 0.45f));
            int y = by + Math.round((1.0f - p) * 14.0f);
            this.bounds[i][0] = bx;
            this.bounds[i][1] = by;
            this.bounds[i][2] = buttonW;
            this.bounds[i][3] = buttonH;
            boolean hovered = p >= 1.0f && mouseX >= bx && mouseX < bx + buttonW && mouseY >= by && mouseY < by + buttonH;
            float before = this.hover[i];
            int n = i;
            this.hover[n] = this.hover[n] + ((hovered ? 1.0f : 0.0f) - this.hover[i]) * (1.0f - (float)Math.exp(-16.0f * this.delta));
            if (hovered && before < 0.05f) {
                UiSounds.hover();
            }
            float h = this.hover[i];
            if (p > 0.01f) {
                int fill = MenuScreen.lerp(-1087623114, entry.danger() ? -699781325 : -700825785, h);
                Smooth.rect(context, bx, y, buttonW, buttonH, 8, MenuScreen.scaled(fill, p));
                if (h > 0.01f) {
                    Smooth.outline(context, bx, y, buttonW, buttonH, 8, MenuScreen.alpha(entry.danger() ? 15238026 : 0xFFFFFF, 0.14f * h * p));
                }
                String label = net.albiz.client.i18n.AlbizI18n.display(entry.label());
                float lw = MenuScreen.width(label, 1.0f);
                int color = entry.danger() ? MenuScreen.lerp(-3880752, -1005394, h) : MenuScreen.lerp(-3091496, -986380, h);
                MenuScreen.text(context, label, (float)cx - lw / 2.0f + h, (float)y + (float)(buttonH - 8) / 2.0f, 1.0f, MenuScreen.alpha(color, p));
            }
            by += buttonH + gap;
        }
        this.footer(context, mouseX, mouseY, t);
        this.hint(context, t, cx);
        Notifications.render(context, true);
    }

    private void drawIsland(class_332 context, int mouseX, int mouseY, float t) {
        float shade = MenuScreen.easeOut(MenuScreen.phase(t, 0.4f, 1.2f));
        context.method_25296(0, this.field_22790 - Math.max(40, this.field_22790 / 6), this.field_22789, this.field_22790, 397862, MenuScreen.alpha(397862, 0.42f * shade));
        IslandScene.title(context, this.field_22789, this.field_22790, t);
        int n = this.entries.size();
        int gap = 6;
        int buttonH = this.field_22790 < 300 ? 20 : 24;
        int columns = n;
        int buttonW = Math.min(104, (this.field_22789 - 40 - gap * (columns - 1)) / columns);
        if (buttonW < 66) {
            columns = (n + 1) / 2;
            buttonW = Math.min(104, (this.field_22789 - 40 - gap * (columns - 1)) / columns);
        }
        int rows = (n + columns - 1) / columns;
        int blockH = rows * buttonH + (rows - 1) * gap;
        int top = Math.round(Math.min((float)this.field_22790 * 0.835f, (float)(this.field_22790 - 36 - blockH)));
        int cx = this.field_22789 / 2;
        for (int i = 0; i < n; ++i) {
            Entry entry = this.entries.get(i);
            int row = i / columns;
            int col = i % columns;
            int inRow = Math.min(columns, n - row * columns);
            int rowW = inRow * buttonW + (inRow - 1) * gap;
            int bx = cx - rowW / 2 + col * (buttonW + gap);
            int by = top + row * (buttonH + gap);
            float p = MenuScreen.easeOut(MenuScreen.phase(t, 2.05f + (float)i * 0.08f, 0.45f));
            int y = by + Math.round((1.0f - p) * 14.0f);
            this.bounds[i][0] = bx;
            this.bounds[i][1] = by;
            this.bounds[i][2] = buttonW;
            this.bounds[i][3] = buttonH;
            boolean hovered = p >= 1.0f && mouseX >= bx && mouseX < bx + buttonW && mouseY >= by && mouseY < by + buttonH;
            float before = this.hover[i];
            int n2 = i;
            this.hover[n2] = this.hover[n2] + ((hovered ? 1.0f : 0.0f) - this.hover[i]) * (1.0f - (float)Math.exp(-16.0f * this.delta));
            if (hovered && before < 0.05f) {
                UiSounds.hover();
            }
            float h = this.hover[i];
            if (!(p > 0.01f)) continue;
            int fill = MenuScreen.lerp(-1087623114, entry.danger() ? -699781325 : -700825785, h);
            Smooth.rect(context, bx - 1, y + 2, buttonW + 2, buttonH + 2, 9, MenuScreen.alpha(0, 0.2f * p));
            Smooth.rect(context, bx, y, buttonW, buttonH, 8, MenuScreen.scaled(fill, p));
            Smooth.sheen(context, bx, y, buttonW, buttonH, 8, MenuScreen.alpha(0xFFFFFF, 0.05f * p));
            if (h > 0.01f) {
                Smooth.outline(context, bx, y, buttonW, buttonH, 8, MenuScreen.alpha(entry.danger() ? 15238026 : 0xFFFFFF, 0.18f * h * p));
            }
            String label = net.albiz.client.i18n.AlbizI18n.display(entry.label());
            float lw = MenuScreen.width(label, 1.0f);
            int color = entry.danger() ? MenuScreen.lerp(-3880752, -1005394, h) : MenuScreen.lerp(-3091496, -986380, h);
            MenuScreen.text(context, label, (float)bx + (float)buttonW / 2.0f - lw / 2.0f, (float)y + (float)(buttonH - 8) / 2.0f, 1.0f, MenuScreen.alpha(color, p));
        }
        this.footer(context, mouseX, mouseY, t);
        this.hint(context, t, cx);
        Notifications.render(context, true);
    }

    private void footer(class_332 context, int mouseX, int mouseY, float t) {
        float footer = MenuScreen.easeOut(MenuScreen.phase(t, 2.5f, 0.5f));
        if (footer > 0.01f) {
            String version = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
            MenuScreen.brand(context, 15.0f, this.field_22790 - 25, 14.0f, footer);
            MenuScreen.text(context, net.albiz.client.i18n.AlbizI18n.display("Albız"), 26.0f, this.field_22790 - 29, 1.0f, MenuScreen.alpha(-986380, footer));
            MenuScreen.text(context, "Minecraft " + version + "  /  " + FabricLoader.getInstance().getAllMods().size() + " mods", 26.0f, this.field_22790 - 17, 0.85f, MenuScreen.alpha(-3880752, footer));
            String link = net.albiz.client.i18n.AlbizI18n.display("Vanilla Menu");
            int lw = Math.round(MenuScreen.width(link, 0.9f));
            int lx = this.field_22789 - lw - 10;
            int ly = this.field_22790 - 17;
            this.linkBounds = new int[]{lx - 4, ly - 4, lw + 8, 15};
            boolean over = mouseX >= lx - 4 && mouseX < lx + lw + 4 && mouseY >= ly - 4 && mouseY < ly + 11;
            this.linkHover += ((over ? 1.0f : 0.0f) - this.linkHover) * (1.0f - (float)Math.exp(-16.0f * this.delta));
            MenuScreen.text(context, link, lx, ly, 0.9f, MenuScreen.alpha(MenuScreen.lerp(-3880752, -986380, this.linkHover), footer));
            int line = Math.round((float)lw * this.linkHover);
            if (line > 0) {
                context.method_25294(lx, ly + 9, lx + line, ly + 10, MenuScreen.alpha(-2762274, footer * 0.8f));
            }
        }
    }

    private void hint(class_332 context, float t, int cx) {
        if (t < 2.4f && t > 0.6f) {
            String hint = net.albiz.client.i18n.AlbizI18n.display("Click to skip");
            MenuScreen.text(context, hint, (float)cx - MenuScreen.width(hint, 0.85f) / 2.0f, this.field_22790 - 16, 0.85f, MenuScreen.alpha(-3880752, 0.55f * MenuScreen.phase(t, 0.6f, 0.5f)));
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        float t = this.time();
        if (t < 2.7f) {
            this.skip();
            return true;
        }
        if (t < 2.75f) {
            return true;
        }
        double mx = click.comp_4798();
        double my = click.comp_4799();
        for (int i = 0; i < this.entries.size(); ++i) {
            int[] b = this.bounds[i];
            if (!(mx >= (double)b[0]) || !(mx < (double)(b[0] + b[2])) || !(my >= (double)b[1]) || !(my < (double)(b[1] + b[3]))) continue;
            UiSounds.click();
            this.entries.get(i).action().run();
            return true;
        }
        if (mx >= (double)this.linkBounds[0] && mx < (double)(this.linkBounds[0] + this.linkBounds[2]) && my >= (double)this.linkBounds[1] && my < (double)(this.linkBounds[1] + this.linkBounds[3])) {
            UiSounds.click();
            this.openVanilla();
            return true;
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25404(class_11908 input) {
        if (this.time() < 2.7f) {
            this.skip();
            return true;
        }
        return super.method_25404(input);
    }

    private record Entry(String label, Runnable action, boolean danger) {
    }
}

