package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites.Sprites;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PokemonMediaHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonMediaHandler.class);

    private final RedisBouncerClient redisBouncerClient;

    public PokemonMediaHandler(RedisBouncerClient redisBouncerClient) {
        this.redisBouncerClient = redisBouncerClient;
    }

    public void handle(PokemonName name, Sprites sprites) {
        var observer = getStreamObserver();
        addIfNotNull(name.name(), "front-default", sprites.frontDefault(), observer);
        addIfNotNull(name.name(), "back-default", sprites.backDefault(), observer);
        observer.onCompleted();
    }

    public void handle(PokemonName name, Cries cries) {
        var observer = getStreamObserver();
        addIfNotNull(name.name(), "legacy", cries.legacy(), observer);
        addIfNotNull(name.name(), "latest", cries.latest(), observer);
        observer.onCompleted();
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
                        log.info("Stream finished");
                    }
                });
    }

    private void addIfNotNull(
            String name,
            String description,
            String url,
            StreamObserver<StreamRecord> streamObserver) {
        if (Objects.nonNull(url)) {
            log.debug("{} - {} - {}", name, description, url);
            streamObserver.onNext(
                    StreamRecord.newBuilder()
                            .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                            .putData("name", name)
                            .putData("description", description)
                            .putData("url", url)
                            .build()
            );
        }
    }
}
