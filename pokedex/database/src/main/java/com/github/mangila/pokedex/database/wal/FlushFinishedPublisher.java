package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.EntryCollection;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class FlushFinishedPublisher implements Flow.Publisher<EntryCollection> {

    private final ExecutorService publisherExecutor = VirtualThreadFactory.newFixedThreadPool(10);
    private final SubmissionPublisher<EntryCollection> publisher;

    FlushFinishedPublisher() {
        this.publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());
    }

    @Override
    public void subscribe(Flow.Subscriber<? super EntryCollection> subscriber) {
        publisher.subscribe(subscriber);
    }

    void submit(EntryCollection entries) {
        publisher.submit(entries);
    }

    void close() {
        publisher.close();
        VirtualThreadFactory.terminateGracefully(publisherExecutor, Duration.ofSeconds(30));
    }
}
