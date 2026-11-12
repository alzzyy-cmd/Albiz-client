package net.albiz.client.gui;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FeedbackThrottleTest {
 @Test void sliderCannotPlayEveryFrame(){var gate=new FeedbackThrottle(100);assertTrue(gate.allow(0));for(int i=1;i<100;i++)assertFalse(gate.allow(i));assertTrue(gate.allow(100));}
 @Test void resetAllowsFreshGesture(){var gate=new FeedbackThrottle(100);assertTrue(gate.allow(0));gate.reset();assertTrue(gate.allow(1));}
 @Test void bridgeDoesNotNeedMinecraft(){var count=new java.util.concurrent.atomic.AtomicInteger();try{UiFeedback.installSlider(count::incrementAndGet);UiFeedback.sliderChanged();assertEquals(1,count.get());}finally{UiFeedback.installSlider(()->{});}}
 @Test void oldSoundDefaultsBecomeEnabledOnce(){assertTrue(net.albiz.client.module.impl.client.SoundFeedbackModule.migratedEnabled(false,0));}
 @Test void laterExplicitMuteIsPreserved(){assertFalse(net.albiz.client.module.impl.client.SoundFeedbackModule.migratedEnabled(false,1));assertTrue(net.albiz.client.module.impl.client.SoundFeedbackModule.migratedEnabled(true,1));}
}
