package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.internal.file.header.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * File structure layout:
 * <p>
 * [DATA FILE]
 * Sequential Pokemon record entries:
 * - Record Format:
 * - Length (4 bytes) - Size of serialized record
 * - Serialized Pokemon Data (variable length)
 * - CRC32C Checksum (8 bytes) - Data integrity validation
 */
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

    public File(DatabaseFileName fileName) {
        this.path = Paths.get(fileName.value());
    }

    public MappedByteBuffer getFileRegion(
            FileChannel.MapMode mode,
            long position,
            long size
    ) throws IOException {
        return switch (mode.toString()) {
            case "READ_ONLY" -> getReadChannel().map(mode, position, size);
            case "READ_WRITE" -> getWriteChannel().map(mode, position, size);
            default -> throw new IllegalArgumentException("Unsupported MapMode: " + mode);
        };
    }

    public long getFileSize() {
        return path.toFile().length();
    }

    public long getFileSizeExcludingHeader() {
        return getFileSize() - FileHeader.HEADER_SIZE;
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
}
