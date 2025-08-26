package com.github.mangila.pokedex.database.model;

import java.util.List;

public record EntryCollection(List<Entry> collection) {

    public int bufferLength() {
        return collection.stream()
                .mapToInt(Entry::bufferLength)
                .sum();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }
}
