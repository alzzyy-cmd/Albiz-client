package net.albiz.client.easyplace.builder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_638;
import net.minecraft.class_639;
import net.minecraft.class_642;

@Environment(EnvType.CLIENT)
final class Relogger {
   private static final int WAIT_TICKS = 60;
   private static final int TRIES = 5;
   private class_642 server;
   private boolean out;
   private int wait;
   private int tries;

   boolean active() {
      return this.server != null;
   }

   boolean start(class_310 mc) {
      class_642 data = mc.method_1542() ? null : mc.method_1558();
      if (data != null && !data.method_52811() && mc.field_1687 != null) {
         this.server = data;
         this.out = false;
         this.wait = 60;
         this.tries = 0;
         mc.method_63588(() -> {
            if (this.server == data && mc.field_1687 != null) {
               mc.method_73360(class_638.field_61021);
               this.out = true;
            }
         });
         return true;
      } else {
         return false;
      }
   }

   void tick(class_310 mc) {
      if (this.server != null && this.out) {
         if (mc.field_1687 != null) {
            this.server = null;
         } else if (!(mc.field_1755 instanceof class_412) && --this.wait <= 0) {
            if (++this.tries > 5) {
               this.server = null;
            } else {
               class_642 data = this.server;
               class_437 parent = (class_437)(mc.field_1755 instanceof class_500 ? mc.field_1755 : new class_500(new class_442()));
               this.wait = 180;
               mc.method_63588(() -> {
                  if (mc.field_1687 == null) {
                     class_412.method_36877(parent, mc, class_639.method_2950(data.field_3761), data, false, null);
                  }
               });
            }
         }
      }
   }
}
