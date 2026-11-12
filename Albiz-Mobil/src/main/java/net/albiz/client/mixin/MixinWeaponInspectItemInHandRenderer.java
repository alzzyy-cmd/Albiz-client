package net.albiz.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.albiz.client.module.impl.render.HandModelModule;
import net.albiz.client.module.impl.render.WeaponInspectModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_759.class})
public abstract class MixinWeaponInspectItemInHandRenderer {
   @Inject(
      method = {"method_22976"},
      at = {@At("HEAD")}
   )
   private void albiz$inspectHands(float partialTick, class_4587 poseStack, class_11659 collector, class_746 player, int light, CallbackInfo ci) {
      net.albiz.client.port.soberui.modules.CsPickaxe.handsBegin(poseStack);
      if (WeaponInspectModule.active != null) {
         WeaponInspectModule.handsBegin(poseStack);
      }
   }

   @WrapOperation(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"
      )}
   )
   private void albiz$inspectItem(
      class_759 renderer,
      class_1309 entity,
      class_1799 stack,
      class_811 context,
      class_4587 poseStack,
      class_11659 collector,
      int light,
      Operation<Void> original,
      @Local(argsOnly = true) class_1268 hand
   ) {
      boolean mainHand = hand == class_1268.field_5808;
      var models=net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.CustomModels.class);
      var picks=net.albiz.client.port.soberui.SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.CsPickaxe.class);
      if((models!=null&&models.isActive())||(picks!=null&&picks.isActive())){
         boolean replaced=false;poseStack.method_22903();
         try{replaced=models!=null&&models.isActive()&&models.render(stack,context,poseStack,collector,light);if(!replaced)replaced=net.albiz.client.port.soberui.modules.CsPickaxe.renderFirstPerson(entity,stack,context,poseStack,collector,light);}
         catch(RuntimeException e){net.albiz.client.port.soberui.SoberClient.LOG.warn("Item visual rendering failed; using the original renderer",e);}
         finally{poseStack.method_22909();}
         if(replaced)return;
      }
      if (WeaponInspectModule.active != null) {
         if (!WeaponInspectModule.renderItem(poseStack, entity, stack, context, collector, light, mainHand)) {
            original.call(new Object[]{renderer, entity, stack, context, poseStack, collector, light});
         }
      } else {
         if (HandModelModule.active != null) {
            HandModelModule.renderArm(poseStack, entity, stack, context, collector, light, mainHand);
         }

         original.call(new Object[]{renderer, entity, stack, context, poseStack, collector, light});
      }
   }
}
