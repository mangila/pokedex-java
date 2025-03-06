package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class PokeApiBouncerService extends PokeApiGrpc.PokeApiImplBase {

    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;

    public PokeApiBouncerService(PokeApiTemplate pokeApiTemplate,
                                 RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void fetchGeneration(StringValue request, StreamObserver<GenerationResponsePrototype> responseObserver) {
        var generationName = request.getValue();
        var key = RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(generationName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), GenerationResponsePrototype.class);
        if (cacheValue.isEmpty()) {
            var response = pokeApiTemplate.fetchGeneration(generationName);
            var proto = response.toProto();
            var entryRequest = EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(Any.pack(proto))
                    .build();
            redisBouncerClient.valueOps().set(entryRequest);
            responseObserver.onNext(proto);
        } else {
            responseObserver.onNext(cacheValue.get());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void fetchPokemonSpecies(StringValue request, StreamObserver<PokemonSpeciesResponsePrototype> responseObserver) {
        super.fetchPokemonSpecies(request, responseObserver);
    }

    @Override
    public void fetchEvolutionChain(StringValue request, StreamObserver<EvolutionChainResponsePrototype> responseObserver) {
        super.fetchEvolutionChain(request, responseObserver);
    }

    @Override
    public void fetchPokemon(StringValue request, StreamObserver<PokemonResponsePrototype> responseObserver) {
        super.fetchPokemon(request, responseObserver);
    }
}
