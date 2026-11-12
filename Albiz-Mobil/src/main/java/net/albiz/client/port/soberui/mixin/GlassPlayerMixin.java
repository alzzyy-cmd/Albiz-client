// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.albiz.client.port.soberui.modules.Glass;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(class_922.class)
public abstract class GlassPlayerMixin {
 @WrapOperation(method="method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",at=@At(value="INVOKE",target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"),require=0)
 private void albiz$glass(class_11659 queue,class_3879<?> model,Object state,class_4587 matrices,class_1921 layer,int light,int overlay,int tint,class_1058 sprite,int outline,class_11683.class_11792 crumble,Operation<Void> original){
  if(state instanceof class_10055 p&&Glass.applies(p)){layer=Glass.layer(p);tint=Glass.tint();}
  original.call(queue,model,state,matrices,layer,light,overlay,tint,sprite,outline,crumble);
 }
 @WrapOperation(method="method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",at=@At(value="INVOKE",target="Lnet/minecraft/class_3887;method_4199(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10017;FF)V"),require=0)
 private void albiz$gear(class_3887<?,?> feature,class_4587 matrices,class_11659 queue,int light,class_10017 state,float angle,float distance,Operation<Void> original){
  Glass.gear(!(feature instanceof class_989)&&Glass.glassGear(state));
  try{original.call(feature,matrices,queue,light,state,angle,distance);}finally{Glass.gear(false);}
 }
}
