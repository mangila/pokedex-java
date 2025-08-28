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
    private final long thresholdSize;
    private final WalFileHandler walFileHandler;
    private final ReentrantLock writeLock;

    RotateThread(long thresholdSize,
                 WalFileHandler walFileHandler,
                 ReentrantLock writeLock) {
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.thresholdSize = thresholdSize;
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
        try {
            if (walFileHandler.size() > thresholdSize) {
                try {
                    writeLock.lock();
                    Path newFileName = Path.of(System.nanoTime() + ".wal");
                    LOGGER.info("Rotating from: {} - to: {}", walFileHandler.path(), newFileName);
                    walFileHandler.rotate(newFileName);
                } finally {
                    writeLock.unlock();
                }
            }
        } catch (Exception e) {
            LOGGER.error("ERR", e);
        }
    }
}
