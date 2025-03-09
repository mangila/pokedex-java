package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.EvolutionChainRequest;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationRequest;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonRequest;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonSpeciesRequest;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.species.PokemonSpeciesResponse;

public interface PokeApi {

    GenerationResponse fetchGeneration(GenerationRequest request);

    PokemonSpeciesResponse fetchPokemonSpecies(PokemonSpeciesRequest request);

    EvolutionChainResponse fetchEvolutionChain(EvolutionChainRequest request);

    PokemonResponse fetchPokemon(PokemonRequest request);
}