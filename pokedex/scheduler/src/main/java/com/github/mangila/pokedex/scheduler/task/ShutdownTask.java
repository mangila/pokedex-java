package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.Config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record ShutdownTask() implements Task {
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.schedule(this, 20, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        // TEMP: testing
        Config.SHUTDOWN_QUEUE.add(true);
    }
}
