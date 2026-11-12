package net.albiz.client.easyplace.builder;

import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.albiz.client.easyplace.util.BlockStateUtil;
import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2199;
import net.minecraft.class_2231;
import net.minecraft.class_2241;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2276;
import net.minecraft.class_2309;
import net.minecraft.class_2312;
import net.minecraft.class_2338;
import net.minecraft.class_2341;
import net.minecraft.class_2346;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2363;
import net.minecraft.class_2366;
import net.minecraft.class_2377;
import net.minecraft.class_2399;
import net.minecraft.class_2406;
import net.minecraft.class_2426;
import net.minecraft.class_2457;
import net.minecraft.class_2458;
import net.minecraft.class_2459;
import net.minecraft.class_2465;
import net.minecraft.class_2510;
import net.minecraft.class_2530;
import net.minecraft.class_2551;
import net.minecraft.class_2555;
import net.minecraft.class_2577;
import net.minecraft.class_2665;
import net.minecraft.class_2680;
import net.minecraft.class_2738;
import net.minecraft.class_2741;
import net.minecraft.class_2760;
import net.minecraft.class_2768;
import net.minecraft.class_2769;
import net.minecraft.class_2771;
import net.minecraft.class_3486;
import net.minecraft.class_3708;
import net.minecraft.class_3715;
import net.minecraft.class_3718;
import net.minecraft.class_4481;
import net.minecraft.class_638;
import net.minecraft.class_8810;
import net.minecraft.class_2350.class_2351;
import net.minecraft.class_2350.class_2352;
import net.minecraft.class_2350.class_2353;

@Environment(EnvType.CLIENT)
final class BuildOrder {
   static final int STAGE_NORMAL = 0;
   static final int STAGE_PISTON = 1;
   static final int STAGE_OBSERVER = 2;
   static final int STAGE_POWER = 3;
   private static final int MAX_TRACK = 512;

   private BuildOrder() {
   }

   static boolean powerOrTnt(class_2680 state) {
      class_2248 block = state.method_26204();
      return block == class_2246.field_10002 || block instanceof class_2459 || block instanceof class_2309 || block instanceof class_2530;
   }

   static boolean late(WorldSchematic schematic, class_2338 pos, class_2680 expected, boolean machineLast) {
      return stage(schematic, pos, expected, machineLast) > 0;
   }

   static int stage(WorldSchematic schematic, class_2338 pos, class_2680 expected, boolean machineLast) {
      int own = ownStage(schematic, pos, expected, machineLast);
      if (own != 0) {
         return own;
      } else {
         class_2338 support = hangsOn(expected, pos);
         return support == null ? 0 : ownStage(schematic, support, schematic.method_8320(support), machineLast);
      }
   }

   private static int ownStage(WorldSchematic schematic, class_2338 pos, class_2680 expected, boolean machineLast) {
      if (powerOrTnt(expected) || pushedIntoTnt(schematic, pos, expected) != null) {
         return 3;
      } else if (!machineLast) {
         return 0;
      } else {
         class_2248 block = expected.method_26204();
         if (block instanceof class_2665) {
            return 1;
         } else {
            return block instanceof class_2426 ? 2 : 0;
         }
      }
   }

   static boolean pistonWouldFire(class_638 level, class_2338 pos) {
      return level.method_49803(pos) || level.method_49803(pos.method_10084());
   }

   static class_2338 pushedIntoTnt(WorldSchematic schematic, class_2338 pos, class_2680 expected) {
      if (!expected.method_26215() && !(expected.method_26204() instanceof class_2530)) {
         for (class_2350 way : class_2350.values()) {
            class_2680 behind = schematic.method_8320(pos.method_10093(way.method_10153()));
            if (behind.method_26204() instanceof class_2665 && behind.method_11654(class_2741.field_12525) == way) {
               class_2338 ahead = pos.method_10093(way);
               if (schematic.method_8320(ahead).method_26204() instanceof class_2530) {
                  return ahead;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static class_2338 railBefore(class_638 level, WorldSchematic schematic, class_2338 pos, class_2680 expected, Predicate<class_2338> gaveUp) {
      List<class_2338> track = track(schematic, pos);
      int index = track.indexOf(pos);
      if (index <= 0) {
         return null;
      } else {
         class_2338 before = track.get(index - 1);
         return !gaveUp.test(before) && !(level.method_8320(before).method_26204() instanceof class_2241) ? before : null;
      }
   }

   static boolean railShapeOff(class_2680 actual, class_2680 expected) {
      class_2768 want = railShape(expected);
      class_2768 got = railShape(actual);
      return want != null && got != null && want != got;
   }

   private static List<class_2338> track(WorldSchematic schematic, class_2338 pos) {
      Set<class_2338> seen = new HashSet<>();
      List<class_2338> stack = new ArrayList<>(List.of(pos));
      class_2338 start = null;

      while (!stack.isEmpty() && seen.size() < 512) {
         class_2338 at = stack.remove(stack.size() - 1);
         if (seen.add(at)) {
            List<class_2338> links = links(schematic, at);
            if (links.size() < 2 && (start == null || before(at, start))) {
               start = at;
            }

            stack.addAll(links);
         }
      }

      if (start == null) {
         for (class_2338 rail : seen) {
            if (start == null || before(rail, start)) {
               start = rail;
            }
         }
      }

      List<class_2338> order = new ArrayList<>();
      Set<class_2338> laid = new HashSet<>();
      class_2338 at = start;

      while (at != null && laid.add(at)) {
         order.add(at);
         class_2338 next = null;

         for (class_2338 link : links(schematic, at)) {
            if (!laid.contains(link) && (next == null || before(link, next))) {
               next = link;
            }
         }

         at = next;
      }

      return order;
   }

   private static boolean before(class_2338 a, class_2338 b) {
      if (a.method_10264() != b.method_10264()) {
         return a.method_10264() < b.method_10264();
      } else {
         return a.method_10263() != b.method_10263() ? a.method_10263() < b.method_10263() : a.method_10260() < b.method_10260();
      }
   }

   private static List<class_2338> links(WorldSchematic schematic, class_2338 pos) {
      List<class_2338> out = new ArrayList<>(2);
      class_2768 shape = railShape(schematic.method_8320(pos));
      if (shape == null) {
         return out;
      } else {
         for (class_2338 end : ends(pos, shape)) {
            class_2338 other = railAt(schematic, end);
            if (other != null) {
               class_2768 otherShape = railShape(schematic.method_8320(other));

               for (class_2338 back : ends(other, otherShape)) {
                  if (pos.equals(railAt(schematic, back))) {
                     out.add(other);
                     break;
                  }
               }
            }
         }

         return out;
      }
   }

   private static class_2338 railAt(WorldSchematic schematic, class_2338 cell) {
      if (railShape(schematic.method_8320(cell)) != null) {
         return cell;
      } else {
         return railShape(schematic.method_8320(cell.method_10074())) != null ? cell.method_10074() : null;
      }
   }

   private static List<class_2338> ends(class_2338 pos, class_2768 shape) {
      return switch (shape) {
         case field_12665 -> List.of(pos.method_10095(), pos.method_10072());
         case field_12674 -> List.of(pos.method_10078(), pos.method_10067());
         case field_12667 -> List.of(pos.method_10078().method_10084(), pos.method_10067());
         case field_12666 -> List.of(pos.method_10067().method_10084(), pos.method_10078());
         case field_12670 -> List.of(pos.method_10095().method_10084(), pos.method_10072());
         case field_12668 -> List.of(pos.method_10072().method_10084(), pos.method_10095());
         case field_12664 -> List.of(pos.method_10072(), pos.method_10078());
         case field_12671 -> List.of(pos.method_10072(), pos.method_10067());
         case field_12672 -> List.of(pos.method_10095(), pos.method_10067());
         case field_12663 -> List.of(pos.method_10095(), pos.method_10078());
         default -> throw new MatchException(null, null);
      };
   }

   private static class_2768 railShape(class_2680 state) {
      if (state.method_26204() instanceof class_2241 rail) {
         class_2769 var3 = rail.method_9474();
         return state.method_28498(var3) ? (class_2768)state.method_11654(var3) : null;
      } else {
         return null;
      }
   }

   static boolean lookOnly(class_2680 state) {
      class_2248 block = state.method_26204();
      return block instanceof class_2510
         || block instanceof class_2363
         || block instanceof class_3708
         || block instanceof class_2465
         || block instanceof class_2276
         || block instanceof class_2366
         || block instanceof class_2349
         || block instanceof class_2406
         || block instanceof class_3718
         || block instanceof class_3715
         || block instanceof class_4481
         || block instanceof class_2199;
   }

   static List<class_2680> turnedVariants(class_2680 state) {
      List<class_2680> out = new ArrayList<>();

      for (class_2769<?> property : List.of(class_2741.field_12525, class_2741.field_12481, class_2741.field_12496, class_2741.field_12529)) {
         if (state.method_28498(property)) {
            for (Object value : property.method_11898()) {
               class_2680 turned = with(state, property, value);
               if (!turned.equals(state)) {
                  out.add(turned);
               }
            }
            break;
         }
      }

      return out;
   }

   private static class_2680 with(class_2680 state, class_2769 property, Object value) {
      return (class_2680)state.method_11657(property, (Comparable)value);
   }

   static class_2338 hangsOn(class_2680 state, class_2338 pos) {
      class_2248 block = state.method_26204();
      if (block instanceof class_2555 || block instanceof class_2458 || block instanceof class_2551 || block instanceof class_2399) {
         return pos.method_10093(((class_2350)state.method_11654(class_2741.field_12481)).method_10153());
      } else if (block instanceof class_2341) {
         return switch ((class_2738)state.method_11654(class_2741.field_12555)) {
            case field_12475 -> pos.method_10074();
            case field_12473 -> pos.method_10084();
            case field_12471 -> pos.method_10093(((class_2350)state.method_11654(class_2741.field_12481)).method_10153());
            default -> throw new MatchException(null, null);
         };
      } else {
         return !(block instanceof class_2457)
               && !(block instanceof class_2312)
               && !(block instanceof class_2241)
               && !(block instanceof class_2577)
               && !(block instanceof class_2231)
               && !(block instanceof class_8810)
            ? null
            : pos.method_10074();
      }
   }

   static List<class_2338> supportCells(class_2680 expected, class_2338 pos) {
      class_2350 look = StateRules.lookFor(expected);
      if (look == null) {
         look = StateRules.flatLookFor(expected);
      }

      if (look != null) {
         List<class_2338> cells = new ArrayList<>(List.of(pos.method_10093(look), pos.method_10074()));

         for (class_2350 side : class_2353.field_11062) {
            if (side != look) {
               cells.add(pos.method_10093(side));
            }
         }

         if (look != class_2350.field_11036) {
            cells.add(pos.method_10084());
         }

         return cells;
      } else {
         class_2248 block = expected.method_26204();
         if (block instanceof class_2377) {
            class_2350 spout = (class_2350)expected.method_11654(class_2741.field_12545);
            return spout == class_2350.field_11033 ? List.of(pos.method_10074(), pos.method_10084()) : List.of(pos.method_10093(spout));
         } else if (hangsOn(expected, pos) != null) {
            return List.of();
         } else if (expected.method_28498(class_2741.field_12496)) {
            class_2351 axis = (class_2351)expected.method_11654(class_2741.field_12496);
            return List.of(
               pos.method_10093(class_2350.method_10169(axis, class_2352.field_11060)), pos.method_10093(class_2350.method_10169(axis, class_2352.field_11056))
            );
         } else if (expected.method_28498(class_2741.field_12485)) {
            return expected.method_11654(class_2741.field_12485) == class_2771.field_12679 ? List.of(pos.method_10084()) : List.of(pos.method_10074());
         } else if (expected.method_28498(class_2741.field_12518)) {
            return expected.method_11654(class_2741.field_12518) == class_2760.field_12619 ? List.of(pos.method_10084()) : List.of(pos.method_10074());
         } else {
            return List.of(pos.method_10074(), pos.method_10095(), pos.method_10072(), pos.method_10078(), pos.method_10067(), pos.method_10084());
         }
      }
   }

   static boolean waits(class_638 level, WorldSchematic schematic, class_2338 pos, class_2680 expected, Predicate<class_2338> gaveUp, boolean machineLast) {
      class_2248 block = expected.method_26204();
      class_2338 tnt = pushedIntoTnt(schematic, pos, expected);
      if (tnt != null && !(level.method_8320(tnt).method_26204() instanceof class_2530)) {
         return true;
      } else if (block instanceof class_2665) {
         return pistonWouldFire(level, pos);
      } else if (block instanceof class_2241 && railBefore(level, schematic, pos, expected, gaveUp) != null) {
         return true;
      } else if (block instanceof class_2426) {
         class_2338 watched = pos.method_10093((class_2350)expected.method_11654(class_2741.field_12525));
         class_2680 wanted = schematic.method_8320(watched);
         if (BlockStateUtil.requiredItem(wanted) == null || stage(schematic, watched, wanted, machineLast) > 2 || gaveUp.test(watched)) {
            return false;
         } else {
            return wanted.method_26204() instanceof class_2426 && watched.method_10093((class_2350)wanted.method_11654(class_2741.field_12525)).equals(pos)
               ? false
               : !StateRules.placed(level.method_8320(watched), wanted);
         }
      } else if (block instanceof class_2346) {
         return class_2346.method_10128(level.method_8320(pos.method_10074()));
      } else if (!expected.method_28498(class_2741.field_12508)
         && expected.method_26227().method_15771()
         && expected.method_26227().method_15767(class_3486.field_15517)) {
         return !level.method_8316(pos).method_15771() || !level.method_8316(pos).method_15767(class_3486.field_15517);
      } else {
         return block instanceof class_2530 ? level.method_49803(pos) : false;
      }
   }
}
