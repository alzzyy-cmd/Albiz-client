// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.AcStatusCard;
import net.albiz.client.port.soberui.gui.Avatars;
import net.albiz.client.port.soberui.gui.Backdrop;
import net.albiz.client.port.soberui.gui.BrandTextures;
import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.Fonts;
import net.albiz.client.port.soberui.gui.HudLayout;
import net.albiz.client.port.soberui.gui.HudMetrics;
import net.albiz.client.port.soberui.gui.HudPalette;
import net.albiz.client.port.soberui.gui.Icons;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.ModuleMenu;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.RegionMap;
import net.albiz.client.port.soberui.gui.Smooth;
import net.albiz.client.port.soberui.gui.SoberScreen;
import net.albiz.client.port.soberui.gui.SpotifyPanel;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.gui.UiSounds;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.modules.AntiCheat;
import net.albiz.client.port.soberui.modules.ChunkFinder;
import net.albiz.client.port.soberui.modules.Freecam;
import net.albiz.client.port.soberui.modules.Freelook;
import net.albiz.client.port.soberui.modules.Hotkeys;
import net.albiz.client.port.soberui.modules.Hud;
import net.albiz.client.port.soberui.modules.InventoryHud;
import net.albiz.client.port.soberui.modules.MediaHider;
import net.albiz.client.module.impl.client.SpotifyHudModule;
import net.albiz.client.port.soberui.modules.StaffLive;
import net.albiz.client.port.soberui.modules.SusChunk;
import net.albiz.client.port.soberui.modules.Zoom;
import net.albiz.client.port.soberui.util.StaffList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_11719;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_5321;
import net.minecraft.class_5348;
import net.minecraft.class_640;
import net.minecraft.class_642;
import net.minecraft.class_742;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public final class HudRenderer {
    private static final int TEXT = -723723;
    private static final int MUTED = -5329226;
    private static final Map<Module, Float> PROGRESS = new HashMap<Module, Float>();
    private static final Map<String, Float> KEYS = new HashMap<String, Float>();
    private static final int EDGE = 5;
    private static final int SPACING = 4;
    private static final int SNAP = 4;
    private static final EnumMap<HudLayout.Element, HudLayout.Box> SHOWN = new EnumMap(HudLayout.Element.class);
    private static final class_2960 TOGIS = class_2960.method_60655((String)"albiz", (String)"dynamic/togis_keyboard");
    private static final int TOGIS_W = 186;
    private static final int TOGIS_H = 141;
    private static boolean togisLoaded;
    private static boolean togisReady;
    private static final int[][] TOGIS_KEYS;
    private static final Map<Integer, Float> TOGIS_GLOW;
    private static long togisFrame;
    private static long lastKeysFrame;
    private static long lastFrame;
    private static HudLayout.Element dragging;
    private static int grabX;
    private static int grabY;
    private static int guideX;
    private static int guideY;
    private static float hudScale = 1f;
    private static float tickProgress;
    private static float blueprint;
    private static final class_2583 HUD_FONT;
    private static final String HUD_FILE = "geist-semibold.ttf";
    private static final float HUD_EM = 8.6f;
    private static final float HEADER_EM = 7.2f;
    private static final float HEADER_TRACK = 0.18f;
    private static final float HEADER_LOGO = 7.0f;
    private static final float BRAND_LOGO = 9.5f;
    private static final String BRAND_LEFT = "ALBIZ";
    private static final String BRAND_RIGHT = "CLIENT";
    private static final int BRAND_SOBER = -5329226;
    private static final float BRAND_EM = 7.2f;
    private static final float BRAND_TRACK = 0.3f;
    private static class_1799 overworldIcon;
    private static class_1799 netherIcon;
    private static class_1799 endIcon;
    private static final DateTimeFormatter CLOCK;
    private static final Set<String> PURPLE_STAR;
    private static final int ARMOR_W = 128;
    private static final int ARMOR_H = 49;
    private static final class_1304[] ARMOR_SLOTS;
    private static final float ARMOR_EM = 7.0f;

    private HudRenderer() {
    }

    private static void blueprint(class_332 context, class_310 mc, float scale, float alpha) {
        class_1041 window = mc.method_22683();
        float factor = window.method_4495();
        int width = window.method_4489();
        int height = window.method_4506();
        int cell = Math.max(8, Math.round(18.0f * scale * factor));
        int cx = width / 2;
        int cy = height / 2;
        int minor = MenuKit.alpha(616555519, alpha);
        int major = MenuKit.alpha(1388307455, alpha);
        int center = MenuKit.alpha(-1929379841, alpha);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.scale(1.0f / factor, 1.0f / factor);
        context.method_25294(0, 0, width, height, MenuKit.alpha(772435199, alpha));
        int k = -(cx / cell);
        while (cx + k * cell < width) {
            int x = cx + k * cell;
            context.method_25294(x, 0, x + 1, height, k == 0 ? center : (k % 4 == 0 ? major : minor));
            ++k;
        }
        k = -(cy / cell);
        while (cy + k * cell < height) {
            int y = cy + k * cell;
            context.method_25294(0, y, width, y + 1, k == 0 ? center : (k % 4 == 0 ? major : minor));
            ++k;
        }
        matrices.popMatrix();
    }

    private static Hud hud() {
        return SoberClient.MODULES.get(Hud.class);
    }

    public static float uiScale(class_332 context) {
        return Math.max(0.3f, (float)context.method_51443() / 540.0f);
    }

    public static void render(class_332 context, class_9779 tickCounter) {
        Zoom zoom;
        float zoomAlpha;
        Freecam freecam;
        float camAlpha;
        float hintAlpha;
        float scale;
        class_310 mc = class_310.method_1551();
        float delta = HudRenderer.frameDelta();
        tickProgress = tickCounter.method_60637(true);
        Backdrop.capture(mc);
        MediaHider.coverHotbar(context);
        if (mc.field_1755 instanceof ModuleMenu) {
            return;
        }
        Hud hud = HudRenderer.hud();
        boolean cards = hud != null && hud.isActive() && !mc.field_1690.field_1842 && mc.field_1724 != null && !mc.method_53526().method_53536();
        boolean standalone = !cards && hud != null && !mc.field_1690.field_1842 && mc.field_1724 != null && !mc.method_53526().method_53536();
        boolean editing = hud != null && mc.field_1724 != null && mc.field_1755 instanceof net.albiz.client.gui.HudEditorScreen;
        if(editing){cards=true;standalone=false;}
        hudScale = scale = HudRenderer.uiScale(context);
        int sw = Math.round((float)context.method_51421() / scale);
        int sh = Math.round((float)context.method_51443() / scale);
        if (!editing) {
            HudRenderer.endDrag();
        }
        if ((blueprint = MenuKit.approach(blueprint, editing ? 1.0f : 0.0f, 12.0f, delta)) > 0.01f) {
            HudRenderer.blueprint(context, mc, scale, blueprint);
        }
        context.method_51448().pushMatrix();
        context.method_51448().scale(scale, scale);
        if (editing) {
            HudRenderer.updateDrag(mc, hud, sw, sh);
        }
        if (cards) {
            HudRenderer.renderCards(context, hud, sw, sh, editing, delta, false);
        } else if (standalone) {
            HudRenderer.renderCards(context, hud, sw, sh, editing, delta, true);
        } else {
            SHOWN.clear();
        }
        boolean hintsShown = !mc.field_1690.field_1842 && mc.field_1724 != null;
        Freelook freelook = SoberClient.MODULES.get(Freelook.class);
        float f = hintAlpha = freelook == null || !hintsShown ? 0.0f : freelook.hint();
        if (hintAlpha > 0.01f) {
            HudRenderer.cameraHint(context, sw, hintAlpha, "FREELOOK", freelook.getInfoString());
        }
        float f2 = camAlpha = (freecam = SoberClient.MODULES.get(Freecam.class)) == null || !hintsShown ? 0.0f : freecam.hint();
        if (camAlpha > 0.01f) {
            HudRenderer.cameraHint(context, sw, camAlpha, "FREECAM", freecam.getInfoString());
        }
        float f3 = zoomAlpha = (zoom = SoberClient.MODULES.get(Zoom.class)) == null || !hintsShown || hintAlpha > 0.01f || camAlpha > 0.01f ? 0.0f : zoom.hint();
        if (zoomAlpha > 0.01f) {
            HudRenderer.cameraHint(context, sw, zoomAlpha, "ZOOM", zoom.getInfoString());
        }
        if (editing) {
            context.method_71048();
            HudRenderer.drawEditing(context, mc, sw, sh);
        }
        context.method_51448().popMatrix();
        if (!mc.field_1690.field_1842) {
            context.method_71048();
            Notifications.render(context);
        }
    }

    private static boolean chatEditing(class_310 mc) {
        Hud hud = HudRenderer.hud();
        return hud != null && mc.field_1724 != null && mc.field_1755 instanceof net.albiz.client.gui.HudEditorScreen;
    }

    private static HudLayout.Element shownAt(double x, double y) {
        HudLayout.Element[] values = HudLayout.Element.values();
        for (int i = values.length - 1; i >= 0; --i) {
            HudLayout.Box box = SHOWN.get((Object)values[i]);
            if (box == null || !box.contains(x, y)) continue;
            return values[i];
        }
        return null;
    }

    private static boolean inChatField(class_310 mc, double guiY) {
        return false; // Editor is a dedicated screen, not a chat input field.
    }

    private static HudLayout.Element selected;
    private static int editorW,editorH;
    public static boolean chatPress(double guiX, double guiY, int button) {
        HudLayout.Box spotify;
        class_310 mc = class_310.method_1551();
        if (!HudRenderer.chatEditing(mc) || HudRenderer.inChatField(mc, guiY)) {
            return false;
        }
        double x = guiX / (double)hudScale;
        double y = guiY / (double)hudScale;
        if (settingsPress(x,y,button)) return true;
        HudLayout.Element element = HudRenderer.shownAt(x, y);
        if (element == null) {
            return false;
        }
        selected = element;
        if (button == 1) {
            HudLayout.resetScale(element);
            HudLayout.save();
            UiSounds.click();
            return true;
        }
        if (button != 0) {
            return false;
        }
        HudLayout.Box box = SHOWN.get((Object)element);
        editorDown=true;editorPointer(guiX,guiY);
        dragging = element;
        grabX = (int)Math.round(x) - box.x();
        grabY = (int)Math.round(y) - box.y();
        UiSounds.click();
        return true;
    }

    public static boolean chatScroll(double guiX, double guiY, double amount) {
        class_310 mc = class_310.method_1551();
        if (!HudRenderer.chatEditing(mc) || HudRenderer.inChatField(mc, guiY) || amount == 0.0) {
            return false;
        }
        if (settingsPress(guiX / hudScale,guiY / hudScale,-1)) return true;
        HudLayout.Element element = HudRenderer.shownAt(guiX / (double)hudScale, guiY / (double)hudScale);
        if (element == null) {
            return false;
        }
        HudLayout.scale(element, 0.05f * (float)Math.signum(amount));
        HudLayout.save();
        UiSounds.key(0.3f);
        return true;
    }

    private static boolean editorDown;
    private static double editorMouseX,editorMouseY;
    public static void editorPointer(double x,double y){editorMouseX=x;editorMouseY=y;}
    public static void editorRelease(){editorDown=false;endDrag();}
    private static void endDrag() {
        if (dragging == null) {
            return;
        }
        dragging = null;
        guideX = -1;
        guideY = -1;
        HudLayout.save();
    }

    private static void updateDrag(class_310 mc, Hud hud, int sw, int sh) {
        if (dragging == null) {
            return;
        }
        if (!editorDown) {
            HudRenderer.endDrag();
            return;
        }
        HudLayout.Box box = SHOWN.get((Object)dragging);
        if (box == null) {
            return;
        }
        int x = (int)Math.round(editorMouseX / (double)hudScale) - grabX;
        int y = (int)Math.round(editorMouseY / (double)hudScale) - grabY;
        int[] snapped = HudRenderer.snap(dragging, x, y, box.width(), box.height(), sw, sh, SHOWN, hud.snapping.get() != false && !mc.method_74187());
        HudLayout.move(dragging, snapped[0], snapped[1], sw, sh, box.width(), box.height());
    }

    private static int[] snap(HudLayout.Element element, int x, int y, int w, int h, int sw, int sh, Map<HudLayout.Element, HudLayout.Box> others, boolean enabled) {
        int d;
        guideX = -1;
        guideY = -1;
        x = Math.max(0, Math.min(sw - w, x));
        y = Math.max(0, Math.min(sh - h, y));
        if (!enabled) {
            return new int[]{x, y};
        }
        ArrayList<int[]> xs = new ArrayList<int[]>();
        ArrayList<int[]> ys = new ArrayList<int[]>();
        xs.add(new int[]{0, 0});
        xs.add(new int[]{5, 5});
        xs.add(new int[]{sw - w, sw - 1});
        xs.add(new int[]{sw - 5 - w, sw - 5});
        xs.add(new int[]{Math.round((float)(sw - w) / 2.0f), sw / 2});
        ys.add(new int[]{0, 0});
        ys.add(new int[]{5, 5});
        ys.add(new int[]{sh - h, sh - 1});
        ys.add(new int[]{sh - 5 - h, sh - 5});
        ys.add(new int[]{Math.round((float)(sh - h) / 2.0f), sh / 2});
        for (Map.Entry<HudLayout.Element, HudLayout.Box> entry : others.entrySet()) {
            if (entry.getKey() == element) continue;
            HudLayout.Box o = entry.getValue();
            xs.add(new int[]{o.x(), o.x()});
            xs.add(new int[]{o.x() + o.width() - w, o.x() + o.width()});
            xs.add(new int[]{o.x() + o.width() + 4, o.x() + o.width() + 4});
            xs.add(new int[]{o.x() - 4 - w, o.x() - 4});
            xs.add(new int[]{o.x() + (o.width() - w) / 2, o.x() + o.width() / 2});
            ys.add(new int[]{o.y(), o.y()});
            ys.add(new int[]{o.y() + o.height() - h, o.y() + o.height()});
            ys.add(new int[]{o.y() + o.height() + 4, o.y() + o.height() + 4});
            ys.add(new int[]{o.y() - 4 - h, o.y() - 4});
            ys.add(new int[]{o.y() + (o.height() - h) / 2, o.y() + o.height() / 2});
        }
        int bestX = x;
        int bestY = y;
        int dx = 5;
        int dy = 5;
        for (int[] c : xs) {
            d = Math.abs(c[0] - x);
            if (d >= dx || c[0] < 0 || c[0] > sw - w) continue;
            dx = d;
            bestX = c[0];
            guideX = c[1];
        }
        for (int[] c : ys) {
            d = Math.abs(c[0] - y);
            if (d >= dy || c[0] < 0 || c[0] > sh - h) continue;
            dy = d;
            bestY = c[0];
            guideY = c[1];
        }
        return new int[]{bestX, bestY};
    }

    private static void guides(class_332 context, int sw, int sh) {
        int thickness = Math.max(1, Math.round(1.0f / hudScale));
        if (guideX >= 0) {
            context.method_25294(guideX, 0, guideX + thickness, sh, -1930103563);
        }
        if (guideY >= 0) {
            context.method_25294(0, guideY, sw, guideY + thickness, -1930103563);
        }
    }

    private static void hint(class_332 context, String value, double mx, double my, int sw, int sh) {
        int w = HudRenderer.textWidth(value) + 16;
        int x = (int)Math.round(mx) + 12;
        int y = (int)Math.round(my) + 14;
        if (x + w > sw - 2) {
            x = (int)Math.round(mx) - 12 - w;
        }
        if (y + 18 > sh - 16) {
            y = (int)Math.round(my) - 14 - 18;
        }
        x = Math.max(2, x);
        y = Math.max(2, y);
        HudRenderer.widget(context, x, y, w, 18, 6, 1.0f);
        HudRenderer.text(context, class_310.method_1551().field_1772, value, x + 8, y + 5, -723723);
    }

    private static void drawEditing(class_332 context, class_310 mc, int sw, int sh) {
        HudLayout.Element active;
        double guiY = editorMouseY;
        double mx = editorMouseX / (double)hudScale;
        double my = guiY / (double)hudScale;
        editorW=sw;editorH=sh;
        settingsPane(context,sw,sh);
        active = dragging != null ? dragging : selected != null ? selected : (HudRenderer.inChatField(mc, guiY) ? null : HudRenderer.shownAt(mx, my));
        if (dragging != null) {
            HudRenderer.guides(context, sw, sh);
        }
        if (active == null) {
            return;
        }
        HudLayout.Box box = SHOWN.get((Object)active);
        if (box == null) {
            return;
        }
        Smooth.outline(context, box.x() - 2, box.y() - 2, box.width() + 4, box.height() + 4, 11, dragging != null ? -723723 : -1711999755);
        String label = dragging != null ? "Shift for free move" : Math.round(box.scale() * 100.0f) + "%  /  Scroll to resize  /  Right click reset";
        HudRenderer.hint(context, label, mx, my, sw, sh);
    }

    private static void editorText(class_332 c,String text,int x,int y){CrispText.draw(c,net.albiz.client.i18n.AlbizI18n.caption(text),"geist-semibold.ttf",x,y,9f,0f,0xFFEDE7ED);}
    private static void editorButton(class_332 c,String text,int x,int y,int w){Smooth.rect(c,x,y,w,17,4,0xE5352736);editorText(c,text,x+5,y+4);}
    private static void settingsPane(class_332 c,int sw,int sh){
        int w=Math.min(160,sw/2),x=sw-w-5,y=34;
        Smooth.rect(c,x,y,w,238,8,0xF01C151E);
        editorText(c,"HUD EDITOR",x+9,y+10);
        if(selected==null){editorText(c,"Click a HUD element",x+9,y+30);editorText(c,"Drag to move",x+9,y+48);return;}
        editorText(c,selected.name().replace('_',' '),x+9,y+29);
        var b=SHOWN.get(selected);
        editorText(c,"Scale: "+Math.round(HudLayout.scaleOf(selected)*100)+"%",x+9,y+49);
        editorButton(c,"-",x+9,y+64,28);editorButton(c,"+",x+43,y+64,28);editorButton(c,"Reset size",x+77,y+64,w-86);
        editorText(c,b==null?"Position unavailable":"X: "+b.x()+"   Y: "+b.y(),x+9,y+91);
        editorButton(c,"Left",x+9,y+105,54);editorButton(c,"Right",x+69,y+105,w-78);
        editorButton(c,"Up",x+9,y+126,54);editorButton(c,"Down",x+69,y+126,w-78);
        editorButton(c,HudLayout.hidden(selected)?"Visibility: Hidden":"Visibility: Shown",x+9,y+149,w-18);
        editorButton(c,"Reset position & size",x+9,y+172,w-18);
        if(selected!=HudLayout.Element.AC_OFF)editorButton(c,enabled(hud(),selected)?"Element: Enabled":"Element: Disabled",x+9,y+195,w-18);
        editorText(c,"Escape: Back",x+9,y+220);
    }
    private static boolean settingsPress(double mx,double my,int button){
        int w=Math.min(160,editorW/2),x=editorW-w-5,y=34;
        if(mx<x||mx>=x+w||my<y||my>=y+238)return false;
        if(button!=0||selected==null)return true;
        int row=(int)my-y;var b=SHOWN.get(selected);
        if(row>=64&&row<81){if(mx<x+39)HudLayout.scale(selected,-.05f);else if(mx<x+75)HudLayout.scale(selected,.05f);else HudLayout.resetScale(selected);}
        else if(b!=null&&row>=105&&row<143){int dx=0,dy=0;if(row<122)dx=mx<x+66?-1:1;else dy=mx<x+66?-1:1;HudLayout.move(selected,b.x()+dx,b.y()+dy,editorW,editorH,b.width(),b.height());}
        else if(row>=149&&row<166)HudLayout.toggleHidden(selected);
        else if(row>=172&&row<189)HudLayout.resetElement(selected);
        else if(row>=195&&row<212)toggleSelected();
        HudLayout.save();return true;
    }

    private static void toggleSelected(){
        var h=hud();if(h==null||selected==null)return;
        net.albiz.client.port.soberui.setting.Setting<Boolean> setting=switch(selected){
            case WATERMARK -> h.watermark;case FPS -> h.fps;case BPS -> h.bps;case TPS -> h.tps;case XYZ -> h.coordinates;case RADAR -> h.radar;case MODULE_LIST -> h.moduleList;case KEYSTROKES -> h.keystrokes;case ARMOR -> h.armorHud;case TOGIS -> h.togisKeyboard;case STAFF_LIST -> h.staffList;default -> null;
        };
        if(setting!=null){boolean desired=!enabled(h,selected);setting.set(desired);if(selected==HudLayout.Element.WATERMARK&&!desired)h.infoLine.set(false);if(selected==HudLayout.Element.STAFF_LIST&&!desired){var m=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.misc.StaffHudModule.class);if(m!=null)m.setEnabled(false);}if(selected==HudLayout.Element.FPS||selected==HudLayout.Element.BPS||selected==HudLayout.Element.TPS)h.infoLine.set(false);}
        else if(selected==HudLayout.Element.INVENTORY){var m=SoberClient.MODULES.get(InventoryHud.class);if(m!=null)m.setEnabled(!m.isActive());}
        else if(selected==HudLayout.Element.SPOTIFY){var m=net.albiz.client.module.ModuleRegistry.get(SpotifyHudModule.class);if(m!=null)m.setEnabled(!m.isEnabled());}
        else if(selected==HudLayout.Element.REGION){var m=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.client.RegionMapModule.class);if(m!=null)m.setEnabled(!m.isEnabled());}
        else if(selected==HudLayout.Element.HOTKEYS){var m=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.client.HotkeysModule.class);if(m!=null)m.setEnabled(!m.isEnabled());}
        else if(selected==HudLayout.Element.STAFF_LIVE){var m=net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.misc.StaffLiveModule.class);if(m!=null)m.setEnabled(!m.isEnabled());}
        net.albiz.client.port.soberui.SoberUi.save();
    }

    private static float frameDelta() {
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (float)(now - lastFrame) / 1.0E9f);
        lastFrame = now;
        return delta;
    }

    private static void renderCards(class_332 context, Hud hud, int sw, int sh, boolean ghost, float delta, boolean standaloneOnly) {
        class_310 mc = class_310.method_1551();
        class_327 font = mc.field_1772;
        List<Module> modules = HudRenderer.modules(delta);
        SHOWN.clear();
        for (HudLayout.Element element : HudLayout.Element.values()) {
            if(element==HudLayout.Element.SPOTIFY && net.albiz.client.platform.PlatformInfo.isMobile())continue;
            if (ghost && element==HudLayout.Element.AC_OFF && AntiCheat.card()==null)continue;
            if (standaloneOnly && element != HudLayout.Element.REGION && element != HudLayout.Element.SPOTIFY && element != HudLayout.Element.HOTKEYS && element != HudLayout.Element.AC_OFF || !ghost && !HudRenderer.enabled(hud, element) || element == HudLayout.Element.MODULE_LIST && modules.isEmpty() && !ghost) continue;
            if (!ghost && HudLayout.hidden(element)) continue;
            int baseW = HudRenderer.baseWidth(element, mc, modules);
            int baseH = HudRenderer.baseHeight(element, modules, mc, hud);
            HudLayout.Box box = HudLayout.box(element, sw, sh, baseW, baseH);
            SHOWN.put(element, box);
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)box.x(), (float)box.y());
            context.method_51448().scale(box.scale(), box.scale());
            HudRenderer.drawElement(context, mc, hud, font, element, modules, ghost);
            context.method_51448().popMatrix();
        }
    }

    private static boolean enabled(Hud hud, HudLayout.Element element) {
        return switch (element) {
            default -> throw new MatchException(null, null);
            case HudLayout.Element.WATERMARK -> {
                if (hud.watermark.get().booleanValue() || hud.infoLine.get().booleanValue()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.FPS -> {
                if (hud.fps.get().booleanValue() && !hud.infoLine.get().booleanValue()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.BPS -> {
                if (hud.bps.get().booleanValue() && !hud.infoLine.get().booleanValue()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.TPS -> {
                if (hud.tps.get().booleanValue() && !hud.infoLine.get().booleanValue()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.XYZ -> hud.coordinates.get();
            case HudLayout.Element.RADAR -> hud.radar.get();
            case HudLayout.Element.MODULE_LIST -> hud.moduleList.get();
            case HudLayout.Element.STAFF_LIST -> hud.staffList.get() || net.albiz.client.module.impl.misc.StaffHudModule.shouldShowHud();
            case HudLayout.Element.KEYSTROKES -> hud.keystrokes.get();
            case HudLayout.Element.INVENTORY -> {
                InventoryHud inventory = SoberClient.MODULES.get(InventoryHud.class);
                if (inventory != null && inventory.isActive()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.REGION -> {
                net.albiz.client.port.soberui.modules.RegionMap map = SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.RegionMap.class);
                if (map != null && map.isActive()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.SPOTIFY -> {
                SpotifyHudModule player = SoberClient.MODULES.get(SpotifyHudModule.class);
                if (player != null && player.isEnabled()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.STAFF_LIVE -> {
                StaffLive live = SoberClient.MODULES.get(StaffLive.class);
                if (live != null && live.isActive() && live.hud.get().booleanValue()) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.TOGIS -> hud.togisKeyboard.get();
            case HudLayout.Element.HOTKEYS -> Hotkeys.shouldShowHud();
            case HudLayout.Element.AC_OFF -> {
                if (AntiCheat.card() != null) {
                    yield true;
                }
                yield false;
            }
            case HudLayout.Element.ARMOR -> hud.armorHud.get();
        };
    }

    private static void togis(class_332 context) {
        if (!togisLoaded) {
            togisLoaded = true;
            try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655((String)"albiz", (String)"textures/hud/togis_keyboard.png"));){
                Smooth.register(TOGIS, class_1011.method_4309((InputStream)stream));
                togisReady = true;
            }
            catch (Exception e) {
                SoberClient.LOG.error("Could not load the Togis keyboard picture", (Throwable)e);
            }
        }
        if (!togisReady) {
            return;
        }
        context.method_25293(class_10799.field_56883, TOGIS, 0, 0, 0.0f, 0.0f, 186, 141, 371, 282, 371, 282, -1);
        class_310 mc = class_310.method_1551();
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (float)(now - togisFrame) / 1.0E9f);
        togisFrame = now;
        float k = 0.5013477f;
        int accent = Theme.accent();
        for (int[] key : TOGIS_KEYS) {
            boolean down = class_3675.method_15987((class_1041)mc.method_22683(), (int)key[0]);
            float shown = TOGIS_GLOW.getOrDefault(key[0], Float.valueOf(0.0f)).floatValue();
            if ((shown += ((down ? 1.0f : 0.0f) - shown) * (1.0f - (float)Math.exp(-delta * (down ? 32.0f : 9.0f)))) < 0.01f) {
                shown = 0.0f;
            }
            TOGIS_GLOW.put(key[0], Float.valueOf(shown));
            if (shown <= 0.0f) continue;
            int x = Math.round((float)key[1] * k);
            int y = Math.round((float)key[2] * k);
            int w = Math.round((float)key[3] * k);
            int h = Math.round((float)key[4] * k);
            int grow = Math.round(1.5f * shown);
            Smooth.glow(context, x + w / 2, y + h / 2, Math.max(w, h), Math.round(112.0f * shown) << 24 | accent & 0xFFFFFF);
            Smooth.rect(context, x - grow, y - grow, w + grow * 2, h + grow * 2, 4, Math.round(85.0f * shown) << 24 | accent & 0xFFFFFF);
            Smooth.outline(context, x - grow, y - grow, w + grow * 2, h + grow * 2, 4, Math.round(224.0f * shown) << 24 | 0xFFFFFF);
        }
    }

    private static int[][] togisKeys() {
        int i;
        ArrayList<int[]> keys = new ArrayList<int[]>();
        int[] top = new int[]{81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91};
        int[] middle = new int[]{65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39};
        int[] columns = new int[]{10, 45, 80, 117, 152, 190, 225, 260, 295, 328, 362};
        for (i = 0; i < top.length; ++i) {
            keys.add(new int[]{top[i], Math.max(0, columns[i] - 16), 44, 32, 46});
        }
        for (i = 0; i < middle.length; ++i) {
            keys.add(new int[]{middle[i], Math.max(0, columns[i] - 16), 106, 32, 46});
        }
        int[] bottom = new int[]{90, 88, 67, 86, 66, 78, 77, 44, 46, 47};
        int[] bottomColumns = new int[]{8, 47, 80, 112, 146, 178, 210, 243, 276, 308};
        for (int i2 = 0; i2 < bottom.length; ++i2) {
            keys.add(new int[]{bottom[i2], Math.max(0, bottomColumns[i2] - 15), 168, 30, 44});
        }
        int[] toolbar = new int[]{15, 58, 100, 145, 185, 228, 275, 315, 358};
        for (int i3 = 0; i3 < toolbar.length; ++i3) {
            keys.add(new int[]{49 + i3, Math.max(0, toolbar[i3] - 17), 2, 34, 28});
        }
        keys.add(new int[]{259, 330, 170, 40, 40});
        keys.add(new int[]{257, 314, 224, 56, 48});
        keys.add(new int[]{32, 92, 226, 206, 52});
        keys.add(new int[]{263, 148, 236, 24, 32});
        keys.add(new int[]{262, 222, 236, 24, 32});
        keys.add(new int[]{340, 0, 224, 40, 50});
        keys.add(new int[]{341, 42, 224, 44, 50});
        return (int[][])keys.toArray(new int[0][]);
    }

    private static class_2561 hudText(String value) {
        return class_2561.method_43470((String)value).method_10862(HUD_FONT);
    }

    private static int textWidth(String value) {
        if (CrispText.canDisplay(value, HUD_FILE)) {
            return (int)Math.ceil(CrispText.width(value, HUD_FILE, 8.6f, 0.1f));
        }
        return class_310.method_1551().field_1772.method_27525((class_5348)HudRenderer.hudText(value));
    }

    private static void text(class_332 context, class_327 font, String value, int x, int y, int color) {
        if (CrispText.canDisplay(value, HUD_FILE)) {
            CrispText.draw(context, value, HUD_FILE, x, (float)y + 7.45f, 8.6f, 0.1f, color);
        } else {
            context.method_51439(font, HudRenderer.hudText(value), x, y, color, false);
        }
    }

    static float labelWidth(String label) {
        return CrispText.width(label, HUD_FILE, 7.2f, 0.18f);
    }

    static void label(class_332 context, String label, float x, float y, int color) {
        CrispText.draw(context, label, HUD_FILE, x, y + 7.6f, 7.2f, 0.18f, color);
    }

    static float headerWidth(String label) {
        return HudRenderer.logoWidth(7.0f) + 4.0f + HudRenderer.labelWidth(label);
    }

    static float logoWidth(float height) {
        BrandTextures.Sprite orbit = BrandTextures.get("orbit_mark");
        return orbit == null ? height * 2.0f : height * (float)orbit.width() / (float)orbit.height();
    }

    static float logo(class_332 context, float x, float y, float height, int color) {
        float width = HudRenderer.logoWidth(height);
        BrandTextures.Sprite orbit = BrandTextures.fit("orbit_mark", Math.max(width, height) * Smooth.pixelsPerUnit(context));
        if (orbit != null) {
            SoberScreen.image(context, orbit, x, y, width, height, color);
        }
        return width;
    }

    static void header(class_332 context, String label, float x, float y) {
        float w = HudRenderer.logo(context, x, y + 1.7f, 7.0f, -1);
        HudRenderer.label(context, label, x + w + 4.0f, y, -2565922);
    }

    static void glass(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        Smooth.liquid(context, x, y, w, h, r, opacity, false, false);
        Smooth.rect(context, x, y, w, h, Math.max(0, Math.min(r, Math.min(w, h) / 2)), MenuKit.alpha(-2146430958, opacity));
    }

    static void panel(class_332 context, int x, int y, int w, int h, int edge) {
        HudRenderer.widget(context, x, y, w, h, Math.min(9, Math.min(w, h) / 2), 1.0f);
        if (edge != 0) {
            Smooth.rect(context, x + 1, y + 5, 2, Math.max(2, h - 10), 1, MenuKit.lerp(edge, -1, 0.3f));
        }
    }

    static void widget(class_332 context, int x, int y, int w, int h, int r, float opacity) {
        Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(HudPalette.widget(), opacity));
        Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(0x1CFFFFFF, opacity));
    }

    private static int edgeColor() {
        return 0;
    }

    private static int pillWidth(String label, String value) {
        return 20 + Math.round(HudRenderer.labelWidth(label)) + HudRenderer.textWidth(value);
    }

    private static float brandPart(String part) {
        return CrispText.width(part, HUD_FILE, 7.2f, 0.3f);
    }

    private static int brandWidth() {
        return Math.round(HudRenderer.brandPart(BRAND_LEFT) + HudRenderer.brandPart(BRAND_RIGHT));
    }

    private static int brandBlock() {
        return Math.round(HudRenderer.logoWidth(9.5f) + 5.0f) + HudRenderer.brandWidth();
    }

    private static void brand(class_332 ctx, int x) {
        float w = HudRenderer.logo(ctx, x, 5.25f, 9.5f, -1);
        float tx = x + Math.round(w + 5.0f);
        CrispText.draw(ctx, BRAND_LEFT, HUD_FILE, tx, 12.6f, 7.2f, 0.3f, -723723);
        CrispText.draw(ctx, BRAND_RIGHT, HUD_FILE, tx + HudRenderer.brandPart(BRAND_LEFT), 12.6f, 7.2f, 0.3f, -5329226);
    }

    private static void cameraHint(class_332 ctx, int sw, float alpha, String title, String value) {
        float lw = HudRenderer.labelWidth(title) - 1.296f;
        float vw = CrispText.width(value, HUD_FILE, 8.6f, 0.1f);
        int w = Math.round(17.0f + lw + 9.0f + vw + 11.0f);
        int h = 18;
        int x = (sw - w) / 2;
        int y = HudRenderer.topClearance(x, x + w) + Math.round((1.0f - alpha) * -6.0f);
        Smooth.rect(ctx, x, y, w, h, 3, MenuKit.alpha(HudPalette.card(), alpha));
        Smooth.outline(ctx, x, y, w, h, 3, MenuKit.alpha(-13750733, alpha));
        Smooth.circle(ctx, x + 10, y + 9, 2, MenuKit.alpha(-723723, alpha));
        HudRenderer.label(ctx, title, (float)x + 17.0f, (float)y + 4.6f, MenuKit.alpha(-2565922, alpha));
        CrispText.draw(ctx, value, HUD_FILE, (float)x + 17.0f + lw + 9.0f, (float)y + 12.6f, 8.6f, 0.1f, MenuKit.alpha(-723723, alpha));
    }

    static int topClearance(int x0, int x1) {
        int y = 6;
        for (HudLayout.Box box : SHOWN.values()) {
            if (box.y() > 40 || box.x() >= x1 || box.x() + box.width() <= x0) continue;
            y = Math.max(y, box.y() + box.height() + 5);
        }
        return y;
    }

    private static int watermarkWidth() {
        return 14 + HudRenderer.brandBlock();
    }

    private static String value(HudLayout.Element element, class_310 mc) {
        return switch (element) {
            case HudLayout.Element.FPS -> Integer.toString(mc.method_47599());
            case HudLayout.Element.BPS -> String.format(Locale.ROOT, "%.1f", HudMetrics.bps());
            case HudLayout.Element.TPS -> {
                if (HudMetrics.hasTps()) {
                    yield String.format(Locale.ROOT, "%.1f", HudMetrics.tps());
                }
                yield "--";
            }
            case HudLayout.Element.XYZ -> {
                if (mc.field_1724 == null) {
                    yield "0  64  0";
                }
                yield (int)Math.floor(mc.field_1724.method_23317()) + "  " + (int)Math.floor(mc.field_1724.method_23318()) + "  " + (int)Math.floor(mc.field_1724.method_23321());
            }
            default -> "";
        };
    }

    private static int baseWidth(HudLayout.Element element, class_310 mc, List<Module> modules) {
        return switch (element) {
            default -> throw new MatchException(null, null);
            case HudLayout.Element.WATERMARK -> {
                if (HudRenderer.infoLineOn()) {
                    yield HudRenderer.infoWidth(mc);
                }
                yield HudRenderer.watermarkWidth();
            }
            case HudLayout.Element.FPS, HudLayout.Element.BPS, HudLayout.Element.TPS -> HudRenderer.pillWidth(element.name(), HudRenderer.value(element, mc));
            case HudLayout.Element.XYZ -> HudRenderer.coordinatesWidth(mc);
            case HudLayout.Element.RADAR -> 88;
            case HudLayout.Element.MODULE_LIST -> Math.max(76, modules.stream().mapToInt(m -> HudRenderer.textWidth(Theme.label(m.name)) + 20).max().orElse(76));
            case HudLayout.Element.STAFF_LIST -> 180;
            case HudLayout.Element.KEYSTROKES -> {
                if (HudRenderer.keysBackground()) {
                    yield 86;
                }
                yield 66;
            }
            case HudLayout.Element.INVENTORY -> 178;
            case HudLayout.Element.REGION -> RegionMap.width(SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.RegionMap.class));
            case HudLayout.Element.SPOTIFY -> SpotifyPanel.INSTANCE.width(false);
            case HudLayout.Element.STAFF_LIVE -> 186;
            case HudLayout.Element.TOGIS -> 186;
            case HudLayout.Element.HOTKEYS -> HudRenderer.hotkeysWidth();
            case HudLayout.Element.AC_OFF -> {
                AcStatusCard.Model card = AntiCheat.card();
                if (card == null) {
                    yield 1;
                }
                yield AcStatusCard.width(card);
            }
            case HudLayout.Element.ARMOR -> 128;
        };
    }

    private static int baseHeight(HudLayout.Element element, List<Module> modules, class_310 mc, Hud hud) {
        return switch (element) {
            case HudLayout.Element.RADAR -> 88;
            case HudLayout.Element.MODULE_LIST -> Math.max(17, Math.round(HudRenderer.listHeight(modules)) - 1);
            case HudLayout.Element.STAFF_LIST -> 23 + 18 * Math.max(1, Math.min(hud.staffShown.get(), StaffList.online(mc, hud).size()));
            case HudLayout.Element.KEYSTROKES -> {
                if (HudRenderer.keysBackground()) {
                    yield 106;
                }
                yield 80;
            }
            case HudLayout.Element.INVENTORY -> 85;
            case HudLayout.Element.REGION -> RegionMap.height(SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.RegionMap.class));
            case HudLayout.Element.SPOTIFY -> SpotifyPanel.INSTANCE.height(false);
            case HudLayout.Element.STAFF_LIVE -> 23 + 18 * Math.max(1, HudRenderer.liveEntries().size());
            case HudLayout.Element.TOGIS -> 141;
            case HudLayout.Element.ARMOR -> 49;
            case HudLayout.Element.HOTKEYS -> 23 + 18 * Math.max(1, HudRenderer.hotkeyLines().size());
            case HudLayout.Element.XYZ -> 8 + 12 * HudRenderer.coordinateRows(mc).size();
            case HudLayout.Element.AC_OFF -> {
                AcStatusCard.Model card = AntiCheat.card();
                if (card == null) {
                    yield 1;
                }
                yield AcStatusCard.height(card);
            }
            default -> 20;
        };
    }

    private static List<Module> modules(float delta) {
        ArrayList<Module> list = new ArrayList<Module>();
        for (Module module : SoberClient.MODULES.all()) {
            if (!module.showInHud()) continue;
            float target = module.isActive() ? 1.0f : 0.0f;
            float current = PROGRESS.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            if (Math.abs((current += (target - current) * (1.0f - (float)Math.exp(-delta * 13.0f * Theme.animationSpeed()))) - target) < 0.002f) {
                current = target;
            }
            PROGRESS.put(module, Float.valueOf(current));
            if (!(current > 0.01f)) continue;
            list.add(module);
        }
        list.sort(Comparator.<Module>comparingInt(m -> HudRenderer.textWidth(Theme.label(m.name))).reversed().thenComparing(m -> m.name));
        return list;
    }

    private static void drawElement(class_332 ctx, class_310 mc, Hud hud, class_327 font, HudLayout.Element element, List<Module> modules, boolean preview) {
        switch (element) {
            case RADAR: {
                HudRenderer.radar(ctx, mc, font, hud);
                break;
            }
            case MODULE_LIST: {
                HudRenderer.moduleList(ctx, font, modules, preview);
                break;
            }
            case STAFF_LIST: {
                HudRenderer.staffList(ctx, mc, hud, font);
                break;
            }
            case WATERMARK: {
                if (HudRenderer.infoLineOn()) {
                    HudRenderer.infoLine(ctx, mc, font);
                    break;
                }
                HudRenderer.watermark(ctx);
                break;
            }
            case KEYSTROKES: {
                HudRenderer.keystrokes(ctx, mc, font);
                break;
            }
            case INVENTORY: {
                HudRenderer.inventory(ctx, mc, font);
                break;
            }
            case ARMOR: {
                HudRenderer.armor(ctx, mc, font);
                break;
            }
            case STAFF_LIVE: {
                HudRenderer.staffLive(ctx, font);
                break;
            }
            case TOGIS: {
                HudRenderer.togis(ctx);
                break;
            }
            case HOTKEYS: {
                HudRenderer.hotkeys(ctx, font);
                break;
            }
            case XYZ: {
                HudRenderer.coordinates(ctx, mc, font);
                break;
            }
            case REGION: {
                RegionMap.draw(ctx, mc, SoberClient.MODULES.get(net.albiz.client.port.soberui.modules.RegionMap.class));
                break;
            }
            case SPOTIFY: {
                HudLayout.Box box = SHOWN.get((Object)element);
                double mx = mc.field_1729.method_68879(mc.method_22683()) / (double)hudScale;
                double my = mc.field_1729.method_68883(mc.method_22683()) / (double)hudScale;
                double lx = box == null ? -1.0 : (mx - (double)box.x()) / (double)box.scale();
                double ly = box == null ? -1.0 : (my - (double)box.y()) / (double)box.scale();
                SpotifyPanel.INSTANCE.draw(ctx, lx, ly, preview, mc.field_1755 instanceof class_408);
                break;
            }
            case AC_OFF: {
                AcStatusCard.Model card = AntiCheat.card();
                if (card == null) break;
                AcStatusCard.draw(ctx, card);
                break;
            }
            default: {
                String label = element.name();
                String value = HudRenderer.value(element, mc);
                HudRenderer.panel(ctx, 0, 0, HudRenderer.pillWidth(label, value), 20, HudRenderer.edgeColor());
                HudRenderer.label(ctx, label, 8.0f, 5.8f, -2565922);
                HudRenderer.text(ctx, font, value, 12 + Math.round(HudRenderer.labelWidth(label)), 6, -723723);
            }
        }
    }

    private static List<CoordinateRow> coordinateRows(class_310 mc) {
        if (overworldIcon == null) {
            overworldIcon = new class_1799((class_1935)class_1802.field_8270);
            netherIcon = new class_1799((class_1935)class_1802.field_8328);
            endIcon = new class_1799((class_1935)class_1802.field_20399);
        }
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return List.of(new CoordinateRow(overworldIcon, "0  64  0", true), new CoordinateRow(netherIcon, "0  64  0", false));
        }
        double x = mc.field_1724.method_23317();
        double z = mc.field_1724.method_23321();
        int y = (int)Math.floor(mc.field_1724.method_23318());
        class_5321 dimension = mc.field_1687.method_27983();
        if (dimension == class_1937.field_25181) {
            return List.of(new CoordinateRow(endIcon, HudRenderer.coordinates(x, y, z), true));
        }
        boolean nether = dimension == class_1937.field_25180;
        String overworld = nether ? HudRenderer.coordinates(x * 8.0, y, z * 8.0) : HudRenderer.coordinates(x, y, z);
        String netherText = nether ? HudRenderer.coordinates(x, y, z) : HudRenderer.coordinates(x / 8.0, y, z / 8.0);
        return List.of(new CoordinateRow(overworldIcon, overworld, !nether), new CoordinateRow(netherIcon, netherText, nether));
    }

    private static String coordinates(double x, int y, double z) {
        return (int)Math.floor(x) + "  " + y + "  " + (int)Math.floor(z);
    }

    private static int coordinatesWidth(class_310 mc) {
        int widest = 0;
        for (CoordinateRow row : HudRenderer.coordinateRows(mc)) {
            widest = Math.max(widest, HudRenderer.textWidth(row.text()));
        }
        return 29 + widest;
    }

    private static void coordinates(class_332 ctx, class_310 mc, class_327 font) {
        List<CoordinateRow> rows = HudRenderer.coordinateRows(mc);
        int width = HudRenderer.coordinatesWidth(mc);
        int height = 8 + 12 * rows.size();
        HudRenderer.panel(ctx, 0, 0, width, height, HudRenderer.edgeColor());
        Matrix3x2fStack matrices = ctx.method_51448();
        for (int i = 0; i < rows.size(); ++i) {
            CoordinateRow row = rows.get(i);
            int y = 4 + i * 12;
            matrices.pushMatrix();
            matrices.translate(8.0f, (float)(y + 1));
            matrices.scale(0.625f, 0.625f);
            ctx.method_51427(row.icon(), 0, 0);
            matrices.popMatrix();
            HudRenderer.text(ctx, font, row.text(), 21, y + 2, row.current() ? -723723 : -5329226);
        }
    }

    private static void card(class_332 ctx, int x, int y, int w, int h, int r) {
        if (r < 40) {
            r = Math.min(9, Math.min(w, h) / 2);
        }
        HudRenderer.widget(ctx, x, y, w, h, r, 1.0f);
    }

    private static void radar(class_332 ctx, class_310 mc, class_327 font, Hud hud) {
        int c = 44;
        int r = 42;
        HudRenderer.card(ctx, 0, 0, 88, 88, 44);
        Smooth.ring(ctx, c, c, 43, 0x2EFFFFFF);
        Smooth.ring(ctx, c, c, 28, 0x1AFFFFFF);
        Smooth.ring(ctx, c, c, 14, 0x14FFFFFF);
        ctx.method_25294(c - r + 6, c, c + r - 6, c + 1, 0x14FFFFFF);
        ctx.method_25294(c, c - r + 6, c + 1, c + r - 6, 0x14FFFFFF);
        boolean rotate = hud.radarRotate.get();
        float playerYaw = mc.field_1724 == null ? 180.0f : mc.field_1724.method_5705(tickProgress);
        double yaw = Math.toRadians(rotate ? (double)playerYaw : 180.0);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        String[] letters = new String[]{"N", "E", "S", "W"};
        int[][] dirs = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        Matrix3x2fStack letterMatrices = ctx.method_51448();
        for (int i = 0; i < 4; ++i) {
            double sx = (double)dirs[i][0] * rx + (double)dirs[i][1] * rz;
            double sy = -((double)dirs[i][0] * fx + (double)dirs[i][1] * fz);
            class_2561 letter = Theme.bold(letters[i]);
            letterMatrices.pushMatrix();
            letterMatrices.translate((float)((double)c + sx * (double)(r - 9)), (float)((double)c + sy * (double)(r - 9)));
            letterMatrices.scale(0.9f, 0.9f);
            letterMatrices.translate((float)(-font.method_27525((class_5348)letter)) / 2.0f, -4.0f);
            ctx.method_51439(font, letter, 0, 0, i == 0 ? -2565922 : -5329226, false);
            letterMatrices.popMatrix();
        }
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return;
        }
        class_243 self = mc.field_1724.method_30950(tickProgress);
        double scale = (double)(r - 3) / (double)Math.max(1, hud.radarRange.get());
        if (hud.radarChunks.get().booleanValue()) {
            ChunkFinder hives;
            SusChunk sus = SoberClient.MODULES.get(SusChunk.class);
            if (sus != null) {
                HudRenderer.radarArea(ctx, c, r, self, sus.radarChunks(), scale, fx, fz, rx, rz, sus.radarColor(), false);
            }
            if ((hives = SoberClient.MODULES.get(ChunkFinder.class)) != null) {
                HudRenderer.radarArea(ctx, c, r, self, hives.radarChunks(), scale, fx, fz, rx, rz, hives.radarColor(), true);
            }
        }
        if (hud.radarPlayers.get().booleanValue()) {
            for (class_742 other : mc.field_1687.method_18456()) {
                if (other == mc.field_1724) continue;
                class_243 pos = other.method_30950(tickProgress);
                double dx = pos.field_1352 - self.field_1352;
                double dz = pos.field_1350 - self.field_1350;
                double sx = (dx * rx + dz * rz) * scale;
                double sy = -(dx * fx + dz * fz) * scale;
                double distance = Math.hypot(sx, sy);
                if (distance > (double)(r - 9)) {
                    if (distance > (double)((r - 9) * 3) || distance < 0.01) continue;
                    double ex = sx / distance * (double)(r - 4);
                    double ey = sy / distance * (double)(r - 4);
                    Smooth.circle(ctx, c + (int)Math.round(ex), c + (int)Math.round(ey), 2, -923470603);
                    continue;
                }
                int px = c + (int)Math.round(sx) - 5;
                int py = c + (int)Math.round(sy) - 5;
                Smooth.rect(ctx, px - 1, py - 1, 12, 12, 4, -723723);
                Avatars.draw(ctx, other.method_52814(), px, py, 10);
            }
        }
        Matrix3x2fStack matrices = ctx.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)c, (float)c);
        if (!rotate) {
            matrices.rotate((float)Math.toRadians(playerYaw - 180.0f));
        }
        Smooth.triangle(ctx, 0.0f, 0.0f, 9.0f, -920844);
        matrices.popMatrix();
    }

    private static void radarArea(class_332 ctx, int c, int r, class_243 self, LongSet chunks, double scale, double fx, double fz, double rx, double rz, int fill, boolean clampFar) {
        if (chunks.isEmpty()) {
            return;
        }
        int edge = Math.min(255, (fill >>> 24) * 3 / 2) << 24 | Math.min(255, (int)((float)(fill >> 16 & 0xFF) * 0.77f)) << 16 | Math.min(255, (int)((float)(fill >> 8 & 0xFF) * 0.77f)) << 8 | Math.min(255, (int)((float)(fill & 0xFF) * 0.77f));
        float angle = (float)Math.atan2(-fx, rx);
        int size = Math.max(1, (int)Math.ceil(16.0 * scale));
        Matrix3x2fStack matrices = ctx.method_51448();
        LongIterator keys = chunks.iterator();
        while (keys.hasNext()) {
            double sy;
            int cz;
            double mz;
            long key = keys.nextLong();
            int cx = class_1923.method_8325((long)key);
            double mx = (double)((cx << 4) + 8) - self.field_1352;
            double sx = (mx * rx + (mz = (double)(((cz = class_1923.method_8332((long)key)) << 4) + 8) - self.field_1350) * rz) * scale;
            double distance = Math.hypot(sx, sy = -(mx * fx + mz * fz) * scale);
            if (distance > (double)(r - 5)) {
                if (!clampFar && distance > (double)((r - 5) * 3)) continue;
                int ex = c + (int)Math.round(sx / distance * (double)(r - 4));
                int ey = c + (int)Math.round(sy / distance * (double)(r - 4));
                if (clampFar) {
                    float pulse = 0.5f + 0.5f * (float)Math.sin((double)System.currentTimeMillis() / 180.0);
                    Smooth.circle(ctx, ex, ey, 4, fill & 0xFFFFFF | Math.round(64.0f + 80.0f * pulse) << 24);
                    Smooth.circle(ctx, ex, ey, 2, fill & 0xFFFFFF | 0xF0000000);
                    continue;
                }
                Smooth.circle(ctx, ex, ey, 2, fill & 0xFFFFFF | 0xB0000000);
                continue;
            }
            double ox = (double)(cx << 4) - self.field_1352;
            double oz = (double)(cz << 4) - self.field_1350;
            double px = (ox * rx + oz * rz) * scale;
            double py = -(ox * fx + oz * fz) * scale;
            matrices.pushMatrix();
            matrices.translate((float)((double)c + px), (float)((double)c + py));
            matrices.rotate(angle);
            ctx.method_25294(0, 0, size, size, fill);
            if (!chunks.contains(class_1923.method_8331((int)cx, (int)(cz - 1)))) {
                ctx.method_25294(0, 0, size, 1, edge);
            }
            if (!chunks.contains(class_1923.method_8331((int)cx, (int)(cz + 1)))) {
                ctx.method_25294(0, size - 1, size, size, edge);
            }
            if (!chunks.contains(class_1923.method_8331((int)(cx - 1), (int)cz))) {
                ctx.method_25294(0, 0, 1, size, edge);
            }
            if (!chunks.contains(class_1923.method_8331((int)(cx + 1), (int)cz))) {
                ctx.method_25294(size - 1, 0, size, size, edge);
            }
            matrices.popMatrix();
        }
    }

    private static void moduleList(class_332 ctx, class_327 font, List<Module> modules, boolean preview) {
        if (modules.isEmpty()) {
            if (preview) {
                HudRenderer.card(ctx, 0, 0, 96, 18, 6);
                HudRenderer.text(ctx, font, "MODULE LIST", 8, 5, -5329226);
            }
            return;
        }
        int width = Math.max(76, modules.stream().mapToInt(m -> HudRenderer.textWidth(Theme.label(m.name)) + 20).max().orElse(76));
        float y = 0.0f;
        Matrix3x2fStack matrices = ctx.method_51448();
        for (Module module : modules) {
            float p = PROGRESS.getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            float ease = p * p * (3.0f - 2.0f * p);
            int alpha = Math.round(255.0f * p);
            String name = Theme.label(module.name);
            int rowWidth = HudRenderer.textWidth(name) + 20;
            matrices.pushMatrix();
            matrices.translate((float)(width - rowWidth) + (1.0f - ease) * 12.0f, y);
            HudRenderer.widget(ctx, 0, 0, rowWidth, 16, 7, (float)alpha / 255.0f);
            HudRenderer.text(ctx, font, name, (rowWidth - HudRenderer.textWidth(name)) / 2, 5, alpha << 24 | 0xF4F4F5);
            matrices.popMatrix();
            y += 18.0f * ease;
        }
    }

    private static float listHeight(List<Module> modules) {
        float height = 0.0f;
        for (Module module : modules) {
            float p = PROGRESS.getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            height += 18.0f * p * p * (3.0f - 2.0f * p);
        }
        return height;
    }

    private static List<HotkeyLine> hotkeyLines() {
        ArrayList<HotkeyLine> lines = new ArrayList<HotkeyLine>();
        for (Module module : SoberClient.MODULES.all()) {
            int key = module.getKey();
            if (key < 0) continue;
            lines.add(new HotkeyLine(module.name, MenuKit.keyName(key).toUpperCase(Locale.ROOT), module.isActive()));
        }
        return lines;
    }

    private static void watermark(class_332 ctx) {
        HudRenderer.panel(ctx, 0, 0, HudRenderer.watermarkWidth(), 20, HudRenderer.edgeColor());
        HudRenderer.brand(ctx, 6);
    }

    private static boolean infoLineOn() {
        Hud hud = SoberClient.MODULES.get(Hud.class);
        return hud != null && hud.infoLine.get() != false;
    }

    private static List<InfoPart> infoParts(class_310 mc) {
        class_640 entry;
        Hud hud = SoberClient.MODULES.get(Hud.class);
        ArrayList<InfoPart> parts = new ArrayList<InfoPart>();
        if (hud.fps.get().booleanValue()) {
            parts.add(new InfoPart(Integer.toString(mc.method_47599()), "FPS"));
        }
        if (hud.linePing.get().booleanValue() && mc.method_1562() != null && mc.field_1724 != null && !mc.method_1542() && (entry = mc.method_1562().method_2871(mc.field_1724.method_5667())) != null) {
            parts.add(new InfoPart(Integer.toString(entry.method_2959()), "ms"));
        }
        if (hud.bps.get().booleanValue()) {
            parts.add(new InfoPart(String.format(Locale.ROOT, "%.1f", HudMetrics.bps()), "BPS"));
        }
        if (hud.tps.get().booleanValue() && HudMetrics.hasTps()) {
            parts.add(new InfoPart(String.format(Locale.ROOT, "%.1f", HudMetrics.tps()), "TPS"));
        }
        if (hud.lineServer.get().booleanValue()) {
            class_642 server = mc.method_1558();
            if (server != null) {
                String address = server.field_3761.toLowerCase(Locale.ROOT);
                int port = address.lastIndexOf(58);
                if (port > 0 && address.indexOf(58) == port) {
                    address = address.substring(0, port);
                }
                parts.add(new InfoPart(address, ""));
            } else if (mc.method_1542()) {
                parts.add(new InfoPart("Singleplayer", ""));
            }
        }
        if (hud.lineTime.get().booleanValue()) {
            parts.add(new InfoPart(LocalTime.now().format(CLOCK), ""));
        }
        return parts;
    }

    private static int infoWidth(class_310 mc) {
        Hud hud = SoberClient.MODULES.get(Hud.class);
        int w = hud.watermark.get() != false ? 6 + HudRenderer.brandBlock() : -7;
        for (InfoPart part : HudRenderer.infoParts(mc)) {
            w += 15 + HudRenderer.textWidth(part.value()) + (part.unit().isEmpty() ? 0 : 3 + HudRenderer.unitWidth(part.unit()));
        }
        return w + 8;
    }

    private static int unitWidth(String unit) {
        return Math.round(HudRenderer.labelWidth(unit.toUpperCase(Locale.ROOT)));
    }

    private static void infoLine(class_332 ctx, class_310 mc, class_327 font) {
        Hud hud = SoberClient.MODULES.get(Hud.class);
        int w = HudRenderer.infoWidth(mc);
        HudRenderer.panel(ctx, 0, 0, w, 20, HudRenderer.edgeColor());
        int x = 6;
        boolean first = true;
        if (hud.watermark.get().booleanValue()) {
            HudRenderer.brand(ctx, 6);
            x = 6 + HudRenderer.brandBlock();
            first = false;
        }
        for (InfoPart part : HudRenderer.infoParts(mc)) {
            if (!first) {
                Smooth.rect(ctx, x + 7, 6, 1, 8, 0, 1089203189);
                x += 15;
            } else {
                x += 2;
                first = false;
            }
            HudRenderer.text(ctx, font, part.value(), x, 6, -723723);
            x += HudRenderer.textWidth(part.value());
            if (part.unit().isEmpty()) continue;
            HudRenderer.label(ctx, part.unit().toUpperCase(Locale.ROOT), x + 3, 5.8f, -5329226);
            x += 3 + HudRenderer.unitWidth(part.unit());
        }
    }

    private static int hotkeysWidth() {
        List<HotkeyLine> lines = HudRenderer.hotkeyLines();
        int title = Math.round(HudRenderer.headerWidth("HOTKEYS"));
        if (lines.isEmpty()) {
            return Math.max(title, HudRenderer.textWidth("No keys bound")) + 16;
        }
        int name = 0;
        int key = 0;
        for (HotkeyLine line : lines) {
            name = Math.max(name, HudRenderer.textWidth(line.name()));
            key = Math.max(key, HudRenderer.textWidth(line.key()));
        }
        return Math.max(title + 16, 16 + name + 16 + key + 10 + 8);
    }

    private static void hotkeys(class_332 ctx, class_327 font) {
        List<HotkeyLine> lines = HudRenderer.hotkeyLines();
        int width = HudRenderer.hotkeysWidth();
        HudRenderer.panel(ctx, 0, 0, width, 23 + 18 * Math.max(1, lines.size()), HudRenderer.edgeColor());
        HudRenderer.header(ctx, "HOTKEYS", 8.0f, 6.3f);
        if (lines.isEmpty()) {
            HudRenderer.text(ctx, font, "No keys bound", 8, 27, -5329226);
            return;
        }
        for (int i = 0; i < lines.size(); ++i) {
            HotkeyLine line = lines.get(i);
            int y = 23 + i * 18;
            Smooth.circle(ctx, 10, y + 8, 1, -5329226);
            HudRenderer.text(ctx, font, line.name(), 16, y + 4, line.on() ? -723723 : -5329226);
            int capW = HudRenderer.textWidth(line.key()) + 10;
            int capX = width - 8 - capW;
            Smooth.rect(ctx, capX, y + 1, capW, 14, 5, 695761024);
            HudRenderer.text(ctx, font, line.key(), capX + 5, y + 4, -2565922);
        }
    }

    private static void staffList(class_332 ctx, class_310 mc, Hud hud, class_327 font) {
        List<StaffList.Member> entries = StaffList.online(mc, hud);
        int count = Math.min(hud.staffShown.get(), entries.size());
        int accent = -723723;
        HudRenderer.panel(ctx, 0, 0, 180, 23 + 18 * Math.max(1, count), HudRenderer.edgeColor());
        HudRenderer.header(ctx, "STAFF LIST", 8.0f, 6.3f);
        String online = count + " ONLINE";
        HudRenderer.label(ctx, online, 172.0f - HudRenderer.labelWidth(online), 6.3f, count > 0 ? accent : -5329226);
        if (count == 0) {
            HudRenderer.text(ctx, font, "No staff online", 8, 27, -5329226);
            return;
        }
        for (int i = 0; i < count; ++i) {
            StaffList.Member member = entries.get(i);
            class_640 entry = member.entry();
            int y = 23 + i * 18;
            Avatars.draw(ctx, entry.method_52810(), 8, y + 2, 12);
            String role = member.role();
            int roleColor = HudRenderer.roleColor(role);
            float roleW = HudRenderer.labelWidth(role.toUpperCase(Locale.ROOT)) - 1.296f + 10.0f;
            int badgeX = Math.round(172.0f - roleW);
            Smooth.rect(ctx, badgeX, y + 2, Math.round(roleW), 12, 5, MenuKit.withAlpha(roleColor, 61));
            HudRenderer.label(ctx, role.toUpperCase(Locale.ROOT), badgeX + 5, (float)y + 4.2f, MenuKit.lerp(roleColor, -1, 0.35f));
            String ping = Math.max(0, entry.method_2959()) + "ms";
            int pingX = badgeX - 6 - HudRenderer.textWidth(ping);
            HudRenderer.text(ctx, font, ping, pingX, y + 4, -5329226);
            String name = member.name();
            Icons.draw(ctx, Icons.Icon.STAR, 23, y + 3, 10, PURPLE_STAR.contains(name.toLowerCase(Locale.ROOT)) ? -5219073 : -12723049);
            int room = pingX - 6 - 36;
            while (HudRenderer.textWidth(name) > room && name.length() > 1) {
                name = name.substring(0, name.length() - 1);
            }
            HudRenderer.text(ctx, font, name, 36, y + 4, -723723);
        }
    }

    private static int roleColor(String role) {
        String lower = role.toLowerCase(Locale.ROOT);
        if (lower.contains("owner") || lower.contains("admin") || lower.contains("manager")) {
            return -47814;
        }
        if (lower.contains("mod")) {
            return -16087809;
        }
        if (lower.contains("helper") || lower.contains("support")) {
            return -13577896;
        }
        if (lower.contains("dev") || lower.contains("builder")) {
            return -4236558;
        }
        return -7434605;
    }

    private static List<StaffLive.Entry> liveEntries() {
        StaffLive live = SoberClient.MODULES.get(StaffLive.class);
        if (live == null) {
            return List.of();
        }
        ArrayList<StaffLive.Entry> result = new ArrayList<StaffLive.Entry>();
        for (StaffLive.Entry entry : live.entries()) {
            if (!entry.enabled()) continue;
            result.add(entry);
        }
        return result;
    }

    private static void staffLive(class_332 ctx, class_327 font) {
        List<StaffLive.Entry> entries = HudRenderer.liveEntries();
        int width = 186;
        long count = entries.stream().filter(StaffLive.Entry::live).count();
        HudRenderer.panel(ctx, 0, 0, width, 23 + 18 * Math.max(1, entries.size()), count > 0L ? -1750963 : HudRenderer.edgeColor());
        HudRenderer.header(ctx, "STAFF LIVE", 8.0f, 6.3f);
        String summary = count + " LIVE";
        if (count > 0L) {
            int sw = Math.round(HudRenderer.labelWidth(summary)) + 10;
            Smooth.rect(ctx, width - 8 - sw, 5, sw, 12, 5, -47814);
            HudRenderer.label(ctx, summary, width - 8 - sw + 5, 5.2f, -1);
        } else {
            HudRenderer.label(ctx, summary, (float)(width - 8) - HudRenderer.labelWidth(summary), 6.3f, -5329226);
        }
        if (entries.isEmpty()) {
            HudRenderer.text(ctx, font, "No streamers selected", 8, 27, -5329226);
            return;
        }
        float pulse = (float)(0.5 + 0.5 * Math.sin((double)System.currentTimeMillis() / 260.0));
        for (int i = 0; i < entries.size(); ++i) {
            StaffLive.Entry entry = entries.get(i);
            int y = 23 + i * 18;
            if (entry.live()) {
                Smooth.circle(ctx, 11, y + 8, 5, Math.round(48.0f + 80.0f * pulse) << 24 | 0xE5484D);
                Smooth.circle(ctx, 11, y + 8, 3, -1750963);
            } else {
                Smooth.circle(ctx, 11, y + 8, 3, -9539978);
            }
            int bw = HudRenderer.textWidth(entry.badge()) + 10;
            int bx = width - 8 - bw;
            Smooth.rect(ctx, bx, y + 2, bw, 12, 5, entry.color());
            HudRenderer.text(ctx, font, entry.badge(), bx + 5, y + 4, -1);
            int px = bx - 6 - HudRenderer.textWidth(entry.platform());
            HudRenderer.text(ctx, font, entry.platform(), px, y + 4, -5329226);
            String name = entry.name();
            while (HudRenderer.textWidth(name) > px - 26 && name.length() > 1) {
                name = name.substring(0, name.length() - 1);
            }
            HudRenderer.text(ctx, font, name, 20, y + 4, entry.live() ? -723723 : -5329226);
        }
    }

    private static void key(class_332 ctx, class_327 font, String label, boolean pressed, int x, int y, int w, float delta) {
        int color;
        float old = KEYS.getOrDefault(label, Float.valueOf(0.0f)).floatValue();
        float value = old + ((pressed ? 1.0f : 0.0f) - old) * (1.0f - (float)Math.exp(-delta * 17.0f));
        KEYS.put(label, Float.valueOf(value));
        HudRenderer.widget(ctx, x, y, w, 18, 6, 1.0f);
        if (value > 0.01f) {
            Smooth.rect(ctx, x, y, w, 18, 6, Math.round(242.0f * value) << 24 | 0xF4F4F5);
        }
        int n = color = value > 0.5f ? -15856112 : -5329226;
        if (label.length() > 1) {
            float lw = HudRenderer.labelWidth(label) - 1.296f;
            HudRenderer.label(ctx, label, (float)x + ((float)w - lw) / 2.0f, (float)y + 3.8f, color);
        } else {
            float lw = CrispText.width(label, HUD_FILE, 8.6f, 0.1f) - 0.8600001f;
            CrispText.draw(ctx, label, HUD_FILE, (float)x + ((float)w - lw) / 2.0f, (float)y + 11.9f, 8.6f, 0.1f, color);
        }
    }

    private static void keystrokes(class_332 ctx, class_310 mc, class_327 font) {
        int oy;
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (float)(now - lastKeysFrame) / 1.0E9f);
        lastKeysFrame = now;
        boolean background = HudRenderer.keysBackground();
        int ox = background ? 0 : -10;
        int n = oy = background ? 0 : -21;
        if (background) {
            HudRenderer.panel(ctx, 0, 0, 86, 106, HudRenderer.edgeColor());
            HudRenderer.header(ctx, "KEYS", 8.0f, 6.3f);
        }
        HudRenderer.key(ctx, font, "W", mc.field_1690.field_1894.method_1434(), 34 + ox, 21 + oy, 18, delta);
        HudRenderer.key(ctx, font, "A", mc.field_1690.field_1913.method_1434(), 10 + ox, 41 + oy, 18, delta);
        HudRenderer.key(ctx, font, "S", mc.field_1690.field_1881.method_1434(), 34 + ox, 41 + oy, 18, delta);
        HudRenderer.key(ctx, font, "D", mc.field_1690.field_1849.method_1434(), 58 + ox, 41 + oy, 18, delta);
        HudRenderer.key(ctx, font, "LMB", mc.field_1690.field_1886.method_1434(), 10 + ox, 62 + oy, 30, delta);
        HudRenderer.key(ctx, font, "RMB", mc.field_1690.field_1904.method_1434(), 46 + ox, 62 + oy, 30, delta);
        HudRenderer.key(ctx, font, "SPACE", mc.field_1690.field_1903.method_1434(), 10 + ox, 83 + oy, 66, delta);
    }

    private static boolean keysBackground() {
        Hud hud = HudRenderer.hud();
        return hud != null && hud.keysBackground.get() != false;
    }

    private static void inventory(class_332 ctx, class_310 mc, class_327 font) {
        HudRenderer.panel(ctx, 0, 0, 178, 85, HudRenderer.edgeColor());
        HudRenderer.header(ctx, "INVENTORY", 8.0f, 6.3f);
        InventoryHud module = SoberClient.MODULES.get(InventoryHud.class);
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                class_1799 stack;
                int x = 8 + col * 18;
                int y = 24 + row * 18;
                class_1799 class_17992 = stack = mc.field_1724 == null ? class_1799.field_8037 : mc.field_1724.method_31548().method_5438(9 + row * 9 + col);
                if (module == null || module.emptySlots.get().booleanValue() || !stack.method_7960()) {
                    Smooth.rect(ctx, x, y, 17, 17, 4, 695761024);
                }
                if (stack.method_7960()) continue;
                ctx.method_51427(stack, x, y);
                ctx.method_51431(font, stack, x, y);
            }
        }
    }

    private static void armor(class_332 ctx, class_310 mc, class_327 font) {
        HudRenderer.panel(ctx, 0, 0, 128, 49, HudRenderer.edgeColor());
        for (int i = 0; i < ARMOR_SLOTS.length; ++i) {
            int x = 8 + i * 29;
            int y = 6;
            class_1799 stack = mc.field_1724 == null ? class_1799.field_8037 : mc.field_1724.method_6118(ARMOR_SLOTS[i]);
            Smooth.rect(ctx, x, y, 25, 37, 7, 695761024);
            if (stack.method_7960()) {
                Smooth.circle(ctx, x + 12, y + 11, 2, 821359861);
                continue;
            }
            ctx.method_51427(stack, x + 4, y + 3);
            if (!stack.method_7963()) continue;
            float left = 1.0f - (float)stack.method_7919() / (float)Math.max(1, stack.method_7936());
            int color = HudRenderer.durabilityColor(left);
            Smooth.rect(ctx, x + 4, y + 22, 17, 3, 1, 653587701);
            Smooth.rect(ctx, x + 4, y + 22, Math.max(1, Math.round(17.0f * left)), 3, 1, color);
            String percent = Math.round(left * 100.0f) + "%";
            float width = CrispText.width(percent, HUD_FILE, 7.0f, 0.05f);
            CrispText.draw(ctx, percent, HUD_FILE, (float)x + 12.5f - width / 2.0f, (float)y + 33.0f, 7.0f, 0.05f, MenuKit.lerp(color, -723723, 0.35f));
        }
    }

    private static int durabilityColor(float left) {
        if (left > 0.5f) {
            return MenuKit.lerp(-24822, -13577896, (left - 0.5f) * 2.0f);
        }
        return MenuKit.lerp(-47814, -24822, left * 2.0f);
    }

    static {
        TOGIS_KEYS = HudRenderer.togisKeys();
        TOGIS_GLOW = new HashMap<Integer, Float>();
        togisFrame = System.nanoTime();
        lastKeysFrame = System.nanoTime();
        lastFrame = System.nanoTime();
        guideX = -1;
        guideY = -1;
        hudScale = 1.0f;
        tickProgress = 1.0f;
        HUD_FONT = class_2583.field_24360.method_27704((class_11719)Fonts.Family.Montserrat.font(Fonts.Weight.Bold));
        CLOCK = DateTimeFormatter.ofPattern("HH:mm");
        PURPLE_STAR = Set.of("owen1212055", "archivepedro", "evify", "0g9");
        ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
    }

    private record CoordinateRow(class_1799 icon, String text, boolean current) {
    }

    private record HotkeyLine(String name, String key, boolean on) {
    }

    private record InfoPart(String value, String unit) {
    }
}

