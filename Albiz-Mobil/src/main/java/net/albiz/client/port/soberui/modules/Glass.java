// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.mixin.GameRendererAccessor;
import net.albiz.client.port.soberui.mixin.GuiRendererAccessor;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.render.Color;
import net.albiz.client.port.soberui.render.GlassGear;
import net.albiz.client.port.soberui.render.GlassLayers;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.ColorSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.minecraft.class_10055;
import net.minecraft.class_11228;
import net.minecraft.class_1921;
import net.minecraft.class_310;
import net.minecraft.class_4722;
import net.minecraft.class_7920;
import net.minecraft.class_811;

public class Glass
extends Module {
    public final Setting<Boolean> self = this.add(new BoolSetting("Self", "Turns you into glass.", true));
    public final Setting<Boolean> others = this.add(new BoolSetting("Other Players", "Turns other players into glass.", true));
    public final Setting<Boolean> gear = this.add(new BoolSetting("Armor & Elytra", "Armor, elytras and capes of glass players are glass too.", true));
    public final Setting<Boolean> items = this.add(new BoolSetting("Items", "Items in the world are glass too: held items, dropped items and item frames.", false)).onChanged(v -> Glass.refreshItems());
    public final Setting<Boolean> inventory = this.add(new BoolSetting("Inventory", "Items in your inventory, hotbar and menus are glass too.", false)).onChanged(v -> Glass.refreshItems());
    public final Setting<Color> tint = this.add(new ColorSetting("Tint", "Color of the glass.", new Color(226, 238, 255, 255))).onChanged(v -> Glass.refreshItems());

    public Glass() {
        super("Glass", "Players, their armor and all items made of clear glass with glowing edges. Only you can see it.", Category.VISUALS);
    }

    @Override
    public void onEnable() {
        Glass.refreshItems();
    }

    @Override
    public void onDisable() {
        Glass.refreshItems();
    }

    private static void refreshItems() {
        class_310 client = class_310.method_1551();
        if (client == null) {
            return;
        }
        client.execute(() -> {
            if (client.field_1773 == null) {
                return;
            }
            class_11228 gui = ((GameRendererAccessor)client.field_1773).albizGlass$getGuiRenderer();
            if (gui == null) {
                return;
            }
            ((GuiRendererAccessor)gui).albizGlass$resetItemAtlas();
            ((GuiRendererAccessor)gui).albizGlass$clearOversizedItems();
        });
    }

    private static Glass active() {
        Glass module = SoberClient.MODULES.get(Glass.class);
        return module != null && module.isActive() ? module : null;
    }

    public static boolean applies(Object state) {
        if (!(state instanceof class_10055)) {
            return false;
        }
        class_10055 player = (class_10055)state;
        Glass module = Glass.active();
        if (module == null) {
            return false;
        }
        boolean own = Glass.mc.field_1724 != null && player.field_53528 == Glass.mc.field_1724.method_5628();
        return own ? module.self.get() : module.others.get();
    }

    public static class_1921 layer(class_10055 state) {
        return GlassLayers.player(state.field_53520.comp_1629() == class_7920.field_41122);
    }

    public static int tint() {
        Glass module = Glass.active();
        return 0xFF000000 | (module == null ? 0xE2EEFF : module.tint.get().packed() & 0xFFFFFF);
    }

    public static boolean glassGear(Object state) {
        Glass module = Glass.active();
        return module != null && module.gear.get() != false && Glass.applies(state);
    }

    public static void gear(boolean on) {
        GlassGear.set(on);
    }

    public static class_1921 item(class_1921 layer, class_811 context) {
        boolean gui;
        Glass module = Glass.active();
        if (module == null) {
            return layer;
        }
        boolean bl = gui = context == class_811.field_4317;
        if (gui ? module.inventory.get() == false : module.items.get() == false) {
            return layer;
        }
        if (layer == class_4722.method_29382()) {
            return GlassLayers.item(false, gui);
        }
        if (layer == class_4722.method_76545() || layer == class_4722.method_24074() || layer == class_4722.method_24073()) {
            return GlassLayers.item(true, gui);
        }
        return layer;
    }
}

