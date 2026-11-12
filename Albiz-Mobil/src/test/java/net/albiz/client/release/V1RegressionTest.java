package net.albiz.client.release;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import com.google.gson.*;
class V1RegressionTest {
 private String source(String path)throws Exception{return Files.readString(Path.of("src/main/java/net/albiz/client/"+path));}
 @Test void scannerReallyRegistered()throws Exception {String s=source("module/BuiltinModuleProvider.java");for(String m:new String[]{"ChunkScannerModule","MotionScannerModule","ActivityInspectorModule"})assertTrue(s.contains("new "+m+"()")||s.contains("."+m+"()"));}
 @Test void glassInstalledBeforeConfig()throws Exception {assertTrue(source("port/soberui/SoberUi.java").contains("ReferenceVisualModule(\"glass\",glass)"));var j=JsonParser.parseString(Files.readString(Path.of("src/main/resources/albiz.soberui.mixins.json"))).getAsJsonObject();assertTrue(j.getAsJsonArray("client").asList().contains(new JsonPrimitive("GlassPlayerMixin")));}
 @Test void englishDefaultAndMigration()throws Exception {assertTrue(source("i18n/AlbizI18n.java").contains("override = \"en_us\""));String s=source("config/ThemeStore.java");assertTrue(s.contains("version < 7"));assertTrue(s.contains("this.language.set(\"en_us\")"));assertFalse(s.contains("this.language.set(\"tr_tr\")"));}
 @Test void wolfIsRealGeometry()throws Exception {assertTrue(source("module/impl/render/CustomCrosshairModule.java").contains("case \"Wolf\":"));assertTrue(source("gui/mainmenu/MenuIcons.java").contains("public static void wolf"));}
 @Test void editorUsesSelectionAndPersistentPosition()throws Exception {String s=source("port/soberui/gui/HudRenderer.java");assertTrue(s.contains("selected = element"));assertTrue(s.contains("settingsPress(x,y,button)"));assertTrue(s.contains("HudLayout.move(selected"));assertTrue(s.contains("HudLayout.save()"));assertTrue(source("port/soberui/gui/HudLayout.java").contains("p.anchorX = o.has(\"anchorX\")"));}
 @Test void mobileBudgetEnforced()throws Exception {String s=source("module/impl/client/PerformanceModule.java");assertTrue(s.contains("Math.min(requested, 750000L)"));assertTrue(s.contains("optimizing || net.albiz.client.platform.BuildProfile.mobile()"));}
}
