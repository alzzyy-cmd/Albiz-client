// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render;

public record Color(int r, int g, int b, int a) {
    public static final Color WHITE = new Color(255, 255, 255, 255);

    public Color withAlpha(int alpha) {
        return new Color(this.r, this.g, this.b, Math.max(0, Math.min(255, alpha)));
    }

    public Color scaleAlpha(double factor) {
        return this.withAlpha((int)((double)this.a * Math.max(0.0, Math.min(1.0, factor))));
    }

    public Color lerp(Color to, double t) {
        return new Color((int)((double)this.r + (double)(to.r - this.r) * t), (int)((double)this.g + (double)(to.g - this.g) * t), (int)((double)this.b + (double)(to.b - this.b) * t), (int)((double)this.a + (double)(to.a - this.a) * t));
    }

    public int packed() {
        return (this.a & 0xFF) << 24 | (this.r & 0xFF) << 16 | (this.g & 0xFF) << 8 | this.b & 0xFF;
    }

    public static Color fromPacked(int argb) {
        return new Color(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
    }

    public Color scaleRgb(double factor) {
        return new Color(Color.clamp((double)this.r * factor), Color.clamp((double)this.g * factor), Color.clamp((double)this.b * factor), this.a);
    }

    public float[] toHsv() {
        float min;
        float rf = (float)this.r / 255.0f;
        float gf = (float)this.g / 255.0f;
        float bf = (float)this.b / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float delta = max - (min = Math.min(rf, Math.min(gf, bf)));
        float hue = delta == 0.0f ? 0.0f : (max == rf ? (gf - bf) / delta % 6.0f : (max == gf ? (bf - rf) / delta + 2.0f : (rf - gf) / delta + 4.0f));
        if ((hue /= 6.0f) < 0.0f) {
            hue += 1.0f;
        }
        float saturation = max == 0.0f ? 0.0f : delta / max;
        return new float[]{hue, saturation, max};
    }

    public static Color fromHsv(float h,float s,float v,int a){
      h=(h-(float)Math.floor(h))*6;s=Math.max(0,Math.min(1,s));v=Math.max(0,Math.min(1,v));int i=(int)h;float f=h-i,p=v*(1-s),q=v*(1-s*f),t=v*(1-s*(1-f));
      float[] rgb=switch(i%6){case 0->new float[]{v,t,p};case 1->new float[]{q,v,p};case 2->new float[]{p,v,t};case 3->new float[]{p,q,v};case 4->new float[]{t,p,v};default->new float[]{v,p,q};};
      return new Color(Math.round(rgb[0]*255),Math.round(rgb[1]*255),Math.round(rgb[2]*255),a);
    }

    public static Color rainbow(double offset, double speed, float saturation, float value, int alpha) {
        double t = (double)System.currentTimeMillis() / 1000.0 * speed + offset;
        return Color.fromHsv((float)(t - Math.floor(t)), saturation, value, alpha);
    }

    private static int clamp(double v) {
        return (int)Math.max(0.0, Math.min(255.0, v));
    }
}

