package net.albiz.client.esp;
import java.util.*;
import java.util.function.*;

/** Keeps unchanged hint geometry alive instead of restarting reveal animation every scan. */
public final class StableHintProjection<T> {
 private final EvidenceHistory<T> evidence;
 private Map<Long,T> shown=new LinkedHashMap<>();
 public StableHintProjection(int capacity,long lifetime){evidence=new EvidenceHistory<>(capacity,lifetime);}
 public void remember(long key,T value,long now){evidence.remember(key,value,now);}
 public void forget(long key){evidence.forget(key);}
 public void clear(){evidence.clear();shown.clear();}
 public void apply(long now,LongPredicate visible,BiConsumer<Long,T> put,LongConsumer remove){
  Map<Long,T> next=new LinkedHashMap<>();evidence.forEachAlive(now,(key,value)->{if(visible.test(key))next.put(key,value);});
  for(long key:shown.keySet())if(!next.containsKey(key))remove.accept(key);
  next.forEach((key,value)->{if(!Objects.equals(shown.get(key),value))put.accept(key,value);});
  shown=next;
 }
}
