// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.HomeScreen;
import net.albiz.client.port.soberui.gui.SdfText;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.VanillaPlusScreen;
import net.albiz.client.port.soberui.modules.MainMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5223;
import net.minecraft.class_5348;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class ScreenStyle {
    private static final int CARD = 2895926;
    private static final int CARD_HOVER = 3817287;
    private static final int DEEP = 1777187;
    private static final int ACCENT = 14014942;
    private static final Map<Object, float[]> HOVER = new WeakHashMap<Object, float[]>();
    private static final Set<Object> PRIMARY = Collections.newSetFromMap(new WeakHashMap());
    private static final Set<Object> FLAT = Collections.newSetFromMap(new WeakHashMap());
    private static int trackX;
    private static int trackY;
    private static int trackH;
    private static boolean labelDrawn;
    static final int SOBER_R = 4;

    private ScreenStyle() {
    }

    private static MainMenu menu() {
        return SoberClient.MODULES.get(MainMenu.class);
    }

    public static boolean screens() {
        MainMenu menu = ScreenStyle.menu();
        return menu != null && menu.isActive() && menu.customScreens.get() != false;
    }

    public static boolean widgets() {
        MainMenu menu = ScreenStyle.menu();
        return menu != null && menu.isActive() && menu.customWidgets.get() != false;
    }

    public static int color() {
        return net.albiz.client.config.ThemeStore.get().accent.rgb();
    }

    private static int tint() {
        int sky;
        MainMenu menu = ScreenStyle.menu();
        if (menu != null && menu.matchSky.get().booleanValue() && (sky = menu.color()) >= 0) {
            return ScreenStyle.mix(5923954, sky, 0.35f);
        }
        return 5923954;
    }

    static int alpha(int color, float alpha) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;
    }

    static int mix(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    private static float hash(int n) {
        int h = n * 374761393 + 668265263;
        h = (h ^ h >>> 13) * 1274126177;
        return (float)((h ^ h >>> 16) & 0xFFFFFF) / 1.6777215E7f;
    }

    private static float animate(Object key, boolean target) {
        float[] state;
        long now = System.nanoTime();
        float delta = Math.min(0.05f, (float)(now - (long)(state = HOVER.computeIfAbsent(key, k -> new float[]{0.0f, now}))[1]) / 1.0E9f);
        if (delta < 0.0f) {
            delta = 0.0f;
        }
        state[1] = now;
        state[0] = state[0] + ((target ? 1.0f : 0.0f) - state[0]) * (1.0f - (float)Math.exp(-16.0f * delta));
        return state[0];
    }

    public static void primary(Object widget) {
        PRIMARY.add(widget);
    }

    public static boolean glassMenu() {
        return Theme.liquidGlass();
    }

    public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        if (!Theme.liquidGlass()) {
            return false;
        }
        float bend = Math.max(2.0f, Math.min(13.0f, (float)Math.min(w, h) * 0.13f));
        return Backdrop.glass(context, x, y, w, h, r, opacity, bend, Theme.glassTint(), false);
    }

    public static void shadow(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        if (opacity <= 0.01f || w <= 0 || h <= 0) {
            return;
        }
        for (int k = 10; k >= 1; --k) {
            float t = (float)k / 10.0f;
            float grow = 1.5f + 9.0f * t;
            int color = ScreenStyle.alpha(1312286, 0.035f * (1.0f - t * 0.7f) * opacity);
            Smooth.rect(context, Math.round((float)x - grow * 0.6f), Math.round((float)y - grow * 0.2f + 5.0f), Math.round((float)w + grow * 1.2f), Math.round((float)h + grow * 1.2f), Math.round((float)r + grow), color);
        }
    }

    public static void flat(Object widget) {
        FLAT.add(widget);
    }

    public static void crisp(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
        if (value == null || value.isEmpty() || color >>> 24 == 0) {
            return;
        }
        if (ScreenStyle.formatted(value)) {
            float cx = x;
            for (Run run : ScreenStyle.runs(class_5348.method_29430((String)value), color)) {
                ScreenStyle.crisp(context, run.text(), cx, y, size, run.color(), heavy);
                cx += ScreenStyle.crispWidth(run.text(), size, heavy);
            }
            return;
        }
        String file = Theme.crispFile(heavy);
        if (!CrispText.canDisplay(value, file)) {
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(size, size);
            context.method_51433(class_310.method_1551().field_1772, value, 0, 0, color, false);
            matrices.popMatrix();
            return;
        }
        float em = 9.4f * size * Theme.crispScale();
        float baseline = y + 7.6f * size;
        CrispText.draw(context, value, file, x, baseline, em, 0.0f, color);
    }

    public static float crispWidth(String value, float size, boolean heavy) {
        if (value == null || value.isEmpty()) {
            return 0.0f;
        }
        if (ScreenStyle.formatted(value)) {
            float width = 0.0f;
            for (Run run : ScreenStyle.runs(class_5348.method_29430((String)value), -1)) {
                width += ScreenStyle.crispWidth(run.text(), size, heavy);
            }
            return width;
        }
        String file = Theme.crispFile(heavy);
        if (!CrispText.canDisplay(value, file)) {
            return (float)class_310.method_1551().field_1772.method_1727(value) * size;
        }
        return CrispText.width(value, file, 9.4f * size * Theme.crispScale(), 0.0f);
    }

    public static void sdf(class_332 context, String value, float x, float y, float size, int color) {
        ScreenStyle.sdf(context, value, x, y, size, color, true);
    }

    public static void sdf(class_332 context, String value, float x, float y, float size, int color, boolean shadow) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(size, size);
        if (shadow && Theme.liquidGlass()) {
            matrices.pushMatrix();
            matrices.translate(0.0f, 0.5f);
            SdfText.draw(context, value, 0, 0, Math.round((float)(color >>> 24) * 0.18f) << 24);
            matrices.popMatrix();
        }
        SdfText.draw(context, value, 0, 0, color);
        matrices.popMatrix();
    }

    public static List<Run> runs(class_5348 text, int base) {
        ArrayList<Run> out = new ArrayList<Run>();
        StringBuilder current = new StringBuilder();
        int[] color = new int[]{base};
        class_5223.method_27476((class_5348)text, (class_2583)class_2583.field_24360, (index, style, codePoint) -> {
            int next;
            int n = next = style.method_10973() != null ? base & 0xFF000000 | style.method_10973().method_27716() & 0xFFFFFF : base;
            if (next != color[0] && current.length() > 0) {
                out.add(new Run(current.toString(), color[0]));
                current.setLength(0);
            }
            color[0] = next;
            current.appendCodePoint(codePoint);
            return true;
        });
        if (current.length() > 0) {
            out.add(new Run(current.toString(), color[0]));
        }
        return out;
    }

    public static boolean formatted(String value) {
        return value.indexOf(167) >= 0;
    }

    public static boolean label(class_332 context, Object key, class_2561 message, int x, int y, int w, int h, boolean active, float opacity) {
        labelDrawn = false;
        if (message == null || opacity <= 0.01f) {
            return false;
        }
        int base = 0xFF000000 | (PRIMARY.contains(key) ? 1707524 : (active ? 0xFFFFFF : 0xA0A0A0));
        List<Run> parts = ScreenStyle.runs((class_5348)message, base).stream().map(run->new Run(net.albiz.client.i18n.AlbizI18n.display(run.text()),run.color())).toList();
        StringBuilder plain = new StringBuilder();
        for (Run run : parts) {
            plain.append(run.text());
        }
        String value = plain.toString();
        if (value.isEmpty()) {
            return false;
        }
        String file = Theme.crispFile(false);
        if (!CrispText.canDisplay(value, file)) {
            return false;
        }
        float em = 9.0f;
        float width = CrispText.width(value, file, em, 0.0f);
        if (width > (float)(w - 6)) {
            return false;
        }
        float cx = Math.round((float)x + ((float)w - width) / 2.0f);
        float baseline = (float)y + (float)(h - 8) / 2.0f + 7.2f;
        for (Run run : parts) {
            if (!PRIMARY.contains(key)) {
                CrispText.draw(context, run.text(), file, cx + 0.5f, baseline + 0.7f, em, 0.0f, ScreenStyle.alpha(0, 0.3f * opacity));
            }
            CrispText.draw(context, run.text(), file, cx, baseline, em, 0.0f, ScreenStyle.alpha(run.color(), opacity));
            cx += CrispText.width(run.text(), file, em, 0.0f);
        }
        labelDrawn = true;
        return true;
    }

    public static boolean consumeLabel() {
        boolean drawn = labelDrawn;
        labelDrawn = false;
        return drawn;
    }

    public static int readable(int color) {
        if (!Theme.liquidGlass()) {
            return color;
        }
        int rgb = color & 0xFFFFFF;
        if (rgb == 10330538 || rgb == 11120053) {
            return color & 0xFF000000 | 0xDADDE3;
        }
        if (rgb == 7040888) {
            return color & 0xFF000000 | 0xC4C8CF;
        }
        return color;
    }

    public static void neon(class_332 context, int x, int y, int w, int h, int r, int color, float strength) {
        if (strength <= 0.01f || w <= 2 || h <= 2) {
            return;
        }
        int rgb = color & 0xFFFFFF;
        if (Theme.liquidGlass()) {
            Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(rgb, 0xFFFFFF, 0.7f), 0.1f * strength));
            return;
        }
        for (int k = 6; k >= 1; --k) {
            float t = (float)k / 6.0f;
            Smooth.outline(context, x - k, y - k, w + k * 2, h + k * 2, r + k, ScreenStyle.alpha(rgb, strength * (0.15f * (1.0f - t) * (1.0f - t) + 0.015f)));
        }
        Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(rgb, 0.6f * strength));
        if (w > 4 && h > 4) {
            Smooth.outline(context, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), ScreenStyle.alpha(ScreenStyle.mix(rgb, 0xFFFFFF, 0.55f), Math.min(1.0f, 0.9f * strength)));
        }
    }

    public static void topLight(class_332 context, int x, int y, int w, int r, float strength) {
        int left = x + r;
        int right = x + w - r;
        int mid = (left + right) / 2;
        if (Theme.liquidGlass() || right - left < 6 || strength <= 0.01f) {
            return;
        }
        Smooth.horizontalGradient(context, left, y, mid, y + 1, ScreenStyle.alpha(0xFFFFFF, 0.0f), ScreenStyle.alpha(0xFFFFFF, strength));
        Smooth.horizontalGradient(context, mid, y, right, y + 1, ScreenStyle.alpha(0xFFFFFF, strength), ScreenStyle.alpha(0xFFFFFF, 0.0f));
    }

    public static boolean vanillaPlus() {
        MainMenu menu = ScreenStyle.menu();
        return menu != null && menu.style.get() == MainMenu.Style.VanillaPlus;
    }

    public static boolean home() {
        MainMenu menu = ScreenStyle.menu();
        return menu != null && (menu.style.get() == MainMenu.Style.Home || menu.style.get() == MainMenu.Style.VanillaPlus || menu.style.get() == MainMenu.Style.Sober);
    }

    public static boolean soberStyle() {
        MainMenu menu = ScreenStyle.menu();
        return menu != null && menu.style.get() == MainMenu.Style.Sober;
    }

    private static int accent() {
        if (ScreenStyle.soberStyle()) {
            return -1513234;
        }
        MainMenu menu = ScreenStyle.menu();
        return 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
    }

    public static void crispPanel(class_332 context, float x, float y, float w, float h, float r, int fill, int border) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        class_1041 window = class_310.method_1551().method_22683();
        float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
        Vector2f a = matrices.transformPosition(x, y, new Vector2f());
        Vector2f b = matrices.transformPosition(x + w, y + h, new Vector2f());
        int x0 = Math.round(Math.min(a.x, b.x) * ratio);
        int y0 = Math.round(Math.min(a.y, b.y) * ratio);
        int x1 = Math.round(Math.max(a.x, b.x) * ratio);
        int y1 = Math.round(Math.max(a.y, b.y) * ratio);
        float zoom = (float)Math.sqrt(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01());
        int radius = Math.max(0, Math.round(r * zoom * ratio));
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        if (fill >>> 24 != 0) {
            Smooth.rect(context, x0, y0, x1 - x0, y1 - y0, radius, fill);
        }
        if (border >>> 24 != 0) {
            Smooth.outline(context, x0, y0, x1 - x0, y1 - y0, Math.max(1, radius), border);
        }
        matrices.popMatrix();
    }

    public static void soberPill(class_332 context, int x, int y, int w, int h, float hover, boolean primary, float opacity) {
        if (opacity <= 0.01f || w <= 0 || h <= 0) {
            return;
        }
        int r = Math.min(4, h / 2);
        if (primary) {
            ScreenStyle.crispPanel(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(0xE4E4E7, 0xFFFFFF, hover), opacity), 0);
            return;
        }
        ScreenStyle.crispPanel(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(0x151517, 0x1E1E22, hover), opacity), ScreenStyle.alpha(ScreenStyle.mix(0x2A2A2E, 4868690, hover), opacity));
    }

    public static void soberPanel(class_332 context, int x, int y, int w, int h, int r, float hover, float selected, float opacity) {
        if (opacity <= 0.01f || w <= 0 || h <= 0) {
            return;
        }
        r = Math.min(r, 5);
        ScreenStyle.crispPanel(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(ScreenStyle.mix(0x141416, 0x19191C, hover), 2039587, selected), (0.9f + 0.04f * selected) * opacity), ScreenStyle.alpha(0xFFFFFF, (0.08f + 0.08f * hover + 0.4f * selected) * opacity));
    }

    public static float intro(long openedAt, class_310 client, class_437 screen) {
        if (openedAt == 0L || client == null || client.field_1687 != null || screen instanceof HomeScreen || screen instanceof VanillaPlusScreen || !ScreenStyle.screens() || !ScreenStyle.home() || ScreenStyle.vanillaPlus()) {
            return 1.0f;
        }
        float p = Math.max(0.0f, Math.min(1.0f, (float)(System.nanoTime() - openedAt) / 1.0E9f / 0.5f));
        float inverse = 1.0f - p;
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    public static void introVeil(class_332 context, int width, int height, float p) {
        if (ScreenStyle.soberStyle()) {
            context.method_25294(0, 0, width, height, ScreenStyle.alpha(723724, 1.0f - p));
            return;
        }
        if (ScreenStyle.home()) {
            ScreenStyle.homeBackdrop(context, width, height, 1.0f - p);
            return;
        }
        context.method_25294(0, 0, width, height, ScreenStyle.alpha(460811, 1.0f - p));
    }

    private static void homeBackdrop(class_332 context, int width, int height, float cover) {
        class_310 client = class_310.method_1551();
        float mx = 0.0f;
        float my = 0.0f;
        if (client.method_22683() != null && width > 0 && height > 0) {
            mx = (float)(client.field_1729.method_68879(client.method_22683()) / (double)width) - 0.5f;
            my = (float)(client.field_1729.method_68883(client.method_22683()) / (double)height) - 0.5f;
        }
        HomeScreen.paintBackdrop(context, width, height, 1.0f, mx, my, cover);
    }

    public static void backdrop(class_332 context, int width, int height) {
        net.albiz.client.gui.mainmenu.DuskBackdrop.draw(context,width,height);
    }

    public static void serverIconOverlay(class_332 context, int x, int y) {
        Smooth.rect(context, x, y, 32, 32, 4, -1207235310);
        Smooth.outline(context, x, y, 32, 32, 4, ScreenStyle.alpha(ScreenStyle.accent(), 0.55f));
    }

    public static void serverIconSprite(class_332 context, int x, int y, String path) {
        boolean lit = path.endsWith("_highlighted");
        int accent = ScreenStyle.accent();
        Matrix3x2fStack matrices = context.method_51448();
        if (path.contains("join")) {
            float cx = (float)x + 20.0f;
            float cy = (float)y + 16.0f;
            if (lit) {
                Smooth.glow(context, Math.round(cx), Math.round(cy), 15, ScreenStyle.alpha(accent, 0.5f));
            }
            matrices.pushMatrix();
            matrices.translate(cx, cy);
            matrices.rotate(1.5707964f);
            Smooth.triangle(context, 0.0f, 0.0f, lit ? 13.0f : 11.0f, lit ? accent : -419430401);
            matrices.popMatrix();
            return;
        }
        boolean up = path.contains("move_up");
        float cx = (float)x + 7.0f;
        float cy = (float)y + (up ? 9.0f : 23.0f);
        if (lit) {
            Smooth.glow(context, Math.round(cx), Math.round(cy), 9, ScreenStyle.alpha(accent, 0.45f));
        }
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        if (!up) {
            matrices.rotate((float)Math.PI);
        }
        Smooth.triangle(context, 0.0f, 0.0f, lit ? 8.0f : 7.0f, lit ? accent : -1275068417);
        matrices.popMatrix();
    }

    public static void darkening(class_332 context, int x, int y, int w, int h) {
        if (Backdrop.menuActive()) {
            return;
        }
        context.method_25294(x, y, x + w, y + h, 0x30000000);
    }

    public static void button(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
        float hover;
        if (ScreenStyle.vanillaPlus()) {
            ScreenStyle.plainButton(context, key, x, y, w, h, active, hovered, opacity);
            return;
        }
        if (Theme.liquidGlass() && FLAT.contains(key)) {
            float hover2 = ScreenStyle.animate(key, active && hovered);
            if (hover2 > 0.01f) {
                Smooth.rect(context, x, y, w, h, h / 2, ScreenStyle.alpha(0xFFFFFF, 0.12f * hover2 * opacity));
            }
            return;
        }
        if (Theme.liquidGlass() && !PRIMARY.contains(key)) {
            hover = ScreenStyle.animate(key, active && hovered);
            int r = h / 2;
            if (ScreenStyle.glass(context, x, y, w, h, r, active ? opacity : opacity * 0.55f)) {
                if (active && hover > 0.01f) {
                    ScreenStyle.neon(context, x, y, w, h, r, ScreenStyle.accent(), hover * opacity);
                }
                return;
            }
        }
        if (ScreenStyle.soberStyle()) {
            hover = ScreenStyle.animate(key, active && hovered);
            if (!active) {
                ScreenStyle.crispPanel(context, x, y, w, h, Math.min(4, h / 2), ScreenStyle.alpha(0x111113, 0.75f * opacity), ScreenStyle.alpha(0xFFFFFF, 0.06f * opacity));
                return;
            }
            ScreenStyle.soberPill(context, x, y, w, h, hover, PRIMARY.contains(key), opacity);
            return;
        }
        if (ScreenStyle.home()) {
            ScreenStyle.homeButton(context, key, x, y, w, h, active, hovered, opacity);
            return;
        }
        int r = Math.min(h / 2, 8);
        float hover3 = ScreenStyle.animate(key, active && hovered);
        if (!active) {
            Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(1777187, 0.7f * opacity));
            return;
        }
        Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(2895926, 3817287, hover3), (0.78f + 0.1f * hover3) * opacity));
        if (hover3 > 0.01f) {
            Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, 0.14f * hover3 * opacity));
        }
    }

    private static void plainButton(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
        float hover = ScreenStyle.animate(key, active && hovered);
        if (!active) {
            Smooth.rect(context, x, y, w, h, 3, ScreenStyle.alpha(0, 0.3f * opacity));
            Smooth.outline(context, x, y, w, h, 3, ScreenStyle.alpha(0xFFFFFF, 0.08f * opacity));
            return;
        }
        boolean primary = PRIMARY.contains(key);
        Smooth.rect(context, x, y, w, h, 3, ScreenStyle.mixAlpha(0x70000000, 0x2EFFFFFF, hover, opacity));
        Smooth.outline(context, x, y, w, h, 3, ScreenStyle.mixAlpha(primary ? 0x70FFFFFF : 0x30FFFFFF, -788529153, hover, opacity));
    }

    private static int mixAlpha(int from, int to, float t, float opacity) {
        int a = Math.round(((float)(from >>> 24) + (float)((to >>> 24) - (from >>> 24)) * t) * opacity);
        return Math.max(0, Math.min(255, a)) << 24 | ScreenStyle.mix(from & 0xFFFFFF, to & 0xFFFFFF, t);
    }

    private static void homeButton(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
        int r = h / 2;
        float hover = ScreenStyle.animate(key, active && hovered);
        if (!active) {
            Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, 0.04f * opacity));
            Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, 0.06f * opacity));
            return;
        }
        int accent = ScreenStyle.accent();
        if (PRIMARY.contains(key)) {
            Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.75f), ScreenStyle.alpha(accent, (0.3f + 0.15f * hover) * opacity));
            ScreenStyle.neon(context, x, y, w, h, r, accent, (0.7f + 0.3f * hover) * opacity);
            Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(accent, 0xFFFFFF, 0.1f + 0.14f * hover), 0.95f * opacity));
            ScreenStyle.topLight(context, x, y, w, r, 0.55f * opacity);
            return;
        }
        if (hover > 0.01f) {
            Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.6f), ScreenStyle.alpha(accent, 0.2f * hover * opacity));
        }
        Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, (0.08f + 0.07f * hover) * opacity));
        Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, 0.12f * (1.0f - hover) * opacity));
        ScreenStyle.topLight(context, x, y, w, r, 0.3f * opacity);
        ScreenStyle.neon(context, x, y, w, h, r, accent, hover * opacity);
    }

    public static void textField(class_332 context, Object key, int x, int y, int w, int h, boolean focused) {
        int r;
        float focus;
        if (ScreenStyle.vanillaPlus()) {
            float focus2 = ScreenStyle.animate(key, focused);
            Smooth.rect(context, x, y, w, h, 3, ScreenStyle.alpha(0, 0.55f));
            Smooth.outline(context, x, y, w, h, 3, ScreenStyle.alpha(0xFFFFFF, 0.25f + 0.6f * focus2));
            return;
        }
        if (Theme.liquidGlass()) {
            focus = ScreenStyle.animate(key, focused);
            r = Math.min(h / 2, 8);
            if (ScreenStyle.glass(context, x, y, w, h, r, 1.0f)) {
                ScreenStyle.neon(context, x, y, w, h, r, ScreenStyle.accent(), focus);
                return;
            }
        }
        if (ScreenStyle.soberStyle()) {
            focus = ScreenStyle.animate(key, focused);
            ScreenStyle.crispPanel(context, x, y, w, h, Math.min(h / 2, 4), ScreenStyle.alpha(ScreenStyle.mix(1316377, 1711136, focus), 0.92f), ScreenStyle.alpha(0xFFFFFF, 0.12f + 0.38f * focus));
            return;
        }
        if (ScreenStyle.home()) {
            focus = ScreenStyle.animate(key, focused);
            r = Math.min(h / 2, 8);
            Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(0xFFFFFF, 0.06f + 0.04f * focus));
            Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(ScreenStyle.mix(0xFFFFFF, ScreenStyle.accent(), focus), 0.14f + 0.5f * focus));
            return;
        }
        focus = ScreenStyle.animate(key, focused);
        r = Math.min(h / 2, 6);
        Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(1777187, 0.9f));
        Smooth.outline(context, x, y, w, h, r, ScreenStyle.alpha(14014942, 0.12f + 0.45f * focus));
    }

    public static void sliderTrack(class_332 context, int x, int y, int w, int h, float opacity) {
        trackX = x;
        trackY = y;
        trackH = h;
        int r = Math.min(h / 2, 8);
        Smooth.rect(context, x, y, w, h, r, ScreenStyle.alpha(2895926, 0.78f * opacity));
    }

    public static void sliderHandle(class_332 context, int x, int y, int w, int h, boolean highlighted, float opacity) {
        int r = Math.min(trackH / 2, 8);
        if (y == trackY && x + w / 2 > trackX + 2) {
            Smooth.rect(context, trackX, trackY, x + w - trackX, trackH, r, ScreenStyle.alpha(14014942, 0.2f * opacity));
        }
        Smooth.rect(context, x + 1, y + 2, w - 2, h - 4, Math.min(3, (w - 2) / 2), ScreenStyle.alpha(highlighted ? 0xFFFFFF : 15790836, opacity));
    }

    public static void listBackground(class_332 context, int x, int y, int w, int h) {
        if (ScreenStyle.vanillaPlus()) {
            context.method_25294(x, y, x + w, y + h, 0x40000000);
            return;
        }
        if (ScreenStyle.home()) {
            context.method_25296(x, y, x + w, y + h, 0x1A000000, 0x10000000);
            return;
        }
        context.method_25296(x, y, x + w, y + h, 0x38000000, 0x28000000);
    }

    public static void separators(class_332 context, int x, int top, int bottom, int w) {
        if (ScreenStyle.vanillaPlus()) {
            context.method_25294(x, top - 1, x + w, top, 0x30FFFFFF);
            context.method_25294(x, bottom, x + w, bottom + 1, 0x30FFFFFF);
            return;
        }
        int quarter = w / 4;
        context.method_25294(x + quarter, top - 1, x + w - quarter, top, ScreenStyle.alpha(14014942, 0.3f));
        context.method_25294(x, top - 1, x + quarter, top, ScreenStyle.alpha(14014942, 0.1f));
        context.method_25294(x + w - quarter, top - 1, x + w, top, ScreenStyle.alpha(14014942, 0.1f));
        context.method_25294(x + quarter, bottom, x + w - quarter, bottom + 1, ScreenStyle.alpha(14014942, 0.3f));
        context.method_25294(x, bottom, x + quarter, bottom + 1, ScreenStyle.alpha(14014942, 0.1f));
        context.method_25294(x + w - quarter, bottom, x + w, bottom + 1, ScreenStyle.alpha(14014942, 0.1f));
    }

    public static void selection(class_332 context, int x, int y, int w, int h, boolean focused) {
        if (ScreenStyle.vanillaPlus()) {
            Smooth.rect(context, x, y, w, h, 3, ScreenStyle.alpha(0xFFFFFF, focused ? 0.1f : 0.06f));
            Smooth.outline(context, x, y, w, h, 3, ScreenStyle.alpha(0xFFFFFF, focused ? 0.85f : 0.45f));
            return;
        }
        if (ScreenStyle.soberStyle()) {
            ScreenStyle.crispPanel(context, x, y, w, h, 4.0f, ScreenStyle.alpha(0xFFFFFF, focused ? 0.08f : 0.05f), ScreenStyle.alpha(0xFFFFFF, focused ? 0.55f : 0.3f));
            return;
        }
        if (ScreenStyle.home()) {
            int accent = ScreenStyle.accent();
            Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)w * 0.45f), ScreenStyle.alpha(accent, focused ? 0.12f : 0.07f));
            Smooth.rect(context, x, y, w, h, 10, ScreenStyle.alpha(0xFFFFFF, focused ? 0.12f : 0.08f));
            Smooth.outline(context, x, y, w, h, 10, ScreenStyle.alpha(accent, focused ? 0.7f : 0.4f));
            return;
        }
        Smooth.rect(context, x, y, w, h, 6, ScreenStyle.alpha(3817287, focused ? 0.85f : 0.6f));
        Smooth.outline(context, x, y, w, h, 6, ScreenStyle.alpha(14014942, focused ? 0.45f : 0.2f));
    }

    public static void scrollTrack(class_332 context, int x, int y, int w, int h) {
        Smooth.rect(context, x + w / 2 - 1, y, 2, h, 1, 0x14FFFFFF);
    }

    public static void scrollThumb(class_332 context, int x, int y, int w, int h) {
        if (ScreenStyle.vanillaPlus()) {
            Smooth.rect(context, x + 1, y, Math.max(2, w - 2), h, 2, -1711276033);
            return;
        }
        Smooth.rect(context, x + 1, y, Math.max(2, w - 2), h, 2, ScreenStyle.alpha(14014942, 0.7f));
    }

    public record Run(String text, int color) {
    }
}

