package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class DataFileHeaderHandler {

    private static final Logger log = LoggerFactory.getLogger(DataFileHeaderHandler.class);
    private final File file;
    private final AtomicReference<DataFileHeader> header;

    public DataFileHeaderHandler(File file) {
        this.file = file;
        this.header = new AtomicReference<>(DataFileHeader.defaultValue());
    }

    public long getOffset() {
        return getHeader().offset();
    }

    public DataFileHeader getHeader() {
        return header.getAcquire();
    }

    public void write() throws IOException {
        log.debug("Writing header {} to file {}", getHeader(), file.getPath().getFileName());
        var buffer = BufferUtils.newByteBuffer(DataFileHeader.HEADER_SIZE);
        var fileHeader = getHeader();
        fileHeader.fillAndFlip(buffer);
        file.write(buffer, 0);
    }

    public void load() throws IOException {
        log.debug("Load header from file {}", file.getPath().getFileName());
        var buffer = file.readAndFlip(
                0,
                DataFileHeader.HEADER_SIZE
        );
        header.updateAndGet(fileHeader -> {
            var magic = new byte[DataFileHeader.MAGIC_NUMBER_SIZE];
            buffer.get(magic);
            ArrayUtils.ensureArrayEquals(magic, DataFileHeader.MAGIC_NUMBER_BYTES);
            var loadedFileHeader = new DataFileHeader(
                    magic,
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getLong()
            );
            log.debug("Loaded header {}", loadedFileHeader);
            return loadedFileHeader;
        });
    }

    public void update(long newOffset) throws IOException {
        header.updateAndGet(fileHeader -> new DataFileHeader(
                fileHeader.magicNumber(),
                fileHeader.version(),
                fileHeader.incrementRecordCount(),
                newOffset
        ));
        write();
    }

    public void truncate() throws IOException {
        header.set(DataFileHeader.defaultValue());
        write();
    }
}
