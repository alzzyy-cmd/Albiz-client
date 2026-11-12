import net.albizlarp.EvidenceRules;
public class RulesCpuTest {
 private static int n;
 private static void check(boolean pass){n++;if(!pass)throw new AssertionError("Rule "+n);}
 public static void main(String[] args){
  check(EvidenceRules.hive("bee_nest"));check(EvidenceRules.hive("beehive"));check(!EvidenceRules.hive("honey_block"));
  check(EvidenceRules.built("chest"));check(EvidenceRules.built("red_shulker_box"));check(EvidenceRules.built("white_bed"));check(!EvidenceRules.built("stone"));
  check(EvidenceRules.score(3,1,3,5,70,true)==85);check(EvidenceRules.score(0,1,3,5,70,true)==0);check(EvidenceRules.score(0,1,3,5,70,false)==70);
  check(EvidenceRules.decay(110,30000,20)==100);check(EvidenceRules.decay(10,60000,20)==0);check(EvidenceRules.decay(110,-500,20)==110);
  check(EvidenceRules.inRing(-12,12,0,12));check(!EvidenceRules.inRing(0,0,1,12));check(!EvidenceRules.inRing(13,0,0,12));check(EvidenceRules.inRing(-3,2,3,12));
  check(EvidenceRules.synergized(100,1,true)==100);check(EvidenceRules.synergized(100,2,true)==150);check(EvidenceRules.synergized(100,3,true)==175);check(EvidenceRules.synergized(100,3,false)==100);
  check(EvidenceRules.synergized(EvidenceRules.score(3,1,3,5,70,true),2,true)==127.5);check(EvidenceRules.score(1,1,3,5,70,true)==70);check(EvidenceRules.score(2,0,3,5,70,true)==0);
  int plates=0;for(int x=EvidenceRules.plateOffsetMin();x<EvidenceRules.plateOffsetMaxExclusive();x++)for(int z=EvidenceRules.plateOffsetMin();z<EvidenceRules.plateOffsetMaxExclusive();z++)plates++;
  check(plates==64);check((EvidenceRules.plateOffsetMaxExclusive()-EvidenceRules.plateOffsetMin())*16==128);
  System.out.println("PASS "+n+" independent addon CPU rule assertions; no Minecraft runtime invoked.");
 }
}
