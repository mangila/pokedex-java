package com.github.mangila.pokedex.shared.pokeapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PokeApiHostTest {

    @Test
    void abc() {
        new PokeApiHost(null);
        new PokeApiHost("asfg");
    }
}