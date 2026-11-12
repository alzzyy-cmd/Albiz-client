package net.albiz.client.gui;

import java.nio.file.Path;
import java.util.List;
import net.albiz.client.config.HudTheme;
import net.albiz.client.config.ModuleConfigStore;
import net.albiz.client.config.ThemeStore;
import net.albiz.client.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

@Environment(EnvType.CLIENT)
public class ImGuiScreen extends class_437 {
   private final ClickGuiRenderer clickGui = new ClickGuiRenderer();

   public ImGuiScreen() {
      super(class_2561.method_43470("ALBIZ"));
   }

   public void inspect(net.albiz.client.module.ClientModule module){this.clickGui.inspect(module);}
   protected void method_25426() {
      super.method_25426();
      ImGuiManager.getInstance().activate(this.clickGui::render);
   }

   public void method_25432() {
      super.method_25432();
      this.clickGui.onRemoved();
      ImGuiManager.getInstance().deactivate();
      net.albiz.client.port.soberui.SoberUi.save();
      ModuleConfigStore.getInstance().saveIfChanged();
      ThemeStore.get().save();
      HudTheme.get().save();
   }

   public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (ThemeStore.get().menuBlur.get()) {
         super.method_25420(guiGraphics, mouseX, mouseY, partialTick);
      }
   }

   public void method_29638(List<Path> paths) {
      this.clickGui.onFilesDropped(paths);
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float tickDelta) {
      ImGuiManager.getInstance().requestCursor(graphics);
   }

   public boolean method_25404(class_11908 event) {
      if (event == null) return false;
      return event.comp_4795() == 256 && this.clickGui.handleEscape() ? true : super.method_25404(event);
   }

   public boolean capturesKeys() {
      return this.clickGui.capturesKeys();
   }

   public boolean method_25421() {
      return false;
   }
}
