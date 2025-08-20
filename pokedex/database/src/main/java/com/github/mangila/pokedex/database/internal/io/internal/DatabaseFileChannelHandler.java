package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

public class DatabaseFileChannelHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileChannelHandler.class);
    private static final Set<StandardOpenOption> FILE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC);
    private final DatabaseFile databaseFile;
    private FileChannel channel;

    public DatabaseFileChannelHandler(DatabaseFile databaseFile) {
        this.databaseFile = databaseFile;
    }

    public long read(ByteBuffer readBuffer, long position) throws IOException {
        long bytesRead = channel.read(readBuffer, position);
        LOGGER.debug("Read {} bytes from position {}", bytesRead, position);
        return bytesRead;
    }

    public void write(ByteBuffer writeBuffer, long position) throws IOException {
        long writtenBytes = channel.write(writeBuffer, position);
        LOGGER.debug("Wrote {} bytes to position {}", writtenBytes, position);
    }

    public FileLock acquireLock(long position, long size, boolean shared) throws IOException {
        LOGGER.debug("Acquiring {} lock for position {} and size {}", shared ? "shared" : "exclusive", position, size);
        FileLock lock = channel.lock(position, size, shared);
        LOGGER.debug("Acquired {} lock for position {} and size {}", shared ? "shared" : "exclusive", position, size);
        return lock;
    }

    public void open() throws IOException {
        if (!isOpen()) {
            LOGGER.info("Opening FileChannel {}", databaseFile.path());
            channel = FileChannel.open(
                    databaseFile.path(),
                    FILE_OPTIONS
            );
        } else {
            LOGGER.info("FileChannel {} already open", databaseFile.path());
        }
    }

    public void close() throws IOException {
        if (isOpen()) {
            LOGGER.info("Closing FileChannel {}", databaseFile.path());
            channel.close();
        } else {
            LOGGER.info("FileChannel {} already closed", databaseFile.path());
        }
    }

    public long size() throws IOException {
        long size = channel.size();
        LOGGER.debug("FileChannel {} size is {}", databaseFile.path(), size);
        return size;
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public String fileName() {
        return databaseFile.path().getFileName().toString();
    }
}
