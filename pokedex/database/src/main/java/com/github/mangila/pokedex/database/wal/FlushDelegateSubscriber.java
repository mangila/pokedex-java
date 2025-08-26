package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.EntryCollection;
import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FlushDelegateSubscriber implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushDelegateSubscriber.class);
    private final Queue queue;
    private final LinkedBlockingQueue<CallbackItem<Entry>> entries;
    private final FlushLimitThresholdThread flushLimitThresholdThread;

    public FlushDelegateSubscriber(Queue queue) {
        this.queue = queue;
        this.entries = new LinkedBlockingQueue<>();
        this.flushLimitThresholdThread = new FlushLimitThresholdThread();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        flushLimitThresholdThread.schedule();
    }

    private boolean isBigWriteThresholdReached(Entry entry) {
        return entry.bufferLength() > 1024;
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        Entry entry = item.value();
        if (isBigWriteThresholdReached(entry)) {
            LOGGER.info("Snapshot for flushing THRESHOLD_BIG_WRITE {}", entry);
            queue.add(new QueueEntry(new FlushOperation(
                    FlushOperation.Reason.THRESHOLD_BIG_WRITE,
                    new EntryCollection(List.of(item))
            )));
        } else {
            entries.add(item);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("ERR", throwable);
    }

    @Override
    public void onComplete() {
        entries.clear();
        flushLimitThresholdThread.shutdown();
    }

    private final class FlushLimitThresholdThread implements SimpleBackgroundThread {

        private final ScheduledExecutorService executorService;

        FlushLimitThresholdThread() {
            this.executorService = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        }

        @Override
        public void schedule() {
            executorService.scheduleWithFixedDelay(this, 0, 1, TimeUnit.SECONDS);
        }

        @Override
        public void shutdown() {
            VirtualThreadFactory.terminateGracefully(executorService, Duration.ofSeconds(30));
        }

        private final List<CallbackItem<Entry>> limits = new ArrayList<>();

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    CallbackItem<Entry> entry = entries.poll(10, TimeUnit.SECONDS);
                    if (entry != null) {
                        limits.add(entry);
                        if (limits.size() >= 50) {
                            addToFlushQueue();
                            limits.clear();
                        }
                    } else {
                        if (!limits.isEmpty()) {
                            addToFlushQueue();
                            limits.clear();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("FlushLimitThresholdThread shutdown");
            }
        }

        private void addToFlushQueue() {
            LOGGER.info("Snapshot for flushing THRESHOLD_LIMIT {}", limits.size());
            queue.add(new QueueEntry(new FlushOperation(
                    FlushOperation.Reason.THRESHOLD_LIMIT,
                    new EntryCollection(limits))
            ));
        }
    }
}
