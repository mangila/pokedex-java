package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.model.GenerationRequest;
import com.github.mangila.pokedex.backstage.model.grpc.model.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpeciesRequest;
import com.github.mangila.pokedex.backstage.model.grpc.service.PokeApiGrpc;
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
    public PokemonSpecies fetchPokemonSpecies(PokemonSpeciesRequest request) {
        return blockingStub.fetchPokemonSpecies(request);
    }
}
