package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WalFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFile.class);
    private static final Set<StandardOpenOption> OPEN_OPTIONS = Set.of(
            StandardOpenOption.WRITE,
            StandardOpenOption.READ,
            StandardOpenOption.SYNC);
    private static final byte[] MAGIC_NUMBER = "WAL".getBytes(Charset.defaultCharset());
    private final Path path;
    private final ExecutorService walChannelExecutor;
    private final WalTable walTable;
    private WalFileChannel channel;
    private AtomicLong position = new AtomicLong(0);
    private AtomicLong appendCount = new AtomicLong(0);
    private AtomicReference<WalFileStatus> status = new AtomicReference<>(WalFileStatus.CLOSED);

    public WalFile(Path path) {
        Ensure.notNull(path, "Path must not be null");
        this.path = path;
        this.walChannelExecutor = VirtualThreadFactory.newFixedThreadPool(10);
        this.walTable = new WalTable(new ConcurrentSkipListMap<>(Comparator.comparing(HashKey::value)));
    }

    public void open() throws IOException {
        LOGGER.info("Opening WAL file {}", path);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, MAGIC_NUMBER);
            LOGGER.info("Created new WAL file {}", path);
        }
        channel = new WalFileChannel(
                AsynchronousFileChannel.open(path,
                        OPEN_OPTIONS,
                        walChannelExecutor)
        );
        position.set(getSize());
        status.set(WalFileStatus.OPEN);
        load();
    }

    private void load() throws IOException {
        long size = getSize();
        if (size == 0) {
            throw new IllegalStateException("WAL file is empty");
        }
        if (size == MAGIC_NUMBER.length) {
            return;
        }
        Buffer readBuffer = Buffer.from((int) getSize());
        CompletableFuture<WalAppendStatus> readFuture = new CompletableFuture<>();
        channel.read(new WalFileChannel.Attachment(readFuture, 0, readBuffer.remaining(), readBuffer));
        readFuture.join();
        readBuffer.flip();
        byte[] magicNumber = readBuffer.getArray(MAGIC_NUMBER.length);
        Ensure.equals(magicNumber, MAGIC_NUMBER);
        var bytes = readBuffer.getArray();
        System.out.println(readBuffer);
    }

    public void close() throws IOException {
        LOGGER.info("Closing WAL file {}", path);
        channel.close();
        VirtualThreadFactory.terminateGracefully(walChannelExecutor, Duration.ofSeconds(30));
        status.set(WalFileStatus.CLOSED);
    }

    public void delete() throws IOException {
        LOGGER.info("Deleting WAL file {}", path);
        close();
        position.set(0);
        appendCount.set(0);
        Files.delete(path);
    }

    public long size() throws IOException {
        return Files.size(path);
    }

    public WalFileChannel channel() {
        return channel;
    }

    public WalTable walTable() {
        return walTable;
    }

    public AtomicReference<WalFileStatus> status() {
        return status;
    }

    public AtomicLong position() {
        return position;
    }

    public AtomicLong appendCount() {
        return appendCount;
    }

    public Path getPath() {
        return path;
    }

    public long getRotation() {
        String name = path.getFileName().toString();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(name.substring(name.lastIndexOf("-") + 1));
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        throw new IllegalStateException("No rotation number found in file name: " + name);
    }

    public long getSize() throws IOException {
        return Files.size(path);
    }
}
