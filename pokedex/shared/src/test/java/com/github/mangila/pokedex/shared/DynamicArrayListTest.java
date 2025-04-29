package com.github.mangila.pokedex.shared;

import org.junit.jupiter.api.Test;

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

        System.out.println(l.get(2));

        l.forEach(System.out::println);
    }
}