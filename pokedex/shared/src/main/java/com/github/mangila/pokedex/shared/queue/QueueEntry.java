package com.github.mangila.pokedex.shared.queue;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public record QueueEntry(Object data,
                         AtomicInteger failCounter) {

    public QueueEntry {
        Objects.requireNonNull(data);
    }

    public QueueEntry(Object data) {
        this(data, new AtomicInteger(0));
    }

    public static QueueEntry of(Object data) {
        return new QueueEntry(data);
    }

    public void incrementFailCounter() {
        failCounter.incrementAndGet();
    }

    public boolean equalsMaxRetries(int maxRetries) {
        return failCounter.get() == maxRetries;
    }

    public <T> T unwrapAs(Class<T> clazz) {
        return clazz.cast(data);
    }

}
