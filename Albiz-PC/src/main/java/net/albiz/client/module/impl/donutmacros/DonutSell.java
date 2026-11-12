package net.albiz.client.module.impl.donutmacros;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.albiz.client.event.EventPriority;
import net.albiz.client.event.ListenerHandle;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.setting.StringListSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_310;
import net.minecraft.class_7439;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class DonutSell {
   private static final Pattern RATE_LIMIT = Pattern.compile("you need to wait another ([\\d.]+) seconds?");
   private static final long GIVE_UP_MS = 45000L;
   private final SellRoutine routine;
   private final ListenerHandle<PacketReceiveNettyEvent> chat;
   private final long started = System.currentTimeMillis();
   private boolean sent;
   private boolean done;
   private int ticks;

   private DonutSell(ClientModule owner, Map<Integer, class_1792> slots) {
      Map<Integer, class_1792> wanted = Map.copyOf(slots);
      this.routine = new SellRoutine(
         owner,
         new StringListSetting("Never Sell"),
         (index, stack) -> wanted.get(index) == null || !stack.method_31574(wanted.get(index)),
         () -> this.sent = true
      );
      this.chat = PacketReceiveNettyEvent.TYPE.listen(this, Stage.PRE, EventPriority.NORMAL, event -> {
         if (event.packet instanceof class_7439 packet && packet.comp_763() != null) {
            Matcher wait = RATE_LIMIT.matcher(ServerText.normalize(packet.comp_763().getString()));
            if (wait.find()) {
               try {
                  long retry = System.currentTimeMillis() + (long)Math.ceil(Double.parseDouble(wait.group(1)) * 1000.0) + 100L;
                  class_310.method_1551().execute(() -> this.routine.commandRejected(retry));
               } catch (NumberFormatException var6) {
               }
            }
         }
      });
   }

   public static DonutSell start(ClientModule owner, Map<Integer, class_1792> slots) {
      return new DonutSell(owner, slots);
   }

   public boolean tick() {
      if (this.done) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         class_746 player = mc.field_1724;
         if (player != null && mc.method_1562() != null && mc.field_1761 != null) {
            long now = System.currentTimeMillis();
            boolean busy = this.routine.step(mc, player, mc.method_1562(), now, true);
            this.ticks++;
            if (!busy && !this.routine.active()) {
               if (this.sent || this.ticks > 3 || now - this.started > 45000L) {
                  this.finish();
               }

               return !this.done;
            } else {
               if (now - this.started > 45000L) {
                  this.cancel();
               }

               return !this.done;
            }
         } else {
            this.cancel();
            return false;
         }
      }
   }

   public void cancel() {
      if (!this.done) {
         this.routine.abort(class_310.method_1551());
         this.finish();
      }
   }

   private void finish() {
      this.done = true;
      this.chat.unregister();
   }
}
