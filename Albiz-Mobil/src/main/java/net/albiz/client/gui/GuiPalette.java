package net.albiz.client.gui;

import net.albiz.client.config.Surface;
import net.albiz.client.config.ThemeStore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class GuiPalette {
   /** Neutral GUI colours (ABGR). Named here so the renderer has no bare colour numbers. */
   public static final int GLYPH_IDLE = -4542808;
   public static final int GLYPH_HOVER = -461582;
   public static final int LABEL_IDLE = -2042416;
   public static final int TILE = -13226454;
   public static final int TILE_HOVER = -12240588;
   public static final int DANGER = -10856974;
   public static final int SHADE = -16777216;
   public static final int TRAIL_ACTIVE = -419430401;
   private static int accent = 0xFF2E26E3;
   private static int accentHover = 0xFF7169FF;
   private static int panelBg = -349691361;
   private static int cardBg = -149022441;
   private static int tabBarBg = -149022441;
   private static float rounding = 10.0F;
   private static float animationSpeed = 1.0F;

   private GuiPalette() {
   }

   public static void refresh() {
      ThemeStore theme = ThemeStore.get();
      accent = abgr(ThemeTint.accent(theme.accent.argbWithAlpha(255)));
      accentHover = AlbizTheme.lerpColor(accent, -1, 0.2F);
      panelBg = fill(theme.panels);
      cardBg = fill(theme.cards);
      tabBarBg = fill(theme.tabBar);
      rounding = theme.rounding.get();
      animationSpeed = theme.animationSpeed.get();
   }

   public static int accent() {
      return accent;
   }

   public static int accentHover() {
      return accentHover;
   }

   public static int panelBg() {
      return panelBg;
   }

   public static int cardBg() {
      return cardBg;
   }

   public static int tabBarBg() {
      return tabBarBg;
   }

   public static int fill(Surface surface) {
      return abgr(ThemeTint.surface(surface.color.argbWithAlpha(Math.round(surface.opacity.get() * 255.0F))));
   }

   public static float rounding() {
      return rounding;
   }

   public static float animationSpeed() {
      return animationSpeed;
   }

   public static int abgr(int argb) {
      return argb & -16711936 | argb >> 16 & 0xFF | (argb & 0xFF) << 16;
   }
}
