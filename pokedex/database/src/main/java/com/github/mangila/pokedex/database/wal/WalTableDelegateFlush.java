package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class WalTableDelegateFlush implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalTableDelegateFlush.class);

    private final List<Entry> entries;
    private final Queue queue;
    private final ScheduledExecutorService flushDelegateExecutor;

    public WalTableDelegateFlush(Queue queue) {
        this.entries = new CopyOnWriteArrayList<>();
        this.queue = queue;
        this.flushDelegateExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        flushDelegateExecutor.scheduleWithFixedDelay(
                () -> {
                    if (!entries.isEmpty()) {
                        List<Entry> snapshot = List.copyOf(entries);
                        LOGGER.info("Snapshot for flushing THRESHOLD_SCHEDULED {}", snapshot);
                        queue.add(new QueueEntry(new FlushOperation(
                                FlushOperation.Reason.THRESHOLD_SCHEDULED,
                                snapshot)
                        ));
                        entries.removeAll(snapshot);
                    }
                }, 1, 1, TimeUnit.MINUTES
        );
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        Entry entry = item.value();
        if (entry.bufferSize() > 1024) {
            LOGGER.info("Snapshot for flushing THRESHOLD_BIG_WRITE {}", entry);
            queue.add(new QueueEntry(new FlushOperation(
                    FlushOperation.Reason.THRESHOLD_BIG_WRITE,
                    List.of(entry)
            )));
            item.callback().complete(null);
            return;
        }
        synchronized (entries) {
            if (entries.size() >= 50) {
                List<Entry> snapshot = List.copyOf(entries);
                LOGGER.info("Snapshot for flushing THRESHOLD_LIMIT {}", snapshot);
                queue.add(new QueueEntry(new FlushOperation(
                        FlushOperation.Reason.THRESHOLD_LIMIT,
                        snapshot)
                ));
                entries.clear();
            }
            entries.add(entry);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        entries.clear();
    }
}
