package com.github.mangila.pokedex.shared.database.internal.file.index;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.database.internal.file.FileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
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
 * +---------------+------------+--------------------------------+
 * | Field         | Size       | Description                    |
 * +---------------+------------+--------------------------------+
 * | Magic Number  | 8 bytes    | File identifier ("yakvsidx")   |
 * | Version       | 4 bytes    | File format version number     |
 * | Record Count  | 4 bytes    | Number of index records        |
 * | Offset        | 8 bytes    | Next write position            |
 * +---------------+------------+--------------------------------+
 * </pre>
 *
 * <h3>Index File Entries</h3>
 * <p>Sequential key-offset mapping entries:</p>
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

    private static final Logger log = LoggerFactory.getLogger(IndexFileHandler.class);

    private final Map<String, Long> dataOffsets = new ConcurrentHashMap<>();
    private final IndexFileHeaderHandler indexFileHeaderHandler;
    private final IndexReader indexReader;
    private final IndexWriter indexWriter;
    private final File file;

    public IndexFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".index")
                .concat(".yakvs");
        this.file = new File(new FileName(fileName));
        this.indexFileHeaderHandler = new IndexFileHeaderHandler(file);
        this.indexReader = new IndexReader(file);
        this.indexWriter = new IndexWriter(file);
    }

    public boolean hasIndex(String key) {
        return dataOffsets.containsKey(key);
    }

    public long getDataOffset(String key) {
        return dataOffsets.get(key);
    }

    public void writeNewIndex(String key, long dataOffset) throws IOException {
        var indexEntry = IndexEntry.from(key.getBytes(), dataOffset);
        var offset = indexFileHeaderHandler.getOffset();
        int size = indexEntry.getSize();
        var buffer = ByteBuffer.allocate(size);
        buffer.putInt(indexEntry.keyLength());
        buffer.put(indexEntry.key());
        buffer.putLong(indexEntry.dataOffset());
        buffer.flip();
        file.write(buffer, offset);
        long newOffset = offset + size;
        log.debug("Inserted index {} at {} -- new offset {}", indexEntry, offset, newOffset);
        indexFileHeaderHandler.update(newOffset);
        dataOffsets.put(key, dataOffset);
    }

    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            indexFileHeaderHandler.write();
        } else {
            indexFileHeaderHandler.loadHeader();
            var map = indexFileHeaderHandler.loadIndexes();
            dataOffsets.putAll(map);
            log.info("Loaded {} index entries", dataOffsets.size());
        }
    }

    public void deleteFile() throws IOException {
        log.info("Deleting file {}", file.getPath().getFileName());
        file.deleteFile();
    }

    public void truncate() throws IOException, InterruptedException {
        file.truncate();
        indexFileHeaderHandler.truncate();
        dataOffsets.clear();
    }

    public boolean isEmpty() {
        return dataOffsets.isEmpty();
    }
}
