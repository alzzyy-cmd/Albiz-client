package net.albiz.client.easyplace.util;

import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2312;
import net.minecraft.class_2315;
import net.minecraft.class_2323;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2363;
import net.minecraft.class_2426;
import net.minecraft.class_2429;
import net.minecraft.class_2482;
import net.minecraft.class_2510;
import net.minecraft.class_2533;
import net.minecraft.class_2541;
import net.minecraft.class_2665;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2746;
import net.minecraft.class_2769;
import net.minecraft.class_2771;
import net.minecraft.class_3708;
import net.minecraft.class_5000;
import net.minecraft.class_5778;
import net.minecraft.class_8886;

@Environment(EnvType.CLIENT)
public final class StateRules {
   private static final Set<class_2769<?>> PLACED = Set.of(
      class_2741.field_12525,
      class_2741.field_12481,
      class_2741.field_12545,
      class_2741.field_28062,
      class_2741.field_12496,
      class_2741.field_12529,
      class_2741.field_12518,
      class_2741.field_12533,
      class_2741.field_12485,
      class_2741.field_12532,
      class_2741.field_12555,
      class_2741.field_23333,
      class_2741.field_12520,
      class_2741.field_12506,
      class_2741.field_12483,
      class_2741.field_17104,
      class_2741.field_16561
   );
   private static final Set<class_2769<?>> CLICKED = Set.of(
      class_2741.field_12494, class_2741.field_12534, class_2741.field_12524, class_2741.field_12537, class_2741.field_12501
   );
   private static final Map<class_2248, class_2248> TIPS = Map.of(
      class_2246.field_10463,
      class_2246.field_9993,
      class_2246.field_23079,
      class_2246.field_23078,
      class_2246.field_22124,
      class_2246.field_22123,
      class_2246.field_28676,
      class_2246.field_28675
   );

   private StateRules() {
   }

   public static class_2248 plant(class_2248 block) {
      return TIPS.getOrDefault(block, block);
   }

   public static boolean placed(class_2680 actual, class_2680 expected) {
      if (plant(actual.method_26204()) != plant(expected.method_26204())) {
         return false;
      } else {
         for (class_2769<?> property : expected.method_28501()) {
            if (actual.method_28498(property)
               && (PLACED.contains(property) || faceProperty(expected, property))
               && !actual.method_11654(property).equals(expected.method_11654(property))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean done(class_2680 actual, class_2680 expected) {
      if (!placed(actual, expected)) {
         return false;
      } else {
         for (class_2769<?> property : expected.method_28501()) {
            if (clickable(expected, property) && actual.method_28498(property) && !actual.method_11654(property).equals(expected.method_11654(property))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean needsClicks(class_2680 actual, class_2680 expected) {
      return placed(actual, expected) && !done(actual, expected);
   }

   public static boolean wouldFit(class_2680 wouldBe, class_2680 desired, boolean skipChestType) {
      if (plant(wouldBe.method_26204()) != plant(desired.method_26204())) {
         return false;
      } else {
         for (class_2769<?> property : desired.method_28501()) {
            if (wouldBe.method_28498(property)
               && (!skipChestType || property != class_2741.field_12506)
               && (property != class_2741.field_12485 || desired.method_11654(class_2741.field_12485) != class_2771.field_12682)) {
               if (faceProperty(desired, property)) {
                  if ((Boolean)wouldBe.method_11654(property) && !(Boolean)desired.method_11654(property)) {
                     return false;
                  }
               } else if (PLACED.contains(property) && !wouldBe.method_11654(property).equals(desired.method_11654(property))) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public static boolean slabHalf(class_2680 actual, class_2680 expected) {
      return expected.method_26204() instanceof class_2482
         && actual.method_26204() == expected.method_26204()
         && expected.method_11654(class_2741.field_12485) == class_2771.field_12682
         && actual.method_11654(class_2741.field_12485) != class_2771.field_12682;
   }

   public static int itemsNeeded(class_2680 actual, class_2680 expected) {
      if (expected.method_26204() instanceof class_2482 && expected.method_11654(class_2741.field_12485) == class_2771.field_12682) {
         return slabHalf(actual, expected) ? 1 : 2;
      } else {
         return 1;
      }
   }

   public static boolean clickable(class_2680 expected, class_2769<?> property) {
      if (!CLICKED.contains(property)) {
         return false;
      } else if (property != class_2741.field_12537) {
         return true;
      } else {
         class_2248 block = expected.method_26204();
         return block != class_2246.field_9973 && block != class_2246.field_10453
            ? block instanceof class_2323 || block instanceof class_2533 || block instanceof class_2349
            : false;
      }
   }

   public static class_2350 lookFor(class_2680 expected) {
      class_2248 block = expected.method_26204();
      if (block instanceof class_8886) {
         return ((class_5000)expected.method_11654(class_2741.field_23333)).method_26426().method_10153();
      } else if (block instanceof class_2426) {
         return (class_2350)expected.method_11654(class_2741.field_12525);
      } else {
         return !(block instanceof class_2665) && !(block instanceof class_2315) && !(block instanceof class_3708)
            ? null
            : ((class_2350)expected.method_11654(class_2741.field_12525)).method_10153();
      }
   }

   public static class_2350 flatLookFor(class_2680 expected) {
      if (!expected.method_28498(class_2741.field_12481)) {
         return null;
      } else {
         class_2248 block = expected.method_26204();
         class_2350 facing = (class_2350)expected.method_11654(class_2741.field_12481);
         if (block instanceof class_2281 || block instanceof class_2363 || block instanceof class_2312) {
            return facing.method_10153();
         } else {
            return !(block instanceof class_2510) && !(block instanceof class_2323) && !(block instanceof class_2349) ? null : facing;
         }
      }
   }

   private static boolean faceProperty(class_2680 state, class_2769<?> property) {
      class_2248 block = state.method_26204();
      return (block instanceof class_5778 || block instanceof class_2541) && property instanceof class_2746 && class_2429.field_11329.containsValue(property);
   }
}
