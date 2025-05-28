package com.github.mangila.pokedex.scheduler.task;

import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {
    String getTaskName();
}
