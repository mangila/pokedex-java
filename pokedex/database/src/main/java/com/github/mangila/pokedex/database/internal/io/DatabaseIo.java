package com.github.mangila.pokedex.database.internal.io;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.internal.DataFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.IndexFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.ReaderThread;
import com.github.mangila.pokedex.database.internal.io.internal.WriterThread;
import com.github.mangila.pokedex.database.internal.io.internal.util.DiskOperationQueue;
import com.github.mangila.pokedex.database.internal.io.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.model.Value;
import com.github.mangila.pokedex.database.internal.io.model.WriteOperation;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

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
    private final DiskOperationQueue<ReadOperation> readOperationQueue;
    private final DiskOperationQueue<WriteOperation> writeOperationQueue;
    private final ReaderThread readerThread;
    private final WriterThread writerThread;

    private final AtomicInteger writeCounter = new AtomicInteger(0);

    public DatabaseIo(DatabaseName databaseName, int nReadThreads) {
        this.indexFileHandler = new IndexFileHandler(databaseName);
        this.dataFileHandler = new DataFileHandler(databaseName);
        this.writeExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.readExecutor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        this.writeOperationQueue = new DiskOperationQueue<>();
        this.readOperationQueue = new DiskOperationQueue<>();
        this.writerThread = new WriterThread(writeOperationQueue, indexFileHandler, dataFileHandler);
        this.readerThread = new ReaderThread(readOperationQueue, indexFileHandler, dataFileHandler);
    }

    public void init() throws Exception {
        indexFileHandler.init();
        dataFileHandler.init();
        writeExecutor.scheduleWithFixedDelay(writerThread, 0, 0, TimeUnit.SECONDS);
        readExecutor.scheduleWithFixedDelay(readerThread, 0, 0, TimeUnit.SECONDS);
    }

    public CompletableFuture<Value> readAsync(ReadOperation readOperation) {
        readOperationQueue.add(readOperation);
        return readOperation.result();
    }

    public CompletableFuture<Boolean> writeAsync(WriteOperation writeOperation) {
        if (writeCounter.incrementAndGet() == 100) {
            // TODO: run compaction
            writeCounter.set(0);
        }
        writeOperationQueue.add(writeOperation);
        return writeOperation.result();
    }

    public void shutdown() {
        VirtualThreadFactory.terminateExecutorGracefully(writeExecutor, Duration.ofSeconds(30));
        VirtualThreadFactory.terminateExecutorGracefully(readExecutor, Duration.ofSeconds(30));
    }
}
