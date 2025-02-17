package com.github.mangila.fileserver.service;

import com.github.mangila.repository.GridFsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GridFsService {

    private final GridFsRepository gridFsRepository;

    public GridFsResource find(String mediaId) {
        if (ObjectId.isValid(mediaId)) {
            var id = new ObjectId(mediaId);
            return gridFsRepository.find(id);
        }
        return null;
    }
}
