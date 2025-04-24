package com.github.mangila.pokedex.shared.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

class PokeApiUriTest {

    @Test
    void constructor_shouldThrowNullPointerException_whenUriIsNull() {
        assertThatThrownBy(() -> new PokeApiUri(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("URI cannot be null");
    }

    @Test
    void constructor_shouldCreateInstance_whenUriIsValid() {
        // Given
        URI validUri = URI.create("https://pokeapi.co/api/v2/pokemon/1");

        // When
        PokeApiUri pokeApiUri = new PokeApiUri(validUri);

        // Then
        assertThat(pokeApiUri.uri()).isEqualTo(validUri);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenUriStringIsNull() {
        assertThatThrownBy(() -> PokeApiUri.create(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("URI string cannot be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://pokeapi.co/api/v2/pokemon/1",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
    })
    void create_shouldCreateInstance_whenUriIsValid(String validUriString) {
        // When
        PokeApiUri pokeApiUri = PokeApiUri.create(validUriString);

        // Then
        assertThat(pokeApiUri.uri()).isEqualTo(URI.create(validUriString));
        assertThat(pokeApiUri.toUriString()).isEqualTo(validUriString);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenUriHasInvalidHost() {
        // Given
        String invalidHostUri = "https://example.com/api/v2/pokemon/1";

        // When/Then
        assertThatThrownBy(() -> PokeApiUri.create(invalidHostUri))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should start with 'raw.githubusercontent.com' or 'pokeapi.co'");
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenUriHasInvalidScheme() {
        // Given
        String invalidSchemeUri = "http://pokeapi.co/api/v2/pokemon/1";

        // When/Then
        assertThatThrownBy(() -> PokeApiUri.create(invalidSchemeUri))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should be 'https'");
    }

    @Test
    void toUriString_shouldReturnStringRepresentation() {
        // Given
        String uriString = "https://pokeapi.co/api/v2/pokemon/1";
        PokeApiUri pokeApiUri = PokeApiUri.create(uriString);

        // When
        String result = pokeApiUri.toUriString();

        // Then
        assertThat(result).isEqualTo(uriString);
    }

    @Test
    void compactConstructor_shouldValidate() {
        var validUri = URI.create("https://pokeapi.co/api/v2/pokemon/1");
        assertThatCode(() -> new PokeApiUri(validUri)).doesNotThrowAnyException();
    }

    @Test
    void compactConstructor_shouldValidateAndThrow() {
        var invalidUri = URI.create("https://example.com/api/v2/pokemon/1");
        assertThatThrownBy(() -> new PokeApiUri(invalidUri))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should start with 'raw.githubusercontent.com' or 'pokeapi.co'");
    }
}