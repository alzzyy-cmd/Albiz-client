package net.albiz.client.module.impl.render;

import net.albiz.client.config.FriendStore;
import net.albiz.client.esp.SkeletonPose;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;

/**
 * Draws a stick skeleton on players. The pose is reconstructed from body yaw, head rotation, the
 * walk cycle and the sneaking pose (see {@link SkeletonPose}); it needs no model mixin and keeps
 * no per-player state, so nothing can leak between players or frames.
 */
public final class SkeletonEspModule extends ClientModule {
   private final NumberSetting range = this.addSetting("range", new NumberSetting("Range", 96.0F, 8.0F, 256.0F).step(8.0F));
   private final NumberSetting maxPlayers = this.addSetting(
      "max-players", new NumberSetting("Max Players", PlatformInfo.isMobile() ? 16.0F : 48.0F, 1.0F, 100.0F).step(1.0F)
   );
   private final NumberSetting width = this.addSetting("line-width", new NumberSetting("Line Width", 2.0F, 1.0F, 5.0F).step(0.5F));
   private final BooleanSetting throughWalls = this.addSetting("through-walls", new BooleanSetting("Through Walls", true));
   private final BooleanSetting self = this.addSetting("self", new BooleanSetting("Show Self In Third Person", false));
   private final BooleanSetting invisible = this.addSetting("invisible", new BooleanSetting("Show Invisible Players", true));
   private final BooleanSetting friends = this.addSetting("friends", new BooleanSetting("Highlight Friends", true));
   private final ColorSetting color = this.addSetting("color", new ColorSetting("Color", 0xFFFFFFFF));
   private final ColorSetting friendColor = this.addSetting("friend-color", new ColorSetting("Friend Color", 0xFF5CFF8A));
   private final ColorSetting sneakColor = this.addSetting("sneak-color", new ColorSetting("Sneaking Color", 0xFFFFB75C));

   private final float[] bones = new float[SkeletonPose.FLOATS];

   public SkeletonEspModule() {
      super("skeleton-esp", "Skeleton ESP", "Draws a skeleton on players that follows their walking, sneaking and head movement.", ModuleCategory.RENDER);
      this.friendColor.visibleWhen(this.friends::get);
   }

   private static float lerpDegrees(float previous, float current, float t) {
      float delta = (current - previous) % 360.0F;
      if (delta > 180.0F) {
         delta -= 360.0F;
      } else if (delta < -180.0F) {
         delta += 360.0F;
      }

      return previous + delta * t;
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null || mc.field_1724 == null) {
         return;
      }

      float partial = event.partialTick;
      double limit = this.range.get();
      double limitSq = limit * limit;
      int budget = this.maxPlayers.getInt();
      boolean firstPerson = mc.field_1690.method_31044().method_31034();
      Render3D render = new Render3D(event);
      render.setDepthTest(!this.throughWalls.get());
      render.setLineWidth(this.width.get());
      Color normal = Color.fromPacked(this.color.argb());
      Color friend = Color.fromPacked(this.friendColor.argb());
      Color sneaking = Color.fromPacked(this.sneakColor.argb());
      boolean markFriends = this.friends.get();
      boolean showInvisible = this.invisible.get();
      class_243 eye = mc.field_1724.method_30950(partial);

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (budget <= 0) {
            break;
         }

         if (player == mc.field_1724 && (firstPerson || !this.self.get())) {
            continue;
         }

         if (!player.method_5805() || player.method_7325() || player.method_5767() && !showInvisible) {
            continue;
         }

         class_243 feet = player.method_30950(partial);
         double dx = feet.field_1352 - eye.field_1352;
         double dy = feet.field_1351 - eye.field_1351;
         double dz = feet.field_1350 - eye.field_1350;
         if (dx * dx + dy * dy + dz * dz > limitSq) {
            continue;
         }

         float height = player.method_17682();
         if (!render.inView(feet.field_1352 - 1.0, feet.field_1351, feet.field_1350 - 1.0, feet.field_1352 + 1.0, feet.field_1351 + height + 0.3, feet.field_1350 + 1.0)) {
            continue;
         }

         budget--;
         boolean sneak = player.method_18276();
         SkeletonPose.compute(
            lerpDegrees(player.field_6220, player.field_6283, partial),
            lerpDegrees(player.field_6259, player.field_6241, partial),
            player.method_5695(partial),
            player.field_42108.method_48569(),
            player.field_42108.method_48566(),
            sneak,
            height,
            this.bones
         );
         Color tint = markFriends && FriendStore.getInstance().isFriend(player.method_7334().name()) ? friend : sneak ? sneaking : normal;
         for (int i = 0; i < SkeletonPose.FLOATS; i += 6) {
            render.line(
               feet.field_1352 + this.bones[i],
               feet.field_1351 + this.bones[i + 1],
               feet.field_1350 + this.bones[i + 2],
               feet.field_1352 + this.bones[i + 3],
               feet.field_1351 + this.bones[i + 4],
               feet.field_1350 + this.bones[i + 5],
               tint
            );
         }
      }

      render.setDepthTest(false);
      render.setLineWidth(1.0F);
   }
}
