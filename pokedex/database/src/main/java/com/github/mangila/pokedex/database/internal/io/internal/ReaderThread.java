package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.util.DiskOperationQueue;
import com.github.mangila.pokedex.database.internal.io.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.model.Value;

public record ReaderThread(
        DiskOperationQueue<ReadOperation> readOperationQueue,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ReadOperation operation = readOperationQueue.poll();
            Offset offset = indexFileHandler.get(operation.key());
            Value value = dataFileHandler.read(offset);
            operation.result().complete(value);
        }
    }
}
