package com.github.mangila.pokedex.database.model;

public record Entry(Key key, Field field, Value value) {
    /**
     * Calculates the total buffer length for the combined components of this entry.
     * The buffer length is derived from the sum of the lengths of the key, field, and value
     * buffer sizes, along with an additional +1 for tombstone indicator.
     *
     * @return the total buffer length of this entry
     */
    public int bufferLength() {
        return key.bufferLength() + field.getBufferSize() + value.getBufferSize() + 1;
    }
}
