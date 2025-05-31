package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * <summary>
 * Dedicated Reader Thread
 * </summary>
 */
public record ReaderThread(FileHandler handler,
                           TransferQueue<ReadTransfer> readTransfers,
                           Semaphore readPermits) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReaderThread.class);

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var transfer = readTransfers.take();
                var pokemon = handler.read(transfer.key());
                transfer.result().complete(pokemon);
                readPermits.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("ERR", e);
            }
        }
    }
}
