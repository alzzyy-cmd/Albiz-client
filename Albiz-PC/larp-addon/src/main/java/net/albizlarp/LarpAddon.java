package net.albizlarp;
import net.albiz.client.addon.AlbizAddon;
import net.albiz.client.module.ModuleRegistry;
/** Independent implementation for Albiz's addon ABI. No Meteor runtime dependency. */
public final class LarpAddon extends AlbizAddon {
 public String getPackage(){return "net.albizlarp";}
 public void onInitialize(){ModuleRegistry.add(new LarpV5());ModuleRegistry.add(new LarpV6());ModuleRegistry.add(new LarpV7());}
}
