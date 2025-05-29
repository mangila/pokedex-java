package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.database.internal.PokemonFile;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * <summary>
 * Dedicated Writer Thread
 * </summary>
 */
public record WriterThread(
        PokemonFile pokemonFile,
        TransferQueue<WriteTransfer> writeTransfers,
        Semaphore writePermits
) implements Runnable {

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var transfer = writeTransfers.take();
                var result = pokemonFile.write(transfer.key(), transfer.pokemon());
                transfer.result().complete(result);
                writePermits.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
