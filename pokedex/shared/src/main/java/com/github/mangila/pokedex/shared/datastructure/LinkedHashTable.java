package com.github.mangila.pokedex.shared.datastructure;

public class LinkedHashTable<K, V> {

    private final DynamicArrayList<DoublyLinkedList<HashNode<K, V>>> table = new DynamicArrayList<>(4);

    private static class HashNode<K, V> {
        private K key;
        private V value;
    }
}
