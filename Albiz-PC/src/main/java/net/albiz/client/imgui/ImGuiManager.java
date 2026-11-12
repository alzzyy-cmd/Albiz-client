package net.albiz.client.imgui;

import com.mojang.blaze3d.opengl.GlStateManager;
import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import net.albiz.client.imgui.GuardedImGui;
import imgui.ImGuiIO;
import imgui.gl3.AlbizGl3;
import imgui.glfw.ImGuiImplGlfw;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import net.albiz.client.config.HudTheme;
import net.albiz.client.config.ThemeStore;
import net.albiz.client.gui.GuiPalette;
import net.albiz.client.gui.ThemeImages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_11875;
import net.minecraft.class_11876;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Environment(EnvType.CLIENT)
public final class ImGuiManager {
   private static final ImGuiManager INSTANCE = new ImGuiManager();
   private static final String MODULE_FONT_RESOURCE = "/assets/albiz/fonts/geist-medium.ttf";
   private static final String MENU_FONT_RESOURCE = "/assets/albiz/fonts/rajdhani-bold.ttf";
   public static final float MENU_FONT_SIZE = 24.0F;
   public static final float MENU_TAG_FONT_SIZE = 15.0F;
   public static final float MENU_BODY_FONT_SIZE = 18.0F;
   public static final float MENU_NAME_FONT_SIZE = 20.0F;
   public static final float MENU_ITEM_FONT_SIZE = 32.0F;
   public static final float HUD_VALUE_FONT_SIZE = 18.0F;
   public static final float HUD_TEXT_FONT_SIZE = 17.0F;
   public static final float HUD_LABEL_FONT_SIZE = 14.0F;
   private static final String EXTENDED_LATIN_GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ÇĞİÖŞÜçğıöşüÂâÊêÎîÔôÛûÀÁÄÈÉËÌÍÏÒÓÖÙÚÜÝàáäèéëìíïòóöùúüýÿ−∞";
   private static final float REFERENCE_HEIGHT = 1080.0F;
   private final ImGuiImplGlfw glfw = new ImGuiImplGlfw() {
      public void keyCallback(long window, int keycode, int scancode, int action, int mods) {
         if (window == 0L || !net.albiz.client.input.ModuleKeybinds.isBindable(keycode) || keycode >= 1000 || action < 0 || action > 2) return;
         net.albiz.client.input.KeyCapture.key(keycode, action);
         boolean tracked = keycode >= 0 && keycode < ImGuiManager.this.imguiKeysDown.length;
         boolean feed = action == 0 ? tracked && ImGuiManager.this.imguiKeysDown[keycode] : ImGuiManager.this.feedsKeyPress(keycode, mods);
         if (!feed) {
            if (ImGuiManager.this.minecraftKeys != null) {
               ImGuiManager.this.minecraftKeys.invoke(window, keycode, scancode, action, mods);
            }
         } else {
            if (tracked) {
               ImGuiManager.this.imguiKeysDown[keycode] = action != 0;
            }

            super.keyCallback(window, keycode, scancode, action, mods);
         }
      }

      @Override public void mouseButtonCallback(long window, int button, int action, int mods) {
         net.albiz.client.input.KeyCapture.mouse(button, action);
         super.mouseButtonCallback(window, button, action, mods);
      }

      public void monitorCallback(long monitor, int event) {
         super.monitorCallback(monitor, event);
         if (ImGuiManager.this.minecraftMonitors != null) {
            ImGuiManager.this.minecraftMonitors.invoke(monitor, event);
         }
      }

      public void cursorPosCallback(long window, double x, double y) {
         super.cursorPosCallback(window, x, y);
         ImGuiManager.feedScaledCursor(x, y);
      }

      protected void updateMouseData() {
         super.updateMouseData();
         long handle = class_310.method_1551().method_22683().method_4490();
         if (GLFW.glfwGetWindowAttrib(handle, 131073) != 0) {
            double[] cx = new double[1];
            double[] cy = new double[1];
            GLFW.glfwGetCursorPos(handle, cx, cy);
            ImGuiManager.feedScaledCursor(cx[0], cy[0]);
         }
      }
   };
   private final AlbizGl3 gl3 = new AlbizGl3();
   private final boolean[] imguiKeysDown = new boolean[349];
   private GLFWKeyCallback minecraftKeys;
   private GLFWMonitorCallback minecraftMonitors;
   private boolean initialized;
   private boolean idleSkipped;
   private Runnable activeRenderer;
   private Runnable hudRenderer;
   private BooleanSupplier hudHasWork = () -> true;
   private Runnable toastRenderer;
   private BooleanSupplier toastHasWork = () -> false;
   private Runnable overlayRenderer;
   private Runnable streamRenderer;
   private BooleanSupplier streamHasWork = () -> false;
   private BooleanSupplier overlayHasWork = () -> false;
   private Runnable alertRenderer;
   private BooleanSupplier alertHasWork = () -> false;
   private float uiScale = 1.0F;
   private float userScale = 1.0F;
   private UiFont uiFont = UiFont.GEIST;
   private UiFont hudFont = UiFont.GEIST;
   private int frameCount;
   private float frameDelta;
   private ImFont categoryFont;
   private ImFont moduleFont;
   private ImFont titleFont;
   private ImFont smallFont;
   private ImFont hudValueFont;
   private ImFont hudTextFont;
   private ImFont hudLabelFont;
   private ImFont menuFont;
   private ImFont menuTagFont;
   private ImFont menuBodyFont;
   private ImFont menuNameFont;
   private ImFont menuItemFont;
   private short[] glyphRanges;

   private static void feedScaledCursor(double x, double y) {
      float sx = pixelsPerPointX();
      float sy = pixelsPerPointY();
      if (sx != 1.0F || sy != 1.0F) {
         GuardedImGui.getIO().addMousePosEvent((float)(x * sx), (float)(y * sy));
      }
   }

   private static float pixelsPerPointX() {
      class_1041 window = class_310.method_1551().method_22683();
      return window.method_4480() > 0 ? (float)window.method_4489() / window.method_4480() : 1.0F;
   }

   private static float pixelsPerPointY() {
      class_1041 window = class_310.method_1551().method_22683();
      return window.method_4507() > 0 ? (float)window.method_4506() / window.method_4507() : 1.0F;
   }

   private ImGuiManager() {
   }

   public static ImGuiManager getInstance() {
      return INSTANCE;
   }

   public ImFont categoryFont() {
      return this.categoryFont;
   }

   public ImFont moduleFont() {
      return this.moduleFont;
   }

   public ImFont titleFont() {
      return this.titleFont;
   }

   public ImFont smallFont() {
      return this.smallFont;
   }

   public ImFont hudValueFont() {
      return this.hudValueFont;
   }

   public ImFont hudTextFont() {
      return this.hudTextFont;
   }

   public ImFont hudLabelFont() {
      return this.hudLabelFont;
   }

   public ImFont menuFont() {
      return this.menuFont;
   }

   public ImFont menuTagFont() {
      return this.menuTagFont;
   }

   public ImFont menuBodyFont() {
      return this.menuBodyFont;
   }

   public ImFont menuNameFont() {
      return this.menuNameFont;
   }

   public ImFont menuItemFont() {
      return this.menuItemFont;
   }

   public float uiScale() {
      return this.uiScale;
   }

   public int frameCount() {
      return this.frameCount;
   }

   public float frameDelta() {
      return this.frameDelta;
   }

   public void activate(Runnable renderer) {
      this.activeRenderer = renderer;
   }

   public boolean typing() {
      return this.initialized && GuardedImGui.getIO().getWantTextInput();
   }

   public void requestCursor(class_332 graphics) {
      if (this.initialized) {
         class_11875 cursor = switch (GuardedImGui.getMouseCursor()) {
            case 1 -> class_11876.field_62453;
            case 2 -> class_11876.field_62458;
            case 3 -> class_11876.field_62456;
            case 4 -> class_11876.field_62457;
            default -> null;
            case 7 -> class_11876.field_62455;
            case 8 -> class_11876.field_62459;
         };
         if (cursor != null) {
            graphics.method_74037(cursor);
         }
      }
   }

   private boolean feedsKeyPress(int keycode, int mods) {
      return this.activeRenderer != null && (keycode != 258 || (mods & 2) == 0);
   }

   public void deactivate() {
      this.activeRenderer = null;
      if (this.initialized) {
         String ini = GuardedImGui.getIO().getIniFilename();
         if (ini != null) {
            GuardedImGui.saveIniSettingsToDisk(ini);
         }
      }
   }

   public void setHudRenderer(Runnable renderer, BooleanSupplier hasWork) {
      this.hudRenderer = renderer;
      this.hudHasWork = hasWork;
   }

   public void setToastRenderer(Runnable renderer, BooleanSupplier hasWork) {
      this.toastRenderer = renderer;
      this.toastHasWork = hasWork;
   }

   public void setStreamRenderer(Runnable renderer, BooleanSupplier hasWork) {
      this.streamRenderer = renderer;
      this.streamHasWork = hasWork;
   }

   public void setOverlayRenderer(Runnable renderer, BooleanSupplier hasWork) {
      this.overlayRenderer = renderer;
      this.overlayHasWork = hasWork;
   }

   public void setAlertRenderer(Runnable renderer, BooleanSupplier hasWork) {
      this.alertRenderer = renderer;
      this.alertHasWork = hasWork;
   }

   public void renderIfActive() {
      boolean hud = this.hudRenderer != null && this.hudHasWork.getAsBoolean();
      boolean toasts = this.toastRenderer != null && this.toastHasWork.getAsBoolean();
      boolean overlay = this.overlayRenderer != null && this.overlayHasWork.getAsBoolean();
      boolean alert = this.alertRenderer != null && this.alertHasWork.getAsBoolean();
      boolean stream = this.streamRenderer != null && this.streamHasWork.getAsBoolean();
      if (this.activeRenderer == null && !hud && !toasts && !overlay && !alert && !stream) {
         if (this.initialized) {
            GuardedImGui.getIO().clearEventsQueue();
            this.idleSkipped = true;
         }
      } else {
         this.initIfNeeded();
         this.rebuildFontsIfNeeded();
         if (this.idleSkipped) {
            ImGuiIO io = GuardedImGui.getIO();
            io.clearInputKeys();
            io.clearInputMouse();
            Arrays.fill(this.imguiKeysDown, false);
            this.idleSkipped = false;
         }

         class_310 mc = class_310.method_1551();
         int w = mc.method_22683().method_4489();
         int h = mc.method_22683().method_4506();
         GL30.glBindFramebuffer(36160, 0);
         GL11.glViewport(0, 0, w, h);
         GlStateManager._viewport(0, 0, w, h);
         this.glfw.newFrame();
         this.frameDelta = Math.min(GuardedImGui.getIO().getDeltaTime(), 0.1F);
         GuardedImGui.getIO().setDeltaTime(this.frameDelta);
         this.updateDisplayMetrics();
         this.gl3.newFrame();
         ThemeImages.get().beginFrame();
         GuardedImGui.newFrame();
         this.frameCount++;
         boolean ended = false;
         int scopeMark = GuardedImGui.scopeMark();

         try {
            GuiPalette.refresh();
            this.renderLayer("stream", this.streamRenderer);

            this.renderLayer("hud", this.hudRenderer);

            this.renderLayer("active", this.activeRenderer);

            this.renderLayer("toast", this.toastRenderer);

            this.renderLayer("alert", this.alertRenderer);

            this.renderLayer("overlay", this.overlayRenderer);

            if (GuardedImGui.scopeMark() != scopeMark) {
               System.err.println("[ALBIZ] Recovered leaked UI scopes before frame completion");
               GuardedImGui.recoverScopes(scopeMark);
            }
            GuardedImGui.render();
            ended = true;
            this.gl3.renderDrawData(GuardedImGui.getDrawData());
         } catch (RuntimeException | LinkageError ex) {
            org.slf4j.LoggerFactory.getLogger("ALBIZ").error("UI frame failed before completion", ex);
            GuardedImGui.recoverScopes(scopeMark);
            this.activeRenderer = null;
         } finally {
            try {
               if (!ended) {
                  GuardedImGui.endFrame();
               }
            } finally {
               ThemeImages.get().endFrame();
            }
         }
      }
   }

   private void renderLayer(String layer, Runnable renderer) {
      if (renderer == null) return;
      int mark = GuardedImGui.scopeMark();
      try { renderer.run(); }
      catch (RuntimeException | LinkageError ex) {
         org.slf4j.LoggerFactory.getLogger("ALBIZ").error("UI layer failed: " + layer, ex);
         net.albiz.client.diagnostics.UiFailureNotes.record(layer,ex);
         GuardedImGui.recoverScopes(mark);
         switch (layer) {
            case "stream" -> this.streamRenderer = null;
            case "hud" -> this.hudRenderer = null;
            case "toast" -> this.toastRenderer = null;
            case "alert" -> this.alertRenderer = null;
            case "overlay" -> this.overlayRenderer = null;
            default -> {
               this.activeRenderer = null;
               class_310 client = class_310.method_1551();
               if (client != null && client.field_1755 instanceof net.albiz.client.gui.ImGuiScreen) client.method_1507(null);
            }
         }
      }
   }

   public void dispose() {
      if (this.initialized) {
         this.gl3.shutdown();
         this.glfw.shutdown();
         GuardedImGui.destroyContext();
         this.categoryFont = null;
         this.moduleFont = null;
         this.titleFont = null;
         this.smallFont = null;
         this.hudValueFont = null;
         this.hudTextFont = null;
         this.hudLabelFont = null;
         this.menuFont = null;
         this.menuTagFont = null;
         this.menuBodyFont = null;
         this.menuNameFont = null;
         this.menuItemFont = null;
         this.initialized = false;
      }
   }

   private void initIfNeeded() {
      if (!this.initialized) {
         long windowHandle = class_310.method_1551().method_22683().method_4490();
         GuardedImGui.createContext();
         ImGuiIO io = GuardedImGui.getIO();
         io.setIniFilename(iniPath());
         io.addConfigFlags(1);
         io.addConfigFlags(32);
         GuardedImGui.styleColorsDark();
         if (net.albiz.client.platform.PlatformInfo.isMobile()) {
            GuardedImGui.getStyle().setTouchExtraPadding(6.0F, 6.0F);
            GuardedImGui.getStyle().setScrollbarSize(24.0F);
            GuardedImGui.getStyle().setGrabMinSize(24.0F);
         }
         this.uiScale = this.computeScale(this.uiScale);
         this.uiFont = chosenUiFont();
         this.hudFont = chosenHudFont();
         this.loadFonts(io);
         this.minecraftKeys = GLFW.glfwSetKeyCallback(windowHandle, null);
         GLFW.glfwSetKeyCallback(windowHandle, this.minecraftKeys);
         this.minecraftMonitors = GLFW.glfwSetMonitorCallback(null);
         GLFW.glfwSetMonitorCallback(this.minecraftMonitors);
         this.glfw.init(windowHandle, true);
         this.gl3.init(net.albiz.client.platform.PlatformInfo.isMobile() ? net.albiz.client.platform.PlatformInfo.glslVersion(org.lwjgl.opengl.GL11.glGetString(7938)) : null);
         this.initialized = true;
      }
   }

   private void rebuildFontsIfNeeded() {
      float scale = this.computeScale(this.uiScale);
      UiFont ui = chosenUiFont();
      UiFont hud = chosenHudFont();
      if (scale != this.uiScale || ui != this.uiFont || hud != this.hudFont) {
         this.uiScale = scale;
         this.uiFont = ui;
         this.hudFont = hud;
         ImGuiIO io = GuardedImGui.getIO();
         io.getFonts().clear();
         this.loadFonts(io);
         this.gl3.destroyFontsTexture();
         this.gl3.createFontsTexture();
      }
   }

   private static UiFont chosenUiFont() {
      return UiFont.of(ThemeStore.get().font.get());
   }

   private static UiFont chosenHudFont() {
      return UiFont.of(HudTheme.get().font.get());
   }

   private float computeScale(float current) {
      int height = class_310.method_1551().method_22683().method_4506();
      if (height < 200) {
         return current;
      } else {
         if (!this.initialized || !GuardedImGui.getIO().getMouseDown(0)) {
            this.userScale = ThemeStore.get().uiScale.get();
         }

         float raw = net.albiz.client.gui.ViewportLayout.scale(class_310.method_1551().method_22683().method_4489(), height, this.userScale);
         return Math.round(raw * 20.0F) / 20.0F;
      }
   }

   private static String iniPath() {
      try {
         Path dir = net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir());
         Files.createDirectories(dir);
         return dir.resolve("gui.ini").toString();
      } catch (RuntimeException | IOException var1) {
         System.err.println("[Albiz] GUI layout will not be saved: " + var1.getMessage());
         return null;
      }
   }

   private void updateDisplayMetrics() {
      class_310 client = class_310.method_1551();
      int w = Math.max(1, client.method_22683().method_4489());
      int h = Math.max(1, client.method_22683().method_4506());
      ImGuiIO io = GuardedImGui.getIO();
      io.setDisplaySize(w, h);
      io.setDisplayFramebufferScale(1.0F, 1.0F);
   }

   private void loadFonts(ImGuiIO io) {
      if (this.glyphRanges == null) {
         this.glyphRanges = buildGlyphRanges(io);
      }

      try {
         io.getFonts().setFreeTypeRenderer(true);
      } catch (Throwable var3) {
         System.err.println("[Albiz] FreeType unavailable, using stb_truetype: " + var3.getMessage());
      }

      if (!this.buildFonts(io, this.uiFont, this.hudFont)) {
         if (this.uiFont != UiFont.GEIST || this.hudFont != UiFont.GEIST) {
            System.err.println("[Albiz] Couldn't build " + this.uiFont.label + " / " + this.hudFont.label + " fonts, using Geist");
            io.getFonts().clear();
            if (this.buildFonts(io, UiFont.GEIST, UiFont.GEIST)) {
               return;
            }
         }

         throw new IllegalStateException("ImGui font atlas could not be built");
      }
   }

   private boolean buildFonts(ImGuiIO io, UiFont ui, UiFont hud) {
      float s = this.uiScale;
      this.moduleFont = this.loadFont(io, ui, false, 16.0F * s);
      this.categoryFont = this.loadFont(io, ui, true, 17.0F * s);
      this.titleFont = this.loadFont(io, ui, true, 24.0F * s);
      this.smallFont = this.loadFont(io, ui, false, 13.0F * s);
      this.hudValueFont = this.loadFont(io, hud, true, 18.0F * s);
      this.hudTextFont = this.loadFont(io, hud, true, 17.0F * s);
      this.hudLabelFont = this.loadFont(io, hud, false, 14.0F * s);
      this.menuFont = loadFont(io, "/assets/albiz/fonts/rajdhani-bold.ttf", 24.0F * s, this.glyphRanges);
      this.menuTagFont = loadFont(io, "/assets/albiz/fonts/rajdhani-bold.ttf", 15.0F * s, this.glyphRanges);
      this.menuBodyFont = loadFont(io, "/assets/albiz/fonts/geist-medium.ttf", 18.0F * s, this.glyphRanges);
      this.menuNameFont = loadFont(io, "/assets/albiz/fonts/rajdhani-bold.ttf", 20.0F * s, this.glyphRanges);
      this.menuItemFont = loadFont(io, "/assets/albiz/fonts/rajdhani-bold.ttf", 32.0F * s, this.glyphRanges);
      if (this.moduleFont == null && this.categoryFont == null) {
         io.getFonts().addFontDefault();
      } else if (this.moduleFont != null) {
         io.setFontDefault(this.moduleFont);
      }

      return io.getFonts().build();
   }

   private static short[] buildGlyphRanges(ImGuiIO io) {
      ImFontGlyphRangesBuilder builder = new ImFontGlyphRangesBuilder();
      builder.addRanges(io.getFonts().getGlyphRangesDefault());
      builder.addRanges(new short[] { 0x0100, 0x017F, 0 });
      builder.addText( "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ÇĞİÖŞÜçğıöşüÂâÊêÎîÔôÛûÀÁÄÈÉËÌÍÏÒÓÖÙÚÜÝàáäèéëìíïòóöùúüýÿ−∞");
      return builder.buildRanges();
   }

   private ImFont loadFont(ImGuiIO io, UiFont family, boolean bold, float sizePixels) {
      float size = family.size(sizePixels);
      float offsetY = family.offsetY(size);
      ImFont font = loadFont(io, family.resource(bold), size, this.glyphRanges, family.flags(), offsetY, false);
      if (font == null) {
         return family == UiFont.GEIST ? null : this.loadFont(io, UiFont.GEIST, bold, sizePixels);
      } else {
         if (family != UiFont.GEIST) {
            loadFont(io, UiFont.GEIST.resource(bold), family.fallbackSize(size), this.glyphRanges, UiFont.GEIST.flags(), offsetY, true);
         }

         return font;
      }
   }

   private static ImFont loadFont(ImGuiIO io, String resourcePath, float sizePixels, short[] ranges) {
      return loadFont(io, resourcePath, sizePixels, ranges, 8, 0.0F, false);
   }

   private static ImFont loadFont(ImGuiIO io, String resourcePath, float sizePixels, short[] ranges, int flags, float offsetY, boolean merge) {
      try {
         ImFont var10;
         try (InputStream in = ImGuiManager.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
               System.err.println("[Albiz] Font resource not found: " + resourcePath);
               return null;
            }

            byte[] data = in.readAllBytes();
            ImFontConfig config = new ImFontConfig();

            try {
               config.setOversampleH(net.albiz.client.platform.PlatformInfo.isMobile() ? 1 : 3);
               config.setOversampleV(1);
               config.setPixelSnapH(true);
               config.setFontBuilderFlags(flags);
               config.setGlyphOffset(0.0F, offsetY);
               config.setMergeMode(merge);
               var10 = io.getFonts().addFontFromMemoryTTF(data, Math.round(sizePixels), config, ranges);
            } finally {
               config.destroy();
            }
         }

         return var10;
      } catch (RuntimeException | IOException var19) {
         System.err.println("[Albiz] Failed to load font " + resourcePath + ": " + var19.getMessage());
         return null;
      }
   }
}
