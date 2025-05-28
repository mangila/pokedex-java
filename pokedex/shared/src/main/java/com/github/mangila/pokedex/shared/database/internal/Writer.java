package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.*;

public class Writer {

    private final TransferQueue<WriteTransfer> writeTransfers = new LinkedTransferQueue<>();
    private final Semaphore writePermits = new Semaphore(3000);
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
        try {
            writePermits.acquire();
            var writeTransfer = new WriteTransfer(key, pokemon, new CompletableFuture<>());
            writeTransfers.transfer(writeTransfer);
            return writeTransfer.result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writePermits.release();
            return CompletableFuture.failedFuture(e);
        }
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
                    var transfer = writeTransfers.take();
                    var result = pokemonFile.write(transfer.key, transfer.pokemon);
                    transfer.result.complete(result);
                    writePermits.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
