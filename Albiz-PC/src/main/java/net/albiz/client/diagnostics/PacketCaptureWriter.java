package net.albiz.client.diagnostics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;
import java.util.zip.GZIPOutputStream;

/** Bounded asynchronous JSONL writer. No Minecraft, network, NBT or packet serialization API. */
public final class PacketCaptureWriter {
    public static final long DEFAULT_BYTES = 64L * 1024 * 1024;
    public static final int DEFAULT_ROWS = 200_000;
    private final ArrayBlockingQueue<JsonObject> queue;
    private final LongSupplier clock;
    private final long started, duration, maxBytes;
    private final int maxRows;
    private final Path path;
    private final AtomicReference<String> reason = new AtomicReference<>();
    private final AtomicLong dropped = new AtomicLong();
    private final ConcurrentHashMap<String, java.util.concurrent.atomic.LongAdder> packetCounts = new ConcurrentHashMap<>();
    private final Object gate = new Object();
    private final CompletableFuture<Result> completion = new CompletableFuture<>();
    private long accepted;
    public record Result(Path path, String reason, long written, long dropped, long bytes) {}

    public PacketCaptureWriter(Path path, long durationNanos) {
        this(path,durationNanos,DEFAULT_BYTES,DEFAULT_ROWS,512,System::nanoTime);
    }
    public PacketCaptureWriter(Path path,long durationNanos,long maxBytes,int maxRows,int capacity,LongSupplier clock) {
        if(durationNanos<=0 || maxBytes<256 || maxRows<1 || capacity<1) throw new IllegalArgumentException("Invalid capture limits");
        this.path=path;this.duration=durationNanos;this.maxBytes=maxBytes;this.maxRows=maxRows;
        this.queue=new ArrayBlockingQueue<>(capacity);this.clock=clock;this.started=clock.getAsLong();
        Thread worker=new Thread(this::write,"Albiz-packet-capture");worker.setDaemon(true);worker.start();
    }
    public CompletableFuture<Result> completion() { return completion; }
    public Path path() { return path; }
    public long elapsedMillis() { return Math.max(0,(clock.getAsLong()-started)/1_000_000L); }
    public long dropped() { return dropped.get(); }
    public boolean active() { expire(); return reason.get()==null; }
    public void countPacket(String type) {
        if(active() && packetCounts.size()<512) packetCounts.computeIfAbsent(type,k->new java.util.concurrent.atomic.LongAdder()).increment();
    }
    public void stop(String why) { synchronized(gate) { reason.compareAndSet(null,why); } }
    private void expire() { if(clock.getAsLong()-started>=duration) stop("duration_limit"); }
    /** Ownership transfers to the queue; callers must not mutate a row after offering it. Never waits for disk. */
    private static final java.util.Set<String> EVENTS=java.util.Set.of("block_update","section_update_summary","block_entity","chunk_data","chunk_entity_summary","light_update","block_event","world_event","block_sound","client_sample","decode_error","session_context","section_update_batch","section_update_applied");
    private static final java.util.Set<String> FIELDS=java.util.Set.of("event","phase","x","y","z","block_id","type_id","nbt_present","chunk_x","chunk_z","total_updates","total_entries","extraction_cap","sound_id","event_id","exception_class","radius","complete","blocks","modules_enabled","mobile_mode","section_x","section_y","section_z","outside_section","unique_cells","checksum_crc32","palette");
    public boolean offer(JsonObject input) {
        if(input==null || !input.has("event") || !input.get("event").isJsonPrimitive() || !EVENTS.contains(input.get("event").getAsString())) return false;
        JsonObject row=new JsonObject();input.entrySet().forEach(e->{ if(FIELDS.contains(e.getKey())) row.add(e.getKey(),e.getValue()); });
        synchronized(gate) {
            expire();if(reason.get()!=null)return false;
            row.addProperty("t_ms",elapsedMillis());row.addProperty("seq",++accepted);
            if(!queue.offer(row)) { dropped.incrementAndGet();return false; }
            return true;
        }
    }
    private void write() {
        Gson gson=new Gson();long written=0,bytes=0,discarded=0,oversized=0;
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            try(var file=Files.newOutputStream(path,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE);
                var gzip=new GZIPOutputStream(file,65536,true);
                var out=new BufferedWriter(new OutputStreamWriter(gzip,StandardCharsets.UTF_8),65536)) {
                JsonObject header=new JsonObject();header.addProperty("event","header");header.addProperty("schema",2);
                header.addProperty("client","Albiz 1.0 / MC 1.21.11");header.addProperty("started_utc",Instant.now().toString());
                header.addProperty("duration_ms",duration/1_000_000L);header.addProperty("max_event_bytes",maxBytes);
                header.addProperty("privacy","Coordinates included. No raw packets, NBT contents, chat, inventory, player identities, server address or credentials.");
                header.addProperty("scope","Decoded inbound event hooks and applied chunk/block/light events; not a complete wire capture. Client samples are not original packet payloads.");
                out.write(gson.toJson(header));out.newLine();out.flush();
                long lastFlush=System.nanoTime();
                while(reason.get()==null || !queue.isEmpty()) {
                    expire();JsonObject row=queue.poll(200,TimeUnit.MILLISECONDS);
                    if(row!=null) {
                        String line=gson.toJson(row);long size=line.getBytes(StandardCharsets.UTF_8).length+1L;
                        if(size>65536) {oversized++;}
                        else if(written>=maxRows || bytes+size>maxBytes) {
                            discarded++;stop(written>=maxRows?"row_limit":"byte_or_row_size_limit");
                        } else { out.write(line);out.newLine();written++;bytes+=size; }
                    }
                    if(System.nanoTime()-lastFlush>=1_000_000_000L) { out.flush();lastFlush=System.nanoTime(); }
                }
                JsonObject footer=new JsonObject();footer.addProperty("event","summary");footer.addProperty("reason",reason.get());
                footer.addProperty("elapsed_ms",elapsedMillis());footer.addProperty("written",written);footer.addProperty("event_bytes",bytes);
                footer.addProperty("dropped_queue",dropped.get());footer.addProperty("discarded_limit",discarded);footer.addProperty("discarded_oversize",oversized);
                JsonObject counts=new JsonObject();packetCounts.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(e->counts.addProperty(e.getKey(),e.getValue().sum()));
                footer.add("inbound_type_counts",counts);out.write(gson.toJson(footer));out.newLine();
            }
            completion.complete(new Result(path,reason.get(),written,dropped.get()+discarded+oversized,bytes));
        } catch(Exception ex) {
            stop("io_failure");completion.completeExceptionally(ex);
        }
    }
}
