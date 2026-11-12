package net.albiz.client.module.setting;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonPrimitive;
import static org.junit.jupiter.api.Assertions.*;
class ModeAliasTest {
 @Test void oldHorusAliasesReadButOnlyAlzzyyIsWritten() {
  for(String old:new String[]{"Horus","horus","Wolf Halo","Double Tap"}) {
   ModeSetting s=new ModeSetting("Style","Circle","Circle","alzzyy");
   s.deserialize(new JsonPrimitive(old));assertEquals("alzzyy",s.get());assertEquals("alzzyy",s.serialize().getAsString());
  }
 }
 @Test void changingPresentationDoesNotChangeModeValueOrId() {
  ModeSetting s=new ModeSetting("Method","ALBIZ+ Scan","ALBIZ+ Scan","Light");
  s.deserialize(new JsonPrimitive("ANBS+ Scan"));assertEquals("ALBIZ+ Scan",s.get());assertEquals("method",s.id());
 }
 @Test void unrelatedDoubleTapModeIsNotRenamed() {
  ModeSetting s=new ModeSetting("Style","Normal","Normal","Double Tap");
  s.deserialize(new JsonPrimitive("Double Tap"));assertEquals("Double Tap",s.get());
 }
}
