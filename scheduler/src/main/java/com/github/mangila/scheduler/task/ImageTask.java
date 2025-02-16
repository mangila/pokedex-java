package com.github.mangila.scheduler.task;

import com.github.mangila.scheduler.service.GridFsService;
import com.github.mangila.scheduler.service.QueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class ImageTask {

    private final QueueService queueService;
    private final GridFsService gridFsService;

    @Scheduled(fixedRate = 10, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void pollImage() {
        var image = queueService.popImageQueue();
        if (Objects.isNull(image)) {
            return;
        }
        log.info("Processing - {}", image.description());
        var id = gridFsService.store(image);
    }
}
