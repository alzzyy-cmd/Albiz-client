package net.albiz.client.suschunk;

/**
 * Kinds of evidence that a chunk was touched by a player. Every kind is a hint: each also has a
 * natural explanation, which {@link #caveat} states so the UI can show it next to the hint.
 */
public enum EvidenceType {
   GROWN_KELP("Fully grown kelp", "Weight: Fully grown kelp", 1.0F, 12, "kelp also reaches full height naturally in old chunks"),
   GROWN_VINES("Fully grown vines", "Weight: Fully grown vines", 0.5F, 12, "cave, weeping and twisting vines also finish growing naturally"),
   AMETHYST_CLUSTER("Amethyst clusters", "Weight: Amethyst clusters", 0.25F, 16, "geodes grow clusters without players"),
   ROTATED_DEEPSLATE("Rotated deepslate", "Weight: Rotated deepslate", 4.0F, 6, "only placed blocks are rotated, but ancient cities contain some"),
   HIGH_DEEPSLATE("Deepslate above its natural range", "Weight: Deepslate above its natural range", 2.0F, 8, "structures and ancient cities place deepslate too"),
   STORAGE("Storage blocks", "Weight: Storage blocks", 3.0F, 8, "villages, mineshafts and dungeons contain chests"),
   SPAWNER("Spawners", "Weight: Spawners", 2.0F, 4, "dungeons and mineshafts contain spawners"),
   REDSTONE("Redstone machinery", "Weight: Redstone machinery", 4.0F, 6, "ancient cities and jungle temples contain redstone"),
   FARM_BLOCK("Farm blocks", "Weight: Farm blocks", 1.5F, 10, "villages contain farmland and composters"),
   BLOCK_CHANGE("Live block changes", "Weight: Live block changes", 1.0F, 20, "fluids, growth and mobs also change blocks"),
   SOUND("Player or machine sounds", "Weight: Player or machine sounds", 2.0F, 10, "some of these sounds are also made by mobs");

   private final String label;
   private final String weightLabel;
   private final float defaultWeight;
   private final int cap;
   private final String caveat;

   EvidenceType(String label, String weightLabel, float defaultWeight, int cap, String caveat) {
      this.label = label;
      this.weightLabel = weightLabel;
      this.defaultWeight = defaultWeight;
      this.cap = cap;
      this.caveat = caveat;
   }

   public String label() {
      return this.label;
   }

   /** Name of the setting that tunes this type's weight. */
   public String weightLabel() {
      return this.weightLabel;
   }

   public float defaultWeight() {
      return this.defaultWeight;
   }

   /** Occurrences beyond this no longer raise the score, so one huge kelp forest cannot dominate. */
   public int cap() {
      return this.cap;
   }

   public String caveat() {
      return this.caveat;
   }
}
