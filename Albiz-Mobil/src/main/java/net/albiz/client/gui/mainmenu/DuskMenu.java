package net.albiz.client.gui.mainmenu;

import imgui.ImDrawList;
import imgui.ImFont;
import java.util.List;
import net.albiz.client.gui.BrandPalette;
import net.albiz.client.gui.SteppeDraw;
import net.albiz.client.gui.ThemeImages;

/** Albız split composition: left navigation, red moon / Altai landscape on the right. */
public final class DuskMenu {
    private static final List<MenuAction> ICONS = List.of(MenuAction.ACCOUNTS, MenuAction.LANGUAGE,
        MenuAction.ACCESSIBILITY, MenuAction.ALBIZ, MenuAction.MODS, MenuAction.CREDITS);
    private static final List<MenuAction> NO_ACCOUNTS = ICONS.subList(1, ICONS.size());
    private DuskMenu() {}

    public static void render(MenuContext c) {
        ImDrawList dl = c.bg();
        float sw = c.width(), sh = c.height(), s = c.scale();
        float margin = Math.max(16.0F, 28.0F * s);
        float panelW = Math.min(sw - margin * 2, 430.0F * s);
        float panelH = Math.min(sh - margin * 2, 740.0F * s);
        float x = sw < sh * 1.15F ? (sw - panelW) * 0.5F : Math.max(margin, sw * 0.055F);
        float y = (sh - panelH) * 0.5F;
        float pad = 28.0F * s;
        DuskScene.draw(dl, sw, sh, s, c.time(), DuskScene.horizon(sh),
            sw * 0.77F, sh * 0.26F, Math.min(sw * 0.095F, sh * 0.15F), c.lookX(), c.lookY());
        dl.addRectFilled(0, 0, sw, sh, SteppeDraw.col(BrandPalette.BACKGROUND_DEEP, 0.16F));
        dl.addRectFilled(x, y, x + panelW, y + panelH, SteppeDraw.col(BrandPalette.PANEL, 0.94F), 14 * s);
        dl.addRect(x, y, x + panelW, y + panelH, SteppeDraw.col(BrandPalette.ACCENT, 0.40F), 14 * s, 0, Math.max(1, s));
        dl.addRectFilled(x, y + 18 * s, x + 3 * s, y + panelH - 18 * s, SteppeDraw.col(BrandPalette.ACCENT, 1), 2 * s);
        float logoW = panelW - pad * 2;
        float logoH = logoW * 348.0F / 1034.0F;
        ThemeImages.Texture logo = MenuTextures.original("/assets/albiz/textures/gui/logo.png");
        if (logo != null) c.image(dl, logo, x + pad, y + pad, x + pad + logoW, y + pad + logoH, -1, 1);
        else c.text(dl, c.nameFont(), x + pad, y + pad, BrandPalette.ACCENT, 1, BrandPalette.WORDMARK);
        float titleY = y + pad + logoH + 14 * s;
        c.text(dl, c.nameFont(), x + pad, titleY, BrandPalette.TEXT, 1, BrandPalette.NAME);
        c.text(dl, c.tagFont(), x + pad, titleY + c.textHeight(c.nameFont()) + 6 * s,
            BrandPalette.MUTED, 1, net.albiz.client.i18n.AlbizI18n.text("menu.albiz.subtitle"));
        float buttonY = titleY + c.textHeight(c.nameFont()) + c.textHeight(c.tagFont()) + 30 * s;
        float footerY = y + panelH - 54 * s;
        float gap = Math.max(6, 8 * s);
        float buttonH = Math.min(56 * s, (footerY - buttonY - gap * 6) / MenuAction.MAIN.size());
        buttonH = Math.max(net.albiz.client.platform.PlatformInfo.isMobile() ? 44 : 28, buttonH);
        for (int i = 0; i < MenuAction.MAIN.size(); i++) {
            MenuAction action = MenuAction.MAIN.get(i);
            item(c, action, x + pad, buttonY + i * (buttonH + gap), panelW - 2 * pad, buttonH);
        }
        List<MenuAction> icons = c.accountsAvailable() ? ICONS : NO_ACCOUNTS;
        float step = (panelW - 2 * pad) / icons.size();
        for (int i = 0; i < icons.size(); i++) {
            MenuAction action = icons.get(i);
            float cx = x + pad + step * (i + 0.5F), cy = footerY + 18 * s;
            float hover = c.control(action, cx - step * 0.5F, cy - 22 * s, step, 44 * s);
            if (hover > 0.01F) dl.addRectFilled(cx - 20 * s, cy - 20 * s, cx + 20 * s, cy + 20 * s,
                SteppeDraw.col(BrandPalette.ACCENT, hover * 0.16F), 7 * s);
            MenuIcons.draw(dl, action, cx, cy, 9 * s,
                SteppeDraw.col(SteppeDraw.mix(BrandPalette.MUTED, BrandPalette.ACCENT_LIGHT, hover), 1), Math.max(1.2F, 1.5F * s));
            if (hover > 0.5F) c.tooltip(action.label(), cx, cy - 36 * s);
        }
        if (sw > panelW + margin * 4) {
            String version = "ALBIZ+  /  1.0  /  MC 1.21.11";
            c.text(dl, c.tagFont(), sw - margin - c.textWidth(c.tagFont(), version), sh - margin - c.textHeight(c.tagFont()),
                BrandPalette.MUTED, 0.8F, version);
        }
    }
    /** No textures/scene allocation: keep navigation usable if an asset fails. */
    public static void renderFallback(MenuContext c) {
        c.bg().addRectFilled(0, 0, c.width(), c.height(), SteppeDraw.col(BrandPalette.BACKGROUND, 1));
        float w = Math.min(c.width() - 32, 360 * c.scale());
        float h = Math.max(36, 48 * c.scale()), x = (c.width() - w) * 0.5F;
        float y = Math.max(40, (c.height() - 5 * (h + 8)) * 0.5F);
        c.text(c.bg(), c.nameFont(), x, y - 38 * c.scale(), BrandPalette.ACCENT, 1, BrandPalette.NAME);
        for (int n = 0; n < MenuAction.MAIN.size(); n++) item(c, MenuAction.MAIN.get(n), x, y + n * (h + 8), w, h);
    }
    private static void item(MenuContext c, MenuAction action, float x, float y, float w, float h) {
        float hover = c.control(action, x, y, w, h);
        boolean enabled = c.enabled(action);
        ImDrawList dl = c.bg();
        int fill = SteppeDraw.mix(0xFF21111F, BrandPalette.ACCENT_DEEP, hover * 0.75F);
        dl.addRectFilled(x, y, x + w, y + h, SteppeDraw.col(fill, enabled ? 1 : 0.4F), 7 * c.scale());
        dl.addRect(x, y, x + w, y + h, SteppeDraw.col(BrandPalette.ACCENT, 0.28F + hover * 0.65F), 7 * c.scale(), 0, Math.max(1, c.scale()));
        if (hover > 0.01F) dl.addRectFilled(x, y + 8 * c.scale(), x + 3 * c.scale(), y + h - 8 * c.scale(),
            SteppeDraw.col(BrandPalette.ACCENT_LIGHT, hover), c.scale());
        ImFont font = c.bodyFont();
        c.text(dl, font, x + 18 * c.scale(), y + (h - c.textHeight(font)) * 0.5F,
            enabled ? BrandPalette.TEXT : BrandPalette.MUTED, 1, action.label());
        c.text(dl, font, x + w - 27 * c.scale(), y + (h - c.textHeight(font)) * 0.5F,
            BrandPalette.ACCENT_LIGHT, enabled ? 1 : 0.4F, "> ");
    }
}
