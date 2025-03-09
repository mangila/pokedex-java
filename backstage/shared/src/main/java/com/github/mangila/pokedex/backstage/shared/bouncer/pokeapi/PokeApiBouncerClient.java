package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.species.PokemonSpeciesResponse;
import org.springframework.stereotype.Service;

@Service
public class PokeApiBouncerClient implements PokeApi {

    private final PokeApiGrpc.PokeApiBlockingStub blockingStub;

    public PokeApiBouncerClient(PokeApiGrpc.PokeApiBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    @Override
    public GenerationResponse fetchGeneration(GenerationRequest request) {
        return blockingStub.fetchGeneration(request);
    }

    @Override
    public PokemonSpeciesResponse fetchPokemonSpecies(PokemonSpeciesRequest request) {
        return blockingStub.fetchPokemonSpecies(request);
    }

    @Override
    public EvolutionChainResponse fetchEvolutionChain(EvolutionChainRequest request) {
        return blockingStub.fetchEvolutionChain(request);
    }

    @Override
    public PokemonResponse fetchPokemon(PokemonRequest request) {
        return blockingStub.fetchPokemon(request);
    }
}
