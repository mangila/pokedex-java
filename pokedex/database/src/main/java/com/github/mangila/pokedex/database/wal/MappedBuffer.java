package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class MappedBuffer {
    private final MappedByteBuffer inner;

    public MappedBuffer(MappedByteBuffer inner) {
        this.inner = inner;
    }

    public void clear() {
        inner.clear();
    }

    public void putShort(short s) {
        inner.putShort(s);
    }

    public void putInt(int i) {
        inner.putInt(i);
    }

    public void put(byte[] bytes) {
        inner.put(bytes);
    }

    public void put(byte b) {
        inner.put(b);
    }

    public void put(Entry value) {
        put(value.key());
        put(value.field());
        put(value.value());
    }

    public void put(Key key) {
        putShort(Key.MAGIC_NUMBER);
        putInt(key.length());
        put(key.getBytes());
    }

    public void put(Field field) {
        putShort(Field.MAGIC_NUMBER);
        putInt(field.length());
        put(field.getBytes());
    }

    public void put(Value value) {
        putShort(Value.MAGIC_NUMBER);
        putInt(value.length());
        put(value.value());
    }

    /**
     * Returns the number of elements remaining in the buffer, which indicates
     * how much data can still be written or read from this buffer without exceeding
     * its capacity or current limits.
     *
     * @return the number of elements remaining in the buffer
     */
    public int remaining() {
        return inner.remaining();
    }

    /**
     * Forces any changes made to the mapped byte buffer to be written to the
     * underlying storage device. This ensures data consistency by making
     * sure that all modifications to the buffer are persisted to disk.
     *
     * @throws UncheckedIOException if an I/O error occurs during the operation
     */
    public void sync() throws UncheckedIOException {
        inner.force();
    }

    /**
     * Ensures that the buffer backing the mapped byte buffer is loaded into memory.
     * This operation forces the content of the mapped region to be loaded into memory
     * for improved access performance. It internally delegates to the `load`
     * method of the underlying `MappedByteBuffer`.
     */
    public void load() {
        inner.load();
    }

    public int position() {
        return inner.position();
    }

    public void fill(Entry entry) {
        put(entry.key());
        put(entry.field());
        put(entry.value());
        put((byte) 0);
    }

    public ByteBuffer get(OffsetBoundary boundary) {
        ByteBuffer dup = inner.duplicate();
        dup.position(boundary.start());
        dup.limit(boundary.end());
        return dup.slice();
    }
}
