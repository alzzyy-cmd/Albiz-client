package net.albiz.client.port.sober.util;
import java.util.*;import java.util.function.Consumer;import net.minecraft.*;
/** Bounded client-thread work. A same-coordinate replacement replaces pending work. */
public final class ChunkScanner {
 private final Consumer<class_2818> task;private final LinkedHashMap<Long,class_2818> queue=new LinkedHashMap<>();private boolean active;private int ticks;
 private static final Set<ChunkScanner> LIVE=Collections.newSetFromMap(new WeakHashMap<>());
 public ChunkScanner(String name,Consumer<class_2818> task){this.task=task;}
 public void start(){stop();active=true;ticks=0;LIVE.add(this);}
 public void stop(){active=false;queue.clear();LIVE.remove(this);}
 public void submit(class_2818 chunk){if(!active||chunk==null)return;long k=chunk.method_12004().method_8324();if(queue.containsKey(k)||queue.size()<(net.albiz.client.platform.PlatformInfo.isMobile()?256:1024))queue.put(k,chunk);}
 public void submitLoaded(){for(var c:loadedChunks())submit(c);}
 public static void drain(){for(var s:new ArrayList<>(LIVE)){
  if(net.albiz.client.platform.PlatformInfo.isMobile()&&s.ticks++%3!=0||s.queue.isEmpty())continue;
  var it=s.queue.entrySet().iterator();var c=it.next().getValue();it.remove();var mc=class_310.method_1551();
  if(mc!=null&&mc.field_1687!=null&&mc.field_1687.method_2935().method_2857(c.method_12004().field_9181,c.method_12004().field_9180,class_2806.field_12803,false)==c)s.task.accept(c);
 }}
 public static List<class_2818> loadedChunks(){var mc=class_310.method_1551();List<class_2818> out=new ArrayList<>();if(mc==null||mc.field_1687==null||mc.field_1724==null)return out;var at=mc.field_1724.method_31476();int r=Math.min(net.albiz.client.platform.PlatformInfo.isMobile()?8:24,mc.field_1690.method_38521()+1);for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){var c=mc.field_1687.method_2935().method_2857(at.field_9181+x,at.field_9180+z,class_2806.field_12803,false);if(c!=null)out.add(c);}return out;}
}
