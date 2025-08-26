package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class WalFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFile.class);
    private final Path path;
    private final WalFileChannel walFileChannel;

    WalFile(Path path) throws IOException {
        this.path = path;
        this.walFileChannel = new WalFileChannel(FileChannel.open(
                path,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE)
        );
    }

    void write(Buffer buffer) throws IOException {
        int writtenBytes = walFileChannel.write(buffer);
        if (buffer.remaining() != 0) {
            LOGGER.error("Not all bytes written: {} bytes written, {} bytes remaining", writtenBytes, buffer.remaining());
            // TODO: write the missing bytes to the file
            throw new IllegalStateException("Not all bytes written: " + writtenBytes);
        }
    }

    private static class WalFileChannel {
        private final FileChannel fileChannel;

        WalFileChannel(FileChannel fileChannel) {
            this.fileChannel = fileChannel;
        }

        int write(Buffer buffer) throws IOException {
            return fileChannel.write(buffer.value());
        }

        void close() throws IOException {
            fileChannel.close();
        }
    }

}
