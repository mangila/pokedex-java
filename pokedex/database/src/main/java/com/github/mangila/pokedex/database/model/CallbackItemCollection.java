package com.github.mangila.pokedex.database.model;

import java.util.List;

public record CallbackItemCollection(List<WriteCallbackItem> value) {

    public int bufferLength() {
        return value.stream()
                .map(WriteCallbackItem::entry)
                .mapToInt(Entry::bufferLength)
                .sum();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public void complete() {
        value.stream()
                .map(WriteCallbackItem::callback)
                .map(WriteCallback::future)
                .forEach(future -> future.complete(null));
    }

    public List<Entry> toValues() {
        return value.stream()
                .map(WriteCallbackItem::entry)
                .toList();
    }
}
