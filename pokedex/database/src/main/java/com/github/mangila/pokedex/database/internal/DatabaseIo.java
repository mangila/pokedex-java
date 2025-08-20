package com.github.mangila.pokedex.database.internal;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.data.DataFileHandler;
import com.github.mangila.pokedex.database.internal.io.index.IndexFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.ReaderThread;
import com.github.mangila.pokedex.database.internal.io.internal.WriterThread;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseIo {
    private final IndexFileHandler indexFileHandler;
    private final DataFileHandler dataFileHandler;
    private final ScheduledExecutorService writeExecutor;
    private final ScheduledExecutorService readExecutor;
    private final String readQueueName;
    private final String writeQueueName;
    private final ReaderThread readerThread;
    private final WriterThread writerThread;
    private final AtomicInteger writeCounter = new AtomicInteger(0);

    public DatabaseIo(DatabaseName databaseName) {
        this.indexFileHandler = new IndexFileHandler(databaseName);
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.writeExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.readExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.readQueueName = databaseName.value().concat("-read");
        this.writeQueueName = databaseName.value().concat("-write");
        QueueService queueService = QueueService.getInstance();
        queueService.createNewQueue(readQueueName);
        queueService.createNewQueue(writeQueueName);
        this.writerThread = new WriterThread(writeQueueName, indexFileHandler, dataFileHandler);
        this.readerThread = new ReaderThread(readQueueName, indexFileHandler, dataFileHandler);
    }

    public void init() throws IOException {
        indexFileHandler.init();
        dataFileHandler.init();
        writeExecutor.scheduleWithFixedDelay(writerThread, 0, 100, TimeUnit.MILLISECONDS);
        readExecutor.scheduleAtFixedRate(readerThread, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        VirtualThreadFactory.terminateExecutorGracefully(writeExecutor, Duration.ofSeconds(30));
        VirtualThreadFactory.terminateExecutorGracefully(readExecutor, Duration.ofSeconds(30));
    }

    public void truncate() throws IOException {
        dataFileHandler.truncate();
        indexFileHandler.truncate();
    }

    public void delete() throws IOException {
        dataFileHandler.delete();
        indexFileHandler.delete();
    }

    public CompletableFuture<Value> readAsync(ReadOperation readOperation) {
        QueueService.getInstance()
                .add(readQueueName, new QueueEntry(readOperation));
        return readOperation.result();
    }

    public CompletableFuture<Boolean> writeAsync(WriteOperation writeOperation) {
        if (writeCounter.incrementAndGet() == 100) {
            // TODO: run compaction
            writeCounter.set(0);
        }
        QueueService.getInstance()
                .add(writeQueueName, new QueueEntry(writeOperation));
        return writeOperation.result();
    }

    public int size() {
        return indexFileHandler.size();
    }
}
