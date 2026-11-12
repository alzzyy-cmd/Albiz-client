package net.albiz.client.easyplace.schematic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

@Environment(EnvType.CLIENT)
public record SchematicTarget(class_2338 pos, SchematicBlockState expected, class_2680 actual, TargetStatus status, double distanceSquared) {
   public SchematicTarget withActual(class_2680 state, TargetStatus status) {
      return new SchematicTarget(this.pos, this.expected, state, status, this.distanceSquared);
   }
}
