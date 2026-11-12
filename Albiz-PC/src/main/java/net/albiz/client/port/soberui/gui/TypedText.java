// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.MenuKit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_332;

final class TypedText {
    private static final float IN = 0.16f;
    private static final float OUT = 0.14f;
    private static final float RISE = 3.0f;
    private final List<Glyph> glyphs = new ArrayList<Glyph>();
    private final List<Ghost> ghosts = new ArrayList<Ghost>();
    private String value = "";
    private float caretX = Float.NaN;
    private float placeholder = 1.0f;
    private long lastFrame = System.nanoTime();
    private long typedAt;

    TypedText() {
    }

    void sync(String next, Measure measure) {
        int suffix;
        int prefix;
        if (next.equals(this.value)) {
            return;
        }
        for (prefix = 0; prefix < this.value.length() && prefix < next.length() && this.value.charAt(prefix) == next.charAt(prefix); ++prefix) {
        }
        for (suffix = 0; suffix < this.value.length() - prefix && suffix < next.length() - prefix && this.value.charAt(this.value.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix); ++suffix) {
        }
        long now = System.nanoTime();
        for (int i = prefix; i < this.value.length() - suffix; ++i) {
            this.ghosts.add(new Ghost(String.valueOf(this.value.charAt(i)), measure.width(this.value.substring(0, i)), now));
        }
        ArrayList<Glyph> next0 = new ArrayList<Glyph>(this.glyphs.subList(0, prefix));
        for (int i = prefix; i < next.length() - suffix; ++i) {
            next0.add(new Glyph(String.valueOf(next.charAt(i)), now));
        }
        next0.addAll(this.glyphs.subList(this.value.length() - suffix, this.value.length()));
        this.glyphs.clear();
        this.glyphs.addAll(next0);
        this.value = next;
        this.typedAt = now;
    }

    private static int fade(int color, float amount) {
        return MenuKit.alpha(color, Math.max(0.0f, Math.min(1.0f, amount)));
    }

    private static float out(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }

    void draw(class_332 context, float x, float y, float maxWidth, float lineHeight, boolean focused, String hint, int textColor, int hintColor, int caretColor, float opacity, Measure measure, Draw draw) {
        int i;
        long now = System.nanoTime();
        float delta = Math.min(0.06f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float total = measure.width(this.value);
        float shift = Math.max(0.0f, total - maxWidth);
        this.placeholder = MenuKit.approach(this.placeholder, this.value.isEmpty() ? 1.0f : 0.0f, 18.0f, delta);
        if (this.placeholder > 0.01f && hint != null && !hint.isEmpty()) {
            draw.text(context, hint, x + (1.0f - this.placeholder) * 6.0f, y, TypedText.fade(hintColor, this.placeholder));
        }
        for (i = 0; i < this.glyphs.size(); ++i) {
            Glyph glyph = this.glyphs.get(i);
            float gx = measure.width(this.value.substring(0, i)) - shift;
            if (gx < -0.5f) continue;
            float shown = TypedText.out((float)(now - glyph.born()) / 1.0E9f / 0.16f);
            draw.text(context, glyph.value(), x + gx, y + (1.0f - shown) * 3.0f, TypedText.fade(textColor, shown));
        }
        for (i = this.ghosts.size() - 1; i >= 0; --i) {
            Ghost ghost = this.ghosts.get(i);
            float t = (float)(now - ghost.died()) / 1.0E9f / 0.14f;
            if (t >= 1.0f) {
                this.ghosts.remove(i);
                continue;
            }
            float gx = ghost.x() - shift;
            if (gx < -0.5f) continue;
            draw.text(context, ghost.value(), x + gx, y + t * 2.0f, TypedText.fade(textColor, 1.0f - TypedText.out(t)));
        }
        float target = x + total - shift;
        float f = this.caretX = Float.isNaN(this.caretX) ? target : this.caretX + (target - this.caretX) * (1.0f - (float)Math.exp(-delta * 30.0f));
        if (!focused) {
            return;
        }
        float since = (float)(now - this.typedAt) / 1.0E9f;
        float blink = since < 0.5f ? 1.0f : 0.5f + 0.5f * (float)Math.cos((double)(since - 0.5f) * Math.PI / (double)0.55f);
        int cx = Math.round(this.caretX + 0.5f);
        context.method_25294(cx, Math.round(y - 1.0f), cx + 1, Math.round(y + lineHeight), MenuKit.alpha(TypedText.fade(caretColor, blink), opacity));
    }

    private record Ghost(String value, float x, long died) {
    }

    static interface Measure {
        public float width(String var1);
    }

    private record Glyph(String value, long born) {
    }

    static interface Draw {
        public void text(class_332 var1, String var2, float var3, float var4, int var5);
    }

    static final class Arrivals {
        private final Map<Object, Long> since = new HashMap<Object, Long>();
        private Set<Object> seen = new HashSet<Object>();
        private int fresh;

        Arrivals() {
        }

        void begin() {
            this.seen = new HashSet<Object>();
            this.fresh = 0;
        }

        float alpha(Object key) {
            this.seen.add(key);
            long now = System.nanoTime();
            Long at = this.since.get(key);
            if (at == null) {
                at = now + (long)this.fresh++ * 25000000L;
                this.since.put(key, at);
            }
            return MenuKit.quint(Math.max(0.0f, (float)(now - at) / 1.0E9f / 0.28f));
        }

        void end() {
            this.since.keySet().retainAll(this.seen);
        }

        void reset() {
            this.since.clear();
        }
    }
}

