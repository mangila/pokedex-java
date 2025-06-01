package com.github.mangila.pokedex.shared.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonTest {

    @Test
    void shouldSerializeAndDeserializePokemon() throws IOException {
        var pokemon = new Pokemon(1, "bulba");
        assertThat(pokemon.deserialize(pokemon.serialize()))
                .isNotNull()
                .extracting(Pokemon::id, Pokemon::name)
                .contains(1, "bulba");
    }
}