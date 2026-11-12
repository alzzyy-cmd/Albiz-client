package net.albiz.client.module.impl.combat.bot;

import java.util.ArrayDeque;
import net.albiz.client.module.impl.combat.crystal.Crystals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
final class BotPause {
   private static final int TOUCH_TICKS = 30;
   private static final int RESPAWN_TICKS = 30;
   private static final int WORLD_TICKS = 40;
   private static final int SETBACK_TICKS = 10;
   private static final int SETBACK_LIMIT = 6;
   private static final long SETBACK_SPAN_MS = 10000L;
   private static final double SETBACK_MAX_JUMP = 6.0;
   private static final double CURSOR_SLACK = 1.5;
   private final ArrayDeque<Long> setbacks = new ArrayDeque<>();
   private class_746 body;
   private class_638 world;
   private double cursorX = Double.NaN;
   private double cursorY = Double.NaN;
   private int tick;
   private int touchedUntil;
   private int heldUntil;
   private class_243 lastPosition;
   private long timeAt;
   private long gameTimeAt = -1L;
   private float tps = 20.0F;

   void reset() {
      this.setbacks.clear();
      this.body = null;
      this.world = null;
      this.cursorX = this.cursorY = Double.NaN;
      this.tick = this.touchedUntil = this.heldUntil = 0;
      this.lastPosition = null;
      this.gameTimeAt = -1L;
      this.tps = 20.0F;
   }

   BotPause.State tick(class_310 mc, class_746 player) {
      this.tick++;
      if (this.body != null && player != this.body) {
         this.heldUntil = Math.max(this.heldUntil, this.tick + (mc.field_1687 != this.world ? 40 : 30));
      }

      this.body = player;
      this.world = mc.field_1687;
      if (this.touched(mc)) {
         this.touchedUntil = this.tick + 30;
      }

      long now = System.currentTimeMillis();

      while (!this.setbacks.isEmpty() && now - this.setbacks.peekFirst() > 10000L) {
         this.setbacks.pollFirst();
      }

      this.lastPosition = player.method_73189();
      if (this.setbacks.size() >= 6) {
         return BotPause.State.STOP;
      } else if (this.tick < this.heldUntil) {
         return BotPause.State.HOLD;
      } else {
         return this.tick < this.touchedUntil ? BotPause.State.YIELD : BotPause.State.RUN;
      }
   }

   void moved(class_243 to, int pearlAge) {
      if (this.lastPosition != null && pearlAge >= 60) {
         if (!(to.method_1022(this.lastPosition) > 6.0)) {
            this.setbacks.add(System.currentTimeMillis());
            this.heldUntil = Math.max(this.heldUntil, this.tick + 10);
         }
      }
   }

   void time(long gameTime) {
      long now = System.currentTimeMillis();
      if (this.gameTimeAt >= 0L && now > this.timeAt && gameTime > this.gameTimeAt) {
         float measured = (float)(gameTime - this.gameTimeAt) * 1000.0F / (float)(now - this.timeAt);
         this.tps = Math.min(20.0F, this.tps + (measured - this.tps) * 0.5F);
      }

      this.gameTimeAt = gameTime;
      this.timeAt = now;
   }

   boolean slowServer() {
      return this.tps < 15.0F;
   }

   int setbacksLately() {
      return this.setbacks.size();
   }

   private boolean touched(class_310 mc) {
      if (mc.field_1755 == null && mc.method_1569()) {
         double x = mc.field_1729.method_1603();
         double y = mc.field_1729.method_1604();
         boolean moved = !Double.isNaN(this.cursorX) && (Math.abs(x - this.cursorX) > 1.5 || Math.abs(y - this.cursorY) > 1.5);
         this.cursorX = x;
         this.cursorY = y;
         if (moved) {
            return true;
         } else {
            class_304[] keys = new class_304[]{
               mc.field_1690.field_1894,
               mc.field_1690.field_1881,
               mc.field_1690.field_1913,
               mc.field_1690.field_1849,
               mc.field_1690.field_1903,
               mc.field_1690.field_1832,
               mc.field_1690.field_1886,
               mc.field_1690.field_1904
            };

            for (class_304 key : keys) {
               if (Crystals.isPhysicallyDown(mc, key)) {
                  return true;
               }
            }

            for (class_304 keyx : mc.field_1690.field_1852) {
               if (Crystals.isPhysicallyDown(mc, keyx)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         this.cursorX = this.cursorY = Double.NaN;
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   static enum State {
      RUN,
      YIELD,
      HOLD,
      STOP;
   }
}
