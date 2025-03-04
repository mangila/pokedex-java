package com.github.mangila.pokedex.backstage.shared.model.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonNameTest {

    @Test
    void TestEmptyAndNull() {
        assertThatThrownBy(() -> PokemonName.create(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name cannot be empty");
        assertThatThrownBy(() -> PokemonName.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name cannot be null");
    }

    @Test
    void TestSpecial() {
        assertThatThrownBy(() -> PokemonName.create("#123%!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name not valid string");
    }

    @Test
    void TestValid() {
        assertThatCode(() -> PokemonName.create("bulbasaur")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonName.create("mr-mime")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonName.create("Test-Pokemon-45")).doesNotThrowAnyException();
    }

}