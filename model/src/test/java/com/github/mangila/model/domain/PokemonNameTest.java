package com.github.mangila.model.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonNameTest {

    @Test
    void TestEmptyAndNull() {
        assertThatThrownBy(() -> new PokemonName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name cannot be empty");
        assertThatThrownBy(() -> new PokemonName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name cannot be null");
    }

    @Test
    void TestSpecial() {
        assertThatThrownBy(() -> new PokemonName("#123%!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Name not valid string");
    }

    @Test
    void TestValid() {
        assertThatCode(() -> new PokemonName("bulbasaur")).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonName("mr-mime")).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonName("Test-Pokemon-45")).doesNotThrowAnyException();
    }
}