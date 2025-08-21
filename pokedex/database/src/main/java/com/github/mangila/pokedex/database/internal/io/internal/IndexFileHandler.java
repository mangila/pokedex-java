package com.github.mangila.pokedex.database.internal.io.internal;


import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.internal.model.IndexEntry;
import com.github.mangila.pokedex.database.internal.io.internal.model.*;
import com.github.mangila.pokedex.database.internal.model.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private final Map<Key, Offset> keyToOffset;

    public IndexFileHandler(DatabaseName databaseName) {
        DatabaseFileName databaseFileName = new DatabaseFileName(databaseName.value()
                .concat(".index")
                .concat(".yakvs"));
        DatabaseFile databaseFile = new DatabaseFile(databaseFileName);
        this.databaseFileHandler = new DatabaseFileHandler(databaseFile);
        this.keyToOffset = new ConcurrentHashMap<>();
    }

    public void init() throws IOException {
        databaseFileHandler.init();
        DatabaseFileHeader header = databaseFileHandler.fileAccess()
                .readHeader();
        keyToOffset.putAll(loadKeys(header));
    }

    public void truncate() throws IOException {
        LOGGER.info("Truncating index file");
        keyToOffset.clear();
        databaseFileHandler.fileModification().truncate();
    }

    public void delete() throws IOException {
        LOGGER.info("Deleting index file");
        keyToOffset.clear();
        databaseFileHandler.fileAccess().channelHandler().close();
        databaseFileHandler.fileModification().delete();
    }

    public int size() {
        return keyToOffset.size();
    }

    public Offset get(Key key) {
        return keyToOffset.get(key);
    }

    public void append(Key key, Offset offset) throws IOException {
        IndexEntry indexEntry = new IndexEntry(key, offset);
        Buffer buffer = indexEntry.toBuffer(true);
        OffsetBoundary boundary = databaseFileHandler.fileAccess()
                .append(buffer);
        keyToOffset.put(key, boundary.start());
        LOGGER.debug("Appended index entry {} - {}", key, boundary);
    }

    private Map<Key, Offset> loadKeys(DatabaseFileHeader header) throws IOException {
        Offset offset = DatabaseFileHeader.HEADER_OFFSET_BOUNDARY.end();
        DatabaseFileHeader.RecordCount recordCount = header.recordCount();
        Map<Key, Offset> indexes = new HashMap<>();
        for (int i = 0; i < recordCount.value(); i++) {
            Buffer keyLength = databaseFileHandler.fileAccess().read(
                    Buffer.from(Integer.BYTES),
                    offset,
                    true);
            Buffer keyBuffer = databaseFileHandler.fileAccess().read(
                    Buffer.from(keyLength.getInt()),
                    new Offset(offset.value() + Integer.BYTES),
                    true);
            Buffer dataOffsetBuffer = databaseFileHandler.fileAccess().read(
                    Buffer.from(Long.BYTES),
                    new Offset(offset.value() + Integer.BYTES + keyBuffer.length()),
                    true);
            Offset dataOffset = new Offset(dataOffsetBuffer.getLong());
            Key key = new Key(new String(keyBuffer.getArray()));
            LOGGER.debug("Loaded index entry {} - {}", key, dataOffset);
            indexes.put(key, dataOffset);
            offset = new Offset(offset.value() + Integer.BYTES + keyBuffer.length() + Long.BYTES);
        }
        LOGGER.debug("Loaded {} index entries", indexes.size());
        return indexes;
    }
}
