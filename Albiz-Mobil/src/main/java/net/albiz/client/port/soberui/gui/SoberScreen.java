// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.BrandTextures;
import net.albiz.client.port.soberui.gui.CleanBackdrop;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.ExactImage;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.modules.MainMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.joml.Matrix3x2fStack;

public final class SoberScreen
extends class_437 {
    static final String BOLD = "montserrat-bold.ttf";
    private static final float REFERENCE_H = 323.0f;
    private static final float BUTTON_W = 100.0f;
    private static final float BUTTON_H = 14.0f;
    private static final float STEP = 17.0f;
    private static final float LOGO_W = 96.0f;
    private static final float LOGO_GAP = 58.0f;
    private static final float BLOCK_H = 140.5f;
    private static final ExactImage LOGO = new ExactImage("textures/brand/lockup.png");
    private static boolean shownOnce;
    private final List<Button> buttons = new ArrayList<Button>();
    private final Map<Button, Float> hover = new HashMap<Button, Float>();
    private final long openedAt = System.nanoTime();
    private long lastFrame = System.nanoTime();
    private float delta;
    private float fade = 1.0f;
    private boolean firstShow;
    private float s;
    private float logoCenterY;

    public SoberScreen() {
        super((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Albız")));
    }

    public boolean method_25422() {
        return false;
    }

    protected void method_25426() {
        this.buttons.clear();
        this.hover.clear();
        this.s = Math.clamp((float)this.field_22790 / 323.0f, 0.8f, 2.4f);
        boolean mods = FabricLoader.getInstance().isModLoaded("modmenu");
        int w = Math.round(100.0f * this.s);
        int h = Math.round(14.0f * this.s);
        int step = Math.round(17.0f * this.s);
        float blockTop = (float)this.field_22790 / 2.0f - 74.75f * this.s;
        this.logoCenterY = blockTop + 16.9f * this.s;
        int x = Math.round((float)(this.field_22789 - w) / 2.0f);
        int y = Math.round(blockTop + 58.0f * this.s);
        this.buttons.add(new Button("Singleplayer", Icon.PERSON, x, y, w, h, () -> this.field_22787.method_1507((class_437)new class_526((class_437)this))));
        this.buttons.add(new Button("Multiplayer", Icon.GLOBE, x, y += step, w, h, () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500((class_437)this) : new class_4749((class_437)this)))));
        y += step;
        if (mods) {
            this.buttons.add(new Button("Mods", Icon.CUBE, x, y, w, h, this::openMods));
            y += step;
        }
        this.buttons.add(new Button("Options", Icon.GEAR, x, y, w, h, () -> this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690))));
        this.buttons.add(new Button("Quit", Icon.CLOSE, x, y += step, w, h, () -> this.field_22787.method_1592()));
        this.buttons.add(new Button("Accounts", Icon.PERSON, 8, this.field_22790-h-8, w, h, () -> this.field_22787.method_1507(new net.albiz.client.gui.MenuUtilityScreen(this,false))));
        this.buttons.add(new Button("Credits", Icon.GEAR, this.field_22789-w-8, this.field_22790-h-8, w, h, () -> this.field_22787.method_1507(new net.albiz.client.gui.MenuUtilityScreen(this,true))));
        if (!shownOnce) {
            shownOnce = true;
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            this.firstShow = menu == null || menu.intro.get() != false;
        }
    }

    private void openMods() {
        try {
            Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(new Object[]{this}));
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            SoberClient.LOG.error("Could not open the mod list", (Throwable)e);
        }
    }

    static void image(class_332 context, BrandTextures.Sprite sprite, float x, float y, float w, float h, int color) {
        if (sprite == null) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(w / (float)sprite.width(), h / (float)sprite.height());
        context.method_25293(class_10799.field_56883, sprite.id(), 0, 0, 0.0f, 0.0f, sprite.width(), sprite.height(), sprite.width(), sprite.height(), sprite.width(), sprite.height(), color);
        matrices.popMatrix();
    }

    public static void paintSpace(class_332 context, int width, int height, float opacity) {
        net.albiz.client.gui.mainmenu.DuskBackdrop.draw(context,width,height);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        SoberScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0f);
    }

    static void glassBackdrop(class_332 context, class_310 client) {
        if (!Backdrop.requestMenu(client)) {
            return;
        }
        try {
            context.method_71048();
            context.method_71278();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
    }

    static void glassPill(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
        if (Theme.liquidGlass()) {
            Smooth.liquid(context, x, y, w, h, r, opacity, true);
            Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(806620200, 351135743, hover), opacity));
            return;
        }
        Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(384690175, 653125631, hover), opacity));
        Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(0x1CFFFFFF, 0x34FFFFFF, hover), opacity));
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        long now = System.nanoTime();
        this.delta = Math.min(0.06f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float since = (float)(now - this.openedAt) / 1.0E9f;
        this.fade = this.firstShow ? MenuKit.quint(Math.min(1.0f, since / 1.1f)) : MenuKit.quint(Math.min(1.0f, since / 0.25f));
        super.method_25394(context, mouseX, mouseY, deltaTicks);
        float head = this.appear(since, 0.0f);
        float logoW = Math.min(this.field_22789 * 0.65f, 148.0f * this.s);
        float logoH = logoW * LOGO.aspect();
        LOGO.draw(context, ((float)this.field_22789 - logoW) / 2.0f, this.logoCenterY - logoH / 2.0f + (1.0f - head) * 5.0f * this.s, logoW, MenuKit.alpha(-1, head));
        for (int i = 0; i < this.buttons.size(); ++i) {
            this.drawButton(context, this.buttons.get(i), this.appear(since, 0.1f + (float)i * 0.05f), mouseX, mouseY);
        }
    }

    private float appear(float since, float delay) {
        float start = this.firstShow ? 0.35f : 0.05f;
        return this.fade * MenuKit.quint(Math.max(0.0f, Math.min(1.0f, (since - start - delay) / 0.45f)));
    }

    private void drawButton(class_332 context, Button button, float opacity, int mouseX, int mouseY) {
        if (opacity <= 0.002f) {
            return;
        }
        boolean over = button.contains(mouseX, mouseY);
        float hv = MenuKit.approach(this.hover.getOrDefault(button, Float.valueOf(0.0f)).floatValue(), over ? 1.0f : 0.0f, 14.0f, this.delta);
        this.hover.put(button, Float.valueOf(hv));
        int x = button.x();
        int y = button.y() + Math.round((1.0f - opacity) * 6.0f * this.s);
        int w = button.w();
        int h = button.h();
        Smooth.rect(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-267119594, -199484385, hv), opacity));
        Smooth.outline(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-14408664, -12829629, hv), opacity));
        int tint = MenuKit.alpha(MenuKit.lerp(-2302752, -1, hv), opacity);
        float em = 4.7f * this.s;
        float iconSize = 4.4f * this.s;
        float gap = 3.2f * this.s;
        float textW = CrispText.width(net.albiz.client.i18n.AlbizI18n.display(button.label()), BOLD, em, 0.0f);
        float left = (float)x + ((float)w - iconSize - gap - textW) / 2.0f;
        SoberScreen.icon(context, button.icon(), left + iconSize / 2.0f, (float)y + (float)h / 2.0f, Math.round(iconSize), tint);
        CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display(button.label()), BOLD, left + iconSize + gap, (float)y + (float)h / 2.0f + em * 0.36f, em, 0.0f, tint);
    }

    private static void icon(class_332 context, Icon icon, float cx, float cy, int size, int color) {
        switch (icon.ordinal()) {
            case 1: {
                SoberScreen.globe(context, cx, cy, (float)size * 0.5f, color);
                break;
            }
            case 0: {
                Icons.draw(context, Icons.Icon.PERSON, Math.round(cx - (float)size / 2.0f), Math.round(cy - (float)size / 2.0f), size, color);
                break;
            }
            case 2: {
                Icons.draw(context, Icons.Icon.CUBE, Math.round(cx - (float)size / 2.0f), Math.round(cy - (float)size / 2.0f), size, color);
                break;
            }
            case 3: {
                Icons.draw(context, Icons.Icon.GEAR, Math.round(cx - (float)size / 2.0f), Math.round(cy - (float)size / 2.0f), size, color);
                break;
            }
            case 4: {
                Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx - (float)size / 2.0f), Math.round(cy - (float)size / 2.0f), size, color);
            }
        }
    }

    private static void globe(class_332 context, float cx, float cy, float radius, int color) {
        int n = 33;
        float line = Math.max(0.4f, radius * 0.2f);
        for (int ring = 0; ring < 2; ++ring) {
            float rx = ring == 0 ? radius : radius * 0.44f;
            float[] xs = new float[n];
            float[] ys = new float[n];
            float[] ws = new float[n];
            int[] cs = new int[n];
            for (int i = 0; i < n; ++i) {
                double a = Math.PI * 2 * (double)i / (double)(n - 1);
                xs[i] = cx + (float)Math.cos(a) * rx;
                ys[i] = cy + (float)Math.sin(a) * radius;
                ws[i] = line;
                cs[i] = color;
            }
            Smooth.polyline(context, xs, ys, ws, cs, n);
        }
        Smooth.polyline(context, new float[]{cx - radius, cx + radius}, new float[]{cy, cy}, new float[]{line, line}, new int[]{color, color}, 2);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (super.method_25402(click, doubled)) {
            return true;
        }
        if (this.fade < 0.5f) {
            return true;
        }
        for (Button button : this.buttons) {
            if (!button.contains(click.comp_4798(), click.comp_4799())) continue;
            UiSounds.click();
            button.action().run();
            return true;
        }
        return false;
    }

    private record Button(String label, Icon icon, int x, int y, int w, int h, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
        }
    }

    private static enum Icon {
        PERSON,
        GLOBE,
        CUBE,
        GEAR,
        CLOSE;

    }
}

