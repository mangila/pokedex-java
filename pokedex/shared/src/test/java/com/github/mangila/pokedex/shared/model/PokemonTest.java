package com.github.mangila.pokedex.shared.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class PokemonTest {

    @Test
    void test() throws IOException {
        var p = new Pokemon(1, "bulba");
        //  var b =  p.toBytes();
        // System.out.println(Arrays.toString(b));
        // System.out.println(Pokemon.fromBytes(b).getName());
    }

}