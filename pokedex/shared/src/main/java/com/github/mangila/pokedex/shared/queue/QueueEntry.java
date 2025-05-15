package com.github.mangila.pokedex.shared.queue;

import com.github.mangila.pokedex.shared.model.PokeApiUri;

import java.util.Objects;

public record QueueEntry(Object data) {

    public QueueEntry {
        Objects.requireNonNull(data);
    }

    /**
     * <summary>
     * Convenient method cast data as the PokeApiUri type
     * </summary>
     */
    public PokeApiUri getDataAsPokeApiUri() {
        return (PokeApiUri) data;
    }

}
