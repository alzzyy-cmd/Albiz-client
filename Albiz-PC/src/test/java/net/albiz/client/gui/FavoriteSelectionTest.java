package net.albiz.client.gui;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
class FavoriteSelectionTest {
 @Test void oldConfigStartsEmpty(){var s=new FavoriteSelection();s.load(null);assertTrue(s.snapshot().isEmpty());}
 @Test void roundTripPreservesMembershipAndOrder(){var s=new FavoriteSelection();s.set("item-esp",true);s.set("storage-esp",true);var other=new FavoriteSelection();other.load(s.json());assertEquals(s.snapshot(),other.snapshot());assertEquals("item-esp",other.snapshot().iterator().next());}
 @Test void deduplicatesAndValidates(){var s=new FavoriteSelection();s.load(JsonParser.parseString("[\"item-esp\",\"item-esp\",\"../bad\",17,null]"));assertEquals(java.util.Set.of("item-esp"),s.snapshot());}
 @Test void snapshotCannotMutateSelection(){var s=new FavoriteSelection();s.set("a",true);assertThrows(UnsupportedOperationException.class,()->s.snapshot().clear());assertTrue(s.contains("a"));}
 @Test void boundedAndCanRemove(){var s=new FavoriteSelection();for(int i=0;i<300;i++)s.set("module-"+i,true);assertEquals(256,s.snapshot().size());s.set("module-0",false);assertFalse(s.contains("module-0"));assertTrue(s.set("new-module",true));}
}
