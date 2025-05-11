package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record TaskConfig(TriggerConfig triggerConfig, WorkerConfig workerConfig) {

    public static TaskConfig from(TriggerConfig triggerConfig, WorkerConfig workerConfig) {
        return new TaskConfig(triggerConfig, workerConfig);
    }

    public static TaskConfig defaultConfig() {
        return TaskConfig.from(
                TriggerConfig.from(
                        VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                        0,
                        500,
                        TimeUnit.MILLISECONDS
                ),
                WorkerConfig.from(5)
        );
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

