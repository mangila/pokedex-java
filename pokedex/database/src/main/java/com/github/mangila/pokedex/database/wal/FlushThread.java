package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.EntryCollection;
import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FlushThread implements SimpleBackgroundThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);
    private final Queue queue;
    private final ScheduledExecutorService flushThreadExecutor;
    private final WalFileHandler walFileHandler;

    FlushThread(Queue queue,
                WalFileHandler walFileHandler) {
        this.queue = queue;
        this.walFileHandler = walFileHandler;
        this.flushThreadExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    }

    @Override
    public void schedule() {
        flushThreadExecutor.scheduleWithFixedDelay(
                this,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry = queue.poll();
            if (queueEntry != null) {
                try {
                    FlushOperation flushOperation = queueEntry.unwrapAs(FlushOperation.class);
                    EntryCollection entryCollection = flushOperation.entries();
                    if (!flushOperation.entries().isEmpty()) {
                        LOGGER.info("Flushing {}", flushOperation.reason());
                        walFileHandler.flush(entryCollection);
                        entryCollection.complete();
                    }
                } catch (Exception e) {
                    LOGGER.error("ERR", e);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        flushThreadExecutor.shutdown();
    }
}
