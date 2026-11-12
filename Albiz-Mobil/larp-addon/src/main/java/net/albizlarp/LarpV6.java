package net.albizlarp;
import java.util.*;import net.albiz.client.module.setting.*;import net.albiz.client.port.sober.render.*;import net.minecraft.class_1923;
/** Corrected reference interpretation: 8x8 CHUNKS (128x128 blocks), not an 8x8-block plate. */
public final class LarpV6 extends PassiveSensor {
 private final NumberSetting minimum=addSetting("minimum-radius",new NumberSetting("Minimum Chunk Radius",0,0,63).step(1));
 private final BooleanSetting plates=addSetting("plates",new BooleanSetting("Player Render Distance Plates",false));
 private final NumberSetting plateY=addSetting("plate-y",new NumberSetting("Plate Y",64,-64,320).step(1));
 private final Set<Long> drawn=new HashSet<>();
 public LarpV6(){super("larp-debug-v6","Larp Debug V6","Bee nests and beehives in a chunk ring; full-world-height pillars and optional 8x8 CHUNK plates at Y64, matching the reference geometry.");}
 @Override public int settingsVersion(){return 1;}
 @Override public void migrateSettings(int old){if(old<1){plates.set(false);mode.set("Pillar");}}
 protected int minRadius(){return Math.min(minimum.getInt(),range.getInt());}
 protected boolean accepts(String id){return EvidenceRules.hive(id);}
 protected boolean qualifies(Summary s,long now){return s.hives()>0;}
 protected void beginRender(){drawn.clear();}
 protected void draw(Render3D r,Summary s,Color c){super.draw(r,s,c);if(plates.get())for(int dx=-4;dx<4;dx++)for(int dz=-4;dz<4;dz++){long k=class_1923.method_8331(s.x()+dx,s.z()+dz);if(!drawn.add(k))continue;double x=(s.x()+dx)*16,z=(s.z()+dz)*16;r.box(x,plateY.get(),z,x+16,plateY.get()+.125,z+16,c.withAlpha(25),c.withAlpha(100),ShapeMode.Both);}}
}
