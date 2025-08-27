package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

class FlushDelegateSubscriber implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushDelegateSubscriber.class);
    private final Queue flushQueue;

    public FlushDelegateSubscriber(Queue flushQueue) {
        this.flushQueue = flushQueue;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        flushQueue.add(new QueueEntry(item));
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("ERR", throwable);
    }

    @Override
    public void onComplete() {

    }
}
