package net.albiz.client.port.soberui.model;
import java.nio.file.*;import java.io.IOException;
/** Imported profiles cannot direct model reads outside the user's model folder. */
public final class VisualModelPaths{
 public static Path resolve(Path folder,String name){if(name==null||name.isBlank()||name.length()>256)throw new IllegalArgumentException("Invalid model name");Path base=folder.toAbsolutePath().normalize(),file=base.resolve(name).normalize();if(!file.startsWith(base)||Path.of(name).isAbsolute())throw new IllegalArgumentException("Model outside configured folder");try{if(Files.exists(file)&&!file.toRealPath().startsWith(base.toRealPath()))throw new IllegalArgumentException("Model symlink outside configured folder");}catch(IOException e){throw new IllegalArgumentException("Invalid model path",e);}return file;}
}
