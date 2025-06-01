package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.data.DataFileHandler;
import com.github.mangila.pokedex.shared.database.internal.file.index.IndexFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FileHandler<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final IndexFileHandler indexFileHandler;
    private final DataFileHandler<V> dataFileHandler;

    public FileHandler(DatabaseName databaseName) {
        this.indexFileHandler = new IndexFileHandler(databaseName);
        this.dataFileHandler = new DataFileHandler<>(databaseName);
    }

    public int write(String key, V value) {
        if (indexFileHandler.hasIndex(key)) {
            // update record
        } else {
            // insert record
            long dataOffset = dataFileHandler.getOffset();
            try {
                dataFileHandler.writeNewRecord(key, value);
                indexFileHandler.writeNewIndex(key, dataOffset);
            } catch (IOException e) {
                log.error("ERR", e);
                return -1;
            }
        }
        return 0;
    }

    public byte[] read(String key) {
        if (!indexFileHandler.hasIndex(key)) {
            return null;
        }
        long offset = indexFileHandler.getOffset(key);
        try {
            return dataFileHandler.read(offset);
        } catch (IOException e) {
            log.error("ERR", e);
            return null;
        }
    }

    public void init() throws IOException {
        indexFileHandler.init();
        dataFileHandler.init();
    }

    public void deleteFile() throws IOException {
        indexFileHandler.deleteFile();
        dataFileHandler.deleteFile();
    }
}
