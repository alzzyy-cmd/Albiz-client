package net.albiz.client.diagnostics;

import com.google.gson.*;
import java.util.*;
import java.util.zip.CRC32;

/** Derived registry IDs and section-local cells, not a raw network payload. */
public final class CompactBlockBatch {
    private final String[] states=new String[4096];
    private int x,y,z,total,outside;private boolean initialized;
    public void add(int px,int py,int pz,String id) {
        if(!initialized){x=px&~15;y=py&~15;z=pz&~15;initialized=true;}
        total++;
        if((px&~15)!=x || (py&~15)!=y || (pz&~15)!=z){outside++;return;}
        states[((px&15)<<8)|((pz&15)<<4)|(py&15)]=Objects.requireNonNull(id);
    }
    public JsonObject json(String phase,boolean includeCells) {
        JsonObject row=new JsonObject();row.addProperty("event",includeCells?"section_update_batch":"section_update_applied");row.addProperty("phase",phase);
        row.addProperty("section_x",x>>4);row.addProperty("section_y",y>>4);row.addProperty("section_z",z>>4);row.addProperty("total_updates",total);row.addProperty("outside_section",outside);
        Map<String,BitSet> groups=new TreeMap<>();CRC32 crc=new CRC32();int cells=0;
        for(int i=0;i<states.length;i++)if(states[i]!=null){
            cells++;groups.computeIfAbsent(states[i],k->new BitSet(4096)).set(i);
            int hash=states[i].hashCode();crc.update(i&255);crc.update(i>>8);
            for(int j=0;j<4;j++)crc.update(hash>>>(j*8)&255);
        }
        row.addProperty("unique_cells",cells);row.addProperty("checksum_crc32",Long.toHexString(crc.getValue()));
        if(includeCells){
            JsonArray palette=new JsonArray();
            groups.forEach((id,bits)->{
                JsonObject group=new JsonObject();group.addProperty("block_id",id);group.addProperty("count",bits.cardinality());
                byte[] bytes;
                if(bits.cardinality()<256){
                    bytes=new byte[bits.cardinality()*2];int j=0;
                    for(int i=bits.nextSetBit(0);i>=0;i=bits.nextSetBit(i+1)){bytes[j++]=(byte)i;bytes[j++]=(byte)(i>>8);}
                    group.addProperty("encoding","u16le");
                }else{bytes=Arrays.copyOf(bits.toByteArray(),512);group.addProperty("encoding","bitmap-le");}
                group.addProperty("cells_b64",Base64.getEncoder().encodeToString(bytes));palette.add(group);
            });row.add("palette",palette);
        }
        return row;
    }
}
