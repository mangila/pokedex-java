package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32C;

import static com.github.mangila.pokedex.shared.database.internal.file.FileHeader.HEADER_SIZE;
import static com.github.mangila.pokedex.shared.database.internal.file.FileHeader.VERSION;
import static java.nio.channels.FileChannel.MapMode;

public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final AtomicInteger version = new AtomicInteger(0);
    private final AtomicInteger recordCount = new AtomicInteger(0);
    private final AtomicLong indexOffset = new AtomicLong(0);
    private final Map<String, Long> indexOffsets = new ConcurrentHashMap<>();
    private final AtomicLong dataOffset = new AtomicLong(0);
    private final ThreadLocal<CRC32C> crc32c = ThreadLocal.withInitial(CRC32C::new);
    private final File file;

    public FileHandler(File file) {
        this.file = file;
        try {
            if (file.isEmpty()) {
                init();
            } else {
                load();
            }
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public long write(String key, Pokemon pokemon) throws IOException {
        if (indexOffsets.containsKey(key)) {
            // update
        } else {
            // insert
            FileSection.writeRecord();
            FileSection.writeIndex();
            FileHeader.updateDataOffset(null, 0L);
            FileHeader.updateIndexOffset(null, 0L);
            FileHeader.updateRecordCount(null, 0);
        }
        return -1L;
    }

    public Pokemon read(String key) throws IOException {
        if (!indexOffsets.containsKey(key)) {
            return null;
        }
        return new Pokemon(1, "");
    }

    public void deleteFile() {
        try {
            log.info("Deleting file {}", file.getPath().getFileName());
            file.deleteFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException {
        var buffer = file.getFileRegion(
                MapMode.READ_WRITE,
                0,
                HEADER_SIZE
        );
        FileHeader.writeHeader(
                buffer,
                VERSION,
                0,
                HEADER_SIZE,
                HEADER_SIZE
        );
        buffer.force();
        log.info("Initialized file {} with version {}",
                file.getPath().getFileName(),
                VERSION);
    }

    private void load() throws IOException {
        var buffer = file.getFileRegion(
                MapMode.READ_ONLY,
                0,
                HEADER_SIZE
        );
        if (!FileHeader.isHeaderValid(buffer)) {
            throw new IllegalStateException("Invalid file header");
        }
        this.version.set(FileHeader.readVersion(buffer));
        this.recordCount.set(FileHeader.readRecordCount(buffer));
        this.indexOffset.set(FileHeader.readIndexOffset(buffer));
        this.dataOffset.set(FileHeader.readDataOffset(buffer));
        this.indexOffsets.putAll(FileSection.loadIndexes(file,
                indexOffset.get(),
                dataOffset.get(),
                recordCount.get()));
        log.info("Loaded file {} with version {} with record count {}",
                file.getPath().getFileName(),
                version.get(),
                recordCount.get());
    }
}
