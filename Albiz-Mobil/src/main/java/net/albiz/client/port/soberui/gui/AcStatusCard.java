// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.albiz.client.port.soberui.gui.CrispText;
import net.albiz.client.port.soberui.gui.HudRenderer;
import net.albiz.client.port.soberui.gui.MenuKit;
import net.albiz.client.port.soberui.gui.Smooth;
import java.util.List;
import net.minecraft.class_332;

public final class AcStatusCard {
    private static final String BOLD = "montserrat-bold.ttf";
    private static final float PAD = 6.0f;
    private static final float STATUS_EM = 8.4f;
    private static final float ROW_EM = 6.4f;

    private AcStatusCard() {
    }

    static int width(Model model) {
        float headerW = HudRenderer.headerWidth("ANTICHEAT");
        float statusW = 12.0f + CrispText.width(model.status(), BOLD, 8.4f, 0.1f);
        float rowsW = 0.0f;
        for (Row row : model.rows()) {
            rowsW = Math.max(rowsW, CrispText.width(row.place(), BOLD, 6.4f, 0.1f) + 12.0f + CrispText.width(row.distance(), BOLD, 6.4f, 0.1f));
        }
        return Math.round(Math.max(Math.max(headerW, statusW), rowsW) + 12.0f);
    }

    static int height(Model model) {
        return Math.round(38.0f + (float)model.rows().size() * 10.0f);
    }

    static void draw(class_332 context, Model model) {
        float time = (float)(System.nanoTime() % 3600000000000L) / 1.0E9f;
        int w = AcStatusCard.width(model);
        int h = AcStatusCard.height(model);
        HudRenderer.panel(context, 0, 0, w, h, model.state().color);
        HudRenderer.header(context, "ANTICHEAT", 6.0f, 6.0f);
        float statusY = 27.0f;
        float pulse = model.state() == State.OFF ? 0.55f + 0.45f * (float)Math.sin((double)time * 6.0) : 1.0f;
        Smooth.circle(context, Math.round(9.0f), Math.round(statusY - 3.0f), 2, MenuKit.alpha(model.state().color, pulse));
        CrispText.draw(context, model.status(), BOLD, 16.0f, statusY, 8.4f, 0.1f, model.state().color);
        float rowY = statusY + 11.0f;
        for (Row row : model.rows()) {
            CrispText.draw(context, row.place(), BOLD, 6.0f, rowY, 6.4f, 0.1f, -723723);
            float dw = CrispText.width(row.distance(), BOLD, 6.4f, 0.1f);
            CrispText.draw(context, row.distance(), BOLD, (float)w - 6.0f - dw, rowY, 6.4f, 0.1f, -5329226);
            rowY += 10.0f;
        }
    }

    public record Model(State state, String status, List<Row> rows) {
    }

    public record Row(String place, String distance) {
    }

    public static enum State {
        ON(-11870592),
        OFF(-41846),
        UNKNOWN(-4669752);

        final int color;

        private State(int color) {
            this.color = color;
        }
    }
}

