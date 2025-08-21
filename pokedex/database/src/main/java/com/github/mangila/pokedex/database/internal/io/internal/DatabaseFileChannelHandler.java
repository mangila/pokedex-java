package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class DatabaseFileChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileChannelHandler.class);
    private final DatabaseFile databaseFile;
    private FileChannel channel;

    public DatabaseFileChannelHandler(DatabaseFile databaseFile) {
        this.databaseFile = databaseFile;
    }

    public long read(Buffer buffer, Offset offset) throws IOException {
        long bytesRead = channel.read(buffer.value(), offset.value());
        LOGGER.debug("Read {} bytes from offset {}", bytesRead, offset);
        return bytesRead;
    }

    public void write(Buffer buffer, Offset offset) throws IOException {
        long writtenBytes = channel.write(buffer.value(), offset.value());
        LOGGER.debug("Wrote {} bytes to offset {}", writtenBytes, offset);
    }

    public FileLock acquireLock(OffsetBoundary boundary, boolean shared) throws IOException {
        Offset start = boundary.start();
        Offset end = boundary.end();
        LOGGER.debug("Acquiring {} lock for position {} and size {}", shared ? "shared" : "exclusive", start, end);
        FileLock lock = channel.lock(boundary.start().value(), boundary.end().value(), shared);
        LOGGER.debug("Acquired {} lock for position {} and size {}", shared ? "shared" : "exclusive", start, end);
        return lock;
    }

    public void open() throws IOException {
        if (!isOpen()) {
            LOGGER.info("Opening FileChannel {}", databaseFile.path());
            channel = FileChannel.open(
                    databaseFile.path(),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC
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
