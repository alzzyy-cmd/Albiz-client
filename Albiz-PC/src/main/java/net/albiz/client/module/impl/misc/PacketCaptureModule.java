package net.albiz.client.module.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.config.ConfigMigration;
import net.albiz.client.diagnostics.PacketCaptureWriter;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.EventPriority;
import net.albiz.client.event.impl.*;
import net.albiz.client.module.*;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.*;

/** Passive, opt-in diagnostic capture: never cancels, modifies or sends a packet. */
public final class PacketCaptureModule extends ClientModule {
    private final NumberSetting minutes=addSetting("capture-minutes",new NumberSetting("Capture Minutes",5,1,10).step(1));
    private final BooleanSetting samples=addSetting("nearby-samples",new BooleanSetting("Nearby Samples",true));
    private volatile PacketCaptureWriter capture;
    private volatile class_638 origin;
    private volatile PacketCaptureWriter closing;
    private long lastSample, lastNotice;
    private static final DateTimeFormatter STAMP=DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss").withZone(ZoneOffset.UTC);
    public PacketCaptureModule() {
        super("packet-capture","Packet Capture","Records selected incoming block, chunk, light and block-entity packet metadata for five minutes. Coordinates are included; chat, credentials, inventory and raw NBT are excluded. Stops automatically and saves a local diagnostic file.",ModuleCategory.MISC);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{
            PacketCaptureWriter session=capture!=null?capture:closing;
            if(session!=null) { session.stop("client_shutdown");try {session.completion().get(2,TimeUnit.SECONDS);}catch(Exception ignored){} }
        });
    }
    @Override public boolean persistEnabled() { return false; }
    @Override protected void onEnable() {
        class_310 client=class_310.method_1551();
        if(client.field_1687==null || client.field_1724==null) {
            ModuleChat.sendAlways(this,ModuleChat.body().warn("Join a world before starting packet capture."));setEnabled(false);return;
        }
        origin=client.field_1687;lastSample=lastNotice=0;
        Path file=ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("diagnostics")
            .resolve("packet-capture-"+STAMP.format(Instant.now())+"-"+UUID.randomUUID().toString().substring(0,8)+".jsonl.gz");
        PacketCaptureWriter session=new PacketCaptureWriter(file,TimeUnit.MINUTES.toNanos((long)minutes.get()));capture=session;
        session.completion().whenComplete((result,error)->client.execute(()-> {
            if(error!=null) ModuleChat.sendAlways(this,ModuleChat.body().warn("Capture write failed; check free disk space. A partial file may remain."));
            else ModuleChat.sendAlways(this,ModuleChat.body().good("Capture saved: ").value(file.getFileName()).text(" | ").text("Dropped events: ").value(result.dropped()));
        }));
        ModuleChat.sendAlways(this,ModuleChat.body().warn("Packet capture started. Coordinates are recorded; review before sharing. ").value((int)minutes.get()).text(" min"));
    }
    @Override protected void onDisable() {
        PacketCaptureWriter session=capture;capture=null;origin=null;
        if(session!=null) {closing=session;session.stop("user_disabled");}
    }
    private PacketCaptureWriter current() {
        PacketCaptureWriter session=capture;
        return session!=null && class_310.method_1551().field_1687==origin && session.active()?session:null;
    }
    private static JsonObject row(String event,String phase) {
        JsonObject row=new JsonObject();row.addProperty("event",event);row.addProperty("phase",phase);return row;
    }
    private static void position(JsonObject row,class_2338 pos) {
        row.addProperty("x",pos.method_10263());row.addProperty("y",pos.method_10264());row.addProperty("z",pos.method_10260());
    }
    private static String block(class_2680 state) { return String.valueOf(class_7923.field_41175.method_10221(state.method_26204())); }
    private static void blockRow(PacketCaptureWriter session,String phase,class_2338 pos,class_2680 state) {
        JsonObject row=row("block_update",phase);position(row,pos);row.addProperty("block_id",block(state));session.offer(row);
    }
    private static void entityRow(PacketCaptureWriter session,String phase,class_2338 pos,class_2591<?> type,boolean tag) {
        JsonObject row=row("block_entity",phase);position(row,pos);
        row.addProperty("type_id",String.valueOf(class_7923.field_41181.method_10221(type)));row.addProperty("nbt_present",tag);session.offer(row);
    }
    private static void chunk(JsonObject row,int x,int z) { row.addProperty("chunk_x",x);row.addProperty("chunk_z",z); }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onIncoming(PacketReceiveNettyEvent event) {
        PacketCaptureWriter session=current();if(session==null)return;
        session.countPacket(event.packet.getClass().getSimpleName());
        try { describe(session,event.packet,"received_hook"); }
        catch(RuntimeException | LinkageError ex) {
            JsonObject row=row("decode_error","received_hook");row.addProperty("exception_class",ex.getClass().getName());session.offer(row);
        }
    }
    @EventHandler public void onApplied(PacketAppliedEvent event) {
        PacketCaptureWriter session=current();if(session==null)return;
        try { describe(session,event.packet,"applied"); }
        catch(RuntimeException | LinkageError ex) {
            JsonObject row=row("decode_error","applied");row.addProperty("exception_class",ex.getClass().getName());session.offer(row);
        }
    }
    private void describe(PacketCaptureWriter session,class_2596<?> packet,String phase) {
        if(packet instanceof class_2626 p) blockRow(session,phase,p.method_11309(),p.method_11308());
        else if(packet instanceof class_2637 p) {
            var batch=new net.albiz.client.diagnostics.CompactBlockBatch();
            p.method_30621((pos,state)->batch.add(pos.method_10263(),pos.method_10264(),pos.method_10260(),block(state)));
            session.offer(batch.json(phase,"received_hook".equals(phase)));
        } else if(packet instanceof class_2672 p) {
            JsonObject row=row("chunk_data",phase);chunk(row,p.method_11523(),p.method_11524());session.offer(row);
            int[] total={0};
            p.method_38598().method_38587(p.method_11523(),p.method_11524()).accept((pos,type,tag)-> {
                if(total[0]++<256 && type!=null && session.active()) entityRow(session,phase,pos,type,tag!=null);
            });
            row=row("chunk_entity_summary",phase);chunk(row,p.method_11523(),p.method_11524());row.addProperty("total_entries",total[0]);row.addProperty("extraction_cap",256);session.offer(row);
        } else if(packet instanceof class_2622 p) entityRow(session,phase,p.method_11293(),p.method_11291(),p.method_11290()!=null);
        else if(packet instanceof class_2676 p) {
            JsonObject row=row("light_update",phase);chunk(row,p.method_11558(),p.method_11554());session.offer(row);
        } else if(packet instanceof class_2623 p) {
            JsonObject row=row("block_event",phase);position(row,p.method_11298());row.addProperty("block_id",String.valueOf(class_7923.field_41175.method_10221(p.method_11295())));session.offer(row);
        } else if(packet instanceof class_2673 p) {
            JsonObject row=row("world_event",phase);position(row,p.method_11531());row.addProperty("event_id",p.method_11532());session.offer(row);
        } else if(packet instanceof class_2767 p && "received_hook".equals(phase)) {
            String id=String.valueOf(p.method_11894().comp_349().comp_3319());
            if(id.startsWith("minecraft:block.")) {
                JsonObject row=row("block_sound",phase);row.addProperty("sound_id",id);row.addProperty("x",p.method_11890());row.addProperty("y",p.method_11889());row.addProperty("z",p.method_11893());session.offer(row);
            }
        }
    }
    @EventHandler public void onTick(TickEvent event) {
        PacketCaptureWriter session=capture;if(session==null)return;
        class_310 client=class_310.method_1551();
        if(client.field_1687!=origin || client.field_1724==null) {session.stop("world_changed_or_disconnected");setEnabled(false);return;}
        if(!session.active() || session.completion().isDone()) {setEnabled(false);return;}
        long now=System.nanoTime();
        if(lastNotice==0 || now-lastNotice>=5_000_000_000L) {
            lastNotice=now;
            context(session);
            ModuleChat.actionBarAlways(this,ModuleChat.body().text("Capture running: ").value(session.elapsedMillis()/1000).text(" s | ").text("Dropped events: ").value(session.dropped()));
        }
        if(samples.get() && (lastSample==0 || now-lastSample>=1_000_000_000L)) {
            lastSample=now;
            try {sample(session,client);} catch(RuntimeException | LinkageError ex) {
                JsonObject row=row("decode_error","client_sample");row.addProperty("exception_class",ex.getClass().getName());session.offer(row);
            }
        }
    }
    private void context(PacketCaptureWriter session) {
        JsonObject row=row("session_context","client_context");JsonObject flags=new JsonObject();
        for(String id:new String[]{"xray","storage-esp","block-entity-debug","amethyst-bypass","player-bypass"}) {
            ClientModule module=ModuleRegistry.resolve(id);flags.addProperty(id,module!=null && module.isEnabled());
        }
        row.add("modules_enabled",flags);row.addProperty("mobile_mode",net.albiz.client.platform.PlatformInfo.isMobile());session.offer(row);
    }
    private void sample(PacketCaptureWriter session,class_310 client) {
        class_2338 center=client.field_1724.method_24515();
        JsonObject row=row("client_sample","client_state_not_packet");position(row,center);row.addProperty("radius",3);
        JsonArray blocks=new JsonArray();long deadline=System.nanoTime()+1_000_000L;boolean complete=true;
        int[] offsets={0,-1,1,-2,2,-3,3};
        outer:for(int x:offsets)for(int z:offsets)for(int y:offsets) {
            if(System.nanoTime()>deadline) {complete=false;break outer;}
            class_2338 pos=new class_2338(center.method_10263()+x,center.method_10264()+y,center.method_10260()+z);
            if(!origin.method_2935().method_12123(pos.method_10263()>>4,pos.method_10260()>>4)) {complete=false;continue;}
            JsonArray value=new JsonArray();value.add(x);value.add(y);value.add(z);value.add(block(origin.method_8320(pos)));blocks.add(value);
        }
        row.addProperty("complete",complete);row.add("blocks",blocks);session.offer(row);
    }
}
