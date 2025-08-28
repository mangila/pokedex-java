package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.shared.SimpleBackgroundThread;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.zip.GZIPOutputStream;

class CompressionThread implements SimpleBackgroundThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionThread.class);
    private final BlockingQueue queue;
    private final ScheduledExecutorService executor;

    CompressionThread(BlockingQueue queue) {
        this.queue = queue;
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    }

    @Override
    public void schedule() {
        executor.submit(this);
    }

    @Override
    public void shutdown() {
        VirtualThreadFactory.terminateGracefully(executor);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("Compression thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
            Path source = queueEntry.unwrapAs(Path.class);
            try {
                LOGGER.info("Compressing {}", source);
                Path target = Path.of(source.toString().concat(".gz"));
                try (var in = Files.newInputStream(source);
                     var out = new GZIPOutputStream(Files.newOutputStream(target))) {
                    in.transferTo(out);
                }
                Files.deleteIfExists(source);
            } catch (IOException e) {
                LOGGER.error("Failed to compress file {}", source, e);
                if (queueEntry.equalsMaxRetries(3)) {
                    queue.addDlq(queueEntry);
                } else {
                    queueEntry.incrementFailCounter();
                    queue.add(queueEntry);
                }
            }
        }
    }
}
