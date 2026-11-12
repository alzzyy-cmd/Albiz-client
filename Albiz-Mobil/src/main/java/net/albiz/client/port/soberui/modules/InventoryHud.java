// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.Setting;

public final class InventoryHud
extends Module {
    public final Setting<Boolean> emptySlots = this.add(new BoolSetting("Empty Slots", "Show subtle cells for empty slots.", true));

    public InventoryHud() {
        super("Inventory HUD", "Movable inventory grid for the HUD editor.", Category.CLIENT);
    }

    @Override
    public boolean showInHud() {
        return false;
    }
}

