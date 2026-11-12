package net.albiz.client.gui;
/** Render-safe bridge; headless UI tests do not bootstrap Minecraft/audio. */
public final class UiFeedback {
 private static Runnable slider=()->{};
 private UiFeedback(){}
 public static void installSlider(Runnable callback){slider=java.util.Objects.requireNonNull(callback);}
 public static void sliderChanged(){slider.run();}
}
