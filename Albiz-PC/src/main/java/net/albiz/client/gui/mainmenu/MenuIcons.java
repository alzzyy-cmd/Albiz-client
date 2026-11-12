package net.albiz.client.gui.mainmenu;

import imgui.ImDrawList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MenuIcons {
   private MenuIcons() {
   }

   public static void draw(ImDrawList dl, MenuAction action, float cx, float cy, float r, int color, float th) {
      switch (action) {
         case LANGUAGE:
            globe(dl, cx, cy, r, color, th);
            break;
         case ACCESSIBILITY:
            figure(dl, cx, cy, r, color, th);
            break;
         case ALBIZ:
            wolf(dl, cx, cy, r, color, th);
            break;
         case MODS:
            crescent(dl, cx, cy, r, color, th);
            break;
         case ACCOUNTS:
            person(dl, cx, cy, r, color, th);
            break;
         case CREDITS:
            sword(dl, cx, cy, r, color, th);
            break;
         default:
            dl.addCircleFilled(cx, cy, r * 0.25F, color, 12);
      }
   }

   public static void person(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      dl.addCircle(cx, cy - r * 0.38F, r * 0.42F, color, 24, th);
      dl.pathClear();
      dl.pathArcTo(cx, cy + r * 0.95F, r * 0.82F, (float) Math.PI, (float) (Math.PI * 2), 20);
      dl.pathStroke(color, 0, th);
   }

   /** A genuine two-arc crescent, not an ankh under a different method name. */
   public static void crescent(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      dl.pathClear();
      for (int i = 0; i <= 20; i++) {
         double a = Math.PI / 3 + (Math.PI * 4 / 3) * i / 20;
         dl.pathLineTo(cx + (float)Math.cos(a) * r, cy + (float)Math.sin(a) * r);
      }
      for (int i = 0; i <= 14; i++) {
         double a = Math.PI * 1.5 - Math.PI * i / 14;
         dl.pathLineTo(cx + r * 0.5F + (float)Math.cos(a) * r * 0.8660254F, cy + (float)Math.sin(a) * r * 0.8660254F);
      }
      dl.pathStroke(color, 1, th);
   }

   public static void wolf(ImDrawList dl,float cx,float cy,float r,int color,float th){
      color=0xFF2E26E3; // ImGui ABGR: Albiz red
      for(double[] line:net.albiz.client.util.AlbizLogoGeometry.segments())dl.addLine(cx+(float)line[0]*r,cy+(float)line[1]*r,cx+(float)line[2]*r,cy+(float)line[3]*r,color,th);
   }

   public static void sword(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      dl.pathClear();
      dl.pathLineTo(cx,cy-r);
      dl.pathLineTo(cx+r*0.23F,cy-r*0.58F);
      dl.pathLineTo(cx+r*0.13F,cy+r*0.35F);
      dl.pathLineTo(cx-r*0.13F,cy+r*0.35F);
      dl.pathLineTo(cx-r*0.23F,cy-r*0.58F);
      dl.pathStroke(color,1,th);
      dl.addLine(cx-r*0.62F,cy+r*0.28F,cx+r*0.62F,cy+r*0.28F,color,th*1.2F);
      dl.addLine(cx,cy+r*0.35F,cx,cy+r*0.88F,color,th*1.8F);
      dl.addCircleFilled(cx,cy+r*0.97F,r*0.14F,color,8);
   }

   public static void globe(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      dl.addCircle(cx, cy, r, color, 32, th);
      ellipse(dl, cx, cy, r * 0.45F, r, color, th);
      dl.addLine(cx - r, cy, cx + r, cy, color, th);
   }

   public static void figure(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      dl.addCircleFilled(cx, cy - r * 0.8F, r * 0.22F, color, 16);
      dl.addLine(cx - r * 0.85F, cy - r * 0.35F, cx + r * 0.85F, cy - r * 0.35F, color, th);
      dl.addLine(cx, cy - r * 0.35F, cx, cy + r * 0.3F, color, th);
      dl.addLine(cx, cy + r * 0.3F, cx - r * 0.5F, cy + r, color, th);
      dl.addLine(cx, cy + r * 0.3F, cx + r * 0.5F, cy + r, color, th);
   }

   public static void folder(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      float l = cx - r;
      float rt = cx + r;
      float tp = cy - r * 0.7F;
      float bt = cy + r * 0.75F;
      dl.pathClear();
      dl.pathLineTo(l, bt);
      dl.pathLineTo(l, tp);
      dl.pathLineTo(cx - r * 0.2F, tp);
      dl.pathLineTo(cx, tp + r * 0.3F);
      dl.pathLineTo(rt, tp + r * 0.3F);
      dl.pathLineTo(rt, bt);
      dl.pathStroke(color, 1, th);
   }

   /** Binary/source compatibility for addons; the drawing itself is a sword now. */
   @Deprecated public static void scroll(ImDrawList dl, float cx, float cy, float r, int color, float th) {
      sword(dl,cx,cy,r,color,th);
   }

   public static void ellipse(ImDrawList dl, float cx, float cy, float rx, float ry, int color, float th) {
      dl.pathClear();
      int segments = 28;

      for (int i = 0; i < segments; i++) {
         float ang = (float)(i * Math.PI * 2.0 / segments);
         dl.pathLineTo(cx + (float)Math.cos(ang) * rx, cy + (float)Math.sin(ang) * ry);
      }

      dl.pathStroke(color, 1, th);
   }
}
