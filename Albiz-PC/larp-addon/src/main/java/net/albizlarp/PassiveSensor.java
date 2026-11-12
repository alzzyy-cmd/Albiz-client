package net.albizlarp;
import java.util.*;
import net.albiz.client.module.*;import net.albiz.client.module.setting.*;
import net.albiz.client.event.EventHandler;import net.albiz.client.event.impl.*;
import net.albiz.client.esp.ChunkScanner;import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Shared palette-filtered time-sliced scan; no unbounded per-chunk section loops. */
public abstract class PassiveSensor extends ClientModule implements ChunkScanner.Consumer {
 protected final NumberSetting range=addSetting("range",new NumberSetting("Maximum Chunk Radius",12,1,64).step(1));
 protected final BooleanSetting tracers=addSetting("tracers",new BooleanSetting("Tracers",false));
 protected final ModeSetting mode=addSetting("mode",new ModeSetting("Display","Pillar","Flat","Pillar","Blocks"));
 protected final ColorSetting color=addSetting("color",new ColorSetting("Color",0xFFE5B75C));
 protected final LinkedHashMap<Long,Sample> samples=new LinkedHashMap<>();protected Object world;protected int ticks;private long summaryAt;private List<Summary> cached=List.of();private long config=Long.MIN_VALUE;
 protected record Sample(String id,long seen){}
 protected record Summary(int x,int z,int blocks,int spawners,int hives,int y,long newest){}
 protected record Section(int x,int z,int y,int blocks,int spawners){}
 protected PassiveSensor(String id,String name,String description){super(id,name,description,ModuleCategory.BASE_RAID);}
 protected abstract boolean accepts(String id);protected abstract boolean qualifies(Summary s,long now);
 protected int minRadius(){return 0;}protected int yCeiling(){return 320;}
 protected static String id(class_2680 state){return class_7923.field_41175.method_10221(state.method_26204()).method_12832();}
 public boolean wants(class_2680 state){return accepts(id(state));}
 protected long configuration(){return ((long)range.getInt()<<32)^((long)minRadius()<<16)^(yCeiling()+1024);}
 @Override protected void onEnable(){world=class_310.method_1551().field_1687;reset();config=configuration();ChunkScanner.register(this);}
 @Override protected void onDisable(){ChunkScanner.unregister(this);reset();world=null;}
 protected boolean inRange(class_2338 p){var mc=class_310.method_1551();return mc.field_1724!=null&&EvidenceRules.inRing((p.method_10263()>>4)-(mc.field_1724.method_31477()>>4),(p.method_10260()>>4)-(mc.field_1724.method_31479()>>4),minRadius(),range.getInt());}
 public void found(long position,class_2680 state){if(!wants(state))return;var p=class_2338.method_10092(position);if(!inRange(p)||p.method_10264()>yCeiling())return;int max=net.albiz.client.platform.PlatformInfo.isMobile()?2048:8192;if(!samples.containsKey(position)&&samples.size()>=max)samples.remove(samples.keySet().iterator().next());var previous=samples.get(position);String id=id(state);if(previous==null||!previous.id.equals(id))samples.put(position,new Sample(id,System.currentTimeMillis()));}
 public void removed(long p){samples.remove(p);}public void reset(){samples.clear();ticks=0;summaryAt=0;cached=List.of();}
 @EventHandler public void tick(TickEvent e){var mc=class_310.method_1551();long cfg=configuration();if(world!=mc.field_1687||cfg!=config){world=mc.field_1687;config=cfg;reset();if(world!=null)ChunkScanner.rescan();}if(world==null||mc.field_1724==null)return;if(++ticks%20==0)samples.entrySet().removeIf(en->{var p=class_2338.method_10092(en.getKey());return !inRange(p)||p.method_10264()>yCeiling()||!net.albiz.client.util.LevelUtil.isLoaded(mc.field_1687,p.method_10263()>>4,p.method_10260()>>4);});}
 protected List<Section> sections(){Map<String,int[]> map=new LinkedHashMap<>();for(var en:samples.entrySet()){var p=class_2338.method_10092(en.getKey());int x=p.method_10263()>>4,z=p.method_10260()>>4,y=p.method_10264()>>4;String k=x+","+z+","+y;var a=map.computeIfAbsent(k,unused->new int[]{x,z,y,0,0});if(en.getValue().id.equals("spawner"))a[4]++;else if(EvidenceRules.built(en.getValue().id))a[3]++;}List<Section> out=new ArrayList<>();for(var a:map.values())out.add(new Section(a[0],a[1],a[2],a[3],a[4]));return out;}
 protected List<Summary> summaries(){long now=System.currentTimeMillis();if(now-summaryAt<500)return cached;summaryAt=now;Map<Long,int[]> counts=new LinkedHashMap<>();Map<Long,Long> newest=new HashMap<>();for(var en:samples.entrySet()){var p=class_2338.method_10092(en.getKey());long k=class_1923.method_8331(p.method_10263()>>4,p.method_10260()>>4);var a=counts.computeIfAbsent(k,unused->new int[]{0,0,0,p.method_10264()});String id=en.getValue().id;if(id.equals("spawner"))a[1]++;else if(EvidenceRules.hive(id))a[2]++;else a[0]++;a[3]=Math.min(a[3],p.method_10264());newest.merge(k,en.getValue().seen,Math::max);}List<Summary> out=new ArrayList<>();for(var en:counts.entrySet()){var a=en.getValue();out.add(new Summary(class_1923.method_8325(en.getKey()),class_1923.method_8332(en.getKey()),a[0],a[1],a[2],a[3],newest.get(en.getKey())));}cached=List.copyOf(out);return cached;}
 @EventHandler public void render(Render3DEvent e){var mc=class_310.method_1551();if(world==null||world!=mc.field_1687||mc.field_1724==null)return;var r=new Render3D(e);var c=Color.fromPacked(color.argb());beginRender();int n=0;for(var s:summaries()){if(!qualifies(s,System.currentTimeMillis()))continue;draw(r,s,c);if(++n>=(net.albiz.client.platform.PlatformInfo.isMobile()?24:96))break;}extra(r,c);}
 protected void beginRender(){}protected void extra(Render3D r,Color c){}
 protected void draw(Render3D r,Summary s,Color c){var mc=class_310.method_1551();double x=s.x*16,z=s.z*16,y=mode.is("Pillar")?mc.field_1687.method_31607():s.y,top=mode.is("Pillar")?mc.field_1687.method_31607()+mc.field_1687.method_31605():y+.125;r.box(x,y,z,x+16,top,z+16,c.withAlpha(16),c,ShapeMode.Both);if(mode.is("Blocks")){int n=0;for(var en:samples.entrySet()){var p=class_2338.method_10092(en.getKey());if(p.method_10263()>>4==s.x&&p.method_10260()>>4==s.z){r.box(p,c.withAlpha(24),c,ShapeMode.Both);if(++n>=32)break;}}}if(tracers.get())r.tracer(x+8,s.y,z+8,c);}
}
