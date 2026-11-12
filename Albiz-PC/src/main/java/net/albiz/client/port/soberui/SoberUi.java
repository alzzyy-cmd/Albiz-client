package net.albiz.client.port.soberui;
import net.albiz.client.port.soberui.gui.*;import net.albiz.client.port.soberui.modules.*;import net.albiz.client.module.ModuleRegistry;import net.minecraft.*;
/** Wires only the reference presentation layer to the existing Albiz client. */
public final class SoberUi {
 private static volatile boolean ready;private static boolean initializing;private static Object world;
 private static boolean visualsInstalled;
 public static synchronized void installVisuals(){if(visualsInstalled)return;visualsInstalled=true;
  var glass=new Glass();SoberClient.MODULES.addHidden(glass);net.albiz.client.module.ModuleRegistry.add(new ReferenceVisualModule("glass",glass));
  var models=new CustomModels();var picks=new CsPickaxe();SoberClient.MODULES.addHidden(models);SoberClient.MODULES.addHidden(picks);
  net.albiz.client.module.ModuleRegistry.add(new ReferenceVisualModule("custom-item-models",models));net.albiz.client.module.ModuleRegistry.add(new ReferenceVisualModule("cs-pickaxe",picks));
 }
 public static boolean customTitleEnabled(){boolean enabled=net.albiz.client.gui.AlbizTitleScreen.enabled();var menu=SoberClient.MODULES.get(MainMenu.class);if(menu!=null&&menu.isActive()!=enabled)menu.setEnabledSilently(enabled);return enabled&&MenuScreen.enabled();}
 public static boolean isReady(){return ready;}
 public static synchronized void init(){if(ready||initializing)return;initializing=true;try{
  var m=SoberClient.MODULES;var gui=new ClickGui();m.add(gui);gui.migrate();gui.accent.set(net.albiz.client.port.soberui.render.Color.fromPacked(net.albiz.client.config.ThemeStore.get().accent.argb()));gui.panelColor.set(new net.albiz.client.port.soberui.render.Color(24,16,22,255));gui.setKey(344);
  var menu=new MainMenu();menu.migrate();menu.matchSky.set(false);m.add(menu);m.add(new Hud());m.add(new InventoryHud());m.add(new TargetHud());m.add(new Configs());m.addHidden(new Sky());m.addHidden(new StaffLive());m.addHidden(new net.albiz.client.port.soberui.modules.RegionMap());
  for(var target:ModuleRegistry.all()){
   if(target instanceof net.albiz.client.module.impl.render.CustomTotemsModule t)m.add(new TotemView(t));
   else if(!target.id().equals("hud"))m.add(new AlbizModuleView(target));
  }
  SoberClient.CONFIG.load();
  net.albiz.client.event.impl.ModuleToggleEvent.TYPE.listen(SoberUi.class,e->Notifications.push(e.module.name(),e.enabled?"Enabled":"Disabled",Notifications.Type.INFO));
  /* PacketAppliedEvent is only dispatched for seven packet types (see MixinClientPacketListener); damage, sound and time packets never reached this listener. PacketReceiveEvent delivers every clientbound packet on the client thread. */
  net.albiz.client.event.impl.PacketReceiveEvent.TYPE.listen(SoberUi.class,e->{if(e.packet instanceof class_8143 damage)TargetHud.onDamage(damage.comp_1267(),damage.comp_1269()>=0?damage.comp_1269():damage.comp_1270());if(e.packet instanceof class_2767 sound)net.albiz.client.port.soberui.fx.CombatFx.onSound(sound);if(e.packet instanceof class_2761 time)HudMetrics.onWorldTimeUpdate(time);});
  ready=true;
  }finally{initializing=false;}
 }
 public static class_437 menu(){init();return SoberClient.MODULES.get(ClickGui.class).style.get()==ClickGui.Style.Dropdown?new DropdownScreen():new PanelScreen();}
 public static class_437 title(){init();return switch(SoberClient.MODULES.get(MainMenu.class).style.get()){case Home->new HomeScreen();case VanillaPlus->new VanillaPlusScreen();case Island,Classic->new MenuScreen();default->new SoberScreen();};}
 public static boolean isMenu(class_437 s){return s instanceof ModuleMenu;}
 public static void tick(){if(!ready)return;var mc=class_310.method_1551();if(world!=mc.field_1687){world=mc.field_1687;HudMetrics.reset();net.albiz.client.port.soberui.fx.CombatFx.reset();SoberClient.MODULES.get(TargetHud.class).onGameLeft();}var picks=SoberClient.MODULES.get(CsPickaxe.class);if(picks!=null&&picks.isActive())picks.onTick();net.albiz.client.port.soberui.fx.CombatFx.tick();HudMetrics.tick(mc);SoberClient.MODULES.get(Hud.class).onTick();SoberClient.MODULES.get(TargetHud.class).onTick();}
 public static void render(class_332 context,class_9779 tick){init();TargetCard.render(context,tick.method_60637(true));HudRenderer.render(context,tick);}
 public static void save(){if(ready)SoberClient.CONFIG.save();}
}
