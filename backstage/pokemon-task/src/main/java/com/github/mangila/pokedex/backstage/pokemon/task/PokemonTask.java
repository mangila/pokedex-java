package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.mongo.MongoBouncerClient;
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

    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;
    private final PokemonHandler pokemonHandler;
    private final PokeApiMapper pokeApiMapper;

    public PokemonTask(MongoBouncerClient mongoBouncerClient,
                       RedisBouncerClient redisBouncerClient,
                       PokemonHandler pokemonHandler,
                       PokeApiMapper pokeApiMapper) {
        this.mongoBouncerClient = mongoBouncerClient;
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
            log.debug("No new messages found");
            return;
        }
        var document = Stream.ofNullable(data.get("name"))
                .peek(name -> log.info("Process - {}", name))
                .map(pokemonHandler::fetchSpecies)
                .map(pokeApiMapper::toDocument)
                .findFirst()
                .orElseThrow();
        mongoBouncerClient.insertOne(document);
        redisBouncerClient.streamOps()
                .acknowledgeOne(StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                        .setRecordId(message.getRecordId())
                        .build());
    }
}
