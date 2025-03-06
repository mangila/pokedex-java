package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.google.protobuf.StringValue;
import org.springframework.stereotype.Service;

@Service
public class PokeApiBouncerClient implements PokeApi {

    private final PokeApiGrpc.PokeApiBlockingStub blockingStub;

    public PokeApiBouncerClient(PokeApiGrpc.PokeApiBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    @Override
    public GenerationResponsePrototype fetchGeneration(StringValue request) {
        return blockingStub.fetchGeneration(request);
    }

    @Override
    public PokemonSpeciesResponsePrototype fetchPokemonSpecies(StringValue request) {
        return blockingStub.fetchPokemonSpecies(request);
    }

    @Override
    public EvolutionChainResponsePrototype fetchEvolutionChain(StringValue request) {
        return blockingStub.fetchEvolutionChain(request);
    }

    @Override
    public PokemonResponsePrototype fetchPokemon(StringValue request) {
        return blockingStub.fetchPokemon(request);
    }
}
