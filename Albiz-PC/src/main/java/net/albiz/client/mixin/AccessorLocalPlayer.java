package net.albiz.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({class_746.class})
public interface AccessorLocalPlayer {
   @Accessor("field_3923")
   void albiz$setPositionReminder(int var1);

   @Accessor("field_3919")
   boolean albiz$wasSprinting();

   @Accessor("field_54156")
   class_10185 albiz$lastSentInput();
}
