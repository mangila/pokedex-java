package com.github.mangila.pokedex.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListIterator;
import java.util.Objects;

@SuppressWarnings("unchecked") // generic array creation
public class DynamicArrayList<T> {

    private static final Logger log = LoggerFactory.getLogger(DynamicArrayList.class);

    private T[] data;
    private int size;

    public DynamicArrayList(int capacity) {
        this.data = (T[]) new Object[capacity];
    }

    public void add(T item) {
        if (size == data.length) {
            resize();
        }
        data[size] = item;
        size++;
    }

    public T get(int index) {
        Objects.checkIndex(index, size);
        return data[index];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    public void trim() {
        if (size < data.length) {
            log.debug("Trimming array to {} - from {}", size, data.length);
            T[] newData = (T[]) new Object[size];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    public ListIterator<T> iterator(int startIndex) {
        return listIterator(startIndex);
    }

    private ListIterator<T> listIterator(int startIndex) {
        Objects.checkIndex(startIndex, size);
        return new ListIterator<T>() {
            private int cursor = startIndex;

            @Override
            public boolean hasNext() {
                return cursor != size;
            }

            @Override
            public T next() {
                int i = cursor;
                if (i >= size) {
                    throw new IllegalStateException("No more elements");
                }
                cursor = i + 1;
                return data[i];
            }

            @Override
            public boolean hasPrevious() {
                return cursor != 0;
            }

            @Override
            public T previous() {
                int i = cursor - 1;
                if (i < 0) {
                    throw new IllegalStateException("No previous elements");
                }
                cursor = i;
                return data[i];
            }

            @Override
            public int nextIndex() {
                return cursor;
            }

            @Override
            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("only for iteration, not modification");
            }

            @Override
            public void set(T t) {
                throw new UnsupportedOperationException("only for iteration, not modification");
            }

            @Override
            public void add(T t) {
                throw new UnsupportedOperationException("only for iteration, not modification");
            }
        };
    }

    private void resize() {
        var newSize = data.length * 2;
        log.debug("Resizing array to {}", newSize);
        T[] newData = (T[]) new Object[newSize];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
}
