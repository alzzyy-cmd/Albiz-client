// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.HudPalette;
import net.albiz.client.port.soberui.gui.HudRenderer;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.modules.Hud;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_5348;
import net.minecraft.class_5481;

public final class Notifications {
    private static final int WIDTH = 220;
    private static final int MAX_VISIBLE = 4;
    private static final float ENTER = 0.45f;
    private static final float EXIT = 0.45f;
    private static final List<Entry> ENTRIES = new ArrayList<Entry>();
    private static long lastSound;
    private static long lastFrame;

    private Notifications() {
    }

    public static void push(String title, String message, Type type) {
        class_310 mc = class_310.method_1551();
        mc.execute(() -> {
            long now;
            boolean quiet;
            Hud hud = SoberClient.MODULES.get(Hud.class);
            float duration = hud != null ? (float)hud.notificationTime.get().intValue() / 10.0f : 4.5f;
            for (Entry entry : ENTRIES) {
                if (!entry.title.equals(title) || !entry.message.equals(message) || !(entry.age < duration - 0.45f)) continue;
                entry.age = Math.min(entry.age, 0.45f);
                ++entry.count;
                return;
            }
            ENTRIES.add(0, new Entry(title, message, type, duration));
            if (ENTRIES.size() > 12) {
                ENTRIES.remove(ENTRIES.size() - 1);
            }
            boolean bl = quiet = (now = System.currentTimeMillis()) - lastSound < 350L || UiSounds.playedWithin(400L);
            if (hud != null && hud.notificationSound.get().booleanValue() && mc.method_1483() != null && !quiet) {
                lastSound = now;
                Hud.SoundType sound = hud.soundType.get();
                float volume = (float)hud.notificationVolume.get().intValue() / 100.0f * sound.volume;
                float shift = switch (type.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 3 -> 0.8f;
                    case 2 -> 0.9f;
                    case 0, 1 -> 1.0f;
                };
                mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)sound.event(), (float)(sound.pitch * shift), (float)volume));
            }
        });
    }

    private static float easeOutCubic(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    private static float easeInCubic(float t) {
        return t * t * t;
    }

    private static int withAlpha(int color, float alpha) {
        int a = Math.round((float)(color >>> 24) * Math.max(0.0f, Math.min(1.0f, alpha)));
        return a << 24 | color & 0xFFFFFF;
    }

    public static void render(class_332 context) {
        Notifications.render(context, false);
    }

    public static void render(class_332 context, boolean bottomRight) {
        long now = System.nanoTime();
        float delta = Math.min(0.05f, (float)(now - lastFrame) / 1.0E9f);
        lastFrame = now;
        if (ENTRIES.isEmpty()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        class_327 font = mc.field_1772;
        float scale = HudRenderer.uiScale(context);
        int screenWidth = Math.round((float)context.method_51421() / scale);
        int screenHeight = Math.round((float)context.method_51443() / scale);
        context.method_51448().pushMatrix();
        context.method_51448().scale(scale, scale);
        float y = bottomRight ? 6.0f : (float)HudRenderer.topClearance((screenWidth - 220) / 2, (screenWidth + 220) / 2);
        int shown = 0;
        Iterator<Entry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            int barColor;
            Entry entry = it.next();
            entry.age += delta;
            if (entry.age >= entry.duration) {
                it.remove();
                continue;
            }
            if (shown >= 4) continue;
            ++shown;
            float in = Notifications.easeOutCubic(Math.min(1.0f, entry.age / 0.45f));
            float out = Notifications.easeInCubic(Math.max(0.0f, (entry.age - (entry.duration - 0.45f)) / 0.45f));
            float visible = in * (1.0f - out);
            List lines = font.method_1728((class_5348)Theme.text(entry.message), 178);
            if (lines.size() > 3) {
                lines = lines.subList(0, 3);
            }
            int height = 22 + lines.size() * 10;
            entry.shownY += (y - entry.shownY) * (1.0f - (float)Math.exp(-14.0f * delta));
            if (entry.age < 0.05f) {
                entry.shownY = y;
            }
            int x = bottomRight ? screenWidth - 220 - 6 : (screenWidth - 220) / 2;
            float top = bottomRight ? (float)screenHeight - entry.shownY - (float)height + (1.0f - in) * 14.0f + out * 10.0f : entry.shownY - (1.0f - in) * 14.0f - out * 10.0f;
            context.method_51448().pushMatrix();
            context.method_51448().translate(0.0f, top);
            int iy = 0;
            int n = barColor = entry.type == Type.INFO ? 0xF4F4F5 : entry.type.color;
            if (Theme.liquidGlass()) {
                HudRenderer.glass(context, x, iy, 220, height, 12, visible);
            } else {
                Smooth.rect(context, x, iy + 2, 220, height, 4, Notifications.withAlpha(0x33000000, visible));
                Smooth.rect(context, x, iy, 220, height, 3, Notifications.withAlpha(HudPalette.card(), visible));
                Smooth.outline(context, x, iy, 220, height, 3, Notifications.withAlpha(-13750733, visible));
            }
            Smooth.rect(context, x + 1, iy + 4, 2, height - 8, 1, Notifications.withAlpha(0xFF000000 | barColor, visible));
            float logoHeight = Math.min((float)height - 10.0f, 21.0f / HudRenderer.logoWidth(1.0f));
            HudRenderer.logo(context, (float)x + 17.0f - HudRenderer.logoWidth(logoHeight) / 2.0f, (float)iy + ((float)height - logoHeight) / 2.0f, logoHeight, Notifications.withAlpha(-1, visible));
            HudRenderer.label(context, entry.title.toUpperCase(Locale.ROOT), x + 33, (float)iy + 5.6f, Notifications.withAlpha(-2565922, visible));
            if (entry.count > 1) {
                String count = "x" + entry.count;
                HudRenderer.label(context, count, (float)(x + 220 - 9) - HudRenderer.labelWidth(count), (float)iy + 5.6f, Notifications.withAlpha(-5329226, visible));
            }
            int ly = iy + 19;
            for (class_5481 line : (java.util.List<class_5481>)lines) {
                context.method_51430(font, line, x + 33, ly, Notifications.withAlpha(-723723, visible), false);
                ly += 10;
            }
            float remaining = 1.0f - Math.min(1.0f, entry.age / entry.duration);
            int barWidth = Math.round(204.0f * remaining);
            context.method_25294(x + 8, iy + height - 3, x + 220 - 8, iy + height - 2, Notifications.withAlpha(653587701, visible));
            if (barWidth > 0) {
                context.method_25294(x + 8, iy + height - 3, x + 8 + barWidth, iy + height - 2, Notifications.withAlpha(0xFF000000 | barColor, visible));
            }
            context.method_51448().popMatrix();
            y += (float)(height + 5) * visible;
        }
        context.method_51448().popMatrix();
    }

    static {
        lastFrame = System.nanoTime();
    }

    public static enum Type {
        INFO(0),
        SUCCESS(-11746422),
        WARNING(-1525686),
        ERROR(-2075307);

        final int color;

        private Type(int color) {
            this.color = color;
        }
    }

    private static final class Entry {
        final String title;
        final String message;
        final Type type;
        final float duration;
        float age;
        float shownY = 6.0f;
        int count = 1;

        Entry(String title, String message, Type type, float duration) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.duration = duration;
        }
    }
}

