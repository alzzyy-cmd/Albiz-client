package net.albiz.client.gui.mainmenu;

import imgui.ImDrawList;
import net.albiz.client.imgui.GuardedImGui;
import java.util.Random;
import net.albiz.client.gui.SteppeDraw;
import net.albiz.client.gui.ThemeImages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class DuskScene {
   public static final float HORIZON = 0.72F;
   private static final String SKY_IMAGE = "/assets/albiz/textures/gui/menu/bg_sky.jpg";
   private static final String SUN_IMAGE = "/assets/albiz/textures/gui/menu/bg_sun.png";
   private static final String MID_IMAGE = "/assets/albiz/textures/gui/menu/bg_mid.png";
   private static final String NEAR_IMAGE = "/assets/albiz/textures/gui/menu/bg_near.png";
   private static final float CANVAS_W = 2880.0F;
   private static final float CANVAS_H = 1620.0F;
   private static final float SAFE_W = 2560.0F;
   private static final float SAFE_H = 1440.0F;
   private static final float CANVAS_HORIZON = 1127.0F;
   private static final float SUN_DISK = 0.3115F;
   private static final float SKY_DRIFT = 4.0F;
   private static final float MID_DRIFT = 14.0F;
   private static final float NEAR_DRIFT = 32.0F;
   private static final int SKY_NIGHT = -16251124;
   private static final int SKY_DUSK = -15069431;
   private static final int SKY_AMBER = -11194353;
   private static final float SKY_TURN = 0.45F;
   private static final int HORIZON_GLOW = -2062038;
   private static final float HORIZON_GLOW_PEAK = 0.2F;
   private static final float HORIZON_GLOW_REACH = 0.55F;
   private static final int GLOW_RINGS = 12;
   private static final int GLOW_SEGMENTS = 72;
   private static final float[] GLOW_COS = new float[72];
   private static final float[] GLOW_SIN = new float[72];
   private static final float SUN_SPIN = 0.03F;
   private static final float SUN_BREATH = 0.8F;
   private static final int MOUNTAIN_LIT = -12966896;
   private static final int MOUNTAIN_SHADE = -15397625;
   private static final int FAR_DUNE = -14412277;
   private static final int NEAR_DUNE = -15594746;
   private static final int DUNE_COLUMNS = 160;
   private static final int GRAIN_COUNT = 70;
   private static final float[][] GRAINS = new float[70][];
   private static final float VIGNETTE = 0.55F;

   private DuskScene() {
   }

   public static float horizon(float height) {
      return height * 0.72F;
   }

   public static void draw(
      ImDrawList dl, float width, float height, float scale, float time, float horizon, float sunX, float sunY, float sunRadius, float lookX, float lookY
   ) {
      boolean mobile = net.albiz.client.platform.PlatformInfo.isMobile();
      ThemeImages.Texture poster = MenuTextures.original(mobile
         ? "/assets/albiz/textures/gui/dusk_backdrop_mobile.jpg"
         : "/assets/albiz/textures/gui/dusk_backdrop.png");
      if (poster != null) {
         float fit = Math.max(width / poster.width(), height / poster.height()) * (mobile ? 1 : 1.02F);
         float pw = poster.width() * fit, ph = poster.height() * fit;
         float ox = (width - pw) * 0.5F - (mobile ? 0 : lookX * 4 * scale);
         float oy = (height - ph) * 0.5F - (mobile ? 0 : lookY * 2 * scale);
         dl.addImage(poster.id(), ox, oy, ox + pw, oy + ph);
         vignette(dl, width, height);
         return;
      }
      float s = Math.max(width / 1920.0F, height / 1080.0F) * 1.035F;
      float x0 = (width - 1920.0F * s) * 0.5F;
      float y0 = (height - 1080.0F * s) * 0.5F;
      if (!layer(dl, "/assets/albiz/textures/gui/menu/bg_sky.jpg", x0, y0, s, 4.0F, lookX, lookY)) {
         sky(dl, width, height, horizon);
      }

      sun(dl, sunX, sunY, sunRadius, scale, time);
      if (!layer(dl, "/assets/albiz/textures/gui/menu/bg_mid.png", x0, y0, s, 14.0F, lookX, lookY)) {
         mountains(dl, width, height, horizon, scale);
      }

      if (!layer(dl, "/assets/albiz/textures/gui/menu/bg_near.png", x0, y0, s, 32.0F, lookX, lookY)) {
         dunes(dl, width, height, horizon, scale, time);
      }

      if (!net.albiz.client.platform.PlatformInfo.isMobile()) sand(dl, width, height, horizon, scale, time);
      vignette(dl, width, height);
   }

   private static boolean layer(ImDrawList dl, String image, float x0, float y0, float s, float drift, float lookX, float lookY) {
      ThemeImages.Texture texture = MenuTextures.original(image);
      if (texture == null) {
         return false;
      } else {
         float x = x0 - lookX * drift * s;
         float y = y0 - lookY * drift * 0.5F * s;
         dl.addImage(texture.id(), x, y, x + 1920.0F * s, y + 1080.0F * s);
         return true;
      }
   }

   private static void sky(ImDrawList dl, float width, float height, float horizon) {
      int night = SteppeDraw.col(-16251124, 1.0F);
      int dusk = SteppeDraw.col(-15069431, 1.0F);
      int amber = SteppeDraw.col(-11194353, 1.0F);
      float turn = Math.min(height * 0.45F, horizon);
      dl.addRectFilledMultiColor(0.0F, 0.0F, width, turn, night, night, dusk, dusk);
      dl.addRectFilledMultiColor(0.0F, turn, width, horizon, dusk, dusk, amber, amber);
      dl.addRectFilled(0.0F, horizon, width, height, SteppeDraw.col(-16777216, 1.0F));
      horizonGlow(dl, width * 0.5F, horizon, width * 0.55F);
   }

   private static void sun(ImDrawList dl, float cx, float cy, float radius, float scale, float time) {
      ThemeImages.Texture texture = MenuTextures.original("/assets/albiz/textures/gui/menu/bg_sun.png");
      if (texture == null) {
         SteppeDraw.sunDisk(dl, cx, cy, radius, time * 0.03F, scale, 1.0F);
      } else {
         float half = radius / 0.3115F * 0.5F;
         float glow = 0.965F + 0.035F * (float)Math.sin(time * 0.8F);
         dl.addImage(texture.id(), cx - half, cy - half, cx + half, cy + half, 0.0F, 0.0F, 1.0F, 1.0F, SteppeDraw.col(-1, glow));
      }
   }

   private static void mountains(ImDrawList dl, float width, float height, float horizon, float scale) {
      mountain(dl, width * 0.17F, horizon, width * 0.34F, height * 0.25F, true, scale);
      mountain(dl, width * 0.35F, horizon, width * 0.13F, height * 0.09F, true, scale);
      mountain(dl, width * 0.83F, horizon, width * 0.26F, height * 0.19F, false, scale);
      mountain(dl, width * 0.96F, horizon, width * 0.14F, height * 0.1F, false, scale);
   }

   private static void dunes(ImDrawList dl, float width, float height, float horizon, float scale, float time) {
      dune(dl, width, height, horizon + height * 0.015F, 9.0F * scale, 0.0021F / scale, 0.7F, time * 0.02F, -14412277);
      dune(dl, width, height, horizon + height * 0.09F, 15.0F * scale, 0.0014F / scale, 2.1F, time * 0.03F, -15594746);
   }

   private static void sand(ImDrawList dl, float width, float height, float horizon, float scale, float time) {
      float band = height - horizon + height * 0.08F;

      for (float[] grain : GRAINS) {
         float x = width * (1.0F - (float)((grain[1] + time * grain[2]) % 1.0)) * 1.1F - width * 0.05F;
         float y = height - band * grain[0] + (float)Math.sin(time * 1.3F + grain[1] * 20.0F) * 3.0F * scale;
         float len = grain[3] * scale;
         dl.addLine(x, y, x + len, y - len * 0.08F, SteppeDraw.col(net.albiz.client.gui.BrandPalette.MUTED, grain[4]), Math.max(1.0F, scale));
      }
   }

   private static void vignette(ImDrawList dl, float width, float height) {
      float edge = Math.min(width, height) * 0.3F;
      int dark = SteppeDraw.col(-16777216, 0.55F);
      int clear = SteppeDraw.col(-16777216, 0.0F);
      dl.addRectFilledMultiColor(0.0F, 0.0F, edge, height, dark, clear, clear, dark);
      dl.addRectFilledMultiColor(width - edge, 0.0F, width, height, clear, dark, dark, clear);
   }

   public static void veil(ImDrawList dl, float width, float height, float amount) {
      dl.addRectFilled(0.0F, 0.0F, width, height, SteppeDraw.col(-16120059, amount));
   }

   private static void mountain(ImDrawList dl, float cx, float base, float w, float h, boolean litRight, float s) {
      int shade = SteppeDraw.col(0xFF241122, 1.0F);
      dl.pathClear();
      dl.pathLineTo(cx - w * 0.5F, base);
      dl.pathLineTo(cx - w * 0.3F, base - h * 0.48F);
      dl.pathLineTo(cx - w * 0.16F, base - h * 0.38F);
      dl.pathLineTo(cx, base - h);
      dl.pathLineTo(cx + w * 0.14F, base - h * 0.65F);
      dl.pathLineTo(cx + w * 0.3F, base - h * 0.48F);
      dl.pathLineTo(cx + w * 0.5F, base);
      dl.pathFillConvex(shade);
   }

   private static void horizonGlow(ImDrawList dl, float cx, float cy, float radius) {
      if (net.albiz.client.platform.PlatformInfo.isMobile()) return;
      float u = GuardedImGui.getFontTexUvWhitePixelX();
      float v = GuardedImGui.getFontTexUvWhitePixelY();
      int n = 72;
      dl.primReserve(n * 3 + 11 * n * 6, 1 + 12 * n);
      int centre = dl.getVtxCurrentIdx();
      dl.primWriteVtx(cx, cy, u, v, SteppeDraw.col(-2062038, 0.2F));

      for (int ring = 1; ring <= 12; ring++) {
         float f = ring / 12.0F;
         float left = 1.0F - f;
         int color = SteppeDraw.col(-2062038, 0.2F * left * left * (3.0F - 2.0F * left));

         for (int i = 0; i < n; i++) {
            dl.primWriteVtx(cx + GLOW_COS[i] * radius * f, cy + GLOW_SIN[i] * radius * f, u, v, color);
         }
      }

      for (int i = 0; i < n; i++) {
         int j = (i + 1) % n;
         dl.primWriteIdx(centre);
         dl.primWriteIdx(centre + 1 + i);
         dl.primWriteIdx(centre + 1 + j);

         for (int ring = 1; ring < 12; ring++) {
            int inner = centre + 1 + (ring - 1) * n;
            int outer = inner + n;
            quad(dl, inner + i, outer + i, outer + j, inner + j);
         }
      }
   }

   private static void dune(ImDrawList dl, float width, float height, float y, float amp, float freq, float phase, float drift, int argb) {
      float u = GuardedImGui.getFontTexUvWhitePixelX();
      float v = GuardedImGui.getFontTexUvWhitePixelY();
      int body = SteppeDraw.col(argb, 1.0F);
      int clear = SteppeDraw.col(argb, 0.0F);
      dl.primReserve(1920, 483);
      int first = dl.getVtxCurrentIdx();

      for (int i = 0; i <= 160; i++) {
         float x = width * i / 160.0F;
         double w = x * freq + phase + drift;
         float ridge = y - (float)(Math.sin(w) * amp + Math.sin(w * 2.3 + 1.7) * amp * 0.45);
         dl.primWriteVtx(x, ridge - 0.5F, u, v, clear);
         dl.primWriteVtx(x, ridge + 0.5F, u, v, body);
         dl.primWriteVtx(x, height, u, v, body);
      }

      for (int i = 0; i < 160; i++) {
         int a = first + i * 3;
         int b = a + 3;
         quad(dl, a, b, b + 1, a + 1);
         quad(dl, a + 1, b + 1, b + 2, a + 2);
      }
   }

   private static void quad(ImDrawList dl, int a, int b, int c, int d) {
      dl.primWriteIdx(a);
      dl.primWriteIdx(b);
      dl.primWriteIdx(c);
      dl.primWriteIdx(a);
      dl.primWriteIdx(c);
      dl.primWriteIdx(d);
   }

   static {
      Random random = new Random(686869L);

      for (int i = 0; i < 70; i++) {
         GRAINS[i] = new float[]{
            random.nextFloat(), random.nextFloat(), 0.04F + random.nextFloat() * 0.09F, 4.0F + random.nextFloat() * 14.0F, 0.15F + random.nextFloat() * 0.35F
         };
      }

      for (int i = 0; i < 72; i++) {
         double angle = i * Math.PI * 2.0 / 72.0;
         GLOW_COS[i] = (float)Math.cos(angle);
         GLOW_SIN[i] = (float)Math.sin(angle);
      }
   }
}
