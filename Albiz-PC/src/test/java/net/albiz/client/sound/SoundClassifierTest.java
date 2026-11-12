package net.albiz.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SoundClassifierTest {
   @Test
   void requestedSoundsAreRecognised() {
      assertEquals(SoundKind.KELP, SoundClassifier.classify("minecraft:block.wet_grass.break"));
      assertEquals(SoundKind.PISTON, SoundClassifier.classify("minecraft:block.piston.extend"));
      assertEquals(SoundKind.PISTON, SoundClassifier.classify("minecraft:block.piston.contract"));
      assertEquals(SoundKind.BONE_MEAL, SoundClassifier.classify("minecraft:item.bone_meal.use"));
      assertEquals(SoundKind.HOSTILE_MOB, SoundClassifier.classify("minecraft:entity.zombie.ambient"));
      assertEquals(SoundKind.HOSTILE_MOB, SoundClassifier.classify("minecraft:entity.zombie_villager.step"));
      assertEquals(SoundKind.HOSTILE_MOB, SoundClassifier.classify("minecraft:entity.skeleton.shoot"));
   }

   @Test
   void playerOnlySoundsCarryPlayerOrigin() {
      for (String id : new String[]{
         "minecraft:entity.player.attack.sweep", "minecraft:entity.generic.eat", "minecraft:item.armor.equip_netherite",
         "minecraft:item.totem.use", "minecraft:entity.ender_pearl.throw", "minecraft:block.chest.open",
         "minecraft:block.shulker_box.close", "minecraft:entity.experience_orb.pickup", "minecraft:block.deepslate.break"
      }) {
         assertEquals(SoundKind.Origin.PLAYER, SoundClassifier.classify(id).origin(), id);
      }
   }

   @Test
   void naturalSoundsNeverScore() {
      for (String id : new String[]{"minecraft:ambient.cave", "minecraft:block.lava.pop", "minecraft:weather.rain", "minecraft:music.game"}) {
         assertEquals(0, SoundClassifier.classify(id).weight(), id);
      }
   }

   @Test
   void machinesAndWorkstations() {
      assertEquals(SoundKind.REDSTONE, SoundClassifier.classify("minecraft:block.dispenser.dispense"));
      assertEquals(SoundKind.REDSTONE, SoundClassifier.classify("minecraft:block.stone_button.click_on"));
      assertEquals(SoundKind.DOOR, SoundClassifier.classify("minecraft:block.iron_trapdoor.open"));
      assertEquals(SoundKind.DOOR, SoundClassifier.classify("minecraft:block.wooden_door.close"));
      assertEquals(SoundKind.WORKSTATION, SoundClassifier.classify("minecraft:block.furnace.fire_crackle"));
      assertEquals(SoundKind.WORKSTATION, SoundClassifier.classify("minecraft:block.crafter.craft"));
      assertEquals(SoundKind.SPAWNER, SoundClassifier.classify("minecraft:block.trial_spawner.spawn_mob"));
      assertEquals(SoundKind.VILLAGER, SoundClassifier.classify("minecraft:entity.villager.work_farmer"));
      assertEquals(SoundKind.MINECART, SoundClassifier.classify("minecraft:entity.minecart.riding"));
   }

   @Test
   void unknownAndForeignIdsStayUnclassified() {
      assertEquals(SoundKind.OTHER, SoundClassifier.classify(null));
      assertEquals(SoundKind.OTHER, SoundClassifier.classify(""));
      assertEquals(SoundKind.OTHER, SoundClassifier.classify("someplugin:custom.sound"));
      assertEquals(SoundKind.OTHER, SoundClassifier.classify("minecraft:entity.unknown_future_mob.ambient"));
      assertEquals("someplugin:custom.sound", SoundClassifier.path("someplugin:custom.sound"));
   }

   @Test
   void entityTypes() {
      assertEquals(SoundKind.HOSTILE_MOB, SoundClassifier.entityKind("zombie"));
      assertEquals(SoundKind.PLAYER, SoundClassifier.entityKind("player"));
      assertEquals(SoundKind.VILLAGER, SoundClassifier.entityKind("iron_golem"));
      assertEquals(SoundKind.MINECART, SoundClassifier.entityKind("hopper_minecart"));
      assertEquals(SoundKind.EXPLOSION, SoundClassifier.entityKind("tnt"));
      assertNull(SoundClassifier.entityKind("item"));
      assertNull(SoundClassifier.entityKind(null));
   }
}
