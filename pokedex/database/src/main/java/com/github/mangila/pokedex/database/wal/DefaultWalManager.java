package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE;

public final class DefaultWalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalTableHandler walTableHandler;
    private WalFileHandler walFileHandler;
    private final FlushDelegateSubscriber flushDelegateSubscriber;
    private final EntryPublisher entryPublisher;
    private final FlushFinishedPublisher flushFinishedPublisher;
    private final FlushThread flushThread;

    public DefaultWalManager() {
        Queue queue = QueueService.getInstance()
                .getQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        WalTable walTable = new WalTable(new ConcurrentHashMap<>());
        this.walFileHandler = new WalFileHandler(new WalBufferPool());
        this.entryPublisher = new EntryPublisher();
        this.flushFinishedPublisher = new FlushFinishedPublisher();
        this.flushThread = new FlushThread(queue, flushFinishedPublisher, walFileHandler);
        this.flushDelegateSubscriber = new FlushDelegateSubscriber(queue);
        this.walTableHandler = new WalTableHandler(walTable, entryPublisher);
    }

    public void open() {
        LOGGER.info("Opening WAL manager");
        walFileHandler.replay();
        entryPublisher.subscribe(flushDelegateSubscriber);
        flushFinishedPublisher.subscribe(walTableHandler);
        flushThread.schedule();
    }

    public void close() {
        LOGGER.info("Closing WAL manager");
        entryPublisher.close();
        flushFinishedPublisher.close();
        flushDelegateSubscriber.onComplete();
        walTableHandler.onComplete();
        flushThread.shutdown();
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        return walTableHandler.putAsync(key, field, value);
    }
}
