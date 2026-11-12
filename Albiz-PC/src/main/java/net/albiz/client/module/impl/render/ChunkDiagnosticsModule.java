package net.albiz.client.module.impl.render;

import net.albiz.client.diagnostics.ChunkReceiptLog;
import net.albiz.client.diagnostics.ChunkReceiptLog.Packet;
import net.albiz.client.diagnostics.ChunkReceiptLog.Receipt;
import net.albiz.client.diagnostics.ChunkReceiptLog.Status;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.ActionSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.util.Chat;
import net.albiz.client.sound.WorldSignalRules;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2672;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_7923;

/**
 * Answers "why is nothing detected below Y=0 here?" with facts instead of guesses.
 *
 * <p>It records which chunk-data, section-update and block-update packets arrived and, on demand,
 * reports what the client world really holds for a chunk: the dimension's build range, which
 * sections contain blocks, and where kelp was received. It cannot see data the server withheld;
 * an air-only section is reported "as received", because the client cannot tell real air from
 * blocks a server-side filter replaced with air.
 */
public final class ChunkDiagnosticsModule extends ClientModule {
   private static final String TITLE = "Chunk Diagnostics";

   private final NumberSetting summaryRadius = this.addSetting("summary-radius", new NumberSetting("Summary Radius", 4.0F, 1.0F, 12.0F).step(1.0F));
   private final ActionSetting printChunk = this.addSetting("print-chunk", new ActionSetting("Print Current Chunk", this::printCurrentChunk));
   private final ActionSetting printNearby = this.addSetting("print-nearby", new ActionSetting("Print Nearby Summary", this::printNearby));
   private final ActionSetting clear = this.addSetting("clear-counters", new ActionSetting("Clear Results", this::clearCounters));

   private final ChunkReceiptLog receipts = new ChunkReceiptLog(PlatformInfo.isMobile() ? 1024 : 4096);
   private volatile boolean accepting;
   private Object world;

   public ChunkDiagnosticsModule() {
      super(
         "chunk-diagnostics",
         TITLE,
         "Reports which chunk packets arrived and what the client really holds per section, including below Y=0. Cannot reveal data the server did not send.",
         ModuleCategory.BASE_RAID
      );
   }

   @Override
   protected void onEnable() {
      this.receipts.clear();
      this.world = class_310.method_1551().field_1687;
      this.accepting = true;
   }

   @Override
   protected void onDisable() {
      this.accepting = false;
      this.receipts.clear();
      this.world = null;
   }

   /** Network thread: counters only, no world access. */
   @EventHandler
   public void onIncoming(PacketReceiveNettyEvent event) {
      if (!this.accepting) {
         return;
      }

      class_2596<?> packet = event.packet;
      long now = System.currentTimeMillis();

      try {
         if (packet instanceof class_2672 chunk) {
            this.receipts.record(chunk.method_11523(), chunk.method_11524(), Packet.CHUNK_DATA, now);
         } else if (packet instanceof class_2626 update) {
            class_2338 pos = update.method_11309();
            this.receipts.record(pos.method_10263() >> 4, pos.method_10260() >> 4, Packet.BLOCK_UPDATE, now);
         } else if (packet instanceof class_2637 section) {
            int[] chunk = new int[3];
            section.method_30621((pos, state) -> {
               if (chunk[2] == 0) {
                  chunk[0] = pos.method_10263() >> 4;
                  chunk[1] = pos.method_10260() >> 4;
                  chunk[2] = 1;
               }
            });
            if (chunk[2] != 0) {
               this.receipts.record(chunk[0], chunk[1], Packet.SECTION_UPDATE, now);
            }
         }
      } catch (RuntimeException | LinkageError ignored) {
         // A packet this module cannot read is simply not counted; it is still applied by the game.
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      if (this.world != mc.field_1687) {
         this.world = mc.field_1687;
         this.receipts.clear();
      }
   }

   private static String t(String english) {
      return AlbizI18n.display(english);
   }

   private boolean ready(class_310 mc) {
      if (!this.isEnabled()) {
         Chat.info(TITLE, t("Enable the module first."));
         return false;
      }

      if (mc.field_1687 == null || mc.field_1724 == null) {
         Chat.info(TITLE, t("Join a world first."));
         return false;
      }

      return true;
   }

   private static boolean isKelp(class_2680 state) {
      return WorldSignalRules.kelp(class_7923.field_41175.method_10221(state.method_26204()).method_12832());
   }

   private void printCurrentChunk() {
      class_310 mc = class_310.method_1551();
      if (!this.ready(mc)) {
         return;
      }

      class_638 level = mc.field_1687;
      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      class_2818 chunk = level.method_2935().method_21730(cx, cz);
      Status status = this.receipts.status(cx, cz, chunk != null);
      Receipt receipt = this.receipts.snapshot(cx, cz);
      Chat.info(
         TITLE,
         t("Chunk") + " " + cx + ", " + cz + " · " + status + " · " + t("chunk data") + " x" + (receipt == null ? 0 : receipt.count(Packet.CHUNK_DATA)) + ", "
            + t("section updates") + " x" + (receipt == null ? 0 : receipt.count(Packet.SECTION_UPDATE)) + ", " + t("block updates") + " x"
            + (receipt == null ? 0 : receipt.count(Packet.BLOCK_UPDATE))
      );

      int bottomY = level.method_31607();
      int height = level.method_31605();
      Chat.info(TITLE, t("World build range") + ": Y " + bottomY + " .. " + (bottomY + height - 1));
      if (chunk == null) {
         Chat.info(TITLE, t("The chunk is not in the client world; there is nothing to inspect."));
         return;
      }

      class_2826[] sections = chunk.method_12006();
      int bottomSection = chunk.method_32891();
      int withBlocks = 0;
      int belowZero = 0;
      int belowZeroWithBlocks = 0;
      int lowestBlockSectionY = Integer.MAX_VALUE;
      int kelp = 0;
      int kelpMinY = Integer.MAX_VALUE;
      int kelpMaxY = Integer.MIN_VALUE;

      for (int i = 0; i < sections.length; i++) {
         class_2826 section = sections[i];
         int baseY = ChunkReceiptLog.sectionBaseY(bottomSection, i);
         boolean empty = section == null || section.method_38292();
         if (baseY < 0) {
            belowZero++;
            if (!empty) {
               belowZeroWithBlocks++;
            }
         }

         if (empty) {
            continue;
         }

         withBlocks++;
         lowestBlockSectionY = Math.min(lowestBlockSectionY, baseY);
         if (!section.method_19523(ChunkDiagnosticsModule::isKelp)) {
            continue;
         }

         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  if (isKelp(section.method_12254(x, y, z))) {
                     kelp++;
                     kelpMinY = Math.min(kelpMinY, baseY + y);
                     kelpMaxY = Math.max(kelpMaxY, baseY + y);
                  }
               }
            }
         }
      }

      Chat.info(
         TITLE,
         t("Sections with blocks") + ": " + withBlocks + "/" + sections.length
            + (withBlocks > 0 ? " · " + t("lowest at Y") + " " + lowestBlockSectionY : "")
      );
      if (belowZero > 0) {
         Chat.info(TITLE, t("Sections below Y 0 with blocks") + ": " + belowZeroWithBlocks + "/" + belowZero);
         if (belowZeroWithBlocks == 0) {
            Chat.info(TITLE, t("Everything below Y 0 is air as received: a void world, or the server did not send those blocks."));
         }
      } else {
         Chat.info(TITLE, t("This dimension has no build space below Y 0."));
      }

      Chat.info(
         TITLE,
         t("Kelp blocks received") + ": " + kelp + (kelp > 0 ? " (Y " + kelpMinY + " .. " + kelpMaxY + ")" : "") + " · " + t("Block entities") + ": "
            + chunk.method_12214().size()
      );
      Chat.info(TITLE, t("Air-only sections are shown as received; the client cannot tell hidden blocks from real air."));
   }

   private void printNearby() {
      class_310 mc = class_310.method_1551();
      if (!this.ready(mc)) {
         return;
      }

      class_638 level = mc.field_1687;
      int cx = mc.field_1724.method_31477() >> 4;
      int cz = mc.field_1724.method_31479() >> 4;
      int radius = this.summaryRadius.getInt();
      int[] byStatus = new int[Status.values().length];
      int loaded = 0;
      int airBelowZero = 0;
      boolean hasBelowZero = level.method_31607() < 0;

      for (int x = cx - radius; x <= cx + radius; x++) {
         for (int z = cz - radius; z <= cz + radius; z++) {
            class_2818 chunk = level.method_2935().method_21730(x, z);
            byStatus[this.receipts.status(x, z, chunk != null).ordinal()]++;
            if (chunk == null) {
               continue;
            }

            loaded++;
            if (!hasBelowZero) {
               continue;
            }

            class_2826[] sections = chunk.method_12006();
            int bottomSection = chunk.method_32891();
            boolean anyBlocks = false;
            for (int i = 0; i < sections.length && ChunkReceiptLog.sectionBaseY(bottomSection, i) < 0; i++) {
               if (sections[i] != null && !sections[i].method_38292()) {
                  anyBlocks = true;
                  break;
               }
            }

            if (!anyBlocks) {
               airBelowZero++;
            }
         }
      }

      StringBuilder line = new StringBuilder();
      for (Status status : Status.values()) {
         if (line.length() > 0) {
            line.append(" · ");
         }

         line.append(status).append(' ').append(byStatus[status.ordinal()]);
      }

      Chat.info(TITLE, t("Chunks within radius") + " " + radius + ": " + line);
      if (hasBelowZero) {
         Chat.info(TITLE, t("Loaded chunks with only air below Y 0, as received") + ": " + airBelowZero + "/" + loaded);
      }

      Chat.info(TITLE, t("UNKNOWN means the chunk arrived before this module was enabled; rejoin or move away and back to observe its packet."));
   }

   private void clearCounters() {
      this.receipts.clear();
      Chat.info(TITLE, t("Results cleared."));
   }
}
