package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi.PokeApiBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@Service
public class GenerationTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GenerationTask.class);

    private final PokeApiBouncerClient pokeApiBouncerClient;
    private final RedisBouncerClient redisBouncerClient;
    private final ExecutorService virtualThreadExecutor;

    public GenerationTask(PokeApiBouncerClient pokeApiBouncerClient, RedisBouncerClient redisBouncerClient,
                          ExecutorService virtualThreadExecutor) {
        this.pokeApiBouncerClient = pokeApiBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * 0. Create a grpc client-stream to RedisBouncer <br>
     * 1. Iterate all Generation enums and create a Callable Thread List<br>
     * 2. Start all Threads
     * 3. Get the PokemonName from the response
     * 4. Put the PokemonName on the stream
     *
     * @param args - program arguments from the Main method
     */
    @Override
    public void run(String[] args) throws Exception {
        var observer = getStreamObserver();
        var fetchGenerations = EnumSet.allOf(Generation.class)
                .stream()
                .map(generation -> (Callable<List<PokemonName>>) () -> {
                    log.info("{}", generation.getName());
                    return pokeApiBouncerClient.getGeneration(StringValue.newBuilder()
                                    .setValue(generation.getName())
                                    .build())
                            .getNameList()
                            .stream()
                            .map(PokemonName::create)
                            .toList();
                })
                .toList();
        var futures = virtualThreadExecutor.invokeAll(fetchGenerations);
        for (var future : futures) {
            var pokemonNames = future.get();
            pokemonNames.forEach(pokemonName -> {
                var record = StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                        .putData("name", pokemonName.getValue())
                        .build();
                observer.onNext(record);
            });
        }
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
}
