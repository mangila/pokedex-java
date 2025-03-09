package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.mapper.PokeApiProtoMapper;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.species.PokemonSpeciesResponse;
import com.github.mangila.pokedex.backstage.model.grpc.redis.entry.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
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
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), GenerationResponse.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchGeneration(generation));
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
    public void fetchPokemonSpecies(PokemonSpeciesRequest request, StreamObserver<PokemonSpeciesResponse> responseObserver) {
        var pokemonSpeciesName = request.getPokemonSpeciesName();
        var key = RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat(pokemonSpeciesName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), PokemonSpeciesResponse.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchSpecies(pokemonSpeciesName));
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
    public void fetchEvolutionChain(EvolutionChainRequest request, StreamObserver<EvolutionChainResponse> responseObserver) {
        var evolutionChainId = request.getEvolutionChainId();
        var key = RedisKeyPrefix.EVOLUTION_CHAIN_KEY_PREFIX.getPrefix().concat(evolutionChainId);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), EvolutionChainResponse.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchEvolutionChain(evolutionChainId));
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
    public void fetchPokemon(PokemonRequest request, StreamObserver<PokemonResponse> responseObserver) {
        var pokemonName = request.getPokemonName();
        var key = RedisKeyPrefix.POKEMON_KEY_PREFIX.getPrefix().concat(pokemonName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), PokemonResponse.class);
        if (cacheValue.isEmpty()) {
            var proto = pokeApiProtoMapper.map(pokeApiTemplate.fetchPokemon(pokemonName));
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
}
