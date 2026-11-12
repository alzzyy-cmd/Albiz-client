package net.albiz.client.port.soberui;
import com.google.gson.*;import java.util.*;import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.minecraft.class_310;
/** Canonical Albiz settings/lifecycle for a reference-only visual renderer. */
public final class ReferenceVisualModule extends ClientModule{
 public final net.albiz.client.port.soberui.module.Module visual;private boolean syncing;
 private final Map<Setting,net.albiz.client.port.soberui.setting.Setting<?>> bindings=new LinkedHashMap<>();
 public ReferenceVisualModule(String id,net.albiz.client.port.soberui.module.Module target){super(id,target.name,target.description,ModuleCategory.VISUALS);visual=target;
  if(target instanceof net.albiz.client.port.soberui.module.Presets)addSetting("visual-picker",new ActionSetting("Visual Selection",()->class_310.method_1551().method_1507(new net.albiz.client.port.soberui.gui.VisualPresetsScreen(class_310.method_1551().field_1755,visual,this::sync))));
  int index=0;for(var s:target.getSettings()){Setting out=null;String name=s.name;
   if(s instanceof net.albiz.client.port.soberui.setting.BoolSetting b){if(name.equals("Open Folder")||name.equals("Reload Models"))out=new ActionSetting(name,()->{b.set(true);sync();});else out=new BooleanSetting(name,b.get());}
   else if(s instanceof net.albiz.client.port.soberui.setting.IntSetting n)out=new NumberSetting(name,n.get(),n.min,n.max).step(1);
   else if(s instanceof net.albiz.client.port.soberui.setting.EnumSetting<?> e){Enum<?> value=e.get();String[] choices=Arrays.stream(value.getDeclaringClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);out=new ModeSetting(name,value.name(),choices);}
   else if(s instanceof net.albiz.client.port.soberui.setting.KeySetting k)out=new KeySetting(name,k.get());
   else if(s instanceof net.albiz.client.port.soberui.setting.ColorSetting c)out=new ColorSetting(name,c.get().packed());
   else if(s instanceof net.albiz.client.port.soberui.setting.ItemSetting item)out=new StringSetting(name,net.minecraft.class_7923.field_41178.method_10221(item.get()).toString()).itemPicker();
   else if(s instanceof net.albiz.client.port.soberui.setting.StringSetting text)out=new StringSetting(name,text.get());
   if(out==null)continue;final Setting core=out;out.visibleWhen(s::isVisible);addSetting("option-"+index++,out);bindings.put(out,s);out.onChange(()->{if(!syncing){JsonElement value=core instanceof ColorSetting c?new JsonPrimitive(c.argb()):core.serialize();s.apply(value);}});
  }
 }
 public void sync(){syncing=true;try{for(var e:bindings.entrySet()){var core=e.getKey();if(core instanceof ColorSetting color && e.getValue() instanceof net.albiz.client.port.soberui.setting.ColorSetting c)color.setArgb(c.get().packed());else core.deserialize(e.getValue().save());}}finally{syncing=false;}if(visual.isActive()!=isEnabled())setEnabled(visual.isActive());}
 protected void onEnable(){visual.setEnabled(true);}protected void onDisable(){visual.setEnabled(false);}
}
