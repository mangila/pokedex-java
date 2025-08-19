package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.util.DiskOperationQueue;
import com.github.mangila.pokedex.database.internal.io.model.WriteOperation;

public record WriterThread(
        DiskOperationQueue<WriteOperation> writeOperationQueue,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            WriteOperation operation = writeOperationQueue.poll();
            Offset offset = dataFileHandler.write(operation.key(), operation.value());
            indexFileHandler.put(operation.key(), offset);
            operation.result().complete(true);
        }
    }
}
