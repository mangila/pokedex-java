package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Writer<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(Writer.class);

    private final TransferQueue<WriteTransfer<V>> writeTransfers;
    private final Semaphore writePermits;
    private final WriterThread<V> writerThread;
    private final ScheduledExecutorService executor;

    public Writer(FileHandler<V> handler) {
        this.writeTransfers = new LinkedTransferQueue<>();
        this.writePermits = new Semaphore(50, Boolean.TRUE);
        this.writerThread = new WriterThread<V>(handler, writeTransfers, writePermits);
        this.executor = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to WriterThread and return result
     * </summary>
     */
    public CompletableFuture<Integer> put(String key, V value) {
        try {
            writePermits.acquire();
            var writeTransfer = new WriteTransfer<>(key, value, new CompletableFuture<>());
            writeTransfers.transfer(writeTransfer);
            return writeTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        log.info("Starting writer thread");
        executor.schedule(writerThread, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        log.info("Shutting down writer thread");
        executor.shutdown();
    }
}
