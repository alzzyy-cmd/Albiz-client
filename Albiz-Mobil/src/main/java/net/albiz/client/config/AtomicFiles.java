package net.albiz.client.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class AtomicFiles {
    private AtomicFiles() {}
    public static void writeUtf8(Path destination, String text) throws IOException {
        Path absolute = destination.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path tmp = Files.createTempFile(absolute.getParent(), ".albiz-write-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = StandardCharsets.UTF_8.encode(text);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            try { Files.move(tmp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }
    public static Path backupCorrupt(Path file) throws IOException {
        Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + java.util.UUID.randomUUID() + ".bak");
        return Files.move(file, backup);
    }
}
