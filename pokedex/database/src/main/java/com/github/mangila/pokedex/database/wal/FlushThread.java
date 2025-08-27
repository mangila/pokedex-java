package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.CallbackItemCollection;
import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class FlushThread implements SimpleBackgroundThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushThread.class);
    private final Queue queue;
    private final ScheduledExecutorService executor;
    private final WalFileHandler walFileHandler;
    private final FlushWriteBuffer flushWriteBuffer;
    private final List<CallbackItem<Entry>> list;
    private final ReentrantLock writeLock;

    FlushThread(Queue queue,
                WalFileHandler walFileHandler,
                ReentrantLock writeLock) {
        this.queue = queue;
        this.walFileHandler = walFileHandler;
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.flushWriteBuffer = new FlushWriteBuffer();
        this.list = new ArrayList<>();
        this.writeLock = writeLock;
    }

    @Override
    public void schedule() {
        executor.scheduleWithFixedDelay(
                this,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry = queue.poll();
            try {
                if (queueEntry != null) {
                    CallbackItem<Entry> entry = queueEntry.unwrapAs(CallbackItem.class);
                    list.add(entry);
                    if (list.size() >= 50) {
                        flush();
                    }
                } else {
                    if (!list.isEmpty()) {
                        flush();
                    }
                }
            } catch (IOException e) {
                LOGGER.error("ERR", e);
            }
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("FlushThread shutdown");
        executor.shutdown();
    }

    private void flush() throws IOException {
        CallbackItemCollection collection = new CallbackItemCollection(list);
        try {
            writeLock.lock();
            walFileHandler.flush(flushWriteBuffer, collection);
        } finally {
            writeLock.unlock();
        }
        list.clear();
        collection.complete();
    }
}
