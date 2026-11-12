package net.albiz.client.port.soberui.gui;
import java.util.*;import net.minecraft.*;import net.albiz.client.port.soberui.module.*;import net.albiz.client.port.soberui.modules.*;
/** A bounded, standalone image picker; returns to the existing Albiz GUI. */
public final class VisualPresetsScreen extends class_437{
 private final class_437 parent;private final net.albiz.client.port.soberui.module.Module module;private final Presets presets;private final Runnable sync;
 private final List<Integer> shown=new ArrayList<>();private final Map<Integer,String> names=new HashMap<>();private String search="";private int scroll,cols,x,y,w,h,cell=76;private boolean closed;
 public VisualPresetsScreen(class_437 parent,net.albiz.client.port.soberui.module.Module module,Runnable sync){super(class_2561.method_43470("Visual Selection"));this.parent=parent;this.module=module;this.presets=(Presets)module;this.sync=sync;for(int i=0;i<Math.min(512,presets.presetCount());i++)names.put(i,presets.presetName(i));filter();}
 private static String tr(String english,String turkish){return net.albiz.client.i18n.AlbizI18n.isTurkish()?turkish:english;}
 private void filter(){shown.clear();String q=search.toLowerCase(Locale.ROOT);names.keySet().stream().sorted().filter(i->names.get(i).toLowerCase(Locale.ROOT).contains(q)).forEach(shown::add);scroll=0;}
 private void layout(){w=Math.max(90,Math.min(650,field_22789-20));h=Math.max(100,Math.min(440,field_22790-20));x=(field_22789-w)/2;y=(field_22790-h)/2;cols=Math.max(1,(w-20)/cell);scroll=Math.min(scroll,Math.max(0,((shown.size()+cols-1)/cols)*cell-(h-76)));}
 public void method_25394(class_332 c,int mx,int my,float dt){layout();net.albiz.client.gui.mainmenu.DuskBackdrop.draw(c,field_22789,field_22790);c.method_25294(x,y,x+w,y+h,0xef111218);var font=field_22787.field_1772;c.method_25303(font,tr("Visual Selection","Görsel Seçim")+" — "+module.name,x+10,y+10,0xffeeeeee);c.method_25303(font,tr("Search: ","Ara: ")+search+"_",x+10,y+30,0xffc5c5cd);c.method_25303(font,tr("Back","Geri"),x+w-45,y+10,0xffff6268);
  c.method_44379(x+6,y+50,x+w-6,y+h-24);
  try{int first=(scroll/cell)*cols,last=Math.min(shown.size(),first+((h-74)/cell+2)*cols);for(int p=first;p<last;p++){int i=shown.get(p),cx=x+10+(p%cols)*cell,cy=y+52+(p/cols)*cell-scroll;boolean selected=presets.presetActive(i),hover=mx>=cx&&mx<cx+cell-5&&my>=cy&&my<cy+cell-5;c.method_25294(cx,cy,cx+cell-5,cy+cell-5,selected?0xee5b202b:hover?0xee34343d:0xcc22232a);
    class_2960 icon=module instanceof CustomModels m?m.previewIcon(i):module instanceof CsPickaxe m?m.previewIcon(i):null;
    if(icon!=null)c.method_25302(class_10799.field_56883,icon,cx+12,cy+3,0f,0f,40,40,64,64,64,64);else{class_1799 stack=presets.presetIcon(i);if(!stack.method_7960()){var mat=c.method_51448();mat.pushMatrix();try{mat.translate(cx+13,cy+3);mat.scale(2.4f,2.4f);c.method_51427(stack,0,0);}finally{mat.popMatrix();}}else c.method_25303(font,i==0?"—":"…",cx+28,cy+20,0xffaaaaaa);}
    String label=net.albiz.client.i18n.AlbizI18n.display(names.get(i));while(font.method_1727(label)>cell-10&&!label.isEmpty())label=label.substring(0,label.length()-1);c.method_25303(font,label,cx+3,cy+cell-22,0xffeeeeee);
  }}finally{c.method_44380();}
  c.method_25303(font,tr("Click to select · Scroll · Escape: back","Seçmek için tıkla · Kaydır · Escape: geri"),x+10,y+h-16,0xffa9a9b1);
 }
 public boolean method_25402(class_11909 e,boolean twice){layout();double mx=e.comp_4798(),my=e.comp_4799();if(my>=y&&my<y+25&&mx>=x+w-55){method_25419();return true;}if(mx>=x+10&&mx<x+w-10&&my>=y+50&&my<y+h-24){int col=(int)(mx-x-10)/cell,row=(int)(my-y-52+scroll)/cell,p=row*cols+col;if(col<cols&&p>=0&&p<shown.size()){presets.selectPreset(shown.get(p));sync.run();net.albiz.client.config.ModuleConfigStore.getInstance().saveIfChanged();return true;}}return true;}
 public boolean method_25401(double mx,double my,double dx,double dy){scroll=Math.max(0,scroll-(int)(dy*cell));layout();return true;}
 public boolean method_25404(class_11908 e){if(e.comp_4795()==256){method_25419();return true;}if(e.comp_4795()==259&&!search.isEmpty()){search=search.substring(0,search.length()-1);filter();return true;}return super.method_25404(e);}
 public boolean method_25400(class_11905 e){if(e.method_74227()&&search.length()<64){search+=e.method_74226();filter();}return true;}
 public boolean method_25421(){return false;}
 public void method_25419(){if(closed)return;closed=true;sync.run();net.albiz.client.config.ModuleConfigStore.getInstance().saveIfChanged();field_22787.method_1507(parent);}
}
