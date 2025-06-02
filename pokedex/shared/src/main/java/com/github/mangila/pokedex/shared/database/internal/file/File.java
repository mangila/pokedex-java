package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class File {

    private static final Logger log = LoggerFactory.getLogger(File.class);

    public static final Set<StandardOpenOption> WRITE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SPARSE,
            StandardOpenOption.DSYNC);

    public static final Set<StandardOpenOption> READ_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.DSYNC);

    private final Path path;
    private FileChannel writeChannel;
    private FileChannel readChannel;

    public File(FileName fileName) {
        this.path = Paths.get(fileName.value());
    }

    public long getFileSize() {
        return path.toFile().length();
    }

    public Path getPath() {
        return path;
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean isEmpty() {
        return exists() && getFileSize() == 0;
    }

    public void tryCreateFileIfNotExists() throws IOException {
        if (!exists()) {
            Files.createFile(path);
        }
    }

    public void deleteFile() throws IOException {
        if (writeChannel != null) {
            writeChannel.close();
        }
        if (readChannel != null) {
            readChannel.close();
        }
        Files.deleteIfExists(path);
    }

    public long write(ByteBuffer buffer, long position) throws IOException {
        long written = getWriteChannel()
                .write(buffer, position);
        getWriteChannel().force(true);
        return written;
    }

    public ByteBuffer readAndFlip(long position, int size) throws IOException {
        var buffer = BufferUtils.newByteBuffer(size);
        getReadChannel().read(buffer, position);
        buffer.flip();
        return buffer;
    }

    public MappedByteBuffer readFileRegion(long position, long size) throws IOException {
        return getReadChannel()
                .map(FileChannel.MapMode.READ_ONLY, position, size);
    }

    private FileChannel getReadChannel() throws IOException {
        if (readChannel == null || !readChannel.isOpen()) {
            log.debug("Opening read channel for {}", path.getFileName());
            this.readChannel = FileChannel.open(
                    path,
                    READ_OPTIONS
            );
        }
        return readChannel;
    }

    private FileChannel getWriteChannel() throws IOException {
        if (writeChannel == null || !writeChannel.isOpen()) {
            log.debug("Opening write channel for {}", path.getFileName());
            this.writeChannel = FileChannel.open(
                    path,
                    WRITE_OPTIONS
            );
        }
        return writeChannel;
    }

    public void truncate() throws IOException, InterruptedException {
        System.gc();
        TimeUnit.SECONDS.sleep(3);
        getWriteChannel().close();
        getReadChannel().close();
        getWriteChannel().truncate(0);
    }
}
