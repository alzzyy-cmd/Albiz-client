package net.albiz.client.port;
import java.util.*;import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.albiz.client.event.*;import net.albiz.client.event.impl.*;import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Explicit settings/lifecycle bridge; no second client, addon loader or duplicate mixin set. */
public final class SoberAdapter extends ClientModule {
 private final net.albiz.client.port.sober.module.Module logic;
 private final List<Runnable> sync=new ArrayList<>();private class_638 world;private static int drainAge=-1;private static Object drainWorld;private final Map<Long,class_2818> loaded=new HashMap<>();private int tick;
 public SoberAdapter(String id,net.albiz.client.port.sober.module.Module logic){super(id,logic.name,logic.description,ModuleCategory.BASE_RAID);this.logic=logic;
  for(var s:logic.getSettings()){
   Setting target;
   if(s instanceof net.albiz.client.port.sober.setting.BoolSetting b){var t=new BooleanSetting(s.name,b.get());sync.add(()->b.set(t.get()));target=t;}
   else if(s instanceof net.albiz.client.port.sober.setting.IntSetting n){var t=new NumberSetting(s.name,n.get(),n.min,n.max).step(1);sync.add(()->n.set(Math.round(t.get())));target=t;}
   else if(s instanceof net.albiz.client.port.sober.setting.ColorSetting c){var t=new ColorSetting(s.name,c.get().packed());sync.add(()->c.set(Color.fromPacked(t.argb())));target=t;}
   else if(s.get() instanceof Enum<?> e){String[] values=Arrays.stream(e.getDeclaringClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);var t=new ModeSetting(s.name,e.name(),values);sync.add(()->{com.google.gson.JsonPrimitive v=new com.google.gson.JsonPrimitive(t.get());if(!s.save().equals(v))s.apply(v);});target=t;}
   else throw new IllegalArgumentException("Unsupported port setting: "+s.name);
   target.visibleWhen(s::isVisible);addSetting(target);
  }
 }
 protected void onEnable(){net.albiz.client.port.sober.module.Module.bindClient();sync.forEach(Runnable::run);world=class_310.method_1551().field_1687;logic.setEnabled(true);}
 protected void onDisable(){logic.setEnabled(false);logic.onGameLeft();world=null;loaded.clear();}
 @EventHandler public void tick(TickEvent event){var mc=class_310.method_1551();net.albiz.client.port.sober.module.Module.bindClient();sync.forEach(Runnable::run);
  if(world!=mc.field_1687){logic.onGameLeft();logic.onDisable();world=mc.field_1687;loaded.clear();if(world!=null)logic.onEnable();}
  if(world==null||mc.field_1724==null)return;
  logic.onTick();
  if(++tick%10==0){Set<Long> now=new HashSet<>();for(var c:net.albiz.client.port.sober.util.ChunkScanner.loadedChunks()){long k=c.method_12004().method_8324();now.add(k);if(loaded.put(k,c)!=c)logic.onChunkLoad(c);}loaded.keySet().retainAll(now);}
  if(drainWorld!=world||drainAge!=mc.field_1724.field_6012){drainWorld=world;drainAge=mc.field_1724.field_6012;net.albiz.client.port.sober.util.ChunkScanner.drain();}
 }
 @EventHandler public void block(BlockUpdateEvent e){if(world!=null&&world==class_310.method_1551().field_1687)logic.onBlockUpdate(e.pos,e.oldState,e.newState);}
 @EventHandler public void render(Render3DEvent e){if(world!=null&&world==class_310.method_1551().field_1687&&class_310.method_1551().field_1724!=null)logic.onRender(new Render3D(e));}
}
