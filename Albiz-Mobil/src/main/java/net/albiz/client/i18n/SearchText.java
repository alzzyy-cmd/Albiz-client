package net.albiz.client.i18n;

/** Locale-independent matching of Turkish and ASCII spellings, without changing stored text. */
public final class SearchText {
    private SearchText() {}
    public static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(java.util.Locale.ROOT).trim().replace("\u0307", "")
            .replace('ı','i').replace('ğ','g').replace('ü','u').replace('ş','s').replace('ö','o').replace('ç','c');
    }
}
