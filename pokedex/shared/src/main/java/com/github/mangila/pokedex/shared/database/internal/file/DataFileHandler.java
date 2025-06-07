package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;

import java.io.IOException;
import java.nio.file.Path;

/**
 * File Structure Layout for Data
 * <p>
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvs" bytes     | File identifier|
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
 * | Data            | Variable length     |
 * +-----------------+----------------------+
 * </pre>
 * <p>
 * Records are stored sequentially
 */
public class DataFileHandler {

    private final File file;
    private FileHeader header;

    public DataFileHandler(DatabaseName databaseName) {
        String fileName = databaseName.value()
                .concat(".data")
                .concat(".yakvs");
        this.file = new File(new FileName(fileName));
        this.header = FileHeader.defaultValue();
    }

    public DataFileHandler(File file) {
        this.file = file;
        this.header = FileHeader.defaultValue();
    }

    /**
     * Initializes the data file. If the file does not exist, it will be created. If the file is empty,
     * a default header will be written to it. Otherwise, the method will read and parse the existing header from the file.
     *
     * @throws IOException if there is an issue, creating the file, reading from it, or writing to it.
     */
    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            file.write(header.toByteBuffer(), 0);
        } else {
            var buffer = file.readAndFlip(0, FileHeader.HEADER_SIZE);
            this.header = FileHeader.from(buffer);
        }
    }

    public OffsetBoundary write(DataRecord record) throws IOException {
        long offset = header.offset();
        int size = record.getSize();
        write(record, offset);
        long newOffset = offset + size;
        return OffsetBoundary.from(offset, newOffset);
    }

    public DataRecord read(long dataOffset) throws IOException {
        var buffer = file.readAndFlip(dataOffset, Integer.BYTES);
        var length = buffer.getInt();
        buffer = file.readAndFlip(dataOffset + Integer.BYTES, length);
        var data = buffer.array();
        return DataRecord.from(data);
    }

    public void updateHeader(long newOffset) throws IOException {
        header = header.updateOffset(newOffset);
        file.write(header.toByteBuffer(), 0);
    }

    public void deleteFile() throws IOException {
        file.tryDeleteFile();
    }

    public void truncate() throws IOException {
        file.truncate();
        header = FileHeader.defaultValue();
        file.write(header.toByteBuffer(), 0);
    }

    public Path getPath() {
        return file.getPath();
    }

    private void write(DataRecord record, long dataOffset) throws IOException {
        file.write(record.toByteBuffer(), dataOffset);
    }

    public void closeFileChannels() {
        file.closeChannels();
    }

    public long getFileSize() {
        return file.getFileSize();
    }
}
