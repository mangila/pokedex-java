package com.github.mangila.pokedex.database.model;

public record Entry(Key key, Field field, Value value) {

    public void fill(Buffer buffer) {
        buffer.put(key);
        buffer.put(field);
        buffer.put(value);
    }

    public int bufferLength() {
        return key.bufferLength() + field.getBufferSize() + value.getBufferSize();
    }
}
