package com.github.mangila.pokedex.shared.queue;

import com.github.mangila.pokedex.shared.model.PokeApiUri;

import java.util.Objects;

public record QueueEntry(Object data) {

    public QueueEntry {
        Objects.requireNonNull(data);
    }

    public PokeApiUri getDataAsPokeApiUri() {
        return (PokeApiUri) data;
    }

}
