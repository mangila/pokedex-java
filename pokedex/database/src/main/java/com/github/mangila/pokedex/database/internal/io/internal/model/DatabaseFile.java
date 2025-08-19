package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.database.internal.io.model.DatabaseFileName;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
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

    public boolean exists() {
        return Files.exists(path);
    }

    public long size() throws IOException {
        return Files.size(path);
    }

    public boolean isEmpty() throws IOException {
        return exists() && size() == 0;
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
