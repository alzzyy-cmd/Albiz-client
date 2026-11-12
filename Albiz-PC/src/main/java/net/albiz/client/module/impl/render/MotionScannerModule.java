package net.albiz.client.module.impl.render;

import java.util.*;
import net.albiz.client.module.*;
import net.albiz.client.module.setting.*;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.*;
import net.albiz.client.port.sober.render.*;
import net.minecraft.*;

/** Passive evidence only: server-sent changes are signals, never proof of a player/base. */
public final class MotionScannerModule extends ClientModule {
 private final NumberSetting threshold=addSetting("threshold",new NumberSetting("Signal Threshold",12,1,100).step(1));
 private final NumberSetting memory=addSetting("memory",new NumberSetting("Memory Seconds",60,5,300).step(5));
 private final NumberSetting ignore=addSetting("ignore-own",new NumberSetting("Ignore Own Radius",24,0,64).step(1));
 private final BooleanSetting blocks=addSetting("blocks",new BooleanSetting("Block Changes",true));
 private final BooleanSetting containers=addSetting("containers",new BooleanSetting("Container Events",true));
 private final BooleanSetting sounds=addSetting("sounds",new BooleanSetting("Block Sounds",true));
 private final BooleanSetting entities=addSetting("entities",new BooleanSetting("Entity Spawns",true));
 private final BooleanSetting worldEvents=addSetting("world-events",new BooleanSetting("World Events",true));
 private final NumberSetting radius=addSetting("radius",new NumberSetting("Scan Radius",128,16,512).step(8));
 private final BooleanSetting notify=addSetting("notify",new BooleanSetting("Notify",true));
 private final BooleanSetting beams=addSetting("beams",new BooleanSetting("Beams",false));
 private final BooleanSetting tracers=addSetting("tracers",new BooleanSetting("Tracers",false));
 private final ColorSetting tint=addSetting("color",new ColorSetting("Color",0xFFEEA95B));
 private final LinkedHashMap<Long,Spot> spots=new LinkedHashMap<>(); private Object world; private int age;
 public MotionScannerModule(){super("motion-scanner","Motion Scanner","Bounded passive chunk activity from block changes, container events, sounds and entity spawns; signals are not proof of players.",ModuleCategory.BASE_RAID);}
 private static final class Spot {double x,y,z;int score;long time;long lastPos=Long.MIN_VALUE;long lastSignal;boolean alerted;Spot(double x,double y,double z,int score,long time){this.x=x;this.y=y;this.z=z;this.score=score;this.time=time;}}
 private void clear(){spots.clear();world=class_310.method_1551().field_1687;age=0;}
 @Override protected void onEnable(){clear();} @Override protected void onDisable(){clear();}
 private void signal(double x,double y,double z,int weight){var mc=class_310.method_1551();if(mc.field_1687!=world){clear();}if(mc.field_1724==null||mc.field_1687==null||mc.field_1724.method_5707(new class_243(x,y,z))<ignore.get()*ignore.get())return;
  /* Radius filter: horizontal distance, so deep signals under the player are kept. */
  double dx=x-mc.field_1724.method_23317(),dz=z-mc.field_1724.method_23321();if(dx*dx+dz*dz>radius.get()*radius.get())return;
  long posKey=class_2338.method_10064((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));
  long key=class_1923.method_8331(((int)Math.floor(x))>>4,((int)Math.floor(z))>>4);long now=System.currentTimeMillis();Spot s=spots.get(key);if(s==null){if(spots.size()>=(net.albiz.client.platform.PlatformInfo.isMobile()?128:512))spots.remove(spots.keySet().iterator().next());s=new Spot(x,y,z,weight,now);spots.put(key,s);}else{
   /* Dedupe: one event often arrives as several packets (block change + sound + world event) at the same block. */
   if(s.lastPos==posKey&&now-s.lastSignal<250)return;
   s.score=Math.min(1000,s.score+weight);s.time=now;s.x=x;s.y=y;s.z=z;}
  s.lastPos=posKey;s.lastSignal=now;
  if(!s.alerted&&s.score>=threshold.get()){s.alerted=true;if(notify.get())net.albiz.client.port.sober.util.Chat.info("Motion Scanner",net.albiz.client.i18n.AlbizI18n.display("Activity signal near")+" X "+(int)Math.floor(x)+" Y "+(int)Math.floor(y)+" Z "+(int)Math.floor(z)+" · "+net.albiz.client.i18n.AlbizI18n.display("a hint, not proof"));}
 }
 @EventHandler public void block(BlockUpdateEvent e){if(blocks.get()&&e.oldState!=e.newState)signal(e.pos.method_10263()+.5,e.pos.method_10264(),e.pos.method_10260()+.5,1);}
 /* Was PacketAppliedEvent, which is never dispatched for block-action, sound or entity-spawn packets: these three signals were dead. */
 @EventHandler public void packet(PacketReceiveEvent e){if(e.packet instanceof class_2623 p&&containers.get())signal(p.method_11298().method_10263()+.5,p.method_11298().method_10264(),p.method_11298().method_10260()+.5,4);
  else if(e.packet instanceof class_2767 p&&sounds.get()&&p.method_11888()==class_3419.field_15245)signal(p.method_11890(),p.method_11889(),p.method_11893(),2);
  else if(e.packet instanceof class_2673 p&&worldEvents.get())signal(p.method_11531().method_10263()+.5,p.method_11531().method_10264(),p.method_11531().method_10260()+.5,2);
  else if(e.packet instanceof class_2604 p&&entities.get())signal(p.method_11175(),p.method_11174(),p.method_11176(),1);
 }
 @EventHandler public void tick(TickEvent e){if(world!=class_310.method_1551().field_1687)clear();if(++age%20==0){long cutoff=System.currentTimeMillis()-(long)(memory.get()*1000);spots.values().removeIf(s->s.time<cutoff);}}
 @EventHandler public void render(Render3DEvent e){var mc=class_310.method_1551();if(mc.field_1687==null||world!=mc.field_1687)return;var r=new Render3D(e);var c=Color.fromPacked(tint.argb());int n=0;for(var s:spots.values()){if(s.score<threshold.get()||++n>(net.albiz.client.platform.PlatformInfo.isMobile()?32:128))continue;int x=((int)Math.floor(s.x))>>4,z=((int)Math.floor(s.z))>>4;r.box(x*16,s.y,z*16,x*16+16,s.y+.15,z*16+16,c.withAlpha(20),c,ShapeMode.Both);if(beams.get())r.beam(x*16+8,z*16+8,s.y,s.y+32,.15,c,.5);if(tracers.get())r.tracer(s.x,s.y,s.z,c);}}
}
