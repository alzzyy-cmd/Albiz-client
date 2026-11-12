package net.albiz.client.module.impl.combat.bot;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10584;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_5134;
import net.minecraft.class_746;
import net.minecraft.class_1304.class_1305;
import net.minecraft.class_1322.class_1323;

@Environment(EnvType.CLIENT)
final class AirTrack {
   private static final double GRAVITY = 0.08;
   private static final double VERTICAL_DRAG = 0.98;
   private static final double AIR_DRAG = 0.91;
   private static final double HIT_KNOCK = 0.4;
   private static final double SPRINT_KNOCK = 0.5;
   private static final double MAX_LIFT = 0.4;
   private static final double KEY_PUSH = 0.026;
   private static final double MAX_PUSH = 0.045;
   private static final double MAX_LEARNED = 0.1;
   static final int LAND_TICKS = 40;
   private static final int KICK_WAIT = 8;
   private static final double BLAST_NEAR = 10.0;
   private class_1657 target;
   private RivalBook.Rival rival;
   private final List<AirTrack.Sample> flight = new ArrayList<>();
   private class_243 lastSeen;
   private boolean airborne;
   private class_243 pushSum = class_243.field_1353;
   private int pushes;
   private class_243 early;
   private class_243 kickFrom;
   private class_243 kick;
   private class_243 kickWay;
   private int kickAt = -1;
   private boolean kickSeen;
   private int kickSeenAt;
   private boolean kickSpoiled;
   AirTrack.Landing bare;
   private double gain;
   AirTrack.Landing guessed;

   void forget() {
      this.target = null;
      this.rival = null;
      this.flight.clear();
      this.lastSeen = null;
      this.airborne = false;
      this.pushSum = class_243.field_1353;
      this.pushes = 0;
      this.kickAt = -1;
      this.bare = this.guessed = null;
   }

   void note(class_1657 target, int tick, RivalBook.Rival rival) {
      if (target != this.target) {
         this.forget();
         this.target = target;
      }

      this.rival = rival;
      class_243 at = server(target);
      boolean fresh = !at.equals(this.lastSeen);
      this.lastSeen = at;
      if (this.kickAt >= 0 && !this.kickSeen) {
         if (at.field_1351 > this.kickFrom.field_1351 + 0.05 || at.method_1020(this.kickFrom).method_37268() > 0.01) {
            this.kickSeen = true;
            this.kickSeenAt = tick;
         } else if (tick - this.kickAt > 8) {
            this.kickAt = -1;
         }
      }

      if (this.kickAt >= 0 && tick - this.kickAt > 40) {
         this.kickAt = -1;
      }

      boolean up = !target.method_24828() && !floating(target);
      if (up) {
         if (!this.airborne) {
            this.airborne = true;
            this.restart();
         }

         if (fresh) {
            this.flight.add(new AirTrack.Sample(tick, at));
            if (this.flight.size() == 2) {
               this.readEarly();
            }

            this.read();
         }
      } else if (this.airborne) {
         this.airborne = false;
         if (this.kickAt >= 0 && this.kickSeen) {
            this.learn(at);
         }

         this.kickAt = -1;
         this.restart();
      }
   }

   void kicked(class_746 self, class_1657 target, float yaw, boolean sprinting, int tick) {
      if (target == this.target) {
         this.kickAt = -1;
         double keep = 1.0 - resistance(target);
         if (!(keep <= 0.0)) {
            class_243 from = server(target);
            class_243 away = from.method_1020(self.method_73189()).method_18805(1.0, 0.0, 1.0);
            if (!(away.method_1027() < 1.0E-6)) {
               away = away.method_1029();
               class_243 sideways = away.method_1021(0.4 * keep);
               double lift = Math.min(0.4, 0.4 * keep);
               if (sprinting) {
                  double radians = Math.toRadians(yaw);
                  class_243 look = new class_243(-Math.sin(radians), 0.0, Math.cos(radians));
                  sideways = sideways.method_1021(0.5).method_1019(look.method_1021(0.5 * keep));
                  lift = Math.min(0.4, lift / 2.0 + 0.5 * keep);
               }

               this.kick = new class_243(sideways.field_1352, lift, sideways.field_1350);
               this.kickWay = sideways.method_1027() < 1.0E-6 ? away : sideways.method_1029();
               this.kickFrom = from;
               this.kickAt = tick;
               this.kickSeen = this.kickSpoiled = false;
               this.early = null;
               class_1937 level = target.method_73183();
               AirTrack.Stop still = fly(level, target, from, this.kick, class_243.field_1353, true, 40);
               AirTrack.Stop nudged = fly(level, target, from, this.kick, this.kickWay.method_1021(0.01), true, 40);
               this.bare = still.down() ? new AirTrack.Landing(still.at(), still.ticks()) : null;
               this.gain = still.down() && nudged.down() ? nudged.at().method_1020(still.at()).method_1026(this.kickWay) / 0.01 : 0.0;
               AirTrack.Stop usual = fly(level, target, from, this.kick, this.learned(0.1), true, 40);
               this.guessed = usual.down() ? new AirTrack.Landing(usual.at(), usual.ticks()) : null;
            }
         }
      }
   }

   void blast(class_243 center) {
      if (this.target != null && !(server(this.target).method_1022(center) > 10.0)) {
         this.kickSpoiled = true;
         if (!this.kickSeen) {
            this.kickAt = -1;
         }

         this.restart();
      }
   }

   AirTrack.Landing landing(int tick) {
      AirTrack.Stop stop = this.path(40, tick);
      return stop != null && stop.down() ? new AirTrack.Landing(stop.at(), stop.ticks()) : null;
   }

   class_243 lead(int ahead, int tick) {
      if (this.target == null || ahead <= 0) {
         return null;
      } else if (this.kickAt < 0 && this.pushes < 2) {
         return null;
      } else {
         AirTrack.Stop stop = this.path(ahead, tick);
         return stop == null ? null : stop.at().method_1020(this.target.method_73189());
      }
   }

   private AirTrack.Stop path(int ahead, int tick) {
      if (this.target != null && !floating(this.target)) {
         class_1937 level = this.target.method_73183();
         class_243 velocity = this.airborne ? this.velocity() : null;
         if (this.kickAt < 0 || this.kickSeen && velocity != null) {
            if (velocity == null) {
               return null;
            } else {
               AirTrack.Sample now = this.flight.get(this.flight.size() - 1);
               int age = Math.min(3, tick - now.tick());
               AirTrack.Stop stop = fly(level, this.target, now.at(), velocity, this.push(velocity), false, ahead + age);
               return new AirTrack.Stop(stop.at(), Math.max(0, stop.ticks() - age), stop.down());
            }
         } else {
            int gone = this.kickSeen ? tick - this.kickSeenAt + 1 : 0;
            AirTrack.Stop stop = fly(level, this.target, this.kickFrom, this.kick, this.learned(0.1), true, ahead + gone);
            return new AirTrack.Stop(stop.at(), Math.max(0, stop.ticks() - gone), stop.down());
         }
      } else {
         return null;
      }
   }

   private class_243 push(class_243 velocity) {
      if (this.pushes >= 2) {
         return limit(this.pushSum.method_1021(1.0 / this.pushes), 0.045);
      } else {
         boolean known = this.kickAt >= 0 && this.rival != null && this.rival.knowsFlight();
         class_243 usual = this.kickAt >= 0
            ? this.learned(0.026)
            : limit(new class_243(velocity.field_1352, 0.0, velocity.field_1350).method_1021(0.08999999999999997), 0.026);
         class_243 seen = this.pushes == 1 ? (this.early != null ? this.pushSum.method_1019(this.early).method_1021(0.5) : this.pushSum) : this.early;
         return seen == null ? usual : limit(known ? seen.method_1019(usual).method_1021(0.5) : seen, 0.045);
      }
   }

   private class_243 learned(double most) {
      return this.rival != null && this.rival.knowsFlight() && this.kickWay != null
         ? limit(this.kickWay.method_1021(this.rival.airAlong).method_1019(right(this.kickWay).method_1021(this.rival.airAcross)), most)
         : class_243.field_1353;
   }

   private void learn(class_243 down) {
      if (this.rival != null && !this.kickSpoiled && this.bare != null && !(this.gain < 1.0)) {
         if (!(Math.abs(down.field_1351 - this.bare.at().field_1351) > 0.6)) {
            class_243 off = down.method_1020(this.bare.at());
            double along = off.method_1026(this.kickWay) / this.gain;
            double across = off.method_1026(right(this.kickWay)) / this.gain;
            if (!(Math.abs(along) > 0.15000000000000002) && !(Math.abs(across) > 0.15000000000000002)) {
               this.rival.flew(along, across);
            }
         }
      }
   }

   private void read() {
      int n = this.flight.size();
      if (n >= 3) {
         AirTrack.Sample a = this.flight.get(n - 3);
         AirTrack.Sample b = this.flight.get(n - 2);
         AirTrack.Sample c = this.flight.get(n - 1);
         int first = b.tick() - a.tick();
         int second = c.tick() - b.tick();
         if (first > 0 && second > 0 && first <= 4 && second <= 4) {
            class_243 before = b.at().method_1020(a.at()).method_1021(1.0 / first);
            class_243 after = c.at().method_1020(b.at()).method_1021(1.0 / second);
            double decay = Math.pow(0.91, (first + second) / 2.0);
            double scale = 0.08999999999999997 / (1.0 - decay);
            class_243 push = new class_243((after.field_1352 - decay * before.field_1352) * scale, 0.0, (after.field_1350 - decay * before.field_1350) * scale);
            if (!(push.method_37267() > 0.045)) {
               this.pushSum = this.pushSum.method_1019(push);
               this.pushes++;
            }
         }
      }
   }

   private void readEarly() {
      if (this.kickAt >= 0 && this.kickSeen && !this.kickSpoiled) {
         AirTrack.Sample b = this.flight.get(0);
         AirTrack.Sample c = this.flight.get(1);
         int span = c.tick() - b.tick();
         if (span > 0 && span <= 4) {
            class_243 mean = c.at().method_1020(b.at()).method_1021(1.0 / span);
            double grip = friction(this.target.method_73183(), this.kickFrom) * 0.91;
            int steps = 20;
            double[] rise = new double[steps + 1];
            double[] carry = new double[steps + 1];
            double[] perPush = new double[steps + 1];
            double y = this.kick.field_1351;
            double h = 1.0;
            double unit = 1.0;

            for (int step = 1; step <= steps; step++) {
               if (step > 1) {
                  double drag = step == 2 ? grip : 0.91;
                  h *= drag;
                  unit = unit * drag + 1.0;
                  y = (y - 0.08) * 0.98;
               }

               rise[step] = y;
               carry[step] = h;
               perPush[step] = unit;
            }

            int best = -1;
            double closest = Double.MAX_VALUE;

            for (int end = span; end <= steps; end++) {
               double off = Math.abs(window(rise, end, span) - mean.field_1351);
               if (off < closest) {
                  closest = off;
                  best = end;
               }
            }

            if (best >= 0 && !(closest > 0.04)) {
               double alone = window(carry, best, span);
               double gain = window(perPush, best, span);
               class_243 push = new class_243(mean.field_1352 - this.kick.field_1352 * alone, 0.0, mean.field_1350 - this.kick.field_1350 * alone)
                  .method_1021(1.0 / gain);
               if (push.method_37267() <= 0.1) {
                  this.early = push;
               }
            }
         }
      }
   }

   private static double window(double[] values, int end, int span) {
      double sum = 0.0;

      for (int i = end - span + 1; i <= end; i++) {
         sum += values[i];
      }

      return sum / span;
   }

   private class_243 velocity() {
      int n = this.flight.size();
      if (n < 2) {
         return null;
      } else {
         AirTrack.Sample b = this.flight.get(n - 2);
         AirTrack.Sample c = this.flight.get(n - 1);
         int span = c.tick() - b.tick();
         if (span > 0 && span <= 4) {
            class_243 mean = c.at().method_1020(b.at()).method_1021(1.0 / span);
            double half = (span - 1) / 2.0;
            double drag = Math.pow(0.91, half);
            return new class_243(mean.field_1352 * drag, mean.field_1351 - 0.0784 * half, mean.field_1350 * drag);
         } else {
            return null;
         }
      }
   }

   private void restart() {
      this.flight.clear();
      this.pushSum = class_243.field_1353;
      this.pushes = 0;
      this.early = null;
   }

   private static AirTrack.Stop fly(class_1937 level, class_1657 body, class_243 from, class_243 velocity, class_243 push, boolean knocked, int ticks) {
      class_238 box = body.method_5829().method_997(from.method_1020(body.method_73189()));
      double grip = knocked ? friction(level, from) * 0.91 : 0.91;
      double x = velocity.field_1352;
      double y = velocity.field_1351;
      double z = velocity.field_1350;
      class_243 at = from;

      for (int tick = 1; tick <= ticks; tick++) {
         if (tick > 1 || !knocked) {
            double drag = knocked && tick == 2 ? grip : 0.91;
            x *= drag;
            z *= drag;
            y = (y - 0.08) * 0.98;
         }

         x += push.field_1352;
         z += push.field_1350;
         class_243 step = class_1297.method_20736(body, new class_243(x, y, z), box, level, List.of());
         box = box.method_997(step);
         at = at.method_1019(step);
         if (step.field_1352 != x) {
            x = 0.0;
         }

         if (step.field_1350 != z) {
            z = 0.0;
         }

         if (step.field_1351 != y) {
            if (y < 0.0) {
               return new AirTrack.Stop(at, tick, true);
            }

            y = 0.0;
         }
      }

      return new AirTrack.Stop(at, ticks, false);
   }

   private static double friction(class_1937 level, class_243 at) {
      return level.method_8320(class_2338.method_49637(at.field_1352, at.field_1351 - 0.5000001, at.field_1350)).method_26204().method_9499();
   }

   private static double resistance(class_1657 target) {
      double[] total = new double[]{0.0};

      for (class_1304 slot : class_1304.values()) {
         if (slot.method_5925() == class_1305.field_6178) {
            target.method_6118(slot).method_57354(slot, (attribute, modifier) -> {
               if (attribute.comp_349() == class_5134.field_23718.comp_349() && modifier.comp_2450() == class_1323.field_6328) {
                  total[0] += modifier.comp_2449();
               }
            });
         }
      }

      return Math.min(1.0, Math.max(0.0, total[0]));
   }

   private static boolean floating(class_1657 target) {
      return target.method_6128()
         || target.method_52535()
         || target.method_6101()
         || target.method_5765()
         || target.method_31549().field_7479
         || target.method_6059(class_1294.field_5902)
         || target.method_6059(class_1294.field_5906);
   }

   private static class_243 server(class_1657 target) {
      class_10584 handler = target.method_66233();
      return handler == null ? target.method_73189() : handler.method_66265();
   }

   private static class_243 right(class_243 way) {
      return new class_243(-way.field_1350, 0.0, way.field_1352);
   }

   private static class_243 limit(class_243 push, double most) {
      double length = push.method_37267();
      return length <= most ? push : push.method_1021(most / length);
   }

   @Environment(EnvType.CLIENT)
   record Landing(class_243 at, int ticks) {
   }

   @Environment(EnvType.CLIENT)
   private record Sample(int tick, class_243 at) {
   }

   @Environment(EnvType.CLIENT)
   private record Stop(class_243 at, int ticks, boolean down) {
   }
}
