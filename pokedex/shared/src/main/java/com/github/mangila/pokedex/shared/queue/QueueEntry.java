package com.github.mangila.pokedex.shared.queue;

import java.util.Objects;

public record QueueEntry(Object data) {

    public QueueEntry {
        Objects.requireNonNull(data);
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
