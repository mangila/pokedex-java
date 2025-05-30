package com.github.mangila.pokedex.scheduler.task;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TODO cron parsing?
 *
 * @param triggerConfig - configure scheduled executor trigger
 * @param workerConfig  - configure workers running
 */
public record TaskConfig(TriggerConfig triggerConfig, WorkerConfig workerConfig) {

    public static TaskConfig from(TriggerConfig triggerConfig, WorkerConfig workerConfig) {
        return new TaskConfig(triggerConfig, workerConfig);
    }

    public enum TaskType {
        FIXED_RATE, FIXED_DELAY, ONE_OFF
    }

    public record TriggerConfig(ScheduledExecutorService executor,
                                TaskType taskType,
                                int initialDelay,
                                int delay,
                                TimeUnit timeUnit) {

        public static TriggerConfig from(ScheduledExecutorService executor,
                                         TaskType taskType,
                                         int initialDelay,
                                         int delay,
                                         TimeUnit timeUnit) {
            return new TriggerConfig(executor, taskType, initialDelay, delay, timeUnit);
        }
    }

    public record WorkerConfig(int poolSize) {
        public static WorkerConfig from(int poolSize) {
            return new WorkerConfig(poolSize);
        }
    }
}

