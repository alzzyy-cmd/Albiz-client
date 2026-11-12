package net.albiz.client.gui;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import net.albiz.client.module.setting.*;
class EditorNavigationTest {
 private static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 private static Object get(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 @Test void blockColorBackReturnsToPickerAndCloseCascades()throws Exception{
  var editor=new BlockListEditor();var setting=new BlockColorListSetting("Blocks",java.util.List.of(),new float[]{1,1,1,1});
  set(editor,"picker",setting);set(editor,"colorList",setting);
  assertTrue(editor.isEditorOpen());assertTrue(editor.closeWindow());assertSame(setting,get(editor,"picker"));assertNull(get(editor,"colorList"));
  set(editor,"colorList",setting);editor.close();assertFalse(editor.isEditorOpen());assertNull(get(editor,"colorList"));
 }
 @Test void itemPickerCloseClearsChildren()throws Exception{
  var editor=new ItemPickerEditor();set(editor,"itemTarget",new StringSetting("Item",""));assertTrue(editor.isEditorOpen());assertTrue(editor.closeWindow());assertFalse(editor.isEditorOpen());
 }
 @Test void paletteBackLeavesParentUntilSecondBack()throws Exception{
  var editor=new ItemPaletteEditor((dl,id,x,y,size)->{});editor.open(new ItemColorListSetting("Colors",java.util.List.of(),new float[]{1,1,1,1}));
  ((imgui.type.ImBoolean)get(editor,"colorOpen")).set(true);assertTrue(editor.back());assertTrue(editor.isOpen());assertTrue(editor.back());assertFalse(editor.isOpen());
 }
 @Test void screenRemovalDoesNotJustBackOneLayer()throws Exception{
  String screen=java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/net/albiz/client/gui/ImGuiScreen.java"));assertTrue(screen.contains("this.clickGui.onRemoved();"));
  String renderer=java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/net/albiz/client/gui/ClickGuiRenderer.java"));assertTrue(renderer.contains("else if(!editorPage)"));assertTrue(renderer.contains("if(!editorPage && !this.editorPageOpen())"));
 }
}
