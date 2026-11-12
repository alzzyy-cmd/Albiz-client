// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Avatars;
import net.albiz.client.port.soberui.gui.BrandTextures;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuAudio;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.modules.MainMenu;
import net.albiz.client.port.soberui.modules.SkinChange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4189;
import net.minecraft.class_426;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_8219;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class VanillaPlusScreen
extends class_437 {
    private static final String TAG_FONT = "geist-medium.ttf";
    private static final String COPYRIGHT = "Copyright Mojang AB. Do not distribute!";
    private static final float TAG_EM = 6.2f;
    private static final float TAG_TRACK = 0.22f;
    private static final String[] KINDS = new String[]{"orbit"};
    private static boolean shownOnce;
    private static List<Prop> props;
    private final List<Button> buttons = new ArrayList<Button>();
    private final Map<Button, Float> hover = new HashMap<Button, Float>();
    private final long openedAt = System.nanoTime();
    private long lastFrame = System.nanoTime();
    private float delta;
    private float fade = 1.0f;
    private boolean firstShow;
    private int buttonTop;
    private Supplier<class_8685> skin;

    public VanillaPlusScreen() {
        super((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Albız")));
    }

    public boolean method_25422() {
        return false;
    }

    protected void method_25426() {
        int top;
        this.buttons.clear();
        this.hover.clear();
        boolean mods = FabricLoader.getInstance().isModLoaded("modmenu");
        int bw = 200;
        int bh = 20;
        int step = 24;
        int rows = mods ? 4 : 3;
        this.buttonTop = top = Math.max(this.field_22790 / 4 + 48, Math.min(this.field_22790 / 2 - 8, this.field_22790 - (rows * step + 12 + bh) - 18));
        int x = this.field_22789 / 2 - bw / 2;
        int y = top;
        this.buttons.add(new Button("Singleplayer", Icon.NONE, x, y, bw, bh, () -> this.field_22787.method_1507((class_437)new class_526((class_437)this))));
        this.buttons.add(new Button("Multiplayer", Icon.NONE, x, y += step, bw, bh, () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500((class_437)this) : new class_4749((class_437)this)))));
        this.buttons.add(new Button("Minecraft Realms", Icon.NONE, x, y += step, bw, bh, () -> this.field_22787.method_1507((class_437)new class_4325((class_437)this))));
        y += step;
        if (mods) {
            this.buttons.add(new Button("Mods", Icon.NONE, x, y, bw, bh, this::openMods));
            y += step;
        }
        this.buttons.add(new Button("Options...", Icon.NONE, x, y += 12, 98, bh, () -> this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690))));
        this.buttons.add(new Button("Quit Game", Icon.NONE, x + 102, y, 98, bh, () -> this.field_22787.method_1592()));
        this.buttons.add(new Button("", Icon.GLOBE, x - 24, y, 20, bh, () -> this.field_22787.method_1507((class_437)new class_426((class_437)this, this.field_22787.field_1690, this.field_22787.method_1526()))));
        this.buttons.add(new Button("", Icon.PERSON, x + bw + 4, y, 20, bh, () -> this.field_22787.method_1507((class_437)new class_4189((class_437)this, this.field_22787.field_1690))));
        try {
            this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
        }
        catch (RuntimeException e) {
            this.skin = null;
        }
        if (props == null) {
            props = VanillaPlusScreen.scatter();
        }
        if (!shownOnce) {
            shownOnce = true;
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            this.firstShow = menu == null || menu.intro.get() != false;
            MenuAudio.menuShown();
        }
    }

    private static List<Prop> scatter() {
        ArrayList<Prop> list = new ArrayList<Prop>();
        Random random = new Random(21L);
        int tries = 0;
        while (list.size() < 32 && tries++ < 4000) {
            int depth = list.size() < 13 ? 0 : (list.size() < 24 ? 1 : 2);
            float x = random.nextFloat();
            float y = random.nextFloat();
            if (depth > 0 && Math.abs(x - 0.5f) < (depth == 2 ? 0.25f : 0.19f) && y > 0.04f && y < 0.93f || depth > 0 && x < 0.3f && y > 0.44f) continue;
            String kind = KINDS[random.nextInt(KINDS.length)];
            float size = (depth == 0 ? 0.045f : (depth == 1 ? 0.07f : 0.1f)) * (0.8f + random.nextFloat() * 0.5f);
            list.add(new Prop(kind, x, y, size, random.nextFloat() * 360.0f - 180.0f, (random.nextFloat() - 0.5f) * 14.0f, random.nextFloat() * 6.283f, depth));
        }
        return list;
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

    private int c(int color) {
        return MenuKit.alpha(color, this.fade);
    }

    private static String version(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    private static void image(class_332 context, BrandTextures.Sprite sprite, float x, float y, float w, float h, int color) {
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

    private static float pixels(class_332 context) {
        return Smooth.pixelsPerUnit(context);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        SoberScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0f);
        this.drawProps(context, mouseX, mouseY);
        context.method_25296(0, Math.round((float)this.field_22790 * 0.62f), this.field_22789, this.field_22790, 722710, 2013988630);
    }

    private void drawProps(class_332 context, int mouseX, int mouseY) {
        if (props == null) {
            return;
        }
        float time = (float)(System.nanoTime() - this.openedAt) / 1.0E9f + 40.0f;
        float ppu = VanillaPlusScreen.pixels(context);
        float scale = Math.min((float)this.field_22789, (float)this.field_22790 * 1.78f);
        float mx = ((float)mouseX - (float)this.field_22789 / 2.0f) / Math.max(1.0f, (float)this.field_22789);
        float my = ((float)mouseY - (float)this.field_22790 / 2.0f) / Math.max(1.0f, (float)this.field_22790);
        int[] alpha = new int[]{44, 70, 102};
        float[] blur = new float[]{3.2f, 1.6f, 1.0f};
        float[] parallax = new float[]{4.0f, 9.0f, 16.0f};
        for (Prop prop : props) {
            float size = prop.size() * scale;
            float x = prop.x() * (float)this.field_22789 + (float)Math.cos(time * 0.21f + prop.phase()) * size * 0.18f - mx * parallax[prop.depth()];
            float y = prop.y() * (float)this.field_22790 + (float)Math.sin(time * 0.33f + prop.phase()) * size * 0.22f - my * parallax[prop.depth()];
            float angle = prop.angle() + prop.spin() * time * 0.6f + (float)Math.sin(time * 0.4f + prop.phase()) * 9.0f;
            BrandTextures.Sprite sprite = BrandTextures.fit(prop.kind(), size * ppu / blur[prop.depth()]);
            if (sprite == null) continue;
            float w = size;
            float h = size * (float)sprite.height() / (float)sprite.width();
            Matrix3x2fStack matrices = context.method_51448();
            if (prop.depth() == 2) {
                matrices.pushMatrix();
                matrices.translate(x + size * 0.04f, y + size * 0.07f);
                matrices.rotate((float)Math.toRadians(angle));
                VanillaPlusScreen.image(context, BrandTextures.fit(prop.kind(), size * ppu / 3.0f), -w / 2.0f, -h / 2.0f, w, h, this.c(0x38000000));
                matrices.popMatrix();
            }
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.rotate((float)Math.toRadians(angle));
            VanillaPlusScreen.image(context, sprite, -w / 2.0f, -h / 2.0f, w, h, this.c(alpha[prop.depth()] << 24 | 0xC4B0FF));
            matrices.popMatrix();
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        long now = System.nanoTime();
        this.delta = Math.min(0.06f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float since = (float)(now - this.openedAt) / 1.0E9f;
        this.fade = this.firstShow ? MenuKit.quint(Math.min(1.0f, since / 1.1f)) : MenuKit.quint(Math.min(1.0f, since / 0.25f));
        super.method_25394(context, mouseX, mouseY, deltaTicks);
        this.drawTitle(context);
        for (Button button : this.buttons) {
            this.drawButton(context, button, mouseX, mouseY);
        }
        this.drawAccount(context);
        this.drawFooter(context, mouseX, mouseY);
    }

    private void drawTitle(class_332 context) {
        float space = (float)this.buttonTop - 10.0f;
        float logoH = space * 0.3f;
        float wordH = space * 0.36f;
        float ppu = VanillaPlusScreen.pixels(context);
        BrandTextures.Sprite logoFull = BrandTextures.get("orbit_glow");
        if (logoFull != null) {
            float logoW = logoH * (float)logoFull.width() / (float)logoFull.height();
            VanillaPlusScreen.image(context, BrandTextures.fit("orbit_glow", logoW * ppu), (float)this.field_22789 / 2.0f - logoW / 2.0f, 8.0f, logoW, logoH, this.c(-1));
        }
        BrandTextures.Sprite wordFull = BrandTextures.get("wordmark");
        float wordW = 0.0f;
        float wordTop = 8.0f + logoH + 2.0f;
        if (wordFull != null) {
            wordW = wordH * (float)wordFull.width() / (float)wordFull.height();
            if (wordW > (float)this.field_22789 - 20.0f) {
                wordW = (float)this.field_22789 - 20.0f;
                wordH = wordW * (float)wordFull.height() / (float)wordFull.width();
            }
            VanillaPlusScreen.image(context, BrandTextures.fit("wordmark", wordW * ppu), (float)this.field_22789 / 2.0f - wordW / 2.0f, wordTop, wordW, wordH, this.c(-1));
        }
        String tag = "ALBIZ CLIENT  \u00b7  " + class_155.method_16673().comp_4025();
        float sw = CrispText.width(tag, TAG_FONT, 6.2f, 0.22f);
        float ty = Math.min(wordTop + wordH + 6.0f, (float)this.buttonTop - 4.0f);
        CrispText.draw(context, tag, TAG_FONT, (float)this.field_22789 / 2.0f - sw / 2.0f, ty, 6.2f, 0.22f, this.c(-926370561));
    }

    private void drawButton(class_332 context, Button button, int mouseX, int mouseY) {
        boolean over = button.contains(mouseX, mouseY);
        float hv = MenuKit.approach(this.hover.getOrDefault(button, Float.valueOf(0.0f)).floatValue(), over ? 1.0f : 0.0f, 18.0f, this.delta);
        this.hover.put(button, Float.valueOf(hv));
        int x = button.x();
        int y = button.y();
        int w = button.w();
        int h = button.h();
        Smooth.rect(context, x, y, w, h, 5, this.c(MenuKit.lerp(-433448366, -7643914, hv)));
        Smooth.rect(context, x, y + h / 2, w, h - h / 2, 5, this.c(MenuKit.lerp(1007355670, 1079714230, hv)));
        Smooth.rect(context, x + 2, y + 1, w - 4, 2, 1, this.c(MenuKit.lerp(0x30FFFFFF, 0x70FFFFFF, hv)));
        Smooth.outline(context, x, y, w, h, 5, this.c(MenuKit.lerp(-2136503302, -3885571, hv)));
        int tint = this.c(-1);
        if (button.icon() == Icon.NONE) {
            MenuKit.centered(context, net.albiz.client.i18n.AlbizI18n.display(button.label()), (float)x + (float)w / 2.0f, (float)y + (float)h / 2.0f - 3.4f, 0.8f, tint, false);
        } else if (button.icon() == Icon.GLOBE) {
            VanillaPlusScreen.globe(context, (float)x + 10.0f, (float)y + 10.0f, tint);
        } else {
            Icons.draw(context, Icons.Icon.PERSON, x + 4, y + 4, 12, tint);
        }
    }

    private static void globe(class_332 context, float cx, float cy, int color) {
        int n = 25;
        float[] xs = new float[n];
        float[] ys = new float[n];
        float[] ws = new float[n];
        int[] cs = new int[n];
        for (int ring = 0; ring < 2; ++ring) {
            float rx = ring == 0 ? 5.0f : 2.2f;
            for (int i = 0; i < n; ++i) {
                double a = Math.PI * 2 * (double)i / (double)(n - 1);
                xs[i] = cx + (float)Math.cos(a) * rx;
                ys[i] = cy + (float)Math.sin(a) * 5.0f;
                ws[i] = 0.9f;
                cs[i] = color;
            }
            Smooth.polyline(context, (float[])xs.clone(), (float[])ys.clone(), (float[])ws.clone(), (int[])cs.clone(), n);
        }
        Smooth.polyline(context, new float[]{cx - 5.0f, cx + 5.0f}, new float[]{cy, cy}, new float[]{0.9f, 0.9f}, new int[]{color, color}, 2);
    }

    private void drawAccount(class_332 context) {
        if (this.field_22787.method_1548() == null) {
            return;
        }
        String name = this.field_22787.method_1548().method_1676();
        float nw = MenuKit.width(name, 0.7f, false);
        int chipW = Math.round(21.0f + nw + 8.0f);
        int chipH = 16;
        int chipX = this.field_22789 - 6 - chipW;
        int chipY = 6;
        Smooth.rect(context, chipX, chipY, chipW, chipH, 8, this.c(-1273491664));
        Smooth.outline(context, chipX, chipY, chipW, chipH, 8, this.c(0x3CFFFFFF));
        try {
            class_8685 textures = this.skin == null ? null : this.skin.get();
            SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
            if (textures != null && skins != null && skins.isActive()) {
                textures = skins.selfSkin(textures);
            }
            if (textures != null && this.fade > 0.6f) {
                Avatars.draw(context, textures, chipX + 4, chipY + 3, 10);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        Smooth.circle(context, chipX + 13, chipY + 12, 1, this.c(-8396640));
        MenuKit.text(context, name, chipX + 18, (float)chipY + 4.6f, 0.7f, this.c(-1), false);
    }

    private boolean overCopyright(double mx, double my) {
        float w = MenuKit.width(COPYRIGHT, 0.6f, false);
        return mx >= (double)((float)(this.field_22789 - 5) - w) && mx < (double)(this.field_22789 - 3) && my >= (double)(this.field_22790 - 11) && my < (double)this.field_22790;
    }

    private void drawFooter(class_332 context, int mouseX, int mouseY) {
        String left = "Minecraft " + class_155.method_16673().comp_4025() + "  \u00b7  Fabric  \u00b7  Albız Client " + VanillaPlusScreen.version("albiz");
        MenuKit.text(context, left, 5.0f, (float)this.field_22790 - 9.0f, 0.6f, this.c(-922746881), false);
        boolean over = this.overCopyright(mouseX, mouseY);
        float w = MenuKit.width(COPYRIGHT, 0.6f, false);
        MenuKit.text(context, COPYRIGHT, (float)(this.field_22789 - 5) - w, (float)this.field_22790 - 9.0f, 0.6f, this.c(over ? -1 : -922746881), false);
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
        if (this.overCopyright(click.comp_4798(), click.comp_4799())) {
            UiSounds.click();
            this.field_22787.method_1507((class_437)new class_8219((class_437)this));
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
        NONE,
        GLOBE,
        PERSON;

    }

    private record Prop(String kind, float x, float y, float size, float angle, float spin, float phase, int depth) {
    }
}

