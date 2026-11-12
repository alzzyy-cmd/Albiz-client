// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.sober.render;

public enum ShapeMode {
    Lines,
    Sides,
    Both;


    public boolean lines() {
        return this != Sides;
    }

    public boolean sides() {
        return this != Lines;
    }
}

