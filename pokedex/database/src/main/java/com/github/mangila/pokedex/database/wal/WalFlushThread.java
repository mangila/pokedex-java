package com.github.mangila.pokedex.database.wal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

record WalFlushThread(WalFileHandler walFileHandler) implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFlushThread.class);

    @Override
    public void run() {
        CompletableFuture<Boolean> callback = walFileHandler.flushCallback();
        try {
            walFileHandler.flushLatch().await();
            WalFile walFile = walFileHandler.walFile();
            walFileHandler.flush().whenComplete((ok, throwable) -> {
                if (throwable != null) {
                    callback.completeExceptionally(throwable);
                } else if (ok == null || Boolean.FALSE.equals(ok)) {
                    callback.complete(false);
                } else {
                    try {
                        walFile.truncate();
                        walFile.status().set(WalFileStatus.OPEN);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    callback.complete(true);
                }
            });
        } catch (InterruptedException e) {
            LOGGER.info("Shutdown WAL Flush Thread");
            Thread.currentThread().interrupt();
            callback.cancel(true);
        }
    }
}
