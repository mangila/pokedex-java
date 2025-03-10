package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;


import com.github.mangila.pokedex.backstage.model.grpc.model.GenerationRequest;
import com.github.mangila.pokedex.backstage.model.grpc.model.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpeciesRequest;

public interface PokeApi {

    GenerationResponse fetchGeneration(GenerationRequest request);

    PokemonSpecies fetchPokemonSpecies(PokemonSpeciesRequest request);

}