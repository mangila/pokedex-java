package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_WRITE_BIG_OBJECT_QUEUE;
import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_WRITE_QUEUE;

public final class DefaultWalManager implements WalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);

    private final WalConfig config;
    private final EntryPublisher entryPublisher;
    private final WriteSubscriber writeSubscriber;
    private final WalFileHandle walFileHandle;
    private final WriteThread writeThread;

    public DefaultWalManager(WalConfig config) {
        // TODO: snappy compression??
        this.config = config;
        BlockingQueue writeQueue = QueueService.getInstance().getBlockingQueue(DATABASE_WAL_WRITE_QUEUE);
        BlockingQueue bigObjectWriteQueue = QueueService.getInstance().getBlockingQueue(DATABASE_WAL_WRITE_BIG_OBJECT_QUEUE);
        this.entryPublisher = new EntryPublisher();
        this.writeSubscriber = new WriteSubscriber(writeQueue, bigObjectWriteQueue);
        this.walFileHandle = new WalFileHandle();
        this.writeThread = new WriteThread(config.thresholdWriteLimit(), walFileHandle, writeQueue);
    }

    @Override
    public void open() {
        LOGGER.info("Opening WAL Manager");
        try {
            // TEMP: do replay here
            walFileHandle.setWalFile(Path.of("hej.wal"), config.walFileSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        entryPublisher.subscribe(writeSubscriber);
        writeThread.schedule();
    }

    @Override
    public void close() {
        LOGGER.info("Closing WAL Manager");
        entryPublisher.close();
        writeThread.shutdown();
        flush();
    }

    @Override
    public void flush() {
        LOGGER.info("Flushing WAL Manager");
        walFileHandle.walTable()
                .mappedBuffer
                .sync();
    }

    @Override
    public WriteCallback put(Entry entry) {
        WriteCallback callback = WriteCallback.newCallback();
        entryPublisher.submit(new WriteCallbackItem(entry, callback));
        return callback;
    }

    @Override
    public Value get(Key key, Field field) {
        return walFileHandle.walTable()
                .readOps()
                .get(key, field);
    }
}
