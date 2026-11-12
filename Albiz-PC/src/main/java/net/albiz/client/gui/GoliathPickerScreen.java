package net.albiz.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import net.albiz.client.imgui.GuardedImGui;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.albiz.client.config.ThemeStore;
import net.albiz.client.imgui.ImGuiManager;
import net.albiz.client.util.donut.DonutGoliaths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

@Environment(EnvType.CLIENT)
public final class GoliathPickerScreen extends class_437 {
   private static final float SQUARE = 7.0F;
   private static final float PAD = 14.0F;
   private static final float HEADER_H = 42.0F;
   private static final float INFO_H = 28.0F;
   private static final float BUTTON_W = 96.0F;
   private static final float BUTTON_H = 26.0F;
   private static final int HEADER_BAND = 251658239;
   private static final int WINDOW_FLAGS = 367;
   private final Set<DonutGoliaths.Goliath> picked;
   private final Consumer<Set<DonutGoliaths.Goliath>> onPick;
   private boolean confirmed;
   private boolean dragging;
   private boolean dragAdds;
   private DonutGoliaths.Goliath dragLast;

   public GoliathPickerScreen(Set<DonutGoliaths.Goliath> preselected, Consumer<Set<DonutGoliaths.Goliath>> onPick) {
      super(class_2561.method_43470("Goliath RTP"));
      this.picked = new LinkedHashSet<>(preselected);
      this.onPick = onPick;
   }

   protected void method_25426() {
      super.method_25426();
      ImGuiManager.getInstance().activate(this::draw);
   }

   public void method_25432() {
      super.method_25432();
      ImGuiManager.getInstance().deactivate();
      this.onPick.accept(this.confirmed ? Set.copyOf(this.picked) : null);
   }

   public void method_25420(class_332 graphics, int mouseX, int mouseY, float partialTick) {
      if (ThemeStore.get().menuBlur.get()) {
         super.method_25420(graphics, mouseX, mouseY, partialTick);
      }
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
      ImGuiManager.getInstance().requestCursor(graphics);
   }

   public boolean method_25404(class_11908 event) {
      if (event.comp_4795() != 257 && event.comp_4795() != 335) {
         return super.method_25404(event);
      } else {
         this.confirm();
         return true;
      }
   }

   public boolean method_25421() {
      return false;
   }

   private void confirm() {
      if (!this.picked.isEmpty()) {
         this.confirmed = true;
         this.field_22787.execute(this::method_25419);
      }
   }

   private void draw() {
      float side = AlbizWidgets.s(252.0F);
      float pad = AlbizWidgets.s(14.0F);
      float headerH = AlbizWidgets.s(42.0F);
      float infoH = AlbizWidgets.s(28.0F);
      float rounding = AlbizWidgets.s(GuiPalette.rounding());
      GuardedImGui.setNextWindowPos(GuardedImGui.getIO().getDisplaySizeX() * 0.5F, GuardedImGui.getIO().getDisplaySizeY() * 0.5F, 1, 0.5F, 0.5F);
      GuardedImGui.pushStyleColor(2, GuiPalette.panelBg());
      GuardedImGui.pushStyleVar(2, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(14, 0.0F, 0.0F);
      GuardedImGui.pushStyleVar(3, rounding);
      GuardedImGui.pushStyleVar(4, 0.0F);
      if (GuardedImGui.begin("##goliath_rtp", 367)) {
         float wx = GuardedImGui.getWindowPosX();
         float wy = GuardedImGui.getWindowPosY();
         float ww = side + pad * 2.0F;
         float wh = GuardedImGui.getWindowHeight();
         ClickGuiRenderer.drawShadow(wx, wy, ww, wh, rounding, 4);
         ImDrawList dl = GuardedImGui.getWindowDrawList();
         SurfaceBackground.draw(dl, ThemeStore.get().panels, wx, wy, ww, wh, rounding);
         boolean guard = HudChrome.needsTextGuard(ThemeStore.get().panels);
         drawHeader(dl, wx, wy, ww, headerH, rounding, guard);
         GuardedImGui.dummy(ww, headerH);
         GuardedImGui.setCursorPos(pad, headerH);
         DonutGoliaths.Goliath hover = this.drawMap(dl, side);
         ImFont small = ImGuiManager.getInstance().smallFont();
         String info = hover != null
            ? hover.label() + (hover.region() == 2 ? "  ·  no /rtp" : "")
            : (
               this.picked.isEmpty()
                  ? "Click or drag to pick goliaths"
                  : this.picked.size() + (this.picked.size() == 1 ? " goliath picked" : " goliaths picked")
            );
         float infoY = wy + headerH + side;
         AlbizWidgets.text(dl, small, wx + pad, infoY + (infoH - AlbizWidgets.lineHeight(small)) * 0.5F, -4542808, info, guard);
         float buttonW = AlbizWidgets.s(96.0F);
         float buttonH = AlbizWidgets.s(26.0F);
         float buttonsY = headerH + side + infoH;
         boolean pushed = ClickGuiRenderer.pushFont(small);
         GuardedImGui.setCursorPos(pad, buttonsY);
         if (AlbizWidgets.chipButton("##goliath_clear", "Clear", buttonW, buttonH, false)) {
            this.picked.clear();
         }

         GuardedImGui.setCursorPos(pad + side - buttonW, buttonsY);
         String go = this.picked.isEmpty() ? "Go" : "Go (" + this.picked.size() + ")";
         if (AlbizWidgets.chipButton("##goliath_go", go, buttonW, buttonH, !this.picked.isEmpty())) {
            this.confirm();
         }

         if (pushed) {
            GuardedImGui.popFont();
         }

         GuardedImGui.setCursorPos(0.0F, buttonsY + buttonH);
         GuardedImGui.dummy(ww, pad);
      }

      GuardedImGui.end();
      GuardedImGui.popStyleVar(4);
      GuardedImGui.popStyleColor();
   }

   private static void drawHeader(ImDrawList dl, float x, float y, float w, float h, float rounding, boolean guard) {
      float cap = Math.min(h, rounding + AlbizWidgets.s(2.0F));
      int top = AlbizWidgets.c(AlbizWidgets.withAlpha(251658239, 1.0F - cap / (2.0F * h)));
      int below = AlbizWidgets.c(AlbizWidgets.withAlpha(251658239, 1.0F - cap / h));
      int clear = AlbizWidgets.c(16777215);
      dl.addRectFilled(x, y, x + w, y + cap, top, rounding, 48);
      dl.addRectFilledMultiColor(x, y + cap, x + w, y + h, below, below, clear, clear);
      ImFont font = ImGuiManager.getInstance().categoryFont();
      float r = AlbizWidgets.s(7.0F);
      float cx = x + AlbizWidgets.s(14.0F) + r;
      float cy = y + h * 0.5F;
      AlbizWidgets.grid(dl, cx, cy, r, -4542808);
      AlbizWidgets.text(dl, font, cx + r + AlbizWidgets.s(9.0F), y + (h - AlbizWidgets.lineHeight(font)) * 0.5F, -461582, "Goliath RTP", guard);
   }

   private DonutGoliaths.Goliath drawMap(ImDrawList dl, float side) {
      float mapX = GuardedImGui.getCursorScreenPosX();
      float mapY = GuardedImGui.getCursorScreenPosY();
      GuardedImGui.invisibleButton("##goliath_map", side, side);
      float square = side / 36.0F;
      DonutGoliaths.Goliath hover = GuardedImGui.isItemHovered() ? goliathAt(GuardedImGui.getMousePosX() - mapX, GuardedImGui.getMousePosY() - mapY, square) : null;
      boolean pickable = hover != null && hover.region() != 2;
      if (GuardedImGui.isItemClicked(0) && pickable) {
         this.dragAdds = !this.picked.contains(hover);
         this.toggle(hover);
         this.dragging = true;
         this.dragLast = hover;
      } else if (this.dragging && GuardedImGui.isMouseDown(0)) {
         DonutGoliaths.Goliath under = goliathAt(GuardedImGui.getMousePosX() - mapX, GuardedImGui.getMousePosY() - mapY, square);
         if (under != null && under.region() != 2 && !under.equals(this.dragLast)) {
            this.toggle(under);
            this.dragLast = under;
         }
      }

      if (!GuardedImGui.isMouseDown(0)) {
         this.dragging = false;
         this.dragLast = null;
      }

      float scale = ImGuiManager.getInstance().uiScale();
      float mapR = Math.min(AlbizWidgets.s(4.0F), square);
      boolean any = !this.picked.isEmpty();

      for (int region = 0; region < DonutGoliaths.CELLS.length; region++) {
         int color = RegionMapRenderer.regionColor(region);
         boolean europe = region == 2;
         int[][] goliaths = DonutGoliaths.CELLS[region];

         for (int index = 0; index < goliaths.length; index++) {
            DonutGoliaths.Goliath goliath = new DonutGoliaths.Goliath(region, index);
            boolean bright = !europe && (this.picked.contains(goliath) || goliath.equals(hover));
            float alpha = europe ? 0.3F : (bright ? 1.0F : (any ? 0.45F : 0.85F));
            RegionMapRenderer.tile(dl, mapX, mapY, square, goliaths[index], HudChrome.scaleAlpha(color, alpha), mapR);
         }
      }

      RegionMapRenderer.drawDividers(dl, mapX, mapY, square, 1.0F, scale);

      for (int region = 0; region < DonutGoliaths.CELLS.length; region++) {
         boolean europe = region == 2;
         int[][] goliaths = DonutGoliaths.CELLS[region];

         for (int index = 0; index < goliaths.length; index++) {
            int[] cell = goliaths[index];
            float x1 = mapX + cell[0] * square;
            float y1 = mapY + cell[1] * square;
            float x2 = mapX + cell[2] * square;
            float y2 = mapY + cell[3] * square;
            DonutGoliaths.Goliath goliath = new DonutGoliaths.Goliath(region, index);
            float inset = AlbizWidgets.s(0.75F);
            if (this.picked.contains(goliath)) {
               dl.addRect(x1 + inset, y1 + inset, x2 - inset, y2 - inset, -1, 0.0F, 0, AlbizWidgets.s(1.5F));
            } else if (!europe && goliath.equals(hover)) {
               dl.addRect(x1 + inset, y1 + inset, x2 - inset, y2 - inset, -1711276033, 0.0F, 0, AlbizWidgets.s(1.0F));
            }

            RegionMapRenderer.drawCenteredTileText(dl, index + 1, x1, y1, x2 - x1, y2 - y1, 1.0F, europe ? 0.5F : 1.0F, scale);
         }
      }

      if (this.field_22787.field_1724 != null && this.field_22787.field_1687 != null && this.field_22787.field_1687.method_27983() != class_1937.field_25181) {
         double factor = this.field_22787.field_1687.method_27983() == class_1937.field_25180 ? 8.0 : 1.0;
         double span = 450000.0;
         double fx = (this.field_22787.field_1724.method_23317() * factor + 225000.0) / span;
         double fz = (this.field_22787.field_1724.method_23321() * factor + 225000.0) / span;
         if (fx >= 0.0 && fx < 1.0 && fz >= 0.0 && fz < 1.0) {
            float px = mapX + (float)(fx * side);
            float pz = mapY + (float)(fz * side);
            dl.addCircleFilled(px, pz, AlbizWidgets.s(3.0F), -13487361);
            dl.addCircle(px, pz, AlbizWidgets.s(3.0F), -1728053248, 16, AlbizWidgets.s(1.0F));
         }
      }

      return hover;
   }

   private void toggle(DonutGoliaths.Goliath goliath) {
      if (this.dragAdds) {
         this.picked.add(goliath);
      } else {
         this.picked.remove(goliath);
      }
   }

   private static DonutGoliaths.Goliath goliathAt(float u, float v, float square) {
      return !(u < 0.0F) && !(v < 0.0F) ? DonutGoliaths.at(u / square * 12500.0F - 225000.0F, v / square * 12500.0F - 225000.0F) : null;
   }
}
