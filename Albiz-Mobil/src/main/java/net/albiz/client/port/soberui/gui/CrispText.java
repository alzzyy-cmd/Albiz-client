// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Smooth;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

final class CrispText {
    private static final int PHASES = 4;
    private static final int PAD = 1;
    private static final int MAX_ATLASES = 24;
    private static final FontRenderContext RENDER = new FontRenderContext(null, true, true);
    private static final Map<String, Font> FONTS = new HashMap<String, Font>();
    private static final Map<String, Atlas> ATLASES = new HashMap<String, Atlas>();
    private static final Map<String, Atlas> METRICS = new HashMap<String, Atlas>();
    private static int counter;

    private CrispText() {
    }

    private static Font base(String file){return FONTS.computeIfAbsent(file,name->{try(InputStream stream=class_310.method_1551().method_1478().open(class_2960.method_60655("albiz","font/"+name))){return Font.createFont(Font.TRUETYPE_FONT,stream);}catch(Exception ex){SoberClient.LOG.warn("Could not load {} for crisp text",name,ex);return new Font("SansSerif",Font.BOLD,12);}});}

    private static Atlas atlas(String file, int pixels, float tracking) {
        String key = file + "|" + pixels + "|" + tracking;
        Atlas atlas = ATLASES.get(key);
        if (atlas == null) {
            if (ATLASES.size() >= 24) {
                CrispText.evict();
            }
            atlas = new Atlas(CrispText.base(file).deriveFont((float)pixels), pixels, tracking);
            ATLASES.put(key, atlas);
        }
        atlas.used = System.currentTimeMillis();
        return atlas;
    }

    private static void evict() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Atlas>> iterator = ATLASES.entrySet().iterator();
        while (iterator.hasNext() && ATLASES.size() >= 24) {
            Atlas atlas = iterator.next().getValue();
            if (now - atlas.used < 2000L) continue;
            atlas.close();
            iterator.remove();
        }
    }

    private static Atlas metrics(String file, float tracking) {
        return METRICS.computeIfAbsent(file + "|" + tracking, key -> new Atlas(CrispText.base(file).deriveFont(100.0f), 100, tracking));
    }

    static boolean canDisplay(String text, String file) {
        return text.isEmpty() || CrispText.base(file).canDisplayUpTo(text) == -1;
    }

    static float width(String text, String file, float em, float tracking) {
        int codePoint;
        if (text.isEmpty()) {
            return 0.0f;
        }
        Atlas atlas = CrispText.metrics(file, tracking);
        float total = 0.0f;
        for (int i = 0; i < text.length(); i += Character.charCount(codePoint)) {
            codePoint = text.codePointAt(i);
            total += atlas.advance(codePoint);
        }
        return total / 100.0f * em;
    }

    static void draw(class_332 context, String text, String file, float x, float y, float em, float tracking, int color) {
        if (text.isEmpty() || color >>> 24 == 0) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        float ppu = Smooth.pixelsPerUnit(context);
        int pixels = Math.max(6, Math.round(em * ppu));
        Atlas atlas = CrispText.atlas(file, pixels, tracking);
        class_1041 window = class_310.method_1551().method_22683();
        float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
        Vector2f origin = matrices.transformPosition(x, y, new Vector2f());
        float start = origin.x * ratio;
        int baseline = Math.round(origin.y * ratio);
        int length = text.codePointCount(0, text.length());
        Glyph[] glyphs = new Glyph[length];
        int[] lefts = new int[length];
        float pen = start;
        int n = 0;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            int whole = (int)Math.floor(pen);
            int phase = Math.min(3, (int)((pen - (float)whole) * 4.0f));
            glyphs[n] = atlas.glyph(codePoint, phase);
            lefts[n] = whole;
            pen += atlas.advance(codePoint);
            ++n;
        }
        for (Page page : atlas.pages) {
            if (!page.dirty) continue;
            page.texture.method_4524();
            page.dirty = false;
        }
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        for (i = 0; i < n; ++i) {
            Glyph glyph = glyphs[i];
            if (glyph.page() < 0) continue;
            class_2960 id = atlas.pages.get((int)glyph.page()).id;
            context.method_25293(class_10799.field_56883, id, lefts[i] + glyph.left(), baseline + glyph.top(), (float)glyph.u(), (float)glyph.v(), glyph.w(), glyph.h(), glyph.w(), glyph.h(), atlas.size, atlas.size, color);
        }
        matrices.popMatrix();
    }

    private static final class Atlas {
        final Font font;
        final int pixels;
        final int size;
        final float extra;
        final int[] boost = new int[256];
        final List<Page> pages = new ArrayList<Page>();
        final Int2ObjectOpenHashMap<Glyph> glyphs = new Int2ObjectOpenHashMap();
        final Int2FloatOpenHashMap advances = new Int2FloatOpenHashMap();
        int x;
        int y;
        int row;
        long used;

        Atlas(Font font, int pixels, float tracking) {
            this.font = font;
            this.pixels = pixels;
            this.size = pixels <= 20 ? 256 : (pixels <= 48 ? 512 : 1024);
            this.extra = tracking * (float)pixels;
            double gamma = pixels <= 12 ? 0.6 : (pixels <= 24 ? 0.72 : (pixels <= 40 ? 0.84 : 1.0));
            for (int a = 0; a < 256; ++a) {
                this.boost[a] = (int)Math.round(255.0 * Math.pow((double)a / 255.0, gamma));
            }
        }

        float advance(int codePoint) {
            if (this.advances.containsKey(codePoint)) {
                return this.advances.get(codePoint);
            }
            GlyphVector vector = this.font.createGlyphVector(RENDER, Character.toChars(codePoint));
            float advance = vector.getNumGlyphs() == 0 ? 0.0f : vector.getGlyphMetrics(0).getAdvance();
            this.advances.put(codePoint, advance + this.extra);
            return advance + this.extra;
        }

        Glyph glyph(int codePoint, int phase) {
            int key = codePoint * 4 + phase;
            Glyph glyph = (Glyph)this.glyphs.get(key);
            if (glyph != null) {
                return glyph;
            }
            glyph = this.rasterize(codePoint, phase);
            this.glyphs.put(key, glyph);
            return glyph;
        }

        private Glyph rasterize(int codePoint, int phase) {
            GlyphVector vector = this.font.createGlyphVector(RENDER, Character.toChars(codePoint));
            float shift = (float)phase / 4.0f;
            Rectangle bounds = vector.getPixelBounds(RENDER, shift, 0.0f);
            if (bounds.width <= 0 || bounds.height <= 0) {
                return new Glyph(-1, 0, 0, 0, 0, 0, 0);
            }
            int w = bounds.width + 2;
            int h = bounds.height + 2;
            if (w > this.size || h > this.size) {
                return new Glyph(-1, 0, 0, 0, 0, 0, 0);
            }
            BufferedImage image = new BufferedImage(w, h, 2);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(Color.WHITE);
            graphics.drawGlyphVector(vector, shift + 1.0f - (float)bounds.x, 1 - bounds.y);
            graphics.dispose();
            if (this.x + w > this.size) {
                this.x = 0;
                this.y += this.row;
                this.row = 0;
            }
            if (this.pages.isEmpty() || this.y + h > this.size) {
                this.pages.add(new Page(class_2960.method_60655((String)"albiz", (String)("dynamic/crisp_atlas_" + counter++)), this.size));
                this.x = 0;
                this.y = 0;
                this.row = 0;
            }
            Page page = this.pages.get(this.pages.size() - 1);
            for (int py = 0; py < h; ++py) {
                for (int px = 0; px < w; ++px) {
                    page.image.method_61941(this.x + px, this.y + py, this.boost[image.getRGB(px, py) >>> 24] << 24 | 0xFFFFFF);
                }
            }
            page.dirty = true;
            Glyph glyph = new Glyph(this.pages.size() - 1, this.x, this.y, w, h, bounds.x - 1, bounds.y - 1);
            this.x += w;
            this.row = Math.max(this.row, h);
            return glyph;
        }

        void close() {
            for (Page page : this.pages) {
                class_310.method_1551().method_1531().method_4615(page.id);
            }
            this.pages.clear();
        }
    }

    private record Glyph(int page, int u, int v, int w, int h, int left, int top) {
    }

    private static final class Page {
        final class_2960 id;
        final class_1011 image;
        final Texture texture;
        boolean dirty;

        Page(class_2960 id, int size) {
            this.id = id;
            this.image = new class_1011(size, size, true);
            this.texture = new Texture(id, this.image);
            class_310.method_1551().method_1531().method_4616(id, (class_1044)this.texture);
        }
    }

    private static final class Texture
    extends class_1043 {
        Texture(class_2960 id, class_1011 image) {
            super(() -> ((class_2960)id).toString(), image);
        }

        public class_12137 method_75484() {
            return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        }
    }
}

