package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;

import java.io.IOException;

public record WriterThread(
        Queue queue,
        IndexFileHandler indexFileHandler,
        DataFileHandler dataFileHandler) implements Runnable {

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry != null) {
            WriteOperation writeOperation = queueEntry.unwrapAs(WriteOperation.class);
            try {
                switch (writeOperation.operation()) {
                    case WRITE -> write(writeOperation);
                    case TRUNCATE -> truncate();
                    case DELETE -> delete();
                }
                writeOperation.result().complete(true);
            } catch (Exception e) {
                writeOperation.result().completeExceptionally(e);
            }
        }
    }

    private void write(WriteOperation writeOperation) throws IOException {
        OffsetBoundary boundary = dataFileHandler.append(writeOperation.value());
        indexFileHandler.append(writeOperation.key(), boundary.start());
    }

    private void truncate() throws IOException {
        dataFileHandler.truncate();
        indexFileHandler.truncate();
    }

    private void delete() throws IOException {
        dataFileHandler.delete();
        indexFileHandler.delete();
    }
}
