package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SubmissionPublisher;

public final class WalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalManager.class);
    private final WalTableHandler walTableHandler;
    private final InsertBufferSubscriber insertBufferSubscriber;
    private final WalTableAfterFlushCleanUpSubscriber walTableAfterFlushCleanUpSubscriber;
    private final SubmissionPublisher<CallbackItem<Entry>> callbackItemPublisher;
    private final SubmissionPublisher<List<Entry>> finishedFlushingPublisher;
    private final FlushThread flushThread;

    public WalManager() {
        Queue queue = QueueService.getInstance().createNewQueue(new QueueName("WAL_FLUSH_BUFFER"));
        WalTable walTable = new WalTable(new ConcurrentHashMap<>());
        this.callbackItemPublisher = new SubmissionPublisher<>();
        this.finishedFlushingPublisher = new SubmissionPublisher<>();
        this.flushThread = new FlushThread(queue, finishedFlushingPublisher);
        this.insertBufferSubscriber = new InsertBufferSubscriber(queue);
        this.walTableAfterFlushCleanUpSubscriber = new WalTableAfterFlushCleanUpSubscriber(walTable);
        this.walTableHandler = new WalTableHandler(walTable, callbackItemPublisher);
    }

    public void subscribe() {
        callbackItemPublisher.subscribe(insertBufferSubscriber);
        finishedFlushingPublisher.subscribe(walTableAfterFlushCleanUpSubscriber);
    }

    public void unSubscribe() {
        callbackItemPublisher.close();
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        return walTableHandler.putAsync(key, field, value);
    }

}
