package com.github.mangila.pokedex.database.internal.read;

import com.github.mangila.pokedex.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <summary>
 * Dedicated Reader Thread
 * </summary>
 */
public record ReaderThread(FileHandler handler,
                           TransferQueue<ReadTransfer> readTransfers,
                           Semaphore readPermits,
                           AtomicBoolean shutdown) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReaderThread.class);

    @Override
    public void run() {
        while (!shutdown.get()) {
            try {
                ReadTransfer transfer = readTransfers.poll(1, TimeUnit.SECONDS);
                if (transfer == null) {
                    continue;
                }
                byte[] value = handler.read(transfer.key());
                transfer.result().complete(value);
                readPermits.release();
            } catch (InterruptedException e) {
                log.error("ERR", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
