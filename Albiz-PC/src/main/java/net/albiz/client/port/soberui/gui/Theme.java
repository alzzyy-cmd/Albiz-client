// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Fonts;
import net.albiz.client.port.soberui.gui.SdfText;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.albiz.client.port.soberui.render.Color;
import java.util.Locale;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_342;
import net.minecraft.class_5223;
import net.minecraft.class_5224;

public final class Theme {
    private static final Color[] GALAXY = new Color[]{new Color(120, 60, 255, 255), new Color(60, 140, 255, 255), new Color(220, 80, 220, 255), new Color(90, 40, 200, 255)};
    private static final int DEFAULT_PANEL = 1777187;
    private static final int DEFAULT_OPACITY = 88;
    private static ClickGui gui;

    private Theme() {
    }

    private static ClickGui gui() {
        if (gui == null) {
            gui = SoberClient.MODULES.get(ClickGui.class);
        }
        return gui;
    }

    public static int accent() {
        return Theme.accent(0.0);
    }

    public static int accent(double offset) {
        ClickGui gui = Theme.gui();
        double speed = (double)gui.accentSpeed.get().intValue() * 0.04;
        return Theme.neon(switch (gui.accentMode.get()) {
            default -> throw new MatchException(null, null);
            case ClickGui.AccentMode.Static -> 0xFF000000 | gui.accent.get().packed();
            case ClickGui.AccentMode.Rainbow -> Color.rainbow(offset, speed, 0.7f, 1.0f, 255).packed();
            case ClickGui.AccentMode.Galaxy -> Theme.galaxy((double)System.currentTimeMillis() / 1000.0 * speed + offset).packed();
        });
    }

    public static int neon(int color) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        if (max <= 0.001f || max - min <= 0.001f) {
            return color;
        }
        float saturation = (max - min) / max;
        if (saturation < 0.12f) {
            return color;
        }
        float value = Math.max(max, 0.97f);
        float vivid = Math.min(saturation, Math.max(0.3f, Math.min(0.62f, saturation * 0.62f + 0.08f)));
        int nr = Math.round(value * (1.0f - vivid * (1.0f - (r - min) / (max - min))) * 255.0f);
        int ng = Math.round(value * (1.0f - vivid * (1.0f - (g - min) / (max - min))) * 255.0f);
        int nb = Math.round(value * (1.0f - vivid * (1.0f - (b - min) / (max - min))) * 255.0f);
        return color & 0xFF000000 | nr << 16 | ng << 8 | nb;
    }

    public static Color galaxy(double t) {
        double scaled = (t % 1.0 + 1.0) % 1.0 * (double)GALAXY.length;
        int index = (int)Math.floor(scaled);
        double f = scaled - (double)index;
        f = f * f * (3.0 - 2.0 * f);
        return GALAXY[index % GALAXY.length].lerp(GALAXY[(index + 1) % GALAXY.length], f);
    }

    public static int panel() {
        ClickGui gui = Theme.gui();
        int alpha = Math.round((float)gui.panelOpacity.get().intValue() / 100.0f * 255.0f);
        return alpha << 24 | gui.panelColor.get().packed() & 0xFFFFFF;
    }

    public static boolean liquidGlass() {
        ClickGui gui = Theme.gui();
        return gui != null && gui.format.get() == ClickGui.Format.LiquidGlass;
    }

    public static boolean shimmer() {
        ClickGui gui = Theme.gui();
        return gui != null && gui.format.get() == ClickGui.Format.LiquidGlass && gui.shimmer.get() != false;
    }

    public static int glassTint() {
        ClickGui gui = Theme.gui();
        float amount = gui == null ? 0.0f : (float)gui.glassTint.get().intValue() / 100.0f * 0.6f;
        return Theme.mix(724241, Theme.accent(), amount);
    }

    public static int glassRim() {
        ClickGui gui = Theme.gui();
        float amount = gui == null ? 0.0f : Math.min(1.0f, (float)gui.glassTint.get().intValue() / 100.0f + 0.15f) * 0.5f;
        return Theme.mix(0xFFFFFF, Theme.accent(), amount);
    }

    private static int mix(int from, int to, float amount) {
        int r = Math.round((float)(from >> 16 & 0xFF) + (float)((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * amount);
        int g = Math.round((float)(from >> 8 & 0xFF) + (float)((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * amount);
        int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * amount);
        return r << 16 | g << 8 | b;
    }

    public static int panelSolid(int alpha) {
        return (alpha & 0xFF) << 24 | Theme.gui().panelColor.get().packed() & 0xFFFFFF;
    }

    public static int text() {
        return 0xFF000000 | Theme.gui().textColor.get().packed();
    }

    public static int fill(int base) {
        int start = base >>> 24;
        int opacity = Theme.gui().panelOpacity.get();
        int alpha = opacity <= 88 ? Math.round((float)(start * opacity) / 88.0f) : Math.round((float)start + (float)((255 - start) * (opacity - 88)) / 12.0f);
        return Math.max(0, Math.min(255, alpha)) << 24 | Theme.tint(base) & 0xFFFFFF;
    }

    public static int opacity(int base) {
        ClickGui gui = Theme.gui();
        if (gui == null) {
            return base;
        }
        int start = base >>> 24;
        int opacity = gui.panelOpacity.get();
        int alpha = opacity <= 88 ? Math.round((float)(start * opacity) / 88.0f) : Math.round((float)start + (float)((255 - start) * (opacity - 88)) / 12.0f);
        return Math.max(0, Math.min(255, alpha)) << 24 | base & 0xFFFFFF;
    }

    public static int tint(int base) {
        int panel = Theme.gui().panelColor.get().packed();
        int r = Theme.shift(base >> 16, panel >> 16, 27);
        int g = Theme.shift(base >> 8, panel >> 8, 6942);
        int b = Theme.shift(base, panel, 1777187);
        return base & 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int shift(int base, int panel, int reference) {
        return Math.max(0, Math.min(255, (base & 0xFF) + (panel & 0xFF) - (reference & 0xFF)));
    }

    public static int textTint(int base) {
        int text = Theme.gui().textColor.get().packed();
        int r = (base >> 16 & 0xFF) * (text >> 16 & 0xFF) / 255;
        int g = (base >> 8 & 0xFF) * (text >> 8 & 0xFF) / 255;
        int b = (base & 0xFF) * (text & 0xFF) / 255;
        return base & 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static float animationSpeed() {
        return (float)Theme.gui().animationSpeed.get().intValue() / 100.0f;
    }

    private static class_2583 styleFor(Fonts.Family family, Fonts.Weight weight) {
        return switch (family) {
            case Fonts.Family.Minecraft -> {
                if (weight.ordinal() >= Fonts.Weight.Bold.ordinal()) {
                    yield class_2583.field_24360.method_27704((class_11719)Fonts.VANILLA).method_10982(Boolean.valueOf(true));
                }
                yield class_2583.field_24360.method_27704((class_11719)Fonts.VANILLA);
            }
            case Fonts.Family.Unifont -> class_2583.field_24360.method_27704((class_11719)Fonts.UNIFONT);
            default -> class_2583.field_24360.method_27704((class_11719)family.font(weight));
        };
    }

    public static class_2583 style() {
        return Theme.styleFor(Theme.gui().font.get(), Theme.gui().fontWeight.get());
    }

    public static class_11719 gameFont(class_11719 source) {
        if (source != class_11719.field_61972 && !class_11719.field_61972.equals((Object)source)) {
            return source;
        }
        ClickGui gui = Theme.gui();
        if (gui == null) {
            return source;
        }
        Fonts.Family family = gui.gameFont.get();
        if (family == Fonts.Family.Minecraft) {
            return source;
        }
        if (family == Fonts.Family.Unifont) {
            return Fonts.UNIFONT;
        }
        return family.font(gui.gameWeight.get());
    }

    private static Fonts.Family cleanFamily() {
        ClickGui gui = Theme.gui();
        Fonts.Family family = gui == null ? Fonts.Family.Ubuntu : gui.font.get();
        return family.custom() ? family : Fonts.Family.Ubuntu;
    }

    public static class_2583 crispStyle() {
        return class_2583.field_24360.method_27704((class_11719)Theme.cleanFamily().large(Fonts.Weight.Medium));
    }

    public static class_342.class_11734 fieldFormatter() {
        return (value, first) -> visitor -> class_5223.method_27479((String)value, (class_2583)Theme.crispStyle(), (class_5224)visitor);
    }

    public static String crispFile(boolean heavy) {
        ClickGui gui = Theme.gui();
        Fonts.Weight weight = gui == null ? Fonts.Weight.Medium : gui.fontWeight.get();
        return Theme.cleanFamily().file(heavy ? weight.heavier() : weight);
    }

    public static float crispScale() {
        return Theme.cleanFamily().scale();
    }

    public static boolean menuCrisp() {
        ClickGui gui = Theme.gui();
        return gui != null && gui.font.get().custom();
    }

    public static class_2561 text(String value) {
        return class_2561.method_43470((String)value).method_10862(Theme.style());
    }

    public static boolean smoothFont(String value) {
        ClickGui gui = Theme.gui();
        return (gui == null || gui.font.get() == Fonts.Family.Xuong) && SdfText.supports(value);
    }

    public static class_2561 bold(String value) {
        Fonts.Family family = Theme.gui().font.get();
        return class_2561.method_43470((String)value).method_10862(Theme.styleFor(family, family == Fonts.Family.Minecraft ? Fonts.Weight.Bold : Theme.gui().fontWeight.get().heavier()));
    }

    public static class_2561 large(String value) {
        Fonts.Family family = Theme.gui().font.get();
        if (!family.custom()) {
            return Theme.bold(value);
        }
        return class_2561.method_43470((String)value).method_10862(class_2583.field_24360.method_27704((class_11719)family.large(Theme.gui().fontWeight.get().heavier())));
    }

    public static String label(String value) {
        final String input=value;
        if(net.albiz.client.module.ModuleRegistry.all().stream().noneMatch(m->m.name().equals(input)) && SoberClient.MODULES.all().stream().noneMatch(m->m.name.equals(input)))value=net.albiz.client.i18n.AlbizI18n.display(input);
        return Theme.gui().uppercase.get() != false ? value.toUpperCase(Locale.ROOT) : value;
    }
}

