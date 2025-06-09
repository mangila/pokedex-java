package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;

import java.util.HashMap;

public record SchedulerConfig(HashMap<String, Task> tasks) {
}
