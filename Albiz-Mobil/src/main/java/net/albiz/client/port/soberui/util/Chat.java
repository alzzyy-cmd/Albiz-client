package net.albiz.client.port.soberui.util;

import net.albiz.client.port.soberui.gui.Notifications;

public final class Chat {
    private Chat() {
    }

    public static void info(String source, String message) {
        Notifications.push(source, message, Notifications.Type.INFO);
    }

    public static void success(String source, String message) {
        Notifications.push(source, message, Notifications.Type.SUCCESS);
    }

    public static void warning(String source, String message) {
        Notifications.push(source, message, Notifications.Type.WARNING);
    }

    public static void error(String source, String message) {
        Notifications.push(source, message, Notifications.Type.ERROR);
    }
}

