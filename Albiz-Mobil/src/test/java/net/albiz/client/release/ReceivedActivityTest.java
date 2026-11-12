package net.albiz.client.release;
import net.albiz.client.util.ReceivedActivityRules;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ReceivedActivityTest {
 @Test void ordinarySpawner(){assertTrue(ReceivedActivityRules.spawner("spawner"));}
 @Test void trialSpawner(){assertTrue(ReceivedActivityRules.spawner("trial_spawner"));}
 @Test void naturalStoneIsNotSpawner(){assertFalse(ReceivedActivityRules.spawner("stone"));}
 @Test void wire(){assertTrue(ReceivedActivityRules.redstone("redstone_wire"));}
 @Test void mechanical(){for(String id:new String[]{"piston","observer","repeater","comparator","hopper","crafter"})assertTrue(ReceivedActivityRules.redstone(id));}
 @Test void inputs(){assertTrue(ReceivedActivityRules.redstone("oak_button"));assertTrue(ReceivedActivityRules.redstone("stone_pressure_plate"));}
 @Test void noStoneSignal(){assertFalse(ReceivedActivityRules.redstone("stone"));}
 @Test void unchangedStateNotActivity(){assertFalse(ReceivedActivityRules.transition("lever","powered=false","lever","powered=false"));}
 @Test void poweredTransition(){assertTrue(ReceivedActivityRules.transition("lever","powered=false","lever","powered=true"));}
 @Test void unrelatedChange(){assertFalse(ReceivedActivityRules.transition("stone","stone","dirt","dirt"));}
}
