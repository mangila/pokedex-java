package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.Pokemon;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpeciesRequest;
import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.pokemon.props.PokemonTaskProperties;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi.PokeApiBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Stream;

@Service
public class PokemonTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    private final PokemonTaskProperties taskProperties;
    private final PokeApiBouncerClient pokeApiBouncerClient;
    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public PokemonTask(PokemonTaskProperties taskProperties,
                       PokeApiBouncerClient pokeApiBouncerClient,
                       MongoBouncerClient mongoBouncerClient,
                       RedisBouncerClient redisBouncerClient) {
        this.taskProperties = taskProperties;
        this.pokeApiBouncerClient = pokeApiBouncerClient;
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    /**
     * 0. Read from Redis stream <br>
     * 1. Does it have any data? Else exit execution <br>
     * 2. Fetch PokemonSpeciesResponse from Cache or PokeAPI <br>
     * 3. Glue everything together and create a PokemonSpeciesProto <br>
     * 4. request mongodb-bouncer to save one to MongoDb <br>
     * 5. Push POKEMON_MEDIA_EVENT messages to Redis Stream <br>
     * 6. Acknowledge the POKEMON_NAME_EVENT message to Redis Stream
     */
    @Override
    public void run() {
        var message = redisBouncerClient.streamOps()
                .readOne(StreamRecord.newBuilder()
                        .setStreamKey(taskProperties.getNameStreamKey().getKey())
                        .build());
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages");
            return;
        }
        var observer = getStreamObserver();
        try {
            var proto = Stream.ofNullable(data.get("name"))
                    .peek(name -> log.info("Process - {}", name))
                    .map(speciesName -> PokemonSpeciesRequest.newBuilder()
                            .setPokemonSpeciesName(speciesName)
                            .build())
                    .map(pokeApiBouncerClient::fetchPokemonSpecies)
                    .findFirst()
                    .orElseThrow();
            mongoBouncerClient.mongoDb().saveOne(proto);
            proto.getVarietiesList()
                    .stream()
                    .map(Pokemon::getMediaMetadataList)
                    .flatMap(Collection::stream)
                    .map(media -> StreamRecord.newBuilder()
                            .setStreamKey(taskProperties.getMediaStreamKey().getKey())
                            .putData("pokemon_name", media.getPokemonName())
                            .putData("species_id", String.valueOf(media.getSpeciesId()))
                            .putData("pokemon_id", String.valueOf(media.getPokemonId()))
                            .putData("description", media.getDescription())
                            .putData("url", media.getUrl())
                            .build())
                    .forEach(observer::onNext);
        } catch (Exception e) {
            log.error("ERR", e);
            redisBouncerClient.streamOps().addOne(
                    StreamRecord.newBuilder()
                            .setStreamKey(RedisStreamKey.POKEMON_NAME_DEAD_EVENT.getKey())
                            .putAllData(data)
                            .build()
            );
        } finally {
            observer.onCompleted();
            redisBouncerClient.streamOps()
                    .acknowledgeOne(StreamRecord.newBuilder()
                            .setStreamKey(taskProperties.getNameStreamKey().getKey())
                            .setRecordId(message.getRecordId())
                            .build());
        }
    }

    private StreamObserver<StreamRecord> getStreamObserver() {
        return redisBouncerClient.streamOps()
                .addWithClientStream(new StreamObserver<>() {
                    @Override
                    public void onNext(Empty empty) {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("ERR", throwable);
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Media Stream finished");
                    }
                });
    }
}
