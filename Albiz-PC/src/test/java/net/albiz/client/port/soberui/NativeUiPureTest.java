package net.albiz.client.port.soberui;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import net.albiz.client.port.soberui.setting.*;import net.albiz.client.port.soberui.render.Color;
/** No Minecraft/GL lifecycle is exercised here. */
class NativeUiPureTest{
 @Test void localJsonIsNotMistakenForRemoteShortCode(){assertNull(ConfigShare.shortCode("{\"modules\":{}}"));assertNull(ConfigShare.shortCode("Y1:abcd"));assertNull(ConfigShare.shortCode(null));assertEquals("ABC123",ConfigShare.shortCode("SC-ABC123"));}
 @Test void remoteSharingIsExplicitlyDisabled(){assertTrue(ConfigShare.upload("{}","",java.util.Set.of()).isCompletedExceptionally());assertTrue(ConfigShare.download("ABC123").isCompletedExceptionally());}
 @Test void boolViewDisplayReadsCanonicalValue(){final boolean[] canonical={true};BoolSetting s=new BoolSetting("A","",false){public Boolean get(){return canonical[0];}public void set(Boolean v){canonical[0]=v;}};assertEquals("On",s.display());s.toggle();assertFalse(canonical[0]);assertEquals("Off",s.display());}
 @Test void integerSliderClampsAndResets(){var s=new IntSetting("A","",5,0,10);s.set(99);assertEquals(10,s.get());s.set(-99);assertEquals(0,s.get());s.reset();assertEquals(5,s.get());}
 @Test void colorPackedRoundTripPreservesAlpha(){int argb=0xbba12c36;assertEquals(argb,Color.fromPacked(argb).packed());}
 @Test void soundCallbacksAreNoOpBeforeUiInitialization(){
  assertFalse(SoberUi.isReady());
  var unused=(Object)null;
  net.albiz.client.port.soberui.gui.UiSounds.click();
  net.albiz.client.port.soberui.gui.UiSounds.select();
  net.albiz.client.port.soberui.gui.UiSounds.toggle(true);
  net.albiz.client.port.soberui.gui.UiSounds.open();
  net.albiz.client.port.soberui.gui.UiSounds.close();
  net.albiz.client.port.soberui.gui.UiSounds.hover();
  net.albiz.client.port.soberui.gui.UiSounds.previewHover();
  net.albiz.client.port.soberui.gui.UiSounds.key(1);
  net.albiz.client.port.soberui.gui.UiSounds.keybind(true);
  net.albiz.client.port.soberui.gui.UiSounds.moduleToggle(true);
  net.albiz.client.port.soberui.gui.UiSounds.intro();
  net.albiz.client.port.soberui.gui.UiSounds.reveal();
  assertFalse(SoberUi.isReady());
 }
}
