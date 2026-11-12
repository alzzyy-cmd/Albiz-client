package net.albiz.client.module.impl.combat.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_2350.class_2353;

@Environment(EnvType.CLIENT)
final class StandPicker {
   private static final float BLAST_PROOF = 600.0F;
   private static final double[] RADII = new double[]{1.5, 3.0, 4.0};
   private static final int HOLE_SEARCH = 4;
   private static final int MAX_HOLES = 6;

   private StandPicker() {
   }

   static StandPicker.Spot pick(class_1937 level, class_1657 self, class_1657 enemy, double min, double ideal, double max, double cover) {
      class_2338 here = self.method_24515();
      List<class_2338> candidates = new ArrayList<>();
      candidates.add(here);

      for (double radius : RADII) {
         for (int i = 0; i < 8; i++) {
            double angle = (Math.PI / 4) * i;
            candidates.add(
               class_2338.method_49637(self.method_23317() + Math.cos(angle) * radius, self.method_23318(), self.method_23321() + Math.sin(angle) * radius)
            );
         }
      }

      candidates.addAll(holesNear(level, here));
      StandPicker.Spot best = null;
      double stay = Double.NEGATIVE_INFINITY;

      for (class_2338 raw : candidates) {
         class_2338 feet = standable(level, raw);
         if (feet != null) {
            double score = score(level, self, enemy, feet, min, ideal, max, cover);
            if (feet.equals(here)) {
               stay = Math.max(stay, score);
            }

            if (best == null || score > best.score()) {
               best = new StandPicker.Spot(feet, score);
            }
         }
      }

      return best != null && !best.feet().equals(here) && !(best.score() < stay + 0.5) ? best : null;
   }

   static double hole(class_1937 level, class_2338 feet) {
      int closed = 0;

      for (class_2350 side : class_2353.field_11062) {
         if (level.method_8320(feet.method_10093(side)).method_26204().method_9520() >= 600.0F) {
            closed++;
         }
      }

      return closed == 4 ? 1.0 : (closed == 3 ? 0.4 : 0.0);
   }

   private static double score(class_1937 level, class_1657 self, class_1657 enemy, class_2338 feet, double min, double ideal, double max, double cover) {
      class_243 at = class_243.method_24955(feet);
      double distance = Math.hypot(enemy.method_23317() - at.field_1352, enemy.method_23321() - at.field_1350);
      if (!(distance < min - 0.5) && !(distance > max + 1.5)) {
         double travel = Math.hypot(self.method_23317() - at.field_1352, self.method_23321() - at.field_1350);
         return -Math.abs(distance - ideal) + 3.0 * cover * hole(level, feet) - 0.15 * travel;
      } else {
         return -10.0 - Math.abs(distance - ideal);
      }
   }

   static class_2338 standable(class_1937 level, class_2338 pos) {
      for (int dy : new int[]{0, -1, 1}) {
         class_2338 feet = pos.method_10086(dy);
         class_2338 floor = feet.method_10074();
         class_2680 under = level.method_8320(floor);
         if (!under.method_26220(level, floor).method_1110()
            && !harmful(under)
            && !under.method_27852(class_2246.field_10092)
            && clear(level, feet)
            && clear(level, feet.method_10084())) {
            return feet;
         }
      }

      return null;
   }

   private static boolean clear(class_1937 level, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      return state.method_26220(level, pos).method_1110() && !harmful(state) && level.method_8316(pos).method_15769();
   }

   private static boolean harmful(class_2680 state) {
      return state.method_27852(class_2246.field_10164)
         || state.method_26164(class_3481.field_21952)
         || state.method_27852(class_2246.field_10029)
         || state.method_27852(class_2246.field_16999)
         || state.method_27852(class_2246.field_10343)
         || state.method_27852(class_2246.field_27879);
   }

   private static List<class_2338> holesNear(class_1937 level, class_2338 here) {
      List<class_2338> holes = new ArrayList<>();

      for (class_2338 pos : class_2338.method_10097(here.method_10069(-4, -1, -4), here.method_10069(4, 1, 4))) {
         if (hole(level, pos) >= 1.0 && standable(level, pos) != null && standable(level, pos).equals(pos)) {
            holes.add(pos.method_10062());
         }
      }

      holes.sort(Comparator.comparingDouble(posx -> posx.method_10262(here)));
      return holes.size() > 6 ? holes.subList(0, 6) : holes;
   }

   @Environment(EnvType.CLIENT)
   record Spot(class_2338 feet, double score) {
      class_243 center() {
         return class_243.method_24955(this.feet);
      }
   }
}
