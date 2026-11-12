package net.albiz.client.port.sober.gui;
import net.albiz.client.port.sober.util.Chat;
public final class Notifications {
 public enum Type{INFO,WARNING,ERROR,SUCCESS}
 public static void push(String title,String message,Type type){Chat.info(title,message);}
}
