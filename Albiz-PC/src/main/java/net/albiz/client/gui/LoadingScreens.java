package net.albiz.client.gui;

import net.albiz.client.mixin.AccessorLevelLoadingScreen;
import net.albiz.client.mixin.AccessorLoadingOverlay;
import imgui.ImDrawList;
import imgui.ImFont;
import net.albiz.client.imgui.GuardedImGui;
import imgui.ImGuiIO;
import java.util.Locale;
import net.albiz.client.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_3928;
import net.minecraft.class_424;
import net.minecraft.class_425;
import net.minecraft.class_435;
import net.minecraft.class_437;

@Environment(EnvType.CLIENT)
public final class LoadingScreens {
   private static final String HEAD_RESOURCE = "/assets/albiz/textures/gui/head.png";
   private static final int BACKGROUND_RGB = 722950;
   private static final float FADE_OUT_SECONDS = 0.45F;
   private static final long ORIGIN = System.nanoTime();
   private static long lastActiveAt = -1L;
   private static float lastProgress;
   private static float lastAlpha;
   private static String lastStatus = "";
   private static float shownFill;

   private LoadingScreens() {
   }

   public static int backgroundColor(int argb) {
      return !AlbizTitleScreen.enabled() ? argb : argb & 0xFF000000 | (BrandPalette.BACKGROUND & 0xFFFFFF);
   }

   public static boolean hasWork() {
      if (!AlbizTitleScreen.enabled()) {
         return false;
      } else if (lastActiveAt >= 0L) {
         return true;
      } else {
         class_310 mc = class_310.method_1551();
         return mc.method_18506() instanceof class_425
            || mc.field_1755 instanceof class_3928
            || mc.field_1755 instanceof class_424
            || mc.field_1755 instanceof class_435;
      }
   }

   public static void render() {
      if (AlbizTitleScreen.enabled()) {
         class_310 mc = class_310.method_1551();
         long now = System.nanoTime();
         float alpha;
         float progress;
         String status;
         if (mc.method_18506() instanceof class_425 overlay) {
            AccessorLoadingOverlay state = (AccessorLoadingOverlay)overlay;
            alpha = reloadAlpha(state);
            progress = state.albiz$getProgress();
            status = "Loading";
         } else {
            class_437 screen = mc.field_1755;
            if (screen instanceof class_3928 loading) {
               progress = ((AccessorLevelLoadingScreen)loading).albiz$getProgress();
               status = "Entering world";
            } else {
               if (!(screen instanceof class_424) && !(screen instanceof class_435)) {
                  drawFadingOut(now);
                  return;
               }

               progress = -1.0F;
               String title = screen.method_25440().getString();
               status = title.isBlank() ? "Loading" : title;
            }

            alpha = 1.0F;
         }

         lastActiveAt = now;
         lastProgress = progress;
         lastStatus = status;
         lastAlpha = alpha;
         draw(alpha, progress, status, now);
      }
   }

   private static void drawFadingOut(long now) {
      if (lastActiveAt >= 0L) {
         float elapsed = seconds(now - lastActiveAt);
         if (elapsed >= 0.45F) {
            lastActiveAt = -1L;
         } else {
            draw(lastAlpha * (1.0F - elapsed / 0.45F), lastProgress < 0.0F ? -1.0F : 1.0F, lastStatus, now);
         }
      }
   }

   private static float reloadAlpha(AccessorLoadingOverlay state) {
      long millis = class_156.method_658();
      long fadeOutStart = state.albiz$getFadeOutStart();
      long fadeInStart = state.albiz$getFadeInStart();
      float fadeOut = fadeOutStart > -1L ? (float)(millis - fadeOutStart) / 1000.0F : -1.0F;
      float fadeIn = fadeInStart > -1L ? (float)(millis - fadeInStart) / 500.0F : -1.0F;
      if (fadeOut >= 1.0F) {
         return 1.0F - clamp01(fadeOut - 1.0F);
      } else {
         return state.albiz$isFadeIn() ? clamp01(fadeIn) : 1.0F;
      }
   }

   private static void draw(float a, float progress, String status, long now) {
      if (a <= 0.001F) return;
      ImGuiIO io = GuardedImGui.getIO();
      float sw = io.getDisplaySizeX(), sh = io.getDisplaySizeY();
      float scale = ImGuiManager.getInstance().uiScale();
      ImDrawList dl = GuardedImGui.getForegroundDrawList();
      dl.addRectFilledMultiColor(0, 0, sw, sh,
         SteppeDraw.col(0xff08090c, a), SteppeDraw.col(0xff08090c, a),
         SteppeDraw.col(0xff08090c, a), SteppeDraw.col(0xff08090c, a));
      float cx = sw * 0.5F, size = Math.min(sh * 0.24F, 230 * scale);
      float top = sh * 0.5F-size*.78F;
      ThemeImages.Texture head = SteppeDraw.texture(HEAD_RESOURCE);
      if (head != null) dl.addImage(head.id(), cx - size * 0.5F, top, cx + size * 0.5F, top + size,
         0, 0, 1, 1, SteppeDraw.col(-1, a));
      ImFont title = ImGuiManager.getInstance().menuFont(), small = ImGuiManager.getInstance().menuBodyFont();
      float nameY = top + size + 20 * scale;
      SteppeDraw.drawSpaced(dl, title, cx, nameY, BrandPalette.WORDMARK, 5 * scale, BrandPalette.ACCENT, a, false);
      float width = Math.min(sw * 0.46F, 320 * scale), x = cx - width * 0.5F;
      float barY = nameY + (title == null ? 24 * scale : title.getFontSize()) + 30 * scale;
      float barH = Math.max(3, 5 * scale);
      dl.addRectFilled(x, barY, x + width, barY + barH, SteppeDraw.col(BrandPalette.ACCENT_FAINT, a), barH);
      if (progress >= 0) {
         shownFill += (clamp01(progress) - shownFill) * Math.min(1, io.getDeltaTime() * 8);
         dl.addRectFilled(x, barY, x + width * shownFill, barY + barH, SteppeDraw.col(BrandPalette.ACCENT, a), barH);
      } else {
         // Linear indeterminate sweep, not the old spinning golden ring.
         float phase = (seconds(now - ORIGIN) * 0.45F) % 1;
         float left = Math.max(0, phase * 1.3F - 0.3F), right = Math.min(1, phase * 1.3F);
         dl.addRectFilled(x + width * left, barY, x + width * right, barY + barH,
            SteppeDraw.col(BrandPalette.ACCENT, a), barH);
      }
      String label = net.albiz.client.i18n.AlbizI18n.display(status);
      SteppeDraw.text(dl, small, cx - SteppeDraw.textWidth(small, label) * 0.5F, barY + 17 * scale,
         label, SteppeDraw.col(BrandPalette.MUTED, a));
   }


   private static float seconds(long nanos) {
      return (float)nanos / 1.0E9F;
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }
}
