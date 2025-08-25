package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

class WalTableFlusher implements Flow.Subscriber<CallbackItem<Entry>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalTableFlusher.class);
    private final List<Entry> entries;
    private final Queue queue;

    public WalTableFlusher(Queue queue) {
        this.entries = new CopyOnWriteArrayList<>();
        this.queue = queue;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(CallbackItem<Entry> item) {
        if (entries.size() == 10) {
            List<Entry> snapshot = List.copyOf(entries);
            LOGGER.info("Snapshot for flushing {}", snapshot);
            queue.add(new QueueEntry(new FlushOperation(
                    FlushOperation.Reason.THRESHOLD_LIMIT,
                    snapshot)
            ));
            entries.removeAll(snapshot);
        }
        entries.add(item.value());
        item.callback().complete(null);
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
