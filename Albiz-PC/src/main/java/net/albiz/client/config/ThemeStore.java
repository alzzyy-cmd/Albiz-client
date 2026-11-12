package net.albiz.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.albiz.client.imgui.UiFont;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.ModeSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.module.setting.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class ThemeStore {
   private static final Logger LOGGER = LoggerFactory.getLogger("Albiz");
   private static final ThemeStore INSTANCE = new ThemeStore();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int CONFIG_VERSION = 7;
   private static final String THEME_KEY = "albizTheme";
   private static final String DEFAULT_BANNER = "donutbets-banner.png";
   private static final String BANNER_RESOURCE = "/assets/albiz/textures/gui/donutbets_banner.png";
   public static final List<ThemeStore.Preset> PRESETS = List.of(
      new ThemeStore.Preset("Default", 0xFFE3262E, 0xFF160F20, 0xFF100B16),
      new ThemeStore.Preset("Midnight", -7643914, -15001050, -15461603),
      new ThemeStore.Preset("Rose", -770210, -14410720, -15002344),
      new ThemeStore.Preset("Mint", -15681151, -15064288, -15459560),
      new ThemeStore.Preset("Amber", -680437, -14409445, -15001324),
      new ThemeStore.Preset("Mono", -1710101, -14671840, -15263977)
   );
   public final ModeSetting platform = new ModeSetting("Platform", "Auto", "Auto", "Mobile", "Desktop");
   public final ModeSetting language = new ModeSetting("Language", "en_us", "Auto", "en_us", "tr_tr");
   public final ColorSetting accent = new ColorSetting("Accent", 0xFFE3262E);
   public final NumberSetting rounding = new NumberSetting("Corner radius", 16.0F, 0.0F, 16.0F).step(1.0F);
   public final NumberSetting backgroundDim = new NumberSetting("Background dim", 0.55F, 0.0F, 0.8F).step(0.05F);
   public final BooleanSetting menuBlur = new BooleanSetting("Menu blur", !net.albiz.client.platform.PlatformInfo.isMobile());
   public final BooleanSetting moduleDividers = new BooleanSetting("Module dividers", true);
   public final NumberSetting uiScale = new NumberSetting("UI scale", net.albiz.client.platform.PlatformInfo.isMobile() ? 1.2F : 1.0F, 0.75F, 2.0F).step(0.05F);
   public final ModeSetting font = new ModeSetting("Font", UiFont.GEIST.label, UiFont.labels());
   public final NumberSetting animationSpeed = new NumberSetting("Animation speed", 1.3F, 0.0F, 2.0F).step(0.1F);
   /** Which menu the open-GUI key shows: the Sober-style menu or the classic Albiz ImGui menu. */
   public final ModeSetting menuStyle = new ModeSetting("Menu style", "Sober", "Sober", "Albiz");
   public final ModeSetting settingsTransition = new ModeSetting("Settings transition", "Smooth", "Smooth", "Fast", "Soft", "Custom", "Off");
   public final NumberSetting transitionDuration = new NumberSetting("Transition duration", 320.0F, 60.0F, 1200.0F).step(20.0F);
   public final ModeSetting transitionEasing = new ModeSetting("Transition easing", "Ease Out", "Linear", "Ease Out", "Ease In Out", "Ease Out Strong", "Overshoot");
   public final NumberSetting transitionDistance = new NumberSetting("Transition distance", 0.35F, 0.0F, 1.0F).step(0.05F);
   public final NumberSetting transitionOpacity = new NumberSetting("Transition start opacity", 0.0F, 0.0F, 1.0F).step(0.05F);
   public final NumberSetting transitionScale = new NumberSetting("Transition start scale", 0.96F, 0.5F, 1.0F).step(0.01F);
   public final ModeSetting transitionDirection = new ModeSetting("Transition direction", "From Top", "From Top", "From Bottom", "From Left", "From Right");
   public final BooleanSetting animateLines = new BooleanSetting("Animate patterns", !net.albiz.client.platform.PlatformInfo.isMobile());
   public final BooleanSetting customMainMenu = new BooleanSetting("Custom menus", true);
   public final BooleanSetting communityPopup = new BooleanSetting("Community popup", true);
   public final Surface panels = new Surface("panels", "Panels", false, 0xFF160F20, 1.0F, 0.45F, "Flow");
   public final Surface cards = new Surface("cards", "Cards", true, 0xFF100B16, 1.0F, 0.85F);
   public final Surface tabBar = new Surface("tabbar", "Tab bar", false, 0xFF100B16, 1.0F, 0.5F, "Flow");
   private final List<Surface> surfaces = List.of(this.panels, this.cards, this.tabBar);
   private final List<Setting> shared;
   private final List<Setting> settings;
   private final JsonObject defaults;
   private boolean loaded;
   private boolean readOnly;
   private boolean needsBrandSave;

   private ThemeStore() {
      for (Setting custom : List.of(this.transitionDuration, this.transitionEasing, this.transitionDistance, this.transitionOpacity, this.transitionScale, this.transitionDirection)) {
         custom.visibleWhen(() -> this.settingsTransition.is("Custom"));
      }

      this.platform.onChange(() -> net.albiz.client.platform.PlatformInfo.setMode(this.platform.get()));
      this.language.onChange(() -> net.albiz.client.i18n.AlbizI18n.setLanguage(this.language.get()));
      List<Setting> all = new ArrayList<>(
         List.of(
            this.platform, this.language, this.accent, this.rounding, this.backgroundDim, this.menuBlur, this.moduleDividers, this.uiScale, this.font, this.animationSpeed, this.animateLines,
            this.settingsTransition, this.transitionDuration, this.transitionEasing, this.transitionDistance, this.transitionOpacity, this.transitionScale,
            this.transitionDirection, this.menuStyle
         )
      );

      for (Surface surface : this.surfaces) {
         all.addAll(surface.settings());
      }

      this.shared = List.copyOf(all);
      all.add(this.customMainMenu);
      all.add(this.communityPopup);
      this.settings = List.copyOf(all);
      useBanner(this.cards);
      this.defaults = snapshot(this.settings, true);
      this.customMainMenu.onChange(() -> {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.method_22683() != null) {
            mc.method_24288();
         }
      });
   }

   /** Settings-panel transition: a preset, or the user's custom values. Presets still honour the global animation-speed switch. */
   public net.albiz.client.gui.SettingsTransition.Config transitionConfig() {
      if (this.animationSpeed.get() <= 0.0F) {
         return net.albiz.client.gui.SettingsTransition.Config.OFF;
      }

      net.albiz.client.gui.SettingsTransition.Config preset = net.albiz.client.gui.SettingsTransition.Config.preset(this.settingsTransition.get());
      return preset != null
         ? preset
         : new net.albiz.client.gui.SettingsTransition.Config(
            true,
            this.transitionDuration.get(),
            net.albiz.client.gui.SettingsTransition.Easing.byLabel(this.transitionEasing.get()),
            this.transitionDistance.get(),
            this.transitionOpacity.get(),
            this.transitionScale.get(),
            net.albiz.client.gui.SettingsTransition.Direction.byLabel(this.transitionDirection.get()),
            0.25F
         );
   }

   public static ThemeStore get() {
      INSTANCE.ensureLoaded();
      return INSTANCE;
   }

   public List<Surface> surfaces() {
      return this.surfaces;
   }

   public void applyPreset(ThemeStore.Preset preset) {
      this.accent.setArgb(preset.accent());
      this.panels.color.setArgb(preset.panel());
      this.cards.color.setArgb(preset.card());
      this.tabBar.color.setArgb(preset.card());
   }

   public boolean matchesPreset(ThemeStore.Preset preset) {
      return this.accent.rgb() == (preset.accent() & 16777215)
         && this.panels.color.rgb() == (preset.panel() & 16777215)
         && this.cards.color.rgb() == (preset.card() & 16777215);
   }

   public void resetToDefaults() {
      apply(this.shared, this.defaults, true);
      this.installBanner();
      this.installBrandArt();
   }

   public static Path backgroundsDir() {
      return net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("backgrounds");
   }

   public String exportString() {
      JsonObject root = new JsonObject();
      root.add("albizTheme", snapshot(this.shared, false));
      return root.toString();
   }

   public boolean importString(String text) {
      if (text != null && !text.isBlank()) {
         try {
            JsonElement parsed = JsonParser.parseString(text.trim());
            if (parsed.isJsonObject() && (parsed.getAsJsonObject().has("albizTheme") || parsed.getAsJsonObject().has("anubisTheme"))) {
               JsonElement values = parsed.getAsJsonObject().get(parsed.getAsJsonObject().has("albizTheme") ? "albizTheme" : "anubisTheme");
               if (!values.isJsonObject()) {
                  return false;
               } else {
                  apply(this.shared, values.getAsJsonObject(), false);
                  return true;
               }
            } else {
               return false;
            }
         } catch (RuntimeException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public JsonObject profileSnapshot() {
      return snapshot(this.shared, true);
   }

   public void applyProfile(JsonObject values) {
      for (Setting setting : this.shared) {
         JsonElement value = values.get(setting.id());
         if (value == null && !setting.isPrivate()) {
            value = this.defaults.get(setting.id());
         }

         if (value != null) {
            setting.deserialize(value);
         }
      }
   }

   public JsonObject shareable(JsonObject values) {
      JsonObject kept = new JsonObject();

      for (Setting setting : this.shared) {
         JsonElement value = values.get(setting.id());
         if (value != null && !setting.isPrivate() && !Setting.sameValue(value, this.defaults.get(setting.id()))) {
            kept.add(setting.id(), value.deepCopy());
         }
      }

      return kept;
   }

   public synchronized void load() {
      if (!this.loaded) {
         this.loaded = true;
         boolean existed = Files.isRegularFile(this.configPath());
         this.readFile();
         if (this.needsBrandSave) this.save();
         if (!existed) { this.applyRedBrandDefaults(); this.save(); }
         this.installBanner();
      this.installBrandArt();
      }
   }

   private void readFile() {
      Path path = this.configPath();
      if (Files.isRegularFile(path)) {
         try {
            try (Reader reader = Files.newBufferedReader(path)) {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
               int version = root.has("version") ? root.get("version").getAsInt() : 0;
               if (version <= 7) {
                  if (root.has("values") && root.get("values").isJsonObject()) {
                     apply(this.settings, root.getAsJsonObject("values"), true);
                  }

                  if (version < 3) {
                     this.adoptNewDefaults();
                  }

                  if (version < 4) {
                     this.adoptBanner();
                  }

                  if (version < 5) {
                     Path backup = path.resolveSibling("theme.before-albiz-red-v2.json");
                     if (!Files.exists(backup)) Files.copy(path, backup);
                     this.applyRedBrandDefaults();
                     this.needsBrandSave = true;
                  }
                  if (version < 7) {
                     Path backup = path.resolveSibling("theme.before-english-v4.json");
                     if (!Files.exists(backup)) Files.copy(path, backup);
                     this.language.set("en_us");
                     this.needsBrandSave = true;
                  }
                  return;
               }

               this.readOnly = true;
               LOGGER.warn("[Albiz] Theme version {} is newer than supported {}; skipping load", version, 7);
            }
         } catch (RuntimeException ex) {
         LOGGER.warn("[Albiz] Invalid theme; backing it up and using red defaults", ex);
         try {
            AtomicFiles.backupCorrupt(path);
            apply(this.settings, this.defaults, true);
            this.applyRedBrandDefaults();
            this.needsBrandSave = true;
         } catch (IOException backupFailure) {
            this.readOnly = true;
            LOGGER.warn("[Albiz] Could not preserve invalid theme; leaving file untouched", backupFailure);
         }
      } catch (IOException ex) {
         LOGGER.warn("[Albiz] Could not read theme; preserving the file", ex);
         this.readOnly = true;
      }
      }
   }

   private void applyRedBrandDefaults() {
      this.applyPreset(PRESETS.get(0));
      this.panels.background.set("None");
      this.cards.background.set("None");
      this.tabBar.background.set("None");
      this.customMainMenu.set(true);
      this.communityPopup.set(false);
      if (net.albiz.client.platform.PlatformInfo.isMobile()) { this.menuBlur.set(false); this.animateLines.set(false); this.uiScale.set(1.25F); }
   }

   private void adoptNewDefaults() {
      if (this.panels.background.is("None")) {
         this.panels.background.set("Flow");
      }

      if (Math.abs(this.panels.opacity.get() - 0.92F) < 0.001F) {
         this.panels.opacity.set(1.0F);
      }

      for (Surface surface : List.of(this.cards, this.tabBar)) {
         if (Math.abs(surface.opacity.get() - 0.97F) < 0.001F) {
            surface.opacity.set(1.0F);
         }
      }
   }

   private void adoptBanner() {
      if (this.cards.background.is("None") && this.cards.image.get().isBlank()) {
         useBanner(this.cards);
      }
   }

   private static void useBanner(Surface surface) {
      surface.background.set("Image");
      surface.image.set("donutbets-banner.png");
      surface.coverage.set("Header");
      surface.headerHeight.set(157.0F);
      surface.imageOpacity.set(1.0F);
      surface.imageScale.set(1.1F);
      surface.imageX.set(0.6F);
      surface.imageY.set(0.55F);
   }

   private void installBrandArt() {
      Path target=backgroundsDir().resolve("albiz-wolf-sword.png");
      if(Files.exists(target))return; // Never overwrite a user's chosen artwork.
      try(InputStream in=ThemeStore.class.getResourceAsStream("/assets/albiz/textures/gui/albiz-wolf-sword.png")) {
         if(in!=null){Files.createDirectories(target.getParent());Files.copy(in,target);}
      }catch(IOException ex){LOGGER.warn("[Albiz] Could not install optional wolf/sword background",ex);}
   }

   private void installBanner() {
      if ("donutbets-banner.png".equals(this.cards.image.get())) {
         Path target = backgroundsDir().resolve("donutbets-banner.png");
         if (!Files.isRegularFile(target)) {
            try {
               try (InputStream in = ThemeStore.class.getResourceAsStream("/assets/albiz/textures/gui/donutbets_banner.png")) {
                  if (in != null) {
                     Files.createDirectories(target.getParent());
                     Files.copy(in, target);
                     return;
                  }
               }
            } catch (IOException var7) {
               LOGGER.warn("[Albiz] Could not install the default card banner", var7);
            }
         }
      }
   }

   public synchronized void save() {
      if (!this.readOnly && this.loaded) {
         JsonObject root = new JsonObject();
         root.addProperty("version", 7);
         root.add("values", snapshot(this.settings, true));
         Path path = this.configPath();

         try {
            AtomicFiles.writeUtf8(path, GSON.toJson(root));
         } catch (IOException var10) {
            LOGGER.warn("[Albiz] Failed to save theme", var10);
         }
      }
   }

   private static JsonObject snapshot(List<Setting> from, boolean withPrivate) {
      JsonObject values = new JsonObject();

      for (Setting setting : from) {
         if (withPrivate || !setting.isPrivate()) {
            values.add(setting.id(), setting.serialize());
         }
      }

      return values;
   }

   private static void apply(List<Setting> from, JsonObject values, boolean withPrivate) {
      for (Setting setting : from) {
         if ((withPrivate || !setting.isPrivate()) && values.has(setting.id())) {
            setting.deserialize(values.get(setting.id()));
         }
      }
   }

   private void ensureLoaded() {
      if (!this.loaded) {
         this.load();
      }
   }

   private Path configPath() {
      return net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("theme.json");
   }

   @Environment(EnvType.CLIENT)
   public record Preset(String name, int accent, int panel, int card) {
   }
}
