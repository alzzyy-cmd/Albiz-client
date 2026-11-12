// Adapted from SoberClient, Violet, GPL-3.0-only. Rendering animation only.
package net.albiz.client.port.soberui.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_759.class) public abstract class PickaxeDeployMixin{
 @Shadow private class_1799 field_4047;@Shadow private float field_4043;@Shadow private float field_4053;
 @Inject(method="method_3220",at=@At("TAIL"),require=0)private void deploy(CallbackInfo ci){var player=class_310.method_1551().field_1724;if(player==null)return;var held=player.method_6047();if(!net.albiz.client.port.soberui.modules.CsPickaxe.instantSwap(held))return;this.field_4047=held;this.field_4043=1;this.field_4053=1;}
}
