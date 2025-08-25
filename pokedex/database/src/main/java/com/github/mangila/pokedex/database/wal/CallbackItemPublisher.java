package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class CallbackItemPublisher implements Flow.Publisher<CallbackItem<Entry>> {

    private final ExecutorService publisherExecutor = VirtualThreadFactory.newFixedThreadPool(10);
    private final SubmissionPublisher<CallbackItem<Entry>> publisher;

    CallbackItemPublisher() {
        this.publisher = new SubmissionPublisher<>(publisherExecutor, 100);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super CallbackItem<Entry>> subscriber) {
        publisher.subscribe(subscriber);
    }

    void submit(CallbackItem<Entry> callbackItem) {
        publisher.submit(callbackItem);
    }

    void close() {
        publisher.close();
        VirtualThreadFactory.terminateGracefully(publisherExecutor, Duration.ofSeconds(30));
    }
}
