package net.albiz.client.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable, renderer-independent catalog, also used by headless localization tests. */
public final class TranslationCatalog {
    public static final TranslationCatalog INSTANCE = new TranslationCatalog();
    private final Map<String,String> en = load("en_us"), tr = load("tr_tr");
    private final Map<String,String> reverse, uppercaseTranslations;
    private final Map<Character,List<String>> prefixes, suffixes;
    private final Set<String> translatedValues;
    private TranslationCatalog() {
        Map<String,String> keys = new HashMap<>();
        en.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> keys.put(e.getValue(),e.getKey()));
        reverse = Map.copyOf(keys);
        Map<String,String> caps = new HashMap<>();
        keys.forEach((value,key) -> caps.put(value.toUpperCase(Locale.ROOT),tr.getOrDefault(key,value).toUpperCase(Locale.forLanguageTag("tr-TR"))));
        uppercaseTranslations = Map.copyOf(caps);
        prefixes = fragments(keys.keySet(),true);
        suffixes = fragments(keys.keySet(),false);
        translatedValues = Set.copyOf(tr.values());
    }
    private static Map<Character,List<String>> fragments(Set<String> values, boolean prefix) {
        Map<Character,List<String>> buckets = new HashMap<>();
        values.stream().filter(s -> s.length()>3 && !s.isBlank() && (prefix ? s.endsWith(" ") : s.startsWith(" ")))
            .sorted(Comparator.comparingInt(String::length).reversed()).forEach(s ->
                buckets.computeIfAbsent(s.charAt(prefix ? 0 : s.length()-1),k -> new ArrayList<>()).add(s));
        buckets.replaceAll((k,v)->List.copyOf(v)); return Map.copyOf(buckets);
    }
    private static Map<String,String> load(String code) {
        try (var stream = TranslationCatalog.class.getResourceAsStream("/assets/albiz/lang/"+code+".json")) {
            if (stream == null) throw new IllegalStateException("Missing language: "+code);
            Map<String,String> values = new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),new TypeToken<Map<String,String>>(){}.getType());
            return Map.copyOf(values);
        } catch (java.io.IOException ex) { throw new IllegalStateException("Cannot load language: "+code,ex); }
    }
    public String text(String key, String fallback, boolean turkish) {
        String english = en.getOrDefault(key,fallback);
        return turkish ? tr.getOrDefault(key,english) : english;
    }
    public String display(String original, boolean turkish) {
        if (original == null) return "";
        if (!turkish || original.isEmpty() || translatedValues.contains(original)) return original;
        String key = reverse.get(original);
        if (key != null) return tr.getOrDefault(key,original);
        if (original.indexOf('\n') >= 0) {
            String[] lines = original.split("\n",-1);
            for (int i=0;i<lines.length;i++) lines[i]=display(lines[i],true);
            return String.join("\n",lines);
        }
        // Explicit, externally translated UI fragments; never translate arbitrary words in user input.
        for (String prefix : prefixes.getOrDefault(original.charAt(0),List.of())) if (original.startsWith(prefix)) {
            String tail=original.substring(prefix.length());
            if (!tail.isEmpty()) for (String suffix : suffixes.getOrDefault(tail.charAt(tail.length()-1),List.of())) if (tail.endsWith(suffix)) {
                tail=tail.substring(0,tail.length()-suffix.length())+tr.getOrDefault(reverse.get(suffix),suffix); break;
            }
            return tr.getOrDefault(reverse.get(prefix),prefix)+tail;
        }
        for (String suffix : suffixes.getOrDefault(original.charAt(original.length()-1),List.of())) if (original.endsWith(suffix))
            return display(original.substring(0,original.length()-suffix.length()),true)+tr.getOrDefault(reverse.get(suffix),suffix);
        return original;
    }
    /** Uppercase translation is opt-in for decorative UI captions, never protocol/enum data. */
    public String caption(String original,boolean turkish) {
        String result=display(original,turkish);
        return turkish && result.equals(original) ? uppercaseTranslations.getOrDefault(original,result) : result;
    }
    public Set<String> keys() { return en.keySet(); }
    public boolean hasTurkish(String key) { return tr.containsKey(key); }
}
