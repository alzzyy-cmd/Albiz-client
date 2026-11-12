package net.albiz.client.port.soberui;
import java.nio.file.*;import net.albiz.client.port.soberui.gui.HudLayout;import net.albiz.client.port.soberui.gui.Notifications;import net.albiz.client.port.soberui.modules.*;
/** A visual reset must never silently erase the original modules, hotkeys or credentials. */
public final class ResetAll{public static void run(){
 try{Path root=net.albiz.client.config.ConfigMigration.root(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()),file=root.resolve("sober-ui.json");if(Files.isRegularFile(file)){Path backup=root.resolve("albiz-ui-backups");Files.createDirectories(backup);Files.copy(file,backup.resolve("ui-"+System.currentTimeMillis()+".json"));}
 for(var module:SoberClient.MODULES.all())if(!(module instanceof AlbizModuleView)&&!(module instanceof TotemView)&&!(module instanceof Configs))for(var setting:module.getSettings())setting.reset();
 var gui=SoberClient.MODULES.get(ClickGui.class);gui.migrate();gui.accent.set(net.albiz.client.port.soberui.render.Color.fromPacked(net.albiz.client.config.ThemeStore.get().accent.argb()));gui.panelColor.set(new net.albiz.client.port.soberui.render.Color(24,16,22,255));SoberClient.MODULES.get(MainMenu.class).matchSky.set(false);HudLayout.reset();SoberClient.CONFIG.save();Notifications.push("Albız","Menu/HUD options reset. Existing modules and credentials preserved.",Notifications.Type.SUCCESS);
 }catch(Exception e){SoberClient.LOG.error("UI reset failed",e);Notifications.push("Albız","UI reset failed; see log.",Notifications.Type.ERROR);}
}}
