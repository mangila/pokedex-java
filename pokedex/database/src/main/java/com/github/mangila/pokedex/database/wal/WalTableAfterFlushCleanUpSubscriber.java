package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Flow;

class WalTableAfterFlushCleanUpSubscriber implements Flow.Subscriber<List<Entry>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalTableAfterFlushCleanUpSubscriber.class);
    private final WalTable walTable;
    private Flow.Subscription subscription;

    WalTableAfterFlushCleanUpSubscriber(WalTable walTable) {
        this.walTable = walTable;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(List<Entry> item) {
        walTable.remove(item);
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("Error in WalTableAfterFlushCleanUpSubscriber", throwable);
        walTable.clear();
    }

    @Override
    public void onComplete() {
        walTable.clear();
    }
}
