package net.albiz.client.input;

import java.util.concurrent.atomic.AtomicInteger;
import net.albiz.client.platform.PlatformInfo;

/** Event-based mobile binding: never polls hundreds of GLFW-stub key slots. */
public final class KeyCapture {
    private static final AtomicInteger pending = new AtomicInteger(-1);
    private KeyCapture() {}
    public static void reset() { pending.set(-1); }
    public static void key(int key, int action) {
        if (action == 1 && key < ModuleKeybinds.MOUSE_OFFSET && ModuleKeybinds.isBindable(key)) pending.set(key);
    }
    public static void mouse(int button, int action) {
        if (action != 1 || button < 0 || button > 7) return;
        // Zalith maps a tap to primary mouse. Do not accidentally bind the finger opening the UI.
        if (PlatformInfo.isMobile() && button == 0) return;
        pending.set(ModuleKeybinds.MOUSE_OFFSET + button);
    }
    public static int poll() { return pending.getAndSet(-1); }
}
