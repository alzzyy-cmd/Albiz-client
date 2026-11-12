package net.albiz.client.module.impl.render;
import java.util.*;
import net.albiz.client.module.*;import net.albiz.client.module.setting.*;
import net.albiz.client.event.EventHandler;import net.albiz.client.event.impl.*;
import net.albiz.client.esp.ChunkScanner;import net.albiz.client.util.*;
import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Passive underground kelp / crafter activity / positional sound inspection. No forced chunk requests. */
public final class AbyzDebugModule extends ClientModule implements ChunkScanner.Consumer {
 private final BooleanSetting spawners=addSetting("spawners",new BooleanSetting("Received Spawners",true));
 private final BooleanSetting redstone=addSetting("redstone",new BooleanSetting("Received Redstone",true));
 private final BooleanSetting changes=addSetting("redstone-changes",new BooleanSetting("Redstone Transitions",true));
 private final BooleanSetting kelp=addSetting("kelp",new BooleanSetting("Underground Kelp",true));
 private final BooleanSetting crafter=addSetting("crafter",new BooleanSetting("Working Crafters",true));
 private final BooleanSetting sound=addSetting("sound",new BooleanSetting("Underground Sounds",true));
 private final BooleanSetting own=addSetting("ignore-own",new BooleanSetting("Ignore Nearby Sounds",true));
 private final NumberSetting ownRadius=addSetting("own-radius",new NumberSetting("Own Sound Radius",8,0,64).step(1));
 private final NumberSetting memory=addSetting("memory",new NumberSetting("Activity Memory Seconds",60,5,300).step(5));
 private final NumberSetting radius=addSetting("radius",new NumberSetting("Chunk Radius",8,1,24).step(1));
 private final NumberSetting ceiling=addSetting("y-ceiling",new NumberSetting("Underground Y Ceiling",0,-64,128).step(1));
 private final BooleanSetting alerts=addSetting("alerts",new BooleanSetting("Notifications",true));
 private final BooleanSetting howl=addSetting("howl",new BooleanSetting("Discovery Howl",true));
 private final BooleanSetting tracers=addSetting("tracers",new BooleanSetting("Tracers",false));
 private final ModeSetting marker=addSetting("marker",new ModeSetting("Marker","Albiz Logo","Color","Albiz Logo"));
 private final ColorSetting color=addSetting("color",new ColorSetting("Kelp Color",0xFF69E69A));
 private final ColorSetting activityColor=addSetting("activity-color",new ColorSetting("Activity Color",0xFFFFB75C));
 private final LinkedHashMap<Long,Hit> found=new LinkedHashMap<>();private Object world;private int ticks;private long nextAlert;
 private record Hit(int type,long time,String reason){}
 public AbyzDebugModule(){super("abyz-debug","Abyz Debug","Received spawners/redstone, block transitions, Y<0 kelp/crafter/sound evidence. No hidden-data recovery or base confirmation.",ModuleCategory.BASE_RAID);spawners.onChange(()->{if(isEnabled())ChunkScanner.rescan();});redstone.onChange(()->{if(isEnabled())ChunkScanner.rescan();});kelp.onChange(()->{if(isEnabled())ChunkScanner.rescan();});crafter.onChange(()->{if(isEnabled())ChunkScanner.rescan();});radius.onChange(()->{if(isEnabled())ChunkScanner.rescan();});}
 private static String id(class_2680 s){return class_7923.field_41175.method_10221(s.method_26204()).method_12832();}
 public boolean wants(class_2680 s){String id=id(s);return kelp.get()&&UndergroundRules.kelp(id)||crafter.get()&&UndergroundRules.workingCrafter(id,s.toString())||spawners.get()&&ReceivedActivityRules.spawner(id)||redstone.get()&&ReceivedActivityRules.redstone(id);}
 @Override protected void onEnable(){world=class_310.method_1551().field_1687;reset();ChunkScanner.register(this);}
 @Override protected void onDisable(){ChunkScanner.unregister(this);reset();world=null;}
 public void reset(){found.clear();ticks=0;nextAlert=0;}
 public void removed(long p){found.remove(p);}
 public void found(long p,class_2680 s){var pos=class_2338.method_10092(p);if(!wants(s))return;String id=id(s);int type=ReceivedActivityRules.spawner(id)?3:UndergroundRules.kelp(id)?0:UndergroundRules.workingCrafter(id,s.toString())?1:4;record(pos,type,type==3?"Received spawner (natural possible)":type==4?"Received redstone component":type==0?"Underground kelp":"Active crafter");}
 private boolean eligible(class_2338 p){var mc=class_310.method_1551();return mc.field_1687!=null&&mc.field_1724!=null&&Math.max(Math.abs((p.method_10263()>>4)-(mc.field_1724.method_31477()>>4)),Math.abs((p.method_10260()>>4)-(mc.field_1724.method_31479()>>4)))<=radius.getInt();}
 private void record(class_2338 p,int type,String why){var mc=class_310.method_1551();if(world!=mc.field_1687){world=mc.field_1687;reset();}if(!eligible(p)||(type<=2&&!UndergroundRules.underground(p.method_10264(),ceiling.getInt())))return;long key=p.method_10063();boolean fresh=!found.containsKey(key);int max=net.albiz.client.platform.PlatformInfo.isMobile()?512:2048;if(fresh&&found.size()>=max)found.remove(found.keySet().iterator().next());long now=System.currentTimeMillis();found.put(key,new Hit(type,now,why));if(fresh&&howl.get())net.albiz.client.module.impl.client.SoundFeedbackModule.abyzAlert();if(fresh&&alerts.get()&&now>=nextAlert){nextAlert=now+4000;net.albiz.client.port.sober.gui.Notifications.push("Abyz Debug",why+" · "+p.method_10263()+", "+p.method_10264()+", "+p.method_10260(),net.albiz.client.port.sober.gui.Notifications.Type.INFO);}}
 @EventHandler public void block(BlockUpdateEvent e){if(spawners.get()&&ReceivedActivityRules.spawner(id(e.newState)))record(e.pos,3,"Received spawner (natural possible)");if(changes.get()&&ReceivedActivityRules.transition(id(e.oldState),e.oldState.toString(),id(e.newState),e.newState.toString()))record(e.pos,5,"Redstone state transition");if(crafter.get()&&UndergroundRules.workingCrafter(id(e.newState),e.newState.toString())&&!UndergroundRules.workingCrafter(id(e.oldState),e.oldState.toString()))record(e.pos,1,"Crafter operation");}
 /* Was PacketAppliedEvent, which is never dispatched for sound packets: underground and crafter sounds were never recorded. */
 @EventHandler public void packet(PacketReceiveEvent e){if(e.packet instanceof class_2767 p){String id=String.valueOf(p.method_11894().comp_349().comp_3319());boolean craft=UndergroundRules.crafterSound(id);if(!(craft?crafter.get():sound.get())||!UndergroundRules.underground(p.method_11889(),ceiling.getInt()))return;var mc=class_310.method_1551();if(mc.field_1724==null)return;if(own.get()&&mc.field_1724.method_5707(new class_243(p.method_11890(),p.method_11889(),p.method_11893()))<ownRadius.get()*ownRadius.get())return;record(class_2338.method_49637(p.method_11890(),p.method_11889(),p.method_11893()),craft?1:2,craft?"Crafter sound":id);}}
 @EventHandler public void tick(TickEvent e){var mc=class_310.method_1551();if(world!=mc.field_1687){world=mc.field_1687;reset();if(world!=null)ChunkScanner.rescan();}if(world==null||++ticks%20!=0)return;long now=System.currentTimeMillis();found.entrySet().removeIf(en->{var p=class_2338.method_10092(en.getKey());Hit h=en.getValue();return !eligible(p)||!LevelUtil.isLoaded(mc.field_1687,p.method_10263()>>4,p.method_10260()>>4)||(h.type==1||h.type==2||h.type==5)&&now-h.time>memory.get()*1000||h.type==0&&!kelp.get()||h.type==1&&!crafter.get()||h.type==2&&!sound.get()||h.type==3&&!spawners.get()||h.type==4&&!redstone.get()||h.type==5&&!changes.get();});}
 @EventHandler public void render(Render3DEvent e){var mc=class_310.method_1551();if(world==null||world!=mc.field_1687||mc.field_1724==null)return;var r=new Render3D(e);int n=0;Set<Long> chunks=new HashSet<>();for(var en:found.entrySet()){var p=class_2338.method_10092(en.getKey());var c=Color.fromPacked(en.getValue().type==0?color.argb():activityColor.argb());if(marker.is("Albiz Logo")){long key=class_1923.method_8331(p.method_10263()>>4,p.method_10260()>>4);if(!chunks.add(key))continue;LogoMarker.draw(r,(p.method_10263()>>4)*16+8,p.method_10264()+.05,(p.method_10260()>>4)*16+8,4,System.nanoTime()/2.6e9,c);}else r.box(p,c.withAlpha(24),c,ShapeMode.Both);if(tracers.get())r.tracer(p.method_10263()+.5,p.method_10264()+.5,p.method_10260()+.5,c);if(++n>=(net.albiz.client.platform.PlatformInfo.isMobile()?24:96))break;}}
}
