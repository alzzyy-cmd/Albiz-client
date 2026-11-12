package net.albiz.client.i18n;
import java.nio.file.*;
import net.albiz.client.config.ThemeStore;import net.fabricmc.loader.api.FabricLoader;
/** One-time gate stored separately from theme migrations. */
public final class LanguageChoice {
 private static String selected;private static boolean loaded;
 private LanguageChoice(){}
 private static Path file(){return net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("language-choice.txt");}
 private static synchronized void load(){if(!loaded){loaded=true;selected=LanguageChoiceFile.read(file()).orElse(null);}}
 public static boolean chosen(){load();return selected!=null;}
 public static void applySaved(){load();if(selected!=null){ThemeStore.get().language.set(selected);AlbizI18n.setLanguage(selected);}}
 public static boolean choose(String code){if(!LanguageChoiceFile.valid(code))return false;try{LanguageChoiceFile.write(file(),code);selected=code;loaded=true;applySaved();ThemeStore.get().save();return true;}catch(java.io.IOException|RuntimeException e){net.albiz.client.AlbizClientMod.LOGGER.warn("Language choice could not be saved",e);return false;}}
}
