package com.github.mangila.pokedex.scheduler.task;

import java.util.concurrent.ScheduledExecutorService;

public interface Task extends Runnable {
    String name();

    void schedule(ScheduledExecutorService executor);
}
