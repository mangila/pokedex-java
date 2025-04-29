package com.github.mangila.pokedex.shared;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class DoublyLinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addHead(T item) {
        var newNode = new Node<T>(null, item, head);
        if (Objects.nonNull(head)) {
            head.prev = newNode;
        }
        head = newNode;
        if (Objects.isNull(tail)) {
            tail = head;
        }
        size++;
    }

    public void addTail(T item) {
        var newNode = new Node<T>(tail, item, null);
        if (Objects.nonNull(tail)) {
            tail.next = newNode;
        }
        tail = newNode;
        if (Objects.isNull(head)) {
            head = tail;
        }
        size++;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return Objects.nonNull(current);
            }

            @Override
            public T next() {
                var currentValue = current.item;
                current = current.next;
                return currentValue;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }


    private static class Node<T> {
        private T item;
        private Node<T> prev;
        private Node<T> next;

        public Node(Node<T> prev, T item, Node<T> next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

}
