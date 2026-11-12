package net.albiz.client.module.impl.render.character;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface CharacterHolder {
   WornCharacter albiz$character();

   void albiz$setCharacter(WornCharacter var1);

   float albiz$seat();

   void albiz$setSeat(float var1);
}
