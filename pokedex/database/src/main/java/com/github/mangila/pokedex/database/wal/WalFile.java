package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

class WalFile {
    private static final int DEFAULT_SIZE = 1024 * 32;
    // 2GB is the max size for mmap, but we want to avoid OOMs.
    private static final int MAX_SIZE_ONE_GB = 1024 * 1024 * 1024;
    private final Path path;
    private final int totalSize;
    private final RandomAccessFile file;
    private final FileChannel fileChannel;
    private final MappedBuffer mappedBuffer;

    WalFile(Path path, int totalSize) throws IOException {
        Ensure.notNull(path);
        Ensure.min(0, totalSize);
        Ensure.max(MAX_SIZE_ONE_GB, totalSize);
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
