package com.github.mangila.pokedex.database.model;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public record WalFileChannel(AsynchronousFileChannel channel) {

    private static final CompletionHandler<Integer, Attachment> COMPLETION_HANDLER = new CompletionHandler<>() {
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

    public record Attachment(CompletableFuture<WalAppendStatus> future,
                             long position,
                             int bytesToWrite,
                             Buffer buffer) {
    }


    public void write(Attachment attachment) {
        channel.write(attachment.buffer.value(), attachment.position, attachment, COMPLETION_HANDLER);
    }

    public void read(Attachment attachment) {
        channel.read(attachment.buffer.value(), attachment.position, attachment, COMPLETION_HANDLER);
    }

    public void close() throws IOException {
        channel.close();
    }
}
