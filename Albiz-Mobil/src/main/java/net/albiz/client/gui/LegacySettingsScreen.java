package net.albiz.client.gui;
import net.minecraft.*;import net.albiz.client.module.ClientModule;
/** Preserve the approved complex icon pickers while the native controls host their parent. */
public final class LegacySettingsScreen extends ImGuiScreen{
 private final class_437 parent;
 public LegacySettingsScreen(class_437 parent,ClientModule target){this.parent=parent;inspect(target);}
 @Override public void method_25419(){class_310.method_1551().method_1507(parent);}
}
