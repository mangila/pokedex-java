package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

class WalTableHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalTableHandler.class);
    private final WalTable walTable;
    private final CallbackItemPublisher callbackItemPublisher;

    WalTableHandler(WalTable walTable,
                    CallbackItemPublisher callbackItemPublisher) {
        this.walTable = walTable;
        this.callbackItemPublisher = callbackItemPublisher;
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        walTable.put(key, field, value);
        return submitEntry(new Entry(key, field, value));
    }

    private CompletableFuture<Void> submitEntry(Entry entry) {
        LOGGER.debug("Submitting entry {}", entry);
        CallbackItem<Entry> callbackItem = new CallbackItem<>(entry, new CompletableFuture<>());
        callbackItemPublisher.submit(callbackItem);
        return callbackItem.callback();
    }
}
