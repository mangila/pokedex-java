package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Reader<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(Reader.class);

    private final Semaphore readPermits;
    private final TransferQueue<ReadTransfer<V>> readTransfers;
    private final ReaderThread<V> readerThread;
    private final ScheduledExecutorService executor;

    public Reader(FileHandler<V> handler) {
        this.readPermits = new Semaphore(100, Boolean.TRUE);
        this.readTransfers = new LinkedTransferQueue<>();
        this.readerThread = new ReaderThread<V>(handler, readTransfers, readPermits);
        this.executor = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to ReaderThread and return result
     * </summary>
     */
    public CompletableFuture<V> get(String key) {
        try {
            readPermits.acquire();
            var readTransfer = new ReadTransfer<V>(key, new CompletableFuture<>());
            readTransfers.transfer(readTransfer);
            return readTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        log.info("Starting reader thread");
        executor.schedule(readerThread, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        log.info("Shutting down reader thread");
        executor.shutdown();
    }
}
