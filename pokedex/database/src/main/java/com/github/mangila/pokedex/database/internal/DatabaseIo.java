package com.github.mangila.pokedex.database.internal;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.internal.DataFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.IndexFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.ReaderThread;
import com.github.mangila.pokedex.database.internal.io.internal.WriterThread;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseIo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIo.class);
    private final IndexFileHandler indexFileHandler;
    private final DataFileHandler dataFileHandler;
    private final ScheduledExecutorService writeExecutor;
    private final ScheduledExecutorService readExecutor;
    private final Queue readQueue;
    private final Queue writeQueue;
    private final ReaderThread readerThread;
    private final WriterThread writerThread;
    private final AtomicInteger writeCounter = new AtomicInteger(0);

    public DatabaseIo(DatabaseName databaseName) {
        this.indexFileHandler = new IndexFileHandler(
                new DatabaseFileName(databaseName.value()
                        .concat("-index")
                        .concat(".yakvs"))
        );
        this.dataFileHandler = new DataFileHandler(
                new DatabaseFileName(databaseName.value()
                        .concat("-data")
                        .concat(".yakvs"))
        );
        this.writeExecutor = VirtualThreadFactory.newScheduledThreadPool(10);
        this.readExecutor = VirtualThreadFactory.newScheduledThreadPool(10);
        QueueService queueService = QueueService.getInstance();
        this.readQueue = queueService.createNewQueue(new QueueName(databaseName.value().concat("-read")));
        this.writeQueue = queueService.createNewQueue(new QueueName(databaseName.value().concat("-write")));
        this.writerThread = new WriterThread(writeQueue, indexFileHandler, dataFileHandler);
        this.readerThread = new ReaderThread(readQueue, indexFileHandler, dataFileHandler);
    }

    public void init() throws IOException {
        indexFileHandler.init();
        indexFileHandler.loadIndexes();
        dataFileHandler.init();
        startThreads();
    }

    public void startThreads() {
        startReaderThread();
        startWriterThread();
    }

    public void startWriterThread() {
        LOGGER.info("Starting writer thread");
        writeExecutor.scheduleAtFixedRate(writerThread, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void startReaderThread() {
        LOGGER.info("Starting reader thread");
        readExecutor.scheduleAtFixedRate(readerThread, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        shutdownReaderThread();
        shutdownWriterThread();
    }

    public void shutdownWriterThread() {
        LOGGER.info("Shutting down writer thread");
        VirtualThreadFactory.terminateGracefully(writeExecutor, Duration.ofSeconds(30));
    }

    public void shutdownReaderThread() {
        LOGGER.info("Shutting down reader thread");
        VirtualThreadFactory.terminateGracefully(readExecutor, Duration.ofSeconds(30));
    }

    public CompletableFuture<Boolean> truncateAsync() {
        WriteOperation writeOperation = new WriteOperation(
                Key.EMPTY,
                Value.EMPTY,
                WriteOperation.Operation.TRUNCATE,
                new CompletableFuture<>()
        );
        writeQueue.add(new QueueEntry(writeOperation));
        return writeOperation.result();
    }

    public CompletableFuture<Boolean> deleteAsync() {
        WriteOperation writeOperation = new WriteOperation(
                Key.EMPTY,
                Value.EMPTY,
                WriteOperation.Operation.DELETE,
                new CompletableFuture<>()
        );
        writeQueue.add(new QueueEntry(writeOperation));
        return writeOperation.result();
    }

    public CompletableFuture<Value> readAsync(ReadOperation readOperation) {
        readQueue.add(new QueueEntry(readOperation));
        return readOperation.result();
    }

    public CompletableFuture<Boolean> writeAsync(WriteOperation writeOperation) {
        if (writeCounter.incrementAndGet() == 100) {
            // TODO: run compaction
            writeCounter.set(0);
        }
        writeQueue.add(new QueueEntry(writeOperation));
        return writeOperation.result();
    }

    public int size() {
        return indexFileHandler.indexMap().size();
    }
}
