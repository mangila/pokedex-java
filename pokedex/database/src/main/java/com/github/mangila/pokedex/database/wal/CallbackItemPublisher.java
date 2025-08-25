package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class CallbackItemPublisher implements Flow.Publisher<CallbackItem<Entry>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackItemPublisher.class);
    private final SubmissionPublisher<CallbackItem<Entry>> publisher;

    public CallbackItemPublisher() {
        this.publisher = new SubmissionPublisher<>();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super CallbackItem<Entry>> subscriber) {
        LOGGER.info("CallbackItemPublisher subscription {}", subscriber);
        publisher.subscribe(subscriber);
    }

    public void submit(CallbackItem<Entry> callbackItem) {
        publisher.submit(callbackItem);
    }

    public void close() {
        publisher.close();
    }
}
