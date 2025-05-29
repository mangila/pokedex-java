package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.internal.PokemonFile;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.*;

public class Reader {

    private final TransferQueue<ReadTransfer> readTransfers;
    private final Semaphore readPermits;

    public Reader(PokemonFile pokemonFile) {
        this.readPermits = new Semaphore(100);
        this.readTransfers = new LinkedTransferQueue<>();
        var readerThread = new ReaderThread(pokemonFile, readTransfers, readPermits);
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .schedule(readerThread, 1, TimeUnit.SECONDS);
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to ReaderThread and return result
     * </summary>
     */
    public CompletableFuture<Pokemon> get(String key) {
        try {
            readPermits.acquire();
            var readTransfer = new ReadTransfer(key, new CompletableFuture<>());
            readTransfers.transfer(readTransfer);
            return readTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            readPermits.release();
            return CompletableFuture.failedFuture(e);
        }
    }
}
