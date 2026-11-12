package net.albiz.client.port.soberui;
import com.google.gson.*;import java.nio.file.*;import java.util.function.Predicate;import net.albiz.client.port.soberui.module.Module;
/** Compatibility surface for selected UI code; NOT the upstream client initializer. */
public final class SoberClient {
 public static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("Albiz-UI");
 public static final UiModules MODULES=new UiModules();public static final UiConfig CONFIG=new UiConfig();
 public static final class UiConfig{
  private Path path(){return net.albiz.client.config.ConfigMigration.root(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()).resolve("sober-ui.json");}
  private boolean secret(Module m,net.albiz.client.port.soberui.setting.Setting<?> setting){return m instanceof AlbizModuleView view&&view.target.settings().stream().anyMatch(s->s.isSecret()&&s.name().equals(setting.name));}
  public JsonObject toJson(){JsonObject out=new JsonObject();for(Module m:MODULES.all()){JsonObject v=new JsonObject(),s=new JsonObject();v.addProperty("enabled",m.isActive());v.addProperty("key",m.getKey());for(var setting:m.getSettings())if(!secret(m,setting))s.add(setting.name,setting.save());v.add("settings",s);out.add(m.name,v);}return out;}
  public void apply(JsonObject json,Predicate<Module> skip,Predicate<Module> skipEnabled){for(Module m:MODULES.all()){if(skip.test(m)||!json.has(m.name))continue;JsonObject v=json.getAsJsonObject(m.name);if(v.has("settings")){var s=v.getAsJsonObject("settings");for(var setting:m.getSettings())if(!secret(m,setting)&&s.has(setting.name))setting.apply(s.get(setting.name));}if(v.has("key"))m.setKey(v.get("key").getAsInt());if(!skipEnabled.test(m)&&v.has("enabled"))m.setEnabled(v.get("enabled").getAsBoolean());}}
  public void save(){try{Files.createDirectories(path().getParent());Path tmp=path().resolveSibling("sober-ui.json.tmp");JsonObject all=new JsonObject();all.add("modules",toJson());all.add("hud",net.albiz.client.port.soberui.gui.HudLayout.toJson());Files.writeString(tmp,new GsonBuilder().setPrettyPrinting().create().toJson(all));Files.move(tmp,path(),StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){LOG.warn("UI settings save failed",e);}}
  public void load(){try{if(Files.isRegularFile(path())){JsonObject root=JsonParser.parseString(Files.readString(path())).getAsJsonObject();apply(root.getAsJsonObject("modules"),m->m instanceof AlbizModuleView || m instanceof TotemView,m->m instanceof net.albiz.client.port.soberui.modules.ClickGui);if(root.has("hud"))net.albiz.client.port.soberui.gui.HudLayout.fromJson(root.getAsJsonObject("hud"));}}catch(Exception e){LOG.warn("UI settings load failed; retaining defaults",e);}}
 }
}
