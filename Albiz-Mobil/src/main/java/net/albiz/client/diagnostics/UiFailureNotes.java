package net.albiz.client.diagnostics;

import com.google.gson.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import net.albiz.client.config.ConfigMigration;
import net.fabricmc.loader.api.FabricLoader;

/** Local, rate-limited stack summaries: no exception message, user data or automatic upload. */
public final class UiFailureNotes {
    private static final AtomicLong LAST=new AtomicLong();
    private UiFailureNotes() {}
    public static void record(String area,Throwable failure) {
        long now=System.nanoTime(),before=LAST.get();
        if(before!=0 && now-before<5_000_000_000L || !LAST.compareAndSet(before,now))return;
        JsonObject report=new JsonObject();report.addProperty("client","Albiz 1.0");report.addProperty("area",area);
        report.addProperty("exception_class",failure.getClass().getName());report.addProperty("utc",Instant.now().toString());
        JsonArray frames=new JsonArray();var stack=failure.getStackTrace();
        for(int i=0;i<Math.min(32,stack.length);i++)frames.add(stack[i].getClassName()+"#"+stack[i].getMethodName()+":"+stack[i].getLineNumber());
        report.add("frames",frames);
        Thread writer=new Thread(()->{
            try {
                Path root=ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("diagnostics");Files.createDirectories(root);
                Files.writeString(root.resolve("ui-error-"+Long.toUnsignedString(now)+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(report),StandardOpenOption.CREATE_NEW);
            } catch(Exception | LinkageError ignored) {}
        },"Albiz-ui-failure-note");writer.setDaemon(true);writer.start();
    }
}
