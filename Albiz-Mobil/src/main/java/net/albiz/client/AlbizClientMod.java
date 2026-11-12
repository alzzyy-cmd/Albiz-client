package net.albiz.client;

import net.albiz.client.addon.AddonManager;
import net.albiz.client.config.HudTheme;
import net.albiz.client.config.ModuleConfigStore;
import net.albiz.client.config.ProfileAutoLoad;
import net.albiz.client.config.ThemeStore;
import net.albiz.client.donutbets.DonutBetsFeed;
import net.albiz.client.esp.ChunkScanner;
import net.albiz.client.event.impl.PacketAppliedEvent;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.Render2DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.gui.BorderlessFullscreen;
import net.albiz.client.gui.HudLayoutStore;
import net.albiz.client.gui.HudRenderer;
import net.albiz.client.gui.ImGuiScreen;
import net.albiz.client.gui.LoadingScreens;
import net.albiz.client.gui.StaffGhost;
import net.albiz.client.gui.ToggleToasts;
import net.albiz.client.gui.mainmenu.OfficialBuildBanner;
import net.albiz.client.imgui.ImGuiManager;
import net.albiz.client.input.AlbizKeyPolling;
import net.albiz.client.input.ModuleKeybinds;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.misc.noinvleak.StreamOverlay;
import net.albiz.client.security.OfficialBuild;
import net.albiz.client.util.BlockEntityLeakTracker;
import net.albiz.client.util.CompletionChannel;
import net.albiz.client.util.combat.CombatTarget;
import net.albiz.client.util.netherite.NetheriteChunkTracker;
import net.albiz.client.util.playerbypass.PlayerBypassLightTracker;
import net.albiz.client.util.playerbypass.ProtectedChunkStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_11908;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class AlbizClientMod implements ClientModInitializer {
   public static final Logger LOGGER = LoggerFactory.getLogger("Albiz");
   public static final class_2960 SPAWNER_NOTIFY_SOUND_ID = class_2960.method_60655("albiz", "spawner_notify");
   public static final class_3414 SPAWNER_NOTIFY_SOUND = class_3414.method_47908(SPAWNER_NOTIFY_SOUND_ID);
   public static final class_11900 KEY_CATEGORY = class_11900.method_74698(class_2960.method_60655("albiz", "controls"));
   private static class_304 openGuiKey;
   private final ModuleKeybinds keybinds = new ModuleKeybinds();
   private static final Object LEAK_TRACKER_OWNER = new Object();
   private static final Object PLAYER_BYPASS_TRACKER_OWNER = new Object();
   private static final Object NETHERITE_TRACKER_OWNER = new Object();

   public void onInitializeClient() {
      LOGGER.info("[Albiz] Initialising");
      AddonManager.init();
      net.albiz.client.gui.UiFeedback.installSlider(net.albiz.client.module.impl.client.SoundFeedbackModule::sliderChanged);
      ImGuiManager.getInstance().setHudRenderer(HudRenderer.getInstance()::renderVisualOverlays, HudRenderer.getInstance()::hasVisualOverlays);
      ImGuiManager.getInstance().setStreamRenderer(StreamOverlay::drawFakes, StreamOverlay::hasFakes);
      ImGuiManager.getInstance().setToastRenderer(()->{}, ()->false);
      ImGuiManager.getInstance().setOverlayRenderer(LoadingScreens::render, LoadingScreens::hasWork);
      ImGuiManager.getInstance().setAlertRenderer(StaffGhost::render, StaffGhost::hasWork);
      Render2DEvent.TYPE.listen(StaffGhost.class, StaffGhost::renderNote);
      ChunkScanner.init();
      PacketReceiveNettyEvent.TYPE.listen(LEAK_TRACKER_OWNER, event -> BlockEntityLeakTracker.ingest(event.packet));
      CompletionChannel.install();
      DonutBetsFeed.start();
      PacketAppliedEvent.TYPE.listen(PLAYER_BYPASS_TRACKER_OWNER, event -> PlayerBypassLightTracker.onPacketApplied(event.packet));
      TickEvent.TYPE.listen(PLAYER_BYPASS_TRACKER_OWNER, event -> PlayerBypassLightTracker.onTick());
      TickEvent.TYPE.listen(BorderlessFullscreen.class, event -> BorderlessFullscreen.tick());
      CombatTarget.install();
      PacketAppliedEvent.TYPE.listen(NETHERITE_TRACKER_OWNER, event -> NetheriteChunkTracker.onPacketApplied(event.packet));
      ToggleToasts.install();
      ProfileAutoLoad.install();
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
         HudLayoutStore.getInstance().save();
         ModuleConfigStore.getInstance().saveAndWait();
         ThemeStore.get().save();
         HudTheme.get().save();
         ProtectedChunkStore.get().flushAndWait();
         StreamOverlay.stop();
      });
      openGuiKey = KeyBindingHelper.registerKeyBinding(new class_304("key.albiz.open_gui", class_307.field_1668, 344, KEY_CATEGORY));
      net.albiz.client.port.soberui.SoberUi.installVisuals();
      ModuleConfigStore.getInstance().load();
      OfficialBuild.enforceIfPending();
      OfficialBuildBanner.installVanillaTitleScreen();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> OfficialBuild.enforceOnFirstTick());
      AddonManager.configLoaded();
      net.albiz.client.i18n.LanguageChoice.applySaved();
      net.albiz.client.port.soberui.SoberUi.init();
      net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(net.albiz.client.port.soberui.SoberUi::render);
      ClientTickEvents.END_CLIENT_TICK.register(client->net.albiz.client.port.soberui.SoberUi.tick());
      ClientLifecycleEvents.CLIENT_STOPPING.register(client->net.albiz.client.port.soberui.SoberUi.save());
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               ModuleConfigStore.getInstance().applyPendingEnabled();

               while (openGuiKey != null && openGuiKey.method_1436()) {
                  if (net.albiz.client.port.soberui.SoberUi.isMenu(client.field_1755)) {
                     client.method_1507(null);
                  } else {
                     client.method_1507(new ImGuiScreen());
                  }
               }

               this.keybinds
                  .update(ModuleRegistry.all(), AlbizClientMod::isBoundKeyDown, client.field_1755 == null && client.method_1569() && client.field_1724 != null);
               ModuleConfigStore.getInstance().tickAutosave();
            }
         );
   }

   public static boolean isGuiKey(long window, class_11908 event) {
      class_310 client = class_310.method_1551();
      return event != null && openGuiKey != null && window != 0L && client != null && client.method_22683() != null
         && window == client.method_22683().method_4490() && openGuiKey.method_1417(event);
   }

   public static boolean toggleGuiFromKey() {
      class_310 client = class_310.method_1551();
      if (client == null) return false;
      if (client.field_1755 == null) {
         client.method_1507(new ImGuiScreen());
         return true;
      } else {
         if (net.albiz.client.port.soberui.SoberUi.isMenu(client.field_1755) || client.field_1755 instanceof ImGuiScreen gui && !gui.capturesKeys()) {
            client.method_1507(null);
         }

         return false;
      }
   }

   public static void dropGuiClicks() {
      while (openGuiKey != null && openGuiKey.method_1436()) {
      }
   }

   private static boolean isBoundKeyDown(int key) {
      class_310 client = class_310.method_1551();
      if (!ModuleKeybinds.isBindable(key) || client == null || client.method_22683() == null || client.method_22683().method_4490() == 0L) return false;
      return key >= 1000
         ? GLFW.glfwGetMouseButton(client.method_22683().method_4490(), key - 1000) == 1
         : AlbizKeyPolling.isKeyDown(client.method_22683(), key);
   }
}
