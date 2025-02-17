package com.github.mangila.fileserver.service;

import com.github.mangila.repository.GridFsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class GridFsService {

    private final GridFsRepository gridFsRepository;

    public Optional<GridFsResource> findByFileName(String fileName) {
        return gridFsRepository.findByFileName(fileName);
    }
}
