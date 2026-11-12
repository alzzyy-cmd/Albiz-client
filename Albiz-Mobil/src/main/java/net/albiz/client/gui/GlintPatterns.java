package net.albiz.client.gui;
/** Original tileable CPU texture presets; evaluated only on a settings/texture change. */
public final class GlintPatterns {
 private GlintPatterns(){}
 public static int luma(String style,int x,int y,int w,int h){
  double u=x/(double)Math.max(1,w),v=y/(double)Math.max(1,h),phase=(u+v)*Math.PI*12;
  double a=switch(style){case "Stripes"->Math.pow(.5+.5*Math.cos(phase),6);case "Wave"->Math.pow(.5+.5*Math.cos(phase+Math.sin(v*Math.PI*8)*2),4);case "Dots"->{double dx=u*8-Math.floor(u*8)-.5,dy=v*8-Math.floor(v*8)-.5;yield Math.max(0,1-Math.hypot(dx,dy)*3);}case "Stars"->{double dx=Math.abs(u*6-Math.floor(u*6)-.5),dy=Math.abs(v*6-Math.floor(v*6)-.5);yield Math.max(0,1-Math.min(dx,dy)*14)*Math.max(0,1-Math.max(dx,dy)*2);}default->.5;};
  return (int)Math.round(20+235*Math.max(0,Math.min(1,a)));
 }
}
