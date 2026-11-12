// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

final class StableWidth {
    private float current = -1.0f;
    private float narrowerFor;

    StableWidth() {
    }

    float update(float target, float dt) {
        if (this.current < 0.0f || target >= this.current) {
            this.current = target;
            this.narrowerFor = 0.0f;
            return this.current;
        }
        this.narrowerFor += dt;
        if (this.narrowerFor < 1.5f) {
            return this.current;
        }
        this.current += (target - this.current) * (1.0f - (float)Math.exp(-12.0f * dt));
        if (Math.abs(this.current - target) < 0.25f) {
            this.current = target;
        }
        return this.current;
    }
}

