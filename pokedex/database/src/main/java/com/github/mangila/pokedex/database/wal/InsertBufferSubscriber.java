package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class InsertBufferSubscriber implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertBufferSubscriber.class);
    private Flow.Subscription subscription;
    private final Buffer buffer;
    private final List<Entry> entries;
    private final SubmissionPublisher<List<Entry>> bufferPublisher;

    public InsertBufferSubscriber(Buffer buffer) {
        this.buffer = buffer;
        this.entries = new CopyOnWriteArrayList<>();
        this.bufferPublisher = new SubmissionPublisher<>();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        try {
            buffer.put(item.value().toBuffer());
            entries.add(item.value());
            item.callback().complete(null);
        } catch (BufferOverflowException e) {
            LOGGER.error("Buffer is full to big write", e);
            item.callback().completeExceptionally(e);
            return;
        }
        if (buffer.remaining() > 1024) {
            bufferPublisher.submit(List.copyOf(entries));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        buffer.clear();
        entries.clear();
    }
}
