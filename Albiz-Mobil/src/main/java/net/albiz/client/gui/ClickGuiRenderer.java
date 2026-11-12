package net.albiz.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import net.albiz.client.imgui.GuardedImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.albiz.client.config.BundledDefaults;
import net.albiz.client.config.ConfigProfiles;
import net.albiz.client.config.FriendStore;
import net.albiz.client.config.HudTheme;
import net.albiz.client.config.ModuleCodes;
import net.albiz.client.config.ModuleConfigStore;
import net.albiz.client.config.ProfileException;
import net.albiz.client.config.Surface;
import net.albiz.client.config.ThemeStore;
import net.albiz.client.imgui.ImGuiManager;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.client.HotkeysModule;
import net.albiz.client.module.impl.client.HudModule;
import net.albiz.client.module.impl.client.PerformanceModule;
import net.albiz.client.module.impl.client.RadarModule;
import net.albiz.client.module.impl.client.RegionMapModule;
import net.albiz.client.module.setting.ActionSetting;
import net.albiz.client.module.setting.BlockColorListSetting;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.ItemColorListSetting;
import net.albiz.client.module.setting.KeySetting;
import net.albiz.client.module.setting.ModeSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.module.setting.Setting;
import net.albiz.client.module.setting.StringListSetting;
import net.albiz.client.module.setting.StringSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class ClickGuiRenderer {
   private static final String[] KEY_TEXT = new String[349];
   static {
      for (int i = 0; i < KEY_TEXT.length; i++) KEY_TEXT[i] = "K" + i;
      for (int i = 65; i <= 90; i++) KEY_TEXT[i] = String.valueOf((char)i);
      for (int i = 48; i <= 57; i++) KEY_TEXT[i] = String.valueOf((char)i);
      for (int i = 290; i <= 314; i++) KEY_TEXT[i] = "F" + (i - 289);
   }

   private static final float PANEL_W = 212.0F;
   private static final float PANEL_GAP = 14.0F;
   private static final float PANEL_TOP = 28.0F;
   private static final float HEADER_H = 42.0F;
   private static final float ROW_H = 30.0F;
   private static final float ROW_GAP = 2.0F;
   private static final float ROW_INSET = 6.0F;
   private static final int HEADER_BAND = 251658239;
   private static final int ROW_RULE = 620756991;
   private static final float CARD_W = 460.0F;
   private static final float CARD_MAX_H = 760.0F;
   private static final float CARD_PAD = 24.0F;
   private static final float SETTING_ROW_H = 46.0F;
   private static final float TAB_BAR_H = 44.0F;
   private static final float TAB_BAR_BOTTOM = 26.0F;
   private static final float INSPECTOR_W = 360.0F;
   private static final float SMALL_TOG_W = 16.0F;
   private static final float SMALL_TOG_H = 16.0F;
   public static final int ERROR_TEXT = GuiPalette.DANGER;
   private static final int[] AVATAR_COLORS = new int[]{-624588, -10893516, -16080897, -2207057, -10536961, -3690448, -2730408, -16724737};
   private static final int PANEL_FLAGS = 123;
   private static final int CARD_FLAGS = 8511;
   public static final int CHILD_FLAGS = 24;
   private static final String PANEL_ID_PREFIX = "##panel_";
   private static final String SEARCH_PANEL_ID = "##panel_search";
   private static final int STYLE_VAR_COUNT = 11;
   private static final int DYNAMIC_COLOR_COUNT = 10;
   private static final int[] THEME_COLORS = new int[]{
      0,
      GuiPalette.GLYPH_HOVER,
      1,
      GuiPalette.GLYPH_IDLE,
      3,
      0,
      4,
      -14015199,
      5,
      352321535,
      6,
      0,
      7,
      GuiPalette.TILE,
      8,
      GuiPalette.TILE_HOVER,
      9,
      GuiPalette.TILE_HOVER,
      21,
      GuiPalette.TILE,
      22,
      GuiPalette.TILE_HOVER,
      24,
      GuiPalette.TILE,
      25,
      GuiPalette.TILE_HOVER,
      27,
      268435455,
      14,
      0,
      15,
      822083583,
      16,
      1358954495,
      17,
      1895825407,
      30,
      0,
      31,
      0,
      32,
      0,
      53,
      0
   };
   private ClickGuiRenderer.Tab tab = ClickGuiRenderer.Tab.MODULES;
   private final long guiOpenedAt = System.nanoTime();
   private long viewChangedAt = this.guiOpenedAt;
   private float tabIndicatorX = -1.0F;
   private float tabIndicatorW;
   private ClientModule settingsModule;
   private final SettingsTransition settingsTransition = new SettingsTransition();
   /** Module whose settings panel is on screen, including while it animates closed. */
   private ClientModule transitionModule;
   /** Current horizontal offset of the module browser (0 or negative), in pixels. */
   private float panelShiftX;
   /** Un-shifted panel positions captured while the browser is shifted, so dragging state and saved homes are untouched. */
   private final Map<String, float[]> shiftBase = new HashMap<>();
   /** Rect {x, y, w, h} for the module settings card while the transition layout is active. */
   private float[] cardOverride;
   private boolean cardJustOpened;
   private boolean dividerPending;
   private boolean panelGuard;
   private boolean tabBarGuard;
   private boolean cardGuard;
   private boolean cardTextLift;
   private final HashMap<String, float[]> scroll = new HashMap<>();
   private static float scrollDragFrom;
   private static final ImBoolean SCROLLBAR_HOVERED = new ImBoolean();
   private static final ImBoolean SCROLLBAR_HELD = new ImBoolean();
   private ClientModule listeningModule = null;
   private boolean listeningFirstFrame = false;
   private KeySetting listeningKeySetting = null;
   private boolean listeningKeyFirstFrame = false;
   private final ImBoolean colorPickerOpen = new ImBoolean(false);
   private ClientModule activeColorModule = null;
   private ColorSetting activeColorSetting = null;
   private final ImString globalSearch = new ImString(128);
   private boolean focusSearch;
   private final imgui.type.ImBoolean favoritesPicker = new imgui.type.ImBoolean(false);
   private final ImString favoriteSearch = new ImString(128);
   private float searchBodyH;
   private final Map<String, float[]> panelSpots = new HashMap<>();
   private final Map<String, float[]> panelHomes = new HashMap<>();
   private final Map<String, float[]> panelTargets = new HashMap<>();
   private String panelLayoutKey = "";
   private boolean panelHomesDirty;
   private ClickGuiRenderer.CategoryPanel[] panelCache = new ClickGuiRenderer.CategoryPanel[0];
   private int panelCacheRevision = -1;
   private final Map<String, float[]> panelFold = new HashMap<>();
   private final Map<String, Motion.Rows> rowMotion = new HashMap<>();
   private final Int2ObjectOpenHashMap<String> keyLabels = new Int2ObjectOpenHashMap();
   private int cachedRegistryRevision = -1;
   private String cachedGlobalSearch = "";
   private boolean cachedSearchTurkish;
   private List<ClientModule> cachedGlobalResults = List.of();
   private final ImString friendInput = new ImString(32);
   private boolean focusFriendInput;
   private String friendMessage;
   private long friendMessageAt;
   private final ImString profileInput = new ImString(64);
   private boolean focusProfileInput;
   private String configMessage;
   private boolean configMessageError;
   private long configMessageAt;
   private String moduleCodeFor;
   private String moduleCodeMessage;
   private boolean moduleCodeError;
   private long moduleCodeAt;
   private final ProfileList profileList = new ProfileList(this::setConfigMessage);
   private String pendingConfirm;
   private long pendingConfirmAt;
   private String themeMessage;
   private boolean themeMessageError;
   private long themeMessageAt;
   private Surface themeSurface;
   private boolean inspectMode;
   private final List<float[]> panelRects = new ArrayList<>();
   private float[] cardRect;
   private float[] tabBarRect;
   private float[] inspectorRect;
   private Surface hoveredSurface;
   private boolean rowDividers;
   private float rowAboveEmphasis;
   private boolean focusInspector;
   private long previewChangedAt;
   private String hudElementId;
   private boolean focusHudInspector;
   private String hudMessage;
   private boolean hudMessageError;
   private long hudMessageAt;
   private final BlockListEditor blockLists = new BlockListEditor();
   private final ItemPickerEditor itemPickers = new ItemPickerEditor();
   private final ItemPaletteEditor itemColors = new ItemPaletteEditor();

   public void render() {
      float dt = animationDelta(Math.max(GuardedImGui.getIO().getDeltaTime(), 0.001F));
      ThemeStore theme = ThemeStore.get();
      this.panelGuard = HudChrome.needsTextGuard(theme.panels);
      this.tabBarGuard = HudChrome.needsTextGuard(theme.tabBar);
      this.cardGuard = HudChrome.needsTextGuard(theme.cards);
      this.cardTextLift = theme.cards.isImage() && !theme.cards.isBanner();
      applyTheme();
      boolean moduleFontPushed = pushFont(ImGuiManager.getInstance().moduleFont());

      int scopeMark = GuardedImGui.scopeMark();
      try {
         float sw = GuardedImGui.getIO().getDisplaySizeX();
         float sh = GuardedImGui.getIO().getDisplaySizeY();
         float open = easeOut(elapsed(this.guiOpenedAt, 240.0F));
         float view = easeOut(elapsed(this.viewChangedAt, 260.0F));
         float alpha = Math.max(0.01F, open * view);
         GuardedImGui.getBackgroundDrawList().addRectFilled(0.0F, 0.0F, sw, sh, AlbizWidgets.withAlpha(GuiPalette.SHADE, theme.backgroundDim.get() * open));
         Path picked = ThemeImages.get().takePendingImport();
         if (picked != null) {
            this.importHeaderImage(picked);
         }

         this.handleShortcuts();
         boolean editorPage=this.editorPageOpen();
         switch (this.tab) {
            case MODULES:
               SettingsTransition.Config transition = theme.transitionConfig();
               long transitionNow = System.nanoTime();
               this.settingsTransition.setOpen(this.settingsModule != null, transitionNow, transition);
               if (!transition.enabled()) {
                  // Transition off: the original centred card, with the browser hidden behind it.
                  this.panelShiftX = 0.0F;
                  this.transitionModule = null;
                  if (this.settingsModule == null) {
                     this.renderModulePanels(dt, alpha);
                  } else if(!editorPage) {
                     this.renderModuleCard(this.settingsModule, dt, alpha, view);
                  }
                  break;
               }

               if (this.settingsModule != null) {
                  this.transitionModule = this.settingsModule;
               } else if (this.settingsTransition.fullyClosed(transitionNow, transition)) {
                  this.transitionModule = null;
               }

               float eased = this.settingsTransition.progress(transitionNow, transition);
               SettingsTransition.Layout layout = SettingsTransition.layout(transition, eased, sw, sh, AlbizWidgets.s(86.0F), AlbizWidgets.s(16.0F));
               // The browser stays on screen and slides left; a quarter of it remains as a reference.
               this.panelShiftX = this.transitionModule == null ? 0.0F : layout.mainShiftX();
               this.renderModulePanels(dt, Math.max(0.01F, open));
               if (this.transitionModule != null && !editorPage) {
                  this.cardOverride = new float[]{layout.panelX(), layout.panelY(), layout.panelW(), layout.panelH()};
                  try {
                     this.renderModuleCard(this.transitionModule, dt, Math.max(0.01F, open * layout.panelAlpha()), Math.max(0.0F, Math.min(1.0F, eased)));
                  } finally {
                     this.cardOverride = null;
                  }
               }
               break;
            case HUD:
               this.renderHudStudio(dt, alpha, view);
               break;
            case FRIENDS:
               this.renderFriendsCard(dt, alpha, view);
               break;
            case THEME:
               this.renderThemeStudio(dt, alpha, view);
               break;
            case CONFIG:
               this.renderConfigCard(dt, alpha, view);
         }

         this.renderTabBar(dt, open);
         this.savePanelHomes();
         this.renderColorPickerWindow();
         this.blockLists.renderWindows();
         this.itemPickers.renderWindows();
         this.itemColors.render();
         if(!editorPage && !this.editorPageOpen())this.closeCardOnOutsideClick();
      } catch (RuntimeException | LinkageError ex) {
         org.slf4j.LoggerFactory.getLogger("ALBIZ").error("ClickGUI render failed; recovering scopes", ex);
         GuardedImGui.recoverScopes(scopeMark);
         throw ex;
      } finally {
         if (moduleFontPushed) {
            GuardedImGui.popFont();
         }

         removeTheme();
      }
   }

   private boolean editorPageOpen() {
      return this.blockLists.isEditorOpen() || this.itemPickers.isEditorOpen() || this.itemColors.isOpen() || this.colorPickerOpen.get();
   }

   private void handleShortcuts() {
      if (this.tab == ClickGuiRenderer.Tab.MODULES && this.settingsModule == null && GuardedImGui.getIO().getKeyCtrl() && GuardedImGui.isKeyPressed(551)) {
         this.focusSearch = true;
      }
   }

   private void switchTab(ClickGuiRenderer.Tab next) {
      if (next != this.tab) {
         this.closeSettings();
         this.settingsTransition.snapClosed();
         this.transitionModule = null;
         this.panelShiftX = 0.0F;
         this.tab = next;
         this.nativeHudControls=null;this.nativeMenuControls=null;
         this.viewChangedAt = System.nanoTime();
         this.pendingConfirm = null;
         this.profileList.disarm();
         if (next == ClickGuiRenderer.Tab.CONFIG) {
            ConfigProfiles.getInstance().refresh();
         }
      }
   }

   private void renderModulePanels(float dt, float alpha) {
      if(this.favoritesPicker.get()){this.renderFavoritesPanel(dt,alpha);return;}
      ClickGuiRenderer.CategoryPanel[] panels = this.visiblePanels();
      boolean placed = this.panelHomes.containsKey("##panel_search") && this.panelHomes.containsKey("##panel_favorites");

      for (ClickGuiRenderer.CategoryPanel panel : panels) {
         placed &= this.panelHomes.containsKey(panel.id());
      }

      if (!placed) {
         this.placeNewPanels(panels);
      }

      this.fitPanels(panels);

      for (ClickGuiRenderer.CategoryPanel panel : panels) {
         this.renderCategoryPanel(panel, dt, alpha);
      }

      this.renderSearchPanel(dt, alpha);
      this.renderFavoritesPanel(dt,alpha);
   }

   private ClickGuiRenderer.CategoryPanel[] visiblePanels() {
      int revision = ModuleRegistry.revision();
      if (revision != this.panelCacheRevision) {
         List<ClickGuiRenderer.CategoryPanel> panels = new ArrayList<>();

         for (ModuleCategory category : ModuleCategory.values()) {
            if (!this.modulesFor(category).isEmpty()) {
               panels.add(new ClickGuiRenderer.CategoryPanel(category, "##panel_" + category.name(), "##rows_" + category.name()));
            }
         }

         this.panelCache = panels.toArray(ClickGuiRenderer.CategoryPanel[]::new);
         this.panelCacheRevision = revision;
      }

      return this.panelCache;
   }

   private void renderCategoryPanel(ClickGuiRenderer.CategoryPanel panel, float dt, float alpha) {
      List<ClientModule> modules = this.modulesFor(panel.category());
      float listH = Math.min(rowListHeight(modules.size()), maxListHeight());
      float open = this.unfolded(panel, dt);
      if (open < 1.0F) {
         GuardedImGui.setNextWindowSize(AlbizWidgets.s(212.0F), AlbizWidgets.s(42.0F) + listH * open, 1);
      }

      if (this.beginPanel(panel.id(), alpha)) {
         String title = panel.category().displayName();
         this.drawPanelChrome(title, panel.category());
         this.recordPanelRect();
         this.renderFoldControl(panel.category(), open);
         if (open > 0.0F) {
            this.renderRowList(panel.rowsId(), modules, listH, false, dt);
         }
      }

      endPanel();
   }

   private float unfolded(ClickGuiRenderer.CategoryPanel panel, float dt) {
      float target = PanelStateStore.getInstance().isCollapsed(panel.category()) ? 0.0F : 1.0F;
      float[] open = this.panelFold.get(panel.id());
      if (open == null) {
         this.panelFold.put(panel.id(), open = new float[]{target});
      }

      open[0] = smooth(open[0], target, dt, 14.0F);
      if (Math.abs(open[0] - target) < 0.002F) {
         open[0] = target;
      }

      return open[0];
   }

   private void renderFoldControl(ModuleCategory category, float open) {
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY();
      float ww = GuardedImGui.getWindowWidth();
      float headerH = AlbizWidgets.s(42.0F);
      float hit = AlbizWidgets.s(22.0F);
      float cx = wx + ww - AlbizWidgets.s(20.0F);
      float cy = wy + headerH * 0.5F;
      boolean overHeader = !this.inspectMode && GuardedImGui.isWindowHovered() && GuardedImGui.isMouseHoveringRect(wx, wy, wx + ww, wy + headerH);
      GuardedImGui.setCursorScreenPos(cx - hit * 0.5F, cy - hit * 0.5F);
      boolean clicked = GuardedImGui.invisibleButton("##fold", hit, hit) && !this.inspectMode;
      float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), overHeader, 18.0F);
      GuardedImGui.setCursorPos(0.0F, headerH);
      if (clicked || overHeader && GuardedImGui.isMouseClicked(1)) {
         PanelStateStore store = PanelStateStore.getInstance();
         store.setCollapsed(category, !store.isCollapsed(category));
      }

      AlbizWidgets.chevronRotated(
         GuardedImGui.getWindowDrawList(), cx, cy, AlbizWidgets.s(4.0F), AlbizTheme.lerpColor(GuiPalette.GLYPH_IDLE, GuiPalette.GLYPH_HOVER, hover), open * (float) (Math.PI / 2)
      );
   }

   private void renderSearchPanel(float dt, float alpha) {
      if (this.beginPanel("##panel_search", alpha)) {
         this.drawPanelChrome("Search", null);
         this.recordPanelRect();
         float inset = AlbizWidgets.s(6.0F) + AlbizWidgets.s(2.0F);
         GuardedImGui.setCursorPos(inset, AlbizWidgets.s(42.0F) - AlbizWidgets.s(4.0F));
         GuardedImGui.pushStyleVar(11, AlbizWidgets.s(10.0F), AlbizWidgets.s(7.0F));
         GuardedImGui.setNextItemWidth(AlbizWidgets.s(212.0F) - inset * 2.0F);
         if (this.focusSearch) {
            GuardedImGui.setKeyboardFocusHere();
            this.focusSearch = false;
         }

         GuardedImGui.inputTextWithHint("##globalsearch", "Search modules...", this.globalSearch);
         GuardedImGui.popStyleVar();
         GuardedImGui.dummy(0.0F, AlbizWidgets.s(8.0F));
         String query = normalizeSearch(this.globalSearch.get());
         List<ClientModule> results = query.isEmpty() ? List.of() : this.searchModules(query);
         float target = query.isEmpty() ? 0.0F : (results.isEmpty() ? AlbizWidgets.s(34.0F) : Math.min(rowListHeight(results.size()), maxListHeight()));
         this.searchBodyH = smooth(this.searchBodyH, target, dt, 14.0F);
         if (Math.abs(this.searchBodyH - target) < 0.5F) {
            this.searchBodyH = target;
         }

         if (this.searchBodyH >= 1.0F) {
            if (results.isEmpty()) {
               GuardedImGui.beginChild("##search_empty", AlbizWidgets.s(212.0F), this.searchBodyH, false, 24);
               String empty = "No results";
               ImFont font = GuardedImGui.getFont();
               AlbizWidgets.text(
                  GuardedImGui.getWindowDrawList(),
                  font,
                  GuardedImGui.getWindowPosX() + (AlbizWidgets.s(212.0F) - AlbizWidgets.textWidth(font, empty)) * 0.5F,
                  GuardedImGui.getWindowPosY() + AlbizWidgets.s(4.0F),
                  GuiPalette.GLYPH_IDLE,
                  empty,
                  this.panelGuard
               );
               GuardedImGui.endChild();
            } else {
               this.renderRowList("##search_rows", results, this.searchBodyH, true, dt);
            }
         }
      }

      endPanel();
   }

   private void renderFavoritesPanel(float dt,float alpha) {
      var store=PanelStateStore.getInstance();
      if(!this.favoritesPicker.get()) {
      if(this.beginPanel("##panel_favorites",alpha)) {
         this.drawPanelChrome("Favorites",null);this.recordPanelRect();
         GuardedImGui.setCursorPos(AlbizWidgets.s(174),AlbizWidgets.s(7));
         if(GuardedImGui.button("+##favorites",AlbizWidgets.s(28),AlbizWidgets.s(26))) {this.favoritesPicker.set(!this.favoritesPicker.get());net.albiz.client.module.impl.client.SoundFeedbackModule.menuClick();}
         GuardedImGui.setCursorPos(AlbizWidgets.s(8),AlbizWidgets.s(44));
         var selected=ModuleRegistry.all().stream().filter(m->store.isFavorite(m.id())).toList();
         if(selected.isEmpty())GuardedImGui.textUnformatted("Use + to choose modules");
         else this.renderRowList("##favorite_rows",selected,Math.min(rowListHeight(selected.size()),maxListHeight()),true,dt);
      }
      endPanel();
      }
      if(!this.favoritesPicker.get())return;
      GuardedImGui.setNextWindowSize(AlbizWidgets.s(340),AlbizWidgets.s(420),4);
      boolean visible=GuardedImGui.begin("Choose Favorites###favorites_picker",this.favoritesPicker,288);
      try {
         if(visible) {
            GuardedImGui.setNextItemWidth(-1);
            GuardedImGui.inputTextWithHint("##favorite_search","Search modules...",this.favoriteSearch);
            String query=normalizeSearch(this.favoriteSearch.get());
            var rows=query.isEmpty()?ModuleRegistry.all():this.searchModules(query);
            boolean child=GuardedImGui.beginChild("##favorite_choices",0,0,false,0);
            try {
               if(child)for(ClientModule module:rows) {
                  GuardedImGui.pushID(module.id());
                  try {
                     var check=new imgui.type.ImBoolean(store.isFavorite(module.id()));
                     if(GuardedImGui.checkbox(module.name(),check)) {store.setFavorite(module.id(),check.get());net.albiz.client.module.impl.client.SoundFeedbackModule.menuClick();}
                  } finally {GuardedImGui.popID();}
               }
            } finally {GuardedImGui.endChild();}
         }
      } finally {GuardedImGui.end();}
   }

   private boolean beginPanel(String id, float alpha) {
      float[] target = this.panelTargets.remove(id);
      boolean shifting = this.panelShiftX != 0.0F;
      float[] base = this.shiftBase.get(id);
      if (shifting) {
         if (target != null) {
            base = new float[]{target[0], target[1]};
            this.shiftBase.put(id, base);
         } else if (base == null) {
            float[] known = this.panelSpots.get(id);
            if (known != null) {
               base = new float[]{known[0], known[1]};
               this.shiftBase.put(id, base);
            }
         }

         if (base != null) {
            GuardedImGui.setNextWindowPos(base[0] + this.panelShiftX, base[1], 1);
         }
      } else if (target != null) {
         this.shiftBase.remove(id);
         GuardedImGui.setNextWindowPos(target[0], target[1], 1);
      } else if (base != null) {
         // Transition finished: put the panel back exactly where it was.
         this.shiftBase.remove(id);
         GuardedImGui.setNextWindowPos(base[0], base[1], 1);
      }

      GuardedImGui.setNextWindowSizeConstraints(AlbizWidgets.s(212.0F), 0.0F, AlbizWidgets.s(212.0F), Float.MAX_VALUE);
      GuardedImGui.pushStyleVar(0, alpha);
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(14, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, panelRounding());
      // While shifted the browser is a reference only: no mouse/nav input (NoMouseInputs | NoNavInputs | NoNavFocus).
      boolean open = GuardedImGui.begin(id, shifting ? 123 | 786944 : 123);
      if (shifting) {
         return open;
      }

      float x = GuardedImGui.getWindowPosX();
      float y = GuardedImGui.getWindowPosY();
      float[] spot = this.panelSpots.computeIfAbsent(id, key -> new float[]{x, y});
      boolean moved = Math.abs(x - spot[0]) > 0.5F || Math.abs(y - spot[1]) > 0.5F;
      if (target == null && moved && GuardedImGui.isMouseDown(0)) {
         this.panelHomes.put(id, new float[]{x / AlbizWidgets.s(1.0F), y / AlbizWidgets.s(1.0F)});
         this.panelHomesDirty = true;
      }

      spot[0] = x;
      spot[1] = y;
      return open;
   }

   private void savePanelHomes() {
      if (this.panelHomesDirty && !GuardedImGui.isMouseDown(0)) {
         this.panelHomesDirty = false;
         PanelStateStore.getInstance().setPanelHomes(this.panelHomes);
      }
   }

   private static void endPanel() {
      GuardedImGui.end();
      GuardedImGui.popStyleVar(4);
   }

   private void drawPanelChrome(String title, ModuleCategory category) {
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY();
      float ww = GuardedImGui.getWindowWidth();
      float wh = GuardedImGui.getWindowHeight();
      drawShadow(wx, wy, ww, wh, panelRounding(), 4);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      SurfaceBackground.draw(dl, ThemeStore.get().panels, wx, wy, ww, wh, panelRounding());
      ImFont font = ImGuiManager.getInstance().categoryFont();
      float headerH = AlbizWidgets.s(42.0F);
      drawHeaderBand(dl, wx, wy, ww, headerH);
      float r = AlbizWidgets.s(7.0F);
      float cx = wx + AlbizWidgets.s(14.0F) + r;
      float cy = wy + headerH * 0.5F;
      if (category == null) {
         AlbizWidgets.magnifier(dl, cx, cy, r, GuiPalette.GLYPH_IDLE);
      } else {
         drawCategoryIcon(dl, category, cx, cy, r, GuiPalette.GLYPH_IDLE);
      }

      float tx = cx + r + AlbizWidgets.s(9.0F);
      float ty = wy + (headerH - AlbizWidgets.lineHeight(font)) * 0.5F;
      AlbizWidgets.text(dl, font, tx, ty, GuiPalette.GLYPH_HOVER, title, this.panelGuard);
      GuardedImGui.dummy(ww, headerH);
   }

   private static void drawHeaderBand(ImDrawList dl, float x, float y, float w, float h) {
      float rounding = panelRounding();
      float cap = Math.min(h, rounding + AlbizWidgets.s(2.0F));
      int top = AlbizWidgets.c(AlbizWidgets.withAlpha(251658239, 1.0F - cap / (2.0F * h)));
      int below = AlbizWidgets.c(AlbizWidgets.withAlpha(251658239, 1.0F - cap / h));
      int clear = AlbizWidgets.c(16777215);
      dl.addRectFilled(x, y, x + w, y + cap, top, rounding, 48);
      dl.addRectFilledMultiColor(x, y + cap, x + w, y + h, below, below, clear, clear);
   }

   private static void softRule(ImDrawList dl, float x0, float x1, float y, int peak) {
      float mid = (x0 + x1) * 0.5F;
      int solid = AlbizWidgets.c(peak);
      int clear = AlbizWidgets.c(peak & 16777215);
      dl.addRectFilledMultiColor(x0, y, mid, y + 1.0F, clear, solid, solid, clear);
      dl.addRectFilledMultiColor(mid, y, x1, y + 1.0F, solid, clear, clear, solid);
   }

   private static void drawCategoryIcon(ImDrawList dl, ModuleCategory category, float cx, float cy, float r, int color) {
      switch (category.icon()) {
         case CROSSHAIR:
            AlbizWidgets.crosshair(dl, cx, cy, r, color);
            break;
         case CHEVRON:
            AlbizWidgets.chevron(dl, cx, cy, r, color, true);
            break;
         case MOVE:
            AlbizWidgets.move(dl, cx, cy, r, color);
            break;
         case GRID:
            AlbizWidgets.grid(dl, cx, cy, r, color);
            break;
         case RING:
            AlbizWidgets.ring(dl, cx, cy, r, color);
            break;
         case EYE:
            AlbizWidgets.eye(dl, cx, cy, r, color);
            break;
         case MONITOR:
            AlbizWidgets.monitor(dl, cx, cy, r, color);
            break;
         case HOUSE:
            AlbizWidgets.house(dl, cx, cy, r, color);
            break;
         case PAPYRUS:
         case SWORD:
            AlbizWidgets.sword(dl, cx, cy, r, color);
            break;
         case WOLF:
            AlbizWidgets.wolf(dl, cx, cy, r, color);
            break;
         case CRESCENT:
            AlbizWidgets.crescent(dl, cx, cy, r, color);
      }
   }

   public static void drawShadow(float x, float y, float w, float h, float rounding, int layers) {
      ImDrawList bg = GuardedImGui.getBackgroundDrawList();

      for (int i = 1; i <= layers; i++) {
         float e = AlbizWidgets.s(2.5F) * i;
         bg.addRectFilled(x - e, y - e + AlbizWidgets.s(4.0F), x + w + e, y + h + e + AlbizWidgets.s(4.0F), AlbizWidgets.c(335544320), rounding + e);
      }
   }

   private static float rowListHeight(int rows) {
      return rows * AlbizWidgets.s(32.0F) - AlbizWidgets.s(2.0F) + AlbizWidgets.s(6.0F);
   }

   private static float maxListHeight() {
      return Math.max(AlbizWidgets.s(120.0F), GuardedImGui.getIO().getDisplaySizeY() * 0.62F - AlbizWidgets.s(42.0F));
   }

   private void renderRowList(String id, List<ClientModule> modules, float height, boolean showCategory, float dt) {
      if (GuardedImGui.beginChild(id, AlbizWidgets.s(212.0F), Math.max(1.0F, height), false, 24)) {
         Motion.Rows motion = this.rowMotion.computeIfAbsent(id, key -> new Motion.Rows());
         motion.bind(modules);
         this.rowDividers = ThemeStore.get().moduleDividers.get();

         for (int i = 0; i < modules.size(); i++) {
            this.renderModuleRow(modules.get(i), showCategory, motion, i, dt);
         }

         GuardedImGui.dummy(1.0F, AlbizWidgets.s(6.0F) - AlbizWidgets.s(2.0F));
         this.applySmoothScroll(id, dt);
      }

      GuardedImGui.endChild();
   }

   private void renderModuleRow(ClientModule module, boolean showCategory, Motion.Rows motion, int row, float dt) {
      GuardedImGui.pushID(module.id());
      float inset = AlbizWidgets.s(6.0F);
      float rowH = AlbizWidgets.s(30.0F);
      float localY = GuardedImGui.getCursorPosY();
      GuardedImGui.setCursorPosX(inset);
      float x0 = GuardedImGui.getCursorScreenPosX();
      float y0 = GuardedImGui.getCursorScreenPosY();
      float w = GuardedImGui.getWindowWidth() - inset * 2.0F;
      if (GuardedImGui.invisibleButton("##row", w, rowH) && !this.inspectMode) {
         if (module.isAction()) {
            module.onClick();
         } else {
            module.toggle();
         }
      }

      boolean hovered = GuardedImGui.isItemHovered();
      if (!this.inspectMode && GuardedImGui.isItemClicked(1)) {
         net.albiz.client.module.impl.client.SoundFeedbackModule.menuClick();
         this.openSettings(module);
      }

      if (!this.inspectMode && GuardedImGui.isItemHovered(65536)) {
         String description = module.description();
         showTooltip(description != null && !description.isBlank() ? description + "\n" + net.albiz.client.i18n.AlbizI18n.display("Right-click for settings") : "Right-click for settings");
      }

      float hover = motion.hover(row, hovered, dt, 18.0F);
      float on = motion.on(row, module.isEnabled(), dt, 14.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      float emphasis = Math.max(hover, on);
      if (this.rowDividers && row > 0) {
         float fade = 1.0F - Math.max(emphasis, this.rowAboveEmphasis);
         if (fade > 0.01F) {
            float y = (float)Math.floor(y0 - AlbizWidgets.s(2.0F) * 0.5F);
            softRule(dl, x0 + AlbizWidgets.s(8.0F), x0 + w - AlbizWidgets.s(8.0F), y, AlbizWidgets.withAlpha(620756991, fade));
         }
      }

      this.rowAboveEmphasis = emphasis;
      float r = AlbizWidgets.s(6.0F);
      if (hover > 0.01F) {
         dl.addRectFilled(x0, y0, x0 + w, y0 + rowH, AlbizWidgets.c(AlbizWidgets.withAlpha(318767103, hover)), r);
      }

      if (on > 0.01F) {
         dl.addRectFilled(
            x0,
            y0,
            x0 + w,
            y0 + rowH,
            AlbizWidgets.c(AlbizWidgets.withAlpha(AlbizTheme.lerpColor(GuiPalette.accent(), GuiPalette.accentHover(), hover * 0.3F), on)),
            r
         );
      }

      ImFont font = GuardedImGui.getFont();
      ImFont small = ImGuiManager.getInstance().smallFont();
      String trailing = showCategory ? module.category().displayName() : (module.keybind() == -1 ? null : this.keyLabel(module.keybind()));
      float tw = trailing == null ? 0.0F : AlbizWidgets.textWidth(small, trailing);
      int textColor = AlbizTheme.lerpColor(AlbizTheme.lerpColor(GuiPalette.LABEL_IDLE, GuiPalette.GLYPH_HOVER, hover), -1, on);
      float room = w - AlbizWidgets.s(trailing == null && !module.isAction() ? 20.0F : 28.0F) - tw;
      String name = AlbizWidgets.ellipsize(font, module.name(), room);
      AlbizWidgets.text(dl, font, x0 + AlbizWidgets.s(10.0F), y0 + (rowH - AlbizWidgets.lineHeight(font)) * 0.5F, textColor, name, this.panelGuard);
      if (trailing != null) {
         int trailColor = AlbizTheme.lerpColor(GuiPalette.GLYPH_IDLE, GuiPalette.TRAIL_ACTIVE, on);
         AlbizWidgets.text(
            dl, small, x0 + w - AlbizWidgets.s(10.0F) - tw, y0 + (rowH - AlbizWidgets.lineHeight(small)) * 0.5F, trailColor, trailing, this.panelGuard
         );
      } else if (module.isAction()) {
         AlbizWidgets.chevronRight(
            dl, x0 + w - AlbizWidgets.s(14.0F), y0 + rowH * 0.5F, AlbizWidgets.s(4.0F), AlbizTheme.lerpColor(GuiPalette.GLYPH_IDLE, GuiPalette.GLYPH_HOVER, hover)
         );
      }

      GuardedImGui.setCursorPos(0.0F, localY + rowH + AlbizWidgets.s(2.0F));
      GuardedImGui.popID();
   }

   public static void showTooltip(String text) {
      GuardedImGui.pushStyleVar(2, AlbizWidgets.s(10.0F), AlbizWidgets.s(8.0F));
      GuardedImGui.beginTooltip();
      GuardedImGui.pushTextWrapPos(AlbizWidgets.s(280.0F));
      GuardedImGui.textUnformatted(text);
      GuardedImGui.popTextWrapPos();
      GuardedImGui.endTooltip();
      GuardedImGui.popStyleVar();
   }

   private void applySmoothScroll(String key, float dt) {
      smoothScroll(this.scroll.computeIfAbsent(key, k -> new float[2]), dt);
   }

   public static void smoothScroll(float[] state, float dt) {
      float max = GuardedImGui.getScrollMaxY();
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY();
      float ww = GuardedImGui.getWindowWidth();
      float wh = GuardedImGui.getWindowHeight();
      float barH = Math.max(AlbizWidgets.s(24.0F), wh * wh / (wh + max));
      float travel = Math.max(1.0F, wh - barH);
      boolean held = false;
      boolean hot = false;
      if (max > 0.5F) {
         int id = GuardedImGui.getID("##scrollbar");
         imgui.internal.ImGui.keepAliveID(id);
         imgui.internal.ImGui.buttonBehavior(wx + ww - AlbizWidgets.s(8.0F), wy, wx + ww, wy + wh, id, SCROLLBAR_HOVERED, SCROLLBAR_HELD);
         held = SCROLLBAR_HELD.get();
         hot = held || SCROLLBAR_HOVERED.get();
         if (held && GuardedImGui.isMouseClicked(0)) {
            float barY = wy + travel * (state[0] / max);
            float mouseY = GuardedImGui.getMousePosY();
            state[1] = mouseY >= barY && mouseY <= barY + barH ? state[0] : (mouseY - wy - barH * 0.5F) / travel * max;
            scrollDragFrom = state[1];
         } else if (held) {
            state[1] = scrollDragFrom + GuardedImGui.getMouseDragDeltaY(0, 0.0F) * max / travel;
         }
      }

      if (!held && GuardedImGui.isWindowHovered()) {
         float wheel = GuardedImGui.getIO().getMouseWheel();
         if (wheel != 0.0F) {
            state[1] -= wheel * AlbizWidgets.s(56.0F);
         }
      }

      state[1] = Math.max(0.0F, Math.min(max, state[1]));
      state[0] = held ? state[1] : smooth(state[0], state[1], dt, 16.0F);
      if (Math.abs(state[0] - state[1]) < 0.3F) {
         state[0] = state[1];
      }

      GuardedImGui.setScrollY(state[0]);
      if (max > 0.5F) {
         float barY = wy + travel * (state[0] / max);
         GuardedImGui.getWindowDrawList()
            .addRectFilled(
               wx + ww - (hot ? AlbizWidgets.s(6.0F) : AlbizWidgets.s(4.0F)),
               barY,
               wx + ww - AlbizWidgets.s(1.5F),
               barY + barH,
               AlbizWidgets.c(hot ? 1728053247 : 872415231),
               AlbizWidgets.s(2.0F)
            );
      }
   }

   private ClickGuiRenderer.PanelLayout panelLayout(ClickGuiRenderer.CategoryPanel[] panels) {
      List<String> ids = new ArrayList<>();
      List<String> keys = new ArrayList<>();
      List<Float> heights = new ArrayList<>();
      PanelStateStore folds = PanelStateStore.getInstance();

      for (ClickGuiRenderer.CategoryPanel panel : panels) {
         ids.add(panel.id());
         keys.add(panel.category().name());
         heights.add(
            folds.isCollapsed(panel.category())
               ? AlbizWidgets.s(42.0F)
               : AlbizWidgets.s(42.0F) + Math.min(rowListHeight(this.modulesFor(panel.category()).size()), maxListHeight())
         );
      }

      ids.add("##panel_search");
      keys.add("search");
      heights.add(AlbizWidgets.s(42.0F) + GuardedImGui.getFontSize() + AlbizWidgets.s(18.0F) + maxListHeight());
      ids.add("##panel_favorites");keys.add("favorites");
      heights.add(AlbizWidgets.s(78)+maxListHeight());
      return new ClickGuiRenderer.PanelLayout(ids, keys, heights);
   }

   private void placeNewPanels(ClickGuiRenderer.CategoryPanel[] panels) {
      ClickGuiRenderer.PanelLayout layout = this.panelLayout(panels);
      List<String> ids = layout.ids();
      List<String> keys = layout.keys();
      List<Float> heights = layout.heights();
      float scale = AlbizWidgets.s(1.0F);
      Map<String, float[]> stored = PanelStateStore.getInstance().panelHomes();
      Map<String, float[]> legacy = stored.isEmpty() ? savedPanelSpots() : Map.of();
      List<float[]> taken = new ArrayList<>();

      for (int i = 0; i < ids.size(); i++) {
         String id = ids.get(i);
         if (!this.panelHomes.containsKey(id)) {
            float[] home = stored.get(id);
            float[] old = legacy.get(id);
            if (home == null && old != null && coveredArea(old[0], old[1], heights.get(i), taken) == 0.0F) {
               home = new float[]{old[0] / scale, old[1] / scale};
               this.panelHomesDirty = true;
            }

            if (home != null) {
               this.panelHomes.put(id, home);
            }
         }

         float[] homex = this.panelHomes.get(id);
         if (homex != null) {
            taken.add(new float[]{homex[0] * scale, homex[1] * scale, AlbizWidgets.s(212.0F), heights.get(i)});
         }
      }

      boolean firstRun = taken.isEmpty();

      for (int i = 0; i < ids.size(); i++) {
         if (!this.panelHomes.containsKey(ids.get(i))) {
            float h = heights.get(i);
            float[] spot = bundledSpot(keys.get(i));
            if (spot == null && firstRun) {
               spot = panelPosFor(i, ids.size());
            }

            if (spot == null || coveredArea(spot[0], spot[1], h, taken) > 0.0F) {
               spot = freeSpot(h, taken);
            }

            this.panelHomes.put(ids.get(i), new float[]{spot[0] / scale, spot[1] / scale});
            taken.add(new float[]{spot[0], spot[1], AlbizWidgets.s(212.0F), h});
            this.panelHomesDirty = true;
         }
      }

      this.panelLayoutKey = "";
   }

   private void fitPanels(ClickGuiRenderer.CategoryPanel[] panels) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float scale = AlbizWidgets.s(1.0F);
      String key = sw + "x" + sh + "@" + scale;
      if (!key.equals(this.panelLayoutKey)) {
         this.panelLayoutKey = key;
         ClickGuiRenderer.PanelLayout layout = this.panelLayout(panels);
         float w = AlbizWidgets.s(212.0F);
         float lowest = sh - AlbizWidgets.s(70.0F) - AlbizWidgets.s(42.0F);
         List<float[]> taken = new ArrayList<>();
         List<Integer> misfits = new ArrayList<>();

         for (int i = 0; i < layout.ids().size(); i++) {
            String id = layout.ids().get(i);
            float[] home = this.panelHomes.get(id);
            float x = home[0] * scale;
            float y = home[1] * scale;
            if (x >= 0.0F && y >= 0.0F && x + w <= sw && y <= lowest) {
               this.panelTargets.put(id, new float[]{x, y});
               taken.add(new float[]{x, y, w, layout.heights().get(i)});
            } else {
               misfits.add(i);
            }
         }

         for (int ix : misfits) {
            float h = layout.heights().get(ix);
            float[] spot = freeSpot(h, taken);
            this.panelTargets.put(layout.ids().get(ix), spot);
            taken.add(new float[]{spot[0], spot[1], w, h});
         }
      }
   }

   private static float[] bundledSpot(String key) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float[] spot = BundledDefaults.panelSpot(key, sw, sh);
      if (spot == null) {
         return null;
      } else {
         spot[0] = Math.max(0.0F, Math.min(spot[0], sw - AlbizWidgets.s(212.0F)));
         spot[1] = Math.max(0.0F, Math.min(spot[1], sh - AlbizWidgets.s(42.0F)));
         return spot;
      }
   }

   private static float[] panelPosFor(int index, int panelCount) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float w = AlbizWidgets.s(212.0F);
      float gap = AlbizWidgets.s(14.0F);
      int columns = Math.max(1, (int)((sw - AlbizWidgets.s(40.0F) + gap) / (w + gap)));
      int inRow = Math.min(columns, panelCount);
      float rowW = inRow * (w + gap) - gap;
      float startX = Math.max(AlbizWidgets.s(20.0F), (sw - rowW) * 0.5F);
      return new float[]{startX + index % columns * (w + gap), AlbizWidgets.s(28.0F) + index / columns * sh * 0.45F};
   }

   private static float[] freeSpot(float h, List<float[]> taken) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float w = AlbizWidgets.s(212.0F);
      float gap = AlbizWidgets.s(14.0F);
      float margin = AlbizWidgets.s(20.0F);
      float bottom = sh - AlbizWidgets.s(70.0F) - gap;
      float highest = Float.MAX_VALUE;

      for (float[] rect : taken) {
         highest = Math.min(highest, rect[1]);
      }

      float[] rightMost = null;

      for (float[] rect : taken) {
         boolean topRow = rect[1] < highest + AlbizWidgets.s(42.0F);
         if (topRow && (rightMost == null || rect[0] + rect[2] > rightMost[0] + rightMost[2])) {
            rightMost = rect;
         }
      }

      float[] beside = rightMost == null ? new float[]{margin, AlbizWidgets.s(28.0F)} : new float[]{rightMost[0] + rightMost[2] + gap, rightMost[1]};
      List<float[]> scan = new ArrayList<>();
      scan.add(new float[]{margin, AlbizWidgets.s(28.0F)});

      for (float[] rectx : taken) {
         scan.add(new float[]{rectx[0] + rectx[2] + gap, rectx[1]});
         scan.add(new float[]{rectx[0], rectx[1] + rectx[3] + gap});
      }

      scan.sort(Comparator.<float[]>comparingDouble(spotx -> spotx[1]).thenComparingDouble(spotx -> spotx[0]));
      scan.addFirst(beside);

      for (float[] spot : scan) {
         boolean onScreen = spot[0] >= 0.0F && spot[1] >= 0.0F && spot[0] + w <= sw - margin && spot[1] + h <= bottom;
         if (onScreen && coveredArea(spot[0], spot[1], h, taken) == 0.0F) {
            return spot;
         }
      }

      float[] best = null;
      float bestArea = Float.MAX_VALUE;

      for (float[] spotx : scan) {
         float x = Math.max(0.0F, Math.min(spotx[0], sw - w - margin));
         float y = Math.max(0.0F, Math.min(spotx[1], sh - h - margin));
         float area = coveredArea(x, y, h, taken);
         if (area < bestArea) {
            best = new float[]{x, y};
            bestArea = area;
         }
      }

      return best;
   }

   private static float coveredArea(float x, float y, float h, List<float[]> taken) {
      float w = AlbizWidgets.s(212.0F);
      float m = AlbizWidgets.s(14.0F) * 0.5F;
      float area = 0.0F;

      for (float[] r : taken) {
         float overlapW = Math.min(x + w, r[0] + r[2]) + m - Math.max(x, r[0]);
         float overlapH = Math.min(y + h, r[1] + r[3]) + m - Math.max(y, r[1]);
         if (overlapW > 0.0F && overlapH > 0.0F) {
            area += overlapW * overlapH;
         }
      }

      return area;
   }

   private static Map<String, float[]> savedPanelSpots() {
      Map<String, float[]> spots = new HashMap<>();
      String ini = GuardedImGui.getIO().getIniFilename();
      if (ini != null && Files.isRegularFile(Path.of(ini))) {
         String section = "[Window][";

         try {
            String window = null;

            for (String line : Files.readAllLines(Path.of(ini))) {
               if (line.startsWith("[")) {
                  boolean panel = line.startsWith(section + "##panel_") && line.endsWith("]");
                  window = panel ? line.substring(section.length(), line.length() - 1) : null;
               } else if (window != null && line.startsWith("Pos=")) {
                  String[] xy = line.substring(4).split(",");
                  spots.put(window, new float[]{Float.parseFloat(xy[0]), Float.parseFloat(xy[1])});
               }
            }
         } catch (RuntimeException | IOException var7) {
            spots.clear();
         }

         return spots;
      } else {
         return spots;
      }
   }

   private List<ClientModule> modulesFor(ModuleCategory category) {
      return ModuleRegistry.inCategory(category);
   }

   private List<ClientModule> searchModules(String query) {
      int revision = ModuleRegistry.revision();
      if (revision != this.cachedRegistryRevision || this.cachedSearchTurkish != net.albiz.client.i18n.AlbizI18n.isTurkish()) {
         this.cachedRegistryRevision = revision;
         this.cachedSearchTurkish = net.albiz.client.i18n.AlbizI18n.isTurkish();
         this.cachedGlobalSearch = "";
         this.cachedGlobalResults = List.of();
      }

      if (query.equals(this.cachedGlobalSearch)) {
         return this.cachedGlobalResults;
      } else {
         List<ClientModule> matches = new ArrayList<>();

         for (ClientModule module : ModuleRegistry.all()) {
            if (normalizeSearch(module.name()).contains(query) || normalizeSearch(net.albiz.client.i18n.AlbizI18n.display(module.name())).contains(query)) {
               matches.add(module);
            }
         }

         this.cachedGlobalSearch = query;
         this.cachedGlobalResults = List.copyOf(matches);
         return this.cachedGlobalResults;
      }
   }

   private static String normalizeSearch(String value) {
      return net.albiz.client.i18n.SearchText.normalize(value);
   }

   private void renderTabBar(float dt, float open) {
      ClickGuiRenderer.Tab[] tabs = ClickGuiRenderer.Tab.values();
      ImFont font = GuardedImGui.getFont();
      float h = AlbizWidgets.s(44.0F);
      float inner = AlbizWidgets.s(4.0F);
      float padX = AlbizWidgets.s(20.0F);
      float[] widths = new float[tabs.length];
      float total = inner * 2.0F;

      for (int i = 0; i < tabs.length; i++) {
         widths[i] = AlbizWidgets.textWidth(font, tabs[i].label) + padX * 2.0F;
         total += widths[i];
      }

      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float x = (sw - total) * 0.5F;
      float y = sh - h - AlbizWidgets.s(26.0F) + AlbizWidgets.s(16.0F) * (1.0F - open);
      GuardedImGui.setNextWindowPos(x, y, 1);
      GuardedImGui.setNextWindowSize(total, h, 1);
      GuardedImGui.pushStyleVar(0, Math.max(0.01F, open));
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, h * 0.5F);
      GuardedImGui.pushStyleColor(2, GuiPalette.tabBarBg());
      if (GuardedImGui.begin("##tabbar", 319)) {
         this.tabBarRect = new float[]{x, y, total, h};
         if (this.tab == ClickGuiRenderer.Tab.THEME) {
            this.outlineSurface(ThemeStore.get().tabBar, x, y, total, h, h * 0.5F);
         }

         drawShadow(x, y, total, h, h * 0.5F, 4);
         ImDrawList dl = GuardedImGui.getWindowDrawList();
         SurfaceBackground.draw(dl, ThemeStore.get().tabBar, x, y, total, h, h * 0.5F);
         dl.addRect(x, y, x + total, y + h, AlbizWidgets.c(318767103), h * 0.5F, 0, 1.0F);
         float targetX = inner;

         for (int i = 0; i < this.tab.ordinal(); i++) {
            targetX += widths[i];
         }

         float targetW = widths[this.tab.ordinal()];
         if (this.tabIndicatorX < 0.0F) {
            this.tabIndicatorX = targetX;
            this.tabIndicatorW = targetW;
         }

         this.tabIndicatorX = smooth(this.tabIndicatorX, targetX, dt, 18.0F);
         this.tabIndicatorW = smooth(this.tabIndicatorW, targetW, dt, 18.0F);
         float pillH = h - inner * 2.0F;
         dl.addRectFilled(
            x + this.tabIndicatorX,
            y + inner,
            x + this.tabIndicatorX + this.tabIndicatorW,
            y + inner + pillH,
            AlbizWidgets.c(GuiPalette.accent()),
            pillH * 0.5F
         );
         float tx = inner;

         for (int i = 0; i < tabs.length; i++) {
            GuardedImGui.setCursorPos(tx, inner);
            if (GuardedImGui.invisibleButton("##tab_" + tabs[i].name(), widths[i], pillH)) {
               this.switchTab(tabs[i]);
            }

            float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), GuardedImGui.isItemHovered(), 18.0F);
            float centre = x + tx + widths[i] * 0.5F;
            float cover = Math.max(0.0F, 1.0F - Math.abs(centre - (x + this.tabIndicatorX + this.tabIndicatorW * 0.5F)) / Math.max(1.0F, widths[i] * 0.5F));
            int color = AlbizTheme.lerpColor(AlbizTheme.lerpColor(GuiPalette.GLYPH_IDLE, GuiPalette.GLYPH_HOVER, hover), -1, cover);
            float tw = AlbizWidgets.textWidth(font, tabs[i].label);
            AlbizWidgets.text(
               dl, font, x + tx + (widths[i] - tw) * 0.5F, y + (h - AlbizWidgets.lineHeight(font)) * 0.5F, color, tabs[i].label, this.tabBarGuard
            );
            tx += widths[i];
         }
      }

      GuardedImGui.end();
      GuardedImGui.popStyleColor();
      GuardedImGui.popStyleVar(3);
   }

   private float[] beginCard(String id, float alpha, float view) {
      return this.beginCard(id, alpha, view, GuardedImGui.getIO().getDisplaySizeX() * 0.5F);
   }

   private float[] beginCard(String id, float alpha, float view, float centreX) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float reserved = AlbizWidgets.s(86.0F);
      float w = Math.min(sw - AlbizWidgets.s(32.0F), AlbizWidgets.s(460.0F));
      float h = Math.min(sh - reserved - AlbizWidgets.s(32.0F), AlbizWidgets.s(760.0F));
      float x = Math.max(AlbizWidgets.s(16.0F), Math.min(sw-w-AlbizWidgets.s(16),centreX - w * 0.5F));
      float y = Math.max(AlbizWidgets.s(16.0F), (sh - reserved - h) * 0.5F + AlbizWidgets.s(8.0F)) + AlbizWidgets.s(24.0F) * (1.0F - view);
      if (this.cardOverride != null && "##module_card".equals(id)) {
         x = this.cardOverride[0];
         y = this.cardOverride[1];
         w = this.cardOverride[2];
         h = this.cardOverride[3];
      }

      GuardedImGui.setNextWindowPos(x, y, 1);
      GuardedImGui.setNextWindowSize(w, h, 1);
      GuardedImGui.pushStyleVar(0, alpha);
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(14, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, cardRounding());
      GuardedImGui.pushStyleColor(2, GuiPalette.cardBg());
      if (this.cardTextLift) {
         GuardedImGui.pushStyleColor(0, -1);
         GuardedImGui.pushStyleColor(1, GuiPalette.LABEL_IDLE);
      }

      GuardedImGui.begin(id, 8511);
      drawShadow(x, y, w, h, cardRounding(), 6);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      Surface cards = ThemeStore.get().cards;
      SurfaceBackground.draw(dl, cards, x, y, w, h, cardRounding());
      dl.addRect(x, y, x + w, y + h, AlbizWidgets.c(318767103), cardRounding(), 0, 1.0F);
      float top = SurfaceBackground.hasImageBanner(cards)
         ? Math.max(AlbizWidgets.s(22.0F), SurfaceBackground.bannerHeight(cards) - AlbizWidgets.s(44.0F))
         : AlbizWidgets.s(22.0F);
      return new float[]{w, h, top, x, y};
   }

   private static float panelRounding() {
      return AlbizWidgets.s(GuiPalette.rounding());
   }

   public static float cardRounding() {
      return AlbizWidgets.s(Math.min(28.0F, GuiPalette.rounding() * 2.2F));
   }

   private void endCard() {
      GuardedImGui.end();
      GuardedImGui.popStyleColor(this.cardTextLift ? 3 : 1);
      GuardedImGui.popStyleVar(4);
   }

   public static void drawCardTitle(String title, float x, float centreY) {
      drawCardTitle(title, x, centreY, false);
   }

   private static void drawCardTitle(String title, float x, float centreY, boolean guard) {
      ImFont font = ImGuiManager.getInstance().titleFont();
      AlbizWidgets.text(
         GuardedImGui.getWindowDrawList(),
         font,
         GuardedImGui.getWindowPosX() + x,
         GuardedImGui.getWindowPosY() + centreY - AlbizWidgets.lineHeight(font) * 0.5F,
         GuiPalette.GLYPH_HOVER,
         title,
         guard
      );
   }

   public static float mutedParagraph(String text, float x, float y, float wrapX) {
      GuardedImGui.setCursorPos(x, y);
      GuardedImGui.pushTextWrapPos(wrapX);
      GuardedImGui.pushStyleColor(0, GuardedImGui.getStyleColorVec4(1));
      GuardedImGui.textUnformatted(text);
      GuardedImGui.popStyleColor();
      GuardedImGui.popTextWrapPos();
      return GuardedImGui.getCursorPosY();
   }

   public static void cardDivider(float pad, float w, float localY) {
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY();
      GuardedImGui.getWindowDrawList().addLine(wx + pad, wy + localY, wx + w - pad, wy + localY, AlbizWidgets.c(268435455), 1.0F);
   }

   private void renderModuleCard(ClientModule module, float dt, float alpha, float view) {
      this.renderModuleCard(module, dt, alpha, view, false);
   }

   private void renderModuleCard(ClientModule module, float dt, float alpha, float view, boolean preview) {
      this.renderModuleCard(module, dt, alpha, view, preview, !preview);
   }

   private void renderModuleCard(ClientModule module, float dt, float alpha, float view, boolean preview, boolean showBack) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float[] size;
      if (preview) {
         size = this.beginCard("##preview_card", alpha, view, (sw - AlbizWidgets.s(408.0F)) * 0.5F);
         this.cardRect = new float[]{size[3], size[4], size[0], size[1]};
         this.outlineSurface(ThemeStore.get().cards, size[3], size[4], size[0], size[1], cardRounding());
         GuardedImGui.pushStyleVar(1, 1.0F);
         GuardedImGui.beginDisabled();
      } else {
         GuardedImGui.getBackgroundDrawList().addRectFilled(0.0F, 0.0F, sw, sh, AlbizWidgets.withAlpha(1426063360, view));
         size = this.beginCard("##module_card", alpha, view);
      }

      float w = size[0];
      float h = size[1];
      float pad = AlbizWidgets.s(24.0F);
      float top = size[2];
      float btn = AlbizWidgets.s(32.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY();
      float titleX = pad;
      if (!preview && showBack) {
         GuardedImGui.setCursorPos(pad - AlbizWidgets.s(4.0F), top);
         if (AlbizWidgets.iconButton("##back", btn)) {
            this.closeSettings();
         }

         AlbizWidgets.chevronLeft(
            dl, wx + pad - AlbizWidgets.s(4.0F) + btn * 0.5F - AlbizWidgets.s(1.0F), wy + top + btn * 0.5F, AlbizWidgets.s(5.0F), GuiPalette.GLYPH_HOVER
         );
         titleX = pad + btn + AlbizWidgets.s(8.0F);
      }

      drawCardTitle(module.name(), titleX, top + btn * 0.5F, this.cardGuard);
      if (module.isAction()) {
         float openW = AlbizWidgets.s(72.0F);
         float openH = AlbizWidgets.s(30.0F);
         GuardedImGui.setCursorPos(w - pad - openW, top + (btn - openH) * 0.5F);
         if (AlbizWidgets.chipButton("##open", "Open", openW, openH, true)) {
            module.onClick();
         }
      } else {
         float swW = AlbizWidgets.s(46.0F);
         float swH = AlbizWidgets.s(26.0F);
         GuardedImGui.setCursorPos(w - pad - swW, top + (btn - swH) * 0.5F);
         float swX = GuardedImGui.getCursorScreenPosX();
         float swY = GuardedImGui.getCursorScreenPosY();
         if (GuardedImGui.invisibleButton("##enabled", swW, swH)) {
            module.toggle();
         }

         AlbizWidgets.drawSwitch(dl, swX, swY, swW, swH, AlbizWidgets.animate(GuardedImGui.getItemID(), module.isEnabled(), 14.0F));
      }

      float y = top + btn + AlbizWidgets.s(12.0F);
      String description = module.description();
      if (description != null && !description.isBlank()) {
         y = mutedParagraph(description, pad, y, w - pad) + AlbizWidgets.s(4.0F);
      }

      y += AlbizWidgets.s(12.0F);
      cardDivider(pad, w, y);
      GuardedImGui.setCursorPos(0.0F, y + 1.0F);
      if (GuardedImGui.beginChild("##body", w, Math.max(1.0F, h - y - 1.0F - AlbizWidgets.s(10.0F)), false, 24)) {
         float contentW = w - pad * 2.0F;
         float labelY = GuardedImGui.getCursorPosY() + AlbizWidgets.s(16.0F);
         this.sectionLabel("Settings", pad);
         if (!preview && module.hasModifiedSettings()) {
            this.renderResetAllChip(module, pad, contentW, labelY);
         }

         boolean any = false;

         for (Setting setting : module.settings()) {
            if (setting.isVisible() && (!module.isAction() || setting != module.showInHud())) {
               any = true;
               GuardedImGui.pushID(setting.id());
               this.renderSetting(module, setting, pad, contentW);
               GuardedImGui.popID();
            }
         }

         if (!any) {
            mutedParagraph("This module has no settings.", pad, GuardedImGui.getCursorPosY() + AlbizWidgets.s(8.0F), w - pad);
         }

         this.sectionLabel("Keybind", pad);
         GuardedImGui.pushID("##keybind_row");
         this.renderKeybindRow(module, pad, contentW);
         GuardedImGui.popID();
         if (isHudModule(module)) {
            this.renderHudActions(pad, contentW);
         }

         if (module.inConfigs()) {
            this.sectionLabel("Config", pad);
            this.renderModuleConfigActions(module, pad, contentW);
         }

         GuardedImGui.dummy(1.0F, AlbizWidgets.s(18.0F));
         this.applySmoothScroll("card_" + module.id(), dt);
      }

      GuardedImGui.endChild();
      if (preview) {
         GuardedImGui.endDisabled();
         GuardedImGui.popStyleVar();
      }

      this.endCard();
   }

   private void renderResetAllChip(ClientModule module, float pad, float contentW, float labelY) {
      float afterY = GuardedImGui.getCursorPosY();
      String key = "reset:" + module.id();
      String label = this.isConfirming(key) ? "Confirm reset" : "Reset all";
      ImFont small = ImGuiManager.getInstance().smallFont();
      boolean pushed = pushFont(small);
      float chipW = AlbizWidgets.textWidth(small, label) + AlbizWidgets.s(18.0F);
      float chipH = AlbizWidgets.lineHeight(small) + AlbizWidgets.s(8.0F);
      GuardedImGui.setCursorPos(pad + contentW - chipW, labelY + (GuardedImGui.getTextLineHeight() - chipH) * 0.5F);
      if (AlbizWidgets.chipButton("##reset_all", label, chipW, chipH, this.isConfirming(key)) && this.confirmClick(key)) {
         module.resetSettings();
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Restore every setting to its default");
      }

      if (pushed) {
         GuardedImGui.popFont();
      }

      GuardedImGui.setCursorPosY(afterY);
   }

   private boolean confirmClick(String key) {
      if (this.isConfirming(key)) {
         this.pendingConfirm = null;
         return true;
      } else {
         this.pendingConfirm = key;
         this.pendingConfirmAt = System.nanoTime();
         return false;
      }
   }

   private boolean isConfirming(String key) {
      return key.equals(this.pendingConfirm) && System.nanoTime() - this.pendingConfirmAt < 3000000000L;
   }

   private void sectionLabel(String text, float pad) {
      GuardedImGui.setCursorPos(pad, GuardedImGui.getCursorPosY() + AlbizWidgets.s(16.0F));
      boolean pushed = pushFont(ImGuiManager.getInstance().smallFont());
      GuardedImGui.pushStyleColor(0, GuardedImGui.getStyleColorVec4(1));
      GuardedImGui.textUnformatted(text);
      GuardedImGui.popStyleColor();
      if (pushed) {
         GuardedImGui.popFont();
      }

      GuardedImGui.setCursorPosY(GuardedImGui.getCursorPosY() + AlbizWidgets.s(4.0F));
      this.dividerPending = false;
   }

   private float rowStart(float pad, float contentW) {
      float localY = GuardedImGui.getCursorPosY();
      if (this.dividerPending) {
         float wx = GuardedImGui.getWindowPosX();
         float sy = GuardedImGui.getCursorScreenPosY();
         GuardedImGui.getWindowDrawList().addLine(wx + pad, sy, wx + pad + contentW, sy, AlbizWidgets.c(268435455), 1.0F);
      }

      this.dividerPending = true;
      return localY;
   }

   public static void rowLabel(String label, float pad, float startY, float rowH) {
      GuardedImGui.setCursorPos(pad, startY + (rowH - GuardedImGui.getTextLineHeight()) * 0.5F);
      GuardedImGui.textUnformatted(label);
   }

   public void inspect(ClientModule module){openSettings(module);}
   private void openSettings(ClientModule module) {
      this.settingsModule = module;
      this.viewChangedAt = System.nanoTime();
      this.cardJustOpened = true;
   }

   private void closeSettings() {
      if (this.settingsModule != null) {
         this.viewChangedAt = System.nanoTime();
      }

      this.settingsModule = null;
      this.listeningModule = null;
      this.listeningKeySetting = null;
      this.activeColorModule = null;
      this.activeColorSetting = null;
      this.colorPickerOpen.set(false);
      this.blockLists.close();
      this.itemPickers.close();
      this.itemColors.close();
      this.favoritesPicker.set(false);
   }

   private void closeCardOnOutsideClick() {
      if (this.cardJustOpened) {
         this.cardJustOpened = false;
      } else if (this.settingsModule != null && this.listeningModule == null && this.listeningKeySetting == null) {
         if (GuardedImGui.isMouseClicked(0) && !GuardedImGui.isWindowHovered(4) && !GuardedImGui.isAnyItemActive()) {
            this.closeSettings();
         }
      }
   }

   private void renderFriendsCard(float dt, float alpha, float view) {
      float[] size = this.beginCard("##friends_card", alpha, view);
      float w = size[0];
      float h = size[1];
      float pad = AlbizWidgets.s(24.0F);
      float top = size[2];
      float btn = AlbizWidgets.s(32.0F);
      FriendStore store = FriendStore.getInstance();
      List<String> friends = store.all();
      drawCardTitle("Friends", pad, top + btn * 0.5F, this.cardGuard);
      String count = friends.size() == 1 ? "1 player" : friends.size() + " players";
      ImFont small = ImGuiManager.getInstance().smallFont();
      AlbizWidgets.text(
         GuardedImGui.getWindowDrawList(),
         small,
         GuardedImGui.getWindowPosX() + w - pad - AlbizWidgets.textWidth(small, count),
         GuardedImGui.getWindowPosY() + top + (btn - AlbizWidgets.lineHeight(small)) * 0.5F,
         GuiPalette.GLYPH_IDLE,
         count,
         this.cardGuard
      );
      float y = mutedParagraph("Add players by their exact username.", pad, top + btn + AlbizWidgets.s(12.0F), w - pad) + AlbizWidgets.s(14.0F);
      float addW = AlbizWidgets.s(84.0F);
      float inputH = GuardedImGui.getTextLineHeight() + AlbizWidgets.s(16.0F);
      GuardedImGui.setCursorPos(pad, y);
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(12.0F), AlbizWidgets.s(8.0F));
      GuardedImGui.setNextItemWidth(w - pad * 2.0F - addW - AlbizWidgets.s(8.0F));
      if (this.focusFriendInput) {
         GuardedImGui.setKeyboardFocusHere();
         this.focusFriendInput = false;
      }

      GuardedImGui.inputTextWithHint("##friend_input", "Username", this.friendInput, 16);
      boolean submitted = AlbizWidgets.submittedWithEnter();
      GuardedImGui.popStyleVar();
      GuardedImGui.setCursorPos(w - pad - addW, y);
      if (AlbizWidgets.chipButton("##friend_add", "Add", addW, inputH, true)) {
         submitted = true;
      }

      if (submitted) {
         this.tryAddFriend(store);
      }

      y += inputH + AlbizWidgets.s(8.0F);
      if (this.friendMessage != null && elapsed(this.friendMessageAt, 2500.0F) < 1.0F) {
         GuardedImGui.setCursorPos(pad, y);
         boolean pushed = pushFont(small);
         GuardedImGui.pushStyleColor(0, GuiPalette.DANGER);
         GuardedImGui.textUnformatted(this.friendMessage);
         GuardedImGui.popStyleColor();
         if (pushed) {
            GuardedImGui.popFont();
         }
      }

      y += AlbizWidgets.lineHeight(small) + AlbizWidgets.s(10.0F);
      cardDivider(pad, w, y);
      GuardedImGui.setCursorPos(0.0F, y + 1.0F);
      if (GuardedImGui.beginChild("##friend_list", w, Math.max(1.0F, h - y - 1.0F - AlbizWidgets.s(10.0F)), false, 24)) {
         if (friends.isEmpty()) {
            String empty = "No friends yet";
            GuardedImGui.setCursorPos((w - GuardedImGui.calcTextSize(empty).x) * 0.5F, AlbizWidgets.s(40.0F));
            GuardedImGui.textDisabled(empty);
         } else {
            GuardedImGui.dummy(1.0F, AlbizWidgets.s(8.0F));

            for (String name : friends) {
               this.renderFriendRow(store, name, pad, w - pad * 2.0F);
            }
         }

         GuardedImGui.dummy(1.0F, AlbizWidgets.s(12.0F));
         this.applySmoothScroll("friends", dt);
      }

      GuardedImGui.endChild();
      this.endCard();
   }

   private void renderFriendRow(FriendStore store, String name, float pad, float contentW) {
      GuardedImGui.pushID("friend_" + name);
      float rowH = AlbizWidgets.s(48.0F);
      float localY = GuardedImGui.getCursorPosY();
      GuardedImGui.setCursorPos(pad, localY);
      float x0 = GuardedImGui.getCursorScreenPosX();
      float y0 = GuardedImGui.getCursorScreenPosY();
      GuardedImGui.setNextItemAllowOverlap();
      GuardedImGui.invisibleButton("##row", contentW, rowH);
      boolean rowHovered = GuardedImGui.isMouseHoveringRect(x0, y0, x0 + contentW, y0 + rowH);
      float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), rowHovered, 18.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      if (hover > 0.01F) {
         dl.addRectFilled(
            x0 - AlbizWidgets.s(8.0F),
            y0,
            x0 + contentW + AlbizWidgets.s(8.0F),
            y0 + rowH,
            AlbizWidgets.c(AlbizWidgets.withAlpha(218103807, hover)),
            AlbizWidgets.s(10.0F)
         );
      }

      float avatarR = AlbizWidgets.s(15.0F);
      float acx = x0 + avatarR;
      float acy = y0 + rowH * 0.5F;
      int avatarColor = AVATAR_COLORS[Math.floorMod(name.toLowerCase(Locale.ROOT).hashCode(), AVATAR_COLORS.length)];
      dl.addCircleFilled(acx, acy, avatarR, AlbizWidgets.c(avatarColor), 32);
      ImFont category = ImGuiManager.getInstance().categoryFont();
      String initial = name.substring(0, 1).toUpperCase(Locale.ROOT);
      AlbizWidgets.text(dl, category, acx - AlbizWidgets.textWidth(category, initial) * 0.5F, acy - AlbizWidgets.lineHeight(category) * 0.5F, -1, initial);
      ImFont font = GuardedImGui.getFont();
      AlbizWidgets.text(dl, font, x0 + avatarR * 2.0F + AlbizWidgets.s(12.0F), y0 + (rowH - AlbizWidgets.lineHeight(font)) * 0.5F, GuiPalette.GLYPH_HOVER, name);
      float removeSize = AlbizWidgets.s(28.0F);
      GuardedImGui.setCursorPos(pad + contentW - removeSize, localY + (rowH - removeSize) * 0.5F);
      float rx = GuardedImGui.getCursorScreenPosX();
      float ry = GuardedImGui.getCursorScreenPosY();
      if (GuardedImGui.invisibleButton("##remove", removeSize, removeSize)) {
         store.remove(name);
      }

      float removeHover = AlbizWidgets.animate(GuardedImGui.getItemID(), GuardedImGui.isItemHovered(), 18.0F);
      float rcx = rx + removeSize * 0.5F;
      float rcy = ry + removeSize * 0.5F;
      if (removeHover > 0.01F) {
         dl.addCircleFilled(rcx, rcy, removeSize * 0.5F, AlbizWidgets.c(AlbizWidgets.withAlpha(GuiPalette.DANGER, 0.18F * removeHover)), 32);
      }

      AlbizWidgets.cross(
         dl, rcx, rcy, AlbizWidgets.s(4.5F), AlbizTheme.lerpColor(AlbizWidgets.withAlpha(GuiPalette.GLYPH_IDLE, 0.25F + 0.75F * hover), GuiPalette.DANGER, removeHover)
      );
      GuardedImGui.setCursorPos(0.0F, localY + rowH + AlbizWidgets.s(4.0F));
      GuardedImGui.popID();
   }

   private void tryAddFriend(FriendStore store) {
      String name = this.friendInput.get().trim();
      if (!FriendStore.isValidName(name)) {
         this.friendMessage = "Use 1-16 letters, digits or _ (Bedrock names start with .)";
      } else if (store.isFriend(name)) {
         this.friendMessage = name + " is already on your list";
      } else {
         store.add(name);
         this.friendInput.set("");
         this.friendMessage = null;
      }

      this.friendMessageAt = System.nanoTime();
      this.focusFriendInput = true;
   }

   private void renderConfigCard(float dt, float alpha, float view) {
      float[] size = this.beginCard("##config_card", alpha, view);
      float w = size[0];
      float h = size[1];
      float pad = AlbizWidgets.s(24.0F);
      float top = size[2];
      float btn = AlbizWidgets.s(32.0F);
      float contentW = w - pad * 2.0F;
      ModuleConfigStore store = ModuleConfigStore.getInstance();
      ConfigProfiles profiles = ConfigProfiles.getInstance();
      ImFont small = ImGuiManager.getInstance().smallFont();
      drawCardTitle("Config", pad, top + btn * 0.5F, this.cardGuard);
      String status = store.isReadOnly() ? "Read-only" : (store.hasUnsavedChanges() ? "Saving..." : "Saved");
      int statusColor = store.isReadOnly() ? GuiPalette.DANGER : GuiPalette.GLYPH_IDLE;
      AlbizWidgets.text(
         GuardedImGui.getWindowDrawList(),
         small,
         GuardedImGui.getWindowPosX() + w - pad - AlbizWidgets.textWidth(small, status),
         GuardedImGui.getWindowPosY() + top + (btn - AlbizWidgets.lineHeight(small)) * 0.5F,
         statusColor,
         status,
         this.cardGuard
      );
      if (store.isReadOnly()
         && GuardedImGui.isMouseHoveringRect(
            GuardedImGui.getWindowPosX() + w - pad - AlbizWidgets.s(80.0F),
            GuardedImGui.getWindowPosY() + top,
            GuardedImGui.getWindowPosX() + w - pad,
            GuardedImGui.getWindowPosY() + top + btn
         )) {
         showTooltip("Client config could not be read. Fix or restore its backup.");
      }

      float y = mutedParagraph("Changes save automatically. Profiles can be shared with a code.", pad, top + btn + AlbizWidgets.s(12.0F), w - pad)
         + AlbizWidgets.s(14.0F);
      float gap = AlbizWidgets.s(8.0F);
      float chipH = GuardedImGui.getTextLineHeight() + AlbizWidgets.s(16.0F);
      float chipW = (contentW - gap * 2.0F) / 3.0F;
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##cfg_save", "Save all", chipW, chipH, true)) {
         this.saveAllConfigs(store);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Save everything now");
      }

      GuardedImGui.setCursorPos(pad + chipW + gap, y);
      if (AlbizWidgets.chipButton("##cfg_folder", "Open folder", chipW, chipH, false)) {
         this.openProfilesFolder(profiles);
      }

      GuardedImGui.setCursorPos(pad + (chipW + gap) * 2.0F, y);
      boolean resetArmed = this.isConfirming("reset-all-modules");
      if (AlbizWidgets.chipButton("##cfg_reset", resetArmed ? "Confirm" : "Reset all", chipW, chipH, resetArmed) && this.confirmClick("reset-all-modules")) {
         for (ClientModule module : ModuleRegistry.all()) {
            module.resetSettings();
         }

         this.setConfigMessage("Every module setting is back to default", false);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Reset all module settings. Keybinds stay.");
      }

      y += chipH + AlbizWidgets.s(18.0F);
      cardDivider(pad, w, y);
      y += AlbizWidgets.s(14.0F);
      float createW = AlbizWidgets.s(84.0F);
      GuardedImGui.setCursorPos(pad, y);
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(12.0F), AlbizWidgets.s(8.0F));
      GuardedImGui.setNextItemWidth(contentW - createW - gap);
      if (this.focusProfileInput) {
         GuardedImGui.setKeyboardFocusHere();
         this.focusProfileInput = false;
      }

      GuardedImGui.inputTextWithHint("##profile_input", "New profile name", this.profileInput);
      boolean submitted = AlbizWidgets.submittedWithEnter();
      GuardedImGui.popStyleVar();
      GuardedImGui.setCursorPos(w - pad - createW, y);
      if (AlbizWidgets.chipButton("##profile_create", "Create", createW, chipH, true)) {
         submitted = true;
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Save your current modules as a new profile");
      }

      if (submitted) {
         this.tryCreateProfile(profiles);
      }

      y += chipH + AlbizWidgets.s(8.0F);
      float halfW = (contentW - gap) * 0.5F;
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##profile_import", "Import file", halfW, chipH, false)) {
         this.importProfileFile();
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Import a profile .json file. You can also drop it here.");
      }

      GuardedImGui.setCursorPos(pad + halfW + gap, y);
      if (AlbizWidgets.chipButton("##profile_paste", "Paste code", halfW, chipH, false)) {
         this.pasteProfileCode();
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Load a share code from your clipboard. Your current settings go to Auto backup.");
      }

      y += chipH + AlbizWidgets.s(8.0F);
      if (this.configMessage != null && elapsed(this.configMessageAt, 2500.0F) < 1.0F) {
         GuardedImGui.setCursorPos(pad, y);
         boolean pushed = pushFont(small);
         GuardedImGui.pushStyleColor(0, this.configMessageError ? GuiPalette.DANGER : GuiPalette.GLYPH_IDLE);
         GuardedImGui.textUnformatted(this.configMessage);
         GuardedImGui.popStyleColor();
         if (pushed) {
            GuardedImGui.popFont();
         }
      }

      y += AlbizWidgets.lineHeight(small) + AlbizWidgets.s(10.0F);
      cardDivider(pad, w, y);
      List<ConfigProfiles.Profile> list = profiles.list();
      GuardedImGui.setCursorPos(0.0F, y + 1.0F);
      if (GuardedImGui.beginChild("##profile_list", w, Math.max(1.0F, h - y - 1.0F - AlbizWidgets.s(10.0F)), false, 24)) {
         if (list.isEmpty()) {
            String empty = "No profiles yet";
            GuardedImGui.setCursorPos((w - GuardedImGui.calcTextSize(empty).x) * 0.5F, AlbizWidgets.s(40.0F));
            GuardedImGui.textDisabled(empty);
         } else {
            GuardedImGui.dummy(1.0F, AlbizWidgets.s(8.0F));
            this.profileList.render(profiles, list, store.activeProfile(), pad, contentW);
         }

         GuardedImGui.dummy(1.0F, AlbizWidgets.s(12.0F));
         float target = this.profileList.takeScrollTarget();
         if (!Float.isNaN(target)) {
            this.scroll.computeIfAbsent("profiles", k -> new float[2])[1] = target;
         }

         this.applySmoothScroll("profiles", dt);
      }

      GuardedImGui.endChild();
      this.endCard();
   }

   private void tryCreateProfile(ConfigProfiles profiles) {
      try {
         String name = profiles.create(this.profileInput.get());
         this.profileInput.set("");
         this.profileList.open(name);
         this.setConfigMessage("Created " + name + " from the current modules", false);
      } catch (ProfileException var3) {
         this.setConfigMessage(var3.getMessage(), true);
      }

      this.focusProfileInput = true;
   }

   private void importProfileFile() {
      if (FileDialogs.busy()) {
         this.setConfigMessage("A file window is already open", true);
      } else {
         FileDialogs.openProfile("Import an Albiz profile", this::importProfile);
      }
   }

   private void importProfile(Path path) {
      try {
         String name = ConfigProfiles.getInstance().importFile(path);
         this.profileList.open(name);
         this.setConfigMessage("Imported " + name + " - press Load to use it", false);
      } catch (ProfileException var3) {
         this.setConfigMessage(var3.getMessage(), true);
      }
   }

   private void pasteProfileCode() {
      ConfigProfiles profiles = ConfigProfiles.getInstance();

      try {
         String name = profiles.importCode(class_310.method_1551().field_1774.method_1460());
         ConfigProfiles.Profile imported = profiles.find(name);
         profiles.backup(imported == null ? Set.of(ConfigProfiles.Part.MODULES) : imported.parts());
         profiles.load(name);
         this.profileList.open(name);
         this.setConfigMessage("Loaded " + name + " - your previous settings are in Auto backup", false);
      } catch (ProfileException var4) {
         this.setConfigMessage(var4.getMessage(), true);
      }
   }

   private void saveAllConfigs(ModuleConfigStore store) {
      store.save();
      ThemeStore.get().save();
      HudTheme.get().save();
      HudLayoutStore.getInstance().save();
      FriendStore.getInstance().save();
      if (store.isReadOnly()) {
         this.setConfigMessage("Client config is unreadable, so it was left untouched", true);
      } else {
         this.setConfigMessage("Everything saved", false);
      }
   }

   private void openProfilesFolder(ConfigProfiles profiles) {
      try {
         Path dir = profiles.directory();
         Files.createDirectories(dir);
         class_156.method_668().method_60932(dir);
      } catch (RuntimeException | IOException var3) {
         this.setConfigMessage("Could not open the profiles folder", true);
      }
   }

   private void setConfigMessage(String message, boolean error) {
      this.configMessage = message;
      this.configMessageError = error;
      this.configMessageAt = System.nanoTime();
   }

   private void renderThemeStudio(float dt, float alpha, float view) {
      ThemeStore theme = ThemeStore.get();
      float preview = easeOut(elapsed(this.previewChangedAt, 260.0F));
      this.panelRects.clear();
      this.cardRect = null;
      this.inspectMode = true;
      if (this.themeSurface == theme.cards) {
         ClientModule sample = previewModule();
         if (sample != null) {
            this.renderModuleCard(sample, dt, alpha * preview, Math.min(view, preview), true);
         }
      } else {
         this.renderModulePanels(dt, alpha * preview);
      }

      this.inspectMode = false;
      this.renderInspector(theme, dt, alpha);
      this.highlightSurfaces(theme);
   }

   private void renderInspector(ThemeStore theme, float dt, float alpha) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float w = Math.min(AlbizWidgets.s(360.0F), sw-AlbizWidgets.s(24));
      float h = Math.min(sh - AlbizWidgets.s(86.0F) - AlbizWidgets.s(40.0F), AlbizWidgets.s(760.0F));
      GuardedImGui.setNextWindowPos(Math.max(AlbizWidgets.s(12),sw - w - AlbizWidgets.s(24)), AlbizWidgets.s(24), 1);
      GuardedImGui.setNextWindowSize(w, h, 1);
      if (this.focusInspector) {
         GuardedImGui.setNextWindowFocus();
         this.focusInspector = false;
      }

      GuardedImGui.pushStyleVar(0, alpha);
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(14, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, cardRounding());
      GuardedImGui.pushStyleColor(2, GuiPalette.abgr(theme.cards.color.argbWithAlpha(255)));
      int flags = 59;
      if (GuardedImGui.begin("##theme_inspector", flags)) {
         float x = GuardedImGui.getWindowPosX();
         float y = GuardedImGui.getWindowPosY();
         this.inspectorRect = new float[]{x, y, w, h};
         drawShadow(x, y, w, h, cardRounding(), 6);
         GuardedImGui.getWindowDrawList().addRect(x, y, x + w, y + h, AlbizWidgets.c(318767103), cardRounding(), 0, 1.0F);
         float pad = AlbizWidgets.s(20.0F);
         float top = AlbizWidgets.s(20.0F);
         drawCardTitle("Theme", pad, top + AlbizWidgets.s(14.0F));
         float ty = top + AlbizWidgets.s(34.0F);
         if (this.themeMessage != null && System.nanoTime() - this.themeMessageAt < 3000000000L) {
            GuardedImGui.setCursorPos(pad, ty);
            GuardedImGui.pushTextWrapPos(w - pad);
            GuardedImGui.pushStyleColor(0, this.themeMessageError ? GuiPalette.DANGER : GuiPalette.GLYPH_HOVER);
            GuardedImGui.textUnformatted(this.themeMessage);
            GuardedImGui.popStyleColor();
            GuardedImGui.popTextWrapPos();
            ty = GuardedImGui.getCursorPosY();
         } else {
            ty = mutedParagraph("Click a panel or card on screen to edit it.", pad, ty, w - pad);
         }

         ty = this.renderSurfaceTabs(theme, pad, ty + AlbizWidgets.s(12.0F), w - pad * 2.0F) + AlbizWidgets.s(14.0F);
         cardDivider(pad, w, ty);
         GuardedImGui.setCursorPos(0.0F, ty + 1.0F);
         if (GuardedImGui.beginChild("##inspector_body", w, Math.max(1.0F, h - ty - 1.0F - AlbizWidgets.s(10.0F)), false, 24)) {
            float contentW = w - pad * 2.0F;
            if (this.themeSurface == null) {
               this.renderGlobalOptions(theme, pad, contentW);
            } else {
               this.renderSurfaceOptions(this.themeSurface, pad, contentW);
            }

            GuardedImGui.dummy(1.0F, AlbizWidgets.s(18.0F));
            this.applySmoothScroll("inspector", dt);
         }

         GuardedImGui.endChild();
      }

      GuardedImGui.end();
      GuardedImGui.popStyleColor();
      GuardedImGui.popStyleVar(4);
   }

   private float renderSurfaceTabs(ThemeStore theme, float pad, float y, float contentW) {
      String[] labels = new String[]{"Global", "Panels", "Cards", "Tab bar"};
      Surface[] targets = new Surface[]{null, theme.panels, theme.cards, theme.tabBar};
      int selected = 0;

      for (int i = 1; i < targets.length; i++) {
         if (targets[i] == this.themeSurface) {
            selected = i;
         }
      }

      float h = AlbizWidgets.s(32.0F);
      GuardedImGui.setCursorPos(pad, y);
      int clicked = AlbizWidgets.segmented("##surface", labels, selected, contentW, h);
      if (clicked >= 0) {
         this.selectSurface(targets[clicked]);
      }

      return y + h;
   }

   private void renderGlobalOptions(ThemeStore theme, float pad, float contentW) {
      this.sectionLabel("Presets", pad);
      this.renderPresetRow(theme, pad, contentW);
      this.sectionLabel("Colors", pad);
      this.settingRows(pad, contentW, theme.accent);
      this.sectionLabel("Layout", pad);
      this.settingRows(pad, contentW, theme.rounding, theme.moduleDividers, theme.backgroundDim, theme.menuBlur);
      this.sectionLabel("Interface", pad);
      this.settingRows(pad, contentW, theme.menuStyle, theme.uiScale, theme.font, theme.animationSpeed, theme.animateLines, theme.customMainMenu, theme.communityPopup);
      this.sectionLabel("Settings Panel Animation", pad);
      this.settingRows(
         pad, contentW, theme.settingsTransition, theme.transitionDuration, theme.transitionEasing, theme.transitionDistance, theme.transitionOpacity,
         theme.transitionScale, theme.transitionDirection
      );
      this.renderNativeMenuOptions(pad,contentW);
      this.sectionLabel("Share", pad);
      this.renderThemeActions(theme, pad, contentW);
   }

   private net.albiz.client.port.soberui.ReferenceVisualModule nativeMenuControls;
   private void renderNativeMenuOptions(float pad,float contentW){
      var menu=net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.MainMenu.class);if(menu==null)return;
      if(nativeMenuControls==null)nativeMenuControls=new net.albiz.client.port.soberui.ReferenceVisualModule("menu-preferences",menu);
      this.sectionLabel("Main Menu",pad);GuardedImGui.pushID("native-menu-preferences");
      var selection=new imgui.type.ImInt(menu.style.get().ordinal());String[] labels=net.albiz.client.i18n.AlbizI18n.isTurkish()?new String[]{"Albız","Vanilla+","Ev","Ada","Klasik"}:new String[]{"Albız","Vanilla+","Home","Island","Classic"};
      GuardedImGui.setCursorPosX(pad);if(GuardedImGui.combo("Menu Style",selection,labels)){menu.style.set(net.albiz.client.port.soberui.modules.MainMenu.Style.values()[selection.get()]);net.albiz.client.port.soberui.gui.MenuScreen.resumeCustom();net.albiz.client.port.soberui.SoberUi.save();}
      this.settingRows(pad,contentW,nativeMenuControls.settings().stream().filter(setting->!setting.name().equals("Menu Style")).toArray(Setting[]::new));GuardedImGui.popID();
   }

   private void renderSurfaceOptions(Surface surface, float pad, float contentW) {
      this.sectionLabel("Fill", pad);
      this.settingRows(pad, contentW, surface.color, surface.opacity);
      this.sectionLabel("Background", pad);
      this.settingRows(pad, contentW, surface.background);
      if (surface.isImage()) {
         this.renderImageGallery(surface, pad, contentW);
      }

      this.settingRows(
         pad,
         contentW,
         surface.mapping,
         surface.coverage,
         surface.headerHeight,
         surface.imageOpacity,
         surface.imageScale,
         surface.imageX,
         surface.imageY,
         surface.lineOpacity
      );
      if (surface.isScreenMapped()) {
         mutedParagraph(
            "Screen mapping pins the background to the screen, so each surface is a window onto it. Drag a panel to see another part.",
            pad,
            GuardedImGui.getCursorPosY() + AlbizWidgets.s(12.0F),
            pad + contentW
         );
      }
   }

   private void selectSurface(Surface surface) {
      if (surface != this.themeSurface) {
         ThemeStore theme = ThemeStore.get();
         if (surface == theme.cards != (this.themeSurface == theme.cards)) {
            this.previewChangedAt = System.nanoTime();
         }

         this.themeSurface = surface;
         this.focusInspector = true;
      }
   }

   private void highlightSurfaces(ThemeStore theme) {
      float mx = GuardedImGui.getIO().getMousePosX();
      float my = GuardedImGui.getIO().getMousePosY();
      boolean blocked = contains(this.inspectorRect, mx, my) || GuardedImGui.isPopupOpen("", 3072);
      Surface hovered = null;
      if (!blocked) {
         for (float[] rect : this.panelRects) {
            if (contains(rect, mx, my)) {
               hovered = theme.panels;
            }
         }

         if (contains(this.cardRect, mx, my)) {
            hovered = theme.cards;
         }

         if (contains(this.tabBarRect, mx, my)) {
            hovered = theme.tabBar;
         }
      }

      this.hoveredSurface = hovered;
      if (hovered != null) {
         ImDrawList fg = GuardedImGui.getForegroundDrawList();
         boolean tabBar = hovered == theme.tabBar;
         String hint = hovered.label + (tabBar ? "  ·  right-click to edit" : "  ·  click to edit");
         ImFont small = ImGuiManager.getInstance().smallFont();
         float chipW = AlbizWidgets.textWidth(small, hint) + AlbizWidgets.s(18.0F);
         float chipH = AlbizWidgets.lineHeight(small) + AlbizWidgets.s(10.0F);
         float cx = Math.min(mx + AlbizWidgets.s(14.0F), GuardedImGui.getIO().getDisplaySizeX() - chipW - AlbizWidgets.s(4.0F));
         float cy = Math.min(my + AlbizWidgets.s(18.0F), GuardedImGui.getIO().getDisplaySizeY() - chipH - AlbizWidgets.s(4.0F));
         fg.addRectFilled(cx, cy, cx + chipW, cy + chipH, GuiPalette.accent(), chipH * 0.5F);
         AlbizWidgets.text(fg, small, cx + AlbizWidgets.s(9.0F), cy + AlbizWidgets.s(5.0F), -1, hint);
         boolean clicked = tabBar ? GuardedImGui.isMouseClicked(1) : GuardedImGui.isMouseClicked(0) || GuardedImGui.isMouseClicked(1);
         if (clicked) {
            this.selectSurface(hovered);
         }
      }
   }

   private void outlineSurface(Surface surface, float x, float y, float w, float h, float rounding) {
      int color;
      if (surface == this.themeSurface) {
         color = AlbizWidgets.withAlpha(GuiPalette.accent(), 0.8F);
      } else {
         if (surface != this.hoveredSurface) {
            return;
         }

         color = -2130706433;
      }

      float e = AlbizWidgets.s(3.0F);
      float reach = e + AlbizWidgets.s(2.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      dl.pushClipRect(x - reach, y - reach, x + w + reach, y + h + reach, false);
      dl.addRect(x - e, y - e, x + w + e, y + h + e, color, rounding + e, 0, AlbizWidgets.s(1.5F));
      dl.popClipRect();
   }

   private void recordPanelRect() {
      if (this.inspectMode) {
         float x = GuardedImGui.getWindowPosX();
         float y = GuardedImGui.getWindowPosY();
         float w = GuardedImGui.getWindowWidth();
         float h = GuardedImGui.getWindowHeight();
         this.panelRects.add(new float[]{x, y, w, h});
         this.outlineSurface(ThemeStore.get().panels, x, y, w, h, panelRounding());
      }
   }

   private static boolean contains(float[] rect, float x, float y) {
      return rect != null && x >= rect[0] && x <= rect[0] + rect[2] && y >= rect[1] && y <= rect[1] + rect[3];
   }

   private static ClientModule previewModule() {
      ClientModule fallback = null;

      for (ClientModule module : ModuleRegistry.all()) {
         if (fallback == null) {
            fallback = module;
         }

         if (module.settings().size() >= 3) {
            return module;
         }
      }

      return fallback;
   }

   private void settingRows(float pad, float contentW, Setting... settings) {
      for (Setting setting : settings) {
         if (setting.isVisible()) {
            GuardedImGui.pushID(setting.id());
            this.renderSetting(null, setting, pad, contentW);
            GuardedImGui.popID();
         }
      }
   }

   private void renderPresetRow(ThemeStore theme, float pad, float contentW) {
      float rowH = AlbizWidgets.s(46.0F) + AlbizWidgets.s(6.0F);
      float startY = this.rowStart(pad, contentW);
      float size = AlbizWidgets.s(28.0F);
      float gap = AlbizWidgets.s(12.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();

      for (int i = 0; i < ThemeStore.PRESETS.size(); i++) {
         ThemeStore.Preset preset = ThemeStore.PRESETS.get(i);
         GuardedImGui.setCursorPos(pad + i * (size + gap), startY + (rowH - size) * 0.5F);
         float cx = GuardedImGui.getCursorScreenPosX() + size * 0.5F;
         float cy = GuardedImGui.getCursorScreenPosY() + size * 0.5F;
         if (GuardedImGui.invisibleButton("##preset" + i, size, size)) {
            theme.applyPreset(preset);
         }

         boolean hovered = GuardedImGui.isItemHovered();
         float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), hovered, 18.0F);
         boolean selected = theme.matchesPreset(preset);
         dl.addCircleFilled(cx, cy, size * 0.5F - AlbizWidgets.s(3.0F), AlbizWidgets.c(GuiPalette.abgr(preset.accent())), 32);
         if (selected) {
            dl.addCircle(cx, cy, size * 0.5F, AlbizWidgets.c(-1), 32, AlbizWidgets.s(1.5F));
         } else if (hover > 0.01F) {
            dl.addCircle(cx, cy, size * 0.5F, AlbizWidgets.c(AlbizWidgets.withAlpha(-1, 0.35F * hover)), 32, AlbizWidgets.s(1.2F));
         }

         if (hovered) {
            showTooltip(preset.name());
         }
      }

      GuardedImGui.setCursorPos(0.0F, startY + rowH);
   }

   private void renderImageGallery(Surface surface, float pad, float contentW) {
      List<String> files = ThemeImages.get().files();
      int columns = 3;
      float gap = AlbizWidgets.s(8.0F);
      float r = AlbizWidgets.s(8.0F);
      float tileW = (contentW - gap * (columns - 1)) / columns;
      float tileH = tileW * 0.6F;
      float startY = GuardedImGui.getCursorPosY() + AlbizWidgets.s(4.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      ImFont small = ImGuiManager.getInstance().smallFont();
      String deleted = null;
      int count = files.size() + 1;

      for (int i = 0; i < count; i++) {
         GuardedImGui.setCursorPos(pad + i % columns * (tileW + gap), startY + i / columns * (tileH + gap));
         float sx = GuardedImGui.getCursorScreenPosX();
         float sy = GuardedImGui.getCursorScreenPosY();
         String name = i == 0 ? null : files.get(i - 1);
         String deleteKey = name == null ? null : "image-delete:" + name;
         boolean armed = deleteKey != null && this.isConfirming(deleteKey);
         float del = AlbizWidgets.s(20.0F);
         float dx = sx + tileW - del - AlbizWidgets.s(5.0F);
         float dy = sy + AlbizWidgets.s(5.0F);
         boolean overTile = GuardedImGui.isWindowHovered(128) && GuardedImGui.isMouseHoveringRect(sx, sy, sx + tileW, sy + tileH);
         boolean deleteShown = deleteKey != null && (armed || overTile);
         boolean deleteHovered = false;
         float deleteHover = 0.0F;
         if (deleteShown) {
            GuardedImGui.setCursorScreenPos(dx, dy);
            if (GuardedImGui.invisibleButton("##tiledel" + i, del, del) && this.confirmClick(deleteKey)) {
               deleted = name;
            }

            deleteHovered = GuardedImGui.isItemHovered();
            deleteHover = AlbizWidgets.animate(GuardedImGui.getItemID(), deleteHovered, 18.0F);
            armed = this.isConfirming(deleteKey);
            if (deleteHovered) {
               showTooltip(armed ? "Click again to delete" : "Delete image");
            }

            GuardedImGui.setCursorScreenPos(sx, sy);
         }

         boolean clicked = GuardedImGui.invisibleButton("##tile" + i, tileW, tileH);
         if (!PerformanceModule.optimizationsEnabled() || GuardedImGui.isItemVisible()) {
            boolean hovered = GuardedImGui.isItemHovered() || deleteHovered;
            float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), hovered, 18.0F);
            if (i == 0) {
               dl.addRectFilled(sx, sy, sx + tileW, sy + tileH, AlbizWidgets.c(AlbizTheme.lerpColor(GuiPalette.TILE, GuiPalette.TILE_HOVER, hover)), r);
               int glyph = AlbizTheme.lerpColor(GuiPalette.GLYPH_IDLE, GuiPalette.GLYPH_HOVER, hover);
               float cx = sx + tileW * 0.5F;
               float cy = sy + tileH * 0.5F - AlbizWidgets.s(8.0F);
               dl.addLine(cx - AlbizWidgets.s(7.0F), cy, cx + AlbizWidgets.s(7.0F), cy, AlbizWidgets.c(glyph), AlbizWidgets.s(1.8F));
               dl.addLine(cx, cy - AlbizWidgets.s(7.0F), cx, cy + AlbizWidgets.s(7.0F), AlbizWidgets.c(glyph), AlbizWidgets.s(1.8F));
               String label = "Add image";
               AlbizWidgets.text(dl, small, cx - AlbizWidgets.textWidth(small, label) * 0.5F, cy + AlbizWidgets.s(12.0F), glyph, label);
               if (clicked) {
                  ThemeImages.get().browseAsync();
               }
            } else {
               ThemeImages.Texture texture = ThemeImages.get().preview(name);
               boolean broken = texture == null && ThemeImages.get().isBroken(name);
               dl.addRectFilled(sx, sy, sx + tileW, sy + tileH, AlbizWidgets.c(GuiPalette.TILE), r);
               if (texture != null) {
                  float[] uv = SurfaceBackground.coverUv(texture.width(), texture.height(), tileW, tileH, 0.5F);
                  dl.addImageRounded(texture.id(), sx, sy, sx + tileW, sy + tileH, uv[0], uv[1], uv[2], uv[3], AlbizWidgets.c(-1), r, 240);
               } else if (broken && !armed) {
                  float cx = sx + tileW * 0.5F;
                  float cy = sy + tileH * 0.5F - AlbizWidgets.s(8.0F);
                  AlbizWidgets.cross(dl, cx, cy, AlbizWidgets.s(5.0F), GuiPalette.GLYPH_IDLE);
                  String label = "Can't load";
                  AlbizWidgets.text(dl, small, cx - AlbizWidgets.textWidth(small, label) * 0.5F, cy + AlbizWidgets.s(10.0F), GuiPalette.GLYPH_IDLE, label);
               }

               if (ThemeImages.isGif(name)) {
                  String badge = "GIF";
                  float badgePad = AlbizWidgets.s(4.0F);
                  float badgeH = AlbizWidgets.lineHeight(small) + AlbizWidgets.s(2.0F);
                  float badgeW = AlbizWidgets.textWidth(small, badge) + badgePad * 2.0F;
                  float bx = sx + AlbizWidgets.s(5.0F);
                  float by = sy + tileH - badgeH - AlbizWidgets.s(5.0F);
                  dl.addRectFilled(bx, by, bx + badgeW, by + badgeH, AlbizWidgets.c(AlbizWidgets.withAlpha(GuiPalette.SHADE, 0.6F)), AlbizWidgets.s(4.0F));
                  AlbizWidgets.text(dl, small, bx + badgePad, by + AlbizWidgets.s(1.0F), -1, badge);
               }

               if (armed) {
                  String ask = "Delete?";
                  dl.addRectFilled(sx, sy, sx + tileW, sy + tileH, AlbizWidgets.c(AlbizWidgets.withAlpha(GuiPalette.SHADE, 0.6F)), r);
                  AlbizWidgets.text(
                     dl, small, sx + (tileW - AlbizWidgets.textWidth(small, ask)) * 0.5F, sy + (tileH - AlbizWidgets.lineHeight(small)) * 0.5F, -1, ask
                  );
               }

               if (name.equals(surface.image.get())) {
                  dl.addRect(
                     sx - AlbizWidgets.s(1.0F),
                     sy - AlbizWidgets.s(1.0F),
                     sx + tileW + AlbizWidgets.s(1.0F),
                     sy + tileH + AlbizWidgets.s(1.0F),
                     AlbizWidgets.c(GuiPalette.accent()),
                     r + AlbizWidgets.s(1.0F),
                     0,
                     AlbizWidgets.s(2.0F)
                  );
               } else if (hover > 0.01F) {
                  dl.addRect(sx, sy, sx + tileW, sy + tileH, AlbizWidgets.c(AlbizWidgets.withAlpha(-1, 0.4F * hover)), r, 0, 1.0F);
               }

               if (deleteShown) {
                  float fade = armed ? 1.0F : hover;
                  float dcx = dx + del * 0.5F;
                  float dcy = dy + del * 0.5F;
                  int disc = armed ? GuiPalette.DANGER : AlbizTheme.lerpColor(AlbizWidgets.withAlpha(GuiPalette.SHADE, 0.55F), GuiPalette.accent(), deleteHover);
                  dl.addCircleFilled(dcx, dcy, del * 0.5F, AlbizWidgets.c(AlbizWidgets.withAlpha(disc, fade)), 32);
                  AlbizWidgets.cross(dl, dcx, dcy, AlbizWidgets.s(3.5F), AlbizWidgets.withAlpha(-1, fade));
               }

               if (clicked) {
                  if (armed) {
                     this.pendingConfirm = null;
                  } else {
                     surface.image.set(name);
                  }
               }

               if (!deleteHovered && GuardedImGui.isItemHovered(65536)) {
                  showTooltip(broken ? name + "\nCouldn't load this image" : name);
               }
            }
         }
      }

      if (deleted != null) {
         this.deleteImage(deleted);
      }

      int rows = (count + columns - 1) / columns;
      float hintY = startY + rows * (tileH + gap) - gap + AlbizWidgets.s(10.0F);
      String hint = "Drop an image on the screen or ";
      String link = "choose an image";
      float hintW = AlbizWidgets.textWidth(small, hint);
      float linkW = AlbizWidgets.textWidth(small, link);
      float lineH = AlbizWidgets.lineHeight(small);
      float wx = GuardedImGui.getWindowPosX();
      float wy = GuardedImGui.getWindowPosY() - GuardedImGui.getScrollY();
      AlbizWidgets.text(dl, small, wx + pad, wy + hintY, GuiPalette.GLYPH_IDLE, hint);
      GuardedImGui.setCursorPos(pad + hintW, hintY);
      if (GuardedImGui.invisibleButton("##openfolder", linkW, lineH)) {
         ThemeImages.get().openFolder();
      }

      float linkHover = AlbizWidgets.animate(GuardedImGui.getItemID(), GuardedImGui.isItemHovered(), 18.0F);
      AlbizWidgets.text(dl, small, wx + pad + hintW, wy + hintY, AlbizTheme.lerpColor(GuiPalette.accent(), GuiPalette.accentHover(), linkHover), link);
      GuardedImGui.setCursorPos(0.0F, hintY + lineH + AlbizWidgets.s(8.0F));
   }

   private void renderThemeActions(ThemeStore theme, float pad, float contentW) {
      float gap = AlbizWidgets.s(8.0F);
      float h = AlbizWidgets.s(34.0F);
      float buttonW = (contentW - gap * 2.0F) / 3.0F;
      float y = GuardedImGui.getCursorPosY() + AlbizWidgets.s(6.0F);
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##copytheme", "Copy theme", buttonW, h, false)) {
         class_310.method_1551().field_1774.method_1455(theme.exportString());
         this.showThemeMessage("Theme copied to the clipboard", false);
      }

      GuardedImGui.setCursorPos(pad + buttonW + gap, y);
      if (AlbizWidgets.chipButton("##pastetheme", "Paste theme", buttonW, h, false)) {
         boolean applied = theme.importString(class_310.method_1551().field_1774.method_1460());
         this.showThemeMessage(applied ? "Theme applied from the clipboard" : "The clipboard doesn't contain an Albiz theme", !applied);
      }

      GuardedImGui.setCursorPos(pad + (buttonW + gap) * 2.0F, y);
      if (AlbizWidgets.chipButton("##resettheme", "Reset", buttonW, h, false)) {
         theme.resetToDefaults();
         this.showThemeMessage("Theme reset to the defaults", false);
      }

      GuardedImGui.setCursorPos(0.0F, y + h);
   }

   private void showThemeMessage(String message, boolean error) {
      this.themeMessage = message;
      this.themeMessageError = error;
      this.themeMessageAt = System.nanoTime();
   }

   public void onFilesDropped(List<Path> paths) {
      if (!paths.isEmpty()) {
         if (this.tab == ClickGuiRenderer.Tab.CONFIG) {
            for (Path path : paths) {
               if (path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
                  this.importProfile(path);
                  return;
               }
            }
         }

         for (Path pathx : paths) {
            if (ThemeImages.isImage(pathx)) {
               if (this.tab == ClickGuiRenderer.Tab.HUD) {
                  this.importHudHeaderImage(pathx);
               } else {
                  this.switchTab(ClickGuiRenderer.Tab.THEME);
                  this.importHeaderImage(pathx);
               }

               return;
            }
         }

         if (this.tab == ClickGuiRenderer.Tab.HUD) {
            this.showHudMessage("Only PNG, JPG and GIF images can be used", true);
         } else {
            this.switchTab(ClickGuiRenderer.Tab.THEME);
            this.showThemeMessage("Only PNG, JPG and GIF images can be used", true);
         }
      }
   }

   private void importHudHeaderImage(Path path) {
      String name = ThemeImages.get().importFile(path);
      if (name == null) {
         this.showHudMessage("Only PNG, JPG and GIF images can be used", true);
      } else {
         String targetId = this.hudElementId != null ? this.hudElementId : "info_bar";
         Surface target = HudTheme.get().forElementId(targetId);
         if (target != null) {
            target.image.set(name);
            target.background.set("Image");
            this.selectHudElement(targetId);
            this.showHudMessage(net.albiz.client.i18n.AlbizI18n.backgroundAssigned(name,target.label), false);
         }
      }
   }

   private void importHeaderImage(Path path) {
      String name = ThemeImages.get().importFile(path);
      if (name == null) {
         this.showThemeMessage("Only PNG, JPG and GIF images can be used", true);
      } else {
         ThemeStore theme = ThemeStore.get();
         Surface target = this.themeSurface != null ? this.themeSurface : theme.cards;
         target.image.set(name);
         target.background.set("Image");
         this.showThemeMessage(net.albiz.client.i18n.AlbizI18n.backgroundAssigned(name,target.label), false);
      }
   }

   private void deleteImage(String name) {
      boolean deleted = ThemeImages.get().delete(name);
      String message = deleted ? "Deleted " + name : "Couldn't delete " + name;
      if (this.tab == ClickGuiRenderer.Tab.HUD) {
         this.showHudMessage(message, !deleted);
      } else {
         this.showThemeMessage(message, !deleted);
      }
   }

   private static void drawCheck(float x, float y, float progress, int accent) {
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      dl.addRectFilled(x, y, x + 16.0F, y + 16.0F, GuiPalette.TILE, 4.0F);
      dl.addRect(x + 0.5F, y + 0.5F, x + 16.0F - 0.5F, y + 16.0F - 0.5F, AlbizTheme.lerpColor(452984831, accent, progress), 4.0F);
      if (progress > 0.01F) {
         dl.addLine(x + 4.0F, y + 8.0F, x + 7.0F, y + 11.0F, accent, 2.0F * progress);
         dl.addLine(x + 7.0F, y + 11.0F, x + 12.0F, y + 5.0F, accent, 2.0F * progress);
      }
   }

   static float smooth(float from, float to, float dt, float speed) {
      return from + (to - from) * (1.0F - (float)Math.exp(-speed * Math.max(0.0F, dt)));
   }

   public static float animationDelta(float dt) {
      float speed = GuiPalette.animationSpeed();
      return speed <= 0.0F ? 1.0F : dt * speed;
   }

   public static float elapsed(long since, float durationMs) {
      float speed = GuiPalette.animationSpeed();
      return speed <= 0.0F ? 1.0F : Math.min(1.0F, (float)(System.nanoTime() - since) * speed / (durationMs * 1000000.0F));
   }

   public static float easeOut(float t) {
      float inv = 1.0F - t;
      return 1.0F - inv * inv * inv;
   }

   private void renderKeybindRow(ClientModule module, float pad, float contentW) {
      float rowH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 54.0F : 46.0F);
      float startY = this.rowStart(pad, contentW);
      boolean isListening = this.listeningModule == module;
      if (isListening) {
         if (this.listeningFirstFrame) {
            if (scanPressedKey() == -1) this.listeningFirstFrame = false;
         } else {
            int pressed = scanPressedKey();
            if (pressed == 256) {
               this.listeningModule = null;
               isListening = false;
            } else if (pressed == 261 || pressed == 259) {
               module.setKeybind(-1);
               this.listeningModule = null;
               isListening = false;
            } else if (net.albiz.client.input.ModuleKeybinds.isBindable(pressed)) {
               module.setKeybind(pressed);
               this.listeningModule = null;
               isListening = false;
            }
         }
      }

      rowLabel("Key", pad, startY, rowH);
      float chipH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 44.0F : 30.0F);
      float keyW = AlbizWidgets.s(96.0F);
      float modeW = AlbizWidgets.s(76.0F);
      float gap = AlbizWidgets.s(8.0F);
      float chipY = startY + (rowH - chipH) * 0.5F;
      if (!module.isAction()) {
         GuardedImGui.setCursorPos(pad + contentW - keyW - gap - modeW, chipY);
         if (AlbizWidgets.chipButton("##bindmode", module.isHoldMode() ? "Hold" : "Toggle", modeW, chipH, false)) {
            module.setHoldMode(!module.isHoldMode());
         }
      }

      String label = isListening ? "Press a key..." : (module.keybind() == -1 ? "None" : keyName(module.keybind()));
      GuardedImGui.setCursorPos(pad + contentW - keyW, chipY);
      if (AlbizWidgets.chipButton("##bind", label, keyW, chipH, isListening)) {
         if (isListening) {
            this.listeningModule = null;
         } else {
            this.listeningModule = module;
            this.listeningFirstFrame = true;
            net.albiz.client.input.KeyCapture.reset();
            this.listeningKeySetting = null;
            this.listeningKeyFirstFrame = false;
         }
      }

      GuardedImGui.setCursorPos(pad, startY + rowH);
      if (AlbizWidgets.chipButton("##clear_bind", "Clear bind", AlbizWidgets.s(140.0F), chipH, false)) {
         module.setKeybind(-1);
         this.listeningModule = null;
         this.listeningFirstFrame = false;
      }
      GuardedImGui.setCursorPos(0.0F, startY + rowH + chipH + AlbizWidgets.s(6.0F));
   }

   public boolean capturesKeys() {
      return this.listeningModule != null || this.listeningKeySetting != null || ImGuiManager.getInstance().typing();
   }

   public void onRemoved() {this.closeSettings();this.favoritesPicker.set(false);}

   public boolean handleEscape() {
      if(this.favoritesPicker.get()){this.favoritesPicker.set(false);return true;}
      if (this.listeningKeySetting != null) {
         this.listeningKeySetting = null;
         this.listeningKeyFirstFrame = false;
         return true;
      } else if (this.listeningModule != null) {
         this.listeningModule = null;
         this.listeningFirstFrame = false;
         return true;
      } else if(this.colorPickerOpen.get()) {
         this.colorPickerOpen.set(false);this.activeColorSetting=null;this.activeColorModule=null;return true;
      } else if (this.blockLists.closeWindow()) {
         return true;
      } else if (this.itemPickers.closeWindow()) {
         return true;
      } else if (this.itemColors.back()) {
         return true;
      } else if (this.settingsModule != null) {
         this.closeSettings();
         return true;
      } else {
         return false;
      }
   }

   public static int scanPressedKey() {
      if (net.albiz.client.platform.PlatformInfo.isMobile()) return net.albiz.client.input.KeyCapture.poll();
      class_310 client = class_310.method_1551();
      if (client == null || client.method_22683() == null) return -1;
      long handle = client.method_22683().method_4490();
      if (handle == 0L) return -1;

      for (int key = 32; key <= 348; key++) {
         if (net.albiz.client.input.ModuleKeybinds.isBindable(key) && GLFW.glfwGetKey(handle, key) == 1) {
            return key;
         }
      }

      for (int button = 0; button <= 7; button++) {
         if (GLFW.glfwGetMouseButton(handle, button) == 1) {
            return 1000 + button;
         }
      }

      return -1;
   }

   private String keyLabel(int key) {
      String label = (String)this.keyLabels.get(key);
      if (label == null) {
         label = keyName(key);
         this.keyLabels.put(key, label);
      }

      return label;
   }

   public static String keyName(int key) {
      if (!net.albiz.client.input.ModuleKeybinds.isBindable(key)) return "None";
      if (key >= 1000) {
         int button = key - 1000;

         return switch (button) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            default -> "MB" + (button + 1);
         };
      } else {
         if ((key >= 65 && key <= 90) || (key >= 48 && key <= 57)) return KEY_TEXT[key];
         if (key >= 290 && key <= 314) return KEY_TEXT[key];
         return switch (key) {
            case 32 -> "SPACE";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 258 -> "TAB";
            case 259 -> "BKSP";
            case 260 -> "INS";
            case 261 -> "DEL";
            case 262 -> "RIGHT";
            case 263 -> "LEFT";
            case 264 -> "DOWN";
            case 265 -> "UP";
            case 266 -> "PGUP";
            case 267 -> "PGDN";
            case 268 -> "HOME";
            case 269 -> "END";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 335 -> "NUM ENTER";
            case 340 -> "L SHIFT";
            case 341 -> "L CTRL";
            case 342 -> "L ALT";
            case 344 -> "R SHIFT";
            case 345 -> "R CTRL";
            case 346 -> "R ALT";
            default -> "K" + key;
         };
      }
   }

   private void renderModuleConfigActions(ClientModule module, float pad, float contentW) {
      float gap = AlbizWidgets.s(8.0F);
      float h = AlbizWidgets.s(36.0F);
      float buttonW = (contentW - gap) * 0.5F;
      float y = GuardedImGui.getCursorPosY() + AlbizWidgets.s(6.0F);
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##copy_config", "Copy Config", buttonW, h, true)) {
         this.copyModuleConfig(module);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Copy this module's settings as a short code");
      }

      String key = "paste:" + module.id();
      boolean confirming = this.isConfirming(key);
      GuardedImGui.setCursorPos(pad + buttonW + gap, y);
      if (AlbizWidgets.chipButton("##paste_config", confirming ? "Confirm paste" : "Paste Config", buttonW, h, confirming) && this.confirmClick(key)) {
         this.pasteModuleConfig(module);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         showTooltip("Replace this module's settings with a code from your clipboard");
      }

      y += h + AlbizWidgets.s(8.0F);
      ImFont small = ImGuiManager.getInstance().smallFont();
      if (module.id().equals(this.moduleCodeFor) && this.moduleCodeMessage != null && elapsed(this.moduleCodeAt, 2500.0F) < 1.0F) {
         GuardedImGui.setCursorPos(pad, y);
         boolean pushed = pushFont(small);
         GuardedImGui.pushStyleColor(0, this.moduleCodeError ? GuiPalette.DANGER : GuiPalette.GLYPH_IDLE);
         GuardedImGui.textUnformatted(this.moduleCodeMessage);
         GuardedImGui.popStyleColor();
         if (pushed) {
            GuardedImGui.popFont();
         }
      }

      GuardedImGui.setCursorPos(0.0F, y + AlbizWidgets.lineHeight(small));
   }

   private void copyModuleConfig(ClientModule module) {
      String code = ModuleCodes.copy(module);
      class_310.method_1551().field_1774.method_1455(code);
      this.setModuleCodeMessage(module, "Copied, " + code.length() + " characters", false);
   }

   private void pasteModuleConfig(ClientModule module) {
      try {
         ModuleCodes.paste(module, class_310.method_1551().field_1774.method_1460());
         this.setModuleCodeMessage(module, "Config pasted", false);
      } catch (ProfileException var3) {
         this.setModuleCodeMessage(module, var3.getMessage(), true);
      }
   }

   private void setModuleCodeMessage(ClientModule module, String message, boolean error) {
      this.moduleCodeFor = module.id();
      this.moduleCodeMessage = message;
      this.moduleCodeError = error;
      this.moduleCodeAt = System.nanoTime();
   }

   private void renderHudActions(float pad, float contentW) {
      float gap = AlbizWidgets.s(8.0F);
      float h = AlbizWidgets.s(36.0F);
      float buttonW = (contentW - gap) * 0.5F;
      float y = GuardedImGui.getCursorPosY() + AlbizWidgets.s(14.0F);
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##editlayout", "Edit Layout", buttonW, h, true)) {
         class_310.method_1551().method_1507(new HudEditorScreen());
      }

      GuardedImGui.setCursorPos(pad + buttonW + gap, y);
      if (AlbizWidgets.chipButton("##resetlayout", "Reset Layout", buttonW, h, false)) {
         HudLayoutStore.getInstance().resetToDefaults(GuardedImGui.getIO().getDisplaySizeX(), GuardedImGui.getIO().getDisplaySizeY());
      }

      GuardedImGui.setCursorPos(0.0F, y + h);
   }

   private static boolean isHudModule(ClientModule module) {
      return module.category() == ModuleCategory.CLIENT && "HUD".equals(module.name());
   }

   private static ClientModule hudModule() {
      for (ClientModule module : ModuleRegistry.all()) {
         if (isHudModule(module)) {
            return module;
         }
      }

      return null;
   }

   private net.albiz.client.port.soberui.ReferenceVisualModule[] nativeHudControls;
   private void renderHudStudio(float dt,float alpha,float view){
      if(nativeHudControls==null)nativeHudControls=new net.albiz.client.port.soberui.ReferenceVisualModule[]{
       new net.albiz.client.port.soberui.ReferenceVisualModule("native-hud-editor",net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.Hud.class)),
       new net.albiz.client.port.soberui.ReferenceVisualModule("native-target-editor",net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.TargetHud.class)),
       new net.albiz.client.port.soberui.ReferenceVisualModule("native-inventory-editor",net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.InventoryHud.class))};
      float sw=GuardedImGui.getIO().getDisplaySizeX(),sh=GuardedImGui.getIO().getDisplaySizeY(),w=Math.min(AlbizWidgets.s(380),sw-AlbizWidgets.s(24));
      GuardedImGui.setNextWindowPos(Math.max(AlbizWidgets.s(12),sw-w-AlbizWidgets.s(24)),AlbizWidgets.s(24),1);
      GuardedImGui.setNextWindowSize(w,Math.max(100,sh-AlbizWidgets.s(100)),1);
      if(GuardedImGui.begin("HUD Settings##native_hud_editor",0)){
       GuardedImGui.textWrapped(net.albiz.client.i18n.AlbizI18n.isTurkish()?"HUD öğelerini seç, taşı ve sağ panelden düzenle.":"Select HUD elements, drag them and edit their layout in the right panel.");
       if(GuardedImGui.button(net.albiz.client.i18n.AlbizI18n.isTurkish()?"HUD Editor Aç":"Open HUD Editor")){
        var client=class_310.method_1551();if(client.field_1724!=null)client.method_1507(new HudEditorScreen(client.field_1755));
       }
       for(var adapter:nativeHudControls){GuardedImGui.pushID(adapter.id());GuardedImGui.separator();GuardedImGui.textUnformatted(adapter.visual.name);var enabled=new imgui.type.ImBoolean(adapter.visual.isActive());if(GuardedImGui.checkbox("Enabled",enabled))adapter.visual.setEnabled(enabled.get());
        for(var setting:adapter.settings())if(setting.isVisible())this.renderSetting(adapter,setting,AlbizWidgets.s(12),w-AlbizWidgets.s(28));
        GuardedImGui.popID();}
      }GuardedImGui.end();
   }

   private static boolean hudStudioBlocked() {
      return GuardedImGui.isWindowHovered(4) || GuardedImGui.isAnyItemActive() || GuardedImGui.isPopupOpen("", 3072);
   }

   private void renderHudInspector(float dt, float alpha) {
      float sw = GuardedImGui.getIO().getDisplaySizeX();
      float sh = GuardedImGui.getIO().getDisplaySizeY();
      float w = Math.min(AlbizWidgets.s(360.0F), sw-AlbizWidgets.s(24));
      float h = Math.min(sh - AlbizWidgets.s(86.0F) - AlbizWidgets.s(40.0F), AlbizWidgets.s(760.0F));
      GuardedImGui.setNextWindowPos(Math.max(AlbizWidgets.s(12),sw - w - AlbizWidgets.s(24)), AlbizWidgets.s(24), 1);
      GuardedImGui.setNextWindowSize(w, h, 1);
      if (this.focusHudInspector) {
         GuardedImGui.setNextWindowFocus();
         this.focusHudInspector = false;
      }

      GuardedImGui.pushStyleVar(0, alpha);
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(14, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, cardRounding());
      GuardedImGui.pushStyleColor(2, GuiPalette.abgr(ThemeStore.get().cards.color.argbWithAlpha(255)));
      int flags = 59;
      if (GuardedImGui.begin("##hud_inspector", flags)) {
         float x = GuardedImGui.getWindowPosX();
         float y = GuardedImGui.getWindowPosY();
         drawShadow(x, y, w, h, cardRounding(), 6);
         GuardedImGui.getWindowDrawList().addRect(x, y, x + w, y + h, AlbizWidgets.c(318767103), cardRounding(), 0, 1.0F);
         float pad = AlbizWidgets.s(20.0F);
         float top = AlbizWidgets.s(20.0F);
         drawCardTitle("HUD", pad, top + AlbizWidgets.s(14.0F));
         float ty = top + AlbizWidgets.s(34.0F);
         if (this.hudMessage != null && System.nanoTime() - this.hudMessageAt < 3000000000L) {
            GuardedImGui.setCursorPos(pad, ty);
            GuardedImGui.pushTextWrapPos(w - pad);
            GuardedImGui.pushStyleColor(0, this.hudMessageError ? GuiPalette.DANGER : GuiPalette.GLYPH_HOVER);
            GuardedImGui.textUnformatted(this.hudMessage);
            GuardedImGui.popStyleColor();
            GuardedImGui.popTextWrapPos();
            ty = GuardedImGui.getCursorPosY();
         } else {
            ty = mutedParagraph("Click an element on screen to edit it.", pad, ty, w - pad);
         }

         ty = this.renderHudElementTabs(pad, ty + AlbizWidgets.s(12.0F), w - pad * 2.0F, dt) + AlbizWidgets.s(14.0F);
         cardDivider(pad, w, ty);
         GuardedImGui.setCursorPos(0.0F, ty + 1.0F);
         if (GuardedImGui.beginChild("##hud_inspector_body", w, Math.max(1.0F, h - ty - 1.0F - AlbizWidgets.s(10.0F)), false, 24)) {
            float contentW = w - pad * 2.0F;
            this.dividerPending = false;
            if (this.hudElementId == null) {
               this.renderHudGeneralOptions(pad, contentW);
            } else {
               this.renderHudElementOptions(this.hudElementId, pad, contentW);
            }

            GuardedImGui.dummy(1.0F, AlbizWidgets.s(18.0F));
            this.applySmoothScroll("hud_inspector", dt);
         }

         GuardedImGui.endChild();
      }

      GuardedImGui.end();
      GuardedImGui.popStyleColor();
      GuardedImGui.popStyleVar(4);
   }

   private float renderHudElementTabs(float pad, float y, float contentW, float dt) {
      @Environment(EnvType.CLIENT)
      record Item(String id, String label) {
      }

      List<Item> items = List.of(
         new Item(null, "General"),
         new Item("info_bar", HudTheme.get().infoBar.label),
         new Item("coordinates", HudTheme.get().coordinates.label),
         new Item("module_list", HudTheme.get().moduleList.label),
         new Item("potion_tracker", HudTheme.get().potions.label),
         new Item("region_map", HudTheme.get().regionMap.label),
         new Item("anti_vanish", HudTheme.get().antiVanish.label),
         new Item("staff_list", HudTheme.get().staffList.label),
         new Item("spotify", HudTheme.get().spotify.label),
         new Item("hotkeys", HudTheme.get().hotkeys.label),
         new Item("radar", HudRenderer.editorLabel("radar")),
         new Item("target_card", HudTheme.get().targetCard.label),
         new Item("armor_status", HudTheme.get().armorStatus.label),
         new Item("item_cooldowns", HudTheme.get().itemCooldowns.label),
         new Item("toggle_toasts", HudTheme.get().toasts.label)
      );
      ImFont small = ImGuiManager.getInstance().smallFont();
      boolean pushed = pushFont(small);
      float chipH = AlbizWidgets.s(24.0F);
      float gap = AlbizWidgets.s(6.0F);
      float rowGap = AlbizWidgets.s(6.0F);
      float curX = pad;
      float curY = y;

      try {
         for (Item item : items) {
            boolean selected = Objects.equals(item.id(), this.hudElementId);
            float chipW = Math.min(contentW,AlbizWidgets.textWidth(small, item.label()) + AlbizWidgets.s(18.0F));
            if (curX + chipW > pad + contentW && curX > pad) {
               curX = pad;
               curY += chipH + rowGap;
            }

            GuardedImGui.setCursorPos(curX, curY);
            if (AlbizWidgets.chipButton("##hudel_" + (item.id() == null ? "general" : item.id()), item.label(), chipW, chipH, selected)) {
               this.selectHudElement(item.id());
            }

            curX += chipW + gap;
         }
      } finally {
         if (pushed) {
            GuardedImGui.popFont();
         }
      }

      return curY + chipH;
   }

   private void renderHudGeneralOptions(float pad, float contentW) {
      ClientModule hud = hudModule();
      if (hud != null) {
         this.sectionLabel("Settings", pad);
         this.settingRows(pad, contentW, hud.settings().toArray(new Setting[0]));
      }

      this.sectionLabel("Layout", pad);
      this.settingRows(pad, contentW, HudTheme.get().font, HudTheme.get().rounding);
      float y = GuardedImGui.getCursorPosY() + AlbizWidgets.s(6.0F);
      float h = AlbizWidgets.s(34.0F);
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##resethudlayout", "Reset positions & sizes", contentW, h, false)) {
         HudLayoutStore.getInstance().resetToDefaults(GuardedImGui.getIO().getDisplaySizeX(), GuardedImGui.getIO().getDisplaySizeY());
         this.showHudMessage("HUD layout reset", false);
      }

      GuardedImGui.setCursorPos(0.0F, y + h);
      this.sectionLabel("Share", pad);
      this.renderHudThemeActions(pad, contentW);
   }

   private void renderHudElementOptions(String id, float pad, float contentW) {
      if (hudModule() instanceof HudModule hud) {
         List<Setting> own = hud.elementSettings(id);
         if (!own.isEmpty()) {
            this.sectionLabel(hudOptionsTitle(id), pad);
            this.settingRows(pad, contentW, own.toArray(new Setting[0]));
         }
      }

      if ("region_map".equals(id)) {
         ClientModule regionMap = ModuleRegistry.get(RegionMapModule.class);
         if (regionMap != null) {
            this.renderInlineModuleToggle(regionMap, pad, contentW);
            this.sectionLabel("Settings", pad);
            this.settingRows(pad, contentW, regionMap.settings().toArray(new Setting[0]));
         }
      }

      ClientModule owner = (ClientModule)("radar".equals(id)
         ? ModuleRegistry.get(RadarModule.class)
         : ("hotkeys".equals(id) ? ModuleRegistry.get(HotkeysModule.class) : null));
      if (owner != null) {
         this.renderInlineModuleToggle(owner, pad, contentW);
         if (!owner.settings().isEmpty()) {
            this.sectionLabel("Settings", pad);
            this.settingRows(pad, contentW, owner.settings().toArray(new Setting[0]));
         }
      }

      Surface surface = HudTheme.get().forElementId(id);
      if (surface != null) {
         this.renderSurfaceOptions(surface, pad, contentW);
      }
   }

   private static String hudOptionsTitle(String id) {
      return switch (id) {
         case "toggle_toasts" -> "Style";
         case "target_card", "armor_status", "item_cooldowns" -> "Options";
         default -> "Show";
      };
   }

   private void renderInlineModuleToggle(ClientModule module, float pad, float contentW) {
      float rowH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 54.0F : 46.0F);
      float startY = this.rowStart(pad, contentW);
      rowLabel("Active", pad, startY, rowH);
      float swW = AlbizWidgets.s(40.0F);
      float swH = AlbizWidgets.s(22.0F);
      GuardedImGui.setCursorPos(pad + contentW - swW, startY + (rowH - swH) * 0.5F);
      float sx = GuardedImGui.getCursorScreenPosX();
      float sy = GuardedImGui.getCursorScreenPosY();
      if (GuardedImGui.invisibleButton("##active", swW, swH)) {
         module.toggle();
      }

      AlbizWidgets.drawSwitch(GuardedImGui.getWindowDrawList(), sx, sy, swW, swH, AlbizWidgets.animate(GuardedImGui.getItemID(), module.isEnabled(), 16.0F));
      GuardedImGui.setCursorPos(0.0F, startY + rowH);
   }

   private void renderHudThemeActions(float pad, float contentW) {
      HudTheme theme = HudTheme.get();
      float gap = AlbizWidgets.s(8.0F);
      float h = AlbizWidgets.s(34.0F);
      float buttonW = (contentW - gap * 2.0F) / 3.0F;
      float y = GuardedImGui.getCursorPosY() + AlbizWidgets.s(6.0F);
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##copyhudtheme", "Copy theme", buttonW, h, false)) {
         class_310.method_1551().field_1774.method_1455(theme.exportString());
         this.showHudMessage("HUD theme copied to the clipboard", false);
      }

      GuardedImGui.setCursorPos(pad + buttonW + gap, y);
      if (AlbizWidgets.chipButton("##pastehudtheme", "Paste theme", buttonW, h, false)) {
         boolean applied = theme.importString(class_310.method_1551().field_1774.method_1460());
         this.showHudMessage(applied ? "HUD theme applied from the clipboard" : "The clipboard doesn't contain an Albiz HUD theme", !applied);
      }

      GuardedImGui.setCursorPos(pad + (buttonW + gap) * 2.0F, y);
      if (AlbizWidgets.chipButton("##resethudtheme", "Reset", buttonW, h, false)) {
         theme.resetToDefaults();
         this.showHudMessage("HUD theme reset to the defaults", false);
      }

      GuardedImGui.setCursorPos(0.0F, y + h);
   }

   private void showHudMessage(String message, boolean error) {
      this.hudMessage = message;
      this.hudMessageError = error;
      this.hudMessageAt = System.nanoTime();
   }

   private void selectHudElement(String id) {
      if (!Objects.equals(id, this.hudElementId)) {
         this.hudElementId = id;
         this.focusHudInspector = true;
      }
   }

   private void highlightHudElements() {
      float mx = GuardedImGui.getIO().getMousePosX();
      float my = GuardedImGui.getIO().getMousePosY();
      boolean dragging = HudRenderer.getInstance().isDragging();
      HudLayoutStore.HudElementState hovered = null;
      if (!dragging && !hudStudioBlocked()) {
         for (HudLayoutStore.HudElementState state : HudLayoutStore.getInstance().orderedElements()) {
            if (state.positioned && contains(new float[]{state.x, state.y, state.width, state.height}, mx, my)) {
               hovered = state;
            }
         }
      }

      ImDrawList under = GuardedImGui.getBackgroundDrawList();
      if (this.hudElementId != null) {
         HudLayoutStore.HudElementState selected = findHudElementState(this.hudElementId);
         if (selected != null) {
            this.outlineHudElement(under, selected, AlbizWidgets.withAlpha(GuiPalette.accent(), 0.8F));
         }
      }

      if (hovered != null) {
         if (!hovered.id.equals(this.hudElementId)) {
            this.outlineHudElement(under, hovered, -2130706433);
         }

         ImDrawList fg = GuardedImGui.getForegroundDrawList();
         Surface surface = HudTheme.get().forElementId(hovered.id);
         String label = surface != null ? surface.label : HudRenderer.editorLabel(hovered.id);
         String hint = label + "  ·  click to edit, drag to move, scroll to resize";
         ImFont small = ImGuiManager.getInstance().smallFont();
         float chipW = AlbizWidgets.textWidth(small, hint) + AlbizWidgets.s(18.0F);
         float chipH = AlbizWidgets.lineHeight(small) + AlbizWidgets.s(10.0F);
         float cx = Math.min(mx + AlbizWidgets.s(14.0F), GuardedImGui.getIO().getDisplaySizeX() - chipW - AlbizWidgets.s(4.0F));
         float cy = Math.min(my + AlbizWidgets.s(18.0F), GuardedImGui.getIO().getDisplaySizeY() - chipH - AlbizWidgets.s(4.0F));
         fg.addRectFilled(cx, cy, cx + chipW, cy + chipH, GuiPalette.accent(), chipH * 0.5F);
         AlbizWidgets.text(fg, small, cx + AlbizWidgets.s(9.0F), cy + AlbizWidgets.s(5.0F), -1, hint);
         if (GuardedImGui.isMouseClicked(0) || GuardedImGui.isMouseClicked(1)) {
            this.selectHudElement(hovered.id);
         }
      }
   }

   private void outlineHudElement(ImDrawList dl, HudLayoutStore.HudElementState state, int color) {
      float e = AlbizWidgets.s(3.0F);
      float rounding = HudRenderer.hudRounding() * HudRenderer.scaleOf(state);
      dl.addRect(state.x - e, state.y - e, state.x + state.width + e, state.y + state.height + e, color, rounding + e, 0, AlbizWidgets.s(1.5F));
   }

   private static HudLayoutStore.HudElementState findHudElementState(String id) {
      for (HudLayoutStore.HudElementState state : HudLayoutStore.getInstance().orderedElements()) {
         if (state.id.equals(id)) {
            return state;
         }
      }

      return null;
   }

   private void openColorPicker(ClientModule module, ColorSetting setting) {
      this.activeColorModule = module;
      this.activeColorSetting = setting;
      this.colorPickerOpen.set(true);
   }

   private void renderColorPickerWindow() {
      if (this.activeColorSetting != null) {
         if (!this.colorPickerOpen.get()) {
            this.activeColorSetting = null;
            this.activeColorModule = null;
         } else {
            GuardedImGui.setNextWindowSize(270.0F, 330.0F, 4);
            GuardedImGui.setNextWindowPos(260.0F, 90.0F, 4);
            int flags = 352;
            String owner = this.activeColorModule != null ? this.activeColorModule.name() + " / " : "";
            if (GuardedImGui.begin("Color - " + owner + this.activeColorSetting.name() + "##color_picker", this.colorPickerOpen, flags)) {
               int pickerFlags = 135593984;
               if (GuardedImGui.colorPicker4("##picker", this.activeColorSetting.value(), pickerFlags)) {
                  this.activeColorSetting.notifyChanged();
               }


               if (GuardedImGui.button("Back##color_back", 80.0F, 22.0F)) {
                  this.activeColorSetting.notifyChanged();
                  this.colorPickerOpen.set(false);
               }
            }

            GuardedImGui.end();
            if (!this.colorPickerOpen.get()) {
               this.activeColorSetting = null;
               this.activeColorModule = null;
            }
         }
      }
   }

   private void renderSetting(ClientModule module, Setting setting, float pad, float contentW) {
      float rowH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 54.0F : 46.0F);
      float startY = this.rowStart(pad, contentW);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      if (setting instanceof BooleanSetting bs) {
         GuardedImGui.setCursorPos(pad, startY);
         float sx = GuardedImGui.getCursorScreenPosX();
         float sy = GuardedImGui.getCursorScreenPosY();
         if (GuardedImGui.invisibleButton("##row", contentW, rowH)) {
            bs.set(!bs.get());
         }

         float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), GuardedImGui.isItemHovered(), 18.0F);
         if (hover > 0.01F) {
            dl.addRectFilled(
               sx - AlbizWidgets.s(10.0F),
               sy + AlbizWidgets.s(3.0F),
               sx + contentW + AlbizWidgets.s(10.0F),
               sy + rowH - AlbizWidgets.s(3.0F),
               AlbizWidgets.c(AlbizWidgets.withAlpha(184549375, hover)),
               AlbizWidgets.s(10.0F)
            );
         }

         rowLabel(setting.name(), pad, startY, rowH);
         float swW = AlbizWidgets.s(40.0F);
         float swH = AlbizWidgets.s(22.0F);
         AlbizWidgets.drawSwitch(dl, sx + contentW - swW, sy + (rowH - swH) * 0.5F, swW, swH, AlbizWidgets.animate(GuardedImGui.getID("##switch"), bs.get(), 16.0F));
      } else if (setting instanceof NumberSetting ns) {
         rowH = AlbizWidgets.s(64.0F);
         GuardedImGui.setCursorPos(pad, startY + AlbizWidgets.s(10.0F));
         GuardedImGui.textUnformatted(setting.name());
         ImFont small = ImGuiManager.getInstance().smallFont();
         String value = ns.formatted();
         float tw = AlbizWidgets.textWidth(small, value);
         float pillW = tw + AlbizWidgets.s(16.0F);
         float pillH = AlbizWidgets.lineHeight(small) + AlbizWidgets.s(8.0F);
         float wx = GuardedImGui.getWindowPosX();
         float wy = GuardedImGui.getWindowPosY() - GuardedImGui.getScrollY();
         float px = wx + pad + contentW - pillW;
         float py = wy + startY + AlbizWidgets.s(8.0F);
         dl.addRectFilled(px, py, px + pillW, py + pillH, AlbizWidgets.c(GuiPalette.TILE), pillH * 0.5F);
         AlbizWidgets.text(dl, small, px + AlbizWidgets.s(8.0F), py + AlbizWidgets.s(4.0F), GuiPalette.GLYPH_HOVER, value);
         GuardedImGui.setCursorPos(pad - AlbizWidgets.s(7.0F), startY + AlbizWidgets.s(36.0F));
         if (AlbizWidgets.sliderFloat("##v", ns.value(), ns.min(), ns.max(), contentW + AlbizWidgets.s(14.0F), ns::snap)) {
            ns.notifyChanged();
         }
      } else if (setting instanceof ModeSetting ms) {
         rowLabel(setting.name(), pad, startY, rowH);
         float widest = 0.0F;

         for (String mode : ms.modes()) {
            widest = Math.max(widest, GuardedImGui.calcTextSize(mode).x);
         }

         float dropW = Math.min(contentW * 0.6F, Math.max(AlbizWidgets.s(120.0F), widest + AlbizWidgets.s(44.0F)));
         float dropH = AlbizWidgets.s(30.0F);
         GuardedImGui.setCursorPos(pad + contentW - dropW, startY + (rowH - dropH) * 0.5F);
         int picked = AlbizWidgets.dropdown("##mode", ms.get(), ms.modes(), dropW, dropH);
         if (picked >= 0) {
            ms.set(ms.modes().get(picked));
         }
      } else if (setting instanceof ColorSetting cs) {
         rowLabel(setting.name(), pad, startY, rowH);
         float swW = AlbizWidgets.s(44.0F);
         float swH = AlbizWidgets.s(24.0F);
         GuardedImGui.setCursorPos(pad + contentW - swW, startY + (rowH - swH) * 0.5F);
         float sxx = GuardedImGui.getCursorScreenPosX();
         float syx = GuardedImGui.getCursorScreenPosY();
         if (GuardedImGui.invisibleButton("##color", swW, swH)) {
            this.openColorPicker(module, cs);
         }

         boolean hovered = GuardedImGui.isItemHovered();
         float[] val = cs.value();
         int r = (int)(val[0] * 255.0F) & 0xFF;
         int g = (int)(val[1] * 255.0F) & 0xFF;
         int b = (int)(val[2] * 255.0F) & 0xFF;
         int a = val.length > 3 ? (int)(val[3] * 255.0F) & 0xFF : 255;
         dl.addRectFilled(sxx, syx, sxx + swW, syx + swH, AlbizWidgets.c(GuiPalette.TILE), AlbizWidgets.s(7.0F));
         dl.addRectFilled(sxx, syx, sxx + swW, syx + swH, AlbizWidgets.c(a << 24 | b << 16 | g << 8 | r), AlbizWidgets.s(7.0F));
         dl.addRect(sxx, syx, sxx + swW, syx + swH, AlbizWidgets.c(hovered ? 1728053247 : 654311423), AlbizWidgets.s(7.0F), 0, 1.0F);
         if (hovered) {
            showTooltip(String.format("#%02X%02X%02X", r, g, b));
         }
      } else if (setting instanceof KeySetting ks) {
         boolean isListening = this.listeningKeySetting == ks;
         if (isListening) {
            if (this.listeningKeyFirstFrame) {
               if (scanPressedKey() == -1) this.listeningKeyFirstFrame = false;
            } else {
               int pressed = scanPressedKey();
               if (pressed == 256) {
                  this.listeningKeySetting = null;
                  isListening = false;
               } else if (pressed == 261 || pressed == 259) {
                  ks.setValue(-1);
                  this.listeningKeySetting = null;
                  isListening = false;
               } else if (net.albiz.client.input.ModuleKeybinds.isBindable(pressed)) {
                  ks.setValue(pressed);
                  this.listeningKeySetting = null;
                  isListening = false;
               }
            }
         }

         rowLabel(setting.name(), pad, startY, rowH);
         float keyW = AlbizWidgets.s(96.0F);
         float chipH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 44.0F : 30.0F);
         String keyLabel = isListening ? "Press a key..." : (ks.value() == -1 ? "None" : keyName(ks.value()));
         GuardedImGui.setCursorPos(pad + contentW - keyW, startY + (rowH - chipH) * 0.5F);
         if (AlbizWidgets.chipButton("##keyval", keyLabel, keyW, chipH, isListening)) {
            if (isListening) {
               this.listeningKeySetting = null;
            } else {
               this.listeningKeySetting = ks;
               this.listeningKeyFirstFrame = true;
               net.albiz.client.input.KeyCapture.reset();
               this.listeningModule = null;
               this.listeningFirstFrame = false;
            }
         }
      } else if (setting instanceof StringSetting ss && !ss.presets().isEmpty()) {
         rowH = this.itemPickers.renderPresets(module, ss, pad, contentW, startY);
      } else if (setting instanceof StringSetting ss && ss.isItemPicker()) {
         rowH = this.itemPickers.renderItem(module, ss, pad, contentW, startY);
      } else if (setting instanceof StringSetting ss && ss.enchantItem() != null) {
         rowH = this.itemPickers.renderEnchants(module, ss, pad, contentW, startY);
      } else if (setting instanceof StringSetting ss) {
         GuardedImGui.setCursorPos(pad, startY + AlbizWidgets.s(10.0F));
         GuardedImGui.textUnformatted(setting.name());
         GuardedImGui.setCursorPos(pad, startY + AlbizWidgets.s(34.0F));
         GuardedImGui.pushStyleVar(11, AlbizWidgets.s(12.0F), AlbizWidgets.s(8.0F));
         GuardedImGui.setNextItemWidth(contentW);
         boolean edited = ss.hint() != null ? GuardedImGui.inputTextWithHint("##str", ss.hint(), ss.getImString()) : GuardedImGui.inputText("##str", ss.getImString());
         if (edited) {
            ss.notifyChanged();
         }

         GuardedImGui.popStyleVar();
         rowH = AlbizWidgets.s(34.0F) + GuardedImGui.getTextLineHeight() + AlbizWidgets.s(16.0F) + AlbizWidgets.s(12.0F);
      } else if (setting instanceof StringListSetting sls && sls.isItemPicker()) {
         rowH = this.itemPickers.renderList(module, sls, pad, contentW, startY);
      } else if (setting instanceof StringListSetting sls) {
         rowH = renderStringList(sls, pad, contentW, startY);
      } else if (setting instanceof ActionSetting actionSetting) {
         float h = AlbizWidgets.s(36.0F);
         GuardedImGui.setCursorPos(pad, startY + (rowH + AlbizWidgets.s(6.0F) - h) * 0.5F);
         if (AlbizWidgets.chipButton("##action", setting.name(), contentW, h, false)) {
            actionSetting.run();
         }

         rowH += AlbizWidgets.s(6.0F);
      } else if (setting instanceof BlockColorListSetting bcls) {
         rowH = this.blockLists.render(module, bcls, pad, contentW, startY);
      } else if (setting instanceof ItemColorListSetting icls) {
         long enabled = icls.entries().stream().filter(en -> en.enabled().get()).count();
         if (listChip(setting.name(), "Edit (" + enabled + ")", pad, contentW, startY, rowH)) {
            this.itemColors.open(icls);
         }
      }

      if (!(setting instanceof ActionSetting)) {
         this.renderResetAffordance(setting, pad, contentW, startY, rowH);
      }

      GuardedImGui.setCursorPos(0.0F, startY + rowH);
   }

   private void renderResetAffordance(Setting setting, float pad, float contentW, float startY, float rowH) {
      if (this.tab != ClickGuiRenderer.Tab.THEME) {
         boolean modified = !setting.isDefault();
         float wx = GuardedImGui.getWindowPosX();
         float wy = GuardedImGui.getWindowPosY() - GuardedImGui.getScrollY();
         boolean topLabel = setting instanceof NumberSetting
            || setting instanceof StringSetting
            || setting instanceof StringListSetting
            || setting instanceof BlockColorListSetting;
         float labelCy = topLabel ? startY + AlbizWidgets.s(10.0F) + GuardedImGui.getTextLineHeight() * 0.5F : startY + rowH * 0.5F;
         if (modified) {
            float dx = wx + pad - AlbizWidgets.s(11.0F);
            float dy = wy + labelCy;
            float r = AlbizWidgets.s(3.0F);
            GuardedImGui.getWindowDrawList().addCircleFilled(dx, dy, r, AlbizWidgets.c(GuiPalette.accent()), 12);
            if (GuardedImGui.isMouseHoveringRect(dx - r * 2.0F, dy - r * 2.0F, dx + r * 2.0F, dy + r * 2.0F)) {
               showTooltip("Modified - right-click the row to reset");
            }
         }

         float x0 = wx + pad - AlbizWidgets.s(10.0F);
         float y0 = wy + startY;
         if (GuardedImGui.isWindowHovered() && GuardedImGui.isMouseClicked(1) && GuardedImGui.isMouseHoveringRect(x0, y0, x0 + contentW + AlbizWidgets.s(20.0F), y0 + rowH)) {
            GuardedImGui.openPopup("##setting_ctx");
         }

         GuardedImGui.pushStyleVar(2, AlbizWidgets.s(6.0F), AlbizWidgets.s(6.0F));
         if (GuardedImGui.beginPopup("##setting_ctx")) {
            GuardedImGui.textDisabled(setting.name());
            GuardedImGui.separator();
            if (modified) {
               if (GuardedImGui.selectable("Reset to default")) {
                  setting.resetToDefault();
               }
            } else {
               GuardedImGui.textDisabled("Already at default");
            }

            GuardedImGui.endPopup();
         }

         GuardedImGui.popStyleVar();
      }
   }

   private static float renderStringList(StringListSetting list, float pad, float contentW, float startY) {
      GuardedImGui.setCursorPos(pad, startY + AlbizWidgets.s(10.0F));
      GuardedImGui.textUnformatted(list.name());
      float y = startY + AlbizWidgets.s(34.0F);
      float gap = AlbizWidgets.s(6.0F);
      float fieldH = GuardedImGui.getTextLineHeight() + AlbizWidgets.s(16.0F);
      float removeW = fieldH;
      int remove = -1;
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(12.0F), AlbizWidgets.s(8.0F));

      for (int i = 0; i < list.size(); i++) {
         GuardedImGui.pushID(i);
         GuardedImGui.setCursorPos(pad, y);
         GuardedImGui.setNextItemWidth(contentW - removeW - gap);
         if (GuardedImGui.inputText("##entry", list.entry(i))) {
            list.edited();
         }

         GuardedImGui.setCursorPos(pad + contentW - removeW, y);
         if (AlbizWidgets.chipButton("##remove", "x", removeW, fieldH, false)) {
            remove = i;
         }

         GuardedImGui.popID();
         y += fieldH + gap;
      }

      GuardedImGui.popStyleVar();
      if (remove >= 0) {
         list.remove(remove);
      }

      if (list.canAdd()) {
         float addH = AlbizWidgets.s(30.0F);
         GuardedImGui.setCursorPos(pad, y);
         if (AlbizWidgets.chipButton("##add", "+ Add", contentW, addH, false)) {
            list.add();
         }

         y += addH;
      }

      return y - startY + AlbizWidgets.s(12.0F);
   }

   private static boolean listChip(String label, String chip, float pad, float contentW, float startY, float rowH) {
      rowLabel(label, pad, startY, rowH);
      float chipW = Math.max(AlbizWidgets.s(96.0F), GuardedImGui.calcTextSize(chip).x + AlbizWidgets.s(28.0F));
      float chipH = AlbizWidgets.s(net.albiz.client.platform.PlatformInfo.isMobile() ? 44.0F : 30.0F);
      GuardedImGui.setCursorPos(pad + contentW - chipW, startY + (rowH - chipH) * 0.5F);
      return AlbizWidgets.chipButton("##listedit", chip, chipW, chipH, false);
   }

   public static void applyTheme() {
      GuardedImGui.pushStyleVar(3, AlbizWidgets.s(10.0F));
      GuardedImGui.pushStyleVar(12, AlbizWidgets.s(8.0F));
      GuardedImGui.pushStyleVar(9, AlbizWidgets.s(10.0F));
      GuardedImGui.pushStyleVar(21, AlbizWidgets.s(6.0F));
      GuardedImGui.pushStyleVar(2, AlbizWidgets.s(10.0F), AlbizWidgets.s(10.0F));
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(10.0F), AlbizWidgets.s(6.0F));
      GuardedImGui.pushStyleVar(14, AlbizWidgets.s(8.0F), AlbizWidgets.s(6.0F));
      GuardedImGui.pushStyleVar(4, 0.0F);
      GuardedImGui.pushStyleVar(10, 1.0F);
      GuardedImGui.pushStyleVar(18, AlbizWidgets.s(6.0F));
      GuardedImGui.pushStyleVar(19, AlbizWidgets.s(3.0F));

      for (int i = 0; i < THEME_COLORS.length; i += 2) {
         GuardedImGui.pushStyleColor(THEME_COLORS[i], THEME_COLORS[i + 1]);
      }

      int accent = GuiPalette.accent();
      int card = GuiPalette.cardBg();
      GuardedImGui.pushStyleColor(2, GuiPalette.panelBg());
      GuardedImGui.pushStyleColor(10, card);
      GuardedImGui.pushStyleColor(11, card);
      GuardedImGui.pushStyleColor(12, card);
      GuardedImGui.pushStyleColor(18, accent);
      GuardedImGui.pushStyleColor(19, accent);
      GuardedImGui.pushStyleColor(20, accent);
      GuardedImGui.pushStyleColor(23, accent);
      GuardedImGui.pushStyleColor(26, accent);
      GuardedImGui.pushStyleColor(51, AlbizWidgets.withAlpha(accent, 0.4F));
   }

   public static void removeTheme() {
      GuardedImGui.popStyleColor(THEME_COLORS.length / 2 + 10);
      GuardedImGui.popStyleVar(11);
   }

   public static boolean pushFont(ImFont font) {
      if (font == null) {
         return false;
      } else {
         GuardedImGui.pushFont(font);
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private record CategoryPanel(ModuleCategory category, String id, String rowsId) {
   }

   @Environment(EnvType.CLIENT)
   private record PanelLayout(List<String> ids, List<String> keys, List<Float> heights) {
   }

   @Environment(EnvType.CLIENT)
   private static enum Tab {
      MODULES("Modules"),
      HUD("HUD"),
      FRIENDS("Friends"),
      THEME("Theme"),
      CONFIG("Config");

      final String label;

      private Tab(String label) {
         this.label = label;
      }
   }
}
