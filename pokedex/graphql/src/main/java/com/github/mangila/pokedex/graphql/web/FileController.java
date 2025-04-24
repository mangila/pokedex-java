package com.github.mangila.pokedex.graphql.web;

import com.github.mangila.pokedex.graphql.service.GridFsService;
import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final GridFsService gridFsService;

    public FileController(GridFsService gridFsService) {
        this.gridFsService = gridFsService;
    }

    @GetMapping(value = "{fileName}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable @Pattern(regexp = "^.*-.*\\.(png|jpg|ogg|gif|svg)$") String fileName,
            @RequestParam(name = "download", required = false) boolean download
    ) throws IOException {
        logger.debug("Serving file: fileName={}, download={}", fileName, download);

        var optionalResource = gridFsService.findByFileName(fileName);
        if (optionalResource.isEmpty()) {
            logger.warn("File not found: {}", fileName);
            return ResponseEntity.notFound().build();
        }

        var resource = optionalResource.get();
        var fileInfo = resource.getGridFSFile();

        logger.info("Serving file: name={}, size={}, contentType={}", 
                   fileInfo.getFilename(), 
                   fileInfo.getLength(), 
                   getContentType(fileInfo));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(fileInfo.getFilename(), download))
                .contentType(MediaType.parseMediaType(getContentType(fileInfo)))
                .contentLength(fileInfo.getLength())
                .lastModified(fileInfo.getUploadDate().getTime())
                .cacheControl(download ? CacheControl.noStore() : CacheControl.maxAge(Duration.ofHours(1)))
                .body(new InputStreamResource(resource.getInputStream()));
    }

    private static String getContentType(GridFSFile file) {
        logger.trace("Getting content type for file: {}", file.getFilename());
        if (Objects.nonNull(file.getMetadata())) {
            String contentType = file.getMetadata().getString("_contentType");
            logger.trace("Found content type in metadata: {}", contentType);
            return contentType;
        }

        logger.trace("No metadata or content type found, using default: {}", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private static String buildContentDisposition(String filename, boolean isDownload) {
        logger.trace("Building content disposition for file: filename={}, isDownload={}", filename, isDownload);
        var contentDisposition = isDownload ? "attachment;" : "inline;";

        String result = new StringBuilder()
                .append(contentDisposition)
                .append(" ")
                .append("filename=")
                .append("\"")
                .append(filename)
                .append("\"")
                .toString();

        logger.trace("Built content disposition: {}", result);
        return result;
    }

}
