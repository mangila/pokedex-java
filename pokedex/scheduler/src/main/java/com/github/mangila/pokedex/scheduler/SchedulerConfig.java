package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.Task;
import com.github.mangila.pokedex.scheduler.task.TaskExecutor;

import java.util.List;

public record SchedulerConfig(List<Task> tasks, TaskExecutor taskExecutor) {
}
