package net.albiz.client.event;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class ListenerErrorReporter {
   private static final int FULL_TRACE_LIMIT = 4;
   private static final int SUMMARY_INTERVAL = 256;
   private static final Map<ListenerHandle<?>, Integer> failureCounts = new IdentityHashMap<>();

   private ListenerErrorReporter() {
   }

   static synchronized void report(ListenerHandle<?> handle, Event event, Exception ex) {
      int count = failureCounts.merge(handle, 1, Integer::sum);
      if (handle.owner() instanceof net.albiz.client.module.ClientModule module && module.isEnabled()) {
         try { module.setEnabled(false); } catch (Exception disableError) { ex.addSuppressed(disableError); }
      }
      String owner = handle.owner() == null ? "<static>" : handle.owner().getClass().getName();
      if (count <= 4) {
         System.err
            .println(
               "[AlbizEventBus] Listener threw (failure #"
                  + count
                  + "): owner="
                  + owner
                  + " event="
                  + event.getClass().getSimpleName()
                  + " stage="
                  + handle.stage()
                  + " priority="
                  + handle.priority()
            );
         ex.printStackTrace();
      } else if (count % 256 == 0) {
         System.err
            .println("[AlbizEventBus] Listener still failing (" + count + " total): owner=" + owner + " event=" + event.getClass().getSimpleName() + ": " + ex);
      }
   }
}
