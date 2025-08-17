package com.github.mangila.pokedex.database.internal.io;

import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

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

    public void write(ByteBuffer buffer, long position) throws IOException {
        if (!databaseFile.isWritable()) {
            throw new IOException("%s is not writable".formatted(databaseFile.getPath()));
        }
        Ensure.min(0, position);
        Ensure.notEquals(0, buffer.capacity(), "Buffer capacity must be greater than zero for writing");
        long writtenBytes = databaseFile.getWriteChannel()
                .write(buffer, position);
        LOGGER.debug("Wrote: {} bytes to file at position {}", writtenBytes, position);
    }

    public ByteBuffer readAndFlip(ByteBuffer buffer, long position) throws IOException {
        if (!databaseFile.isReadable()) {
            throw new IOException("%s is not readable".formatted(databaseFile.getPath()));
        }
        Ensure.min(0, position);
        Ensure.notEquals(0, buffer.capacity(), "Buffer capacity must be greater than zero for reading");
        databaseFile.getReadChannel().read(buffer, position);
        buffer.flip();
        LOGGER.debug("Read: {} bytes from file at position {}", buffer.limit(), position);
        return buffer;
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
