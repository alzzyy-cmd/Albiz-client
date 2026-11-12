package net.albiz.client.port.soberui.model;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
/** Exercises the exact production contour builder without initializing a GPU. */
class SpriteRegressionTest {
 @Test void closedContourStitchesEverySegment(){var s=new SpriteSegments();s.add(0,0,1,0);s.add(1,0,1,1);s.add(1,1,0,1);s.add(0,1,0,0);var chains=s.chains();assertEquals(1,chains.size());assertEquals(10,chains.getFirst().length);}
 @Test void disconnectedContoursStaySeparate(){var s=new SpriteSegments();s.add(0,0,1,0);s.add(3,0,4,0);assertEquals(2,s.chains().size());}
 @Test void branchingEndpointDoesNotDropSegments(){var s=new SpriteSegments();for(int i=0;i<20;i++)s.add(0,0,i+1,1);assertEquals(20,s.chains().stream().mapToInt(c->c.length/2-1).sum());}
 @Test void longContourGrowsBuffersSafely(){var s=new SpriteSegments();for(int i=0;i<2048;i++)s.add(i,0,i+1,0);var chains=s.chains();assertEquals(1,chains.size());assertEquals(4098,chains.getFirst().length);}
 @Test void everyDeclaredFortnitePngProducesFiniteContour()throws Exception{
   String root="/assets/albiz/models/fortnite_pack/";try(var stream=getClass().getResourceAsStream(root+"items.txt")){assertNotNull(stream);var names=new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).lines().filter(n->!n.isBlank()).toList();assertEquals(24,names.size());
   for(String name:names){try(var in=getClass().getResourceAsStream(root+name.trim()+".png")){assertNotNull(in,name);byte[] encoded=in.readAllBytes();SpritePngBudget.check(encoded);var image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(encoded));assertNotNull(image,name);int w=image.getWidth(),h=image.getHeight();float[] alpha=new float[w*h];for(int y=0;y<h;y++)for(int x=0;x<w;x++)alpha[y*w+x]=(image.getRGB(x,y)>>>24)/255f;alpha=SpriteGeometryBuilder.soften(alpha,w,h,1.1f);var mesh=new SpriteGeometryBuilder.Geometry(64);SpriteGeometryBuilder.contour(mesh,alpha,w,h,.5208333f,.47916666f);assertTrue(mesh.count>0,name);assertTrue(mesh.count<60000,name);for(int i=0;i<mesh.count*12;i++){assertTrue(Float.isFinite(mesh.p[i]),name);assertTrue(Float.isFinite(mesh.n[i]),name);}}}
   }
 }
 @Test void invalidOrHugePngIsRejectedBeforeNativeDecode()throws Exception{
   byte[] header=new byte[24];byte[] signature={(byte)137,80,78,71,13,10,26,10};System.arraycopy(signature,0,header,0,8);header[12]=73;header[13]=72;header[14]=68;header[15]=82;var data=java.nio.ByteBuffer.wrap(header);data.putInt(16,128);data.putInt(20,128);assertDoesNotThrow(()->SpritePngBudget.check(header));data.putInt(16,2049);assertThrows(java.io.IOException.class,()->SpritePngBudget.check(header));data.putInt(16,-1);assertThrows(java.io.IOException.class,()->SpritePngBudget.check(header));assertThrows(java.io.IOException.class,()->SpritePngBudget.check(new byte[24]));assertThrows(java.io.IOException.class,()->SpritePngBudget.check(new byte[3]));
 }
}
