package net.albiz.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientBrand {
   public static final String NAME = "Albiz";
   public static final String WINDOW_TITLE = "Albız Client";
   public static final String LOGGER_NAME = "Albiz";

   private ClientBrand() {
   }
}
