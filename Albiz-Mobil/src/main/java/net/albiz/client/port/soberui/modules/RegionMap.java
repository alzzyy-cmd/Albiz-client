package net.albiz.client.port.soberui.modules;
import net.albiz.client.module.ModuleRegistry;import net.albiz.client.module.impl.client.RegionMapModule;
public final class RegionMap extends net.albiz.client.port.soberui.module.Module{
 public RegionMap(){super("Region Map","",net.albiz.client.port.soberui.module.Category.CLIENT);}
 private RegionMapModule target(){return ModuleRegistry.get(RegionMapModule.class);}
 public boolean isActive(){return target()!=null&&target().isEnabled();}
 public float cellSize(){return target()==null?22:target().squareSize();}public float cellGap(){return 2;}
 public float playerScale(){return target()==null?1:target().playerScale();}public float textScale(){return target()==null?1:target().textScale();}
 public float opacity(){return target()==null?1:target().opacity();}public boolean shadow(){return target()==null||target().shadow();}
 public boolean showRegionIds(){return target()==null||target().showRegionIds();}public boolean showLegend(){return target()==null||target().showLegend();}public boolean showCoordinates(){return target()==null||target().showCoordinates();}
 public net.albiz.client.module.setting.ColorSetting playerIndicatorColor(){return target()==null?new net.albiz.client.module.setting.ColorSetting("Player Indicator",0xffff3333):target().playerIndicatorColor();}
}
