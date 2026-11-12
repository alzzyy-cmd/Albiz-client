// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render.knife;

import java.util.Locale;
import net.minecraft.class_1799;
import net.minecraft.class_7923;

public final class KnifeMaterial {
    static final int BLADE = 0;
    static final int BLADE_DARK = 1;
    static final int EDGE = 2;
    static final int HANDLE = 3;
    static final int HANDLE_DARK = 4;
    static final int METAL = 5;
    static final int ACCENT = 6;
    static final int WRAP = 7;
    static final int GOLD = 8;
    private static final KnifeMaterial DIAMOND = new KnifeMaterial(0x6FFFF0, 4187087, 1941139, 13828087, 1780531, 989213, 12109772, 4187087, 1391168, 15253850);
    private static final KnifeMaterial NETHERITE = new KnifeMaterial(16742973, 6445663, 3813942, 11773356, 1709592, 920333, 7234665, 13715758, 3806228, 13213770);
    private static final KnifeMaterial IRON = new KnifeMaterial(0xFFFFFF, 0xD9D9D9, 0x9B9B9B, 0xFFFFFF, 4862754, 2825490, 0x8E8E8E, 0xC8C8C8, 0x2B2B2B, 14267466);
    private static final KnifeMaterial GOLDEN = new KnifeMaterial(16769126, 16242762, 13015574, 16775096, 5905950, 3674128, 15188812, 16770688, 7216927, 15912795);
    private static final KnifeMaterial COPPER = new KnifeMaterial(0xFFAA77, 15043163, 11096108, 16763302, 3096378, 1844771, 6274975, 6274975, 2375738, 14263130);
    private static final KnifeMaterial STONE = new KnifeMaterial(0xDDDDDD, 0x9C9C9C, 0x666666, 0xC8C8C8, 4862754, 2825490, 0x7A7A7A, 0xA8A8A8, 3811868, 12558416);
    private static final KnifeMaterial WOOD = new KnifeMaterial(15782560, 12290902, 8279854, 14529402, 5914146, 3679763, 9071172, 13081442, 4861976, 13213770);
    final int[] palette;
    final int glow;
    public static final String[] NAMES = new String[]{"Auto", "Diamond", "Netherite", "Iron", "Golden", "Copper", "Stone", "Wooden"};

    private KnifeMaterial(int glow, int ... palette) {
        this.glow = glow;
        this.palette = new int[palette.length];
        for (int i = 0; i < palette.length; ++i) {
            this.palette[i] = 0xFF000000 | palette[i];
        }
    }

    public static KnifeMaterial of(class_1799 stack) {
        return KnifeMaterial.named(class_7923.field_41178.method_10221(stack.method_7909()).method_12832());
    }

    public static KnifeMaterial named(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        if (p.startsWith("netherite")) {
            return NETHERITE;
        }
        if (p.startsWith("diamond")) {
            return DIAMOND;
        }
        if (p.startsWith("gold")) {
            return GOLDEN;
        }
        if (p.startsWith("copper")) {
            return COPPER;
        }
        if (p.startsWith("stone")) {
            return STONE;
        }
        if (p.startsWith("wood")) {
            return WOOD;
        }
        return IRON;
    }

    public int glow() {
        return this.glow;
    }

    static boolean blade(int material) {
        return material == 0 || material == 1 || material == 2;
    }

    static boolean metallic(int material) {
        return KnifeMaterial.blade(material) || material == 5 || material == 8;
    }
}

