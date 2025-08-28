package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.CallbackItem;
import com.github.mangila.pokedex.database.model.CallbackItemCollection;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.WriteCallback;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_COMPRESSION_QUEUE;
import static com.github.mangila.pokedex.shared.Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE;

public final class DefaultWalManager implements WalManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWalManager.class);
    private final WalFileHandler walFileHandler;
    private final FlushDelegateSubscriber flushDelegateSubscriber;
    private final EntryPublisher entryPublisher;
    private final ReentrantLock writeLock;
    private final RotateThread rotateThread;
    private final List<FlushLimitThread> flushLimitThreads;
    private final CompressionThread compressionThread;

    public DefaultWalManager(WalConfig config) {
        BlockingQueue queue = QueueService.getInstance()
                .getBlockingQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        this.walFileHandler = new WalFileHandler();
        this.entryPublisher = new EntryPublisher();
        this.writeLock = new ReentrantLock(true);
        this.rotateThread = new RotateThread(config.thresholdSize(), walFileHandler, writeLock);
        this.flushLimitThreads = new ArrayList<>();
        this.compressionThread = new CompressionThread(
                QueueService.getInstance()
                        .getBlockingQueue(DATABASE_WAL_COMPRESSION_QUEUE)
        );
        for (int i = 0; i < 3; i++) {
            flushLimitThreads.add(new FlushLimitThread(config.thresholdLimit(), queue, walFileHandler, writeLock));
        }
        this.flushDelegateSubscriber = new FlushDelegateSubscriber(queue);
    }

    @Override
    public void open() {
        LOGGER.info("Opening WAL manager");
        walFileHandler.replay();
        entryPublisher.subscribe(flushDelegateSubscriber);
        rotateThread.schedule();
        flushLimitThreads.forEach(FlushLimitThread::schedule);
        compressionThread.schedule();
    }

    @Override
    public void close() {
        LOGGER.info("Closing WAL manager");
        try {
            walFileHandler.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close WAL file {}", walFileHandler.path(), e);
        }
        entryPublisher.close();
        rotateThread.shutdown();
        flushLimitThreads.forEach(FlushLimitThread::shutdown);
        compressionThread.shutdown();
    }

    @Override
    public void flush() {
        for (FlushLimitThread flushLimitThread : flushLimitThreads) {
            List<CallbackItem<Entry>> items = flushLimitThread.items();
            try {
                walFileHandler.flush(new FlushWriteBuffer(), new CallbackItemCollection(items));
            } catch (IOException e) {
                LOGGER.error("ERR", e);
            }
        }
    }

    @Override
    public WriteCallback put(Entry entry) {
        WriteCallback writeCallback = WriteCallback.newCallback();
        CallbackItem<Entry> callbackItem = new CallbackItem<>(entry, writeCallback);
        entryPublisher.submit(callbackItem);
        return writeCallback;
    }
}
