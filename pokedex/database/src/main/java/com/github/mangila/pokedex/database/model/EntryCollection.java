package com.github.mangila.pokedex.database.model;

import java.util.List;

public record EntryCollection(List<Entry> collection) {
    public int bufferSize() {
        return collection.stream()
                .mapToInt(Entry::bufferSize)
                .sum();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }
}
