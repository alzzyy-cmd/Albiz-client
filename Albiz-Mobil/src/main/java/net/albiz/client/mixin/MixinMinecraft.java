package net.albiz.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.albiz.client.event.EventBus;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.gui.AlbizTitleScreen;
import net.albiz.client.module.impl.misc.AutoEatModule;
import net.albiz.client.util.PerformanceBudget;
import net.albiz.client.util.pvp.ServerRotation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_155;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_638;
import net.minecraft.class_239.class_240;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public abstract class MixinMinecraft {
   @Shadow
   public class_638 field_1687;

   @Shadow
   public abstract void method_1507(class_437 var1);

   @Inject(
      method = {"method_1507"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void albiz$replaceTitleScreen(class_437 screen, CallbackInfo ci) {
      boolean firstTitle=screen instanceof class_442 || screen==null && this.field_1687==null;
      if(firstTitle && !net.albiz.client.i18n.LanguageChoice.chosen()){
         ci.cancel();this.method_1507(new net.albiz.client.gui.LanguageChoiceScreen());return;
      }
      if(screen instanceof net.minecraft.class_500 && !(screen instanceof net.albiz.client.port.soberui.gui.MultiplayerGridScreen)){
         ci.cancel();this.method_1507(net.albiz.client.port.soberui.gui.MultiplayerGridScreen.replace((net.minecraft.class_500)screen));return;
      }
      if (!(screen instanceof net.albiz.client.port.soberui.gui.SoberScreen)) {
         boolean title = screen instanceof class_442 || screen == null && this.field_1687 == null;
         if (title && net.albiz.client.port.soberui.SoberUi.customTitleEnabled()) {
            ci.cancel();
            this.method_1507(net.albiz.client.port.soberui.SoberUi.title());
         }
      }
   }

   @Inject(
      method = {"method_24287"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void albiz$windowTitle(CallbackInfoReturnable<String> cir) {
      if (AlbizTitleScreen.enabled()) {
         cir.setReturnValue("Minecraft " + class_155.method_16673().comp_4025() + " - Albız Client");
      }
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("HEAD")}
   )
   private void albiz$onTickStart(CallbackInfo ci) {
      PerformanceBudget.beginTick();
      EventBus.INSTANCE.drainAsync();
      ServerRotation.startTick();
      TickEvent.dispatch();
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("RETURN")}
   )
   private void albiz$onTickEnd(CallbackInfo ci) {
      TickEvent.dispatchPost();
   }

   @ModifyExpressionValue(
      method = {"method_1583"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_239;method_17783()Lnet/minecraft/class_239$class_240;"
      )}
   )
   private class_240 albiz$autoEatIgnoreCrosshair(class_240 type) {
      return AutoEatModule.isEating() ? class_240.field_1333 : type;
   }
}
