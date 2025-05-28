package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.*;

public class Writer {

    private final TransferQueue<WriteTransfer> writeQueue = new LinkedTransferQueue<>();
    private final ExecutorService writerThread = VirtualThreadConfig.newSingleThreadExecutor();
    private final PokemonFile pokemonFile;

    private record WriteTransfer(String key, Pokemon pokemon, CompletableFuture<Long> result) {
    }

    public Writer(PokemonFile pokemonFile) {
        this.pokemonFile = pokemonFile;
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .schedule(this::write, 1, TimeUnit.SECONDS);
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * The producer will wait for result
     * </summary>
     */
    public CompletableFuture<Long> newRecord(String key, Pokemon pokemon) {
        var writeTransfer = new WriteTransfer(key, pokemon, new CompletableFuture<>());
        writeQueue.tryTransfer(writeTransfer);
        return writeTransfer.result;
    }

    /**
     * <summary>
     * Dedicated Writer Thread
     * </summary>
     */
    private void write() {
        writerThread.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var poll = writeQueue.take();
                    var result = pokemonFile.write(poll.key, poll.pokemon);
                    poll.result.complete(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
