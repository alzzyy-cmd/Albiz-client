package net.albizlarp;
import java.util.*;import net.albiz.client.module.setting.*;
import net.albiz.client.event.EventHandler;import net.albiz.client.event.impl.*;
import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Independent multi-signal forensic implementation. No reference bytecode or Meteor dependency. */
public final class LarpV5 extends PassiveSensor {
 private final NumberSetting ceiling=addSetting("ceiling",new NumberSetting("Y Ceiling",0,-64,320).step(1));
 private final NumberSetting threshold=addSetting("score",new NumberSetting("Alert Threshold",100,5,1000).step(5));
 private final NumberSetting cluster=addSetting("cluster",new NumberSetting("Minimum Section Cluster",3,1,20).step(1));
 private final NumberSetting blockWeight=addSetting("block-weight",new NumberSetting("Block Weight",5,1,50).step(1));
 private final NumberSetting spawnerWeight=addSetting("spawner-weight",new NumberSetting("Spawner Weight",70,0,200).step(5));
 private final NumberSetting decay=addSetting("decay",new NumberSetting("Decay Per Minute",20,0,100).step(1));
 private final BooleanSetting decayOn=addSetting("decay-on",new BooleanSetting("Enable Decay",true));
 private final BooleanSetting synergy=addSetting("synergy",new BooleanSetting("Synergy Multiplier",true));
 private final BooleanSetting natural=addSetting("filter-natural",new BooleanSetting("Filter Natural Spawners",true));
 private final BooleanSetting notifications=addSetting("chat-alerts",new BooleanSetting("Chat Alerts",true));
 private final BooleanSetting packets=addSetting("packet-signals",new BooleanSetting("Packet Evidence",true));
 private final ColorSetting synergyColor=addSetting("synergy-color",new ColorSetting("Synergy Color",0xFFFFD740));
 private final LinkedHashMap<Long,Intel> intelligence=new LinkedHashMap<>();private final Map<Long,Integer> resends=new HashMap<>();private final Map<String,Integer> signatures=new HashMap<>();
 private volatile class_2338 interaction;private volatile long interactionAt;private long lootAt,lastDecay;
 private static final class Intel {double score;int types;boolean flagged;}
 public LarpV5(){super("larp-debug-v5","Larp Debug V5","Section clusters, spawner blocks/packets, underground hostile sounds, interaction-correlated loot sync and repeated chunk packets; type synergy and decay. Evidence is not proof of a base.");color.setArgb(0xFFFF3232);tracers.set(true);mode.set("Flat");}
 protected int yCeiling(){return ceiling.getInt();}
 protected boolean accepts(String id){return EvidenceRules.built(id)||id.equals("spawner");}
 protected boolean qualifies(Summary s,long now){return false;}
 @Override public void reset(){super.reset();intelligence.clear();resends.clear();signatures.clear();interaction=null;interactionAt=0;lootAt=0;lastDecay=System.currentTimeMillis();}
 private void add(int x,int z,double weight,int type){var p=new class_2338(x*16+8,-1,z*16+8);if(!inRange(p))return;long key=class_1923.method_8331(x,z);Intel intel=intelligence.get(key);if(intel==null){if(intelligence.size()>=(net.albiz.client.platform.PlatformInfo.isMobile()?128:512))intelligence.remove(intelligence.keySet().iterator().next());intel=new Intel();intelligence.put(key,intel);}intel.score=Math.min(10000,intel.score+Math.max(0,weight));intel.types|=type;double score=effective(intel);if(!intel.flagged&&score>=threshold.get()){intel.flagged=true;if(notifications.get())net.albiz.client.port.sober.gui.Notifications.push("Larp Debug V5","Evidence threshold · chunk "+x+", "+z+" · score "+Math.round(score),net.albiz.client.port.sober.gui.Notifications.Type.WARNING);}}
 private double effective(Intel i){return EvidenceRules.synergized(i.score,Integer.bitCount(i.types),synergy.get());}
 @EventHandler @Override public void tick(TickEvent event){super.tick(event);if(world==null)return;long now=System.currentTimeMillis();if(decayOn.get()){double amount=(now-lastDecay)/60000.0*decay.get();intelligence.values().removeIf(i->{i.score=Math.max(0,i.score-amount);return i.score<=0;});}lastDecay=now;
 if(ticks%100==0){Set<String> activeSections=new HashSet<>();for(var s:sections()){String key=s.x()+","+s.z()+","+s.y();activeSections.add(key);int signature=s.blocks()*31+s.spawners();Integer old=signatures.put(key,signature);if(old!=null&&old==signature)continue;if(s.blocks()>=cluster.getInt())add(s.x(),s.z(),s.blocks()*blockWeight.get(),1);if(s.spawners()>0&&(!natural.get()||s.blocks()>0))add(s.x(),s.z(),s.spawners()*spawnerWeight.get(),2);}
  signatures.keySet().retainAll(activeSections);if(signatures.size()>2048)signatures.clear();var mc=class_310.method_1551();intelligence.keySet().removeIf(k->!inRange(new class_2338(class_1923.method_8325(k)*16,0,class_1923.method_8332(k)*16))||!net.albiz.client.util.LevelUtil.isLoaded(mc.field_1687,class_1923.method_8325(k),class_1923.method_8332(k)));resends.keySet().removeIf(k->!inRange(new class_2338(class_1923.method_8325(k)*16,0,class_1923.method_8332(k)*16)));}}
 @EventHandler public void send(PacketSendEvent e){if(packets.get()&&e.packet instanceof class_2885 p){interaction=p.method_12543().method_17777().method_10062();interactionAt=System.currentTimeMillis();}}
 @EventHandler public void packet(PacketAppliedEvent e){if(!packets.get()||world!=class_310.method_1551().field_1687||world==null)return;
 if(e.packet instanceof class_2626 p&&id(p.method_11308()).equals("spawner")&&p.method_11309().method_10264()<ceiling.get()){var pos=p.method_11309();add(pos.method_10263()>>4,pos.method_10260()>>4,spawnerWeight.get(),4);}
 else if(e.packet instanceof class_2767 p&&p.method_11888()==class_3419.field_15251&&p.method_11889()<0)add(((int)Math.floor(p.method_11890()))>>4,((int)Math.floor(p.method_11893()))>>4,25,8);
 else if(e.packet instanceof class_2672 p){long key=class_1923.method_8331(p.method_11523(),p.method_11524());if(resends.size()>=512&&!resends.containsKey(key))resends.clear();int count=resends.merge(key,1,(a,b)->Math.min(1000,a+b));if(count>3)add(p.method_11523(),p.method_11524(),15,16);}
 else if(e.packet instanceof class_2649 p){long now=System.currentTimeMillis();var pos=interaction;if(pos==null||pos.method_10264()>=0||now-interactionAt>5000||lootAt==interactionAt)return;int signatures=0;for(var stack:p.comp_3839()){if(stack.method_7960())continue;String id=class_7923.field_41178.method_10221(stack.method_7909()).method_12832();if(Set.of("diamond","netherite_ingot","ancient_debris","totem_of_undying","elytra","enchanted_golden_apple","end_crystal","ender_pearl","emerald","golden_apple").contains(id))signatures++;}if(signatures>=2){lootAt=interactionAt;add(pos.method_10263()>>4,pos.method_10260()>>4,60,32);}}
 }
 @EventHandler @Override public void render(Render3DEvent event){if(world==null||world!=class_310.method_1551().field_1687)return;var r=new Render3D(event);int n=0;for(var en:intelligence.entrySet()){var intel=en.getValue();if(!intel.flagged)continue;double x=class_1923.method_8325(en.getKey())*16,z=class_1923.method_8332(en.getKey())*16;var c=Color.fromPacked(Integer.bitCount(intel.types)>=3?synergyColor.argb():color.argb());r.box(x,64,z,x+16,64.2,z+16,c.withAlpha(45),c,ShapeMode.Both);if(tracers.get())r.tracer(x+8,64.1,z+8,c);if(++n>=(net.albiz.client.platform.PlatformInfo.isMobile()?24:96))break;}}
}
