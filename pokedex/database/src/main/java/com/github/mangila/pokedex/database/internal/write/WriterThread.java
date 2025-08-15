package com.github.mangila.pokedex.database.internal.write;

import com.github.mangila.pokedex.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <summary>
 * Dedicated Writer Thread
 * </summary>
 */
public record WriterThread(
        FileHandler handler,
        TransferQueue<WriteTransfer> writeTransfers,
        Semaphore writePermits,
        AtomicBoolean shutdown) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WriterThread.class);

    @Override
    public void run() {
        while (!shutdown.get()) {
            try {
                WriteTransfer transfer = writeTransfers.poll(1, TimeUnit.SECONDS);
                if (transfer == null) {
                    continue;
                }
                boolean result = handler.write(transfer.key(), transfer.value());
                transfer.result().complete(result);
                writePermits.release();
            } catch (InterruptedException e) {
                log.error("ERR", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
