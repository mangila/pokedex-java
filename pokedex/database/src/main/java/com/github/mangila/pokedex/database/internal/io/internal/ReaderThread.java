package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.data.DataFileHandler;
import com.github.mangila.pokedex.database.internal.io.index.IndexFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;

public record ReaderThread(
        String readQueueName,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        QueueEntry queueEntry = QueueService.getInstance().poll(readQueueName);
        if (queueEntry != null) {
            ReadOperation operation = queueEntry.unwrapAs(ReadOperation.class);
            try {
                Offset offset = indexFileHandler.get(operation.key());
                Value value = dataFileHandler.read(offset);
                operation.result().complete(value);
            } catch (Exception e) {
                operation.result().completeExceptionally(e);
            }
        }
    }
}
