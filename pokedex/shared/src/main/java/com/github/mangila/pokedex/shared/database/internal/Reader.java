package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.*;

public class Reader {

    private final TransferQueue<ReadTransfer> readTransfers = new LinkedTransferQueue<>();
    private final ExecutorService readerThread = VirtualThreadConfig.newSingleThreadExecutor();
    private final PokemonFile pokemonFile;

    private record ReadTransfer(String key, CompletableFuture<Pokemon> result) {
    }

    public Reader(PokemonFile pokemonFile) {
        this.pokemonFile = pokemonFile;
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .schedule(this::read, 1, TimeUnit.SECONDS);
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * The producer will wait for result
     * </summary>
     */
    public CompletableFuture<Pokemon> get(String key) {
        var transfer = new ReadTransfer(key, new CompletableFuture<>());
        readTransfers.tryTransfer(transfer);
        return transfer.result;
    }

    /**
     * <summary>
     * Dedicated Reader Thread
     * </summary>
     */
    private void read() {
        readerThread.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var transfer = readTransfers.take();
                    var pokemon = pokemonFile.read(transfer.key);
                    transfer.result.complete(pokemon);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
