package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.WriteCallback;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE;

public final class DefaultWalManager implements WalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalFileHandler walFileHandler;
    private final FlushDelegateSubscriber flushDelegateSubscriber;
    private final EntryPublisher entryPublisher;
    private final ReentrantLock writeLock;
    private final RotateThread rotateThread;
    private final List<FlushThread> flushThreads = new ArrayList<>();

    public DefaultWalManager(WalConfig config) {
        Queue queue = QueueService.getInstance()
                .getQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        this.walFileHandler = new WalFileHandler();
        this.entryPublisher = new EntryPublisher();
        this.writeLock = new ReentrantLock(true);
        this.rotateThread = new RotateThread(walFileHandler, writeLock);
        for (int i = 0; i < 3; i++) {
            flushThreads.add(new FlushThread(queue, walFileHandler, writeLock));
        }
        this.flushDelegateSubscriber = new FlushDelegateSubscriber(queue);
    }

    @Override
    public void open() {
        LOGGER.info("Opening WAL manager");
        walFileHandler.replay();
        entryPublisher.subscribe(flushDelegateSubscriber);
        rotateThread.schedule();
        flushThreads.forEach(FlushThread::schedule);
    }

    @Override
    public void close() {
        LOGGER.info("Closing WAL manager");
        entryPublisher.close();
        flushDelegateSubscriber.onComplete();
        rotateThread.shutdown();
        flushThreads.forEach(FlushThread::shutdown);
    }

    @Override
    public WriteCallback putAsync(Entry entry) {
        WriteCallback writeCallback = WriteCallback.newCallback();
        CallbackItem<Entry> callbackItem = new CallbackItem<>(entry, writeCallback);
        entryPublisher.submit(callbackItem);
        return writeCallback;
    }
}
