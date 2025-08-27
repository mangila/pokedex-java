package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    public static final AtomicBoolean RUNNING = new AtomicBoolean(Boolean.FALSE);
    public static final AtomicBoolean SHUTDOWN = new AtomicBoolean(Boolean.FALSE);
    private final List<Task> tasks;

    public Scheduler(SchedulerConfig config) {
        this.tasks = config.tasks();
    }

    public void init() {
        LOGGER.info("Initializing scheduler");
        tasks.forEach(Task::schedule);
        RUNNING.set(Boolean.TRUE);
    }

    public void shutdownAllTasks() {
        if (!RUNNING.get()) {
            LOGGER.debug("Scheduler is not running");
            return;
        }
        if (SHUTDOWN.get()) {
            LOGGER.info("Shutting down scheduler");
            tasks.forEach(this::shutdown);
            RUNNING.set(Boolean.FALSE);
        } else {
            LOGGER.warn("Scheduler is still running, skipping shutdown");
        }
    }

    private void shutdown(Task task) {
        String name = task.name();
        LOGGER.info("Shutting down {}", name);
        task.shutdown();
    }
}
