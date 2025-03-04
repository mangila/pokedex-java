package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.species.SpeciesResponse;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.util.JsonUtil;
import com.github.mangila.pokedex.backstage.shared.util.UriUtil;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class PokemonHandler {

    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;
    private final ObjectMapper objectMapper;

    public PokemonHandler(PokeApiTemplate pokeApiTemplate,
                          RedisBouncerClient redisBouncerClient,
                          ObjectMapper objectMapper) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.redisBouncerClient = redisBouncerClient;
        this.objectMapper = objectMapper;
    }

    public SpeciesResponse fetchSpecies(String speciesName) {
        var key = RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat(speciesName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build());
        if (cacheValue.isEmpty()) {
            var speciesResponse = pokeApiTemplate.fetchSpecies(speciesName);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(speciesResponse.toJson(objectMapper))
                    .build());
            return speciesResponse;
        }
        return JsonUtil.readValueFrom(cacheValue.get(), objectMapper, SpeciesResponse.class);
    }

    public PokemonResponse fetchPokemon(String pokemonName) {
        var key = RedisKeyPrefix.POKEMON_KEY_PREFIX.getPrefix().concat(pokemonName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build());
        if (cacheValue.isEmpty()) {
            var pokemonResponse = pokeApiTemplate.fetchPokemon(pokemonName);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(pokemonResponse.toJson(objectMapper))
                    .build());
            return pokemonResponse;
        }
        return JsonUtil.readValueFrom(cacheValue.get(), objectMapper, PokemonResponse.class);
    }

    public EvolutionChainResponse fetchEvolutionChain(URI uri) {
        var evolutionChainId = UriUtil.getLastPathSegment(uri);
        var key = RedisKeyPrefix.EVOLUTION_CHAIN_KEY_PREFIX.getPrefix().concat(evolutionChainId);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build());
        if (cacheValue.isEmpty()) {
            var evolutionChainResponse = pokeApiTemplate.fetchEvolutionChain(evolutionChainId);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(evolutionChainResponse.toJson(objectMapper))
                    .build());
            return evolutionChainResponse;
        }
        return JsonUtil.readValueFrom(cacheValue.get(), objectMapper, EvolutionChainResponse.class);
    }
}
