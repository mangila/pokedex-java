package com.github.mangila.pokedex.shared.database.internal.file.index;

import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class IndexFileHeaderHandler {

    private static final Logger log = LoggerFactory.getLogger(IndexFileHeaderHandler.class);
    private final File file;
    private final AtomicReference<IndexFileHeader> header;

    public IndexFileHeaderHandler(File file) {
        this.file = file;
        this.header = new AtomicReference<>(IndexFileHeader.defaultValue());
    }

    public void write() throws IOException {
        log.debug("Writing header {} to file {}", header.get(), file.getPath().getFileName());
        var buffer = BufferUtils.newByteBuffer(IndexFileHeader.HEADER_SIZE);
        var fileHeader = getHeader();
        fileHeader.fillAndFlip(buffer);
        file.write(buffer, 0);
    }

    public void update(long newOffset) throws IOException {
        header.updateAndGet(fileHeader -> new IndexFileHeader(
                fileHeader.magicNumber(),
                fileHeader.version(),
                fileHeader.incrementRecordCount(),
                newOffset
        ));
        write();
    }

    public IndexFileHeader getHeader() {
        return header.getAcquire();
    }

    public void loadHeader() throws IOException {
        log.debug("Load header from file {}", file.getPath().getFileName());
        var buffer = file.readAndFlip(0, IndexFileHeader.HEADER_SIZE);
        header.updateAndGet(fileHeader -> {
            var magic = new byte[IndexFileHeader.MAGIC_NUMBER_SIZE];
            buffer.get(magic);
            ArrayUtils.ensureArrayEquals(magic, IndexFileHeader.MAGIC_NUMBER_BYTES);
            var loadedFileHeader = new IndexFileHeader(
                    magic,
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getLong()
            );
            log.debug("Loaded header {}", loadedFileHeader);
            return loadedFileHeader;
        });
    }

    // TODO: read chunked
    public Map<String, Long> loadIndexes() throws IOException {
        log.debug("Load indexes from file {}", file.getPath().getFileName());
        long size = file.getFileSize() - IndexFileHeader.HEADER_SIZE;
        var buffer = file.readFileRegion(
                IndexFileHeader.HEADER_SIZE,
                size);
        int recordCount = getHeader().recordCount();
        var indexMap = new HashMap<String, Long>();
        for (int i = 0; i < recordCount; i++) {
            int keyLength = buffer.getInt();
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);
            long dataPos = buffer.getLong();
            String key = new String(keyBytes);
            indexMap.put(key, dataPos);
        }
        return indexMap;
    }

    public long getOffset() {
        return getHeader().offset();
    }

    public void truncate() throws IOException {
        header.set(IndexFileHeader.defaultValue());
        write();
    }
}
