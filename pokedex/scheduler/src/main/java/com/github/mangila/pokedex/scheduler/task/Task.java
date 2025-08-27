package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.SimpleBackgroundThread;

public interface Task extends SimpleBackgroundThread {
    String name();
}
