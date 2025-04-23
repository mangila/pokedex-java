package com.github.mangila.pokedex.scheduler.domain;


import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.response.allpokemons.Result;

/**
 * Represents a Pokémon entry with basic identification information.
 * This record holds a Pokémon's name and its validated API URI.
 */
@lombok.Builder
public record PokemonEntry(
        String name,
        PokeApiUri pokeApiUri
) {
    /**
     * Creates a new PokemonEntry with validation.
     *
     * @param name       The name of the Pokémon
     * @param pokeApiUri The validated URI to the Pokémon resource
     */
    public PokemonEntry {
        java.util.Objects.requireNonNull(name, "Pokémon name cannot be null");
        java.util.Objects.requireNonNull(pokeApiUri, "PokeApiUri cannot be null");
    }

    /**
     * Factory method to create a PokemonEntry from a Result object.
     * Converts the raw URL from Result into a validated PokeApiUri.
     *
     * @param result The Result object containing Pokémon data
     * @return A new PokemonEntry instance
     * @throws IllegalArgumentException if the URI is not valid or doesn't meet requirements
     */
    public static PokemonEntry fromResult(Result result) {
        return new PokemonEntry(result.name(), PokeApiUri.create(result.url()));
    }
}