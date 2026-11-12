from pathlib import Path
root=Path('src/main/java/net/albiz/client/port/soberui/gui')
p=root/'HudLayout.java';s=p.read_text().replace('o.addProperty("scale",','o.addProperty("hidden", p.hidden);\n            o.addProperty("scale",').replace('p.y = o.get("y").getAsInt();','p.y = o.get("y").getAsInt();\n                p.hidden = o.has("hidden") && o.get("hidden").getAsBoolean();').replace('int anchorY;\n','int anchorY;\n        boolean hidden;\n');idx=s.index('    public static float scaleOf');s=s[:idx]+'''    public static boolean hidden(Element element){return POSITIONS.get(element).hidden;}
    public static void toggleHidden(Element element){var p=POSITIONS.get(element);p.hidden=!p.hidden;}
    public static void resetElement(Element element){var p=POSITIONS.get(element);p.x=5;p.y=5;p.scale=1;p.anchorX=0;p.anchorY=0;p.hidden=false;}

'''+s[idx:];p.write_text(s)
p=root/'HudRenderer.java';s=p.read_text().replace('    public static boolean chatPress(','''    private static HudLayout.Element selected;
    private static int editorW,editorH;
    public static boolean chatPress(''').replace('        HudLayout.Element element = HudRenderer.shownAt(x, y);','        if (settingsPress(x,y,button)) return true;\n        HudLayout.Element element = HudRenderer.shownAt(x, y);',1).replace('        if (element == HudLayout.Element.SPOTIFY','        selected = element;\n        if (element == HudLayout.Element.SPOTIFY',1).replace('        active = dragging != null ? dragging :','        editorW=sw;editorH=sh;\n        settingsPane(context,sw,sh);\n        active = dragging != null ? dragging : selected != null ? selected :',1).replace('            int baseW = HudRenderer.baseWidth(element, mc, modules);','            if (!ghost && HudLayout.hidden(element)) continue;\n            int baseW = HudRenderer.baseWidth(element, mc, modules);');idx=s.index('    private static float frameDelta()');s=s[:idx]+'''    private static void editorText(class_332 c,String text,int x,int y){CrispText.draw(c,text,"geist-semibold.ttf",x,y,9f,0f,0xFFEDE7ED);}
    private static void editorButton(class_332 c,String text,int x,int y,int w){Smooth.rect(c,x,y,w,17,4,0xE5352736);editorText(c,text,x+5,y+4);}
    private static void settingsPane(class_332 c,int sw,int sh){
        int w=Math.min(160,sw/2),x=sw-w-5,y=34;
        Smooth.rect(c,x,y,w,204,8,0xF01C151E);
        editorText(c,"HUD EDITOR",x+9,y+10);
        if(selected==null){editorText(c,"Click a HUD element",x+9,y+30);editorText(c,"Drag to move",x+9,y+48);return;}
        editorText(c,selected.name().replace('_',' '),x+9,y+29);
        var b=SHOWN.get(selected);
        editorText(c,"Scale: "+Math.round(HudLayout.scaleOf(selected)*100)+"%",x+9,y+49);
        editorButton(c,"-",x+9,y+64,28);editorButton(c,"+",x+43,y+64,28);editorButton(c,"Reset size",x+77,y+64,w-86);
        editorText(c,b==null?"Position unavailable":"X: "+b.x()+"   Y: "+b.y(),x+9,y+91);
        editorButton(c,"Left",x+9,y+105,54);editorButton(c,"Right",x+69,y+105,w-78);
        editorButton(c,"Up",x+9,y+126,54);editorButton(c,"Down",x+69,y+126,w-78);
        editorButton(c,HudLayout.hidden(selected)?"Visibility: Hidden":"Visibility: Shown",x+9,y+149,w-18);
        editorButton(c,"Reset position & size",x+9,y+172,w-18);
    }
    private static boolean settingsPress(double mx,double my,int button){
        int w=Math.min(160,editorW/2),x=editorW-w-5,y=34;
        if(mx<x||mx>=x+w||my<y||my>=y+204)return false;
        if(button!=0||selected==null)return true;
        int row=(int)my-y;var b=SHOWN.get(selected);
        if(row>=64&&row<81){if(mx<x+39)HudLayout.scale(selected,-.05f);else if(mx<x+75)HudLayout.scale(selected,.05f);else HudLayout.resetScale(selected);}
        else if(b!=null&&row>=105&&row<143){int dx=0,dy=0;if(row<122)dx=mx<x+66?-1:1;else dy=mx<x+66?-1:1;HudLayout.move(selected,b.x()+dx,b.y()+dy,editorW,editorH,b.width(),b.height());}
        else if(row>=149&&row<166)HudLayout.toggleHidden(selected);
        else if(row>=172&&row<189)HudLayout.resetElement(selected);
        HudLayout.save();return true;
    }

'''+s[idx:];p.write_text(s)
