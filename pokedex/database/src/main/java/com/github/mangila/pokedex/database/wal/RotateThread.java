package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class RotateThread implements SimpleBackgroundThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(RotateThread.class);
    private final ScheduledExecutorService executor;
    private final WalFileHandler walFileHandler;
    private final ReentrantLock writeLock;

    RotateThread(WalFileHandler walFileHandler,
                 ReentrantLock writeLock) {
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.walFileHandler = walFileHandler;
        this.writeLock = writeLock;
    }

    @Override
    public void schedule() {
        executor.scheduleWithFixedDelay(
                this, 0, 5, TimeUnit.SECONDS
        );
    }

    @Override
    public void shutdown() {
        VirtualThreadFactory.terminateGracefully(executor, Duration.ofSeconds(30));
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (walFileHandler.size() > 150_000) {
                    try {
                        writeLock.lock();
                        walFileHandler.rotate(Path.of(System.nanoTime() + ".wal"));
                    } finally {
                        writeLock.unlock();
                    }
                }
            } catch (Exception e) {
                LOGGER.error("ERR", e);
            }
        }
    }
}
