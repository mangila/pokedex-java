package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PokemonDatabaseTest {

    @Test
    @Disabled
    void shouldPutAndGetPokemon() {
        // Given
        var database = PokemonDatabase.getInstance();
        var pokemon = new Pokemon(1, "Bulbasaur");

        // When
        database.put("bulbasaur", pokemon);
        var retrievedPokemon = database.get("bulbasaur");

        // Then
        assertThat(retrievedPokemon)
                .isNotNull()
                .isEqualTo(pokemon);

        database.deleteFile();
    }

    @Test
    @Disabled
    void shouldReturnNullForNonExistentKey() {
        // Given
        var database = PokemonDatabase.getInstance();

        // When
        var retrievedPokemon = database.get("non-existent-key");

        // Then
        assertThat(retrievedPokemon).isNull();

        database.deleteFile();
    }
}
