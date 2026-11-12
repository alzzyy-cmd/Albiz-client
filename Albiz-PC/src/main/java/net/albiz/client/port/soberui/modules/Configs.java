package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.ConfigShare;
import net.albiz.client.port.soberui.ConfigSlots;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.setting.StringSetting;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Configs
extends Module
implements Presets {
    public static final int SAVE = 0;
    public static final int ACTIVATE = 1;
    public static final int EXPORT = 2;
    public static final int RENAME = 3;
    public static final int DELETE = 4;
    public static final int IMPORT = 5;
    public static final int ADD = 6;
    public static final String[] TIPS = new String[]{"Saves your current modules, settings, keybinds and HUD positions into this config.", "Loads this config. All modules, settings, keybinds and HUD positions switch to it.", "Copies the local config text to your clipboard. No server upload.", "Renames this config. Type the new name and press Enter.", "Deletes this config. Press it twice to confirm.", "Copy exported Albız config text, then press Import.", "Saves your current setup as a new config."};
    private final Setting<Integer> selected = this.add(new IntSetting("Selected", "", 0, 0, 4)).visible(() -> false);
    private final Setting<Boolean> importConfig = this.add(new BoolSetting("Import", "Copy exported Albız config text, then press Import.", false)).onChanged(v -> this.press((boolean)v, this.importConfig, () -> this.importText(null)));
    private final Setting<Boolean> export = this.add(new BoolSetting("Share", "Copies the local config text to your clipboard. No server upload.", false)).onChanged(v -> this.press((boolean)v, this.export, this::exportSlot));
    private final Setting<Boolean> save = this.add(new BoolSetting("Save", "Saves all your current modules, settings, keybinds and HUD positions into the selected config.", false)).onChanged(v -> this.press((boolean)v, this.save, this::saveSlot));
    private final Setting<Boolean> activate = this.add(new BoolSetting("Activate", "Loads the selected config. All modules, settings, keybinds and HUD positions switch to it.", false)).onChanged(v -> this.press((boolean)v, this.activate, this::activateSlot));
    private final Setting<Boolean> delete = this.add(new BoolSetting("Delete", "Deletes the selected config. Press it twice to confirm.", false)).onChanged(v -> this.press((boolean)v, this.delete, this::deleteSlot));
    public final Setting<String> shareName = this.add(new StringSetting("Share Name", "Your own share code: type SOBER to share as SC-SOBER. Letters, numbers and _, 3 to 16 long. If someone else already uses the name, pick another. Empty gives a random number.", ""){

        @Override
        protected String validate(String value) {
            return ConfigShare.cleanName(value);
        }

        @Override
        public String display() {
            return ((String)this.value).isEmpty() ? "Random number" : "SC-" + (String)this.value;
        }
    });
    private final Setting<String> ownCodes = this.add(new StringSetting("Own Codes", "", "")).visible(() -> false);
    private final Setting<String> rename = this.add(new StringSetting("Rename", "Type a new name for the selected config and press Enter.", ConfigSlots.name(0))).onChanged(v -> ConfigSlots.rename(this.slot(), v));
    private int deleteSlot = -1;
    private long deleteUntil;
    private boolean busy;

    public Configs() {
        super("Configs", "Five local configs you can save, activate, rename, delete and share using clipboard export/import.", Category.CONFIG);
        this.shareName.visible(()->false);
    }

    public int slot() {
        return Math.max(0, Math.min(4, this.selected.get()));
    }

    public void select(int index) {
        this.selected.set(index);
        this.rename.set(ConfigSlots.name(index));
    }

    public boolean deleteArmed() {
        return this.deleteSlot == this.slot() && System.currentTimeMillis() < this.deleteUntil;
    }

    public String status(int slot) {
        if (ConfigSlots.empty(slot)) {
            return "Empty";
        }
        long time = ConfigSlots.savedAt(slot);
        String prefix = ConfigSlots.imported(slot) ? "Imported" : "Saved";
        return time > 0L ? prefix + " " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time)) : prefix;
    }

    private void press(boolean value, Setting<Boolean> setting, Runnable action) {
        if (!value) {
            return;
        }
        setting.set(false);
        action.run();
    }

    public void saveSlot() {
        ConfigSlots.save(this.slot());
        Notifications.push("Configs", "Saved your settings into " + ConfigSlots.name(this.slot()) + ".", Notifications.Type.SUCCESS);
    }

    public void activateSlot() {
        int slot = this.slot();
        boolean imported = ConfigSlots.imported(slot);
        if (!ConfigSlots.activate(slot)) {
            Notifications.push("Configs", ConfigSlots.name(slot) + " is empty. Press Save first.", Notifications.Type.WARNING);
            return;
        }
        this.rename.set(ConfigSlots.name(slot));
        Notifications.push("Configs", ConfigSlots.name(slot) + " is now active.", Notifications.Type.SUCCESS);
        if (imported) {
            Notifications.push("Configs", "Auction Sniper, Auto Sell, Auto Shop, RTP Method and Panic Order kept your own settings, because they send commands.", Notifications.Type.INFO);
        }
    }

    public void deleteSlot() {
        int slot = this.slot();
        String name = ConfigSlots.name(slot);
        if (ConfigSlots.empty(slot) && name.equals("Config " + (slot + 1))) {
            Notifications.push("Configs", name + " is already empty.", Notifications.Type.INFO);
            return;
        }
        if (!this.deleteArmed()) {
            this.deleteSlot = slot;
            this.deleteUntil = System.currentTimeMillis() + 3000L;
            Notifications.push("Configs", "Press Delete again to delete " + name + ".", Notifications.Type.WARNING);
            return;
        }
        this.deleteSlot = -1;
        this.deleteUntil = 0L;
        ConfigSlots.delete(slot);
        this.rename.set(ConfigSlots.name(slot));
        Notifications.push("Configs", name + " was deleted.", Notifications.Type.SUCCESS);
    }

    public void renameSlot(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        ConfigSlots.rename(this.slot(), name);
        this.rename.set(ConfigSlots.name(this.slot()));
        SoberClient.CONFIG.save();
    }

    public void run(int action) {
        switch (action) {
            case 0: {
                this.saveSlot();
                break;
            }
            case 1: {
                this.activateSlot();
                break;
            }
            case 2: {
                this.exportSlot();
                break;
            }
            case 4: {
                this.deleteSlot();
                break;
            }
            case 6: {
                this.addConfig();
                break;
            }
            case 5: {
                this.importText(null);
                break;
            }
        }
    }

    public boolean busy() {
        return this.busy;
    }

    public void addConfig() {
        int slot = ConfigSlots.firstEmpty();
        if (slot < 0) {
            Notifications.push("Configs", "All 5 configs are used. Delete one first.", Notifications.Type.WARNING);
            return;
        }
        this.select(slot);
        this.saveSlot();
    }

    public void exportSlot(){String json=ConfigSlots.export(this.slot());if(json==null){Notifications.push("Configs","This slot is empty. Save first.",Notifications.Type.WARNING);return;}mc.field_1774.method_1455(json);Notifications.push("Configs","Local config copied to clipboard. No upload is performed.",Notifications.Type.SUCCESS);}

    public void importText(String text) {
        String source = text == null || text.isBlank() ? Configs.mc.field_1774.method_1460() : text;
        int slot = ConfigSlots.firstEmpty();
        if (slot < 0) {
            Notifications.push("Configs", "All 5 configs are used. Delete one first.", Notifications.Type.WARNING);
            return;
        }
        String code = ConfigShare.shortCode(source);
        if (code == null) {
            this.store(slot, source);
            return;
        }
        if (this.busy) {
            return;
        }
        this.busy = true;
        Notifications.push("Configs", "Loading SC-" + code + "...", Notifications.Type.INFO);
        ConfigShare.download(code).whenComplete((json, error) -> mc.execute(() -> {
            this.busy = false;
            if (error == null) {
                this.store(slot, (String)json);
            } else {
                Notifications.push("Configs", Configs.cause(error), Notifications.Type.ERROR);
            }
        }));
    }

    private Set<String> ownCodes() {
        HashSet<String> codes = new HashSet<String>();
        for (String part : this.ownCodes.get().split(",")) {
            if (part.isBlank()) continue;
            codes.add(part.strip());
        }
        return codes;
    }

    private void store(int slot, String source) {
        try {
            String name = ConfigSlots.importJson(slot, source);
            this.select(slot);
            Notifications.push("Configs", "Imported " + name + ". Press Load to use it.", Notifications.Type.SUCCESS);
        }
        catch (IllegalArgumentException e) {
            Notifications.push("Configs", e.getMessage(), Notifications.Type.ERROR);
        }
        catch (RuntimeException e) {
            Notifications.push("Configs", "This config could not be read.", Notifications.Type.ERROR);
        }
    }

    private static Throwable root(Throwable error) {
        Throwable root;
        for (root = error; root.getCause() != null && root != root.getCause(); root = root.getCause()) {
        }
        return root;
    }

    private static String cause(Throwable error) {
        String message = Configs.root(error).getMessage();
        return message == null || message.isBlank() ? "Something went wrong." : message;
    }

    @Override
    public void onEnable() {
        this.setEnabledSilently(false);
    }

    @Override
    public int presetCount() {
        return 5;
    }

    @Override
    public String presetName(int index) {
        String name = ConfigSlots.name(index);
        if (ConfigSlots.active() == index) {
            return name + " *";
        }
        return ConfigSlots.empty(index) ? name + " (empty)" : name;
    }

    @Override
    public int presetColor(int index) {
        return ConfigSlots.empty(index) ? -9801856 : -2762274;
    }

    @Override
    public boolean presetActive(int index) {
        return index == this.slot();
    }

    @Override
    public void selectPreset(int index) {
        this.select(index);
    }

    @Override
    public String presetDescription() {
        return "Pick a config slot, then use Save, Activate, Rename, Delete, Export or Import below. * marks the active config.";
    }

    @Override
    public String getInfoString() {
        int active = ConfigSlots.active();
        return active >= 0 ? ConfigSlots.name(active) : null;
    }

    @Override
    public boolean showInHud() {
        return false;
    }
}

