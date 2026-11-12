package net.albiz.client.util;
/** CPU-testable detection rules; positions at Y=0 are deliberately excluded. */
public final class UndergroundRules {
 private UndergroundRules(){}
 public static boolean underground(double y){return Double.isFinite(y)&&y<0;}
 /** Same strict rule with a configurable ceiling, for dimensions whose build range does not go below Y=0. */
 public static boolean underground(double y,int ceiling){return Double.isFinite(y)&&y<ceiling;}
 public static boolean kelp(String id){return id.equals("kelp")||id.equals("kelp_plant");}
 public static boolean workingCrafter(String id,String state){return id.equals("crafter")&&(state.contains("crafting=true")||state.contains("triggered=true"));}
 public static boolean crafterSound(String id){return id.startsWith("minecraft:block.crafter.");}
}
