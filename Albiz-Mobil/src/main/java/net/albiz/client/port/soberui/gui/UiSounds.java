// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.ModuleMenu;
import net.albiz.client.port.soberui.modules.ClickGui;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_437;

public final class UiSounds {
    private static long lastHover;
    private static int burst;
    private static long lastPlayed;

    private UiSounds() {
    }

    private static ClickGui gui() {
        return SoberClient.MODULES.get(ClickGui.class);
    }

    public static boolean playedWithin(long millis) {
        return System.currentTimeMillis() - lastPlayed < millis;
    }

    private static boolean silk() {
        return gui()!=null && gui().soundPack.get() == ClickGui.SoundPack.Silk;
    }

    private static boolean pulse() {
        return gui()!=null && (gui().soundPack.get() == ClickGui.SoundPack.Pulse || UiSounds.glide());
    }

    private static boolean glide() {
        return gui()!=null && gui().soundPack.get() == ClickGui.SoundPack.Glide;
    }

    private static void play(class_3414 sound, float pitch, float volume) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        var feedback=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.client.SoundFeedbackModule.class);if(feedback!=null&&!feedback.isEnabled())return;
        class_310 mc = class_310.method_1551();
        float master = (float)UiSounds.gui().volume.get().intValue() / 100.0f;
        if (master <= 0.0f || mc.method_1483() == null) {
            return;
        }
        mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)sound, (float)pitch, (float)(volume * master)));
    }

    private static void silk(String name, float pitch, float volume) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        class_310 mc = class_310.method_1551();
        float master = (float)UiSounds.gui().volume.get().intValue() / 100.0f;
        if (master <= 0.0f || mc.method_1483() == null) {
            return;
        }
        class_2960 id = class_2960.method_60655((String)"albiz", (String)("ui." + name));
        mc.method_1483().method_4873((class_1113)new class_1109(id, class_3419.field_61058, volume * master, pitch, class_1113.method_43221(), false, 0, class_1113.class_1114.field_5478, 0.0, 0.0, 0.0, true));
    }

    private static float jitter(float amount) {
        return 1.0f + (float)(Math.random() * (double)amount * 2.0 - (double)amount);
    }

    public static void open() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        if (!UiSounds.gui().openSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk("glide_open", 1.0f, 0.45f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_open", 1.0f, 0.3f);
        } else if (UiSounds.silk()) {
            UiSounds.silk("silk_open", 1.0f, 0.55f);
        } else {
            UiSounds.play(class_3417.field_26980, 1.35f, 0.8f);
        }
    }

    public static void close() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        if (!UiSounds.gui().openSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk("glide_close", 1.0f, 0.45f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_close", 1.0f, 0.3f);
        } else if (UiSounds.silk()) {
            UiSounds.silk("silk_close", 1.0f, 0.45f);
        } else {
            UiSounds.play(class_3417.field_26980, 0.95f, 0.6f);
        }
    }

    public static void hover() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        long now = System.currentTimeMillis();
        if (!UiSounds.gui().hoverSound.get().booleanValue()) {
            return;
        }
        if (now - lastHover > 400L) {
            burst = 0;
        }
        if (now - lastHover < 90L || burst >= 4) {
            lastHover = now;
            return;
        }
        lastHover = now;
        ++burst;
        UiSounds.hoverSound();
    }

    public static void previewHover() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        class_437 screen = class_310.method_1551().field_1755;
        if (!(screen instanceof ModuleMenu)) {
            return;
        }
        lastHover = System.currentTimeMillis();
        UiSounds.hoverSound();
    }

    private static void hoverSound() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        float jitter = (float)(Math.random() * 0.08 - 0.04);
        switch (UiSounds.gui().hoverStyle.get()) {
            case Glide: {
                UiSounds.silk("glide_hover", UiSounds.jitter(0.02f), 0.2f);
                break;
            }
            case Silk: {
                UiSounds.silk("silk_hover", UiSounds.jitter(0.03f), 0.35f);
                break;
            }
            case Bubble: {
                UiSounds.play(class_3417.field_54891, 1.45f + jitter, 0.45f);
                break;
            }
            case Crystal: {
                UiSounds.play(class_3417.field_26982, 1.7f + jitter, 0.3f);
                break;
            }
            case Wood: {
                UiSounds.play(class_3417.field_40067, 1.75f + jitter, 0.22f);
                break;
            }
            case Tick: {
                UiSounds.play((class_3414)class_3417.field_15204.comp_349(), 1.8f + jitter, 0.18f);
                break;
            }
            case Classic: {
                UiSounds.play((class_3414)class_3417.field_15015.comp_349(), 1.9f + jitter * 1.5f, 0.2f);
            }
        }
    }

    public static void key(float volume) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        if (UiSounds.glide()) {
            UiSounds.silk("glide_type", UiSounds.jitter(0.02f), 0.22f * volume + 0.1f);
            return;
        }
        if (UiSounds.silk() || UiSounds.pulse()) {
            UiSounds.silk("silk_type", UiSounds.jitter(0.025f), 0.5f * volume + 0.15f);
            return;
        }
        float pitch = 1.9f + (float)(Math.random() * 0.12 - 0.06);
        UiSounds.play((class_3414)class_3417.field_15015.comp_349(), pitch, 0.2f * volume);
    }

    public static void click() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        lastPlayed = System.currentTimeMillis();
        if (!UiSounds.gui().clickSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk("glide_click", 1.0f, 0.4f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_swish", 1.0f, 1.0f);
        } else if (UiSounds.silk()) {
            UiSounds.silk("silk_nav", 1.0f, 0.55f);
        } else {
            UiSounds.play((class_3414)class_3417.field_15015.comp_349(), 1.6f, 0.25f);
        }
    }

    public static void select() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        lastPlayed = System.currentTimeMillis();
        if (!UiSounds.gui().clickSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk("glide_click", 1.19f, 0.4f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_swish", 1.06f, 1.0f);
        } else if (UiSounds.silk()) {
            UiSounds.silk("silk_select", 1.0f, 0.5f);
        } else {
            UiSounds.play((class_3414)class_3417.field_15015.comp_349(), 1.75f, 0.25f);
        }
    }

    public static void keybind(boolean on) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        lastPlayed = System.currentTimeMillis();
        if (!UiSounds.gui().hoverSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk("glide_click", on ? 1.12f : 0.94f, 0.35f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_swish", on ? 1.05f : 0.95f, 0.9f);
        } else if (UiSounds.silk()) {
            UiSounds.silk("silk_select", on ? 1.06f : 0.94f, 0.45f);
        } else {
            UiSounds.play((class_3414)class_3417.field_15015.comp_349(), on ? 1.9f : 1.75f, 0.2f);
        }
    }

    public static void moduleToggle(boolean enabled) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return; /* Canonical SoundFeedbackModule listens to real module toggle events. */ }

    public static void intro() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        UiSounds.play(class_3417.field_14703, 1.5f, 0.7f);
    }

    public static void reveal() {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        UiSounds.play(class_3417.field_26980, 1.2f, 0.9f);
    }

    public static void toggle(boolean on) {
        if (!net.albiz.client.port.soberui.SoberUi.isReady() || gui() == null) return;
        lastPlayed = System.currentTimeMillis();
        if (!UiSounds.gui().clickSound.get().booleanValue()) {
            return;
        }
        if (UiSounds.glide()) {
            UiSounds.silk(on ? "glide_check_on" : "glide_check_off", 1.0f, 0.45f);
        } else if (UiSounds.pulse()) {
            UiSounds.silk("pulse_check", on ? 1.04f : 0.94f, 0.6f);
        } else if (UiSounds.silk()) {
            UiSounds.silk(on ? "silk_on" : "silk_off", 1.0f, 0.5f);
        } else {
            UiSounds.play((class_3414)class_3417.field_14622.comp_349(), on ? 1.7f : 1.2f, 0.3f);
        }
    }
}

