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
    private final Phaser writePhaser;

    WalFileChannel(AsynchronousFileChannel channel) throws IOException {
        this.channel = channel;
        this.position = new AtomicLong(size());
        this.writePhaser = new Phaser();
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
        writePhaser.register();
        channel.write(attachment.buffer.value(), attachment.position, attachment, WRITE_COMPLETION_HANDLER);
    }

    private final CompletionHandler<Integer, Attachment> WRITE_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result != attachment.bytesToWrite) {
                // TODO retry read or write or throw exception for partial writes and discard write
                attachment.future.complete(WalIoOperationStatus.FAILED);
                return;
            }
            writePhaser.arriveAndDeregister();
            attachment.future.complete(WalIoOperationStatus.SUCCESS);
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            writePhaser.arriveAndDeregister();
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

    void read(Attachment attachment) {
        channel.read(attachment.buffer.value(), attachment.position, attachment, READ_COMPLETION_HANDLER);
    }

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

    long size() throws IOException {
        return channel.size();
    }

    void close() throws IOException {
        position.set(0);
        channel.close();
    }

    AtomicLong position() {
        return position;
    }

    void awaitInFlightWrites(Duration timeout) throws InterruptedException, TimeoutException {
        int phase = writePhaser.getPhase();
        if (writePhaser.getRegisteredParties() == 0 || writePhaser.getUnarrivedParties() == 0) {
            return;
        }
        writePhaser.awaitAdvanceInterruptibly(
                phase,
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

}
