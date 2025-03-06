package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.species.PokemonSpeciesResponse;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class PokemonHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonHandler.class);

    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;

    public PokemonHandler(PokeApiTemplate pokeApiTemplate,
                          RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.redisBouncerClient = redisBouncerClient;
    }

    public PokemonSpeciesResponse fetchSpecies(String speciesName) {
        var key = RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat(speciesName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build());
        if (cacheValue.isEmpty()) {
            log.debug("Cache miss - {}", key);
            var speciesResponse = pokeApiTemplate.fetchSpecies(speciesName);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(speciesResponse.toJson(objectMapper))
                    .build());
            return speciesResponse;
        }
        log.debug("Cache hit - {}", key);
        return JsonUtil.readValueFrom(cacheValue.get(), objectMapper, PokemonSpeciesResponse.class);
    }

    public PokemonResponse fetchPokemon(String pokemonName) {
        var key = RedisKeyPrefix.POKEMON_KEY_PREFIX.getPrefix().concat(pokemonName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build());
        if (cacheValue.isEmpty()) {
            log.debug("Cache miss - {}", key);
            var pokemonResponse = pokeApiTemplate.fetchPokemon(pokemonName);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(pokemonResponse.toJson(objectMapper))
                    .build());
            return pokemonResponse;
        }
        log.debug("Cache hit - {}", key);
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
            log.debug("Cache miss - {}", key);
            var evolutionChainResponse = pokeApiTemplate.fetchEvolutionChain(evolutionChainId);
            redisBouncerClient.valueOps().set(EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(evolutionChainResponse.toJson(objectMapper))
                    .build());
            return evolutionChainResponse;
        }
        log.debug("Cache hit - {}", key);
        return JsonUtil.readValueFrom(cacheValue.get(), objectMapper, EvolutionChainResponse.class);
    }
}
