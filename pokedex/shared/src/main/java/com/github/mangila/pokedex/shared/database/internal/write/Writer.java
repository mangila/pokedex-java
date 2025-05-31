package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Writer {

    private static final Logger log = LoggerFactory.getLogger(Writer.class);

    private final TransferQueue<WriteTransfer> writeTransfers;
    private final Semaphore writePermits;
    private final ScheduledExecutorService executor;

    public Writer(FileHandler handler) {
        this.writeTransfers = new LinkedTransferQueue<>();
        this.writePermits = new Semaphore(50, Boolean.TRUE);
        var writerThread = new WriterThread(handler, writeTransfers, writePermits);
        this.executor = VirtualThreadConfig.newSingleThreadScheduledExecutor();
        executor.schedule(writerThread, 1, TimeUnit.SECONDS);
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

    public void shutdown() {
        log.info("Shutting down writer thread");
        executor.shutdown();
    }
}
