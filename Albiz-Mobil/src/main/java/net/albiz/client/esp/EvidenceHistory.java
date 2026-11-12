package net.albiz.client.esp;

import java.util.*;
import java.util.function.BiConsumer;

/** Bounded, expiring history of previously received evidence; not a present-tense verification. */
public final class EvidenceHistory<T> {
    private record Mark<T>(T value,long deadline) {}
    private final LinkedHashMap<Long,Mark<T>> entries=new LinkedHashMap<>();
    private final int capacity;private final long lifetime;
    public EvidenceHistory(int capacity,long lifetimeNanos) {
        if(capacity<1 || lifetimeNanos<=0)throw new IllegalArgumentException();this.capacity=capacity;this.lifetime=lifetimeNanos;
    }
    public void remember(long pos,T value,long now) {
        entries.remove(pos);entries.put(pos,new Mark<>(value,now+lifetime));
        while(entries.size()>capacity)entries.remove(entries.keySet().iterator().next());
    }
    public void forget(long pos){entries.remove(pos);}
    public void clear(){entries.clear();}
    public int size(){return entries.size();}
    public void forEachAlive(long now,BiConsumer<Long,T> action) {
        var iterator=entries.entrySet().iterator();
        while(iterator.hasNext()) {
            var entry=iterator.next();if(now-entry.getValue().deadline()>=0)iterator.remove();else action.accept(entry.getKey(),entry.getValue().value());
        }
    }
}
