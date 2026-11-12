package net.albiz.client.port.soberui;
import java.nio.file.*;import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;import static org.junit.jupiter.api.Assertions.*;import net.albiz.client.port.soberui.model.*;
/** CPU geometry/parser tests only; not a renderer or FPS test. */
class ItemVisualsPureTest{
 @TempDir Path dir;
 @Test void everyBuiltinMeshIsFiniteAndBounded(){assertFalse(ModelLibrary.names().isEmpty());for(String n:ModelLibrary.names()){var m=ModelLibrary.build(n);assertNotNull(m,n);assertTrue(m.triangles>0&&m.triangles<=60000,n);assertEquals(m.triangles*9,m.positions.length,n);for(float f:m.positions)assertTrue(Float.isFinite(f),n);}}
 @Test void objTriangleLoads()throws Exception{Path p=dir.resolve("shape.obj");Files.writeString(p,"v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n");assertEquals(1,ModelLoader.load(p).triangles);}
 @Test void unsupportedModelFailsWithoutTryingToRender()throws Exception{Path p=dir.resolve("bad.exe");assertThrows(java.io.IOException.class,()->ModelLoader.load(p));}
 @Test void malformedMeshDoesNotProduceNanModel()throws Exception{Path p=dir.resolve("empty.stl");Files.writeString(p,"solid empty\nendsolid empty\n");assertThrows(java.io.IOException.class,()->ModelLoader.load(p));}
 @Test void importedModelPathCannotEscape()throws Exception{Path folder=Files.createDirectory(dir.resolve("models"));assertThrows(IllegalArgumentException.class,()->VisualModelPaths.resolve(folder,"../passwords.obj"));assertThrows(IllegalArgumentException.class,()->VisualModelPaths.resolve(folder,dir.resolve("outside.obj").toString()));assertEquals(folder.resolve("ok.obj").toAbsolutePath(),VisualModelPaths.resolve(folder,"ok.obj"));}
 @Test void binaryStlTriangleLoads()throws Exception{var bytes=java.nio.ByteBuffer.allocate(134).order(java.nio.ByteOrder.LITTLE_ENDIAN);bytes.putInt(80,1);bytes.putFloat(96,0);bytes.putFloat(100,0);bytes.putFloat(104,0);bytes.putFloat(108,1);bytes.putFloat(112,0);bytes.putFloat(116,0);bytes.putFloat(120,0);bytes.putFloat(124,1);bytes.putFloat(128,0);Path p=dir.resolve("one.stl");Files.write(p,bytes.array());assertEquals(1,ModelLoader.load(p).triangles);}
 @Test void nonFiniteCoordinatesAreRejected(){assertThrows(IllegalArgumentException.class,()->ModelMesh.normalize(new float[]{Float.NaN,0,0,1,0,0,0,1,0},new int[]{-1},1));}
}
