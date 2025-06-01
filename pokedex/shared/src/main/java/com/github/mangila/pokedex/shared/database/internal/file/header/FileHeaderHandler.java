package com.github.mangila.pokedex.shared.database.internal.file.header;

import com.github.mangila.pokedex.shared.database.internal.file.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicReference;

public class FileHeaderHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHeaderHandler.class);

    private final AtomicReference<FileHeader> header = new AtomicReference<>(FileHeader.defaultValue());
    private final File file;

    public FileHeaderHandler(File file) {
        this.file = file;
    }

    public long getOffset() {
        return header.getAcquire().offset();
    }

    public int getRecordCount() {
        return header.getAcquire().recordCount();
    }

    public void updateNewWrite(long newOffset) throws IOException {
        header.updateAndGet(fileHeader -> new FileHeader(
                fileHeader.magicNumber(),
                fileHeader.version(),
                fileHeader.incrementRecordCount(),
                newOffset));
        writeHeaderToFile();
    }

    public void writeHeaderToFile() throws IOException {
        log.debug("Writing header to file {}", file.getPath().getFileName());
        var buffer = file.getWriteChannel().map(
                FileChannel.MapMode.READ_WRITE,
                0,
                FileHeader.HEADER_SIZE
        );
        var h = header.getAcquire();
        h.fill(buffer);
        buffer.force();
    }

    public void loadHeader() throws IOException {
        log.debug("Load header from file {}", file.getPath().getFileName());
        var buffer = file.getReadChannel().map(
                FileChannel.MapMode.READ_ONLY,
                0,
                FileHeader.HEADER_SIZE
        );
        header.updateAndGet(fileHeader -> {
            var magic = new byte[FileHeader.POKEMON_MAGIC_NUMBER_SIZE];
            buffer.get(magic);
            FileHeader.ensureValidMagicHeader(magic);
            var loadedFileHeader = new FileHeader(
                    magic,
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getLong()
            );
            log.debug("Loaded header {}", loadedFileHeader);
            return loadedFileHeader;
        });
    }
}
