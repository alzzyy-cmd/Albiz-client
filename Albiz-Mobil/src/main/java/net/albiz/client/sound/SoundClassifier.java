package net.albiz.client.sound;

/**
 * Maps a sound identifier such as {@code minecraft:block.piston.extend} to a {@link SoundKind}.
 *
 * <p>Pure string rules so they can be unit-tested without Minecraft. Rules are ordered from the
 * most specific to the most general; add a new rule above the generic {@code block.*} fallbacks.
 */
public final class SoundClassifier {
   private static final String[] HOSTILE = {
      "zombie", "zombie_villager", "husk", "drowned", "skeleton", "stray", "bogged", "wither_skeleton", "creeper",
      "spider", "cave_spider", "enderman", "endermite", "silverfish", "witch", "slime", "magma_cube", "phantom", "blaze", "ghast",
      "piglin", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "pillager", "vindicator", "evoker", "ravager", "vex",
      "guardian", "elder_guardian", "shulker", "warden", "breeze", "creaking", "wither", "illusioner"
   };
   private static final String[] PASSIVE = {
      "cow", "pig", "sheep", "chicken", "horse", "donkey", "mule", "llama", "rabbit", "bat", "squid", "glow_squid", "cod", "salmon",
      "tropical_fish", "pufferfish", "axolotl", "frog", "tadpole", "turtle", "bee", "fox", "wolf", "cat", "ocelot", "parrot", "goat",
      "mooshroom", "panda", "polar_bear", "dolphin", "strider", "camel", "sniffer", "armadillo", "allay", "snow_golem", "happy_ghast"
   };

   private SoundClassifier() {
   }

   /** Strips the {@code minecraft:} namespace; other namespaces are kept so custom sounds stay distinguishable. */
   public static String path(String id) {
      if (id == null) {
         return "";
      }
      return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
   }

   public static SoundKind classify(String id) {
      String p = path(id);
      if (p.isEmpty()) {
         return SoundKind.OTHER;
      }
      if (p.startsWith("ambient.") || p.startsWith("music.") || p.startsWith("music_disc.") || p.startsWith("weather.")) {
         return SoundKind.AMBIENT;
      }
      if (p.startsWith("entity.")) {
         return entity(p.substring("entity.".length()));
      }
      if (p.startsWith("item.")) {
         return item(p.substring("item.".length()));
      }
      if (p.startsWith("block.")) {
         return block(p.substring("block.".length()));
      }
      if (p.startsWith("ui.") || p.startsWith("enchant.") || p.startsWith("event.")) {
         return SoundKind.OTHER;
      }
      return SoundKind.OTHER;
   }

   /**
    * Classifies an entity type path (e.g. {@code zombie}) for sightings and spawn packets.
    * Returns {@code null} for types that carry no activity information.
    */
   public static SoundKind entityKind(String typePath) {
      if (typePath == null || typePath.isEmpty()) {
         return null;
      }
      if (typePath.equals("player")) {
         return SoundKind.PLAYER;
      }
      if (typePath.equals("villager") || typePath.equals("iron_golem") || typePath.equals("wandering_trader")) {
         return SoundKind.VILLAGER;
      }
      if (typePath.equals("tnt") || typePath.equals("tnt_minecart")) {
         return SoundKind.EXPLOSION;
      }
      if (typePath.endsWith("minecart") || typePath.endsWith("_boat") || typePath.endsWith("_raft")) {
         return SoundKind.MINECART;
      }
      if (typePath.equals("ender_pearl") || typePath.equals("arrow") || typePath.equals("spectral_arrow") || typePath.equals("trident")
         || typePath.equals("fishing_bobber") || typePath.equals("firework_rocket") || typePath.equals("eye_of_ender")
         || typePath.equals("experience_bottle") || typePath.equals("splash_potion") || typePath.equals("lingering_potion")
         || typePath.equals("potion") || typePath.equals("item_frame") || typePath.equals("glow_item_frame") || typePath.equals("armor_stand")
         || typePath.equals("painting") || typePath.equals("leash_knot") || typePath.equals("end_crystal") || typePath.equals("wind_charge")) {
         return SoundKind.PLAYER;
      }
      if (contains(HOSTILE, typePath)) {
         return SoundKind.HOSTILE_MOB;
      }
      if (contains(PASSIVE, typePath)) {
         return SoundKind.PASSIVE_MOB;
      }
      return null;
   }

   private static SoundKind entity(String p) {
      if (p.startsWith("player.")) {
         return SoundKind.PLAYER;
      }
      if (p.startsWith("experience_orb.") || p.equals("item.pickup")) {
         return SoundKind.ITEM_PICKUP;
      }
      if (p.startsWith("generic.eat") || p.startsWith("generic.drink")) {
         return SoundKind.PLAYER;
      }
      if (p.startsWith("generic.explode") || p.startsWith("tnt.") || p.startsWith("firework_rocket.blast")) {
         return SoundKind.EXPLOSION;
      }
      if (p.startsWith("minecart.") || p.startsWith("boat.")) {
         return SoundKind.MINECART;
      }
      if (p.startsWith("ender_pearl.") || p.startsWith("arrow.shoot") || p.startsWith("fishing_bobber.") || p.startsWith("splash_potion.throw")
         || p.startsWith("experience_bottle.throw") || p.startsWith("firework_rocket.launch") || p.startsWith("ender_eye.")
         || p.startsWith("item_frame.") || p.startsWith("painting.") || p.startsWith("armor_stand.") || p.startsWith("leash_knot.")) {
         return SoundKind.PLAYER;
      }
      if (p.startsWith("villager.") || p.startsWith("iron_golem.") || p.startsWith("wandering_trader.")) {
         return SoundKind.VILLAGER;
      }
      String mob = firstSegment(p);
      if (contains(HOSTILE, mob)) {
         return SoundKind.HOSTILE_MOB;
      }
      if (contains(PASSIVE, mob)) {
         return SoundKind.PASSIVE_MOB;
      }
      if (p.startsWith("generic.splash") || p.startsWith("generic.swim") || p.startsWith("generic.extinguish_fire") || p.startsWith("generic.burn")) {
         return SoundKind.LIQUID;
      }
      return SoundKind.OTHER;
   }

   private static SoundKind item(String p) {
      if (p.startsWith("bone_meal.")) {
         return SoundKind.BONE_MEAL;
      }
      // Every remaining item sound needs a holder: armor equip, buckets, tools, totems, elytra, weapons.
      // Dispensers reuse a few of them (bucket, flint and steel), which the hotspot text states.
      return SoundKind.PLAYER;
   }

   private static SoundKind block(String p) {
      if (p.startsWith("piston.")) {
         return SoundKind.PISTON;
      }
      if (p.startsWith("chest.") || p.startsWith("ender_chest.") || p.startsWith("barrel.") || p.startsWith("shulker_box.")
         || p.startsWith("copper_chest.") || p.startsWith("decorated_pot.insert") || p.startsWith("vault.")) {
         return SoundKind.CONTAINER;
      }
      if (p.contains("door.") || p.contains("trapdoor.") || p.contains("fence_gate.")) {
         return SoundKind.DOOR;
      }
      if (p.startsWith("lever.") || p.contains("button.") || p.contains("pressure_plate.") || p.startsWith("dispenser.") || p.startsWith("dropper.")
         || p.startsWith("comparator.") || p.startsWith("note_block.") || p.startsWith("tripwire.") || p.startsWith("redstone_torch.")
         || p.startsWith("observer.") || p.startsWith("sculk_sensor.") || p.startsWith("bell.") || p.startsWith("copper_bulb.")) {
         return SoundKind.REDSTONE;
      }
      if (p.startsWith("furnace.") || p.startsWith("blastfurnace.") || p.startsWith("smoker.") || p.startsWith("anvil.")
         || p.startsWith("grindstone.") || p.startsWith("smithing_table.") || p.startsWith("brewing_stand.") || p.startsWith("crafter.")
         || p.startsWith("enchantment_table.") || p.startsWith("composter.") || p.startsWith("beacon.") || p.startsWith("respawn_anchor.")
         || p.startsWith("campfire.") || p.startsWith("conduit.") || p.startsWith("beehive.") || p.startsWith("cauldron.")
         || p.startsWith("lodestone.") || p.startsWith("stonecutter.") || p.startsWith("loom.") || p.startsWith("cartography_table.")) {
         return SoundKind.WORKSTATION;
      }
      if (p.startsWith("spawner.") || p.startsWith("trial_spawner.")) {
         return SoundKind.SPAWNER;
      }
      if (p.startsWith("portal.") || p.startsWith("end_portal") || p.startsWith("end_gateway.")) {
         return SoundKind.PORTAL;
      }
      if (p.startsWith("lava.") || p.startsWith("water.") || p.startsWith("bubble_column.") || p.startsWith("pointed_dripstone.drip")
         || p.startsWith("fire.ambient")) {
         return SoundKind.LIQUID;
      }
      // Kelp, seagrass and other underwater plants share the "wet_grass" sound group in vanilla.
      if (p.startsWith("wet_grass.")) {
         return SoundKind.KELP;
      }
      if (p.startsWith("crop.") || p.startsWith("sweet_berry_bush.") || p.startsWith("bamboo.") || p.startsWith("bamboo_sapling.")
         || p.startsWith("nether_wart.") || p.startsWith("chorus_flower.") || p.startsWith("cave_vines.") || p.startsWith("vine.")
         || p.startsWith("big_dripleaf.") || p.startsWith("moss.") || p.startsWith("azalea.") || p.startsWith("pumpkin.")
         || p.startsWith("sugar_cane.") || p.startsWith("cactus.") || p.startsWith("growing_plant.") || p.startsWith("hanging_roots.")) {
         return SoundKind.PLANT;
      }
      if (p.endsWith(".break")) {
         return SoundKind.BLOCK_BREAK;
      }
      if (p.endsWith(".place")) {
         return SoundKind.BLOCK_PLACE;
      }
      if (p.endsWith(".hit")) {
         return SoundKind.BLOCK_HIT;
      }
      if (p.endsWith(".step") || p.endsWith(".fall")) {
         return SoundKind.BLOCK_STEP;
      }
      return SoundKind.OTHER;
   }

   private static String firstSegment(String p) {
      int dot = p.indexOf('.');
      return dot < 0 ? p : p.substring(0, dot);
   }

   private static boolean contains(String[] values, String value) {
      for (String candidate : values) {
         if (candidate.equals(value)) {
            return true;
         }
      }
      return false;
   }
}
