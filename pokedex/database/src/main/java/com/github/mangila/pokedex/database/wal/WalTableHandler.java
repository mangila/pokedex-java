package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

class WalTableHandler implements Flow.Subscriber<EntryCollection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalTableHandler.class);
    private final WalTable walTable;
    private final EntryPublisher entryPublisher;

    WalTableHandler(WalTable walTable,
                    EntryPublisher entryPublisher) {
        this.walTable = walTable;
        this.entryPublisher = entryPublisher;
    }

    CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        walTable.put(key, field, value);
        return submitEntry(new Entry(key, field, value));
    }

    private CompletableFuture<Void> submitEntry(Entry entry) {
        CallbackItem<Entry> callbackItem = new CallbackItem<>(entry, new CompletableFuture<>());
        entryPublisher.submit(callbackItem);
        return callbackItem.callback();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(EntryCollection item) {
        walTable.remove(item);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        walTable.clear();
    }
}
