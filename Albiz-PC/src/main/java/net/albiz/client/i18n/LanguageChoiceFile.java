package net.albiz.client.i18n;
import java.nio.file.*;import java.io.IOException;import java.util.Optional;
/** Pure file layer: invalid selections never overwrite a valid decision. */
public final class LanguageChoiceFile {
 private LanguageChoiceFile(){}
 public static boolean valid(String code){return "en_us".equals(code)||"tr_tr".equals(code);}
 public static Optional<String> read(Path p){try{String s=Files.readString(p).trim();return valid(s)?Optional.of(s):Optional.empty();}catch(IOException e){return Optional.empty();}}
 public static void write(Path p,String code)throws IOException {if(!valid(code))throw new IllegalArgumentException("Unsupported language choice");Files.createDirectories(p.toAbsolutePath().getParent());Path temp=Files.createTempFile(p.toAbsolutePath().getParent(),"language-choice-",".tmp");try{Files.writeString(temp,code);try{Files.move(temp,p,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,p,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}}
}
