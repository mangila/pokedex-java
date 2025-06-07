package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Reader {

    private static final Logger log = LoggerFactory.getLogger(Reader.class);

    private final DatabaseConfig.ReaderThreadConfig config;
    private final Semaphore readPermits;
    private final TransferQueue<ReadTransfer> readTransfers;
    private final ReaderThread readerThread;
    private final List<ScheduledExecutorService> executors;

    public Reader(DatabaseConfig.ReaderThreadConfig readerThreadConfig, FileHandler handler) {
        this.config = readerThreadConfig;
        this.readPermits = new Semaphore(config.permits(), Boolean.TRUE);
        this.readTransfers = new LinkedTransferQueue<>();
        this.readerThread = new ReaderThread(handler, readTransfers, readPermits);
        this.executors = new ArrayList<>(config.nThreads());
        for (int i = 0; i < config.nThreads(); i++) {
            this.executors.add(VirtualThreadConfig.newSingleThreadScheduledExecutor());
        }
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to ReaderThread and return result
     * </summary>
     */
    public CompletableFuture<byte[]> get(String key) {
        try {
            readPermits.acquire();
            var readTransfer = new ReadTransfer(key, new CompletableFuture<>());
            readTransfers.transfer(readTransfer);
            return readTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        for (var executor : executors) {
            log.info("Starting executor thread -- {}", executor.toString());
            executor.scheduleAtFixedRate(readerThread, 1, 1, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        for (var executor : executors) {
            log.info("Shutting down executor thread -- {}", executor.toString());
            executor.shutdown();
        }
    }
}
