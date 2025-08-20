package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.data.DataFileHandler;
import com.github.mangila.pokedex.database.internal.io.index.IndexFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;

import java.io.IOException;

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
                switch (operation.operation()) {
                    case WRITE -> write(operation);
                    case TRUNCATE -> truncate(operation);
                    case DELETE -> delete(operation);
                }
            } catch (Exception e) {
                operation.result().completeExceptionally(e);
            }
        }
    }

    private void write(WriteOperation operation) throws IOException {
        OffsetBoundary boundary = dataFileHandler.append(operation.value());
        indexFileHandler.append(operation.key(), boundary.start());
        operation.result().complete(true);
    }

    private void truncate(WriteOperation operation) throws IOException {
        dataFileHandler.truncate();
        indexFileHandler.truncate();
        operation.result().complete(true);
    }

    private void delete(WriteOperation operation) throws IOException {
        dataFileHandler.delete();
        indexFileHandler.delete();
        operation.result().complete(true);
    }
}
