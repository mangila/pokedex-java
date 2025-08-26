package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE;

public final class DefaultWalManager implements WalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalFileHandler walFileHandler;
    private final FlushDelegateSubscriber flushDelegateSubscriber;
    private final EntryPublisher entryPublisher;
    private final FlushThread flushThread;

    public DefaultWalManager(WalConfig config) {
        Queue queue = QueueService.getInstance()
                .getQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        this.walFileHandler = new WalFileHandler(new WalWriteBuffer());
        this.entryPublisher = new EntryPublisher();
        this.flushThread = new FlushThread(queue, walFileHandler);
        this.flushDelegateSubscriber = new FlushDelegateSubscriber(queue);
    }

    @Override
    public void open() {
        LOGGER.info("Opening WAL manager");
        walFileHandler.replay();
        entryPublisher.subscribe(flushDelegateSubscriber);
        flushThread.schedule();
    }

    @Override
    public void close() {
        LOGGER.info("Closing WAL manager");
        entryPublisher.close();
        flushDelegateSubscriber.onComplete();
        flushThread.shutdown();
    }

    @Override
    public CompletableFuture<Void> putAsync(Entry entry) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CallbackItem<Entry> callbackItem = new CallbackItem<>(entry, future);
        entryPublisher.submit(callbackItem);
        return future;
    }
}
