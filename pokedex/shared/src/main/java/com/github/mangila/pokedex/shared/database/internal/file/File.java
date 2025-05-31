package com.github.mangila.pokedex.shared.database.internal.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File structure layout:
 * <p>
 * [HEADER SECTION]
 * - Magic Number ("Pok3mon1" bytes) - File identifier
 * - Version (4 bytes) - File format version number
 * - Record Count (4 bytes) - Number of Pokemon records
 * - Index Offset (8 bytes) - Start position of the index section
 * - Data Offset (8 bytes) - Start position of the data section
 * <p>
 * [INDEX SECTION]
 * Sequential key-offset mapping entries:
 * - Entry Format:
 * - Key Length (4 bytes)
 * - Key Bytes (variable length)
 * - Data Offset (8 bytes) - Points to record in the data section
 * <p>
 * [DATA SECTION]
 * Sequential Pokemon record entries:
 * - Record Format:
 * - Length (4 bytes) - Size of serialized data
 * - Version (4 bytes) - Version of the record - if the version is an odd number, file write is ongoing
 * - Serialized Pokemon Data (variable length)
 * - CRC32C Checksum (8 bytes) - Data integrity validation
 */
public class File {

    private static final Logger log = LoggerFactory.getLogger(File.class);

    private final Path path;
    private FileChannel writeChannel;
    private FileChannel readChannel;

    public File(FileName fileName) {
        try {
            this.path = Paths.get(fileName.value());
            if (!exists()) {
                log.info("Creating new file {}", path.getFileName());
                Files.createFile(path);
            }
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public FileChannel getReadChannel() throws IOException {
        if (readChannel == null || !readChannel.isOpen()) {
            log.debug("Opening read channel for {}", path.getFileName());
            this.readChannel = FileChannel.open(
                    path,
                    FileOptions.READ_OPTIONS
            );
        }
        return readChannel;
    }

    public FileChannel getWriteChannel() throws IOException {
        if (writeChannel == null || !writeChannel.isOpen()) {
            log.debug("Opening write channel for {}", path.getFileName());
            this.writeChannel = FileChannel.open(
                    path,
                    FileOptions.WRITE_OPTIONS
            );
        }
        return writeChannel;
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

    public Path getPath() {
        return path;
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean isEmpty() {
        return exists() && path.toFile().length() == 0;
    }

    public void deleteFile() throws IOException {
        writeChannel.close();
        readChannel.close();
        Files.deleteIfExists(path);
    }
}
