package com.github.mangila.pokedex.api.file.controller;

import com.github.mangila.pokedex.api.file.service.GridFsService;
import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.validation.constraints.Pattern;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/file")
@lombok.AllArgsConstructor
public class FileController {

    private final GridFsService gridFsService;

    @SneakyThrows({IOException.class})
    @GetMapping(value = "{fileName}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable @Pattern(regexp = "^.*-.*\\.(png|jpg|ogg)$") String fileName,
            @RequestParam(name = "download", required = false) boolean download
    ) {
        var optionalResource = gridFsService.findByFileName(fileName);
        if (optionalResource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var resource = optionalResource.get();
        var fileInfo = resource.getGridFSFile();
        var stream = new InputStreamResource(resource.getInputStream());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(fileInfo.getFilename(), download))
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

    private static String buildContentDisposition(String filename, boolean isDownload) {
        var contentDisposition = isDownload ? "attachment;" : "inline;";
        return new StringBuilder()
                .append(contentDisposition)
                .append(" ")
                .append("filename=")
                .append("\"")
                .append(filename)
                .append("\"")
                .toString();
    }

}
