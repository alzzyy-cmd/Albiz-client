package net.albiz.client.module.impl.client;

import imgui.ImDrawList;
import imgui.ImFont;
import java.util.Locale;
import net.albiz.client.esp.ChunkScanner;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.gui.AlbizWidgets;
import net.albiz.client.imgui.ImGuiManager;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.HudOverlayProvider;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.util.PerformanceBudget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class PerformanceModule extends ClientModule implements HudOverlayProvider {
   private static volatile boolean optimizing;
   private final BooleanSetting optimize = this.addSetting("optimize-client", new BooleanSetting("Optimize Client", true));
   private final BooleanSetting showStats = this.addSetting("show-stats", new BooleanSetting("Show Stats", true));
   private final BooleanSetting frustumCulling = this.addSetting("frustum-culling", new BooleanSetting("Frustum Culling", true));
   /** Default is Custom so configurations saved before profiles existed keep their own scan budget. */
   private final net.albiz.client.module.setting.ModeSetting profile = this.addSetting(
      "profile", new net.albiz.client.module.setting.ModeSetting("Performance Profile", "Custom", "Quality", "Balanced", "Performance", "Custom")
   );
   private final NumberSetting scanBudget = this.addSetting("scan-budget-ms", new NumberSetting("Scan Budget ms", 2.0F, 0.5F, 8.0F).step(0.5F));
   private long smoothedNanos;
   private long nextStatsUpdate;
   private String statsText = "";

   public PerformanceModule() {
      super(
         "performance",
         "Performance",
         "Reduces background work and image memory while keeping effects and features. Disable to compare FPS.",
         ModuleCategory.CLIENT
      );
      this.optimize.onChange(this::updateOptimizationState);
      this.scanBudget.visibleWhen(() -> this.profile.is("Custom"));
   }

   public static boolean optimizationsEnabled() {
      return optimizing || net.albiz.client.platform.BuildProfile.mobile();
   }

   @Override
   protected void onEnable() {
      this.updateOptimizationState();
      this.nextStatsUpdate = 0L;
   }

   @Override
   protected void onDisable() {
      optimizing = false;
   }

   private void updateOptimizationState() {
      optimizing = this.isEnabled() && this.optimize.get();
      this.nextStatsUpdate = 0L;
   }

   public static boolean frustumCulling() {
      PerformanceModule module = ModuleRegistry.get(PerformanceModule.class);
      return module == null || module.frustumCulling.get();
   }

   public static long scanBudgetNanos() {
      PerformanceModule module = ModuleRegistry.get(PerformanceModule.class);
      boolean mobile = net.albiz.client.platform.BuildProfile.mobile();
      if (module == null) {
         return mobile ? 750000L : 2000000L;
      }

      return (long)(profile(module).scanBudgetMs(module.scanBudget.get(), mobile) * 1000000.0F);
   }

   private static net.albiz.client.perf.PerformanceProfile profile(PerformanceModule module) {
      return net.albiz.client.perf.PerformanceProfile.byLabel(module.profile.get());
   }

   /** Chunks a budgeted scanner may process this tick, after the selected profile and the mobile reduction. */
   public static int chunksPerTick(int requested) {
      PerformanceModule module = ModuleRegistry.get(PerformanceModule.class);
      net.albiz.client.perf.PerformanceProfile selected = module == null ? net.albiz.client.perf.PerformanceProfile.CUSTOM : profile(module);
      // The scanners' own defaults are already lower on mobile, so only presets apply the extra mobile halving.
      boolean mobile = selected != net.albiz.client.perf.PerformanceProfile.CUSTOM && net.albiz.client.platform.BuildProfile.mobile();
      return selected.chunksPerTick(requested, mobile);
   }

   @Override
   public boolean isHudOverlayEnabled() {
      return this.isEnabled() && this.showStats.get();
   }

   @Override
   public void renderHudOverlay(ImDrawList dl, float screenW, float screenH) {
      this.smoothedNanos = this.smoothedNanos + (Render3DEvent.lastDurationNanos() - this.smoothedNanos) / 8L;
      long now = System.nanoTime();
      if (now >= this.nextStatsUpdate) {
         String scans = optimizationsEnabled() ? String.format(Locale.ROOT, "  ·  scans %.2f ms", PerformanceBudget.lastTickNanos() / 1000000.0) : "";
         this.statsText = String.format(
            Locale.ROOT,
            "%s  ·  %d FPS  ·  3D CPU %.2f ms%s  ·  queue %d",
            optimizationsEnabled() ? "Optimized" : "Standard",
            class_310.method_1551().method_47599(),
            this.smoothedNanos / 1000000.0,
            scans,
            ChunkScanner.pending()
         );
         this.nextStatsUpdate = now + 250000000L;
      }

      String text = this.statsText;
      ImFont font = ImGuiManager.getInstance().hudLabelFont();
      float x = screenW - AlbizWidgets.textWidth(font, text) - 8.0F;
      float y = screenH - AlbizWidgets.lineHeight(font) - 6.0F;
      AlbizWidgets.text(dl, font, x + 1.0F, y + 1.0F, -1610612736, text);
      AlbizWidgets.text(dl, font, x, y, -3618616, text);
   }
}
