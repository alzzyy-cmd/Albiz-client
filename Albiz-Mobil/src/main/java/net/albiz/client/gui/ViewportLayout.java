package net.albiz.client.gui;
/** Physical framebuffer constraints shared by desktop and tall Android displays. */
public final class ViewportLayout {
 private ViewportLayout(){}
 public static float scale(int width,int height,float user){
  if(!Float.isFinite(user)||user<=0)user=1;
  float requested=Math.max(.35f,Math.min(3.5f,height/1080f*user));
  return Math.max(.35f,Math.min(requested,Math.min(width/520f,height/430f)));
 }
}
