package net.albiz.client.easyplace.util;

import java.util.Locale;
import net.albiz.client.easyplace.EasyPlace;
import net.albiz.client.easyplace.placement.PlacementState;
import net.albiz.client.easyplace.placement.PlacementTarget;
import net.albiz.client.easyplace.sync.ServerAckManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class EasyPlaceDebug {
   private EasyPlaceDebug() {
   }

   public static void log(String event, PlacementState state, PlacementTarget placement, float[] rotation, ServerAckManager.Result ack, int retry) {
      class_746 player = class_310.method_1551().field_1724;
      float playerYaw = player != null ? player.method_36454() : Float.NaN;
      float playerPitch = player != null ? player.method_36455() : Float.NaN;
      EasyPlace.LOG
         .info(
            "EasyPlace Debug | event={} | Face={} | PlaceSide={} | Yaw={} | Pitch={} | PlayerYaw={} | PlayerPitch={} | State={} | ACK={} | Retry={}",
            new Object[]{
               event,
               placement == null ? "-" : placement.clickedFace(),
               placement == null ? "-" : placement.placeSide(),
               rotation == null ? "-" : String.format(Locale.ROOT, "%.2f", rotation[0]),
               rotation == null ? "-" : String.format(Locale.ROOT, "%.2f", rotation[1]),
               String.format(Locale.ROOT, "%.2f", playerYaw),
               String.format(Locale.ROOT, "%.2f", playerPitch),
               state,
               ack,
               retry
            }
         );
   }
}
