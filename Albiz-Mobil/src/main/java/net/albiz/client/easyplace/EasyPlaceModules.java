package net.albiz.client.easyplace;

import java.util.List;
import net.albiz.client.easyplace.builder.SchematicBuilderModule;
import net.albiz.client.easyplace.module.EasyPlaceModule;
import net.albiz.client.module.ClientModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class EasyPlaceModules {
   private EasyPlaceModules() {
   }

   public static boolean available() {
      FabricLoader loader = FabricLoader.getInstance();
      return loader.isModLoaded("litematica") && loader.isModLoaded("malilib");
   }

   public static List<ClientModule> create() {
      return List.of(new EasyPlaceModule(), new SchematicBuilderModule());
   }
}
