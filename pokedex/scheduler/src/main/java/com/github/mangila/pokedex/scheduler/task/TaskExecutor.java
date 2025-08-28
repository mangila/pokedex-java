package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

public record TaskExecutor(ScheduledExecutorService scheduledPool) {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);

    public void schedule(Task task) {
        LOGGER.info("Scheduling task: {}", task.name());
        task.schedule(scheduledPool);
    }

    public void shutdown() {
        LOGGER.info("Shutting down TaskExecutor");
        VirtualThreadFactory.terminateGracefully(scheduledPool);
    }

}
