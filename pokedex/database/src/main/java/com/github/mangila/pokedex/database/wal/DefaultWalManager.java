package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class DefaultWalManager implements WalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);

    private final EntryPublisher entryPublisher;
    private final WriteSubscriber writeSubscriber;
    private final WalFileHandle walFileHandle;
    private final List<WriteThread> writeThreads;
    private final ReentrantLock writeLock;

    public DefaultWalManager(WalConfig config) {
        this.entryPublisher = new EntryPublisher();
        this.writeSubscriber = new WriteSubscriber(
                QueueService.getInstance().getQueue(Config.DATABASE_WAL_WRITE_QUEUE),
                QueueService.getInstance().getQueue(Config.DATABASE_WAL_WRITE_BIG_OBJECT_QUEUE)
        );
        this.walFileHandle = new WalFileHandle();
        this.writeThreads = new ArrayList<>();
        this.writeLock = new ReentrantLock(true);
    }

    @Override
    public void open() {
        LOGGER.info("Opening WAL Manager");
        entryPublisher.subscribe(writeSubscriber);
    }

    @Override
    public void close() {
        LOGGER.info("Closing WAL Manager");
        entryPublisher.close();
        writeThreads.forEach(WriteThread::shutdown);
        flush();
    }

    @Override
    public void flush() {
        LOGGER.info("Flushing WAL Manager");
        walFileHandle.flush();
    }

    @Override
    public WriteCallback put(Entry entry) {
        return null;
    }

    @Override
    public Value get(Key key, Field field) {
        return null;
    }
}
