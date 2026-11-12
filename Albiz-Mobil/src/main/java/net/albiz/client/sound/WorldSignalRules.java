package net.albiz.client.sound;

/**
 * Classification of non-sound packets that still reveal audible activity: vanilla world events
 * (the packet that makes other clients play break/bone-meal effects), block actions (pistons,
 * chest lids) and block-state transitions.
 *
 * <p>Only identifiers that vanilla documents are named. Unknown ids are reported verbatim as
 * {@link SoundKind#OTHER} instead of being guessed.
 */
public final class WorldSignalRules {
   private WorldSignalRules() {
   }

   /** A named world event. {@code label} is English and translated at draw time. */
   public record Signal(SoundKind kind, String label) {
   }

   /** Vanilla world-event ids (LevelEvent). Returns {@code null} for effects that carry no activity information. */
   public static Signal worldEvent(int id) {
      return switch (id) {
         case 1000, 1001, 1002 -> new Signal(SoundKind.REDSTONE, "Dispenser fired");
         case 1004 -> new Signal(SoundKind.PLAYER, "Firework launched");
         case 1009 -> new Signal(SoundKind.LIQUID, "Fire extinguished");
         case 1019, 1020 -> new Signal(SoundKind.HOSTILE_MOB, "Zombie attacking a door");
         case 1021 -> new Signal(SoundKind.HOSTILE_MOB, "Zombie broke a door");
         case 1029 -> new Signal(SoundKind.WORKSTATION, "Anvil destroyed");
         case 1030 -> new Signal(SoundKind.WORKSTATION, "Anvil used");
         case 1031 -> new Signal(SoundKind.WORKSTATION, "Anvil landed");
         case 1032 -> new Signal(SoundKind.PORTAL, "Portal travel");
         case 1035 -> new Signal(SoundKind.WORKSTATION, "Brewing stand finished");
         case 1042 -> new Signal(SoundKind.WORKSTATION, "Grindstone used");
         case 1043 -> new Signal(SoundKind.PLAYER, "Book page turned");
         case 1044 -> new Signal(SoundKind.WORKSTATION, "Smithing table used");
         case 1049 -> new Signal(SoundKind.WORKSTATION, "Crafter crafted");
         case 1050 -> new Signal(SoundKind.WORKSTATION, "Crafter failed");
         case 1500 -> new Signal(SoundKind.WORKSTATION, "Composter filled");
         case 1503 -> new Signal(SoundKind.PORTAL, "End portal frame filled");
         case 1505 -> new Signal(SoundKind.BONE_MEAL, "Bone meal used");
         case 2001 -> new Signal(SoundKind.BLOCK_BREAK, "Block broken");
         case 2002, 2007 -> new Signal(SoundKind.PLAYER, "Potion splashed");
         case 2003 -> new Signal(SoundKind.PLAYER, "Eye of ender broke");
         case 2004 -> new Signal(SoundKind.SPAWNER, "Spawner spawned mobs");
         case 3003, 3004, 3005 -> new Signal(SoundKind.PLAYER, "Copper waxed or scraped");
         case 3008 -> new Signal(SoundKind.PLAYER, "Block brushed");
         // Lava fizz, dripstone drips, sculk, dragon and similar effects are natural.
         case 1501, 1502, 1504, 2000, 2006, 2008, 2009, 3000, 3001, 3002, 3006, 3007, 3009 -> null;
         default -> new Signal(SoundKind.OTHER, "World event " + id);
      };
   }

   /** Block actions are sent for pistons, container lids, note blocks, bells and spawners. */
   public static Signal blockAction(String blockPath) {
      if (blockPath == null || blockPath.isEmpty()) {
         return null;
      }
      if (blockPath.equals("piston") || blockPath.equals("sticky_piston") || blockPath.equals("moving_piston") || blockPath.equals("piston_head")) {
         return new Signal(SoundKind.PISTON, "Piston moved");
      }
      if (blockPath.equals("chest") || blockPath.equals("trapped_chest") || blockPath.equals("ender_chest") || blockPath.equals("barrel")
         || blockPath.endsWith("shulker_box") || blockPath.endsWith("copper_chest")) {
         return new Signal(SoundKind.CONTAINER, "Container lid moved");
      }
      if (blockPath.equals("note_block") || blockPath.equals("bell")) {
         return new Signal(SoundKind.REDSTONE, "Note block or bell");
      }
      if (blockPath.equals("spawner") || blockPath.equals("trial_spawner")) {
         return new Signal(SoundKind.SPAWNER, "Spawner activity");
      }
      if (blockPath.equals("end_gateway") || blockPath.equals("end_portal")) {
         return new Signal(SoundKind.PORTAL, "Gateway activity");
      }
      return new Signal(SoundKind.OTHER, "Block action: " + blockPath);
   }

   public static boolean kelp(String blockPath) {
      return "kelp".equals(blockPath) || "kelp_plant".equals(blockPath);
   }

   public static boolean crop(String blockPath) {
      return switch (blockPath == null ? "" : blockPath) {
         case "wheat", "carrots", "potatoes", "beetroots", "nether_wart", "sugar_cane", "bamboo", "bamboo_sapling", "cactus", "melon",
            "pumpkin", "melon_stem", "pumpkin_stem", "attached_melon_stem", "attached_pumpkin_stem", "sweet_berry_bush", "cocoa",
            "cave_vines", "cave_vines_plant", "chorus_flower", "chorus_plant", "torchflower_crop", "pitcher_crop" -> true;
         default -> false;
      };
   }

   /**
    * A block-state transition that is audible in vanilla and relevant to farms: a kelp or crop
    * block disappearing or appearing. Natural growth exists, so this is never player proof.
    * Returns {@code null} when the transition is not interesting.
    */
   public static Signal transition(String oldPath, String newPath) {
      boolean oldKelp = kelp(oldPath);
      boolean newKelp = kelp(newPath);
      if (oldKelp && !newKelp) {
         return new Signal(SoundKind.KELP, "Kelp removed");
      }
      if (!oldKelp && newKelp) {
         return new Signal(SoundKind.KELP, "Kelp grew or was placed");
      }
      boolean oldCrop = crop(oldPath);
      boolean newCrop = crop(newPath);
      if (oldCrop && !newCrop) {
         return new Signal(SoundKind.PLANT, "Crop removed");
      }
      if (!oldCrop && newCrop) {
         return new Signal(SoundKind.PLANT, "Crop grew or was planted");
      }
      return null;
   }
}
