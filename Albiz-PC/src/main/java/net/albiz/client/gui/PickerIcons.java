package net.albiz.client.gui;

import imgui.ImDrawList;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2248;

/** Real cached icons on desktop and Android. Caller submits visible rows only. */
final class PickerIcons {
 private PickerIcons() {}
 static float pixels(float size) { return Math.max(8,Math.min(96,size<16?Math.round(size):16*(float)Math.floor(size/16+.25F))); }
 static float blockSize(float size) {return BlockIcons.size(size);}
 static void item(ImDrawList dl,class_1792 item,float x,float y,float size,float alpha) {if(item!=null)ItemIcons.draw(dl,item,x,y,size,alpha);}
 static void item(ImDrawList dl,class_1799 stack,float x,float y,float size,float alpha) {if(stack!=null)ItemIcons.draw(dl,stack,x,y,size,alpha);}
 static void block(ImDrawList dl,class_2248 block,float x,float y,float size) {if(block!=null)BlockIcons.draw(dl,block,x,y,size);}
}
