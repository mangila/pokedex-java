package com.github.mangila.pokedex.shared.database.internal.file.index;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.internal.file.DatabaseFileName;
import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.database.internal.file.header.FileHeader;
import com.github.mangila.pokedex.shared.database.internal.file.header.FileHeaderHandler;
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
 * +---------------+------------+--------------------------------+
 * | Field         | Size       | Description                    |
 * +---------------+------------+--------------------------------+
 * | Magic Number  | 8 bytes    | File identifier ("Pok3mon1")   |
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

    private final Map<String, Long> indexOffsets = new ConcurrentHashMap<>();
    private final FileHeaderHandler fileHeaderHandler;
    private final File file;

    public IndexFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".index")
                .concat(".yakvs");
        this.file = new File(new DatabaseFileName(fileName));
        this.fileHeaderHandler = new FileHeaderHandler(file);
    }

    public boolean hasIndex(String key) {
        return indexOffsets.containsKey(key);
    }

    public void writeNewIndex(String key, long dataOffset) throws IOException {
        var index = IndexEntry.from(key.getBytes(), dataOffset);
        var size = index.getSize();
        var buffer = file.getFileRegion(
                FileChannel.MapMode.READ_WRITE,
                fileHeaderHandler.getOffset(),
                size
        );
        buffer.putInt(index.keyLength());
        buffer.put(index.key());
        buffer.putLong(index.dataOffset());
        long newOffset = fileHeaderHandler.getOffset() + size;
        log.debug("Inserted index {} at {} -- new offset {}", index, fileHeaderHandler.getOffset(), newOffset);
        fileHeaderHandler.updateNewWrite(newOffset);
    }

    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            fileHeaderHandler.writeHeaderToFile();
        } else {
            fileHeaderHandler.loadHeader();
            indexOffsets.putAll(loadIndexes());
            log.info("Loaded {} index entries", indexOffsets.size());
        }
    }

    public void deleteFile() throws IOException {
        log.info("Deleting file {}", file.getPath().getFileName());
        file.deleteFile();
    }

    private Map<String, Long> loadIndexes() throws IOException {
        var buffer = file.getReadChannel().map(
                FileChannel.MapMode.READ_ONLY,
                FileHeader.HEADER_SIZE,
                file.getReadChannel().size() - FileHeader.HEADER_SIZE);
        var recordCount = fileHeaderHandler.getRecordCount();
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
}
