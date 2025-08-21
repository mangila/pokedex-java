package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;

public record ReaderThread(
        Queue queue,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry != null) {
            ReadOperation operation = queueEntry.unwrapAs(ReadOperation.class);
            try {
                Offset offset = indexFileHandler.get(operation.key());
                if (offset == null) {
                    operation.result().complete(Value.EMPTY);
                    return;
                }
                Value value = dataFileHandler.read(offset);
                operation.result().complete(value);
            } catch (Exception e) {
                operation.result().completeExceptionally(e);
            }
        }
    }
}
