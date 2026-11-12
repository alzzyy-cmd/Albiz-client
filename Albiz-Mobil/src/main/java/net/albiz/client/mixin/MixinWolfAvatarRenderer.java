package net.albiz.client.mixin;

import net.albiz.client.module.impl.render.WolfCrestLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_11901;
import net.minecraft.class_591;
import net.minecraft.class_922;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_1007.class})
public abstract class MixinWolfAvatarRenderer<T extends class_11890 & class_11901> extends class_922<T, class_10055, class_591> {
   protected MixinWolfAvatarRenderer(class_5618 context, class_591 model, float shadowRadius) {
      super(context, model, shadowRadius);
   }

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void albiz$addHeadCosmetics(class_5618 context, boolean slim, CallbackInfo ci) {
      this.method_4046(new WolfCrestLayer(this));
   }
}
