package com.github.mangila.pokedex.shared.pokeapi;

import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import org.junit.jupiter.api.Test;

class PokeApiHostTest {

    @Test
    void abc() {
        new PokeApiHost(null);
        new PokeApiHost("asfg");
    }
}