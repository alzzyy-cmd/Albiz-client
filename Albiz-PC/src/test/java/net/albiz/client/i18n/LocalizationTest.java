package net.albiz.client.i18n;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class LocalizationTest {
    private final TranslationCatalog c=TranslationCatalog.INSTANCE;
    @AfterEach void reset() { AlbizI18n.setLanguage("tr_tr"); }
    @Test void everyCatalogKeyHasTurkishEntry() { for(String key:c.keys()) assertTrue(c.hasTurkish(key),key); }
    @Test void all162ModuleDescriptionsAreTranslated() {
        long count=c.keys().stream().filter(k->k.startsWith("module.")&&k.endsWith(".description")).peek(k->{
            String en=c.text(k,"",false),tr=c.text(k,"",true);
            assertFalse(en.isBlank(),k); assertFalse(tr.isBlank(),k); assertNotEquals(en,tr,k);
        }).count(); assertEquals(162,count);
    }
    @Test void moduleHoverAndFooterTranslateSeparately() {
        String text=c.text("module.crystal-aura.description","",true)+"\nRight-click for settings";
        String result=c.display(text,true);
        assertTrue(result.contains("uç kristallerini"));assertTrue(result.contains("Ayarlar için sağ tıkla"));
        assertFalse(result.contains("Right-click"));
    }
    @Test void settingsAndModesUseTranslatedPresentation() {
        assertEquals("Roket satın al",c.display("Buy Rockets",true));
        assertEquals("Kazma ilerlemesi",c.display("Mining Progress",true));
        assertEquals("Türkçe",c.display("tr_tr",true));
        assertEquals("AÇIK",c.display("ON",true));
    }
    @Test void localeChangesRetainExplicitWidgetIdentity() {
        AlbizI18n.setLanguage("tr_tr"); String tr=AlbizI18n.label("Save all###save");
        AlbizI18n.setLanguage("en_us"); String en=AlbizI18n.label("Save all###save");
        assertTrue(tr.endsWith("###save"));assertTrue(en.endsWith("###save"));
        assertEquals("##raw_id",AlbizI18n.label("##raw_id"));
    }
    @Test void ordinaryWidgetLabelsAlsoRetainIdentityAcrossLocales() {
        AlbizI18n.setLanguage("tr_tr");String tr=AlbizI18n.label("Save all");
        AlbizI18n.setLanguage("en_us");String en=AlbizI18n.label("Save all");
        assertEquals(tr.substring(tr.indexOf("###")),en.substring(en.indexOf("###")));
    }
    @Test void userValuesCommandsAndTechnicalIdsRemainUnchanged() {
        for(String s:new String[]{"Notch","/pay alzzyy 100","minecraft:diamond_ore","keybind","COMBAT","SomeUnknownProfile"}) assertEquals(s,c.display(s,true));
    }
    @Test void englishModeReturnsEnglish() { assertEquals("Save all",c.display("Save all",false)); }
    @Test void dynamicProfileNoticeUsesExternalFragment() { assertEquals("Yüklendi: Test123",c.display("Loaded Test123",true)); }
    @Test void turkishSearchWorksWithAsciiAndDottedCapitalI() {
        assertEquals("isik",SearchText.normalize("IŞIK"));
        assertEquals("isik",SearchText.normalize("İŞIK"));
        assertEquals("gorus acisi",SearchText.normalize("Görüş Açısı"));
        assertEquals("kristal aurasi",SearchText.normalize("Kristal Aurası"));
    }
    @Test void numberedPriceLabelTranslatesBothFragments() {
        String result=c.display("Price 1 (each)",true);
        assertTrue(result.startsWith("Fiyat"),result);assertTrue(result.endsWith("(adet başına)"),result);
    }
    @Test void uppercaseMenuTextUsesTurkishCapitalization() {
        assertEquals(c.display("Multiplayer",true).toUpperCase(java.util.Locale.forLanguageTag("tr-TR")),c.caption("MULTIPLAYER",true));
        assertFalse(c.caption("MULTIPLAYER",true).contains("MULTIPLAYER"));
    }
    @Test void backgroundNoticeUsesTemplateWithoutTranslatingFilename() {
        AlbizI18n.setLanguage("tr_tr");String result=AlbizI18n.backgroundAssigned("Right.png","Main Menu");
        assertTrue(result.contains("Right.png"));assertTrue(result.contains("arka planı"));
        AlbizI18n.setLanguage("en_us");assertEquals("Right.png is now the Main Menu background",AlbizI18n.backgroundAssigned("Right.png","Main Menu"));
    }
 @Test void moduleNamesStayEnglishWhileDescriptionsStayTurkish() {
  long names=c.keys().stream().filter(k->k.startsWith("module.")&&k.endsWith(".name")).peek(k->{
   assertEquals(c.text(k,"",false),c.text(k,"",true),k);
   assertEquals(c.text(k,"",false),c.display(c.text(k,"",false),true),k);
  }).count();assertEquals(153,names);
 }
    @Test void restoredMenuAndNewItemLabelsAreTurkish(){AlbizI18n.setLanguage("tr_tr");assertEquals("Çok Oyunculu",AlbizI18n.display("Multiplayer"));assertEquals("Görsel Seçim",AlbizI18n.display("Visual Selection"));assertEquals("Bıçak Görünümü",AlbizI18n.display("Knife Skin"));assertEquals("Dünya Seç",AlbizI18n.display("Select World"));}
}
