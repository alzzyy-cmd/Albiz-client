package net.albiz.client.port.soberui;
import java.util.*;import net.minecraft.*;import com.google.gson.*;import net.albiz.client.port.soberui.setting.*;
/** Registry-thumbnail selectors backed directly by canonical Albiz settings. */
final class NativeSelections{
 private static String id(class_1792 item){return class_7923.field_41178.method_10221(item).toString();}
 private static String id(class_2248 block){return class_7923.field_41175.method_10221(block).toString();}
 private static int rgb(float[] c){return (Math.round(c[0]*255)<<16)|(Math.round(c[1]*255)<<8)|Math.round(c[2]*255);}
 private static void rgb(float[] c,Integer v){if(v!=null){c[0]=((v>>16)&255)/255f;c[1]=((v>>8)&255)/255f;c[2]=(v&255)/255f;}}
 static BlockListSetting blocks(net.albiz.client.module.setting.BlockColorListSetting s){return new BlockListSetting(s.name(),""){
  public Set<class_2248> get(){return s.enabledBlocks();}
  public boolean allows(class_2248 b){return s.entry(id(b))!=null;}
  public void set(Set<class_2248> chosen){for(var e:s.entries()){var key=class_2960.method_12829(e.id());var b=key==null?null:class_7923.field_41175.method_63535(key);if(chosen.contains(b))s.select(e);else s.deselect(e);}s.notifyChanged();}
  public Integer color(class_2248 b){return s.colorArgb(b)&0xffffff;}
  public void setColor(class_2248 b,Integer c){var e=s.entry(id(b));if(e!=null){rgb(e.color(),c);s.notifyChanged();}}
  public JsonElement save(){return s.serialize();}public void load(JsonElement e){s.deserialize(e);}
 };}
 static ItemListSetting items(net.albiz.client.module.setting.ItemColorListSetting s){return new ItemListSetting(s.name(),""){
  public boolean allows(class_1792 item){return s.entries().stream().anyMatch(e->e.id().equals(id(item)));}
  public Set<class_1792> get(){var out=new LinkedHashSet<class_1792>();for(var e:s.entries())if(e.enabled().get()){var k=class_2960.method_12829(e.id());if(k!=null&&class_7923.field_41178.method_10250(k))out.add(class_7923.field_41178.method_63535(k));}return out;}
  public void set(Set<class_1792> chosen){for(var e:s.entries()){var k=class_2960.method_12829(e.id());e.enabled().set(k!=null&&chosen.contains(class_7923.field_41178.method_63535(k)));}s.notifyChanged();}
  public Integer color(class_1792 item){return s.entries().stream().filter(e->e.id().equals(id(item))).findFirst().map(e->rgb(e.color())).orElse(null);}
  public void setColor(class_1792 item,Integer c){s.entries().stream().filter(e->e.id().equals(id(item))).forEach(e->rgb(e.color(),c));s.notifyChanged();}
  public JsonElement save(){return s.serialize();}public void load(JsonElement e){s.deserialize(e);}
 };}
 private static Set<class_1792> resolve(List<String> ids){var out=new LinkedHashSet<class_1792>();for(String name:ids){var k=class_2960.method_12829(name);if(k!=null&&class_7923.field_41178.method_10250(k))out.add(class_7923.field_41178.method_63535(k));}return out;}
 static ItemListSetting items(net.albiz.client.module.setting.StringListSetting s){return new ItemListSetting(s.name(),""){
  public Set<class_1792> get(){return resolve(s.get());}public void set(Set<class_1792> chosen){var a=new JsonArray();chosen.stream().map(NativeSelections::id).forEach(a::add);s.deserialize(a);}
  public JsonElement save(){return s.serialize();}public void load(JsonElement e){s.deserialize(e);}
 };}
 static ItemListSetting item(net.albiz.client.module.setting.StringSetting s){return new ItemListSetting(s.name(),""){
  public Set<class_1792> get(){return resolve(List.of(s.get()));}public void toggle(class_1792 item){s.set(id(item));}public void clear(){s.set("");}
  public JsonElement save(){return s.serialize();}public void load(JsonElement e){s.deserialize(e);}
 };}
}
