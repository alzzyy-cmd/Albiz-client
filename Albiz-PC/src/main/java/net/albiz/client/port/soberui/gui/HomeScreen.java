// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Avatars;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.Logo3D;
import net.albiz.client.port.soberui.gui.MenuAudio;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.ScreenStyle;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.modules.MainMenu;
import net.albiz.client.port.soberui.modules.NameTags;
import net.albiz.client.port.soberui.modules.SkinChange;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_412;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_5348;
import net.minecraft.class_639;
import net.minecraft.class_641;
import net.minecraft.class_642;
import net.minecraft.class_8685;
import net.minecraft.class_8765;
import org.joml.Matrix3x2fStack;

public class HomeScreen
extends class_437 {
    private static final float WIDTH = 860.0f;
    private static final float HEIGHT = 440.0f;
    private static final float SLIDE = 0.6f;
    private static final float LEAVE = 0.22f;
    private static final float HOLD = 3.4f;
    private static final float WINDOW = 4.3f;
    private static final float INTRO = 5.2f;
    private static final int TEXT = -855051;
    private static final int MUTED = -4275510;
    private static final int FAINT = -7367779;
    private static final int CARD = 0x14FFFFFF;
    private static final int CARD_HOVER = 0x22FFFFFF;
    private static final int EDGE = 0x1FFFFFFF;
    private static final float[][] STARS = HomeScreen.stars();
    private static final class_2960 SAKURA = class_2960.method_60655((String)"albiz", (String)"textures/gui/sakura_backdrop.png");
    private static final int SAKURA_W = 2400;
    private static final int SAKURA_H = 1350;
    private static boolean introPlayed;
    private static long firstShown;
    private final Map<String, Float> hover = new HashMap<String, Float>();
    private final List<Tile> tiles = new ArrayList<Tile>();
    private final List<class_642> servers = new ArrayList<class_642>();
    private Supplier<class_8685> baseSkin;
    private class_8765 skinWidget;
    private class_342 nameField;
    private class_342 skinField;
    private String pendingSkin;
    private long skinChangedAt;
    private float scale = 1.0f;
    private float originX;
    private float originY;
    private long lastFrame = System.nanoTime();
    private long openedAt;
    private float delta;
    private float time;
    private float now;
    private float fade = 1.0f;
    private float parallaxX;
    private float parallaxY;
    private String pressedKey;
    private long pressedAt;
    private Runnable leaving;
    private long leaveStart;
    private boolean replay;
    private boolean shown;
    private float introShift;

    public HomeScreen() {
        super((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Albız")));
    }

    protected void method_25426() {
        if (this.openedAt == 0L || this.replay) {
            this.openedAt = System.nanoTime();
            this.replay = false;
            this.hover.clear();
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            this.introShift = introPlayed || menu != null && menu.intro.get() == false ? 0.0f : 5.2f;
            introPlayed = true;
        }
        this.leaving = null;
        this.scale = Math.max(0.45f, Math.min((float)this.field_22789 / 860.0f, (float)this.field_22790 / 440.0f));
        this.originX = Math.round(((float)this.field_22789 - 860.0f * this.scale) / 2.0f);
        this.originY = Math.round(((float)this.field_22790 - 440.0f * this.scale) / 2.0f);
        this.loadServers();
        this.layoutTiles();
        this.baseSkin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
        this.skinWidget = new class_8765(Math.round(130.0f * this.scale), Math.round(170.0f * this.scale), this.field_22787.method_31974(), () -> this.worn(this.baseSkin.get()));
        this.skinWidget.method_48229(this.px(626.0f), this.py(96.0f));
        this.method_37063(this.skinWidget);
        int fieldWidth = Math.round(134.0f * this.scale);
        int inset = Math.round((22.0f * this.scale - 8.0f) / 2.0f);
        this.nameField = new class_342(this.field_22793, this.px(624.0f), this.py(307.0f) + inset, fieldWidth, 10, (class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Name")));
        HomeScreen.styleField(this.nameField);
        this.nameField.method_1880(32);
        this.nameField.method_1852(NameTags.customName());
        this.nameField.method_47404((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Name on your screen")).method_10862(Theme.crispStyle().method_36139(7040888)));
        this.nameField.method_1863(value -> NameTags.setCustomName(value.trim()));
        this.method_37063(this.nameField);
        this.skinField = new class_342(this.field_22793, this.px(624.0f), this.py(343.0f) + inset, fieldWidth, 10, (class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Skin")));
        HomeScreen.styleField(this.skinField);
        this.skinField.method_1880(16);
        this.skinField.method_1890(value -> value.matches("[A-Za-z0-9_]*"));
        SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
        this.skinField.method_1852(skins == null ? "" : skins.selfPlayer());
        this.skinField.method_47404((class_2561)class_2561.method_43470(net.albiz.client.i18n.AlbizI18n.display("Wear a player's skin")).method_10862(Theme.crispStyle().method_36139(7040888)));
        this.skinField.method_1863(value -> {
            this.pendingSkin = value.trim();
            this.skinChangedAt = System.currentTimeMillis();
        });
        this.method_37063(this.skinField);
        this.showWidgets(false);
        if (!this.shown) {
            this.shown = true;
            MenuAudio.menuShown();
        }
    }

    private static void styleField(class_342 field) {
        field.method_1858(false);
        field.method_71503(false);
        field.method_1868(-855051);
        field.method_73210(Theme.fieldFormatter());
    }

    private class_8685 worn(class_8685 original) {
        SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
        return skins != null && skins.isActive() ? skins.selfSkin(original) : original;
    }

    private void loadServers() {
        this.servers.clear();
        try {
            class_641 list = new class_641(this.field_22787);
            list.method_2981();
            for (int i = 0; i < list.method_2984() && this.servers.size() < 3; ++i) {
                this.servers.add(list.method_2982(i));
            }
        }
        catch (RuntimeException e) {
            SoberClient.LOG.warn("Could not read the server list for Quick Join", (Throwable)e);
        }
        if (this.servers.isEmpty()) {
            this.servers.add(new class_642("DonutSMP", "donutsmp.net", class_642.class_8678.field_45611));
        }
    }

    private void layoutTiles() {
        this.tiles.clear();
        this.tiles.add(new Tile("single", 74.0f, 184.0f, 198.0f, 70.0f, 0.1f, () -> this.field_22787.method_1507((class_437)new class_526((class_437)this))));
        this.tiles.add(new Tile("multi", 284.0f, 184.0f, 198.0f, 70.0f, 0.14f, this::openMultiplayer));
        ArrayList<String> small = new ArrayList<String>(List.of("Realms", "Options", "Quit"));
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            small.add(2, "Mods");
        }
        float gap = 7.0f;
        float each = (408.0f - gap * (float)(small.size() - 1)) / (float)small.size();
        for (int i = 0; i < small.size(); ++i) {
            String label;
            Runnable action = switch (label = (String)small.get(i)) {
                case "Realms" -> () -> this.field_22787.method_1507((class_437)new class_4325((class_437)this));
                case "Options" -> () -> this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
                case "Mods" -> this::openMods;
                default -> () -> this.field_22787.method_1592();
            };
            this.tiles.add(new Tile(label, Math.round(74.0f + (float)i * (each + gap)), 266.0f, Math.round(each), 32.0f, 0.18f + (float)i * 0.03f, action));
        }
        float serverEach = (408.0f - gap * 2.0f) / 3.0f;
        for (int i = 0; i < this.servers.size(); ++i) {
            class_642 info = this.servers.get(i);
            this.tiles.add(new Tile("server" + i, Math.round(74.0f + (float)i * (serverEach + gap)), 332.0f, Math.round(serverEach), 38.0f, 0.28f + (float)i * 0.03f, () -> this.join(info)));
        }
    }

    private void openMultiplayer() {
        this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500((class_437)this) : new class_4749((class_437)this)));
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

    private void join(class_642 info) {
        if (!class_639.method_36224((String)info.field_3761)) {
            return;
        }
        class_412.method_36877((class_437)this, (class_310)this.field_22787, (class_639)class_639.method_2950((String)info.field_3761), (class_642)info, (boolean)false, null);
    }

    public boolean method_25422() {
        return false;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25393() {
        super.method_25393();
        if (this.pendingSkin != null && System.currentTimeMillis() - this.skinChangedAt > 700L) {
            this.applySkin();
        }
    }

    private void applySkin() {
        SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
        if (skins != null && this.pendingSkin != null) {
            skins.setSelfPlayer(this.pendingSkin);
        }
        this.pendingSkin = null;
    }

    public void method_25432() {
        this.applySkin();
        SoberClient.CONFIG.save();
    }

    private int px(float x) {
        return Math.round(this.originX + x * this.scale);
    }

    private int py(float y) {
        return Math.round(this.originY + y * this.scale);
    }

    private static int accent() {
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        return 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
    }

    private static int alpha(int color, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;
    }

    private int f(int color) {
        return HomeScreen.alpha(color, (float)(color >>> 24) / 255.0f * this.fade);
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

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float quint(float p) {
        float inverse = 1.0f - HomeScreen.clamp(p);
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    private static float smooth(float p) {
        return (p = HomeScreen.clamp(p)) < 0.5f ? 4.0f * p * p * p : 1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3.0) / 2.0f;
    }

    private float shown(float delay) {
        return HomeScreen.quint((this.time - delay) / 0.6f);
    }

    private float leave() {
        if (this.leaving == null) {
            return 0.0f;
        }
        float p = HomeScreen.clamp((float)(System.nanoTime() - this.leaveStart) / 1.0E9f / 0.22f);
        return p * p * p;
    }

    private float animate(String key, boolean on, float speed) {
        float value = this.hover.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        value += ((on ? 1.0f : 0.0f) - value) * (1.0f - (float)Math.exp(-speed * this.delta));
        float f = on ? 1.0f : 0.0f;
        if (Math.abs(f - value) < 0.002f) {
            value = on ? 1.0f : 0.0f;
        }
        this.hover.put(key, Float.valueOf(value));
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void group(class_332 context, float delay, float dx, float dy, Runnable draw) {
        float out;
        float in = this.shown(delay);
        float visible = in * (1.0f - (out = this.leave()));
        if (visible <= 0.002f) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(dx * (1.0f - in), dy * (1.0f - in) - 8.0f * out);
        this.fade = visible;
        try {
            draw.run();
        }
        finally {
            this.fade = 1.0f;
            matrices.popMatrix();
        }
    }

    private class_2561 styled(String value, float size) {
        return size >= 1.6f ? Theme.large(value) : (size >= 1.1f ? Theme.bold(value) : Theme.text(value));
    }

    private void text(class_332 context, String value, float x, float y, float size, int color) {
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
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(size, size);
            context.method_51439(this.field_22793, this.styled(value, size), 0, 0, this.f(ScreenStyle.readable(color)), Theme.liquidGlass());
            matrices.popMatrix();
            return;
        }
        int shown = this.f(ScreenStyle.readable(color));
        float em = 9.4f * size;
        float baseline = y + 7.6f * size;
        if (Theme.liquidGlass()) {
            CrispText.draw(context, value, file, x + 0.5f, baseline + 0.7f, em, 0.0f, HomeScreen.alpha(0, (float)(shown >>> 24) / 255.0f * 0.3f));
        }
        CrispText.draw(context, value, file, x, baseline, em, 0.0f, shown);
    }

    private boolean glass(class_332 context, int x, int y, int w, int h, int r) {
        return ScreenStyle.glass(context, x, y, w, h, r, this.fade);
    }

    private float textWidth(String value, float size) {
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

    private void rect(class_332 context, int x, int y, int w, int h, int r, int color) {
        Smooth.rect(context, x, y, w, h, r, this.f(color));
    }

    private void outline(class_332 context, int x, int y, int w, int h, int r, int color) {
        Smooth.outline(context, x, y, w, h, r, this.f(color));
    }

    private void logo(class_332 context, float x, float y, float size) {
        Logo3D.draw(context, x + size / 2.0f, y + size / 2.0f, size, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, this.fade, 0.7f);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        if (firstShown == 0L) {
            firstShown = System.nanoTime();
        }
        float intro = HomeScreen.smooth((float)(System.nanoTime() - firstShown) / 1.0E9f / 1.2f);
        HomeScreen.paintBackdrop(context, this.field_22789, this.field_22790, intro, this.parallaxX, this.parallaxY);
        float black = 1.0f - HomeScreen.smooth((this.introElapsed() - 4.3f) / 1.1999998f);
        if (black > 0.002f) {
            context.method_25294(0, 0, this.field_22789, this.field_22790, HomeScreen.alpha(197638, black));
        }
        if (Backdrop.requestMenu(this.field_22787)) {
            try {
                context.method_71048();
                context.method_71278();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    public static void paintBackdrop(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY) {
        HomeScreen.paintBackdrop(context, width, height, intro, parallaxX, parallaxY, 1.0f);
    }

    public static void paintBackdrop(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY, float cover) {
        if (cover <= 0.002f) {
            return;
        }
        intro *= cover;
        if (Theme.liquidGlass()) {
            HomeScreen.paintBright(context, width, height, intro, parallaxX, parallaxY, cover);
            return;
        }
        context.method_25294(0, 0, width, height, HomeScreen.alpha(395018, cover));
        int accent = HomeScreen.accent();
        float clock = (float)System.nanoTime() / 1.0E9f;
        float breathe = 0.5f + 0.5f * (float)Math.sin(clock * 0.5f);
        float glowX = (float)width * 0.2f + (float)Math.sin(clock * 0.15f) * (float)width * 0.04f - parallaxX * 16.0f;
        float glowY = (float)height * 0.33f + (float)Math.cos(clock * 0.12f) * (float)height * 0.05f - parallaxY * 10.0f;
        Smooth.glow(context, Math.round(glowX), Math.round(glowY), Math.round((float)width * 0.46f), HomeScreen.alpha(accent, (0.2f + 0.06f * breathe) * intro));
        float glowX2 = (float)width * 0.8f + (float)Math.cos(clock * 0.13f) * (float)width * 0.04f - parallaxX * 22.0f;
        float glowY2 = (float)height * 0.68f + (float)Math.sin(clock * 0.16f) * (float)height * 0.04f - parallaxY * 14.0f;
        Smooth.glow(context, Math.round(glowX2), Math.round(glowY2), Math.round((float)width * 0.42f), HomeScreen.alpha(accent, (0.15f + 0.05f * (1.0f - breathe)) * intro));
        Smooth.glow(context, Math.round((float)width * 0.62f - parallaxX * 18.0f), Math.round((float)height * 0.2f - parallaxY * 10.0f), Math.round((float)width * 0.3f), HomeScreen.alpha(4059872, (0.06f + 0.02f * breathe) * intro));
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        if (menu != null && !menu.stars.get().booleanValue()) {
            return;
        }
        for (float[] star : STARS) {
            float depth = 0.3f + star[4];
            float sy = (star[1] + clock * 0.004f * depth) % 1.0f * ((float)height + 20.0f) - 10.0f;
            float sx = star[0] * (float)width - parallaxX * 20.0f * depth;
            sy -= parallaxY * 12.0f * depth;
            float twinkle = 0.45f + 0.55f * (0.5f + 0.5f * (float)Math.sin(clock * star[3] * 0.7f + star[2]));
            if (star[4] > 0.86f) {
                Smooth.softDot(context, sx, sy, 3.6f, HomeScreen.alpha(0xFFFFFF, 0.16f * twinkle * intro));
                Smooth.softDot(context, sx, sy, 1.3f, HomeScreen.alpha(0xFFFFFF, 0.7f * twinkle * intro));
                continue;
            }
            Smooth.softDot(context, sx, sy, 1.1f, HomeScreen.alpha(15133165, 0.5f * twinkle * intro));
        }
        HomeScreen.paintParticles(context, width, height, intro, parallaxX, parallaxY);
    }

    public static void paintParticles(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY) {
        if (intro <= 0.01f) {
            return;
        }
        int accent = HomeScreen.accent();
        float clock = (float)System.nanoTime() / 1.0E9f;
        for (int i = 0; i < 22; ++i) {
            float[] star = STARS[i * 5 % STARS.length];
            float depth = 0.5f + star[4];
            float rise = ((star[1] - clock * 0.01f * depth) % 1.0f + 1.0f) % 1.0f;
            float x = star[0] * (float)width + (float)Math.sin(clock * 0.3f + star[2]) * 12.0f - parallaxX * 26.0f * depth;
            float y = rise * ((float)height + 40.0f) - 20.0f - parallaxY * 16.0f * depth;
            float edge = Math.min(1.0f, Math.min(rise, 1.0f - rise) * 6.0f);
            float pulse = 0.5f + 0.5f * (float)Math.sin(clock * star[3] * 0.8f + star[2]);
            float radius = 1.4f + star[4] * 2.8f;
            float a = intro * edge;
            Smooth.softDot(context, x, y, radius * 5.5f, HomeScreen.alpha(accent, (0.07f + 0.06f * pulse) * a));
            Smooth.softDot(context, x, y, radius * 2.0f, HomeScreen.alpha(accent, (0.3f + 0.22f * pulse) * a));
            Smooth.softDot(context, x, y, radius * 0.8f, HomeScreen.alpha(0xFFFFFF, (0.4f + 0.3f * pulse) * a));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void paintBright(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY, float cover) {
        float clock = (float)System.nanoTime() / 1.0E9f;
        int mid = Math.round((float)height * 0.55f);
        context.method_25296(0, 0, width, mid, HomeScreen.alpha(2759758, cover), HomeScreen.alpha(9060222, cover));
        context.method_25296(0, mid, width, height, HomeScreen.alpha(9060222, cover), HomeScreen.alpha(3809349, cover));
        float fit = Math.max((float)width / 2400.0f, (float)height / 1350.0f) * 1.05f;
        int w = (int)Math.ceil(2400.0f * fit);
        int h = (int)Math.ceil(1350.0f * fit);
        float x = (float)(width - w) / 2.0f - parallaxX * 12.0f;
        float y = (float)(height - h) / 2.0f - parallaxY * 8.0f;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        try {
            context.method_25293(class_10799.field_56883, SAKURA, 0, 0, 0.0f, 0.0f, w, h, 2400, 1350, 2400, 1350, HomeScreen.alpha(0xFFFFFF, Math.max(intro, 0.001f) * cover));
        }
        catch (RuntimeException e) {
            SoberClient.LOG.debug("Sakura backdrop unavailable", (Throwable)e);
        }
        finally {
            matrices.popMatrix();
        }
        context.method_25294(0, 0, width, height, HomeScreen.alpha(1181720, 0.32f * cover));
        for (int i = 0; i < 18; ++i) {
            float[] star = STARS[i * 7 % STARS.length];
            float speed = 0.012f + star[4] * 0.012f;
            float fall = ((star[1] + clock * speed) % 1.0f + 1.0f) % 1.0f;
            float px = star[0] * (float)width + (float)Math.sin(clock * (0.4f + star[3] * 0.2f) + star[2]) * 18.0f + fall * (float)width * 0.08f - parallaxX * 22.0f;
            float py = fall * ((float)height + 30.0f) - 15.0f - parallaxY * 14.0f;
            float edge = Math.min(1.0f, Math.min(fall, 1.0f - fall) * 8.0f);
            float size = 1.4f + star[4] * 1.6f;
            Smooth.softDot(context, px, py, size * 2.6f, HomeScreen.alpha(16758736, 0.18f * edge * intro));
            Smooth.softDot(context, px, py, size, HomeScreen.alpha(16766694, 0.85f * edge * intro));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        long nano = System.nanoTime();
        this.delta = Math.min(0.05f, (float)(nano - this.lastFrame) / 1.0E9f);
        this.lastFrame = nano;
        this.time = (float)(nano - this.openedAt) / 1.0E9f - this.introShift;
        this.now = (float)nano / 1.0E9f;
        float targetX = this.field_22789 > 0 ? (float)mouseX / (float)this.field_22789 - 0.5f : 0.0f;
        float targetY = this.field_22790 > 0 ? (float)mouseY / (float)this.field_22790 - 0.5f : 0.0f;
        float follow = 1.0f - (float)Math.exp(-3.0f * this.delta);
        this.parallaxX += (targetX - this.parallaxX) * follow;
        this.parallaxY += (targetY - this.parallaxY) * follow;
        if (this.leaving != null && this.leave() >= 1.0f) {
            Runnable action = this.leaving;
            this.leaving = null;
            this.replay = true;
            action.run();
            return;
        }
        double mx = ((float)mouseX - this.originX) / this.scale;
        double my = ((float)mouseY - this.originY) / this.scale;
        this.surface(context, mouseX, mouseY);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(this.originX, this.originY);
        matrices.scale(this.scale, this.scale);
        try {
            this.introWindow(context);
            this.heroLogo(context);
            this.group(context, 0.0f, 0.0f, -12.0f, () -> this.topBar(context));
            this.group(context, 0.05f, -22.0f, 0.0f, () -> this.title(context));
            for (Tile tile : this.tiles) {
                this.group(context, tile.delay(), -22.0f, 0.0f, () -> this.drawTile(context, tile, this.leaving == null && tile.contains(mx, my)));
            }
            this.group(context, 0.26f, -22.0f, 0.0f, () -> this.text(context, net.albiz.client.i18n.AlbizI18n.display("QUICK JOIN"), 76.0f, 318.0f, 0.78f, -7367779));
            this.group(context, 0.12f, 0.0f, 0.0f, () -> this.skinCard(context));
            this.group(context, 0.34f, 0.0f, 6.0f, () -> this.footer(context));
        }
        finally {
            matrices.popMatrix();
        }
        this.showWidgets(this.shown(0.12f) * (1.0f - this.leave()) > 0.6f);
        super.method_25394(context, mouseX, mouseY, deltaTicks);
        this.introLabels(context, mouseX, mouseY);
        Notifications.render(context, true);
    }

    private void surface(class_332 context, int mouseX, int mouseY) {
        int peak;
        float span;
        float visible = this.shown(0.0f) * (1.0f - this.leave());
        if (visible <= 0.003f || Theme.liquidGlass()) {
            return;
        }
        int accent = HomeScreen.accent();
        float cell = 40.0f * this.scale;
        float reach = 170.0f * this.scale;
        float hover = this.animate("surface", mouseX > 0 && mouseY > 0 && mouseX < this.field_22789 - 1 && mouseY < this.field_22790 - 1, 4.0f);
        Smooth.glow(context, mouseX, mouseY, Math.round(reach * 0.9f), HomeScreen.alpha(accent, 0.05f * visible * hover));
        for (float lineX = this.originX % cell; lineX < (float)this.field_22789; lineX += cell) {
            int x = Math.round(lineX);
            context.method_25294(x, 0, x + 1, this.field_22790, HomeScreen.alpha(0xFFFFFF, 0.018f * visible));
            float dx = Math.abs(lineX - (float)mouseX);
            if (dx >= reach || hover <= 0.01f) continue;
            span = (float)Math.sqrt(reach * reach - dx * dx);
            peak = HomeScreen.alpha(0xFFFFFF, 0.075f * visible * hover * (1.0f - dx / reach) * (1.0f - dx / reach));
            context.method_25296(x, Math.round((float)mouseY - span), x + 1, mouseY, peak & 0xFFFFFF, peak);
            context.method_25296(x, mouseY, x + 1, Math.round((float)mouseY + span), peak, peak & 0xFFFFFF);
        }
        for (float lineY = this.originY % cell; lineY < (float)this.field_22790; lineY += cell) {
            int y = Math.round(lineY);
            context.method_25294(0, y, this.field_22789, y + 1, HomeScreen.alpha(0xFFFFFF, 0.018f * visible));
            float dy = Math.abs(lineY - (float)mouseY);
            if (dy >= reach || hover <= 0.01f) continue;
            span = (float)Math.sqrt(reach * reach - dy * dy);
            peak = HomeScreen.alpha(0xFFFFFF, 0.075f * visible * hover * (1.0f - dy / reach) * (1.0f - dy / reach));
            Smooth.horizontalGradient(context, Math.round((float)mouseX - span), y, mouseX, y + 1, peak & 0xFFFFFF, peak);
            Smooth.horizontalGradient(context, mouseX, y, Math.round((float)mouseX + span), y + 1, peak, peak & 0xFFFFFF);
        }
        float drift = 0.5f + 0.5f * (float)Math.sin(this.now * 0.35f);
        float markX = this.originX + 520.0f * this.scale - this.parallaxX * 14.0f;
        float markY = this.originY + 250.0f * this.scale - this.parallaxY * 10.0f + drift * 4.0f;
        float markSize = 330.0f * this.scale;
        float distance = (float)Math.hypot((float)mouseX - markX, (float)mouseY - markY);
        float reveal = this.animate("markReveal", distance < markSize * 0.55f, 4.5f) * HomeScreen.clamp(1.2f - distance / (markSize * 0.6f));
        if (reveal > 0.01f) {
            Smooth.glow(context, Math.round(markX), Math.round(markY), Math.round(markSize * 0.55f), HomeScreen.alpha(accent, 0.06f * reveal * visible));
        }
        if (!Theme.liquidGlass()) {
            Logo3D.draw(context, markX, markY, markSize, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, (0.035f + 0.11f * reveal) * visible);
        }
    }

    private void showWidgets(boolean visible) {
        if (this.skinWidget == null) {
            return;
        }
        this.skinWidget.field_22764 = visible;
        this.nameField.field_22764 = visible;
        this.skinField.field_22764 = visible;
    }

    private float introElapsed() {
        return this.introShift > 0.0f ? (float)(System.nanoTime() - this.openedAt) / 1.0E9f : Float.MAX_VALUE;
    }

    private float introCenterX() {
        return ((float)this.field_22789 / 2.0f - this.originX) / this.scale;
    }

    private float introCenterY() {
        return ((float)this.field_22790 * 0.43f - this.originY) / this.scale;
    }

    private float introBig() {
        return Math.min(230.0f, (float)this.field_22790 / this.scale * 0.48f);
    }

    private float[] introLogo(float elapsed) {
        float centerX = this.introCenterX();
        float centerY = this.introCenterY();
        float big = this.introBig();
        float enter = HomeScreen.quint(elapsed / 0.9f);
        float move = HomeScreen.smooth((elapsed - 3.4f) / 1.7999997f);
        float shrink = HomeScreen.smooth((elapsed - 3.4f) / 1.7999997f);
        float size = big * (1.05f - 0.05f * enter);
        size += (30.0f - size) * shrink;
        float cx = centerX + (89.0f - centerX) * move;
        float cy = centerY + (123.0f - centerY) * move;
        return new float[]{cx, cy, size, move};
    }

    private void introWindow(class_332 context) {
        float elapsed = this.introElapsed();
        if (elapsed < 4.3f || elapsed > 6.6f) {
            return;
        }
        float grow = HomeScreen.smooth((elapsed - 4.3f) / 0.9999995f);
        float[] logo = this.introLogo(Math.min(elapsed, 5.2f));
        float sx = logo[0] - logo[2] * 0.72f;
        float sy = logo[1] - logo[2] * 0.62f;
        float sw = logo[2] * 1.44f;
        float sh = logo[2] * 1.24f;
        float ex = 40.0f;
        float ey = 70.0f;
        float ew = 780.0f;
        float eh = 322.0f;
        int x = Math.round(sx + (ex - sx) * grow);
        int y = Math.round(sy + (ey - sy) * grow);
        int w = Math.round(sw + (ew - sw) * grow);
        int h = Math.round(sh + (eh - sh) * grow);
        float alpha = HomeScreen.clamp((elapsed - 4.3f) / 0.18f) * (1.0f - HomeScreen.smooth((elapsed - 5.2f - 0.6f) / 0.7f)) * (1.0f - this.leave());
        if (alpha <= 0.003f) {
            return;
        }
        this.fade = alpha;
        this.rect(context, x, y, w, h, 16, -267710955);
        context.method_25296(x + 8, y + 1, x + w - 8, y + Math.min(h - 1, 60), this.f(0xCFFFFFF), this.f(0xFFFFFF));
        this.outline(context, x, y, w, h, 16, 0x1CFFFFFF);
        this.fade = 1.0f;
    }

    private void introLabels(class_332 context, int mouseX, int mouseY) {
        float elapsed = this.introElapsed();
        if (elapsed > 4.0f) {
            return;
        }
        float alpha = HomeScreen.quint((elapsed - 0.2f) / 0.6f) * (1.0f - HomeScreen.smooth((elapsed - 3.4f) / 0.45f));
        if (alpha <= 0.003f) {
            return;
        }
        int accent = HomeScreen.accent();
        this.fade = alpha;
        MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
        boolean sound = menu == null || menu.audio.get() != false;
        String version = FabricLoader.getInstance().getModContainer("albiz").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("ALBIZ CLIENT"), 14.0f, 12.0f, 0.72f, -7367779);
        this.text(context, "V" + version, (float)(this.field_22789 - 14) - this.textWidth("V" + version, 0.72f), 12.0f, 0.72f, -7367779);
        boolean overSound = mouseX >= 8 && mouseX < 90 && mouseY >= this.field_22790 - 24 && mouseY < this.field_22790 - 4;
        this.text(context, sound ? "SOUND ON" : "SOUND OFF", 14.0f, this.field_22790 - 17, 0.72f, overSound ? -855051 : -4275510);
        Smooth.softDot(context, 14.0f + this.textWidth(sound ? "SOUND ON" : "SOUND OFF", 0.72f) + 7.0f, (float)this.field_22790 - 14.5f, 3.2f, this.f(sound ? HomeScreen.alpha(accent, 0.95f) : -2140442760));
        String skip = "SKIP INTRO";
        float skipWidth = this.textWidth(skip, 0.72f);
        boolean overSkip = (float)mouseX >= (float)this.field_22789 - skipWidth - 34.0f && mouseY >= this.field_22790 - 24 && mouseY < this.field_22790 - 4;
        float skipX = (float)(this.field_22789 - 26) - skipWidth;
        this.text(context, skip, skipX, this.field_22790 - 17, 0.72f, overSkip ? -855051 : -4275510);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)this.field_22789 - 18.0f, (float)this.field_22790 - 14.5f);
        matrices.rotate(1.5707964f);
        Smooth.triangle(context, 0.0f, 0.0f, 6.0f, this.f(overSkip ? -855051 : -4275510));
        matrices.popMatrix();
        float progress = HomeScreen.clamp(elapsed / 3.4f);
        int barX = this.field_22789 / 2 - 30;
        int barY = this.field_22790 - 14;
        context.method_25294(barX, barY, barX + 60, barY + 1, this.f(0x22FFFFFF));
        Smooth.horizontalGradient(context, barX, barY, barX + Math.max(1, Math.round(60.0f * progress)), barY + 1, this.f(HomeScreen.alpha(accent, 0.2f)), this.f(HomeScreen.alpha(accent, 0.95f)));
        this.fade = 1.0f;
    }

    private void introName(class_332 context, float elapsed) {
        float alpha = HomeScreen.quint((elapsed - 0.35f) / 0.6f) * (1.0f - HomeScreen.smooth((elapsed - 3.4f + 0.1f) / 0.4f));
        if (alpha <= 0.003f) {
            return;
        }
        float centerX = this.introCenterX();
        float top = this.introCenterY() + this.introBig() * 0.56f + (1.0f - alpha) * 3.0f;
        this.fade = alpha;
        this.spaced(context, net.albiz.client.i18n.AlbizI18n.display("ALBIZ CLIENT"), centerX, top, 1.25f, 5.5f, -2499358);
        this.spaced(context, net.albiz.client.i18n.AlbizI18n.display("CLIENT"), centerX, top + 16.0f, 0.7f, 3.2f, -7367779);
        this.rect(context, Math.round(centerX - 9.0f), Math.round(top + 29.0f), 18, 1, 0, 0x40FFFFFF);
        this.fade = 1.0f;
    }

    private void spaced(class_332 context, String value, float centerX, float y, float size, float spacing, int color) {
        float total = -spacing;
        for (int i = 0; i < value.length(); ++i) {
            total += this.textWidth(String.valueOf(value.charAt(i)), size) + spacing;
        }
        float x = centerX - total / 2.0f;
        for (int i = 0; i < value.length(); ++i) {
            String letter = String.valueOf(value.charAt(i));
            this.text(context, letter, x, y, size, color);
            x += this.textWidth(letter, size) + spacing;
        }
    }

    private void skipIntro() {
        if (this.introElapsed() < 4.3f) {
            this.openedAt = System.nanoTime() - 4300000256L;
        }
    }

    private void heroLogo(class_332 context) {
        float visible;
        int accent = HomeScreen.accent();
        float elapsed = this.time + this.introShift;
        float out = this.leave();
        float targetCx = 89.0f;
        float targetCy = 123.0f;
        float targetSize = 30.0f;
        float cx = targetCx;
        float cy = targetCy;
        float size = targetSize;
        float yaw = 0.0f;
        float pitch = 0.0f;
        float depth = 0.0f;
        float shine = 0.0f;
        float shinePos = 0.0f;
        float glow = 0.3f;
        if (this.introShift > 0.0f && elapsed < 5.2f) {
            float[] logo = this.introLogo(elapsed);
            cx = logo[0];
            cy = logo[1];
            size = logo[2];
            float move = logo[3];
            float drift = HomeScreen.clamp(elapsed / 3.4f);
            yaw = (-0.34f + 0.1f * drift) * (1.0f - move) + 0.22f * (float)Math.sin(Math.PI * (double)move);
            pitch = 0.07f * (1.0f - move);
            depth = 1.0f - move * move;
            float first = HomeScreen.clamp((elapsed - 0.6f) / 1.3f);
            float second = HomeScreen.clamp((elapsed - 2.2f) / 1.1f);
            if (first > 0.0f && first < 1.0f) {
                shine = (float)Math.sin(Math.PI * (double)first);
                shinePos = -0.25f + 1.5f * HomeScreen.smooth(first);
            } else if (second > 0.0f && second < 1.0f) {
                shine = 0.6f * (float)Math.sin(Math.PI * (double)second);
                shinePos = -0.25f + 1.5f * HomeScreen.smooth(second);
            }
            visible = HomeScreen.quint(elapsed / 0.6f);
            glow = 0.18f + 0.22f * (1.0f - move);
            this.introName(context, elapsed);
        } else if (this.introShift > 0.0f) {
            visible = 1.0f;
            glow = 0.3f + 0.35f * (float)Math.exp(-(elapsed - 5.2f) * 5.0f);
        } else {
            float flip;
            visible = flip = HomeScreen.quint((this.time - 0.05f) / 0.75f);
            yaw = -1.45f * (1.0f - flip);
            pitch = 0.18f * (1.0f - flip);
            depth = 1.0f - flip;
        }
        float shown = visible * (1.0f - out);
        if (shown <= 0.003f) {
            return;
        }
        this.fade = shown;
        float breathe = 0.5f + 0.5f * (float)Math.sin(this.now * 1.2f);
        Smooth.glow(context, Math.round(cx), Math.round(cy), Math.round(size * 0.9f), this.f(HomeScreen.alpha(accent, glow + 0.1f * breathe)));
        this.fade = 1.0f;
        Logo3D.draw(context, cx, cy, size, yaw, pitch, depth, shine, shinePos, shown, 1.0f);
    }

    private void topBar(class_332 context) {
        if (!this.glass(context, 20, 8, 820, 34, 17)) {
            this.rect(context, 20, 8, 820, 34, 17, 0x12FFFFFF);
            this.outline(context, 20, 8, 820, 34, 17, 0x1FFFFFFF);
        }
        ScreenStyle.topLight(context, 20, 8, 820, 17, 0.35f * this.fade);
        Smooth.glow(context, 42, 25, 16, this.f(HomeScreen.alpha(4059872, 0.3f)));
        this.logo(context, 32.0f, 15.0f, 20.0f);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("Albız"), 58.0f, 20.0f, 1.15f, -855051);
        String name = this.shownName();
        int pill = Math.round(30.0f + this.textWidth(name, 1.0f));
        int pillX = 830 - pill;
        if (!this.glass(context, pillX, 12, pill, 26, 13)) {
            this.rect(context, pillX, 12, pill, 26, 13, 0x14FFFFFF);
            this.outline(context, pillX, 12, pill, 26, 13, 0x1FFFFFFF);
        }
        if (this.fade > 0.9f) {
            Avatars.draw(context, this.worn(this.baseSkin.get()), pillX + 6, 17, 16);
        }
        this.text(context, name, pillX + 26, 21.0f, 1.0f, -855051);
        LocalTime local = LocalTime.now();
        String clock = String.format("%02d:%02d", local.getHour(), local.getMinute());
        this.text(context, clock, (float)(pillX - 12) - this.textWidth(clock, 1.0f), 21.0f, 1.0f, -855051);
    }

    private String shownName() {
        String custom = NameTags.customName().trim();
        if (!custom.isEmpty()) {
            return custom;
        }
        return this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
    }

    private void title(class_332 context) {
        int accent = HomeScreen.accent();
        if (!Theme.liquidGlass()) {
            Smooth.glow(context, 200, 124, 120, this.f(HomeScreen.alpha(accent, 0.2f)));
        }
        for (int k = 0; k < 8 && !Theme.liquidGlass(); ++k) {
            double angle = Math.PI * 2 * (double)k / 8.0;
            CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display("Albız"), "montserrat-bold.ttf", 112.0f + (float)Math.cos(angle) * 1.6f, 136.0f + (float)Math.sin(angle) * 1.6f, 37.0f, -0.01f, this.f(HomeScreen.alpha(accent, 0.16f)));
        }
        CrispText.draw(context, net.albiz.client.i18n.AlbizI18n.display("Albız"), "montserrat-bold.ttf", 112.0f, 136.0f, 37.0f, -0.01f, this.f(-855051));
        String minecraft = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        String sober = FabricLoader.getInstance().getModContainer("albiz").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        this.text(context, "Fabric " + minecraft + "  \u00b7  Albız Client " + sober, 76.0f, 150.0f, 1.0f, -4275510);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawTile(class_332 context, Tile tile, boolean over) {
        float glow = this.animate(tile.key(), over, 11.0f);
        float press = tile.key().equals(this.pressedKey) ? HomeScreen.clamp(1.0f - (float)(System.nanoTime() - this.pressedAt) / 1.0E9f / 0.35f) : 0.0f;
        int x = Math.round(tile.x());
        int y = Math.round(tile.y());
        int w = Math.round(tile.w());
        int h = Math.round(tile.h());
        int accent = HomeScreen.accent();
        boolean big = tile.key().equals("single") || tile.key().equals("multi");
        boolean server = tile.key().startsWith("server");
        int radius = big ? 16 : (server ? 12 : h / 2);
        float lit = Math.max(glow, press);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        try {
            if (lit > 0.01f) {
                Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.7f), this.f(HomeScreen.alpha(accent, 0.2f * lit)));
            }
            if (!this.glass(context, x, y, w, h, radius)) {
                this.rect(context, x, y, w, h, radius, HomeScreen.lerp(0x14FFFFFF, 0x22FFFFFF, glow));
                this.outline(context, x, y, w, h, radius, HomeScreen.lerp(0x1FFFFFFF, 0xFFFFFF, lit));
            }
            ScreenStyle.topLight(context, x, y, w, radius, 0.4f * (1.0f - lit) * this.fade);
            ScreenStyle.neon(context, x, y, w, h, radius, accent, lit * this.fade);
            if (big) {
                boolean single = tile.key().equals("single");
                if (glow > 0.01f) {
                    ScreenStyle.neon(context, x + 14, y + 18, 34, 34, 11, accent, glow * this.fade);
                }
                this.rect(context, x + 14, y + 18, 34, 34, 11, HomeScreen.lerp(0x16FFFFFF, HomeScreen.alpha(accent, 0.92f), glow));
                this.outline(context, x + 14, y + 18, 34, 34, 11, HomeScreen.lerp(0x24FFFFFF, 0xFFFFFF, glow));
                Icons.draw(context, single ? Icons.Icon.CUBE : Icons.Icon.GRID, x + 23, y + 27, 16, this.f(HomeScreen.lerp(-855051, -15069692, glow)));
                this.text(context, net.albiz.client.i18n.AlbizI18n.display(single ? "Singleplayer" : "Multiplayer"), x + 58, y + 22, 1.35f, -855051);
                this.text(context, net.albiz.client.i18n.AlbizI18n.display(single ? "Jump back into your worlds" : "Servers and LAN games"), x + 58, y + 40, 0.95f, HomeScreen.lerp(-4275510, -855051, glow * 0.4f));
            } else if (server) {
                int index = Integer.parseInt(tile.key().substring(6));
                class_642 info = this.servers.get(index);
                float wave = (this.now * 0.5f + (float)index * 0.33f) % 1.0f;
                float ring = HomeScreen.smooth(wave);
                Smooth.softDot(context, (float)x + 14.0f, (float)y + 19.0f, 4.0f + 6.0f * ring, this.f(HomeScreen.alpha(accent, 0.4f * (1.0f - ring))));
                Smooth.glow(context, x + 14, y + 19, 10, this.f(HomeScreen.alpha(accent, 0.55f)));
                Smooth.circle(context, x + 14, y + 19, 4, this.f(accent));
                this.text(context, this.fit(info.field_3752, w - 34, 1.1f), x + 26, y + 9, 1.1f, -855051);
                this.text(context, this.fit(info.field_3761, w - 34, 0.85f), x + 26, y + 23, 0.85f, -4275510);
            } else {
                Icons.Icon icon = switch (tile.key()) {
                    case "Realms" -> Icons.Icon.SPARKLE;
                    case "Options" -> Icons.Icon.COLUMNS;
                    case "Mods" -> Icons.Icon.WINDOW;
                    default -> Icons.Icon.CLOSE;
                };
                int color = tile.key().equals("Quit") ? HomeScreen.lerp(-855051, -38037, glow) : -855051;
                Icons.draw(context, icon, x + 14, y + 10, 12, this.f(color));
                this.text(context, net.albiz.client.i18n.AlbizI18n.display(tile.key()), x + 33, y + 12, 1.1f, color);
            }
        }
        finally {
            matrices.popMatrix();
        }
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

    private void skinCard(class_332 context) {
        int accent = HomeScreen.accent();
        float breathe = 0.5f + 0.5f * (float)Math.sin(this.now * 1.1f);
        Smooth.glow(context, 691, 232, 124, this.f(HomeScreen.alpha(accent, 0.12f + 0.05f * breathe)));
        if (!this.glass(context, 604, 84, 174, 296, 22)) {
            this.rect(context, 604, 84, 174, 296, 22, 0x12FFFFFF);
        }
        ScreenStyle.neon(context, 604, 84, 174, 296, 22, accent, (0.4f + 0.2f * breathe) * this.fade);
        String name = this.shownName();
        this.text(context, name, 691.0f - this.textWidth(name, 1.15f) / 2.0f, 272.0f, 1.15f, -855051);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("Drag to spin"), 691.0f - this.textWidth("Drag to spin", 0.85f) / 2.0f, 286.0f, 0.85f, -7367779);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("NAME"), 616.0f, 297.0f, 0.72f, -7367779);
        this.text(context, net.albiz.client.i18n.AlbizI18n.display("SKIN"), 616.0f, 333.0f, 0.72f, -7367779);
        this.fieldBox(context, "nameBox", 614, 307, this.nameField != null && this.nameField.method_25370());
        this.fieldBox(context, "skinBox", 614, 343, this.skinField != null && this.skinField.method_25370());
    }

    private void fieldBox(class_332 context, String key, int x, int y, boolean focused) {
        float focus = this.animate(key, focused, 12.0f);
        int accent = HomeScreen.accent();
        if (focus > 0.01f) {
            Smooth.glow(context, x + 77, y + 11, 70, this.f(HomeScreen.alpha(accent, 0.16f * focus)));
        }
        if (!this.glass(context, x, y, 154, 22, 11)) {
            this.rect(context, x, y, 154, 22, 11, HomeScreen.lerp(0xEFFFFFF, 0x18FFFFFF, focus));
            this.outline(context, x, y, 154, 22, 11, HomeScreen.lerp(0x1CFFFFFF, 0xFFFFFF, focus));
        }
        ScreenStyle.neon(context, x, y, 154, 22, 11, accent, focus * this.fade);
    }

    private static boolean inBox(double mx, double my, float x, float y) {
        return mx >= (double)x && mx < (double)(x + 154.0f) && my >= (double)y && my < (double)(y + 22.0f);
    }

    private void footer(class_332 context) {
        String minecraft = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        this.text(context, "Albız Client  \u00b7  Minecraft " + minecraft, 22.0f, 420.0f, 0.85f, -7367779);
        String copyright = "Copyright Mojang AB. Do not distribute!";
        this.text(context, copyright, 838.0f - this.textWidth(copyright, 0.85f), 420.0f, 0.85f, -7367779);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.leaving != null) {
            return true;
        }
        if (this.introElapsed() < 4.3f) {
            if (click.method_74245() == 0 && click.comp_4798() >= 8.0 && click.comp_4798() < 90.0 && click.comp_4799() >= (double)(this.field_22790 - 24) && click.comp_4799() < (double)(this.field_22790 - 4)) {
                MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
                if (menu != null) {
                    menu.audio.set(menu.audio.get() == false);
                }
                UiSounds.click();
                return true;
            }
            this.skipIntro();
            return true;
        }
        if (super.method_25402(click, doubled)) {
            return true;
        }
        if (click.method_74245() != 0) {
            return false;
        }
        double mx = (click.comp_4798() - (double)this.originX) / (double)this.scale;
        double my = (click.comp_4799() - (double)this.originY) / (double)this.scale;
        if (this.nameField != null && this.nameField.field_22764 && HomeScreen.inBox(mx, my, 614.0f, 307.0f)) {
            this.method_25395((class_364)this.nameField);
            return true;
        }
        if (this.skinField != null && this.skinField.field_22764 && HomeScreen.inBox(mx, my, 614.0f, 343.0f)) {
            this.method_25395((class_364)this.skinField);
            return true;
        }
        for (Tile tile : this.tiles) {
            if (!(this.shown(tile.delay()) > 0.7f) || !tile.contains(mx, my)) continue;
            UiSounds.click();
            this.pressedKey = tile.key();
            this.pressedAt = System.nanoTime();
            this.leaving = tile.action();
            this.leaveStart = System.nanoTime() + 90000000L;
            return true;
        }
        this.method_25395(null);
        return false;
    }

    public boolean method_25404(class_11908 input) {
        if (this.introElapsed() < 4.3f && (input.comp_4795() == 32 || input.comp_4795() == 257 || input.comp_4795() == 256)) {
            this.skipIntro();
            return true;
        }
        if (input.comp_4795() == 257 && this.skinField != null && this.skinField.method_25370()) {
            this.applySkin();
            return true;
        }
        return super.method_25404(input);
    }

    private static float[][] stars() {
        Random random = new Random(1337L);
        float[][] stars = new float[130][];
        for (int i = 0; i < stars.length; ++i) {
            stars[i] = new float[]{random.nextFloat(), random.nextFloat(), random.nextFloat() * 6.28f, 0.6f + random.nextFloat() * 1.6f, random.nextFloat()};
        }
        return stars;
    }

    private record Tile(String key, float x, float y, float w, float h, float delay, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }
}

