package com.github.mangila.pokedex.backstage.shared.model.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonIdTest {

    @Test
    void TestEmptyAndNull() {
        assertThatThrownBy(() -> PokemonId.create(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id cannot be empty");
        assertThatThrownBy(() -> PokemonId.create((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void TestSpecial() {
        assertThatThrownBy(() -> PokemonId.create("#123%!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id not valid number");
        assertThatThrownBy(() -> PokemonId.create("4.5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Id not valid number");
    }

    @Test
    void TestValid() {
        assertThatCode(() -> PokemonId.create("1")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonId.create("45")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonId.create("151")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonId.create("1050")).doesNotThrowAnyException();
        assertThatCode(() -> PokemonId.create(12345)).doesNotThrowAnyException();
    }

}