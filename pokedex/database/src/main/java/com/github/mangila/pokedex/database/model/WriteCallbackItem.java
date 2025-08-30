package com.github.mangila.pokedex.database.model;

public record WriteCallbackItem(Entry entry, WriteCallback callback) {
    public int bufferLength() {
        return entry.bufferLength();
    }
}
