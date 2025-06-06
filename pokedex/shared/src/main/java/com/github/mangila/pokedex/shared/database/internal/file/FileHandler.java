package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final DataFileHandler dataFileHandler;
    private final IndexFileHandler indexFileHandler;
    private final Semaphore compactPermits;
    private final CompactThread compactThread;

    public FileHandler(DatabaseName databaseName) {
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.indexFileHandler = new IndexFileHandler(databaseName);
        this.compactPermits = new Semaphore(1, Boolean.TRUE);
        this.compactThread = new CompactThread(
                databaseName,
                indexFileHandler,
                dataFileHandler,
                compactPermits);
    }

    /**
     * Append only
     */
    public boolean write(String key, byte[] value) {
        try {
            compactPermits.acquireUninterruptibly();
            var pair = dataFileHandler.write(value);
            var record = pair.first();
            var boundary = pair.second();
            dataFileHandler.updateHeader(boundary.end());
            long newIndexOffset = indexFileHandler.write(key, boundary.start());
            indexFileHandler.updateHeader(newIndexOffset);
            indexFileHandler.putIndex(key, boundary.start());
            log.debug("Wrote new record with key {} and offset {} -- {}", key, boundary.start(), record);
            compactPermits.release();
            return Boolean.TRUE;
        } catch (IOException e) {
            log.error("ERR", e);
            compactPermits.release();
            return Boolean.FALSE;
        }
    }

    public byte[] read(String key) {
        try {
            if (!indexFileHandler.hasIndex(key)) {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            long dataOffset = indexFileHandler.getDataOffset(key);
            var record = dataFileHandler.read(dataOffset);
            return record.data();
        } catch (IOException e) {
            log.error("ERR", e);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public void init() throws IOException {
        indexFileHandler.init();
        dataFileHandler.init();
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(compactThread,
                        12,
                        12,
                        TimeUnit.HOURS);
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
