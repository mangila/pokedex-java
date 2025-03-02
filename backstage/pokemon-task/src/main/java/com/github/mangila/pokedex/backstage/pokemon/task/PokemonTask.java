package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.mongodb.MongoDbBouncerClient;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.pokemon.handler.PokemonHandler;
import com.github.mangila.pokedex.backstage.pokemon.mapper.PokeApiMapper;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Stream;

@Service
public class PokemonTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    private final MongoDbBouncerClient mongoDbBouncerClient;
    private final RedisBouncerClient redisBouncerClient;
    private final PokemonHandler pokemonHandler;
    private final PokeApiMapper pokeApiMapper;

    public PokemonTask(MongoDbBouncerClient mongoDbBouncerClient,
                       RedisBouncerClient redisBouncerClient,
                       PokemonHandler pokemonHandler,
                       PokeApiMapper pokeApiMapper) {
        this.mongoDbBouncerClient = mongoDbBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
        this.pokemonHandler = pokemonHandler;
        this.pokeApiMapper = pokeApiMapper;
    }

    @Override
    public void run(String[] args) {
        var message = redisBouncerClient.streamOps()
                .readOne(StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                        .build());
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        var document = Stream.ofNullable(data.get("name"))
                .peek(name -> log.info("name: {}", name))
                .map(pokemonHandler::fetchSpecies)
                .map(pokeApiMapper::toDocument)
                .findFirst()
                .orElseThrow();
        // TODO: add to media stream
        // TODO: add to db
        // TODO: acknowledge message to redis
    }
}
