package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites.Sprites;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PokemonMediaHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonMediaHandler.class);

    private final RedisBouncerClient redisBouncerClient;

    public PokemonMediaHandler(RedisBouncerClient redisBouncerClient) {
        this.redisBouncerClient = redisBouncerClient;
    }

    public void handle(Sprites sprites) {
        var observer = getStreamObserver();
        addIfNotNull(sprites.frontDefault(), observer);
        addIfNotNull(sprites.backDefault(), observer);
        observer.onCompleted();
    }

    public void handle(Cries cries) {
        var observer = getStreamObserver();
        addIfNotNull(cries.legacy(), observer);
        addIfNotNull(cries.latest(), observer);
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

    private void addIfNotNull(String url, StreamObserver<StreamRecord> streamObserver) {
        streamObserver.onNext(
                StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                        .putData("url", url)
                        .build()
        );
    }
}
