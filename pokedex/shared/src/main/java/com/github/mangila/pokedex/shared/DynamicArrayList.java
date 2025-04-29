package com.github.mangila.pokedex.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.function.Consumer;

@SuppressWarnings("unchecked") // generic array creation
public class DynamicArrayList<T> implements Iterable<T> {

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

    private void resize() {
        var newSize = data.length * 2;
        log.debug("Resizing array to {}", newSize);
        T[] newData = (T[]) new Object[newSize];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                var currentValue = data[index];
                index++;
                return currentValue;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }
}
