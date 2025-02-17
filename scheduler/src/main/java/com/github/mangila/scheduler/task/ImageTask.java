package com.github.mangila.scheduler.task;

import com.github.mangila.scheduler.service.GridFsService;
import com.github.mangila.scheduler.service.MongoDbService;
import com.github.mangila.scheduler.service.QueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class ImageTask {

    private final QueueService queueService;
    private final GridFsService gridFsService;
    private final MongoDbService mongoDbService;

    @Scheduled(fixedRate = 2, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void pollImage() {
        var optionalImage = queueService.popImageQueue();
        if (optionalImage.isEmpty()) {
            return;
        }
        var image = optionalImage.get();
        log.info("Processing - {}", image.buildFileName());
        var mediaId = gridFsService.store(image);
        mongoDbService.saveImageToVariety(mediaId, image);
    }
}