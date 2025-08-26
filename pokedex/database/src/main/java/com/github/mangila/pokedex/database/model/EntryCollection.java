package com.github.mangila.pokedex.database.model;

import java.util.List;

public record EntryCollection(List<CallbackItem<Entry>> collection) {

    public int bufferLength() {
        return collection.stream()
                .map(CallbackItem::value)
                .mapToInt(Entry::bufferLength)
                .sum();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public void complete() {
        collection.stream()
                .map(CallbackItem::callback)
                .forEach(future -> future.complete(null));
    }

    public List<Entry> toValues() {
        return collection.stream()
                .map(CallbackItem::value)
                .toList();
    }
}
