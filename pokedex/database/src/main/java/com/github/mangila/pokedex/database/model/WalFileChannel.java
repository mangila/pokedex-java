package com.github.mangila.pokedex.database.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class WalFileChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileChannel.class);
    private final AsynchronousFileChannel channel;
    private final AtomicLong position;
    private final Phaser phaser;

    public WalFileChannel(AsynchronousFileChannel channel) throws IOException {
        this.channel = channel;
        this.position = new AtomicLong(size());
        this.phaser = new Phaser();
    }

    private final CompletionHandler<Integer, Attachment> WRITE_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result != attachment.bytesToWrite) {
                // TODO retry read or write or throw exception for partial writes and discard write
                attachment.future.complete(WalAppendStatus.FAILED);
                return;
            }
            phaser.arriveAndDeregister();
            attachment.future.complete(WalAppendStatus.SUCCESS);
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            phaser.arriveAndDeregister();
            attachment.future.completeExceptionally(exc);
        }
    };

    private final CompletionHandler<Integer, Attachment> READ_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            if (result != attachment.bytesToWrite) {
                // TODO retry read or write or throw exception for partial writes and discard write
                attachment.future.complete(WalAppendStatus.FAILED);
                return;
            }
            attachment.future.complete(WalAppendStatus.SUCCESS);
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            attachment.future.completeExceptionally(exc);
        }
    };


    public void write(Buffer writeBuffer, CompletableFuture<WalAppendStatus> future) {
        int bytesToWrite = writeBuffer.remaining();
        var attachment = new WalFileChannel.Attachment(
                future,
                position.getAndAdd(bytesToWrite),
                bytesToWrite,
                writeBuffer
        );
        write(attachment);
    }

    public record Attachment(CompletableFuture<WalAppendStatus> future,
                             long position,
                             int bytesToWrite,
                             Buffer buffer) {
    }


    public void write(Attachment attachment) {
        phaser.register();
        channel.write(attachment.buffer.value(), attachment.position, attachment, WRITE_COMPLETION_HANDLER);
    }

    public void read(Attachment attachment) {
        channel.read(attachment.buffer.value(), attachment.position, attachment, READ_COMPLETION_HANDLER);
    }

    public long size() throws IOException {
        return channel.size();
    }

    public void close() throws IOException {
        position.set(0);
        channel.close();
    }

    public boolean awaitInFlightWritesWithRetry(Duration timeout, int attempts) throws InterruptedException {
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
