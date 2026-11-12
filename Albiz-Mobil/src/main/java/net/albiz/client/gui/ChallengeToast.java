package net.albiz.client.gui;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_2477;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5251;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_368.class_369;

@Environment(EnvType.CLIENT)
public class ChallengeToast implements class_368 {
   public static final String RAINBOW = "albiz:rainbow";
   private static final long RAINBOW_MILLIS = 2500L;
   private static final int BACKGROUND = -266921698;
   private static final int TITLE_COLOR = -986891;
   private static final int MESSAGE_COLOR = -5656902;
   private static final int COUNT_COLOR = -6643540;
   private static final int TIMER_TRACK = 654311423;
   private static final int TIMER_FILL = -1711276033;
   private static final int TEXT_X = 30;
   private static final int TEXT_PAD = 7;
   private static final int RIGHT_GAP = 3;
   private static final int MAX_WIDTH = 240;
   private static final int LINE_HEIGHT = 10;
   private static final int MAX_LINES = 5;
   private static final long DISPLAY_MILLIS = 5000L;
   private final Object token;
   private final class_1799 icon;
   private final boolean sound;
   private class_2561 title;
   private class_2561 message;
   private int width;
   private int height = 32;
   private List<class_5481> lines = List.of();
   private int count = 1;
   private boolean changed;
   private boolean hiding;
   private long changedAt;
   private class_369 wanted = class_369.field_2209;

   public ChallengeToast(class_1799 icon, class_2561 title, class_2561 message, boolean sound) {
      this(field_2208, icon, title, message, sound);
   }

   private ChallengeToast(Object token, class_1799 icon, class_2561 title, class_2561 message, boolean sound) {
      this.token = token;
      this.icon = icon.method_7972();
      this.title = title;
      this.message = message;
      this.sound = sound;
      this.fit();
   }

   public static void addOrUpdate(class_1799 icon, String title, class_2561 message, boolean sound) {
      class_310 mc = class_310.method_1551();
      if (!mc.method_18854()) {
         mc.execute(() -> addOrUpdate(icon, title, message, sound));
      } else {
         class_374 toasts = mc.method_1566();
         ChallengeToast toast = (ChallengeToast)toasts.method_1997(ChallengeToast.class, title);
         if (toast == null) {
            toasts.method_1999(new ChallengeToast(title, icon, class_2561.method_43470(title), message, sound));
         } else {
            toast.count++;
            toast.message = message;
            toast.changed = true;
            toast.fit();
         }
      }
   }

   private String countLabel() {
      return this.count > 1 ? " x" + this.count : "";
   }

   private static class_2561 translate(class_2561 component) {
      String original = component.getString();
      String translated = net.albiz.client.i18n.AlbizI18n.display(original);
      return original.equals(translated) ? component : class_2561.method_43470(translated);
   }

   private void fit() {
      this.title = translate(this.title);
      this.message = translate(this.message);
      class_327 font = class_310.method_1551().field_1772;
      int text = Math.max(font.method_27525(this.title) + font.method_1727(this.countLabel()), font.method_27525(this.message));
      this.width = class_3532.method_15340(30 + text + 7, 160, 240);
      List<class_5481> split = this.message.getString().isEmpty() ? List.of() : font.method_1728(this.message, this.width - 30 - 7);
      this.lines = split.size() > 5 ? List.copyOf(split.subList(0, 5)) : split;
      this.height = Math.max(32, 18 + this.lines.size() * 10 + 4);
   }

   public Object method_1987() {
      return this.token;
   }

   public class_3414 method_66530() {
      return this.sound ? (class_3414)class_3417.field_14793.comp_349() : null;
   }

   public class_369 method_61988() {
      return this.wanted;
   }

   public int method_29049() {
      return this.width;
   }

   public int method_29050() {
      return this.height;
   }

   public float method_71808(int firstSlotIndex) {
      float y = firstSlotIndex * 32.0F;
      float[] bar = HudRenderer.infoBarBox();
      if (bar == null) {
         return y;
      } else {
         class_1041 window = class_310.method_1551().method_22683();
         float scale = window.method_4495();
         float left = this.method_71809(window.method_4486(), 1.0F);
         float right = left + this.width;
         float barLeft = bar[0] / scale;
         float barTop = bar[1] / scale;
         float barRight = bar[2] / scale;
         float barBottom = bar[3] / scale;
         return !(barTop > 32.0F) && !(right <= barLeft) && !(left >= barRight) ? y + barBottom + 2.0F : y;
      }
   }

   protected long displayMillis() {
      return 5000L;
   }

   protected final int cardRight() {
      return this.width - 3;
   }

   protected final int cardBottom() {
      return this.height - 1;
   }

   public void method_61989(class_374 toasts, long visibleFor) {
      if (!this.hiding) {
         if (this.changed) {
            this.changedAt = visibleFor;
            this.changed = false;
         }

         this.hiding = visibleFor - this.changedAt >= this.displayMillis() * toasts.method_48221();
         this.wanted = this.hiding ? class_369.field_2209 : class_369.field_2210;
      }
   }

   public void method_71995() {
      if (this.changed) {
         this.hiding = false;
         class_310.method_1551().method_1566().method_1999(this);
      }
   }

   public void method_1986(class_332 graphics, class_327 font, long visibleFor) {
      int right = this.width - 3;
      int bottom = this.height - 1;
      rounded(graphics, 0, 1, right, bottom, -266921698);
      this.drawTimer(graphics, visibleFor, right, bottom);
      boolean single = this.lines.isEmpty();
      this.drawTitle(graphics, font, single ? 12 : 7);

      for (int i = 0; i < this.lines.size(); i++) {
         graphics.method_51430(font, rainbowed(this.lines.get(i)), 30, 18 + i * 10, -5656902, true);
      }

      this.renderIcon(graphics, this.icon);
   }

   protected void renderIcon(class_332 graphics, class_1799 icon) {
      graphics.method_51445(icon, 8, 8);
   }

   private void drawTimer(class_332 graphics, long visibleFor, int right, int bottom) {
      double duration = this.displayMillis() * class_310.method_1551().method_1566().method_48221();
      float left = 1.0F - (float)class_3532.method_15350((visibleFor - this.changedAt) / duration, 0.0, 1.0);
      int x0 = 4;
      int x1 = right - 4;
      int y = bottom - 2;
      graphics.method_25294(x0, y, x1, y + 1, 654311423);
      graphics.method_25294(x0, y, x0 + Math.round((x1 - x0) * left), y + 1, -1711276033);
   }

   private static void rounded(class_332 graphics, int x0, int y0, int x1, int y1, int color) {
      graphics.method_25294(x0 + 2, y0, x1 - 2, y0 + 1, color);
      graphics.method_25294(x0 + 1, y0 + 1, x1 - 1, y0 + 2, color);
      graphics.method_25294(x0, y0 + 2, x1, y1 - 2, color);
      graphics.method_25294(x0 + 1, y1 - 2, x1 - 1, y1 - 1, color);
      graphics.method_25294(x0 + 2, y1 - 1, x1 - 2, y1, color);
   }

   private void drawTitle(class_332 graphics, class_327 font, int y) {
      String suffix = this.countLabel();
      int room = this.width - 30 - 7 - font.method_1727(suffix);
      class_5481 shown = this.title.method_30937();
      if (font.method_27525(this.title) > room) {
         class_2561 ellipsis = class_2561.method_43470("…");
         class_5348 cut = font.method_1714(this.title, room - font.method_27525(ellipsis));
         shown = class_2477.method_10517().method_30934(class_5348.method_29433(new class_5348[]{cut, ellipsis}));
      }

      graphics.method_51430(font, rainbowed(shown), 30, y, -986891, true);
      if (!suffix.isEmpty()) {
         graphics.method_51433(font, suffix, 30 + font.method_30880(shown), y, -6643540, true);
      }
   }

   private static class_5481 rainbowed(class_5481 text) {
      float turn = (float)(class_156.method_658() % 2500L) / 2500.0F;
      return sink -> text.accept((index, style, codePoint) -> {
         if (!"albiz:rainbow".equals(style.method_10955())) {
            return sink.accept(index, style, codePoint);
         } else {
            float hue = index * 0.045F - turn;
            int rgb = class_3532.method_15369(hue - (float)Math.floor(hue), 0.75F, 1.0F);
            return sink.accept(index, style.method_10975(null).method_27703(class_5251.method_27717(rgb)), codePoint);
         }
      });
   }
}
