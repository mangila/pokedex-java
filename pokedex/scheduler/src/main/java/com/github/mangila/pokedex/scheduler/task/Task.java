package com.github.mangila.pokedex.scheduler.task;

public interface Task extends Runnable {
    String name();

    void schedule();

    boolean shutdown();
}
