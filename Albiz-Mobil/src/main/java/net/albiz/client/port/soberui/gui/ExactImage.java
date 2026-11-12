// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.SharpTexture;
import net.albiz.client.port.soberui.gui.Smooth;
import java.io.InputStream;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

final class ExactImage {
    private final class_2960 source;
    private final String key;
    private class_1011 image;
    private boolean failed;
    private class_2960 current;
    private int shownW;
    private int texW;
    private int texH;
    private static int instances;

    ExactImage(String path) {
        this.source = class_2960.method_60655((String)"albiz", (String)path);
        this.key = path.replace('/', '_').replace(".png", "") + "_" + instances++;
    }

    private boolean load(){
      if(image!=null)return true;if(failed)return false;
      try(InputStream stream=class_310.method_1551().method_1478().open(source)){image=class_1011.method_4309(stream);return true;}
      catch(Exception ex){failed=true;SoberClient.LOG.error("Could not load {}",source,ex);return false;}
    }

    float aspect() {
        return this.load() ? (float)this.image.method_4323() / (float)this.image.method_4307() : 1.0f;
    }

    void draw(class_332 context, float x, float y, float w, int color) {
        if (color >>> 24 == 0 || w <= 0.0f || !this.load()) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        class_1041 window = class_310.method_1551().method_22683();
        float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
        float zoom = (float)Math.sqrt(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01());
        int pw = Math.max(1, Math.round(w * zoom * ratio));
        int ph = Math.max(1, Math.round((float)(pw * this.image.method_4323()) / (float)this.image.method_4307()));
        if (this.current == null || pw != this.shownW) {
            this.rebuild(pw, ph);
        }
        Vector2f origin = matrices.transformPosition(x, y, new Vector2f());
        int px = Math.round(origin.x * ratio);
        int py = Math.round(origin.y * ratio);
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        context.method_25293(class_10799.field_56883, this.current, px, py, 0.0f, 0.0f, pw, ph, this.texW, this.texH, this.texW, this.texH, color);
        matrices.popMatrix();
    }

    private void rebuild(int pw, int ph) {
        class_1011 scaled;
        if (pw >= this.image.method_4307()) {
            scaled = new class_1011(this.image.method_4307(), this.image.method_4323(), false);
            scaled.method_4317(this.image);
        } else {
            scaled = SharpTexture.downscale(this.image, pw, ph);
        }
        if (this.current != null) {
            class_310.method_1551().method_1531().method_4615(this.current);
        }
        this.current = class_2960.method_60655((String)"albiz", (String)("dynamic/exact_" + this.key + "_" + pw));
        Smooth.register(this.current, scaled);
        this.shownW = pw;
        this.texW = scaled.method_4307();
        this.texH = scaled.method_4323();
    }
}

