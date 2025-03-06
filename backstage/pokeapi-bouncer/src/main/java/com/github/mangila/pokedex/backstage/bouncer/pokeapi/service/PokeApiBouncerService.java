package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.util.UriUtil;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.net.URI;

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
        var speciesName = request.getValue();
        var key = RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat(speciesName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), PokemonSpeciesResponsePrototype.class);
        if (cacheValue.isEmpty()) {
            var response = pokeApiTemplate.fetchSpecies(speciesName);
            var proto = response.toProto();
            var entryRequest = EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(Any.pack(proto))
                    .build();
            redisBouncerClient.valueOps().set(entryRequest);
            responseObserver.onNext(proto);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void fetchEvolutionChain(StringValue request, StreamObserver<EvolutionChainResponsePrototype> responseObserver) {
        var evolutionChainId = UriUtil.getLastPathSegment(URI.create(request.getValue()));
        var key = RedisKeyPrefix.EVOLUTION_CHAIN_KEY_PREFIX.getPrefix().concat(evolutionChainId);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), EvolutionChainResponsePrototype.class);
        if (cacheValue.isEmpty()) {
            var response = pokeApiTemplate.fetchEvolutionChain(evolutionChainId);
            var proto = response.toProto();
            var entryRequest = EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(Any.pack(proto))
                    .build();
            redisBouncerClient.valueOps().set(entryRequest);
            responseObserver.onNext(proto);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void fetchPokemon(StringValue request, StreamObserver<PokemonResponsePrototype> responseObserver) {
        var pokemonName = request.getValue();
        var key = RedisKeyPrefix.EVOLUTION_CHAIN_KEY_PREFIX.getPrefix().concat(pokemonName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), PokemonResponsePrototype.class);
        if (cacheValue.isEmpty()) {
            var response = pokeApiTemplate.fetchPokemon(pokemonName);
            var proto = response.toProto();
            var entryRequest = EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(Any.pack(proto))
                    .build();
            redisBouncerClient.valueOps().set(entryRequest);
            responseObserver.onNext(proto);
        }
        responseObserver.onCompleted();
    }
}
