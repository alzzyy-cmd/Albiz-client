package net.albiz.client.easyplace.schematic;

import com.google.common.collect.UnmodifiableIterator;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement.RequiredEnabled;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.LayerRange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.albiz.client.easyplace.EasyPlace;
import net.albiz.client.easyplace.util.BlockStateUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_2338.class_2339;

@Environment(EnvType.CLIENT)
public final class SchematicManager {
   private static final int CACHE_REFRESH_TICKS = 5;
   private static final int NAV_REFRESH_TICKS = 20;
   private final class_310 mc;
   private final Map<Long, SchematicTarget> cachedTargets = new LinkedHashMap<>();
   private final List<SchematicManager.ActiveBox> activeBoxes = new ArrayList<>();
   private long lastRefreshTick = Long.MIN_VALUE;
   private class_2338 lastCenter;
   private double lastRange = -1.0;
   private boolean lastAura;
   private boolean lastAllBlocks;
   private int lastLayerRangeHash;
   private SchematicTarget cachedNavTarget;
   private long lastNavRefreshTick = Long.MIN_VALUE;

   public SchematicManager(class_310 mc) {
      this.mc = mc;
   }

   public void clear() {
      this.cachedTargets.clear();
      this.activeBoxes.clear();
      this.lastRefreshTick = Long.MIN_VALUE;
      this.lastNavRefreshTick = Long.MIN_VALUE;
      this.cachedNavTarget = null;
      this.lastCenter = null;
   }

   public void invalidate() {
      this.lastRefreshTick = Long.MIN_VALUE;
   }

   public boolean hasActiveSchematic() {
      return this.rebuildActiveBoxes();
   }

   public List<SchematicManager.ActiveBox> activeBoxes() {
      this.rebuildActiveBoxes();
      return List.copyOf(this.activeBoxes);
   }

   public WorldSchematic getSchematicWorld() {
      return SchematicWorldHandler.getSchematicWorld();
   }

   public Collection<SchematicTarget> getTargets(long tick, double range, boolean aura, boolean allBlocks) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         class_2338 center = this.mc.field_1724.method_24515();
         boolean moved = this.lastCenter == null || center.method_10262(this.lastCenter) > 1.0;
         int layerHash = this.getLayerRangeHash();
         boolean settingsChanged = range != this.lastRange || aura != this.lastAura || allBlocks != this.lastAllBlocks || layerHash != this.lastLayerRangeHash;
         if (tick - this.lastRefreshTick >= 5L || moved || settingsChanged) {
            this.refresh(tick, center, range, aura, allBlocks);
         }

         return this.cachedTargets.values();
      } else {
         this.clear();
         return List.of();
      }
   }

   private void refresh(long tick, class_2338 center, double range, boolean aura, boolean allBlocks) {
      this.cachedTargets.clear();
      this.lastRefreshTick = tick;
      this.lastCenter = center.method_10062();
      this.lastRange = range;
      this.lastAura = aura;
      this.lastAllBlocks = allBlocks;
      this.lastLayerRangeHash = this.getLayerRangeHash();
      if (this.rebuildActiveBoxes()) {
         WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
         if (schematicWorld != null) {
            if (!aura) {
               class_2338 traced = RayTraceUtils.getSchematicWorldTraceIfClosestNoFluids(this.mc.field_1687, this.mc.field_1724, range);
               if (traced != null) {
                  this.addTarget(traced, schematicWorld, allBlocks);
               }
            } else {
               class_243 playerPos = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
               int radius = (int)Math.ceil(range);
               double rangeSquared = range * range;
               class_2339 mutable = new class_2339();

               for (int x = center.method_10263() - radius; x <= center.method_10263() + radius; x++) {
                  for (int y = center.method_10264() - radius; y <= center.method_10264() + radius; y++) {
                     for (int z = center.method_10260() - radius; z <= center.method_10260() + radius; z++) {
                        mutable.method_10103(x, y, z);
                        if (!(mutable.method_46558().method_1025(playerPos) > rangeSquared)) {
                           this.addTarget(mutable, schematicWorld, allBlocks);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void addTarget(class_2338 pos, WorldSchematic schematicWorld, boolean allBlocks) {
      if (this.insideActivePlacement(pos)) {
         if (this.isInRenderLayer(pos)) {
            class_2680 expected = schematicWorld.method_8320(pos);
            if (!expected.method_26215() && BlockStateUtil.isPrimaryPlacementState(expected)) {
               if (allBlocks || BlockStateUtil.isSimpleFullBlock(expected, schematicWorld, pos)) {
                  class_2680 actual = this.mc.field_1687.method_8320(pos);
                  TargetStatus status = BlockStateUtil.classify(expected, actual);
                  if (status != TargetStatus.CORRECT) {
                     class_2338 immutable = pos.method_10062();
                     double distance = immutable.method_46558().method_1025(this.mc.field_1724.method_33571());
                     this.cachedTargets
                        .put(immutable.method_10063(), new SchematicTarget(immutable, SchematicBlockState.of(expected), actual, status, distance));
                  }
               }
            }
         }
      }
   }

   private boolean rebuildActiveBoxes() {
      this.activeBoxes.clear();

      for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
         if (placement.isEnabled()) {
            UnmodifiableIterator var4 = placement.getSubRegionBoxes(RequiredEnabled.PLACEMENT_ENABLED).values().iterator();

            while (var4.hasNext()) {
               Box box = (Box)var4.next();
               if (box.getPos1() != null && box.getPos2() != null) {
                  this.activeBoxes.add(SchematicManager.ActiveBox.of(box.getPos1(), box.getPos2()));
               }
            }
         }
      }

      return !this.activeBoxes.isEmpty();
   }

   private boolean insideActivePlacement(class_2338 pos) {
      for (SchematicManager.ActiveBox box : this.activeBoxes) {
         if (box.contains(pos)) {
            return true;
         }
      }

      return false;
   }

   public static boolean inRenderLayer(class_2338 pos) {
      try {
         LayerRange range = DataManager.getRenderLayerRange();
         return range == null || range.isPositionWithinRange(pos);
      } catch (Exception var2) {
         return true;
      }
   }

   private boolean isInRenderLayer(class_2338 pos) {
      try {
         LayerRange range = DataManager.getRenderLayerRange();
         return range == null || range.isPositionWithinRange(pos);
      } catch (Exception var3) {
         return true;
      }
   }

   private int getLayerRangeHash() {
      try {
         LayerRange range = DataManager.getRenderLayerRange();
         return range == null
            ? 0
            : Objects.hash(range.getLayerMode(), range.getLayerRangeMin(), range.getLayerRangeMax(), range.getLayerSingle(), range.getAxis());
      } catch (Exception var2) {
         return 0;
      }
   }

   public SchematicTarget nearestMissingForNavigation(long tick, boolean allBlocks) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (tick - this.lastNavRefreshTick < 20L) {
            return this.cachedNavTarget;
         } else {
            this.lastNavRefreshTick = tick;
            this.cachedNavTarget = null;

            try {
               if (!this.rebuildActiveBoxes()) {
                  return null;
               }

               WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
               if (schematicWorld == null) {
                  return null;
               }

               class_243 eye = this.mc.field_1724.method_33571();
               double bestDist = Double.MAX_VALUE;
               class_2339 mutable = new class_2339();
               int scanned = 0;
               int nonAir = 0;
               int missing = 0;

               for (SchematicManager.ActiveBox box : this.activeBoxes) {
                  for (int x = box.minX(); x <= box.maxX(); x++) {
                     for (int y = box.minY(); y <= box.maxY(); y++) {
                        for (int z = box.minZ(); z <= box.maxZ(); z++) {
                           scanned++;
                           mutable.method_10103(x, y, z);
                           class_2680 expected = schematicWorld.method_8320(mutable);
                           if (!expected.method_26215() && BlockStateUtil.isPrimaryPlacementState(expected)) {
                              nonAir++;
                              if (allBlocks || BlockStateUtil.isSimpleFullBlock(expected, schematicWorld, mutable)) {
                                 class_2680 actual = this.mc.field_1687.method_8320(mutable);
                                 if (BlockStateUtil.classify(expected, actual) == TargetStatus.MISSING) {
                                    missing++;
                                    class_2338 immutable = mutable.method_10062();
                                    double dist = immutable.method_46558().method_1025(eye);
                                    if (dist < bestDist) {
                                       bestDist = dist;
                                       this.cachedNavTarget = new SchematicTarget(
                                          immutable, SchematicBlockState.of(expected), actual, TargetStatus.MISSING, dist
                                       );
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               EasyPlace.LOG
                  .debug(
                     "NavScan: scanned={} nonAir={} missing={} result={}",
                     new Object[]{scanned, nonAir, missing, this.cachedNavTarget == null ? "null" : this.cachedNavTarget.pos().method_23854()}
                  );
            } catch (Exception var22) {
               EasyPlace.LOG.warn("NavScan error: {}: {}", var22.getClass().getSimpleName(), var22.getMessage());
            }

            return this.cachedNavTarget;
         }
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   public record ActiveBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      private static SchematicManager.ActiveBox of(class_2338 first, class_2338 second) {
         return new SchematicManager.ActiveBox(
            Math.min(first.method_10263(), second.method_10263()),
            Math.min(first.method_10264(), second.method_10264()),
            Math.min(first.method_10260(), second.method_10260()),
            Math.max(first.method_10263(), second.method_10263()),
            Math.max(first.method_10264(), second.method_10264()),
            Math.max(first.method_10260(), second.method_10260())
         );
      }

      public boolean contains(class_2338 pos) {
         return pos.method_10263() >= this.minX
            && pos.method_10263() <= this.maxX
            && pos.method_10264() >= this.minY
            && pos.method_10264() <= this.maxY
            && pos.method_10260() >= this.minZ
            && pos.method_10260() <= this.maxZ;
      }
   }
}
