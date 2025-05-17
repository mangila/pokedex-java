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

    public void incrementFailCounter() {
        failCounter.incrementAndGet();
    }

    public boolean equalsMaxRetries(int maxRetries) {
        return failCounter.get() == maxRetries;
    }

    /**
     * <summary>
     * Convenient method cast data as the specified type
     * </summary>
     */
    public <T> T getDataAs(Class<T> clazz) {
        return clazz.cast(data);
    }

}
