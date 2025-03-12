package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.mapper.PokeApiProtoMapper;
import com.github.mangila.pokedex.backstage.model.grpc.model.*;
import com.github.mangila.pokedex.backstage.model.grpc.service.PokeApiGrpc;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class PokeApiBouncerService extends PokeApiGrpc.PokeApiImplBase {

    private final PokeApiTemplate pokeApiTemplate;
    private final PokeApiProtoMapper pokeApiProtoMapper;
    private final RedisBouncerClient redisBouncerClient;

    public PokeApiBouncerService(PokeApiTemplate pokeApiTemplate,
                                 PokeApiProtoMapper pokeApiProtoMapper,
                                 RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.pokeApiProtoMapper = pokeApiProtoMapper;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void fetchGeneration(GenerationRequest request, StreamObserver<GenerationResponse> responseObserver) {
        var generation = request.getGeneration();
        var key = RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(generation);
        var cacheValue = redisBouncerClient.valueOps()
                .get(ValueRequest.newBuilder()
                        .setKey(key)
                        .build(), GenerationResponse.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchGeneration(Generation.from(generation)));
            var entryRequest = ValueRequest.newBuilder()
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
    public void fetchPokemonSpecies(PokemonSpeciesRequest request, StreamObserver<PokemonSpecies> responseObserver) {
        var name = PokemonName.create(request.getPokemonSpeciesName());
        var key = RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat(name.getValue());
        var cacheValue = redisBouncerClient.valueOps()
                .get(ValueRequest.newBuilder()
                        .setKey(key)
                        .build(), PokemonSpecies.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchSpecies(name));
            var entryRequest = ValueRequest.newBuilder()
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

}
