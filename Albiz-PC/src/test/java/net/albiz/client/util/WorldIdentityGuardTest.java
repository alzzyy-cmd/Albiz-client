package net.albiz.client.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WorldIdentityGuardTest {
 @Test void sameDimensionDifferentWorldStillInvalidates(){var g=new WorldIdentityGuard<String>();String a=new String("overworld"),b=new String("overworld");assertTrue(g.update(a));assertFalse(g.update(a));assertTrue(g.update(b));assertFalse(g.matches(a));assertTrue(g.matches(b));}
 @Test void disconnectAndReconnectInvalidate(){var g=new WorldIdentityGuard<Object>();var world=new Object();assertFalse(g.update(null));assertTrue(g.update(world));assertTrue(g.update(null));assertTrue(g.update(world));}
}
