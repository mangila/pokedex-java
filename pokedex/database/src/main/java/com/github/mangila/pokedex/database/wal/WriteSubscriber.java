package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.WriteCallbackItem;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

class WriteSubscriber implements Flow.Subscriber<WriteCallbackItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteSubscriber.class);
    private final BlockingQueue writeQueue;
    private final BlockingQueue bigObjectWriteQueue;

    WriteSubscriber(BlockingQueue writeQueue,
                    BlockingQueue bigObjectWriteQueue) {
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
        int len = item.bufferLength();
        QueueEntry queueEntry = new QueueEntry(item);
        if (len < _64KB) {
            writeQueue.add(queueEntry);
        } else {
            bigObjectWriteQueue.add(queueEntry);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("Error in WriteSubscriber", throwable);
    }

    @Override
    public void onComplete() {
        // do nothing
    }
}
