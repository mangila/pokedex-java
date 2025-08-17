package com.github.mangila.pokedex.database.internal.io;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseFile {
    private final Path path;
    private FileChannel writeChannel;
    private FileChannel readChannel;

    public DatabaseFile(DatabaseFileName fileName) {
        this.path = Paths.get(fileName.value());
    }

    public Path getPath() {
        return path;
    }

    public FileChannel getWriteChannel() {
        return writeChannel;
    }

    public void setWriteChannel(FileChannel writeChannel) {
        this.writeChannel = writeChannel;
    }

    public FileChannel getReadChannel() {
        return readChannel;
    }

    public void setReadChannel(FileChannel readChannel) {
        this.readChannel = readChannel;
    }

    public boolean isReadable() {
        return isOpen(readChannel);
    }

    public boolean isWritable() {
        return isOpen(writeChannel);
    }

    private boolean isOpen(FileChannel channel) {
        return channel != null && channel.isOpen();
    }
}
