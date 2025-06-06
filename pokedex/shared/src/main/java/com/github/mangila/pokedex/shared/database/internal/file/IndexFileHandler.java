package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
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
 * +---------------+------------+--------------------------------+
 * | Field         | Size       | Description                    |
 * +---------------+------------+--------------------------------+
 * | Magic Number  | n bytes    | File identifier ("yakvs")   |
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

    private final File file;
    private final Map<String, Long> dataOffsets;
    private FileHeader header;

    public IndexFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".index")
                .concat(".yakvs");
        this.file = new File(new FileName(fileName));
        this.dataOffsets = new ConcurrentHashMap<>();
        this.header = FileHeader.defaultValue();
    }

    /**
     * Initializes the index file. If the file does not exist, it will be created. If the file is empty,
     * a default header will be written to it. Otherwise, the method will read and parse the existing header from the file.
     *
     * @throws IOException if there is an issue, creating the file, reading from it, or writing to it.
     */
    public void init() throws IOException {
        ByteBuffer buffer;
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            file.write(header.toByteBuffer(), 0);
        } else {
            buffer = file.readAndFlip(0, FileHeader.HEADER_SIZE);
            this.header = FileHeader.from(buffer);
            this.dataOffsets.putAll(loadIndexes());
            log.debug("Loaded {} index entries", dataOffsets.size());
        }
    }

    public long write(String key, long dataOffset) throws IOException {
        var indexEntry = IndexEntry.from(key.getBytes(), dataOffset);
        var offset = header.offset();
        int size = indexEntry.getSize();
        file.write(indexEntry.toByteBuffer(), offset);
        return offset + size;
    }

    public void updateHeader(long newOffset) throws IOException {
        header = header.updateOffset(newOffset);
        file.write(header.toByteBuffer(), 0);
    }

    public boolean hasIndex(String key) {
        return dataOffsets.containsKey(key);
    }

    public long getDataOffset(String key) {
        return dataOffsets.get(key);
    }

    public void putIndex(String key, long dataOffset) {
        dataOffsets.put(key, dataOffset);
    }

    private Map<String, Long> loadIndexes() throws IOException {
        long size = file.getFileSize() - FileHeader.HEADER_SIZE;
        var buffer = file.readFileRegion(
                FileHeader.HEADER_SIZE,
                size);
        int recordCount = header.recordCount();
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

    public void deleteFile() throws IOException {
        file.tryDeleteFile();
    }

    public void truncate() throws IOException {
        file.truncate();
        header = FileHeader.defaultValue();
        file.write(header.toByteBuffer(), 0);
        dataOffsets.clear();
    }

    public boolean isEmpty() {
        return dataOffsets.isEmpty();
    }

    public Map<String, Long> getIndexMap() {
        return dataOffsets;
    }
}
