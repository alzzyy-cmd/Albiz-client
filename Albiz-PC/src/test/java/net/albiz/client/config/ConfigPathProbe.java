package net.albiz.client.config;
public class ConfigPathProbe {
 public static void main(String[] args) throws Exception {
  java.nio.file.Path root=ConfigMigration.root(java.nio.file.Path.of(args[0]));
  AtomicFiles.writeUtf8(root.resolve(ConfigMigration.mainFileName()),"ÇĞİÖŞÜ");
  System.out.println("FOLDER="+root.getFileName());
 }
}
