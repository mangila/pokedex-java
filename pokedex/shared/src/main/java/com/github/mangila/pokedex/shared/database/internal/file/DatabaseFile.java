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

public class DatabaseFile {

    private static final Logger log = LoggerFactory.getLogger(DatabaseFile.class);

    public static final Set<StandardOpenOption> WRITE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC);

    public static final Set<StandardOpenOption> READ_OPTIONS = EnumSet.of(
            StandardOpenOption.READ);

    private final Path path;
    private FileChannel writeChannel;
    private FileChannel readChannel;

    public DatabaseFile(FileName fileName) {
        this.path = Paths.get(fileName.value());
    }

    public long getFileSize() {
        return path.toFile().length();
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean isEmpty() {
        return exists() && getFileSize() == 0;
    }

    public void tryCreateFileIfNotExists() throws IOException {
        log.debug("Trying to create file {}", path.getFileName());
        if (!exists()) {
            Files.createFile(path);
        }
    }

    public void tryDeleteFile() throws IOException {
        log.info("Trying to delete file {}", path.getFileName());
        closeChannels();
        Files.deleteIfExists(path);
    }

    public long write(ByteBuffer buffer, long position) throws IOException {
        return getWriteChannel().write(buffer, position);
    }

    public ByteBuffer readAndFlip(long position, int size) throws IOException {
        var buffer = BufferUtils.newByteBuffer(size);
        getReadChannel().read(buffer, position);
        buffer.flip();
        return buffer;
    }

    public MappedByteBuffer readFileRegion(long position, long size) throws IOException {
        return getReadChannel().map(FileChannel.MapMode.READ_ONLY, position, size);
    }

    private static boolean isOpen(FileChannel channel) {
        return channel != null && channel.isOpen();
    }

    private FileChannel getReadChannel() throws IOException {
        if (!isOpen(readChannel)) {
            log.debug("Opening read channel for {}", path.getFileName());
            this.readChannel = FileChannel.open(
                    path,
                    READ_OPTIONS
            );
        }
        return readChannel;
    }

    private FileChannel getWriteChannel() throws IOException {
        if (!isOpen(writeChannel)) {
            log.debug("Opening write channel for {}", path.getFileName());
            this.writeChannel = FileChannel.open(
                    path,
                    WRITE_OPTIONS
            );
        }
        return writeChannel;
    }

    public void truncate() throws IOException {
        log.debug("Truncating file {}", path.getFileName());
        if (isOpen(writeChannel)) {
            writeChannel.truncate(0);
            writeChannel.close();
        }
        if (isOpen(readChannel)) {
            readChannel.close();
        }
    }

    public Path getPath() {
        return path;
    }

    public void closeChannels() {
        try {
            if (isOpen(writeChannel)) {
                log.debug("Closing write channel for {}", path.getFileName());
                writeChannel.force(true);
                writeChannel.close();
            }
            if (isOpen(readChannel)) {
                log.debug("Closing read channel for {}", path.getFileName());
                readChannel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
