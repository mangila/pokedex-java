package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import com.github.mangila.pokedex.database.internal.io.model.WriteOperation;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;

public record WriterThread(
        String writeQueueName,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        QueueEntry queueEntry = QueueService.getInstance().poll(writeQueueName);
        if (queueEntry != null) {
            WriteOperation operation = queueEntry.unwrapAs(WriteOperation.class);
            try {
                OffsetBoundary boundary = dataFileHandler.append(operation.value());
                indexFileHandler.append(operation.key(), boundary.start());
                operation.result().complete(true);
            } catch (Exception e) {
                operation.result().completeExceptionally(e);
            }
        }
    }
}
