package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record ShutdownTask(QueueService queueService) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadFactory.newSingleThreadScheduledExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public boolean shutdown() {
        LOGGER.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateGracefully(SCHEDULED_EXECUTOR, duration);
    }

    @Override
    public void run() {
        if (queueService.allQueuesEmpty()) {
            LOGGER.info("All queues empty, shutting down Scheduler");
            Scheduler.SHUTDOWN.set(Boolean.TRUE);
        }
    }
}
