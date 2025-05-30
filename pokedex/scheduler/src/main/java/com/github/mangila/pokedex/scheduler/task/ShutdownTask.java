package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.queue.QueueService;

import java.util.concurrent.TimeUnit;

public record ShutdownTask(QueueService queueService) implements Task {

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

    }
}
