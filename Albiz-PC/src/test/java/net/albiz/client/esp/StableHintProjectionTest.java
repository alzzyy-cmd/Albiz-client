package net.albiz.client.esp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class StableHintProjectionTest {
 @Test void unchangedHintDoesNotRestartGeometry(){var p=new StableHintProjection<String>(2,100);var added=new ArrayList<Long>();var removed=new ArrayList<Long>();p.remember(1,"a",0);p.apply(0,k->true,(k,v)->added.add(k),removed::add);p.remember(1,"a",10);p.apply(10,k->true,(k,v)->added.add(k),removed::add);assertEquals(List.of(1L),added);assertTrue(removed.isEmpty());}
 @Test void expiresAndRemoves(){var p=new StableHintProjection<String>(2,100);var removed=new ArrayList<Long>();p.remember(1,"a",0);p.apply(0,k->true,(k,v)->{},removed::add);p.apply(100,k->true,(k,v)->{},removed::add);assertEquals(List.of(1L),removed);}
 @Test void replacementForgettingAndVisibility(){var p=new StableHintProjection<String>(2,100);var values=new ArrayList<String>();p.remember(1,"a",0);p.apply(0,k->true,(k,v)->values.add(v),k->{});p.remember(1,"b",1);p.apply(1,k->true,(k,v)->values.add(v),k->{});assertEquals(List.of("a","b"),values);p.forget(1);var removed=new ArrayList<Long>();p.apply(2,k->true,(k,v)->{},removed::add);assertEquals(List.of(1L),removed);}
 @Test void capacityAndWorldReset(){var p=new StableHintProjection<String>(2,100);p.remember(1,"a",0);p.remember(2,"b",0);p.remember(3,"c",0);var added=new ArrayList<Long>();p.apply(0,k->true,(k,v)->added.add(k),k->{});assertEquals(List.of(2L,3L),added);p.clear();p.apply(1,k->true,(k,v)->fail(),k->fail());}
}
