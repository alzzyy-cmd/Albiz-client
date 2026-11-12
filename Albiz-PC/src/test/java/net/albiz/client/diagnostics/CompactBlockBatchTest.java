package net.albiz.client.diagnostics;

import com.google.gson.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompactBlockBatchTest {
    @Test void fullSectionIsOneSmallRowInsteadOf4096Rows() {
        var batch=new CompactBlockBatch();for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++)batch.add(x,y,z,y<8?"minecraft:deepslate":"minecraft:cave_air");
        var row=batch.json("received_hook",true);assertEquals(4096,row.get("unique_cells").getAsInt());
        assertEquals(2,row.getAsJsonArray("palette").size());assertTrue(row.toString().length()<2500);
        for(var value:row.getAsJsonArray("palette")) {var group=value.getAsJsonObject();assertEquals("bitmap-le",group.get("encoding").getAsString());assertEquals(512,Base64.getDecoder().decode(group.get("cells_b64").getAsString()).length);}
    }
    @Test void sparseCoordinatesRoundTripIncludingNegativeSections() {
        var batch=new CompactBlockBatch();batch.add(-1,-17,31,"minecraft:chest");var row=batch.json("received_hook",true);
        assertEquals(-1,row.get("section_x").getAsInt());assertEquals(-2,row.get("section_y").getAsInt());assertEquals(1,row.get("section_z").getAsInt());
        var group=row.getAsJsonArray("palette").get(0).getAsJsonObject();assertEquals("u16le",group.get("encoding").getAsString());
        byte[] bytes=Base64.getDecoder().decode(group.get("cells_b64").getAsString());int index=Byte.toUnsignedInt(bytes[0])|(Byte.toUnsignedInt(bytes[1])<<8);
        assertEquals(15,index>>8);assertEquals(15,(index>>4)&15);assertEquals(15,index&15);
    }
    @Test void finalStateWinsAndAppliedDigestOmitsRepeatedCells() {
        var batch=new CompactBlockBatch();batch.add(0,0,0,"minecraft:stone");batch.add(0,0,0,"minecraft:chest");
        var received=batch.json("received_hook",true);var applied=batch.json("applied",false);
        assertEquals(received.get("checksum_crc32"),applied.get("checksum_crc32"));assertFalse(applied.has("palette"));
        assertEquals(2,received.get("total_updates").getAsInt());assertEquals(1,received.get("unique_cells").getAsInt());
        assertEquals("minecraft:chest",received.getAsJsonArray("palette").get(0).getAsJsonObject().get("block_id").getAsString());
    }
    @Test void outsideSectionIsCountedRatherThanWrappedToAnotherPosition() {
        var batch=new CompactBlockBatch();batch.add(0,0,0,"minecraft:stone");batch.add(16,0,0,"minecraft:chest");
        var row=batch.json("received_hook",true);assertEquals(1,row.get("outside_section").getAsInt());assertEquals(1,row.get("unique_cells").getAsInt());
    }
}
