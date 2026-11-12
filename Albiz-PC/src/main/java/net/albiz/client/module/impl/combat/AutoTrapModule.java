package net.albiz.client.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.impl.combat.crystal.Crystals;
import net.albiz.client.module.impl.combat.melee.MeleeAttacks;
import net.albiz.client.module.impl.combat.melee.SlotReturn;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.util.pvp.Rotation;
import net.albiz.client.util.pvp.RotationUtils;
import net.albiz.client.util.pvp.ServerRotation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public final class AutoTrapModule extends ClientModule {
   private static final class_2350[] SUPPORT_ORDER = new class_2350[]{
      class_2350.field_11033, class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039, class_2350.field_11036
   };
   private static final int TARGET_COLOR = -24512;
   private static final int CELL_COLOR = -2130706433;
   private final NumberSetting range = this.addSetting("range", new NumberSetting("Range", 4.0F, 1.0F, 6.0F).step(0.5F));
   private final BooleanSetting rotate = this.addSetting("rotate", new BooleanSetting("Rotate", true));
   private final NumberSetting perTick = this.addSetting("blocks-per-tick", new NumberSetting("Blocks Per Tick", 4.0F, 1.0F, 8.0F).step(1.0F));
   private final BooleanSetting top = this.addSetting("top-block", new BooleanSetting("Top Block", true));
   private final BooleanSetting show = this.addSetting("show-target", new BooleanSetting("Show Target", true));
   private final class_310 mc = class_310.method_1551();
   private final SlotReturn slot = new SlotReturn(this);
   private class_1657 target;
   private List<class_2338> plan = List.of();
   private boolean warnedEmpty;

   public AutoTrapModule() {
      super("auto-trap", "Auto Trap", "Boxes the nearest enemy in with obsidian: sides at feet and head height, then the top.", ModuleCategory.COMBAT);
      this.perTick.visibleWhen(() -> !this.rotate.get());
   }

   @Override
   protected void onEnable() {
      this.target = null;
      this.plan = List.of();
      this.warnedEmpty = false;
      this.on(TickEvent.TYPE, Stage.PRE, event -> this.tick());
      this.on(Render3DEvent.TYPE, Stage.POST, event -> this.render());
   }

   @Override
   protected void onDisable() {
      this.slot.drop(this.mc, true);
      this.target = null;
      this.plan = List.of();
   }

   private void tick() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null && this.mc.field_1761 != null && MeleeAttacks.canAct(this.mc)) {
         this.target = this.pick(player);
         this.plan = this.target == null ? List.of() : this.cells(level, player, this.target);
         if (this.plan.isEmpty()) {
            this.slot.tick(this.mc, true);
         } else {
            int obsidian = Crystals.hotbarSlotOf(player.method_31548(), class_1802.field_8281);
            if (obsidian < 0) {
               if (!this.warnedEmpty) {
                  ModuleChat.send(this, ModuleChat.body().warn("No obsidian in the hotbar"));
               }

               this.warnedEmpty = true;
               this.slot.tick(this.mc, true);
            } else {
               this.warnedEmpty = false;
               int budget = this.rotate.get() ? 1 : Math.round(this.perTick.get());
               int placed = 0;

               for (class_2338 cell : this.plan) {
                  if (placed < budget) {
                     class_3965 hit = support(level, player, cell);
                     if (hit == null) {
                        continue;
                     }

                     if (this.rotate.get()) {
                        class_3965 seen = seenByServer(level, player, hit);
                        if (seen == null) {
                           ServerRotation.request(RotationUtils.fromTo(player.method_33571(), hit.method_17784()));
                           return;
                        }

                        hit = seen;
                     }

                     if (placed != 0 || !MeleeAttacks.actedThisTick(player) && MeleeAttacks.handsFree(this.mc, player)) {
                        if (!this.slot.take(player, obsidian)) {
                           return;
                        }

                        class_1269 result = this.mc.field_1761.method_2896(player, class_1268.field_5808, hit);
                        if (result.method_23665()) {
                           player.method_6104(class_1268.field_5808);
                           MeleeAttacks.noteAction(player);
                           placed++;
                        }
                        continue;
                     }

                     return;
                  }
                  break;
               }

               if (this.rotate.get() && placed > 0) {
                  ServerRotation.request(ServerRotation.sent());
               }

               return;
            }
         }
      } else {
         this.slot.tick(this.mc, true);
      }
   }

   private class_1657 pick(class_746 player) {
      double reach = this.range.get() + 1.5;
      class_1657 best = null;

      for (class_1657 other : this.mc.field_1687.method_18456()) {
         if (other != player && other.method_5805() && MeleeAttacks.isEnemy(player, other)) {
            double distance = other.method_5739(player);
            if (distance <= reach && (best == null || distance < best.method_5739(player))) {
               best = other;
            }
         }
      }

      return best;
   }

   private List<class_2338> cells(class_638 level, class_746 player, class_1657 who) {
      class_238 box = who.method_5829();
      int feet = class_3532.method_15357(box.field_1322 + 0.01);
      Set<Long> columns = new LinkedHashSet<>();

      for (int x = class_3532.method_15357(box.field_1323); x <= class_3532.method_15357(box.field_1320 - 1.0E-4); x++) {
         for (int z = class_3532.method_15357(box.field_1321); z <= class_3532.method_15357(box.field_1324 - 1.0E-4); z++) {
            columns.add(class_2338.method_10064(x, 0, z));
         }
      }

      List<class_2338> out = new ArrayList<>();

      for (int dy = 0; dy <= 1; dy++) {
         List<class_2338> ring = new ArrayList<>();

         for (long key : columns) {
            class_2338 column = class_2338.method_10092(key).method_33096(feet + dy);

            for (class_2350 side : class_2353.field_11062) {
               class_2338 cell = column.method_10093(side);
               if (!columns.contains(class_2338.method_10064(cell.method_10263(), 0, cell.method_10260()))
                  && this.open(level, player, cell)
                  && !ring.contains(cell)) {
                  ring.add(cell);
               }
            }
         }

         ring.sort(Comparator.comparingDouble(cellx -> cellx.method_19770(player.method_33571())));
         out.addAll(ring);
      }

      if (this.top.get()) {
         for (long key : columns) {
            class_2338 cell = class_2338.method_10092(key).method_33096(feet + 2);
            if (this.open(level, player, cell)) {
               out.add(cell);
            }
         }
      }

      return out;
   }

   private boolean open(class_638 level, class_746 player, class_2338 cell) {
      class_2680 state = level.method_8320(cell);
      if (state.method_45474() && (state.method_26227().method_15769() || state.method_26227().method_15771())) {
         return cell.method_46558().method_1022(player.method_33571()) > player.method_55754()
            ? false
            : level.method_8628(class_2246.field_10540.method_9564(), cell, class_3726.method_16194());
      } else {
         return false;
      }
   }

   public static class_3965 support(class_638 level, class_746 player, class_2338 cell) {
      class_243 eye = player.method_33571();
      double reach = player.method_55754();

      for (class_2350 toward : SUPPORT_ORDER) {
         class_2338 against = cell.method_10093(toward);
         class_2680 state = level.method_8320(against);
         if (!state.method_45474() && !state.method_26220(level, against).method_1110()) {
            class_2350 face = toward.method_10153();
            class_243 point = class_243.method_24953(against).method_1031(face.method_10148() * 0.5, face.method_10164() * 0.5, face.method_10165() * 0.5);
            if (!(point.method_1022(eye) > reach)) {
               class_3965 ray = level.method_17742(
                  new class_3959(
                     eye,
                     point.method_1019(class_243.method_24954(toward.method_62675()).method_1021(0.05)),
                     class_3960.field_17559,
                     class_242.field_1348,
                     player
                  )
               );
               if (ray.method_17783() == class_240.field_1332 && ray.method_17777().equals(against) && ray.method_17780() == face) {
                  return new class_3965(point, face, against, false);
               }
            }
         }
      }

      return null;
   }

   public static class_3965 seenByServer(class_638 level, class_746 player, class_3965 wanted) {
      Rotation sent = ServerRotation.sent();
      if (sent == null) {
         return null;
      } else {
         class_243 eye = player.method_33571();
         class_243 end = eye.method_1019(class_243.method_1030(sent.pitch(), sent.yaw()).method_1021(player.method_55754()));
         class_3965 ray = level.method_17742(new class_3959(eye, end, class_3960.field_17559, class_242.field_1348, player));
         return ray.method_17783() == class_240.field_1332 && ray.method_17777().equals(wanted.method_17777()) && ray.method_17780() == wanted.method_17780()
            ? ray
            : null;
      }
   }

   private void render() {
      if (this.show.get() && this.target != null) {
         class_12180.method_75541(this.target.method_5829(), class_12179.method_75536(-24512, 2.0F));

         for (class_2338 cell : this.plan) {
            class_12180.method_75550(cell, class_12179.method_75536(-2130706433, 1.0F));
         }
      }
   }
}
