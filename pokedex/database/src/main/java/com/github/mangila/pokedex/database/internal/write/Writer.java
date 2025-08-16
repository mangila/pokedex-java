package com.github.mangila.pokedex.database.internal.write;

import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Writer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Writer.class);

    private final AtomicBoolean shutdown;
    private final TransferQueue<WriteTransfer> writeTransfers;
    private final Semaphore writePermits;
    private final WriterThread writerThread;
    private final ScheduledExecutorService executor;

    public Writer(DatabaseConfig.WriteThreadConfig writeThreadConfig, FileHandler handler) {
        this.shutdown = new AtomicBoolean(false);
        this.writeTransfers = new LinkedTransferQueue<>();
        this.writePermits = new Semaphore(writeThreadConfig.permits(), Boolean.TRUE);
        this.writerThread = new WriterThread(handler, writeTransfers, writePermits, shutdown);
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    }

    public CompletableFuture<Boolean> put(String key, byte[] value) {
        try {
            writePermits.acquire();
            WriteTransfer writeTransfer = new WriteTransfer(key, value, new CompletableFuture<>());
            writeTransfers.transfer(writeTransfer);
            return writeTransfer.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        LOGGER.info("Starting writer thread");
        executor.schedule(writerThread, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        LOGGER.info("Shutting down writer thread");
        shutdown.set(true);
        VirtualThreadFactory.terminateExecutorGracefully(executor, Duration.ofMinutes(1));
    }
}
