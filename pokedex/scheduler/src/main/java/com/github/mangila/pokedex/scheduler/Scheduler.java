package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private static SchedulerConfig config;

    private final List<Task> tasks;

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
        tasks.forEach(this::scheduleTask);
    }

    /**
     * Trigger Thread with a Worker pool
     */
    private void scheduleTask(Task task) {
        log.info("Scheduling {}", task.getTaskName());
        var taskConfig = task.getTaskConfig();
        var triggerConfig = taskConfig.triggerConfig();
        var workerConfig = taskConfig.workerConfig();
        var workerPool = VirtualThreadConfig.newFixedThreadPool(workerConfig.poolSize());
        switch (triggerConfig.taskType()) {
            case ONE_OFF -> triggerConfig.executor()
                    .schedule(() -> workerPool.submit(task),
                            triggerConfig.initialDelay(),
                            triggerConfig.timeUnit());
            case FIXED_RATE -> triggerConfig.executor()
                    .scheduleAtFixedRate(() -> workerPool.submit(task),
                            triggerConfig.initialDelay(),
                            triggerConfig.delay(),
                            triggerConfig.timeUnit());
            case FIXED_DELAY -> triggerConfig.executor()
                    .scheduleWithFixedDelay(() -> workerPool.submit(task),
                            triggerConfig.initialDelay(),
                            triggerConfig.delay(),
                            triggerConfig.timeUnit());
        }
    }
}
