package net.albiz.client.module.impl.render;
import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.minecraft.*;
/** Assets adapted from SoberClient, Violet, GPL-3.0-only; see THIRD_PARTY_PORTS.md. */
public final class CustomTotemsModule extends ClientModule {
 private static final String[] STYLES={"Default","Amethyst Blob","Antlers","Aqua","Batman","Blaze","Blue Eyes","Blush","Bright Hd","Bubblegum","Cat","Cherry","Classic Ash","Classic Dusk","Classic Hd","Copper","Coral","Crimson","Crystal","Dark Figure","Dark Statue","Demon","Demon Eyes","Ember","Emerald","Emerald Gaze","Ender","Frost","Frost Ghost","Gentleman","Ghost","Glacier","Gold","Gray Figure","Green Gaze","Hellspawn","Honey","Ice","Inferno","Jade","Lavender","Lilac","Lilac Figure","Lime","Magenta","Marble","Mini","Mint","Mint Gaze","Monkey","Navy","Neon Lime","Nightshade","Ocean Gaze","Orchid","Pale Emerald","Pale Watcher","Peach","Pearl","Pink Figure","Pink Gaze","Pink Ghost","Pink Robot","Pocket","Polar","Purple Gaze","Purple Hd","Red Eyes","Redstone","Robot","Rose","Rose Gaze","Royal Violet","Ruby","Sakura","Salmon","Santa","Sapphire","Shade","Shadow","Silver","Skull","Sky","Slime","Snow","Snowman","Spirit","Statue","Steve","Stone Statue","Sunset","Teal","Tiny","Violet Eyes","Void","Wisp","Xkisses Pink Blue","Zombie"};
 private final ModeSetting style=addSetting("style",new ModeSetting("Style","Default",STYLES));
 private final ActionSetting visualPicker=addSetting("visual-picker",new ActionSetting("Visual Selection",()->{var mc=class_310.method_1551();var view=net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.TotemView.class);if(view!=null)mc.method_1507(new net.albiz.client.port.soberui.gui.VisualPresetsScreen(mc.field_1755,view,()->{}));}));
 public CustomTotemsModule(){super("custom-totems","Custom Totems","Changes the totem model locally in hand, inventory and pop animation.",ModuleCategory.VISUALS);}
 public static class_2960 model(class_1799 stack,class_2960 original){
  var m=ModuleRegistry.get(CustomTotemsModule.class);
  if(m==null||!m.isEnabled()||m.style.is("Default")||!stack.method_31574(class_1802.field_8288)||!original.equals(class_2960.method_60654("minecraft:totem_of_undying")))return original;
  return class_2960.method_60655("albiz","totem/"+m.style.get().toLowerCase(java.util.Locale.ROOT).replace(' ','_'));
 }
}
