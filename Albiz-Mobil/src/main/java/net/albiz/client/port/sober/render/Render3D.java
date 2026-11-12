package net.albiz.client.port.sober.render;
import net.albiz.client.event.impl.Render3DEvent;import net.albiz.client.util.*;import net.minecraft.*;
/** Sober primitive API adapted to Albiz's batched, bounded world renderer. */
public final class Render3D {
 private final Render3DEvent event;private final FoglessLines lines;private float width=1.5f;private boolean depth;private static long sharedFrame=Long.MIN_VALUE;private static int sharedPrimitives;private final int limit=net.albiz.client.platform.PlatformInfo.isMobile()?1600:6000;
 public Render3D(Render3DEvent event){this.event=event;if(sharedFrame!=Render3DEvent.frame()){sharedFrame=Render3DEvent.frame();sharedPrimitives=0;}this.lines=FoglessLines.forFrame(event.partialTick);}
 public void setDepthTest(boolean value){depth=value;lines.withDepthTest(value);}
 public void setLineWidth(float value){width=Math.max(.5f,Math.min(5,value));}
 public boolean inView(double a,double b,double c,double d,double e,double f){return sharedPrimitives<limit&&(event.frustum==null||event.frustum.method_23093(new class_238(a,b,c,d,e,f)));}
 public void line(double ax,double ay,double az,double bx,double by,double bz,Color color){if(++sharedPrimitives<=limit)lines.line(ax,ay,az,bx,by,bz,color.packed(),width);}
 public void quad(double ax,double ay,double az,double bx,double by,double bz,double cx,double cy,double cz,double dx,double dy,double dz,Color color){if(++sharedPrimitives<=limit)WorldQuads.layer(!depth).quad(ax,ay,az,bx,by,bz,cx,cy,cz,dx,dy,dz,color.packed());}
 public void box(class_2338 p,Color fill,Color edge,ShapeMode mode){box(p.method_10263(),p.method_10264(),p.method_10260(),p.method_10263()+1,p.method_10264()+1,p.method_10260()+1,fill,edge,mode);}
 public void box(class_238 b,Color fill,Color edge,ShapeMode mode){box(b.field_1323,b.field_1322,b.field_1321,b.field_1320,b.field_1325,b.field_1324,fill,edge,mode);}
 public void box(double x,double y,double z,double X,double Y,double Z,Color fill,Color edge,ShapeMode mode){if(!inView(x,y,z,X,Y,Z))return;if(mode.sides())boxSides(x,y,z,X,Y,Z,fill);if(mode.lines())boxLines(x,y,z,X,Y,Z,edge);}
 public void boxSides(double x,double y,double z,double X,double Y,double Z,Color c){quad(x,y,z,X,y,z,X,Y,z,x,Y,z,c);quad(x,y,Z,x,Y,Z,X,Y,Z,X,y,Z,c);quad(x,y,z,x,Y,z,x,Y,Z,x,y,Z,c);quad(X,y,z,X,y,Z,X,Y,Z,X,Y,z,c);quad(x,y,z,x,y,Z,X,y,Z,X,y,z,c);quad(x,Y,z,X,Y,z,X,Y,Z,x,Y,Z,c);}
 public void boxLines(double x,double y,double z,double X,double Y,double Z,Color c){if(++sharedPrimitives<=limit)lines.box(new class_238(x,y,z,X,Y,Z),c.packed(),width);}
 public void tracer(double x,double y,double z,Color c){tracer(x,y,z,c,width,false);}
 public void tracer(double x,double y,double z,Color c,float w,boolean ignored){if(++sharedPrimitives<=limit)TracerEngine.draw(new class_243(x,y,z),c.packed());}
 public void beam(double x,double z,double bottom,double top,double radius,Color c,double glow){
  if(!inView(x-radius,bottom,z-radius,x+radius,top,z+radius))return;
  for(int layer=2;layer>=0;layer--){double r=radius*(1+layer*.7);Color tint=c.scaleAlpha((layer==0?.62:.12)*glow);boxSides(x-r,bottom,z-r,x+r,top,z+r,tint);}
 }
 public void disc(double x,double y,double z,double inner,double outer,Color a,Color b,int steps){int count=Math.min(net.albiz.client.platform.PlatformInfo.isMobile()?16:32,steps);for(int i=0;i<count;i++){double p=i*Math.PI*2/count,q=(i+1)*Math.PI*2/count;quad(x+Math.cos(p)*inner,y,z+Math.sin(p)*inner,x+Math.cos(p)*outer,y,z+Math.sin(p)*outer,x+Math.cos(q)*outer,y,z+Math.sin(q)*outer,x+Math.cos(q)*inner,y,z+Math.sin(q)*inner,a.scaleAlpha(.35));}}
}
