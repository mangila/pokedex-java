package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.util.VirtualThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Writer {

    private static final Logger log = LoggerFactory.getLogger(Writer.class);

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
        this.executor = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    }

    /**
     * <summary>
     * Fan-Out - Fan-In <br>
     * Transfer to WriterThread and return result
     * </summary>
     */
    public CompletableFuture<Boolean> put(String key, byte[] value) {
        try {
            writePermits.acquire();
            var writeTransfer = new WriteTransfer(key, value, new CompletableFuture<>());
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
        shutdown.set(true);
        VirtualThreadUtils.terminateExecutorGracefully(executor, Duration.ofMinutes(1));
    }
}
