package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.EntryCollection;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class FlushDelegateSubscriber implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushDelegateSubscriber.class);

    private final ReentrantLock lock = new ReentrantLock(true);
    private final List<Entry> entries;
    private final Queue queue;
    private final FlushDelegateThread flushDelegateThread;

    public FlushDelegateSubscriber(Queue queue) {
        this.entries = new CopyOnWriteArrayList<>();
        this.queue = queue;
        this.flushDelegateThread = new FlushDelegateThread();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        flushDelegateThread.schedule();
    }

    private boolean isLimitThresholdReached() {
        return entries.size() >= 50;
    }

    private boolean isBigWriteThresholdReached(Entry entry) {
        return entry.bufferSize() > 1024;
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        Entry entry = item.value();
        if (isBigWriteThresholdReached(entry)) {
            LOGGER.info("Snapshot for flushing THRESHOLD_BIG_WRITE {}", entry);
            queue.add(new QueueEntry(new FlushOperation(
                    FlushOperation.Reason.THRESHOLD_BIG_WRITE,
                    new EntryCollection(List.of(entry))
            )));
            item.callback().complete(null);
        } else {
            if (isLimitThresholdReached()) {
                try {
                    lock.lock();
                    List<Entry> snapshot = List.copyOf(entries);
                    if (!snapshot.isEmpty()) {
                        LOGGER.info("Snapshot for flushing THRESHOLD_LIMIT {}", snapshot.size());
                        queue.add(new QueueEntry(new FlushOperation(
                                FlushOperation.Reason.THRESHOLD_LIMIT,
                                new EntryCollection(snapshot))
                        ));
                    }
                    entries.clear();
                } finally {
                    lock.unlock();
                }
            }
            entries.add(entry);
            item.callback().complete(null);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        entries.clear();
        flushDelegateThread.shutdown();
    }

    private class FlushDelegateThread implements Runnable {

        private final ScheduledExecutorService flushDelegateExecutor;

        FlushDelegateThread() {
            this.flushDelegateExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        }

        void schedule() {
            flushDelegateExecutor.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
        }

        void shutdown() {
            VirtualThreadFactory.terminateGracefully(flushDelegateExecutor, Duration.ofSeconds(30));
        }

        @Override
        public void run() {
            if (!entries.isEmpty()) {
                try {
                    lock.lock();
                    List<Entry> snapshot = List.copyOf(entries);
                    if (!snapshot.isEmpty()) {
                        LOGGER.info("Snapshot for flushing THRESHOLD_SCHEDULED {}", snapshot.size());
                        queue.add(new QueueEntry(new FlushOperation(
                                FlushOperation.Reason.THRESHOLD_SCHEDULED,
                                new EntryCollection(snapshot))
                        ));
                        entries.removeAll(snapshot);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
