// Detection rules adapted from SoberClient (dev.sober.modules.BedrockVoidESP), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.module.impl.render;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.albiz.client.esp.ChunkWorkQueue;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.BlockUpdateEvent;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.util.Chat;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;

/**
 * Highlights openings in the bedrock floor (and optionally the Nether roof).
 *
 * <p>The floor is the dimension's real bottom Y, not a hardcoded -64. Scanning is one 16x16 layer
 * per chunk, on the client thread, a few chunks per tick.
 */
public final class BedrockVoidEspModule extends ClientModule {
   private static final String TITLE = "Bedrock Void ESP";

   private final BooleanSetting onlyVoid = this.addSetting("only-void", new BooleanSetting("Only Void", true));
   private final BooleanSetting netherRoof = this.addSetting("nether-roof", new BooleanSetting("Nether Roof", false));
   private final NumberSetting chunksPerTick = this.addSetting(
      "chunks-per-tick", new NumberSetting("Chunks Per Tick", PlatformInfo.isMobile() ? 2.0F : 6.0F, 1.0F, 24.0F).step(1.0F)
   );
   private final NumberSetting rescanSeconds = this.addSetting("rescan-seconds", new NumberSetting("Rescan Seconds", 10.0F, 2.0F, 120.0F).step(1.0F));
   private final ColorSetting color = this.addSetting("color", new ColorSetting("Color", 0x8CA05AFF));
   private final BooleanSetting beam = this.addSetting("beam", new BooleanSetting("Beam", true));
   private final NumberSetting beamHeight = this.addSetting("beam-height", new NumberSetting("Beam Height", 80.0F, 10.0F, 320.0F).step(1.0F));
   private final BooleanSetting throughWalls = this.addSetting("through-walls", new BooleanSetting("Through Walls", true));
   private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
   private final BooleanSetting notify = this.addSetting("notify", new BooleanSetting("Notify", true));

   private final int holeLimit = PlatformInfo.isMobile() ? 1024 : 4096;
   /** Block position (packed) to first-seen time. Insertion ordered so the oldest entry is dropped at the cap. */
   private final LinkedHashMap<Long, Long> holes = new LinkedHashMap<>();
   private final ChunkWorkQueue work = new ChunkWorkQueue(4096);
   private Object world;
   private long nextRefillMs;
   private int pendingAlerts;
   private long pendingPos;
   private long alertAtMs;
   private boolean settingsDirty;

   public BedrockVoidEspModule() {
      super("bedrock-void-esp", TITLE, "Shows holes in the bedrock floor where you can look or fall into the void.", ModuleCategory.BASE_RAID);
      this.beamHeight.visibleWhen(this.beam::get);
      this.onlyVoid.onChange(() -> this.settingsDirty = true);
      this.netherRoof.onChange(() -> this.settingsDirty = true);
   }

   @Override
   protected void onEnable() {
      this.reset();
   }

   @Override
   protected void onDisable() {
      this.reset();
      this.world = null;
   }

   private void reset() {
      this.holes.clear();
      this.work.clear();
      this.nextRefillMs = 0L;
      this.pendingAlerts = 0;
      this.settingsDirty = false;
   }

   private static boolean scannable(class_638 level) {
      return level.method_27983() != class_1937.field_25181;
   }

   private boolean roofActive(class_638 level) {
      return this.netherRoof.get() && level.method_27983() == class_1937.field_25180;
   }

   private static int roofY(class_638 level) {
      return level.method_8597().comp_653() - 1;
   }

   private boolean isHole(class_2680 state) {
      if (state.method_27852(class_2246.field_9987)) {
         return false;
      }

      return !this.onlyVoid.get() || state.method_26215() || state.method_45474();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level != this.world) {
         this.world = level;
         this.reset();
      }

      if (level == null || mc.field_1724 == null || !scannable(level)) {
         return;
      }

      if (this.settingsDirty) {
         this.reset();
      }

      long now = System.currentTimeMillis();
      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      int radius = (Integer)mc.field_1690.method_42503().method_41753() + 1;
      if (this.work.isEmpty() && now >= this.nextRefillMs) {
         this.work.offerSquare(cx, cz, radius);
         this.nextRefillMs = now + (long)this.rescanSeconds.getInt() * 1000L;
         this.prune(level, cx, cz, radius + 2);
      }

      int budget = net.albiz.client.module.impl.client.PerformanceModule.chunksPerTick(this.chunksPerTick.getInt());
      while (budget > 0 && !this.work.isEmpty()) {
         long key = this.work.poll();
         class_2818 chunk = level.method_2935().method_21730(ChunkWorkQueue.x(key), ChunkWorkQueue.z(key));
         if (chunk == null) {
            continue;
         }

         budget--;
         this.scanLayer(level, chunk, level.method_31607());
         if (this.roofActive(level)) {
            this.scanLayer(level, chunk, roofY(level));
         }
      }

      if (this.pendingAlerts > 0 && now >= this.alertAtMs) {
         class_2338 pos = class_2338.method_10092(this.pendingPos);
         String where = pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260();
         Chat.info(
            TITLE,
            this.pendingAlerts == 1
               ? AlbizI18n.display("Bedrock hole found at") + " " + where
               : this.pendingAlerts + " " + AlbizI18n.display("bedrock holes found, closest at") + " " + where
         );
         this.pendingAlerts = 0;
      }
   }

   private void prune(class_638 level, int cx, int cz, int keep) {
      Iterator<Map.Entry<Long, Long>> it = this.holes.entrySet().iterator();
      while (it.hasNext()) {
         long packed = it.next().getKey();
         int x = class_2338.method_10061(packed) >> 4;
         int z = class_2338.method_10083(packed) >> 4;
         if (Math.abs(x - cx) > keep || Math.abs(z - cz) > keep || !level.method_2935().method_12123(x, z)) {
            it.remove();
         }
      }
   }

   private void scanLayer(class_638 level, class_2818 chunk, int y) {
      int index = chunk.method_31602(y);
      class_2826[] sections = chunk.method_12006();
      if (index < 0 || index >= sections.length) {
         return;
      }

      int baseX = chunk.method_12004().method_8326();
      int baseZ = chunk.method_12004().method_8328();
      class_2826 section = sections[index];
      // An entirely empty bottom section is a void world or withheld data, not a hole in a bedrock floor.
      boolean empty = section == null || section.method_38292();
      int localY = y & 15;

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            long packed = class_2338.method_10064(baseX + x, y, baseZ + z);
            if (!empty && this.isHole(section.method_12254(x, localY, z))) {
               this.found(packed);
            } else {
               this.holes.remove(packed);
            }
         }
      }
   }

   private void found(long packed) {
      if (this.holes.containsKey(packed)) {
         return;
      }

      if (this.holes.size() >= this.holeLimit) {
         Iterator<Long> eldest = this.holes.keySet().iterator();
         eldest.next();
         eldest.remove();
      }

      long now = System.currentTimeMillis();
      this.holes.put(packed, now);
      if (!this.notify.get()) {
         return;
      }

      class_310 mc = class_310.method_1551();
      if (this.pendingAlerts == 0) {
         this.alertAtMs = now + 1500L;
         this.pendingPos = packed;
      } else if (mc.field_1724 != null) {
         class_2338 current = class_2338.method_10092(this.pendingPos);
         class_2338 candidate = class_2338.method_10092(packed);
         if (mc.field_1724.method_5707(candidate.method_46558()) < mc.field_1724.method_5707(current.method_46558())) {
            this.pendingPos = packed;
         }
      }

      this.pendingAlerts++;
   }

   @EventHandler
   public void onBlockUpdate(BlockUpdateEvent event) {
      class_638 level = class_310.method_1551().field_1687;
      if (level == null || event.pos == null || event.newState == null || !scannable(level)) {
         return;
      }

      int y = event.pos.method_10264();
      if (y != level.method_31607() && !(this.roofActive(level) && y == roofY(level))) {
         return;
      }

      long packed = event.pos.method_10063();
      if (this.isHole(event.newState)) {
         this.found(packed);
      } else {
         this.holes.remove(packed);
      }
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (this.holes.isEmpty() || class_310.method_1551().field_1687 == null) {
         return;
      }

      Render3D render = new Render3D(event);
      render.setDepthTest(!this.throughWalls.get());
      Color base = Color.fromPacked(this.color.argb());
      Color edge = base.withAlpha(255);
      boolean drawBeam = this.beam.get();
      boolean drawTracers = this.tracers.get();
      int height = this.beamHeight.getInt();
      long lastBeamColumn = Long.MIN_VALUE;

      for (long packed : this.holes.keySet()) {
         int x = class_2338.method_10061(packed);
         int y = class_2338.method_10071(packed);
         int z = class_2338.method_10083(packed);
         render.box(x, y, z, x + 1, y + 1, z + 1, base, edge, ShapeMode.Both);
         if (drawBeam) {
            // One beam per 4x4 column group; holes are stored chunk by chunk, so neighbours are adjacent in iteration order.
            long column = ChunkWorkQueue.key(x >> 2, z >> 2);
            if (column != lastBeamColumn) {
               lastBeamColumn = column;
               render.beam(x + 0.5, z + 0.5, y + 1.0, y + 1.0 + height, 0.2, edge, 0.75);
            }
         }

         if (drawTracers) {
            render.tracer(x + 0.5, y + 0.5, z + 0.5, edge);
         }
      }

      render.setDepthTest(false);
   }
}
