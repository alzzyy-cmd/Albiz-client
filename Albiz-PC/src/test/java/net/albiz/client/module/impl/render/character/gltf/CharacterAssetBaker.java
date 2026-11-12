package net.albiz.client.module.impl.render.character.gltf;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import com.google.gson.Gson;
import java.util.*;
import net.albiz.client.module.impl.render.character.kit.*;

/** Offline authoring utility; never loaded by the client and requires no native image codec. */
public final class CharacterAssetBaker {
 public static void main(String[] args) throws Exception {
  Path root=Path.of(System.getProperty("user.dir"));
  for(String id:args){
   var options=new CharacterBake.Options(); options.flat=.25f;
   if(id.equals("guts")){
    options.bones.put("head","spine.006_011");
    for(String side:new String[]{"left","right"}){
     String s=side.equals("left")?"L":"R";
     options.bones.put(side+" arm","upper_arm."+s);
     options.bones.put(side+" elbow","forearm."+s);
     options.bones.put(side+" hand","hand."+s);
     options.bones.put(side+" leg","thighA."+s);
     options.bones.put(side+" knee","shinA."+s);
    }
   }
   byte[] glb=Files.readAllBytes(root.resolve("artwork/characters/"+id+".glb"));
   System.out.println(id+": "+CharacterBake.describe(glb,options));
   var baked=CharacterBake.bake(glb,options,bytes->{
    BufferedImage im=ImageIO.read(new ByteArrayInputStream(bytes));
    if(im==null)throw new IOException("Unsupported image");
    return new ImageDecoder.Picture(im.getWidth(),im.getHeight(),im.getRGB(0,0,im.getWidth(),im.getHeight(),null,0,im.getWidth()));
   });
   Path target=root.resolve("src/main/resources/assets/albiz/characters/"+id+".anc");
   Files.write(target,baked.data());
   System.out.println("wrote "+target+" "+baked.data().length+" bytes, limbs="+baked.limbs());
   var design=new BakedDesign(id,id,()->Files.newInputStream(target));
   var model=CharacterBuilder.build(design,Detail.HIGH);
   Path preview=root.resolve("artwork/characters/"+id+"-preview.json");
   List<Object> surfaces=new ArrayList<>();
   for(var p:model.parts)for(var s:new Surface[]{p.base,p.decals})if(s!=null){
    var row=new LinkedHashMap<String,Object>();row.put("bone",p.bone.name());row.put("pivot",new float[]{p.pivotX,p.pivotY,p.pivotZ});row.put("positions",s.pos);row.put("normals",s.nrm);row.put("uv",s.uv);row.put("quads",s.quads);surfaces.add(row);
   }
   Files.writeString(preview,new Gson().toJson(Map.of("surfaces",surfaces,"triangles",model.triangles())));
   BufferedImage atlas=new BufferedImage(model.atlasWidth,model.atlasHeight,BufferedImage.TYPE_INT_ARGB);atlas.setRGB(0,0,model.atlasWidth,model.atlasHeight,model.atlasMips[0],0,model.atlasWidth);ImageIO.write(atlas,"png",root.resolve("artwork/characters/"+id+"-atlas.png").toFile());
   System.out.println("HIGH="+model.triangles()+" triangles, atlas="+model.atlasWidth+"x"+model.atlasHeight);
  }
 }
}
