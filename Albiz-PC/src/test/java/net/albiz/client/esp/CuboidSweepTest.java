package net.albiz.client.esp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CuboidSweepTest {
 @Test void allCellsExactlyOnceAndWrap(){var s=new CuboidSweep(2);var seen=new java.util.HashSet<String>();for(int i=0;i<s.total();i++){int n=s.next();assertTrue(seen.add(s.x(n)+","+s.y(n)+","+s.z(n)));}assertEquals(125,seen.size());assertEquals(0,s.next());}
 @Test void rejectsUnboundedRadii(){assertThrows(IllegalArgumentException.class,()->new CuboidSweep(0));assertThrows(IllegalArgumentException.class,()->new CuboidSweep(65));}
 @Test void pauseResumesWithoutStarvation(){var s=new CuboidSweep(3);for(int i=0;i<10;i++)assertEquals(i,s.next());assertEquals(10,s.next());}
}
