package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.DatabaseName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Semaphore;

public record CompactThread(DatabaseName databaseName,
                            IndexFileHandler indexFileHandler,
                            DataFileHandler dataFileHandler,
                            Semaphore compactWritePermit,
                            DatabaseConfig.ReaderThreadConfig readThreadConfig,
                            Semaphore compactReadPermit) implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompactThread.class);

    /**
     * Executes the compaction process for a database.
     * This involves consolidating the data and index files to reduce file size
     * and eliminate unnecessary entries, ensuring optimal performance.
     * <p>
     * The method works by:
     * - Acquiring necessary permits to synchronize write and read operations.
     * - Creating temporary data and index files for the compaction.
     * - Reading existing records from the current data file and writing them to the temporary files.
     * - Updating file headers and index mappings in the temporary files as entries are written.
     * - Logging the results of the compaction process, including the size difference.
     * - Replacing the old data and index files with the newly compacted versions atomically.
     * - Handling any `IOException` that may occur during file operations.
     * - Releasing the acquired permits after the process is completed.
     * <p>
     * This method ensures data consistency and integrity even when the application
     * is actively reading and writing to the database by using synchronization mechanisms.
     */
    @Override
    public void run() {
        LOGGER.info("Compacting database {}", databaseName);
        compactWritePermit.acquireUninterruptibly();
        try {
            var indexTmp = new DatabaseFile(new FileName("index.tmp.yakvs"));
            indexTmp.tryDeleteFile();
            var dataTmp = new DatabaseFile(new FileName("data.tmp.yakvs"));
            dataTmp.tryDeleteFile();
            var indexFileHandlerTmp = new IndexFileHandler(indexTmp);
            var dataFileHandlerTmp = new DataFileHandler(dataTmp);
            indexFileHandlerTmp.init();
            dataFileHandlerTmp.init();
            for (var entry : indexFileHandler.getDataOffsets().entrySet()) {
                String key = entry.getKey();
                long dataOffset = entry.getValue();
                var record = dataFileHandler.read(dataOffset);
                var dataOffsetBoundary = dataFileHandlerTmp.write(record);
                dataFileHandlerTmp.updateHeader(dataOffsetBoundary.endOffset());
                var indexOffsetBoundary = indexFileHandlerTmp.write(IndexEntry.from(key.getBytes(), dataOffsetBoundary.startOffset()));
                indexFileHandlerTmp.updateHeader(indexOffsetBoundary.endOffset());
                indexFileHandlerTmp.putIndex(key, dataOffsetBoundary.startOffset());
            }
            LOGGER.info("Compact database {}. Old size: {} bytes, New size: {} bytes",
                    databaseName,
                    dataFileHandler.getFileSize(),
                    dataFileHandlerTmp.getFileSize());
            compactReadPermit.acquireUninterruptibly(readThreadConfig.nThreads());
            indexFileHandlerTmp.closeFileChannels();
            indexFileHandler.closeFileChannels();
            indexFileHandler.setDataOffsets(indexFileHandlerTmp.getDataOffsets());
            Files.move(
                    indexFileHandlerTmp.getPath(),
                    indexFileHandler.getPath(),
                    StandardCopyOption.ATOMIC_MOVE);
            dataFileHandlerTmp.closeFileChannels();
            dataFileHandler.closeFileChannels();
            Files.move(
                    dataFileHandlerTmp.getPath(),
                    dataFileHandler.getPath(),
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            LOGGER.error("ERR", e);
        }
        compactReadPermit.release(readThreadConfig.nThreads());
        compactWritePermit.release();
    }
}
