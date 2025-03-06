package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.EvolutionChainResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonSpeciesResponsePrototype;
import com.google.protobuf.StringValue;

public interface PokeApi {

    GenerationResponsePrototype fetchGeneration(StringValue request);

    PokemonSpeciesResponsePrototype fetchPokemonSpecies(StringValue request);

    EvolutionChainResponsePrototype fetchEvolutionChain(StringValue request);

    PokemonResponsePrototype fetchPokemon(StringValue request);
}