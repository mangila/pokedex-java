package com.github.mangila.pokedex.database.internal.read;

import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

    private final AtomicBoolean shutdown;
    private final Semaphore readPermits;
    private final TransferQueue<ReadTransfer> readTransfers;
    private final ReaderThread readerThread;
    private final List<ScheduledExecutorService> executors;

    public Reader(DatabaseConfig.ReaderThreadConfig readerThreadConfig, FileHandler handler) {
        this.shutdown = new AtomicBoolean(false);
        this.readPermits = new Semaphore(readerThreadConfig.permits(), Boolean.TRUE);
        this.readTransfers = new LinkedTransferQueue<>();
        this.readerThread = new ReaderThread(handler, readTransfers, readPermits, shutdown);
        this.executors = new ArrayList<>(readerThreadConfig.nThreads());
        for (int i = 0; i < readerThreadConfig.nThreads(); i++) {
            this.executors.add(VirtualThreadFactory.newSingleThreadScheduledExecutor());
        }
    }

    public CompletableFuture<byte[]> get(String key) {
        try {
            readPermits.acquire();
            ReadTransfer readTransfer = new ReadTransfer(key, new CompletableFuture<>());
            readTransfers.transfer(readTransfer);
            return readTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        for (var executor : executors) {
            LOGGER.info("Starting Reader thread -- {}", executor.toString());
            executor.scheduleAtFixedRate(readerThread, 1, 1, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        shutdown.set(true);
        for (var executor : executors) {
            LOGGER.info("Shutting down Reader thread -- {}", executor.toString());
            VirtualThreadFactory.terminateExecutorGracefully(executor, Duration.ofMinutes(1));
        }
    }
}
