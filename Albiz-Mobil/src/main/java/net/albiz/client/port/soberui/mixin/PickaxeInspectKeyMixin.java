// Adapted from SoberClient, Violet, GPL-3.0-only. Only the pickaxe inspect key is filtered.
package net.albiz.client.port.soberui.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_304.class) public abstract class PickaxeInspectKeyMixin{
 private static boolean blocks(class_3675.class_306 key){return key!=null&&key.method_1442()==class_3675.class_307.field_1668&&class_310.method_1551().field_1755==null&&net.albiz.client.port.soberui.modules.CsPickaxe.blocks(key.method_1444());}
 @Inject(method="method_1420",at=@At("HEAD"),cancellable=true)private static void press(class_3675.class_306 key,CallbackInfo ci){if(blocks(key))ci.cancel();}
 @Inject(method="method_1416",at=@At("HEAD"),cancellable=true)private static void hold(class_3675.class_306 key,boolean pressed,CallbackInfo ci){if(pressed&&blocks(key))ci.cancel();}
}
