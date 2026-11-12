package net.albiz.client.util;
import net.albiz.client.port.sober.render.*;
/** Baked outline of the actual supplied Albiz wolf-and-sword icon, drawn as a small bounded world-line logo. */
public final class LogoMarker {
 private static final double[][] LINES=AlbizLogoGeometry.segments();
 private LogoMarker(){}
 public static void draw(Render3D r,double x,double y,double z,double radius,double angle,Color c){draw(r,x,y,z,radius,angle,c,0);}
 /** plane 0 top XZ, 1 XY, 2 ZY. */
 public static void draw(Render3D r,double x,double y,double z,double radius,double angle,Color c,int plane){c=Color.fromPacked(0xFFE3262E);double cos=Math.cos(angle),sin=Math.sin(angle);for(double[] l:LINES){double a=(l[0]*cos-l[1]*sin)*radius,b=(l[0]*sin+l[1]*cos)*radius,A=(l[2]*cos-l[3]*sin)*radius,B=(l[2]*sin+l[3]*cos)*radius;if(plane==1)r.line(x+a,y-b,z,x+A,y-B,z,c);else if(plane==2)r.line(x,y-b,z+a,x,y-B,z+A,c);else r.line(x+a,y,z+b,x+A,y,z+B,c);}}
}
