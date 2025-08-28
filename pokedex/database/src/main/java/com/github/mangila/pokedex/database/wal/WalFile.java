package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

class WalFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFile.class);

    private final Path path;
    private final WalFileChannel walFileChannel;
    private final AtomicLong size;

    WalFile(Path path) throws IOException {
        this.path = path;
        this.walFileChannel = new WalFileChannel(FileChannel.open(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND)
        );
        this.size = new AtomicLong(walFileChannel.size());
    }

    void write(Buffer buffer) throws IOException {
        int writtenBytes = walFileChannel.write(buffer);
        if (buffer.remaining() != 0) {
            LOGGER.error("Not all bytes written: {} bytes written, {} bytes remaining", writtenBytes, buffer.remaining());
            // TODO: write the missing bytes to the file
            throw new IllegalStateException("Not all bytes written: " + writtenBytes);
        }
        size.addAndGet(writtenBytes);
    }

    public long size() {
        return size.get();
    }

    public Path path() {
        return path;
    }

    public void close() throws IOException {
        walFileChannel.close();
    }

    public void flush() throws IOException {
      var m =   walFileChannel.fileChannel.map(null,0,0);
      m.compact()
        walFileChannel.flush();
    }

    public void sync() throws IOException {
        walFileChannel.sync();
    }

    private record WalFileChannel(FileChannel fileChannel) {

        int write(Buffer buffer) throws IOException {
            return fileChannel.write(buffer.value());
        }

        void flush() throws IOException {
            fileChannel.force(false);
        }

        void sync() throws IOException {
            fileChannel.force(true);
        }

        long size() throws IOException {
            return fileChannel.size();
        }

        void close() throws IOException {
            fileChannel.close();
        }
    }

}
