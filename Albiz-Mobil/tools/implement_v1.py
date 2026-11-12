from pathlib import Path
root=Path('src/main/java/net/albiz/client')
def edit(f,a,b):
 p=root/f;s=p.read_text();assert a in s,(f,a);p.write_text(s.replace(a,b))
edit(Path('module/BuiltinModuleProvider.java'),'new CustomCrosshairModule(),','new CustomCrosshairModule(),\n            new ChunkScannerModule(),\n            new net.albiz.client.module.impl.render.MotionScannerModule(),\n            new net.albiz.client.module.impl.render.ActivityInspectorModule(),\n            new net.albiz.client.port.soberui.adapter.NativeModuleAdapter("glass",new net.albiz.client.port.soberui.modules.Glass()),') if False else None
# Native visual modules installed through existing registry, below.
edit(Path('module/BuiltinModuleProvider.java'),'new CustomCrosshairModule(),','new CustomCrosshairModule(),\n            new ChunkScannerModule(),\n            new net.albiz.client.module.impl.render.MotionScannerModule(),\n            new net.albiz.client.module.impl.render.ActivityInspectorModule(),')
edit(Path('module/impl/render/CustomCrosshairModule.java'),'"Ring Cross"))','"Ring Cross", "Wolf"))')
edit(Path('module/impl/render/CustomCrosshairModule.java'),'case "Dot":\n','case "Wolf":\n            net.albiz.client.gui.mainmenu.MenuIcons.wolf(dl,g.cx(),g.cy(),g.size()+g.open()+grow,color,g.thick()+grow*2);\n            break;\n         case "Dot":\n')
edit(Path('config/ThemeStore.java'),'"Language", "tr_tr"','"Language", "en_us"')
edit(Path('config/ThemeStore.java'),'version <= 6','version <= 7')
edit(Path('config/ThemeStore.java'),'LOGGER.warn("[Albiz] Theme version {} is newer than supported {}; skipping load", version, 6);','LOGGER.warn("[Albiz] Theme version {} is newer than supported {}; skipping load", version, 7);')
edit(Path('config/ThemeStore.java'),'this.language.set("tr_tr");','this.language.set("en_us");')
edit(Path('config/ThemeStore.java'),'if (version < 6) {','if (version < 7) {')
edit(Path('config/ThemeStore.java'),'root.addProperty("version", 6);','root.addProperty("version", 7);')
# Durable profile marker, independent from launcher OS.
p=root/'platform/BuildProfile.java';p.write_text('''package net.albiz.client.platform;
public final class BuildProfile {
 private static final boolean MOBILE = load();
 private BuildProfile() {}
 private static boolean load(){try(var in=BuildProfile.class.getResourceAsStream("/albiz-profile.properties")){var p=new java.util.Properties();if(in!=null)p.load(in);return "mobile".equals(p.getProperty("profile"));}catch(java.io.IOException e){return false;}}
 public static boolean mobile(){return MOBILE;}
}
''')
edit(Path('platform/PlatformInfo.java'),'default -> DETECTED;','default -> DETECTED || BuildProfile.mobile();')
edit(Path('module/impl/client/PerformanceModule.java'),'return module == null ? 2000000L : (long)(module.scanBudget.get() * 1000000.0F);','long requested = module == null ? 2000000L : (long)(module.scanBudget.get() * 1000000.0F);\n      return net.albiz.client.platform.BuildProfile.mobile() ? Math.min(requested, 750000L) : requested;')
# Fix persistence typo in existing native editor without changing IDs.
edit(Path('port/soberui/gui/HudLayout.java'),'int n = o.has("anchorX") ?','p.anchorX = o.has("anchorX") ?')
