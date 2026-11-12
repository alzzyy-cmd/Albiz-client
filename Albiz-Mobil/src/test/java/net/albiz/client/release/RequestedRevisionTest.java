package net.albiz.client.release;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;import net.albiz.client.i18n.LanguageChoiceFile;import net.albiz.client.util.UndergroundRules;
class RequestedRevisionTest {
 @TempDir Path temp;
 @Test void strictBelowZero(){assertTrue(UndergroundRules.underground(-.01));assertTrue(UndergroundRules.underground(-64));assertFalse(UndergroundRules.underground(0));assertFalse(UndergroundRules.underground(Double.NaN));assertFalse(UndergroundRules.underground(Double.NEGATIVE_INFINITY));}
 @Test void kelpBothFormsOnly(){assertTrue(UndergroundRules.kelp("kelp"));assertTrue(UndergroundRules.kelp("kelp_plant"));assertFalse(UndergroundRules.kelp("seagrass"));}
 @Test void crafterWorkNotIdle(){assertTrue(UndergroundRules.workingCrafter("crafter","[crafting=true,triggered=false]"));assertTrue(UndergroundRules.workingCrafter("crafter","[triggered=true]"));assertFalse(UndergroundRules.workingCrafter("crafter","[crafting=false,triggered=false]"));assertFalse(UndergroundRules.workingCrafter("chest","[crafting=true]"));}
 @Test void soundClassificationNotAmbientProof(){assertTrue(UndergroundRules.crafterSound("minecraft:block.crafter.craft"));assertFalse(UndergroundRules.crafterSound("minecraft:block.furnace.fire_crackle"));}
 @Test void firstRunMissing(){assertTrue(LanguageChoiceFile.read(temp.resolve("new/choice.txt")).isEmpty());}
 @Test void persistsTurkishAndEnglish()throws Exception{Path p=temp.resolve("nested/language.txt");LanguageChoiceFile.write(p,"tr_tr");assertEquals("tr_tr",LanguageChoiceFile.read(p).orElseThrow());LanguageChoiceFile.write(p,"en_us");assertEquals("en_us",LanguageChoiceFile.read(p).orElseThrow());}
 @Test void invalidDoesNotOverwrite()throws Exception{Path p=temp.resolve("choice.txt");LanguageChoiceFile.write(p,"tr_tr");assertThrows(IllegalArgumentException.class,()->LanguageChoiceFile.write(p,"xx"));assertEquals("tr_tr",LanguageChoiceFile.read(p).orElseThrow());}
 @Test void corruptChoiceReopensGate()throws Exception{Path p=temp.resolve("choice.txt");Files.writeString(p,"bad");assertTrue(LanguageChoiceFile.read(p).isEmpty());}
 @Test void noTemporaryFileLeft()throws Exception{Path p=temp.resolve("choice.txt");LanguageChoiceFile.write(p,"en_us");try(var files=Files.list(temp)){assertEquals(1,files.count());}}
 private String source(String path)throws Exception{return Files.readString(Path.of("src/main/java/net/albiz/client/"+path));}
 @Test void chatIsNoLongerIntercepted()throws Exception {String chat=source("port/soberui/mixin/ChatScreenMixin.java");assertFalse(chat.contains("@Inject"));assertTrue(source("port/soberui/gui/HudRenderer.java").contains("instanceof net.albiz.client.gui.HudEditorScreen"));}
 @Test void mobileUsesProfileEvenOnDesktopJvm()throws Exception {assertTrue(source("platform/PlatformInfo.java").contains("BuildProfile.mobile() || switch"));}
 @Test void renderingBudgetSharedAcrossModules()throws Exception {String s=source("port/sober/render/Render3D.java");assertTrue(s.contains("static int sharedPrimitives"));assertTrue(s.contains("Render3DEvent.frame()"));}
 @Test void audioIsUserAssetAndThrottled()throws Exception {String s=source("module/impl/client/SoundFeedbackModule.java");assertTrue(s.contains("8_000_000_000L"));assertTrue(source("port/sober/modules/ChunkFinder.java").contains("Discovery Sound"));var bytes=Files.readAllBytes(Path.of("src/main/resources/assets/albiz/sounds/alerts/chunk_found.ogg"));assertEquals("OggS",new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII));}
 @Test void languageGatePrecedesCustomMenus()throws Exception {String s=source("mixin/MixinMinecraft.java");assertTrue(s.indexOf("LanguageChoice.chosen()")<s.indexOf("SoberUi.customTitleEnabled()"));String screen=source("gui/LanguageChoiceScreen.java");assertTrue(screen.contains("tr?\"tr\":\"gb\""));}
 @Test void abyzRegisteredAndSeparate()throws Exception {assertTrue(source("module/BuiltinModuleProvider.java").contains("new net.albiz.client.module.impl.render.AbyzDebugModule()"));}
 @Test void suppliedLogoGeometryIsBounded(){var lines=net.albiz.client.util.AlbizLogoGeometry.segments();assertTrue(lines.length>50&&lines.length<=160);for(var l:lines){assertEquals(4,l.length);for(double v:l)assertTrue(Double.isFinite(v)&&Math.abs(v)<=1.1);}}
}
