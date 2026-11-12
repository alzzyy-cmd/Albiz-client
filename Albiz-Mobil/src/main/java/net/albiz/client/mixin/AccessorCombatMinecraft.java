package net.albiz.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public interface AccessorCombatMinecraft {
   @Accessor("field_1752")
   int albiz$rightClickDelay();

   @Accessor("field_1752")
   void albiz$setRightClickDelay(int var1);
}
