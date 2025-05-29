package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.database.internal.PokemonFile;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * <summary>
 * Dedicated Reader Thread
 * </summary>
 */
public record ReaderThread(PokemonFile pokemonFile,
                           TransferQueue<ReadTransfer> readTransfers,
                           Semaphore readPermits) implements Runnable {

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var transfer = readTransfers.take();
                var pokemon = pokemonFile.read(transfer.key());
                transfer.result().complete(pokemon);
                readPermits.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
