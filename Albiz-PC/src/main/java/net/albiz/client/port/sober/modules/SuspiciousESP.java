// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.modules;

import net.albiz.client.port.sober.module.Category;
import net.albiz.client.port.sober.module.Module;
import net.albiz.client.port.sober.render.Color;
import net.albiz.client.port.sober.render.Render3D;
import net.albiz.client.port.sober.render.ShapeMode;
import net.albiz.client.port.sober.setting.BoolSetting;
import net.albiz.client.port.sober.setting.ColorSetting;
import net.albiz.client.port.sober.setting.EnumSetting;
import net.albiz.client.port.sober.setting.IntSetting;
import net.albiz.client.port.sober.setting.Setting;
import net.albiz.client.port.sober.util.ChunkScanner;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1501;
import net.minecraft.class_1604;
import net.minecraft.class_1641;
import net.minecraft.class_1646;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2393;
import net.minecraft.class_2541;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3989;
import net.minecraft.class_4776;
import net.minecraft.class_4777;
import net.minecraft.class_4950;
import net.minecraft.class_4951;

public class SuspiciousESP
extends Module {
    private final Setting<Boolean> wanderingTraders;
    private final Setting<Boolean> villagers;
    private final Setting<Boolean> llamas;
    private final Setting<Boolean> pillagers;
    private final Setting<Boolean> deepslate;
    private final Setting<Boolean> rotatedDeepslate;
    private final Setting<Integer> deepslateMinY;
    private final Setting<Integer> deepslateMaxY;
    private final Setting<Boolean> vines;
    private final Setting<Boolean> amethyst;
    private final Setting<Boolean> kelp;
    private final Setting<Integer> kelpThreshold;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Boolean> tracers;
    private final Setting<Color> traderColor;
    private final Setting<Color> villagerColor;
    private final Setting<Color> llamaColor;
    private final Setting<Color> deepslateColor;
    private final Setting<Color> rotatedColor;
    private final Setting<Color> vineColor;
    private final Setting<Color> amethystColor;
    private final Setting<Color> kelpColor;
    private final Setting<Boolean> chunkVerdict;
    private final Setting<Boolean> verdictOnlyStrong;
    private final Setting<Color> suspiciousColor;
    private final Setting<Color> strongColor;
    private final int[] kindCounts = new int[4];
    private final Map<class_1923, Map<class_2338, BlockType>> blocks;
    /** Chunk to {lowest Y, highest Y, count} of the fully grown kelp that was actually received. */
    private final Map<class_1923, int[]> kelpChunks;
    private int ticks;
    private final ChunkScanner scanner;

    public SuspiciousESP() {
        super("Suspicious ESP", "Highlights suspicious entities and blocks.", Category.BASE_FINDING);
        this.group("Entities");
        this.wanderingTraders = this.add(new BoolSetting("Wandering Traders", "Highlights wandering traders.", true));
        this.villagers = this.add(new BoolSetting("Villagers", "Villagers and zombie villagers.", true));
        this.llamas = this.add(new BoolSetting("Llamas", "Highlights llamas.", true));
        this.pillagers = this.add(new BoolSetting("Pillagers", "Highlights pillagers.", true));
        this.group("Blocks");
        this.deepslate = this.add(new BoolSetting("Deepslate", "Deepslate inside the Y range (doesn't generate above Y 8).", true)).onChanged(v -> this.rescan());
        this.rotatedDeepslate = this.add(new BoolSetting("Rotated Deepslate", "Sideways placed deepslate inside the Y range.", true)).onChanged(v -> this.rescan());
        this.deepslateMinY = this.add(new IntSetting("Deepslate Min Y", "Lower end of the deepslate Y range.", 16, -64, 320)).visible(() -> this.deepslate.get() != false || this.rotatedDeepslate.get() != false).onChanged(v -> this.rescan());
        this.deepslateMaxY = this.add(new IntSetting("Deepslate Max Y", "Upper end of the deepslate Y range.", 128, -64, 320)).visible(() -> this.deepslate.get() != false || this.rotatedDeepslate.get() != false).onChanged(v -> this.rescan());
        this.vines = this.add(new BoolSetting("Vines", "Vines, twisting and weeping vines.", true)).onChanged(v -> this.rescan());
        this.amethyst = this.add(new BoolSetting("Amethyst Clusters", "Highlights amethyst clusters.", true)).onChanged(v -> this.rescan());
        this.kelp = this.add(new BoolSetting("Kelp Chunks", "Chunks with lots of fully grown kelp.", true)).onChanged(v -> this.rescan());
        this.kelpThreshold = this.add(new IntSetting("Kelp Threshold", "Fully grown kelp needed to mark a chunk.", 10, 1, 50)).visible(this.kelp::get).onChanged(v -> this.rescan());
        this.group("Render");
        this.shapeMode = this.add(new EnumSetting<ShapeMode>("Shape Mode", "How boxes are drawn.", ShapeMode.Both));
        this.tracers = this.add(new BoolSetting("Tracers", "Draws lines to everything found.", false));
        this.traderColor = this.add(new ColorSetting("Trader Color", "Wandering traders and pillagers.", new Color(255, 0, 0, 150)));
        this.villagerColor = this.add(new ColorSetting("Villager Color", "Villagers and zombie villagers.", new Color(0, 255, 0, 150)));
        this.llamaColor = this.add(new ColorSetting("Llama Color", "Llamas.", new Color(255, 165, 0, 150)));
        this.deepslateColor = this.add(new ColorSetting("Deepslate Color", "Deepslate.", new Color(0, 200, 255, 150)));
        this.rotatedColor = this.add(new ColorSetting("Rotated Color", "Rotated deepslate.", new Color(255, 0, 255, 150)));
        this.vineColor = this.add(new ColorSetting("Vine Color", "Vines.", new Color(0, 255, 0, 150)));
        this.amethystColor = this.add(new ColorSetting("Amethyst Color", "Amethyst clusters.", new Color(180, 0, 255, 150)));
        this.kelpColor = this.add(new ColorSetting("Kelp Color", "Kelp chunks.", new Color(0, 255, 255, 150)));
        this.group("Chunk Verdict");
        this.chunkVerdict = this.add(new BoolSetting("Chunk Verdict", "Outlines chunks by how suspicious their blocks are together.", false));
        this.verdictOnlyStrong = this.add(new BoolSetting("Only Strong Verdicts", "Outlines only chunks with several kinds of player-made evidence.", false)).visible(this.chunkVerdict::get);
        this.suspiciousColor = this.add(new ColorSetting("Suspicious Chunk Color", "Chunks with a cluster or one player-made sign.", new Color(255, 200, 0, 200))).visible(this.chunkVerdict::get);
        this.strongColor = this.add(new ColorSetting("Strong Chunk Color", "Chunks with several kinds of evidence.", new Color(255, 40, 40, 220))).visible(this.chunkVerdict::get);
        this.blocks = new ConcurrentHashMap<class_1923, Map<class_2338, BlockType>>();
        this.kelpChunks = new ConcurrentHashMap<class_1923, int[]>();
        this.scanner = new ChunkScanner("SuspiciousESP", this::scanChunk);
    }

    @Override
    public void onEnable() {
        this.blocks.clear();
        this.kelpChunks.clear();
        this.scanner.start();
        if (SuspiciousESP.mc.field_1687 != null) {
            this.scanner.submitLoaded();
        }
    }

    @Override
    public void onDisable() {
        this.scanner.stop();
        this.blocks.clear();
        this.kelpChunks.clear();
    }

    private void rescan() {
        if (this.isActive()) {
            this.onEnable();
        }
    }

    @Override
    public void onGameLeft() {
        this.blocks.clear();
        this.kelpChunks.clear();
    }

    @Override
    public void onChunkLoad(class_2818 chunk) {
        this.scanner.submit(chunk);
    }

    @Override
    public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
        BlockType type = this.classify(newState, pos.method_10264());
        class_1923 chunk = new class_1923(pos);
        if (type != null) {
            this.blocks.computeIfAbsent(chunk, k -> new ConcurrentHashMap()).put(pos.method_10062(), type);
        } else {
            Map<class_2338, BlockType> map = this.blocks.get(chunk);
            if (map != null) {
                map.remove(pos);
            }
        }
    }

    @Override
    public void onTick() {
        if (++this.ticks % 100 != 0 || SuspiciousESP.mc.field_1724 == null || SuspiciousESP.mc.field_1687 == null) {
            return;
        }
        class_1923 here = SuspiciousESP.mc.field_1724.method_31476();
        int keep = SuspiciousESP.mc.field_1690.method_38521() + 2;
        this.blocks.keySet().removeIf(p -> (Math.abs(p.field_9181 - here.field_9181) > keep || Math.abs(p.field_9180 - here.field_9180) > keep) || !SuspiciousESP.mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180));
        this.kelpChunks.keySet().removeIf(p -> (Math.abs(p.field_9181 - here.field_9181) > keep || Math.abs(p.field_9180 - here.field_9180) > keep) || !SuspiciousESP.mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180));
    }

    private void scanChunk(class_2818 chunk) {
        if (SuspiciousESP.mc.field_1687 == null) {
            return;
        }
        class_1923 chunkPos = chunk.method_12004();
        class_2826[] sections = chunk.method_12006();
        int bottomSection = chunk.method_32891();
        HashMap<class_2338, BlockType> found = new HashMap<class_2338, BlockType>();
        int grownKelp = 0;
        int kelpMinY = Integer.MAX_VALUE;
        int kelpMaxY = Integer.MIN_VALUE;
        for (int i = 0; i < sections.length; ++i) {
            class_2826 section = sections[i];
            if (section == null || section.method_38292()) continue;
            int sectionY = bottomSection + i << 4;
            if (!section.method_19523(s -> this.isRelevant(s.method_26204()))) continue;
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        int blockY;
                        class_2680 state = section.method_12254(x, y, z);
                        BlockType type = this.classify(state, blockY = sectionY + y);
                        if (type != null) {
                            if(found.size()<(net.albiz.client.platform.PlatformInfo.isMobile()?256:2048))found.put(new class_2338(chunkPos.method_8326() + x, blockY, chunkPos.method_8328() + z), type);
                        }
                        if (!this.kelp.get().booleanValue() || !state.method_27852(class_2246.field_9993) || (Integer)state.method_11654((class_2769)class_2393.field_22509) != 25) continue;
                        ++grownKelp;
                        kelpMinY = Math.min(kelpMinY, blockY);
                        kelpMaxY = Math.max(kelpMaxY, blockY);
                    }
                }
            }
        }
        if (found.isEmpty()) {
            this.blocks.remove(chunkPos);
        } else {
            this.blocks.put(chunkPos, new ConcurrentHashMap(found));
        }
        if (this.kelp.get().booleanValue() && grownKelp >= this.kelpThreshold.get()) {
            this.kelpChunks.put(chunkPos, new int[]{kelpMinY, kelpMaxY, grownKelp});
        } else {
            this.kelpChunks.remove(chunkPos);
        }
    }

    private boolean isRelevant(class_2248 block) {
        return block == class_2246.field_9993 || block == class_2246.field_27161 || SuspiciousESP.isVine(block) || SuspiciousESP.isDeepslateType(block);
    }

    private BlockType classify(class_2680 state, int y) {
        boolean inRange;
        class_2248 block = state.method_26204();
        if (this.amethyst.get().booleanValue() && block == class_2246.field_27161) {
            return BlockType.AMETHYST;
        }
        if (this.vines.get().booleanValue() && SuspiciousESP.isVine(block)) {
            return BlockType.VINE;
        }
        boolean bl = inRange = y >= this.deepslateMinY.get() && y <= this.deepslateMaxY.get();
        if (!inRange) {
            return null;
        }
        if (this.rotatedDeepslate.get().booleanValue() && SuspiciousESP.isDeepslateType(block) && state.method_28498((class_2769)class_2741.field_12496) && state.method_11654((class_2769)class_2741.field_12496) != class_2350.class_2351.field_11052) {
            return BlockType.ROTATED_DEEPSLATE;
        }
        if (this.deepslate.get().booleanValue() && block == class_2246.field_28888) {
            return BlockType.DEEPSLATE;
        }
        return null;
    }

    private static boolean isDeepslateType(class_2248 block) {
        return block == class_2246.field_28888 || block == class_2246.field_28892 || block == class_2246.field_28900 || block == class_2246.field_28896 || block == class_2246.field_28904;
    }

    private static boolean isVine(class_2248 block) {
        return block instanceof class_2541 || block instanceof class_4777 || block instanceof class_4950 || block instanceof class_4776 || block instanceof class_4951;
    }

    @Override
    public void onRender(Render3D renderer) {
        if (SuspiciousESP.mc.field_1687 == null || SuspiciousESP.mc.field_1724 == null) {
            return;
        }
        int playerChunkX = SuspiciousESP.mc.field_1724.method_31477() >> 4;
        int playerChunkZ = SuspiciousESP.mc.field_1724.method_31479() >> 4;
        int viewDistance = SuspiciousESP.mc.field_1690.method_38521();
        for (Map.Entry<class_1923, Map<class_2338, BlockType>> chunk : this.blocks.entrySet()) {
            class_1923 chunkPos = chunk.getKey();
            if (Math.abs(chunkPos.field_9181 - playerChunkX) > viewDistance || Math.abs(chunkPos.field_9180 - playerChunkZ) > viewDistance) continue;
            boolean verdicts = this.chunkVerdict.get().booleanValue();
            int lowY = Integer.MAX_VALUE;
            int highY = Integer.MIN_VALUE;
            java.util.Arrays.fill(this.kindCounts, 0);
            for (Map.Entry<class_2338, BlockType> entry : chunk.getValue().entrySet()) {
                class_2338 pos = entry.getKey();
                if (verdicts) {
                    ++this.kindCounts[entry.getValue().ordinal()];
                    lowY = Math.min(lowY, pos.method_10264());
                    highY = Math.max(highY, pos.method_10264());
                }
                Color color = this.blockColor(entry.getValue());
                renderer.box(pos, color, color, this.shapeMode.get());
                if (!this.tracers.get().booleanValue()) continue;
                renderer.tracer((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5, color);
            }
            if (verdicts && lowY <= highY) {
                int[] kelpRange = this.kelp.get().booleanValue() ? this.kelpChunks.get(chunkPos) : null;
                net.albiz.client.suschunk.SuspicionVerdict verdict = net.albiz.client.suschunk.SuspicionVerdict.of(this.kindCounts[BlockType.DEEPSLATE.ordinal()], this.kindCounts[BlockType.ROTATED_DEEPSLATE.ordinal()], this.kindCounts[BlockType.VINE.ordinal()], this.kindCounts[BlockType.AMETHYST.ordinal()], kelpRange != null);
                boolean strong = verdict == net.albiz.client.suschunk.SuspicionVerdict.STRONG;
                if (strong || verdict == net.albiz.client.suschunk.SuspicionVerdict.SUSPICIOUS && !this.verdictOnlyStrong.get().booleanValue()) {
                    if (kelpRange != null) {
                        lowY = Math.min(lowY, kelpRange[0]);
                        highY = Math.max(highY, kelpRange[1]);
                    }
                    Color outline = strong ? this.strongColor.get() : this.suspiciousColor.get();
                    renderer.box(chunkPos.method_8326(), (double)lowY, chunkPos.method_8328(), chunkPos.method_8326() + 16, (double)highY + 1.0, chunkPos.method_8328() + 16, outline.scaleAlpha(0.12), outline, ShapeMode.Lines);
                }
            }
        }
        if (this.kelp.get().booleanValue()) {
            for (Map.Entry<class_1923, int[]> kelpEntry : this.kelpChunks.entrySet()) {
                class_1923 pos = kelpEntry.getKey();
                if (Math.abs(pos.field_9181 - playerChunkX) > viewDistance || Math.abs(pos.field_9180 - playerChunkZ) > viewDistance) continue;
                // Drawn where the kelp really is (works below Y 0) instead of at a fixed sea-level Y 63.
                int[] range = kelpEntry.getValue();
                renderer.box(pos.method_8326(), (double)range[0], pos.method_8328(), pos.method_8326() + 16, (double)range[1] + 1.0, pos.method_8328() + 16, this.kelpColor.get().scaleAlpha(0.25), this.kelpColor.get(), this.shapeMode.get());
                if (!this.tracers.get().booleanValue()) continue;
                renderer.tracer((double)pos.method_8326() + 8.0, (double)(range[0] + range[1]) * 0.5, (double)pos.method_8328() + 8.0, this.kelpColor.get());
            }
        }
        for (class_1297 entity : SuspiciousESP.mc.field_1687.method_18112()) {
            Color color = this.entityColor(entity);
            if (color == null) continue;
            class_238 box = entity.method_5829();
            renderer.box(box, color, color, this.shapeMode.get());
            if (!this.tracers.get().booleanValue()) continue;
            renderer.tracer(box.method_1005().field_1352, box.method_1005().field_1351, box.method_1005().field_1350, color);
        }
    }

    private Color entityColor(class_1297 entity) {
        if (this.wanderingTraders.get().booleanValue() && entity instanceof class_3989) {
            return this.traderColor.get();
        }
        if (this.pillagers.get().booleanValue() && entity instanceof class_1604) {
            return this.traderColor.get();
        }
        if (this.villagers.get().booleanValue() && (entity instanceof class_1646 || entity instanceof class_1641)) {
            return this.villagerColor.get();
        }
        if (this.llamas.get().booleanValue() && entity instanceof class_1501) {
            return this.llamaColor.get();
        }
        return null;
    }

    private Color blockColor(BlockType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.deepslateColor.get();
            case 1 -> this.rotatedColor.get();
            case 2 -> this.vineColor.get();
            case 3 -> this.amethystColor.get();
        };
    }

    private static enum BlockType {
        DEEPSLATE,
        ROTATED_DEEPSLATE,
        VINE,
        AMETHYST;

    }
}

