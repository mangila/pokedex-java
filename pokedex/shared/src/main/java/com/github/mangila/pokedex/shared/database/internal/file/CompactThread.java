package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;

public record CompactThread(DatabaseName databaseName,
                            IndexFileHandler indexFileHandler,
                            DataFileHandler dataFileHandler,
                            Semaphore compactPermits) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CompactThread.class);

    @Override
    public void run() {
        compactPermits.acquireUninterruptibly();
        try {
            var tmpDatabaseName = databaseName.value() + ".tmp";
            var tmpIndex = Files.createTempFile(
                    databaseName.value(),
                    "index.tmp.yakvs");
            var tmpData = Files.createTempFile(
                    databaseName.value(),
                    "data.tmp.yakvs");
            indexFileHandler
                    .getIndexMap()
                    .forEach((key, dataOffset) -> {

                    });
        } catch (IOException e) {
            log.error("ERR", e);
        }
        compactPermits.release();
    }
}
