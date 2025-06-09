package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private static SchedulerConfig config;

    private final Map<String, Task> tasks;

    private Scheduler(SchedulerConfig config) {
        this.tasks = config.tasks();
    }

    public static void configure(SchedulerConfig config) {
        Objects.requireNonNull(config, "SchedulerConfig must not be null");
        if (Scheduler.config != null) {
            throw new IllegalStateException("SchedulerConfig is already configured");
        }
        log.info("Configuring Scheduler with {}", config);
        Scheduler.config = config;
    }

    private static final class Holder {
        private static final Scheduler INSTANCE = new Scheduler(config);
    }

    public static Scheduler getInstance() {
        Objects.requireNonNull(config, "Scheduler must be configured");
        return Holder.INSTANCE;
    }

    public void init() {
        tasks.forEach((key, task) -> {
            log.info("Schedule task {}", key);
            task.schedule();
        });
    }

    public void shutdownAllTasks() {
        tasks.forEach((key, task) -> {
            log.info("Shutting down task {}", key);
            shutdownTask(task);
        });
    }

    private void shutdownTask(Task task) {
        var name = task.name();
        var isShutdown = task.shutdown();
        if (isShutdown) {
            log.info("Task {} shutdown successfully", name);
        } else {
            log.warn("Task {} failed to shutdown", name);
        }
    }
}
