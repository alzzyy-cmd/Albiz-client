// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.setting.Setting;

public class ColorSetting
extends Setting<Color> {
    private float hue = -1.0f;
    private float saturation;

    public ColorSetting(String name, String description, Color defaultValue) {
        super(name, description, defaultValue);
    }

    public void setChannel(int channel, int v) {
        Color c = (Color)this.value;
        int clamped = Math.max(0, Math.min(255, v));
        this.set(switch (channel) {
            case 0 -> new Color(clamped, c.g(), c.b(), c.a());
            case 1 -> new Color(c.r(), clamped, c.b(), c.a());
            case 2 -> new Color(c.r(), c.g(), clamped, c.a());
            default -> new Color(c.r(), c.g(), c.b(), clamped);
        });
    }

    public float[] hsv() {
        float[] hsv = ((Color)this.value).toHsv();
        if (this.hue < 0.0f) {
            this.hue = hsv[0];
            this.saturation = hsv[1];
        }
        if (hsv[2] > 0.001f && hsv[1] > 0.001f) {
            this.hue = hsv[0];
            this.saturation = hsv[1];
        } else if (hsv[2] > 0.001f) {
            this.saturation = hsv[1];
        }
        return new float[]{this.hue, this.saturation, hsv[2]};
    }

    public void setHsv(float h, float s, float v) {
        this.hue = h;
        this.saturation = s;
        this.set(Color.fromHsv(h, s, v, ((Color)this.value).a()));
    }

    public void setAlpha(int alpha) {
        this.set(((Color)this.value).withAlpha(alpha));
    }

    public boolean setHex(String text) {
        Object hex = text.trim();
        if (((String)hex).startsWith("#")) {
            hex = ((String)hex).substring(1);
        } else if (((String)hex).regionMatches(true, 0, "0x", 0, 2)) {
            hex = ((String)hex).substring(2);
        }
        if (!((String)hex).matches("[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8}")) {
            return false;
        }
        if (((String)hex).length() == 3) {
            hex = "" + ((String)hex).charAt(0) + ((String)hex).charAt(0) + ((String)hex).charAt(1) + ((String)hex).charAt(1) + ((String)hex).charAt(2) + ((String)hex).charAt(2);
        }
        int rgb = Integer.parseInt(((String)hex).substring(0, 6), 16);
        int alpha = ((String)hex).length() == 8 ? Integer.parseInt(((String)hex).substring(6), 16) : ((Color)this.value).a();
        this.hue = -1.0f;
        this.set(new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha));
        return true;
    }

    public int channel(int channel) {
        return switch (channel) {
            case 0 -> ((Color)this.value).r();
            case 1 -> ((Color)this.value).g();
            case 2 -> ((Color)this.value).b();
            default -> ((Color)this.value).a();
        };
    }

    @Override
    public String display() {
        return String.format("#%02X%02X%02X", ((Color)this.value).r(), ((Color)this.value).g(), ((Color)this.value).b());
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive((Number)((Color)this.value).packed());
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.value = Color.fromPacked(element.getAsInt());
        }
    }
}

