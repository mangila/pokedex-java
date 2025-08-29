package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import com.github.mangila.pokedex.scheduler.task.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final List<Task> tasks;
    private final TaskExecutor taskExecutor;

    public Scheduler(SchedulerConfig config) {
        this.tasks = config.tasks();
        this.taskExecutor = config.taskExecutor();
    }

    public void init() {
        LOGGER.info("Initializing Scheduler");
        tasks.forEach(taskExecutor::schedule);
    }

    public void shutdown() {
        LOGGER.info("Shutting down Scheduler");
        taskExecutor.shutdown();
    }
}
