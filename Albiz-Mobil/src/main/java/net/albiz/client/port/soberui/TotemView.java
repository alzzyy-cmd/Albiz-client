package net.albiz.client.port.soberui;
import net.albiz.client.module.impl.render.CustomTotemsModule;import net.albiz.client.module.setting.ModeSetting;
public final class TotemView extends net.albiz.client.port.soberui.modules.CustomTotems{
 private net.albiz.client.port.soberui.setting.Setting<String> saved;
 private final CustomTotemsModule target;private final ModeSetting actual;
 public TotemView(CustomTotemsModule target){this.target=target;actual=(ModeSetting)target.settings().stream().filter(s->s.id().equals("style")).findFirst().orElseThrow();saved=new net.albiz.client.port.soberui.setting.StringSetting("Style","",actual.get()){@Override public String get(){return actual.get();}@Override public com.google.gson.JsonElement save(){return actual.serialize();}@Override public void load(com.google.gson.JsonElement e){actual.deserialize(e);}};saved.visible(()->false);}
 @Override public java.util.List<net.albiz.client.port.soberui.setting.Setting<?>> getSettings(){return saved==null?super.getSettings():java.util.List.of(saved);}
 @Override public boolean isActive(){return target!=null&&target.isEnabled();}@Override public void setEnabled(boolean value){target.setEnabled(value);}@Override public void toggle(){target.toggle();}
 @Override public int getKey(){return target.keybind();}@Override public void setKey(int key){target.setKeybind(key);}
 @Override public boolean presetActive(int index){return actual.is(presetName(index));}
 @Override public void selectPreset(int index){actual.set(presetName(index));if(index>0)target.setEnabled(true);}
}
