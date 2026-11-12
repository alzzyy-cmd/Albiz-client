package net.albiz.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class SteppeDraw {
   private static final Logger LOGGER = LoggerFactory.getLogger("Albiz");
   public static final int GOLD = net.albiz.client.gui.BrandPalette.ACCENT;
   public static final int GOLD_LIGHT = net.albiz.client.gui.BrandPalette.ACCENT_LIGHT;
   public static final int GOLD_TEXT = -1326998;
   public static final int LACQUER = -16120059;
   public static final int SAND = net.albiz.client.gui.BrandPalette.MUTED;
   public static final int LAPIS = net.albiz.client.gui.BrandPalette.ACCENT_DEEP;
   public static final int LAPIS_LIGHT = net.albiz.client.gui.BrandPalette.ACCENT_LIGHT;
   public static final int MALACHITE = -12600956;
   public static final int CARNELIAN = -2537681;
   public static final int TEXT = net.albiz.client.gui.BrandPalette.TEXT;
   public static final int MUTED = net.albiz.client.gui.BrandPalette.MUTED;
   public static final int BLACK = -16777216;
   private static final Map<String, ThemeImages.Texture> TEXTURES = new HashMap<>();
   private static final String[] GLYPHS = new String[384];
   static { for (int i = 0; i < GLYPHS.length; i++) GLYPHS[i] = String.valueOf((char)i); }
   private static String glyph(char ch) { return ch < GLYPHS.length ? GLYPHS[ch] : String.valueOf(ch); }

   private SteppeDraw() {
   }

   public static int col(int argb, float alpha) {
      int a = Math.round((argb >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return GuiPalette.abgr(a << 24 | argb & 16777215);
   }

   public static int mix(int from, int to, float k) {
      k = Math.max(0.0F, Math.min(1.0F, k));
      int a = Math.round((from >>> 24 & 0xFF) + ((to >>> 24 & 0xFF) - (from >>> 24 & 0xFF)) * k);
      int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * k);
      int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * k);
      int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * k);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static void glow(ImDrawList dl, float cx, float cy, float radius, int argb, float alpha) {
      if (net.albiz.client.platform.PlatformInfo.isMobile()) return;
      int layers = 18;

      for (int i = 0; i < layers; i++) {
         float f = 1.0F - (float)i / layers;
         dl.addCircleFilled(cx, cy, radius * f, col(argb, alpha / layers * 2.2F), 48);
      }
   }

   public static void sunDisk(ImDrawList dl, float cx, float cy, float r, float spin, float s, float a) {
      glow(dl, cx, cy, r * 1.4F, 0xFFE3262E, 0.12F * a);
      dl.addCircleFilled(cx, cy, r, col(0xFFB3121F, a), 48);
      dl.addCircle(cx, cy, r, col(0xFFE96B72, a), 48, Math.max(1.0F, s));
   }

   public static void text(ImDrawList dl, ImFont font, float x, float y, String text, int abgr) {
      text = net.albiz.client.i18n.AlbizI18n.display(text);
      if (font != null) {
         net.albiz.client.i18n.LocalizedDraw.text(dl, font, Math.round(font.getFontSize()), x, y, abgr, text);
      } else {
         net.albiz.client.i18n.LocalizedDraw.text(dl, x, y, abgr, text);
      }
   }

   public static float textWidth(ImFont font, String text) {
      text = net.albiz.client.i18n.AlbizI18n.display(text);
      return font == null ? text.length() * 7.0F : font.calcTextSizeAX(font.getFontSize(), Float.MAX_VALUE, 0.0F, text);
   }

   public static float spacedWidth(ImFont font, String text, float spacing) {
      text = net.albiz.client.i18n.AlbizI18n.caption(text);
      float w = 0.0F;

      for (int i = 0; i < text.length(); i++) {
         w += textWidth(font, glyph(text.charAt(i)));
      }

      return w + spacing * Math.max(0, text.length() - 1);
   }

   public static void drawSpaced(ImDrawList dl, ImFont font, float cx, float y, String text, float spacing, int argb, float a, boolean shadow) {
      text = net.albiz.client.i18n.AlbizI18n.caption(text);
      float x = cx - spacedWidth(font, text, spacing) * 0.5F;

      for (int i = 0; i < text.length(); i++) {
         String glyph = glyph(text.charAt(i));
         if (shadow) {
            text(dl, font, x + 1.0F, y + 1.0F, glyph, col(-16777216, 0.7F * a));
         }

         text(dl, font, x, y, glyph, col(argb, a));
         x += textWidth(font, glyph) + spacing;
      }
   }

   public static ThemeImages.Texture texture(String resource) {
      if (TEXTURES.containsKey(resource)) {
         return TEXTURES.get(resource);
      } else {
         ThemeImages.Texture texture = null;

         try (InputStream in = SteppeDraw.class.getResourceAsStream(resource)) {
            if (in != null) {
               texture = ThemeImages.decode(in.readAllBytes(), resource, false);
            } else {
               LOGGER.warn("[Albiz+] Missing texture {}", resource);
            }
         } catch (RuntimeException | IOException var7) {
            LOGGER.warn("[Albiz+] Failed to load {}", resource, var7);
         }

         TEXTURES.put(resource, texture);
         return texture;
      }
   }

   public static float intro(float t, float delay) {
      float x = Math.max(0.0F, Math.min(1.0F, (t - delay) / 0.6F));
      float inv = 1.0F - x;
      return 1.0F - inv * inv * inv;
   }
}
