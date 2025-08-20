package com.github.mangila.pokedex.database.internal.io.internal;


import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.internal.model.*;
import com.github.mangila.pokedex.database.internal.io.internal.util.FileChannelUtil;
import com.github.mangila.pokedex.database.internal.io.model.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.model.Key;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
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
                .concat(".index")
                .concat(".yakvs"));
        DatabaseFile databaseFile = new DatabaseFile(databaseFileName);
        this.databaseFileHandler = new DatabaseFileHandler(databaseFile);
        this.keys = new ConcurrentHashMap<>();
    }

    public void init() throws IOException {
        databaseFileHandler.init();
        if (databaseFileHandler.databaseFile().isEmpty()) {
            LOGGER.info("Creating new index file");
            FileChannelUtil.writeHeader(
                    databaseFileHandler.channel(),
                    DatabaseFileHeader.EMPTY
            );
        } else {
            LOGGER.info("Loading existing index file");
            DatabaseFileHeader header = FileChannelUtil.readHeader(databaseFileHandler.channel());
            keys.putAll(loadKeys(header));
        }
    }

    public void truncate() throws IOException {
        databaseFileHandler.truncate();
        keys.clear();
    }

    public void deleteFile() throws IOException {
        databaseFileHandler.deleteFile();
        keys.clear();
    }

    public int size() {
        return keys.size();
    }

    public Offset get(Key key) {
        return keys.get(key);
    }

    public void append(Key key, Offset offset) throws IOException {
        IndexEntry indexEntry = new IndexEntry(key, offset);
        Entry entry = new Entry(indexEntry.toByteBuffer(true));
        OffsetBoundary boundary = FileChannelUtil.append(entry, databaseFileHandler.channel());
        keys.put(key, boundary.start());
    }

    private Map<Key, Offset> loadKeys(DatabaseFileHeader header) throws IOException {
        long position = DatabaseFileHeader.HEADER_SIZE.value();
        int recordCount = header.recordCount().value();
        Map<Key, Offset> indexes = new HashMap<>();
        FileChannel channel = databaseFileHandler.channel();
        for (int i = 0; i < recordCount; i++) {
            Entry keyLengthBuffer = FileChannelUtil.read(
                    BufferUtils.newByteBuffer(Integer.BYTES),
                    position,
                    true,
                    channel);
            int keyLength = keyLengthBuffer.getInt();
            Entry keyBuffer = FileChannelUtil.read(
                    BufferUtils.newByteBuffer(keyLength),
                    position + Integer.BYTES,
                    true,
                    channel);
            Key key = new Key(new String(keyBuffer.getArray()));
            Entry dataOffsetBuffer = FileChannelUtil.read(
                    BufferUtils.newByteBuffer(Long.BYTES),
                    position + Integer.BYTES + keyLength,
                    true,
                    channel);
            Offset offset = new Offset(dataOffsetBuffer.getLong());
            position += Integer.BYTES + keyLength + Long.BYTES;
            indexes.put(key, offset);
        }
        return indexes;
    }
}
