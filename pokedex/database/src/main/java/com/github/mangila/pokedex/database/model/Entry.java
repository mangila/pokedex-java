package com.github.mangila.pokedex.database.model;

public record Entry(Key key, Field field, Value value) {
    public Buffer toBuffer() {
        Buffer buffer = Buffer.from(bufferSize());
        buffer.put(key);
        buffer.put(field);
        buffer.put(value);
        return buffer;
    }

    public int bufferSize() {
        return key.getBufferSize() + field.getBufferSize() + value.getBufferSize();
    }
}
