package net.albiz.client.sound;

/**
 * Coarse classification of a server-sent sound or world event.
 *
 * <p>{@link Origin} states how strongly the kind points at a player. It is an inference about
 * the cause, never an observation: the only observed fact is that the packet was received.
 */
public enum SoundKind {
   KELP("Kelp", Origin.AUTOMATION_OR_PLAYER, 6),
   PLANT("Plant / Crop", Origin.AUTOMATION_OR_PLAYER, 4),
   BONE_MEAL("Bone Meal", Origin.AUTOMATION_OR_PLAYER, 8),
   PISTON("Piston", Origin.AUTOMATION_OR_PLAYER, 7),
   CONTAINER("Container", Origin.PLAYER, 9),
   DOOR("Door / Gate", Origin.AUTOMATION_OR_PLAYER, 5),
   REDSTONE("Redstone", Origin.AUTOMATION_OR_PLAYER, 5),
   WORKSTATION("Workstation", Origin.AUTOMATION_OR_PLAYER, 6),
   BLOCK_BREAK("Block Break", Origin.PLAYER, 7),
   BLOCK_PLACE("Block Place", Origin.PLAYER, 7),
   BLOCK_HIT("Block Hit", Origin.PLAYER, 6),
   BLOCK_STEP("Footstep", Origin.NATURAL_POSSIBLE, 2),
   PLAYER("Player Action", Origin.PLAYER, 10),
   ITEM_PICKUP("Item / XP Pickup", Origin.PLAYER, 8),
   VILLAGER("Villager / Golem", Origin.AUTOMATION_OR_PLAYER, 4),
   HOSTILE_MOB("Hostile Mob", Origin.NATURAL_POSSIBLE, 1),
   PASSIVE_MOB("Passive Mob", Origin.NATURAL_POSSIBLE, 2),
   SPAWNER("Spawner", Origin.NATURAL_POSSIBLE, 3),
   EXPLOSION("Explosion", Origin.AUTOMATION_OR_PLAYER, 6),
   PORTAL("Portal", Origin.AUTOMATION_OR_PLAYER, 6),
   MINECART("Minecart / Rail", Origin.AUTOMATION_OR_PLAYER, 5),
   LIQUID("Liquid", Origin.NATURAL, 0),
   AMBIENT("Ambient", Origin.NATURAL, 0),
   OTHER("Other", Origin.UNKNOWN, 1);

   /** How the cause of a sound is interpreted. Ordered from weakest to strongest player link. */
   public enum Origin {
      /** World generation or weather; carries no activity information. */
      NATURAL,
      /** Not classified; nothing can be said about the cause. */
      UNKNOWN,
      /** Mobs and natural mechanics produce it too. */
      NATURAL_POSSIBLE,
      /** Produced by a farm/machine or by a player; both imply a build. */
      AUTOMATION_OR_PLAYER,
      /** Vanilla only produces it through a player action. */
      PLAYER
   }

   private final String label;
   private final Origin origin;
   private final int weight;

   SoundKind(String label, Origin origin, int weight) {
      this.label = label;
      this.origin = origin;
      this.weight = weight;
   }

   /** English display label; translated through the central catalog at draw time. */
   public String label() {
      return this.label;
   }

   public Origin origin() {
      return this.origin;
   }

   /** Default hotspot weight. Zero means the kind never raises a hotspot. */
   public int weight() {
      return this.weight;
   }
}
