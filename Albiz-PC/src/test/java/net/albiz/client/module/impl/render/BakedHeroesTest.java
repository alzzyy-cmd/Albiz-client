package net.albiz.client.module.impl.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import net.albiz.client.module.impl.render.character.CharacterCatalog;
import net.albiz.client.module.impl.render.character.kit.*;

class BakedHeroesTest {
 private BakedDesign design(String id){return BakedDesign.builtIn(id,id,"/assets/albiz/characters/"+id+".anc");}
 private CharacterModel model(String id,Detail d){return CharacterBuilder.build(design(id),d);}
 @Test void catalogUsesBakedModelsAndRetainsSavedIds(){
  for(String id:new String[]{"guts","subaru_natsuki"})assertInstanceOf(BakedDesign.class,CharacterCatalog.all().stream().filter(x->x.id().equals(id)).findFirst().orElseThrow());
  assertEquals("Sakura",CharacterCatalog.all().getFirst().name());
 }
 @Test void gutsIsDetailedTexturedMesh(){var m=model("guts",Detail.HIGH);assertEquals(23761,m.triangles());assertEquals(2048,m.atlasWidth);assertEquals(2048,m.atlasHeight);assertTrue(m.atlasMips.length>1);assertTrue(Arrays.stream(m.atlasMips[0]).distinct().limit(2049).count()>2048);}
 @Test void subaruIsDetailedTexturedMesh(){var m=model("subaru",Detail.HIGH);assertEquals(13968,m.triangles());assertEquals(1024,m.atlasWidth);assertEquals(1024,m.atlasHeight);assertTrue(Arrays.stream(m.atlasMips[0]).distinct().limit(2049).count()>2048);}
 @Test void allThreeDetailLevelsLoadWithMonotonicBudgets(){for(String id:new String[]{"guts","subaru"}){int last=0;for(Detail d:Detail.values()){var m=model(id,d);assertTrue(m.triangles()>=last);assertTrue(m.triangles()<=30000);last=m.triangles();assertEquals(30,m.maxY,.01);assertEquals(0,m.minY,.01);for(Bone b:Bone.values())assertNotNull(m.part(b),id+" "+b);}}}
 @Test void bothCharactersHaveArticulatedLimbs(){for(String id:new String[]{"guts","subaru"}){var m=model(id,Detail.HIGH);for(Bone b:new Bone[]{Bone.RIGHT_ARM,Bone.LEFT_ARM,Bone.RIGHT_LEG,Bone.LEFT_LEG}){var p=m.part(b);assertTrue(p.jointed,id+" "+b);assertTrue(p.base.joint!=null||p.decals!=null&&p.decals.joint!=null);}}}
 @Test void allLodsHaveFinitePositionsAndValidTextureCoordinates(){for(String id:new String[]{"guts","subaru"})for(Detail d:Detail.values())for(var p:model(id,d).parts)for(var s:new Surface[]{p.base,p.decals})if(s!=null){for(float v:s.pos)assertTrue(Float.isFinite(v));for(float uv:s.uv)assertTrue(Float.isFinite(uv)&&uv>=0&&uv<=1);for(int q:s.quads)assertTrue(q>=0&&q<s.vertexCount());}}
 @Test void normalsAreUnitLengthAtEveryLod(){for(String id:new String[]{"guts","subaru"})for(Detail d:Detail.values())for(var p:model(id,d).parts)for(var s:new Surface[]{p.base,p.decals})if(s!=null)for(int k=0;k<s.nrm.length;k+=3){double length=Math.sqrt(s.nrm[k]*s.nrm[k]+s.nrm[k+1]*s.nrm[k+1]+s.nrm[k+2]*s.nrm[k+2]);assertTrue(length>.97&&length<1.03,id+" "+d+" bad normal "+length);}}
 @Test void newReferenceCharactersHaveFiniteBoundedLods(){for(String id:new String[]{"banana","robot","cat"}){int last=0;for(Detail d:Detail.values()){var m=model(id,d);assertTrue(m.triangles()>0&&m.triangles()<=30000);assertTrue(m.triangles()>=last);last=m.triangles();for(var p:m.parts)for(var surface:new Surface[]{p.base,p.decals})if(surface!=null){for(float v:surface.pos)assertTrue(Float.isFinite(v));for(float uv:surface.uv)assertTrue(Float.isFinite(uv)&&uv>=0&&uv<=1);}}}}
}
