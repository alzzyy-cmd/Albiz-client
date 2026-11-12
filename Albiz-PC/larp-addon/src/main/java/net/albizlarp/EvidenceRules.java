package net.albizlarp;
import java.util.Set;
/** Pure rules shared with CPU tests. Heuristics, not anti-ESP bypass or proof of a base. */
public final class EvidenceRules {
 private static final Set<String> BUILT=Set.of("chest","trapped_chest","barrel","hopper","furnace","blast_furnace","smoker","crafting_table","enchanting_table","brewing_stand","ender_chest","anvil","beacon","dispenser","dropper","piston","sticky_piston","observer","redstone_wire","repeater","comparator");
 public static boolean hive(String id){return id.equals("bee_nest")||id.equals("beehive");}
 public static boolean built(String id){return BUILT.contains(id)||id.endsWith("_shulker_box")||id.endsWith("_bed");}
 public static double score(int blocks,int spawners,int minimum,double blockWeight,double spawnerWeight,boolean naturalFilter){return (blocks>=minimum?blocks*blockWeight:0)+(naturalFilter&&blocks==0?0:spawners*spawnerWeight);}
 public static double decay(double score,long elapsedMillis,double perMinute){return Math.max(0,score-Math.max(0,elapsedMillis)/60000.0*perMinute);}
 public static boolean inRing(int dx,int dz,int minimum,int maximum){int d=Math.max(Math.abs(dx),Math.abs(dz));return d>=minimum&&d<=maximum;}
 public static double synergized(double score,int types,boolean enabled){return enabled&&types>=2?score*(1+types*.25):score;}
 public static int plateOffsetMin(){return -4;}
 public static int plateOffsetMaxExclusive(){return 4;}
 private EvidenceRules(){}
}
