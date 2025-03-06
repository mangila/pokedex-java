package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokeApiGrpc;
import com.google.protobuf.StringValue;
import org.springframework.stereotype.Service;

@Service
public class PokeApiBouncerClient implements PokeApi {

    private final PokeApiGrpc.PokeApiBlockingStub blockingStub;

    public PokeApiBouncerClient(PokeApiGrpc.PokeApiBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    @Override
    public GenerationResponsePrototype getGeneration(StringValue request) {
        return blockingStub.getGeneration(request);
    }
}
