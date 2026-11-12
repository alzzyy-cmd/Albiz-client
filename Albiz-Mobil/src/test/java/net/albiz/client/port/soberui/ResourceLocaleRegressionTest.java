package net.albiz.client.port.soberui;
import java.util.Locale;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ResourceLocaleRegressionTest{
 @Test void resourceIdsRemainAsciiInTurkish(){Locale old=Locale.getDefault();try{Locale.setDefault(Locale.forLanguageTag("tr-TR"));assertEquals("pickaxe",UiResourceNames.lower("PICKAXE"));assertEquals("grid",UiResourceNames.lower("GRID"));assertEquals("minecraft:iron_pickaxe",UiResourceNames.lower("MINECRAFT:IRON_PICKAXE"));assertTrue(UiResourceNames.lower("PICKAXE").matches("[a-z0-9/._-]+"));}finally{Locale.setDefault(old);}}
}
