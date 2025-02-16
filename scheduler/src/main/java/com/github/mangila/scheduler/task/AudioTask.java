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
@Slf4j
@AllArgsConstructor
public class AudioTask {

    private final QueueService queueService;
    private final GridFsService gridFsService;
    private final MongoDbService mongoDbService;

    @Scheduled(fixedRate = 2, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void pollAudio() {
        var optionalAudio = queueService.popAudioQueue();
        if (optionalAudio.isEmpty()) {
            return;
        }
        var audio = optionalAudio.get();
        log.info("Processing - {}", audio.buildFileName());
        var mediaId = gridFsService.store(audio);
        mongoDbService.saveAudioToVariety(mediaId, audio);
    }

}
