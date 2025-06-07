package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
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

    private static final Logger log = LoggerFactory.getLogger(CompactThread.class);

    @Override
    public void run() {
        log.info("Compacting database {}", databaseName);
        compactWritePermit.acquireUninterruptibly();
        try {
            var indexTmp = new File(new FileName("index.tmp.yakvs"));
            var dataTmp = new File(new FileName("data.tmp.yakvs"));
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
            log.info("Compact database {}. Old size: {} bytes, new size: {} bytes",
                    databaseName,
                    dataFileHandler.getFileSize(),
                    dataFileHandlerTmp.getFileSize());
            compactReadPermit.acquireUninterruptibly(readThreadConfig.nThreads());
            indexFileHandler.setDataOffsets(indexFileHandlerTmp.getDataOffsets());
            indexFileHandlerTmp.closeFileChannels();
            indexFileHandler.closeFileChannels();
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
            log.error("ERR", e);
        }
        compactReadPermit.release(readThreadConfig.nThreads());
        compactWritePermit.release();
    }
}
