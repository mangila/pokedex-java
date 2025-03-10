package com.github.mangila.pokedex.backstage.image.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class MediaTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(MediaTask.class);

    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public MediaTask(MongoBouncerClient mongoBouncerClient,
                     RedisBouncerClient redisBouncerClient) {
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void run(String[] args) {
        var message = redisBouncerClient.streamOps()
                .readOne(StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                        .build());
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages found");
            return;
        }
    }
}
