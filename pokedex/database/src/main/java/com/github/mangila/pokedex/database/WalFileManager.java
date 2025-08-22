package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WalFileManager {
    private final WalFile walFile;
    private final MemTable memTable;
    private final ScheduledExecutorService FLUSH_SYNC = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    private final AtomicLong writePosition = new AtomicLong();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private AsynchronousFileChannel walFileChannel;

    public WalFileManager(WalFile walFile, MemTable memTable) {
        this.walFile = walFile;
        this.memTable = memTable;
    }

    void open() throws IOException {
        if (!Files.exists(walFile.path())) {
            Files.createFile(walFile.path());
        }
        this.walFileChannel = AsynchronousFileChannel.open(
                walFile.path(),
                Set.of(StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.DSYNC),
                VirtualThreadFactory.newFixedThreadPool(10)
        );
        writePosition.set(Files.size(walFile.path()));
        QueueService.getInstance().createNewQueue(new QueueName("hej"));
        FLUSH_SYNC.scheduleWithFixedDelay(() -> {
            while (true) {
                if (memTable.size() >= 50) {
                    flush();
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public static final CompletionHandler<Integer, CompletableFuture<Boolean>> COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, CompletableFuture<Boolean> attachment) {
            attachment.complete(true);
        }

        @Override
        public void failed(Throwable exc, CompletableFuture<Boolean> attachment) {
            attachment.completeExceptionally(exc);
        }
    };

    CompletableFuture<Boolean> append(Key key, Value value) {
        rwLock.readLock().lock();
        CompletableFuture<Boolean> writeFuture = new CompletableFuture<>();
        Buffer keyBuffer = key.toBuffer();
        Buffer valueBuffer = value.toBuffer();
        Buffer writeBuffer = Buffer.from(keyBuffer.capacity() + valueBuffer.capacity());
        writeBuffer.put(keyBuffer);
        writeBuffer.put(valueBuffer);
        writeBuffer.flip();
        int bytesToWrite = writeBuffer.value().remaining();
        long position = writePosition.getAndAdd(bytesToWrite);
        walFileChannel.write(writeBuffer.value(), position, writeFuture, COMPLETION_HANDLER);
        memTable.put(key, value);
        rwLock.readLock().unlock();
        return writeFuture;
    }

    CompletableFuture<Boolean> flush() {
        rwLock.writeLock().lock();
        CompletableFuture<Boolean> flushFuture = new CompletableFuture<>();
        try (var executor = VirtualThreadFactory.newSingleThreadExecutor()) {
            executor.submit(() -> {
                try {
                    MemTable snapshot = new MemTable(memTable.map());
                    System.out.println("LOCK %s".formatted(snapshot.size()));
                    QueueService.getInstance().add(
                            new QueueName("hej"),
                            new QueueEntry(snapshot)
                    );
                    memTable.clear();
                    walFileChannel.truncate(0);
                    writePosition.set(0);
                    flushFuture.complete(true);
                } catch (Exception e) {
                    flushFuture.completeExceptionally(e);
                }
            });
        }
        rwLock.writeLock().unlock();
        return flushFuture;
    }
}
