from pathlib import Path
r=Path('src/main/java/net/albiz/client')
p=r/'gui/HudEditorScreen.java';p.write_text('''package net.albiz.client.gui;
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
 public void method_25394(class_332 c,int mx,int my,float delta){HudRenderer.editorPointer(mx,my);}
 public void method_25420(class_332 c,int x,int y,float delta){}
 public void method_25432(){HudRenderer.editorRelease();HudLayout.save();net.albiz.client.port.soberui.SoberUi.save();super.method_25432();}
 public void method_25419(){class_310.method_1551().method_1507(parent);}
 public boolean method_25421(){return false;}
}
''')
p=r/'port/soberui/gui/HudRenderer.java';s=p.read_text().replace('boolean editing = (cards || standalone) && mc.field_1755 instanceof class_408 && hud.chatEditing.get() != false;','boolean editing = hud != null && mc.field_1724 != null && mc.field_1755 instanceof net.albiz.client.gui.HudEditorScreen;\n        if(editing){cards=true;standalone=false;}').replace('return hud != null && hud.chatEditing.get() != false && !mc.field_1690.field_1842 && mc.field_1724 != null && mc.field_1755 instanceof class_408 && !mc.method_53526().method_53536();','return hud != null && mc.field_1724 != null && mc.field_1755 instanceof net.albiz.client.gui.HudEditorScreen;').replace('return guiY >= (double)(mc.method_22683().method_4502() - 14);','return false; // Editor is a dedicated screen, not a chat input field.')
s=s.replace('    private static void endDrag() {','''    private static boolean editorDown;
    private static double editorMouseX,editorMouseY;
    public static void editorPointer(double x,double y){editorMouseX=x;editorMouseY=y;}
    public static void editorRelease(){editorDown=false;endDrag();}
    private static void endDrag() {''').replace('        dragging = element;','        editorDown=true;editorPointer(guiX,guiY);\n        dragging = element;')
s=s.replace('if (GLFW.glfwGetMouseButton((long)mc.method_22683().method_4490(), (int)0) != 1) {','if (!editorDown) {').replace('mc.field_1729.method_68879(mc.method_22683()) / (double)hudScale) - grabX','editorMouseX / (double)hudScale) - grabX').replace('mc.field_1729.method_68883(mc.method_22683()) / (double)hudScale) - grabY','editorMouseY / (double)hudScale) - grabY')
s=s.replace('        HudLayout.Element element = HudRenderer.shownAt(guiX / (double)hudScale, guiY / (double)hudScale);','        if (settingsPress(guiX / hudScale,guiY / hudScale,-1)) return true;\n        HudLayout.Element element = HudRenderer.shownAt(guiX / (double)hudScale, guiY / (double)hudScale);')
# Keep all elements available as editor ghosts even when normal HUD is disabled.
s=s.replace('!HudRenderer.enabled(hud, element) ||','!ghost && !HudRenderer.enabled(hud, element) ||')
p.write_text(s)
p=r/'gui/ClickGuiRenderer.java';s=p.read_text().replace('GuardedImGui.textWrapped(net.albiz.client.i18n.AlbizI18n.display("Open chat to move and resize the current HUD."));','''GuardedImGui.textWrapped(net.albiz.client.i18n.AlbizI18n.isTurkish()?"HUD öğelerini seç, taşı ve sağ panelden düzenle.":"Select HUD elements, drag them and edit their layout in the right panel.");
       if(GuardedImGui.button(net.albiz.client.i18n.AlbizI18n.isTurkish()?"HUD Editor Aç":"Open HUD Editor")){
        var client=class_310.method_1551();if(client.field_1724!=null)client.method_1507(new HudEditorScreen(client.field_1755));
       }''');p.write_text(s)
p=r/'port/soberui/modules/Hud.java';s=p.read_text().replace('"Chat Editing", "Drag HUD cards while the chat is open, scroll on a card to resize it and right click to reset its size.", true','"Legacy Chat Editing", "HUD layout is now edited only in the dedicated HUD Editor screen.", false');p.write_text(s)
# No chat injections: they should not consume chat input even if a stale config enables the old setting.
p=r/'port/soberui/mixin/ChatScreenMixin.java';p.write_text('''package net.albiz.client.port.soberui.mixin;
@org.spongepowered.asm.mixin.Mixin(net.minecraft.class_408.class)
public abstract class ChatScreenMixin { /* Editing moved to HudEditorScreen. Chat is not intercepted. */ }
''')
