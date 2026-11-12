package net.albiz.client.easyplace.util;

import net.albiz.client.easyplace.schematic.TargetStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2742;
import net.minecraft.class_2756;

@Environment(EnvType.CLIENT)
public final class BlockStateUtil {
   private BlockStateUtil() {
   }

   public static boolean statesMatch(class_2680 expected, class_2680 actual) {
      return StateRules.placed(actual, expected) || StateRules.slabHalf(actual, expected);
   }

   public static TargetStatus classify(class_2680 expected, class_2680 actual) {
      if (StateRules.done(actual, expected)) {
         return TargetStatus.CORRECT;
      } else if (StateRules.placed(actual, expected)) {
         return TargetStatus.ADJUST;
      } else {
         return !actual.method_26215() && !actual.method_45474() && !StateRules.slabHalf(actual, expected) ? TargetStatus.WRONG : TargetStatus.MISSING;
      }
   }

   public static class_1792 requiredItem(class_2680 expected) {
      class_1792 item = StateRules.plant(expected.method_26204()).method_8389();
      return item == class_1802.field_8162 ? null : item;
   }

   public static boolean isPrimaryPlacementState(class_2680 state) {
      return state.method_28498(class_2741.field_12533) && state.method_11654(class_2741.field_12533) == class_2756.field_12609
         ? false
         : !state.method_28498(class_2741.field_12483) || state.method_11654(class_2741.field_12483) != class_2742.field_12560;
   }

   public static boolean isSimpleFullBlock(class_2680 state, class_1937 level, class_2338 pos) {
      return state.method_26234(level, pos) && state.method_26204().method_8389() != class_1802.field_8162;
   }
}
