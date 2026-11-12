package net.albiz.client.module.impl.render;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.albiz.client.module.impl.render.character.kit.*;
class HeroGeometryTest {
 private BakedDesign hero(String id){return BakedDesign.builtIn(id,id,"/assets/albiz/characters/"+id+".anc");}
 private void check(CharacterDesign design){
  var model=CharacterBuilder.build(design,Detail.LOW);assertTrue(model.triangles()>100);assertTrue(model.triangles()<30000);assertEquals(30f,model.maxY,.01f);assertTrue(model.minY<1);
  for(Bone bone:Bone.values())assertNotNull(model.part(bone));
  for(var part:model.parts)for(var surface:new Surface[]{part.base,part.outline,part.decals})if(surface!=null)for(float v:surface.pos)assertTrue(Float.isFinite(v));
 }
 @Test void gutsBuildsActualBoundedMesh(){check(hero("guts"));}
 @Test void subaruBuildsActualBoundedMesh(){check(hero("subaru"));}
 @Test void gutsSwordHasWideLongBladeAndStableInspect()throws Exception{
  var mesh=KnifeModels.gutsSword();var field=KnifeMesh.class.getDeclaredField("pos");field.setAccessible(true);float[] pos=(float[])field.get(mesh);assertTrue(pos.length>100);
  float minX=999,maxX=-999,minY=999,maxY=-999;for(int i=0;i<pos.length;i+=3){minX=Math.min(minX,pos[i]);maxX=Math.max(maxX,pos[i]);minY=Math.min(minY,pos[i+1]);maxY=Math.max(maxY,pos[i+1]);}
  assertTrue(maxX-minX>80);assertTrue(maxY-minY>=20);var skin=KnifeSkin.of("Guts Sword");assertNotNull(skin);float[] sample=new float[16];skin.inspect.sample(.5f,sample);for(float v:sample)assertTrue(Float.isFinite(v));
 }
 @Test void existingKnifeModelsStillBuild(){for(String name:new String[]{"Butterfly","Karambit","Katana"})assertNotNull(KnifeSkin.of(name));}
 @Test void bothHeroesAlsoBuildAtDefaultMediumDetail(){for(CharacterDesign d:new CharacterDesign[]{hero("guts"),hero("subaru")}){var m=CharacterBuilder.build(d,Detail.MEDIUM);assertTrue(m.triangles()>100);assertTrue(m.triangles()<60000);assertNotNull(m.part(Bone.HEAD));}}
}
