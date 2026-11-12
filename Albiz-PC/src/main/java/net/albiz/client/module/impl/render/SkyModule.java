package net.albiz.client.module.impl.render;
import java.util.*;import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.albiz.client.event.*;import net.albiz.client.event.impl.TickEvent;import net.albiz.client.port.sober.sky.*;
public final class SkyModule extends ClientModule {
 private final ModeSetting style=addSetting("style",new ModeSetting("Sky Style","AURORA",Arrays.stream(SkyType.values()).filter(x->x!=SkyType.VANILLA&&x!=SkyType.SOBER).map(Enum::name).toArray(String[]::new)));
 private final NumberSetting brightness=addSetting("brightness",new NumberSetting("Brightness",100,10,180).step(5));
 private final NumberSetting speed=addSetting("speed",new NumberSetting("Speed",60,0,200).step(5));
 private final ModeSetting quality=addSetting("quality",new ModeSetting("Quality",net.albiz.client.platform.PlatformInfo.isMobile()?"Performance":"Balanced","Performance","Balanced","Ultra"));
 private final ColorSetting aurora=addSetting("aurora",new ColorSetting("Aurora Color",0xff41d9a2));
 private final BooleanSetting clouds=addSetting("clouds",new BooleanSetting("Hide Vanilla Clouds",true));
 private final BooleanSetting fog=addSetting("fog",new BooleanSetting("Match Fog",true));
 public SkyModule(){super("sky","Sky","Animated skies with quality control and a vanilla fallback if a shader fails.",ModuleCategory.VISUALS);}
 protected void onEnable(){apply();}protected void onDisable(){SkySettings.sky=SkyType.VANILLA;}
 @EventHandler public void tick(TickEvent e){apply();}
 private void apply(){SkySettings.sky=SkyType.valueOf(style.get());SkySettings.brightness=Math.round(brightness.get());SkySettings.speed=Math.round(speed.get());SkySettings.quality=quality.is("Ultra")?100:quality.is("Balanced")?50:25;SkySettings.auroraColor=aurora.rgb();SkySettings.matchFog=fog.get();SkySettings.hideVanillaClouds=clouds.get();}
}
