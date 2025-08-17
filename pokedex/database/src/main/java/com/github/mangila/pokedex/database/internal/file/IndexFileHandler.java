package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.database.DatabaseName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
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
 * | Magic Number  | n bytes    | File identifier ("yakvs")      |
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

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexFileHandler.class);

    private final DatabaseFile databaseFile;
    private Map<String, Long> dataOffsets;
    private FileHeader header;

    public IndexFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".index")
                .concat(".yakvs");
        this.databaseFile = new DatabaseFile(new FileName(fileName));
        this.dataOffsets = new ConcurrentHashMap<>();
        this.header = FileHeader.defaultValue();
    }

    public IndexFileHandler(DatabaseFile databaseFile) {
        this.databaseFile = databaseFile;
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
        databaseFile.tryCreateFileIfNotExists();
        if (databaseFile.isEmpty()) {
            databaseFile.write(header.toByteBuffer(), 0);
        } else {
            ByteBuffer buffer = databaseFile.readAndFlip(0, FileHeader.HEADER_SIZE);
            this.header = FileHeader.from(buffer);
            this.dataOffsets.putAll(loadIndexes());
            LOGGER.debug("Loaded {} index entries", dataOffsets.size());
        }
    }

    public OffsetBoundary write(IndexEntry entry) throws IOException {
        long offset = header.offset();
        int size = entry.getSize();
        databaseFile.write(entry.toByteBuffer(), offset);
        long newOffset = offset + size;
        return OffsetBoundary.from(offset, newOffset);
    }

    public void updateHeader(long newOffset) throws IOException {
        header = header.updateOffset(newOffset);
        databaseFile.write(header.toByteBuffer(), 0);
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
        long size = databaseFile.getFileSize() - FileHeader.HEADER_SIZE;
        var buffer = databaseFile.readFileRegion(
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
        databaseFile.tryDeleteFile();
        header = FileHeader.defaultValue();
        dataOffsets.clear();
    }

    public void truncate() throws IOException {
        databaseFile.truncate();
        header = FileHeader.defaultValue();
        databaseFile.write(header.toByteBuffer(), 0);
        dataOffsets.clear();
    }

    public boolean isEmpty() {
        return dataOffsets.isEmpty();
    }

    public int size() {
        return dataOffsets.size();
    }

    public Map<String, Long> getDataOffsets() {
        return dataOffsets;
    }

    public void setDataOffsets(Map<String, Long> indexMap) {
        this.dataOffsets = indexMap;
    }

    public Path getPath() {
        return databaseFile.getPath();
    }

    public void closeFileChannels() {
        databaseFile.closeChannels();
    }
}
