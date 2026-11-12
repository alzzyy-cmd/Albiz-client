package net.albiz.client.gui;

import imgui.ImDrawList;
import net.albiz.client.imgui.GuardedImGui;

/** Portable RGBA controls: no native ColorPicker/ColorEdit/ColorButton or texture creation. */
public final class SafeColorEditor {
    private SafeColorEditor() {}
    public static float channel(float value) { return Float.isFinite(value)?Math.max(0,Math.min(1,value)):1; }
    public static boolean draw(String id,float[] color,int flags) {
        if(color==null || color.length<3) { GuardedImGui.textDisabled("Invalid color data");return false; }
        boolean changed=false;GuardedImGui.pushID(id);
        try {
            int count=(flags&2)!=0?3:Math.min(4,color.length);
            String[] names={"R","G","B","A"};
            for(int i=0;i<count;i++) {
                float[] value={channel(color[i])};
                if(value[0]!=color[i]) {color[i]=value[0];changed=true;}
                GuardedImGui.setNextItemWidth(Math.max(40,GuardedImGui.getContentRegionAvailX()-28));
                if(GuardedImGui.sliderFloat(names[i]+"##channel"+i,value,0,1,"%.2f")) {color[i]=channel(value[0]);changed=true;UiFeedback.sliderChanged();}
            }
        } finally {GuardedImGui.popID();}
        return changed;
    }
    public static boolean swatch(String id,float[] color,float width,float height) {
        float w=Math.max(1,width),h=Math.max(1,height);
        float x=GuardedImGui.getCursorScreenPosX(),y=GuardedImGui.getCursorScreenPosY();
        boolean clicked=GuardedImGui.invisibleButton(id,w,h);
        ImDrawList dl=GuardedImGui.getWindowDrawList();
        dl.addRectFilled(x,y,x+w,y+h,0xff383038,4);
        if(color!=null && color.length>=3) {
            int r=Math.round(channel(color[0])*255),g=Math.round(channel(color[1])*255),b=Math.round(channel(color[2])*255);
            int a=color.length>3?Math.round(channel(color[3])*255):255;
            dl.addRectFilled(x,y,x+w,y+h,a<<24|b<<16|g<<8|r,4);
        }
        dl.addRect(x,y,x+w,y+h,GuardedImGui.isItemHovered()?0xffdddddd:0xff725866,4,0,1);
        return clicked;
    }
}
