package com.github.mangila.pokedex.database.model;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public record WalFileChannel(AsynchronousFileChannel channel) {

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

    public void write(Buffer writeBuffer, long position, CompletableFuture<Boolean> attachment) {
        channel.write(writeBuffer.value(), position, attachment, COMPLETION_HANDLER);
    }

    public void truncate(long size) {
        try {
            channel.truncate(size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
