package net.albiz.client.diagnostics;

import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PacketCaptureWriterTest {
    @TempDir Path root;
    JsonObject row(int x) { JsonObject row=new JsonObject();row.addProperty("event","block_update");row.addProperty("x",x);row.addProperty("block_id","minecraft:deepslate");return row; }
    List<JsonObject> read(Path path) throws Exception {
        try(var in=new GZIPInputStream(Files.newInputStream(path))) {
            return new String(in.readAllBytes(),StandardCharsets.UTF_8).lines().map(s->JsonParser.parseString(s).getAsJsonObject()).toList();
        }
    }
    @Test void stoppingDrainsQueueAndProducesReadableGzipFooter() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("capture.jsonl.gz"),TimeUnit.MINUTES.toNanos(5));
        for(int i=0;i<100;i++)assertTrue(writer.offer(row(i)));
        writer.countPacket("class_2626");writer.stop("user_disabled");
        var result=writer.completion().get(5,TimeUnit.SECONDS);assertEquals(100,result.written());
        var lines=read(result.path());assertEquals(102,lines.size());assertEquals("header",lines.getFirst().get("event").getAsString());
        var footer=lines.getLast();assertEquals("summary",footer.get("event").getAsString());
        assertEquals("user_disabled",footer.get("reason").getAsString());assertEquals(1,footer.getAsJsonObject("inbound_type_counts").get("class_2626").getAsInt());
        assertFalse(writer.offer(row(101)));
    }
    @Test void timerExpiresWithoutAnyGameTick() throws Exception {
        AtomicLong clock=new AtomicLong();var writer=new PacketCaptureWriter(root.resolve("timed.gz"),300_000_000_000L,65536,100,16,clock::get);
        assertTrue(writer.offer(row(1)));clock.set(300_000_000_001L);
        var result=writer.completion().get(5,TimeUnit.SECONDS);assertEquals("duration_limit",result.reason());assertFalse(writer.active());
        assertEquals("duration_limit",read(result.path()).getLast().get("reason").getAsString());
    }
    @Test void rowLimitStopsInsteadOfGrowingForever() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("rows.gz"),TimeUnit.MINUTES.toNanos(5),65536,1,16,System::nanoTime);
        writer.offer(row(1));writer.offer(row(2));
        var result=writer.completion().get(5,TimeUnit.SECONDS);assertEquals(1,result.written());assertEquals("row_limit",result.reason());assertTrue(result.dropped()>=1);
    }
    @Test void byteLimitStopsAndFinishesValidFile() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("bytes.gz"),TimeUnit.MINUTES.toNanos(5),256,10000,16,System::nanoTime);
        for(int i=0;i<10000 && writer.active();i++)writer.offer(row(i));
        var result=writer.completion().get(5,TimeUnit.SECONDS);assertTrue(result.bytes()<=256);assertEquals("byte_or_row_size_limit",result.reason());assertEquals("summary",read(result.path()).getLast().get("event").getAsString());
    }
    @Test void schemaDropsUnknownFieldsAndRejectsRawCustomPayload() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("privacy.gz"),TimeUnit.MINUTES.toNanos(5));
        var input=row(42);input.addProperty("raw_nbt","SECRET_NBT");input.addProperty("chat","SECRET_CHAT");input.addProperty("server_address","SECRET_ADDRESS");input.addProperty("token","SECRET_TOKEN");
        assertTrue(writer.offer(input));var raw=new JsonObject();raw.addProperty("event","custom_payload");raw.addProperty("payload","SECRET_PAYLOAD");assertFalse(writer.offer(raw));
        writer.stop("user_disabled");var result=writer.completion().get(5,TimeUnit.SECONDS);
        String content=read(result.path()).toString();assertFalse(content.contains("SECRET_"));assertTrue(content.contains("minecraft:deepslate"));assertTrue(content.contains("42"));
    }
    @Test void existingFileIsNotOverwritten() throws Exception {
        Path file=root.resolve("existing.gz");Files.writeString(file,"keep-original");
        var writer=new PacketCaptureWriter(file,TimeUnit.MINUTES.toNanos(5));
        assertThrows(ExecutionException.class,()->writer.completion().get(5,TimeUnit.SECONDS));assertEquals("keep-original",Files.readString(file));assertFalse(writer.active());
    }
    @Test void concurrentProducersHaveBoundedQueueAndAccountForDroppedRows() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("concurrent.gz"),TimeUnit.MINUTES.toNanos(5),1_000_000,10000,8,System::nanoTime);
        var pool=Executors.newFixedThreadPool(4);List<Future<?>> futures=new ArrayList<>();
        for(int t=0;t<4;t++) futures.add(pool.submit(()->{for(int i=0;i<500;i++)writer.offer(row(i));}));
        for(var future:futures)future.get(5,TimeUnit.SECONDS);pool.shutdown();
        writer.stop("user_disabled");var result=writer.completion().get(5,TimeUnit.SECONDS);assertEquals(2000,result.written()+result.dropped());
        var lines=read(result.path());long previous=0;
        for(var line:lines)if(line.has("seq")){long seq=line.get("seq").getAsLong();assertTrue(seq>previous);previous=seq;}
    }
    @Test void invalidLimitsAreRejectedBeforeOpeningFile() {
        assertThrows(IllegalArgumentException.class,()->new PacketCaptureWriter(root.resolve("bad.gz"),0));
    }
    @Test void oversizedSingleRowDoesNotStopTheEntireCapture() throws Exception {
        var writer=new PacketCaptureWriter(root.resolve("oversize.gz"),TimeUnit.MINUTES.toNanos(5));
        var huge=row(0);huge.addProperty("block_id","x".repeat(70000));writer.offer(huge);writer.offer(row(1));writer.stop("user_disabled");
        var result=writer.completion().get(5,TimeUnit.SECONDS);assertEquals("user_disabled",result.reason());assertEquals(1,result.written());
        assertEquals(1,read(result.path()).getLast().get("discarded_oversize").getAsInt());
    }
}
