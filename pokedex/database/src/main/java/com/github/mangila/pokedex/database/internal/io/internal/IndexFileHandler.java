package com.github.mangila.pokedex.database.internal.io.internal;


import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
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
public final class IndexFileHandler extends AbstractFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexFileHandler.class);
    private final IndexMap indexMap;

    public IndexFileHandler(DatabaseFileName databaseFileName) {
        super(new DatabaseFile(databaseFileName));
        this.indexMap = new IndexMap(new ConcurrentHashMap<>());
    }

    public IndexMap indexMap() {
        return indexMap;
    }

    public void loadIndexes() throws IOException {
        DatabaseFileHeader header = fileAccess().readHeader();
        var loadedIndexes = loadIndexes(header);
        LOGGER.info("Loaded {} index entries from {}", loadedIndexes.size(), fileName());
        indexMap.putAll(loadedIndexes);
    }

    private Map<Key, Offset> loadIndexes(DatabaseFileHeader header) throws IOException {
        Offset offset = DatabaseFileHeader.HEADER_OFFSET_BOUNDARY.end();
        DatabaseFileHeader.RecordCount recordCount = header.recordCount();
        Map<Key, Offset> indexes = new HashMap<>(recordCount.value());
        Buffer keyLengthBuffer = Buffer.from(Integer.BYTES);
        Buffer dataOffsetBuffer = Buffer.from(Long.BYTES);
        for (int i = 0; i < recordCount.value(); i++) {
            fileAccess().read(
                    keyLengthBuffer,
                    offset,
                    true);
            Buffer keyDataBuffer = Buffer.from(keyLengthBuffer.getInt());
            fileAccess().read(
                    keyDataBuffer,
                    new Offset(offset.value() + Integer.BYTES),
                    true);
            Key key = new Key(new String(keyDataBuffer.getArray()));
            fileAccess().read(
                    dataOffsetBuffer,
                    new Offset(offset.value() + Integer.BYTES + key.length()),
                    true);
            Offset dataOffset = new Offset(dataOffsetBuffer.getLong());
            indexes.put(key, dataOffset);
            offset = new Offset(offset.value() + Integer.BYTES + key.length() + Long.BYTES);
            keyLengthBuffer.clear();
            dataOffsetBuffer.clear();
        }
        return indexes;
    }
}
