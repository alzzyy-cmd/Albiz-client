package net.albiz.client.gui;
import net.minecraft.*;import net.albiz.client.i18n.LanguageChoice;
/** Two static flag textures; no emoji-font or external resource dependency. */
public final class LanguageChoiceScreen extends class_437 {
 private boolean failed;
 public LanguageChoiceScreen(){super(class_2561.method_43470("Albiz / Language / Dil"));}
 private void text(class_332 c,String t,int x,int y,int color){c.method_25303(class_310.method_1551().field_1772,t,x,y,color);}
 public void method_25394(class_332 c,int mx,int my,float dt){int w=field_22789,h=field_22790,card=Math.min(210,Math.max(90,(w-36)/2)),y=Math.max(35,h/2-60);c.method_25294(0,0,w,h,0xFF17101D);text(c,"ALBIZ",w/2-16,y-25,0xFFFFF0F4);card(c,w/2-card-6,y,card,true,mx,my);card(c,w/2+6,y,card,false,mx,my);if(failed)text(c,"Save failed / Kaydedilemedi",Math.max(5,w/2-80),y+123,0xFFFF6677);}
 private void card(class_332 c,int x,int y,int w,boolean tr,int mx,int my){boolean hover=mx>=x&&mx<x+w&&my>=y&&my<y+115;c.method_25294(x,y,x+w,y+115,hover?0xFF493247:0xFF302236);int fw=Math.min(90,w-20),fh=fw*88/144;c.method_25302(class_10799.field_56883,class_2960.method_60655("albiz","textures/flags/"+(tr?"tr":"gb")+".png"),x+(w-fw)/2,y+12,0f,0f,fw,fh,144,88,144,88);text(c,tr?"Türkçe":"English",x+w/2-20,y+75,0xFFFFFFFF);text(c,tr?"Bir kez seçilir":"Choose once",x+10,y+97,0xFFD9C4D9);}
 public boolean method_25402(class_11909 click,boolean twice){if(click.method_74245()!=0)return false;int w=field_22789,card=Math.min(210,Math.max(90,(w-36)/2)),y=Math.max(35,field_22790/2-60);double x=click.comp_4798(),cy=click.comp_4799();int l=w/2-card-6,r=w/2+6;if(cy<y||cy>=y+115)return false;String code=x>=l&&x<l+card?"tr_tr":x>=r&&x<r+card?"en_us":null;if(code==null)return false;if(LanguageChoice.choose(code))class_310.method_1551().method_1507(new class_442());else failed=true;return true;}
 public boolean method_25422(){return false;}
 public void method_25419(){}
 public boolean method_25421(){return false;}
}
