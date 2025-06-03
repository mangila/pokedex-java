package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final DataFileHandler dataFileHandler;
    private final IndexFileHandler indexFileHandler;

    public FileHandler(DatabaseName databaseName) {
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.indexFileHandler = new IndexFileHandler(databaseName);
    }

    public boolean write(String key, byte[] value) {
        try {
            boolean shouldUpdate = indexFileHandler.hasIndex(key);
            if (shouldUpdate) {
                long dataOffset = indexFileHandler.getDataOffset(key);
                var resultPair = dataFileHandler.updateIfSameSize(dataOffset, value);
                boolean result = resultPair.first();
                int sizeOfRecord = resultPair.second();
                if (!result) {
                    // TODO flag space as free
                    writeNewRecord(key, value);
                    result = true;
                }
                return result;
            }
            writeNewRecord(key, value);
            return Boolean.TRUE;
        } catch (IOException e) {
            log.error("ERR", e);
            return Boolean.FALSE;
        }
    }

    private void writeNewRecord(String key, byte[] value) throws IOException {
        var pair = dataFileHandler.write(value);
        var record = pair.first();
        var boundary = pair.second();
        dataFileHandler.updateHeader(boundary.newOffset());
        long newIndexOffset = indexFileHandler.write(key, boundary.oldOffset());
        indexFileHandler.updateHeader(newIndexOffset);
        indexFileHandler.putIndex(key, boundary.oldOffset());
        log.debug("Wrote new record with key {} and offset {} -- {}", key, boundary.oldOffset(), record);
    }

    public byte[] read(String key) {
        try {
            if (!indexFileHandler.hasIndex(key)) {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            long dataOffset = indexFileHandler.getDataOffset(key);
            var record = dataFileHandler.read(dataOffset);
            return record.data();
        } catch (IOException e) {
            log.error("ERR", e);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public void init() throws IOException {
        indexFileHandler.init();
        dataFileHandler.init();
    }

    public void deleteFiles() throws IOException {
        indexFileHandler.deleteFile();
        dataFileHandler.deleteFile();
    }

    public void truncateFiles() throws IOException {
        indexFileHandler.truncate();
        dataFileHandler.truncate();
    }

    public boolean isEmpty() {
        return indexFileHandler.isEmpty();
    }
}
