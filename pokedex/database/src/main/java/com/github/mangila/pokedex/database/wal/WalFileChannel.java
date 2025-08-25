package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

class WalFileChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileChannel.class);
    private final AsynchronousFileChannel channel;
    private final AtomicLong position;
    private final Phaser phaser;

    WalFileChannel(AsynchronousFileChannel channel) throws IOException {
        this.channel = channel;
        this.position = new AtomicLong(size());
        this.phaser = new Phaser();
    }

    private final CompletionHandler<Integer, Attachment> WRITE_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result != attachment.bytesToWrite) {
                // TODO retry read or write or throw exception for partial writes and discard write
                attachment.future.complete(WalIoOperationStatus.FAILED);
                return;
            }
            phaser.arriveAndDeregister();
            attachment.future.complete(WalIoOperationStatus.SUCCESS);
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            phaser.arriveAndDeregister();
            switch (exc) {
                case IllegalArgumentException iae -> attachment.future.completeExceptionally(iae);
                case NonWritableChannelException nwe -> attachment.future.completeExceptionally(nwe);
                default -> {
                    // TODO: panic
                    attachment.future.completeExceptionally(exc);
                }
            }
        }
    };

    private final CompletionHandler<Integer, Attachment> READ_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result != attachment.bytesToWrite) {
                // TODO retry read or write or throw exception for partial writes and discard write
                attachment.future.complete(WalIoOperationStatus.FAILED);
                return;
            }
            attachment.future.complete(WalIoOperationStatus.SUCCESS);
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            switch (exc) {
                case IllegalArgumentException iae -> attachment.future.completeExceptionally(iae);
                case NonReadableChannelException nre -> attachment.future.completeExceptionally(nre);
                default -> {
                    // TODO: panic
                    attachment.future.completeExceptionally(exc);
                }
            }
        }
    };

    void write(Buffer writeBuffer, CompletableFuture<WalIoOperationStatus> future) {
        int bytesToWrite = writeBuffer.remaining();
        var attachment = new WalFileChannel.Attachment(
                future,
                position.getAndAdd(bytesToWrite),
                bytesToWrite,
                writeBuffer
        );
        write(attachment);
    }

    void truncate(long size) throws IOException {
        channel.truncate(size);
    }

    record Attachment(CompletableFuture<WalIoOperationStatus> future,
                      long position,
                      int bytesToWrite,
                      Buffer buffer) {
    }


    void write(Attachment attachment) {
        phaser.register();
        channel.write(attachment.buffer.value(), attachment.position, attachment, WRITE_COMPLETION_HANDLER);
    }

    void read(Attachment attachment) {
        channel.read(attachment.buffer.value(), attachment.position, attachment, READ_COMPLETION_HANDLER);
    }

    long size() {
        try {
            return channel.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void close() throws IOException {
        position.set(0);
        channel.close();
    }

    boolean awaitInFlightWritesWithRetry(Duration timeout, int attempts) throws InterruptedException {
        boolean success = false;
        do {
            try {
                awaitInFlightWrites(timeout);
                success = true;
                break;
            } catch (TimeoutException e) {
                attempts--;
                TimeUnit.SECONDS.sleep(1);
            }
        } while (attempts >= 0);
        return success;
    }

    private void awaitInFlightWrites(Duration timeout) throws InterruptedException, TimeoutException {
        int phase = phaser.getPhase();
        if (phaser.getRegisteredParties() == 0 || phaser.getUnarrivedParties() == 0) {
            return;
        }
        phaser.awaitAdvanceInterruptibly(
                phase,
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

}
