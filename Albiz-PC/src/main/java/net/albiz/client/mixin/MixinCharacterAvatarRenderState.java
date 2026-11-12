package net.albiz.client.mixin;

import net.albiz.client.module.impl.render.character.CharacterHolder;
import net.albiz.client.module.impl.render.character.WornCharacter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin({class_10055.class})
public abstract class MixinCharacterAvatarRenderState implements CharacterHolder {
   @Unique
   private WornCharacter albiz$character;
   @Unique
   private float albiz$seat;

   @Override
   public WornCharacter albiz$character() {
      return this.albiz$character;
   }

   @Override
   public void albiz$setCharacter(WornCharacter character) {
      this.albiz$character = character;
   }

   @Override
   public float albiz$seat() {
      return this.albiz$seat;
   }

   @Override
   public void albiz$setSeat(float knee) {
      this.albiz$seat = knee;
   }
}
