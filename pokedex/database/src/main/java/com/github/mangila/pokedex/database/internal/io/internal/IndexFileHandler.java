package com.github.mangila.pokedex.database.internal.io.internal;


import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.file.FileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.model.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.model.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Index File Structure Layout
 * <p>
 * This file maintains a mapping between record keys and their respective
 * offsets in the data file, enabling efficient lookups without scanning
 * the entire data file.
 * </p>
 *
 * <h3>Header Section</h3>
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvs" bytes     | File identifier|
 * | Version        | 4 bytes           | Format version |
 * | Record Count   | 4 bytes           | Num records    |
 * | Next Offset    | 8 bytes           | Write position |
 * +----------------+--------------------+----------------+
 * </pre>
 *
 * <h3>Index File Entries</h3>
 * <pre>
 * +---------------+------------+--------------------------------+
 * | Field         | Size       | Description                    |
 * +---------------+------------+--------------------------------+
 * | Key Length    | 4 bytes    | Length of key bytes            |
 * | Key Bytes     | Variable   | Key data                       |
 * | Data Offset   | 8 bytes    | Points to record in data file  |
 * +---------------+------------+--------------------------------+
 * </pre>
 */
public class IndexFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexFileHandler.class);
    private final DatabaseFileHandler databaseFileHandler;
    private final Map<Key, Offset> keys;

    public IndexFileHandler(DatabaseName databaseName) {
        DatabaseFileName databaseFileName = new DatabaseFileName(databaseName.value()
                .concat(".data")
                .concat(".yakvs"));
        DatabaseFile databaseFile = new DatabaseFile(databaseFileName);
        this.databaseFileHandler = new DatabaseFileHandler(databaseFile);
        this.keys = new ConcurrentHashMap<>();
    }

    public void init() throws IOException {
        databaseFileHandler.init();
        if (databaseFileHandler.databaseFile().isEmpty()) {
            LOGGER.info("Creating new index file");
            DatabaseFileHeader emptyHeader = DatabaseFileHeader.EMPTY;
            databaseFileHandler.write(emptyHeader);
        } else {
            LOGGER.info("Loading existing index file");
            DatabaseFileHeader header = databaseFileHandler.readHeader();
            keys.putAll(loadKeys(header));
        }
    }

    private Map<Key, Offset> loadKeys(DatabaseFileHeader header) throws IOException {
        long position = FileHeader.HEADER_SIZE;
        int recordCount = header.recordCount().value();
        Map<Key, Offset> indexes = new HashMap<>();
        for (int i = 0; i < recordCount; i++) {
            ByteBuffer keyLengthBuffer = databaseFileHandler.read(ByteBuffer.allocate(Integer.BYTES), position, true);
            int keyLength = keyLengthBuffer.getInt();
            byte[] keyBytes = new byte[keyLength];
            ByteBuffer keyBuffer = databaseFileHandler.read(ByteBuffer.allocate(keyLength), position + Integer.BYTES, true);
            keyBuffer.get(keyBytes);
            Key key = new Key(new String(keyBytes));
            ByteBuffer dataOffsetBuffer = databaseFileHandler.read(ByteBuffer.allocate(Long.BYTES), position + Integer.BYTES + keyLength, true);
            Offset offset = new Offset(dataOffsetBuffer.getLong());
            LOGGER.debug("Loaded index entry for key {} and offset {}", key, offset);
            position += Integer.BYTES + keyLength + Long.BYTES;
            indexes.put(key, offset);
        }
        return indexes;
    }

    public Offset get(Key key) {
        return keys.get(key);
    }

    public void put(Key key, Offset offset) {
        keys.put(key, offset);
    }
}
