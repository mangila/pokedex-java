package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final List<Task> tasks;

    public Scheduler(SchedulerConfig config) {
        this.tasks = config.tasks();
    }

    public void init() {
        LOGGER.info("Initializing scheduler");
        tasks.forEach(Task::schedule);
    }

    public void shutdownAllTasks() {
        LOGGER.info("Shutting down scheduler");
        tasks.forEach(this::shutdownTask);
    }

    private void shutdownTask(Task task) {
        String name = task.name();
        boolean isShutdown = task.shutdown();
        if (isShutdown) {
            LOGGER.info("{} shutdown successfully", name);
        } else {
            LOGGER.warn("{} failed to shutdown", name);
        }
    }
}
