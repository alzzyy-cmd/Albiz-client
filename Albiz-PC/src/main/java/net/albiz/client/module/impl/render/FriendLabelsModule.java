package net.albiz.client.module.impl.render;

import net.albiz.client.config.FriendStore;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.Render2DEvent;
import net.albiz.client.gui.WorldLabel;
import net.albiz.client.module.*;
import net.albiz.client.module.setting.*;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.util.AlbizLogoGeometry;
import net.minecraft.*;

/** Local friend-list annotation, NOT remote client detection; no network or chat messages. */
public final class FriendLabelsModule extends ClientModule {
 private final BooleanSetting labels=addSetting("labels",new BooleanSetting("Show Friend Labels",true));
 private final BooleanSetting logo=addSetting("logo",new BooleanSetting("Albiz Logo",true));
 private final NumberSetting range=addSetting("range",new NumberSetting("Range",64,8,128).step(8));
 public FriendLabelsModule(){super("friend-labels","Friend Labels","Local Albiz User labels on manually added friends. Does NOT verify their client.",ModuleCategory.RENDER);}
 @EventHandler public void draw(Render2DEvent e){
  var mc=class_310.method_1551();if(!labels.get()||mc.field_1687==null||mc.field_1724==null||mc.field_1690.field_1842)return;
  int w=e.graphics.method_51421(),h=e.graphics.method_51443(),count=0;
  String text=AlbizI18n.isTurkish()?"Albiz Kullanıcısı":"Albiz User";
  for(var p:mc.field_1687.method_18456()){
   if(p==mc.field_1724||!FriendStore.getInstance().isFriend(p.method_7334().name())||mc.field_1724.method_5707(p.method_73189())>range.get()*range.get())continue;
   var v=p.method_73189().method_1031(0,p.method_17682()+.65,0);var screen=WorldLabel.project(mc,v,w,h);if(screen==null)continue;
   int x=Math.round(screen[0])-mc.field_1772.method_1727(text)/2,y=Math.round(screen[1]);
   e.graphics.method_51433(mc.field_1772,text,x,y,0xFFE3262E,true);
   if(logo.get()){
    // Same supplied icon contour as world markers, rasterized with bounded one-pixel samples.
    for(double[] l:AlbizLogoGeometry.segments()){
     double ax=x-10+l[0]*6,ay=y+4+l[1]*6,bx=x-10+l[2]*6,by=y+4+l[3]*6;int n=Math.max(1,(int)Math.ceil(Math.max(Math.abs(bx-ax),Math.abs(by-ay))));
     for(int i=0;i<=n;i++){int xx=(int)Math.round(ax+(bx-ax)*i/n),yy=(int)Math.round(ay+(by-ay)*i/n);e.graphics.method_25294(xx,yy,xx+1,yy+1,0xFFE3262E);}
    }
   }
   if(++count>=16)break;
  }
 }
}
