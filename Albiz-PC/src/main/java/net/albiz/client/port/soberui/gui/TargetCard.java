// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.fx.CombatFx;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.gui.WorldLabel;
import net.albiz.client.port.soberui.modules.TargetHud;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_4184;
import net.minecraft.class_742;
import net.minecraft.class_7532;
import net.minecraft.class_7923;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class TargetCard {
    private static final int W = 158;
    private static final int H = 62;
    private static final String FONT = "geist-semibold.ttf";
    private static final int CARD = -351005672;
    private static final int EDGE = 0x1FFFFFFF;
    private static final int TEXT = -723723;
    private static final int MUTED = -6645082;
    private static final int FAINT = -9539974;
    private static final int VIOLET = -7643914;
    private static final int MAGENTA = -2537745;
    private static final int LILAC = -2575106;
    private static final int PILL = -532996754;
    private static final int PILL_TEXT = -1319425;
    private static final int HEART = -757066;
    private static final int DAMAGE = -298619;
    private static final int MINT = -5770288;
    private static final int AMBER = -208051;
    private static final int STAT = 0x12FFFFFF;
    private static final int TRACK = 0x2EFFFFFF;
    private static final int FLAME = -290244;
    private static final int SWORDS = -4160260;
    private static final int AHEAD = -13315175;
    private static final int BEHIND = -757066;
    private static final int GONE_CARD = -349893600;
    private static final int GONE_EDGE = 1727296182;
    private static final int GONE_TEXT = -415532;
    private static float appear;
    private static float seen;
    private static float side;
    private static float bar;
    private static float ghost;
    private static float scale;
    private static long last;
    private static long ghostHold;
    private static class_1309 shownFor;
    private static int shownId;

    private TargetCard() {
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int fade(int color, float alpha) {
        return MenuKit.alpha(color, alpha);
    }

    private static void text(class_332 context, String value, float x, float top, float em, int color) {
        if (CrispText.canDisplay(value, FONT)) {
            CrispText.draw(context, value, FONT, x, top + em * 0.866f, em, 0.1f, color);
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, top);
        matrices.scale(em / 9.0f, em / 9.0f);
        context.method_51433(class_310.method_1551().field_1772, value, 0, 0, color, false);
        matrices.popMatrix();
    }

    private static float width(String value, float em) {
        if (CrispText.canDisplay(value, FONT)) {
            return CrispText.width(value, FONT, em, 0.1f);
        }
        return (float)class_310.method_1551().field_1772.method_1727(value) * em / 9.0f;
    }

    private static void label(class_332 context, String value, float x, float top, float em, int color) {
        CrispText.draw(context, value, FONT, x, top + em * 0.866f, em, 0.16f, color);
    }

    private static float labelWidth(String value, float em) {
        return CrispText.width(value, FONT, em, 0.16f);
    }

    private static String fit(String value, float room, float em) {
        if (TargetCard.width(value, em) <= room) {
            return value;
        }
        String cut = value;
        while (cut.length() > 1 && TargetCard.width(cut + "..", em) > room) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "..";
    }

    static String number(float value) {
        float rounded = (float)Math.round(value * 10.0f) / 10.0f;
        if (Math.abs(rounded - (float)Math.round(rounded)) < 0.05f) {
            return String.valueOf(Math.round(rounded));
        }
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(rounded));
    }

    public static boolean showing(int entity) {
        return shownId == entity && appear * seen > 0.05f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 context, float tickDelta) {
        class_310 mc = class_310.method_1551();
        long now = System.nanoTime();
        float delta = last == 0L ? 0.0f : Math.min(0.1f, (float)(now - last) / 1.0E9f);
        last = now;
        try {
            TargetCard.card(context, mc, tickDelta, delta);
        }
        finally {
            TargetCard.shields(context, mc, tickDelta);
        }
    }

    private static void card(class_332 context, class_310 mc, float tickDelta, float delta) {
        boolean flip;
        boolean fighting;
        class_1309 target;
        TargetHud module = TargetHud.active();
        class_1309 class_13092 = target = module == null ? null : module.target();
        if (target == null || mc.field_1724 == null || mc.field_1687 == null || mc.field_1690.field_1842) {
            appear = 0.0f;
            shownFor = null;
            shownId = -1;
            return;
        }
        if (target != shownFor) {
            shownFor = target;
            shownId = target.method_5628();
            ghost = -1.0f;
            bar = -1.0f;
        }
        appear = MenuKit.approach(appear, (fighting = module.fighting()) ? 1.0f : 0.0f, fighting ? 11.0f : 6.0f, delta);
        if (!fighting && appear <= 0.004f) {
            appear = 0.0f;
            module.release();
            shownFor = null;
            shownId = -1;
            return;
        }
        float sw = context.method_51421();
        float sh = context.method_51443();
        class_243 base = target.method_30950(tickDelta);
        class_243 head = base.method_1031(0.0, (double)target.method_17682() * 0.8, 0.0);
        float[] anchor = WorldLabel.project(mc, head, sw, sh);
        seen = MenuKit.approach(seen, anchor != null ? 1.0f : 0.0f, anchor != null ? 12.0f : 18.0f, delta);
        if (anchor == null) {
            return;
        }
        class_4184 camera = mc.field_1773.method_19418();
        class_243 right = class_243.method_1030((float)0.0f, (float)(camera.method_19330() + 90.0f)).method_1021((double)target.method_17681() * 0.5 + 0.3);
        float[] edge = WorldLabel.project(mc, head.method_1019(right), sw, sh);
        float reach = edge == null ? 30.0f : Math.abs(edge[0] - anchor[0]);
        double distance = camera.method_71156().method_1022(head);
        float wanted = (float)module.size.get().intValue() / 100.0f * Math.max(0.6f, Math.min(1.0f, (float)(1.08 - distance * 0.035)));
        scale = bar < 0.0f ? wanted : MenuKit.approach(scale, wanted, 10.0f, delta);
        float s = scale * (0.94f + 0.06f * MenuKit.quint(appear));
        float width = 158.0f * s;
        float height = 62.0f * s;
        float rightX = anchor[0] + reach + 4.0f;
        float leftX = anchor[0] - reach - 4.0f - width;
        boolean bl = flip = rightX + width > sw - 6.0f && leftX > 6.0f;
        side = bar < 0.0f ? (flip ? 1.0f : 0.0f) : MenuKit.approach(side, flip ? 1.0f : 0.0f, 9.0f, delta);
        float x = rightX + (leftX - rightX) * side;
        float y = Math.max(4.0f, Math.min(sh - height - 22.0f, anchor[1] - height * 0.5f)) + (1.0f - appear) * 5.0f;
        float alpha = TargetCard.clamp(appear * seen);
        if (alpha <= 0.01f) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(s, s);
        TargetCard.body(context, mc, module, target, alpha, delta);
        float remaining = CombatFx.shieldRemaining(target.method_5628());
        if (remaining > -0.5f) {
            float pillAlpha = alpha * TargetCard.clamp((remaining + 0.5f) / 0.5f);
            float pw = TargetCard.shieldWidth(remaining);
            TargetCard.shieldPill(context, 151.0f - pw, 66.0f, remaining, pillAlpha);
        }
        matrices.popMatrix();
    }

    private static void body(class_332 context, class_310 mc, TargetHud module, class_1309 target, float alpha, float delta) {
        String value;
        int tone;
        Icons.Icon trend;
        long nowMs = System.currentTimeMillis();
        boolean gone = module.eliminated();
        float goneMix = gone ? TargetCard.clamp((float)(nowMs - module.eliminatedAt()) / 350.0f) : 0.0f;
        Smooth.rect(context, 0, 0, 158, 62, 8, TargetCard.fade(MenuKit.lerp(-351005672, -349893600, goneMix), alpha));
        Smooth.outline(context, 0, 0, 158, 62, 8, TargetCard.fade(MenuKit.lerp(0x1FFFFFFF, 1727296182, goneMix), alpha));
        if (gone) {
            Smooth.rect(context, 7, 7, 22, 22, 5, TargetCard.fade(-12905168, alpha));
            Icons.draw(context, Icons.Icon.SKULL, 10, 10, 16, TargetCard.fade(-415532, alpha * goneMix + alpha * (1.0f - goneMix) * 0.4f));
        } else if (target instanceof class_742) {
            class_742 player = (class_742)target;
            class_7532.method_44443((class_332)context, (class_8685)player.method_52814(), (int)7, (int)7, (int)22, (int)TargetCard.fade(-1, alpha));
            Smooth.outline(context, 6, 6, 24, 24, 4, TargetCard.fade(0x30FFFFFF, alpha));
        } else {
            Smooth.rect(context, 7, 7, 22, 22, 5, TargetCard.fade(-14277076, alpha));
            Icons.draw(context, Icons.Icon.PERSON, 11, 11, 14, TargetCard.fade(-6645082, alpha));
        }
        String name = target.method_5477().getString();
        String weapon = TargetCard.weapon(target.method_6047());
        float pillW = 15.0f + TargetCard.labelWidth(weapon, 6.2f);
        float nameRoom = 116.0f - pillW - 6.0f;
        String shownName = TargetCard.fit(name, nameRoom, 8.6f);
        int nameColor = gone ? TargetCard.fade(MenuKit.lerp(-723723, -6645082, goneMix), alpha) : TargetCard.fade(-723723, alpha);
        TargetCard.text(context, shownName, 35.0f, 7.0f, 8.6f, nameColor);
        float nameW = TargetCard.width(shownName, 8.6f);
        if (gone) {
            context.method_25294(35, 12, Math.round(35.0f + nameW * goneMix), 13, TargetCard.fade(-415532, alpha * 0.85f));
        }
        float since = (float)(nowMs - module.damageAt()) / 1000.0f;
        if (module.damage() > 0.04f && since < 1.4f) {
            float pop = since < 0.12f ? since / 0.12f : 1.0f;
            float out = since > 0.9f ? 1.0f - (since - 0.9f) / 0.5f : 1.0f;
            float vx = 35.0f + nameW + 4.0f;
            String value2 = "-" + TargetCard.number(module.damage());
            if (vx + TargetCard.width(value2, 7.6f) < 148.0f - pillW) {
                TargetCard.text(context, value2, vx, 7.6f - pop * 1.4f + (1.0f - pop) * 2.0f, 7.6f, TargetCard.fade(-298619, alpha * TargetCard.clamp(pop) * TargetCard.clamp(out)));
            }
        }
        float hp = Math.max(0.0f, target.method_6032());
        float max = Math.max(1.0f, target.method_6063());
        Icons.draw(context, Icons.Icon.HEART, 35, 20, 8, TargetCard.fade(gone ? -9539974 : -757066, alpha));
        TargetCard.text(context, TargetCard.number(hp), 46.0f, 19.4f, 8.6f, TargetCard.fade(-723723, alpha));
        int px = Math.round(151.0f - pillW);
        Smooth.rect(context, px, 7, Math.round(pillW), 11, 5, TargetCard.fade(-532996754, alpha));
        Icons.draw(context, Icons.Icon.SWORD, px + 4, 9, 7, TargetCard.fade(-2575106, alpha));
        TargetCard.label(context, weapon, (float)px + 12.5f, 9.2f, 6.2f, TargetCard.fade(-1319425, alpha));
        String distance = TargetCard.number(mc.field_1724.method_5739((class_1297)target)) + " m";
        TargetCard.text(context, distance, 151.0f - TargetCard.width(distance, 7.6f), 20.4f, 7.6f, TargetCard.fade(-6645082, alpha));
        float fraction = TargetCard.clamp(hp / max);
        if (bar < 0.0f) {
            bar = ghost = fraction;
        }
        bar = MenuKit.approach(bar, fraction, 14.0f, delta);
        if (fraction < ghost - 0.001f) {
            if (ghostHold == 0L) {
                ghostHold = nowMs + 380L;
            }
            if (nowMs > ghostHold) {
                ghost = MenuKit.approach(ghost, fraction, 4.5f, delta);
            }
        } else {
            ghost = Math.max(fraction, bar);
            ghostHold = 0L;
        }
        int bx = 7;
        int by = 33;
        int bw = 144;
        Smooth.rect(context, bx, by, bw, 4, 2, TargetCard.fade(0x2EFFFFFF, alpha));
        int ghostW = Math.round((float)bw * ghost);
        int fillW = Math.round((float)bw * bar);
        if (ghostW > fillW) {
            Smooth.rect(context, bx, by, Math.max(4, ghostW), 4, 2, TargetCard.fade(-1930384388, alpha));
        }
        if (fillW > 0) {
            boolean low = fraction < 0.3f;
            int from = low ? -770210 : -7643914;
            int to = low ? -298619 : -2537745;
            int end = MenuKit.lerp(from, to, bar);
            Smooth.rect(context, bx, by, Math.max(4, fillW), 4, 2, TargetCard.fade(end, alpha));
            Smooth.rect(context, bx, by, 4, 4, 2, TargetCard.fade(from, alpha));
            if (fillW > 4) {
                Smooth.horizontalGradient(context, bx + 2, by, bx + fillW - 2, by + 4, TargetCard.fade(from, alpha), TargetCard.fade(end, alpha));
            }
        }
        class_1304[] slots = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
        float segment = (float)(bw - 9) / 4.0f;
        for (int i = 0; i < 4; ++i) {
            float left;
            int sx = Math.round((float)bx + (float)i * (segment + 3.0f));
            int segW = Math.round(segment);
            Smooth.rect(context, sx, 40, segW, 2, 1, TargetCard.fade(0x1FFFFFFF, alpha));
            class_1799 armor = target.method_6118(slots[i]);
            if (armor.method_7960()) continue;
            float f = left = armor.method_7963() ? 1.0f - (float)armor.method_7919() / (float)Math.max(1, armor.method_7936()) : 1.0f;
            int color = left > 0.5f ? -5770288 : (left > 0.25f ? -208051 : -298619);
            Smooth.rect(context, sx, 40, Math.max(2, Math.round((float)segW * left)), 2, 1, TargetCard.fade(gone ? -1579542 : color, alpha));
        }
        if (gone) {
            Smooth.rect(context, 7, 46, 144, 11, 4, TargetCard.fade(1089761974, alpha * goneMix));
            int sweep = Math.round(144.0f * TargetCard.clamp((float)(nowMs - module.eliminatedAt()) / 700.0f));
            Smooth.horizontalGradient(context, 9, 46, 7 + Math.max(2, sweep), 57, TargetCard.fade(1442083510, alpha * goneMix), TargetCard.fade(16020150, alpha * goneMix));
            Icons.draw(context, Icons.Icon.SKULL, 11, 48, 7, TargetCard.fade(-415532, alpha * goneMix));
            TargetCard.label(context, "ELIMINATED", 21.0f, 48.3f, 6.2f, TargetCard.fade(-415532, alpha * goneMix));
            String total = module.hits() + " hits";
            TargetCard.text(context, total, 147.0f - TargetCard.width(total, 7.0f), 47.6f, 7.0f, TargetCard.fade(-274456, alpha * goneMix));
            return;
        }
        float statW = 46.0f;
        float comboPop = Math.max(0.0f, 1.0f - (float)(nowMs - module.comboAt()) / 220.0f);
        TargetCard.stat(context, 7.0f, statW, Icons.Icon.FLAME, -290244, String.valueOf(module.combo()), "combo", TargetCard.fade(-723723, alpha), alpha, comboPop);
        TargetCard.stat(context, 7.0f + statW + 3.0f, statW, Icons.Icon.SWORDS, -4160260, module.hits() + "-" + module.taken(), "hits", TargetCard.fade(-723723, alpha), alpha, 0.0f);
        float diff = mc.field_1724.method_6032() - hp;
        Icons.Icon icon = trend = diff >= 0.0f ? Icons.Icon.TREND_UP : Icons.Icon.TREND_DOWN;
        tone = Math.abs(diff) < 0.05f ? -6645082 : (diff > 0.0f ? -13315175 : -757066);
        value = Math.abs(diff) < 0.05f ? "" : (diff > 0.0f ? "+" : "-") + TargetCard.number(Math.abs(diff));
        String word = Math.abs(diff) < 0.05f ? "Even" : (diff > 0.0f ? "ahead" : "behind");
        TargetCard.stat(context, 7.0f + (statW + 3.0f) * 2.0f, statW, trend, tone, value, word, TargetCard.fade(tone, alpha), alpha, 0.0f);
    }

    private static void stat(class_332 context, float x, float w, Icons.Icon icon, int iconColor, String value, String word, int valueColor, float alpha, float pop) {
        Smooth.rect(context, Math.round(x), 46, Math.round(w), 11, 4, TargetCard.fade(0x12FFFFFF, alpha));
        Icons.draw(context, icon, Math.round(x + 3.0f), 48, 7, TargetCard.fade(iconColor, alpha));
        float tx = x + 12.0f;
        if (!value.isEmpty()) {
            Matrix3x2fStack matrices = context.method_51448();
            float grow = 1.0f + 0.22f * pop * pop;
            float vw = TargetCard.width(value, 7.4f);
            matrices.pushMatrix();
            matrices.translate(tx + vw / 2.0f, 51.5f);
            matrices.scale(grow, grow);
            TargetCard.text(context, value, -vw / 2.0f, -4.3f, 7.4f, valueColor);
            matrices.popMatrix();
            tx += vw + 2.5f;
        }
        if (tx + TargetCard.width(word, 6.4f) <= x + w - 2.0f) {
            TargetCard.text(context, word, tx, 48.6f, 6.4f, TargetCard.fade(-9539974, alpha));
        }
    }

    private static String weapon(class_1799 stack) {
        if (stack.method_7960()) {
            return "FIST";
        }
        if (stack.method_31573(class_3489.field_42611)) {
            return "SWORD";
        }
        if (stack.method_31573(class_3489.field_42612)) {
            return "AXE";
        }
        if (stack.method_31574(class_1802.field_49814)) {
            return "MACE";
        }
        if (stack.method_31574(class_1802.field_8547)) {
            return "TRIDENT";
        }
        if (stack.method_31574(class_1802.field_8102)) {
            return "BOW";
        }
        if (stack.method_31574(class_1802.field_8399)) {
            return "CROSSBOW";
        }
        if (stack.method_31574(class_1802.field_8255)) {
            return "SHIELD";
        }
        if (stack.method_31573(class_3489.field_42614)) {
            return "PICKAXE";
        }
        if (stack.method_31573(class_3489.field_42615)) {
            return "SHOVEL";
        }
        if (stack.method_31573(class_3489.field_42613)) {
            return "HOE";
        }
        String path = class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
        if (path.contains("spear")) {
            return "SPEAR";
        }
        if (path.contains("totem")) {
            return "TOTEM";
        }
        return "ITEM";
    }

    private static float shieldWidth(float remaining) {
        return 18.0f + TargetCard.width(String.format(Locale.ROOT, "%.1fs", Float.valueOf(Math.max(0.0f, remaining))), 7.6f) + 6.0f;
    }

    public static void shieldPill(class_332 context, float x, float y, float remaining, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        String value = String.format(Locale.ROOT, "%.1fs", Float.valueOf(Math.max(0.0f, remaining)));
        int w = Math.round(TargetCard.shieldWidth(remaining));
        int h = 14;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        Smooth.rect(context, 0, 0, w, h, 7, TargetCard.fade(-351005672, alpha));
        Smooth.outline(context, 0, 0, w, h, 7, TargetCard.fade(0x1FFFFFFF, alpha));
        float cx = 8.0f;
        float cy = 7.0f;
        float radius = 4.6f;
        Smooth.ring(context, 8, 7, 5, TargetCard.fade(0x26FFFFFF, alpha));
        float left = TargetCard.clamp(remaining / 5.0f);
        int steps = Math.max(2, Math.round(28.0f * left));
        if (left > 0.01f) {
            float[] xs = new float[steps + 1];
            float[] ys = new float[steps + 1];
            float[] ws = new float[steps + 1];
            int[] cs = new int[steps + 1];
            for (int i = 0; i <= steps; ++i) {
                double a = -1.5707963267948966 + Math.PI * 2 * (double)left * (double)i / (double)steps;
                xs[i] = cx + (float)Math.cos(a) * radius;
                ys[i] = cy + (float)Math.sin(a) * radius;
                ws[i] = 1.4f;
                cs[i] = TargetCard.fade(-4160260, alpha);
            }
            Smooth.polyline(context, xs, ys, ws, cs, steps + 1);
        }
        Icons.draw(context, Icons.Icon.SHIELD_OFF, 5, 4, 6, TargetCard.fade(remaining > 0.0f ? -2575106 : -9539974, alpha));
        TargetCard.text(context, value, 17.0f, 3.2f, 7.6f, TargetCard.fade(remaining > 0.0f ? -723723 : -6645082, alpha));
        matrices.popMatrix();
    }

    private static void shields(class_332 context, class_310 mc, float tickDelta) {
        if (mc.field_1687 == null || mc.field_1724 == null || mc.field_1690.field_1842) {
            return;
        }
        float sw = context.method_51421();
        float sh = context.method_51443();
        IntIterator it = CombatFx.shieldEntities().iterator();
        while (it.hasNext()) {
            class_1309 living;
            class_243 top;
            float[] p;
            class_1297 entity;
            float remaining;
            int id = it.nextInt();
            if (TargetCard.showing(id) || (remaining = CombatFx.shieldRemaining(id)) <= -0.5f || !((entity = mc.field_1687.method_8469(id)) instanceof class_1309) || (p = WorldLabel.project(mc, top = (living = (class_1309)entity).method_30950(tickDelta).method_1031(0.0, (double)living.method_17682() + 0.55, 0.0), sw, sh)) == null) continue;
            float alpha = TargetCard.clamp((remaining + 0.5f) / 0.5f) * TargetCard.clamp((5.0f - remaining) / 0.2f);
            float w = TargetCard.shieldWidth(remaining);
            TargetCard.shieldPill(context, p[0] - w / 2.0f, p[1] - 7.0f, remaining, alpha);
        }
    }

    static {
        bar = -1.0f;
        ghost = -1.0f;
        scale = 1.0f;
        shownId = -1;
    }
}

