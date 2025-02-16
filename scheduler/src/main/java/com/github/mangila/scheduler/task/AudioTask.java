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
@Slf4j
@AllArgsConstructor
public class AudioTask {

    private final QueueService queueService;
    private final GridFsService gridFsService;

    @Scheduled(fixedRate = 10, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void pollAudio() {
        var audio = queueService.popAudioQueue();
        if (Objects.isNull(audio)) {
            return;
        }
        log.info("Processing - {}", audio.buildFileName());
        var k = gridFsService.store(audio);
    }

}
