package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
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
    private final AtomicLong writePosition = new AtomicLong(0);
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private WalFileChannel walFileChannel;

    public WalFileManager(WalFile walFile, MemTable memTable) {
        this.walFile = walFile;
        this.memTable = memTable;
    }

    void open() throws IOException {
        if (!Files.exists(walFile.path())) {
            Files.createFile(walFile.path());
        }
        this.walFileChannel = new WalFileChannel(
                AsynchronousFileChannel.open(walFile.path(),
                        Set.of(StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.DSYNC),
                        VirtualThreadFactory.newFixedThreadPool(10)));
        writePosition.set(Files.size(walFile.path()));
        QueueService.getInstance().createNewQueue(new QueueName("hej"));
        FLUSH_SYNC.scheduleWithFixedDelay(() -> {
            while (true) {
                if (memTable.size() >= 50) {
                    try {
                        rwLock.writeLock().lock();
                        flush();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        rwLock.readLock().unlock();
                    }
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    CompletableFuture<Boolean> append(Key key, Value value) {
        CompletableFuture<Boolean> writeFuture = new CompletableFuture<>();
        try {
            rwLock.readLock().lock();
            Buffer keyBuffer = key.toBuffer(true);
            Buffer valueBuffer = value.toBuffer(true);
            Buffer writeBuffer = Buffer.from(keyBuffer.capacity() + valueBuffer.capacity());
            writeBuffer.put(keyBuffer);
            writeBuffer.put(valueBuffer);
            writeBuffer.flip();
            int bytesToWrite = writeBuffer.value().remaining();
            long position = writePosition.getAndAdd(bytesToWrite);
            walFileChannel.write(writeBuffer, position, writeFuture);
        } catch (Exception e) {
            writeFuture.completeExceptionally(e);
        } finally {
            rwLock.readLock().unlock();
        }
        writeFuture.thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                memTable.put(key, value);
            }
        });
        return writeFuture;
    }

    private void flush() {
        MemTable memTableSnapshot = new MemTable(memTable.map());
        QueueService.getInstance().add(
                new QueueName("hej"),
                new QueueEntry(memTableSnapshot)
        );
        memTable.clear();
        walFileChannel.truncate(0);
        writePosition.set(0);
    }
}
