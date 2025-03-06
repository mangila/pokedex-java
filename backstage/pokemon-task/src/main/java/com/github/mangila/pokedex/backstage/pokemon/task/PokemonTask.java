package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.pokemon.mapper.PokeApiMapper;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi.PokeApiBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Stream;

@Service
public class PokemonTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    private final PokeApiBouncerClient pokeApiBouncerClient;
    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;
    private final PokeApiMapper pokeApiMapper;

    public PokemonTask(PokeApiBouncerClient pokeApiBouncerClient,
                       MongoBouncerClient mongoBouncerClient,
                       RedisBouncerClient redisBouncerClient,
                       PokeApiMapper pokeApiMapper) {
        this.pokeApiBouncerClient = pokeApiBouncerClient;
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
        this.pokeApiMapper = pokeApiMapper;
    }

    /**
     * 0. Read from Redis stream
     * 1. Does it have any data? Else exit execution
     * 2. Fetch PokemonSpeciesResponse from Cache or PokeAPI
     * 3. Glue everything together and create a PokemonSpeciesDocument
     * 4. Save to MongoDb
     * 5. Acknowledge the message to Redis Stream
     *
     * @param args - Program arguments from Main/Invoker
     */
    @Override
    public void run(String[] args) {
        var streamKey = RedisStreamKey.POKEMON_NAME_EVENT.getKey();
        var message = redisBouncerClient.streamOps()
                .readOne(StreamRecord.newBuilder()
                        .setStreamKey(streamKey)
                        .build());
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages found");
            return;
        }
        var document = Stream.ofNullable(data.get("name"))
                .peek(name -> log.info("Process - {}", name))
                .map(StringValue::of)
                .map(pokeApiBouncerClient::fetchPokemonSpecies)
                .map(pokeApiMapper::toProto)
                .findFirst()
                .orElseThrow();
        mongoBouncerClient.mongoDb().saveOne(document);
        redisBouncerClient.streamOps()
                .acknowledgeOne(StreamRecord.newBuilder()
                        .setStreamKey(streamKey)
                        .setRecordId(message.getRecordId())
                        .build());
    }
}
