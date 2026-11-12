package net.albiz.client.port.soberui;
import com.google.gson.*;import net.albiz.client.module.ClientModule;import net.albiz.client.port.soberui.module.Category;import net.albiz.client.port.soberui.module.Module;import net.albiz.client.port.soberui.setting.*;
/** A view of an existing Albiz module, never a second running implementation. */
public class AlbizModuleView extends Module {
 public final ClientModule target;
 public AlbizModuleView(ClientModule target){super(target.name(),target.description(),categoryOf(target));this.target=target;for(var s:target.settings()){var v=control(s);if(v!=null){v.visible(s::isVisible);add(v);}}}
 static Category categoryOf(ClientModule m){return switch(m.category().name()){case "COMBAT"->Category.COMBAT;case "MOVEMENT"->Category.MOVEMENT;case "BASE_RAID"->Category.BASE_FINDING;case "CLIENT"->Category.CLIENT;case "RENDER"->Category.RENDER;case "VISUALS"->Category.VISUALS;case "DONUT_MACROS"->Category.MACRO;default->Category.DONUT;};}
 private Setting<?> control(net.albiz.client.module.setting.Setting s){String n=s.name();
  if(s instanceof net.albiz.client.module.setting.BooleanSetting b)return new BoolSetting(n,"",b.get()){@Override public Boolean get(){return b.get();}@Override public void set(Boolean v){super.set(v);b.set(v);}@Override public void toggle(){b.set(!b.get());}@Override public JsonElement save(){return b.serialize();}@Override public void load(JsonElement e){b.set(e.getAsBoolean());}};
  if(s instanceof net.albiz.client.module.setting.NumberSetting num){int f=(int)Math.pow(10,Math.min(3,num.decimals()));return new IntSetting(n,"",Math.round(num.get()*f),Math.round(num.min()*f),Math.round(num.max()*f)){@Override public Integer get(){return Math.round(num.get()*f);}@Override public void set(Integer v){super.set(v);num.set(v/(float)f);}@Override public String display(){return String.format(java.util.Locale.ROOT,"%."+num.decimals()+"f",num.get());}@Override public JsonElement save(){return new JsonPrimitive(get());}@Override public void load(JsonElement e){set(e.getAsInt());}};}
  if(s instanceof net.albiz.client.module.setting.ModeSetting mode)return new EnumSetting<Choice>(n,"",Choice.VALUE){@Override public void cycle(boolean forward){var modes=mode.modes();int index=modes.indexOf(mode.get());mode.set(modes.get(Math.floorMod(index+(forward?1:-1),modes.size())));}@Override public String display(){return mode.get();}@Override public JsonElement save(){return mode.serialize();}@Override public void load(JsonElement e){mode.deserialize(e);}};
  if(s instanceof net.albiz.client.module.setting.ColorSetting color)return new ColorSetting(n,"",net.albiz.client.port.soberui.render.Color.fromPacked(color.argb())){@Override public net.albiz.client.port.soberui.render.Color get(){return net.albiz.client.port.soberui.render.Color.fromPacked(color.argb());}@Override public void set(net.albiz.client.port.soberui.render.Color v){super.set(v);color.setArgb(v.packed());}@Override public void load(JsonElement e){super.load(e);color.setArgb(super.get().packed());}};
  if(s instanceof net.albiz.client.module.setting.BlockColorListSetting blocks)return NativeSelections.blocks(blocks);
  if(s instanceof net.albiz.client.module.setting.ItemColorListSetting items)return NativeSelections.items(items);
  if(s instanceof net.albiz.client.module.setting.StringListSetting list && list.isItemPicker())return NativeSelections.items(list);
  if(s instanceof net.albiz.client.module.setting.StringListSetting list)return new StringSetting(n,"Separate entries with |.",String.join(" | ",list.get()),s.isSecret()){@Override public String get(){return String.join(" | ",list.get());}@Override public void set(String value){list.deserialize(new JsonPrimitive(value));}@Override public String display(){return s.isSecret()?"********":get();}@Override public JsonElement save(){return list.serialize();}@Override public void load(JsonElement e){list.deserialize(e);}};
  if(s instanceof net.albiz.client.module.setting.StringSetting str && str.isItemPicker())return NativeSelections.item(str);
  if(s instanceof net.albiz.client.module.setting.KeySetting key)return new KeySetting(n,"",key.value()){@Override public Integer get(){return key.value();}@Override public void bind(int v){super.bind(v);key.setValue(v);}@Override public JsonElement save(){return key.serialize();}@Override public void load(JsonElement e){key.deserialize(e);super.bind(key.value());}};
  if(s instanceof net.albiz.client.module.setting.StringSetting str)return new StringSetting(n,"",str.get(),s.isSecret()){@Override public String get(){return str.get();}@Override public void set(String value){super.set(value);str.set(value);}@Override public String display(){return str.isSecret()?"*".repeat(Math.min(12,str.get().length())):str.get();}@Override public JsonElement save(){return str.serialize();}@Override public void load(JsonElement e){str.deserialize(e);}};
  // Preserve complex item/block/icon editors instead of degrading them to a text field.
  return new LegacyEditorSetting(s);
 }
 public enum Choice{VALUE}
 @Override public boolean isActive(){return target.isEnabled();}@Override public void setEnabled(boolean v){target.setEnabled(v);}@Override public void toggle(){if(target.isAction())target.onClick();else target.toggle();}
 @Override public int getKey(){return target.keybind();}@Override public void setKey(int key){target.setKeybind(key);}@Override public boolean showInHud(){return !target.isAction();}
 public static final class LegacyEditorSetting extends StringSetting{
  public final net.albiz.client.module.setting.Setting original;
  LegacyEditorSetting(net.albiz.client.module.setting.Setting s){super(s.name(),"Open the existing visual item/block editor.","Open visual editor");original=s;}
 }
}
