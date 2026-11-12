package net.albiz.client.module.impl.client;

import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.ModuleToggleEvent;
import net.albiz.client.module.*;
import net.albiz.client.module.setting.*;
import net.minecraft.*;

/** Procedural UI feedback plus the user-supplied wolf recording for chunk alerts. */
public final class SoundFeedbackModule extends ClientModule {
 private final BooleanSetting toggles=addSetting("module-sounds",new BooleanSetting("Module Sounds",true));
 private final BooleanSetting clicks=addSetting("menu-clicks",new BooleanSetting("Menu Clicks",true));
 private final BooleanSetting chunks=addSetting("chunk-alerts",new BooleanSetting("Chunk Alerts",true));
 private final NumberSetting volume=addSetting("volume",new NumberSetting("Volume",.5f,0,1).step(.05f));
 private final KeySetting howlKey=addSetting("howl-key",new KeySetting("Howl Key",-1));
 private final ActionSetting howlButton=addSetting("play-howl",new ActionSetting("Play Wolf Howl",()->manualHowl()));
 private long lastClick,lastAlert;
 @EventHandler public void key(net.albiz.client.event.impl.KeyEvent e){if(e.action==1 && e.key==howlKey.value() && class_310.method_1551().field_1755==null)manualHowl();}
 public static boolean manualHowl(){var m=ModuleRegistry.get(SoundFeedbackModule.class);if(m==null||!m.isEnabled())return false;return m.howl();}
 public static boolean abyzAlert(){return manualHowl();}
 private boolean howl(){long now=System.nanoTime();if(lastAlert!=0&&now-lastAlert<8_000_000_000L)return false;lastAlert=now;play("chunk_alert");return true;}

 private final net.albiz.client.gui.FeedbackThrottle sliderThrottle=new net.albiz.client.gui.FeedbackThrottle(100_000_000L);
 public SoundFeedbackModule(){super("sound-feedback","Sound Feedback","Menu/module feedback and the supplied wolf recording for chunk alerts.",ModuleCategory.CLIENT);}
 @Override public boolean enabledByDefault(){return true;}
 @Override public int settingsVersion(){return 1;}
 public static boolean migratedEnabled(boolean saved,int version){return version<1 || saved;}
 @Override public boolean enabledAfterConfigLoad(boolean saved,int version){return migratedEnabled(saved,version);}
 public static void sliderChanged(){
  var m=ModuleRegistry.get(SoundFeedbackModule.class);
  if(m!=null && m.isEnabled() && m.clicks.get() && m.sliderThrottle.allow(System.nanoTime()))m.play("menu_click");
 }
 @EventHandler public void toggleSound(ModuleToggleEvent event){if(toggles.get())play(event.enabled?"module_on":"module_off");}
 public static void menuClick(){
  var m=ModuleRegistry.get(SoundFeedbackModule.class);long now=System.nanoTime();
  if(m!=null && m.isEnabled() && m.clicks.get() && now-m.lastClick>50_000_000L){m.lastClick=now;m.play("menu_click");}
 }
 public static boolean chunkAlert(){
  var m=ModuleRegistry.get(SoundFeedbackModule.class);if(m==null || !m.isEnabled() || !m.chunks.get())return false;
  long now=System.nanoTime();if(m.lastAlert==0 || now-m.lastAlert>=8_000_000_000L){m.lastAlert=now;m.play("chunk_alert");}return true;
 }
 private void play(String name){
  var mc=class_310.method_1551();
  if(mc==null)return;
  mc.execute(()->{
   try {
    float v=volume.get();float gain=Float.isFinite(v)?Math.max(0,Math.min(1,v)):.5f;
    if(gain>0)mc.method_1483().method_4873(class_1109.method_4757(class_3414.method_47908(class_2960.method_60655("albiz",name)),1,gain));
   }catch(RuntimeException | LinkageError ex){net.albiz.client.diagnostics.UiFailureNotes.record("sound-feedback",ex);}
  });
 }
}
