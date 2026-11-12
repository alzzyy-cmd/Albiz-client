// Shield display subset adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.fx;
import it.unimi.dsi.fastutil.ints.*;import net.minecraft.*;
/** Observational UI timer only. No attack, movement or packet modification. */
public final class CombatFx{
 private static final Int2LongOpenHashMap shields=new Int2LongOpenHashMap();
 public static void reset(){shields.clear();}
 public static void tick(){long now=System.nanoTime();var it=shields.int2LongEntrySet().fastIterator();while(it.hasNext())if(now-it.next().getLongValue()>6_000_000_000L)it.remove();}
 public static float shieldRemaining(int id){return shields.containsKey(id)?5-(System.nanoTime()-shields.get(id))/1_000_000_000f:-1;}
 public static IntOpenHashSet shieldEntities(){return new IntOpenHashSet(shields.keySet());}
 public static void onSound(class_2767 packet){var mc=class_310.method_1551();if(mc.field_1687==null||mc.field_1724==null||!packet.method_11894().method_40225(class_3417.field_15239.method_40237()))return;class_742 closest=null;double best=6.25;for(var p:mc.field_1687.method_18456()){double d=p.method_5649(packet.method_11890(),packet.method_11889(),packet.method_11893());if(p!=mc.field_1724&&d<best){closest=p;best=d;}}if(closest!=null)shields.put(closest.method_5628(),System.nanoTime());}
}
