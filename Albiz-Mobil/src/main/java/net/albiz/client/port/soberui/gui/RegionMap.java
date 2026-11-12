// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.HudPalette;
import net.albiz.client.port.soberui.gui.Smooth;
import net.minecraft.class_11719;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_746;
import org.joml.Matrix3x2fStack;

public final class RegionMap {
    private static final int REGION_BLOCKS = 50000;
    private static final int ORIGIN = -225000;
    private static final int SIZE = 9;
    private static final float UNIT = 0.5f;
    private static final float PAD = 10.0f;
    private static final float INFO_GAP = 8.0f;
    private static final float INFO_LINE_H = 22.0f;
    private static final float LEGEND_GAP = 4.0f;
    private static final float LEGEND_ROW_H = 16.0f;
    private static final float LEGEND_SWATCH = 9.0f;
    private static final float LABEL_GAP = 8.0f;
    private static final float TEXT_PX = 17.0f;
    private static final float LABEL_PX = 14.0f;
    private static final float FONT_PX = 9.0f;
    private static final float CARD_ROUNDING = 8.0f;
    private static final String MINUS = "\u2212";
    private static final String DOT = "  \u00b7  ";
    private static final String COORDINATE_LABEL = "Coordinate";
    private static final String REGION_LABEL = "Region";
    private static final int TEXT_VALUE = 0xF4F4F5;
    private static final int TEXT_NAME = 0xF4F4F5;
    private static final int TEXT_LABEL = 11447990;
    private static final int BORDER = -13750733;
    private static final int GUARD = -1946157056;
    private static final int ARROW_OUTLINE = -1728053248;
    private static final class_2583 TEXT_FONT = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)"geist_text")));
    private static final class_2583 LABEL_FONT = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)"geist_label")));
    private static final int[][] LAYOUT = new int[][]{{82, 5}, {100, 3}, {101, 3}, {102, 3}, {103, 2}, {104, 2}, {105, 2}, {106, 2}, {91, 2}, {83, 5}, {44, 3}, {75, 3}, {42, 3}, {41, 2}, {40, 2}, {39, 2}, {38, 2}, {92, 2}, {84, 5}, {45, 3}, {14, 3}, {13, 3}, {12, 2}, {11, 2}, {10, 2}, {37, 2}, {93, 2}, {85, 5}, {46, 5}, {74, 5}, {3, 3}, {2, 2}, {1, 2}, {25, 2}, {36, 2}, {94, 2}, {86, 4}, {47, 4}, {72, 4}, {71, 4}, {5, 2}, {4, 2}, {24, 2}, {35, 2}, {95, 2}, {87, 4}, {51, 1}, {17, 1}, {9, 0}, {8, 0}, {7, 0}, {23, 0}, {34, 0}, {96, 2}, {88, 4}, {54, 1}, {18, 1}, {61, 0}, {62, 0}, {21, 0}, {22, 0}, {33, 0}, {97, 0}, {89, 0}, {26, 1}, {27, 0}, {28, 0}, {29, 0}, {30, 0}, {59, 0}, {32, 0}, {98, 0}, {90, 0}, {107, 1}, {108, 1}, {109, 1}, {110, 1}, {111, 1}, {112, 1}, {113, 1}, {99, 0}};
    private static final Server[] TYPES = new Server[]{Server.EUC, Server.EUW, Server.NAE, Server.NAW, Server.ASIA, Server.OCE};

    private RegionMap() {
    }

    private static Server server(int row, int col) {
        return TYPES[LAYOUT[row * 9 + col][1]];
    }

    private static int regionId(int row, int col) {
        return LAYOUT[row * 9 + col][0];
    }

    private static int cell(double coordinate) {
        long index = Math.floorDiv((long)Math.floor(coordinate) - -225000L, 50000);
        return (int)Math.max(0L, Math.min(8L, index));
    }

    public static Region at(double x, double z) {
        int col = RegionMap.cell(x);
        int row = RegionMap.cell(z);
        double minX = -225000.0 + (double)col * 50000.0;
        double minZ = -225000.0 + (double)row * 50000.0;
        double cx = Math.max(minX, Math.min(minX + 50000.0, x));
        double cz = Math.max(minZ, Math.min(minZ + 50000.0, z));
        double edge = Math.min(Math.min(cx - minX, minX + 50000.0 - cx), Math.min(cz - minZ, minZ + 50000.0 - cz));
        return new Region(row, col, String.valueOf(RegionMap.regionId(row, col)), RegionMap.server(row, col), edge, (float)((cx - minX) / 50000.0), (float)((cz - minZ) / 50000.0));
    }

    public static Region current(class_310 mc) {
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1687.method_27983() != class_1937.field_25179) {
            return null;
        }
        return RegionMap.at(mc.field_1724.method_23317(), mc.field_1724.method_23321());
    }

    private static int[] worldToGrid(double x, double z) {
        return new int[]{(int)((x + 225000.0) / 50000.0), (int)((z + 225000.0) / 50000.0)};
    }

    private static double[] worldToCellPosition(double x, double z) {
        return new double[]{RegionMap.positiveModulo(x + 225000.0, 50000.0) / 50000.0, RegionMap.positiveModulo(z + 225000.0, 50000.0) / 50000.0};
    }

    private static double positiveModulo(double value, double divisor) {
        double mod = value % divisor;
        return mod < 0.0 ? mod + divisor : mod;
    }

    private static boolean inside(int[] grid) {
        return grid[0] >= 0 && grid[0] < 9 && grid[1] >= 0 && grid[1] < 9;
    }

    public static int width(net.albiz.client.port.soberui.modules.RegionMap map) {
        float grid = RegionMap.gridSize(map);
        float content = Math.max(grid, RegionMap.infoWidth(map));
        return (int)Math.ceil((content + 20.0f) * 0.5f);
    }

    public static int height(net.albiz.client.port.soberui.modules.RegionMap map) {
        return (int)Math.ceil((RegionMap.gridSize(map) + 20.0f + 8.0f + RegionMap.infoHeight(map)) * 0.5f);
    }

    private static float gridSize(net.albiz.client.port.soberui.modules.RegionMap map) {
        return 9.0f * (map.cellSize() + map.cellGap()) - map.cellGap();
    }

    private static float infoWidth(net.albiz.client.port.soberui.modules.RegionMap map) {
        float labelCol = RegionMap.labelColumn(map) + 8.0f;
        float values = map.showCoordinates() ? RegionMap.width("\u2212000000, \u2212000000", 17.0f, TEXT_FONT) : 0.0f;
        float legend = 0.0f;
        for (Server type : TYPES) {
            values = Math.max(values, RegionMap.width("000  \u00b7  " + type.name, 17.0f, TEXT_FONT));
            legend = Math.max(legend, 15.0f + RegionMap.width(type.name, 14.0f, LABEL_FONT));
        }
        values = Math.max(values, RegionMap.width("000  \u00b7  Unknown", 17.0f, TEXT_FONT));
        return (float)Math.ceil(Math.max(labelCol + values, map.showLegend() ? legend : 0.0f));
    }

    private static float infoHeight(net.albiz.client.port.soberui.modules.RegionMap map) {
        float height = 22.0f * (float)RegionMap.infoLines(map);
        if (map.showLegend()) {
            height += 4.0f + (float)TYPES.length * 16.0f;
        }
        return height;
    }

    private static int infoLines(net.albiz.client.port.soberui.modules.RegionMap map) {
        return map.showCoordinates() ? 2 : 1;
    }

    private static float labelColumn(net.albiz.client.port.soberui.modules.RegionMap map) {
        float region = RegionMap.width(REGION_LABEL, 14.0f, LABEL_FONT);
        return map.showCoordinates() ? Math.max(RegionMap.width(COORDINATE_LABEL, 14.0f, LABEL_FONT), region) : region;
    }

    private static float width(String value, float px, class_2583 font) {
        return (float)class_310.method_1551().field_1772.method_27525((class_5348)class_2561.method_43470((String)value).method_10862(font)) * px / 9.0f;
    }

    private static void text(class_332 context, String value, float x, float y, float px, class_2583 font, int color, boolean guard) {
        if (value.isEmpty() || color >>> 24 == 0) {
            return;
        }
        float scale = px / 9.0f;
        class_5250 line = class_2561.method_43470((String)value).method_10862(font);
        Matrix3x2fStack matrices = context.method_51448();
        if (guard) {
            matrices.pushMatrix();
            matrices.translate(x + 1.0f, y + 1.0f);
            matrices.scale(scale, scale);
            context.method_51439(class_310.method_1551().field_1772, (class_2561)line, 0, 0, RegionMap.scaleAlpha(-1946157056, (float)(color >>> 24) / 255.0f), false);
            matrices.popMatrix();
        }
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale, scale);
        context.method_51439(class_310.method_1551().field_1772, (class_2561)line, 0, 0, color, false);
        matrices.popMatrix();
    }

    private static int alpha(int rgb, float alpha) {
        return Math.round(255.0f * Math.max(0.0f, Math.min(1.0f, alpha))) << 24 | rgb & 0xFFFFFF;
    }

    private static int scaleAlpha(int argb, float alpha) {
        return Math.round((float)(argb >>> 24) * Math.max(0.0f, Math.min(1.0f, alpha))) << 24 | argb & 0xFFFFFF;
    }

    private static void card(class_332 context, float w, float h, float alpha, boolean shadow) {
        int r = Math.round(8.0f);
        Smooth.rect(context, 0, 0, Math.round(w), Math.round(h), r, RegionMap.scaleAlpha(HudPalette.widget(), alpha));
        Smooth.outline(context, 0, 0, Math.round(w), Math.round(h), r, RegionMap.scaleAlpha(0x1CFFFFFF, alpha));
    }

    public static void draw(class_332 context, class_310 mc, net.albiz.client.port.soberui.modules.RegionMap map) {
        class_746 player = mc.field_1724;
        float alpha = map.opacity();
        int w = RegionMap.width(map);
        int h = RegionMap.height(map);
        float layoutWidth = (float)w / 0.5f;
        float layoutHeight = (float)h / 0.5f;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.scale(0.5f, 0.5f);
        RegionMap.card(context, layoutWidth, layoutHeight, alpha, map.shadow());
        if (player == null) {
            matrices.popMatrix();
            return;
        }
        float cellSize = map.cellSize();
        float cellGap = map.cellGap();
        float gridSize = RegionMap.gridSize(map);
        float gridX = Math.round((layoutWidth - gridSize) * 0.5f);
        float gridY = 10.0f;
        int cellR = Math.round(Math.min(Math.max(2.0f, -2.0f), cellSize * 0.3f));
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                float cx = gridX + (float)col * (cellSize + cellGap);
                float cy = gridY + (float)row * (cellSize + cellGap);
                Smooth.rect(context, Math.round(cx), Math.round(cy), Math.round(cellSize), Math.round(cellSize), cellR, RegionMap.alpha(RegionMap.server((int)row, (int)col).color, alpha));
                if (!map.showRegionIds()) continue;
                RegionMap.drawCenteredCellText(context, RegionMap.regionId(row, col), cx, cy, cellSize, map.textScale(), alpha);
            }
        }
        RegionMap.renderPlayerPin(context, map, player, gridX, gridY, cellSize, cellGap, alpha);
        RegionMap.renderInfoPanel(context, map, player, 10.0f, gridY + gridSize + 8.0f, alpha);
        matrices.popMatrix();
    }

    private static void drawCenteredCellText(class_332 context, int regionId, float cx, float cy, float size, float textScale, float alpha) {
        float fontPx = Math.max(8.0f, (float)Math.round(14.0f * textScale * 0.85f));
        String label = String.valueOf(regionId);
        float tx = cx + (size - RegionMap.width(label, fontPx, LABEL_FONT)) * 0.5f;
        float ty = cy + (size - fontPx * 7.0f / 9.0f) * 0.5f;
        RegionMap.text(context, label, tx, ty, fontPx, LABEL_FONT, RegionMap.alpha(0xF4F4F5, alpha), true);
    }

    private static void renderPlayerPin(class_332 context, net.albiz.client.port.soberui.modules.RegionMap map, class_746 player, float gridX, float gridY, float cellSize, float cellGap, float alpha) {
        double pz;
        double px = player.method_23317();
        int[] grid = RegionMap.worldToGrid(px, pz = player.method_23321());
        if (!RegionMap.inside(grid)) {
            return;
        }
        double[] cellPos = RegionMap.worldToCellPosition(px, pz);
        float centerX = gridX + (float)grid[0] * (cellSize + cellGap) + (float)(cellPos[0] * (double)cellSize);
        float centerY = gridY + (float)grid[1] * (cellSize + cellGap) + (float)(cellPos[1] * (double)cellSize);
        double angle = Math.toRadians((double)(-player.method_36454()) - 90.0);
        float arrowSize = 9.0f * map.playerScale();
        float dx = (float)Math.cos(angle);
        float dy = (float)(-Math.sin(angle));
        int color = map.playerIndicatorColor().argb();
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.rotate((float)Math.atan2(dx, -dy));
        float reach = arrowSize * 2.8f;
        float spread = reach * 0.62f;
        int beam = RegionMap.scaleAlpha(color & 0xFFFFFF | 0x66000000, alpha);
        int clear = color & 0xFFFFFF;
        Smooth.polygon(context, new float[]{0.0f, 0.0f, -spread, -reach, 0.0f, -reach * 1.08f, spread, -reach}, new int[]{beam, clear, clear, clear});
        float l = arrowSize * 1.05f;
        float[] dart = new float[]{0.0f, -l, l * 0.74f, l * 0.78f, 0.0f, l * 0.34f, -l * 0.74f, l * 0.78f};
        float[] xs = new float[]{dart[0], dart[2], dart[4], dart[6], dart[0]};
        float[] ys = new float[]{dart[1], dart[3], dart[5], dart[7], dart[1]};
        int edge = RegionMap.scaleAlpha(-1728053248, alpha);
        Smooth.polyline(context, xs, ys, new float[]{2.4f, 2.4f, 2.4f, 2.4f, 2.4f}, new int[]{edge, edge, edge, edge, edge}, 5);
        Smooth.polygon(context, dart, RegionMap.scaleAlpha(color, alpha));
        int rim = RegionMap.scaleAlpha(-855638017, alpha);
        Smooth.polyline(context, xs, ys, new float[]{0.8f, 0.8f, 0.8f, 0.8f, 0.8f}, new int[]{rim, rim, rim, rim, rim}, 5);
        matrices.popMatrix();
    }

    private static void renderInfoPanel(class_332 context, net.albiz.client.port.soberui.modules.RegionMap map, class_746 player, float x, float y, float alpha) {
        int[] grid;
        float labelOffset = Math.round(2.4f);
        float valueX = x + RegionMap.labelColumn(map) + 8.0f;
        float lineH = 22.0f;
        float textInset = (float)Math.floor((lineH - 17.0f) * 0.5f);
        float lineTop = y + textInset;
        if (map.showCoordinates()) {
            RegionMap.text(context, COORDINATE_LABEL, x, lineTop + labelOffset, 14.0f, LABEL_FONT, RegionMap.alpha(11447990, alpha), false);
            RegionMap.text(context, RegionMap.number((long)Math.floor(player.method_23317())) + ", " + RegionMap.number((long)Math.floor(player.method_23321())), valueX, lineTop, 17.0f, TEXT_FONT, RegionMap.alpha(0xF4F4F5, alpha), false);
            lineTop += lineH;
        }
        int regionId = RegionMap.inside(grid = RegionMap.worldToGrid(player.method_23317(), player.method_23321())) ? RegionMap.regionId(grid[1], grid[0]) : -1;
        String regionName = RegionMap.inside(grid) ? RegionMap.server((int)grid[1], (int)grid[0]).name : "Unknown";
        RegionMap.text(context, REGION_LABEL, x, lineTop + labelOffset, 14.0f, LABEL_FONT, RegionMap.alpha(11447990, alpha), false);
        RegionMap.text(context, regionId + DOT + regionName, valueX, lineTop, 17.0f, TEXT_FONT, RegionMap.alpha(0xF4F4F5, alpha), false);
        if (!map.showLegend()) {
            return;
        }
        float legendY = y + lineH * (float)RegionMap.infoLines(map) + 4.0f;
        for (int i = 0; i < TYPES.length; ++i) {
            float rowY = legendY + (float)i * 16.0f;
            float swatchY = rowY + 3.5f;
            Smooth.rect(context, Math.round(x), Math.round(swatchY), Math.round(9.0f), Math.round(9.0f), 2, RegionMap.alpha(RegionMap.TYPES[i].color, alpha));
            RegionMap.text(context, RegionMap.TYPES[i].name, x + 9.0f + 6.0f, rowY + (float)Math.floor(2.5555553436279297), 14.0f, LABEL_FONT, RegionMap.alpha(0xF4F4F5, alpha), false);
        }
    }

    private static String number(long value) {
        return value < 0L ? MINUS + -value : Long.toString(value);
    }

    public static enum Server {
        EUC("EU-C", "EU Central", 10473059),
        EUW("EU-W", "EU West", 42595),
        NAE("NA-E", "NA East", 5221866),
        NAW("NA-W", "NA West", 3108538),
        ASIA("ASIA", "Asia", 16106050),
        OCE("OCE", "Oceania", 16549891);

        public final String title;
        public final String name;
        public final int color;

        private Server(String title, String name, int color) {
            this.title = title;
            this.name = name;
            this.color = color;
        }
    }

    public record Region(int row, int col, String number, Server server, double edge, float fx, float fz) {
    }
}

