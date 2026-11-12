package net.albiz.client.easyplace.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1747;
import net.minecraft.class_1750;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin({class_1747.class})
public interface InvokerBlockItem {
   @Invoker("method_7707")
   class_2680 easyplace$getPlacementState(class_1750 var1);
}
