package net.albiz.client.gui;
import java.util.*;import imgui.type.ImString;import net.albiz.client.imgui.*;import net.minecraft.*;
/** Albiz's bounded card browser. Server editing/connecting stays owned by vanilla. */
public final class AlbizMultiplayerScreen extends class_500 {
 private java.util.concurrent.ThreadPoolExecutor pingPool;private volatile boolean closed;
 private final Map<String,class_4185> actions=new LinkedHashMap<>();private final ImString search=new ImString(128);
 private static final String[] KEYS={"selectServer.select","selectServer.direct","selectServer.add","selectServer.edit","selectServer.delete","selectServer.refresh","gui.back"};
 public AlbizMultiplayerScreen(class_437 parent){super(parent);}
 protected void method_25426(){
  super.method_25426();closed=false;pingPool=new java.util.concurrent.ThreadPoolExecutor(2,2,15,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.ArrayBlockingQueue<>(32),r->{Thread t=new Thread(r,"Albiz server ping");t.setDaemon(true);return t;});actions.clear();this.method_37066(field_3043);
  for(var el:List.copyOf(method_25396()))if(el instanceof class_4185 button){
   var content=button.method_25369().method_10851();if(content instanceof class_2588 t&&Arrays.asList(KEYS).contains(t.method_11022())){actions.put(t.method_11022(),button);this.method_37066(button);}
  }
  ImGuiManager.getInstance().activate(this::renderGrid);
 }
 public void method_25432(){closed=true;if(pingPool!=null)pingPool.shutdownNow();super.method_25432();ImGuiManager.getInstance().deactivate();}
 public void method_25420(class_332 c,int x,int y,float dt){}
 public void method_25394(class_332 c,int x,int y,float dt){ImGuiManager.getInstance().requestCursor(c);c.method_73199();}
 public boolean method_25402(class_11909 e,boolean twice){return false;}
 public boolean method_25404(class_11908 e){if(e.comp_4795()==256){this.method_25419();return true;}return false;}
 private void press(String key){var b=actions.get(key);if(b!=null&&b.field_22763)class_310.method_1551().method_63588(()->b.method_25306(new class_11908(257,0,0)));}
 private void renderGrid(){
  GuiPalette.refresh();float sw=GuardedImGui.getIO().getDisplaySizeX(),sh=GuardedImGui.getIO().getDisplaySizeY(),pad=AlbizWidgets.s(20);
  GuardedImGui.getBackgroundDrawList().addRectFilled(0,0,sw,sh,0xff141018);
  float w=sw-pad*2,h=sh-pad*2;GuardedImGui.setNextWindowPos(pad,pad,1);GuardedImGui.setNextWindowSize(w,h,1);
  GuardedImGui.pushStyleVar(2,AlbizWidgets.s(16),AlbizWidgets.s(16));GuardedImGui.pushStyleVar(3,AlbizWidgets.s(18));GuardedImGui.pushStyleColor(2,GuiPalette.cardBg());
  if(GuardedImGui.begin("##albiz_servers",319)){
   GuardedImGui.textUnformatted("ALBIZ  /  Multiplayer");GuardedImGui.setNextItemWidth(w-AlbizWidgets.s(32));GuardedImGui.inputTextWithHint("##search_servers","Search servers",search);
   float dockH=AlbizWidgets.s(38)*Math.max(1,(int)Math.ceil(KEYS.length/(double)Math.max(1,(int)((w-AlbizWidgets.s(32))/AlbizWidgets.s(108))))),listH=Math.max(AlbizWidgets.s(48),h-GuardedImGui.getCursorPosY()-dockH);
   if(GuardedImGui.beginChild("##servers",w-AlbizWidgets.s(32),listH,false,24)){
    float avail=w-AlbizWidgets.s(40),gap=AlbizWidgets.s(12),min=AlbizWidgets.s(250);int cols=Math.max(1,Math.min(3,(int)((avail+gap)/(min+gap))));float cw=(avail-gap*(cols-1))/cols,ch=AlbizWidgets.s(110);int i=0;
    String query=search.get().toLowerCase(Locale.ROOT);
    for(var entry:field_3043.method_25396()){
     String name,address;class_642 info=null;
     if(entry instanceof class_4267.class_4270 server){info=server.method_20133();ping(info);name=info.field_3752;address=info.field_3761;}
     else if(entry instanceof class_4267.class_4269){name="LAN server";address="Local network";}else continue;
     if(!(name+" "+address).toLowerCase(Locale.ROOT).contains(query))continue;
     int col=i%cols,row=i/cols;GuardedImGui.setCursorPos(col*(cw+gap),row*(ch+gap));float x=GuardedImGui.getCursorScreenPosX(),y=GuardedImGui.getCursorScreenPosY();GuardedImGui.pushID(i++);
     if(GuardedImGui.invisibleButton("##server",cw,ch)){field_3043.method_20122(entry);method_20121();if(GuardedImGui.isMouseDoubleClicked(0))class_310.method_1551().method_63588(entry::method_74043);}
     boolean chosen=entry==field_3043.method_25334();var dl=GuardedImGui.getWindowDrawList();dl.addRectFilled(x,y,x+cw,y+ch,AlbizWidgets.c(chosen?GuiPalette.accent():GuiPalette.panelBg()),AlbizWidgets.s(14));
     text(dl,name,x+AlbizWidgets.s(14),y+AlbizWidgets.s(20),cw-AlbizWidgets.s(28),-1);
     text(dl,address,x+AlbizWidgets.s(14),y+AlbizWidgets.s(48),cw-AlbizWidgets.s(28),0xffb9b6c6);
     if(info!=null&&info.field_3757!=null)text(dl,info.field_3757.getString().replace('\n',' '),x+AlbizWidgets.s(14),y+AlbizWidgets.s(76),cw-AlbizWidgets.s(28),0xffb9b6c6);
     GuardedImGui.popID();
    }
    GuardedImGui.setCursorPos(0,((i+cols-1)/cols)*(ch+gap));GuardedImGui.dummy(1,1);
   }GuardedImGui.endChild();
   float xx=0,yy=GuardedImGui.getCursorPosY(),bw=AlbizWidgets.s(102),bh=AlbizWidgets.s(32);
   for(String key:KEYS){if(xx+bw>w-AlbizWidgets.s(32)){xx=0;yy+=bh+AlbizWidgets.s(6);}GuardedImGui.setCursorPos(AlbizWidgets.s(16)+xx,yy);var b=actions.get(key);if(b!=null&&GuardedImGui.button(b.method_25369().getString()+"##"+key,bw,bh))press(key);xx+=bw+AlbizWidgets.s(6);}
  }GuardedImGui.end();GuardedImGui.popStyleColor();GuardedImGui.popStyleVar(2);
 }
 private void ping(class_642 server){
  if(closed||server.method_55825()!=class_642.class_9083.field_47880||pingPool.getQueue().remainingCapacity()==0)return;
  server.method_55824(class_642.class_9083.field_47881);
  try{pingPool.execute(()->{if(closed)return;try{
   method_2538().method_3003(server,()->{if(!closed)class_310.method_1551().execute(()->method_2529().method_2987());},()->{
    if(!closed)server.method_55824(server.field_3756==class_155.method_16673().comp_4027()?class_642.class_9083.field_47884:class_642.class_9083.field_47883);
   },class_12239.method_75867(class_310.method_1551().field_1690.method_1639()));
  }catch(Exception ex){if(!closed){server.method_55824(class_642.class_9083.field_47882);server.field_3757=class_2561.method_43471("multiplayer.status.no_connection");}}
  });}catch(java.util.concurrent.RejectedExecutionException ex){server.method_55824(class_642.class_9083.field_47880);}
 }
 private static void text(imgui.ImDrawList dl,String t,float x,float y,float w,int color){AlbizWidgets.text(dl,GuardedImGui.getFont(),x,y,color,AlbizWidgets.ellipsize(GuardedImGui.getFont(),t,w),false);}
}
