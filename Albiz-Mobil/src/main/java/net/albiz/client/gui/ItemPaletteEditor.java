package net.albiz.client.gui;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import java.util.*;
import net.albiz.client.i18n.SearchText;
import net.albiz.client.imgui.GuardedImGui;
import net.albiz.client.diagnostics.UiFailureNotes;
import net.albiz.client.module.setting.ItemColorListSetting;

/** Visible rows only; every window, child and ID has an exception-safe matching end. */
public final class ItemPaletteEditor {
    @FunctionalInterface public interface IconRenderer {void draw(imgui.ImDrawList dl,String id,float x,float y,float size);}
    private final IconRenderer icons;
    public ItemPaletteEditor() {this((dl,id,x,y,size)->PickerIcons.item(dl,net.albiz.client.util.ItemEnchants.resolve(id),x,y,size,1));}
    public ItemPaletteEditor(IconRenderer icons) {this.icons=Objects.requireNonNull(icons);}
    private ItemColorListSetting palette;
    private final ImBoolean open=new ImBoolean(false),colorOpen=new ImBoolean(false);
    private final ImString search=new ImString(256);
    private List<ItemColorListSetting.ItemEntry> filtered=List.of();
    private String previousQuery;
    private float[] target;
    private int renderedRows;
    public void open(ItemColorListSetting value) {
        close();palette=Objects.requireNonNull(value);open.set(true);search.set("");previousQuery=null;
    }
    public boolean isOpen() {return open.get();}
    public int renderedRows() {return renderedRows;}
    public boolean back() {if(colorOpen.get()){colorOpen.set(false);target=null;return true;}return close();}
    public boolean close() {
        boolean was=open.get()||colorOpen.get();open.set(false);colorOpen.set(false);target=null;
        if(palette!=null)palette.notifyChanged();palette=null;filtered=List.of();previousQuery=null;return was;
    }
    public void render() {
        if(palette==null || !open.get()) {if(palette!=null)close();return;}
        int mark=GuardedImGui.scopeMark();
        try {if(colorOpen.get())renderColor();else renderPalette();}
        catch(RuntimeException | LinkageError ex) {
            GuardedImGui.recoverScopes(mark);close();UiFailureNotes.record("item-palette",ex);
        }
    }
    private void renderPalette() {
        float sw=GuardedImGui.getIO().getDisplaySizeX(),sh=GuardedImGui.getIO().getDisplaySizeY();
        GuardedImGui.setNextWindowSize(Math.max(180,Math.min(520,sw-24)),Math.max(180,Math.min(570,sh-24)),4);
        renderedRows=0;
        boolean visible=GuardedImGui.begin("Item Colors###albiz_item_palette",open,288);
        try {
            if(visible) {
                GuardedImGui.textUnformatted("Default Color");GuardedImGui.sameLine();
                if(SafeColorEditor.swatch("##default",palette.defaultColor(),44,24)){target=palette.defaultColor();colorOpen.set(true);}
                GuardedImGui.setNextItemWidth(Math.max(80,GuardedImGui.getContentRegionAvailX()));
                GuardedImGui.inputTextWithHint("##search","Search items...",search);
                String query=SearchText.normalize(search.get());
                boolean changed=!query.equals(previousQuery);
                if(changed) {
                    filtered=palette.entries().stream().filter(e->query.isEmpty()||SearchText.normalize(e.displayName()).contains(query)||SearchText.normalize(e.id()).contains(query)).toList();
                    previousQuery=query;
                }
                float height=Math.max(40,GuardedImGui.getContentRegionAvailY()-44);
                boolean child=GuardedImGui.beginChild("##visible_items",0,height,false,0);
                try {
                    if(child) {
                        if(changed)GuardedImGui.setScrollY(0);
                        float base=GuardedImGui.getCursorPosY(),rowH=Math.max(38,GuardedImGui.getTextLineHeight()+16);
                        int first=Math.max(0,Math.min(filtered.size(),(int)((GuardedImGui.getScrollY()-base)/rowH)-1));
                        int last=Math.min(filtered.size(),first+(int)Math.ceil(height/rowH)+3);
                        float available=GuardedImGui.getContentRegionAvailX();
                        for(int i=first;i<last;i++) {
                            var entry=filtered.get(i);float y=base+i*rowH;
                            GuardedImGui.pushID(entry.id());
                            try {
                                GuardedImGui.setCursorPos(4,y+5);
                                if(GuardedImGui.checkbox("##enabled",entry.enabled()))palette.notifyChanged();
                                var dl=GuardedImGui.getWindowDrawList();
                                float iconSize=32;
                                GuardedImGui.setCursorPos(34,y+(rowH-iconSize)*.5f);
                                icons.draw(dl,entry.id(),GuardedImGui.getCursorScreenPosX(),GuardedImGui.getCursorScreenPosY(),iconSize);
                                float textStart=74,swatchX=Math.max(textStart,available-48);
                                float textWidth=Math.max(0,swatchX-textStart-8);
                                GuardedImGui.setCursorPos(textStart,y+2);
                                float tx=GuardedImGui.getCursorScreenPosX(),ty=GuardedImGui.getCursorScreenPosY();
                                if(textWidth>0) {
                                    if(GuardedImGui.invisibleButton("##name",textWidth,rowH-4)) {entry.enabled().set(!entry.enabled().get());palette.notifyChanged();}
                                    dl.pushClipRect(tx,ty,tx+textWidth,ty+rowH-4,true);
                                    try {net.albiz.client.i18n.LocalizedDraw.text(dl,tx,ty+(rowH-GuardedImGui.getTextLineHeight())*.5f-2,0xffeeeeee,entry.displayName());}
                                    finally {dl.popClipRect();}
                                }
                                GuardedImGui.setCursorPos(swatchX,y+(rowH-26)*.5f);
                                if(SafeColorEditor.swatch("##color",entry.color(),42,26)){target=entry.color();colorOpen.set(true);}
                            } finally {GuardedImGui.popID();}
                            GuardedImGui.setCursorPos(0,y+rowH-1);GuardedImGui.dummy(1,1);renderedRows++;
                        }
                        // A submitted item, not a dangling SetCursorPos, extends the scroll area.
                        if(!filtered.isEmpty()) {GuardedImGui.setCursorPos(0,base+filtered.size()*rowH-1);GuardedImGui.dummy(1,1);}
                    }
                } finally {GuardedImGui.endChild();}
                if(GuardedImGui.button("Back##palette",90,28))open.set(false);
            }
        } finally {GuardedImGui.end();}
        if(!open.get())close();
    }
    private void renderColor() {
        if(target==null) {colorOpen.set(false);return;}
        GuardedImGui.setNextWindowSize(280,320,4);
        boolean visible=GuardedImGui.begin("Item Color###albiz_item_color",colorOpen,288);
        try {
            if(visible) {
                if(SafeColorEditor.draw("##rgba",target,0))palette.notifyChanged();
                if(GuardedImGui.button("Back##color",90,28))colorOpen.set(false);
            }
        } finally {GuardedImGui.end();}
        if(!colorOpen.get())target=null;
    }
}
