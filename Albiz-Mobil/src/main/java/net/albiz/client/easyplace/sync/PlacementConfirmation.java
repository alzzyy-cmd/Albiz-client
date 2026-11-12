package net.albiz.client.easyplace.sync;

import net.albiz.client.easyplace.schematic.SchematicBlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

@Environment(EnvType.CLIENT)
public record PlacementConfirmation(
   class_2338 pos, SchematicBlockState expected, long sentTick, long deadlineTick, int retry, int sequence, class_2680 predicted
) {
}
