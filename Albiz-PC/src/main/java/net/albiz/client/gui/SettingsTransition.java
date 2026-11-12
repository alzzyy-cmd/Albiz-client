package net.albiz.client.gui;

/**
 * Time-based open/close transition for the module settings panel.
 *
 * <p>While it opens, the module browser slides left until only {@link Config#mainVisible} of it
 * remains on screen as a reference, and the settings panel takes the freed area, arriving from
 * {@link Config#direction}. Progress depends on elapsed time only, never on frame count, and an
 * interrupted transition reverses from where it is. Pure Java: no rendering types.
 */
public final class SettingsTransition {
   public enum Easing {
      LINEAR("Linear"),
      EASE_OUT("Ease Out"),
      EASE_IN_OUT("Ease In Out"),
      EASE_OUT_STRONG("Ease Out Strong"),
      OVERSHOOT("Overshoot");

      public final String label;

      Easing(String label) {
         this.label = label;
      }

      public static Easing byLabel(String label) {
         for (Easing easing : values()) {
            if (easing.label.equals(label)) {
               return easing;
            }
         }
         return EASE_OUT;
      }
   }

   public enum Direction {
      TOP("From Top"),
      BOTTOM("From Bottom"),
      LEFT("From Left"),
      RIGHT("From Right");

      public final String label;

      Direction(String label) {
         this.label = label;
      }

      public static Direction byLabel(String label) {
         for (Direction direction : values()) {
            if (direction.label.equals(label)) {
               return direction;
            }
         }
         return TOP;
      }
   }

   /**
    * @param durationMs time for a full open or close
    * @param distance travel of the settings panel as a fraction of the screen (0..1)
    * @param startOpacity panel opacity at the start of opening (0..1)
    * @param startScale panel size at the start of opening relative to its final size (0.5..1)
    * @param mainVisible fraction of the module browser that stays on screen (0..1)
    */
   public record Config(
      boolean enabled, float durationMs, Easing easing, float distance, float startOpacity, float startScale, Direction direction, float mainVisible
   ) {
      public static final Config SMOOTH = new Config(true, 320.0F, Easing.EASE_OUT, 0.35F, 0.0F, 0.96F, Direction.TOP, 0.25F);
      public static final Config FAST = new Config(true, 160.0F, Easing.EASE_OUT, 0.2F, 0.2F, 0.98F, Direction.TOP, 0.25F);
      public static final Config SOFT = new Config(true, 520.0F, Easing.EASE_IN_OUT, 0.5F, 0.0F, 0.92F, Direction.TOP, 0.25F);
      public static final Config OFF = new Config(false, 0.0F, Easing.LINEAR, 0.0F, 1.0F, 1.0F, Direction.TOP, 0.25F);

      public Config {
         durationMs = clamp(durationMs, 0.0F, 5000.0F);
         distance = clamp(distance, 0.0F, 1.0F);
         startOpacity = clamp(startOpacity, 0.0F, 1.0F);
         startScale = clamp(startScale, 0.5F, 1.0F);
         mainVisible = clamp(mainVisible, 0.0F, 1.0F);
         easing = easing == null ? Easing.EASE_OUT : easing;
         direction = direction == null ? Direction.TOP : direction;
      }

      /** @return the named preset, or {@code null} for "Custom" and unknown names. */
      public static Config preset(String name) {
         if (name == null) {
            return null;
         }
         return switch (name) {
            case "Smooth" -> SMOOTH;
            case "Fast" -> FAST;
            case "Soft" -> SOFT;
            case "Off" -> OFF;
            default -> null;
         };
      }
   }

   /** Where things are for one frame. Coordinates are pixels. */
   public record Layout(float mainShiftX, float panelX, float panelY, float panelW, float panelH, float panelAlpha) {
   }

   private float from;
   private float target;
   private long startNanos;
   private boolean started;

   /** Starts moving towards open or closed. Calling it again with the same target is a no-op. */
   public void setOpen(boolean open, long nowNanos, Config config) {
      float next = open ? 1.0F : 0.0F;
      if (this.started && next == this.target) {
         return;
      }
      this.from = this.linear(nowNanos, config);
      this.target = next;
      this.startNanos = nowNanos;
      this.started = true;
   }

   /** Drops any running animation and rests closed (used when the view is left). */
   public void snapClosed() {
      this.from = 0.0F;
      this.target = 0.0F;
      this.started = false;
   }

   public boolean opening() {
      return this.target == 1.0F;
   }

   /** Un-eased progress, 0 (closed) to 1 (open). */
   public float linear(long nowNanos, Config config) {
      if (!this.started) {
         return 0.0F;
      }
      float span = Math.abs(this.target - this.from);
      if (!config.enabled() || config.durationMs() <= 0.0F || span <= 0.0F) {
         return this.target;
      }
      // A reversed transition only needs the time for the distance it still has to cover.
      double needed = (double)config.durationMs() * 1000000.0 * (double)span;
      double t = Math.max(0.0, Math.min(1.0, (double)(nowNanos - this.startNanos) / needed));
      return this.from + (this.target - this.from) * (float)t;
   }

   /** Eased progress. Only {@link Easing#OVERSHOOT} can exceed 1 briefly. */
   public float progress(long nowNanos, Config config) {
      return ease(config.easing(), this.linear(nowNanos, config));
   }

   public boolean fullyClosed(long nowNanos, Config config) {
      return this.target == 0.0F && this.linear(nowNanos, config) <= 0.0F;
   }

   public boolean settled(long nowNanos, Config config) {
      return this.linear(nowNanos, config) == this.target;
   }

   public static float ease(Easing easing, float t) {
      t = clamp(t, 0.0F, 1.0F);
      float inv = 1.0F - t;
      return switch (easing) {
         case LINEAR -> t;
         case EASE_OUT -> 1.0F - inv * inv * inv;
         case EASE_IN_OUT -> t < 0.5F ? 4.0F * t * t * t : 1.0F - 4.0F * inv * inv * inv;
         case EASE_OUT_STRONG -> 1.0F - inv * inv * inv * inv * inv;
         case OVERSHOOT -> 1.0F + 2.70158F * (t - 1.0F) * (t - 1.0F) * (t - 1.0F) + 1.70158F * (t - 1.0F) * (t - 1.0F);
      };
   }

   /**
    * @param eased value from {@link #progress}
    * @param bottomReserved pixels kept free at the bottom (navigation bar)
    * @param margin gap around the settings panel
    */
   public static Layout layout(Config config, float eased, float screenW, float screenH, float bottomReserved, float margin) {
      float visible = config.mainVisible();
      float shown = clamp(eased, 0.0F, 1.0F);
      float mainShiftX = -(1.0F - visible) * screenW * shown;
      float finalX = visible * screenW + margin;
      float finalW = Math.max(1.0F, screenW - finalX - margin);
      float finalY = margin;
      float finalH = Math.max(1.0F, screenH - bottomReserved - margin * 2.0F);
      float scale = config.startScale() + (1.0F - config.startScale()) * shown;
      float w = finalW * scale;
      float h = finalH * scale;
      float x = finalX + (finalW - w) * 0.5F;
      float y = finalY + (finalH - h) * 0.5F;
      // Uses the raw eased value so OVERSHOOT travels slightly past the rest position and settles back.
      float remaining = 1.0F - eased;
      switch (config.direction()) {
         case TOP -> y -= config.distance() * screenH * remaining;
         case BOTTOM -> y += config.distance() * screenH * remaining;
         case LEFT -> x -= config.distance() * screenW * remaining;
         case RIGHT -> x += config.distance() * screenW * remaining;
      }
      float alpha = config.startOpacity() + (1.0F - config.startOpacity()) * shown;
      return new Layout(mainShiftX, x, y, w, h, alpha);
   }

   private static float clamp(float value, float min, float max) {
      return value < min ? min : Math.min(value, max);
   }
}
