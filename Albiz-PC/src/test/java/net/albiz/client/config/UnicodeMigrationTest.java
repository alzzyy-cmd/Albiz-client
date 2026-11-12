package net.albiz.client.config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class UnicodeMigrationTest {
 @TempDir Path dir;
 @Test void currentAsciiFolderMigratesBindingBytesWithoutChangingThem() throws Exception {
  Path old=dir.resolve("albiz");Files.createDirectories(old);
  byte[] bytes="{\"keybind\":65,\"name\":\"ÇĞİÖŞÜ\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
  Files.write(old.resolve("modules.json"),bytes);
  Path actual=ConfigMigration.root(dir);
  assertEquals("albız",actual.getFileName().toString());
  assertArrayEquals(bytes,Files.readAllBytes(actual.resolve("modules.json")));
  assertArrayEquals(bytes,Files.readAllBytes(actual.resolve("albız.json")));
  assertArrayEquals(bytes,Files.readAllBytes(old.resolve("modules.json")));
 }
 @Test void newestAsciiFolderTakesPrecedenceOverOlderAnubis() throws Exception {
  for(String name:new String[]{"albiz","anubis"}) {Files.createDirectories(dir.resolve(name));Files.writeString(dir.resolve(name+"/modules.json"),name);}
  assertEquals("albiz",Files.readString(ConfigMigration.root(dir).resolve("modules.json")));
 }
 @Test void preexistingTurkishTargetIsNeverOverwritten() throws Exception {
  Files.createDirectories(dir.resolve("albız"));Files.writeString(dir.resolve("albız/modules.json"),"keep");
  Files.createDirectories(dir.resolve("albiz"));Files.writeString(dir.resolve("albiz/modules.json"),"old");
  assertEquals("keep",Files.readString(ConfigMigration.root(dir).resolve("modules.json")));
 }
 @Test void nonUtf8JvmFallsBackToAlbizNeverAnubis() throws Exception {
  org.junit.jupiter.api.Assumptions.assumeTrue(java.io.File.separatorChar=='/');
  String cp=ConfigMigration.class.getProtectionDomain().getCodeSource().getLocation().getPath()+java.io.File.pathSeparator+
      ConfigPathProbe.class.getProtectionDomain().getCodeSource().getLocation().getPath();
  ProcessBuilder builder=new ProcessBuilder(System.getProperty("java.home")+"/bin/java","-cp",cp,ConfigPathProbe.class.getName(),dir.toString());
  builder.environment().put("LC_ALL","C");builder.environment().put("LANG","C");builder.redirectErrorStream(true);
  Process process=builder.start();assertTrue(process.waitFor(15,java.util.concurrent.TimeUnit.SECONDS));
  String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
  assertEquals(0,process.exitValue(),output);assertTrue(output.contains("FOLDER=albiz"),output);
  assertTrue(Files.exists(dir.resolve("albiz/albiz.json")));
 }
 @Test void existingNamedMainFileIsNeverOverwritten() throws Exception {
  Files.createDirectories(dir.resolve("albız"));
  Files.writeString(dir.resolve("albız/albız.json"),"new");Files.writeString(dir.resolve("albız/modules.json"),"old");
  assertEquals("new",Files.readString(ConfigMigration.root(dir).resolve("albız.json")));
 }
}
