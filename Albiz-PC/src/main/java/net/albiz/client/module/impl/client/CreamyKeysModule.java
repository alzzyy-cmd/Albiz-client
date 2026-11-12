package net.albiz.client.module.impl.client;
import java.util.*;import net.albiz.client.event.*;import net.albiz.client.event.impl.*;import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.minecraft.*;
/** Playback via Minecraft, never javax.sound.sampled or a replacement native library. */
public final class CreamyKeysModule extends ClientModule {
 private final ModeSetting profile=addSetting("profile",new ModeSetting("Keyboard","Aula F75","Aula F75","FB67","60HE V2","Frosted 65"));
 private final BooleanSetting inGame=addSetting("in-game",new BooleanSetting("In Game",true));
 private final BooleanSetting movement=addSetting("movement",new BooleanSetting("Movement Keys",true));
 private final BooleanSetting release=addSetting("release",new BooleanSetting("Release Sound",false));
 private final BooleanSetting mouse=addSetting("mouse",new BooleanSetting("Mouse Clicks",true));
 private final NumberSetting volume=addSetting("volume",new NumberSetting("Volume",.7f,0,1).step(.05f));
 private final NumberSetting cooldown=addSetting("cooldown",new NumberSetting("Cooldown",70,0,300).step(10));
 private final Set<Integer> down=new HashSet<>(),buttons=new HashSet<>();private long lastKey,lastMouse;
 public CreamyKeysModule(){super("creamy-keys","Creamy Keys","Recorded mechanical keyboard and mouse sounds played by Minecraft's sound engine.",ModuleCategory.CLIENT);}
 protected void onEnable(){down.clear();buttons.clear();lastKey=lastMouse=0;}
 protected void onDisable(){down.clear();buttons.clear();}
 @EventHandler public void key(KeyInputEvent e){
  boolean up=e.action==0;if(up){if(!down.remove(e.key)||!release.get())return;}else if(e.action!=1||!down.add(e.key))return;
  var mc=class_310.method_1551();if(mc.field_1755==null&&(!inGame.get()||!movement.get()&&movementKey(e.key)))return;
  long now=System.nanoTime();if(now-lastKey<(long)(cooldown.get()*1_000_000L))return;lastKey=now;
  String p=profile.is("FB67")?"fb67":profile.is("60HE V2")?"60he_v2":profile.is("Frosted 65")?"frost65":"aula";
  String kind=e.key==32?"space":"press";
  if(up)kind=e.key==32&&(p.equals("60he_v2")||p.equals("frost65"))?"space_release":"release";
  play("keys."+p+"."+kind,up?.55f:1);
 }
 @EventHandler public void mouse(MouseInputEvent e){
  if(e.action==0){buttons.remove(e.button);return;}if(e.action!=1||!buttons.add(e.button)||!mouse.get())return;
  if(class_310.method_1551().field_1755==null&&!inGame.get())return;
  long now=System.nanoTime();if(now-lastMouse<30_000_000L)return;lastMouse=now;play("keys.mouse.click",1);
 }
 private boolean movementKey(int key){var o=class_310.method_1551().field_1690;for(var b:new class_304[]{o.field_1894,o.field_1881,o.field_1913,o.field_1849,o.field_1903,o.field_1832,o.field_1867})if(class_3675.method_15981(b.method_1428()).method_1444()==key)return true;return false;}
 private void play(String event,float gain){var mc=class_310.method_1551();float v=Math.max(0,Math.min(1,volume.get()*gain));if(v>0)mc.execute(()->mc.method_1483().method_4873(class_1109.method_4757(class_3414.method_47908(class_2960.method_60655("albiz",event)),1,v)));}
}
