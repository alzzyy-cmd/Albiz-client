package net.albiz.client.module.impl.render;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.albiz.client.esp.*;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.*;
import net.albiz.client.module.*;
import net.albiz.client.module.setting.*;
import net.minecraft.*;

/** Original integration of Albiz's existing GPL growth heuristics, not Goobaa code. */
public final class SuspiciousEspModule extends ClientModule {
 private final NumberSetting radius=addSetting("scan-radius",new NumberSetting("Scan Radius",24,8,48).step(1));
 private final ColorSetting color=addSetting("color",new ColorSetting("Color",0xffef6548));
 private final EspStyle style=new EspStyle(this::addSetting,"Boxes",.25f,96);
 private final LongOpenHashSet marked=new LongOpenHashSet();
 private final BlockLayer layer=new BlockLayer(new BlockLayer.Palette(){
  public int color(Object kind){return color.argb();}
  public int tracerColor(Object kind){return color.argb();}
 },()->this.world==null?null:this.world.method_27983());
 private class_638 world;
 private class_2338 center;
 private CuboidSweep sweep;
 private int appliedRadius;
 public SuspiciousEspModule(){
  super("suspicious-esp","Suspicious ESP","Highlights received growth anomalies; heuristic, not proof of player activity.",ModuleCategory.BASE_RAID);
  radius.onChange(this::reset);
 }
 protected void onEnable(){reset();}
 protected void onDisable(){reset();}
 private void reset(){layer.clear();marked.clear();sweep=null;center=null;world=null;}
 @EventHandler public void onTick(TickEvent event){
  var client=class_310.method_1551();
  if(client.field_1687==null || client.field_1724==null){reset();return;}
  var at=client.field_1724.method_24515();
  int r=Math.max(8,Math.min(48,Math.round(radius.get())));
  if(world!=client.field_1687 || center==null || Math.abs(at.method_10263()-center.method_10263())>=16 || Math.abs(at.method_10264()-center.method_10264())>=16 || Math.abs(at.method_10260()-center.method_10260())>=16 || appliedRadius!=r){
   reset();world=client.field_1687;center=at.method_10062();appliedRadius=r;sweep=new CuboidSweep(r);
  }
  boolean mobile=net.albiz.client.platform.PlatformInfo.isMobile();
  long deadline=System.nanoTime()+(mobile?400_000:1_000_000);
  int budget=mobile?512:2048;
  for(int n=0;n<budget && System.nanoTime()<deadline;n++){
   int i=sweep.next();var pos=new class_2338(center.method_10263()+sweep.x(i),center.method_10264()+sweep.y(i),center.method_10260()+sweep.z(i));
   long key=pos.method_10063();
   if(!world.method_2935().method_12123(pos.method_10263()>>4,pos.method_10260()>>4)){if(marked.remove(key))layer.remove(key);continue;}
   var state=world.method_8320(pos);
   if(SusChunkFinderModule.suspiciousGrowth(world,pos,state)){
    if(marked.contains(key) || marked.size()<1500){marked.add(key);layer.put(key,Boolean.TRUE);}
   }else if(marked.remove(key))layer.remove(key);
  }
 }
 @EventHandler public void onRender(Render3DEvent event){
  if(world!=class_310.method_1551().field_1687){reset();return;}layer.render(event,style);
 }
}
