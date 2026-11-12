package net.albizlarp;
import java.util.*;import net.albiz.client.module.setting.*;import net.albiz.client.port.sober.render.*;import net.minecraft.*;
/** Finder is not a storage detector: confirmed received spawners and separate player/entity evidence. */
public final class LarpV7 extends PassiveSensor {
 private final BooleanSetting onlyBedrock=addSetting("only-bedrock",new BooleanSetting("Only Y0 Spawners",true));
 private final BooleanSetting underground=addSetting("underground-spawners",new BooleanSetting("Include Underground Spawners",true));
 private final BooleanSetting players=addSetting("players",new BooleanSetting("Player Evidence",true));
 private final BooleanSetting bedrockPlayers=addSetting("bedrock-players",new BooleanSetting("Players At Bedrock",true));
 private final BooleanSetting deepslate=addSetting("deepslate-players",new BooleanSetting("Players Below Zero",true));
 private final BooleanSetting requirePlayer=addSetting("require-player",new BooleanSetting("Require Nearby Player",false));
 private final BooleanSetting strict=addSetting("strict",new BooleanSetting("Validate Received Spawner Block",false));
 private final BooleanSetting entityEvidence=addSetting("entity-evidence",new BooleanSetting("Entity On Bedrock Or Deepslate",true));
 private final NumberSetting flatY=addSetting("flat-y",new NumberSetting("Flat Render Y",64,-64,320).step(1));
 private final ColorSetting playerColor=addSetting("player-color",new ColorSetting("Player Evidence Color",0xFFFFD751));
 private final Set<Long> rendered=new HashSet<>();
 public LarpV7(){super("larp-debug-v7","Larp Debug V7","Finder: received spawners plus separately colored visible player/entity evidence on bedrock/deepslate. Never claims anti-ESP bypass or a spawner from a player alone.");range.set(5);}
 @Override public int settingsVersion(){return 1;}
 @Override public void migrateSettings(int old){if(old<1){range.set(5);mode.set("Pillar");}}
 protected boolean accepts(String id){return id.equals("spawner");}
 protected boolean qualifies(Summary s,long now){if(s.spawners()==0)return false;boolean valid=false;var mc=class_310.method_1551();for(var en:samples.entrySet()){var p=class_2338.method_10092(en.getKey());if((p.method_10263()>>4)!=s.x()||(p.method_10260()>>4)!=s.z())continue;int y=p.method_10264();if(onlyBedrock.get()&&y!=0&&!(underground.get()&&y<0))continue;if(strict.get()&&!id(mc.field_1687.method_8320(p)).equals("spawner"))continue;valid=true;break;}if(!valid)return false;if(requirePlayer.get()){for(var p:mc.field_1687.method_18456())if(p!=mc.field_1724&&p.method_31476().field_9181==s.x()&&p.method_31476().field_9180==s.z())return true;return false;}return true;}
 protected void beginRender(){rendered.clear();}
 private void mark(Render3D r,int x,int z,Color c){long k=class_1923.method_8331(x,z);if(!rendered.add(k)||rendered.size()>(net.albiz.client.platform.PlatformInfo.isMobile()?24:96))return;var mc=class_310.method_1551();double y=mode.is("Pillar")?mc.field_1687.method_31607():flatY.get(),top=mode.is("Pillar")?mc.field_1687.method_31607()+mc.field_1687.method_31605():y+.125;r.box(x*16,y,z*16,x*16+16,top,z*16+16,c.withAlpha(20),c,ShapeMode.Both);if(tracers.get())r.tracer(x*16+8,flatY.get(),z*16+8,c);}
 protected void draw(Render3D r,Summary s,Color c){mark(r,s.x(),s.z(),c);}
 protected void extra(Render3D r,Color color){var mc=class_310.method_1551();var c=Color.fromPacked(playerColor.argb());if(players.get())for(var p:mc.field_1687.method_18456()){if(p==mc.field_1724||!inRange(p.method_24515()))continue;double y=p.method_23318();if(bedrockPlayers.get()&&y<=-64||deepslate.get()&&y<0)mark(r,p.method_31477()>>4,p.method_31479()>>4,c);}if(entityEvidence.get()){int tested=0;for(var entity:mc.field_1687.method_18112()){if(entity==mc.field_1724||!inRange(entity.method_24515()))continue;var state=mc.field_1687.method_8320(entity.method_24515().method_10074());String id=id(state);if(id.equals("bedrock")||id.equals("deepslate"))mark(r,entity.method_31477()>>4,entity.method_31479()>>4,c);if(++tested>=(net.albiz.client.platform.PlatformInfo.isMobile()?64:256))break;}}}
}
