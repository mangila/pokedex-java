package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.DatabaseFileName;
import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.database.internal.file.header.FileHeaderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32C;

/**
 * File Structure Layout for Pokemon Data
 * <p>
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "Pok3mon1" bytes  | File identifier|
 * | Version        | 4 bytes           | Format version |
 * | Record Count   | 4 bytes           | Num records    |
 * | Next Offset    | 8 bytes           | Write position |
 * +----------------+--------------------+----------------+
 * </pre>
 * <p>
 * Data Records Section:
 * <pre>
 * +-----------------+----------------------+
 * | Record Length   | 4 bytes             |
 * | Pokemon Data    | Variable length     |
 * | CRC32C Checksum | 8 bytes            |
 * +-----------------+----------------------+
 * </pre>
 * <p>
 * Records are stored sequentially with CRC32C checksums for data integrity.
 */
public class DataFileHandler<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(DataFileHandler.class);

    private final ThreadLocal<CRC32C> crc32CThreadLocal = ThreadLocal.withInitial(CRC32C::new);
    private final FileHeaderHandler fileHeaderHandler;
    private final File file;

    public DataFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".data")
                .concat(".yakvs");
        this.file = new File(new DatabaseFileName(fileName));
        this.fileHeaderHandler = new FileHeaderHandler(file);
        if (file.isEmpty()) {
            try {
                fileHeaderHandler.writeHeaderToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeNewRecord(String key, V value) throws IOException {
        var record = DataRecord.from(value.serialize(), crc32CThreadLocal.get());
        var size = record.getSize();
        var offset = getOffset();
        var buffer = file.getFileRegion(
                FileChannel.MapMode.READ_WRITE,
                offset,
                size
        );
        record.fill(buffer);
        buffer.force();
        long newOffset = offset + size;
        log.debug("Inserted record {} at {} -- new offset {}", key, offset, newOffset);
        fileHeaderHandler.updateNewWrite(newOffset);
    }

    public long getOffset() {
        return fileHeaderHandler.getOffset();
    }

    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            fileHeaderHandler.writeHeaderToFile();
        }
    }

    public void deleteFile() throws IOException {
        log.info("Deleting file {}", file.getPath().getFileName());
        file.deleteFile();
    }

    public byte[] read(long offset) throws IOException {
        offset = offset + 4;
        var buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset,
                Integer.BYTES
        );
        var length = buffer.getInt();
        buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset,
                length
        );
        var data = new byte[length];
        buffer.get(data);
        buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset,
                Long.BYTES
        );
        var checksum = buffer.getLong();
        return data;
    }
}
