package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.WriteCallbackItem;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

class EntryPublisher implements Flow.Publisher<WriteCallbackItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPublisher.class);
    private final SubmissionPublisher<WriteCallbackItem> publisher;

    EntryPublisher() {
        this.publisher = new SubmissionPublisher<>(VirtualThreadFactory.THREAD_PER_TASK_EXECUTOR, Flow.defaultBufferSize());
    }

    @Override
    public void subscribe(Flow.Subscriber<? super WriteCallbackItem> subscriber) {
        LOGGER.info("Starting EntryPublisher");
        publisher.subscribe(subscriber);
    }

    void submit(WriteCallbackItem writeCallbackItem) {
        publisher.submit(writeCallbackItem);
    }

    void close() {
        LOGGER.info("Shutting down EntryPublisher");
        publisher.close();
    }
}
