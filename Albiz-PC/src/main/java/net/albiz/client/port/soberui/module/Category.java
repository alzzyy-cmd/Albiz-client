// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.module;

public enum Category {
    BASE_FINDING("Base Finding"),
    DONUT("Misc"),
    COMBAT("Combat"),
    VISUALS("Visuals"),
    MOVEMENT("Movement"),
    CLIENT("Client"),
    RENDER("Render"), MACRO("Macro"), CONFIG("Config");

    public final String title;

    private Category(String title) {
        this.title = title;
    }
}

