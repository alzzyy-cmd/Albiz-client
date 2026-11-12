// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.module;

public enum Category {
    BASE_FINDING("Base Finding"),
    DONUT("Donut"),
    COMBAT("Combat"),
    VISUALS("Visuals"),
    MOVEMENT("AC OFF"),
    CLIENT("Client"),
    CONFIG("Config");

    public final String title;

    private Category(String title) {
        this.title = title;
    }
}

