package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.database.internal.file.PokemonFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * <summary>
 * Dedicated Writer Thread
 * </summary>
 */
public record WriterThread(
        PokemonFileHandler handler,
        TransferQueue<WriteTransfer> writeTransfers,
        Semaphore writePermits
) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WriterThread.class);

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var transfer = writeTransfers.take();
                var result = handler.write(transfer.key(), transfer.pokemon());
                transfer.result().complete(result);
                writePermits.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("ERR", e);
            }
        }
    }
}
