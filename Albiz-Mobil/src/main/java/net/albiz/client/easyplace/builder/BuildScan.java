package net.albiz.client.easyplace.builder;

import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.albiz.client.easyplace.schematic.SchematicManager;
import net.albiz.client.easyplace.schematic.TargetStatus;
import net.albiz.client.easyplace.util.BlockStateUtil;
import net.albiz.client.easyplace.util.StateRules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2377;
import net.minecraft.class_2401;
import net.minecraft.class_2404;
import net.minecraft.class_2530;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_638;
import net.minecraft.class_8886;
import net.minecraft.class_8887;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_2350.class_2353;

@Environment(EnvType.CLIENT)
final class BuildScan {
   static final int NONE = Integer.MAX_VALUE;
   final List<BuildScan.Cell> missing = new ArrayList<>();
   final List<BuildScan.Cell> adjust = new ArrayList<>();
   final List<BuildScan.Liquid> liquids = new ArrayList<>();
   final Set<Long> waiting = new HashSet<>();
   final List<class_2338> poweredTnt = new ArrayList<>();
   final List<class_2338> tntForPlayer = new ArrayList<>();
   final List<class_2338> afterTntForPlayer = new ArrayList<>();
   private final List<BuildScan.Cell> tntCells = new ArrayList<>();
   private boolean powerLeft;
   private final Set<Long> waterBelow = new HashSet<>();
   private final Map<Long, Integer> staged = new HashMap<>();
   private final List<BuildScan.Cell> lateCells = new ArrayList<>();
   private boolean machineLast;
   int late;
   int leversOff;
   final List<class_2338> levers = new ArrayList<>();
   int railsOff;
   final List<class_2338> crafters = new ArrayList<>();
   final Map<class_2338, boolean[]> crafterSlots = new HashMap<>();
   int layer = Integer.MAX_VALUE;
   int wrong;
   int noItem;
   int unloaded;
   boolean wholeSchematic = true;
   private boolean placeTnt;

   static BuildScan run(
      class_638 level,
      WorldSchematic schematic,
      List<SchematicManager.ActiveBox> boxes,
      Predicate<class_2338> deferred,
      Predicate<class_2338> gaveUp,
      Predicate<class_2338> waterPending,
      boolean lateNow,
      Set<Long> scaffold,
      Set<Long> craftersDone,
      boolean wantCrafters,
      boolean placeTnt,
      boolean machineLast
   ) {
      BuildScan scan = new BuildScan();
      scan.placeTnt = placeTnt;
      scan.machineLast = machineLast;
      if (boxes.isEmpty()) {
         return scan;
      } else {
         int minY = Integer.MAX_VALUE;
         int maxY = Integer.MIN_VALUE;

         for (SchematicManager.ActiveBox box : boxes) {
            minY = Math.min(minY, box.minY());
            maxY = Math.max(maxY, box.maxY());
         }

         class_2339 cursor = new class_2339();
         int y = minY;

         do {
            for (scan.wholeSchematic = true; y <= maxY; y++) {
               if (scan.layer != Integer.MAX_VALUE && y > scan.layer + 1) {
                  scan.wholeSchematic = false;
                  break;
               }

               for (SchematicManager.ActiveBox box : boxes) {
                  if (y >= box.minY() && y <= box.maxY()) {
                     for (int x = box.minX(); x <= box.maxX(); x++) {
                        for (int z = box.minZ(); z <= box.maxZ(); z++) {
                           cursor.method_10103(x, y, z);
                           scan.visit(level, schematic, cursor, deferred, gaveUp, waterPending, lateNow, scaffold, craftersDone, wantCrafters);
                        }
                     }
                  }
               }
            }

            if (scan.wholeSchematic) {
               break;
            }

            scan.keepApproachesOpen(level, schematic, gaveUp, deferred);
         } while (scan.layer == Integer.MAX_VALUE);

         scan.releaseStages(gaveUp);
         scan.releaseTnt(level, schematic, deferred, gaveUp, scaffold);
         scan.keepApproachesOpen(level, schematic, gaveUp, deferred);
         return scan;
      }
   }

   private void releaseStages(Predicate<class_2338> gaveUp) {
      int lowest = Integer.MAX_VALUE;

      for (Entry<Long, Integer> entry : this.staged.entrySet()) {
         if (!gaveUp.test(class_2338.method_10092(entry.getKey()))) {
            lowest = Math.min(lowest, entry.getValue());
         }
      }

      for (Entry<Long, Integer> entryx : this.staged.entrySet()) {
         if (entryx.getValue() > lowest) {
            this.waiting.add(entryx.getKey());
         }
      }
   }

   private void keepApproachesOpen(class_638 level, WorldSchematic schematic, Predicate<class_2338> gaveUp, Predicate<class_2338> deferred) {
      Set<Long> open = new HashSet<>();

      for (BuildScan.Cell cell : this.missing) {
         open.add(cell.pos().method_10063());
      }

      Set<Long> held = new HashSet<>();

      for (BuildScan.Cell cell : this.lateCells) {
         open.add(cell.pos().method_10063());
         held.add(cell.pos().method_10063());
      }

      held.addAll(this.staged.keySet());
      List<BuildScan.Cell> all = new ArrayList<>(this.missing);
      all.addAll(this.lateCells);

      for (BuildScan.Cell cell : all) {
         if (!this.waiting.contains(cell.pos().method_10063()) || held.contains(cell.pos().method_10063())) {
            if (StateRules.lookFor(cell.expected()) == class_2350.field_11036) {
               class_2338 low = lowStand(level, schematic, cell.pos(), open);
               if (low != null) {
                  if (open.contains(low.method_10063())) {
                     this.waiting.add(low.method_10063());
                  }

                  if (open.contains(low.method_10084().method_10063())) {
                     this.waiting.add(low.method_10084().method_10063());
                  }
               } else {
                  this.waiting.add(cell.pos().method_10063());
               }
            } else if (!gaveUp.test(cell.pos())) {
               class_2338 approach = approach(cell.expected(), cell.pos());
               if (approach != null && open.contains(approach.method_10063())) {
                  class_2338 back = approach(schematic.method_8320(approach), approach);
                  if (!cell.pos().equals(back)) {
                     this.waiting.add(approach.method_10063());
                  }
               }
            }
         }
      }

      this.layer = Integer.MAX_VALUE;

      for (BuildScan.Cell cellx : this.missing) {
         if (!this.waiting.contains(cellx.pos().method_10063()) && !deferred.test(cellx.pos())) {
            this.layer = Math.min(this.layer, cellx.pos().method_10264());
         }
      }
   }

   private static class_2338 lowStand(class_638 level, WorldSchematic schematic, class_2338 pos, Set<Long> open) {
      class_2338 best = null;
      int bestScore = -1;

      for (class_2350 side : class_2353.field_11062) {
         class_2338 feet = pos.method_10074().method_10093(side);
         class_2338 floor = feet.method_10074();
         boolean floorNow = !level.method_8320(floor).method_26220(level, floor).method_1110();
         class_2680 floorPlanned = schematic.method_8320(floor);
         if ((floorNow || !floorPlanned.method_26215() && floorPlanned.method_26227().method_15769())
            && freeCell(level, schematic, feet, open)
            && freeCell(level, schematic, feet.method_10084(), open)
            && StateRules.lookFor(schematic.method_8320(feet.method_10084())) != class_2350.field_11036) {
            int score = (floorNow ? 2 : 0) + (open.contains(feet.method_10063()) ? 0 : 1);
            if (score > bestScore) {
               bestScore = score;
               best = feet;
            }
         }
      }

      return best;
   }

   private static boolean freeCell(class_638 level, WorldSchematic schematic, class_2338 cell, Set<Long> open) {
      class_2680 now = level.method_8320(cell);
      if (now.method_45474() && now.method_26227().method_15769()) {
         class_2680 planned = schematic.method_8320(cell);
         return planned.method_26204() instanceof class_2404 ? false : planned.method_26215() || open.contains(cell.method_10063());
      } else {
         return false;
      }
   }

   private static class_2338 approach(class_2680 expected, class_2338 pos) {
      class_2350 look = StateRules.lookFor(expected);
      if (look == null) {
         look = StateRules.flatLookFor(expected);
      }

      if (look == null && expected.method_26204() instanceof class_2377) {
         class_2350 spout = (class_2350)expected.method_11654(class_2741.field_12545);
         if (spout != class_2350.field_11033) {
            look = spout;
         }
      }

      return look == null ? null : pos.method_10093(look.method_10153());
   }

   static Map<class_1792, Integer> remainingItems(class_638 level, WorldSchematic schematic, List<SchematicManager.ActiveBox> boxes) {
      Map<class_1792, Integer> out = new HashMap<>();
      class_2339 cursor = new class_2339();

      for (SchematicManager.ActiveBox box : boxes) {
         for (int y = box.minY(); y <= box.maxY(); y++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
               for (int z = box.minZ(); z <= box.maxZ(); z++) {
                  cursor.method_10103(x, y, z);
                  class_2680 expected = schematic.method_8320(cursor);
                  if (!expected.method_26215() && level.method_22340(cursor)) {
                     class_2680 actual = level.method_8320(cursor);
                     CellFiller.Kind liquid = needsLiquid(expected, actual);
                     if (liquid != null) {
                        out.merge(liquid.item, 1, Integer::sum);
                     }

                     if (BlockStateUtil.isPrimaryPlacementState(expected)) {
                        class_1792 item = BlockStateUtil.requiredItem(expected);
                        if (item != null && BlockStateUtil.classify(expected, actual) == TargetStatus.MISSING) {
                           out.merge(item, StateRules.itemsNeeded(actual, expected), Integer::sum);
                        }
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private void releaseTnt(class_638 level, WorldSchematic schematic, Predicate<class_2338> deferred, Predicate<class_2338> gaveUp, Set<Long> scaffold) {
      if (!this.powerLeft && scaffold.isEmpty()) {
         for (BuildScan.Cell cell : this.tntCells) {
            if (BuildOrder.waits(level, schematic, cell.pos(), cell.expected(), gaveUp, this.machineLast)) {
               this.poweredTnt.add(cell.pos());
            } else {
               this.waiting.remove(cell.pos().method_10063());
               if (!deferred.test(cell.pos()) && (this.layer == Integer.MAX_VALUE || cell.pos().method_10264() < this.layer)) {
                  this.layer = cell.pos().method_10264();
               }
            }
         }
      }
   }

   private void visit(
      class_638 level,
      WorldSchematic schematic,
      class_2339 cursor,
      Predicate<class_2338> deferred,
      Predicate<class_2338> gaveUp,
      Predicate<class_2338> waterPending,
      boolean lateNow,
      Set<Long> scaffold,
      Set<Long> craftersDone,
      boolean wantCrafters
   ) {
      class_2680 expected = schematic.method_8320(cursor);
      if (!expected.method_26215()) {
         if (!level.method_22340(cursor)) {
            this.unloaded++;
         } else {
            class_2680 actual = level.method_8320(cursor);
            CellFiller.Kind liquid = needsLiquid(expected, actual);
            if (liquid != null) {
               class_2338 pos = cursor.method_10062();
               boolean capped = liquid == CellFiller.Kind.LAVA_SOURCE && capped(schematic, pos);
               this.liquids.add(new BuildScan.Liquid(pos, liquid, capped));
               if ((liquid != CellFiller.Kind.LAVA_SOURCE || capped) && waterPending.test(pos)) {
                  this.waterBelow.add(pos.method_10063());
               }
            }

            if (expected.method_26227().method_15769() || expected.method_26204().method_8389() != class_1802.field_8162) {
               if (BlockStateUtil.isPrimaryPlacementState(expected)) {
                  if (BlockStateUtil.requiredItem(expected) == null) {
                     this.noItem++;
                  } else {
                     TargetStatus status = BlockStateUtil.classify(expected, actual);
                     switch (status) {
                        case MISSING:
                           class_2338 pos = cursor.method_10062();
                           boolean tnt = expected.method_26204() instanceof class_2530;
                           boolean intoTnt = !tnt && BuildOrder.pushedIntoTnt(schematic, pos, expected) != null;
                           if (!this.placeTnt && (tnt || intoTnt)) {
                              (tnt ? this.tntForPlayer : this.afterTntForPlayer).add(pos);
                              this.waiting.add(pos.method_10063());
                              return;
                           }

                           int stage = BuildOrder.stage(schematic, pos, expected, this.machineLast);
                           if (lateNow && !tnt && !intoTnt && stage > 0) {
                              this.powerLeft = true;
                              this.staged.put(pos.method_10063(), stage);
                           }

                           if (lateNow && tnt) {
                              this.missing.add(new BuildScan.Cell(pos, expected, actual));
                              this.tntCells.add(new BuildScan.Cell(pos, expected, actual));
                              this.waiting.add(pos.method_10063());
                              return;
                           }

                           if (!lateNow && stage > 0) {
                              this.late++;
                              this.lateCells.add(new BuildScan.Cell(pos, expected, actual));
                              this.waiting.add(pos.method_10063());
                              return;
                           }

                           this.missing.add(new BuildScan.Cell(pos, expected, actual));
                           if (this.waterBelow.contains(pos.method_10074().method_10063())
                              || BuildOrder.waits(level, schematic, pos, expected, gaveUp, this.machineLast)) {
                              this.waiting.add(pos.method_10063());
                              return;
                           }

                           if (this.layer == Integer.MAX_VALUE && !deferred.test(pos)) {
                              this.layer = pos.method_10264();
                           }
                           break;
                        case ADJUST:
                           this.adjust.add(new BuildScan.Cell(cursor.method_10062(), expected, actual));
                           break;
                        case CORRECT:
                           if (expected.method_26204() instanceof class_2401
                              && (Boolean)expected.method_11654(class_2741.field_12484)
                              && !(Boolean)actual.method_11654(class_2741.field_12484)) {
                              this.leversOff++;
                              this.levers.add(cursor.method_10062());
                           }

                           if (BuildOrder.railShapeOff(actual, expected)) {
                              this.railsOff++;
                           }

                           if (wantCrafters && expected.method_26204() instanceof class_8886 && !craftersDone.contains(cursor.method_10063())) {
                              boolean[] disabled = disabledSlots(schematic, cursor);
                              if (disabled != null) {
                                 class_2338 crafterPos = cursor.method_10062();
                                 this.crafters.add(crafterPos);
                                 this.crafterSlots.put(crafterPos, disabled);
                              }
                           }
                           break;
                        case WRONG:
                           if (!scaffold.contains(cursor.method_10063())) {
                              this.wrong++;
                           }
                     }
                  }
               }
            }
         }
      }
   }

   static CellFiller.Kind needsLiquid(class_2680 expected, class_2680 actual) {
      class_3610 fluid = expected.method_26227();
      if (!fluid.method_15769() && fluid.method_15771()) {
         class_3610 there = actual.method_26227();
         if (fluid.method_15767(class_3486.field_15518)) {
            if (there.method_15771() && there.method_15767(class_3486.field_15518)) {
               return null;
            } else {
               return !actual.method_26215() && (!actual.method_45474() || !there.method_15769() && !there.method_15767(class_3486.field_15518))
                  ? null
                  : CellFiller.Kind.LAVA_SOURCE;
            }
         } else if (!fluid.method_15767(class_3486.field_15517)) {
            return null;
         } else if (expected.method_28498(class_2741.field_12508)) {
            return StateRules.placed(actual, expected) && !actual.method_11654(class_2741.field_12508) ? CellFiller.Kind.WATERLOG : null;
         } else if (there.method_15771() && there.method_15767(class_3486.field_15517)) {
            return null;
         } else {
            return !actual.method_26215() && !actual.method_45474() ? null : CellFiller.Kind.WATER_SOURCE;
         }
      } else {
         return null;
      }
   }

   private static boolean capped(WorldSchematic schematic, class_2338 pos) {
      class_2339 up = pos.method_25503();

      for (int i = 0; i < 64; i++) {
         up.method_10098(class_2350.field_11036);
         class_2680 state = schematic.method_8320(up);
         if (!state.method_26227().method_15767(class_3486.field_15518)) {
            return !state.method_26215() && state.method_26227().method_15769();
         }
      }

      return false;
   }

   private static boolean[] disabledSlots(WorldSchematic schematic, class_2338 pos) {
      if (!(schematic.method_8321(pos) instanceof class_8887 crafter)) {
         return null;
      } else {
         boolean[] var6 = new boolean[9];
         boolean any = false;

         for (int i = 0; i < 9; i++) {
            var6[i] = crafter.method_54483(i);
            any |= var6[i];
         }

         return any ? var6 : null;
      }
   }

   Map<class_1792, Integer> needs(int top) {
      Map<class_1792, Integer> out = new HashMap<>();

      for (BuildScan.Cell cell : this.missing) {
         if (cell.pos().method_10264() <= top) {
            class_1792 item = BlockStateUtil.requiredItem(cell.expected());
            if (item != null) {
               out.merge(item, StateRules.itemsNeeded(cell.actual(), cell.expected()), Integer::sum);
            }
         }
      }

      return out;
   }

   @Environment(EnvType.CLIENT)
   record Cell(class_2338 pos, class_2680 expected, class_2680 actual) {
   }

   @Environment(EnvType.CLIENT)
   record Liquid(class_2338 pos, CellFiller.Kind kind, boolean capped) {
   }
}
