package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class WalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalManager.class);
    private final WalTableHandler walTableHandler;
    private final InsertBufferSubscriber insertBufferSubscriber;
    private final WalTableAfterFlushCleanUpSubscriber walTableAfterFlushCleanUpSubscriber;
    private final CallbackItemPublisher callbackItemPublisher;

    public WalManager() {
        WalTable walTable = new WalTable(new ConcurrentHashMap<>());
        this.callbackItemPublisher = new CallbackItemPublisher();
        this.insertBufferSubscriber = new InsertBufferSubscriber(Buffer.from(1204 * 1024));
        this.walTableAfterFlushCleanUpSubscriber = new WalTableAfterFlushCleanUpSubscriber(walTable);
        this.walTableHandler = new WalTableHandler(walTable, callbackItemPublisher);
    }

    public void subscribe() {
        callbackItemPublisher.subscribe(insertBufferSubscriber);
    }

    public void unSubscribe() {
        callbackItemPublisher.close();
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        return walTableHandler.putAsync(key, field, value);
    }

}
