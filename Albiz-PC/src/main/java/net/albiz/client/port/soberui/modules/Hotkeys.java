package net.albiz.client.port.soberui.modules;
public class Hotkeys extends net.albiz.client.port.soberui.module.Module { public Hotkeys(){super("Hotkeys","",net.albiz.client.port.soberui.module.Category.CLIENT);}  public static boolean shouldShowHud(){var m=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.client.HotkeysModule.class);return m!=null&&m.isEnabled();} }
