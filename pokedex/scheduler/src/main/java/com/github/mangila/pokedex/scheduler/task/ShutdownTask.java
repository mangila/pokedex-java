package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record ShutdownTask(QueueService queueService) implements Task {

    private static final Logger log = LoggerFactory.getLogger(ShutdownTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadConfig.newSingleThreadScheduledExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(this, 1, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean shutdown() {
        log.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadUtils.terminateExecutorGracefully(SCHEDULED_EXECUTOR, duration);
    }

    @Override
    public void run() {
        if (queueService.allQueuesEmpty()) {
            log.info("All queues empty, shutting down");
            SchedulerApplication.IS_RUNNING.set(Boolean.FALSE);
        }
    }
}
