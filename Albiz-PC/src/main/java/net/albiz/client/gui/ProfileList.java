package net.albiz.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import net.albiz.client.imgui.GuardedImGui;
import imgui.type.ImString;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.albiz.client.config.ConfigProfiles;
import net.albiz.client.config.ProfileException;
import net.albiz.client.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
final class ProfileList {
   private static final float ROW_H = 52.0F;
   private static final float CHIP_W = 58.0F;
   private static final float CHIP_H = 28.0F;
   private static final float GAP = 6.0F;
   private static final float LABEL_W = 96.0F;
   private static final float FIELD_H = 30.0F;
   private static final long ARM_NANOS = 3000000000L;
   private static final int DISCORD_LIMIT = 2000;
   private static final String PRIVATE_NOTE = "Webhooks, macros, seeds and other private settings are not included.";
   private final BiConsumer<String, Boolean> messages;
   private final ImString renameInput = new ImString(64);
   private final ImString serverInput = new ImString(256);
   private final Map<String, Float> editorHeights = new HashMap<>();
   private String expanded;
   private String armed;
   private long armedAt;
   private String scrollTo;
   private float scrollTarget = Float.NaN;
   private boolean focusServer;

   ProfileList(BiConsumer<String, Boolean> messages) {
      this.messages = messages;
   }

   void open(String name) {
      this.expanded = null;
      this.toggle(name);
      this.scrollTo = key(name);
   }

   void disarm() {
      this.armed = null;
   }

   float takeScrollTarget() {
      float target = this.scrollTarget;
      this.scrollTarget = Float.NaN;
      return target;
   }

   void render(ConfigProfiles profiles, List<ConfigProfiles.Profile> list, String active, float pad, float contentW) {
      for (ConfigProfiles.Profile profile : list) {
         this.row(profiles, profile, profile.name().equalsIgnoreCase(active), pad, contentW);
      }
   }

   private void row(ConfigProfiles profiles, ConfigProfiles.Profile profile, boolean active, float pad, float contentW) {
      String name = profile.name();
      String key = key(name);
      boolean open = key.equals(this.expanded);
      GuardedImGui.pushID("profile_" + key);
      float localY = GuardedImGui.getCursorPosY();
      if (key.equals(this.scrollTo)) {
         this.scrollTarget = localY - AlbizWidgets.s(8.0F);
         this.scrollTo = null;
      }

      float rowH = AlbizWidgets.s(52.0F);
      float reveal = AlbizWidgets.animate(GuardedImGui.getID("##reveal"), open, 16.0F);
      float editorH = reveal > 0.001F ? this.editorHeights.getOrDefault(key, AlbizWidgets.s(240.0F)) * reveal : 0.0F;
      GuardedImGui.setCursorPos(pad, localY);
      float x0 = GuardedImGui.getCursorScreenPosX();
      float y0 = GuardedImGui.getCursorScreenPosY();
      GuardedImGui.setNextItemAllowOverlap();
      GuardedImGui.invisibleButton("##row", contentW, rowH);
      if (GuardedImGui.isItemHovered() && GuardedImGui.isMouseDoubleClicked(0)) {
         this.load(profiles, profile);
      }

      boolean rowHovered = GuardedImGui.isMouseHoveringRect(x0, y0, x0 + contentW, y0 + rowH);
      float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), rowHovered, 18.0F);
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      float left = x0 - AlbizWidgets.s(8.0F);
      float right = x0 + contentW + AlbizWidgets.s(8.0F);
      float r = AlbizWidgets.s(10.0F);
      int corners = reveal > 0.001F ? 48 : 240;
      if (reveal > 0.001F) {
         dl.addRectFilled(left, y0, right, y0 + rowH + editorH, AlbizWidgets.c(AlbizWidgets.withAlpha(184549375, reveal)), r);
      }

      if (active) {
         dl.addRectFilled(left, y0, right, y0 + rowH, AlbizWidgets.c(AlbizWidgets.withAlpha(GuiPalette.accent(), 0.16F)), r, corners);
      }

      if (hover > 0.01F) {
         dl.addRectFilled(left, y0, right, y0 + rowH, AlbizWidgets.c(AlbizWidgets.withAlpha(218103807, hover)), r, corners);
      }

      ImFont font = GuardedImGui.getFont();
      ImFont small = ImGuiManager.getInstance().smallFont();
      float chipW = AlbizWidgets.s(58.0F);
      float chipH = AlbizWidgets.s(28.0F);
      float editSize = AlbizWidgets.s(28.0F);
      float gap = AlbizWidgets.s(6.0F);
      float buttonsW = chipW * 2.0F + editSize + gap * 2.0F;
      float textRoom = contentW - buttonsW - AlbizWidgets.s(12.0F);
      float badgeW = active ? AlbizWidgets.textWidth(small, "Active") + AlbizWidgets.s(14.0F) : 0.0F;
      String shown = AlbizWidgets.ellipsize(font, name, textRoom - (active ? badgeW + AlbizWidgets.s(8.0F) : 0.0F));
      float lineH = AlbizWidgets.lineHeight(font);
      float smallH = AlbizWidgets.lineHeight(small);
      float ty = y0 + (rowH - lineH - AlbizWidgets.s(3.0F) - smallH) * 0.5F;
      AlbizWidgets.text(dl, font, x0, ty, -461582, shown);
      if (active) {
         float bx = x0 + AlbizWidgets.textWidth(font, shown) + AlbizWidgets.s(8.0F);
         float bh = smallH + AlbizWidgets.s(6.0F);
         float by = ty + (lineH - bh) * 0.5F;
         dl.addRectFilled(bx, by, bx + badgeW, by + bh, AlbizWidgets.c(GuiPalette.accent()), bh * 0.5F);
         AlbizWidgets.text(dl, small, bx + AlbizWidgets.s(7.0F), by + AlbizWidgets.s(3.0F), -1, "Active");
      }

      AlbizWidgets.text(
         dl, small, x0, ty + lineH + AlbizWidgets.s(3.0F), profile.broken() ? -10856974 : -4542808, AlbizWidgets.ellipsize(small, summary(profile), textRoom)
      );
      float bx = pad + contentW - buttonsW;
      float by = localY + (rowH - chipH) * 0.5F;
      GuardedImGui.setCursorPos(bx, by);
      if (AlbizWidgets.chipButton("##load", "Load", chipW, chipH, false)) {
         this.load(profiles, profile);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip("Apply the saved " + partList(profile.parts()) + " of this profile");
      }

      String saveKey = "save:" + key;
      boolean saveArmed = this.isArmed(saveKey);
      GuardedImGui.setCursorPos(bx + chipW + gap, by);
      if (AlbizWidgets.chipButton("##save", saveArmed ? "Sure?" : "Save", chipW, chipH, saveArmed) && this.confirm(saveKey)) {
         this.save(profiles, profile);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip("Overwrite this profile with your current " + partList(profile.parts()));
      }

      GuardedImGui.setCursorPos(bx + (chipW + gap) * 2.0F, localY + (rowH - editSize) * 0.5F);
      float ex = GuardedImGui.getCursorScreenPosX() + editSize * 0.5F;
      float ey = GuardedImGui.getCursorScreenPosY() + editSize * 0.5F;
      if (GuardedImGui.invisibleButton("##edit", editSize, editSize)) {
         this.toggle(name);
      }

      boolean editHovered = GuardedImGui.isItemHovered();
      float editHover = Math.max(AlbizWidgets.animate(GuardedImGui.getItemID(), editHovered, 18.0F), reveal);
      if (editHover > 0.01F) {
         dl.addCircleFilled(ex, ey, editSize * 0.5F, AlbizWidgets.c(AlbizWidgets.withAlpha(-12240588, editHover)), 32);
      }

      AlbizWidgets.chevronRotated(dl, ex, ey, AlbizWidgets.s(4.0F), AlbizTheme.lerpColor(-4542808, -461582, editHover), reveal * (float) (Math.PI / 2));
      if (editHovered) {
         ClickGuiRenderer.showTooltip(open ? "Close" : "Edit, share or delete");
      }

      float bottom = localY + rowH;
      if (reveal > 0.001F) {
         GuardedImGui.pushClipRect(left, y0 + rowH, right, y0 + rowH + editorH, true);
         GuardedImGui.pushStyleVar(0, GuardedImGui.getStyle().getAlpha() * reveal);
         this.editorHeights.put(key, this.editor(profiles, profile, key, pad, contentW, bottom));
         GuardedImGui.popStyleVar();
         GuardedImGui.popClipRect();
      }

      GuardedImGui.setCursorPos(0.0F, bottom + editorH + AlbizWidgets.s(4.0F));
      GuardedImGui.popID();
   }

   private float editor(ConfigProfiles profiles, ConfigProfiles.Profile profile, String key, float pad, float contentW, float top) {
      ImDrawList dl = GuardedImGui.getWindowDrawList();
      ImFont font = GuardedImGui.getFont();
      float h = AlbizWidgets.s(30.0F);
      float gap = AlbizWidgets.s(6.0F);
      float framePadY = (h - GuardedImGui.getTextLineHeight()) * 0.5F;
      float fieldX = pad + AlbizWidgets.s(96.0F);
      float fieldW = contentW - AlbizWidgets.s(96.0F);
      float y = top + AlbizWidgets.s(6.0F);
      label("Name", pad, y, h);
      float renameW = AlbizWidgets.s(78.0F);
      GuardedImGui.setCursorPos(fieldX, y);
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(10.0F), framePadY);
      GuardedImGui.setNextItemWidth(fieldW - renameW - gap);
      GuardedImGui.inputTextWithHint("##rename", "Profile name", this.renameInput);
      boolean rename = AlbizWidgets.submittedWithEnter();
      GuardedImGui.popStyleVar();
      GuardedImGui.setCursorPos(pad + contentW - renameW, y);
      if (AlbizWidgets.chipButton("##rename_go", "Rename", renameW, h, false)) {
         rename = true;
      }

      if (rename) {
         this.rename(profiles, profile);
      }

      y += h + AlbizWidgets.s(10.0F);
      label("Includes", pad, y, h);
      ConfigProfiles.Part[] parts = ConfigProfiles.Part.values();
      float partW = (fieldW - gap * (parts.length - 1)) / parts.length;

      for (int i = 0; i < parts.length; i++) {
         ConfigProfiles.Part part = parts[i];
         boolean on = profile.parts().contains(part);
         GuardedImGui.setCursorPos(fieldX + i * (partW + gap), y);
         if (AlbizWidgets.chipButton("##part_" + part.key, part.label, partW, h, on)) {
            this.setPart(profiles, profile, part, !on);
         }

         if (GuardedImGui.isItemHovered(65536)) {
            ClickGuiRenderer.showTooltip(partTip(part, on));
         }
      }

      y += h + AlbizWidgets.s(10.0F);
      label("Load on join", pad, y, h);

      for (String server : profile.servers()) {
         GuardedImGui.pushID("server_" + server);
         GuardedImGui.setCursorPos(fieldX, y);
         float sx = GuardedImGui.getCursorScreenPosX();
         float sy = GuardedImGui.getCursorScreenPosY();
         dl.addRectFilled(sx, sy, sx + fieldW, sy + h, AlbizWidgets.c(-13226454), AlbizWidgets.s(7.0F));
         AlbizWidgets.text(
            dl,
            font,
            sx + AlbizWidgets.s(10.0F),
            sy + (h - AlbizWidgets.lineHeight(font)) * 0.5F,
            -461582,
            AlbizWidgets.ellipsize(font, server, fieldW - h - AlbizWidgets.s(14.0F))
         );
         GuardedImGui.setCursorPos(fieldX + fieldW - h, y);
         if (GuardedImGui.invisibleButton("##remove", h, h)) {
            this.removeServer(profiles, profile, server);
         }

         boolean removeHovered = GuardedImGui.isItemHovered();
         float removeHover = AlbizWidgets.animate(GuardedImGui.getItemID(), removeHovered, 18.0F);
         float rcx = sx + fieldW - h * 0.5F;
         float rcy = sy + h * 0.5F;
         if (removeHover > 0.01F) {
            dl.addCircleFilled(rcx, rcy, h * 0.38F, AlbizWidgets.c(AlbizWidgets.withAlpha(-10856974, 0.18F * removeHover)), 24);
         }

         AlbizWidgets.cross(dl, rcx, rcy, AlbizWidgets.s(3.5F), AlbizTheme.lerpColor(-4542808, -10856974, removeHover));
         if (removeHovered) {
            ClickGuiRenderer.showTooltip("Stop loading on " + server);
         }

         GuardedImGui.popID();
         y += h + gap;
      }

      float addW = AlbizWidgets.s(60.0F);
      GuardedImGui.setCursorPos(fieldX, y);
      GuardedImGui.pushStyleVar(11, AlbizWidgets.s(10.0F), framePadY);
      GuardedImGui.setNextItemWidth(fieldW - addW - gap);
      if (this.focusServer) {
         GuardedImGui.setKeyboardFocusHere();
         this.focusServer = false;
      }

      GuardedImGui.inputTextWithHint("##server", "Server address", this.serverInput, 16);
      boolean add = AlbizWidgets.submittedWithEnter();
      GuardedImGui.popStyleVar();
      GuardedImGui.setCursorPos(pad + contentW - addW, y);
      if (AlbizWidgets.chipButton("##server_add", "Add", addW, h, false)) {
         add = true;
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip("Loads this profile when you join one of these servers.");
      }

      if (add) {
         this.addServer(profiles, profile);
      }

      y += h + AlbizWidgets.s(12.0F);
      float actionW = (contentW - gap * 2.0F) / 3.0F;
      GuardedImGui.setCursorPos(pad, y);
      if (AlbizWidgets.chipButton("##export", "Export file", actionW, h, false)) {
         this.export(profile);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip("Save this profile as a .json file.\nWebhooks, macros, seeds and other private settings are not included.");
      }

      GuardedImGui.setCursorPos(pad + actionW + gap, y);
      if (AlbizWidgets.chipButton("##code", "Copy code", actionW, h, false)) {
         this.copyCode(profiles, profile);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip(
            "Copy a share code. Others load it with Paste code.\nWebhooks, macros, seeds and other private settings are not included."
         );
      }

      String deleteKey = "delete:" + key;
      boolean deleteArmed = this.isArmed(deleteKey);
      GuardedImGui.setCursorPos(pad + (actionW + gap) * 2.0F, y);
      if (dangerChip("##delete", deleteArmed ? "Sure?" : "Delete", actionW, h, deleteArmed) && this.confirm(deleteKey)) {
         this.delete(profiles, profile);
      }

      if (GuardedImGui.isItemHovered(65536)) {
         ClickGuiRenderer.showTooltip(deleteArmed ? "Click again to delete" : "Delete this profile");
      }

      y += h + AlbizWidgets.s(12.0F);
      return y - top;
   }

   private void load(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         Set<ConfigProfiles.Part> applied = profiles.load(profile.name());
         this.messages.accept("Loaded " + profile.name() + " (" + partList(applied) + ")", false);
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }
   }

   private void save(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         Set<ConfigProfiles.Part> saved = profiles.save(profile.name());
         this.messages.accept("Saved your current " + partList(saved) + " to " + profile.name(), false);
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }
   }

   private void rename(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         String renamed = profiles.rename(profile.name(), this.renameInput.get());
         if (renamed.equals(profile.name())) {
            return;
         }

         this.expanded = key(renamed);
         this.renameInput.set(renamed);
         this.messages.accept("Renamed " + profile.name() + " to " + renamed, false);
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }
   }

   private void setPart(ConfigProfiles profiles, ConfigProfiles.Profile profile, ConfigProfiles.Part part, boolean included) {
      try {
         profiles.setPart(profile.name(), part, included);
         this.messages
            .accept(included ? "Saved your current " + part.label + " into " + profile.name() : part.label + " is no longer part of " + profile.name(), false);
      } catch (ProfileException var6) {
         this.messages.accept(var6.getMessage(), true);
      }
   }

   private void addServer(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         String added = profiles.addServer(profile.name(), this.serverInput.get());
         this.serverInput.set("");
         this.messages.accept(profile.name() + " now loads when you join " + added, false);
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }

      this.focusServer = true;
   }

   private void removeServer(ConfigProfiles profiles, ConfigProfiles.Profile profile, String server) {
      try {
         profiles.removeServer(profile.name(), server);
         this.messages.accept(profile.name() + " no longer loads on " + server, false);
      } catch (ProfileException var5) {
         this.messages.accept(var5.getMessage(), true);
      }
   }

   private void export(ConfigProfiles.Profile profile) {
      if (FileDialogs.busy()) {
         this.messages.accept("A file window is already open", true);
      } else {
         String name = profile.name();
         FileDialogs.saveProfile("Export " + name, name + ".json", path -> {
            String fileName = path.getFileName().toString();
            Path target = fileName.toLowerCase(Locale.ROOT).endsWith(".json") ? path : path.resolveSibling(fileName + ".json");

            try {
               ConfigProfiles.getInstance().exportTo(name, target);
               this.messages.accept("Exported " + name + " to " + target.getFileName(), false);
            } catch (ProfileException var6) {
               this.messages.accept(var6.getMessage(), true);
            }
         });
      }
   }

   private void copyCode(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         String code = profiles.shareCode(profile.name());
         class_310.method_1551().field_1774.method_1455(code);
         this.messages
            .accept(
               code.length() > 2000 ? "Copied, but it is too long for Discord. Send the file instead" : "Share code for " + profile.name() + " copied", false
            );
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }
   }

   private void delete(ConfigProfiles profiles, ConfigProfiles.Profile profile) {
      try {
         profiles.delete(profile.name());
         if (key(profile.name()).equals(this.expanded)) {
            this.expanded = null;
         }

         this.messages.accept("Deleted " + profile.name(), false);
      } catch (ProfileException var4) {
         this.messages.accept(var4.getMessage(), true);
      }
   }

   private void toggle(String name) {
      String key = key(name);
      if (key.equals(this.expanded)) {
         this.expanded = null;
      } else {
         this.expanded = key;
         this.renameInput.set(name);
         this.serverInput.set("");
      }
   }

   private boolean isArmed(String key) {
      return key.equals(this.armed) && System.nanoTime() - this.armedAt < 3000000000L;
   }

   private boolean confirm(String key) {
      if (this.isArmed(key)) {
         this.armed = null;
         return true;
      } else {
         this.armed = key;
         this.armedAt = System.nanoTime();
         return false;
      }
   }

   private static boolean dangerChip(String id, String label, float w, float h, boolean armed) {
      float sx = GuardedImGui.getCursorScreenPosX();
      float sy = GuardedImGui.getCursorScreenPosY();
      boolean clicked = GuardedImGui.invisibleButton(id, w, h);
      float hover = AlbizWidgets.animate(GuardedImGui.getItemID(), GuardedImGui.isItemHovered(), 18.0F);
      int bg = armed ? -10856974 : AlbizTheme.lerpColor(-13226454, -12240588, hover);
      if (GuardedImGui.isItemActive()) {
         bg = AlbizTheme.lerpColor(bg, -16777216, 0.12F);
      }

      ImDrawList dl = GuardedImGui.getWindowDrawList();
      dl.addRectFilled(sx, sy, sx + w, sy + h, AlbizWidgets.c(bg), AlbizWidgets.s(7.0F));
      ImFont font = GuardedImGui.getFont();
      int text = armed ? -1 : AlbizTheme.lerpColor(-461582, -10856974, hover);
      AlbizWidgets.text(dl, font, sx + (w - AlbizWidgets.textWidth(font, label)) * 0.5F, sy + (h - AlbizWidgets.lineHeight(font)) * 0.5F, text, label);
      return clicked;
   }

   private static void label(String text, float x, float y, float h) {
      ImFont small = ImGuiManager.getInstance().smallFont();
      float sx = GuardedImGui.getWindowPosX() + x;
      float sy = GuardedImGui.getWindowPosY() - GuardedImGui.getScrollY() + y;
      AlbizWidgets.text(GuardedImGui.getWindowDrawList(), small, sx, sy + (h - AlbizWidgets.lineHeight(small)) * 0.5F, -4542808, text);
   }

   private static String summary(ConfigProfiles.Profile profile) {
      if (profile.broken()) {
         return "Can't be read - fix the file or delete it";
      } else {
         String parts = partList(profile.parts());
         List<String> servers = profile.servers();
         if (servers.size() == 1) {
            return parts + "  ·  loads on " + servers.get(0);
         } else {
            return servers.size() > 1 ? parts + "  ·  loads on " + servers.size() + " servers" : parts;
         }
      }
   }

   private static String partList(Set<ConfigProfiles.Part> parts) {
      StringBuilder text = new StringBuilder();

      for (ConfigProfiles.Part part : parts) {
         if (!text.isEmpty()) {
            text.append(", ");
         }

         text.append(part.label);
      }

      return text.isEmpty() ? "nothing" : text.toString();
   }

   private static String partTip(ConfigProfiles.Part part, boolean included) {
      String what = switch (part) {
         case MODULES -> "Module settings, keybinds and toggles.";
         case HUD -> "HUD layout and the HUD theme.";
         case THEME -> "GUI theme.";
         case FRIENDS -> "Your friends list.";
      };
      return what + (included ? "\nClick to remove it from this profile." : "\nClick to add it to this profile.");
   }

   private static String key(String name) {
      return name.toLowerCase(Locale.ROOT);
   }
}
