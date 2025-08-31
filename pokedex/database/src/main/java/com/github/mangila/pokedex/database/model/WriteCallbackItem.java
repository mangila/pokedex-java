package com.github.mangila.pokedex.database.model;

public record WriteCallbackItem(Entry entry, WriteOperation operation, WriteCallback callback) {
    public static WriteCallbackItem newItem(Entry entry, WriteOperation operation) {
        return new WriteCallbackItem(entry, operation, WriteCallback.newCallback());
    }

    public int bufferLength() {
        return entry.bufferLength();
    }
}
