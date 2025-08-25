package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

public final class DefaultWalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalTableHandler walTableHandler;
    private final WalTableDelegateFlush walTableDelegateFlush;
    private final CallbackItemPublisher callbackItemPublisher;
    private final SubmissionPublisher<List<Entry>> finishedFlushingPublisher;
    private final ScheduledExecutorService flushThreadExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    private final FlushThread flushThread;

    public DefaultWalManager() {
        Queue queue = QueueService.getInstance()
                .createNewQueue(new QueueName("WAL_FLUSH_BUFFER"));
        WalTable walTable = new WalTable(new ConcurrentHashMap<>());
        this.callbackItemPublisher = new CallbackItemPublisher();
        this.finishedFlushingPublisher = new SubmissionPublisher<>();
        this.flushThread = new FlushThread(queue, finishedFlushingPublisher);
        this.walTableDelegateFlush = new WalTableDelegateFlush(queue);
        this.walTableHandler = new WalTableHandler(walTable, callbackItemPublisher);
    }

    public void open() {
        callbackItemPublisher.subscribe(walTableDelegateFlush);
        flushThreadExecutor.scheduleWithFixedDelay(
                flushThread,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    public void close() {
        callbackItemPublisher.close();
        finishedFlushingPublisher.close();
        VirtualThreadFactory.terminateGracefully(flushThreadExecutor, Duration.ofSeconds(30));
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        return walTableHandler.putAsync(key, field, value);
    }
}
