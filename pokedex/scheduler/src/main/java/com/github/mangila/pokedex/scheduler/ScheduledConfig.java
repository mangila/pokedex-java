package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.TimeUnit;

public record ScheduledConfig(int initialDelay,
                              int delay,
                              TimeUnit timeUnit) {
}
