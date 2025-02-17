package com.github.mangila.fileserver.controller;

import com.github.mangila.fileserver.service.GridFsService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/file")
@AllArgsConstructor
public class FileController {

    private final GridFsService gridFsService;

    @SneakyThrows({IOException.class})
    @GetMapping(value = "{mediaId}")
    public ResponseEntity<Resource> serveFile(@PathVariable String mediaId) {
        var resource = gridFsService.find(mediaId);
        if (Objects.isNull(resource)) {
            return ResponseEntity.notFound().build();
        }
        var fileInfo = resource.getGridFSFile();
        var stream = new InputStreamResource(resource.getInputStream());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(fileInfo.getFilename()))
                .contentType(MediaType.parseMediaType(getContentType(fileInfo)))
                .contentLength(fileInfo.getLength())
                .lastModified(fileInfo.getUploadDate().getTime())
                .cacheControl(CacheControl.noCache())
                .body(stream);
    }

    private static String getContentType(GridFSFile file) {
        if (Objects.nonNull(file.getMetadata())) {
            return file.getMetadata().getString("_contentType");
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private static String buildContentDisposition(String filename) {
        return new StringBuilder()
                .append("inline;")
                .append(" ")
                .append("filename=")
                .append("\"")
                .append(filename)
                .append("\"")
                .toString();
    }

}
