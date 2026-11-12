package net.albiz.client.perf;

/**
 * Work budgets for background scanning. {@link #CUSTOM} leaves every value to the user's own
 * settings, so existing configurations behave exactly as before.
 */
public enum PerformanceProfile {
   QUALITY("Quality", 4.0F, 1.5F),
   BALANCED("Balanced", 2.0F, 1.0F),
   PERFORMANCE("Performance", 1.0F, 0.5F),
   CUSTOM("Custom", Float.NaN, 1.0F);

   /** Hard ceiling for the shared scan budget on phones and tablets, in milliseconds. */
   public static final float MOBILE_SCAN_BUDGET_MS = 0.75F;

   private final String label;
   private final float scanBudgetMs;
   private final float chunkFactor;

   PerformanceProfile(String label, float scanBudgetMs, float chunkFactor) {
      this.label = label;
      this.scanBudgetMs = scanBudgetMs;
      this.chunkFactor = chunkFactor;
   }

   public String label() {
      return this.label;
   }

   public static PerformanceProfile byLabel(String label) {
      for (PerformanceProfile profile : values()) {
         if (profile.label.equals(label)) {
            return profile;
         }
      }
      return CUSTOM;
   }

   /** Shared scan budget per tick in milliseconds; {@code customMs} is used only by {@link #CUSTOM}. */
   public float scanBudgetMs(float customMs, boolean mobile) {
      float value = this == CUSTOM ? customMs : this.scanBudgetMs;
      return mobile ? Math.min(value, MOBILE_SCAN_BUDGET_MS) : value;
   }

   /**
    * Chunks a scanner may process this tick, given what its own setting asks for. Never below 1,
    * so a scanner always makes progress. Mobile halves the result.
    */
   public int chunksPerTick(int requested, boolean mobile) {
      float scaled = (float)Math.max(1, requested) * this.chunkFactor;
      if (mobile) {
         scaled *= 0.5F;
      }
      return Math.max(1, Math.round(scaled));
   }
}
