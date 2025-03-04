package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites.Sprites;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonId;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Objects;

@Service
public class PokemonMediaHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonMediaHandler.class);

    private final RedisBouncerClient redisBouncerClient;

    public PokemonMediaHandler(RedisBouncerClient redisBouncerClient) {
        this.redisBouncerClient = redisBouncerClient;
    }

    public void handle(Pair<PokemonId, PokemonId> idPair, PokemonName name, Sprites sprites) {
        var observer = getStreamObserver("sprites");
        addIfNotNull(idPair, name, "front-default", URI.create(sprites.frontDefault()), observer);
        addIfNotNull(idPair, name, "back-default", URI.create(sprites.backDefault()), observer);
        observer.onCompleted();
    }

    public void handle(Pair<PokemonId, PokemonId> idPair, PokemonName name, Cries cries) {
        var observer = getStreamObserver("cries");
        addIfNotNull(idPair, name, "legacy", URI.create(cries.legacy()), observer);
        addIfNotNull(idPair, name, "latest", URI.create(cries.latest()), observer);
        observer.onCompleted();
    }

    private StreamObserver<StreamRecord> getStreamObserver(String type) {
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
                        log.debug("Stream finished - {}", type);
                    }
                });
    }

    private void addIfNotNull(
            Pair<PokemonId, PokemonId> idPair,
            PokemonName pokemonName,
            String description,
            URI url,
            StreamObserver<StreamRecord> streamObserver) {
        if (Objects.nonNull(url)) {
            log.debug("{} - {} - {} - {} - {}",
                    idPair.getFirst(),
                    idPair.getSecond(),
                    pokemonName,
                    description,
                    url);
            streamObserver.onNext(
                    StreamRecord.newBuilder()
                            .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                            .putData("species_id", idPair.getFirst().getValue())
                            .putData("pokemon_id", idPair.getSecond().getValue())
                            .putData("name", pokemonName.getValue())
                            .putData("description", description)
                            .putData("url", url.toString())
                            .build()
            );
        }
    }
}
