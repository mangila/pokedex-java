package com.github.mangila.pokedex.backstage.image.task;

import com.github.mangila.pokedex.backstage.shared.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageTask implements Task {

    Logger log = LoggerFactory.getLogger(ImageTask.class);

    @Override
    public void run() {
        log.info("Starting Image Task");
    }
}
