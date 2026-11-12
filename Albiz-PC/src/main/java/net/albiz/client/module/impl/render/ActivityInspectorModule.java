package net.albiz.client.module.impl.render;
import java.util.*;
import net.albiz.client.module.*;import net.albiz.client.module.setting.*;
import net.albiz.client.event.EventHandler;import net.albiz.client.event.impl.*;
import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Independent bounded player history plus underground block/light-update evidence. */
public final class ActivityInspectorModule extends ClientModule {
 private final BooleanSetting players=addSetting("players",new BooleanSetting("Track Visible Players",true));
 private final BooleanSetting trails=addSetting("trails",new BooleanSetting("Trails",!net.albiz.client.platform.PlatformInfo.isMobile()));
 private final BooleanSetting underground=addSetting("underground",new BooleanSetting("Underground Activity",true));
 private final NumberSetting ceiling=addSetting("ceiling",new NumberSetting("Underground Y Ceiling",0,-64,128).step(1));
 private final NumberSetting hits=addSetting("hits",new NumberSetting("Hotspot Threshold",8,1,100).step(1));
 private final NumberSetting own=addSetting("own",new NumberSetting("Ignore Own Radius",24,0,64).step(1));
 private final NumberSetting memory=addSetting("memory",new NumberSetting("Memory Seconds",300,10,600).step(10));
 private final BooleanSetting tracers=addSetting("tracers",new BooleanSetting("Tracers",false));
 private final LinkedHashMap<UUID,Track> tracks=new LinkedHashMap<>();private final LinkedHashMap<Long,Heat> heat=new LinkedHashMap<>();private Object world;private int age;
 private static final class Track {class_243 p;long seen;double distance;boolean approaching;final ArrayDeque<class_243> trail=new ArrayDeque<>();}
 private static final class Heat {int hits;long time;int y;}
 public ActivityInspectorModule(){super("activity-inspector","Activity Inspector","Remembers visible-player positions, bounded trails and underground block/light activity. Uses only data actually sent by the server.",ModuleCategory.BASE_RAID);}
 private void clear(){tracks.clear();heat.clear();world=class_310.method_1551().field_1687;age=0;}
 @Override protected void onEnable(){clear();}@Override protected void onDisable(){clear();}
 @EventHandler public void tick(TickEvent e){var mc=class_310.method_1551();if(world!=mc.field_1687)clear();if(mc.field_1687==null||mc.field_1724==null||++age%10!=0)return;long now=System.currentTimeMillis();long cutoff=now-(long)(memory.get()*1000);tracks.values().removeIf(t->t.seen<cutoff);heat.values().removeIf(h->h.time<cutoff);
 if(!players.get())return;for(var p:mc.field_1687.method_18456()){if(p==mc.field_1724)continue;var id=p.method_5667();Track t=tracks.get(id);if(t==null){if(tracks.size()>=64)tracks.remove(tracks.keySet().iterator().next());t=new Track();tracks.put(id,t);}var pos=p.method_73189();double d=mc.field_1724.method_5707(pos);t.approaching=t.p!=null&&d<t.distance-1;t.p=pos;t.distance=d;t.seen=now;if(trails.get()){t.trail.addLast(pos);while(t.trail.size()>(net.albiz.client.platform.PlatformInfo.isMobile()?16:60))t.trail.removeFirst();}else t.trail.clear();}}
 @EventHandler public void block(BlockUpdateEvent e){var mc=class_310.method_1551();if(world!=mc.field_1687)clear();if(!underground.get()||e.oldState==e.newState||e.pos.method_10264()>ceiling.get()||mc.field_1724==null)return;var p=new class_243(e.pos.method_10263()+.5,e.pos.method_10264(),e.pos.method_10260()+.5);if(mc.field_1724.method_5707(p)<own.get()*own.get())return;long k=class_1923.method_8331(e.pos.method_10263()>>4,e.pos.method_10260()>>4);Heat h=heat.get(k);if(h==null){if(heat.size()>=128)heat.remove(heat.keySet().iterator().next());h=new Heat();heat.put(k,h);}h.hits=Math.min(1000,h.hits+1+(e.oldState.method_26213()!=e.newState.method_26213()?1:0));h.time=System.currentTimeMillis();h.y=e.pos.method_10264();}
 @EventHandler public void render(Render3DEvent e){var mc=class_310.method_1551();if(world!=mc.field_1687||world==null)return;var r=new Render3D(e);long now=System.currentTimeMillis();if(players.get())for(var t:tracks.values()){var c=new Color(t.approaching?255:100,t.approaching?110:180,230,now-t.seen>2000?110:230);r.box(t.p.field_1352-.3,t.p.field_1351,t.p.field_1350-.3,t.p.field_1352+.3,t.p.field_1351+1.8,t.p.field_1350+.3,c.withAlpha(20),c,ShapeMode.Both);if(tracers.get())r.tracer(t.p.field_1352,t.p.field_1351,t.p.field_1350,c);class_243 last=null;for(var p:t.trail){if(last!=null)r.line(last.field_1352,last.field_1351,last.field_1350,p.field_1352,p.field_1351,p.field_1350,c);last=p;}}
 if(underground.get()){int n=0;for(var en:heat.entrySet()){var h=en.getValue();if(h.hits<hits.get()||++n>32)continue;int x=class_1923.method_8325(en.getKey())*16,z=class_1923.method_8332(en.getKey())*16;var c=new Color(255,185,70,220);r.box(x,h.y,z,x+16,h.y+.15,z+16,c.withAlpha(25),c,ShapeMode.Both);}}}
}
