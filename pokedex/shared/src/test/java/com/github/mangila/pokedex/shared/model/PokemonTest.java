package com.github.mangila.pokedex.shared.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PokemonTest {

    @Test
    void shouldSerializeAndDeserializePokemon() {
        var bulbaBytes = new Pokemon(1, "bulba").toBytes();
        assertThat(Pokemon.fromBytes(bulbaBytes))
                .isNotNull()
                .extracting(Pokemon::id, Pokemon::name)
                .contains(1, "bulba");
    }
}