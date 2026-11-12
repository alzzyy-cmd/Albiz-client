package net.albiz.client.gui;
import net.albiz.client.port.soberui.gui.HudRenderer;
import net.albiz.client.port.soberui.gui.HudLayout;
import net.minecraft.*;
/** Native screen input, independent of chat and ImGui mouse capture. */
public final class HudEditorScreen extends class_437 {
 private final class_437 parent;
 public HudEditorScreen(){this(null);}
 public HudEditorScreen(class_437 parent){super(class_2561.method_43470("Albiz HUD Editor"));this.parent=parent;}
 public boolean method_25402(class_11909 click,boolean twice){return HudRenderer.chatPress(click.comp_4798(),click.comp_4799(),click.method_74245())||super.method_25402(click,twice);}
 public boolean method_25403(class_11909 click,double dx,double dy){HudRenderer.editorPointer(click.comp_4798(),click.comp_4799());return true;}
 public boolean method_25406(class_11909 click){HudRenderer.editorRelease();return true;}
 public boolean method_25401(double x,double y,double horizontal,double vertical){return HudRenderer.chatScroll(x,y,vertical);}
 public void method_25394(class_332 c,int mx,int my,float delta){HudRenderer.editorPointer(mx,my);c.method_25303(class_310.method_1551().field_1772,net.albiz.client.i18n.AlbizI18n.isTurkish()?"HUD Editor · Sürükle · Kaydır: Boyut · Esc: Geri":"HUD Editor · Drag · Scroll: Resize · Esc: Back",8,8,0xFFF0DAE8);}
 public void method_25420(class_332 c,int x,int y,float delta){}
 public void method_25432(){HudRenderer.editorRelease();HudLayout.save();net.albiz.client.port.soberui.SoberUi.save();super.method_25432();}
 public void method_25419(){class_310.method_1551().method_1507(parent);}
 public boolean method_25421(){return false;}
}
