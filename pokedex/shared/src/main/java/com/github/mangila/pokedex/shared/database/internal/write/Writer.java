package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.internal.PokemonFile;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.*;

public class Writer {

    private final TransferQueue<WriteTransfer> writeTransfers;
    private final Semaphore writePermits;

    public Writer(PokemonFile pokemonFile) {
        this.writeTransfers = new LinkedTransferQueue<>();
        this.writePermits = new Semaphore(50);
        var writerThread = new WriterThread(pokemonFile, writeTransfers, writePermits);
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .schedule(writerThread, 1, TimeUnit.SECONDS);
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to WriterThread and return result
     * </summary>
     */
    public CompletableFuture<Long> newRecord(String key, Pokemon pokemon) {
        try {
            writePermits.acquire();
            var writeTransfer = new WriteTransfer(key, pokemon, new CompletableFuture<>());
            writeTransfers.transfer(writeTransfer);
            return writeTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writePermits.release();
            return CompletableFuture.failedFuture(e);
        }
    }
}
