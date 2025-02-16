package com.github.mangila.model.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonIdTest {

    @Test
    void TestEmptyAndNull() {
        assertThatThrownBy(() -> new PokemonId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id cannot be empty");
        assertThatThrownBy(() -> new PokemonId((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void TestSpecial() {
        assertThatThrownBy(() -> new PokemonId("#%!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id not valid number");
        assertThatThrownBy(() -> new PokemonId("4.5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id not valid number");
    }

    @Test
    void TestValid() {
        assertThatCode(() -> new PokemonId(1)).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonId(45)).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonId("1")).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonId("12")).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonId("1337")).doesNotThrowAnyException();
        assertThatCode(() -> new PokemonId("10555")).doesNotThrowAnyException();
    }
}