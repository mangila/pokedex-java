package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@Service
public class GenerationTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GenerationTask.class);

    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;
    private final ExecutorService virtualThreadExecutor;

    public GenerationTask(PokeApiTemplate pokeApiTemplate,
                          RedisBouncerClient redisBouncerClient,
                          ExecutorService virtualThreadExecutor) {
        this.pokeApiTemplate = pokeApiTemplate;
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
                .map(generation -> (Callable<GenerationResponsePrototype>) () -> {
                    log.info("{}", generation.getName());
                    var generationName = generation.getName();
                    var key = RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(generationName);
                    var cacheValue = redisBouncerClient.valueOps()
                            .get(EntryRequest.newBuilder()
                                    .setKey(key)
                                    .build(), GenerationResponsePrototype.class);
                    if (cacheValue.isEmpty()) {
                        var response = pokeApiTemplate.fetchGeneration(generationName);
                        var proto = response.toProto();
                        var entryRequest = EntryRequest.newBuilder()
                                .setKey(key)
                                .setValue(Any.pack(proto))
                                .build();
                        redisBouncerClient.valueOps().set(entryRequest);
                        return proto;
                    }
                    return cacheValue.get();
                })
                .toList();
        var futures = virtualThreadExecutor.invokeAll(fetchGenerations);
        for (var future : futures) {
            var response = future.get();
            response.getNameList()
                    .stream()
                    .map(PokemonName::create)
                    .forEach(pokemonName -> {
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
