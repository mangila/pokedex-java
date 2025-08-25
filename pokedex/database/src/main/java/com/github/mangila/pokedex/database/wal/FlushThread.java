package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record FlushThread(Queue queue, FinishedFlushingPublisher publisher) implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry != null) {
            FlushOperation flushOperation = queueEntry.unwrapAs(FlushOperation.class);
            if (!flushOperation.entries().isEmpty()) {
                LOGGER.info("Flushing {}", flushOperation.reason());
                publisher.submit(flushOperation.entries());
            }
        }
    }
}
