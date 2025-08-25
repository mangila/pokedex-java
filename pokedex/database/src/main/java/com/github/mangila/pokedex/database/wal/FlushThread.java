package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;

record FlushThread(Queue queue, SubmissionPublisher<List<Entry>> finishedFlushingPublisher) implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry != null) {
            FlushOperation flushOperation = queueEntry.unwrapAs(FlushOperation.class);
            LOGGER.info(flushOperation.toString());
            finishedFlushingPublisher.submit(flushOperation.entries());
        }
    }
}
