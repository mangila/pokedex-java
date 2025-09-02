package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
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

    @Override
    public void run() {
        int writeLimit = 0;
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
            Entry entry = item.entry();
            walFileHandle.walTable().writeOps().put(entry);
            try {
                item.callback().future().complete(null);
                writeLimit += entry.bufferLength();
                if (writeLimit == writeLimitThreshold) {
                    LOGGER.info("Write limit reached, flushing");
                    walFileHandle.walTable()
                            .mappedBuffer
                            .sync();
                    writeLimit = 0;
                }
            } catch (Exception e) {
                LOGGER.error("Error when writing {}", entry, e);
            }
        }
    }
}
