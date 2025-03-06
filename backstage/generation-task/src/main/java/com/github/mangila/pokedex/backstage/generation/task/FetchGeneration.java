package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.google.protobuf.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class FetchGeneration implements Callable<GenerationResponse> {

    private static final Logger log = LoggerFactory.getLogger(FetchGeneration.class);
    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;
    private final Generation generation;

    public FetchGeneration(
            Generation generation,
            PokeApiTemplate pokeApiTemplate,
            RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.redisBouncerClient = redisBouncerClient;
        this.generation = generation;
    }

    @Override
    public GenerationResponse call() {
        log.info("{}", generation.getName());
        var generationName = generation.getName();
        var key = RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(generationName);
        var cacheValue = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(key)
                        .build(), GenerationResponsePrototype.class);
        if (cacheValue.isEmpty()) {
            var response = pokeApiTemplate.fetchGeneration(generationName);
            var entryRequest = EntryRequest.newBuilder()
                    .setKey(key)
                    .setValue(Any.pack(response.toProto()))
                    .build();
            redisBouncerClient.valueOps().set(entryRequest);
            return response;
        }
        return GenerationResponse.fromProto(cacheValue.get());
    }
}
