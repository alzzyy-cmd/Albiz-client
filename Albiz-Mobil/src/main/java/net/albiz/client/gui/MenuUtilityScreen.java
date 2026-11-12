package net.albiz.client.gui;
import net.minecraft.*;import net.albiz.client.imgui.ImGuiManager;import net.albiz.client.addon.MenuPanels;import net.albiz.client.gui.mainmenu.CreditsPopup;import net.albiz.client.imgui.GuardedImGui;
/** Existing account/credits panels, without changing native title-screen routing. */
public final class MenuUtilityScreen extends class_437{
 private final class_437 parent;private final MenuPanels.Panel accounts;private final CreditsPopup credits;private boolean missing;
 public MenuUtilityScreen(class_437 parent,boolean showCredits){super(class_2561.method_43470(showCredits?"Credits":"Accounts"));this.parent=parent;credits=showCredits?new CreditsPopup():null;accounts=showCredits?null:MenuPanels.newAccountsPanel();}
 protected void method_25426(){if(credits!=null)credits.open();else if(accounts!=null)accounts.open();else missing=true;ImGuiManager.getInstance().activate(this::paint);}
 private void paint(){var io=GuardedImGui.getIO();float w=io.getDisplaySizeX(),h=io.getDisplaySizeY(),s=Math.max(.65f,h/720f);if(credits!=null)credits.render(GuardedImGui.getForegroundDrawList(),w,h,s,io.getDeltaTime(),io.getMousePosX(),io.getMousePosY(),GuardedImGui.isMouseClicked(0));else if(accounts!=null)accounts.render(w,h,s,io.getDeltaTime());if((credits!=null&&!credits.isOpen())||(accounts!=null&&!accounts.isOpen()))method_25419();}
 public boolean method_25404(class_11908 e){return key(e.comp_4795())||super.method_25404(e);}
 private boolean key(int key){if(key==256){method_25419();return true;}return credits!=null?credits.keyPressed(key):accounts!=null&&accounts.keyPressed(key);}
 public void method_25394(class_332 c,int x,int y,float dt){c.method_25294(0,0,field_22789,field_22790,0xff08090c);if(missing)c.method_25303(field_22787.field_1772,"Accounts component unavailable. Escape: back.",20,20,0xffdddddd);}
 public void method_25432(){ImGuiManager.getInstance().deactivate();}
 public boolean method_25421(){return false;}
 public void method_25419(){field_22787.method_1507(parent);}
}
