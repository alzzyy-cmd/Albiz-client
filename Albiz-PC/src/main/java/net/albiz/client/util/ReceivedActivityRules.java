package net.albiz.client.util;
/** Classifies only server-delivered states. Never infers unseen blocks or a confirmed base. */
public final class ReceivedActivityRules {
 private ReceivedActivityRules(){}
 public static boolean spawner(String id){return "spawner".equals(id)||"trial_spawner".equals(id);}
 public static boolean redstone(String id){return switch(id){case "redstone_wire","repeater","comparator","observer","piston","sticky_piston","piston_head","redstone_torch","redstone_wall_torch","lever","dispenser","dropper","hopper","crafter","daylight_detector","target","tripwire_hook","trapped_chest","redstone_lamp" -> true;default -> id.endsWith("_button")||id.endsWith("_pressure_plate");};}
 public static boolean transition(String oldId,String oldState,String newId,String newState){return (redstone(oldId)||redstone(newId))&&!oldState.equals(newState);}
}
