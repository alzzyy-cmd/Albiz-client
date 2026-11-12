package net.albiz.client.suschunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BlockEvidenceRulesTest {
   @Test
   void classifiesCandidates() {
      assertEquals(EvidenceType.GROWN_KELP, BlockEvidenceRules.candidate("kelp"));
      assertNull(BlockEvidenceRules.candidate("kelp_plant"));
      assertEquals(EvidenceType.GROWN_VINES, BlockEvidenceRules.candidate("cave_vines"));
      assertNull(BlockEvidenceRules.candidate("cave_vines_plant"));
      assertEquals(EvidenceType.AMETHYST_CLUSTER, BlockEvidenceRules.candidate("amethyst_cluster"));
      assertNull(BlockEvidenceRules.candidate("small_amethyst_bud"));
      assertEquals(EvidenceType.HIGH_DEEPSLATE, BlockEvidenceRules.candidate("deepslate_tiles"));
      assertNull(BlockEvidenceRules.candidate("deepslate_iron_ore"));
      assertEquals(EvidenceType.SPAWNER, BlockEvidenceRules.candidate("spawner"));
      assertNull(BlockEvidenceRules.candidate("trial_spawner"));
      assertEquals(EvidenceType.STORAGE, BlockEvidenceRules.candidate("barrel"));
      assertEquals(EvidenceType.STORAGE, BlockEvidenceRules.candidate("shulker_box"));
      assertEquals(EvidenceType.STORAGE, BlockEvidenceRules.candidate("light_blue_shulker_box"));
      assertEquals(EvidenceType.REDSTONE, BlockEvidenceRules.candidate("sticky_piston"));
      assertEquals(EvidenceType.FARM_BLOCK, BlockEvidenceRules.candidate("farmland"));
      assertNull(BlockEvidenceRules.candidate("bee_nest"));
      assertNull(BlockEvidenceRules.candidate("stone"));
      assertNull(BlockEvidenceRules.candidate(""));
      assertNull(BlockEvidenceRules.candidate(null));
   }

   @Test
   void deepslateRange() {
      assertFalse(BlockEvidenceRules.deepslateOutOfRange(8, 16));
      assertTrue(BlockEvidenceRules.deepslateOutOfRange(16, 16));
      assertFalse(BlockEvidenceRules.deepslateOutOfRange(-40, 16));
   }
}
