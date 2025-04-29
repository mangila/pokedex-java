package com.github.mangila.pokedex.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoublyLinkedListTest {

    @Test
    void abc() {
        var list = new DoublyLinkedList<Integer>();

        list.addHead(1);
        list.addHead(2);
        list.addHead(2);

        list.addTail(3);
        list.addTail(4);

        list.forEach(System.out::println);

    }

}