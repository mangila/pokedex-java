package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE;

public final class DefaultWalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalTableHandler walTableHandler;
    private final WalTableDelegateFlush walTableDelegateFlush;
    private final CallbackItemPublisher callbackItemPublisher;
    private final FinishedFlushingPublisher finishedFlushingPublisher;
    private final ScheduledExecutorService flushThreadExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    private final FlushThread flushThread;

    public DefaultWalManager() {
        Queue queue = QueueService.getInstance()
                .getQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        WalTable walTable = new WalTable(new ConcurrentHashMap<>());
        this.callbackItemPublisher = new CallbackItemPublisher();
        this.finishedFlushingPublisher = new FinishedFlushingPublisher();
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
        LOGGER.info("Closing WAL manager");
        callbackItemPublisher.close();
        finishedFlushingPublisher.close();
        walTableDelegateFlush.onComplete();
        VirtualThreadFactory.terminateGracefully(flushThreadExecutor, Duration.ofSeconds(30));
    }

    public CompletableFuture<Void> putAsync(Key key, Field field, Value value) {
        return walTableHandler.putAsync(key, field, value);
    }
}
