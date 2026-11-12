// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.CardChrome;
import net.albiz.client.port.soberui.gui.CardFont;
import net.albiz.client.port.soberui.gui.CardTexture;
import net.albiz.client.port.soberui.gui.GuiPalette;
import net.albiz.client.port.soberui.gui.OverlayCanvas;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SpotifyLogo;
import net.albiz.client.port.soberui.gui.StableWidth;
import net.albiz.client.module.impl.client.SpotifyHudModule;
import net.albiz.client.util.spotify.AlbumArt;
import net.albiz.client.util.spotify.DisplayText;
import net.albiz.client.util.spotify.Lyrics;
import net.albiz.client.util.spotify.NowPlaying;
import net.albiz.client.util.spotify.PlaybackClock;
import net.albiz.client.util.spotify.SpotifyWindow;
import net.albiz.client.util.spotify.SyncedLyrics;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

public final class SpotifyPanel {
    public static final SpotifyPanel INSTANCE = new SpotifyPanel();
    private static final float UNIT = 0.5f;
    private static final float LOGO_ROOM = 22.0f;
    private final Map<String, Int2ObjectOpenHashMap<StableWidth>> slots = new HashMap<String, Int2ObjectOpenHashMap<StableWidth>>();
    private float mouseX = -1.0f;
    private float mouseY = -1.0f;
    private boolean chatInput;
    private SpotifyPlan plan;
    private long lastFrame;
    private static final SpotifyWindow.MediaKey[] SPOTIFY_BUTTONS = new SpotifyWindow.MediaKey[]{SpotifyWindow.MediaKey.PREVIOUS, SpotifyWindow.MediaKey.PLAY_PAUSE, SpotifyWindow.MediaKey.NEXT};
    private final float[] spotifyButtons = new float[12];
    private boolean spotifyButtonsShown;
    private boolean spotifyButtonsDrawn;
    private static final float SPOTIFY_PAD_Y = 8.0f;
    private static final float SPOTIFY_LINE_GAP = 2.0f;
    private static final float SPOTIFY_GLYPH = 10.0f;
    private static final float SPOTIFY_GLYPH_GAP = 10.0f;
    private static final float SPOTIFY_PAUSED = 0.62f;
    private static final float MARQUEE_SPEED = 30.0f;
    private static final float MARQUEE_HOLD = 2.4f;
    private static final int LYRIC_MAX_ROWS = 4;
    private static final float LYRIC_GROW_SPEED = 14.0f;
    private static final float LYRIC_SHRINK_SPEED = 9.0f;
    private static final float LYRIC_FAR_ALPHA = 0.6f;
    private static final float LYRIC_PAUSED_ALPHA = 0.7f;
    private static final float LYRIC_PAUSE_SPEED = 8.0f;
    private static final float LYRIC_BREATH_PERIOD = 1.6f;
    private static final long LYRIC_LEAD_MS = 200L;
    private static final long LYRIC_SWEEP_MS = 180L;
    private static final float LYRIC_SLOW_PACE = 2.0f;
    private static final float LYRIC_CHAR_MS = 90.0f;
    private static final float LYRIC_LIT_ALL = Float.POSITIVE_INFINITY;
    private static final long LYRIC_QUIET_MS = 700L;
    private static final float LYRIC_MIN_COVERAGE = 0.75f;
    private static final String[] NO_ROWS = new String[0];
    private static final NowPlaying SAMPLE_TRACK = new NowPlaying(NowPlaying.Status.PLAYING, "Golden Hour Drive", "Sample Artist", "", NowPlaying.Source.SESSION, 214000L, 84000L, 0L, 1.0, null);
    private static final Lyrics SAMPLE_LYRICS = new Lyrics(Lyrics.Status.SYNCED, SyncedLyrics.parse("[01:21.00]Every light in the city knows my name\n[01:27.40]We keep on driving till the morning comes\n[01:33.80]Nothing but the radio and you\n"), List.of(), 0L);
    private float marqueeTime;
    private String marqueeTitle = "";
    private String marqueeArtist = "";
    private final PlaybackClock spotifyClock = new PlaybackClock();
    private NowPlaying spotifyFolded;
    private String spotifyTitle = "";
    private String spotifyArtist = "";
    private AlbumArt spotifyArt;
    private CardTexture spotifyArtTexture;
    private Lyrics lyricPrepared;
    private String[] lyricTexts = NO_ROWS;
    private String[][] lyricRows = new String[0][];
    private int lyricRowsPx;
    private int lyricRowsWidth;
    private LyricWords[] lyricWords = new LyricWords[0];
    private float lyricCharMs = 90.0f;
    private boolean lyricReadable = true;
    private float lyricViewH = -1.0f;
    private int lyricViewLines;
    private float lyricViewScale;
    private SyncedLyrics lyricSource;
    private int lyricStart;
    private final List<LyricShift> lyricShifts = new ArrayList<LyricShift>(8);
    private float lyricClock;
    private List<LyricItem> lyricFadeItems;
    private float lyricFade = 1.0f;
    private List<LyricItem> lyricLastItems = List.of();
    private List<LyricItem> lyricLeaveItems;
    private float lyricLeave;
    private float[] lyricShown = new float[0];
    private float lyricBreath;
    private float lyricPlaying = 1.0f;
    private int lyricCurrentFirst = -1;
    private int lyricCurrentLast = -1;
    private float[] lyricBright = new float[0];
    private int lyricBrightLo;
    private int lyricBrightHi = -1;
    private float lyricBlockH;
    private long lyricLoadingSince;

    private SpotifyPanel() {
    }

    public int width(boolean editMode) {
        SpotifyPlan current = this.ensurePlan(editMode);
        return (int)Math.ceil(current.width() * 0.5f);
    }

    public int height(boolean editMode) {
        SpotifyPlan current = this.ensurePlan(editMode);
        return (int)Math.ceil(current.height() * 0.5f);
    }

    public boolean visible() {
        return SpotifyHudModule.shouldShowHud();
    }

    private SpotifyPlan ensurePlan(boolean editMode) {
        long now = System.nanoTime();
        if (this.plan == null || now - this.lastFrame > 5000000L) {
            float dt = this.lastFrame == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.lastFrame) / 1.0E9f);
            this.lastFrame = now;
            this.plan = this.planSpotify(editMode, dt);
        }
        return this.plan;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void draw(class_332 context, double localX, double localY, boolean editMode, boolean chatOpen) {
        SpotifyPlan current = this.ensurePlan(editMode);
        this.chatInput = chatOpen;
        this.mouseX = (float)(localX / 0.5);
        this.mouseY = (float)(localY / 0.5);
        this.spotifyButtonsShown = this.spotifyButtonsDrawn;
        this.spotifyButtonsDrawn = false;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.scale(0.5f, 0.5f);
        try {
            this.drawSpotify(OverlayCanvas.of(context), current, 1.0f);
        }
        finally {
            matrices.popMatrix();
        }
        this.plan = null;
    }

    public boolean press(double localX, double localY) {
        if (!this.spotifyButtonsShown) {
            return false;
        }
        int button = this.spotifyButtonAt((float)(localX / 0.5), (float)(localY / 0.5));
        if (button < 0) {
            return false;
        }
        SpotifyHudModule.hudButton(SPOTIFY_BUTTONS[button]);
        return true;
    }

    private SpotifyPlan planSpotify(boolean editMode, float dt) {
        boolean track;
        float s = 1.0f;
        Type t = Type.of(s);
        if (!editMode && !SpotifyHudModule.shouldShowHud()) {
            this.removeSlot("spotify", t.textPx());
            this.releaseSpotifyArt();
            this.lyricBlockH = 0.0f;
            return new SpotifyPlan(t, SpotifyLook.HIDDEN, SpotifyGlyph.NONE, "", "", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, null, SpotifyColors.DEFAULT, 1.0f, Math.round(28.0f * s));
        }
        SpotifyHudModule module = SpotifyHudModule.get();
        SpotifyHudModule active = SpotifyHudModule.active();
        NowPlaying now = active != null ? active.nowPlaying() : NowPlaying.WAITING;
        boolean bl = track = now.active() && now.hasTrack();
        if (track && now.source() == NowPlaying.Source.SESSION) {
            return this.planSpotifyCard(module, now, false, true, t, s, editMode, dt);
        }
        if (editMode && !track) {
            return this.planSpotifyCard(module, SAMPLE_TRACK, true, true, t, s, true, dt);
        }
        this.releaseSpotifyArt();
        this.lyricBlockH = 0.0f;
        return this.planSpotifyCompact(module, now, t, s, editMode, dt);
    }

    private void drawSpotify(OverlayCanvas dl, SpotifyPlan plan, float a) {
        if (a <= 0.001f || plan.look() == SpotifyLook.HIDDEN) {
            return;
        }
        float s = 1.0f;
        float x = 0.0f;
        float y = 0.0f;
        float w = plan.width();
        float h = plan.height();
        CardChrome.card(dl, x, y, w, h, 8.0f * s, a, true, s);
        if (plan.look() == SpotifyLook.CARD) {
            this.drawSpotifyCard(dl, plan, x, y, w, h, s, a, false);
            float size = plan.type().labelPx();
            this.drawLogo(dl, x + w - 10.0f * s - size, y + plan.card().textTop(), size, a);
        } else {
            this.drawSpotifyCompact(dl, plan, x, y, w, h, s, a, false);
            float size = 14.0f * s;
            this.drawLogo(dl, x + w - 12.0f * s - size, y + (h - size) * 0.5f, size, a);
        }
    }

    private void drawLogo(OverlayCanvas dl, float x, float y, float size, float a) {
        if (a <= 0.003f) {
            return;
        }
        class_332 context = dl.context();
        Matrix3x2fStack matrices = context.method_51448();
        Vector2f origin = matrices.transformPosition(x, y, new Vector2f());
        class_1041 window = class_310.method_1551().method_22683();
        float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
        int side = Math.max(4, Math.round(size * Smooth.pixelsPerUnit(context)));
        int texture = SpotifyLogo.sizeFor(side);
        int left = Math.round(origin.x * ratio);
        int top = Math.round(origin.y * ratio);
        int color = Math.round(Math.max(0.0f, Math.min(1.0f, a)) * 255.0f) << 24 | 0xFFFFFF;
        matrices.pushMatrix();
        matrices.identity();
        matrices.scale(1.0f / ratio, 1.0f / ratio);
        context.method_25293(class_10799.field_56883, SpotifyLogo.texture(texture), left, top, 0.0f, 0.0f, side, side, texture, texture, texture, texture, color);
        matrices.popMatrix();
    }

    private float slot(String key, int px, float target, float dt) {
        Int2ObjectOpenHashMap bySize = this.slots.computeIfAbsent(key, k -> new Int2ObjectOpenHashMap());
        StableWidth width = (StableWidth)bySize.get(px);
        if (width == null) {
            width = new StableWidth();
            bySize.put(px, (Object)width);
        }
        return width.update(target, dt);
    }

    private void removeSlot(String key, int px) {
        Int2ObjectOpenHashMap<StableWidth> bySize = this.slots.get(key);
        if (bySize != null) {
            bySize.remove(px);
        }
    }

    private SpotifyPlan planSpotifyCompact(SpotifyHudModule module, NowPlaying now, Type t, float s, boolean editMode, float dt) {
        String title;
        boolean withArtist = module == null || module.showArtist();
        String artist = "";
        SpotifyGlyph glyph = SpotifyGlyph.NONE;
        boolean idle = false;
        switch (now.status()) {
            case PLAYING: 
            case PAUSED: {
                glyph = now.playing() ? SpotifyGlyph.PLAYING : SpotifyGlyph.PAUSED;
                this.foldTrack(now, t.text());
                String string = title = now.hasTrack() ? this.spotifyTitle : "Not playing";
                if (!withArtist) break;
                artist = this.spotifyArtist;
                break;
            }
            case NOT_RUNNING: {
                title = "Spotify is not open";
                idle = true;
                break;
            }
            case UNSUPPORTED: {
                title = "Spotify \u00b7 Windows only";
                idle = true;
                break;
            }
            default: {
                title = "Nothing playing";
                idle = true;
            }
        }
        CardFont titleFont = idle ? t.label() : t.text();
        int titlePx = idle ? t.labelPx() : t.textPx();
        float titleW = CardChrome.width(titleFont, titlePx, title);
        float artistW = artist.isEmpty() ? 0.0f : CardChrome.width(t.label(), t.labelPx(), artist);
        float glyphW = glyph == SpotifyGlyph.NONE ? 0.0f : 20.0f * s;
        float fixed = 12.0f * s * 2.0f + glyphW + 22.0f * s;
        float maxText = Math.max(40.0f * s, (module != null ? module.cardWidth() : 320.0f) * s - fixed);
        float textW = Math.min(Math.max(titleW, artistW), maxText);
        float w = this.slot("spotify", t.textPx(), (float)Math.ceil(fixed + textW), dt);
        float height = artist.isEmpty() ? 28.0f * s : 8.0f * s * 2.0f + (float)t.textPx() + 2.0f * s + (float)t.labelPx();
        float avail = Math.max(1.0f, w - fixed);
        boolean scroll = module == null || module.scrollTitles();
        float loopGap = 40.0f * s;
        float[] offsets = this.marquee(title, artist, titleW, artistW, avail, scroll, now.playing() || editMode, loopGap, s, dt);
        return new SpotifyPlan(t, idle ? SpotifyLook.IDLE : SpotifyLook.COMPACT, glyph, title, artist, titleW, artistW, offsets[0], offsets[1], loopGap, scroll, null, SpotifyColors.of(module), Math.round(w), Math.round(height));
    }

    private void drawSpotifyCompact(OverlayCanvas dl, SpotifyPlan plan, float x, float y, float w, float h, float s, float a, boolean guard) {
        Type t = plan.type();
        float cx = x + 12.0f * s;
        if (plan.glyph() != SpotifyGlyph.NONE) {
            this.drawPlayState(dl, cx, y + h * 0.5f, 10.0f * s, plan.glyph() == SpotifyGlyph.PLAYING, a);
            cx += 20.0f * s;
        }
        float avail = Math.max(1.0f, x + w - 12.0f * s - 22.0f * s - cx);
        if (plan.look() == SpotifyLook.IDLE) {
            float top = y + (float)Math.floor((h - (float)t.labelPx()) * 0.5f);
            SpotifyPanel.marqueeLine(dl, t.label(), t.labelPx(), cx, top, avail, plan.titleW(), 0.0f, plan.loopGap(), false, plan.title(), CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a), guard);
        } else {
            float dim = plan.glyph() == SpotifyGlyph.PAUSED ? 0.62f : 1.0f;
            boolean twoLines = !plan.artist().isEmpty();
            float contentH = twoLines ? (float)t.textPx() + 2.0f * s + (float)t.labelPx() : (float)t.textPx();
            float titleTop = y + (float)Math.floor((h - contentH) * 0.5f);
            SpotifyPanel.marqueeLine(dl, t.text(), t.textPx(), cx, titleTop, avail, plan.titleW(), plan.titleOffset(), plan.loopGap(), plan.scroll(), plan.title(), CardChrome.scaleAlpha(plan.colors().title(), a * dim), guard);
            if (twoLines) {
                float artistTop = titleTop + (float)t.textPx() + 2.0f * s;
                SpotifyPanel.marqueeLine(dl, t.label(), t.labelPx(), cx, artistTop, avail, plan.artistW(), plan.artistOffset(), plan.loopGap(), plan.scroll(), plan.artist(), CardChrome.scaleAlpha(plan.colors().artist(), a), guard);
            }
        }
    }

    private void drawPlayState(OverlayCanvas dl, float left, float centerY, float size, boolean playing, float a) {
        float top = Math.round(centerY - size * 0.5f);
        float bottom = top + (float)Math.round(size);
        if (playing) {
            dl.addTriangleFilled(left, top, left + size * 0.9f, (top + bottom) * 0.5f, left, bottom, CardChrome.scaleAlpha(CardChrome.accent(), a));
        } else {
            float bar = Math.max(2.0f, (float)Math.round(size * 0.3f));
            float gap = Math.max(2.0f, (float)Math.round(size * 0.25f));
            int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a);
            float l = Math.round(left + (size - bar * 2.0f - gap) * 0.5f);
            dl.addRectFilled(l, top, l + bar, bottom, color, 1.0f);
            dl.addRectFilled(l + bar + gap, top, l + bar * 2.0f + gap, bottom, color, 1.0f);
        }
    }

    private SpotifyPlan planSpotifyCard(SpotifyHudModule module, NowPlaying track, boolean sample, boolean lyricsSeen, Type t, float s, boolean editMode, float dt) {
        boolean lyricsWanted;
        boolean withArtist = module == null || module.showArtist();
        boolean withArt = module == null || module.showAlbumArt();
        boolean withProgress = module == null || module.showProgress();
        boolean withLyrics = module == null || module.showLyrics();
        boolean scroll = module == null || module.scrollTitles();
        long nowMs = System.currentTimeMillis();
        this.foldTrack(track, t.text());
        String title = this.spotifyTitle;
        String artist = withArtist ? this.spotifyArtist : "";
        boolean timeline = track.hasTimeline();
        long position = track.positionMs();
        if (!sample) {
            this.spotifyClock.update(track, nowMs);
            position = this.spotifyClock.positionAt(nowMs);
        }
        float capTop = this.capTop(t.label(), t.labelPx());
        float transportTop = (float)t.labelPx() + 3.0f * s + (float)t.textPx() + (artist.isEmpty() ? 0.0f : 1.0f * s + (float)t.labelPx()) + 7.0f * s;
        boolean progressBar = withProgress && timeline;
        float columnBottom = progressBar ? transportTop + (float)t.labelPx() + 6.0f * s + SpotifyPanel.progressHeight(s) : transportTop + CardChrome.ascent(t.label(), t.labelPx());
        float header = Math.round(columnBottom - capTop);
        float pad = 10.0f * s;
        float w = Math.round(Math.max(200.0f, module != null ? module.cardWidth() : 320.0f) * s);
        boolean art = withArt && w - pad * 2.0f - header - 10.0f * s >= 128.0f * s;
        float column = w - pad * 2.0f - (art ? header + 10.0f * s : 0.0f);
        String elapsed = "";
        Object remaining = "";
        float progress = 0.0f;
        if (timeline) {
            long elapsedSeconds = position / 1000L;
            long totalSeconds = (track.durationMs() + 500L) / 1000L;
            elapsed = SpotifyPanel.formatSeconds(elapsedSeconds);
            remaining = "\u2212" + SpotifyPanel.formatSeconds(Math.max(0L, totalSeconds - elapsedSeconds));
            progress = SpotifyPanel.clamp01((float)position / (float)track.durationMs());
        }
        float titleW = CardChrome.width(t.text(), t.textPx(), title);
        float artistW = artist.isEmpty() ? 0.0f : CardChrome.width(t.label(), t.labelPx(), artist);
        float loopGap = 40.0f * s;
        float[] offsets = this.marquee(title, artist, titleW, artistW, column, scroll, track.playing() || editMode, loopGap, s, dt);
        lyricsWanted = sample ? withLyrics && timeline : lyricsSeen && withLyrics && module != null && module.wantsLyrics(track);
        Lyrics lyrics = !lyricsWanted ? null : (sample ? SAMPLE_LYRICS : module.lyricsFor(track));
        boolean online = module == null || module.lyricsOnline();
        int lines = module == null ? 2 : module.lyricLines();
        SpotifyHudModule.LyricMotion motion = module == null ? SpotifyHudModule.LyricMotion.WAVE : module.lyricMotion();
        SpotifyHudModule.LyricHighlight highlight = module == null ? SpotifyHudModule.LyricHighlight.OFF : module.lyricHighlight();
        LyricView view = this.planLyrics(lyrics, lyricsWanted, online, position, lines, motion, highlight, t, w - pad * 2.0f, s, dt, nowMs);
        float playing = track.playing() ? 1.0f : 0.0f;
        this.lyricPlaying += (playing - this.lyricPlaying) * (1.0f - (float)Math.exp(-8.0f * dt));
        float lyricAlpha = 0.7f + 0.3f * this.lyricPlaying;
        SpotifyCard card = new SpotifyCard(track, sample, art, header, pad - capTop, column, timeline, elapsed, (String)remaining, progress, progressBar, view, lyricAlpha);
        float height = pad * 2.0f + header + view.height();
        return new SpotifyPlan(t, SpotifyLook.CARD, SpotifyGlyph.NONE, title, artist, titleW, artistW, offsets[0], offsets[1], loopGap, scroll, card, SpotifyColors.of(module), w, Math.round(height));
    }

    private void drawSpotifyCard(OverlayCanvas dl, SpotifyPlan plan, float x, float y, float w, float h, float s, float a, boolean guard) {
        LyricView lyrics;
        SpotifyCard card = plan.card();
        Type t = plan.type();
        float pad = 10.0f * s;
        boolean playing = card.track().playing();
        float left = x + pad;
        if (card.art()) {
            this.drawAlbumArt(dl, card, left, y + pad, card.header(), s, a);
            left += card.header() + 10.0f * s;
        } else {
            this.releaseSpotifyArt();
        }
        float column = card.column();
        float top = y + card.textTop();
        CardChrome.text(dl, t.label(), t.labelPx(), left, Math.round(top), CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a), playing ? "NOW PLAYING" : "PAUSED", guard);
        SpotifyPanel.marqueeLine(dl, t.text(), t.textPx(), left, Math.round(top += (float)t.labelPx() + 3.0f * s), column, plan.titleW(), plan.titleOffset(), plan.loopGap(), plan.scroll(), plan.title(), CardChrome.scaleAlpha(plan.colors().title(), a), guard);
        top += (float)t.textPx();
        if (!plan.artist().isEmpty()) {
            SpotifyPanel.marqueeLine(dl, t.label(), t.labelPx(), left, Math.round(top += 1.0f * s), column, plan.artistW(), plan.artistOffset(), plan.loopGap(), plan.scroll(), plan.artist(), CardChrome.scaleAlpha(plan.colors().artist(), a), guard);
            top += (float)t.labelPx();
        }
        top = Math.round(top + 7.0f * s);
        this.drawTransport(dl, t, card, left, top, column, s, a, guard);
        if (card.progressBar()) {
            float barTop = Math.round(top + (float)t.labelPx() + 6.0f * s);
            this.drawProgress(dl, left, barTop, column, SpotifyPanel.progressHeight(s), card.progress(), playing, a);
        }
        if ((lyrics = card.lyrics()).height() > 0.5f) {
            float blockTop = Math.round(y + pad + card.header() + 12.0f * s);
            this.drawLyrics(dl, t, lyrics, plan.colors(), x + pad, blockTop, w - pad * 2.0f, y + h - Math.max(1.0f, s), s, a * card.lyricAlpha(), guard);
        }
    }

    private void drawAlbumArt(OverlayCanvas dl, SpotifyCard card, float x, float y, float size, float s, float a) {
        CardTexture texture;
        float r = Math.min(4.0f, 4.0f) * s;
        if (card.sample()) {
            this.releaseSpotifyArt();
        }
        CardTexture cardTexture = texture = card.sample() ? null : this.spotifyArtTexture(card.track().art());
        if (texture != null) {
            dl.addImage(texture.id(), x, y, x + size, y + size, CardChrome.scaleAlpha(-1, a));
        } else {
            dl.addRectFilled(x, y, x + size, y + size, CardChrome.scaleAlpha(0x12FFFFFF, a), r);
            float bar = Math.max(2.0f, (float)Math.round(size * 0.07f));
            float gap = Math.max(2.0f, (float)Math.round(bar * 0.8f));
            float base = Math.round(y + size * 0.62f);
            float l = Math.round(x + (size - bar * 3.0f - gap * 2.0f) * 0.5f);
            int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a * 0.45f);
            float[] heights = new float[]{0.2f, 0.34f, 0.26f};
            for (int i = 0; i < heights.length; ++i) {
                float bx = l + (float)i * (bar + gap);
                dl.addRectFilled(bx, base - (float)Math.round(size * heights[i]), bx + bar, base, color, 1.0f);
            }
        }
        dl.addRect(x, y, x + size, y + size, CardChrome.scaleAlpha(0x17FFFFFF, a), r, 0, 1.0f);
    }

    private void drawTransport(OverlayCanvas dl, Type t, SpotifyCard card, float left, float top, float column, float s, float a, boolean guard) {
        float margin;
        float spacing;
        float play;
        float skip;
        float cluster;
        float centre;
        float elapsedW = 0.0f;
        float remainingW = 0.0f;
        if (card.timeline()) {
            elapsedW = CardChrome.tabularWidth(t.label(), t.labelPx(), card.elapsed());
            remainingW = CardChrome.tabularWidth(t.label(), t.labelPx(), card.remaining());
            int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a);
            CardChrome.tabular(dl, t.label(), t.labelPx(), left, top, color, card.elapsed(), guard);
            CardChrome.tabular(dl, t.label(), t.labelPx(), Math.round(left + column - remainingW), top, color, card.remaining(), guard);
        }
        if (!((centre = left + column * 0.5f) - (cluster = (skip = Math.max(4.0f, (float)Math.round(8.0f * s))) * 2.0f + (play = Math.max(5.0f, (float)Math.round(10.0f * s))) + (spacing = (float)Math.round(14.0f * s)) * 2.0f) * 0.5f < left + elapsedW + (margin = 8.0f * s)) && !(centre + cluster * 0.5f > left + column - remainingW - margin)) {
            float centerY = top + (this.capTop(t.label(), t.labelPx()) + CardChrome.ascent(t.label(), t.labelPx())) * 0.5f;
            float playLeft = Math.round(centre - play * 0.5f);
            float[] lefts = new float[]{playLeft - spacing - skip, playLeft, playLeft + play + spacing};
            float[] sizes = new float[]{skip, play, skip};
            float reach = Math.round(3.0f * s);
            float mouseX = this.mouseX;
            float mouseY = this.mouseY;
            for (int i = 0; i < 3; ++i) {
                boolean hovered;
                float l = lefts[i] - reach;
                float r = lefts[i] + sizes[i] + reach;
                float tp = (float)Math.round(centerY - sizes[i] * 0.5f) - reach;
                float b = (float)Math.round(centerY + sizes[i] * 0.5f) + reach;
                this.spotifyButtons[i * 4] = l;
                this.spotifyButtons[i * 4 + 1] = tp;
                this.spotifyButtons[i * 4 + 2] = r;
                this.spotifyButtons[i * 4 + 3] = b;
                boolean bl = hovered = this.chatInput && mouseX >= l && mouseX <= r && mouseY >= tp && mouseY <= b;
                if (hovered) {
                    dl.addRectFilled(l, tp, r, b, CardChrome.scaleAlpha(0x26FFFFFF, a), (r - l) * 0.5f);
                }
                int color = CardChrome.scaleAlpha(!hovered && i != 1 ? CardChrome.TEXT_LABEL : CardChrome.TEXT_NAME, a);
                if (i == 1) {
                    SpotifyPanel.drawPlayPauseGlyph(dl, lefts[i], centerY, play, card.track().playing(), color);
                    continue;
                }
                SpotifyPanel.drawSkipGlyph(dl, lefts[i], centerY, skip, i == 2, color);
            }
            this.spotifyButtonsDrawn = true;
        }
    }

    private int spotifyButtonAt(float x, float y) {
        for (int i = 0; i < SPOTIFY_BUTTONS.length; ++i) {
            if (!(x >= this.spotifyButtons[i * 4]) || !(x <= this.spotifyButtons[i * 4 + 2]) || !(y >= this.spotifyButtons[i * 4 + 1]) || !(y <= this.spotifyButtons[i * 4 + 3])) continue;
            return i;
        }
        return -1;
    }

    private static void drawSkipGlyph(OverlayCanvas dl, float left, float centerY, float size, boolean next, int color) {
        float top = Math.round(centerY - size * 0.5f);
        float bottom = top + size;
        float mid = (top + bottom) * 0.5f;
        float bar = Math.max(1.0f, (float)Math.round(size * 0.2f));
        if (next) {
            dl.addTriangleFilled(left, top, left + size - bar, mid, left, bottom, color);
            dl.addRectFilled(left + size - bar, top, left + size, bottom, color);
        } else {
            dl.addRectFilled(left, top, left + bar, bottom, color);
            dl.addTriangleFilled(left + size, top, left + size, bottom, left + bar, mid, color);
        }
    }

    private static void drawPlayPauseGlyph(OverlayCanvas dl, float left, float centerY, float size, boolean playing, int color) {
        float top = Math.round(centerY - size * 0.5f);
        float bottom = top + size;
        if (playing) {
            float bar = Math.max(2.0f, (float)Math.round(size * 0.3f));
            float gap = Math.max(2.0f, (float)Math.round(size * 0.24f));
            float l = Math.round(left + (size - bar * 2.0f - gap) * 0.5f);
            dl.addRectFilled(l, top, l + bar, bottom, color, 1.0f);
            dl.addRectFilled(l + bar + gap, top, l + bar * 2.0f + gap, bottom, color, 1.0f);
        } else {
            float inset = Math.round(size * 0.08f);
            dl.addTriangleFilled(left + inset, top, left + inset + size * 0.86f, (top + bottom) * 0.5f, left + inset, bottom, color);
        }
    }

    private void drawProgress(OverlayCanvas dl, float left, float top, float width, float height, float progress, boolean playing, float a) {
        float r = height * 0.5f;
        dl.addRectFilled(left, top, left + width, top + height, CardChrome.scaleAlpha(0x1AFFFFFF, a), r);
        float fill = Math.round(width * SpotifyPanel.clamp01(progress));
        if (!(fill < 1.0f)) {
            dl.addRectFilled(left, top, left + fill, top + height, CardChrome.scaleAlpha(playing ? CardChrome.accent() : CardChrome.TEXT_LABEL, a), r);
        }
    }

    private static float progressHeight(float s) {
        return Math.max(1.0f, (float)Math.round(2.0f * s));
    }

    private LyricView planLyrics(Lyrics lyrics, boolean wanted, boolean online, long position, int lines, SpotifyHudModule.LyricMotion motion, SpotifyHudModule.LyricHighlight highlight, Type t, float innerW, float s, float dt, long nowMs) {
        List<LyricItem> items = List.of();
        String status = null;
        float content = -1.0f;
        boolean live = false;
        if (!wanted) {
            this.lyricLoadingSince = 0L;
            content = 0.0f;
        } else {
            if (lyrics == null || lyrics.status() != Lyrics.Status.LOADING) {
                this.lyricLoadingSince = 0L;
            } else if (this.lyricLoadingSince == 0L) {
                this.lyricLoadingSince = nowMs;
            }
            if (lyrics != null) {
                switch (lyrics.status()) {
                    case LOADING: {
                        status = nowMs - this.lyricLoadingSince < 700L ? null : "Loading lyrics";
                        break;
                    }
                    case SYNCED: {
                        this.prepareLyrics(lyrics, t.text());
                        if (this.lyricReadable) {
                            items = new ArrayList(16);
                            float viewH = this.lyricViewHeight(lyrics.synced(), lines, t, innerW, s);
                            this.syncedItems(items, lyrics.synced(), position, viewH, motion, highlight, t, innerW, s, dt);
                            content = viewH - ((float)t.textPx() - CardChrome.ascent(t.text(), t.textPx()));
                            live = true;
                            break;
                        }
                        status = "Lyrics in a script the HUD font lacks";
                        break;
                    }
                    case PLAIN: {
                        status = "No synced lyrics";
                        break;
                    }
                    case INSTRUMENTAL: {
                        status = "Instrumental";
                        break;
                    }
                    case NOT_FOUND: {
                        status = online ? "No lyrics found" : "No saved lyrics";
                        break;
                    }
                    case UNAVAILABLE: {
                        status = "Lyrics unavailable";
                    }
                }
            }
        }
        if (status != null) {
            content = (float)Math.round((float)(t.textPx() - t.labelPx()) * 0.5f) + CardChrome.ascent(t.label(), t.labelPx());
        }
        if (live) {
            this.lyricLastItems = items;
            this.lyricLeaveItems = null;
        } else {
            items = this.leavingItems(status == null, t, s, dt);
        }
        float target = content < 0.0f ? this.lyricBlockH : (content > 0.0f ? 12.0f * s + (float)Math.round(content) : 0.0f);
        return new LyricView(items, status, this.easeLyricBlock(target, dt));
    }

    private List<LyricItem> leavingItems(boolean quiet, Type t, float s, float dt) {
        this.lyricSource = null;
        if (!this.lyricLastItems.isEmpty()) {
            this.lyricLeaveItems = this.lyricLastItems;
            this.lyricLeave = 0.0f;
            this.lyricLastItems = List.of();
        }
        if (this.lyricLeaveItems == null) {
            return List.of();
        }
        this.lyricLeave = Math.min(1.0f, this.lyricLeave + dt / 0.32f);
        if (quiet && !(this.lyricLeave >= 1.0f)) {
            ArrayList<LyricItem> items = new ArrayList<LyricItem>(this.lyricLeaveItems.size());
            SpotifyPanel.addFaded(items, this.lyricLeaveItems, SpotifyPanel.smooth(this.lyricLeave), 8.0f * s);
            return items;
        }
        this.lyricLeaveItems = null;
        return List.of();
    }

    private static void addFaded(List<LyricItem> items, List<LyricItem> frozen, float fade, float rise) {
        for (LyricItem item : frozen) {
            float alpha = item.alpha() * (1.0f - fade);
            if (alpha <= 0.004f) continue;
            items.add(new LyricItem(item.text(), item.top() - rise * fade, item.highlight(), alpha, item.progress(), item.fill()));
        }
    }

    private void syncedItems(List<LyricItem> items, SyncedLyrics synced, long position, float viewH, SpotifyHudModule.LyricMotion motion, SpotifyHudModule.LyricHighlight highlight, Type t, float innerW, float s, float dt) {
        float stagger;
        float lineGap = 6.0f * s;
        int currentLast = synced.indexAt(position + 200L);
        int currentFirst = synced.firstWithTimeOf(currentLast);
        int start = Math.max(0, currentFirst);
        if (synced != this.lyricSource) {
            this.lyricSource = synced;
            this.lyricStart = start;
            this.lyricShifts.clear();
            this.lyricClock = 0.0f;
            this.lyricFadeItems = null;
            this.lyricFade = 1.0f;
            this.lyricShown = new float[synced.size() * 2];
            this.lyricBright = new float[synced.size()];
            this.lyricBrightLo = 0;
            this.lyricBrightHi = -1;
            for (int i = Math.max(0, currentFirst); i <= currentLast; ++i) {
                this.lyricBright[i] = 1.0f;
            }
            if (currentLast >= 0) {
                this.lyricBrightLo = Math.max(0, currentFirst);
                this.lyricBrightHi = currentLast;
            }
        }
        this.lyricClock += dt;
        this.lyricBreath = (this.lyricBreath + dt) % 1.6f;
        this.lyricCurrentFirst = currentFirst;
        this.lyricCurrentLast = currentLast;
        float f = stagger = motion == SpotifyHudModule.LyricMotion.WAVE ? 0.045f : 0.0f;
        if (start != this.lyricStart) {
            float distance;
            float f2 = distance = start > this.lyricStart ? this.restDistance(this.lyricStart, start, t, innerW, s) : -1.0f;
            if (motion != SpotifyHudModule.LyricMotion.FADE && distance > 0.0f && distance <= viewH + lineGap + 0.5f) {
                if (this.lyricShifts.size() == 8) {
                    this.lyricShifts.remove(0);
                }
                this.lyricShifts.add(new LyricShift(distance, this.lyricClock, start, stagger));
            } else {
                this.lyricFadeItems = this.lyricLastItems;
                this.lyricFade = 0.0f;
                this.lyricShifts.clear();
                this.showView(synced, start, viewH, t, innerW, s);
            }
            this.lyricStart = start;
        }
        this.updateBrightness(currentFirst, currentLast, dt);
        this.lyricFade = Math.min(1.0f, this.lyricFade + dt / 0.32f);
        if (this.lyricFade >= 1.0f) {
            this.lyricFadeItems = null;
        }
        float settled = 1.37f;
        this.lyricShifts.removeIf(shift -> this.lyricClock - shift.at() > settled);
        if (this.lyricShifts.isEmpty()) {
            this.lyricClock = 0.0f;
        }
        float rise = 8.0f * s;
        if (this.lyricFadeItems != null) {
            float fade = SpotifyPanel.smooth(this.lyricFade);
            SpotifyPanel.addFaded(items, this.lyricFadeItems, fade, rise);
            this.addLyricView(items, synced, this.lyricStart, fade, rise * (1.0f - fade), viewH, position, highlight, t, innerW, s, dt);
        } else {
            this.addLyricView(items, synced, this.lyricStart, 1.0f, 0.0f, viewH, position, highlight, t, innerW, s, dt);
        }
    }

    private void addLyricView(List<LyricItem> items, SyncedLyrics synced, int from, float alpha, float rise, float viewH, long position, SpotifyHudModule.LyricHighlight mode, Type t, float innerW, float s, float dt) {
        float progress;
        float highlight;
        String[] rows;
        CardFont font = t.text();
        int px = t.textPx();
        float rowGap = 6.0f * s;
        float lineGap = 6.0f * s;
        float slot = (float)px + lineGap;
        boolean karaoke = mode == SpotifyHudModule.LyricHighlight.KARAOKE;
        float restTop = 0.0f;
        for (int i = from; i < synced.size() && restTop <= viewH + slot * 2.0f; restTop += SpotifyPanel.lineHeight(rows, px, rowGap) + lineGap, ++i) {
            rows = this.rowsFor(i, font, px, innerW);
            int count = Math.max(1, rows.length);
            float lineTop = restTop + this.lyricOffset(i) + rise;
            float depth = 1.0f - 0.39999998f * SpotifyPanel.clamp01(lineTop / slot - 1.0f);
            highlight = this.lyricGlow(i, mode);
            progress = rows.length == 0 ? this.breakProgress(synced, i, position) : 1.0f;
            for (int r = 0; r < count; ++r) {
                float rowRest = restTop + (float)r * ((float)px + rowGap);
                float shown = this.updateShown(i, r, rowRest + (float)px <= viewH + 0.5f, dt);
                float top = lineTop + (float)r * ((float)px + rowGap);
                float rowAlpha = alpha * depth * SpotifyPanel.smooth(shown) * SpotifyPanel.topFade(top, s) * SpotifyPanel.bottomFade(top + (float)px - viewH, px);
                if (rowAlpha <= 0.004f) continue;
                float fill = karaoke && rows.length > 0 ? this.rowFill(synced, i, rows, r, font, px, position) : 0.0f;
                items.add(new LyricItem(rows.length == 0 ? null : rows[r], top, highlight, rowAlpha, progress, fill));
            }
        }
        float lineRest = 0.0f;
        for (int i = from - 1; i >= 0; --i) {
            String[] rows2 = this.rowsFor(i, font, px, innerW);
            int count = Math.max(1, rows2.length);
            float lineTop = (lineRest -= SpotifyPanel.lineHeight(rows2, px, rowGap) + lineGap) + this.lyricOffset(i) + rise;
            if (SpotifyPanel.topFade(lineTop + (float)(count - 1) * ((float)px + rowGap), s) <= 0.0f) break;
            highlight = this.lyricGlow(i, mode);
            progress = rows2.length == 0 ? this.breakProgress(synced, i, position) : 1.0f;
            for (int rx = 0; rx < count; ++rx) {
                float top = lineTop + (float)rx * ((float)px + rowGap);
                float rowAlpha = alpha * SpotifyPanel.smooth(this.lyricShown[i * 2 + rx]) * SpotifyPanel.topFade(top, s);
                if (rowAlpha <= 0.004f) continue;
                float fill = karaoke && rows2.length > 0 ? this.rowFill(synced, i, rows2, rx, font, px, position) : 0.0f;
                items.add(new LyricItem(rows2.length == 0 ? null : rows2[rx], top, highlight, rowAlpha, progress, fill));
            }
        }
    }

    private float lyricOffset(int index) {
        float offset = 0.0f;
        for (int k = 0; k < this.lyricShifts.size(); ++k) {
            LyricShift shift = this.lyricShifts.get(k);
            float delay = (float)Math.max(0, Math.min(6, index - shift.start())) * shift.stagger();
            float time = this.lyricClock - shift.at() - delay;
            offset += time <= 0.0f ? shift.distance() : shift.distance() * SpotifyPanel.springLeft(time);
        }
        return offset;
    }

    private static float springLeft(float time) {
        float wt = 13.0f * time;
        return (1.0f + wt) * (float)Math.exp(-wt);
    }

    private static float topFade(float top, float s) {
        return top >= 0.0f ? 1.0f : SpotifyPanel.smooth(1.0f + top / (12.0f * s * 0.75f));
    }

    private static float bottomFade(float below, int px) {
        return below <= 0.5f ? 1.0f : SpotifyPanel.smooth(1.0f - below / ((float)px * 0.6f));
    }

    private float updateShown(int index, int row, boolean fits, float dt) {
        float shown;
        int k = index * 2 + row;
        float step = dt / 0.3f;
        this.lyricShown[k] = shown = fits ? Math.min(1.0f, this.lyricShown[k] + step) : Math.max(0.0f, this.lyricShown[k] - step);
        return shown;
    }

    private void showView(SyncedLyrics synced, int from, float viewH, Type t, float innerW, float s) {
        String[] rows;
        Arrays.fill(this.lyricShown, 0.0f);
        int px = t.textPx();
        float rowGap = 6.0f * s;
        float restTop = 0.0f;
        for (int i = from; i < synced.size() && restTop < viewH; restTop += SpotifyPanel.lineHeight(rows, px, rowGap) + 6.0f * s, ++i) {
            rows = this.rowsFor(i, t.text(), px, innerW);
            int count = Math.max(1, rows.length);
            for (int r = 0; r < count; ++r) {
                if (!(restTop + (float)r * ((float)px + rowGap) + (float)px <= viewH + 0.5f)) continue;
                this.lyricShown[i * 2 + r] = 1.0f;
            }
        }
    }

    private float restDistance(int from, int to, Type t, float innerW, float s) {
        float rowGap = 6.0f * s;
        float distance = 0.0f;
        for (int i = from; i < to; ++i) {
            distance += SpotifyPanel.lineHeight(this.rowsFor(i, t.text(), t.textPx(), innerW), t.textPx(), rowGap) + 6.0f * s;
        }
        return distance;
    }

    private void updateBrightness(int currentFirst, int currentLast, float dt) {
        int lo = this.lyricBrightHi >= 0 ? this.lyricBrightLo : Integer.MAX_VALUE;
        int hi = this.lyricBrightHi;
        if (currentLast >= 0) {
            lo = Math.min(lo, Math.max(0, currentFirst));
            hi = Math.max(hi, currentLast);
        }
        float step = dt / 0.35f;
        int lit = Integer.MAX_VALUE;
        int last = -1;
        for (int i = Math.max(0, lo); i <= hi && i < this.lyricBright.length; ++i) {
            float bright;
            boolean sung = i >= currentFirst && i <= currentLast;
            this.lyricBright[i] = bright = sung ? Math.min(1.0f, this.lyricBright[i] + step) : Math.max(0.0f, this.lyricBright[i] - step);
            if (!(bright > 0.0f)) continue;
            lit = Math.min(lit, i);
            last = i;
        }
        this.lyricBrightLo = last < 0 ? 0 : lit;
        this.lyricBrightHi = last;
    }

    private float lyricGlow(int index, SpotifyHudModule.LyricHighlight mode) {
        if (mode != SpotifyHudModule.LyricHighlight.OFF && index >= 0 && index < this.lyricCurrentFirst) {
            return 1.0f;
        }
        return index >= 0 && index < this.lyricBright.length ? SpotifyPanel.smooth(this.lyricBright[index]) : 0.0f;
    }

    private float breakProgress(SyncedLyrics synced, int index, long position) {
        if (index >= this.lyricCurrentFirst && index <= this.lyricCurrentLast) {
            int next = synced.lastWithTimeOf(index) + 1;
            if (next >= synced.size()) {
                return 1.0f;
            }
            long begin = synced.line(index).timeMs();
            long end = synced.line(next).timeMs() - 200L;
            return end <= begin ? 1.0f : SpotifyPanel.clamp01((float)(position - begin) / (float)(end - begin));
        }
        return 1.0f;
    }

    private float rowFill(SyncedLyrics synced, int index, String[] rows, int row, CardFont font, int px, long position) {
        float lit;
        if (index < this.lyricCurrentFirst) {
            return Float.POSITIVE_INFINITY;
        }
        if (index > this.lyricCurrentLast) {
            return 0.0f;
        }
        String text = rows[row];
        int rowStart = 0;
        if (row > 0) {
            String line = this.lyricTexts[index];
            for (rowStart = rows[0].length(); rowStart < line.length() && line.charAt(rowStart) == ' '; ++rowStart) {
            }
        }
        if ((lit = this.litChars(synced, index, position) - (float)rowStart) <= 0.0f) {
            return 0.0f;
        }
        if (lit >= (float)text.length()) {
            return Float.POSITIVE_INFINITY;
        }
        int whole = (int)lit;
        if (whole > 0 && Character.isLowSurrogate(text.charAt(whole))) {
            --whole;
        }
        int next = whole + Character.charCount(text.codePointAt(whole));
        float x0 = CardChrome.width(font, px, text.substring(0, whole));
        float x1 = CardChrome.width(font, px, text.substring(0, next));
        return x0 + (x1 - x0) * SpotifyPanel.clamp01((lit - (float)whole) / (float)(next - whole));
    }

    private float litChars(SyncedLyrics synced, int index, long position) {
        LyricWords words = this.lyricWords[index];
        if (words == null) {
            this.timeWords(synced, synced.firstWithTimeOf(index));
            words = this.lyricWords[index];
        }
        float lit = 0.0f;
        long[] start = words.start();
        for (int k = 0; k < start.length && position >= start[k]; ++k) {
            float swept = SpotifyPanel.smooth((float)(position - start[k]) / (float)words.sweep()[k]);
            lit = (float)words.from()[k] + (float)(words.to()[k] - words.from()[k]) * swept;
        }
        return lit;
    }

    private void timeWords(SyncedLyrics synced, int first) {
        int last = synced.lastWithTimeOf(first);
        int next = last + 1;
        long begin = synced.line(first).timeMs();
        long handover = next < synced.size() ? synced.line(next).timeMs() - 200L : Long.MAX_VALUE;
        int letters = 0;
        for (int i = first; i <= last; ++i) {
            letters += SpotifyPanel.letterCount(this.lyricTexts[i]);
        }
        long window = next < synced.size() ? Math.min(handover - begin, (long)Math.round((float)letters * this.lyricCharMs * 2.0f)) : (long)Math.round((float)letters * this.lyricCharMs);
        window = Math.max(0L, window);
        int done = 0;
        for (int i = first; i <= last; ++i) {
            String text = this.lyricTexts[i];
            int count = text.isEmpty() ? 0 : text.length() - SpotifyPanel.letterCount(text) + 1;
            int[] from = new int[count];
            int[] to = new int[count];
            long[] start = new long[count];
            long[] sweep = new long[count];
            List<Long> timed = synced.line(i).wordTimes();
            boolean own = timed.size() == count;
            int c = 0;
            for (int k = 0; k < count; ++k) {
                from[k] = c;
                if ((c = text.indexOf(32, c)) < 0) {
                    c = text.length();
                }
                to[k] = c++;
                long at = own ? timed.get(k) : begin + window * (long)done / (long)letters;
                long until = own ? (k + 1 < count ? timed.get(k + 1) : Math.min(handover, at + 180L)) : begin + window * (long)(done += to[k] - from[k]) / (long)letters;
                start[k] = at;
                sweep[k] = Math.max(1L, Math.min(180L, until - at));
            }
            this.lyricWords[i] = new LyricWords(from, to, start, sweep);
        }
    }

    private float lyricPace(SyncedLyrics synced) {
        float[] paces = new float[synced.size()];
        int n = 0;
        int first = 0;
        while (first < synced.size()) {
            int last = synced.lastWithTimeOf(first);
            int letters = 0;
            for (int i = first; i <= last; ++i) {
                letters += SpotifyPanel.letterCount(this.lyricTexts[i]);
            }
            if (letters > 0 && last + 1 < synced.size()) {
                paces[n++] = (float)(synced.line(last + 1).timeMs() - synced.line(first).timeMs()) / (float)letters;
            }
            first = last + 1;
        }
        if (n == 0) {
            return 90.0f;
        }
        Arrays.sort(paces, 0, n);
        return paces[n / 2];
    }

    private static int letterCount(String text) {
        int letters = text.length();
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) != ' ') continue;
            --letters;
        }
        return letters;
    }

    private float lyricViewHeight(SyncedLyrics synced, int lines, Type t, float innerW, float s) {
        int px = t.textPx();
        this.prepareRows(px, innerW);
        if (this.lyricViewH >= 0.0f && this.lyricViewLines == lines && this.lyricViewScale == s) {
            return this.lyricViewH;
        }
        int slots = lines;
        int start = 0;
        while (start < synced.size()) {
            int rows = 0;
            int last = synced.lastWithTimeOf(start);
            for (int i = start; i <= last; ++i) {
                rows += Math.max(1, this.rowsFor(i, t.text(), px, innerW).length);
            }
            slots = Math.max(slots, Math.min(4, rows));
            start = synced.lastWithTimeOf(start) + 1;
        }
        this.lyricViewH = (float)(slots * px) + (float)(slots - 1) * 6.0f * s;
        this.lyricViewLines = lines;
        this.lyricViewScale = s;
        return this.lyricViewH;
    }

    private static float lineHeight(String[] rows, int px, float rowGap) {
        return rows.length <= 1 ? (float)px : (float)(rows.length * px) + (float)(rows.length - 1) * rowGap;
    }

    private float easeLyricBlock(float target, float dt) {
        float diff = target - this.lyricBlockH;
        if (Math.abs(diff) < 0.5f) {
            this.lyricBlockH = target;
        } else {
            float speed = diff > 0.0f ? 14.0f : 9.0f;
            this.lyricBlockH += diff * (1.0f - (float)Math.exp(-speed * dt));
        }
        return this.lyricBlockH;
    }

    private void prepareLyrics(Lyrics lyrics, CardFont font) {
        if (lyrics != this.lyricPrepared) {
            this.lyricPrepared = lyrics;
            this.lyricViewH = -1.0f;
            SyncedLyrics synced = lyrics.synced();
            this.lyricTexts = new String[synced.size()];
            this.lyricRows = new String[synced.size()][];
            this.lyricWords = new LyricWords[synced.size()];
            int letters = 0;
            int missing = 0;
            IntPredicate drawable = cpx -> this.hudHasGlyph(font, cpx);
            for (int i = 0; i < synced.size(); ++i) {
                String text;
                this.lyricTexts[i] = text = DisplayText.fold(synced.line(i).text(), drawable);
                int k = 0;
                while (k < text.length()) {
                    int cp = text.codePointAt(k);
                    k += Character.charCount(cp);
                    if (!Character.isLetter(cp)) continue;
                    ++letters;
                    if (cp < 128 || this.hudHasGlyph(font, cp)) continue;
                    ++missing;
                }
            }
            this.lyricReadable = (float)missing <= (float)letters * 0.25f;
            this.lyricCharMs = this.lyricPace(synced);
        }
    }

    private boolean hudHasGlyph(CardFont font, int cp) {
        return cp <= 65535;
    }

    private void prepareRows(int px, float maxW) {
        int widthKey = (int)maxW;
        if (px != this.lyricRowsPx || widthKey != this.lyricRowsWidth) {
            Arrays.fill((Object[])this.lyricRows, null);
            this.lyricRowsPx = px;
            this.lyricRowsWidth = widthKey;
            this.lyricViewH = -1.0f;
        }
    }

    private String[] rowsFor(int index, CardFont font, int px, float maxW) {
        this.prepareRows(px, maxW);
        String[] rows = this.lyricRows[index];
        if (rows == null) {
            rows = SpotifyPanel.wrapLyric(font, px, this.lyricTexts[index], this.lyricRowsWidth);
            this.lyricRows[index] = rows;
        }
        return rows;
    }

    private static String[] wrapLyric(CardFont font, int px, String text, float maxW) {
        String[] stringArray;
        float firstW;
        if (text.isEmpty()) {
            return NO_ROWS;
        }
        if (CardChrome.width(font, px, text) <= maxW) {
            return new String[]{text};
        }
        int best = -1;
        float bestW = Float.MAX_VALUE;
        int i = text.indexOf(32);
        while (i > 0 && !((firstW = CardChrome.width(font, px, text.substring(0, i).stripTrailing())) > maxW)) {
            float secondW = CardChrome.width(font, px, text.substring(i + 1).stripLeading());
            if (secondW <= maxW && Math.max(firstW, secondW) < bestW) {
                bestW = Math.max(firstW, secondW);
                best = i;
            }
            i = text.indexOf(32, i + 1);
        }
        if (best > 0) {
            return new String[]{text.substring(0, best).stripTrailing(), text.substring(best + 1).stripLeading()};
        }
        int cut = SpotifyPanel.fitPrefix(font, px, text, maxW);
        String first = text.substring(0, cut).stripTrailing();
        String rest = text.substring(cut).stripLeading();
        if (rest.isEmpty()) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = first;
        } else {
            String[] stringArray3 = new String[2];
            stringArray3[0] = first;
            stringArray = stringArray3;
            stringArray3[1] = CardChrome.width(font, px, rest) <= maxW ? rest : SpotifyPanel.ellipsize(font, px, rest, maxW);
        }
        return stringArray;
    }

    private static int fitPrefix(CardFont font, int px, String text, float maxW) {
        int lastBreak = -1;
        int i = text.indexOf(32);
        while (i > 0 && !(CardChrome.width(font, px, text.substring(0, i)) > maxW)) {
            lastBreak = i;
            i = text.indexOf(32, i + 1);
        }
        if (lastBreak > 0) {
            return lastBreak + 1;
        }
        int lo = 1;
        int hi = text.length();
        while (lo < hi) {
            int mid = lo + hi + 1 >>> 1;
            if (CardChrome.width(font, px, text.substring(0, mid)) <= maxW) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        if (lo > 1 && lo < text.length() && Character.isHighSurrogate(text.charAt(lo - 1))) {
            --lo;
        }
        return lo;
    }

    private void drawLyrics(OverlayCanvas dl, Type t, LyricView view, SpotifyColors colors, float left, float top, float width, float bottom, float s, float a, boolean guard) {
        float clipTop = top - 12.0f * s + Math.max(1.0f, 2.0f * s);
        if (!(bottom <= clipTop) && !(a <= 0.001f)) {
            float clipLeft = (float)Math.floor(left - 2.0f);
            float clipRight = (float)Math.ceil(left + width + 2.0f);
            dl.pushClipRect(clipLeft, clipTop, clipRight, bottom, true);
            if (view.status() != null) {
                float statusTop = Math.round(top + (float)(t.textPx() - t.labelPx()) * 0.5f);
                CardChrome.text(dl, t.label(), t.labelPx(), left, statusTop, CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a * 0.85f), view.status(), guard);
            }
            for (LyricItem item : view.items()) {
                float alpha = a * item.alpha();
                if (alpha <= 0.004f) continue;
                float rowTop = top + item.top();
                if (item.text() == null) {
                    float centerY = rowTop + (this.capTop(t.text(), t.textPx()) + CardChrome.ascent(t.text(), t.textPx())) * 0.5f;
                    this.drawLyricBreak(dl, colors, left, centerY, s, item.highlight(), item.progress(), alpha);
                    continue;
                }
                int color = CardChrome.scaleAlpha(CardChrome.lerpColor(colors.upcoming(), colors.lyric(), item.highlight()), alpha);
                int lit = CardChrome.scaleAlpha(colors.lit(), alpha);
                float edge = Math.min(clipRight, (float)Math.floor(left) + (float)Math.round(item.fill()));
                if (item.fill() <= 0.0f) {
                    CardChrome.text(dl, t.text(), t.textPx(), left, Math.round(rowTop), color, item.text(), guard);
                    continue;
                }
                if (edge >= clipRight) {
                    CardChrome.text(dl, t.text(), t.textPx(), left, Math.round(rowTop), lit, item.text(), guard);
                    continue;
                }
                dl.pushClipRect(clipLeft, clipTop, edge, bottom, true);
                CardChrome.text(dl, t.text(), t.textPx(), left, Math.round(rowTop), lit, item.text(), guard);
                dl.popClipRect();
                dl.pushClipRect(edge, clipTop, clipRight, bottom, true);
                CardChrome.text(dl, t.text(), t.textPx(), left, Math.round(rowTop), color, item.text(), guard);
                dl.popClipRect();
            }
            dl.popClipRect();
        }
    }

    private void drawLyricBreak(OverlayCanvas dl, SpotifyColors colors, float left, float centerY, float s, float highlight, float progress, float alpha) {
        float r = Math.max(1.0f, 1.6f * s);
        float step = Math.max(4.0f, (float)Math.round(6.0f * s));
        float breath = 0.5f - 0.5f * (float)Math.cos((double)(this.lyricBreath / 1.6f) * 2.0 * Math.PI);
        for (int i = 0; i < 3; ++i) {
            float lit = SpotifyPanel.smooth(progress * 3.0f - (float)i) * highlight;
            int color = CardChrome.scaleAlpha(CardChrome.lerpColor(colors.upcoming(), colors.lit(), lit), alpha);
            float radius = r * (1.0f + 0.3f * lit + 0.12f * breath * highlight);
            dl.addCircleFilled(left + r + (float)i * step, centerY, radius, color, 12);
        }
    }

    private void foldTrack(NowPlaying now, CardFont font) {
        if (now != this.spotifyFolded) {
            this.spotifyFolded = now;
            IntPredicate drawable = cp -> this.hudHasGlyph(font, cp);
            String title = DisplayText.fold(now.title(), drawable);
            this.spotifyTitle = title.isEmpty() ? now.title() : title;
            this.spotifyArtist = DisplayText.fold(now.artist(), drawable);
        }
    }

    private float[] marquee(String title, String artist, float titleW, float artistW, float avail, boolean scroll, boolean advancing, float loopGap, float s, float dt) {
        if (!title.equals(this.marqueeTitle) || !artist.equals(this.marqueeArtist)) {
            this.marqueeTitle = title;
            this.marqueeArtist = artist;
            this.marqueeTime = 0.0f;
        }
        float titleOffset = 0.0f;
        float artistOffset = 0.0f;
        float longest = Math.max(titleW > avail ? titleW : 0.0f, artistW > avail ? artistW : 0.0f);
        if (scroll && longest > 0.0f) {
            float speed = 30.0f * s;
            float period = 2.4f + (longest + loopGap) / speed;
            this.marqueeTime %= period;
            if (advancing || this.marqueeTime > 2.4f) {
                this.marqueeTime = (this.marqueeTime + dt) % period;
            }
            float travelled = Math.max(0.0f, this.marqueeTime - 2.4f) * speed;
            if (titleW > avail) {
                titleOffset = Math.min(travelled, titleW + loopGap);
            }
            if (artistW > avail) {
                artistOffset = Math.min(travelled, artistW + loopGap);
            }
        } else {
            this.marqueeTime = 0.0f;
        }
        return new float[]{titleOffset, artistOffset};
    }

    private static void marqueeLine(OverlayCanvas dl, CardFont font, int px, float x, float top, float avail, float textW, float offset, float loopGap, boolean scroll, String text, int color, boolean guard) {
        if (textW <= avail + 0.5f) {
            CardChrome.text(dl, font, px, x, top, color, text, guard);
        } else if (!scroll) {
            CardChrome.text(dl, font, px, x, top, color, SpotifyPanel.ellipsize(font, px, text, avail), guard);
        } else {
            dl.pushClipRect(x, top - 2.0f, x + avail, top + (float)px + 3.0f, true);
            CardChrome.text(dl, font, px, x - offset, top, color, text, guard);
            if (offset > 0.0f) {
                CardChrome.text(dl, font, px, x - offset + textW + loopGap, top, color, text, guard);
            }
            dl.popClipRect();
        }
    }

    private static String ellipsize(CardFont font, int px, String text, float maxW) {
        float room = maxW - CardChrome.width(font, px, "...");
        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = lo + hi + 1 >>> 1;
            if (CardChrome.width(font, px, text.substring(0, mid)) <= room) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        return text.substring(0, lo).stripTrailing() + "...";
    }

    private static String formatSeconds(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long hours = seconds / 3600L;
        long minutes = seconds / 60L % 60L;
        StringBuilder out = new StringBuilder(8);
        if (hours > 0L) {
            out.append(hours).append(':').append(minutes < 10L ? "0" : "");
        }
        out.append(minutes).append(':');
        if (seconds % 60L < 10L) {
            out.append('0');
        }
        return out.append(seconds % 60L).toString();
    }

    private float capTop(CardFont font, int px) {
        return Math.max(0.0f, (float)Math.round((float)px * (font == null ? 0.2f : font.capTop())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CardTexture spotifyArtTexture(AlbumArt art) {
        if (art == this.spotifyArt) {
            return this.spotifyArtTexture;
        }
        this.releaseSpotifyArt();
        if (art == null) {
            return null;
        }
        this.spotifyArt = art;
        ByteBuffer pixels = MemoryUtil.memAlloc((int)art.byteSize());
        try {
            art.copyTo(pixels);
            pixels.flip();
            this.spotifyArtTexture = CardTexture.fromPixels(pixels, art.size());
        }
        catch (RuntimeException var7) {
            this.spotifyArtTexture = null;
        }
        finally {
            MemoryUtil.memFree((Buffer)pixels);
        }
        return this.spotifyArtTexture;
    }

    private void releaseSpotifyArt() {
        if (this.spotifyArtTexture != null) {
            this.spotifyArtTexture.release();
        }
        this.spotifyArtTexture = null;
        this.spotifyArt = null;
    }

    private static float smooth(float value) {
        float t = SpotifyPanel.clamp01(value);
        return t * t * (3.0f - 2.0f * t);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private record LyricWords(int[] from, int[] to, long[] start, long[] sweep) {
    }

    private record SpotifyPlan(Type type, SpotifyLook look, SpotifyGlyph glyph, String title, String artist, float titleW, float artistW, float titleOffset, float artistOffset, float loopGap, boolean scroll, SpotifyCard card, SpotifyColors colors, float width, float height) {
    }

    private record Type(CardFont value, int valuePx, CardFont text, int textPx, CardFont label, int labelPx) {
        static Type of(float scale) {
            return new Type(CardFont.TEXT, CardChrome.px(18.0f, scale), CardFont.TEXT, CardChrome.px(17.0f, scale), CardFont.LABEL, CardChrome.px(14.0f, scale));
        }
    }

    private static enum SpotifyLook {
        HIDDEN,
        IDLE,
        COMPACT,
        CARD;

    }

    private static enum SpotifyGlyph {
        NONE,
        PLAYING,
        PAUSED;

    }

    private record SpotifyColors(int title, int artist, int lyric, int upcoming, int lit) {
        static final SpotifyColors DEFAULT = new SpotifyColors(CardChrome.TEXT_NAME, CardChrome.TEXT_LABEL, CardChrome.TEXT_VALUE, CardChrome.TEXT_LABEL, CardChrome.TEXT_VALUE);

        static SpotifyColors of(SpotifyHudModule module) {
            if (module == null) {
                return DEFAULT;
            }
            SpotifyHudModule.LyricHighlight mode = module.lyricHighlight();
            int lyric = GuiPalette.abgr(module.lyricsColor());
            int highlight = GuiPalette.abgr(module.highlightColor());
            return new SpotifyColors(GuiPalette.abgr(module.titleColor()), GuiPalette.abgr(module.artistColor()), mode == SpotifyHudModule.LyricHighlight.LINE ? highlight : lyric, GuiPalette.abgr(module.upcomingLyricsColor()), mode == SpotifyHudModule.LyricHighlight.OFF ? lyric : highlight);
        }
    }

    private record SpotifyCard(NowPlaying track, boolean sample, boolean art, float header, float textTop, float column, boolean timeline, String elapsed, String remaining, float progress, boolean progressBar, LyricView lyrics, float lyricAlpha) {
    }

    private record LyricView(List<LyricItem> items, String status, float height) {
    }

    private record LyricItem(String text, float top, float highlight, float alpha, float progress, float fill) {
    }

    private record LyricShift(float distance, float at, int start, float stagger) {
    }
}

