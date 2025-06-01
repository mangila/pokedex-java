package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * <summary>
 * Dedicated Reader Thread
 * </summary>
 */
public record ReaderThread<V extends DatabaseObject<V>>(FileHandler<V> handler,
                                                        TransferQueue<ReadTransfer> readTransfers,
                                                        Semaphore readPermits) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReaderThread.class);

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var transfer = readTransfers.take();
                var value = handler.read(transfer.key());
                transfer.result().complete(value);
                readPermits.release();
            } catch (InterruptedException e) {
                log.error("ERR", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
