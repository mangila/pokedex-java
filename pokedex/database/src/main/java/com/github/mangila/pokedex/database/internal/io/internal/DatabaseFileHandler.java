package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.file.FileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.Record;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * Handles operations with a database file in terms of reading, writing,
 * opening, closing, truncating, and deleting its contents. This class
 * provides standardized methods to interact with file channels.
 * <br>
 * It operates within the context of the {@code DatabaseFile} instance
 * which encapsulates the actual file path and channels for reading and
 * writing operations.
 */
public record DatabaseFileHandler(DatabaseFile databaseFile) {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileHandler.class);

    /**
     * A set of {@link StandardOpenOption} options specifying how the file
     * should be opened or created when performing write operations.
     * These options include:
     * - {@link StandardOpenOption#READ}: Ensures the file is readable.
     * - {@link StandardOpenOption#WRITE}: Allows writing to the file.
     * - {@link StandardOpenOption#SYNC}: Requires changes to be written synchronously.
     * <p>
     * This set is immutable and is used to standardize the file opening behavior
     * for write operations in the context of the DatabaseFileHandler.
     */
    private static final Set<StandardOpenOption> WRITE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC);

    /**
     * A set of {@link StandardOpenOption} options specifying how the file
     * should be opened when performing read operations.
     * <p>
     * This set only includes {@link StandardOpenOption#READ}, ensuring that
     * the file is opened in read-only mode. The set is immutable and is used
     * to standardize the file opening behavior for read operations within
     * the context of the {@code DatabaseFileHandler}.
     */
    private static final Set<StandardOpenOption> READ_OPTIONS = EnumSet.of(
            StandardOpenOption.READ);

    public void init() throws IOException {
        LOGGER.debug("Initializing file {}", databaseFile.getPath());
        create();
        openReadChannel();
        openWriteChannel();
    }

    public void write(Record record) throws IOException {
        Ensure.isTrue(databaseFile.isWritable(), "File is not writable");
        long currentFileSize = databaseFile.size();
        // Exclusive lock for writing, basically a Write lock
        boolean shared = false;
        LOGGER.debug("Try to acquire exclusive lock for writing");
        try (FileLock headerLock = databaseFile.getWriteChannel().lock(0, DatabaseFileHeader.HEADER_SIZE.value(), shared)) {
            DatabaseFileHeader currentHeader = readHeader();
            long position = currentHeader.offset().value();
            long writtenBytes = databaseFile.getWriteChannel()
                    .write(record.value(), position);
            LOGGER.debug("Wrote new Record: {} bytes to file {} at position {}", writtenBytes, databaseFile.getPath(), position);
            long newOffset = currentHeader.offset().value() + record.value().capacity();
            DatabaseFileHeader newHeader = new DatabaseFileHeader(
                    currentHeader.magicNumber(),
                    currentHeader.version(),
                    DatabaseFileHeader.RecordCount.increment(currentHeader.recordCount()),
                    new Offset(newOffset)
            );
            write(newHeader);
        } catch (IOException e) {
            LOGGER.error("Error while writing header to file {} will try to rollback to old size {}",
                    databaseFile.getPath(),
                    currentFileSize,
                    e);
            databaseFile.getWriteChannel().truncate(currentFileSize);
            throw e;
        }
    }

    public void write(DatabaseFileHeader header) throws IOException {
        Ensure.isTrue(databaseFile.isWritable(), "File is not writable");
        long writtenBytes = databaseFile.getWriteChannel().write(header.toByteBuffer(Boolean.TRUE), 0);
        LOGGER.debug("Updated Header: {} bytes to file {} at position {}", writtenBytes, databaseFile.getPath(), 0);
    }

    public ByteBuffer read(ByteBuffer readBuffer,
                           long position,
                           boolean flip) throws IOException {
        Ensure.isTrue(databaseFile.isReadable(), "File is not readable");
        Ensure.min(0, position);
        databaseFile.getReadChannel().read(readBuffer, position);
        if (flip) {
            LOGGER.debug("Flip buffer");
            readBuffer.flip();
        }
        LOGGER.debug("Read: {} bytes from file at position {}", readBuffer.limit(), position);
        return readBuffer;
    }

    /**
     * Convenient method for reading the header from the file.
     *
     * @return the header read from the file.
     */
    public DatabaseFileHeader readHeader() throws IOException {
        ByteBuffer buffer = read(ByteBuffer.allocate(FileHeader.HEADER_SIZE), 0, true);
        return DatabaseFileHeader.from(buffer);
    }

    public void openReadChannel() throws IOException {
        if (!databaseFile.isReadable()) {
            databaseFile.setReadChannel(
                    FileChannel.open(databaseFile.getPath(), READ_OPTIONS)
            );
        } else {
            LOGGER.warn("Read channel already open for {}", databaseFile.getPath());
        }
    }

    public void closeReadChannel() throws IOException {
        if (databaseFile.isReadable()) {
            databaseFile.getReadChannel().close();
        } else {
            LOGGER.warn("Read channel already closed for {}", databaseFile.getPath());
        }
    }

    public void openWriteChannel() throws IOException {
        if (!databaseFile.isWritable()) {
            databaseFile.setWriteChannel(
                    FileChannel.open(databaseFile.getPath(), WRITE_OPTIONS)
            );
        } else {
            LOGGER.warn("Write channel already open for {}", databaseFile.getPath());
        }
    }

    public void closeWriteChannel() throws IOException {
        if (databaseFile.isWritable()) {
            databaseFile.getWriteChannel().close();
        } else {
            LOGGER.warn("Write channel already closed for {}", databaseFile.getPath());
        }
    }

    public void create() throws IOException {
        if (Files.exists(databaseFile.getPath())) {
            LOGGER.info("File {} already exists", databaseFile.getPath());
        } else {
            LOGGER.info("Creating file {}", databaseFile.getPath());
            Files.createFile(databaseFile.getPath());
        }
    }

    public void truncate() throws IOException {
        LOGGER.debug("Truncating file {}", databaseFile.getPath());
        if (databaseFile.isWritable() && databaseFile.isReadable()) {
            databaseFile.getWriteChannel().truncate(0);
            databaseFile.getWriteChannel().position(0);
            databaseFile.getReadChannel().position(0);
        } else {
            throw new IOException("Cannot truncate file %s".formatted(databaseFile.getPath()));
        }
    }

    public void delete() throws IOException {
        truncate();
        closeWriteChannel();
        closeReadChannel();
        Files.delete(databaseFile.getPath());
    }
}
