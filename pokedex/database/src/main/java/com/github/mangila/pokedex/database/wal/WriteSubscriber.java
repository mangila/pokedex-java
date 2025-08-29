package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.WriteCallbackItem;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

class WriteSubscriber implements Flow.Subscriber<WriteCallbackItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteSubscriber.class);
    private final Queue writeQueue;
    private final Queue bigObjectWriteQueue;

    WriteSubscriber(Queue writeQueue,
                    Queue bigObjectWriteQueue) {
        this.writeQueue = writeQueue;
        this.bigObjectWriteQueue = bigObjectWriteQueue;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    private static final int _64KB = 1024 * 64;

    @Override
    public void onNext(WriteCallbackItem item) {
        int len = item.entry().bufferLength();
        QueueEntry queueEntry = new QueueEntry(item);
        if (len >= _64KB) {
            bigObjectWriteQueue.add(queueEntry);
        } else {
            writeQueue.add(queueEntry);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("ERR", throwable);
    }

    @Override
    public void onComplete() {
        // do nothing
    }
}
