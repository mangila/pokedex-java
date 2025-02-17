package com.github.mangila.scheduler.task;

import com.github.mangila.scheduler.service.GridFsService;
import com.github.mangila.scheduler.service.MongoDbService;
import com.github.mangila.scheduler.service.QueueService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ImageTask {

    private final QueueService queueService;
    private final GridFsService gridFsService;
    private final MongoDbService mongoDbService;
    private final RestClient http;

    public ImageTask(QueueService queueService,
                     GridFsService gridFsService,
                     MongoDbService mongoDbService,
                     @Qualifier("pokemon-media-client") RestClient restClient
    ) {
        this.queueService = queueService;
        this.gridFsService = gridFsService;
        this.mongoDbService = mongoDbService;
        this.http = restClient;
    }

    @SneakyThrows({URISyntaxException.class})
    @Scheduled(fixedRate = 2, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void pollImage() {
        var optionalImage = queueService.popImageQueue();
        if (optionalImage.isEmpty()) {
            return;
        }
        var image = optionalImage.get();
        log.info("Processing - {}", image.buildFileName());
        var bytes = http.get()
                .uri(image.url().toURI())
                .retrieve()
                .body(byte[].class);
        var mediaId = gridFsService.store(image, bytes);
        mongoDbService.saveImageToVariety(mediaId, image);
    }
}