package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class FinishedFlushingPublisher implements Flow.Publisher<List<Entry>> {

    private final ExecutorService publisherExecutor = VirtualThreadFactory.newFixedThreadPool(10);
    private final SubmissionPublisher<List<Entry>> publisher;

    FinishedFlushingPublisher() {
        this.publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());
    }

    @Override
    public void subscribe(Flow.Subscriber<? super List<Entry>> subscriber) {
        publisher.subscribe(subscriber);
    }

    void submit(List<Entry> entries) {
        publisher.submit(entries);
    }

    void close() {
        publisher.close();
        VirtualThreadFactory.terminateGracefully(publisherExecutor, Duration.ofSeconds(30));
    }
}
