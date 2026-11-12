package net.albiz.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_9848;

@Environment(EnvType.CLIENT)
public final class ScreenText {
   private static final float IN = 0.4F;
   private static final float OUT = 0.8F;
   private final int rgb;
   private final float stay;
   private class_2561 title;
   private class_2561 subtitle;
   private long start;

   public ScreenText(int rgb, float staySeconds) {
      this.rgb = rgb & 16777215;
      this.stay = staySeconds;
   }

   public void show(String line, String below) {
      this.title = class_2561.method_43470(line);
      this.subtitle = class_2561.method_43470(below);
      this.start = 0L;
   }

   public void render(class_332 g) {
      if (this.title != null) {
         class_310 mc = class_310.method_1551();
         long now = System.nanoTime();
         if (this.start == 0L) {
            if (mc.field_1755 != null || mc.field_1687 == null) {
               return;
            }

            this.start = now;
         }

         float t = (float)(now - this.start) / 1.0E9F;
         if (t >= 0.4F + this.stay + 0.8F) {
            this.title = null;
         } else {
            float fade = t < 0.4F ? t / 0.4F : Math.min(1.0F, (0.4F + this.stay + 0.8F - t) / 0.8F);
            int alpha = Math.round(fade * 255.0F);
            if (alpha > 8) {
               class_327 font = mc.field_1772;
               int color = class_9848.method_61330(alpha, this.rgb);
               g.method_51448().pushMatrix();
               g.method_51448().translate(g.method_51421() / 2.0F, g.method_51443() / 2.0F - 30.0F);
               g.method_51448().scale(3.0F, 3.0F);
               g.method_51439(font, this.title, -font.method_27525(this.title) / 2, -9 / 2, color, true);
               g.method_51448().popMatrix();
               g.method_51448().pushMatrix();
               g.method_51448().translate(g.method_51421() / 2.0F, g.method_51443() / 2.0F);
               g.method_51448().scale(1.5F, 1.5F);
               g.method_51439(font, this.subtitle, -font.method_27525(this.subtitle) / 2, -9 / 2, color, true);
               g.method_51448().popMatrix();
            }
         }
      }
   }
}
