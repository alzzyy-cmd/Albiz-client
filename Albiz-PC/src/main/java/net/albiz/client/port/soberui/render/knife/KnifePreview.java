package net.albiz.client.port.soberui.render.knife;
import java.util.*;import net.albiz.client.port.soberui.model.*;import net.minecraft.*;
/** Cached thumbnails generated from the very meshes used by the renderer. */
public final class KnifePreview{
 private static final Map<String,ModelMesh> meshes=new HashMap<>();
 public static class_2960 icon(String name){ModelMesh mesh=meshes.computeIfAbsent(name,KnifePreview::bake);return ModelIcon.get(mesh,()->{int[] colors=new int[mesh.triangles*3];for(int i=0;i<colors.length;i++)colors[i]=mesh.colors[i/3]|0xff000000;return colors;},false,name.hashCode());}
 private static ModelMesh bake(String name){var skin=KnifeSkin.of(name);var vertices=new ArrayList<Float>();var colors=new ArrayList<Integer>();var mat=KnifeMaterial.named("Diamond");skin.draw(new class_4587(),KnifeSkin.REST,1,1,new KnifeSkin.Sink(){public void part(class_4587 pose,KnifeMesh mesh,KnifeMesh.Shine shine){mesh.collect(pose,vertices,colors,mat.palette);}public void glow(class_4587 pose,KnifeMesh mesh,KnifeMesh.Shine shine){}},new float[12]);float[] raw=new float[vertices.size()];for(int i=0;i<raw.length;i++)raw[i]=vertices.get(i);int[] rgb=colors.stream().mapToInt(Integer::intValue).toArray();return ModelMesh.normalize(raw,rgb,rgb.length);}
}
