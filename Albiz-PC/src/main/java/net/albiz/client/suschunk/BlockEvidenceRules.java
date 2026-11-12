package net.albiz.client.suschunk;

import java.util.Set;

/** Maps a block registry path (e.g. {@code "sticky_piston"}) to the evidence it may represent. */
public final class BlockEvidenceRules {
   private static final Set<String> STORAGE = Set.of(
      "chest", "trapped_chest", "barrel", "hopper", "dropper", "dispenser", "furnace", "blast_furnace", "smoker", "ender_chest", "brewing_stand", "crafter"
   );
   private static final Set<String> REDSTONE = Set.of(
      "piston", "sticky_piston", "piston_head", "moving_piston", "observer", "repeater", "comparator", "redstone_wire", "redstone_torch",
      "redstone_wall_torch", "redstone_block", "target", "daylight_detector", "note_block", "redstone_lamp"
   );
   private static final Set<String> FARM = Set.of("farmland", "composter", "beehive", "bone_block");
   private static final Set<String> DEEPSLATE = Set.of(
      "deepslate", "cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "deepslate_tiles", "chiseled_deepslate", "cracked_deepslate_bricks",
      "cracked_deepslate_tiles"
   );
   private static final Set<String> VINE_HEADS = Set.of("weeping_vines", "twisting_vines", "cave_vines");

   private BlockEvidenceRules() {
   }

   /**
    * @return the candidate evidence type, or {@code null} when the block is irrelevant. Candidates
    *     for {@link EvidenceType#GROWN_KELP} and {@link EvidenceType#GROWN_VINES} still need their
    *     age checked, and {@link EvidenceType#HIGH_DEEPSLATE} its Y and axis, by the caller.
    */
   public static EvidenceType candidate(String path) {
      if (path == null || path.isEmpty()) {
         return null;
      }
      if (path.equals("kelp")) {
         return EvidenceType.GROWN_KELP;
      }
      if (VINE_HEADS.contains(path)) {
         return EvidenceType.GROWN_VINES;
      }
      if (path.equals("amethyst_cluster")) {
         return EvidenceType.AMETHYST_CLUSTER;
      }
      if (DEEPSLATE.contains(path)) {
         return EvidenceType.HIGH_DEEPSLATE;
      }
      if (path.equals("spawner")) {
         return EvidenceType.SPAWNER;
      }
      if (STORAGE.contains(path) || path.endsWith("shulker_box")) {
         return EvidenceType.STORAGE;
      }
      if (REDSTONE.contains(path)) {
         return EvidenceType.REDSTONE;
      }
      return FARM.contains(path) ? EvidenceType.FARM_BLOCK : null;
   }

   /** Deepslate counts as out of place only at or above {@code minY}; natural deepslate ends near Y 8. */
   public static boolean deepslateOutOfRange(int y, int minY) {
      return y >= minY;
   }
}
