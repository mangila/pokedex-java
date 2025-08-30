package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.WriteCallbackItem;
import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

class WriteThread implements SimpleBackgroundThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteThread.class);
    private final int writeLimitThreshold;
    private final WalFileHandle walFileHandle;
    private final BlockingQueue queue;
    private final ScheduledExecutorService executor;

    WriteThread(
            int writeLimitThreshold,
            WalFileHandle walFileHandle,
            BlockingQueue queue) {
        this.writeLimitThreshold = writeLimitThreshold;
        this.walFileHandle = walFileHandle;
        this.queue = queue;
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    }

    @Override
    public void schedule() {
        LOGGER.info("Starting write thread");
        executor.submit(this);
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down write thread");
        VirtualThreadFactory.terminateGracefully(executor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        int writeCount = 0;
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("Write thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
            WriteCallbackItem item = queueEntry.unwrapAs(WriteCallbackItem.class);
            try {
                // TODO: buffer check, rotation
                walFileHandle.walTable()
                        .writeOps()
                        .put(item.entry());
                writeCount = writeCount + 1;
                item.callback().future().complete(null);
                if (writeCount == writeLimitThreshold) {
                    LOGGER.info("Limit reached, flushing");
                    walFileHandle.walTable()
                            .mappedBuffer
                            .sync();
                    writeCount = 0;
                }
            } catch (Exception e) {
                LOGGER.error("err", e);
            }
//            try {
//                writeLock.lock();
//                if (walFileHandle.putIfHasRemaining(item.entry())) {
//                    item.callback().future().complete(null);
//                    if (LIMIT_THRESHOLD.incrementAndGet() == 50) {
//                        LOGGER.info("Limit reached, flushing");
//                        syncThread.submit(() -> walFile.sync());
//                        LIMIT_THRESHOLD.set(0);
//                    }
//                } else {
//                    walFile.sync();
//                    String name = walFile.path().getFileName().toString();
//                    walFile.mark();
//                    walFile.close();
//                    walFile = new WalFile(Path.of("active-".concat(name)), walFile.totalSize());
//                    walFile.open();
//                    walFile.put(item.entry());
//                    item.callback().future().complete(null);
//                }
//            } catch (Exception e) {
//                LOGGER.error("Failed to insert to {}", walFile.path(), e);
//                item.callback().future().completeExceptionally(e);
//            } finally {
//                writeLock.unlock();
//            }
        }
    }
}
