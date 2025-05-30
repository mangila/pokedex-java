package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public record ShutdownTask(QueueService queueService) implements Task {

    private static final Logger log = LoggerFactory.getLogger(ShutdownTask.class);

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public TaskConfig getTaskConfig() {
        var trigger = TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                TaskConfig.TaskType.FIXED_DELAY,
                1,
                5,
                TimeUnit.MINUTES);
        var workers = TaskConfig.WorkerConfig.from(1);
        return TaskConfig.from(trigger, workers);
    }

    @Override
    public void run() {
        if (queueService.allQueuesEmpty()) {
            log.info("All queues empty, shutting down");
            SchedulerApplication.IS_RUNNING.set(Boolean.FALSE);
        }
    }
}
