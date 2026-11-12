package net.albiz.client.port.sober.util;
import net.minecraft.*;
public final class Chat {
 public static void info(String title,String message){var mc=class_310.method_1551();if(mc!=null&&mc.field_1724!=null)mc.field_1724.method_7353(class_2561.method_43470("["+title+"] "+message),false);}
 public static void warning(String title,String message){info(title,message);}
 public static void error(String title,String message){info(title,message);}
}
