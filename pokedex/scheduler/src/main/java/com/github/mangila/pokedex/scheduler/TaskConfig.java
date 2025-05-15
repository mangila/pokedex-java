package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record TaskConfig(TriggerConfig triggerConfig, WorkerConfig workerConfig) {

    public static TaskConfig from(TriggerConfig triggerConfig, WorkerConfig workerConfig) {
        return new TaskConfig(triggerConfig, workerConfig);
    }

    public record TriggerConfig(ScheduledExecutorService executor,
                                int initialDelay,
                                int delay,
                                TimeUnit timeUnit) {

        public static TriggerConfig from(ScheduledExecutorService executor,
                                         int initialDelay,
                                         int delay,
                                         TimeUnit timeUnit) {
            return new TriggerConfig(executor, initialDelay, delay, timeUnit);
        }
    }

    public record WorkerConfig(int poolSize) {
        public static WorkerConfig from(int poolSize) {
            return new WorkerConfig(poolSize);
        }
    }
}

