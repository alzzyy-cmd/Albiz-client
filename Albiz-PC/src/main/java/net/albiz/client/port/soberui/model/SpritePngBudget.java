package net.albiz.client.port.soberui.model;
import java.io.IOException;import java.nio.*;
/** Reject invalid/oversized images before the native decoder allocates pixels. */
final class SpritePngBudget {
 static void check(byte[] h)throws IOException{
  byte[] signature={(byte)137,80,78,71,13,10,26,10};
  if(h.length<24)throw new IOException("Truncated PNG header");
  for(int i=0;i<8;i++)if(h[i]!=signature[i])throw new IOException("Only PNG images are supported");
  if(h[12]!=73||h[13]!=72||h[14]!=68||h[15]!=82)throw new IOException("Invalid PNG IHDR");
  var bytes=ByteBuffer.wrap(h).order(ByteOrder.BIG_ENDIAN);int w=bytes.getInt(16),height=bytes.getInt(20);
  if(w<1||height<1||w>2048||height>2048||(long)w*height>4_194_304L)throw new IOException("Image exceeds dimension budget");
 }
}
