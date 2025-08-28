package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.CallbackItemCollection;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

class FlushThread implements SimpleBackgroundThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);

    private final ScheduledExecutorService executor;
    private final long thresholdLimit;
    private final BlockingQueue queue;
    private final WalFileHandler walFileHandler;
    private final FlushWriteBuffer flushWriteBuffer;
    private final List<CallbackItem<Entry>> items;
    private final ReentrantLock writeLock;

    FlushThread(long thresholdLimit,
                BlockingQueue queue,
                WalFileHandler walFileHandler,
                ReentrantLock writeLock) {
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.thresholdLimit = thresholdLimit;
        this.queue = queue;
        this.walFileHandler = walFileHandler;
        this.flushWriteBuffer = new FlushWriteBuffer();
        this.items = new ArrayList<>();
        this.writeLock = writeLock;
    }

    @Override
    public void schedule() {
        executor.submit(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("Flush thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
            try {
                CallbackItem<Entry> entry = queueEntry.unwrapAs(CallbackItem.class);
                items.add(entry);
                if (items.size() >= thresholdLimit) {
                    flush();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to flush {}", walFileHandler.path(), e);
                if (queueEntry.equalsMaxRetries(3)) {
                    queue.addDlq(queueEntry);
                } else {
                    queueEntry.incrementFailCounter();
                    queue.add(queueEntry);
                }
            }
        }
    }

    List<CallbackItem<Entry>> items() {
        return items;
    }

    @Override
    public void shutdown() {
        VirtualThreadFactory.terminateGracefully(executor);
    }

    private void flush() throws IOException {
        CallbackItemCollection collection = new CallbackItemCollection(items);
        try {
            writeLock.lock();
            LOGGER.info("Flushing {} entries to {}", items.size(), walFileHandler.path());
            walFileHandler.flush(flushWriteBuffer, collection);
        } finally {
            writeLock.unlock();
        }
        items.clear();
        collection.complete();
    }
}
