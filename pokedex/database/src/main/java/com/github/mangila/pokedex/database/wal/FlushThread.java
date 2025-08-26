package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FlushThread implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);
    private final Queue queue;
    private final FlushFinishedPublisher publisher;
    private final ScheduledExecutorService flushThreadExecutor;
    private final WalFileHandler walFileHandler;

    FlushThread(Queue queue,
                FlushFinishedPublisher publisher,
                WalFileHandler walFileHandler) {
        this.queue = queue;
        this.publisher = publisher;
        this.walFileHandler = walFileHandler;
        this.flushThreadExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    }

    void schedule() {
        flushThreadExecutor.scheduleWithFixedDelay(
                this,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry != null) {
            try {
                FlushOperation flushOperation = queueEntry.unwrapAs(FlushOperation.class);
                if (!flushOperation.entries().isEmpty()) {
                    LOGGER.info("Flushing {}", flushOperation.reason());
                    walFileHandler.flush(flushOperation);
                    publisher.submit(flushOperation.entries());
                }
            } catch (Exception e) {
                LOGGER.error("ERR", e);
            }
        }
    }

    public void shutdown() {
        flushThreadExecutor.shutdown();
    }
}
