package net.albiz.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorldSignalRulesTest {
   @Test
   void boneMealAndBlockBreakWorldEvents() {
      assertEquals(SoundKind.BONE_MEAL, WorldSignalRules.worldEvent(1505).kind());
      assertEquals(SoundKind.BLOCK_BREAK, WorldSignalRules.worldEvent(2001).kind());
      assertEquals(SoundKind.SPAWNER, WorldSignalRules.worldEvent(2004).kind());
      assertEquals(SoundKind.HOSTILE_MOB, WorldSignalRules.worldEvent(1021).kind());
   }

   @Test
   void naturalEffectsAreDroppedAndUnknownIdsAreNotGuessed() {
      assertNull(WorldSignalRules.worldEvent(1501));
      assertNull(WorldSignalRules.worldEvent(1504));
      WorldSignalRules.Signal unknown = WorldSignalRules.worldEvent(987654);
      assertEquals(SoundKind.OTHER, unknown.kind());
      assertTrue(unknown.label().endsWith("987654"));
   }

   @Test
   void blockActions() {
      assertEquals(SoundKind.PISTON, WorldSignalRules.blockAction("sticky_piston").kind());
      assertEquals(SoundKind.CONTAINER, WorldSignalRules.blockAction("purple_shulker_box").kind());
      assertEquals(SoundKind.CONTAINER, WorldSignalRules.blockAction("ender_chest").kind());
      assertEquals(SoundKind.SPAWNER, WorldSignalRules.blockAction("spawner").kind());
      assertNull(WorldSignalRules.blockAction(""));
   }

   @Test
   void kelpTransitionsInBothDirections() {
      assertEquals("Kelp removed", WorldSignalRules.transition("kelp_plant", "water").label());
      assertEquals("Kelp grew or was placed", WorldSignalRules.transition("water", "kelp").label());
      // Kelp top turning into a kelp stem is growth of the same plant, not a new signal.
      assertNull(WorldSignalRules.transition("kelp", "kelp_plant"));
      assertEquals(SoundKind.PLANT, WorldSignalRules.transition("sugar_cane", "air").kind());
      assertNull(WorldSignalRules.transition("stone", "air"));
   }
}
