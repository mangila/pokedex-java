package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

class WalFile {
    private static final long DEFAULT_SIZE = 1024 * 32;
    private final Path path;
    private final long totalSize;
    private final RandomAccessFile file;
    private final FileChannel fileChannel;
    private final MappedBuffer mappedBuffer;

    WalFile(Path path, long totalSize) throws IOException {
        Ensure.notNull(path);
        Ensure.min(0, totalSize);
        this.path = path;
        this.totalSize = totalSize;
        this.file = new RandomAccessFile(path.toFile(), "rw");
        file.setLength(totalSize);
        this.fileChannel = file.getChannel();
        this.mappedBuffer = new MappedBuffer(fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, totalSize));
    }

    public WalFile(Path path) throws IOException {
        this(path, DEFAULT_SIZE);
    }

    public Path getPath() {
        return path;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public RandomAccessFile getFile() {
        return file;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public MappedBuffer getMappedBuffer() {
        return mappedBuffer;
    }
}
