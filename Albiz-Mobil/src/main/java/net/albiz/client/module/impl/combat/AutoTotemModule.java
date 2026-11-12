package net.albiz.client.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.PacketAppliedEvent;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.Render2DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.ModuleRegistry;
import net.albiz.client.module.impl.combat.totem.OffhandRefill;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1934;
import net.minecraft.class_2649;
import net.minecraft.class_2653;
import net.minecraft.class_2663;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_746;
import net.minecraft.class_9835;

@Environment(EnvType.CLIENT)
public final class AutoTotemModule extends ClientModule {
   private static final int OFFHAND_BUTTON = 40;
   private static final int OFFHAND_MENU_SLOT = 45;
   private static final int TOTEM_POP_EVENT = 35;
   private static final int NEAREST_CHOICES = 3;
   private static AutoTotemModule instance;
   private final NumberSetting refillDelay = this.addSetting("refill-delay", new NumberSetting("Refill Delay", 3.0F, 1.0F, 10.0F).step(1.0F));
   private final BooleanSetting useHotbar = this.addSetting("use-hotbar-totems", new BooleanSetting("Use Hotbar Totems", false));
   private final OffhandRefill refill = new OffhandRefill(
      new OffhandRefill.Driver() {
         @Override
         public boolean needsTotem() {
            return AutoTotemModule.player().method_6079().method_7960();
         }

         @Override
         public boolean canInteract() {
            class_310 mc = class_310.method_1551();
            class_746 player = mc.field_1724;
            return mc.field_1761.method_2920() == class_1934.field_9215
               && player.field_7512 == player.field_7498
               && player.field_7498.method_34255().method_7960();
         }

         @Override
         public int findSource() {
            return AutoTotemModule.this.findSource(AutoTotemModule.player().method_31548());
         }

         @Override
         public boolean validSource(int index) {
            class_746 player = AutoTotemModule.player();
            if (!AutoTotemModule.isSource(player.method_31548(), index, AutoTotemModule.this.useHotbar.get())) {
               return false;
            } else {
               class_1735 slot = player.field_7498.method_7611(AutoTotemModule.menuSlot(index));
               return slot.field_7871 == player.method_31548() && slot.method_34266() == index && slot.method_7674(player);
            }
         }

         @Override
         public void swap(int index) {
            class_310 mc = class_310.method_1551();
            class_746 player = mc.field_1724;
            mc.field_1761.method_2906(player.field_7498.field_7763, AutoTotemModule.menuSlot(index), 40, class_1713.field_7791, player);
            AutoTotemModule.this.previousSource = index;
         }

         @Override
         public int containerId() {
            return AutoTotemModule.player().field_7498.field_7763;
         }

         @Override
         public int stateId() {
            return AutoTotemModule.player().field_7498.method_37421();
         }

         @Override
         public int latencyMillis() {
            class_634 connection = class_310.method_1551().method_1562();
            class_640 info = connection == null ? null : connection.method_2871(AutoTotemModule.player().method_5667());
            return info == null ? 0 : info.method_2959();
         }
      },
      ThreadLocalRandom.current()
   );
   private int previousSource = -1;

   public AutoTotemModule() {
      super(
         "autototem",
         "Auto Totem",
         "Refills an empty offhand with a totem from your inventory, at a human pace you choose with Refill Delay.",
         ModuleCategory.COMBAT
      );
      instance = this;
   }

   public static boolean wantsHotbar() {
      return false;
   }

   public static boolean busy() {
      AutoTotemModule module = instance;
      class_746 player = class_310.method_1551().field_1724;
      return module != null && module.isEnabled() && player != null && !player.method_7325() && !player.method_56992()
         ? module.refill.busy() || player.method_6079().method_7960() && module.findSource(player.method_31548()) >= 0
         : false;
   }

   @Override
   protected void onEnable() {
      AutoInvTotemModule other = ModuleRegistry.get(AutoInvTotemModule.class);
      if (other != null) {
         other.setEnabled(false);
      }

      this.reset();
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @EventHandler(
      stage = Stage.POST
   )
   public void onTick(TickEvent event) {
      this.update();
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      this.update();
   }

   @EventHandler
   public void onPacketApplied(PacketAppliedEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (event.packet instanceof class_2663 status && status.method_11470() == 35 && status.method_11469(mc.field_1687) == mc.field_1724) {
            this.reset();
         }
      }
   }

   @EventHandler
   public void onPacketReceiveNetty(PacketReceiveNettyEvent event) {
      long now = System.nanoTime();
      if (event.packet instanceof class_9835 update) {
         if (update.comp_2891() == 40) {
            this.observe(-1, -1, offhand(update.comp_2892()), now);
         }
      } else if (event.packet instanceof class_2653 updatex) {
         if (updatex.method_11450() == 45) {
            this.observe(updatex.method_11452(), updatex.method_37439(), offhand(updatex.method_11449()), now);
         }
      } else if (event.packet instanceof class_2649 updatexx && updatexx.comp_3839().size() > 45) {
         this.observe(updatexx.comp_3837(), updatexx.comp_3838(), offhand((class_1799)updatexx.comp_3839().get(45)), now);
      }
   }

   private void observe(int containerId, int stateId, OffhandRefill.Offhand offhand, long receivedAt) {
      class_310.method_1551().execute(() -> {
         if (this.isEnabled()) {
            this.refill.observe(containerId, stateId, offhand, receivedAt);
         }
      });
   }

   private void update() {
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (mc.field_1687 != null && player != null && mc.field_1761 != null && player.method_5805() && !player.method_31481() && !(player.method_6032() <= 0.0F)
         )
       {
         this.refill.step(System.nanoTime(), this.refillDelay.getInt());
      } else {
         this.reset();
      }
   }

   private void reset() {
      this.refill.reset();
      this.previousSource = -1;
   }

   private int findSource(class_1661 inventory) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      if (this.useHotbar.get()) {
         List<Integer> hotbar = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            if (isSource(inventory, i, true)) {
               hotbar.add(i);
            }
         }

         if (!hotbar.isEmpty()) {
            return hotbar.get(random.nextInt(hotbar.size()));
         }
      }

      List<Integer> stored = new ArrayList<>();

      for (int ix = 9; ix < 36; ix++) {
         if (isSource(inventory, ix, false)) {
            stored.add(ix);
         }
      }

      if (stored.isEmpty()) {
         return -1;
      } else {
         int anchor = this.previousSource >= 9 && this.previousSource < 36 ? this.previousSource - 9 : random.nextInt(27);
         stored.sort(Comparator.comparingInt(index -> {
            int offset = index - 9;
            return Math.abs(offset % 9 - anchor % 9) + Math.abs(offset / 9 - anchor / 9);
         }));
         return stored.get(random.nextInt(Math.min(3, stored.size())));
      }
   }

   private static boolean isSource(class_1661 inventory, int index, boolean hotbar) {
      return index >= (hotbar ? 0 : 9) && index < 36 && inventory.method_5438(index).method_31574(class_1802.field_8288);
   }

   private static int menuSlot(int index) {
      return index < 9 ? index + 36 : index;
   }

   private static OffhandRefill.Offhand offhand(class_1799 stack) {
      if (stack.method_7960()) {
         return OffhandRefill.Offhand.EMPTY;
      } else {
         return stack.method_31574(class_1802.field_8288) ? OffhandRefill.Offhand.TOTEM : OffhandRefill.Offhand.OTHER;
      }
   }

   private static class_746 player() {
      return class_310.method_1551().field_1724;
   }
}
