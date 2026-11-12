package net.albiz.client.easyplace.sync;

import net.albiz.client.easyplace.schematic.SchematicTarget;
import net.albiz.client.easyplace.util.BlockStateUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2680;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public final class ServerAckManager {
   private PlacementConfirmation pending;

   public void begin(SchematicTarget target, long tick, int timeoutTicks, int retry, int sequence, class_2680 predicted) {
      this.pending = new PlacementConfirmation(target.pos(), target.expected(), tick, tick + timeoutTicks, retry, sequence, predicted);
   }

   public ServerAckManager.Result poll(class_638 level, long tick) {
      if (this.pending == null) {
         return ServerAckManager.Result.IDLE;
      } else if (!BlockAcks.answered(level, this.pending.sequence())) {
         return tick >= this.pending.deadlineTick() ? ServerAckManager.Result.TIMEOUT : ServerAckManager.Result.WAITING;
      } else {
         class_2680 wanted = this.pending.predicted() != null && !this.pending.predicted().method_26215()
            ? this.pending.predicted()
            : this.pending.expected().state();
         return BlockStateUtil.statesMatch(wanted, level.method_8320(this.pending.pos())) ? ServerAckManager.Result.CONFIRMED : ServerAckManager.Result.REFUSED;
      }
   }

   public PlacementConfirmation getPending() {
      return this.pending;
   }

   public void clear() {
      this.pending = null;
   }

   @Environment(EnvType.CLIENT)
   public static enum Result {
      IDLE,
      WAITING,
      CONFIRMED,
      REFUSED,
      TIMEOUT;
   }
}
