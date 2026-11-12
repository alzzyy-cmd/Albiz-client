package net.albiz.client.module.impl.render;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class WolfCrestGeometryTest {
 private String positions(SteppeMesh mesh)throws Exception{var f=SteppeMesh.class.getDeclaredField("pos");f.setAccessible(true);return Arrays.toString((float[])f.get(mesh));}
 @Test void allFiveHelmetsDiffer()throws Exception{Set<String> all=new HashSet<>();for(int i=0;i<5;i++)assertTrue(all.add(positions(SteppeMesh.head(i,5,false))));}
 @Test void eightCapePatternsDiffer()throws Exception{Set<String> all=new HashSet<>();for(int i=5;i<13;i++)assertTrue(all.add(positions(SteppeMesh.body(i,5,false,true))));}
 @Test void capesDoNotAddHelmets(){for(int i=5;i<13;i++)assertTrue(SteppeMesh.head(i,5,false).isEmpty());}
 @Test void wolfSetHasChestAndMask(){assertFalse(SteppeMesh.head(13,5,false).isEmpty());assertFalse(SteppeMesh.body(13,5,false,true).isEmpty());}
}
