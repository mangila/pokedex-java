package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
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

    public void read(Buffer buffer, Offset offset) throws IOException {
        channel.read(buffer.value(), offset.value());
    }

    public void write(Buffer buffer, Offset offset) throws IOException {
        channel.write(buffer.value(), offset.value());
    }

    public FileLock acquireLock(OffsetBoundary boundary, boolean shared) throws IOException {
        Offset start = boundary.start();
        Offset end = boundary.end();
        return channel.lock(start.value(), end.value(), shared);
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
        return channel.size();
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public String fileName() {
        return databaseFile.path().getFileName().toString();
    }
}
