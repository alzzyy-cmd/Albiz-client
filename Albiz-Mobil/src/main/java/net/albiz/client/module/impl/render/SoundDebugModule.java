package net.albiz.client.module.impl.render;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import net.albiz.client.config.ConfigMigration;
import net.albiz.client.event.EventHandler;
import net.albiz.client.event.impl.BlockUpdateEvent;
import net.albiz.client.event.impl.PacketReceiveNettyEvent;
import net.albiz.client.event.impl.Render3DEvent;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.i18n.AlbizI18n;
import net.albiz.client.module.ClientModule;
import net.albiz.client.module.ModuleCategory;
import net.albiz.client.module.setting.ActionSetting;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.albiz.client.platform.PlatformInfo;
import net.albiz.client.port.sober.gui.Notifications;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.util.Chat;
import net.albiz.client.sound.PacketDiagnostics;
import net.albiz.client.sound.PacketDiagnostics.Channel;
import net.albiz.client.sound.SoundClassifier;
import net.albiz.client.sound.SoundEvidenceLog;
import net.albiz.client.sound.SoundEvidenceLog.Confidence;
import net.albiz.client.sound.SoundEvidenceLog.Entry;
import net.albiz.client.sound.SoundEvidenceLog.Hotspot;
import net.albiz.client.sound.SoundEvidenceLog.Source;
import net.albiz.client.sound.SoundKind;
import net.albiz.client.sound.Underground;
import net.albiz.client.sound.WorldSignalRules;
import net.albiz.client.util.LevelUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2604;
import net.minecraft.class_2623;
import net.minecraft.class_2673;
import net.minecraft.class_2680;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_7923;

/**
 * Passive audit of everything audible the server sends: positional sounds, entity sounds, world
 * events (block break / bone meal effects), block actions (pistons, container lids), kelp and
 * crop block transitions and underground entity sightings.
 *
 * <p>Packets are read on the network thread through {@link PacketReceiveNettyEvent}, which sees
 * every clientbound packet, and handed to the client thread through a bounded queue. Nothing is
 * requested from the server, nothing is forged and no server-side filter is circumvented: a sound
 * the server does not send (vanilla only sends within roughly 16 blocks per volume unit) cannot
 * be observed and is reported as not received, never as "no activity".
 */
public final class SoundDebugModule extends ClientModule {
   private static final String TITLE = "Sound Debug";
   private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());
   private static final int DRAIN_PER_TICK = 256;

   private final BooleanSetting soundPackets = this.addSetting("sound-packets", new BooleanSetting("Sound Packets", true));
   private final BooleanSetting entitySounds = this.addSetting("entity-sounds", new BooleanSetting("Entity Sound Packets", true));
   private final BooleanSetting worldEvents = this.addSetting("world-events", new BooleanSetting("World Events", true));
   private final BooleanSetting blockActions = this.addSetting("block-actions", new BooleanSetting("Block Actions", true));
   private final BooleanSetting blockChanges = this.addSetting("block-changes", new BooleanSetting("Kelp And Crop Changes", true));
   private final BooleanSetting entitySpawns = this.addSetting("entity-spawns", new BooleanSetting("Entity Spawn Packets", true));
   private final BooleanSetting sightings = this.addSetting("sightings", new BooleanSetting("Underground Entity Sightings", true));

   private final BooleanSetting plants = this.addSetting("kind-plants", new BooleanSetting("Kelp, Plants And Bone Meal", true));
   private final BooleanSetting machines = this.addSetting("kind-machines", new BooleanSetting("Pistons And Redstone", true));
   private final BooleanSetting containers = this.addSetting("kind-containers", new BooleanSetting("Containers And Doors", true));
   private final BooleanSetting workstations = this.addSetting("kind-workstations", new BooleanSetting("Furnaces And Workstations", true));
   private final BooleanSetting blocks = this.addSetting("kind-blocks", new BooleanSetting("Block Break And Place", true));
   private final BooleanSetting playerActions = this.addSetting("kind-players", new BooleanSetting("Player Actions", true));
   private final BooleanSetting hostileMobs = this.addSetting("kind-hostile", new BooleanSetting("Hostile Mobs", true));
   private final BooleanSetting otherMobs = this.addSetting("kind-mobs", new BooleanSetting("Villagers And Animals", true));
   private final BooleanSetting footsteps = this.addSetting("kind-steps", new BooleanSetting("Footsteps", false));
   private final BooleanSetting otherSounds = this.addSetting("kind-other", new BooleanSetting("Unclassified Sounds", false));

   private final BooleanSetting onlyUnderground = this.addSetting("only-underground", new BooleanSetting("Only Underground", true));
   private final NumberSetting ceiling = this.addSetting("y-ceiling", new NumberSetting("Y Ceiling", 20.0F, -256.0F, 320.0F).step(1.0F));
   private final BooleanSetting requireDepth = this.addSetting("require-depth", new BooleanSetting("Require Depth Below Surface", true));
   private final NumberSetting minDepth = this.addSetting("min-depth", new NumberSetting("Minimum Depth", 8.0F, 0.0F, 64.0F).step(1.0F));
   private final NumberSetting radius = this.addSetting("radius", new NumberSetting("Detection Radius", 0.0F, 0.0F, 256.0F).step(8.0F));
   private final NumberSetting ownRadius = this.addSetting("own-radius", new NumberSetting("Ignore Own Radius", 6.0F, 0.0F, 64.0F).step(1.0F));
   private final NumberSetting memory = this.addSetting("memory", new NumberSetting("Memory Seconds", 180.0F, 10.0F, 900.0F).step(10.0F));
   private final NumberSetting duplicateWindow = this.addSetting("duplicate-window", new NumberSetting("Duplicate Window Ms", 400.0F, 0.0F, 5000.0F).step(50.0F));
   private final NumberSetting threshold = this.addSetting("threshold", new NumberSetting("Hotspot Threshold", 12.0F, 1.0F, 200.0F).step(1.0F));

   private final NumberSetting alertCooldown = this.addSetting("alert-cooldown", new NumberSetting("Alert Cooldown Seconds", 5.0F, 0.0F, 60.0F).step(1.0F));
   private final BooleanSetting alertSound = this.addSetting("alert-sound", new BooleanSetting("Discovery Howl", true));
   private final BooleanSetting chatLog = this.addSetting("chat-log", new BooleanSetting("Log Every Event To Chat", false));

   private final BooleanSetting markers = this.addSetting("markers", new BooleanSetting("Event Markers", true));
   private final BooleanSetting hotspotBoxes = this.addSetting("hotspot-boxes", new BooleanSetting("Hotspot Chunks", true));
   private final BooleanSetting beams = this.addSetting("beams", new BooleanSetting("Beams", false));
   private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
   private final NumberSetting maxMarkers = this.addSetting("max-markers", new NumberSetting("Max Markers", 64.0F, 8.0F, 256.0F).step(8.0F));
   private final ColorSetting playerColor = this.addSetting("player-color", new ColorSetting("Player Evidence Color", 0xFFE3262E));
   private final ColorSetting machineColor = this.addSetting("machine-color", new ColorSetting("Machine Evidence Color", 0xFFFFB75C));
   private final ColorSetting mobColor = this.addSetting("mob-color", new ColorSetting("Mob Evidence Color", 0xFF8FA3B8));

   private final ActionSetting summary = this.addSetting("print-summary", new ActionSetting("Print Summary", this::printSummary));
   private final ActionSetting diagnosticsAction = this.addSetting("print-diagnostics", new ActionSetting("Print Packet Diagnostics", this::printDiagnostics));
   private final ActionSetting export = this.addSetting("export-log", new ActionSetting("Export Log", this::exportLog));
   private final ActionSetting clearAction = this.addSetting("clear-log", new ActionSetting("Clear Results", this::clearResults));

   private final boolean mobile = PlatformInfo.isMobile();
   private final ArrayBlockingQueue<Pending> incoming = new ArrayBlockingQueue<>(this.mobile ? 512 : 2048);
   private final SoundEvidenceLog log = new SoundEvidenceLog(this.mobile ? 256 : 1024, this.mobile ? 64 : 256);
   private final PacketDiagnostics diagnostics = new PacketDiagnostics();
   private final Map<Integer, Long> sightedEntities = new HashMap<>();
   private final List<Entry> markerScratch = new ArrayList<>();
   private volatile boolean accepting;
   private Object world;
   private int ticks;
   private long nextAlertMs;
   private long nextChatMs;

   /** Immutable snapshot of one packet, created on the network thread. */
   private static final class Pending {
      final Channel channel;
      final Source source;
      final SoundKind kind;
      final String detail;
      final double x;
      final double y;
      final double z;
      /** Entity whose position must be resolved on the client thread, or -1. */
      final int entityId;

      Pending(Channel channel, Source source, SoundKind kind, String detail, double x, double y, double z, int entityId) {
         this.channel = channel;
         this.source = source;
         this.kind = kind;
         this.detail = detail;
         this.x = x;
         this.y = y;
         this.z = z;
         this.entityId = entityId;
      }
   }

   public SoundDebugModule() {
      super(
         "sound-debug",
         TITLE,
         "Logs every sound, world event and block action the server sends and ranks underground chunks by evidence. Hints only, never proof of a base.",
         ModuleCategory.BASE_RAID
      );
      this.usesNotifications();
      this.ceiling.visibleWhen(this.onlyUnderground::get);
      this.requireDepth.visibleWhen(this.onlyUnderground::get);
      this.minDepth.visibleWhen(() -> this.onlyUnderground.get() && this.requireDepth.get());
   }

   @Override
   protected void onEnable() {
      this.resetState();
      this.world = class_310.method_1551().field_1687;
      this.diagnostics.listening(true);
      this.accepting = true;
   }

   @Override
   protected void onDisable() {
      this.accepting = false;
      this.diagnostics.listening(false);
      this.resetState();
      this.world = null;
   }

   private void resetState() {
      this.incoming.clear();
      this.log.clear();
      this.diagnostics.reset();
      this.sightedEntities.clear();
      this.markerScratch.clear();
      this.ticks = 0;
      this.nextAlertMs = 0L;
      this.nextChatMs = 0L;
   }

   // ---------------------------------------------------------------- network thread

   @EventHandler
   public void onIncoming(PacketReceiveNettyEvent event) {
      if (!this.accepting) {
         return;
      }

      class_2596<?> packet = event.packet;
      Channel channel = null;

      try {
         Pending pending = null;
         if (packet instanceof class_2767 sound) {
            channel = Channel.SOUND;
            this.diagnostics.received(channel);
            if (this.soundPackets.get()) {
               String id = String.valueOf(sound.method_11894().comp_349().comp_3319());
               pending = new Pending(
                  channel, Source.SOUND_PACKET, SoundClassifier.classify(id), id, sound.method_11890(), sound.method_11889(), sound.method_11893(), -1
               );
            }
         } else if (packet instanceof class_2765 sound) {
            channel = Channel.ENTITY_SOUND;
            this.diagnostics.received(channel);
            if (this.entitySounds.get()) {
               String id = String.valueOf(sound.method_11882().comp_349().comp_3319());
               pending = new Pending(channel, Source.ENTITY_SOUND_PACKET, SoundClassifier.classify(id), id, 0.0, 0.0, 0.0, sound.method_11883());
            }
         } else if (packet instanceof class_2673 worldEvent) {
            channel = Channel.WORLD_EVENT;
            this.diagnostics.received(channel);
            if (this.worldEvents.get() && !worldEvent.method_11533()) {
               WorldSignalRules.Signal signal = WorldSignalRules.worldEvent(worldEvent.method_11532());
               if (signal != null) {
                  class_2338 pos = worldEvent.method_11531();
                  pending = new Pending(
                     channel,
                     Source.WORLD_EVENT,
                     signal.kind(),
                     signal.label(),
                     pos.method_10263() + 0.5,
                     pos.method_10264() + 0.5,
                     pos.method_10260() + 0.5,
                     -1
                  );
               }
            }
         } else if (packet instanceof class_2623 action) {
            channel = Channel.BLOCK_ACTION;
            this.diagnostics.received(channel);
            if (this.blockActions.get()) {
               String block = class_7923.field_41175.method_10221(action.method_11295()).method_12832();
               WorldSignalRules.Signal signal = WorldSignalRules.blockAction(block);
               if (signal != null) {
                  class_2338 pos = action.method_11298();
                  pending = new Pending(
                     channel,
                     Source.BLOCK_ACTION,
                     signal.kind(),
                     signal.label(),
                     pos.method_10263() + 0.5,
                     pos.method_10264() + 0.5,
                     pos.method_10260() + 0.5,
                     -1
                  );
               }
            }
         } else if (packet instanceof class_2604 spawn) {
            channel = Channel.ENTITY_SPAWN;
            this.diagnostics.received(channel);
            if (this.entitySpawns.get()) {
               String type = class_7923.field_41177.method_10221(spawn.method_11169()).method_12832();
               SoundKind kind = SoundClassifier.entityKind(type);
               if (kind != null) {
                  pending = new Pending(
                     channel, Source.ENTITY_SEEN, kind, "Entity spawned: " + type, spawn.method_11175(), spawn.method_11174(), spawn.method_11176(), -1
                  );
               }
            }
         }

         if (channel == null) {
            return;
         }

         this.diagnostics.decoded(channel);
         if (pending != null && !this.incoming.offer(pending)) {
            // The client thread is behind; say so instead of silently losing the packet.
            this.diagnostics.incomplete(channel);
         }
      } catch (RuntimeException | LinkageError error) {
         if (channel != null) {
            this.diagnostics.incomplete(channel);
         }
      }
   }

   // ---------------------------------------------------------------- client thread

   @EventHandler
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      if (this.world != mc.field_1687) {
         this.resetState();
         this.world = mc.field_1687;
      }

      class_638 level = mc.field_1687;
      if (level == null || mc.field_1724 == null) {
         this.incoming.clear();
         return;
      }

      long now = System.currentTimeMillis();

      for (int i = 0; i < DRAIN_PER_TICK; i++) {
         Pending pending = this.incoming.poll();
         if (pending == null) {
            break;
         }

         double x = pending.x;
         double y = pending.y;
         double z = pending.z;
         if (pending.entityId >= 0) {
            class_1297 entity = level.method_8469(pending.entityId);
            if (entity == null) {
               this.diagnostics.incomplete(pending.channel);
               continue;
            }

            if (entity == mc.field_1724) {
               this.diagnostics.available(pending.channel);
               continue;
            }

            x = entity.method_23317();
            y = entity.method_23318();
            z = entity.method_23321();
         }

         this.diagnostics.available(pending.channel);
         this.observe(mc, level, now, pending.source, pending.kind, pending.detail, x, y, z);
      }

      this.ticks++;
      if (this.ticks % 20 == 0) {
         this.log.expire(now - (long)(this.memory.get() * 1000.0F));
         if (this.sightings.get()) {
            this.scanEntities(mc, level, now);
         }
      }
   }

   @EventHandler
   public void onBlockUpdate(BlockUpdateEvent event) {
      if (!this.blockChanges.get() || event.oldState == event.newState) {
         return;
      }

      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level == null || mc.field_1724 == null || this.world != level) {
         return;
      }

      String oldPath = blockPath(event.oldState);
      String newPath = blockPath(event.newState);
      if (oldPath.equals(newPath)) {
         return;
      }

      WorldSignalRules.Signal signal = WorldSignalRules.transition(oldPath, newPath);
      if (signal != null) {
         this.observe(
            mc,
            level,
            System.currentTimeMillis(),
            Source.BLOCK_CHANGE,
            signal.kind(),
            signal.label(),
            event.pos.method_10263() + 0.5,
            event.pos.method_10264() + 0.5,
            event.pos.method_10260() + 0.5
         );
      }
   }

   private static String blockPath(class_2680 state) {
      return class_7923.field_41175.method_10221(state.method_26204()).method_12832();
   }

   /** Sees entities the server already sent; an entity the server hides is simply not in the list. */
   private void scanEntities(class_310 mc, class_638 level, long now) {
      long relog = 30000L;
      if (this.sightedEntities.size() > 2048) {
         this.sightedEntities.clear();
      } else {
         Iterator<Map.Entry<Integer, Long>> it = this.sightedEntities.entrySet().iterator();
         while (it.hasNext()) {
            if (now - it.next().getValue() > relog) {
               it.remove();
            }
         }
      }

      for (class_1297 entity : level.method_18112()) {
         if (entity == mc.field_1724) {
            continue;
         }

         Integer id = entity.method_5628();
         if (this.sightedEntities.containsKey(id)) {
            continue;
         }

         String type = class_7923.field_41177.method_10221(entity.method_5864()).method_12832();
         SoundKind kind = SoundClassifier.entityKind(type);
         if (kind == null) {
            continue;
         }

         this.sightedEntities.put(id, now);
         this.observe(
            mc, level, now, Source.ENTITY_SEEN, kind, "Entity seen: " + type, entity.method_23317(), entity.method_23318(), entity.method_23321()
         );
      }
   }

   private boolean kindEnabled(SoundKind kind) {
      return switch (kind) {
         case KELP, PLANT, BONE_MEAL -> this.plants.get();
         case PISTON, REDSTONE, MINECART, EXPLOSION, PORTAL -> this.machines.get();
         case CONTAINER, DOOR -> this.containers.get();
         case WORKSTATION, SPAWNER -> this.workstations.get();
         case BLOCK_BREAK, BLOCK_PLACE, BLOCK_HIT -> this.blocks.get();
         case PLAYER, ITEM_PICKUP -> this.playerActions.get();
         case HOSTILE_MOB -> this.hostileMobs.get();
         case VILLAGER, PASSIVE_MOB -> this.otherMobs.get();
         case BLOCK_STEP -> this.footsteps.get();
         case OTHER -> this.otherSounds.get();
         case LIQUID, AMBIENT -> false;
      };
   }

   private void observe(class_310 mc, class_638 level, long now, Source source, SoundKind kind, String detail, double x, double y, double z) {
      if (!this.kindEnabled(kind)) {
         return;
      }

      double distanceSq = mc.field_1724.method_5707(new class_243(x, y, z));
      double own = this.ownRadius.get();
      if (distanceSq < own * own) {
         return;
      }

      double limit = this.radius.get();
      if (limit > 0.0 && distanceSq > limit * limit) {
         return;
      }

      if (this.onlyUnderground.get()) {
         int blockX = (int)Math.floor(x);
         int blockZ = (int)Math.floor(z);
         int surface = Underground.NO_SURFACE;
         if (LevelUtil.isLoaded(level, blockX >> 4, blockZ >> 4)) {
            surface = level.method_8624(class_2903.field_13197, blockX, blockZ);
         }

         if (!Underground.accept(y, this.ceiling.getInt(), level.method_31607(), surface, this.minDepth.getInt(), this.requireDepth.get())) {
            return;
         }
      }

      Hotspot spot = this.log.add(now, source, kind, detail, x, y, z, kind.weight(), (long)this.duplicateWindow.get());
      if (spot == null) {
         return;
      }

      if (kind.origin() == SoundKind.Origin.PLAYER || kind.origin() == SoundKind.Origin.AUTOMATION_OR_PLAYER) {
         SusChunkModule evidence = net.albiz.client.module.ModuleRegistry.get(SusChunkModule.class);
         if (evidence != null) {
            evidence.reportSound((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4, (int)Math.floor(y));
         }
      }

      if (this.chatLog.get() && now >= this.nextChatMs) {
         this.nextChatMs = now + 250L;
         Chat.info(TITLE, AlbizI18n.display(kind.label()) + " · " + AlbizI18n.display(detail) + " · " + coords(x, y, z));
      }

      this.maybeAlert(spot, now);
   }

   private void maybeAlert(Hotspot spot, long now) {
      if (spot.alerted || spot.score < this.threshold.get()) {
         return;
      }

      spot.alerted = true;
      if (this.alertSound.get()) {
         net.albiz.client.module.impl.client.SoundFeedbackModule.abyzAlert();
      }

      if (this.notificationsOn() && now >= this.nextAlertMs) {
         this.nextAlertMs = now + (long)(this.alertCooldown.get() * 1000.0F);
         Notifications.push(TITLE, this.describe(spot), spot.confidence() == Confidence.HIGH ? Notifications.Type.WARNING : Notifications.Type.INFO);
      }
   }

   private String describe(Hotspot spot) {
      return AlbizI18n.display(spot.confidence().label() + " confidence")
         + " · "
         + coords(spot.chunkX * 16 + 8, spot.averageY(), spot.chunkZ * 16 + 8)
         + " · "
         + translateEvidence(spot.explain(4))
         + " · "
         + AlbizI18n.display("Hint only, not proof");
   }

   /** {@code "Piston x4, Kelp x2"} -> each label translated, counts untouched. */
   private static String translateEvidence(String evidence) {
      StringBuilder out = new StringBuilder();
      for (String part : evidence.split(", ")) {
         if (out.length() > 0) {
            out.append(", ");
         }

         int split = part.lastIndexOf(" x");
         out.append(split > 0 ? AlbizI18n.display(part.substring(0, split)) + part.substring(split) : part);
      }

      return out.toString();
   }

   private static String coords(double x, double y, double z) {
      return (int)Math.floor(x) + ", " + (int)Math.floor(y) + ", " + (int)Math.floor(z);
   }

   // ---------------------------------------------------------------- actions

   private void printSummary() {
      if (!this.isEnabled()) {
         Chat.info(TITLE, AlbizI18n.display("Enable the module first."));
         return;
      }

      List<Hotspot> ranked = new ArrayList<>();
      for (Hotspot spot : this.log.hotspots()) {
         ranked.add(spot);
      }

      ranked.sort((a, b) -> Double.compare(b.score, a.score));
      Chat.info(
         TITLE,
         AlbizI18n.display("Events") + ": " + this.log.historySize() + " · " + AlbizI18n.display("Merged duplicates") + ": " + this.log.merged() + " · "
            + AlbizI18n.display("Hotspots") + ": " + ranked.size()
      );
      for (int i = 0; i < ranked.size() && i < 8; i++) {
         Hotspot spot = ranked.get(i);
         Chat.info(TITLE, "#" + (i + 1) + " " + AlbizI18n.display("Score") + " " + (int)spot.score + " · " + this.describe(spot));
      }

      if (ranked.isEmpty()) {
         Chat.info(TITLE, AlbizI18n.display("No evidence received yet. This does not prove that nothing happened."));
      }
   }

   private void printDiagnostics() {
      for (Channel channel : Channel.values()) {
         Chat.info(TITLE, this.diagnostics.line(channel));
      }

      Chat.info(TITLE, AlbizI18n.display("Queued for the client thread") + ": " + this.incoming.size());
   }

   private void clearResults() {
      this.incoming.clear();
      this.log.clear();
      this.sightedEntities.clear();
      this.nextAlertMs = 0L;
      Chat.info(TITLE, AlbizI18n.display("Results cleared."));
   }

   private void exportLog() {
      StringBuilder out = new StringBuilder(4096);
      out.append("# Albiz Sound Debug export. Rows are packets the server sent; absence of rows is not evidence of absence.\n");
      for (Channel channel : Channel.values()) {
         out.append("# ").append(this.diagnostics.line(channel)).append('\n');
      }

      out.append("section\ttime\tsource\tkind\torigin\tdetail\tx\ty\tz\trepeats\n");
      for (Entry entry : this.log.history()) {
         out.append("event\t")
            .append(Instant.ofEpochMilli(entry.timeMs))
            .append('\t')
            .append(entry.source)
            .append('\t')
            .append(entry.kind)
            .append('\t')
            .append(entry.kind.origin())
            .append('\t')
            .append(entry.detail.replace('\t', ' '))
            .append('\t')
            .append((int)Math.floor(entry.x))
            .append('\t')
            .append((int)Math.floor(entry.y))
            .append('\t')
            .append((int)Math.floor(entry.z))
            .append('\t')
            .append(entry.repeats)
            .append('\n');
      }

      out.append("section\tchunkX\tchunkZ\tscore\tconfidence\tevents\tminY\tmaxY\tevidence\n");
      for (Hotspot spot : this.log.hotspots()) {
         out.append("hotspot\t")
            .append(spot.chunkX)
            .append('\t')
            .append(spot.chunkZ)
            .append('\t')
            .append((int)spot.score)
            .append('\t')
            .append(spot.confidence())
            .append('\t')
            .append(spot.events)
            .append('\t')
            .append((int)Math.floor(spot.minY))
            .append('\t')
            .append((int)Math.floor(spot.maxY))
            .append('\t')
            .append(spot.explain(16))
            .append('\n');
      }

      String text = out.toString();
      Path file = ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("diagnostics").resolve("sound-debug-" + STAMP.format(Instant.now()) + ".tsv");
      class_310 mc = class_310.method_1551();
      CompletableFuture.runAsync(() -> {
         String message;
         try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, text, StandardCharsets.UTF_8);
            message = AlbizI18n.display("Log exported") + ": " + file.getFileName();
         } catch (java.io.IOException | RuntimeException error) {
            message = AlbizI18n.display("Log export failed") + ": " + error.getClass().getSimpleName();
         }

         String result = message;
         mc.execute(() -> Chat.info(TITLE, result));
      });
   }

   // ---------------------------------------------------------------- rendering

   private Color colorFor(SoundKind kind) {
      return switch (kind.origin()) {
         case PLAYER -> Color.fromPacked(this.playerColor.argb());
         case AUTOMATION_OR_PLAYER -> Color.fromPacked(this.machineColor.argb());
         default -> Color.fromPacked(this.mobColor.argb());
      };
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      class_310 mc = class_310.method_1551();
      if (this.world == null || this.world != mc.field_1687 || mc.field_1724 == null) {
         return;
      }

      Render3D render = new Render3D(event);
      if (this.markers.get()) {
         // Newest events matter most: collect once, draw from the tail.
         this.markerScratch.clear();
         for (Entry entry : this.log.history()) {
            this.markerScratch.add(entry);
         }

         int budget = Math.min(this.maxMarkers.getInt(), this.mobile ? 48 : 256);
         for (int i = this.markerScratch.size() - 1; i >= 0 && budget > 0; i--, budget--) {
            Entry entry = this.markerScratch.get(i);
            Color color = this.colorFor(entry.kind);
            render.box(entry.x - 0.3, entry.y - 0.3, entry.z - 0.3, entry.x + 0.3, entry.y + 0.3, entry.z + 0.3, color.withAlpha(36), color, ShapeMode.Both);
         }

         this.markerScratch.clear();
      }

      if (this.hotspotBoxes.get() || this.beams.get() || this.tracers.get()) {
         int drawn = 0;
         int limit = this.mobile ? 16 : 64;
         float minimum = this.threshold.get();
         for (Hotspot spot : this.log.hotspots()) {
            if (spot.score < minimum) {
               continue;
            }

            if (++drawn > limit) {
               break;
            }

            Confidence confidence = spot.confidence();
            Color color = confidence == Confidence.HIGH
               ? Color.fromPacked(this.playerColor.argb())
               : confidence == Confidence.MEDIUM ? Color.fromPacked(this.machineColor.argb()) : Color.fromPacked(this.mobColor.argb());
            double x = spot.chunkX * 16;
            double z = spot.chunkZ * 16;
            double y = spot.averageY();
            if (this.hotspotBoxes.get()) {
               render.box(x, spot.minY - 0.5, z, x + 16.0, spot.maxY + 0.5, z + 16.0, color.withAlpha(18), color, ShapeMode.Both);
            }

            if (this.beams.get()) {
               render.beam(x + 8.0, z + 8.0, y, y + 48.0, 0.2, color, 0.6);
            }

            if (this.tracers.get()) {
               render.tracer(x + 8.0, y, z + 8.0, color);
            }
         }
      }
   }
}
