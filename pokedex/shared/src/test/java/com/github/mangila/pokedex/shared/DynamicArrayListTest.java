package com.github.mangila.pokedex.shared;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class DynamicArrayListTest {

    @Test
    void abc() {
        var l = new DynamicArrayList<Integer>(24);
        l.add(0);
        l.add(1);
        l.trim();
        l.add(2);
        l.add(3);
        l.add(4);

        var iter = l.iterator(0);

        while (iter.hasNext()) {
            var next = iter.next();
            System.out.println(next);
        }

        System.out.println();

        while (iter.hasPrevious()) {
            var previous = iter.previous();
            System.out.println(previous);
        }


    }
}