package net.albiz.client.i18n;

import net.minecraft.class_310;

/** Presentation-only translation. Never mutate module/setting IDs, stored modes or commands. */
public final class AlbizI18n {
    private static volatile String override = "en_us";
    private static volatile boolean turkish = false;
    private static volatile long nextRefresh;
    private AlbizI18n() {}
    public static void setLanguage(String value) {
        String next = value == null ? "Auto" : value;
        if (next.equals(override)) return;
        override = next; nextRefresh = 0;
        if (!"Auto".equals(next)) turkish = "tr_tr".equals(next) || "Türkçe".equals(next);
    }
    private static void refresh() {
        if (!"Auto".equals(override)) return;
        long now = System.nanoTime();
        if (now < nextRefresh) return;
        nextRefresh = now + 500_000_000L;
        class_310 mc = class_310.method_1551();
        String code = mc != null && mc.method_1526()!=null ? mc.method_1526().method_4669() : "en_us";
        turkish = "tr_tr".equals(code);
    }
    public static boolean isTurkish() { refresh(); return turkish; }
    public static String text(String key) { return textOr(key,key); }
    public static String textOr(String key,String fallback) {
        refresh(); return TranslationCatalog.INSTANCE.text(key,fallback,turkish);
    }
    public static String display(String original) {
        refresh(); return TranslationCatalog.INSTANCE.display(original,turkish);
    }
    public static String caption(String original) { refresh(); return TranslationCatalog.INSTANCE.caption(original,turkish); }
    public static String uppercase(String original) { return caption(original).toUpperCase(turkish ? java.util.Locale.forLanguageTag("tr-TR") : java.util.Locale.ROOT); }
    public static String backgroundAssigned(String name,String target) {
        return text("notification.background_assigned").replace("{name}",name).replace("{target}",display(target));
    }
    /** Preserve ImGui's identity when the visible label changes language. */
    public static String label(String original) {
        if (original == null || original.startsWith("##")) return original;
        int split = original.indexOf("##");
        String visible = split < 0 ? original : original.substring(0,split);
        String translated = display(visible);
        if (split >= 0 && original.startsWith("###",split)) return translated+original.substring(split);
        // Always canonicalize, including English, so a locale switch cannot change widget identity.
        return translated+"###"+original;
    }
}
