package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final DataFileHandler dataFileHandler;
    private final IndexFileHandler indexFileHandler;
    private final Semaphore compactWritePermit;
    private final Semaphore compactReadPermit;
    private final DatabaseConfig.CompactThreadConfig compactThreadConfig;
    private final CompactThread compactThread;
    private final ScheduledExecutorService healthProbeExecutor;

    public FileHandler(DatabaseConfig config) {
        var databaseName = config.databaseName();
        var readThreadConfig = config.readerThreadConfig();
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.indexFileHandler = new IndexFileHandler(databaseName);
        this.compactWritePermit = new Semaphore(1, Boolean.TRUE);
        this.compactReadPermit = new Semaphore(readThreadConfig.nThreads(), Boolean.TRUE);
        this.compactThreadConfig = config.compactThreadConfig();
        this.healthProbeExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.compactThread = new CompactThread(
                databaseName,
                indexFileHandler,
                dataFileHandler,
                compactWritePermit,
                readThreadConfig,
                compactReadPermit);
    }

    /**
     * Append only
     */
    public boolean write(String key, byte[] value) {
        try {
            compactWritePermit.acquireUninterruptibly();
            var record = DataRecord.from(value);
            var dataOffsetBoundary = dataFileHandler.write(record);
            dataFileHandler.updateHeader(dataOffsetBoundary.endOffset());
            var entry = IndexEntry.from(key.getBytes(), dataOffsetBoundary.startOffset());
            var indexOffsetBoundary = indexFileHandler.write(entry);
            indexFileHandler.updateHeader(indexOffsetBoundary.endOffset());
            indexFileHandler.putIndex(key, entry.dataOffset());
            log.debug("Wrote new record with key {} and offset {} - {} -- {}", key, dataOffsetBoundary.startOffset(), dataOffsetBoundary.endOffset(), record);
            compactWritePermit.release();
            return Boolean.TRUE;
        } catch (IOException e) {
            log.error("ERR", e);
            compactWritePermit.release();
            return Boolean.FALSE;
        }
    }

    public byte[] read(String key) {
        try {
            if (!indexFileHandler.hasIndex(key)) {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            compactReadPermit.acquireUninterruptibly();
            long dataOffset = indexFileHandler.getDataOffset(key);
            var record = dataFileHandler.read(dataOffset);
            compactReadPermit.release();
            return record.data();
        } catch (IOException e) {
            log.error("ERR", e);
            compactReadPermit.release();
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public void init() throws IOException {
        indexFileHandler.init();
        dataFileHandler.init();
        healthProbeExecutor.scheduleWithFixedDelay(
                compactThread,
                compactThreadConfig.initialDelay(),
                compactThreadConfig.delay(),
                compactThreadConfig.timeUnit()
        );
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
