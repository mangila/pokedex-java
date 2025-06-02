package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FileHandler<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final DataFileHandler dataFileHandler;
    private final IndexFileHandler indexFileHandler;

    public FileHandler(DatabaseName databaseName) {
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.indexFileHandler = new IndexFileHandler(databaseName);
    }

    public int write(String key, V value) {
        if (indexFileHandler.hasIndex(key)) {
            // update record
        } else {
            try {
                var pair = dataFileHandler.write(value.serialize());
                long dataOffset = pair.first();
                long newDataOffset = pair.second();
                dataFileHandler.updateHeader(newDataOffset);
                long newIndexOffset = indexFileHandler.write(key, dataOffset);
                indexFileHandler.updateHeader(newIndexOffset);
                indexFileHandler.putIndex(key, dataOffset);
            } catch (IOException e) {
                log.error("ERR", e);
                return -1;
            }
        }
        return 1;
    }

    public byte[] read(String key) {
        try {
            if (!indexFileHandler.hasIndex(key)) {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            long dataOffset = indexFileHandler.getDataOffset(key);
            var record = dataFileHandler.read(dataOffset);
            log.debug("Read record {}", record);
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
