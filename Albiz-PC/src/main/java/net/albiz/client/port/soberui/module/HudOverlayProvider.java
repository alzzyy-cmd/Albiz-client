// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.module;

import net.albiz.client.port.soberui.gui.OverlayCanvas;

public interface HudOverlayProvider {
    public boolean isHudOverlayEnabled();

    public void renderHudOverlay(OverlayCanvas var1, float var2, float var3);
}

