package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record ShutdownTask(QueueService queueService,
                           TaskExecutor taskExecutor) implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownTask.class);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleWithFixedDelay(this, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (queueService.allQueuesEmpty()) {
            LOGGER.info("All queues empty, shutting down Scheduler");
            Scheduler.shutdown = true;
            VirtualThreadFactory.newThread(taskExecutor::shutdown)
                    .start();
            // TODO: temp when just testing
            Config.SHUTDOWN_QUEUE.add(true);
        }
    }
}
