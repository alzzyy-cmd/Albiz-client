package net.albiz.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_368;
import net.minecraft.class_368.class_369;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(
   targets = {"net/minecraft/class_374$class_375"}
)
public interface AccessorHudToastInstance {
   @Accessor("field_2241")
   class_368 albiz$hudToast();

   @Accessor("field_52787")
   int albiz$hudFirstSlot();

   @Accessor("field_2244")
   class_369 albiz$hudVisibility();

   @Accessor("field_52789")
   float albiz$hudVisiblePortion();
}
