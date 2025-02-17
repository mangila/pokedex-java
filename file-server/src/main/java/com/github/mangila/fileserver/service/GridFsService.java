package com.github.mangila.fileserver.service;

import com.github.mangila.fileserver.config.RedisConfig;
import com.github.mangila.repository.GridFsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class GridFsService {

    private final GridFsRepository gridFsRepository;

    @Cacheable(value = RedisConfig.FILE_SERVER, key = "#mediaId")
    public Optional<GridFsResource> find(ObjectId mediaId) {
        return gridFsRepository.find(mediaId);
    }
}
