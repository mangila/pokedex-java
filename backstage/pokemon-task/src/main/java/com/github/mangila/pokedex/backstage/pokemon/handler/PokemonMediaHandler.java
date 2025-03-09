package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.Cries;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.Sprites;
import com.github.mangila.pokedex.backstage.model.grpc.redis.stream.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonId;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PokemonMediaHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonMediaHandler.class);

    private final RedisBouncerClient redisBouncerClient;

    public PokemonMediaHandler(RedisBouncerClient redisBouncerClient) {
        this.redisBouncerClient = redisBouncerClient;
    }

    public void handle(PokemonId speciesId,
                       PokemonId pokemonId,
                       PokemonName name,
                       Sprites sprites) {
        var observer = getStreamObserver("sprites");
        addIfUrlHasText(speciesId, pokemonId, name, "front-default", sprites.getFrontDefault(), observer);
        addIfUrlHasText(speciesId, pokemonId, name, "official-artwork", sprites.getOfficialArtwork(), observer);
        observer.onCompleted();
    }

    public void handle(PokemonId speciesId,
                       PokemonId pokemonId,
                       PokemonName name,
                       Cries cries) {
        var observer = getStreamObserver("cries");
        addIfUrlHasText(speciesId, pokemonId, name, "legacy", cries.getLegacy(), observer);
        addIfUrlHasText(speciesId, pokemonId, name, "latest", cries.getLatest(), observer);
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

    private void addIfUrlHasText(
            PokemonId speciesId,
            PokemonId pokemonId,
            PokemonName pokemonName,
            String description,
            String url,
            StreamObserver<StreamRecord> streamObserver) {
        if (StringUtils.hasText(url)) {
            log.debug("{} - {} - {} - {} - {}",
                    speciesId.getValue(),
                    pokemonId.getValue(),
                    pokemonName,
                    description,
                    url);
            streamObserver.onNext(
                    StreamRecord.newBuilder()
                            .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                            .putData("species_id", speciesId.getValue())
                            .putData("pokemon_id", pokemonId.getValue())
                            .putData("name", pokemonName.getValue())
                            .putData("description", description)
                            .putData("url", url)
                            .build()
            );
        }
    }
}
